package com.example.eruvis.carnotebook.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;

public class SpinnerFragment extends DialogFragment {

    private static final String TITLEID = "titleId";
    private static final String LISTID = "listId";
    private static final String LISTARRAY = "listArray";
    private static final String EDITTEXTID = "editTextId";

    public static SpinnerFragment newInstance(int titleId, int listId, ArrayList<String> list, int editTextId) {
        Bundle bundle = new Bundle();
        bundle.putInt(TITLEID, titleId);
        bundle.putInt(LISTID, listId);
        bundle.putStringArrayList(LISTARRAY, list);
        bundle.putInt(EDITTEXTID, editTextId);
        SpinnerFragment spinnerFragment = new SpinnerFragment();
        spinnerFragment.setArguments(bundle);

        return spinnerFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int titleId = getArguments().getInt(TITLEID);
        final int listId = getArguments().getInt(LISTID);
        final ArrayList<String> listArray = getArguments().getStringArrayList(LISTARRAY);
        final int editTextId = getArguments().getInt(EDITTEXTID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        try {
            if (listId != 0) {
                final String[] items = getResources().getStringArray(listId);

                builder.setTitle(titleId)
                        .setItems(listId, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int pos) {
                                EditText et = (EditText) getActivity().findViewById(editTextId);
                                String selectedText = items[pos];
                                if (!TextUtils.isEmpty(selectedText)) {
                                    et.setText(selectedText);
                                } else {
                                    et.getText().clear();
                                }
                            }
                        });
            } else {
                final ArrayList<String> arrayItems = listArray;

                builder.setTitle(titleId)
                        .setItems(arrayItems.toArray(new String[arrayItems.size()]), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int pos) {
                                EditText et = (EditText) getActivity().findViewById(editTextId);
                                String selectedText = arrayItems.get(pos);
                                if (!TextUtils.isEmpty(selectedText)) {
                                    et.setText(selectedText);
                                } else {
                                    et.getText().clear();
                                }
                            }
                        });
            }

        } catch (NullPointerException e) {
            Log.e(getClass().toString(), "Failed to select option in " + getActivity().toString() + " as there are no references for passed in resource Ids in Bundle", e);
        }

        return builder.create();
    }
}