package org.example;

public class TestResult {
    private final String tags;
    private final CSVResultType result;
    private final String note;

    public TestResult(String tags, String result, String note) {
        this.tags = tags.replaceAll("\\s", "");
        this.result = CSVResultType.from(result);
        this.note = note;
    }

    public String getTags() {
        return tags;
    }

    public CSVResultType getResult() {
        return result;
    }

    public String getNote() {
        return note;
    }
}
