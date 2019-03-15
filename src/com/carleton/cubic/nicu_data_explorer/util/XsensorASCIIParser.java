package com.carleton.cubic.nicu_data_explorer.util;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;


public class XsensorASCIIParser
{

    private File asciiFile;
    Thread parserThread;

    public XsensorASCIIParser(File asciiFile)
    {
        this.asciiFile = asciiFile;
    }

    public int parseForLastFrameNumber()
    {
        ReversedLinesFileReader reversedReader;
        try
        {
            reversedReader = new ReversedLinesFileReader(asciiFile, Charset.defaultCharset());
            String line;
            line = reversedReader.readLine();
            while (line != null)
            {
                if (line.startsWith("Frame:"))
                {
                    String[] frameNumberHeader = line.split(":,");
                    return Integer.parseInt(frameNumberHeader[1]);
                }
                line = reversedReader.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    public PSMRecording parse() throws IndexOutOfBoundsException
    {
        PSMRecording psmRecording = new PSMRecording();

        Thread parserThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new FileReader(asciiFile)))
            {
                //Process the file header
                String headerLine;
                while ((headerLine = br.readLine()) != null)
                {
                    if (headerLine.isEmpty() || headerLine.charAt(0) == ',')
                    {
                        break;
                    }
                    String[] headerKeyValue = headerLine.split(Pattern.quote(":,"));

                    psmRecording.putFileHeader(headerKeyValue[0], headerKeyValue[1]);
                }

                //Process the frame header
                String frameLine;
                int frameCount = 0;
                while ((frameLine = br.readLine()) != null)
                {
                    String frameHeaderLine = frameLine;
                    while (!frameHeaderLine.contains("Sensels:"))
                    {
                        if (!frameHeaderLine.isEmpty() && !(frameHeaderLine.charAt(0) == ','))
                        {
                            String[] headerKeyValue = frameHeaderLine.split(":,");
                            String key = headerKeyValue[0];
                            String value = headerKeyValue[1];
                            if (key.equals("Frame"))
                            {
                                psmRecording.addNewFrame();
                                frameCount++;
                            }

                            int frameIndex = frameCount - 1;
                            psmRecording.addFrameHeader(frameIndex, key, value);
                        }
                        frameHeaderLine = br.readLine();
                    }

                    String senselLine = br.readLine();
                    int frameRow = 0;
                    while (!senselLine.isEmpty() && senselLine.charAt(0) != ',')
                    {
                        String[] dataValuesStr = senselLine.split(",");
                        float[] dataValues = new float[dataValuesStr.length];
                        for (int i = 0; i < dataValuesStr.length; i++)
                        {
                            dataValues[i] = Float.parseFloat(dataValuesStr[i]);
                        }

                        psmRecording.addFrameData(frameCount - 1, frameRow, dataValues);
                        frameRow++;
                        senselLine = br.readLine();
                    }
                }
                psmRecording.setParsingComplete(true);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        parserThread.start();

        return psmRecording;
    }

    public int parseForFirstFrameNumber() {

        return -1;

    }
}
