package np1815.feedback.plugin.util;

import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.FileException;
import np1815.feedback.metricsbackend.model.FilePerformance;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.services.TranslatedLineNumber;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FilePerformanceDisplayProvider {

    private final PerformanceForFile performance;
    private final boolean stale;
    // TODO: Rename this field and class (maybe?)
    private final Map<Integer, TranslatedLineNumber> translatedLineNumbers;

    public FilePerformanceDisplayProvider(PerformanceForFile performance, boolean stale, Map<Integer, TranslatedLineNumber> translatedLineNumbers) {
        this.performance = performance;
        this.stale = stale;
        this.translatedLineNumbers = translatedLineNumbers;
    }

    public Optional<Color> getBackgroundColourForLine(int line) {
        Optional<String> lineNumber = getLineNumberBeforeTranslation(line);

        if (!lineNumber.isPresent()) {
            return Optional.empty();
        }

        PerformanceForFileLines perf = performance.getLines().get(lineNumber.get());
        FilePerformance linePerf = perf.getPerformance();

        if (linePerf.getStatus() == FilePerformance.StatusEnum.NOT_PROFILED) {
            return Optional.empty();
        }

        double fractionalPerformance = linePerf.getGlobalAverage() / performance.getGlobalAverageForFile();

        float brightness = isLineVeryStale(line) ? 0.5f : 1f;

        // 0 = red
        return Optional.of(JBColor.getHSBColor(0, (float) fractionalPerformance, brightness));
    }

    public Optional<Color> getForegroundColourForLine(int line) {
        Optional<String> lineNumber = getLineNumberBeforeTranslation(line);

        if (!lineNumber.isPresent()) {
            return Optional.empty();
        }

        List<FileException> exceptions = performance.getLines().get(lineNumber.get()).getExceptions();

        return exceptions.size() > 0 ? Optional.of(JBColor.RED) : Optional.empty();
    }

    private Optional<String> getLineNumberBeforeTranslation(int line) {
        TranslatedLineNumber translatedLineNumber = translatedLineNumbers.get(line);
        return translatedLineNumber != null ? Optional.of(translatedLineNumber.getNewLineNumber()) : Optional.empty();
    }

    public boolean isLineVeryStale(int line) {
        TranslatedLineNumber translatedLineNumber = translatedLineNumbers.get(line);
        return translatedLineNumber != null && translatedLineNumber.isVeryStale();
    }

    public Optional<String> getGlobalAverageForLine(int line) {
        Optional<String> lineNumber = getLineNumberBeforeTranslation(line);

        if (!lineNumber.isPresent()) {
            return Optional.empty();
        }

        FilePerformance per = performance.getLines().get(lineNumber.get()).getPerformance();

        if (per.getStatus() == FilePerformance.StatusEnum.PROFILED) {
            return Optional.of(per.getGlobalAverage().toString());
        }

        return Optional.empty();
    }

    // IntelliJ editor starts counting from 0
    public Set<Integer> getLineNumbers() {
        return translatedLineNumbers.keySet();
    }

    public boolean isStale() {
        return stale;
    }

    public boolean containsFeedbackForLine(int lineNumber) {
        return translatedLineNumbers.containsKey(lineNumber);
    }
}
