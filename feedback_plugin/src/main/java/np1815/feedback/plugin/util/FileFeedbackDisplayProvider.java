package np1815.feedback.plugin.util;

import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.LineException;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class FileFeedbackDisplayProvider {

    private final FileFeedbackWrapper fileFeedbackWrapper;
    private final Map<Integer, Double> branchProbabilities;

    public FileFeedbackDisplayProvider(FileFeedbackWrapper fileFeedbackWrapper, Map<Integer, Double> branchProbabilities) {
        this.fileFeedbackWrapper = fileFeedbackWrapper;
        this.branchProbabilities = branchProbabilities;
    }

    public Color getBackgroundColourForLine(int line) {
        Optional<Double> lineGlobalAverage = fileFeedbackWrapper.getGlobalAverageForLine(line);
        Optional<Double> fileGlobalAverage = fileFeedbackWrapper.getGlobalAverageForFile();
        Optional<Boolean> lineVeryStale = fileFeedbackWrapper.isLineVeryStale(line);

        if (!Stream.of(lineGlobalAverage, fileGlobalAverage, lineVeryStale).allMatch(Optional::isPresent)) {
            return null;
        }

        double fractionalPerformance = lineGlobalAverage.get() / fileGlobalAverage.get();
        float brightness = lineVeryStale.get() ? 0.5f : 1f;

        // 0 = red
        return JBColor.getHSBColor(0, (float) fractionalPerformance, brightness);
    }

    public Color getForegroundColourForLine(int line) {
        List<LineException> exceptions = fileFeedbackWrapper.getExceptions(line);

        return exceptions.size() > 0 ? JBColor.RED : null;
    }


    public String getLineStatus(int line) {
        Optional<Boolean> lineVeryStale = fileFeedbackWrapper.isLineVeryStale(line);
        return lineVeryStale.isPresent() ? (lineVeryStale.get() ? "Very Stale" : "Latest Version") : "Line Not Profiled";
    }

    public String getLastInstrumentedVersion(int line) {
        return fileFeedbackWrapper.getLatestAvailableVersion(line).orElse("Line Not Instrumented");
    }

    public String getGlobalAverageForFile(int line) {
        Optional<Double> globalAverageForFile = fileFeedbackWrapper.getGlobalAverageForFile();
        return globalAverageForFile.isPresent() ? String.format("%.0fms", globalAverageForFile.get()) : "File Not Profiled";
    }

    public String getGlobalAverageForLine(int line) {
        Optional<Double> globalAverageForLine = fileFeedbackWrapper.getGlobalAverageForLine(line);
        return globalAverageForLine.isPresent() ? String.format("%.0fms", globalAverageForLine.get()) : "Line Not Profiled";
    }

    public List<LineException> getExceptions(int line) {
        return fileFeedbackWrapper.getExceptions(line);
    }

    public String getBranchProbabilityForLine(int line) {
        return branchProbabilities.containsKey(line) ? String.format("%.1f", branchProbabilities.get(line) * 100) + "% of the time" : "Not a branch";
    }

    public Set<Integer> getLineNumbers() {
        return fileFeedbackWrapper.getLineNumbers();
    }

    public boolean isFileStale() {
        return fileFeedbackWrapper.isFileStale();
    }

    public boolean containsFeedbackForLine(int line) {
        return fileFeedbackWrapper.containsFeedbackForLine(line);
    }
}
