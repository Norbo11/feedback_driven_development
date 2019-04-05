package np1815.feedback.metricsbackend.profile.parsing;

import np1815.feedback.metricsbackend.profile.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RawPyflameParser implements PyflameParser {

    @Override
    public Profile parseFlamegraph(String input, String basePath) {
        Path base = Paths.get(basePath);
        Profile profile = new Profile();

        // TODO: Convert to streams/functional (or regex)
        String[] lines = input.split("\n");

        int totalSamples = 0;

        for (String line : lines) {
            String[] lineParts = line.split(" ");
            int samplesHere = Integer.valueOf(lineParts[1]);
            totalSamples += samplesHere;

            for (String singleLine : lineParts[0].split(";")) {
                String[] lineParts2 = singleLine.split(":");

                //TODO: Record relative path to a defined project base
                String path = base.relativize(Paths.get(lineParts2[0])).toString();
                String function = lineParts2[1];
                String lineNumber = lineParts2[2];

                int lineNumberParsed = Integer.valueOf(lineNumber);
                profile.addProfileForLine(path, lineNumberParsed, function, samplesHere);
            }
        }

        profile.setTotalSamples(totalSamples);
        return profile;
    }

}
