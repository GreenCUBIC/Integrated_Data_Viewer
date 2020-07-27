package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.DefaultInstancePackage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class Controller {


    @FXML
    private ChoiceBox<String> dataChoiceBox;
    @FXML
    private Button fileLoadButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Button zoomInButton;
    @FXML
    private TextField scaleTextField;

    private AnnotationTableHandler annotationTableHandler;
    private VideoDataViewer videoDataViewerInstance;
    private final List<VideoDataViewer> listOfVideoDataViewers = new ArrayList<>();
    private final List<PSMDataViewer> listOfPSMDataViewers = new ArrayList<>();
    private PSMDataViewer psmDataViewerInstance;
    private PmdiDataViewer pmdiDataViewerInstance;
    private final SlideScaler slideScaler = new SlideScaler();
    private double currentScalingFactor = 1;
    private final List<PmdiDataViewer> listOfPmdiDataViewers = new ArrayList<>();
    private static final String VIDEO_SELECTOR_LABEL = "Video";
    private static final String PSM_SELECTOR_LABEL = "PSM";
    private static final String ANNOTATION_SELECTOR_LABEL = "Annotation";
    private static final String PMDI_SELECTOR_LABEL = "PMDI";
    private static final String TIME_ZONE = "PMDI";

    @FXML
    public void initialize() {
        TimeZone zone = TimeZone.getDefault();
        Date date = new Date();
        TimeZone tz = TimeZone.getDefault();
        String name = tz.getDisplayName(tz.inDaylightTime(date), TimeZone.LONG);

        setupZoomActions();
        setupInstanceSelection();
    }

    private void setupInstanceSelection() {

        dataChoiceBox.setItems(FXCollections.observableArrayList(
                VIDEO_SELECTOR_LABEL, PSM_SELECTOR_LABEL, ANNOTATION_SELECTOR_LABEL, PMDI_SELECTOR_LABEL)
        );
        dataChoiceBox.getSelectionModel().selectFirst();

        fileLoadButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            setFileExtension(dataChoiceBox, fileChooser);
            File file = fileChooser.showOpenDialog(((Node) actionEvent.getTarget()).getScene().getWindow());

            if (file != null) {
                String dataSelectionValue = dataChoiceBox.getValue();
                if (dataSelectionValue.equalsIgnoreCase(VIDEO_SELECTOR_LABEL)) {

                    loadVideoInstance(file);

                } else if (dataSelectionValue.equalsIgnoreCase(PSM_SELECTOR_LABEL)) {

                    loadPSMInstance(file);

                } else if (dataSelectionValue.equalsIgnoreCase(ANNOTATION_SELECTOR_LABEL)) {

                    createAnnotationInstance(file);


                } else if (dataSelectionValue.equalsIgnoreCase(PMDI_SELECTOR_LABEL)) {
                    loadPMDIInstance(file);
                }
            }
        });
    }


    private void loadPMDIInstance(File file) {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("PmdiInstance.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (root !=null){
            Scene scene = new Scene(root);
            scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");

            stage.setScene(scene);
            DefaultInstancePackage defaultInstancePackage = assignInstanceControls(scene);
            SubScene subScene = (SubScene) scene.lookup("#subScene");
            Button increaseSampleSizeButton = (Button) scene.lookup("#increaseSampleSizeButton");
            Button decreaseSampleSizeButton = (Button) scene.lookup("#decreaseSampleSizeButton");
            Button autoScaleYAxisButton = (Button) scene.lookup("#autoScaleYAxisButton");
            Button removeBubblesButton = (Button) scene.lookup("#removeBubblesButton");


            ButtonPackage pmdiButtonPackage = new ButtonPackage(increaseSampleSizeButton, decreaseSampleSizeButton, autoScaleYAxisButton, removeBubblesButton);


            pmdiDataViewerInstance = new PmdiDataViewer(subScene, file, stage, defaultInstancePackage, pmdiButtonPackage);
            listOfPmdiDataViewers.add(pmdiDataViewerInstance);
            adjustOtherInstanceRangeSliders(listOfVideoDataViewers, listOfPSMDataViewers, listOfPmdiDataViewers);
            AnnotationUpdateFocusListener(scene, defaultInstancePackage);
        }



    }



    private void loadVideoInstance(File file) {

        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("VideoInstance.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (root !=null) {


            Scene scene = new Scene(root);
            scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");

            stage.setScene(scene);

            MediaView mediaViewInstance = (MediaView) scene.lookup("#mediaView");
            DefaultInstancePackage defaultInstancePackage = assignInstanceControls(scene);
            stage.show();

            videoDataViewerInstance = new VideoDataViewer(file, defaultInstancePackage, scene);
            videoDataViewerInstance.openWithControls(mediaViewInstance);
            setTextLabelsToThumbs(defaultInstancePackage,scene);
            listOfVideoDataViewers.add(videoDataViewerInstance);
            adjustOtherInstanceRangeSliders(listOfVideoDataViewers, listOfPSMDataViewers, listOfPmdiDataViewers);

            AnnotationUpdateFocusListener(scene, defaultInstancePackage);
        }
    }

    private void setTextLabelsToThumbs(DefaultInstancePackage defaultInstancePackage, Scene scene) {

        Slider mainSlider = defaultInstancePackage.getTimeSlider();
        Text text = defaultInstancePackage.getText();
        double initialMin = mainSlider.getMin();

        text.setLayoutX(mainSlider.getLayoutX() + (mainSlider.getValue() - initialMin)
                / (mainSlider.getMax() - initialMin) * mainSlider.getWidth());

        mainSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            final double timeSliderMin = mainSlider.getMin();
            text.textProperty().bind(defaultInstancePackage.getPlayTime().textProperty());
            text.setLayoutX(mainSlider.getLayoutX() + (mainSlider.getValue() - timeSliderMin)
                    / (mainSlider.getMax() - timeSliderMin) * mainSlider.getWidth());
        });

            AnnotationUpdateFocusListener(scene, defaultInstancePackage);
        }



    private void loadPSMInstance(File file) {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("PSMInstance.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (root !=null) {


            Scene scene = new Scene(root);
            scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");

            stage.setScene(scene);

            Canvas canvasInstance = (Canvas) scene.lookup("#canvas");
            DefaultInstancePackage defaultInstancePackage = assignInstanceControls(scene);
            stage.show();


            stage.setScene(scene);

            psmDataViewerInstance = new PSMDataViewer(file, defaultInstancePackage);
            psmDataViewerInstance.openWithControls(canvasInstance);
            listOfPSMDataViewers.add(psmDataViewerInstance);
            adjustOtherInstanceRangeSliders(listOfVideoDataViewers, listOfPSMDataViewers, listOfPmdiDataViewers);


            AnnotationUpdateFocusListener(scene, defaultInstancePackage);
        }
    }

    private void createAnnotationInstance(File file) {

        try {
            createAnnotationTable(file);
            SetAnnotationsOnInstances();
            annotationTableHandler.setPlayAllButtonAction(listOfVideoDataViewers, listOfPSMDataViewers, listOfPmdiDataViewers);
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
            case 4:
                extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                break;
            case 3:
                extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
                fileChooser.getExtensionFilters().add(extFilter);
                break;


        }


    }

    private void SetAnnotationsOnInstances() {
        annotationTableHandler.getAnnotationTable().setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 1) {
                if (videoDataViewerInstance != null) {
                    annotationTableHandler.setAnnotationsPerVideo(listOfVideoDataViewers, slideScaler, annotationTableHandler);
                }
                if (psmDataViewerInstance != null) {
                    annotationTableHandler.setAnnotationsPerPSM(listOfPSMDataViewers, slideScaler, annotationTableHandler);
                }
                if (pmdiDataViewerInstance != null) {
                    annotationTableHandler.setAnnotationsPerPmdi(listOfPmdiDataViewers, slideScaler, annotationTableHandler);
                }


            }
        });
    }


    private void createAnnotationTable(File file) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("annotations.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 650, 650);
        scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");

        Stage stage = new Stage();
        stage.setTitle("Annotation Table");
        stage.setScene(scene);
        stage.show();

        annotationTableHandler = new AnnotationTableHandler(scene);
        annotationTableHandler.setAnnotationData(file);

    }

    private void adjustOtherInstanceRangeSliders(List<VideoDataViewer> listOfVideoDataViewers, List<PSMDataViewer> listOfPSMDataViewers, List<PmdiDataViewer> listOfPmdiDataViewers) {

        List<IntegratedDataViewerInstance> compiledList = new ArrayList<>();
        compiledList.addAll(listOfVideoDataViewers);
        compiledList.addAll(listOfPSMDataViewers);
        compiledList.addAll(listOfPmdiDataViewers);

        for (IntegratedDataViewerInstance individualInstance : compiledList) {
            RangeSlider currentRangeSlider = individualInstance.getCustomRangeSlider().getRangeSlider();
            currentRangeSlider.lowValueProperty().addListener((ov, old_val, new_val) -> {
                if (currentRangeSlider.isFocused()) {
                    for (IntegratedDataViewerInstance otherInstance : compiledList) {
                        CustomRangeSlider otherCustomRangeSlider = otherInstance.getCustomRangeSlider();
                        RangeSlider otherRangeSlider = otherCustomRangeSlider.getRangeSlider();
                        otherRangeSlider.setLowValue((Double) new_val);
                    }
                }

            });
            currentRangeSlider.highValueProperty().addListener((ov, old_val, new_val) -> {
                if (currentRangeSlider.isFocused()) {

                    for (IntegratedDataViewerInstance otherInstance : compiledList) {
                        CustomRangeSlider otherCustomRangeSlider = otherInstance.getCustomRangeSlider();
                        RangeSlider otherRangeSlider = otherCustomRangeSlider.getRangeSlider();
                        otherRangeSlider.setHighValue((Double) new_val);
                    }
                }

            });


        }
    }
    private void setupZoomActions() {

        zoomInButton.setOnAction(event -> {

            if (currentScalingFactor > 0) {

                currentScalingFactor = currentScalingFactor - 0.1;
                currentScalingFactor = (double) Math.round(currentScalingFactor * 10d) / 10d;
            }
            scaleTextField.setText(String.valueOf(currentScalingFactor));
            slideScaler.setScalingFactor(currentScalingFactor);

            adjustToNewBounds();
        });
        zoomOutButton.setOnAction(event -> {

            if (currentScalingFactor >= 0) {
                currentScalingFactor = currentScalingFactor + 0.1;
                currentScalingFactor = (double) Math.round(currentScalingFactor * 10d) / 10d;
            }
            scaleTextField.setText(String.valueOf(currentScalingFactor));
            slideScaler.setScalingFactor(currentScalingFactor);

            adjustToNewBounds();
        });


    }

    private void adjustToNewBounds() {

        if (annotationTableHandler != null && annotationTableHandler.getSelectedAnnotation() != null) {

            annotationTableHandler.setAnnotationsPerVideo(listOfVideoDataViewers, slideScaler, annotationTableHandler);
            annotationTableHandler.setAnnotationsPerPSM(listOfPSMDataViewers, slideScaler, annotationTableHandler);
            annotationTableHandler.setAnnotationsPerPmdi(listOfPmdiDataViewers, slideScaler, annotationTableHandler);

        }

    }

    private void AnnotationUpdateFocusListener(Scene scene, DefaultInstancePackage defaultInstancePackage) {

        scene.getWindow().focusedProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue) {
                if (annotationTableHandler != null) {
                    annotationTableHandler.SaveAndUpdateButtonHandler(defaultInstancePackage.getCustomRangeSlider());
                }
            }
        });
    }

    private DefaultInstancePackage assignInstanceControls(Scene scene) {
        Slider sliderInstance = (Slider) scene.lookup("#mainSlider");
        RangeSlider rangeSliderInstance = (RangeSlider) scene.lookup("#rangeSlider");
        CustomRangeSlider customRangeSliderInstance = new CustomRangeSlider(rangeSliderInstance);
        Button loopButtonInstance = (Button) scene.lookup("#loopButton");
        Button playButtonInstance = (Button) scene.lookup("#playButton");
        Label lowValText = (Label) scene.lookup("#lowValText");
        Label highValText = (Label) scene.lookup("#highValText");
        Label timeLineText = (Label) scene.lookup("#timeLineText");
        ChoiceBox playbackChoiceBoxInstance = (ChoiceBox) scene.lookup("#playbackChoiceBox");
        Text text = (Text) scene.lookup("#mainSliderText");
        return new DefaultInstancePackage(playButtonInstance, loopButtonInstance, sliderInstance, customRangeSliderInstance, playbackChoiceBoxInstance, lowValText, highValText, timeLineText, text);
    }


}
