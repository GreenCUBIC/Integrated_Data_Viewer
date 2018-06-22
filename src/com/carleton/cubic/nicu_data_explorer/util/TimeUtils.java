package com.carleton.cubic.nicu_data_explorer.util;

import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils
{
    public static String formattedDurationForDisplay(Duration duration)
    {
        double totalSeconds = duration.toSeconds();
        int hour = (int) totalSeconds / 3600;
        double remainingSeconds = totalSeconds % 3600;
        int minute = (int) (remainingSeconds / 60);
        int seconds = (int) (remainingSeconds % 60);
        int oneTenthSecond = (int) ((remainingSeconds % 1) * 10);

        return "" + hour + ":" + minute + ":" + seconds + ":" + oneTenthSecond;
    }

    public static String getFormattedTimeWithOutMillis(Date dateTime)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(dateTime);
    }

    public static String getFormattedTimeWithMillis(Date dateTime)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.S");
        return dateFormat.format(dateTime);
    }

    public static Date addOffsetToTime(Date baseTime, Double offsetMs)
    {
        return addOffsetToTime(baseTime, offsetMs.longValue());
    }
    public static Date addOffsetToTime(Date baseTime, Long offsetMs)
    {
        Date tickTime = new Date();
        tickTime.setTime(baseTime.getTime() + offsetMs);
        return tickTime;
    }

}
