package np1815.feedback.plugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.ui.table.ExceptionsTableModel;
import np1815.feedback.plugin.ui.table.LoggingTableModel;
import np1815.feedback.plugin.ui.table.VersionTableModel;
import np1815.feedback.plugin.util.DateTimeUtils;
import np1815.feedback.plugin.util.ui.FileFeedbackDisplayProvider;
import org.jfree.chart.*;

import javax.swing.*;
import javax.swing.table.*;

public class FeedbackPopup {

    public static Logger LOG = Logger.getInstance(FeedbackPopup.class);

    private final int line;
    private final FileFeedbackDisplayProvider displayProvider;
    private final FeedbackDrivenDevelopment feedbackComponent;
    private JPanel rootComponent;
    private JPanel performance;
    private JPanel exceptions;
    private JBLabel globalAverageField;
    private JBTable exceptionsTable;
    private JPanel info;
    private JLabel lastInstrumentedVersionLabel;
    private JPanel branches;
    private JLabel branchProbabilityLabel;
    private JLabel executionCountLabel;
    private ChartPanel performanceChartPanel;
    private JBTable loggingTable;
    private JLabel statusLabel;
    private JBTextField performanceChartFromTextField;
    private JBTextField performanceChartToTextField;
    private JBTabbedPane tabbedPane;
    private JBTable versionTable;
    private TableModel exceptionsTableModel;
    private PerformanceGraph performanceChart;

    public FeedbackPopup(FeedbackDrivenDevelopment feedbackComponent, int line, FileFeedbackDisplayProvider displayProvider) {
        this.feedbackComponent = feedbackComponent;
        this.line = line;
        this.displayProvider = displayProvider;

        performanceChartFromTextField.setText(feedbackComponent.getState().fromDateTime);
        performanceChartToTextField.setText(feedbackComponent.getState().toDateTime);

        this.performanceChartFromTextField.addActionListener(e -> {
            if (DateTimeUtils.parseDateTimeString(performanceChartFromTextField.getText()).isPresent()) {
                feedbackComponent.getState().fromDateTime = this.performanceChartFromTextField.getText();
                performanceChart.update(displayProvider.getFileFeedbackWrapper());
            } else {
                performanceChartFromTextField.setText("Invalid Format");
            }
        });

        this.performanceChartToTextField.addActionListener(e -> {
            if (DateTimeUtils.parseDateTimeString(performanceChartToTextField.getText()).isPresent()) {
                feedbackComponent.getState().toDateTime = this.performanceChartToTextField.getText();
                performanceChart.update(displayProvider.getFileFeedbackWrapper());
            } else {
                performanceChartToTextField.setText("Invalid Format");
            }
        });

        update();
    }

    private void createUIComponents() {
        this.performanceChart = new PerformanceGraph(feedbackComponent, line);
        this.performanceChartPanel = new ChartPanel(performanceChart.getPerformanceChart());
        this.performanceChartPanel.addChartMouseListener(performanceChart.getMouseListener(performanceChartPanel));
    }

    public void update() {
        // General
        lastInstrumentedVersionLabel.setText(displayProvider.getLastInstrumentedVersion(line));
        executionCountLabel.setText(displayProvider.getExecutionCount(line));
        statusLabel.setText(displayProvider.getLineStatus(line));
        versionTable.setModel(new VersionTableModel(displayProvider.getFileFeedbackWrapper().getVersions(line)));

        // Performance
        globalAverageField.setText(displayProvider.getGlobalAverageForLine(line));
        performanceChart.update(displayProvider.getFileFeedbackWrapper());

        // Branches
        branchProbabilityLabel.setText(displayProvider.getBranchProbabilityForLine(line));

        // Exceptions
        exceptionsTable.setModel(new ExceptionsTableModel(displayProvider.getExceptions(line)));

        // Logging
        loggingTable.setModel(new LoggingTableModel(displayProvider.getFileFeedbackWrapper().getLogging(line)));
    }

    public JPanel getRootComponent() {
        return rootComponent;
    }

}
