package np1815.feedback.plugin.util.backend;

import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileFeedbackWrapper {

    private static final String LOCAL_VERSION = "#LOCAL#";
    private final List<String> sortedVersions;
    private final Map<String, FileFeedback> versionedFeedback;
    private final Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations;

    public FileFeedbackWrapper(List<String> sortedVersions,
                               Map<String, FileFeedback> versionedFeedback,
                               Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations,
                               Map<Integer, TranslatedLineNumber> localTranslations) {

        this.versionedFeedback = versionedFeedback;
        this.versionTranslations = versionTranslations;
        this.sortedVersions = new ArrayList<>(sortedVersions);

        this.sortedVersions.add(0, LOCAL_VERSION);
        this.versionTranslations.put(LOCAL_VERSION, localTranslations);
        this.versionedFeedback.put(LOCAL_VERSION, new FileFeedback().versionExists(false));

        assert this.sortedVersions.size() == this.versionedFeedback.size();
        assert this.sortedVersions.size() == this.versionTranslations.size() + 1;
    }

    private Map<Integer, TranslatedLineNumber> getLocalTranslations() {
        return versionTranslations.get(LOCAL_VERSION);
    }

    private Optional<String> getLatestVersionFeedback() {
        return sortedVersions.stream().filter(v -> versionedFeedback.get(v).getVersionExists()).findFirst();
    }

    public Optional<VersionWithLineNumber> getLatestAvailableVersion(int line) {
        return collectFeedbackForAllVersions(line).stream().findFirst().map(vf -> vf.getVersionWithLineNumber());
    }

    private List<VersionedFeedback> collectFeedbackForAllVersions(int line) {
        List<VersionedFeedback> feedbacks = new ArrayList<>();
        TranslatedLineNumber lineNumber = new TranslatedLineNumber(line);

        for (String version : sortedVersions) {
            FileFeedback feedbackForVersion = versionedFeedback.get(version);

            // If this version is an instrumented version that contains feedback for this line, add it to the list
            if (feedbackForVersion.getVersionExists() && feedbackForVersion.getLines().containsKey(lineNumber.getLineNumberBeforeChange())) {
                feedbacks.add(new VersionedFeedback(new VersionWithLineNumber(version, lineNumber), feedbackForVersion));
            }

            if (version.equals(sortedVersions.get(sortedVersions.size() - 1))) {
                // Final version doesn't have a version translation
                break;
            }

            // If we ever reach a version which breaks the translation, then we can stop traversing (as this line is new, or wasn't able to be translated)
            TranslatedLineNumber translatedLineNumber = versionTranslations.get(version).get(Integer.valueOf(lineNumber.getLineNumberBeforeChange()));
            if (translatedLineNumber == null) {
                return feedbacks;
            }

            lineNumber = translatedLineNumber;
        }

        return feedbacks;
    }

    private <T> T getLatestVersionAttributeForLine(int line, T valueIfNotPresent, Function<FileFeedbackLines, T> function) {
        Optional<VersionWithLineNumber> latestVersion = getLatestAvailableVersion(line);

        if (!latestVersion.isPresent()) {
            return valueIfNotPresent;
        }

        VersionWithLineNumber versionWithLineNumber = latestVersion.get();
        FileFeedback fileFeedback = versionedFeedback.get(versionWithLineNumber.getVersion());

        if (fileFeedback.getLines().containsKey(versionWithLineNumber.getLineNumber().getLineNumberBeforeChange())) {
            return function.apply(fileFeedback.getLines().get(versionWithLineNumber.getLineNumber().getLineNumberBeforeChange()));
        }

        return valueIfNotPresent;
    }

    public boolean containsFeedbackForLine(int line) {
        return getLatestAvailableVersion(line).isPresent();
    }

    public Optional<Double> getGlobalAverageForFile() {
        // TODO: Should return empty if no line in the file was profiled
        return Optional.ofNullable(versionedFeedback.get(getLatestVersionFeedback().get()).getGlobalAverageForFile());
    }

    public List<LineException> getExceptions(int line) {
        return getLatestVersionAttributeForLine(line, new ArrayList<>(), l -> l.getExceptions());
    }

    public List<LogRecord> getLogging(int line) {
        return getLatestVersionAttributeForLine(line, new ArrayList<>(), l -> l.getLogging());
    }

    public Optional<Double> getGlobalAverageForLine(int line) {
        return getLatestVersionAttributeForLine(line, Optional.empty(), l -> Optional.ofNullable(l.getPerformance().getGlobalAverage()));
    }

    public Integer getExecutionCount(int line) {
        return getLatestVersionAttributeForLine(line, 0, l -> l.getGeneral().getExecutionCount());
    }

    public List<LinePerformanceRequestProfileHistory> getPerformanceHistory(int line) {
        return collectFeedbackForAllVersions(line).stream()
            .flatMap(vf -> vf.getFileFeedback().getLines()
                .get(vf.getVersionWithLineNumber().getLineNumber().getLineNumberBeforeChange())
                .getPerformance()
                .getRequestProfileHistory().stream()
            ).sorted((a, b) -> a.getStartTimestamp().compareTo(b.getStartTimestamp()))
            .collect(Collectors.toList());
    }

    public boolean isLineStale(int line) {
        return getLatestAvailableVersion(line).map(versionWithLineNumber -> !versionWithLineNumber.getVersion().equals(sortedVersions.get(1))).orElse(false);
    }
}
