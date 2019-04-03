package np1815.feedback.metricsbackend.api.factories;

import com.google.inject.Guice;
import com.google.inject.Injector;
import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.api.impl.DatabaseModule;
import np1815.feedback.metricsbackend.api.impl.DslContextFactory;
import np1815.feedback.metricsbackend.api.impl.MetricsApiServiceImpl;

/* This needs to stay in this location in order to be discovered by the generated server stub!
 */

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-02-19T01:29:50.436168Z[Europe/London]")
public class MetricsApiServiceFactory {
    private static MetricsApiService service = null;

    static {
        Injector injector = Guice.createInjector(new DatabaseModule());
        DslContextFactory dslContextFactory = injector.getInstance(DslContextFactory.class);
        service = new MetricsApiServiceImpl(dslContextFactory);
    }

    public static MetricsApiService getMetricsApi() {
        return service;
    }
}
