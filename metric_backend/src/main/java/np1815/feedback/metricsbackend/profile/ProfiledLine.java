package np1815.feedback.metricsbackend.profile;

public class ProfiledLine {

    private final ProfiledLineKey key;
    private int numberOfSamples;

    public ProfiledLine(ProfiledLineKey key) {
        this.key = key;
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
}
