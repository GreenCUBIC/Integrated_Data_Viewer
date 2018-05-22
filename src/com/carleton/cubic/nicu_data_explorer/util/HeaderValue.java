package com.carleton.cubic.nicu_data_explorer.util;

public class HeaderValue
{

    private String stringValue;

    public HeaderValue(String value)
    {
        this.stringValue = value;
    }

    public String stringValue()
    {
        return stringValue;
    }

    public int integerValue()
    {
        return Integer.parseInt(stringValue);
    }
}
