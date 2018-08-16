package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.SliderAndButtonPackage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
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
    private ChoiceBox<String> dataChoiceBox;
    @FXML
    private Button fileLoadButton;
    @FXML
    private TextField scaleTextField;
    @FXML
    private CheckBox scaleCheckBox;
    private AnnotationTableHandler annotationTableHandler;
    private VideoDataViewer videoDataViewerInstance;
    private List<VideoDataViewer> listOfVideoDataViewers = new ArrayList<>();
    private List<PSMDataViewer> listOfPSMDataViewers = new ArrayList<>();
    private PSMDataViewer psmDataViewerInstance;
    private SlideScaler slideScaler = new SlideScaler();


    public Controller() {

    }

    private static final String VIDEO_SELECTOR_LABEL = "Video";
    private static final String PSM_SELECTOR_LABEL = "PSM";
    private static final String ANNOTATION_SELECTOR_LABEL = "Annotation";


    @FXML
    public void initialize() {

        retrieveScalingFactorAndScalingActive();
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
        Label lowValText = (Label) scene.lookup("#lowValText");
        Label highValText = (Label) scene.lookup("#highValText");
        Label timeLineText = (Label) scene.lookup("#timeLineText");
        VBox vboxInstance = (VBox) scene.lookup("#vBox");
        SliderAndButtonPackage sliderAndButtonPackage = new SliderAndButtonPackage(playButtonInstance,loopButtonInstance,sliderInstance,customRangeSliderInstance);
        stage.show();

        videoDataViewerInstance = new VideoDataViewer(file, mediaViewInstance, sliderAndButtonPackage, scene);
        videoDataViewerInstance.openWithControls(mediaViewInstance, timeLineText, lowValText, highValText, listOfVideoDataViewers,listOfPSMDataViewers);
        listOfVideoDataViewers.add(videoDataViewerInstance);

        scene.getWindow().focusedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue){
                if(annotationTableHandler!=null) {
                    annotationTableHandler.SaveAndUpdateButtonHandler(customRangeSliderInstance);
                }


            }
        });

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
        Label lowValText = (Label) scene.lookup("#lowValText");
        Label highValText = (Label) scene.lookup("#highValText");
        Label timeLineText = (Label) scene.lookup("#timeLineText");
        VBox vboxInstance = (VBox) scene.lookup("#vBox");
        SliderAndButtonPackage sliderAndButtonPackage = new SliderAndButtonPackage(playButtonInstance,loopButtonInstance,sliderInstance,customRangeSliderInstance);
        stage.show();


        psmDataViewerInstance = new PSMDataViewer(file,sliderAndButtonPackage);
        psmDataViewerInstance.openWithControls(canvasInstance, timeLineText  , scene,lowValText,highValText,listOfVideoDataViewers,listOfPSMDataViewers);
        listOfPSMDataViewers.add(psmDataViewerInstance);

        scene.getWindow().focusedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue){
                if(annotationTableHandler!=null) {
                    annotationTableHandler.SaveAndUpdateButtonHandler(customRangeSliderInstance);
                }
            }
        });
    }

    private void createAnnotationInstance(File file) {

        try {
            createAnnotationTable(file);
            SetAnnotationsOnInstances();
            annotationTableHandler.setPlayAllButtonAction(listOfVideoDataViewers,listOfPSMDataViewers);
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

    private void SetAnnotationsOnInstances() {
        annotationTableHandler.getAnnotationTable().setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 1) {
                if (videoDataViewerInstance != null) {
                    annotationTableHandler.SetAnnotationsPerVideo(listOfVideoDataViewers, slideScaler, annotationTableHandler);
                }
                if (psmDataViewerInstance != null) {
                    annotationTableHandler.SetAnnotationsPerPSM(listOfPSMDataViewers, slideScaler, annotationTableHandler);
                }


            }
        });
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






    public void retrieveScalingFactorAndScalingActive(){

        scaleCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> slideScaler.setActive(newValue));

        scaleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals("")){

                slideScaler.setScalingFactor((long)0);
            }
            else {
                slideScaler.setScalingFactor(Long.parseLong(newValue));
            }
        });


    }
   

}
