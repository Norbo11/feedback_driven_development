package np1815.feedback.metricsbackend.profile;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Profile {
    private LinkedHashMap<ProfiledLineKey, ProfiledLine> samples;
    private long totalSamples;

    public Profile() {
        samples = new LinkedHashMap<>();
    }

    public int numberOfUniqueLines() {
        return samples.size();
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(int totalSamples) {
        this.totalSamples = totalSamples;
    }

    public ProfiledLineKey addProfileForLine(String path, int lineNumber, String function, ProfiledLineKey parentKey, int samplesForLine) {
        ProfiledLineKey newKey = new ProfiledLineKey(path, lineNumber, parentKey);

        ProfiledLine profiledLine;

        if (samples.containsKey(newKey)) {
            profiledLine = samples.get(newKey);
        } else {
            profiledLine = new ProfiledLine(newKey);
            samples.put(newKey, profiledLine);
        }

        profiledLine.addSamples(samplesForLine);
        return newKey;
    }

    public ProfiledLine getProfileForLine(String path, int lineNumber, ProfiledLineKey parentKey) {
        return samples.get(new ProfiledLineKey(path, lineNumber, parentKey));
    }

    public ProfiledLine getProfileForLine(String path, int lineNumber) {
        List<ProfiledLine> lines = getAllLineProfilesSatisfyingPredicate(p -> p.getFilePath().equals(path) && p.getLineNumber() == lineNumber);
        if (lines.size() != 1) {
            throw new MoreThanOneProfileException("Expected only one profile, found " + lines.size());
        }
        return new ArrayList<>(lines).get(0);
    }

    public List<ProfiledLine> getAllLineProfiles() {
        return new ArrayList<>(samples.values());
    }

    public List<ProfiledLine> getAllLineProfilesRegex(String pathRegex) {
        return getAllLineProfilesSatisfyingPredicate(s -> Pattern.compile(pathRegex).asPredicate().test(s.getFilePath()));
    }

    public List<ProfiledLine> getAllLineProfilesStartingWith(String startPath) {
        return getAllLineProfilesSatisfyingPredicate(s -> s.getFilePath().startsWith(startPath));
    }

    private List<ProfiledLine> getAllLineProfilesSatisfyingPredicate(Predicate<ProfiledLineKey> p) {
        Set<ProfiledLineKey> filteredKeys = samples.keySet().stream().filter(p).collect(Collectors.toSet());
        return filteredKeys.stream().map(key -> samples.get(key)).collect(Collectors.toList());
    }

    public class MoreThanOneProfileException extends Error {
        private final String message;

        public MoreThanOneProfileException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
