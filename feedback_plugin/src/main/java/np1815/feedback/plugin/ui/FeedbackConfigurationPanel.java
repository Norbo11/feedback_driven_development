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

    private final JTextField metricBackendUrlTextfield;
    private final JTextField feedbackConfigPathTextField;
    private final JCheckBox takeBackendUrlFromConfigCheckbox;
    private final BorderLayoutPanel rootComponent;
    private final FeedbackDrivenDevelopment feedbackComponent;

    public FeedbackConfigurationPanel(Project project, FeedbackDrivenDevelopment feedbackComponent) {
        this.feedbackComponent = feedbackComponent;

        metricBackendUrlTextfield = new JTextField();
        feedbackConfigPathTextField = new JTextField();
        takeBackendUrlFromConfigCheckbox = new JCheckBox("Take from config");

        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        TextFieldWithBrowseButton chooseDirectoriesToInstrumentBrowser = new TextFieldWithBrowseButton(feedbackConfigPathTextField);
        chooseDirectoriesToInstrumentBrowser.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor, project));

        takeBackendUrlFromConfigCheckbox.addItemListener(a -> metricBackendUrlTextfield.setEnabled(a.getStateChange() == ItemEvent.DESELECTED));

        /* Set up layout */
        rootComponent = new BorderLayoutPanel(0, 2);

        BorderLayoutPanel metricBackendUrlPanel = new BorderLayoutPanel(2, 0);
        metricBackendUrlPanel.addToLeft(new Label("Metric Backend Url"));
        metricBackendUrlPanel.addToCenter(metricBackendUrlTextfield);
        metricBackendUrlPanel.addToRight(takeBackendUrlFromConfigCheckbox);

        BorderLayoutPanel chooseDirectoriesToInstrumentPanel = new BorderLayoutPanel();
        chooseDirectoriesToInstrumentPanel.addToLeft(new JLabel("Feedback Config Path"));
        chooseDirectoriesToInstrumentPanel.addToCenter(chooseDirectoriesToInstrumentBrowser);

        rootComponent.addToTop(metricBackendUrlPanel);
        rootComponent.addToCenter(chooseDirectoriesToInstrumentPanel);
        rootComponent.setPreferredSize(new Dimension(400, 500));

        update();
    }

    public void update() {
        metricBackendUrlTextfield.setText(feedbackComponent.getState().metricBackendUrl);
        feedbackConfigPathTextField.setText(feedbackComponent.getState().feedbackConfigPath);
        takeBackendUrlFromConfigCheckbox.setSelected(feedbackComponent.getState().takeMetricBackendUrlFromConfig);
    }

    public BorderLayoutPanel getRootComponent() {
        return rootComponent;
    }
}
