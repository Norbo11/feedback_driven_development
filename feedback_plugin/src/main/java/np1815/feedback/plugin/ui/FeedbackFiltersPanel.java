package np1815.feedback.plugin.ui;

import com.intellij.ui.components.JBList;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;

import javax.swing.*;

public class FeedbackFiltersPanel {
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JBList requestParametersList;
    private JPanel rootComponent;
    private JTextField endpointNameTextField;
    private DefaultListModel<String> listModel;


    public FeedbackFiltersPanel(FeedbackDrivenDevelopment feedbackComponent) {
        endpointNameTextField.setText("/files/load_csv");
    }

    private void createUIComponents() {
        listModel = new DefaultListModel<String>();
        listModel.addElement("file_name == slow_file.csv");
        listModel.addElement("load_with_header == true");
        requestParametersList = new JBList<String>(listModel);
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }
}
