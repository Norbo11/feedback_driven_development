package np1815.feedback.plugin.util;

import java.text.DecimalFormat;

public class FormatUtils {


    private static DecimalFormat percentageFormat = new DecimalFormat("##.##%");

    public static String formatPercentage(double value) {
        return percentageFormat.format(value);
    }
}
