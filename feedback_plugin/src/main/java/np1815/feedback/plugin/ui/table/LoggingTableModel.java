package np1815.feedback.plugin.ui.table;

import np1815.feedback.metricsbackend.model.LogRecord;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class LoggingTableModel extends AbstractTableModel {

    private final List<LogRecord> logRecords;
    private final String[] columnNames = {"Time", "Logger Name", "Log Level", "Message"};
    private final Class[] columnClasses = {String.class, String.class, String.class, String.class};

    public LoggingTableModel(List<LogRecord> logRecords) {
        super();

        this.logRecords = logRecords;
    }

    @Override
    public int getRowCount() {
        return logRecords.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return logRecords.get(rowIndex).getLogTimestamp().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
            case 1:
                return logRecords.get(rowIndex).getLogger();
            case 2:
                return logRecords.get(rowIndex).getLevel();
            case 3:
                return logRecords.get(rowIndex).getMessage();
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
