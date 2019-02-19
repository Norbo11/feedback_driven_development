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
import java.util.HashMap;
import java.util.List;
import org.openapitools.api.NotFoundException;

import java.io.InputStream;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

import static uk.ac.ic.doc.np1815.jooq.metrics.tables.Performance.*;
import static uk.ac.ic.doc.np1815.jooq.requests.tables.Profile.*;
import static uk.ac.ic.doc.np1815.jooq.requests.tables.ProfileLines.*;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-02-19T01:29:50.436168Z[Europe/London]")
public class MetricsApiServiceImpl extends MetricsApiService {
    String userName = "metric_backend";
    String password = "imperial";
    String url = "jdbc:postgresql:feedback_driven_development";

    @Override
    public Response addPyflameProfile(PyflameProfile pyflameProfile, SecurityContext securityContext) throws NotFoundException {
        Connection conn = null;

        try {
            System.out.println(pyflameProfile.getPyflameOutput());

            conn = DriverManager.getConnection(url, userName, password);
            DSLContext jooq = DSL.using(conn);

            jooq.insertInto(PERFORMANCE).columns(PERFORMANCE.FILE_NAME, PERFORMANCE.AVERAGE_PERFORMANCE).values("test_file.py", 12.0).execute();
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
