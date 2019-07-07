package com.carleton.cubic.nicu_data_explorer.ui;

import com.carleton.cubic.nicu_data_explorer.util.Session;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;

class JsonDataViewer {
    private Session session;


    JsonDataViewer() {

    }


    Session loadSession(File sessionFile) throws FileNotFoundException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonReader reader = new JsonReader(new FileReader(sessionFile.getAbsolutePath()));
        Session session = gson.fromJson(reader, Session.class);
        this.session = session;
        return session;
    }

    void SaveFile(File saveFile) {

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        try {
            FileWriter fileWriter = new FileWriter(saveFile);
            fileWriter.write(gson.toJson(session));
            fileWriter.close();
        } catch (IOException ignored) {
        }


    }


}
