package np1815.feedback.plugin.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import np1815.feedback.metricsbackend.api.DefaultApi;
import np1815.feedback.metricsbackend.client.ApiClient;
import np1815.feedback.plugin.services.MetricsBackendService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@State(name = "FeedbackDrivenDevelopment")
public class FeedbackDrivenDevelopment implements ProjectComponent, PersistentStateComponent<FeedbackDrivenDevelopment.State> {

    private final Project project;

    public static class State {
        public String feedbackConfigPath;
        public String metricBackendUrl;
        public boolean takeMetricBackendUrlFromConfig;

        // TODO: Username and password at some point. Use PasswordSafe class
        // https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_sensitive_data.html

        public State() {
            this.feedbackConfigPath = "";
            this.metricBackendUrl = "";
            this.takeMetricBackendUrlFromConfig = true;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof State) {
                State o = (State) obj;
                return o.feedbackConfigPath.equals(feedbackConfigPath)
                    && o.metricBackendUrl.equals(metricBackendUrl)
                    && o.takeMetricBackendUrlFromConfig == takeMetricBackendUrlFromConfig;
            }
            return false;
        }
    }

    private ApiClient client;
    private FeedbackConfiguration feedbackConfiguration;
    private State state;

    public FeedbackDrivenDevelopment(Project project) {
        this.project = project;
    }

    public static FeedbackDrivenDevelopment getInstance(Project project) {
        return project.getComponent(FeedbackDrivenDevelopment.class);
    }

    @Override
    @Nullable
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // Feedback config format is snake_case, as per Python standards
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        try {
            feedbackConfiguration = mapper.readValue(new File(state.feedbackConfigPath), FeedbackConfiguration.class);

            if (!state.takeMetricBackendUrlFromConfig) {
                feedbackConfiguration.setMetricBackendUrl(state.metricBackendUrl);
            }

            client = new ApiClient(feedbackConfiguration.getMetricBackendUrl(), null, null, null);

            Notifications.Bus.notify(new Notification(
                "FeedbackDrivenDevelopment.Info",
                "Reloaded feedback configuration",
                Paths.get(project.getBasePath()).relativize(Paths.get(state.feedbackConfigPath)).toString(),
                NotificationType.INFORMATION
            ));
        } catch (IOException e) {
            Notifications.Bus.notify(new Notification(
                "FeedbackDrivenDevelopment.Error",
                "Could not read feedback configuration",
                e.getMessage(),
                NotificationType.ERROR
            ));

            return;
        }
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void initComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "FeedbackDrivenDevelopment";
    }

    public FeedbackConfiguration getFeedbackConfiguration() {
        return feedbackConfiguration;
    }

    public DefaultApi getApiClient() {
        return client.defaultApi();
    }

}

