package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.TimeUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.RangeSlider;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;

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
    private CustomSlider customSlider = new CustomSlider();

    private final static String RECORDING_START_HEADER = "recordingStart";
    private final static String LEGACY_RECORDING_START_HEADER = "Recording Start";
    private final static String LOOP_STATUS_ON = "Loop:On";
    private final static String LOOP_STATUS_OFF = "Loop:Off";
    private final static String PLAY_BUTTON_STATUS_PLAY = "Play";
    private final static String PLAY_BUTTON_STATUS_PAUSE = "Pause";
    private final static String LONG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private final static String DEFAULT_RECODRING_START_TIME = "2018-06-26 11:36:30.370";







    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    public VideoDataViewer(File mediaFile)
    {
        this.mediaFile = mediaFile;

        try
        {
            customMetaDataMap = loadCustomVideoMetadata();
            customMetaDataMap.putIfAbsent(RECORDING_START_HEADER, customMetaDataMap.getOrDefault(LEGACY_RECORDING_START_HEADER,DEFAULT_RECODRING_START_TIME));

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Media media = new Media(this.mediaFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);


    }

    protected void updateValues(Label playTime, Slider timeSlider)
    {
        if (playTime != null && timeSlider != null)
        {
            Platform.runLater(() -> {

                Duration currentTime = mediaPlayer.getCurrentTime();
                playTime.setText(TimeUtils.formattedDurationForDisplay(currentTime));
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

    public void openWithControls(Button playButton, Slider timeSlider, MediaView mediaView, Label playTime,
                                 RangeSlider rangeSlider, Button loopButton, Label lowValText, Label highValText)
    {
        mediaView.setMediaPlayer(mediaPlayer);

        Date absoluteRecordingTime = getAbsoluteRecordingStartTime();
        mediaPlayer.currentTimeProperty().addListener(observable -> updateValues(playTime, timeSlider));
        mediaPlayer.setOnPlaying(() -> {
            customSlider.sliderLimit(timeSlider, rangeSlider);
            playButton.setText(PLAY_BUTTON_STATUS_PAUSE);
        });

        mediaPlayer.setOnPaused(() -> {
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);
        });
        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            timeSlider.setMax(duration.toSeconds() * 10);
            rangeSlider.setMax(duration.toSeconds() * 10);
            rangeSlider.setLowValue(0);
            rangeSlider.setHighValue(rangeSlider.getMax());
            updateValues(playTime, timeSlider);
        });

        mediaPlayer.setCycleCount(1);
        mediaPlayer.setOnEndOfMedia(() -> {
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);
            updateValues(playTime, timeSlider);
        });

        playButton.setOnAction(e -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();

            if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED || timeSlider.getValue() == rangeSlider.getHighValue())
            {
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

            if (customSlider.isPositionOutOfBounds(timeSlider, rangeSlider))
            {
                programmaticSliderValueChange = true;
                timeSlider.setValue(rangeSlider.getLowValue());
                mediaPlayer.seek(Duration.seconds(rangeSlider.getLowValue() / 10));
                programmaticSliderValueChange = false;
            }
        });
        loopButton.setOnAction(e -> {

            if (!loopRequested)
            {
                loopRequested = true;
                loopButton.setText(LOOP_STATUS_ON);
                customSlider.loopIfStoppedAtEnd(rangeSlider,timeSlider,mediaPlayer);

            }
            else
            {
                loopRequested = false;
                loopButton.setText(LOOP_STATUS_OFF);
            }

        });
        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange)
            {
                mediaPlayer.seek(Duration.millis(timeSlider.getValue() * 100));
            }
            if (customSlider.shouldStopAtEnd(timeSlider, rangeSlider, loopRequested))
            {
                mediaPlayer.pause();
            }
            if (customSlider.shouldLoopAtEnd(timeSlider, rangeSlider, loopRequested))
            {
                mediaPlayer.seek(Duration.millis(rangeSlider.getLowValue() * 100));
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
        timeSlider.setOnMouseDragged(drag -> {
            mediaPlayer.pause();
        });
        timeSlider.setMinorTickCount(0);
        timeSlider.setMajorTickUnit(10 * 3600);
        timeSlider.setShowTickMarks(true);
        timeSlider.setShowTickLabels(true);

        rangeSlider.setMajorTickUnit(10 * 600); // Every ten minute
        rangeSlider.setShowTickMarks(true);


        StringConverter labelFormatterForSlider = new StringConverter()
        {
            @Override
            public String toString(Object timeValFromSlider)
            {
                Date tickTime = TimeUtils.addOffsetToTime(absoluteRecordingTime, (Double) timeValFromSlider * 100);
                return TimeUtils.getFormattedTimeWithOutMillis(tickTime);
            }

            @Override
            public Double fromString(String s)
            {
                //TODO: Not sure why this is needed
                return 0.0;
            }
        };

        timeSlider.setLabelFormatter(labelFormatterForSlider);
        rangeSlider.setLabelFormatter(labelFormatterForSlider);

        rangeSlider.lowValueProperty().addListener((ov, old_val, new_val) -> {
            lowValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(absoluteRecordingTime, rangeSlider.getLowValue() * 100)));
        });
        rangeSlider.highValueProperty().addListener((ov, old_val, new_val) -> {
            highValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(absoluteRecordingTime, rangeSlider.getHighValue() * 100)));
        });

    }

    public Date getAbsoluteRecordingStartTime()
    {
        String dateTimeStr = customMetaDataMap.get(RECORDING_START_HEADER);
        if (dateTimeStr != null)
        {
            // Trim last few ms parts of the date time format from the metadata
            int fractionPartIndex = dateTimeStr.indexOf(".");
            String trimmedDateTimeStr = dateTimeStr.substring(0, fractionPartIndex + 3);
            SimpleDateFormat format = new SimpleDateFormat(LONG_TIME_FORMAT);

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

    public Map<String, String> loadCustomVideoMetadata() throws IOException
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

    /*public void addCustomVideoMetadata(String key, String value, File mediaFile) throws IOException
    {
        MetadataEditor mediaMeta = MetadataEditor.createFrom(mediaFile);
        Map<String, MetaValue> keyedMeta = mediaMeta.getKeyedMeta();
        keyedMeta.put(key, MetaValue.createString(value));
        mediaMeta.save(true);
    }*/



}
