package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

class CustomSlider {


    CustomSlider() {
    }

    void sliderLimit(Slider mainSlider, RangeSlider sliderLimits) {    //This method sets the limits for the main Slider based of the upper and lower bounds.

        //Listener on main slider
        mainSlider.valueProperty().addListener((ov, old_val, new_val) -> {

            if (new_val.doubleValue() < sliderLimits.getLowValue()) {
                mainSlider.adjustValue(sliderLimits.getLowValue());

            } else if (new_val.doubleValue() > sliderLimits.getHighValue()) {
                mainSlider.adjustValue(sliderLimits.getHighValue());

            }
        });
    }

    boolean shouldStopAtEnd(Slider mainSlider, RangeSlider rangeSlider, boolean loopActive) {

        return mainSlider.getValue() == rangeSlider.getHighValue() && !loopActive;

    }

    boolean shouldLoopAtEnd(Slider mainSlider, RangeSlider rangeSlider, boolean loopActive) {
        return mainSlider.getValue() == rangeSlider.getHighValue() && loopActive;
    }

    boolean isPositionOutOfBounds(Slider mainSlider, RangeSlider rangeSlider) {
        return mainSlider.getValue() < rangeSlider.getLowValue() - 1 || mainSlider.getValue() > rangeSlider.getHighValue() + 1;

    }

    void loopIfStoppedAtEnd(RangeSlider rangeSlider, Slider mainSlider, MediaPlayer mediaPlayer) {

        if (Math.abs(rangeSlider.getHighValue() - mainSlider.getValue()) < 5) {

            mainSlider.setValue(rangeSlider.getLowValue());
            mediaPlayer.seek(Duration.seconds(rangeSlider.getLowValue() / 10));
        }


    }

    void loopIfStoppedAtEndPSM(RangeSlider rangeSlider, Slider timeSlider, int[] frameIndex, double psmFrameRatePerSec) {

        if (Math.abs(rangeSlider.getHighValue() - timeSlider.getValue()) < 5) {

            timeSlider.setValue(rangeSlider.getLowValue());
            double seekDurationValueSeconds = rangeSlider.getLowValue() / 10;
            frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
        }
    }

}