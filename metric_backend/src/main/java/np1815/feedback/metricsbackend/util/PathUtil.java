package np1815.feedback.metricsbackend.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

public class PathUtil {

    public static boolean pathMatchesAnyGlob(String string, List<String> globs) {
        return globs.stream().anyMatch(g -> {
            Path path = Paths.get(string);
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + g);
            return matcher.matches(path);
        });
    }
}
