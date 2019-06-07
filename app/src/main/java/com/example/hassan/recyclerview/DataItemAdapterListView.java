package com.example.hassan.recyclerview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hassan.recyclerview.model.DataItems;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DataItemAdapterListView extends ArrayAdapter<DataItems>{
    List<DataItems> mDataItems;
    LayoutInflater mInflater;

    public DataItemAdapterListView(@NonNull Context context, @NonNull List<DataItems> objects) {
        super(context, R.layout.list_item, objects);

        mDataItems = objects;
        mInflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
        }

        DataItems item = mDataItems.get(position);
        TextView tvName = convertView.findViewById(R.id.itemNameText);
        ImageView imageView = convertView.findViewById(R.id.imageView);

        tvName.setText(item.getItemName());
        //imageView.setImageResource(R.drawable.apple_pie);

        InputStream inputStream = null;
        try {
            String imageFile = item.getImage();
            inputStream = getContext().getAssets().open(imageFile);
            Drawable d = Drawable.createFromStream(inputStream, null);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return convertView;

    }
}
