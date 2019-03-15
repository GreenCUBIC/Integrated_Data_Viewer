package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.DefaultInstancePackage;
import com.carleton.cubic.nicu_data_explorer.util.TimeUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.RangeSlider;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VideoDataViewer extends IntegratedDataViewerInstance {
    private MediaPlayer mediaPlayer;
    private File file;
    private Duration duration;
    private Map<String, String> customMetaDataMap;
    private boolean programmaticSliderValueChange = true;
    private MediaView mediaView;
    private Date absoluteEndDate;
    private Scene scene;
    private final static String RECORDING_START_HEADER = "recordingStart";
    private final static String LEGACY_RECORDING_START_HEADER = "Recording Start";
    private final static String DEFAULT_RECORDING_START_TIME = "2018-02-08 17:47:39.870";


    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public VideoDataViewer(File mediaFile, MediaView mediaView, DefaultInstancePackage defaultInstancePackage, Scene scene) {

        super(defaultInstancePackage);
        this.file = mediaFile;
        this.mediaView = mediaView;
        this.scene = scene;

        playbackSpeedHandler();
        try {
            customMetaDataMap = loadCustomVideoMetadata();
            promptForTextFile();
            // This will add the video metadata recording header to the map if it doesn't exist in the video.
            customMetaDataMap.putIfAbsent(RECORDING_START_HEADER, customMetaDataMap.getOrDefault(LEGACY_RECORDING_START_HEADER, DEFAULT_RECORDING_START_TIME));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Media media = new Media(this.file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

    }

    private void setPlaybackChoices() {

        playbackChoiceBox.setItems(FXCollections.observableArrayList(
                "0.5", "1.0", "2.0", "4.0", "8.0")
        );
        playbackChoiceBox.getSelectionModel().select(1);
    }


    private void playbackSpeedHandler() {

        setPlaybackChoices();
        playbackChoiceBox.setOnAction(event -> {
            String dataSelectionValue = playbackChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase("0.5")) {
                mediaPlayer.setRate(0.5);
            } else if (dataSelectionValue.equalsIgnoreCase("1.0")) {
                mediaPlayer.setRate(1);
            } else if (dataSelectionValue.equalsIgnoreCase("2.0")) {
                mediaPlayer.setRate(2);
            } else if (dataSelectionValue.equalsIgnoreCase("4.0")) {
                mediaPlayer.setRate(4);
            } else if (dataSelectionValue.equalsIgnoreCase("8.0")) {
                mediaPlayer.setRate(8);
            }
        });
    }

    private void updateValues(Label playTime, Slider timeSlider) {
        if (playTime != null && timeSlider != null) {
            Platform.runLater(() -> {

                Duration currentTime = mediaPlayer.getCurrentTime();
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
                        && !timeSlider.isValueChanging()) {
                    programmaticSliderValueChange = true;
                    timeSlider.setValue(currentTime.toSeconds() * 10);
                    programmaticSliderValueChange = false;
                }
            });
        }
    }

    public void openWithControls(MediaView mediaView, List<VideoDataViewer> listOfVideoDataViewers, List<PSMDataViewer> listOfPSMDataViewers) {


        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        mediaView.setMediaPlayer(mediaPlayer);
        Date absoluteRecordingTime = getAbsoluteRecordingStartTime();
        mediaPlayer.currentTimeProperty().addListener(observable -> updateValues(playTime, timeSlider));
        mediaPlayer.setVolume(0);
        mediaPlayer.setOnPlaying(() -> {
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
            customSlider.sliderLimit(timeSlider, rangeSlider);
            customRangeSlider.setAbsoluteStartDate(getAbsoluteRecordingStartTime());
            customRangeSlider.setAbsoluteEndDate(calculateAbsoluteEndDate());
            setCustomRangeSliderStartAndEndDates();
            adjustOtherInstanceRangeSliders(customRangeSlider, listOfVideoDataViewers, listOfPSMDataViewers);

        });

        mediaPlayer.setCycleCount(1);
        mediaPlayer.setOnEndOfMedia(() -> {
            playButton.setText(PLAY_BUTTON_STATUS_PLAY);
            updateValues(playTime, timeSlider);
        });

        playButton.setOnAction(e -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();

            if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED || timeSlider.getValue() == rangeSlider.getHighValue()) {
                return;
            }

            if (status == MediaPlayer.Status.PAUSED
                    || status == MediaPlayer.Status.READY
                    || status == MediaPlayer.Status.STOPPED) {
                mediaPlayer.play();
            } else {
                mediaPlayer.pause();
            }

            if (customSlider.isPositionOutOfBounds(timeSlider, rangeSlider)) {
                programmaticSliderValueChange = true;
                timeSlider.setValue(rangeSlider.getLowValue());
                mediaPlayer.seek(Duration.seconds(rangeSlider.getLowValue() / 10));
                programmaticSliderValueChange = false;
            }
        });
        loopButton.setOnAction(e -> {

            if (!loopRequested) {
                loopRequested = true;
                loopButton.setText(LOOP_STATUS_ON);
                customSlider.loopIfStoppedAtEnd(rangeSlider, timeSlider, mediaPlayer);

            } else {
                loopRequested = false;
                loopButton.setText(LOOP_STATUS_OFF);
            }

        });
        timeSlider.valueProperty().addListener(ov -> {
            if (!programmaticSliderValueChange) {
                mediaPlayer.seek(Duration.millis(timeSlider.getValue() * 100));
            }
            if (customSlider.shouldStopAtEnd(timeSlider, rangeSlider, loopRequested)) {
                mediaPlayer.pause();
            }
            if (customSlider.shouldLoopAtEnd(timeSlider, rangeSlider, loopRequested)) {
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


        StringConverter labelFormatterForSlider = new StringConverter() {
            @Override
            public String toString(Object timeValFromSlider) {
                Date tickTime = TimeUtils.addOffsetToTime(absoluteRecordingTime, (Double) timeValFromSlider * 100);
                return TimeUtils.getFormattedTimeWithOutMillis(tickTime);
            }

            @Override
            public Double fromString(String s) {
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
        timeSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            playTime.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(absoluteRecordingTime, timeSlider.getValue() * 100)));
        });

    }

    private void setCustomRangeSliderStartAndEndDates() {

        customRangeSlider.setAbsoluteStartDate(getAbsoluteRecordingStartTime());
        Long absoluteEndInMillis = getAbsoluteRecordingStartTime().getTime() + (long) mediaPlayer.getTotalDuration().toMillis();
        Date absoluteEndinDate = new Date(absoluteEndInMillis);
        customRangeSlider.setAbsoluteEndDate(absoluteEndinDate);
    }

    private void promptForTextFile() throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Load a .txt File?");
        alert.setContentText("Would you like to load an alternate Start Date?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(scene.getWindow());


            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String headerLine;
            String string1[] = new String[2];
            while ((headerLine = bufferedReader.readLine()) != null) {
                if (headerLine.contains("recordingStart")) {
                    string1 = headerLine.split("=");
                }
            }

            customMetaDataMap.put(RECORDING_START_HEADER, string1[1]);

        } else {
            alert.close();
        }
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
        timeSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING && !customSlider.pausedAtRangeSliderLimit(customRangeSlider.getRangeSlider(), timeSlider)) {
                setNewValuesForVideoInstances(videoDataViewers);
                setNewValuesForPSMInstances(listOfPSMDataViewers);
            }

        });
    }

    private void setNewValuesForPSMInstances(List<PSMDataViewer> listOfPSMDataViewers) {

        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {

            CustomRangeSlider customRangeSlider1 = psmDataViewer.getCustomRangeSlider();
            Date absoluteStartDate = this.getAbsoluteRecordingStartTime();
            customRangeSlider1.setLowValueUsingDate(this.customRangeSlider.getLowValueInDate(absoluteStartDate));
            customRangeSlider1.setHighValueUsingDate(this.customRangeSlider.getHighValueInDate(absoluteStartDate));
            Date sliderValueAsDate = this.convertTimeSliderValueToDate();
            psmDataViewer.setTimeSliderValueUsingDate(sliderValueAsDate);

        }
    }

    private void setNewValuesForVideoInstances(List<VideoDataViewer> videoDataViewers) {

        for (VideoDataViewer videoDataViewer : videoDataViewers) {

            CustomRangeSlider customRangeSlider1 = videoDataViewer.getCustomRangeSlider();
            Date absoluteStartDate = this.getAbsoluteRecordingStartTime();
            customRangeSlider1.setLowValueUsingDate(this.customRangeSlider.getLowValueInDate(absoluteStartDate));
            customRangeSlider1.setHighValueUsingDate(this.customRangeSlider.getHighValueInDate(absoluteStartDate));
            Date sliderValueAsDate = this.convertTimeSliderValueToDate();
            videoDataViewer.setTimeSliderValueUsingDate(sliderValueAsDate);

        }
    }

    public Date convertTimeSliderValueToDate() {

        Long currentSliderValue = (long) this.timeSlider.getValue();
        return new Date(getAbsoluteRecordingStartTime().getTime() + currentSliderValue * 100);

    }

    public void setTimeSliderValueUsingDate(Date newDate) {

        if (getAbsoluteRecordingStartTime().getTime() < newDate.getTime() && newDate.getTime() < absoluteEndDate.getTime()) {
            long dateInSliderUnits = (newDate.getTime() - this.getAbsoluteRecordingStartTime().getTime()) / 100;
            timeSlider.setValue(dateInSliderUnits);
        }

    }

    public Date getAbsoluteRecordingStartTime() {
        String dateTimeStr = customMetaDataMap.get(RECORDING_START_HEADER);
        if (dateTimeStr != null) {
            // Trim last few ms parts of the date time format from the metadata
            int fractionPartIndex = dateTimeStr.indexOf(".");
            String trimmedDateTimeStr = dateTimeStr.substring(0, fractionPartIndex + 3);
            SimpleDateFormat format = new SimpleDateFormat(LONG_TIME_FORMAT);

            try {
                return format.parse(trimmedDateTimeStr);
            } catch (ParseException e) {
                e.printStackTrace();
                return new Date();
            }
        }
        return new Date();
    }

    public Date calculateAbsoluteEndDate() {

        Long durationInSliderTime = ((long) (mediaPlayer.getTotalDuration().toMillis()));
        absoluteEndDate = new Date(getAbsoluteRecordingStartTime().getTime() + durationInSliderTime);
        return absoluteEndDate;
    }

    public Map<String, String> loadCustomVideoMetadata() throws IOException {
        MetadataEditor mediaMeta = MetadataEditor.createFrom(file);
        Map<String, MetaValue> keyedMeta = mediaMeta.getKeyedMeta();

        Map<String, String> customMetaDataMap = new HashMap<>();
        for (String key : keyedMeta.keySet()) {
            MetaValue value = keyedMeta.get(key);
            customMetaDataMap.put(key, value.getString());
        }

        return customMetaDataMap;
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    public CustomRangeSlider getCustomRangeSlider() {
        return customRangeSlider;
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

    public Scene getScene() {
        return scene;
    }


    public Date getAbsoluteEndDate() {
        return absoluteEndDate;
    }

    public void setAbsoluteEndDate(Date absoluteEndDate) {
        this.absoluteEndDate = absoluteEndDate;
    }
}
