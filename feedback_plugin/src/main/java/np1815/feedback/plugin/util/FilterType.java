package np1815.feedback.plugin.util;

public enum FilterType {
    LESS_THAN("<"), GREATER_THAN(">"), EQUAL("==");

    private String string;

    FilterType(String string) {
        this.string = string;
    }

    public String toString() {
        return string;
    }
}
