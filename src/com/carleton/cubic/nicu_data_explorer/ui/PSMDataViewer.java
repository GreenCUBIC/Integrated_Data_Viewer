package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

import java.io.File;

public class PSMDataViewer
{

    private XsensorASCIIParser xsensorASCIIParser;
    private PSMRecording psmRecording;
    private boolean programmaticSliderValueChange = false;

    private float MAX_PRESSURE = 0.4f;
    private float MIN_PRESSURE = 0.09f;

    public PSMDataViewer(File psmFile)
    {
        xsensorASCIIParser = new XsensorASCIIParser(psmFile);
    }


    public void openWithControls(Canvas canvas, Slider timeSlider, Button playButton, Label playTime)
    {

        double psmFrameRatePerSec = 18; //TODO: How to get this from the PSM file?
        int totalNumberOfFrames = xsensorASCIIParser.parseForLastFrameNumber();
        Duration totalRecordingTime = Duration.seconds(totalNumberOfFrames / psmFrameRatePerSec);
        timeSlider.setMax(totalRecordingTime.toSeconds() * 10); //slider value is tenth of a second
        psmRecording = xsensorASCIIParser.parse();

        int[] frameIndex = new int[]{0};
        final Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(1000 / psmFrameRatePerSec),
                        event -> {
                            if (frameIndex[0] < psmRecording.frameCount())
                            {
                                drawFrame(psmRecording.getFrameData(frameIndex[0]), canvas);
                                frameIndex[0]++;
                                Duration currentFrameTime = Duration.seconds(frameIndex[0] / psmFrameRatePerSec);
                                playTime.setText(TimeUtils.formattedDurationForDisplay(currentFrameTime));
                                programmaticSliderValueChange = true;
                                timeSlider.setValue(currentFrameTime.toSeconds() * 10);
                                programmaticSliderValueChange = false;
                            }
                            else
                            {
                                int progressPercent = (int)((psmRecording.frameCount() * 100.0)/frameIndex[0]);
                                drawPlaceHolderFrame(canvas, progressPercent);
                            }
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);

        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange)
            {
                double seekDurationValueSeconds = timeSlider.getValue() / 10;
                frameIndex[0] = (int) (seekDurationValueSeconds * psmFrameRatePerSec);
            }
        });

        playButton.setOnAction(e -> {
            Animation.Status timeLineStatus = timeline.getStatus();
            if (timeLineStatus == Animation.Status.PAUSED ||
                    timeLineStatus == Animation.Status.STOPPED)
            {
                timeline.play();
                playButton.setText("Pause");
            }
            else
            {
                timeline.pause();
                playButton.setText("Play");
            }
        });

        while (psmRecording.frameCount() <= 0)
        {
            //Wait until some frames are parsed
        }

        // Draw first frame
        if (frameIndex[0] < psmRecording.frameCount())
        {
            drawFrame(psmRecording.getFrameData(frameIndex[0]), canvas);
            frameIndex[0]++;
        }
    }

    private Color getColorForPressureValue(float value)
    {
        int colorMapSize = ColorMap.JET.length;
        int colorIndex = Math.round(((value - MIN_PRESSURE) * (colorMapSize - 1)) / (MAX_PRESSURE - MIN_PRESSURE));
        if (colorIndex < 0)
        {
            colorIndex = 0;
        }
        else if (colorIndex > colorMapSize - 1)
        {
            colorIndex = colorMapSize - 1;
        }

        return ColorMap.JET[colorIndex];
    }

    private void drawFrame(float[][] frameData, Canvas canvas)
    {
        int length = frameData.length;
        int width = frameData[0].length;

        double canvasWidth = canvas.getWidth();
        double squareSize = canvasWidth / width;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int j = 0; j < length; j++)
        {
            for (int i = 0; i < width; i++)
            {
                gc.setFill(getColorForPressureValue(frameData[j][i]));
                gc.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
            }
        }
    }

    private void drawPlaceHolderFrame(Canvas canvas, int progressPercent)
    {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.fillText("Loading " + progressPercent + "% ...", 20, 20);
    }

}
