package com.carleton.cubic.nicu_data_explorer.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;


public class AnnotationController
{
    private TableView<Annotation> annotationTable;
    JsonDataViewer jsonDataViewer;
    ObservableList<Annotation> tableData;
    private File file;

    public File getFile()
    {
        return file;
    }

    public TableView<Annotation> getAnnotationTable()
    {
        return annotationTable;
    }


    public AnnotationController(Scene scene)
    {
        annotationTable = (TableView) scene.lookup("#annotationTable");
        for (Object object : annotationTable.getColumns())
        {
            TableColumn column = (TableColumn) object;
            column.setCellValueFactory(new PropertyValueFactory<Annotation, String>(column.getId()));

        }

    }

    public void setJSON(File jsonFile) throws FileNotFoundException, ParseException
    {
        JsonDataViewer jsonDataViewer = new JsonDataViewer(jsonFile);
        Session session = jsonDataViewer.loadSession(jsonFile);
        this.file = jsonFile;
        ObservableList<Annotation> tableData = FXCollections.observableList(session.getAnnotations());
        this.tableData = tableData;
        annotationTable.setItems(tableData);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
        String formattedDate;
        this.jsonDataViewer = jsonDataViewer;
        for (int i = 0; i < annotationTable.getItems().size(); i++)
        {
            Annotation annotation = annotationTable.getItems().get(i);
            Date date = new Date(Long.parseLong(annotation.getStart_time()));
            formattedDate = format.format(date);
            annotation.setStart_time(formattedDate);
            date = new Date(Long.parseLong(annotation.getEnd_time()));
            formattedDate = format.format(date);
            annotation.setEnd_time(formattedDate);
        }


    }


    public Annotation getSelectedAnnotation()
    {
        Annotation selectedAnnotation;
        selectedAnnotation = annotationTable.getSelectionModel().getSelectedItem();
        return selectedAnnotation;

    }

    public void updateTable()
    {


        for (int i = 0; i < annotationTable.getColumns().size(); i++)
        {
            annotationTable.getColumns().get(i).setVisible(false);
            annotationTable.getColumns().get(i).setVisible(true);

        }
    }
    public void setUpAnnotation(Annotation annotation, RangeSlider rangeSlider, Session session) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

        String formattedSessionStartDate;
        Date startDate = new Date(Long.parseLong(session.getStart_time()));
        formattedSessionStartDate = format.format(startDate);

        Date AnnotationStartTime = format.parse(annotation.getStart_time());
        Date startTime = format.parse(annotation.getStart_time());
        Date endTime = format.parse(annotation.getEnd_time());
        Date SessionstartTime = format.parse(formattedSessionStartDate);

        long sliderStartTime = startTime.getTime() - SessionstartTime.getTime();
        long sliderEndTime = endTime.getTime() - SessionstartTime.getTime();
        rangeSlider.setLowValue(0);
        rangeSlider.setHighValue(1000000000);
        rangeSlider.setLowValue(sliderStartTime / 100);
        rangeSlider.setHighValue(sliderEndTime / 100);

    }

}
