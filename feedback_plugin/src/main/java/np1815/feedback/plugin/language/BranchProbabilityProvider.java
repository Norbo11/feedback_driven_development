package np1815.feedback.plugin.language;

import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;

import java.util.Map;

public interface BranchProbabilityProvider {

    public Map<Integer, Double> getBranchExecutionProbability(FileFeedbackWrapper feedbackWrapper);
}
