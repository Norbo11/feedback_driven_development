package np1815.feedback.plugin.util.vcs;

public class TranslatedLineNumber {
    private final String oldLineNumber;
    private final boolean veryStale;
    private final String latestAvailableVersion;

    public TranslatedLineNumber(int oldLineNumber, boolean veryStale, String latestAvailableVersion) {
        this.oldLineNumber = String.valueOf(oldLineNumber);
        this.veryStale = veryStale;
        this.latestAvailableVersion = latestAvailableVersion;
    }

    public String getLineNumberBeforeChange() {
        return oldLineNumber;
    }

    public boolean isVeryStale() {
        return veryStale;
    }

    public String getLatestAvailableVersion() {
        return latestAvailableVersion;
    }

    @Override
    public String toString() {
        return "old=" + oldLineNumber + " (" + veryStale + ")";
    }
}
