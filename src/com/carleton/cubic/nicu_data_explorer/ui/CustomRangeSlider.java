package com.carleton.cubic.nicu_data_explorer.ui;

import org.controlsfx.control.RangeSlider;

import java.util.Date;

public class CustomRangeSlider {

    private Date absoluteStartDate;
    private Date absoluteEndDate;
    private final RangeSlider rangeSlider;

    CustomRangeSlider(RangeSlider rangeSlider) {

        this.rangeSlider = rangeSlider;

    }

    Date getAbsoluteStartDate() {
        return absoluteStartDate;
    }

    void setAbsoluteStartDate(Date absoluteStartDate) {
        this.absoluteStartDate = absoluteStartDate;
    }

    void setMinUsingDate(Date newDate) {

        long minInSliderUnits = (newDate.getTime() - absoluteStartDate.getTime()) / 100;
        rangeSlider.setMin(minInSliderUnits);


    }

    void setMaxUsingDate(Date newDate) {

        long maxInSliderUnits = (newDate.getTime() - absoluteStartDate.getTime()) / 100;
        rangeSlider.setMax(maxInSliderUnits);


    }


    void returnToDefault() {

        this.setMinUsingDate(absoluteStartDate);
        this.setMaxUsingDate(absoluteEndDate);

    }

    Date getAbsoluteEndDate() {
        return absoluteEndDate;
    }

    void setAbsoluteEndDate(Date absoluteEndDate) {
        this.absoluteEndDate = absoluteEndDate;
    }

    public RangeSlider getRangeSlider() {
        return rangeSlider;
    }


}
