import org.junit.jupiter.api.Test;
import com.carleton.cubic.nicu_data_explorer.util.PSMRecording;
import com.carleton.cubic.nicu_data_explorer.util.XsensorASCIIParser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class XSensorASCIIParserTest
{

    @Test
    public void parserTest()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("xsensorTestASCII").getFile());
        XsensorASCIIParser parser = new XsensorASCIIParser(file);

        PSMRecording psmRecording = parser.parse();

        String fileHeaderStr = psmRecording.getFileHeader("File").stringValue();
        assertTrue(fileHeaderStr.equals("E:\\Test_Data\\xsensorTestASCII"));

        String dateHeaderStr = psmRecording.getFrameHeader(0, "Date").stringValue();
        assertTrue(dateHeaderStr.equals("\"2016-10-20\""));
        assertTrue(psmRecording.frameCount() == 5);

        float[][] frameData0 = psmRecording.getFrameData(0);
        assertTrue(frameData0.length == 36);
        assertTrue(frameData0[0].length == 36);

    }

    @Test
    public void parseForLastFrameNumberTest()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("xsensorTestASCII").getFile());
        XsensorASCIIParser parser = new XsensorASCIIParser(file);

        int lastFrameNumber = parser.parseForLastFrameNumber();
        assertTrue(lastFrameNumber == 5);
    }
}
