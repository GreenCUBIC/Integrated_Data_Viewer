package com.carleton.cubic.nicu_data_explorer.util;

import com.carleton.cubic.nicu_data_explorer.ui.CustomRangeSlider;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class DefaultInstancePackage {


    private final Button playButton;
    private final Button loopButton;
    private final Slider timeSlider;
    private final CustomRangeSlider customRangeSlider;
    private final ChoiceBox<String> playbackChoiceBox;
    private final Label lowValText;
    private final Label highValText;
    private final Label playTime;


    public DefaultInstancePackage(Button playButton, Button loopButton, Slider timeSlider,CustomRangeSlider customRangeSlider, ChoiceBox<String> playbackChoiceBox, Label lowValText, Label highValText, Label playTime) {
        this.playButton = playButton;
        this.loopButton = loopButton;
        this.timeSlider = timeSlider;
        this.customRangeSlider = customRangeSlider;
        this.playbackChoiceBox = playbackChoiceBox;
        this.lowValText = lowValText;
        this.highValText = highValText;
        this.playTime = playTime;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public Button getLoopButton() {
        return loopButton;
    }

    public Slider getTimeSlider() {
        return timeSlider;
    }

    public CustomRangeSlider getCustomRangeSlider() {
        return customRangeSlider;
    }

    public ChoiceBox getPlaybackChoiceBox() {
        return playbackChoiceBox;
    }

    public Label getLowValText() {
        return lowValText;
    }

    public Label getHighValText() {
        return highValText;
    }

    public Label getPlayTime() {
        return playTime;
    }

}
