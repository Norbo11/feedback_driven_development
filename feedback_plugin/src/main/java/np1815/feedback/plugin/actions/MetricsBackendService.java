package np1815.feedback.plugin.actions;

import com.intellij.openapi.components.ServiceManager;
import np1815.feedback.metricsbackend.api.DefaultApi;
import np1815.feedback.metricsbackend.client.ApiClient;

public class MetricsBackendService {
    private final ApiClient client;

    public static MetricsBackendService getInstance() {
        return ServiceManager.getService(MetricsBackendService.class);
    }

    public MetricsBackendService() {
        client = new ApiClient("http://localhost:8080/api", null, null, null);
    }

    public DefaultApi getClient() {
        return client.defaultApi();
    }
}
