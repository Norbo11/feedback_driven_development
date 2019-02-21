package uk.ac.ic.doc.np1815;

import java.util.HashMap;

public class ParsedPyflameProfile {

    private HashMap<String, Integer> samples;

    public ParsedPyflameProfile(HashMap<String, Integer> samples) {

        this.samples = samples;
    }

    public int size() {
        return samples.size();
    }

    public HashMap<String, Integer> getSamples() {
        return samples;
    }
}
