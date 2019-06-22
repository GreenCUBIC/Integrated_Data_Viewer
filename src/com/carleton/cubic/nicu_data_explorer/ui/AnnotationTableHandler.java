package com.carleton.cubic.nicu_data_explorer.ui;


import com.carleton.cubic.nicu_data_explorer.util.Annotation;
import com.carleton.cubic.nicu_data_explorer.util.Session;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnnotationTableHandler {
    private Button editAnnotationButton;
    private TableView<Annotation> annotationTable;
    private Button saveSessionButton;
    private Button saveUpdatesButton;
    private Button addAnnotationButton;
    private Button deleteAnnotationButton;
    private List<String> categoryList;
    private List<String> eventNameList;
    private Button playAllButton;
    private static final String SIMPLE_DATE_FORMAT = "HH:mm:ss.SSS";
    private static final String HIGHLIGHTED_CELL_FORMAT = "-fx-background-color: yellow";
    private JsonDataViewer jsonDataViewer;


    public TableView<Annotation> getAnnotationTable() {
        return annotationTable;
    }

    public AnnotationTableHandler(Scene scene) {
        annotationTable = (TableView<Annotation>) scene.lookup("#annotationTable");
        saveSessionButton = (Button) scene.lookup("#saveSessionButton");
        saveUpdatesButton = (Button) scene.lookup("#saveUpdatesButton");
        addAnnotationButton = (Button) scene.lookup("#addAnnotationButton");
        playAllButton = (Button) scene.lookup("#playAllButton");
        deleteAnnotationButton = (Button) scene.lookup("#deleteAnnotationButton");
        editAnnotationButton = (Button) scene.lookup("#editAnnotationButton");
    }

    public void AnnotationLogScrollHandler() {

        annotationTable.addEventFilter(ScrollEvent.ANY, event -> {
                    highlightColumnChanges();
                    annotationTable.refresh();
                }
        );
        annotationTable.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if ((event.getTarget() instanceof TableColumnHeader) | event.isDragDetect()) {
                highlightColumnChanges();
                annotationTable.refresh();
            }
        });

    }

    public void setAnnotationData(File jsonFile) throws FileNotFoundException {
        JsonDataViewer jsonDataViewer = new JsonDataViewer(jsonFile);
        Session session = jsonDataViewer.loadSession(jsonFile);
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
        addAnnotationButton.setOnAction(event -> openNewAnnotationDialogue());
        editAnnotationButton.setOnAction(event -> editAnnotationDialogue());
        deleteAnnotationButton.setOnAction(event -> deleteAnnotationDialogue());
        AnnotationLogScrollHandler();
        categoryList = new ArrayList<>();
        eventNameList = new ArrayList<>();
        for (Annotation item : annotationTable.getItems()) {

            categoryList.add(item.getCategory());
            eventNameList.add(item.getName());
        }
        categoryList = removeDuplicates(categoryList);
        eventNameList = removeDuplicates(eventNameList);

    }


    private void deleteAnnotationDialogue() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you would like to delete the selected annotation?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            deleteSelectedAnnotation();
        } else {
            alert.close();
        }
    }

    private void deleteSelectedAnnotation() {

        Annotation selectedAnnotation = getSelectedAnnotation();

        for (int i = 0; i < annotationTable.getItems().size(); i++) {

            Annotation IteratingAnnotation = annotationTable.getItems().get(i);

            if (selectedAnnotation == IteratingAnnotation) {

                annotationTable.getItems().remove(i);
                annotationTable.refresh();
            }

        }
    }

    private void editAnnotationDialogue() {

        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("editAnnotation.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");

        stage.setScene(scene);
        stage.show();
        Button saveNewAnnotationButton = (Button) scene.lookup("#saveNewAnnotationButton");
        TextField nameTextField = (TextField) scene.lookup("#nameTextField");
        TextField categoryTextField = (TextField) scene.lookup("#categoryTextField");

        saveNewAnnotationButton.setOnAction(event -> {
            Annotation annotation = getSelectedAnnotation();
            annotation.setName(nameTextField.getText());
            annotation.setCategory(categoryTextField.getText());
            stage.close();
            annotationTable.refresh();
        });


    }

    public void setPlayAllButtonAction(List<VideoDataViewer> listOfVideoDataViewers, List<PSMDataViewer> listOFPSMViewers, List<PmdiDataViewer> listOfPmdiViewers) {

        playAllButton.setOnAction(event -> {

            for (VideoDataViewer videoDataViewer : listOfVideoDataViewers) {

                videoDataViewer.getPlayButton().fire();

            }
            for (PSMDataViewer psmDataViewer : listOFPSMViewers) {

                psmDataViewer.getPlayButton().fire();

            }
            for (PmdiDataViewer pmdiDataViewer : listOfPmdiViewers) {

                pmdiDataViewer.getPlayButton().fire();

            }

        });

    }


    private void setAnnotationOnVideoSlider(VideoDataViewer videoDataViewer, CustomRangeSlider customRangeSlider, SlideScaler slideScaler) {
        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        Annotation annotation = getSelectedAnnotation();
        Date videoStartDate = videoDataViewer.getAbsoluteRecordingStartTime();
        Date relativeStartDate;


        if (slideScaler.getScalingFactor() > 0 && slideScaler.relativeStartDateWithinBounds(customRangeSlider)) {
            relativeStartDate = slideScaler.getRelativeStartDate();
        } else {
            relativeStartDate = videoStartDate;
            customRangeSlider.returnToDefault();
        }

        Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
        Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
        Long startInSliderUnits = (annotationStartDate.getTime() - relativeStartDate.getTime()) / 100;
        Long endInSliderUnits = (annotationEndDate.getTime() - relativeStartDate.getTime()) / 100;

        if (startInSliderUnits >= 0 && endInSliderUnits >= 0) {
            setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);
            videoDataViewer.getMediaPlayer().pause();
            videoDataViewer.getMediaPlayer().seek(Duration.seconds(rangeSlider.getLowValue() / 10.0));
        }
    }

    private void setAnnotationOnPMDISlider(PmdiDataViewer pmdiDataViewer, CustomRangeSlider customRangeSlider, SlideScaler slideScaler) {
        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        Annotation annotation = getSelectedAnnotation();
        Date pmdiStartDate = pmdiDataViewer.getAbsoluteStartDate();
        Date relativeStartDate;


        if (slideScaler.getScalingFactor() > 0 && slideScaler.relativeStartDateWithinBounds(customRangeSlider)) {
            relativeStartDate = slideScaler.getRelativeStartDate();
        } else {
            relativeStartDate = pmdiStartDate;
            customRangeSlider.returnToDefault();
        }

        Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
        Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
        Long startInSliderUnits = (annotationStartDate.getTime() - relativeStartDate.getTime()) / 100;
        Long endInSliderUnits = (annotationEndDate.getTime() - relativeStartDate.getTime()) / 100;

        if (startInSliderUnits >= 0 && endInSliderUnits >= 0) {
            setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);
            pmdiDataViewer.stop();
            long seekValueSeconds = Math.round(rangeSlider.getLowValue() / 10);

            pmdiDataViewer.clearGraphAndSeekSeconds(seekValueSeconds);
            pmdiDataViewer.getTimeSlider().setValue(rangeSlider.getLowValue());
        }
    }

    private void setAnnotationOnPSMSlider(PSMDataViewer psmDataViewer, CustomRangeSlider customRangeSlider, SlideScaler slideScaler) {
        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        Annotation annotation = getSelectedAnnotation();
        Date psmStartDate = psmDataViewer.getAbsolutePSMStartDate();
        Date relativeStartDate;


        if (slideScaler.getScalingFactor() > 0 && slideScaler.relativeStartDateWithinBounds(customRangeSlider)) {
            relativeStartDate = slideScaler.getRelativeStartDate();
        } else {
            relativeStartDate = psmStartDate;
            customRangeSlider.returnToDefault();
        }
        Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
        Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
        Long startInSliderUnits = (annotationStartDate.getTime() - relativeStartDate.getTime()) / 100;
        Long endInSliderUnits = (annotationEndDate.getTime() - relativeStartDate.getTime()) / 100;

        if (startInSliderUnits >= 0 && endInSliderUnits >= 0) {
            setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);
            psmDataViewer.seek(rangeSlider.getLowValue() / 10.0);
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
            annotationTable.refresh();
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
    public static <T> ArrayList<String> removeDuplicates(List<String> list)
    {

        // Create a new LinkedHashSet

        // Add the elements to set
        Set<String> set = new LinkedHashSet<>(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return new ArrayList<>(list);
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
        scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");
        stage.setScene(scene);
        stage.show();
        Button saveNewAnnotationButton = (Button) scene.lookup("#saveNewAnnotationButton");
        ChoiceBox nameChoiceField = (ChoiceBox) scene.lookup("#nameChoiceField");
        ChoiceBox categoryChoiceField = (ChoiceBox) scene.lookup("#categoryChoiceField");
        nameChoiceField.getItems().addAll(eventNameList);
        categoryChoiceField.getItems().addAll(categoryList);
        Button customAnnotation = (Button) scene.lookup("#customAnnotationButton");

        customAnnotation.setOnAction(event -> {


            openCustomAnnotationDialogue();
            stage.close();
        });

        saveNewAnnotationButton.setOnAction(event -> {
            Annotation annotation = new Annotation();
            annotation.setName((String)nameChoiceField.getValue());
            annotation.setCategory((String)categoryChoiceField.getValue());
            annotation.setStart_time("0");
            annotation.setEnd_time("10000000000000");
            stage.close();
            annotationTable.getItems().add(annotation);
            annotationTable.refresh();
        });


    }

    private void openCustomAnnotationDialogue() {

        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("customAnnotation.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add("com/carleton/cubic/nicu_data_explorer/stylesheets/stylesheet.css");
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
            annotationTable.refresh();
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

                        } else {
                            setText(item);
                            setStyle(style);
                        }
                    }

                }
            });
        }


    }

    public List<String> getCategoryList() {
        return categoryList;
    }

    public List<String> getEventNameList() {
        return eventNameList;
    }

    public void setAnnotationsPerVideo(List<VideoDataViewer> list, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) {

        for (VideoDataViewer videoDataViewer : list) {

            if (checkIfAnnotationWithinBounds(annotationTableHandler.getSelectedAnnotation(), videoDataViewer.getCustomRangeSlider())) {
                slideScaler.scaleInstance(videoDataViewer.getCustomRangeSlider(), getSelectedAnnotation());
                annotationTableHandler.setAnnotationOnVideoSlider(videoDataViewer, videoDataViewer.getCustomRangeSlider(), slideScaler);
            }
        }
    }


    public void setAnnotationsPerPSM(List<PSMDataViewer> listOfPSMDataViewers, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) {

        for (PSMDataViewer psmDataViewer : listOfPSMDataViewers) {
            if (checkIfAnnotationWithinBounds(annotationTableHandler.getSelectedAnnotation(), psmDataViewer.getCustomRangeSlider())) {
                slideScaler.scaleInstance(psmDataViewer.getCustomRangeSlider(), getSelectedAnnotation());
                annotationTableHandler.setAnnotationOnPSMSlider(psmDataViewer, psmDataViewer.getCustomRangeSlider(), slideScaler);
            }
        }
    }


    public void setAnnotationsPerPmdi(List<PmdiDataViewer> listOfPmdiDataViewers, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) {

        for (PmdiDataViewer pmdiDataViewer : listOfPmdiDataViewers) {
            if (checkIfAnnotationWithinBounds(annotationTableHandler.getSelectedAnnotation(), pmdiDataViewer.getCustomRangeSlider())) {
                slideScaler.scaleInstance(pmdiDataViewer.getCustomRangeSlider(), getSelectedAnnotation());
                annotationTableHandler.setAnnotationOnPMDISlider(pmdiDataViewer, pmdiDataViewer.getCustomRangeSlider(), slideScaler);
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

        if ((annotationStartTime >= absoluteStartTime) && (annotationEndTime <= absoluteEndTime)) {

            return true;
        } else {

            return false;
        }
    }


}


