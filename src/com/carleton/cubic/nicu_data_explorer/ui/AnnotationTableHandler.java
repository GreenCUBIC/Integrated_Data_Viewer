package com.carleton.cubic.nicu_data_explorer.ui;


import com.carleton.cubic.nicu_data_explorer.util.Annotation;
import com.carleton.cubic.nicu_data_explorer.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AnnotationTableHandler {
    private TableView<Annotation> annotationTable;
    private Button saveSessionButton;
    private Button saveUpdatesButton;
    private Button addAnnotationButton;
    private Button deleteAnnotationButton;
    private Button playAllButton;
    private static final String SIMPLE_DATE_FORMAT = "HH:mm:ss.SSS";
    private static final String HIGHLIGHTED_CELL_FORMAT = "-fx-background-color: yellow";
    private JsonDataViewer jsonDataViewer;
    private Date absoluteVideoStartDate;
    private Session session;
    private Date absolutePSMStartDate;

    private void calculateAbsoluteVideoStartDate(VideoDataViewer videoDataViewer) {

        this.absoluteVideoStartDate = videoDataViewer.getAbsoluteRecordingStartTime();
    }

    private void calculateAbsolutePSMStartDate(PSMDataViewer psmDataViewer) {

        this.absolutePSMStartDate = psmDataViewer.getAbsolutePSMStartDate();
    }

    public TableView<Annotation> getAnnotationTable() {
        return annotationTable;
    }

    public AnnotationTableHandler(Scene scene) {
        annotationTable = (TableView) scene.lookup("#annotationTable");
        saveSessionButton = (Button) scene.lookup("#saveSessionButton");
        saveUpdatesButton = (Button) scene.lookup("#saveUpdatesButton");
        addAnnotationButton = (Button) scene.lookup("#addAnnotationButton");
        playAllButton = (Button) scene.lookup("#playAllButton");
        deleteAnnotationButton = (Button) scene.lookup("#deleteAnnotationButton");
    }

    public void AnnotationLogScrollHandler(){

     annotationTable.setOnScroll(event -> {

         highlightColumnChanges();
         updateTable();
     });
    }

    public void setAnnotationData(File jsonFile) throws FileNotFoundException {
        JsonDataViewer jsonDataViewer = new JsonDataViewer(jsonFile);
        Session session = jsonDataViewer.loadSession(jsonFile);
        this.session = session;
        ObservableList<Annotation> tableData = FXCollections.observableList(session.getAnnotations());
        annotationTable.setItems(tableData);
        SimpleDateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        this.jsonDataViewer = jsonDataViewer;
        for (int i = 0; i < annotationTable.getItems().size(); i++) {
            Annotation annotation = annotationTable.getItems().get(i);


            String formattedStartDate = formatStartDate(annotation, format);
            annotation.setDisplayStartTime(formattedStartDate);

            String formattedEndDate = formatEndDate(annotation, format);
            annotation.setDisplayEndTime(formattedEndDate);
        }

        for (Object object : annotationTable.getColumns()) {
            TableColumn column = (TableColumn) object;
            column.setCellValueFactory(new PropertyValueFactory<Annotation, String>(column.getId()));
        }
        addAnnotationButton.setOnAction(event -> {

            openNewAnnotationDialogue();
        });
        deleteAnnotationButton.setOnAction(event -> {

            deleteAnnotationDialogue();

        });
        AnnotationLogScrollHandler();
    }

    private void deleteAnnotationDialogue() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you would like to delete the selected annotation?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            deleteSelectedAnnotation();
        } else {
            alert.close();
        }
    }

    private void deleteSelectedAnnotation() {

        Annotation selectedAnnotation = getSelectedAnnotation();

        for (int i = 0;i<annotationTable.getItems().size();i++){

            Annotation IteratingAnnotation = annotationTable.getItems().get(i);

            if(selectedAnnotation == IteratingAnnotation){

                annotationTable.getItems().remove(i);
            }

        }
    }

    public void setPlayAllButtonAction(List<VideoDataViewer> listOfVideoDataViewers,List<PSMDataViewer> listOFPSMViewers){

        playAllButton.setOnAction(event -> {

            for (VideoDataViewer videoDataViewer : listOfVideoDataViewers) {

                videoDataViewer.getPlayButton().fire();

            }
            for (PSMDataViewer psmDataViewer : listOFPSMViewers) {

                psmDataViewer.getPlayButton().fire();

            }

        });

    }

    private void updateTable() {
        for (int i = 0; i < annotationTable.getColumns().size(); i++) {
            annotationTable.getColumns().get(i).setVisible(false);
            annotationTable.getColumns().get(i).setVisible(true);
        }
    }

    private void setAnnotationOnVideoSlider(VideoDataViewer videoDataViewer, CustomRangeSlider customRangeSlider, SlideScaler slideScaler) {
        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        Annotation annotation = getSelectedAnnotation();
        Date sessionStartDate = new Date(Long.parseLong(session.getStart_time()));
        Date videoStartDate = videoDataViewer.getAbsoluteRecordingStartTime();
        Date relativeStartDate;

        if (sessionStartDate.getTime() < videoStartDate.getTime()) {
            return;
        } else {
            if (slideScaler.getScalingFactor()>0 && slideScaler.relativeStartDateWithinBounds(customRangeSlider)) {
                relativeStartDate = slideScaler.getRelativeStartDate();
            } else {
                relativeStartDate = videoStartDate;
                customRangeSlider.returnToDefault();
            }

            Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
            Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
            Long startInSliderUnits = (annotationStartDate.getTime() - relativeStartDate.getTime()) / 100;
            Long endInSliderUnits = (annotationEndDate.getTime() - relativeStartDate.getTime()) / 100;

            if (startInSliderUnits < 0 || endInSliderUnits < 0) {

                return;
            } else {
                setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);
                videoDataViewer.getMediaPlayer().seek(Duration.seconds((startInSliderUnits / (float) 10) + videoDataViewer.getMediaPlayer().getStartTime().toSeconds()));
            }
        }

    }


    private void setAnnotationOnPSMSlider(PSMDataViewer psmDataViewer, CustomRangeSlider customRangeSlider, SlideScaler slideScaler) {
        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        Annotation annotation = getSelectedAnnotation();
        Date sessionStartDate = new Date(Long.parseLong(session.getStart_time()));
        Date psmStartDate = psmDataViewer.getAbsolutePSMStartDate();
        Date relativeStartDate;

        if (sessionStartDate.getTime() < psmStartDate.getTime()) {
            return;
        } else {

            if (slideScaler.getScalingFactor()>0 && slideScaler.relativeStartDateWithinBounds(customRangeSlider)) {
                relativeStartDate = slideScaler.getRelativeStartDate();
            } else {
                relativeStartDate = psmStartDate;
                customRangeSlider.returnToDefault();
            }
            Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
            Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
            Long startInSliderUnits = (annotationStartDate.getTime() - relativeStartDate.getTime()) / 100;
            Long endInSliderUnits = (annotationEndDate.getTime() - relativeStartDate.getTime()) / 100;

            if (startInSliderUnits < 0 || endInSliderUnits < 0) {

                return;
            } else {
                setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);
                double seekDurationValueSeconds = rangeSlider.getLowValue() / 10;
                psmDataViewer.seek(seekDurationValueSeconds);
            }

        }

    }


    public void SaveAndUpdateButtonHandler(CustomRangeSlider customRangeSlider) {
        saveUpdatesButton.setOnAction(actionEvent -> {

            RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
            Annotation annotation = getSelectedAnnotation();

            SimpleDateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

            Date startDate = customRangeSlider.getAbsoluteStartDate();

            long lowValue = (long) rangeSlider.getLowValue();
            long highValue = (long) rangeSlider.getHighValue();


            Date newStartDate = new Date(startDate.getTime() + lowValue * 100);
            formatAndSetNewDisplayStartTime(format, newStartDate, annotation);

            Date newEndDate = new Date(startDate.getTime() + highValue * 100);
            formatAndSetNewDisplayEndTime(format, newEndDate, annotation);

            Long newUnixStartTime = newStartDate.toInstant().toEpochMilli();
            Long newUnixEndTime = newEndDate.toInstant().toEpochMilli();
            annotation.setStart_time(Long.toString(newUnixStartTime));
            annotation.setEnd_time(newUnixEndTime.toString());
            annotation.setIsUpdated(true);
            highlightColumnChanges();
            updateTable();

        });
        saveSessionButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

            if (file != null) {
                jsonDataViewer.SaveFile(file);
                removeHighlightingOnSave();
            }
        });



    }

    private void openNewAnnotationDialogue() {

        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("newAnnotation.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        Button saveNewAnnotationButton = (Button) scene.lookup("#saveNewAnnotationButton");
        TextField nameTextField = (TextField) scene.lookup("#nameTextField");
        TextField categoryTextField = (TextField) scene.lookup("#categoryTextField");

        saveNewAnnotationButton.setOnAction(event -> {
            Annotation annotation = new Annotation();
            annotation.setName(nameTextField.getText());
            annotation.setCategory(categoryTextField.getText());
            annotation.setStart_time("0");
            annotation.setEnd_time("10000000000000");
            stage.close();
            annotationTable.getItems().add(annotation);
            updateTable();
        });


    }

    private void formatAndSetNewDisplayStartTime(SimpleDateFormat format, Date newStartDate, Annotation annotation) {
        String newDisplayStartTime = format.format(newStartDate);
        annotation.setDisplayStartTime(newDisplayStartTime);
    }

    private String formatStartDate(Annotation annotation, SimpleDateFormat format) {

        Date startDate = new Date(Long.parseLong(annotation.getStart_time()));
        return format.format(startDate);

    }

    private String formatEndDate(Annotation annotation, SimpleDateFormat format) {

        Date endDate = new Date(Long.parseLong(annotation.getEnd_time()));
        return format.format(endDate);

    }

    private void formatAndSetNewDisplayEndTime(SimpleDateFormat format, Date newEndDate, Annotation annotation) {
        String newDisplayEndTime = format.format(newEndDate);
        annotation.setDisplayEndTime(newDisplayEndTime);
    }

    private void setNewRangeSliderBounds(RangeSlider rangeSlider, Long startInSliderUnits, Long endInSliderUnits) {


        rangeSlider.setLowValue(rangeSlider.getMin());
        rangeSlider.setHighValue(rangeSlider.getMax());
        rangeSlider.setLowValue(rangeSlider.getMin() + startInSliderUnits);
        rangeSlider.setHighValue(rangeSlider.getMin() + endInSliderUnits);


    }

    private void highlightColumnChanges() {
        TableColumn categoryColumn = annotationTable.getColumns().get(0);
        TableColumn nameColumn = annotationTable.getColumns().get(1);
        TableColumn startColumn = annotationTable.getColumns().get(2);
        TableColumn endColumn = annotationTable.getColumns().get(3);

        highlightUpdatedCells(categoryColumn);
        highlightUpdatedCells(nameColumn);
        highlightUpdatedCells(startColumn);
        highlightUpdatedCells(endColumn);

    }

    private void highlightUpdatedCells(TableColumn tableColumn) {

        tableColumn.setCellFactory(column -> new TableCell<Annotation, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                Annotation annotation = (Annotation) getTableRow().getItem();
                if (annotation != null) {

                    super.updateItem(item, empty);
                    String style = getStyle();
                    setText(item);
                    setStyle(style);

                    if (annotation.isUpdated()) {

                        setTextFill(Color.CHOCOLATE);
                        setStyle(HIGHLIGHTED_CELL_FORMAT);

                    }
                }

            }
        });


    }

    public Annotation getSelectedAnnotation() {
        Annotation selectedAnnotation;
        selectedAnnotation = annotationTable.getSelectionModel().getSelectedItem();
        return selectedAnnotation;

    }

    private void removeHighlightingOnSave() {
        for (int i = 0; i < annotationTable.getColumns().size(); i++) {
            TableColumn tableColumn = annotationTable.getColumns().get(i);

            tableColumn.setCellFactory(column -> new TableCell<Annotation, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    Annotation annotation = (Annotation) getTableRow().getItem();
                    if (annotation != null) {

                        super.updateItem(item, empty);
                        String style = getStyle();


                        if (annotation.isUpdated()) {
                            setStyle("");
                            annotation.setIsUpdated(false);

                        }else{
                            setText(item);
                            setStyle(style);
                        }
                    }

                }
            });
        }


    }


    public void SetAnnotationsPerVideo(List<VideoDataViewer> list, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) {

        for (VideoDataViewer videoDataViewer : list) {

            if (checkIfAnnotationWithinBounds(annotationTableHandler.getSelectedAnnotation(), videoDataViewer.getCustomRangeSlider())) {
                slideScaler.scaleInstance(videoDataViewer.getCustomRangeSlider(), getSelectedAnnotation());
                annotationTableHandler.calculateAbsoluteVideoStartDate(videoDataViewer);
                annotationTableHandler.setAnnotationOnVideoSlider(videoDataViewer, videoDataViewer.getCustomRangeSlider(), slideScaler);
            }
        }
    }


    public void SetAnnotationsPerPSM(List<PSMDataViewer> listOfPSMDataViewers, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) {

        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {
            if (checkIfAnnotationWithinBounds(annotationTableHandler.getSelectedAnnotation(), psmDataViewer.getCustomRangeSlider())) {
                slideScaler.scaleInstance(psmDataViewer.getCustomRangeSlider(), getSelectedAnnotation());
                annotationTableHandler.calculateAbsolutePSMStartDate(psmDataViewer);
                annotationTableHandler.setAnnotationOnPSMSlider(psmDataViewer, psmDataViewer.getCustomRangeSlider(), slideScaler);
            }
        }
    }

    private boolean checkIfAnnotationWithinBounds(Annotation selectedAnnotation, CustomRangeSlider customRangeSlider) {

        Date annotationStartDate = new Date(Long.parseLong(selectedAnnotation.getStart_time()));
        Date annotationEndDate = new Date(Long.parseLong(selectedAnnotation.getEnd_time()));
        Long annotationStartTime = annotationStartDate.getTime();
        Long annotationEndTime = annotationEndDate.getTime();
        Long absoluteStartTime = customRangeSlider.getAbsoluteStartDate().getTime();
        Long absoluteEndTime = customRangeSlider.getAbsoluteEndDate().getTime();
        return annotationStartTime >= absoluteStartTime && annotationEndTime <= absoluteEndTime;
    }


}


