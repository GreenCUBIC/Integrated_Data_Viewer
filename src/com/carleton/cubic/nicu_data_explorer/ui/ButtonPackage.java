package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.control.Button;


public class ButtonPackage {
    private Button increaseSampleSizeButton;
    private Button decreaseSampleSizeButton;
    private Button autoScaleYAxisButton;
    private Button removeBubblesButton;

    public ButtonPackage(Button increaseSampleSizeButton, Button decreaseSampleSizeButton, Button autoScaleYAxisButton, Button removeBubblesButton) {


        this.increaseSampleSizeButton = increaseSampleSizeButton;
        this.decreaseSampleSizeButton = decreaseSampleSizeButton;
        this.autoScaleYAxisButton = autoScaleYAxisButton;
        this.removeBubblesButton = removeBubblesButton;
    }


    public Button getIncreaseSampleSizeButton() {
        return increaseSampleSizeButton;
    }

    public Button getDecreaseSampleSizeButton() {
        return decreaseSampleSizeButton;
    }

    public Button getAutoScaleYAxisButton() {
        return autoScaleYAxisButton;
    }

    public Button getRemoveBubblesButton() {
        return removeBubblesButton;
    }
}
