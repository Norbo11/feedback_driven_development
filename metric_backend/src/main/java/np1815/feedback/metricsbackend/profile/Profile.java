package np1815.feedback.metricsbackend.profile;

import np1815.feedback.metricsbackend.util.PathUtil;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Profile {
    private HashMap<ProfiledLineKey, ProfiledLine> samples;
    private long totalSamples;

    public Profile() {
        samples = new HashMap<>();
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
            profiledLine = new ProfiledLine(newKey, function);
            samples.put(newKey, profiledLine);
        }

        profiledLine.addSamples(samplesForLine);
        return newKey;
    }

    public ProfiledLine getProfileForLine(String path, int lineNumber, ProfiledLineKey parentKey) {
        return samples.get(new ProfiledLineKey(path, lineNumber, parentKey));
    }

    public ProfiledLine getProfileForLine(String path, int lineNumber) {
        Set<ProfiledLine> lines = getAllLineProfilesSatisfyingPredicate(p -> p.getFilePath().equals(path) && p.getLineNumber() == lineNumber);
        if (lines.size() != 1) {
            throw new MoreThanOneProfileException("Expected only one profile, found " + lines.size());
        }
        return new ArrayList<>(lines).get(0);
    }

    public Set<ProfiledLine> getAllLineProfiles() {
        return new HashSet<>(samples.values());
    }

    public Set<ProfiledLine> getAllLineProfilesMatchingGlobs(List<String> globs) {
        return getAllLineProfilesSatisfyingPredicate(s -> PathUtil.pathMatchesAnyGlob(s.getFilePath(), globs));
    }

    public Set<ProfiledLine> getAllLineProfilesRegex(String pathRegex) {
        return getAllLineProfilesSatisfyingPredicate(s -> Pattern.compile(pathRegex).asPredicate().test(s.getFilePath()));
    }

    public Set<ProfiledLine> getAllLineProfilesStartingWith(List<Path> paths) {
        return getAllLineProfilesSatisfyingPredicate(s -> paths.stream().anyMatch(p -> Paths.get(s.getFilePath()).startsWith(p)));
    }

    private Set<ProfiledLine> getAllLineProfilesSatisfyingPredicate(Predicate<ProfiledLineKey> p) {
        Set<ProfiledLineKey> filteredKeys = samples.keySet().stream().filter(p).collect(Collectors.toSet());
        return filteredKeys.stream().map(key -> samples.get(key)).collect(Collectors.toSet());
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
