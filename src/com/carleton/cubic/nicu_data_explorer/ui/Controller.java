package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Controller
{

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
    private Button loadJsonButton;
    @FXML
    private Button setAnnotation;
    @FXML
    private Button saveJsonButton;

    private AnnotationController annotationController;
    private Annotation selectedAnnotation;

    private VideoDataViewer videoDataViewer;
    private PSMDataViewer psmDataViewer;

    public Controller()
    {

    }

    private static final String VIDEO_SELECTOR_LABEL = "Video";
    private static final String PSM_SELECTOR_LABEL = "PSM";
    private static final String SYNCHRONIZED_SELECTOR_LABEL = "Synchronized";


    @FXML
    public void initialize()
    {
        dataChoiceBox.setItems(FXCollections.observableArrayList(
                VIDEO_SELECTOR_LABEL, PSM_SELECTOR_LABEL, SYNCHRONIZED_SELECTOR_LABEL)
        );

        dataChoiceBox.getSelectionModel().selectFirst();

        fileLoadButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
            if (file == null)
            {
                return;
            }

            String dataSelectionValue = (String) dataChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase(VIDEO_SELECTOR_LABEL))
            {
                videoDataViewer = new VideoDataViewer(file);
                videoDataViewer.openWithControls(playButton, timeSlider, mediaView, playTime, rangeSlider, loopButton, lowValText, highValText);
            }
            else if (dataSelectionValue.equalsIgnoreCase(PSM_SELECTOR_LABEL))
            {
                psmDataViewer = new PSMDataViewer(file);
                psmDataViewer.openWithControls(psmCanvas, timeSlider, playButton, playTime);
            }
        });

        loadJsonButton.setOnAction(actionEvent -> {
            FileChooser jsonFileChooser = new FileChooser();
            File jsonFile = jsonFileChooser.showOpenDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
            if (jsonFile == null)
            {
                return;
            }
            else
            {

                try
                {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("annotations.fxml"));
                    Scene scene = new Scene(fxmlLoader.load(), 600, 400);
                    Stage stage = new Stage();
                    stage.setTitle("Annotation Table");
                    stage.setScene(scene);
                    stage.show();
                    annotationController = new AnnotationController(scene);
                    annotationController.setJSON(jsonFile);
                    annotationHandler();
                }
                catch (IOException | ParseException e)
                {
                    e.printStackTrace();
                }
            }

        });
        setAnnotation.setOnAction(actionEvent -> {
            Session session = annotationController.jsonDataViewer.getSession();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
            Date SessionStartDate = new Date(Long.parseLong(session.getStart_time()));
            String formattedSessionStartDate = format.format(SessionStartDate);
            try
            {
                Date SessionStartTime = format.parse(formattedSessionStartDate);
                Long lowSliderInMillis = (long) rangeSlider.getLowValue() * 100;

                Long startTime = lowSliderInMillis + SessionStartTime.getTime();
                Date startTimeDate = new Date(Long.parseLong(startTime.toString()));
                String formattedStartDate = format.format(startTimeDate);
                annotationController.getSelectedAnnotation().setStart_time(formattedStartDate);

                Long highSliderInMillis = (long) rangeSlider.getHighValue() * 100;

                Long endTime = highSliderInMillis + SessionStartTime.getTime();
                Date endTimeDate = new Date(Long.parseLong(endTime.toString()));
                String formattedEndDate = format.format(endTimeDate);
                annotationController.getSelectedAnnotation().setEnd_time(formattedEndDate);

                annotationController.updateTable();


            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }


        });
        saveJsonButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();

                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
                fileChooser.getExtensionFilters().add(extFilter);

                File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

                if(file != null){
                }
            }
        });
    }

    public void annotationHandler()
    {
        annotationController.getAnnotationTable().setOnMouseClicked((MouseEvent event) -> {
            selectedAnnotation = annotationController.getSelectedAnnotation();
            try
            {
                annotationController.setUpAnnotation(selectedAnnotation, rangeSlider,
                        annotationController.jsonDataViewer.getSession());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        });
    }
}
