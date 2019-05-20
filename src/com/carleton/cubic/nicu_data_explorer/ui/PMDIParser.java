package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PMDIParser {

    private List<String[]> content;
    private int counter;
    private XYChart.Series hrSeries;
    private XYChart.Series rrSeries;
    private XYChart.Series spO2Series;
    private XYChart.Series plsSeries;


    public PMDIParser(File file) {

        try {
            parseCsvIntoList(file);
            hrSeries = extractSeriesFromList(2);
            rrSeries = extractSeriesFromList(4);
            spO2Series = extractSeriesFromList(6);
            plsSeries = extractSeriesFromList(3);
            counter = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public XYChart.Data<Number, Number> getNextPoint(int columnNumber) {
        XYChart.Data<Number, Number> point;
        XYChart.Series selectedSeries = new XYChart.Series();
        switch (columnNumber){
            case 2:  selectedSeries = hrSeries;
            break;
            case 4:  selectedSeries = rrSeries;
            break;
            case 6:  selectedSeries = spO2Series;
            break;
            case 3:  selectedSeries = plsSeries;
            break;
        }
        point =  (XYChart.Data<Number, Number>)selectedSeries.getData().get(counter);


        return point;

    }
    public XYChart.Series extractSeriesFromList(int columnNumber) {

        XYChart.Series extractedSeries = new XYChart.Series();
        for (int i = 1; i<content.size();i++) {
            String[] line = content.get(i);
            Long epochInMillis = parseDate(line[0]).toInstant().toEpochMilli();
            if((line.length>columnNumber)) {

                if(line[columnNumber].equals("^^")){
                    extractedSeries.getData().add(new AreaChart.Data<>(i*10, 0));

                }else if(line[columnNumber].equals("")){
                    extractedSeries.getData().add(new AreaChart.Data<>(i*10, 0));

                }else if(line[columnNumber].equals("N/A")){
                    extractedSeries.getData().add(new AreaChart.Data<>(i*10, 0));

                }else{
                    extractedSeries.getData().add(new AreaChart.Data<>(i*10, Double.parseDouble(line[columnNumber])));

                }
            }
        }
        return extractedSeries;
    }


    public List<String[]> parseCsvIntoList(File file) throws IOException {
        List<String[]> content = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.equals(",,,,")){

                }
                else {
                    content.add(line.split(","));
                }
            }
        } catch (FileNotFoundException e) {
            //Some error logging
        } catch (IOException e) {
            e.printStackTrace();
        }
        displayData(content);
        this.content = content;
        return content;
    }

    public Date getStartDate() {

        String dateString = content.get(1)[0];

        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public Date getEndDate() {

        String dateString = content.get(content.size()-1)[0];

        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public Date parseDate(String string) {



        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            date = formatter.parse(string);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public void displayData(List<String[]> data) {

        for (String[] line : data) {

            for (int i = 0; i < line.length; i++) {

                System.out.print(line[i] + " ");
            }
            System.out.print("\n");
        }

    }

    public XYChart.Series getHrSeries() {
        return hrSeries;
    }

    public XYChart.Series getRRSeries() {
        return rrSeries;
    }

    public XYChart.Series getSpO2Series() {
        return spO2Series;
    }

    public XYChart.Series getPLsSeries() {
        return plsSeries;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void incrementCounter() {
        counter++;
    }

    public boolean isNextPoint(int columnNumber) {
        XYChart.Series selectedSeries = new XYChart.Series();
        switch (columnNumber){
            case 2:  selectedSeries = hrSeries;
                break;
            case 4:  selectedSeries = rrSeries;
                break;
            case 6:  selectedSeries = spO2Series;
                break;
            case 3:  selectedSeries = plsSeries;
                break;
        }
        if(counter  >= selectedSeries.getData().size()){
            return false;
        }
      return selectedSeries.getData().get(counter) != null;

    }
    }
