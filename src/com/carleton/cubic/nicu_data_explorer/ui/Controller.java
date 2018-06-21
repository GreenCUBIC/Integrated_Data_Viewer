package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import org.controlsfx.control.RangeSlider;

import java.io.File;

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
                videoDataViewer.openWithControls(playButton, timeSlider, mediaView, playTime,rangeSlider,loopButton,lowValText,highValText);
            }
            else if (dataSelectionValue.equalsIgnoreCase(PSM_SELECTOR_LABEL))
            {
                psmDataViewer = new PSMDataViewer(file);
                psmDataViewer.openWithControls(psmCanvas, timeSlider, playButton, playTime);
            }
        });

    }

}
