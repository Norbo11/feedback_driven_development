package np1815.feedback.metricsbackend.profile;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Profile {
    private Table<String, Integer, ProfiledLine> samples;
    private long totalSamples;

    public Profile() {
        samples = HashBasedTable.create();
    }

    public int numberOfUniqueLines() {
        return samples.size();
    }

    public ProfiledLine getProfileForLine(String path, int lineNumber) {
        return samples.get(path, lineNumber);
    }

    public ProfiledLine addProfileForLine(String path, int lineNumber, String function, int samplesForLine) {
        ProfiledLine profiledLine;

        if (samples.contains(path, lineNumber)) {
            profiledLine = samples.get(path, lineNumber);
        } else {
            profiledLine = new ProfiledLine(path, function, lineNumber);
            samples.put(path, lineNumber, profiledLine);
        }

        profiledLine.addSamples(samplesForLine);
        return profiledLine;
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public Collection<ProfiledLine> getAllLineProfiles() {
        return samples.values();
    }

    public Collection<ProfiledLine> getAllLineProfiles(String pathRegex) {
        Set<String> filteredKeys = samples.rowKeySet().stream().filter(Pattern.compile(pathRegex).asPredicate()).collect(Collectors.toSet());
        return filteredKeys.stream().flatMap(key -> samples.row(key).values().stream()).collect(Collectors.toList());
    }

    public void setTotalSamples(int totalSamples) {
        this.totalSamples = totalSamples;
    }
}
