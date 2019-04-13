package np1815.feedback.metricsbackend.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import np1815.feedback.metricsbackend.api.impl.DslContextFactory;
import np1815.feedback.metricsbackend.api.impl.MetricsApiServiceImpl;
import np1815.feedback.metricsbackend.persistance.MetricsBackendOperations;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    MetricsApiServiceImpl providesMetricsApiService(MetricsBackendOperations metricsBackendOperations) {
        return new MetricsApiServiceImpl(metricsBackendOperations);
    }
}
