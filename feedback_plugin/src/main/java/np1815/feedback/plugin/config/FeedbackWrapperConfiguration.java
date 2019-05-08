package np1815.feedback.plugin.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * This is a bean representing the feedback.yaml configuration file expected by the Python feedback wrapper which communicates with the metric backend
 */
public class FeedbackWrapperConfiguration {

    @JsonProperty
    private String metricBackendUrl;

    @JsonProperty
    private String sourceBasePath;

    @JsonProperty
    private String gitBasePath;

    @JsonProperty
    private String applicationName;

    @JsonProperty
    private List<String> instrumentFileGlobs;

    public String getMetricBackendUrl() {
        return metricBackendUrl;
    }

    public String getSourceBasePath() {
        return sourceBasePath;
    }

    public String getGitBasePath() {
        return gitBasePath;
    }

    public List<String> getInstrumentFileGlobs() {
        return instrumentFileGlobs;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setMetricBackendUrl(String metricBackendUrl) {
        this.metricBackendUrl = metricBackendUrl;
    }
}
