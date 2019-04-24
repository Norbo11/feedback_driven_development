package np1815.feedback.plugin.services;

public class TranslatedLineNumber {
    private final String newLineNumber;
    private final boolean veryStale;
    private final String latestAvailableVersion;

    public TranslatedLineNumber(int newLineNumber, boolean veryStale, String latestAvailableVersion) {
        this.newLineNumber = String.valueOf(newLineNumber);
        this.veryStale = veryStale;
        this.latestAvailableVersion = latestAvailableVersion;
    }

    public String getNewLineNumber() {
        return newLineNumber;
    }

    public boolean isVeryStale() {
        return veryStale;
    }

    public String getLatestAvailableVersion() {
        return latestAvailableVersion;
    }
}
