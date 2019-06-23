package np1815.feedback.plugin.components;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import np1815.feedback.metricsbackend.api.DefaultApi;
import np1815.feedback.metricsbackend.client.ApiClient;
import np1815.feedback.plugin.config.FeedbackWrapperConfiguration;
import np1815.feedback.plugin.util.FeedbackFilter;
import np1815.feedback.plugin.util.backend.FileFeedbackManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "FeedbackDrivenDevelopment")
public class FeedbackDrivenDevelopment implements ProjectComponent, PersistentStateComponent<FeedbackDrivenDevelopment.State> {

    public static final NotificationGroup NOTIFICATIONS_GROUP_ERROR = NotificationGroup.balloonGroup("FeedbackDrivenDevelopment.Error");
    public static final int HIGHLIGHTER_LAYER = HighlighterLayer.SELECTION - 1;

    private final Project project;
    private final Map<VirtualFile, FileFeedbackManager> feedbackManagers;
    private final List<Runnable> multiFileFeedbackChangeListeners;
    private HashMap<String, FeedbackFilter> filters;

    public static class State {
        public String feedbackConfigPath = "";
        public String metricBackendUrl = "";
        public boolean takeMetricBackendUrlFromConfig = true;
        public boolean autoRefresh = true;
        public int autoRefreshInterval = 5;
        public FeedbackColouringOptions colourFeedbackRelativeTo = FeedbackColouringOptions.RELATIVE_TO_FUNCTION;
        public String fromDateTime = "now-1hour";
        public String toDateTime = "now";

        // TODO: Username and password at some point. Use PasswordSafe class
        // https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_sensitive_data.html

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof State) {
                State o = (State) obj;
                return o.feedbackConfigPath.equals(feedbackConfigPath)
                    && o.metricBackendUrl.equals(metricBackendUrl)
                    && o.takeMetricBackendUrlFromConfig == takeMetricBackendUrlFromConfig
                    && o.autoRefresh == autoRefresh
                    && o.autoRefreshInterval == autoRefreshInterval
                    && o.colourFeedbackRelativeTo == colourFeedbackRelativeTo
                    && o.fromDateTime.equals(fromDateTime)
                    && o.toDateTime.equals(toDateTime);
            }
            return false;
        }
    }

    private ApiClient client;
    private FeedbackWrapperConfiguration feedbackWrapperConfiguration;
    private State state;

    public FeedbackDrivenDevelopment(Project project) {
        this.project = project;
        this.feedbackManagers = new HashMap<>();
        this.filters = new HashMap<>();
        this.multiFileFeedbackChangeListeners = new ArrayList<>();
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
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            feedbackWrapperConfiguration = mapper.readValue(new File(state.feedbackConfigPath), FeedbackWrapperConfiguration.class);

            if (!state.takeMetricBackendUrlFromConfig) {
                feedbackWrapperConfiguration.setMetricBackendUrl(state.metricBackendUrl);
            }

            client = new ApiClient(feedbackWrapperConfiguration.getMetricBackendUrl(), null, null, null);

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

    public FeedbackWrapperConfiguration getFeedbackWrapperConfiguration() {
        return feedbackWrapperConfiguration;
    }

    public DefaultApi getApiClient() {
        return client.defaultApi();
    }

    public Map<VirtualFile, FileFeedbackManager> getFeedbackManagers() {
        return feedbackManagers;
    }

    public void addMultiFileFeedbackChangeListener(Runnable runnable) {
        multiFileFeedbackChangeListeners.add(runnable);
    }

    public void removeMultiFileFeedbackChangeListener(Runnable runnable) {
        multiFileFeedbackChangeListeners.remove(runnable);
    }

    public List<Runnable> getMultiFileFeedbackChangeListeners() {
        return multiFileFeedbackChangeListeners;
    }

    public HashMap<String, FeedbackFilter> getFilters() {
        return filters;
    }

    public void repaintAllFeedback() {
        for (FileFeedbackManager manager : feedbackManagers.values()) {
            manager.repaintFeedback();
        }
    }

}

