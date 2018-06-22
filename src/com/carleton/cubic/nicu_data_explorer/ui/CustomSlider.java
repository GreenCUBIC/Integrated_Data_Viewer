package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.control.Slider;
import org.controlsfx.control.RangeSlider;

public class CustomSlider
{


    public CustomSlider()
    {
    }

    public void sliderLimit(Slider mainSlider, RangeSlider sliderLimits)
    {    //This method sets the limits for the main Slider based of the upper and lower bounds.

        //Listener on main slider
        mainSlider.valueProperty().addListener((ov, old_val, new_val) -> {

            if (new_val.doubleValue() < sliderLimits.getLowValue())
            {
                mainSlider.adjustValue(sliderLimits.getLowValue());

            }
            else if (new_val.doubleValue() > sliderLimits.getHighValue())
            {
                mainSlider.adjustValue(sliderLimits.getHighValue());

            }
        });
    }

    public boolean shouldStopAtEnd(Slider mainSlider, RangeSlider rangeSlider, boolean loopActive)
    {

        if (mainSlider.getValue() == rangeSlider.getHighValue() && loopActive == false)
        {
            return true;

        }
        return false;

    }

    public boolean shouldLoopAtEnd(Slider mainSlider, RangeSlider rangeSlider, boolean loopActive)
    {
        if (mainSlider.getValue() == rangeSlider.getHighValue() && loopActive == true)
        {
            return true;
        }
        return false;
    }

    public boolean isPositionOutOfBounds(Slider mainSlider, RangeSlider rangeSlider)
    {
        if (mainSlider.getValue() < rangeSlider.getLowValue() - 1 || mainSlider.getValue() > rangeSlider.getHighValue() + 1)
        {
            return true;

        }
        return false;

    }

}