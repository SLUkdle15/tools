package org.example;

import java.util.Locale;

public class TestResult {
    private final String tags;
    private final String testName;
    private final CSVResultType result;
    private final String note;

    public TestResult(String tags, String name, String result, String note) {
        this.tags = tags.replaceAll("\\s", "");
        this.testName = name;
        this.result = CSVResultType.valueOf(result.toUpperCase(Locale.ROOT));
        this.note = note;
    }

    public String getTags() {
        return tags;
    }

    public String getTestName() {
        return testName;
    }

    public CSVResultType getResult() {
        return result;
    }

    public String getNote() {
        return note;
    }
}
