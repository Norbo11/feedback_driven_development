package np1815.feedback.plugin.services;

public class TranslatedLineNumber {
    private final String newLineNumber;
    private final boolean veryStale;

    public TranslatedLineNumber(int newLineNumber, boolean veryStale) {
        this.newLineNumber = String.valueOf(newLineNumber);
        this.veryStale = veryStale;
    }

    public String getNewLineNumber() {
        return newLineNumber;
    }

    public boolean isVeryStale() {
        return veryStale;
    }
}
