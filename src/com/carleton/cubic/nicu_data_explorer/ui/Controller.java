package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import java.util.ArrayList;
import java.util.List;

public class Controller {

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

    private VideoDataViewer videoDataViewerInstance;
    private List<VideoDataViewer> listOfVideoDataViewers = new ArrayList<>();
    private List<PSMDataViewer> listOfPSMDataViewers = new ArrayList<>();
    private String type;

    private PSMDataViewer psmDataViewerInstance;


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

            String dataSelectionValue = (String) dataChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase(VIDEO_SELECTOR_LABEL)) {

                loadVideoInstance(file);

            } else if (dataSelectionValue.equalsIgnoreCase(PSM_SELECTOR_LABEL)) {

                loadPSMInstance(file);

            } else if (dataSelectionValue.equalsIgnoreCase(ANNOTATION_SELECTOR_LABEL)) {

                createAnnotationInstance(file);


            }
        });

    }

    private void createAnnotationInstance(File file) {

        try {

            createAnnotationTable(file);
            scaleAndSetAnnotationsOnInstances();
            if(videoDataViewerInstance!=null) {
                for (int i = 0; i < listOfVideoDataViewers.size(); i++) {
                    annotationTableHandler.SaveAndUpdateButtonHandler("Video",videoDataViewerInstance.getRangeSlider());
                }
                slideScaler = new SlideScaler();
                slideScaler.calculateAbsoluteVideoStartEndTimes(videoDataViewerInstance);
            }
            if(psmDataViewerInstance !=null) {
                for (int i = 0; i < listOfPSMDataViewers.size(); i++) {
                    annotationTableHandler.SaveAndUpdateButtonHandler("PSM",psmDataViewerInstance.getRangeSlider());      //TODO GOTTA CHANGE THIS INTO PSM VERSION
                }
                slideScaler = new SlideScaler();
                slideScaler.calculateAbsolutePSMStartEndTimes(psmDataViewerInstance);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }


    private void setFileExtension(ChoiceBox dataChoiceBox, FileChooser fileChooser) {

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

    public void scaleAndSetAnnotationsOnInstances() {
        annotationTableHandler.getAnnotationTable().setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 1) {
                try {
                    if (videoDataViewerInstance != null) {
                        scaleAndSetAnnotationOnVideoSliders();
                    }
                    if (psmDataViewerInstance != null) {
                        scaleAndSetAnnotationOnPSMSliders();
                    }


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void scaleAndSetAnnotationOnVideoSliders() throws ParseException {

                    slideScaler.calculateRelativeScalingBoundaries(annotationTableHandler, scaleCheckBox, scaleTextField);
                    annotationTableHandler.scaleAndSetAnnotationsPerVideo(listOfVideoDataViewers, slideScaler, annotationTableHandler);

    }

    private void scaleAndSetAnnotationOnPSMSliders() throws ParseException {

                    slideScaler.calculateAbsolutePSMStartEndTimes(psmDataViewerInstance);
                    annotationTableHandler.scaleAndSetAnnotationsPerPSM(listOfPSMDataViewers, slideScaler, annotationTableHandler);

    }
    public void loadVideoInstance(File file) {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("VideoInstance.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);

        Label lowValText = (Label) scene.lookup("#lowValText");
        Label highValText = (Label) scene.lookup("#highValText");
        MediaView mediaViewInstance = (MediaView) scene.lookup("#mediaView");
        Slider sliderInstance = (Slider) scene.lookup("#mainSlider");
        RangeSlider rangeSliderInstance = (RangeSlider) scene.lookup("#rangeSlider");
        Button loopButtonInstance = (Button) scene.lookup("#loopButton");
        Button playButtonInstance = (Button) scene.lookup("#playButton");

        stage.show();

        videoDataViewerInstance = new VideoDataViewer(file, mediaViewInstance, sliderInstance, rangeSliderInstance, loopButtonInstance, playButtonInstance,scene,lowValText,highValText);
        videoDataViewerInstance.openWithControls(playButtonInstance, sliderInstance, mediaViewInstance, playTime, rangeSliderInstance, loopButtonInstance, lowValText, highValText);


        listOfVideoDataViewers.add(videoDataViewerInstance);

    }
    public void loadPSMInstance(File file) {


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
        Button loopButtonInstance = (Button) scene.lookup("#loopButton");
        Button playButtonInstance = (Button) scene.lookup("#playButton");

        stage.show();

         psmDataViewerInstance = new PSMDataViewer(file);
        psmDataViewerInstance.openWithControls(canvasInstance,sliderInstance,playButtonInstance,playTime,rangeSliderInstance,loopButtonInstance,scene);

        listOfPSMDataViewers.add(psmDataViewerInstance);

    }

    public void createAnnotationTable(File file) throws IOException, ParseException {
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

    public void loadFile() {


        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("loadFile.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);

        dataChoiceBox = (ChoiceBox) scene.lookup("#dataChoiceBox");
        fileLoadButton = (Button) scene.lookup("#fileLoadButton");
        stage.show();

    }

}
