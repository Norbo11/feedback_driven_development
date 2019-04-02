package org.openapitools.api.impl;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.openapitools.api.*;
import org.openapitools.model.*;

import org.openapitools.model.PerformanceForFile;
import org.openapitools.model.PyflameProfile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import org.openapitools.api.NotFoundException;

import java.io.InputStream;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import uk.ac.ic.doc.np1815.LineProfile;
import uk.ac.ic.doc.np1815.ParsedPyflameProfile;
import uk.ac.ic.doc.np1815.PyflameParser;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

import static uk.ac.ic.doc.np1815.metricsbackend.db.metrics.tables.Performance.*;
import static uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.Profile.*;
import static uk.ac.ic.doc.np1815.metricsbackend.db.requests.tables.ProfileLines.*;

public class MetricsApiServiceImpl extends MetricsApiService {
    private String userName = "metric_backend";
    private String password = "imperial";
    private String url = "jdbc:postgresql:feedback_driven_development";

    @Override
    public Response addPyflameProfile(PyflameProfile pyflameProfile, SecurityContext securityContext) throws NotFoundException {
        Connection conn = null;

        try {
            System.out.println(pyflameProfile.getPyflameOutput());

            PyflameParser parser = new PyflameParser();
            ParsedPyflameProfile parsed = parser.parseFlamegraph(pyflameProfile.getPyflameOutput());

            conn = DriverManager.getConnection(url, userName, password);
            DSLContext jooq = DSL.using(conn);

            double duration = pyflameProfile.getEndTimestamp() - pyflameProfile.getStartTimestamp();

            Record1<Integer> id = jooq.insertInto(PROFILE)
                    .columns(PROFILE.DURATION, PROFILE.START_TIMESTAMP, PROFILE.END_TIMESTAMP)
                    .values(duration, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()))
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
