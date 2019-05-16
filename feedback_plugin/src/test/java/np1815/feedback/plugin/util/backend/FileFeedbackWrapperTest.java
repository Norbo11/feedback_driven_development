package np1815.feedback.plugin.util.backend;

import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.time.LocalDateTime;
import java.util.*;

public class FileFeedbackWrapperTest {

    private static FileFeedbackWrapper fileFeedbackWrapper;

    @BeforeClass
    public static void setUp() {
        /* aaaa:
           0: someFunction() (no feedback)

           bbbb:
           0: if (request.flag == True):
           1:   anotherFunction() (two requests: 100ms, 100ms)
           2: someFunction() (two requests: 500ms, 700ms)

           cccc:
           0: if (request.flag == True):
           1:   anotherFunction() (no requests)
           2:
           3: someFunction() (two requests: 200ms, 400ms)

           local:
           0: if (request.flag == True):
           1:   anotherFunction() (no requests)
           2:
           3:
           4: someFunction() (no requests)

           Fetching feedback for line 4 of the local version should show feedback from all previous versions.
        */

        List<String> sortedVersions = Arrays.asList("cccc", "bbbb", "aaaa");
        String latestVersion = "cccc";

        Map<String, FileFeedbackLines> feedbackLinesBbbb = new HashMap<>();
        feedbackLinesBbbb.put("1", new FileFeedbackLines()
            .general(new LineGeneral().executionCount(2))
            .exceptions(new ArrayList<>())
            .logging(new ArrayList<>())
            .performance(new LinePerformance()
                .status(LinePerformance.StatusEnum.PROFILED)
                .globalAverage(100.0)
                .requestProfileHistory(Arrays.asList(
                    new LinePerformanceRequestProfileHistory().startTimestamp(LocalDateTime.of(1997, 4, 10, 11, 0)).sampleTime(100.0),
                    new LinePerformanceRequestProfileHistory().startTimestamp(LocalDateTime.of(1997, 4, 10, 11, 30)).sampleTime(100.0)
                ))
            )
        );

        feedbackLinesBbbb.put("2", new FileFeedbackLines()
            .general(new LineGeneral().executionCount(2))
            .exceptions(new ArrayList<>())
            .logging(new ArrayList<>())
            .performance(new LinePerformance()
                .status(LinePerformance.StatusEnum.PROFILED)
                .globalAverage(600.0)
                .requestProfileHistory(Arrays.asList(
                    new LinePerformanceRequestProfileHistory().startTimestamp(LocalDateTime.of(1997, 4, 10, 11, 0)).sampleTime(500.0),
                    new LinePerformanceRequestProfileHistory().startTimestamp(LocalDateTime.of(1997, 4, 10, 11, 30)).sampleTime(700.0)
                ))
            )
        );

        Map<String, FileFeedbackLines> feedbackLinesCccc = new HashMap<>();
        feedbackLinesCccc.put("3", new FileFeedbackLines()
            .general(new LineGeneral().executionCount(2))
            .exceptions(new ArrayList<>())
            .logging(new ArrayList<>())
            .performance(new LinePerformance()
                .status(LinePerformance.StatusEnum.PROFILED)
                .globalAverage(300.0)
                .requestProfileHistory(Arrays.asList(
                    new LinePerformanceRequestProfileHistory().startTimestamp(LocalDateTime.of(1997, 4, 10, 12, 0)).sampleTime(200.0),
                    new LinePerformanceRequestProfileHistory().startTimestamp(LocalDateTime.of(1997, 4, 10, 12, 30)).sampleTime(400.0)
                ))
            )
        );

        Map<String, FileFeedback> versionedFeedback = new HashMap<>();
        versionedFeedback.put("cccc", new FileFeedback()
            .versionExists(true)
            .globalAverageForFile(400.0)
            .lines(feedbackLinesCccc));

        versionedFeedback.put("bbbb", new FileFeedback()
            .versionExists(true)
            .globalAverageForFile(700.0)
            .lines(feedbackLinesBbbb));

        versionedFeedback.put("aaaa", new FileFeedback()
            .versionExists(false)
            .lines(new HashMap<>()));

        Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations = new HashMap<>();

        Map<Integer, TranslatedLineNumber> versionTranslationsBbbb = new HashMap<>();
        versionTranslationsBbbb.put(2, new TranslatedLineNumber(0, false, null));

        Map<Integer, TranslatedLineNumber> versionTranslationsCccc = new HashMap<>();
        versionTranslationsCccc.put(1, new TranslatedLineNumber(1, false, null));
        versionTranslationsCccc.put(3, new TranslatedLineNumber(2, false, null));

        Map<Integer, TranslatedLineNumber> localTranslations = new HashMap<>();
        localTranslations.put(1, new TranslatedLineNumber(1, false, null));
        localTranslations.put(4, new TranslatedLineNumber(3, false, null));


        versionTranslations.put("bbbb", versionTranslationsBbbb);
        versionTranslations.put("cccc", versionTranslationsCccc);

        fileFeedbackWrapper = new FileFeedbackWrapper(sortedVersions, versionedFeedback, versionTranslations, localTranslations);
    }

    @Test
    public void performanceRequestHistoryContainsHistoryFromAllVersions() {
        List<LinePerformanceRequestProfileHistory> performanceHistory = fileFeedbackWrapper.getPerformanceHistory(4);

        Assert.assertEquals(4, performanceHistory.size());

        Assert.assertEquals(LocalDateTime.of(1997, 4, 10, 11, 0), performanceHistory.get(0).getStartTimestamp());
        Assert.assertEquals(500.0, performanceHistory.get(0).getSampleTime(), 0.0);

        Assert.assertEquals(LocalDateTime.of(1997, 4, 10, 11, 30), performanceHistory.get(1).getStartTimestamp());
        Assert.assertEquals(700.0, performanceHistory.get(1).getSampleTime(), 0.0);

        Assert.assertEquals(LocalDateTime.of(1997, 4, 10, 12, 0), performanceHistory.get(2).getStartTimestamp());
        Assert.assertEquals(200.0, performanceHistory.get(2).getSampleTime(), 0.0);

        Assert.assertEquals(LocalDateTime.of(1997, 4, 10, 12, 30), performanceHistory.get(3).getStartTimestamp());
        Assert.assertEquals(400.0, performanceHistory.get(3).getSampleTime(), 0.0);
    }

    @Test
    public void globalAverageIsForLatestVersion() {
        Optional<Double> globalAverageForLine = fileFeedbackWrapper.getGlobalAverageForLine(1);

        Assert.assertTrue(globalAverageForLine.isPresent());
        Assert.assertEquals(100.0, globalAverageForLine.get(), 0.0);

        globalAverageForLine = fileFeedbackWrapper.getGlobalAverageForLine(4);

        Assert.assertTrue(globalAverageForLine.isPresent());
        Assert.assertEquals(300.0, globalAverageForLine.get(), 0.0);
    }

    @Test
    public void latestAvailableVersion() {
        Optional<VersionWithLineNumber> version = fileFeedbackWrapper.getLatestAvailableVersion(1);

        Assert.assertTrue(version.isPresent());
        Assert.assertEquals("bbbb", version.get().getVersion());

        version = fileFeedbackWrapper.getLatestAvailableVersion(4);

        Assert.assertTrue(version.isPresent());
        Assert.assertEquals("cccc", version.get().getVersion());
    }

    @Test
    public void linesContainFeedback() {
        Assert.assertFalse(fileFeedbackWrapper.containsFeedbackForLine(0));
        Assert.assertTrue(fileFeedbackWrapper.containsFeedbackForLine(1));
        Assert.assertFalse(fileFeedbackWrapper.containsFeedbackForLine(2));
        Assert.assertFalse(fileFeedbackWrapper.containsFeedbackForLine(3));
        Assert.assertTrue(fileFeedbackWrapper.containsFeedbackForLine(4));
    }

    @Test
    public void executionCountIsForLatestVersion() {
        Assert.assertEquals(2, fileFeedbackWrapper.getExecutionCount(1), 0);
        Assert.assertEquals(2, fileFeedbackWrapper.getExecutionCount(4), 0);
    }

}