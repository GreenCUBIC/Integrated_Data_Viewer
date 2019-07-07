package com.carleton.cubic.nicu_data_explorer.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PSMRecording
{

    private final Map<String, HeaderValue> fileHeader;
    private final ArrayList<Map<String, HeaderValue>> frameHeaders;
    private final ArrayList<float[][]> frameData;

    PSMRecording()
    {
        this.fileHeader = new HashMap<>();
        this.frameHeaders = new ArrayList<>();
        this.frameData = new ArrayList<>();
    }

    void putFileHeader(String key, String value)
    {

        this.fileHeader.put(key, new HeaderValue(value));
    }

    public HeaderValue getFileHeader(String key)
    {
        return this.fileHeader.get(key);
    }

    void addFrameHeader(int frameIndex, String key, String value)
    {
        Map<String, HeaderValue> frameHeader = frameHeaders.get(frameIndex);
        frameHeader.put(key, new HeaderValue(value));
    }

    public HeaderValue getFrameHeader(int frameIndex, String key)
    {
        Map<String, HeaderValue> frameHeader = frameHeaders.get(frameIndex);
        return frameHeader.get(key);
    }

    void addNewFrame()
    {
        this.frameHeaders.add(new HashMap<>());
    }

    void addFrameData(int frameIndex, int frameRow, float[] columnValues)
    {
        if (frameData.size() < frameIndex + 1)
        {
            HeaderValue rowCount = frameHeaders.get(frameIndex).get("Rows");
            HeaderValue columnCount = frameHeaders.get(frameIndex).get("Columns");
            frameData.add(new float[rowCount.integerValue()][columnCount.integerValue()]);
        }

        float[][] singleFrameData = frameData.get(frameIndex);
        System.arraycopy(columnValues, 0, singleFrameData[frameRow], 0, columnValues.length);
    }

    public int frameCount()
    {
        return frameData.size();
    }

    public float[][] getFrameData(int frameIndex)
    {
        return frameData.get(frameIndex);
    }

}
