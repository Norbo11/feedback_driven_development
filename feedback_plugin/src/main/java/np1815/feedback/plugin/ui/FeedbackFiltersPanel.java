package np1815.feedback.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import np1815.feedback.metricsbackend.model.NewRequestParam;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.util.FeedbackFilter;
import np1815.feedback.plugin.util.PassThroughFeedbackFilter;
import np1815.feedback.plugin.util.backend.FileFeedbackManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import static np1815.feedback.plugin.ui.FeedbackFilterDialog.CLEAR_FEEDBACK_EXIT_CODE;

public class FeedbackFiltersPanel {
    private final Project project;
    private final FeedbackDrivenDevelopment feedbackComponent;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JBList requestParametersList;
    private JPanel rootComponent;
    private JComboBox<String> endpointNameComboBox;
    private DefaultListModel<FeedbackFilter> listModel;

    public FeedbackFiltersPanel(Project project, FeedbackDrivenDevelopment feedbackComponent) {
        this.project = project;
        this.feedbackComponent = feedbackComponent;
        feedbackComponent.addMultiFileFeedbackChangeListener(this::update);
        endpointNameComboBox.addActionListener(e -> update());
        update();
    }

    private void createUIComponents() {
        listModel = new DefaultListModel<FeedbackFilter>();
        requestParametersList = new JBList<FeedbackFilter>(listModel);

        requestParametersList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = requestParametersList.locationToIndex(evt.getPoint());
                    FeedbackFilter filter = listModel.getElementAt(index);
                    String endpoint = (String) endpointNameComboBox.getSelectedItem();

                    FeedbackFilterDialog feedbackFilterDialog = new FeedbackFilterDialog(project, filter.getParamaterName());
                    feedbackFilterDialog.show();

                    if (feedbackFilterDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        feedbackComponent.getFilters().put(endpoint + filter.getParamaterName(), new FeedbackFilter(endpoint, filter.getParamaterName(), feedbackFilterDialog.getValue(), feedbackFilterDialog.getFilterType()));
                        update();
                        feedbackComponent.repaintAllFeedback();
                    }

                    if (feedbackFilterDialog.getExitCode() == CLEAR_FEEDBACK_EXIT_CODE) {
                        feedbackComponent.getFilters().remove(endpoint + filter.getParamaterName());
                        update();
                        feedbackComponent.repaintAllFeedback();
                    }
                }
            }
        });
    }

    private void update() {
        String endpoint = (String) endpointNameComboBox.getSelectedItem();

        endpointNameComboBox.removeAllItems();
        listModel.clear();

        for (FileFeedbackManager manager : feedbackComponent.getFeedbackManagers().values()) {
            for (String name : manager.getDisplayProvider().getFileFeedbackWrapper().getEndpointNames()) {
                endpointNameComboBox.addItem(name);
            }

            for (String param : manager.getDisplayProvider().getFileFeedbackWrapper().getRequestParameterNames(endpoint)) {
                listModel.addElement(feedbackComponent.getFilters().containsKey(endpoint + param) ? feedbackComponent.getFilters().get(endpoint + param) : new PassThroughFeedbackFilter(endpoint, param));
            }
        }

    }

    public JComponent getRootComponent() {
        return rootComponent;
    }
}
