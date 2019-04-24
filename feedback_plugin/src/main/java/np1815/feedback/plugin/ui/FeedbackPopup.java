package np1815.feedback.plugin.ui;

import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.Vector;
import np1815.feedback.metricsbackend.model.FileException;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;

import javax.swing.*;
import javax.swing.table.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class FeedbackPopup {

    private final int line;
    private final FilePerformanceDisplayProvider displayProvider;
    private JPanel rootComponent;
    private JPanel performance;
    private JPanel exceptions;
    private JLabel globalAverageField;
    private JBTable exceptionsTable;
    private JPanel info;
    private JLabel lastInstrumentedVersionLabel;
    private TableModel exceptionsTableModel;

    public class ExceptionsTableModel extends AbstractTableModel {

        private final List<FileException> exceptions;
        private final String[] columnNames = {"Time", "Type", "Message"};
        private final Class[] columnClasses = {String.class, String.class, String.class};

        public ExceptionsTableModel(List<FileException> exceptions) {
            super();

            this.exceptions = exceptions;
        }

        @Override
        public int getRowCount() {
            return exceptions.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return exceptions.get(rowIndex).getExceptionTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
                case 1:
                    return exceptions.get(rowIndex).getExceptionType();
                case 2:
                    return exceptions.get(rowIndex).getExceptionMessage();
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnClasses[columnIndex];
        }
    }

    public FeedbackPopup(int line, FilePerformanceDisplayProvider displayProvider) {
        this.line = line;
        this.displayProvider = displayProvider;

        update();
    }

    public void update() {
        lastInstrumentedVersionLabel.setText(displayProvider.getLastInstrumentedVersion(line));
        globalAverageField.setText(displayProvider.getGlobalAverageForLine(line).orElse(""));
        exceptionsTable.setModel(new ExceptionsTableModel(displayProvider.getExceptions(line)));
        exceptionsTable.createDefaultColumnsFromModel();
        exceptionsTable.setShowColumns(true);
        JTableHeader header = exceptionsTable.getTableHeader();
    }

    public JPanel getRootComponent() {
        return rootComponent;
    }
}
