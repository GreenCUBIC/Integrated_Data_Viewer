package com.carleton.cubic.nicu_data_explorer.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AnnotationTableHandler {
    private TableView<Annotation> annotationTable;
    private Button saveSessionButton;
    private Button saveUpdatesButton;
    private static final String SIMPLE_DATE_FORMAT = "HH:mm:ss.SSS";
    private static final String HIGHLIGHTED_CELL_FORMAT = "-fx-background-color: yellow";
    JsonDataViewer jsonDataViewer;
    private Date absoluteVideoStartDate;
    private Session session;
    private Date absolutePSMStartDate;

    public void calculateAbsoluteVideoStartDate(VideoDataViewer videoDataViewer) {

        this.absoluteVideoStartDate = videoDataViewer.getAbsoluteRecordingStartTime();
    }
    public void calculateAbsolutePSMStartDate(PSMDataViewer psmDataViewer) {

        this.absolutePSMStartDate = psmDataViewer.getAbsolutePSMStartDate();
    }

    public TableView<Annotation> getAnnotationTable() {
        return annotationTable;
    }

    public AnnotationTableHandler(Scene scene) {
        annotationTable = (TableView) scene.lookup("#annotationTable");
        saveSessionButton = (Button) scene.lookup("#saveSessionButton");
        saveUpdatesButton = (Button) scene.lookup("#saveUpdatesButton");
    }

    public void setAnnotationData(File jsonFile) throws FileNotFoundException, ParseException {
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
            //column.setCellFactory(TextFieldTableCell.forTableColumn());  //working on editing cells
            column.setCellValueFactory(new PropertyValueFactory<Annotation, String>(column.getId()));
        }
    }


    public void updateTable() {
        for (int i = 0; i < annotationTable.getColumns().size(); i++) {
            ;
            annotationTable.getColumns().get(i).setVisible(false);
            annotationTable.getColumns().get(i).setVisible(true);
        }
    }

    public void setAnnotationOnVideoSlider(VideoDataViewer videoDataViewer, RangeSlider rangeSlider, SlideScaler slideScaler) throws ParseException {

        Annotation annotation = getSelectedAnnotation();
        Date sessionStartDate = new Date(Long.parseLong(session.getStart_time()));
        Date videoStartDate = videoDataViewer.getAbsoluteRecordingStartTime();

        if (sessionStartDate.getTime() < videoStartDate.getTime()) {
            return;
        } else {

            Date sliderStartDate;
            if (slideScaler.getIsActive()) {
                sliderStartDate = slideScaler.getRelativeStartDate();
            } else {
                sliderStartDate = videoStartDate;
            }


            Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
            Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
            Long startInSliderUnits = (annotationStartDate.getTime() - sliderStartDate.getTime()) / 100;
            Long endInSliderUnits = (annotationEndDate.getTime() - sliderStartDate.getTime()) / 100;

            setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);

            videoDataViewer.getMediaPlayer().seek(Duration.seconds((startInSliderUnits / (float) 10) + videoDataViewer.getMediaPlayer().getStartTime().toSeconds()));
        }

    }

    public void setAnnotationOnPSMSlider(PSMDataViewer psmDataViewer, RangeSlider rangeSlider, SlideScaler slideScaler) throws ParseException {

        Annotation annotation = getSelectedAnnotation();
        Date sessionStartDate = new Date(Long.parseLong(session.getStart_time()));
        Date psmStartDate = psmDataViewer.getAbsolutePSMStartDate();

        if (sessionStartDate.getTime() < psmStartDate.getTime()) {
            return;
        } else {

            Date sliderStartDate;
            if (slideScaler.getIsActive()) {
                sliderStartDate = slideScaler.getRelativeStartDate();
            } else {
                sliderStartDate = psmStartDate;
            }


            Date annotationStartDate = new Date(Long.parseLong(annotation.getStart_time()));
            Date annotationEndDate = new Date(Long.parseLong(annotation.getEnd_time()));
            Long startInSliderUnits = (annotationStartDate.getTime() - sliderStartDate.getTime()) / 100;
            Long endInSliderUnits = (annotationEndDate.getTime() - sliderStartDate.getTime()) / 100;

            setNewRangeSliderBounds(rangeSlider, startInSliderUnits, endInSliderUnits);

            double seekDurationValueSeconds = rangeSlider.getLowValue() / 10;
            psmDataViewer.seek(seekDurationValueSeconds);
        }

    }


    public void SaveAndUpdateButtonHandler(String type,RangeSlider focusedSlider) {
        saveUpdatesButton.setOnAction(actionEvent -> {

            Annotation annotation = getSelectedAnnotation();

            SimpleDateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

            Date startDate = new Date();

            if(type == "PSM"){
                startDate = absolutePSMStartDate;

            }else if(type == "Video"){
                startDate = this.absoluteVideoStartDate;

            }

            Long lowValue = (long) focusedSlider.getLowValue();
            Long highValue = (long) focusedSlider.getHighValue();


            Date newStartDate = new Date(startDate.getTime() + lowValue * 100);
            formatAndSetNewDisplayStartTime(format, newStartDate, annotation);

            Date newEndDate = new Date(startDate.getTime() + highValue * 100);
            formatAndSetNewDisplayEndTime(format, newEndDate, annotation);

            Long newUnixStartTime = newStartDate.toInstant().toEpochMilli();
            Long newUnixEndTime = newEndDate.toInstant().toEpochMilli();
            annotation.setStart_time(newUnixStartTime.toString());
            annotation.setEnd_time(newUnixEndTime.toString());
            annotation.setIsUpdated(true);
            highlightColumnChanges();
            updateTable();

        });
        saveSessionButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

                if (file != null) {
                    try {
                        jsonDataViewer.SaveFile(file);
                        removeHighlightingOnSave();
                    } catch (FileNotFoundException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    public void formatAndSetNewDisplayStartTime(SimpleDateFormat format, Date newStartDate, Annotation annotation) {
        String newDisplayStartTime = format.format(newStartDate);
        annotation.setDisplayStartTime(newDisplayStartTime);
    }

    public String formatStartDate(Annotation annotation, SimpleDateFormat format) {

        Date startDate = new Date(Long.parseLong(annotation.getStart_time()));
        String formattedStartDate = format.format(startDate);
        return formattedStartDate;

    }

    public String formatEndDate(Annotation annotation, SimpleDateFormat format) {

        Date endDate = new Date(Long.parseLong(annotation.getEnd_time()));
        String formattedEndDate = format.format(endDate);
        return formattedEndDate;

    }

    public void formatAndSetNewDisplayEndTime(SimpleDateFormat format, Date newEndDate, Annotation annotation) {
        String newDisplayEndTime = format.format(newEndDate);
        annotation.setDisplayEndTime(newDisplayEndTime);
    }

    public void setNewRangeSliderBounds(RangeSlider rangeSlider, Long startInSliderUnits, Long endInSliderUnits) {

        rangeSlider.setLowValue(rangeSlider.getMin());
        rangeSlider.setHighValue(rangeSlider.getMax());
        rangeSlider.setLowValue(rangeSlider.getMin() + startInSliderUnits);
        rangeSlider.setHighValue(rangeSlider.getMin() + endInSliderUnits);

    }

    public void highlightColumnChanges() {
        TableColumn categoryColumn = annotationTable.getColumns().get(0);
        TableColumn nameColumn = annotationTable.getColumns().get(1);
        TableColumn startColumn = annotationTable.getColumns().get(2);
        TableColumn endColumn = annotationTable.getColumns().get(3);

        highlightUpdatedCells(categoryColumn);
        highlightUpdatedCells(nameColumn);
        highlightUpdatedCells(startColumn);
        highlightUpdatedCells(endColumn);

    }

    public void highlightUpdatedCells(TableColumn tableColumn) {

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

    public void removeHighlightingOnSave() {
        for (int i = 0; i < annotationTable.getColumns().size(); i++) {
            TableColumn tableColumn = annotationTable.getColumns().get(i);

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
                            setStyle("");
                            annotation.setIsUpdated(false);

                        }
                    }

                }
            });
        }


    }


    public void scaleAndSetAnnotationsPerVideo(List<VideoDataViewer> list, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) throws ParseException {

        for (int i = 0; i < list.size(); i++) {
            VideoDataViewer videoDataViewer = list.get(i);
            slideScaler.setRelativeSliderVideoBoundaries(videoDataViewer.getTimeSlider(), videoDataViewer.getRangeSlider(), videoDataViewer.getMediaPlayer());
            annotationTableHandler.calculateAbsoluteVideoStartDate(videoDataViewer);
            annotationTableHandler.setAnnotationOnVideoSlider(videoDataViewer, videoDataViewer.getRangeSlider(), slideScaler);
        }
    }
    public void scaleAndSetAnnotationsPerPSM(List<PSMDataViewer> listOfPSMDataViewers, SlideScaler slideScaler, AnnotationTableHandler annotationTableHandler) throws ParseException {

        for (int i = 0; i < listOfPSMDataViewers.size(); i++) {
            PSMDataViewer psmDataViewer = listOfPSMDataViewers.get(i);
            slideScaler.setRelativeSliderPSMBoundaries(psmDataViewer.getTimeSlider(), psmDataViewer.getRangeSlider(),psmDataViewer);
            annotationTableHandler.calculateAbsolutePSMStartDate(psmDataViewer);
            annotationTableHandler.setAnnotationOnPSMSlider(psmDataViewer, psmDataViewer.getRangeSlider(), slideScaler);
        }
    }


}


