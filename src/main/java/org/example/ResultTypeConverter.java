package org.example;

public class ResultTypeConverter {
    public static ExcelResultType from(CSVResultType type) {
        return switch (type) {
            case FAILED -> ExcelResultType.U;
            case PASSED -> ExcelResultType.OK;
            case SKIPPED -> ExcelResultType.Cancelled;
        };
    }
}
