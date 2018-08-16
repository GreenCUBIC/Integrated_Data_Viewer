package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.Annotation;
import com.carleton.cubic.nicu_data_explorer.util.Session;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public class JsonDataViewer {
    private File jsonFile;
    private List<Annotation> annotationList;
    private Session session;
    private Session runningSession;





    public JsonDataViewer(File jsonFile) {
        this.jsonFile = jsonFile;

    }



    public void setAnnotationList(List<Annotation> annotationList) {
        this.annotationList = annotationList;
    }

    public void openJson() throws FileNotFoundException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        FileReader reader = new FileReader(jsonFile);
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        JsonArray annotationArray = jsonObject.getAsJsonArray("annotations");
        Type type = new TypeToken<List<Annotation>>() {
        }.getType();
        List<Annotation> annotationList = gson.fromJson(annotationArray, type);
        this.setAnnotationList(annotationList);
    }

    public Session loadSession(File sessionFile) throws FileNotFoundException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonReader reader = new JsonReader(new FileReader(sessionFile.getAbsolutePath()));
        Session session = gson.fromJson(reader, Session.class);
        this.session = session;
        return session;
    }

    public void SaveFile(File saveFile) {

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        try {
            FileWriter fileWriter = new FileWriter(saveFile);
            fileWriter.write(gson.toJson(session));
            fileWriter.close();
        } catch (IOException ex) {
        }


    }


}
