package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.DefaultInstancePackage;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

class IntegratedDataViewerInstance {


    final CustomRangeSlider customRangeSlider;
    final Slider timeSlider;
    final Button playButton;
    final Button loopButton;
    final ChoiceBox playbackChoiceBox;
    final Label lowValText;
    final Label highValText;
    final Label playTime;
    final CustomSlider customSlider;
    boolean loopRequested;
    final static String LONG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    final static String PLAY_BUTTON_STATUS_PLAY = "Play";
    final static String PLAY_BUTTON_STATUS_PAUSE = "Pause";
    final static String LOOP_STATUS_ON = "Loop:On";
    final static String LOOP_STATUS_OFF = "Loop:Off";

    IntegratedDataViewerInstance(DefaultInstancePackage defaultInstancePackage) {

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

    public Slider getTimeSlider() {
        return timeSlider;
    }

}
