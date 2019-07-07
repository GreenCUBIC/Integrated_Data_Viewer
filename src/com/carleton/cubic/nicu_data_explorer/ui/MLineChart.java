package com.carleton.cubic.nicu_data_explorer.ui;

import javafx.beans.NamedArg;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

import java.util.ArrayList;
import java.util.List;

class MLineChart<X, Y> extends LineChart<X, Y> {

    MLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    @Override
    protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> ya = getYAxis();
        List<X> xData = null;
        List<Y> yData = null;
        if (xa.isAutoRanging()) xData = new ArrayList<>();
        if (ya.isAutoRanging()) yData = new ArrayList<>();
        if (xData != null || yData != null) {
            for (Series<X, Y> series : getData()) {
                if (series.getNode().isVisible()) { // consider only visible series
                    for (Data<X, Y> data : series.getData()) {
                        if (xData != null) xData.add(data.getXValue());
                        if (yData != null) yData.add(data.getYValue());
                    }
                }
            }
            // RT-32838 No need to invalidate range if there is one data item - whose value is zero.
            if (xData != null && !(xData.size() == 1 && getXAxis().toNumericValue(xData.get(0)) == 0)) {
                xa.invalidateRange(xData);
            }
            if (yData != null && !(yData.size() == 1 && getYAxis().toNumericValue(yData.get(0)) == 0)) {
                ya.invalidateRange(yData);
            }

        }
    }
}