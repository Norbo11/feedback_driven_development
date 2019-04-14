package np1815.feedback.metricsbackend.api.impl;

import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.NotFoundException;
import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.metricsbackend.persistance.MetricsBackendOperations;
import np1815.feedback.metricsbackend.profile.Profile;
import np1815.feedback.metricsbackend.profile.parsing.FlaskPyflameParser;
import np1815.feedback.metricsbackend.util.DateTimeUtil;

import java.time.Duration;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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

        int addedProfileId = metricsBackendOperations.addProfile(
            DateTimeUtil.dateTimeToTimestamp(pyflameProfile.getStartTimestamp()),
            DateTimeUtil.dateTimeToTimestamp(pyflameProfile.getEndTimestamp()),
            duration.toMillis(),
            pyflameProfile.getVersion());

        //TODO: Get the paths to store in DB from project configuration
        for (ProfiledLine line : profile.getAllLineProfilesStartingWith("playground_application")) {
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

        return Response.ok().entity(new AddedEntityResponse().id(addedProfileId)).build();
    }

    @Override
    public Response getApplicationVersions(SecurityContext securityContext) throws NotFoundException {
        Set<String> versions = metricsBackendOperations.getApplicationVersions();
        return Response.ok().entity(new AllApplicationVersions().versions(new ArrayList<>(versions))).build();
    }

    @Override
    public Response getPerformanceForFile(String filename, String version, SecurityContext securityContext) throws NotFoundException {
        Map<String, PerformanceForFileLines> lines = metricsBackendOperations.getGlobalAveragePerLine(filename, version);

        double globalAverageForFile = lines.values().stream().mapToDouble(PerformanceForFileLines::getGlobalAverage).sum();

        return Response.ok().entity(new PerformanceForFile()
            .lines(lines)
            .globalAverageForFile(globalAverageForFile)).build();
    }

}
