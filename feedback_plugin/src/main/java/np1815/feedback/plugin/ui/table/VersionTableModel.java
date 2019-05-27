package np1815.feedback.plugin.ui.table;

import np1815.feedback.plugin.model.VersionRecord;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class VersionTableModel extends AbstractTableModel {

    private final String[] columnNames = {"First Request Time", "Version", "Execution Count"};
    private final Class[] columnClasses = {String.class, String.class, Integer.class};
    private final List<VersionRecord> versions;

    public VersionTableModel(List<VersionRecord> versions) {
        super();
        this.versions = versions;
    }

    @Override
    public int getRowCount() {
        return versions.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return versions.get(rowIndex).getFirstRequest().getStartTimestamp().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
            case 1:
                return versions.get(rowIndex).getVersion();
            case 2:
                return versions.get(rowIndex).getExecutions();
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
