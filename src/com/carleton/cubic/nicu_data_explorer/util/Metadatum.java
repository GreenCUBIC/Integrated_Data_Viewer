package com.carleton.cubic.nicu_data_explorer.util;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Metadatum {

    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("value")
    @Expose
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}