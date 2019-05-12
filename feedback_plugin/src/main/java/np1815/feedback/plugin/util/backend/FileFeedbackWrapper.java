package np1815.feedback.plugin.util.backend;

import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;

import java.util.*;
import java.util.List;

public class FileFeedbackWrapper {

    private final FileFeedback fileFeedback;
    private final boolean stale;
    // TODO: Rename this field and class (maybe?)
    private final Map<Integer, TranslatedLineNumber> translatedLineNumbers;
    private String latestAvailableVersion;

    public FileFeedbackWrapper(FileFeedback fileFeedback, boolean stale, Map<Integer, TranslatedLineNumber> translatedLineNumbers, String latestAvailableVersion) {
        this.fileFeedback = fileFeedback;
        this.stale = stale;
        this.translatedLineNumbers = translatedLineNumbers;
        this.latestAvailableVersion = latestAvailableVersion;
    }

    private String getLineNumberBeforeTranslation(int line) {
        return containsFeedbackForLine(line) ? translatedLineNumbers.get(line).getLineNumberBeforeChange() : null;
    }

    public List<LineException> getExceptions(int line) {
        String lineNumber = getLineNumberBeforeTranslation(line);
        return lineNumber != null ? fileFeedback.getLines().get(lineNumber).getExceptions() : new ArrayList<>();
    }

    public List<LogRecord> getLogging(int line) {
        String lineNumber = getLineNumberBeforeTranslation(line);
        return lineNumber != null ? fileFeedback.getLines().get(lineNumber).getLogging() : new ArrayList<>();
    }

    public Optional<Double> getGlobalAverageForLine(int line) {
        String lineNumber = getLineNumberBeforeTranslation(line);

        if (lineNumber == null) {
            return Optional.empty();
        }

        LinePerformance perf = fileFeedback.getLines().get(lineNumber).getPerformance();

        if (perf.getStatus() == LinePerformance.StatusEnum.PROFILED) {
            return Optional.of(perf.getGlobalAverage());
        }

        return Optional.empty();
    }

    public Optional<List<LinePerformanceRequestProfileHistory>> getPerformanceHistory(int line) {
        String lineNumber = getLineNumberBeforeTranslation(line);

        if (lineNumber == null) {
            return Optional.empty();
        }

        return Optional.of(fileFeedback.getLines().get(lineNumber).getPerformance().getRequestProfileHistory());
    }

    public Integer getExecutionCount(int line) {
        String lineNumber = getLineNumberBeforeTranslation(line);
        return lineNumber != null ? fileFeedback.getLines().get(lineNumber).getGeneral().getExecutionCount() : 0;
    }

    public Set<Integer> getLineNumbers() {
        return translatedLineNumbers.keySet();
    }

    public boolean isFileStale() {
        return stale;
    }

    public boolean containsFeedbackForLine(int line) {
        return translatedLineNumbers.containsKey(line);
    }

    public Optional<Boolean> isLineVeryStale(int line) {
        return containsFeedbackForLine(line) ? Optional.of(translatedLineNumbers.get(line).isVeryStale()) : Optional.empty();
    }

    public Optional<String> getLatestAvailableVersion(int line) {
//      return containsFeedbackForLine(line) ? Optional.of(translatedLineNumbers.get(line).getLatestAvailableVersion()) : Optional.empty();
//      TODO: Fetch feedback per-line instead of per-file

        return Optional.of(latestAvailableVersion);
    }

    public Optional<Double> getGlobalAverageForFile() {
        // TODO: Should return empty if no line in the file was profiled
        return Optional.ofNullable(fileFeedback.getGlobalAverageForFile());
    }

}
