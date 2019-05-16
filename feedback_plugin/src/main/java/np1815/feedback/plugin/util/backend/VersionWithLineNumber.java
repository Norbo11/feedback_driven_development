package np1815.feedback.plugin.util.backend;

import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;

public class VersionWithLineNumber {
    private final String version;
    private final TranslatedLineNumber lineNumber;

    public VersionWithLineNumber(String version, TranslatedLineNumber lineNumber) {
        this.version = version;
        this.lineNumber = lineNumber;
    }

    public String getVersion() {
        return version;
    }

    public TranslatedLineNumber getLineNumber() {
        return lineNumber;
    }
}
