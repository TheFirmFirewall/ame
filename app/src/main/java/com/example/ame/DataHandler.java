package com.example.ame;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DataHandler {
    Map<String, ArrayList<String>> uriData, fileData, assetData;
    Context ctx;
    String fileName, assetName;
    Uri uri;

    public static final int FROM_FILE = 0;
    public static final int FROM_URI = 1;
    public static final int FROM_ASSETS = 2;

    DataHandler(Context context) {
        this.uriData = new HashMap<>();
        this.fileData = new HashMap<>();
        this.assetData = new HashMap<>();
        this.ctx = context;
        fileName = "";
        assetName = "data_small.txt";
        uri = null;
    }

    ArrayList<String> parser(BufferedReader reader) throws IOException {
        ArrayList<String> arrayList = new ArrayList<>();
        if(reader == null) return arrayList;
        String mLine;
        String[] arr;
        while ((mLine = reader.readLine()) != null) {
            arr = mLine.split(" ");
            for (String value : arr) {
                if (value.length() > 0)
                    arrayList.add(value.toLowerCase(Locale.ROOT));
            }
        }
        return arrayList;
    }


    ArrayList<String> get(int from) {
        Log.d("data", "get: " + from);
        if((from == FROM_URI && uri == null) ||
                (from == FROM_FILE && fileName.length() == 0) ||
                (from == FROM_ASSETS && assetName.length() == 0))
            return new ArrayList<>();
        if(from == FROM_URI && (uriData.containsKey(uri.toString()))) {
            return uriData.get(uri.toString());
        } else if(from == FROM_FILE && fileData.containsKey(fileName)){
            return fileData.get(fileName);
        } else if(from == FROM_ASSETS && assetData.containsKey(assetName)) {
            return assetData.get(assetName);
        }
        ArrayList<String> ans = new ArrayList<>();
        BufferedReader reader = null;
        try {
            switch (from) {
                case FROM_FILE:
                    reader = new BufferedReader(
                            new InputStreamReader(ctx.openFileInput(fileName), StandardCharsets.UTF_8));
                    break;
                case FROM_URI:
                    reader = new BufferedReader(
                            new InputStreamReader((ctx.getContentResolver().openInputStream(uri)), StandardCharsets.UTF_8));
                    break;
                case FROM_ASSETS:
                    reader = new BufferedReader(
                            new InputStreamReader(ctx.getAssets().open(assetName), StandardCharsets.UTF_8));
                    break;
            }
            ans = parser(reader);
        } catch (IOException e) {
            Log.d("IO", "failed to get: " + uri);
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        switch (from) {
            case FROM_FILE:
                fileData.put(fileName, ans);
                break;
            case FROM_URI:
                uriData.put(uri.toString(), ans);
                break;
            case FROM_ASSETS:
                assetData.put(assetName, ans);
                break;
        }
        return ans;
    }

    void write(String fileName, ArrayList<String> data) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0;i<data.size();i++) {
                stringBuilder.append(data.get(i)).append('\n');
            }
            String strData = stringBuilder.toString();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctx.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(strData);
            outputStreamWriter.close();
            Log.d("write", "write: written?" + ctx.getFileStreamPath(fileName));
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }
    }
}
