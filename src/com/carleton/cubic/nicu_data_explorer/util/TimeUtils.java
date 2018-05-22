package com.carleton.cubic.nicu_data_explorer.util;

import javafx.util.Duration;

public class TimeUtils
{
    public static String formatDisplayTime(Duration duration)
    {
        double totalSeconds = duration.toSeconds();
        int hour = (int) totalSeconds / 3600;
        double remainingSeconds = totalSeconds % 3600;
        int minute = (int) (remainingSeconds / 60);
        int seconds = (int) (remainingSeconds % 60);
        int oneTenthSecond = (int) ((remainingSeconds % 1) * 10);

        return "" + hour + ":" + minute + ":" + seconds + ":" + oneTenthSecond;
    }
}
