package np1815.feedback.plugin.util;

import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;

import java.awt.*;
import java.util.Optional;

public class FilePerformanceDisplayProvider {

    private final PerformanceForFile performance;

    public FilePerformanceDisplayProvider(PerformanceForFile performance) {
        this.performance = performance;
    }

    public Optional<Color> getColorForLine(int line) {
        PerformanceForFileLines perf = performance.getLines().get(String.valueOf(line));

        if (perf == null) {
            return Optional.empty();
        }

        double fractionalPerformance = perf.getGlobalAverage() / performance.getGlobalAverageForFile();

        // 0 = red
        return Optional.of(JBColor.getHSBColor(0, (float) fractionalPerformance, 1));
    }

    public Optional<String> getGlobalAverageForLine(int line) {
        PerformanceForFileLines perf = performance.getLines().get(String.valueOf(line));
        return perf == null ? Optional.empty() : Optional.of(perf.getGlobalAverage().toString());
    }
}
