package uk.ac.ic.doc.np1815;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class PyflameParser {

    public ParsedPyflameProfile parseFlamegraph(String input) {

        HashMap<String, Integer> samples = new HashMap<>();

        // TODO: Convert to streams/functional (or regex)
        String[] lines = input.split("\n");

        for (String line : lines) {
            String[] lineParts = line.split(" ");
            String samplesHere = lineParts[1];

            for (String singleLine : lineParts[0].split(";")) {
                String[] lineParts2 = singleLine.split(":");

                //TODO: Record relative path to a defined project base
                String path = Paths.get(lineParts2[0]).getFileName().toString();
                String function = lineParts2[1];
                String lineNumber = lineParts2[2];

                samples.put(path, Integer.valueOf(samplesHere));
            }
        }

        ParsedPyflameProfile parsed = new ParsedPyflameProfile(samples);
        return parsed;
    }
}
