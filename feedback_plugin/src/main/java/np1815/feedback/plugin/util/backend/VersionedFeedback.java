package np1815.feedback.plugin.util.backend;

import np1815.feedback.metricsbackend.model.FileFeedback;

class VersionedFeedback {
    private final VersionWithLineNumber versionWithLineNumber;
    private final FileFeedback fileFeedback;

    public VersionedFeedback(VersionWithLineNumber versionWithLineNumber, FileFeedback fileFeedback) {
        this.versionWithLineNumber = versionWithLineNumber;
        this.fileFeedback = fileFeedback;
    }

    public VersionWithLineNumber getVersionWithLineNumber() {
        return versionWithLineNumber;
    }

    public FileFeedback getFileFeedback() {
        return fileFeedback;
    }
}
