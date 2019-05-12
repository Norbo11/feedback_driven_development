package np1815.feedback.metricsbackend.api.impl;

import com.google.common.collect.Sets;
import np1815.feedback.metricsbackend.api.ApiResponseMessage;
import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.NotFoundException;
import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.metricsbackend.persistance.MetricsBackendOperations;
import np1815.feedback.metricsbackend.persistance.models.LineGlobalPerformance;
import np1815.feedback.metricsbackend.profile.Profile;
import np1815.feedback.metricsbackend.profile.parsing.FlaskPyflameParser;

import java.nio.file.Paths;
import java.time.Duration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import np1815.feedback.metricsbackend.profile.ProfiledLine;
import np1815.feedback.metricsbackend.util.PathUtil;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class MetricsApiServiceImpl extends MetricsApiService {

    private final MetricsBackendOperations metricsBackendOperations;

    public MetricsApiServiceImpl(MetricsBackendOperations metricsBackendOperations) {
        this.metricsBackendOperations = metricsBackendOperations;
    }

    @Override
    public Response addPyflameProfile(PyflameProfile pyflameProfile, SecurityContext securityContext) throws NotFoundException {
        FlaskPyflameParser parser = new FlaskPyflameParser();
        Profile profile = parser.parseFlamegraph(pyflameProfile.getPyflameOutput(), pyflameProfile.getBasePath());
        Duration duration = Duration.between(pyflameProfile.getStartTimestamp(), pyflameProfile.getEndTimestamp());

        metricsBackendOperations.addApplicationIfDoesntExist(pyflameProfile.getApplicationName());

        LocalDateTime addedProfileId = metricsBackendOperations.addProfile(
            pyflameProfile.getApplicationName(),
            pyflameProfile.getVersion(),
            pyflameProfile.getStartTimestamp(),
            pyflameProfile.getEndTimestamp(),
            duration.toMillis()
        );

        for (ProfiledLine line : profile.getAllLineProfilesMatchingGlobs(pyflameProfile.getInstrumentDirectories())) {
            double fractionSpent = (line.getNumberOfSamples() / (double) profile.getTotalSamples());
            long sampleTime = Math.round(fractionSpent * duration.toMillis());

            metricsBackendOperations.addProfileLine(
                addedProfileId,
                line.getFilePath(),
                line.getLineNumber() - 1, // Pyflame starts counting lines from 1, IntelliJ counts from 0, want DB to be consistent with IntelliJ,
                line.getNumberOfSamples(),
                sampleTime,
                line.getFunction()
            );
        }

        if (pyflameProfile.getException() != null) {
            int addedExceptionId = metricsBackendOperations.addException(
                addedProfileId,
                pyflameProfile.getException().getExceptionType(),
                pyflameProfile.getException().getExceptionMessage()
            );

            Integer addedFrameId = null;
            for (NewLineExceptionFrames frame : pyflameProfile.getException().getFrames()) {
                addedFrameId = metricsBackendOperations.addExceptionFrame(
                    addedExceptionId,
                    Paths.get(pyflameProfile.getBasePath()).relativize(Paths.get(frame.getFilename())).toString(),
                    frame.getLineNumber() - 1,
                    frame.getFunctionName(),
                    addedFrameId
                );
            }
        }

        for (NewLogRecord newLogRecord : pyflameProfile.getLoggingLines()) {
            LogRecord record = newLogRecord.getLogRecord();
            String relativePath = Paths.get(pyflameProfile.getBasePath()).relativize(Paths.get(newLogRecord.getFilename())).toString();
            if (PathUtil.pathMatchesAnyGlob(relativePath, pyflameProfile.getInstrumentDirectories())) {
                metricsBackendOperations.addLoggingLine(
                    addedProfileId,
                    relativePath,
                    newLogRecord.getLineNumber() - 1,
                    record.getLogger(),
                    record.getLevel(),
                    record.getMessage(),
                    record.getLogTimestamp()
                    );
            }
        }

        return Response.ok().entity(new AddedEntityResponse().id(addedProfileId)).build();
    }

    @Override
    public Response getApplicationVersions(String applicationName, SecurityContext securityContext) throws NotFoundException {
        Set<String> versions = metricsBackendOperations.getApplicationVersions(applicationName);
        return Response.ok().entity(new AllApplicationVersions().versions(new ArrayList<>(versions))).build();
    }

    @Override
    public Response getFeedbackForFile(String applicationName, String version, String filename, String historySinceType, LocalDateTime historySinceDateTime, SecurityContext securityContext) throws NotFoundException {
        Map<Integer, LineGeneral> general = metricsBackendOperations.getGeneralFeedbackForLines(applicationName, version, filename);
        Map<Integer, LineGlobalPerformance> performance = metricsBackendOperations.getGlobalPerformanceForLines(applicationName, version, filename);
        Map<Integer, List<LinePerformanceRequestProfileHistory>> performanceHistory;

        if (historySinceType.equals(FeedbackFilterOptions.HistorySinceTypeEnum.BEGINNING_OF_VERSION.toString())) {
            performanceHistory = metricsBackendOperations.getPerformanceHistoryForLines(applicationName, version, filename);
        } else if (historySinceType.equals(FeedbackFilterOptions.HistorySinceTypeEnum.DATE_TIME.toString())) {
            performanceHistory = metricsBackendOperations.getPerformanceHistoryForLines(applicationName, version, filename, historySinceDateTime);
        } else {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Unknown filter parameter: " + historySinceType))
                .build();
        }

        Map<Integer, List<LineException>> exceptions = metricsBackendOperations.getExceptionsFeedbackForLines(applicationName, version, filename);
        Map<Integer, List<LogRecord>> loggingRecords = metricsBackendOperations.getLoggingRecordsForLines(applicationName, version, filename);

        double globalAverageForFile = performance.values().stream().mapToDouble(LineGlobalPerformance::getAvg).sum();

        Set<Integer> allLineNumbers = Sets.union(loggingRecords.keySet(), Sets.union(general.keySet(), Sets.union(performance.keySet(), exceptions.keySet())));

        Map<String, FileFeedbackLines> lines = allLineNumbers.stream()
            .collect(Collectors.toMap(
                k -> k.toString(),
                k -> new FileFeedbackLines()
                    .general(general.getOrDefault(k, new LineGeneral().executionCount(0)))
                    .performance(new LinePerformance()
                        .globalAverage(performance.containsKey(k) ? performance.get(k).getAvg() : null)
                        .status(performance.containsKey(k) ? performance.get(k).getStatus() : LinePerformance.StatusEnum.NOT_PROFILED)
                        .requestProfileHistory(performanceHistory.getOrDefault(k, new ArrayList<>())))
                    .exceptions(exceptions.getOrDefault(k, new ArrayList<>()))
                    .logging(loggingRecords.getOrDefault(k, new ArrayList<>()))
        ));

        return Response.ok().entity(new FileFeedback()
            .lines(lines)
            .globalAverageForFile(globalAverageForFile)
        ).build();
    }

}
