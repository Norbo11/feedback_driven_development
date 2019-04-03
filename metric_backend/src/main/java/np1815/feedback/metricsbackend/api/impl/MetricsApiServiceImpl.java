package np1815.feedback.metricsbackend.api.impl;

import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.ApiResponseMessage;
import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.NotFoundException;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.metricsbackend.model.PyflameProfile;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

import java.util.Map;

import np1815.feedback.metricsbackend.util.LineProfile;
import np1815.feedback.metricsbackend.util.ParsedPyflameProfile;
import np1815.feedback.metricsbackend.util.PyflameParser;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

import static np1815.feedback.metricsbackend.db.metrics.tables.Performance.*;
import static np1815.feedback.metricsbackend.db.requests.tables.Profile.*;
import static np1815.feedback.metricsbackend.db.requests.tables.ProfileLines.*;

public class MetricsApiServiceImpl extends MetricsApiService {
    private String userName = "metric_backend";
    private String password = "imperial";
    private String url = "jdbc:postgresql://cloud-vm-46-203.doc.ic.ac.uk:5432/feedback_driven_development";

    @Override
    public Response addPyflameProfile(PyflameProfile pyflameProfile, SecurityContext securityContext) throws NotFoundException {
        Connection conn = null;

        try {
            System.out.println(pyflameProfile.getPyflameOutput());

            PyflameParser parser = new PyflameParser();
            ParsedPyflameProfile parsed = parser.parseFlamegraph(pyflameProfile.getPyflameOutput());

            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, userName, password);
            DSLContext jooq = DSL.using(conn);

            Duration duration = Duration.between(pyflameProfile.getStartTimestamp(), pyflameProfile.getEndTimestamp());

            Record1<Integer> id = jooq.insertInto(PROFILE)
                    .columns(PROFILE.DURATION, PROFILE.START_TIMESTAMP, PROFILE.END_TIMESTAMP)
                    .values(duration.toMillis(),
                            dateTimeToTimestamp(pyflameProfile.getStartTimestamp()),
                            dateTimeToTimestamp(pyflameProfile.getEndTimestamp()))
                    .returningResult(PROFILE.ID).fetchOne();

            for (LineProfile line : parsed.getProfiles().values()) {
                jooq.insertInto(PROFILE_LINES)
                        .columns(PROFILE_LINES.PROFILE_ID,
                                 PROFILE_LINES.FILE_NAME,
                                 PROFILE_LINES.SAMPLES)
                        .values(id.getValue(PROFILE.ID),
                                line.getFilePath(),
                                line.getNumberOfSamples())
                        .execute();
            }

            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "added")).build();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "error occurred")).build();
    }

    private static Timestamp dateTimeToTimestamp(LocalDateTime startTimestamp) {
        return Timestamp.from(startTimestamp.toInstant(ZoneOffset.UTC));
    }

    @Override
    public Response getPerformanceForFile( @NotNull String filename, SecurityContext securityContext) throws NotFoundException {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url, userName, password);
            DSLContext jooq = DSL.using(conn);

            System.out.println(filename);
            Record1<Double> result = jooq.select(PERFORMANCE.AVERAGE_PERFORMANCE).from(PERFORMANCE).where(PERFORMANCE.FILE_NAME.eq(filename)).fetchOne();
            System.out.println(result);
            Double performance = result.getValue(PERFORMANCE.AVERAGE_PERFORMANCE);
            Map<String, PerformanceForFileLines> map = new HashMap<>();
            map.put("1", new PerformanceForFileLines().globalAverage(performance.toString()));

            return Response.ok().entity(new PerformanceForFile().lines(map)).build();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "error occurred")).build();
    }
}
