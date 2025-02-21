package org.example;

import java.util.Locale;

public enum  CSVResultType {
    PASSED, FAILED, SKIPPED, NO_RESULT;

    public static CSVResultType from(String result) {
        if (result.isEmpty()) return NO_RESULT;
        else return valueOf(result.toUpperCase(Locale.ROOT));
    }
}
