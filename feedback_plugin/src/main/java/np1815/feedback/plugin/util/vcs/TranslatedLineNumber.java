package np1815.feedback.plugin.util.vcs;

public class TranslatedLineNumber {
    private final String oldLineNumber;

    public TranslatedLineNumber(int oldLineNumber) {
        this.oldLineNumber = String.valueOf(oldLineNumber);
    }

    public String getLineNumberBeforeChange() {
        return oldLineNumber;
    }

    @Override
    public String toString() {
        return "old=" + oldLineNumber;
    }
}
