package uk.ac.ic.doc.np1815;

public class LineProfile {
    public LineProfile(String filePath, String function, int lineNumber, int numberOfSamples) {
        this.filePath = filePath;
        this.function = function;
        this.lineNumber = lineNumber;
        this.numberOfSamples = numberOfSamples;
    }

    private String filePath;
    private String function;
    private int lineNumber;
    private int numberOfSamples;

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFunction() {
        return function;
    }
}
