package np1815.feedback.metricsbackend.persistance;

import np1815.feedback.metricsbackend.model.PerformanceForFileLines;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

public interface MetricsBackendOperations {
    public void addProfileLine(int profileId, String filePath, int lineNumber, int numberOfSamples, long sampleTime);

    public int addProfile(String applicationName, String version, Timestamp startTime, Timestamp endTime, long duration);

    public Map<String, PerformanceForFileLines> getGlobalAveragePerLine(String applicationName, String version, String filename);

    public Set<String> getApplicationVersions(String applicationName);

    public void addApplicationIfDoesntExist(String applicationName);

    int addException(int profileId, String exceptionType, String message);

    Integer addExceptionFrame(int exceptionId, String filename, Integer lineNumber, String functionName, Integer parentFrameId);
}
