package np1815.feedback.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import np1815.feedback.plugin.util.FilterType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class FeedbackFilterDialog extends DialogWrapper {

    public static final int CLEAR_FEEDBACK_EXIT_CODE = 999;

    private JPanel rootComponent;
    private JComboBox<FilterType> filterTypeComboBox;
    private JTextField filterValueTextField;
    private JLabel parameterNameLabel;

    protected FeedbackFilterDialog(@Nullable Project project, String parameterName) {
        super(project);
        init();
        setTitle("Customize filter for " + parameterName);
        parameterNameLabel.setText(parameterName);

        filterTypeComboBox.addItem(FilterType.EQUAL);
        filterTypeComboBox.addItem(FilterType.GREATER_THAN);
        filterTypeComboBox.addItem(FilterType.LESS_THAN);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootComponent;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return filterValueTextField;
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        //TODO: Implement validation
        return Collections.emptyList();
    }

    public String getValue() {
        return filterValueTextField.getText();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        Action[] oldActions = super.createActions();
        Action[] newActions = new Action[oldActions.length + 1];

        for (int i = 0; i < oldActions.length; i++) {
            newActions[i] = oldActions[i];
        }

        newActions[oldActions.length] = new ClearAction();

        return newActions;
    }

    public FilterType getFilterType() {
        return (FilterType) filterTypeComboBox.getSelectedItem();
    }

    public class ClearAction extends DialogWrapperExitAction {
        public ClearAction() {
            super("Clear", CLEAR_FEEDBACK_EXIT_CODE);
        }
    }
}
