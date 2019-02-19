package org.openapitools.api.factories;

import org.openapitools.api.MetricsApiService;
import org.openapitools.api.impl.MetricsApiServiceImpl;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-02-19T01:29:50.436168Z[Europe/London]")
public class MetricsApiServiceFactory {
    private final static MetricsApiService service = new MetricsApiServiceImpl();

    public static MetricsApiService getMetricsApi() {
        return service;
    }
}
