package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.Annotation;

import java.util.Date;


class SlideScaler {
    private Date relativeStartDate;
    private Date relativeEndDate;
    private Date absoluteStartDate;
    private Date absoluteEndDate;
    private double scalingFactor = 1;


    SlideScaler() {


    }

    private void calculateRelativeScalingDates(CustomRangeSlider customRangeSlider, Annotation annotation) {

        absoluteStartDate = customRangeSlider.getAbsoluteStartDate();
        absoluteEndDate = customRangeSlider.getAbsoluteEndDate();
        Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
        Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
        long annotationRangeInMillis = (annotationEndDate.getTime() - annotationStartDate.getTime());

        if (annotationRangeInMillis < 1000) {


            annotationRangeInMillis = (long) 1000;
        }

        long relativeStartTime = annotationStartDate.getTime() - Math.round((scalingFactor * annotationRangeInMillis));
        long relativeEndTime = (annotationEndDate.getTime() + Math.round((scalingFactor * annotationRangeInMillis)));

        relativeStartDate = new Date(relativeStartTime);
        relativeEndDate = new Date(relativeEndTime);

    }


    void scaleInstance(CustomRangeSlider customRangeSlider, Annotation annotation) {


        calculateRelativeScalingDates(customRangeSlider, annotation);
        setRelativeMinMaxOfSlider(customRangeSlider);

    }

    private void setRelativeMinMaxOfSlider(CustomRangeSlider customRangeSlider) {

        if (scalingFactor > 0) {
            checkIfWithinAbsoluteBounds();
            customRangeSlider.setMinUsingDate(relativeStartDate);
            customRangeSlider.setMaxUsingDate(relativeEndDate);

        }
    }

    private void checkIfWithinAbsoluteBounds() {

        if (relativeStartDate.getTime() < absoluteStartDate.getTime()) {
            relativeStartDate = absoluteStartDate;
        }
        if (relativeEndDate.getTime() > absoluteEndDate.getTime()) {
            relativeEndDate = absoluteEndDate;
        }

    }

    double getScalingFactor() {
        return scalingFactor;
    }

    void setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    Date getRelativeStartDate() {
        return relativeStartDate;
    }


    boolean relativeStartDateWithinBounds(CustomRangeSlider customRangeSlider) {

        return customRangeSlider.getAbsoluteStartDate().getTime() <= relativeStartDate.getTime() && relativeStartDate.getTime() <= absoluteEndDate.getTime();
    }
}
