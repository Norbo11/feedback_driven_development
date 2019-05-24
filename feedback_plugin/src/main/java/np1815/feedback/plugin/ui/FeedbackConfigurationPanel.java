package np1815.feedback.plugin.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.openapi.project.Project;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class FeedbackConfigurationPanel {

    private final Project project;
    private final FeedbackDrivenDevelopment feedbackComponent;

    private JPanel rootComponent;

    /* Bound to GUI Designer */
    private JCheckBox takeBackendUrlFromConfigCheckBox;
    private JTextField metricBackendUrlTextField;

    /* Custom initialization */
    private JTextField feedbackConfigPathTextField;
    private TextFieldWithBrowseButton chooseDirectoriesToInstrumentBrowser;

    public FeedbackConfigurationPanel(Project project, FeedbackDrivenDevelopment feedbackComponent) {
        this.project = project;
        this.feedbackComponent = feedbackComponent;

        takeBackendUrlFromConfigCheckBox.addItemListener(a -> metricBackendUrlTextField.setEnabled(a.getStateChange() == ItemEvent.DESELECTED));

        update();
    }

    public void update() {
        metricBackendUrlTextField.setText(feedbackComponent.getState().metricBackendUrl);
        feedbackConfigPathTextField.setText(feedbackComponent.getState().feedbackConfigPath);
        takeBackendUrlFromConfigCheckBox.setSelected(feedbackComponent.getState().takeMetricBackendUrlFromConfig);
    }

    public JPanel getRootComponent() {
        return rootComponent;
    }

    private void createUIComponents() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        feedbackConfigPathTextField = new JTextField();
        chooseDirectoriesToInstrumentBrowser = new TextFieldWithBrowseButton(feedbackConfigPathTextField);
        chooseDirectoriesToInstrumentBrowser.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor, project));
    }

    public JCheckBox getTakeBackendUrlFromConfigCheckBox() {
        return takeBackendUrlFromConfigCheckBox;
    }

    public JTextField getMetricBackendUrlTextField() {
        return metricBackendUrlTextField;
    }

    public JTextField getFeedbackConfigPathTextField() {
        return feedbackConfigPathTextField;
    }
}
