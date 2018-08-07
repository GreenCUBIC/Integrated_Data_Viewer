package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import com.carleton.cubic.nicu_data_explorer.util.ColorMap;
import com.carleton.cubic.nicu_data_explorer.util.PSMRecording;
import com.carleton.cubic.nicu_data_explorer.util.TimeUtils;
import com.carleton.cubic.nicu_data_explorer.util.XsensorASCIIParser;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PSMDataViewer {

    private XsensorASCIIParser xsensorASCIIParser;
    private PSMRecording psmRecording;
    private boolean loopRequested = false;
    private Canvas canvas;
    private Slider timeSlider;
    private Button playButton;
    private Button loopButton;
    private Label playTime;
    private RangeSlider rangeSlider;
    private Scene scene;
    private CustomSlider customSlider = new CustomSlider();
    private boolean programmaticSliderValueChange = false;

    private final static String LOOP_STATUS_ON = "Loop:On";
    private final static String LOOP_STATUS_OFF = "Loop:Off";
    private final static String LONG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


    private float MAX_PRESSURE = 0.4f;
    private float MIN_PRESSURE = 0.09f;
    private Date absoluteStartDate;
    private int[] frameIndex;
    private double psmFrameRatePerSec = 18;

    public PSMDataViewer(File psmFile) {
        xsensorASCIIParser = new XsensorASCIIParser(psmFile);
    }


    public void openWithControls(Canvas canvas, Slider timeSlider, Button playButton, Label playTime, RangeSlider rangeSlider, Button loopButton, Scene scene) {
        this.canvas = canvas;
        this.timeSlider = timeSlider;
        this.playButton = playButton;
        this.playTime = playTime;
        this.rangeSlider = rangeSlider;
        this.loopButton = loopButton;
        this.scene = scene;

        double psmFrameRatePerSec = 18; //TODO: How to get this from the PSM file?
        int totalNumberOfFrames = xsensorASCIIParser.parseForLastFrameNumber();
        customSlider.sliderLimit(timeSlider, rangeSlider);
        Duration totalRecordingTime = Duration.seconds(totalNumberOfFrames / psmFrameRatePerSec);
        timeSlider.setMax(totalRecordingTime.toSeconds() * 10); //slider value is tenth of a second
        rangeSlider.setMax(totalRecordingTime.toSeconds() * 10);
        rangeSlider.setLowValue(0);
        rangeSlider.setHighValue(rangeSlider.getMax());
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

        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange) {
                double seekDurationValueSeconds = timeSlider.getValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
            }
            if (customSlider.shouldStopAtEnd(timeSlider, rangeSlider, loopRequested)) {
                timeline.pause();
            }
            if (customSlider.shouldLoopAtEnd(timeSlider, rangeSlider, loopRequested)) {
                double seekDurationValueSeconds = rangeSlider.getLowValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
                timeSlider.adjustValue(rangeSlider.getLowValue());
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
            } else if (timeSlider.getValue() == rangeSlider.getHighValue()) {

                return;

            } else {
                timeline.pause();
                playButton.setText("Play");
            }

            if (customSlider.isPositionOutOfBounds(timeSlider, rangeSlider)) {
                programmaticSliderValueChange = true;
                timeSlider.setValue(rangeSlider.getLowValue());
                double seekDurationValueSeconds = rangeSlider.getLowValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
                programmaticSliderValueChange = false;
            }
        });
        loopButton.setOnAction(e -> {

            if (!loopRequested) {
                loopRequested = true;
                loopButton.setText(LOOP_STATUS_ON);
                customSlider.loopIfStoppedAtEndPSM(rangeSlider, timeSlider, frameIndex, psmFrameRatePerSec);

            } else {
                loopRequested = false;
                loopButton.setText(LOOP_STATUS_OFF);
            }

        });

        rangeSlider.setOnMouseDragged(drag -> {
            timeline.pause();
            playButton.setText("Play");
        });
        rangeSlider.setOnMouseClicked(click -> {
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


        String stringDate = psmRecording.getFrameHeader(frameIndex.length-1, "Date").stringValue();
        String stringTime = psmRecording.getFrameHeader(frameIndex.length-1, "Time").stringValue();
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


    public void seek(double seekDurationValueSeconds) {
        frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);

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

    public RangeSlider getRangeSlider() {
        return rangeSlider;
    }

    public Scene getScene() {
        return scene;
    }
}
