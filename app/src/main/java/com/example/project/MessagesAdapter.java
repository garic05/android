package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public class MessagesAdapter extends ArrayAdapter {


    public MessagesAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
    }

//    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Message message = (Message) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message, null);
        }

        ((TextView) convertView.findViewById(R.id.msg_IP)).setText(message.IP);
        ((TextView) convertView.findViewById(R.id.msg_mes)).setText(message.message);

        return convertView;
    }
}
