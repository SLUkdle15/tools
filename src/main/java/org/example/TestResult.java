package org.example;

import java.util.Locale;

public class TestResult {
    private final String id;
    private final String name;
    private final CSVResultType result;
    private final String note;

    public TestResult(String id, String name, String result, String note) {
        this.id = id.replaceAll("\\s", "");
        this.name = name;
        this.result = CSVResultType.valueOf(result.toUpperCase(Locale.ROOT));
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CSVResultType getResult() {
        return result;
    }

    public String getNote() {
        return note;
    }
}
