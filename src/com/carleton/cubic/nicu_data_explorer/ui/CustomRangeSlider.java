package com.carleton.cubic.nicu_data_explorer.ui;

import org.controlsfx.control.RangeSlider;

import java.util.Date;

public class CustomRangeSlider{

    private Date absoluteStartDate;
    private Date absoluteEndDate;
    private RangeSlider rangeSlider;

    public CustomRangeSlider(RangeSlider rangeSlider) {

        this.rangeSlider = rangeSlider;

    }

    public Date getAbsoluteStartDate() {
        return absoluteStartDate;
    }

    public void setAbsoluteStartDate(Date absoluteStartDate) {
        this.absoluteStartDate = absoluteStartDate;
    }

    public void setLowValueUsingDate(Date newDate) {
        if(absoluteStartDate.getTime()<newDate.getTime()&&newDate.getTime()<absoluteEndDate.getTime()) {
            long startInSliderUnits = (newDate.getTime() - absoluteStartDate.getTime()) / 100;
            rangeSlider.setLowValue(startInSliderUnits);
        }

    }

    public void setHighValueUsingDate(Date newDate) {
        if(absoluteStartDate.getTime()<newDate.getTime()&&newDate.getTime()<absoluteEndDate.getTime()) {
            long startInSliderUnits = (newDate.getTime() - absoluteStartDate.getTime()) / 100;
            rangeSlider.setHighValue(startInSliderUnits);
        }

    }

    public Date getLowValueInDate(Date absoluteStartDate) {

        Long sliderLowValue = (long)rangeSlider.getLowValue();
       long dateInEpochMillis = absoluteStartDate.getTime()+sliderLowValue*100;
       Date date = new Date(dateInEpochMillis);

        return date;
    }

    public Date getHighValueInDate(Date absoluteStartDate) {

        Long sliderHighValue = (long)rangeSlider.getHighValue();
        long dateInEpochMillis = absoluteStartDate.getTime()+sliderHighValue*100;
        Date date = new Date(dateInEpochMillis);

        return date;
    }

    public Date getAbsoluteEndDate() {
        return absoluteEndDate;
    }

    public void setAbsoluteEndDate(Date absoluteEndDate) {
        this.absoluteEndDate = absoluteEndDate;
    }

    public RangeSlider getRangeSlider() {
        return rangeSlider;
    }

    public void setRangeSlider(RangeSlider rangeSlider) {
        this.rangeSlider = rangeSlider;
    }
}
