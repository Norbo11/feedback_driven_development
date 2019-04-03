package np1815.feedback.metricsbackend.api.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class DatabaseModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    DslContextFactory providesDSLContext() {
        return new DslContextFactory();
    }
}
