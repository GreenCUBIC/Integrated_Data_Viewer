package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.util.Date;


public class SlideScaler {
    private Date relativeStartDate;
    private Date relativeEndDate;
    private Date absoluteStartDate;
    private Date absoluteEndDate;

    private Boolean isActive =false;

    public Boolean getIsActive() {
        return isActive;
    }

    public Date getRelativeStartDate() {
        return relativeStartDate;
    }

    public SlideScaler() {
    }

    public void calculateAbsoluteVideoStartEndTimes(VideoDataViewer videoDataViewer) {

        this.absoluteStartDate = videoDataViewer.getAbsoluteRecordingStartTime();
        this.absoluteEndDate = new Date((long) (absoluteStartDate.getTime() + videoDataViewer.getMediaPlayer().getTotalDuration().toMillis()));
    }
    public void calculateAbsolutePSMStartEndTimes(PSMDataViewer psmDataViewer) {

        this.absoluteStartDate = psmDataViewer.getAbsolutePSMStartDate();
        this.absoluteEndDate = new Date((absoluteStartDate.getTime() + psmDataViewer.getAbsolutePSMEndDate().getTime()));
    }

    public void calculateRelativeScalingBoundaries(AnnotationTableHandler annotationTableHandler, CheckBox scaleCheckBox, TextField scaleTextField) {

        Annotation selectedAnnotation = annotationTableHandler.getSelectedAnnotation();
        this.isActive = checkIfScalingActive(scaleCheckBox);
        defaultScaleTextFieldIfEmpty(scaleTextField);

        if (isActive) {

            Long scaleFactor = (long) Double.parseDouble(scaleTextField.getText());

            Date annotationStartDate = new Date(Long.parseLong(selectedAnnotation.getStart_time()));
            Date annotationEndDate = new Date(Long.parseLong(selectedAnnotation.getEnd_time()));


            Long annotationRange = (annotationEndDate.getTime() - annotationStartDate.getTime()) / 100;

            Date relativeStartDate = calculateRelativeStartDate(annotationStartDate, scaleFactor, annotationRange);
            Date relativeEndDate = calculateRelativeEndDate(annotationEndDate, scaleFactor, annotationRange);

            this.relativeStartDate = relativeStartDate;
            this.relativeEndDate = relativeEndDate;

        }

    }


    public void setRelativeSliderVideoBoundaries(Slider slider, RangeSlider rangeSlider, MediaPlayer mediaPlayer) {

        if (getIsActive()) {

            this.relativeStartDate = absoluteStartIfExceedLowerBound();
            this.relativeEndDate = absoluteEndIfExceedHigherBound();


            Long sliderMaxLimit = (absoluteEndDate.getTime() - absoluteStartDate.getTime()) / 100;
            Long startInSliderUnits = (relativeStartDate.getTime() - absoluteStartDate.getTime()) / 100;
            Long endInSliderUnits = (relativeEndDate.getTime() - absoluteStartDate.getTime()) / 100;
            slider = setSliderBounds(startInSliderUnits, endInSliderUnits, sliderMaxLimit, slider);
            rangeSlider = setRangeSliderBounds(startInSliderUnits, endInSliderUnits, sliderMaxLimit, rangeSlider);
            mediaPlayer = setMediaPlayerBounds(startInSliderUnits, endInSliderUnits, sliderMaxLimit, mediaPlayer);


        } else {
            return;
        }
    }
    public void setRelativeSliderPSMBoundaries(Slider slider, RangeSlider rangeSlider,PSMDataViewer psmDataViewer) {

        if (getIsActive()) {

            this.relativeStartDate = absoluteStartIfExceedLowerBound();
            this.relativeEndDate = absoluteEndIfExceedHigherBound();


            Long sliderMaxLimit = (absoluteEndDate.getTime() - absoluteStartDate.getTime()) / 100;
            Long startInSliderUnits = (relativeStartDate.getTime() - absoluteStartDate.getTime()) / 100;
            Long endInSliderUnits = (relativeEndDate.getTime() - absoluteStartDate.getTime()) / 100;
            slider = setSliderBounds(startInSliderUnits, endInSliderUnits, sliderMaxLimit, slider);
            rangeSlider = setRangeSliderBounds(startInSliderUnits, endInSliderUnits, sliderMaxLimit, rangeSlider);
            //TODO: DO I NEED TO ADD LIMITS TO THE PSM ON CANVAS???



        } else {
            return;
        }
    }


    public Boolean checkIfScalingActive(CheckBox scaleCheckBox) {

        if (scaleCheckBox.isSelected()) {

            isActive = true;
        } else {

            isActive = false;
        }


        return isActive;
    }

    public MediaPlayer setMediaPlayerBounds(Long startInSliderUnits, Long endInSliderUnits, Long sliderMaxLimit, MediaPlayer mediaPlayer) {

        if (isActive) {
            mediaPlayer.setStartTime(Duration.seconds(0));
            mediaPlayer.setStopTime(Duration.seconds(sliderMaxLimit / (float) 10));

            mediaPlayer.setStartTime(Duration.seconds(startInSliderUnits / (float) 10));
            mediaPlayer.setStopTime(Duration.seconds(endInSliderUnits / (float) 10));
        }

        return mediaPlayer;
    }


    private Date calculateRelativeStartDate(Date annotationStartDate, Long scaleFactor, Long annotationRange) {

        if (isInstant(annotationRange)) {
            annotationRange = (long) 20;
        }

        Long relativeStartTime = annotationStartDate.getTime() - ((scaleFactor * annotationRange) * 100);
        Date relativeStartDate = new Date(relativeStartTime);
        return relativeStartDate;
    }


    private Date calculateRelativeEndDate(Date annotationEndDate, Long scaleFactor, Long annotationRange) {

        if (isInstant(annotationRange)) {
            annotationRange = (long) 20;
        }
        Long relativeEndTime = (annotationEndDate.getTime() + ((scaleFactor * annotationRange)) * 100);
        Date relativeEndDate = new Date(relativeEndTime);
        return relativeEndDate;
    }

    private Slider setSliderBounds(Long startInSliderUnits, Long endInSliderUnits, Long sliderMaxLimit, Slider slider) {

        slider.setMin(0);
        slider.setMin(startInSliderUnits);
        slider.setMax(sliderMaxLimit);
        slider.setMax(endInSliderUnits);
        return slider;
    }

    private RangeSlider setRangeSliderBounds(Long startInSliderUnits, Long endInSliderUnits, Long sliderMaxLimit, RangeSlider rangeSlider) {

        rangeSlider.setMin(0);
        rangeSlider.setMin(startInSliderUnits);
        rangeSlider.setMax(sliderMaxLimit);
        rangeSlider.setMax(endInSliderUnits);
        return rangeSlider;
    }

    private boolean isInstant(Long annotationRange) {

        if (annotationRange < 20) {

            return true;
        }
        return false;

    }

    private void defaultScaleTextFieldIfEmpty(TextField scaleTextField) {

        if (scaleTextField.getText().equals("")) {

            scaleTextField.setText("0");
        }
    }

    private Date absoluteStartIfExceedLowerBound() {

        if (relativeStartDate.getTime() < absoluteStartDate.getTime()) {

            relativeStartDate = absoluteStartDate;

        }
        return relativeStartDate;
    }

    private Date absoluteEndIfExceedHigherBound() {

        if (relativeEndDate.getTime() > absoluteEndDate.getTime()) {

            relativeEndDate = absoluteEndDate;

        }
        return relativeEndDate;
    }

}
