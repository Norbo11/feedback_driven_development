package np1815.feedback.metricsbackend.persistance;

import np1815.feedback.metricsbackend.model.PerformanceForFileLines;

import java.sql.Timestamp;
import java.util.Map;

public interface MetricsBackendOperations {
    public void addProfileLine(int profileId, String filePath, int lineNumber, int numberOfSamples, long sampleTime);

    public int addProfile(Timestamp startTime, Timestamp endTime, long duration, String version);

    public Map<String, PerformanceForFileLines> getGlobalAveragePerLine(String filename, String version);
}
