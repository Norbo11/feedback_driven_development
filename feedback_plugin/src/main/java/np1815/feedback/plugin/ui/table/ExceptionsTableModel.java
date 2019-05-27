package np1815.feedback.plugin.ui.table;

import np1815.feedback.metricsbackend.model.LineException;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class ExceptionsTableModel extends AbstractTableModel {

    private final List<LineException> exceptions;
    private final String[] columnNames = {"Time", "Type", "Message"};
    private final Class[] columnClasses = {String.class, String.class, String.class};

    public ExceptionsTableModel(List<LineException> exceptions) {
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
