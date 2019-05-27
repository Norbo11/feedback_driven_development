package np1815.feedback.plugin.util.ui;

import com.google.common.collect.Streams;
import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.LineException;
import np1815.feedback.metricsbackend.model.LogRecord;
import np1815.feedback.plugin.components.FeedbackColouringOptions;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.language.BranchProbabilityProvider;
import np1815.feedback.plugin.language.python.PythonAggregatePerformanceProvider;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;
import np1815.feedback.plugin.util.backend.VersionWithLineNumber;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFeedbackDisplayProvider {

    private final PythonAggregatePerformanceProvider aggregatePerformanceProvider;
    private FileFeedbackWrapper fileFeedbackWrapper;
    private Map<Integer, Double> branchProbabilities;

    private final FeedbackDrivenDevelopment feedbackComponent;
    private final BranchProbabilityProvider branchProbabilityProvider;
    private final List<Runnable> feedbackChangeListeners;

    public FileFeedbackDisplayProvider(FeedbackDrivenDevelopment feedbackComponent, BranchProbabilityProvider branchProbabilityProvider, PythonAggregatePerformanceProvider aggregatePerformanceProvider) {
        this.feedbackComponent = feedbackComponent;
        this.branchProbabilityProvider = branchProbabilityProvider;
        this.feedbackChangeListeners = new ArrayList<>();
        this.aggregatePerformanceProvider = aggregatePerformanceProvider;
    }

    public Color getBackgroundColourForLine(int line) {
        Optional<Double> lineGlobalAverage = fileFeedbackWrapper.getGlobalAverageForLine(line);

        if (!lineGlobalAverage.isPresent()) {
            return null;
        }

        Optional<Double> scopeGlobalAverage = Optional.empty();

        if (feedbackComponent.getState().colourFeedbackRelativeTo == FeedbackColouringOptions.RELATIVE_TO_FILE) {
            scopeGlobalAverage = aggregatePerformanceProvider.getAggregatePerformanceForFile(fileFeedbackWrapper);
        }

        if (feedbackComponent.getState().colourFeedbackRelativeTo == FeedbackColouringOptions.RELATIVE_TO_FUNCTION) {
            scopeGlobalAverage = aggregatePerformanceProvider.getAggregatePerformanceForFunction(fileFeedbackWrapper, line);
        }

        if (feedbackComponent.getState().colourFeedbackRelativeTo == FeedbackColouringOptions.RELATIVE_TO_CURRENT_SCOPE) {
            scopeGlobalAverage = aggregatePerformanceProvider.getAggregatePerformanceForCurrentScope(fileFeedbackWrapper, line);
        }

        if (!scopeGlobalAverage.isPresent()) {
            return null;
        }

        double fractionalPerformance = lineGlobalAverage.get() / scopeGlobalAverage.get();
        float brightness = 1f;

        // 0 = red
        return JBColor.getHSBColor(0, (float) fractionalPerformance, brightness);
    }

    public Color getForegroundColourForLine(int line) {
        List<LineException> exceptions = fileFeedbackWrapper.getExceptions(line);

        return exceptions.size() > 0 ? JBColor.RED : null;
    }

    public String getLastInstrumentedVersion(int line) {
        Optional<VersionWithLineNumber> latestVersion = fileFeedbackWrapper.getLatestAvailableVersion(line);
        return latestVersion.isPresent() ? latestVersion.get().getVersion() : "Line Not Instrumented";
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

    public boolean containsAnyFeedback() {
        return fileFeedbackWrapper != null;
    }

    public boolean containsFeedbackForLine(int line) {
        return containsAnyFeedback() && (fileFeedbackWrapper.containsFeedbackForLine(line) || branchProbabilities.containsKey(line));
    }

    public void addFeedbackChangeListener(Runnable runnable) {
        feedbackChangeListeners.add(runnable);
    }

    public void removeFeedbackChangeListener(Runnable runnable) {
        feedbackChangeListeners.remove(runnable);
    }

    public void refreshFeedback(FileFeedbackWrapper newFeedback) {
        this.fileFeedbackWrapper = newFeedback;
        this.branchProbabilities = branchProbabilityProvider.getBranchExecutionProbability(newFeedback);

        for (Runnable runnable : feedbackChangeListeners) {
            runnable.run();
        }
    }

    public String getGutterTextForLine(int line) {
        Optional<Double> lineGlobalAverage = fileFeedbackWrapper.getGlobalAverageForLine(line);
        String part1 = lineGlobalAverage.isPresent() ? getGlobalAverageForLine(line) : "";
        String part2 = branchProbabilities.containsKey(line) ? getBranchProbabilityForLine(line) : "";

        List<LogRecord> logging = fileFeedbackWrapper.getLogging(line);
        String part3 = logging.size() > 0 ? logging.size() + " log records" : "";

        List<LineException> exceptions = fileFeedbackWrapper.getExceptions(line);
        String part4 = exceptions.size() > 0 ? exceptions.size() + " exceptions" : "";

        return Stream.of(part1, part2, part3, part4).filter(p -> !p.equals("")).collect(Collectors.joining(" - "));
    }

    public String getExecutionCount(int line) {
        return fileFeedbackWrapper.getExecutionCount(line) + " times";
    }

    public FileFeedbackWrapper getFileFeedbackWrapper() {
        return fileFeedbackWrapper;
    }

    public String getLineStatus(int line) {
        if (fileFeedbackWrapper.isLineStale(line).isPresent()) {
            return fileFeedbackWrapper.isLineStale(line).get() ? "Stale (Instrumented Version < Checked-Out Version)" : "Recent (Instrumented Version = Checked-Out Version)";
        }

        return "N/A";
    }
}
