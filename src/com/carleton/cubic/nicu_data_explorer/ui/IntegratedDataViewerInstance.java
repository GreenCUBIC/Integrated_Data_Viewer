package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.DefaultInstancePackage;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class IntegratedDataViewerInstance {


    protected CustomRangeSlider customRangeSlider;
    protected Slider timeSlider;
    protected Button playButton;
    protected Button loopButton;
    protected ChoiceBox<String> playbackChoiceBox;
    protected Label lowValText;
    protected Label highValText;
    protected Label playTime;
    protected CustomSlider customSlider;
    protected boolean loopRequested;

    protected final static String LONG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    protected final static String PLAY_BUTTON_STATUS_PLAY = "Play";
    protected final static String PLAY_BUTTON_STATUS_PAUSE = "Pause";
    protected final static String LOOP_STATUS_ON = "Loop:On";
    protected final static String LOOP_STATUS_OFF = "Loop:Off";

    public IntegratedDataViewerInstance(DefaultInstancePackage defaultInstancePackage) {

        this.customRangeSlider = defaultInstancePackage.getCustomRangeSlider();
        this.loopButton = defaultInstancePackage.getLoopButton();
        this.playbackChoiceBox = defaultInstancePackage.getPlaybackChoiceBox();
        this.playButton = defaultInstancePackage.getPlayButton();
        this.timeSlider = defaultInstancePackage.getTimeSlider();
        this.lowValText = defaultInstancePackage.getLowValText();
        this.highValText = defaultInstancePackage.getHighValText();
        this.playTime = defaultInstancePackage.getPlayTime();
        this.customSlider = new CustomSlider();
        this.loopRequested = false;
    }


    public CustomRangeSlider getCustomRangeSlider() {
        return customRangeSlider;
    }

    public CustomSlider getCustomSlider() {
        return customSlider;
    }

    public void setCustomRangeSlider(CustomRangeSlider customRangeSlider) {
        this.customRangeSlider = customRangeSlider;
    }

    public Slider getTimeSlider() {
        return timeSlider;
    }

    public void setTimeSlider(Slider timeSlider) {
        this.timeSlider = timeSlider;
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

    public ChoiceBox<String> getPlaybackChoiceBox() {
        return playbackChoiceBox;
    }

    public void setPlaybackChoiceBox(ChoiceBox<String> playbackChoiceBox) {
        this.playbackChoiceBox = playbackChoiceBox;
    }

    public Label getLowValText() {
        return lowValText;
    }

    public void setLowValText(Label lowValText) {
        this.lowValText = lowValText;
    }

    public Label getHighValText() {
        return highValText;
    }

    public void setHighValText(Label highValText) {
        this.highValText = highValText;
    }

    public Label getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Label playTime) {
        this.playTime = playTime;
    }

    public void setCustomSlider(CustomSlider customSlider) {
        this.customSlider = customSlider;
    }

    public boolean isLoopRequested() {
        return loopRequested;
    }

    public void setLoopRequested(boolean loopRequested) {
        this.loopRequested = loopRequested;
    }
}
