package np1815.feedback.plugin.util.RegressionItem;

import np1815.feedback.plugin.util.FormatUtils;

public class RegressionItem {

    private int lineNumber;
    private double increase;

    public RegressionItem(int lineNumber, double increase) {
        this.lineNumber = lineNumber;
        this.increase = increase;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public double getIncrease() {
        return increase;
    }
}
