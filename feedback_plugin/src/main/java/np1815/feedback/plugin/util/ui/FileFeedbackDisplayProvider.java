package np1815.feedback.plugin.util.ui;

import com.google.common.collect.Sets;
import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.LineException;
import np1815.feedback.plugin.language.BranchProbabilityProvider;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class FileFeedbackDisplayProvider {

    private FileFeedbackWrapper fileFeedbackWrapper;
    private final BranchProbabilityProvider branchProbabilityProvider;
    private Map<Integer, Double> branchProbabilities;
    private final List<Runnable> feedbackChangeListeners;

    public FileFeedbackDisplayProvider(BranchProbabilityProvider branchProbabilityProvider) {
        this.branchProbabilityProvider = branchProbabilityProvider;
        this.feedbackChangeListeners = new ArrayList<>();
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
        return Sets.union(fileFeedbackWrapper.getLineNumbers(), branchProbabilities.keySet());
    }

    public boolean isFileStale() {
        return fileFeedbackWrapper.isFileStale();
    }

    public boolean containsFeedbackForLine(int line) {
        return fileFeedbackWrapper.containsFeedbackForLine(line) || branchProbabilities.containsKey(line);
    }

    public void addFeedbackChangeListener(Runnable runnable) {
        feedbackChangeListeners.add(runnable);
    }

    public void refreshFeedback(FileFeedbackWrapper newFeedback) {
        this.fileFeedbackWrapper = newFeedback;
        this.branchProbabilities = branchProbabilityProvider.getBranchExecutionProbability(newFeedback);

        for (Runnable runnable : feedbackChangeListeners) {
            runnable.run();
        }
    }

    public Optional<Boolean> isLineVeryStale(int line) {
        return fileFeedbackWrapper.isLineVeryStale(line);
    }

    public String getGutterTextForLine(int line) {
        Optional<Double> lineGlobalAverage = fileFeedbackWrapper.getGlobalAverageForLine(line);
        String part1 = lineGlobalAverage.isPresent() ? getGlobalAverageForLine(line) : "";
        String part2 = branchProbabilities.containsKey(line) ? getBranchProbabilityForLine(line) : "";
        return part1 + (!part1.isEmpty() && !part2.isEmpty() ? " - " : "") + part2;
    }

    public String getExecutionCount(int line) {
        return fileFeedbackWrapper.getExecutionCount(line) + " times";
    }
}