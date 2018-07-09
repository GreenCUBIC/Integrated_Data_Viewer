package com.carleton.cubic.nicu_data_explorer.ui;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Annotation {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("start_time")
    @Expose
    private String startTime;
    @SerializedName("end_time")
    @Expose
    private String endTime;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("metadata")
    @Expose
    private List<Metadatum> metadata = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStart_time() {
        return startTime;
    }

    public void setStart_time(String startTime) {
        this.startTime = startTime;
    }

    public String getEnd_time() {
        return endTime;
    }

    public void setEnd_time(String endTime) {
        this.endTime = endTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Metadatum> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadatum> metadata) {
        this.metadata = metadata;
    }

}