package np1815.feedback.metricsbackend.api.factories;

import com.google.inject.Guice;
import com.google.inject.Injector;
import np1815.feedback.metricsbackend.api.MetricsApiService;
import np1815.feedback.metricsbackend.modules.DatabaseModule;
import np1815.feedback.metricsbackend.api.impl.DslContextFactory;
import np1815.feedback.metricsbackend.api.impl.MetricsApiServiceImpl;
import np1815.feedback.metricsbackend.modules.MainModule;
import np1815.feedback.metricsbackend.modules.PersistanceModule;
import np1815.feedback.metricsbackend.persistance.MetricsBackendOperations;

/* This needs to stay in this location in order to be discovered by the generated server stub!
 */

public class MetricsApiServiceFactory {
    private static MetricsApiService service = null;

    static {
        Injector injector = Guice.createInjector(
            new DatabaseModule(),
            new PersistanceModule(),
            new MainModule()
        );

        service =  injector.getInstance(MetricsApiServiceImpl.class);
    }

    public static MetricsApiService getMetricsApi() {
        return service;
    }
}
