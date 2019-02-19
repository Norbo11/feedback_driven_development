package org.openapitools.api;

import org.openapitools.api.*;
import org.openapitools.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.openapitools.model.PerformanceForFile;
import org.openapitools.model.PyflameProfile;

import java.util.List;
import org.openapitools.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-02-19T01:29:50.436168Z[Europe/London]")
public abstract class MetricsApiService {
    public abstract Response addPyflameProfile(PyflameProfile pyflameProfile,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getPerformanceForFile( @NotNull String filename,SecurityContext securityContext) throws NotFoundException;
}
