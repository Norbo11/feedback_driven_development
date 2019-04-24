package np1815.feedback.metricsbackend.persistance;

import np1815.feedback.metricsbackend.model.LineException;
import np1815.feedback.metricsbackend.model.LineGeneral;
import np1815.feedback.metricsbackend.model.LinePerformance;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetricsBackendOperations {
    public void addProfileLine(int profileId, String filePath, int lineNumber, int numberOfSamples, long sampleTime);

    public int addProfile(String applicationName, String version, Timestamp startTime, Timestamp endTime, long duration);

    public Set<String> getApplicationVersions(String applicationName);

    public void addApplicationIfDoesntExist(String applicationName);

    public int addException(int profileId, String exceptionType, String message);

    public Integer addExceptionFrame(int exceptionId, String filename, Integer lineNumber, String functionName, Integer parentFrameId);

    public Map<Integer, List<LineException>> getExceptionsForLine(String applicationName, String version, String filename);

    public Map<Integer, LinePerformance> getPerformanceForLine(String applicationName, String version, String filename);

    public Map<Integer, LineGeneral> getGeneralForLine(String applicationName, String version, String filename);

}
