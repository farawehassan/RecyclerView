package com.example.hassan.recyclerview;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.hassan.recyclerview.database.DBHelper;
import com.example.hassan.recyclerview.database.DataSource;
import com.example.hassan.recyclerview.model.DataItems;
import com.example.hassan.recyclerview.sample.SampleDataProvider;
import com.example.hassan.recyclerview.utils.JSONHelper;

import java.util.List;

import static com.example.hassan.recyclerview.sample.SampleDataProvider.dataItemList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_WRITE = 1002;
    private static final String TAG = "tag";
    List<DataItems> dataExport = SampleDataProvider.dataItemList;
    private boolean result = JSONHelper.exportToJSON(this, dataExport);
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    SQLiteDatabase database;
    DataSource mDataSource;
    List<DataItems> listFromDB;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    String[] mCategories;
    RecyclerView mRecyclerView;
    DataItemAdapter mItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Code to manage sliding navigation drawer
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mCategories = getResources().getStringArray(R.array.categories);
        mDrawerList = findViewById(R.id.leftDrawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mCategories));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String category = mCategories[position];
                Toast.makeText(MainActivity.this, "You chose " + category, Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(mDrawerList);
                displayDataItems(category);
            }
        });

        //end of the navigation drawer

        mDataSource = new DataSource(this);
        SQLiteOpenHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
//      Toast.makeText(this, "Database acquired!", Toast.LENGTH_SHORT).show();

        mDataSource.seedDatabase(dataItemList);
        //checkPermissions();

        /** Collections.sort(dataItemList, new Comparator<DataItems>() {
            @Override
            public int compare(DataItems o1, DataItems o2) {
                return o1.getItemName().compareTo(o2.getItemName());
            }
        });*/

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i("preferences", "onSharedPreferenceChanged: " + key);
            }
        };
        settings.registerOnSharedPreferenceChangeListener(prefListener);

        boolean grid = settings.getBoolean(getString(R.string.pref_display_grid), false);

        mRecyclerView = findViewById(R.id.rvItems);
        if (grid){
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
        displayDataItems(null);
    }

    private void displayDataItems(String category){
        listFromDB = mDataSource.getAllItems(category);
        mItemAdapter = new DataItemAdapter(this, listFromDB);
        mRecyclerView.setAdapter(mItemAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_signIn:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, PrefsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_export:
                //boolean result = JSONHelper.exportToJSON(this, dataExport);
                if (result){
                    Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_import:
                List<DataItems> dataItems = JSONHelper.importFromJSON(this);
                if (dataItems != null){
                    for (DataItems dataItem:
                            dataItems) {
                        Log.i(TAG, "onOptionsItemSelected: " + dataItem.getItemName());
                    }
                }
                return true;
            case R.id.action_displayAll:
                displayDataItems(null);
                return true;
            case R.id.action_chooseCategory:
                //Opening the drawer
                mDrawerLayout.openDrawer(mDrawerList);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    // Initiate request for permissions.
    private boolean checkPermissions() {

        if (!isExternalStorageReadable() || !isExternalStorageWritable()) {
            Toast.makeText(this, "This app only works on devices with usable external storage",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);
            return false;
        } else {
            return true;
        }
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean permissionGranted = true;
                    Toast.makeText(this, "External storage permission granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
