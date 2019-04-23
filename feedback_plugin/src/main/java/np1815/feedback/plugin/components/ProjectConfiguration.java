package np1815.feedback.plugin.components;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ProjectConfiguration implements Configurable {

    public static final Logger LOG = LoggerFactory.getLogger(ProjectConfiguration.class);

    private final FeedbackDrivenDevelopment feedbackDrivenDevelopmentComponent;
    private final Project project;
    private FeedbackDrivenDevelopment.State initialState;

    private JTextField metricBackendUrlTextfield;
    private JTextField feedbackConfigPathTextField;
    private JCheckBox takeBackendUrlFromConfigCheckbox;

    public ProjectConfiguration(Project project) {
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
        /* Initialize components */
        metricBackendUrlTextfield = new JTextField(initialState.metricBackendUrl);
        feedbackConfigPathTextField = new JTextField(initialState.feedbackConfigPath);

        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        TextFieldWithBrowseButton chooseDirectoriesToInstrumentBrowser = new TextFieldWithBrowseButton(feedbackConfigPathTextField);
        chooseDirectoriesToInstrumentBrowser.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor, project));

        takeBackendUrlFromConfigCheckbox = new JCheckBox("Take from config");
        takeBackendUrlFromConfigCheckbox.addItemListener(a -> metricBackendUrlTextfield.setEnabled(a.getStateChange() == ItemEvent.DESELECTED));

        /* Set up layout */
        BorderLayoutPanel rootPanel = new BorderLayoutPanel(0, 2);

        BorderLayoutPanel metricBackendUrlPanel = new BorderLayoutPanel(2, 0);
        metricBackendUrlPanel.addToLeft(new Label("Metric Backend Url"));
        metricBackendUrlPanel.addToCenter(metricBackendUrlTextfield);
        metricBackendUrlPanel.addToRight(takeBackendUrlFromConfigCheckbox);

        BorderLayoutPanel chooseDirectoriesToInstrumentPanel = new BorderLayoutPanel();
        chooseDirectoriesToInstrumentPanel.addToLeft(new JLabel("Feedback Config Path"));
        chooseDirectoriesToInstrumentPanel.addToCenter(chooseDirectoriesToInstrumentBrowser);

        rootPanel.addToTop(metricBackendUrlPanel);
        rootPanel.addToCenter(chooseDirectoriesToInstrumentPanel);
        rootPanel.setPreferredSize(new Dimension(400, 500));
        return rootPanel;
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
        state.feedbackConfigPath = feedbackConfigPathTextField.getText();
        state.metricBackendUrl = metricBackendUrlTextfield.getText();
        state.takeMetricBackendUrlFromConfig = takeBackendUrlFromConfigCheckbox.isSelected();
        return state;
    }
}
