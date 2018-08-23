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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class PSMDataViewer {

    private XsensorASCIIParser xsensorASCIIParser;
    private PSMRecording psmRecording;
    private boolean loopRequested = false;
    private Canvas canvas;
    private Slider timeSlider;
    private Button playButton;
    private Button loopButton;
    private Label playTime;
    private Label lowValText;
    private Label highValText;
    private Duration totalRecordingTime;
    private CustomRangeSlider customRangeSlider;
    private Scene scene;
    private Timeline timeLine;
    private CustomSlider customSlider = new CustomSlider();
    private boolean programmaticSliderValueChange = false;
    private boolean locked = false;
    private ChoiceBox<String> playbackChoiceBox;


    private final static String LOOP_STATUS_ON = "Loop:On";
    private final static String LOOP_STATUS_OFF = "Loop:Off";
    private final static String LONG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


    private float MAX_PRESSURE = 0.4f;
    private float MIN_PRESSURE = 0.09f;
    private Date absoluteStartDate;
    private int[] frameIndex;
    private double psmFrameRatePerSec = 18;

    public PSMDataViewer(File psmFile, SliderAndButtonPackage sliderAndButtonPackage) {
        this.timeSlider = sliderAndButtonPackage.getTimeSlider();
        this.playButton = sliderAndButtonPackage.getPlayButton();
        this.customRangeSlider = sliderAndButtonPackage.getCustomRangeSlider();
        this.loopButton = sliderAndButtonPackage.getLoopButton();
        this.playbackChoiceBox = sliderAndButtonPackage.getPlaybackChoiceBox();
        xsensorASCIIParser = new XsensorASCIIParser(psmFile);

        playbackChoiceBox.setItems(FXCollections.observableArrayList(
                "0.5","1.0","2.0","4.0","8.0")
        );
        playbackChoiceBox.getSelectionModel().select(1);
    }


    public void openWithControls(Canvas canvas, Label playTime, Scene scene,Label lowValText,Label highValText,List<VideoDataViewer> listOfVideoDataViewers,List<PSMDataViewer> listOfPSMDataViewers) {
        this.canvas = canvas;
        this.playTime = playTime;
        this.scene = scene;
        this.lowValText =lowValText;
        this.highValText = highValText;

        double psmFrameRatePerSec = 18; //TODO: How to get this from the PSM file?
        int totalNumberOfFrames = xsensorASCIIParser.parseForLastFrameNumber();
        customSlider.sliderLimit(timeSlider, customRangeSlider.getRangeSlider());
        totalRecordingTime = Duration.seconds(totalNumberOfFrames / psmFrameRatePerSec);
        timeSlider.setMax(totalRecordingTime.toSeconds() * 10); //slider value is tenth of a second
        customRangeSlider.getRangeSlider().setMax(totalRecordingTime.toSeconds() * 10);
        customRangeSlider.getRangeSlider().setLowValue(0);
        customRangeSlider.getRangeSlider().setHighValue(customRangeSlider.getRangeSlider().getMax());
        psmRecording = xsensorASCIIParser.parse();

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
                playButton.setText("Pause");
            } else if (timeSlider.getValue() == customRangeSlider.getRangeSlider().getHighValue()) {

                return;

            } else {
                timeline.pause();
                playButton.setText("Play");
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
            playButton.setText("Play");
        });
        customRangeSlider.getRangeSlider().setOnMouseClicked(click -> {
            timeline.pause();
            playButton.setText("Play");

        });
        timeSlider.setOnMouseClicked(click -> {
            timeline.pause();
            playButton.setText("Play");

        });
        timeSlider.setOnMouseDragged(drag -> {
            timeline.pause();
            playButton.setText("Play");

        });

        while (psmRecording.frameCount() <= 0) {
            //Wait until some frames are parsed
        }

        // Draw first frame
        if (frameIndex[0] < psmRecording.frameCount()) {
            drawFrame(psmRecording.getFrameData(frameIndex[0]), canvas);
            frameIndex[0]++;
        }
        while (!psmRecording.isParsingComplete()) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (psmRecording.isParsingComplete()) {
            setCustomRangeSliderStartAndEndDates();
            updateLowHighLabels();
            adjustOtherInstanceRangeSliders(customRangeSlider, listOfVideoDataViewers, listOfPSMDataViewers);
        }
    }

    private Color getColorForPressureValue(float value) {
        int colorMapSize = ColorMap.JET.length;
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

    public Date getAbsolutePSMStartDate() {


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

    public Date getAbsolutePSMEndDate() {

        while (!psmRecording.isParsingComplete()) {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            absoluteStartDate = getAbsolutePSMStartDate();
         Date absoluteEndDate = new Date(absoluteStartDate.getTime()+(long)totalRecordingTime.toMillis());
        

        return absoluteEndDate;
    }


    public void seek(double seekDurationValueSeconds) {
        frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);

    }

    private void adjustOtherInstanceRangeSliders(CustomRangeSlider customRangeSlider, List<VideoDataViewer> videoDataViewers, List<PSMDataViewer> listOfPSMDataViewers) {

        customRangeSlider.getRangeSlider().lowValueProperty().addListener((ov, old_val, new_val) -> {

            setNewValuesForVideoInstances(videoDataViewers);
            setNewValuesForPSMInstances(listOfPSMDataViewers);

        });
        customRangeSlider.getRangeSlider().highValueProperty().addListener((ov, old_val, new_val) -> {

            setNewValuesForVideoInstances(videoDataViewers);
            setNewValuesForPSMInstances(listOfPSMDataViewers);


        });
    }

    private void updateLowHighLabels() {

        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();

        rangeSlider.lowValueProperty().addListener((ov, old_val, new_val) -> {
            lowValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), rangeSlider.getLowValue() * 100)));
        });

        rangeSlider.highValueProperty().addListener((ov, old_val, new_val) -> {
            highValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), rangeSlider.getHighValue() * 100)));
        });

        timeSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            playTime.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(getAbsolutePSMStartDate(), timeSlider.getValue() * 100)));
        });

    }

    private void setNewValuesForPSMInstances(List<PSMDataViewer> listOfPSMDataViewers) {

        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {

            CustomRangeSlider customRangeSlider1 = psmDataViewer.getCustomRangeSlider();
            Date absoluteStartDate = this.getAbsolutePSMStartDate();
            customRangeSlider1.setLowValueUsingDate(this.customRangeSlider.getLowValueInDate(absoluteStartDate));
            customRangeSlider1.setHighValueUsingDate(this.customRangeSlider.getHighValueInDate(absoluteStartDate));
        }
    }

    private void setNewValuesForVideoInstances(List<VideoDataViewer> videoDataViewers) {

        for (VideoDataViewer videoDataViewer2 : videoDataViewers) {     //TODO this is interfering with scaling

            CustomRangeSlider customRangeSlider1 = videoDataViewer2.getCustomRangeSlider();
            Date absoluteStartDate = this.getAbsolutePSMStartDate();
            customRangeSlider1.setLowValueUsingDate(this.customRangeSlider.getLowValueInDate(absoluteStartDate));
            customRangeSlider1.setHighValueUsingDate(this.customRangeSlider.getHighValueInDate(absoluteStartDate));
        }

    }
    private void speedHandler() {

        playbackChoiceBox.setOnAction(event->{
            String dataSelectionValue = playbackChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase("0.5")) {
                timeLine.setRate(0.5);
            }else if(dataSelectionValue.equalsIgnoreCase("1.0")){
                timeLine.setRate(1);
            }
            else if(dataSelectionValue.equalsIgnoreCase("2.0")){
                timeLine.setRate(2);
            }
            else if(dataSelectionValue.equalsIgnoreCase("4.0")){
                timeLine.setRate(4);
            }
            else if(dataSelectionValue.equalsIgnoreCase("8.0")){
                timeLine.setRate(8);
            }
        });
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Slider getTimeSlider() {
        return timeSlider;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public Button getLoopButton() {
        return loopButton;
    }

    public CustomRangeSlider getCustomRangeSlider() {
        return customRangeSlider;
    }

    public Scene getScene() {
        return scene;
    }

    public boolean isLocked() {
        return locked;
    }
}
