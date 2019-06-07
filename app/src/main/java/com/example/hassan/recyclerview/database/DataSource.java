package com.example.hassan.recyclerview.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hassan.recyclerview.model.DataItems;

import java.util.ArrayList;
import java.util.List;


public class DataSource {
    private SQLiteDatabase mDatabase;
    private SQLiteOpenHelper mDbHelper;

    public DataSource(Context mContext) {
        mDbHelper = new DBHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void open(){mDatabase = mDbHelper.getWritableDatabase(); }

    public void close(){mDbHelper.close();}

    public DataItems createItem(DataItems items){
        ContentValues values = items.toValues();
        mDatabase.insert(ItemsTable.TABLE_ITEMS, null, values);
        return items;
    }

    public long getDataItemsCount(){
        return DatabaseUtils.queryNumEntries(mDatabase, ItemsTable.TABLE_ITEMS);
    }

    public void seedDatabase(List<DataItems> dataItemList) {
        // long numItems = getDataItemsCount();
        // if (numItems == 0){
        try {
            mDatabase.beginTransaction();
            for (DataItems items :
                    dataItemList) {
                try {
                    createItem(items);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // }
    }

    public List<DataItems> getAllItems(String category){
        List<DataItems> dataItems = new ArrayList<>();
        Cursor cursor = null;
        if (category == null){
            cursor = mDatabase.query(ItemsTable.TABLE_ITEMS, ItemsTable.ALL_COLUMNS,
                    null, null, null, null, ItemsTable.COLUMN_NAME);
        }else {
            String[] categories = {category};
            cursor = mDatabase.query(ItemsTable.TABLE_ITEMS, ItemsTable.ALL_COLUMNS,
                    ItemsTable.COLUMN_CATEGORY + "=?",  categories, null, null, ItemsTable.COLUMN_NAME);
        }

        while (cursor.moveToNext()){
            DataItems items = new DataItems();
            items.setItemId(cursor.getString(
                    cursor.getColumnIndex(ItemsTable.COLUMN_ID)));
            items.setItemName(cursor.getString(
                    cursor.getColumnIndex(ItemsTable.COLUMN_NAME)));
            items.setDescription(cursor.getString(
                    cursor.getColumnIndex(ItemsTable.COLUMN_DESCRIPTION)));
            items.setCategory(cursor.getString(
                    cursor.getColumnIndex(ItemsTable.COLUMN_CATEGORY)));
            items.setSortPosition(cursor.getInt(
                    cursor.getColumnIndex(ItemsTable.COLUMN_POSITION)));
            items.setPrice(cursor.getDouble(
                    cursor.getColumnIndex(ItemsTable.COLUMN_PRICE)));
            items.setImage(cursor.getString(
                    cursor.getColumnIndex(ItemsTable.COLUMN_IMAGE)));
            dataItems.add(items);
        }
        cursor.close();
        return dataItems;
    }
}
