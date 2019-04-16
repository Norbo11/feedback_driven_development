package np1815.feedback.plugin.util;

import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.services.TranslatedLineNumber;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public Optional<Color> getColorForLine(int line) {
        Optional<String> lineNumber = getLineNumberBeforeTranslation(line);

        if (!lineNumber.isPresent()) {
            return Optional.empty();
        }

        PerformanceForFileLines perf = performance.getLines().get(lineNumber.get());
        double fractionalPerformance = perf.getGlobalAverage() / performance.getGlobalAverageForFile();

        float brightness = isLineVeryStale(line) ? 0.5f : 1f;

        // 0 = red
        return Optional.of(JBColor.getHSBColor(0, (float) fractionalPerformance, brightness));
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
        return lineNumber.map(s -> performance.getLines().get(s).getGlobalAverage().toString());

    }

    public List<Integer> getLines() {
        return translatedLineNumbers.keySet().stream().map(Integer::valueOf).collect(Collectors.toList());
    }

    public boolean isStale() {
        return stale;
    }
}
