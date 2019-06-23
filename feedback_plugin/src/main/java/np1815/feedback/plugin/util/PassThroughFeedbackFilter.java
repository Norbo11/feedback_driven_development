package np1815.feedback.plugin.util;

import np1815.feedback.metricsbackend.model.Request;

public class PassThroughFeedbackFilter extends FeedbackFilter {

    public PassThroughFeedbackFilter(String endpointName, String paramaterName) {
        super(endpointName, paramaterName, null, null);
    }

    public String toString() {
        return getParamaterName();
    }

    @Override
    public boolean testRequest(Request request) {
        return true;
    }
}
