package np1815.feedback.metricsbackend.api.impl;

import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.ApiResponseMessage;
import np1815.feedback.metricsbackend.api.NotFoundException;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.metricsbackend.model.PyflameProfile;
import np1815.feedback.metricsbackend.profile.Profile;
import np1815.feedback.metricsbackend.profile.parsing.FlaskPyflameParser;
import org.jooq.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Map;
import java.util.stream.Collectors;

import np1815.feedback.metricsbackend.profile.ProfiledLine;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

import static np1815.feedback.metricsbackend.db.requests.tables.Profile.*;
import static np1815.feedback.metricsbackend.db.requests.tables.ProfileLines.*;

import static org.jooq.impl.DSL.*;

public class MetricsApiServiceImpl extends MetricsApiService {

    private final DslContextFactory dslContextFactory;

    public MetricsApiServiceImpl(DslContextFactory dslContextFactory) {
        this.dslContextFactory = dslContextFactory;
    }

    @Override
    public Response addPyflameProfile(PyflameProfile pyflameProfile, SecurityContext securityContext) throws NotFoundException {
        FlaskPyflameParser parser = new FlaskPyflameParser();
        Profile profile = parser.parseFlamegraph(pyflameProfile.getPyflameOutput(), pyflameProfile.getBasePath());
        Duration duration = Duration.between(pyflameProfile.getStartTimestamp(), pyflameProfile.getEndTimestamp());
        DSLContext dslContext = dslContextFactory.create();

        Record1<Integer> id = dslContext.insertInto(PROFILE)
                .columns(PROFILE.DURATION,
                         PROFILE.START_TIMESTAMP,
                         PROFILE.END_TIMESTAMP,
                         PROFILE.VERSION
                        )
                .values(duration.toMillis(),
                        dateTimeToTimestamp(pyflameProfile.getStartTimestamp()),
                        dateTimeToTimestamp(pyflameProfile.getEndTimestamp()),
                        pyflameProfile.getVersion()
                )
                .returningResult(PROFILE.ID).fetchOne();

        //TODO: Get the paths to store in DB from project configuration
        for (ProfiledLine line : profile.getAllLineProfilesStartingWith("playground_application")) {
            double fractionSpent = (line.getNumberOfSamples() / (double) profile.getTotalSamples());
            long sampleTime = Math.round(fractionSpent * duration.toMillis());

            dslContext.insertInto(PROFILE_LINES)
                    .columns(PROFILE_LINES.PROFILE_ID,
                             PROFILE_LINES.FILE_NAME,
                             PROFILE_LINES.SAMPLES,
                             PROFILE_LINES.LINE_NUMBER,
                             PROFILE_LINES.SAMPLE_TIME
                            )
                    .values(id.getValue(PROFILE.ID),
                            line.getFilePath(),
                            line.getNumberOfSamples(),
                            line.getLineNumber() - 1, // Pyflame starts counting lines from 1, IntelliJ counts from 0, want DB to be consistent with IntelliJ
                            sampleTime
                            )
                    .execute();
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "added")).build();
    }

    private static Timestamp dateTimeToTimestamp(LocalDateTime startTimestamp) {
        return Timestamp.from(startTimestamp.toInstant(ZoneOffset.UTC));
    }

    @Override
    /*
     Currently a global average
     */
    public Response getPerformanceForFile( @NotNull String filename, SecurityContext securityContext) throws NotFoundException {
        Result<Record2<Integer, BigDecimal>> result = dslContextFactory.create()
            .select(PROFILE_LINES.LINE_NUMBER,
                    avg(PROFILE_LINES.SAMPLE_TIME).as("avg")
            )
            .from(PROFILE_LINES)
            .where(PROFILE_LINES.FILE_NAME.eq(filename))
            .groupBy(PROFILE_LINES.FILE_NAME, PROFILE_LINES.LINE_NUMBER)
            .fetch();

        Map<String, PerformanceForFileLines> lines = result.stream().collect(Collectors.toMap(
            x -> x.getValue(PROFILE_LINES.LINE_NUMBER).toString(),
            x -> new PerformanceForFileLines().globalAverage(x.get("avg", Double.class))
        ));

        double globalAverageForFile = lines.values().stream().mapToDouble(PerformanceForFileLines::getGlobalAverage).sum();

        PerformanceForFile entity = new PerformanceForFile()
            .lines(lines)
            .globalAverageForFile(globalAverageForFile);

        return Response.ok().entity(entity).build();
    }
}
