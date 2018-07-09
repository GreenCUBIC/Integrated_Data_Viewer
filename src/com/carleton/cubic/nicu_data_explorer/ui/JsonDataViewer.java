package com.carleton.cubic.nicu_data_explorer.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JsonDataViewer
{
    private File jsonFile;
    private List<Annotation> annotationList;
    private Session session;

    public Session getSession()
    {
        return session;
    }

    public JsonDataViewer(File jsonFile)
    {
        this.jsonFile = jsonFile;

    }

    public List getAnnotationList()
    {
        return this.annotationList;
    }

    public void setAnnotationList(List<Annotation> annotationList)
    {
        this.annotationList = annotationList;
    }

    public void openJson() throws FileNotFoundException
    {
        Gson gson = new Gson();
        FileReader reader = new FileReader(jsonFile);
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        JsonArray annotationArray = jsonObject.getAsJsonArray("annotations");
        Type type = new TypeToken<List<Annotation>>()
        {
        }.getType();
        List<Annotation> annotationList = gson.fromJson(annotationArray, type);
        this.setAnnotationList(annotationList);
    }

    public Session loadSession(File sessionFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(sessionFile.getAbsolutePath()));
        Session session = gson.fromJson(reader, Session.class);
        this.session = session;
        return session;
    }


}
