package np1815.feedback.plugin.util;

import np1815.feedback.metricsbackend.model.NewRequestParam;
import np1815.feedback.metricsbackend.model.Request;

import java.util.Optional;

public class FeedbackFilter {
    private String endpointName;
    private String paramaterName;
    private String filterValue;
    private FilterType filterType;

    public FeedbackFilter(String endpointName, String paramaterName, String filterValue, FilterType filterType) {
        this.endpointName = endpointName;
        this.paramaterName = paramaterName;
        this.filterValue = filterValue;
        this.filterType = filterType;
    }

    public String toString() {
        return paramaterName + " " + filterType + " " + filterValue;
    }

    public boolean testRequest(Request request) {
        Optional<NewRequestParam> requestParam = request.getRequestParams().stream().filter(p2 -> p2.getName().equals(paramaterName)).findAny();
        if (requestParam.isPresent()) {
            if (filterType == FilterType.EQUAL) {
                return filterValue.equals(requestParam.get().getValue());
            }

            if (filterType == FilterType.GREATER_THAN) {
                double filterValue = Double.valueOf(this.filterValue);
                double trueValue = Double.valueOf(requestParam.get().getValue());
                return trueValue > filterValue;
            }

            if (filterType == FilterType.LESS_THAN) {
                double filterValue = Double.valueOf(this.filterValue);
                double trueValue = Double.valueOf(requestParam.get().getValue());
                return trueValue < filterValue;
            }
        }
        return false;
    }

    public String getParamaterName() {
        return paramaterName;
    }

}
