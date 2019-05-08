package np1815.feedback.metricsbackend.profile;

import com.google.common.base.Objects;

public class ProfiledLineKey {

    private final String filePath;
    private final Integer lineNumber;
    private final ProfiledLineKey parentKey;

    public ProfiledLineKey(String filePath, Integer lineNumber, ProfiledLineKey parentKey) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.parentKey = parentKey;
    }

    public ProfiledLineKey getParentKey() {
        return parentKey;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProfiledLineKey) {
            ProfiledLineKey o = (ProfiledLineKey) obj;
            return Objects.equal(filePath, o.filePath)
                && Objects.equal(lineNumber, o.lineNumber)
                && Objects.equal(parentKey, o.parentKey);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(filePath, lineNumber, parentKey);
    }
}
