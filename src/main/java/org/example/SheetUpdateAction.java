package org.example;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SheetUpdateAction {
    private final String sheetId;
    private final String sheetName;
    private final List<TestResult> changes;

    public SheetUpdateAction(String sheetId, String sheetName, List<TestResult> changes) {
        this.sheetId = sheetId;
        this.sheetName = sheetName;
        this.changes = changes;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getSheetId() {
        return sheetId;
    }

    public Map<String, TestResult> getTestResults() {
        return changes.stream().collect(Collectors.toMap(TestResult::getTags, Function.identity(), (oldValue, newValue) -> newValue));
    }
}
