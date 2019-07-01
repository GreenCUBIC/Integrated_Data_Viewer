package com.carleton.cubic.nicu_data_explorer.util;

public class HeaderValue
{

    private final String stringValue;

    HeaderValue(String value)
    {
        this.stringValue = value;
    }

    public String stringValue()
    {
        return stringValue;
    }

    int integerValue()
    {
        return Integer.parseInt(stringValue);
    }
}
