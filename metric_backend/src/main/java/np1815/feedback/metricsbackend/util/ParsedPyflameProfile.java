package np1815.feedback.metricsbackend.util;

import com.google.common.collect.ListMultimap;

public class ParsedPyflameProfile {

    private ListMultimap<String, LineProfile> profiles;

    public ParsedPyflameProfile(ListMultimap<String, LineProfile> profiles) {

        this.profiles = profiles;
    }

    public int size() {
        return profiles.size();
    }

    public ListMultimap<String, LineProfile> getProfiles() {
        return profiles;
    }
}
