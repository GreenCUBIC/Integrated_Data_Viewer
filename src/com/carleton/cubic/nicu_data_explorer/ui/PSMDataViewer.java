package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PSMDataViewer extends IntegratedDataViewerInstance {

    private final XsensorASCIIParser xsensorASCIIParser;
    private PSMRecording psmRecording;
    private Canvas canvas;
    private Duration totalRecordingTime;
    private boolean programmaticSliderValueChange = false;
    private Timeline timeLine;
    private Date absoluteStartDate;
    private int[] frameIndex;

    PSMDataViewer(File psmFile, DefaultInstancePackage defaultInstancePackage) {
        super(defaultInstancePackage);

        xsensorASCIIParser = new XsensorASCIIParser(psmFile);

        playbackChoiceBox.setItems(FXCollections.observableArrayList(
                "0.5", "1.0", "2.0", "4.0", "8.0")
        );
        playbackChoiceBox.getSelectionModel().select(1);
    }

    void openWithControls(Canvas canvasScene) {
        this.canvas = canvasScene;

        double psmFrameRatePerSec = 18; //TODO: How to get this from the PSM file?
        int totalNumberOfFrames;
        if(xsensorASCIIParser.isFirstFrameZero()){
            totalNumberOfFrames = xsensorASCIIParser.parseForLastFrameNumber();
        }else{
            totalNumberOfFrames = xsensorASCIIParser.parseForLastFrameNumber()-xsensorASCIIParser.parseForFirstFrameNumber();
        }

        customSlider.sliderLimit(timeSlider, customRangeSlider.getRangeSlider());
        totalRecordingTime = Duration.seconds(totalNumberOfFrames / psmFrameRatePerSec);
        timeSlider.setMax(totalRecordingTime.toSeconds() * 10); //slider value is tenth of a second
        customRangeSlider.getRangeSlider().setMax(totalRecordingTime.toSeconds() * 10);
        customRangeSlider.getRangeSlider().setLowValue(0);
        customRangeSlider.getRangeSlider().setHighValue(customRangeSlider.getRangeSlider().getMax());
        try {
            psmRecording = xsensorASCIIParser.parse();
        }catch(IndexOutOfBoundsException e){
            System.out.println("Wrong File Type");

            Stage stage = (Stage)canvas.getScene().getWindow();
            stage.close();
        }

        int[] frameIndex = new int[]{0};
        this.frameIndex = frameIndex;
        final Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(1000 / psmFrameRatePerSec),
                        event -> {
                            if (frameIndex[0] < psmRecording.frameCount()) {
                                drawFrame(psmRecording.getFrameData(frameIndex[0]), canvas);
                                frameIndex[0]++;
                                Duration currentFrameTime = Duration.seconds(frameIndex[0] / psmFrameRatePerSec);
                                playTime.setText(TimeUtils.formattedDurationForDisplay(currentFrameTime));

                                programmaticSliderValueChange = true;
                                timeSlider.setValue(currentFrameTime.toSeconds() * 10);
                                programmaticSliderValueChange = false;
                            } else {
                                int progressPercent = (int) ((psmRecording.frameCount() * 100.0) / frameIndex[0]);
                                drawPlaceHolderFrame(canvas, progressPercent);
                            }
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        this.timeLine = timeline;
        speedHandler();
        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange) {
                double seekDurationValueSeconds = timeSlider.getValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
            }
            if (customSlider.shouldStopAtEnd(timeSlider, customRangeSlider.getRangeSlider(), loopRequested)) {
                timeline.pause();
            }
            if (customSlider.shouldLoopAtEnd(timeSlider, customRangeSlider.getRangeSlider(), loopRequested)) {
                double seekDurationValueSeconds = customRangeSlider.getRangeSlider().getLowValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
                timeSlider.adjustValue(customRangeSlider.getRangeSlider().getLowValue());
                timeline.play();
            }
        });

        playButton.setOnAction(e -> {
            Animation.Status timeLineStatus = timeline.getStatus();
            this.absoluteStartDate = getAbsolutePSMStartDate();
            if (timeLineStatus == Animation.Status.PAUSED ||
                    timeLineStatus == Animation.Status.STOPPED) {
                timeline.play();
                playButton.setText(PLAY_BUTTON_STATUS_PAUSE);
            } else if (timeSlider.getValue() == customRangeSlider.getRangeSlider().getHighValue()) {

                return;

            } else {
                timeline.pause();
                playButton.setText(PLAY_BUTTON_STATUS_PLAY);
            }

            if (customSlider.isPositionOutOfBounds(timeSlider, customRangeSlider.getRangeSlider())) {
                programmaticSliderValueChange = true;
                timeSlider.setValue(customRangeSlider.getRangeSlider().getLowValue());
                double seekDurationValueSeconds = customRangeSlider.getRangeSlider().getLowValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
                programmaticSliderValueChange = false;
            }
        });
        loopButton.setOnAction(e -> {

            if (!loopRequested) {
                loopRequested = true;
                loopButton.setText(LOOP_STATUS_ON);
                customSlider.loopIfStoppedAtEndPSM(customRangeSlider.getRangeSlider(), timeSlider, frameIndex, psmFrameRatePerSec);

            } else {
                loopRequested = false;
                loopButton.setText(LOOP_STATUS_OFF);
            }

        });

        customRangeSlider.getRangeSlider().setOnMouseDragged(drag -> {
            timeline.pause();
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);
        });
        customRangeSlider.getRangeSlider().setOnMouseClicked(click -> {
            timeline.pause();
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);

        });
        timeSlider.setOnMouseClicked(click -> {
            timeline.pause();
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);

        });
        timeSlider.setOnMouseDragged(drag -> {
            timeline.pause();
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);

        });

        while (psmRecording.frameCount() <= 0) {
            System.out.println("Waiting");
        }

        // Draw first frame
        if (frameIndex[0] < psmRecording.frameCount()) {
            drawFrame(psmRecording.getFrameData(frameIndex[0]), canvas);
            frameIndex[0]++;
        }

        setCustomRangeSliderStartAndEndDates();
        updateLowHighLabels();
       // adjustOtherInstanceRangeSliders(customRangeSlider, listOfVideoDataViewers, listOfPSMDataViewers);

    }

    private Color getColorForPressureValue(float value) {
        int colorMapSize = ColorMap.JET.length;
        float MAX_PRESSURE = 0.4f;
        float MIN_PRESSURE = 0.09f;
        int colorIndex = Math.round(((value - MIN_PRESSURE) * (colorMapSize - 1)) / (MAX_PRESSURE - MIN_PRESSURE));
        if (colorIndex < 0) {
            colorIndex = 0;
        } else if (colorIndex > colorMapSize - 1) {
            colorIndex = colorMapSize - 1;
        }

        return ColorMap.JET[colorIndex];
    }

    private void drawFrame(float[][] frameData, Canvas canvas) {
        int length = frameData.length;
        int width = frameData[0].length;

        double canvasWidth = canvas.getWidth();
        double squareSize = canvasWidth / width;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int j = 0; j < length; j++) {
            for (int i = 0; i < width; i++) {
                gc.setFill(getColorForPressureValue(frameData[j][i]));
                gc.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
            }
        }
    }

    private void drawPlaceHolderFrame(Canvas canvas, int progressPercent) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.fillText("Loading " + progressPercent + "% ...", 20, 20);
    }

    private void setCustomRangeSliderStartAndEndDates() {
        customRangeSlider.setAbsoluteStartDate(getAbsolutePSMStartDate());
        customRangeSlider.setAbsoluteEndDate(getAbsolutePSMEndDate());
    }

    Date getAbsolutePSMStartDate() {


        String stringDate = psmRecording.getFrameHeader(0, "Date").stringValue();
        String stringTime = psmRecording.getFrameHeader(0, "Time").stringValue();
        stringDate = stringDate.replaceAll("^\"|\"$", "");
        stringTime = stringTime.replaceAll("^\"|\"$", "");
        String fullDateString = stringDate.concat(" ").concat(stringTime);
        SimpleDateFormat format = new SimpleDateFormat(LONG_TIME_FORMAT);
        Date fullDate = new Date();
        try {
            fullDate = format.parse(fullDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return fullDate;
    }

    private Date getAbsolutePSMEndDate() {

//        while (!psmRecording.isParsingComplete()) {
//            try {
//                sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        absoluteStartDate = getAbsolutePSMStartDate();


        return new Date(absoluteStartDate.getTime() + (long) totalRecordingTime.toMillis());
    }


    void seek(double seekDurationValueSeconds) {
        double psmFrameRatePerSec = 18;
        frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);

    }

    private void updateLowHighLabels() {

        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        lowValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), rangeSlider.getLowValue() * 100)));
        rangeSlider.lowValueProperty().addListener((ov, old_val, new_val) -> lowValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), rangeSlider.getLowValue() * 100))));
        highValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), rangeSlider.getHighValue() * 100)));
        rangeSlider.highValueProperty().addListener((ov, old_val, new_val) -> highValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), rangeSlider.getHighValue() * 100))));
        playTime.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), timeSlider.getValue() * 100)));
        timeSlider.valueProperty().addListener((ov, old_val, new_val) -> playTime.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), timeSlider.getValue() * 100))));

    }

    private void speedHandler() {

        playbackChoiceBox.setOnAction(event -> {
            String dataSelectionValue = (String)playbackChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase("0.5")) {
                timeLine.setRate(0.5);
            } else if (dataSelectionValue.equalsIgnoreCase("1.0")) {
                timeLine.setRate(1);
            } else if (dataSelectionValue.equalsIgnoreCase("2.0")) {
                timeLine.setRate(2);
            } else if (dataSelectionValue.equalsIgnoreCase("4.0")) {
                timeLine.setRate(4);
            } else if (dataSelectionValue.equalsIgnoreCase("8.0")) {
                timeLine.setRate(8);
            }
        });
    }


    public Slider getTimeSlider() {
        return timeSlider;
    }

    Button getPlayButton() {
        return playButton;
    }

    public CustomRangeSlider getCustomRangeSlider() {
        return customRangeSlider;
    }


}
