package com.example.hassan.recyclerview.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.hassan.recyclerview.model.DataItems;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class JSONHelper {

    private static final String FILE_NAME = "menuitems.json";
    public static final String TAG = "JSONHelper";

    public static boolean exportToJSON(Context context, List<DataItems> dataItemList){
        DataItem menuData = new DataItem();
        menuData.setDataItems(dataItemList);

        Gson gson = new Gson();
        String jsonString = gson.toJson(menuData);
        Log.i(TAG, "exportToJSON: " + jsonString);

        FileOutputStream fileOutputStream = null;
        File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static List<DataItems> importFromJSON(Context context){
        FileReader reader = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            reader = new FileReader(file);

            Gson gson = new Gson();
            DataItem dataItems = gson.fromJson(reader, DataItem.class);
            return dataItems.getDataItems();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static class DataItem{
        List<DataItems> dataItems;

        public List<DataItems> getDataItems() { return dataItems; }

        public void setDataItems(List<DataItems> dataItems) { this.dataItems = dataItems; }
    }

}
