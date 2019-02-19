package org.openapitools.api;

import org.openapitools.model.*;
import org.openapitools.api.MetricsApiService;
import org.openapitools.api.factories.MetricsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.openapitools.model.PerformanceForFile;
import org.openapitools.model.PyflameProfile;

import java.util.Map;
import java.util.List;
import org.openapitools.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/metrics")


@io.swagger.annotations.Api(description = "the metrics API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-02-19T01:29:50.436168Z[Europe/London]")
public class MetricsApi  {
   private final MetricsApiService delegate;

   public MetricsApi(@Context ServletConfig servletContext) {
      MetricsApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("MetricsApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (MetricsApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = MetricsApiServiceFactory.getMetricsApi();
      }

      this.delegate = delegate;
   }

    @POST
    @Path("/requests/profile/pyflame")
    @Consumes({ "application/json" })
    
    @io.swagger.annotations.ApiOperation(value = "Add a new profile using the pyflame format", notes = "", response = Void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 400, message = "Error processing", response = Void.class) })
    public Response addPyflameProfile(@ApiParam(value = "" ,required=true) @Valid PyflameProfile pyflameProfile
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addPyflameProfile(pyflameProfile,securityContext);
    }
    @GET
    @Path("/performance/for_file")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get performance information for particular file", notes = "", response = PerformanceForFile.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = PerformanceForFile.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid filename", response = Void.class) })
    public Response getPerformanceForFile(@ApiParam(value = "",required=true) @QueryParam("filename") String filename
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getPerformanceForFile(filename,securityContext);
    }
}
