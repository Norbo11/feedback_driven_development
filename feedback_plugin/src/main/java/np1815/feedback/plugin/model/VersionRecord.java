package np1815.feedback.plugin.model;

import np1815.feedback.metricsbackend.model.Request;

public class VersionRecord {

    private final Request firstRequest;
    private final String version;
    private final int executions;

    public VersionRecord(Request firstRequest, String version, int executions) {
        this.firstRequest = firstRequest;
        this.version = version;
        this.executions = executions;
    }

    public Request getFirstRequest() {
        return firstRequest;
    }

    public String getVersion() {
        return version;
    }

    public int getExecutions() {
        return executions;
    }
}
