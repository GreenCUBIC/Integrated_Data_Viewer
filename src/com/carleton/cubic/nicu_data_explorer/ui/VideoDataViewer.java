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
    private boolean stopRequested = false;
    private Duration duration;
    private boolean programmaticSliderValueChange = true;
    private Map<String, String> customMetaDataMap;


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

    public void openWithControls(Button playButton, Slider timeSlider, MediaView mediaView, Label playTime)
    {
        mediaPlayer.currentTimeProperty().addListener(observable -> updateValues(playTime, timeSlider));

        mediaPlayer.setOnPlaying(() -> {
            if (stopRequested)
            {
                mediaPlayer.pause();
                stopRequested = false;
            }
            else
            {
                playButton.setText("Pause");
            }
        });

        mediaPlayer.setOnPaused(() -> playButton.setText("Play"));

        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            timeSlider.setMax(duration.toSeconds() * 10);
            updateValues(playTime, timeSlider);
        });

        mediaPlayer.setCycleCount(1);
        mediaPlayer.setOnEndOfMedia(() -> stopRequested = true);

        playButton.setOnAction(e -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();

            if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED)
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
        });

        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange)
            {
                // Slider has value of duration in 10th of a second. Multiply by 100 to convert to ms
                mediaPlayer.seek(new Duration(timeSlider.getValue() * 100));
            }
        });

        timeSlider.setMinorTickCount(0);
        timeSlider.setMajorTickUnit(10 * 3600);

        timeSlider.setShowTickMarks(true);
        timeSlider.setShowTickLabels(true);

        Date absoluteRecordingTime = getAbsoluteRecordingStartTime();

        timeSlider.setLabelFormatter(new StringConverter<>()
        {
            @Override
            public String toString(Double n)
            {
                Date tickTime = new Date();
                tickTime.setTime(absoluteRecordingTime.getTime() + (n.longValue() * 100));
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
