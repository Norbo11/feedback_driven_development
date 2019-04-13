package np1815.feedback.metricsbackend.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import np1815.feedback.metricsbackend.api.impl.DslContextFactory;
import np1815.feedback.metricsbackend.persistance.JooqMetricsBackendOperations;
import np1815.feedback.metricsbackend.persistance.MetricsBackendOperations;
import org.jooq.DSLContext;

public class PersistanceModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    public MetricsBackendOperations providesMetricBackendOperations(DslContextFactory dslContextFactory) {
        return new JooqMetricsBackendOperations(dslContextFactory);
    }
}
