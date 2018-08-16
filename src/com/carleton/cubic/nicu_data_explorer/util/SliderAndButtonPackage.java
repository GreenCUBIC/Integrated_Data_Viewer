package com.carleton.cubic.nicu_data_explorer.util;

import com.carleton.cubic.nicu_data_explorer.ui.CustomRangeSlider;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.controlsfx.control.RangeSlider;

public class SliderAndButtonPackage {


    private Button playButton;
    private Button loopButton;
    private Slider timeSlider;
    private CustomRangeSlider customRangeSlider;


    public SliderAndButtonPackage(Button playButton, Button loopButton, Slider timeSlider, CustomRangeSlider customRangeSlider) {
        this.playButton = playButton;
        this.loopButton = loopButton;
        this.timeSlider = timeSlider;
        this.customRangeSlider = customRangeSlider;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public void setPlayButton(Button playButton) {
        this.playButton = playButton;
    }

    public Button getLoopButton() {
        return loopButton;
    }

    public void setLoopButton(Button loopButton) {
        this.loopButton = loopButton;
    }

    public Slider getTimeSlider() {
        return timeSlider;
    }

    public void setTimeSlider(Slider timeSlider) {
        this.timeSlider = timeSlider;
    }

    public CustomRangeSlider getCustomRangeSlider() {
        return customRangeSlider;
    }

    public void setCustomRangeSlider(CustomRangeSlider customRangeSlider) {
        this.customRangeSlider = customRangeSlider;
    }
}
