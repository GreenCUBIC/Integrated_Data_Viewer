package com.carleton.cubic.nicu_data_explorer.ui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Metadatum {

    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("value")
    @Expose
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Metadatum createMetaDatum(String key, String value)
    {
        Metadatum m = new Metadatum();
        m.setKey(key);
        m.setValue(value);
        return m;
    }

}