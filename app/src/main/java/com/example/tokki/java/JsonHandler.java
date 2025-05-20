package com.example.tokki.java;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class JsonHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonHandler() {
    }

    public static Store readStoreFromJson(String filePath) throws IOException {
        /*
        Diavazei ena json arxeio kai dhmiourgei ena antikeimeno tupou Store apo auto
         */
        File file = new File(filePath);

        if (!file.exists()) { //elexos an iparxei to path
            throw new FileNotFoundException("JSON file not found: " + filePath);
        }
        if (file.length() == 0) {  //an to arxeio einai keno
            throw new IOException("JSON file is empty: " + filePath);
        }

        return objectMapper.readValue(file, Store.class);
    }

    public static Store readStoreFromAssets(Context context, String filename) throws IOException {
        try (InputStream inputStream = context.getAssets().open(filename)) {
            // Add debug logging
            Log.d("AssetDebug", "Attempting to read: " + filename);
            String json = new Scanner(inputStream).useDelimiter("\\A").next();
            Log.d("AssetDebug", "Raw JSON content: " + json.substring(0, Math.min(100, json.length())) + "...");

            inputStream.reset(); // Reset stream after reading
            return objectMapper.readValue(inputStream, Store.class);
        }
    }

}
