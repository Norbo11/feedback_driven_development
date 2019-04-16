package np1815.feedback.plugin.components;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import np1815.feedback.metricsbackend.client.ApiClient;
import np1815.feedback.plugin.services.MetricsBackendService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "FeedbackDrivenDevelopment")
public class FeedbackDrivenDevelopment implements ProjectComponent, PersistentStateComponent<FeedbackDrivenDevelopment.State> {

    static class State {
        public String metricsBackendUrl;

        // TODO: Username and password at some point. Use PasswordSafe class
        // https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_sensitive_data.html

        public State() {
            this.metricsBackendUrl = "";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof State) {
                State o = (State) obj;
                return o.metricsBackendUrl.equals(metricsBackendUrl);
            }
            return false;
        }
    }

    private State state;

    public FeedbackDrivenDevelopment(Project project) {
    }

    public static FeedbackDrivenDevelopment getInstance(Project project) {
        return project.getComponent(FeedbackDrivenDevelopment.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
        MetricsBackendService.getInstance().setClient(new ApiClient(state.metricsBackendUrl, null, null, null));
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
}

