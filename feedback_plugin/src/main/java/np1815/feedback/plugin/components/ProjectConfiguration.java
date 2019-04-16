package np1815.feedback.plugin.components;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ProjectConfiguration implements Configurable {

    public static final Logger LOG = LoggerFactory.getLogger(ProjectConfiguration.class);

    private final FeedbackDrivenDevelopment feedbackDrivenDevelopmentComponent;
    private JTextField metricBackendUrlTextfield;
    private FeedbackDrivenDevelopment.State initialState;

    public ProjectConfiguration(Project project) {
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
        JPanel rootPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Metric Backend URL");
        metricBackendUrlTextfield = new JTextField(this.initialState.metricsBackendUrl, 60);

        rootPanel.add(label);
        rootPanel.add(metricBackendUrlTextfield);
        return rootPanel;
    }

    @Override
    public boolean isModified() {
//        return false;
        return !initialState.equals(getNewState());
    }

    @Override
    public void apply() throws ConfigurationException {
        feedbackDrivenDevelopmentComponent.loadState(getNewState());
    }

    @NotNull
    private FeedbackDrivenDevelopment.State getNewState() {
        FeedbackDrivenDevelopment.State state = new FeedbackDrivenDevelopment.State();
        state.metricsBackendUrl = metricBackendUrlTextfield.getText();
        return state;
    }
}
