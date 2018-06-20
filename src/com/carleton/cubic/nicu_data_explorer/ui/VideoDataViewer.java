package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;
import com.carleton.cubic.nicu_data_explorer.util.TimeUtils;
import org.controlsfx.control.RangeSlider;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VideoDataViewer
{

    private MediaPlayer mediaPlayer;
    private File mediaFile;
    private boolean loopRequested = false;
    private Duration duration;
    private boolean programmaticSliderValueChange = true;
    private Map<String, String> customMetaDataMap;
    CustomSlider customSlider = new CustomSlider();             //Not sure if this is the right place to put it.


    public VideoDataViewer(File mediaFile)
    {
        this.mediaFile = mediaFile;
        Media media = new Media(this.mediaFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);


        try
        {
            customMetaDataMap = getCustomVideoMetadata();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
//good//
    protected void updateValues(Label playTime, Slider timeSlider)
    {
        if (playTime != null && timeSlider != null)
        {
            Platform.runLater(() -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                playTime.setText(TimeUtils.formatDisplayTime(currentTime));
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
                        && !timeSlider.isValueChanging())
                {
                    programmaticSliderValueChange = true;
                    timeSlider.setValue(currentTime.toSeconds() * 10);
                    programmaticSliderValueChange = false;
                }
            });
        }
    }
//good//
    public void openWithControls(Button playButton, Slider timeSlider, MediaView mediaView, Label playTime, RangeSlider rangeSlider, Button loopButton)
    {
        mediaPlayer.currentTimeProperty().addListener(observable -> updateValues(playTime, timeSlider));

        mediaPlayer.setOnPlaying(() -> {
            customSlider.sliderLimit(timeSlider,rangeSlider);                   //Limits thumb movement of main slider.
                playButton.setText("Pause");
        });

        mediaPlayer.setOnPaused(() -> {
            playButton.setText("Play");
        });

        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            timeSlider.setMax(duration.toSeconds() * 10);
            rangeSlider.setMax(duration.toSeconds() * 10);      //Naman adds set max for range slider so the two slider have same timescale
            updateValues(playTime, timeSlider);
        });

        mediaPlayer.setCycleCount(1);
        mediaPlayer.setOnEndOfMedia(() -> {
            playButton.setText("Play");
            updateValues(playTime, timeSlider);             //End of media plays but on playing does not near the end with loops
        });

        playButton.setOnAction(e -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();

            if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED || timeSlider.getValue() == rangeSlider.getHighValue())       //Add not at end of interval condition?
            {
                // don't do anything in these states
                return;
            }

            if (status == MediaPlayer.Status.PAUSED
                    || status == MediaPlayer.Status.READY
                    || status == MediaPlayer.Status.STOPPED)
            {
                mediaPlayer.play();
            }
            else
            {
                mediaPlayer.pause();
            }

            if(customSlider.isPositionOutOfBounds(timeSlider,rangeSlider)){
                programmaticSliderValueChange = true;
                timeSlider.setValue(rangeSlider.getLowValue());
                mediaPlayer.seek(Duration.millis(rangeSlider.getLowValue()*100));
                programmaticSliderValueChange = false;
            }
        });
        loopButton.setOnAction(e -> {

            if(loopRequested == false){
                loopRequested = true;
                loopButton.setText("Loop:On");
            }
            else{
                loopRequested = false;
                loopButton.setText("Loop:Off");
            }


        });
//good ( 1 bug )//
        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange)
            {
                // Slider has value of duration in 10th of a second. Multiply by 100 to convert to ms
                mediaPlayer.seek(new Duration(timeSlider.getValue() * 100));
            }
            if(customSlider.shouldStopAtEnd(timeSlider,rangeSlider,loopRequested)){           //Checks if it needs to stop at end of interval
                mediaPlayer.pause();
            }
            if(customSlider.shouldLoopAtEnd(timeSlider,rangeSlider,loopRequested)){           //Checks if it needs to loop at end of interval
                mediaPlayer.seek(Duration.millis(rangeSlider.getLowValue()*100));
                timeSlider.adjustValue(rangeSlider.getLowValue());
                mediaPlayer.play();
            }
        });
        rangeSlider.setOnMouseDragged(drag -> {
            mediaPlayer.pause();
        });
        rangeSlider.setOnMouseClicked(click -> {
            mediaPlayer.pause();
        });
        timeSlider.setOnMouseClicked(click -> {
            mediaPlayer.pause();
        });
        timeSlider.setOnMouseDragged(drag -> {                  //These 4 set on states could be shortened into one method on custom slider?
            mediaPlayer.pause();
        });
        timeSlider.setMinorTickCount(0);
        timeSlider.setMajorTickUnit(10 * 3600);

        timeSlider.setShowTickMarks(true);
        timeSlider.setShowTickLabels(true);

        Date absoluteRecordingTime = getAbsoluteRecordingStartTime();

        timeSlider.setLabelFormatter(new StringConverter()
        {
            @Override
            public String toString(Object object)
            {
                Date tickTime = new Date();
                tickTime.setTime(absoluteRecordingTime.getTime() + (((Double)object).longValue() * 100));
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                return dateFormat.format(tickTime);
            }

            @Override
            public Double fromString(String s)
            {
                //TODO: Not sure why this is needed
                return 0.0;
            }
        });

        mediaView.setMediaPlayer(mediaPlayer);
    }
//good (1 improvement)
    public Date getAbsoluteRecordingStartTime()
    {
        String dateTimeStr = customMetaDataMap.get("Recording Start");
        if (dateTimeStr != null)
        {
            // Trim last few ms parts of the date time format from the metadata
            int fractionPartIndex = dateTimeStr.indexOf(".");
            String trimmedDateTimeStr = dateTimeStr.substring(0, fractionPartIndex + 3);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            try
            {
                return format.parse(trimmedDateTimeStr);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                return new Date();
            }
        }
        return new Date();
    }

    public Map<String, String> getCustomVideoMetadata() throws IOException
    {
        MetadataEditor mediaMeta = MetadataEditor.createFrom(mediaFile);
        Map<String, MetaValue> keyedMeta = mediaMeta.getKeyedMeta();

        Map<String, String> customMetaDataMap = new HashMap<>();
        for (String key : keyedMeta.keySet())
        {
            MetaValue value = keyedMeta.get(key);
            customMetaDataMap.put(key, value.getString());
        }

        return customMetaDataMap;
    }
}
