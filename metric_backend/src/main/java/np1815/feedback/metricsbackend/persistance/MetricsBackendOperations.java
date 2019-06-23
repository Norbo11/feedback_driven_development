package np1815.feedback.metricsbackend.persistance;

import np1815.feedback.metricsbackend.model.*;
import np1815.feedback.metricsbackend.persistance.models.LineGlobalPerformance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetricsBackendOperations {
    public void addProfileLine(LocalDateTime profileStartTimestamp, String filePath, int lineNumber, int numberOfSamples, long sampleTime, String functionName);

    public LocalDateTime addProfile(String applicationName, String version, LocalDateTime startTime, LocalDateTime endTime, String urlRule, long duration);

    public Set<String> getApplicationVersions(String applicationName);

    public void addApplicationIfDoesntExist(String applicationName);

    public int addException(LocalDateTime profileStartTimestamp, String exceptionType, String message);

    public Integer addExceptionFrame(int exceptionId, String filename, Integer lineNumber, String functionName, Integer parentFrameId);

    public Map<Integer, List<LineException>> getExceptionsFeedbackForLines(String applicationName, String version, String filename);

    public Map<Integer, LineGlobalPerformance> getGlobalPerformanceForLines(String applicationName, String version, String filename);

    public Map<Integer, LineGeneral> getGeneralFeedbackForLines(String applicationName, String version, String filename);

    public Request getFirstRequestForLine(String applicationName, String version, String filename, int line);

    public Map<Integer, List<LineExecution>> getPerformanceHistoryForLines(String applicationName, String version, String filename);

    public Map<Integer, List<LineExecution>> getPerformanceHistoryForLines(String applicationName, String version, String filename, LocalDateTime historySinceDateTime);

    public void addLoggingLine(LocalDateTime profileStartTimestamp, String filePath, int lineNumber, String logger, String level, String message, LocalDateTime timestamp);

    public Map<Integer, List<LogRecord>> getLoggingRecordsForLines(String applicationName, String version, String filename);

    public void addRequestParam(LocalDateTime profileStartTimestamp, String value, String type, String name);

    public List<Request> getRequests(String applicationName, String version);
}
