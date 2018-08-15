package com.carleton.cubic.nicu_data_explorer.ui;

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
        Long annotationRangeInMillis = (annotationEndDate.getTime()-annotationStartDate.getTime());

        if(annotationRangeInMillis<1000){


            annotationRangeInMillis=(long)1000;
        }

        long relativeStartTime = annotationStartDate.getTime() - ((scalingFactor * annotationRangeInMillis));
        long relativeEndTime = (annotationEndDate.getTime() + ((scalingFactor * annotationRangeInMillis)));

        relativeStartDate = new Date(relativeStartTime);
        relativeEndDate = new Date(relativeEndTime);
    }


    public void scaleInstance(CustomRangeSlider customRangeSlider, Annotation annotation) {


            calculateRelativeScalingDates(customRangeSlider,annotation);
            setRelativeMinMaxOfSlider(customRangeSlider);

    }

    public void setRelativeMinMaxOfSlider(CustomRangeSlider customRangeSlider) {

       if(isActive) {
           checkIfWithinAbsoluteBounds();
           customRangeSlider.setMinUsingDate(relativeStartDate);
           customRangeSlider.setMaxUsingDate(relativeEndDate);

       }
    }

    private void checkIfWithinAbsoluteBounds() {

        if(relativeStartDate.getTime()<absoluteStartDate.getTime()){
            relativeStartDate=absoluteStartDate;
        }
        if(relativeEndDate.getTime()>absoluteEndDate.getTime()){
            relativeEndDate=absoluteEndDate;
        }

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


    public boolean relativeStartDateWithinBounds(CustomRangeSlider customRangeSlider) {

        return customRangeSlider.getAbsoluteStartDate().getTime()<=relativeStartDate.getTime() && relativeStartDate.getTime()<=absoluteEndDate.getTime();
    }
}
