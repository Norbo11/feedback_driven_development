package np1815.feedback.metricsbackend.persistance;

import np1815.feedback.metricsbackend.model.LineException;
import np1815.feedback.metricsbackend.model.LineGeneral;
import np1815.feedback.metricsbackend.model.LinePerformanceRequestProfileHistory;
import np1815.feedback.metricsbackend.persistance.models.LineGlobalPerformance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetricsBackendOperations {
    public void addProfileLine(LocalDateTime profileStartTimestamp, String filePath, int lineNumber, int numberOfSamples, long sampleTime, String functionName);

    public LocalDateTime addProfile(String applicationName, String version, LocalDateTime startTime, LocalDateTime endTime, long duration);

    public Set<String> getApplicationVersions(String applicationName);

    public void addApplicationIfDoesntExist(String applicationName);

    public int addException(LocalDateTime profileStartTimestamp, String exceptionType, String message);

    public Integer addExceptionFrame(int exceptionId, String filename, Integer lineNumber, String functionName, Integer parentFrameId);

    public Map<Integer, List<LineException>> getExceptionsFeedbackForLines(String applicationName, String version, String filename);

    public Map<Integer, LineGlobalPerformance> getGlobalPerformanceForLines(String applicationName, String version, String filename);

    public Map<Integer, LineGeneral> getGeneralFeedbackForLines(String applicationName, String version, String filename);

    public Map<Integer, List<LinePerformanceRequestProfileHistory>> getPerformanceHistoryForLines(String applicationName, String version, String filename);

    public Map<Integer, List<LinePerformanceRequestProfileHistory>> getPerformanceHistoryForLines(String applicationName, String version, String filename, LocalDateTime historySinceDateTime);

    public void addLoggingLine(LocalDateTime profileStartTimestamp, String filePath, int lineNumber, String logger, String level, String message);
}
