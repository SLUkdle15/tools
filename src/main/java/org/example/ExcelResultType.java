package org.example;

public enum ExcelResultType {
    U("U"), OK("OK"), NG("NG"), NiA("NiA"), Cancelled("Cancelled"),
    NoResult("");
    final String presentation;

    ExcelResultType(String presentation){
        this.presentation = presentation;
    }

    String getPresentation() {
        return presentation;
    }
}
