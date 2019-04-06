package np1815.feedback.metricsbackend.profile.parsing;

import np1815.feedback.metricsbackend.profile.Profile;

public interface PyflameParser {

    public Profile parseFlamegraph(String input, String basePath);
}
