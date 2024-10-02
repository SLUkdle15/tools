package org.example;

import java.util.List;
import java.util.Map;
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

    public Map<String, CSVResultType> getChanges() {
        //return a map of tags and result and merge duplicate tags
        return changes.stream().collect(Collectors.toMap(TestResult::getTags, TestResult::getResult, (oldValue, newValue) -> newValue));
    }
}
