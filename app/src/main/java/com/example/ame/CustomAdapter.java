package com.example.ame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomAdapter<E> extends ArrayAdapter<E> {

    private static class ViewHolder {
        TextView word;
    }


    Context ctx;
    MainActivity mainActivity;
    ActivityResultLauncher<String> mGetContent;


    public CustomAdapter(ArrayList<E> data, Context context, MainActivity mainActivity) {
        super(context, R.layout.list_item, data);
        this.ctx = context;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        E val = getItem(position);
        ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(ctx).inflate(R.layout.list_item, parent, false);
            viewHolder.word = convertView.findViewById(R.id.text);
            viewHolder.word.setOnClickListener(view -> mainActivity.accept(((ViewHolder) view.getTag()).word.getText().toString()));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.word.setText(val.toString());
        return convertView;
    }

}