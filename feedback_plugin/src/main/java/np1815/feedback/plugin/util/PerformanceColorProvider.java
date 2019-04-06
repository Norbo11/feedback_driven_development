package np1815.feedback.plugin.util;

import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.PerformanceForFile;

import java.awt.*;

public class PerformanceColorProvider {

    private final PerformanceForFile performance;

    public PerformanceColorProvider(PerformanceForFile performance) {
        this.performance = performance;
    }

    public Color getColorForLine(String path, int lineNumber) {
        double fractionalPerformance = performance.getLines().get(String.valueOf(lineNumber)).getGlobalAverage() / performance.getGlobalAverageForFile();

        // 0 = red
        return JBColor.getHSBColor(0, (float) fractionalPerformance, 1);
    }
}
