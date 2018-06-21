package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.TimeUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.text.DecimalFormat;

public class CustomSlider
{


    Slider mainSlider;
    RangeSlider sliderLimits;

    public CustomSlider()
    {
    }

    ;

    public void sliderLimit(Slider mainSlider, RangeSlider sliderLimits)
    {    //This method sets the limits for the main Slider based of the upper and lower bounds.

        mainSlider.valueProperty().addListener(new ChangeListener<Number>()
        {                  //Listener on main slider
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {

                if (new_val.doubleValue() < sliderLimits.getLowValue())
                {
                    mainSlider.adjustValue(sliderLimits.getLowValue());

                }
                else if (new_val.doubleValue() > sliderLimits.getHighValue())
                {
                    mainSlider.adjustValue(sliderLimits.getHighValue());

                }
            }
        });
    }

    public void checkWitinBound(Slider mainSlider, RangeSlider sliderLimits)
    {


    }

    //Sets up tick units, min and max values and the position of upper bound.
    public RangeSlider setLimitValues(RangeSlider sliderLimits, MediaPlayer smallerDuration)
    {
        sliderLimits.setMin(smallerDuration.getStartTime().toSeconds());
        sliderLimits.setMax(smallerDuration.getStopTime().toSeconds());
        sliderLimits.setLowValue(smallerDuration.getStartTime().toSeconds());
        sliderLimits.setHighValue(smallerDuration.getTotalDuration().toSeconds());

        return sliderLimits;
    }

    //Sets up tick units, min and max values and the position of main Slider.
    public Slider setMainSliderValues(Slider mainSlider, MediaPlayer smallerDuration)
    {
        mainSlider.setMin(smallerDuration.getStartTime().toSeconds());
        mainSlider.setMax(smallerDuration.getStopTime().toSeconds());
        mainSlider.setMajorTickUnit(1);
        return mainSlider;
    }

    public MediaPlayer setSmallerDuration(MediaPlayer mp, MediaPlayer psmMp)
    {

        if (mp.getTotalDuration().lessThan(psmMp.getTotalDuration()))
        {
            return mp;

        }
        return psmMp;
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

    public void updateLabels(RangeSlider rangeSlider, Label lowVal, Label highVal)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        rangeSlider.lowValueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                lowVal.setText(TimeUtils.formatDisplayTime(Duration.seconds(rangeSlider.getLowValue() / 10)));
            }
        });
        rangeSlider.highValueProperty().addListener(new ChangeListener<Number>()
        {                  //Listener on main slider
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                highVal.setText(TimeUtils.formatDisplayTime(Duration.seconds(rangeSlider.getHighValue() / 10)));
            }
        });

    }

}