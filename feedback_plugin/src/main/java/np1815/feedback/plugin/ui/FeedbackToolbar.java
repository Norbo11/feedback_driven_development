package np1815.feedback.plugin.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;

import javax.swing.*;
import java.awt.event.ItemEvent;


public class FeedbackToolbar {
    private final FeedbackDrivenDevelopment feedbackComponent;
    private JPanel rootComponent;
    private JComponent actionToolbar;
    private JCheckBox autoRefreshEverySCheckBox;
    private JTextField refreshIntervalTextField;

    public FeedbackToolbar(Project project, ToolWindow toolWindow) {
        this.feedbackComponent = FeedbackDrivenDevelopment.getInstance(project);

        autoRefreshEverySCheckBox.addItemListener(e -> {
            feedbackComponent.getState().autoRefresh = e.getStateChange() == ItemEvent.SELECTED;
        });

        refreshIntervalTextField.addActionListener(e -> {
            String text = refreshIntervalTextField.getText();
            int interval = 0;

            try {
                interval = Integer.valueOf(text);
            } catch (NumberFormatException ex) {
                refreshIntervalTextField.setText("Please enter an integer");
            }

            feedbackComponent.getState().autoRefreshInterval = interval;
        });

        update();
    }

    private void update() {
        refreshIntervalTextField.setText(feedbackComponent.getState().autoRefreshInterval + "");
        autoRefreshEverySCheckBox.setSelected(feedbackComponent.getState().autoRefresh);
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    private void createUIComponents() {
        ActionManager am = ActionManager.getInstance();

        ActionToolbar actionToolbar = am.createActionToolbar(
            ActionPlaces.getActionGroupPopupPlace("FeedbackDrivenDevelopment.General.DisplayFeedback"),
            (DefaultActionGroup) am.getAction("FeedbackDrivenDevelopment.General"), true);

        actionToolbar.setTargetComponent(rootComponent);
        this.actionToolbar = actionToolbar.getComponent();


    }
}
