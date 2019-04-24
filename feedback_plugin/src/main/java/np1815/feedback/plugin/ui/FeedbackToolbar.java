package np1815.feedback.plugin.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import groovy.swing.impl.DefaultAction;

import javax.swing.*;


public class FeedbackToolbar {
    private JPanel rootComponent;
    private JComponent actionToolbar;

    public FeedbackToolbar(ToolWindow toolWindow) {
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
