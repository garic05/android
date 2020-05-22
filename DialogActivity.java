package com.example.project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogActivity extends DialogFragment {
    public static final String FIND_TAG = "FIND";
    public static final String REC_TAG = "REC";
    public static final String ERROR_TAG = "ERROR";
    public static final String LOG_TAG = "MY_TAG";
    public static final String CODE_TAG = "CODE";
    public static final String DEVICE_TAG = "DEVICE_TAG";

    public String MY_TAG;
    EditText editText;
    Intent intent;
    Client client;
    private final String lang;

    public DialogActivity(String lang) {
        this.lang = lang;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        editText = new EditText(getContext());
        intent = new Intent(getContext(), SeansActivity.class);
        MY_TAG = getTag();
        switch (MY_TAG){
            case REC_TAG:
                return  builder
                        .setTitle("join to session")
                        .setMessage("enter code to join to session")
                        .setView(editText)
                        .setPositiveButton("OK", listener_positive)
                        .setNegativeButton("Cancel", null)
                        .create();
            case FIND_TAG:
                return  builder
                        .setTitle("create session(server)")
                        .setMessage("create code to join to session")
                        .setView(editText)
                        .setPositiveButton("OK", listener_positive)
                        .setNegativeButton("Cancel", null)
                        .create();
            case ERROR_TAG:
                return  builder
                        .setTitle("ERROR")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("give out or join to wifi and check the Internet connection")
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", null)
                        .create();
        }
        return null;
    }

    DialogInterface.OnClickListener listener_positive = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (Server.getAddress() != null && Server.hasConnection(getContext())) {
                intent.putExtra(CODE_TAG, editText.getText().toString());
                intent.putExtra(DEVICE_TAG, MY_TAG);
                intent.putExtra("lang", lang);
                startActivity(intent);
            } else
                new DialogActivity(lang).show(getFragmentManager(), ERROR_TAG);
        }
    };

}
