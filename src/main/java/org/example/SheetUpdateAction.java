package org.example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SheetUpdateAction {
    private final String sheetId;
    private final List<TestResult> changes;

    public SheetUpdateAction(String sheetId, List<TestResult> changes) {
        this.sheetId = sheetId;
        this.changes = changes;
    }

    public String getSheetId() {
        return sheetId;
    }

    public Map<String, CSVResultType> getChanges() {
        return changes.stream().collect(Collectors.toMap(TestResult::getTags, TestResult::getResult));
    }
}
