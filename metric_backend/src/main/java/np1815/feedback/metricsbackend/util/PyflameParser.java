package np1815.feedback.metricsbackend.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.nio.file.Paths;

public class PyflameParser {

    public ParsedPyflameProfile parseFlamegraph(String input) {

        ListMultimap<String, LineProfile> samples = MultimapBuilder.hashKeys().arrayListValues().build();

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

                samples.put(path, new LineProfile(path, function, Integer.valueOf(lineNumber), Integer.valueOf(samplesHere)));
            }
        }

        ParsedPyflameProfile parsed = new ParsedPyflameProfile(samples);
        return parsed;
    }
}
