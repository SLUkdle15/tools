package org.example;

public class ResultTypeConverter {
    public static ExcelResultType from(CSVResultType type) {
        return switch (type) {
            case FAILED -> ExcelResultType.NG;
            case PASSED -> ExcelResultType.OK;
        };
    }
}
