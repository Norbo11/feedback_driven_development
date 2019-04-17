package np1815.feedback.metricsbackend.profile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TestProfile {

    @Test
    public void profilesStartingWithSubdirectory() {
        Profile profile = new Profile();
        profile.addProfileForLine("app/filename.py", 1, "function", null, 5);
        profile.addProfileForLine("app/subdirectory/filename.py", 1, "function", null, 6);
        profile.addProfileForLine("app/subdirectory/filename.py", 2, "function", null, 7);

        Set<ProfiledLine> lines = profile.getAllLineProfilesStartingWith(Collections.singletonList(Paths.get("app/subdirectory")));

        Assertions.assertEquals(2, lines.size());
    }

    @Test
    public void profilesMatchingGlob() {
        Profile profile = new Profile();
        profile.addProfileForLine("app/filename.py", 1, "function", null, 5);
        profile.addProfileForLine("app/subdirectory/filename.py", 1, "function", null, 6);
        profile.addProfileForLine("app/subdirectory/filename.py", 2, "function", null, 7);

        Set<ProfiledLine> lines = profile.getAllLineProfilesMatchingGlobs(Collections.singletonList("app/**"));

        Assertions.assertEquals(3, lines.size());
    }

    @Test
    public void profilesMatchingGlobRelativeDirectory() {
        Profile profile = new Profile();
        profile.addProfileForLine("../../python/filename.py", 1, "function", null, 5);
        profile.addProfileForLine("app/subdirectory/filename.py", 1, "function", null, 6);
        profile.addProfileForLine("app/subdirectory/filename.py", 2, "function", null, 7);

        Set<ProfiledLine> lines = profile.getAllLineProfilesMatchingGlobs(Collections.singletonList("**/python/**"));

        Assertions.assertEquals(1, lines.size());
    }

    @Test
    public void profilesMatchingEverythingRecursivelyGlob() {
        Profile profile = new Profile();
        profile.addProfileForLine("app/filename.py", 1, "function", null, 5);
        profile.addProfileForLine("app/subdirectory/filename.py", 1, "function", null, 6);
        profile.addProfileForLine("app/subdirectory/filename.py", 2, "function", null, 7);
        profile.addProfileForLine("../../python/whatever.py", 2, "function", null, 7);

        Set<ProfiledLine> lines = profile.getAllLineProfilesMatchingGlobs(Collections.singletonList("**"));

        Assertions.assertEquals(4, lines.size());
    }

    @Test
    public void profilesMatchingEverythingGlob() {
        Profile profile = new Profile();
        profile.addProfileForLine("app/filename.py", 1, "function", null, 5);
        profile.addProfileForLine("app/subdirectory/filename.py", 1, "function", null, 6);
        profile.addProfileForLine("app/subdirectory/filename.py", 2, "function", null, 7);
        profile.addProfileForLine("../../python/whatever.py", 2, "function", null, 7);
        profile.addProfileForLine("whatever.py", 2, "function", null, 7);

        Set<ProfiledLine> lines = profile.getAllLineProfilesMatchingGlobs(Collections.singletonList("*"));

        Assertions.assertEquals(1, lines.size());
    }
}
