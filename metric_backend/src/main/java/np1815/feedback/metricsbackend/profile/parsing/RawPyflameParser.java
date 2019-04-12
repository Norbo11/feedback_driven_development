package np1815.feedback.metricsbackend.profile.parsing;

import com.sun.javaws.exceptions.InvalidArgumentException;
import np1815.feedback.metricsbackend.profile.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawPyflameParser implements PyflameParser {

    private static Pattern lineRegex = Pattern.compile("(?<stackTrace>.*) (?<numSamples>\\d*)");
    private static Pattern pathFunctionLineNumberRegex = Pattern.compile("(?<path>.*):(?<function>.*):(?<lineNumber>\\d*)");

    // For things like <frozen importlib._bootstrap> reported by Pyflame (internal Python calls with no path)
    private static Pattern irregularPathRegex = Pattern.compile("<.*>");

    @Override
    public Profile parseFlamegraph(String input, String basePath) {
        Path base = Paths.get(basePath);
        Profile profile = new Profile();

        String[] lines = input.split("\n");

        int totalSamples = 0;

        for (String line : lines) {
            Matcher matcher = lineRegex.matcher(line);

            if (!matcher.find()) {
                throw new IllegalArgumentException("PyFlame input is broken: could not match line " + line);
            }

            int samplesHere = Integer.valueOf(matcher.group("numSamples"));
            totalSamples += samplesHere;

            for (String pathFunctionLineNumber : matcher.group("stackTrace").split(";")) {
                Matcher pathFunctionLineNumberMatcher = pathFunctionLineNumberRegex.matcher(pathFunctionLineNumber);

                if (!pathFunctionLineNumberMatcher.find()) {
                    throw new IllegalArgumentException("PyFlame input is broken: could not match path/function/lineNumber:  " + pathFunctionLineNumber);
                }

                String path = pathFunctionLineNumberMatcher.group("path");
                Matcher irregularPathMatcher = irregularPathRegex.matcher(path);

                if (irregularPathMatcher.find()) {
                    // Do not record profiles with irregular paths
                    continue;
                }

                String relativePath = base.relativize(Paths.get(path)).toString();

                String function = pathFunctionLineNumberMatcher.group("function");
                int lineNumber = Integer.valueOf(pathFunctionLineNumberMatcher.group("lineNumber"));

                profile.addProfileForLine(relativePath, lineNumber, function, samplesHere);
            }
        }

        profile.setTotalSamples(totalSamples);
        return profile;
    }

}
