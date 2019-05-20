package np1815.feedback.plugin.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import np1815.feedback.plugin.components.FeedbackColouringOptions;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;

import javax.swing.*;
import java.awt.event.ItemEvent;


public class FeedbackToolbar {
    private final FeedbackDrivenDevelopment feedbackComponent;
    private JPanel rootComponent;
    private JComponent actionToolbar;
    private JBCheckBox autoRefreshCheckBox;
    private JBTextField autoRefreshIntervalField;
    private JBRadioButton colourFeedbackRelativeToFunctionRadio;
    private JBRadioButton colourFeedbackRelativeToCurrentScopeRadio;
    private JBRadioButton colourFeedbackRelativeToFile;

    public FeedbackToolbar(Project project, ToolWindow toolWindow) {
        this.feedbackComponent = FeedbackDrivenDevelopment.getInstance(project);

        autoRefreshCheckBox.addItemListener(e -> {
            feedbackComponent.getState().autoRefresh = e.getStateChange() == ItemEvent.SELECTED;
        });

        autoRefreshIntervalField.addActionListener(e -> {
            String text = autoRefreshIntervalField.getText();
            int interval = 0;

            try {
                interval = Integer.valueOf(text);
            } catch (NumberFormatException ex) {
                autoRefreshIntervalField.setText("Please enter an integer");
            }

            feedbackComponent.getState().autoRefreshInterval = interval;
        });

        colourFeedbackRelativeToCurrentScopeRadio.addActionListener((e) -> feedbackComponent.getState().colourFeedbackRelativeTo =
            FeedbackColouringOptions.RELATIVE_TO_CURRENT_SCOPE);

        colourFeedbackRelativeToFunctionRadio.addActionListener((e) -> feedbackComponent.getState().colourFeedbackRelativeTo =
            FeedbackColouringOptions.RELATIVE_TO_FUNCTION);

        colourFeedbackRelativeToFile.addActionListener((e) -> feedbackComponent.getState().colourFeedbackRelativeTo =
            FeedbackColouringOptions.RELATIVE_TO_FILE);

        update();
    }

    private void update() {
        autoRefreshIntervalField.setText(feedbackComponent.getState().autoRefreshInterval + "");
        autoRefreshCheckBox.setSelected(feedbackComponent.getState().autoRefresh);

        if (feedbackComponent.getState().colourFeedbackRelativeTo == FeedbackColouringOptions.RELATIVE_TO_CURRENT_SCOPE) {
            colourFeedbackRelativeToCurrentScopeRadio.setSelected(true);
        }

        if (feedbackComponent.getState().colourFeedbackRelativeTo == FeedbackColouringOptions.RELATIVE_TO_FUNCTION) {
            colourFeedbackRelativeToFunctionRadio.setSelected(true);
        }

        if (feedbackComponent.getState().colourFeedbackRelativeTo == FeedbackColouringOptions.RELATIVE_TO_FILE) {
            colourFeedbackRelativeToFile.setSelected(true);
        }
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    private void createUIComponents() {
        ActionManager am = ActionManager.getInstance();

        ActionToolbar actionToolbar = am.createActionToolbar(
            ActionPlaces.getActionGroupPopupPlace("FeedbackDrivenDevelopment.General.DisplayFeedback"),
            (DefaultActionGroup) am.getAction("FeedbackDrivenDevelopment.General"), false);

        actionToolbar.setTargetComponent(rootComponent);
        this.actionToolbar = actionToolbar.getComponent();


    }
}
