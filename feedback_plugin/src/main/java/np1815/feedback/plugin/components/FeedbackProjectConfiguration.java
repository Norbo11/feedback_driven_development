package np1815.feedback.plugin.components;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import np1815.feedback.plugin.ui.FeedbackConfigurationPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;


import javax.swing.*;

public class FeedbackProjectConfiguration implements Configurable {

    public static final Logger LOG = Logger.getInstance(FeedbackProjectConfiguration.class);

    private final FeedbackDrivenDevelopment feedbackDrivenDevelopmentComponent;
    private final Project project;
    private FeedbackDrivenDevelopment.State initialState;
    private FeedbackConfigurationPanel feedbackConfigPanel;

    public FeedbackProjectConfiguration(Project project) {
        this.project = project;
        this.feedbackDrivenDevelopmentComponent = FeedbackDrivenDevelopment.getInstance(project);
        this.initialState = feedbackDrivenDevelopmentComponent.getState();

        if (this.initialState == null) {
            this.initialState = new FeedbackDrivenDevelopment.State();
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Feedback Driven Development";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        feedbackConfigPanel = new FeedbackConfigurationPanel(project, feedbackDrivenDevelopmentComponent);
        return feedbackConfigPanel.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return !initialState.equals(getNewState());
    }

    @Override
    public void apply() throws ConfigurationException {
        feedbackDrivenDevelopmentComponent.loadState(getNewState());
    }

    @NotNull
    private FeedbackDrivenDevelopment.State getNewState() {
        FeedbackDrivenDevelopment.State state = new FeedbackDrivenDevelopment.State();
        state.feedbackConfigPath = feedbackConfigPanel.getFeedbackConfigPathTextField().getText();
        state.metricBackendUrl = feedbackConfigPanel.getMetricBackendUrlTextField().getText();
        state.takeMetricBackendUrlFromConfig = feedbackConfigPanel.getTakeBackendUrlFromConfigCheckBox().isSelected();
        return state;
    }
}
