package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class Controller {

    @FXML
    private MediaView mediaView;
    @FXML
    private Button playButton;
    @FXML
    private Button loopButton;
    @FXML
    private Slider timeSlider;
    @FXML
    private Label playTime;
    @FXML
    private Label lowValText;
    @FXML
    private Label highValText;
    @FXML
    private ChoiceBox dataChoiceBox;
    @FXML
    private Button fileLoadButton;
    @FXML
    private Canvas psmCanvas;
    @FXML
    private RangeSlider rangeSlider;
    @FXML
    private TextField scaleTextField;
    @FXML
    private CheckBox scaleCheckBox;
    private AnnotationTableHandler annotationTableHandler;
    private SlideScaler slideScaler;
    private VideoDataViewer videoDataViewer;
    private PSMDataViewer psmDataViewer;

    public Controller() {

    }

    private static final String VIDEO_SELECTOR_LABEL = "Video";
    private static final String PSM_SELECTOR_LABEL = "PSM";
    private static final String SYNCHRONIZED_SELECTOR_LABEL = "Synchronized";
    private static final String ANNOTATION_SELECTOR_LABEL = "Annotation";


    @FXML
    public void initialize() {
        dataChoiceBox.setItems(FXCollections.observableArrayList(
                VIDEO_SELECTOR_LABEL, PSM_SELECTOR_LABEL, SYNCHRONIZED_SELECTOR_LABEL,
                ANNOTATION_SELECTOR_LABEL)
        );

        dataChoiceBox.getSelectionModel().selectFirst();

        fileLoadButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
            if (file == null) {
                return;
            }

            String dataSelectionValue = (String) dataChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase(VIDEO_SELECTOR_LABEL)) {
                videoDataViewer = new VideoDataViewer(file);
                videoDataViewer.openWithControls(playButton, timeSlider, mediaView, playTime, rangeSlider, loopButton, lowValText, highValText);
            } else if (dataSelectionValue.equalsIgnoreCase(PSM_SELECTOR_LABEL)) {
                psmDataViewer = new PSMDataViewer(file);
                psmDataViewer.openWithControls(psmCanvas, timeSlider, playButton, playTime);
            } else if (dataSelectionValue.equalsIgnoreCase(ANNOTATION_SELECTOR_LABEL)) {
                try {
                    loadAnnotationTable(file);
                    scaleAndSetAnnotationOnSlider();
                    annotationTableHandler.SaveAndUpdateButtonHandler(rangeSlider, videoDataViewer);
                    slideScaler = new SlideScaler();
                    slideScaler.calculateAbsoluteStartEndTimes(videoDataViewer);

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void loadAnnotationTable(File file) throws IOException, ParseException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("annotations.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("Annotation Table");
        stage.setScene(scene);
        stage.show();
        annotationTableHandler = new AnnotationTableHandler(scene);
        annotationTableHandler.setAnnotationData(file);
    }

    public void scaleAndSetAnnotationOnSlider() {
        annotationTableHandler.getAnnotationTable().setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 1) {
                try {
                    slideScaler.calculateRelativeScalingBoundaries(annotationTableHandler, scaleCheckBox, scaleTextField);
                    slideScaler.setRelativeSliderBoundaries(timeSlider, rangeSlider, videoDataViewer.getMediaPlayer());
                    annotationTableHandler.calculateAbsoluteVideoStartDate(videoDataViewer);
                    annotationTableHandler.setAnnotationOnSlider(videoDataViewer, rangeSlider, slideScaler);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
