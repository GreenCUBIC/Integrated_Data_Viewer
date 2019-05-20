package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.DefaultInstancePackage;
import com.carleton.cubic.nicu_data_explorer.util.TimeUtils;
import com.sun.javafx.charts.Legend;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PmdiDataViewer extends IntegratedDataViewerInstance {

    private int HR_Y_LOWER_RANGE = -100;
    private int HR_y_UPPER_RANGE = 100;
    private int TICK_UNIT = 10;
    private int UPDATE_INTERVAL_MS = 1000;
    private Date absoluteStartDate;
    private Date absoluteEndDate;
    private boolean programmaticSliderValueChange = false;
    private boolean bubblesActive = true;


    private LineChart.Series<Number, Number> series1;
    private LineChart.Series<Number, Number> series2;
    private LineChart.Series<Number, Number> series3;
    private LineChart.Series<Number, Number> series4;

    private MLineChart<Number, Number> lineChart;
    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();
    private PMDIParser pmdiParser;

    private Data<Number, Number> nextHRPoint;
    private Data<Number, Number> nextRRPoint;
    private Data<Number, Number> nextSPO2Point;
    private Data<Number, Number> nextPLsPoint;
    private int sampleSize;

    private SequentialTransition animation;
    private Button increaseSampleSizeButton;
    private Button decreaseSampleSizeButton;
    private Button autoScaleYAxisButton;
    private Button removeBubblesButton;
    private Timeline timeline;


    public PmdiDataViewer(SubScene subScene, File file, Stage stage, DefaultInstancePackage defaultInstancePackage, ButtonPackage pmdiButtonPackage) {
        super(defaultInstancePackage);
        unpack(pmdiButtonPackage);
        sampleSize = 10;
        pmdiParser = new PMDIParser(file);
        absoluteStartDate = pmdiParser.getStartDate();
        absoluteEndDate = pmdiParser.getEndDate();
        subScene.setRoot(createContent());
        toggleLegend();
        stage.show();
        RangeSlider rangeSlider = customRangeSlider.getRangeSlider();
        setCustomRangeSliderStartAndEndDates();
        customSlider.sliderLimit(timeSlider, rangeSlider);
        sampleSizeButtonHandler(increaseSampleSizeButton, decreaseSampleSizeButton);
        yAxisAutoScalingButtonHandler(autoScaleYAxisButton);
        removeBubbleButtonHandler(removeBubblesButton);


        double seconds = (absoluteEndDate.toInstant().toEpochMilli() - absoluteStartDate.toInstant().toEpochMilli()) / 1000;

        playbackChoiceBox.setItems(FXCollections.observableArrayList(
                "0.5", "1.0", "2.0", "4.0", "8.0")
        );
        playbackChoiceBox.getSelectionModel().select(1);

        customSlider.sliderLimit(timeSlider, customRangeSlider.getRangeSlider());
        setRangeSliderMinAndMax(seconds);
        timeSlider.setMax(seconds * 10); //slider value is tenth of a second
        timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(UPDATE_INTERVAL_MS), actionEvent -> {

            incrementCounterAndShiftPoints();

            Date tickTime = TimeUtils.addOffsetToTime(absoluteStartDate, (long) pmdiParser.getCounter() * 1000);
            playTime.setText(TimeUtils.getFormattedTimeWithOutMillis(tickTime));
            programmaticSliderValueChange = true;
            timeSlider.setValue(nextHRPoint.getXValue().doubleValue());
            programmaticSliderValueChange = false;


            setNewXAxisBounds(xAxis, series1);            // update using series 1 as reference

        }));
        timeline.setCycleCount(Animation.INDEFINITE);


        animation = new SequentialTransition();
        animation.getChildren().addAll(timeline);
        speedHandler();

        playButton.setOnAction(e -> {
            if (animation.getStatus() == Animation.Status.RUNNING) {
                animation.pause();
                playButton.setText("Play");
            } else {
                animation.play();
                playButton.setText("Pause");
            }

            if (customSlider.isPositionOutOfBounds(timeSlider, rangeSlider)) {

                programmaticSliderValueChange = true;
                timeSlider.setValue(rangeSlider.getLowValue());
                long seekValueSeconds = Math.round(rangeSlider.getLowValue() / 10);

                animation.pause();

                clearGraphAndSeekSeconds(seekValueSeconds);
                programmaticSliderValueChange = false;

            }
        });

        loopButton.setOnAction(e -> {

            if (!loopRequested) {
                loopRequested = true;
                loopButton.setText(LOOP_STATUS_ON);

                if(Math.abs(rangeSlider.getHighValue()-timeSlider.getValue())<5){

                    timeSlider.setValue(rangeSlider.getLowValue());
                    long seekValueSeconds = Math.round(timeSlider.getValue() / 10);
                    clearGraphAndSeekSeconds(seekValueSeconds);
                }

            } else {
                loopRequested = false;
                loopButton.setText(LOOP_STATUS_OFF);
            }

        });

        timeSlider.setOnMouseReleased(event -> {

            long seekValueSeconds = Math.round(timeSlider.getValue() / 10);
            clearGraphAndSeekSeconds(seekValueSeconds);
        });

        timeSlider.valueProperty().addListener(ov -> {

            if (customSlider.shouldStopAtEnd(timeSlider, customRangeSlider.getRangeSlider(), loopRequested)) {
                animation.pause();
            }
            if (customSlider.shouldLoopAtEnd(timeSlider, customRangeSlider.getRangeSlider(), loopRequested)) {
                long seekValueSeconds = Math.round(customRangeSlider.getRangeSlider().getLowValue() / 10);

                clearGraphAndSeekSeconds(seekValueSeconds);

                timeSlider.adjustValue(customRangeSlider.getRangeSlider().getLowValue());
                animation.play();
            }
        });

        rangeSlider.lowValueProperty().addListener((ov, old_val, new_val) -> {
            lowValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(absoluteStartDate, rangeSlider.getLowValue() * 100)));
        });
        rangeSlider.highValueProperty().addListener((ov, old_val, new_val) -> {
            highValText.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(absoluteStartDate, rangeSlider.getHighValue() * 100)));
        });
        timeSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            playTime.setText(TimeUtils.getFormattedTimeWithMillis(TimeUtils.addOffsetToTime(absoluteStartDate, timeSlider.getValue() * 100)));
        });
    }


    private void clearBubblesForSeries(LineChart.Series<Number, Number> s) {

        for (Data<Number, Number> d : s.getData()) {
            if (d.getNode() != null) {
                d.getNode().setVisible(false); // Toggle visibility of every node in the series
            }
        }
    }

    private void unpack(ButtonPackage pmdiButtonPackage) {

        increaseSampleSizeButton = pmdiButtonPackage.getIncreaseSampleSizeButton();
        decreaseSampleSizeButton = pmdiButtonPackage.getDecreaseSampleSizeButton();
        autoScaleYAxisButton = pmdiButtonPackage.getAutoScaleYAxisButton();
        removeBubblesButton = pmdiButtonPackage.getRemoveBubblesButton();
    }

    private void yAxisAutoScalingButtonHandler(Button autoScaleYAxisButton) {

        autoScaleYAxisButton.setOnAction(event -> {

            if (autoScaleYAxisButton.getText().equals("Enable")) {

                yAxis.setAutoRanging(true);

                autoScaleYAxisButton.setText("Disable");
            } else {
                yAxis.setAutoRanging(false);
                autoScaleYAxisButton.setText("Enable");
            }

        });
    }

    private void removeBubbleButtonHandler(Button removeBubblesButton) {


        removeBubblesButton.setOnAction(event -> {

            if (removeBubblesButton.getText().equals("Enable")) {
                bubblesActive = false;

                clearBubblesForSeries(series1);
                clearBubblesForSeries(series2);
                clearBubblesForSeries(series3);
                clearBubblesForSeries(series4);

                removeBubblesButton.setText("Disable");
            } else {
                bubblesActive = true;
                removeBubblesButton.setText("Enable");
            }

        });

    }

    private void sampleSizeButtonHandler(Button increaseSampleSizeButton, Button decreaseSampleSizeButton) {

        increaseSampleSizeButton.setOnAction(event -> {

            sampleSize += 5;
        });
        decreaseSampleSizeButton.setOnAction(event -> {
            if (sampleSize > 5) {
                sampleSize -= 5;
            }
        });
    }

    private void setCustomRangeSliderStartAndEndDates() {

        customRangeSlider.setAbsoluteStartDate(absoluteStartDate);
        customRangeSlider.setAbsoluteEndDate(absoluteEndDate);
    }

    public void clearGraphAndSeekSeconds(long seekValueSeconds) {


        animation.pause();
        clearAllSeries();
        pmdiParser.setCounter((int) seekValueSeconds);
        System.out.println("Counter: " + pmdiParser.getCounter());

    }

    private void clearAllSeries() {
        series1.getData().clear();
        series2.getData().clear();
        series3.getData().clear();
        series4.getData().clear();
    }

    private void setNewXAxisBounds(NumberAxis xAxis, XYChart.Series<Number, Number> series1) {

        List<Data<Number, Number>> data = series1.getData();
        if (series1.getData().size() < sampleSize) {
            xAxis.setLowerBound(data.get(0).getXValue().doubleValue());
        } else {
            xAxis.setLowerBound(data.get(data.size() - sampleSize).getXValue().doubleValue());
        }
        xAxis.setUpperBound(data.get(data.size() - 1).getXValue().doubleValue());
    }

    private void setRangeSliderMinAndMax(Double seconds) {
        customRangeSlider.getRangeSlider().setMax(seconds * 10);
        customRangeSlider.getRangeSlider().setLowValue(0);
        customRangeSlider.getRangeSlider().setHighValue(customRangeSlider.getRangeSlider().getMax());
    }

    private void incrementCounterAndShiftPoints() {

        getSetOfPointsAndIncrementCounter();
        addPoints(nextHRPoint, nextRRPoint, nextSPO2Point, nextPLsPoint);
        removePointsOverMaxNumber();
    }

    public void toggleLegend() {
        //Itai wrote this code on Stack Overflow and I really liked his structure of code. https://stackoverflow.com/a/44957354


        for (Node n : lineChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (Legend.LegendItem li : l.getItems()) {
                    for (XYChart.Series<Number, Number> s : lineChart.getData()) {
                        if (s.getName().equals(li.getText())) {
                            li.getSymbol().setCursor(Cursor.HAND); // Hint user that legend symbol is clickable
                            li.getSymbol().setOnMouseClicked(me -> {
                                if (me.getButton() == MouseButton.PRIMARY) {
                                    s.getNode().setVisible(!s.getNode().isVisible()); // Toggle visibility of line
                                    for (Data<Number, Number> d : s.getData()) {
                                        if (d.getNode() != null) {
                                            d.getNode().setVisible(s.getNode().isVisible()); // Toggle visibility of every node in the series
                                        }
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }


    }

    private void speedHandler() {

        playbackChoiceBox.setOnAction(event -> {
            String dataSelectionValue = playbackChoiceBox.getValue();
            if (dataSelectionValue.equalsIgnoreCase("0.5")) {
                animation.setRate(0.5);
            } else if (dataSelectionValue.equalsIgnoreCase("1.0")) {
                animation.setRate(1);
            } else if (dataSelectionValue.equalsIgnoreCase("2.0")) {
                animation.setRate(2);
            } else if (dataSelectionValue.equalsIgnoreCase("4.0")) {
                animation.setRate(4);
            } else if (dataSelectionValue.equalsIgnoreCase("8.0")) {
                animation.setRate(8);
            }
        });
    }

    private void getSetOfPointsAndIncrementCounter() {

        if (pmdiParser.isNextPoint(2)) {
            nextHRPoint = pmdiParser.getNextPoint(2);
            int seriesNumber = 1;
            if (series1.getNode().isVisible() && bubblesActive) {
                nextHRPoint.setNode(new HoveredThresholdNode(nextHRPoint.getYValue().intValue(), seriesNumber));
                System.out.println("counter: " + pmdiParser.getCounter());
                System.out.println("Time Slider Value: " + timeSlider.getValue());
                System.out.println("Time in seconds: " + timeSlider.getValue()/10);

            }
        }
        if (pmdiParser.isNextPoint(4)) {
            nextRRPoint = pmdiParser.getNextPoint(4);
            int seriesNumber = 2;

            if (series2.getNode().isVisible() && bubblesActive) {
                nextRRPoint.setNode(new HoveredThresholdNode(nextRRPoint.getYValue().intValue(), seriesNumber));
            }
        }
        if (pmdiParser.isNextPoint(6)) {
            nextSPO2Point = pmdiParser.getNextPoint(6);
            System.out.println("nextSPO2Point X value: " + nextSPO2Point.getXValue());
            System.out.println("nextSPO2Point Y value: " + nextSPO2Point.getYValue());
            int seriesNumber = 3;

            if (series3.getNode().isVisible() && bubblesActive) {
                nextSPO2Point.setNode(new HoveredThresholdNode(nextSPO2Point.getYValue().intValue(), seriesNumber));
            }
        }
        if (pmdiParser.isNextPoint(3)) {
            nextPLsPoint = pmdiParser.getNextPoint(3);
            int seriesNumber = 4;

            if (series4.getNode().isVisible() && bubblesActive) {
                nextPLsPoint.setNode(new HoveredThresholdNode(nextPLsPoint.getYValue().intValue(), seriesNumber));
            }
        }
        if (!pmdiParser.isNextPoint(2)) {
            animation.pause();
        }

        pmdiParser.incrementCounter();
    }


    private void removePointsOverMaxNumber() {

        removeSeriesPointOverMax(series1);
        removeSeriesPointOverMax(series2);
        removeSeriesPointOverMax(series3);
        removeSeriesPointOverMax(series4);

    }

    private void removeSeriesPointOverMax(XYChart.Series<Number, Number> series) {

        if (series.getData().size() > sampleSize) {
            series.getData().remove(0);
        }
    }

    private void addPoints(Data<Number, Number> nextHRPoint, Data<Number, Number> nextRRPoint, Data<Number, Number> nextSPO2Point, Data<Number, Number> nextPLsPoint) {

        series1.getData().add(nextHRPoint);
        series2.getData().add(nextRRPoint);
        series3.getData().add(nextSPO2Point);
        series4.getData().add(nextPLsPoint);
    }

    public Parent createContent() {
        xAxis = new NumberAxis();
        xAxis.tickLabelFontProperty().set(Font.font(10));
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        xAxis.setLabel("Time");
        StringConverter<Number> converter = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {

                Date currentTime = new Date(absoluteStartDate.toInstant().toEpochMilli() + object.longValue() * 100);
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                return df.format(currentTime);

            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        };

        xAxis.setAnimated(false);
        xAxis.setTickLabelFormatter(converter);

        yAxis = new NumberAxis(HR_Y_LOWER_RANGE, HR_y_UPPER_RANGE, TICK_UNIT);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(150);
        yAxis.setLabel("Arbitrary Unit");

        lineChart = new MLineChart<>(xAxis, yAxis);
        lineChart.setAnimated(true);
        lineChart.setLegendVisible(true);
        lineChart.setCreateSymbols(false);

        series1 = new LineChart.Series<>();
        series1.setName("HR");
        lineChart.getData().add(series1);

        series2 = new LineChart.Series<>();
        series2.setName("RR");
        lineChart.getData().add(series2);

        series3 = new LineChart.Series<>();
        series3.setName("SpO2");
        lineChart.getData().add(series3);

        series4 = new LineChart.Series<>();
        series4.setName("PLs");
        lineChart.getData().add(series4);

        return lineChart;
    }

    public void play() {
        animation.play();
    }

    public void stop() {
        animation.pause();
    }

    public Date getAbsoluteStartDate() {
        return absoluteStartDate;
    }

    public Button getPlayButton() {

        return playButton;
    }

    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int value, int color) {
            setPrefSize(15, 15);
            final Label label = createDataThresholdLabel(value, color);

            setOnMouseEntered(mouseEvent -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        private Label createDataThresholdLabel(int value, int seriesNumber) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            Color color = Color.DARKGRAY;
            switch (seriesNumber) {
                case 1:
                    color = Color.RED;
                    break;
                case 2:
                    color = Color.ORANGE;
                    break;
                case 3:
                    color = Color.GREEN;
                    break;
                case 4:
                    color = Color.BLUE;
                    break;
            }

            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
            label.setTextFill(color);
            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }


    }


}