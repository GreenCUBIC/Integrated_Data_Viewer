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
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnnotationTableHandler {
    private TableView<Annotation> annotationTable;
    private Button saveSessionButton;
    private Button saveUpdatesButton;
    private static final String SIMPLE_DATE_FORMAT = "HH:mm:ss.SSS";
    private static final String HIGHLIGHTED_CELL_FORMAT = "-fx-background-color: yellow";

    JsonDataViewer jsonDataViewer;
    ObservableList<Annotation> tableData;
    ObservableList<Annotation> highlightedRows;
    private File file;
    private Date absoluteVideoStartDate;
    private Session session;
    public void calculateAbsoluteVideoStartDate(VideoDataViewer videoDataViewer){

        MediaPlayer mediaPlayer = videoDataViewer.getMediaPlayer();
        Date absoluteStartTime = videoDataViewer.getAbsoluteRecordingStartTime();
        Long absoluteEndMillis = absoluteStartTime.toInstant().toEpochMilli() + (long) mediaPlayer.getStopTime().toMillis();
        Date absoluteEndTime = new Date(absoluteEndMillis);
        this.absoluteVideoStartDate = absoluteStartTime;
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

    private String formatStartDate(Annotation annotation, SimpleDateFormat format) {

        Date startDate = new Date(Long.parseLong(annotation.getStart_time()));
        String formattedStartDate = format.format(startDate);
        return formattedStartDate;

    }

    private String formatEndDate(Annotation annotation, SimpleDateFormat format) {

        Date endDate = new Date(Long.parseLong(annotation.getEnd_time()));
        String formattedEndDate = format.format(endDate);
        return formattedEndDate;

    }


    public Annotation getSelectedAnnotation() {
        Annotation selectedAnnotation;
        selectedAnnotation = annotationTable.getSelectionModel().getSelectedItem();
        return selectedAnnotation;

    }

    public void updateTable() {
        for (int i = 0; i < annotationTable.getColumns().size(); i++) {
            ;
            annotationTable.getColumns().get(i).setVisible(false);
            annotationTable.getColumns().get(i).setVisible(true);
        }
    }

    public void setAnnotationOnSlider(VideoDataViewer videoDataViewer, RangeSlider rangeSlider, SlideScaler slideScaler) throws ParseException {

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

            System.out.println(rangeSlider.getMin());
            System.out.println(rangeSlider.getMax());

            rangeSlider.setLowValue(rangeSlider.getMin());
            rangeSlider.setHighValue(rangeSlider.getMax());
            rangeSlider.setLowValue(rangeSlider.getMin()+startInSliderUnits);
            rangeSlider.setHighValue(rangeSlider.getMin()+endInSliderUnits);

            System.out.println(rangeSlider.getLowValue());
            System.out.println(rangeSlider.getHighValue());

            videoDataViewer.getMediaPlayer().seek(Duration.seconds((startInSliderUnits/(float)10)+videoDataViewer.getMediaPlayer().getStartTime().toSeconds()));
        }

    }

    public void SaveAndUpdateButtonHandler(RangeSlider rangeSlider, VideoDataViewer videoDataViewer) {
        saveUpdatesButton.setOnAction(actionEvent -> {
            Annotation annotation = getSelectedAnnotation();

            SimpleDateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);Date videoStartDate = this.absoluteVideoStartDate;

            Long lowValue = (long) rangeSlider.getLowValue();
            Long highValue = (long) rangeSlider.getHighValue();


            Date newStartDate = new Date(videoStartDate.getTime() + lowValue * 100);
            formatAndSetNewDisplayStartTime(format, newStartDate, annotation);

            Date newEndDate = new Date(videoStartDate.getTime() + highValue * 100);
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

    private void formatAndSetNewDisplayStartTime(SimpleDateFormat format, Date newStartDate, Annotation annotation) {
        String newDisplayStartTime = format.format(newStartDate);
        annotation.setDisplayStartTime(newDisplayStartTime);
    }

    private void formatAndSetNewDisplayEndTime(SimpleDateFormat format, Date newEndDate, Annotation annotation) {
        String newDisplayEndTime = format.format(newEndDate);
        annotation.setDisplayEndTime(newDisplayEndTime);
    }

    public void highlightColumnChanges() {
        TableColumn categoryColumn = annotationTable.getColumns().get(0);
        TableColumn nameColumn = annotationTable.getColumns().get(1);
        TableColumn startColumn = annotationTable.getColumns().get(2);
        TableColumn endColumn = annotationTable.getColumns().get(3);

        handleRowFactory(categoryColumn);
        handleRowFactory(nameColumn);
        handleRowFactory(startColumn);
        handleRowFactory(endColumn);

    }

    public void handleRowFactory(TableColumn tableColumn) {

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


}


