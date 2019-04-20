package np1815.feedback.metricsbackend.api.impl;

import com.google.common.collect.Sets;
import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.NotFoundException;
import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.metricsbackend.persistance.MetricsBackendOperations;
import np1815.feedback.metricsbackend.profile.Profile;
import np1815.feedback.metricsbackend.profile.parsing.FlaskPyflameParser;
import np1815.feedback.metricsbackend.util.DateTimeUtil;

import java.nio.file.Paths;
import java.time.Duration;

import java.util.*;
import java.util.stream.Collectors;

import np1815.feedback.metricsbackend.profile.ProfiledLine;

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

        int addedProfileId = metricsBackendOperations.addProfile(
            pyflameProfile.getApplicationName(),
            pyflameProfile.getVersion(),
            DateTimeUtil.dateTimeToTimestamp(pyflameProfile.getStartTimestamp()),
            DateTimeUtil.dateTimeToTimestamp(pyflameProfile.getEndTimestamp()),
            duration.toMillis()
        );

        for (ProfiledLine line : profile.getAllLineProfilesMatchingGlobs(pyflameProfile.getInstrumentDirectories())) {
            double fractionSpent = (line.getNumberOfSamples() / (double) profile.getTotalSamples());
            long sampleTime = Math.round(fractionSpent * duration.toMillis());

            metricsBackendOperations.addProfileLine(
                addedProfileId,
                line.getFilePath(),
                line.getLineNumber() - 1,
                line.getNumberOfSamples(),
                sampleTime
            );
        }

        if (pyflameProfile.getException() != null) {
            int addedExceptionId = metricsBackendOperations.addException(
                addedProfileId,
                pyflameProfile.getException().getExceptionType(),
                pyflameProfile.getException().getExceptionMessage()
            );

            Integer addedFrameId = null;
            for (NewExceptionFrames frame : pyflameProfile.getException().getFrames()) {
                addedFrameId = metricsBackendOperations.addExceptionFrame(
                    addedExceptionId,
                    Paths.get(pyflameProfile.getBasePath()).relativize(Paths.get(frame.getFilename())).toString(),
                    frame.getLineNumber(),
                    frame.getFunctionName(),
                    addedFrameId
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
    public Response getPerformanceForFile(String applicationName, String version, String filename, SecurityContext securityContext) throws NotFoundException {
        Map<Integer, FilePerformance> performance = metricsBackendOperations.getGlobalAveragePerLine(applicationName, version, filename);
        Map<Integer, List<FileException>> exceptions = metricsBackendOperations.getExceptionsForLine(applicationName, version, filename);

        double globalAverageForFile = performance.values().stream().mapToDouble(FilePerformance::getGlobalAverage).sum();

        Map<String, PerformanceForFileLines> lines = Sets.union(performance.keySet(), exceptions.keySet()).stream().collect(Collectors.toMap(
            k -> k.toString(),
            k -> new PerformanceForFileLines()
                .performance(performance.get(k))
                .exceptions(exceptions.get(k))
        ));

        return Response.ok().entity(new PerformanceForFile()
            .lines(lines)
            .globalAverageForFile(globalAverageForFile)
        ).build();
    }

}
