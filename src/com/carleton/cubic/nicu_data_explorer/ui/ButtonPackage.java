package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.control.Button;


class ButtonPackage {
    private final Button increaseSampleSizeButton;
    private final Button decreaseSampleSizeButton;
    private final Button autoScaleYAxisButton;
    private final Button removeBubblesButton;

    ButtonPackage(Button increaseSampleSizeButton, Button decreaseSampleSizeButton, Button autoScaleYAxisButton, Button removeBubblesButton) {


        this.increaseSampleSizeButton = increaseSampleSizeButton;
        this.decreaseSampleSizeButton = decreaseSampleSizeButton;
        this.autoScaleYAxisButton = autoScaleYAxisButton;
        this.removeBubblesButton = removeBubblesButton;
    }


    Button getIncreaseSampleSizeButton() {
        return increaseSampleSizeButton;
    }

    Button getDecreaseSampleSizeButton() {
        return decreaseSampleSizeButton;
    }

    Button getAutoScaleYAxisButton() {
        return autoScaleYAxisButton;
    }

    Button getRemoveBubblesButton() {
        return removeBubblesButton;
    }
}
