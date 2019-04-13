package np1815.feedback.metricsbackend.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import np1815.feedback.metricsbackend.api.impl.DslContextFactory;

public class DatabaseModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    DslContextFactory providesDSLContext() {
        return new DslContextFactory();
    }
}
