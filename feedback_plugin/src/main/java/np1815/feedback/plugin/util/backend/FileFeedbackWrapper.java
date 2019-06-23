package np1815.feedback.plugin.util.backend;

import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.model.VersionRecord;
import np1815.feedback.plugin.util.FeedbackFilter;
import np1815.feedback.plugin.util.RegressionItem.RegressionItem;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFeedbackWrapper {

    private static final String LOCAL_VERSION = "#LOCAL#";
    private final List<String> sortedVersions;
    private final Map<Integer, TranslatedLineNumber> localTranslations;
    private final Map<String, FeedbackFilter> filters;
    private final Map<String, FileFeedback> versionedFeedback;
    private final Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations;

    public FileFeedbackWrapper(List<String> sortedVersions,
                               Map<String, FileFeedback> versionedFeedback,
                               Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations,
                               Map<Integer, TranslatedLineNumber> localTranslations) {
        this(Collections.emptyMap(), sortedVersions, versionedFeedback, versionTranslations, localTranslations);
    }

    public FileFeedbackWrapper(Map<String, FeedbackFilter> filters,
                               List<String> sortedVersions,
                               Map<String, FileFeedback> versionedFeedback,
                               Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations,
                               Map<Integer, TranslatedLineNumber> localTranslations) {
        this.filters = filters;

        this.versionedFeedback = versionedFeedback;
        this.versionTranslations = versionTranslations;
        this.sortedVersions = new ArrayList<>(sortedVersions);
        this.localTranslations = localTranslations;

        this.sortedVersions.add(0, LOCAL_VERSION);
        this.versionTranslations.put(LOCAL_VERSION, localTranslations);
        this.versionedFeedback.put(LOCAL_VERSION, new FileFeedback().versionExists(false));

        assert this.sortedVersions.size() == this.versionTranslations.size() + 1;
    }

    private Map<Integer, TranslatedLineNumber> getLocalTranslations() {
        return versionTranslations.get(LOCAL_VERSION);
    }

    private Optional<String> getLatestVersion() {
        return sortedVersions.stream().filter(v -> versionedFeedback.get(v).getVersionExists()).findFirst();
    }

    public Set<Integer> getLineNumbersForLatestAvailableVersion() {
        Optional<String> latest = getLatestVersion();

        if (latest.isPresent()) {
            return versionedFeedback.get(latest.get()).getLines().keySet().stream().map(Integer::valueOf).collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    public Optional<VersionWithLineNumber> getNthAvailableVersion(int line, int n) {
        List<VersionedFeedback> feedback = collectFeedbackForAllVersions(line);

        if (feedback.size() > n) {
            return Optional.ofNullable(feedback.get(n).getVersionWithLineNumber());
        } else {
            return Optional.empty();
        }
    }

    public Optional<VersionWithLineNumber> getLatestAvailableVersion(int line) {
        return getNthAvailableVersion(line, 0);
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

    private <T extends List<V>, V> List<V> getLatestVersionAttributeForLine(int line, T valueIfNotPresent, Function<FileFeedbackLines, T> function, Function<V, LocalDateTime> startTimestampFunction) {
        return getNthVersionAttributeForLine(line, 0, valueIfNotPresent, function, startTimestampFunction);
    }

    private <T extends List<V>, V> List<V> getNthVersionAttributeForLine(int line, int n, T valueIfNotPresent, Function<FileFeedbackLines, T> function, Function<V, LocalDateTime> startTimestampFunction) {
        Optional<VersionWithLineNumber> version = getNthAvailableVersion(line, n);

        if (!version.isPresent()) {
            return valueIfNotPresent;
        }

        VersionWithLineNumber versionWithLineNumber = version.get();
        FileFeedback fileFeedback = versionedFeedback.get(versionWithLineNumber.getVersion());

        if (fileFeedback.getLines().containsKey(versionWithLineNumber.getLineNumber().getLineNumberBeforeChange())) {
            T applied = function.apply(fileFeedback.getLines().get(versionWithLineNumber.getLineNumber().getLineNumberBeforeChange()));
            return applied.stream().filter(i -> getFilterPredicate(versionWithLineNumber.getVersion()).test(startTimestampFunction.apply(i))).collect(Collectors.toList());
        }

        return valueIfNotPresent;
    }

    public List<RegressionItem> getRegressions() {
        List<RegressionItem> regressionItems = new ArrayList<>();

        for (int line : getLineNumbersForLatestAvailableVersion()) {
            List<LineExecution> current = getNthVersionAttributeForLine(line, 0, new ArrayList<>(), l -> l.getPerformance().getRequestProfileHistory(), p -> p.getProfileStartTimestamp());
            List<LineExecution> previous = getNthVersionAttributeForLine(line, 1, new ArrayList<>(), l -> l.getPerformance().getRequestProfileHistory(), p -> p.getProfileStartTimestamp());

            double currentMean = current.stream().mapToDouble(LineExecution::getSampleTime).average().orElse(0.0);
            double previousMean = previous.stream().mapToDouble(LineExecution::getSampleTime).average().orElse(0.0);

            if (previousMean == 0.0) {
                continue;
            }

            // TODO: do t-test
            double change = (currentMean - previousMean) / previousMean;
            regressionItems.add(new RegressionItem(line, change));
        }

        return regressionItems;
    }

    public boolean containsFeedbackForLine(int line) {
        return getLatestAvailableVersion(line).isPresent();
    }

    public List<LineException> getExceptions(int line) {
        return getLatestVersionAttributeForLine(line, new ArrayList<>(), l -> l.getExceptions(), e -> e.getProfileStartTimestamp());
    }

    public List<LogRecord> getLogging(int line) {
        return getLatestVersionAttributeForLine(line, new ArrayList<>(), l -> l.getLogging(), l -> l.getProfileStartTimestamp());
    }

    public Optional<Double> getGlobalAverageForLine(int line) {
        List<LineExecution> filteredHistory = getLatestVersionAttributeForLine(line, new ArrayList<>(), l -> l.getPerformance().getRequestProfileHistory(), p -> p.getProfileStartTimestamp());
        OptionalDouble average = filteredHistory.stream().mapToLong(f -> f.getSampleTime()).average();
        return average.isPresent() ? Optional.of(average.getAsDouble()) : Optional.empty();
    }

    @NotNull
    private Predicate<LocalDateTime> getFilterPredicate(String version) {
        return startTimestamp -> {
                Request request = versionedFeedback.get(version).getRequests().stream().filter(
                        r2 -> startTimestamp.equals(r2.getStartTimestamp())).findAny()
                        .orElseThrow(() -> new AssertionError("Request not present in overall request list"));

                return filters.values().stream().allMatch(f-> f.testRequest(request));
            };
    }

    public Integer getExecutionCount(int line) {
        List<LineExecution> filteredHistory = getLatestVersionAttributeForLine(line, new ArrayList<>(), l -> l.getPerformance().getRequestProfileHistory(), p -> p.getProfileStartTimestamp());
        return filteredHistory.size();
    }

    public List<LineExecution> getPerformanceHistory(int line) {
        return collectFeedbackForAllVersions(line).stream()
            .flatMap(vf -> vf.getFileFeedback().getLines()
                .get(vf.getVersionWithLineNumber().getLineNumber().getLineNumberBeforeChange())
                .getPerformance()
                .getRequestProfileHistory().stream()
            ).sorted((a, b) -> a.getProfileStartTimestamp().compareTo(b.getProfileStartTimestamp()))
            .collect(Collectors.toList());
    }

    public Optional<Boolean> isLineStale(int line) {
        return getLatestAvailableVersion(line).map(versionWithLineNumber -> !versionWithLineNumber.getVersion().equals(sortedVersions.get(1)));
    }

    public List<Request> getFirstRequestsForLine(int line) {
        return collectFeedbackForAllVersions(line).stream()
            .map(vf -> vf.getFileFeedback().getLines()
                .get(vf.getVersionWithLineNumber().getLineNumber().getLineNumberBeforeChange())
                .getGeneral()
                .getLineFirstRequest()
            ).sorted((a, b) -> a.getStartTimestamp().compareTo(b.getStartTimestamp()))
            .collect(Collectors.toList());
    }

    public List<VersionRecord> getVersions(int line) {
        return collectFeedbackForAllVersions(line).stream()
            .map(vf -> {
                FileFeedbackLines feedback = vf.getFileFeedback().getLines().get(vf.getVersionWithLineNumber().getLineNumber().getLineNumberBeforeChange());

                return new VersionRecord(
                    feedback.getGeneral().getLineFirstRequest(),
                    vf.getVersionWithLineNumber().getVersion(),
                    feedback.getGeneral().getProfileCount()
                );
            }).collect(Collectors.toList());
    }

    public List<String> getEndpointNames() {
        Optional<String> latestVersion = getLatestVersion();
        return latestVersion.map(s -> versionedFeedback.get(s).getRequests().stream().map(
            Request::getUrlRule
        ).distinct().collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    public List<String> getRequestParameterNames(String urlRule) {
        Optional<String> latestVersion = getLatestVersion();
        return latestVersion.map(s -> versionedFeedback.get(s).getRequests().stream().filter(r -> r.getUrlRule().equals(urlRule)).flatMap(
            r -> r.getRequestParams().stream().map(NewRequestParam::getName)
        ).distinct().collect(Collectors.toList())).orElse(Collections.emptyList());
    }
}
