package np1815.feedback.metricsbackend.profile;

public class ProfiledLine {
    public ProfiledLine(String filePath, String function, int lineNumber) {
        this.filePath = filePath;
        this.function = function;
        this.lineNumber = lineNumber;
    }

    private String filePath;
    private String function;
    private int lineNumber;
    private int numberOfSamples;

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFunction() {
        return function;
    }

    public void addSamples(int samples) {
        numberOfSamples += samples;
    }
}
