package np1815.feedback.metricsbackend.profile;

public class ProfiledLine {

    private final ProfiledLineKey key;
    private final String function;
    private int numberOfSamples;

    public ProfiledLine(ProfiledLineKey key, String function) {
        this.key = key;
        this.function = function;
    }

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public void addSamples(int samples) {
        numberOfSamples += samples;
    }

    public ProfiledLineKey getParentKey() {
        return key.getParentKey();
    }

    public Integer getLineNumber() {
        return key.getLineNumber();
    }

    public String getFilePath() {
        return key.getFilePath();
    }

    public ProfiledLineKey getKey() {
        return key;
    }

    public String getFunction() {
        return function;
    }
}
