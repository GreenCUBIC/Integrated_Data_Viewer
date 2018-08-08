package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {


    @FXML
    private Label playTime;
    @FXML
    private Label lowValText;
    @FXML
    private Label highValText;
    @FXML
    private ChoiceBox<String> dataChoiceBox;
    @FXML
    private Button fileLoadButton;
    @FXML
    private TextField scaleTextField;
    @FXML
    private CheckBox scaleCheckBox;

    private ToggleButton synchronizeButton;
    private AnnotationTableHandler annotationTableHandler;
    private SlideScaler slideScaler;
    private VideoDataViewer videoDataViewerInstance;
    private List<VideoDataViewer> listOfVideoDataViewers = new ArrayList<>();
    private List<PSMDataViewer> listOfPSMDataViewers = new ArrayList<>();
    private PSMDataViewer psmDataViewerInstance;

    private boolean synchronization = false;

    public Controller() {

    }

    private static final String VIDEO_SELECTOR_LABEL = "Video";
    private static final String PSM_SELECTOR_LABEL = "PSM";
    private static final String ANNOTATION_SELECTOR_LABEL = "Annotation";


    @FXML
    public void initialize() {


        loadFile();

        dataChoiceBox.setItems(FXCollections.observableArrayList(
                VIDEO_SELECTOR_LABEL, PSM_SELECTOR_LABEL, ANNOTATION_SELECTOR_LABEL)
        );

        dataChoiceBox.getSelectionModel().selectFirst();

        fileLoadButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            setFileExtension(dataChoiceBox, fileChooser);

            File file = fileChooser.showOpenDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
            if (file == null) {
                return;
            }

            String dataSelectionValue = dataChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase(VIDEO_SELECTOR_LABEL)) {

                loadVideoInstance(file);

            } else if (dataSelectionValue.equalsIgnoreCase(PSM_SELECTOR_LABEL)) {

                loadPSMInstance(file);

            } else if (dataSelectionValue.equalsIgnoreCase(ANNOTATION_SELECTOR_LABEL)) {

                createAnnotationInstance(file);


            }
        });

        synchronizeButton.setOnAction(actionEvent -> {

            synchronization = synchronizeButton.isSelected();

            if (synchronization) {
                synchronizeButton.setText("Synchro Is Active");
            } else {
                synchronizeButton.setText("Synchro Is Inactive");
            }
            synchronizeSliders();

        });

    }

    private void createAnnotationInstance(File file) {

        try {

            createAnnotationTable(file);
            scaleAndSetAnnotationsOnInstances();
            if (videoDataViewerInstance != null) {
                for (VideoDataViewer videoDataViewer : listOfVideoDataViewers) {

                    slideScaler = new SlideScaler();
                    slideScaler.calculateAbsoluteVideoStartEndTimes(videoDataViewer);
                    videoDataViewer.getScene().setOnMouseClicked(event -> {
                        annotationTableHandler.SaveAndUpdateButtonHandler(getSelectedInstanceType(),getSelectedRangeSlider());
                    });
                }
            }
            if (psmDataViewerInstance != null) {
                for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {
                    slideScaler = new SlideScaler();
                    slideScaler.calculateAbsolutePSMStartEndTimes(psmDataViewer);
                    psmDataViewer.getScene().setOnMouseClicked(event -> {
                        annotationTableHandler.SaveAndUpdateButtonHandler(getSelectedInstanceType(),getSelectedRangeSlider());
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setFileExtension(ChoiceBox<String> dataChoiceBox, FileChooser fileChooser) {

        int selectedChoice = dataChoiceBox.getSelectionModel().getSelectedIndex() + 1;
        FileChooser.ExtensionFilter extFilter;

        switch (selectedChoice) {

            case 1:
                extFilter = new FileChooser.ExtensionFilter("MP4 files (*.mp4)", "*.mp4");
                fileChooser.getExtensionFilters().add(extFilter);
                break;
            case 2:
                extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                break;
            case 3:
                extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
                fileChooser.getExtensionFilters().add(extFilter);
                break;


        }


    }

    private void scaleAndSetAnnotationsOnInstances() {
        annotationTableHandler.getAnnotationTable().setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 1) {
                if (videoDataViewerInstance != null) {
                    scaleAndSetAnnotationOnVideoSliders();
                }
                if (psmDataViewerInstance != null) {
                    scaleAndSetAnnotationOnPSMSliders();
                }


            }
        });
    }

    private void scaleAndSetAnnotationOnVideoSliders() {

        slideScaler.calculateRelativeScalingBoundaries(annotationTableHandler, scaleCheckBox, scaleTextField);
        annotationTableHandler.scaleAndSetAnnotationsPerVideo(listOfVideoDataViewers, slideScaler, annotationTableHandler);

    }

    private void scaleAndSetAnnotationOnPSMSliders() {

        slideScaler.calculateAbsolutePSMStartEndTimes(psmDataViewerInstance);
        annotationTableHandler.scaleAndSetAnnotationsPerPSM(listOfPSMDataViewers, slideScaler, annotationTableHandler);

    }

    private void loadVideoInstance(File file) {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("VideoInstance.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);

        MediaView mediaViewInstance = (MediaView) scene.lookup("#mediaView");
        Slider sliderInstance = (Slider) scene.lookup("#mainSlider");
        RangeSlider rangeSliderInstance = (RangeSlider) scene.lookup("#rangeSlider");
        CustomRangeSlider customRangeSliderInstance = new CustomRangeSlider(rangeSliderInstance);
        Button loopButtonInstance = (Button) scene.lookup("#loopButton");
        Button playButtonInstance = (Button) scene.lookup("#playButton");
        CheckBox sliderLockCheckBox = (CheckBox) scene.lookup("#sliderLockCheckBox");

        stage.show();

        videoDataViewerInstance = new VideoDataViewer(file, mediaViewInstance, sliderInstance, customRangeSliderInstance, loopButtonInstance, playButtonInstance, scene, sliderLockCheckBox);
        videoDataViewerInstance.openWithControls(playButtonInstance, sliderInstance, mediaViewInstance, playTime, rangeSliderInstance, loopButtonInstance, lowValText, highValText);


        listOfVideoDataViewers.add(videoDataViewerInstance);
        synchronizeSliders();
    }

    private void loadPSMInstance(File file) {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("PSMInstance.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);

        Canvas canvasInstance = (Canvas) scene.lookup("#canvas");
        Slider sliderInstance = (Slider) scene.lookup("#mainSlider");
        RangeSlider rangeSliderInstance = (RangeSlider) scene.lookup("#rangeSlider");
        CustomRangeSlider customRangeSliderInstance = new CustomRangeSlider(rangeSliderInstance);
        Button loopButtonInstance = (Button) scene.lookup("#loopButton");
        Button playButtonInstance = (Button) scene.lookup("#playButton");

        stage.show();


        psmDataViewerInstance = new PSMDataViewer(file);
        psmDataViewerInstance.openWithControls(canvasInstance, sliderInstance, playButtonInstance, playTime, customRangeSliderInstance, loopButtonInstance, scene);

        listOfPSMDataViewers.add(psmDataViewerInstance);
        synchronizeSliders();
    }


    private void createAnnotationTable(File file) throws IOException {
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


    private void loadFile() {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("loadFile.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);

        dataChoiceBox = (ChoiceBox<String>) scene.lookup("#dataChoiceBox");
        fileLoadButton = (Button) scene.lookup("#fileLoadButton");
        scaleCheckBox = (CheckBox) scene.lookup("#scaleCheckBox");
        scaleTextField = (TextField) scene.lookup("#scaleTextField");
        synchronizeButton = (ToggleButton) scene.lookup("#synchronizeButton");
        synchronization = synchronizeButton.isSelected();
        stage.show();

    }

    private void synchronizeSliders() {


        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {

            CustomRangeSlider rangeSlider = psmDataViewer.getCustomRangeSlider();
            rangeSlider.setAbsoluteStartDate(psmDataViewer.getAbsolutePSMStartDate());
            rangeSlider.setAbsoluteEndDate(psmDataViewer.getAbsolutePSMEndDate());
            setValueForOtherRangeSliders(rangeSlider);
        }
        for (VideoDataViewer videoDataViewer : listOfVideoDataViewers) {

            CustomRangeSlider rangeSlider = videoDataViewer.getRangeSlider();
            rangeSlider.setAbsoluteStartDate(videoDataViewer.getAbsoluteRecordingStartTime());
            rangeSlider.setAbsoluteEndDate(videoDataViewer.getAbsoluteRecordingEndTime());
            setValueForOtherRangeSliders(rangeSlider);
        }


    }

    private void setValueForOtherRangeSliders(CustomRangeSlider customRangeSlider) {

        customRangeSlider.getRangeSlider().lowValueProperty().addListener((ov, old_val, new_val) -> {
            if (synchronization) {
                for (PSMDataViewer psmDataViewer2 : listOfPSMDataViewers) {

                    CustomRangeSlider rangeSlider2 = psmDataViewer2.getCustomRangeSlider();
                    rangeSlider2.setLowValueUsingDate(psmDataViewer2.getAbsolutePSMStartDate(),rangeSlider2.getAbsoluteStartDate());

                }
                for (VideoDataViewer videoDataViewer2 : listOfVideoDataViewers) {

                    CustomRangeSlider rangeSlider2 = videoDataViewer2.getRangeSlider();
                    rangeSlider2.setLowValueUsingDate(videoDataViewer2.getAbsoluteRecordingStartTime(),rangeSlider2.getAbsoluteStartDate());

                }
            }
        });

        customRangeSlider.getRangeSlider().highValueProperty().addListener((ov, old_val, new_val) -> {
            if (synchronization) {
                for (PSMDataViewer psmDataViewer2 : listOfPSMDataViewers) {

                    CustomRangeSlider rangeSlider2 = psmDataViewer2.getCustomRangeSlider();
                    rangeSlider2.setHighValueUsingDate(psmDataViewer2.getAbsolutePSMStartDate(),rangeSlider2.getAbsoluteEndDate());

                }
                for (VideoDataViewer videoDataViewer2 : listOfVideoDataViewers) {

                    CustomRangeSlider rangeSlider2 = videoDataViewer2.getRangeSlider();
                    rangeSlider2.setHighValueUsingDate(videoDataViewer2.getAbsoluteRecordingStartTime(),rangeSlider2.getAbsoluteEndDate());

                }
            }
        });


    }

    private RangeSlider getSelectedRangeSlider() {

        Scene selectedScene = null;

        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {

            if (psmDataViewer.getScene().getWindow().isFocused()) {

                selectedScene = psmDataViewer.getScene();

            }
        }
        for (VideoDataViewer videoDataViewer : listOfVideoDataViewers) {

            if (videoDataViewer.getScene().getWindow().isFocused()) {

                selectedScene = videoDataViewer.getScene();

            }
        }
        RangeSlider rangeSlider = new RangeSlider();
        if (selectedScene != null) {
            rangeSlider = (RangeSlider) selectedScene.lookup("#rangeSlider");
        }
        return rangeSlider;
    }


    private String getSelectedInstanceType() {

        Scene selectedScene = null;

        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {

            if (psmDataViewer.getScene().getWindow().isFocused()) {

                selectedScene = psmDataViewer.getScene();

            }
        }
        for (VideoDataViewer videoDataViewer : listOfVideoDataViewers) {

            if (videoDataViewer.getScene().getWindow().isFocused()) {

                selectedScene = videoDataViewer.getScene();

            }
        }
        String type = "";
        if (selectedScene != null) {
            if ((VBox) selectedScene.lookup("#video") != null) {
                type = "Video";
            } else {
                type = "PSM";
            }
        }
        return type;
    }

}
