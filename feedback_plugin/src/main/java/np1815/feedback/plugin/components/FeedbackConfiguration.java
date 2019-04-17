package np1815.feedback.plugin.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FeedbackConfiguration {

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
