package np1815.feedback.metricsbackend.persistance.models;

import np1815.feedback.metricsbackend.model.LinePerformance;

public class LineGlobalPerformance {
    private final double avg;
    private final LinePerformance.StatusEnum status;

    public LineGlobalPerformance(double avg, LinePerformance.StatusEnum status) {
        this.avg = avg;
        this.status = status;
    }

    public double getAvg() {
        return avg;
    }

    public LinePerformance.StatusEnum getStatus() {
        return status;
    }
}
