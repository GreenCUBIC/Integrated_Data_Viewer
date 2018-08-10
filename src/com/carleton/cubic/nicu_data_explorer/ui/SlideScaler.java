package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.util.Date;


public class SlideScaler {
    private Date relativeStartDate;
    private Date relativeEndDate;
    private Date absoluteStartDate;
    private Date absoluteEndDate;
    private Long scalingFactor = (long)0;
    private Boolean isActive = false;


    public SlideScaler() {













    }
    public void calculateRelativeScalingDates(CustomRangeSlider customRangeSlider,Annotation annotation){

        absoluteStartDate = customRangeSlider.getAbsoluteStartDate();
        absoluteEndDate = customRangeSlider.getAbsoluteEndDate();
        Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
        Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
        Long annotationRangeInSeconds = (annotationEndDate.getTime()-annotationStartDate.getTime())/1000;


        Long relativeStartTime = annotationStartDate.getTime() - ((scalingFactor * annotationRangeInSeconds) * 10);
        Long relativeEndTime = (annotationEndDate.getTime() + ((scalingFactor * annotationRangeInSeconds)) * 10);

        relativeStartDate = new Date(relativeStartTime);
        relativeEndDate = new Date(relativeEndTime);
    }



    public Long getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(Long scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Date getRelativeStartDate() {
        return relativeStartDate;
    }

    public Date getRelativeEndDate() {
        return relativeEndDate;
    }

}
