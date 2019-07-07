package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.scene.chart.XYChart;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class PMDIParser {

    private List<String[]> content;
    private int counter;
    private final XYChart.Series<Number, Number> hrSeries;
    private final XYChart.Series<Number, Number> rrSeries;
    private final XYChart.Series<Number, Number> spO2Series;
    private final XYChart.Series<Number, Number> plsSeries;


    PMDIParser(File file) {

        parseCsvIntoList(file);
        hrSeries = extractSeriesFromList(2);
        rrSeries = extractSeriesFromList(4);
        spO2Series = extractSeriesFromList(6);
        plsSeries = extractSeriesFromList(3);
        counter = 0;
    }

    XYChart.Data<Number, Number> getNextPoint(int columnNumber) {
        XYChart.Data<Number, Number> point;
        XYChart.Series<Number, Number> selectedSeries = new XYChart.Series<>();
        switch (columnNumber) {
            case 2:
                selectedSeries = hrSeries;
                break;
            case 4:
                selectedSeries = rrSeries;
                break;
            case 6:
                selectedSeries = spO2Series;
                break;
            case 3:
                selectedSeries = plsSeries;
                break;
        }
        point = selectedSeries.getData().get(counter);


        return point;

    }

    private XYChart.Series<Number, Number> extractSeriesFromList(int columnNumber) {

        XYChart.Series<Number, Number> extractedSeries = new XYChart.Series<>();
        for (int i = 1; i < content.size(); i++) {
            String[] line = content.get(i);
            if ((line.length > columnNumber)) {

                switch (line[columnNumber]) {
                    case "^^":
                    case "":
                    case "N/A":
                        extractedSeries.getData().add(new XYChart.Data<>(i * 10, 0));
                        break;
                    default:
                        extractedSeries.getData().add(new XYChart.Data<>(i * 10, Double.parseDouble(line[columnNumber])));
                        break;
                }

            } else {
                extractedSeries.getData().add(new XYChart.Data<>(i * 10, 0));
            }
        }
        return extractedSeries;
    }


    private void parseCsvIntoList(File file) {
        List<String[]> content = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals(",,,,")) {
                    content.add(line.split(","));
                }
            }
        } catch (FileNotFoundException e) {
            //Some error logging
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.content = content;
    }

    Date getStartDate() {

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

    Date getEndDate() {

        String dateString = content.get(content.size() - 1)[0];

        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    int getCounter() {
        return counter;
    }

    void setCounter(int counter) {
        this.counter = counter;
    }

    void incrementCounter() {
        counter++;
    }

    boolean isNextPoint(int columnNumber) {
        XYChart.Series<Number, Number> selectedSeries = new XYChart.Series<>();
        switch (columnNumber) {
            case 2:
                selectedSeries = hrSeries;
                break;
            case 4:
                selectedSeries = rrSeries;
                break;
            case 6:
                selectedSeries = spO2Series;
                break;
            case 3:
                selectedSeries = plsSeries;
                break;
        }
        if (counter >= selectedSeries.getData().size()) {
            return false;
        }
        return selectedSeries.getData().get(counter) != null;

    }
}
