package org.example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SheetUpdateAction {
    private final String csvFileName;
    private final List<TestResult> changes;

    public SheetUpdateAction(String csvFileName, List<TestResult> changes) {
        this.csvFileName = csvFileName;
        this.changes = changes;
    }

    public Map<String, CSVResultType> getChanges() {
        return changes.stream().collect(Collectors.toMap(TestResult::getTags, TestResult::getResult));
    }
}
