package com.ogangi.demo.walletapp;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;



/*
 * Created by betza on 09/08/17.
 */

public class UrlDialogFragment extends DialogFragment {
    ClipboardManager clipboard;

    @Nullable
    NoticeDialogListener listener;

    public UrlDialogFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_message, null);

        clipboard = (ClipboardManager) getContext().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);

        final TextInputEditText mUrlView = view.findViewById(R.id.urlText);
        mUrlView.setError(null);

        final Button pasteBtn =  view.findViewById(R.id.paste);

        pasteBtn.setOnClickListener(view1 -> {

            // Examines the item on the clipboard. If getText() does not return null, the clip item contains the
            // text. Assumes that this application can only handle one item at a time.
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

            // Gets the clipboard as text.
            String pasteData = item.getText().toString();

            // If the string contains data, then the paste operation is done
            if (pasteData.length() > 0) {
                mUrlView.setText(pasteData);
            } else {
                Uri pasteUri = item.getUri();
                // If the URI contains something, try to get text from it
                if (pasteUri != null) {
                    mUrlView.setText(pasteUri.toString());
                } else {
                    // Something is wrong. The MIME type was plain text, but the clipboard does not contain either
                    // text or a Uri. Report an error.
                    Toast.makeText(getContext(), "Clipboard contains an invalid data type", Toast.LENGTH_LONG).show();
                    Log.e("PASTE_ERROR", "Clipboard contains an invalid data type");
                }
            }
        });

        if (!(clipboard.hasPrimaryClip())) {
            pasteBtn.setEnabled(false);
        }else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))){
            // This disables the paste menu item, since the clipboard has data but it is not plain text
            pasteBtn.setEnabled(false);
        }else{
            // This enables the paste menu item, since the clipboard contains plain text.
            pasteBtn.setEnabled(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("URL");
        builder.setView(view);
        builder.setPositiveButton("Accept", (dialog, which) -> {
            String url = mUrlView.getText().toString();
            if(URLUtil.isValidUrl(url)){
                // Send the positive button event back to the host activity
                if (listener != null) {
                     listener.onDialogPositiveClick(UrlDialogFragment.this, url);
                }
            } else {
                // Write your code here to execute after dialog
                Toast.makeText(getActivity().getApplicationContext(),
                    "Url invalid!", Toast.LENGTH_SHORT)
                    .show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (listener != null) {
                listener.onDialogNegativeClick(UrlDialogFragment.this);
            }
        });
        return builder.create();
    }

    /***
     * The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     **/
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(UrlDialogFragment dialog, String url);
        public void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog);
    }

    //En este metodo es donde se asigna el listener, el onAttach se llama cuando aparece el bottomSheet
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            listener = (NoticeDialogListener) parent;
        } else {
            listener = (NoticeDialogListener) context;
        }
    }

    //El onDetach se llama cuando desaparece el bottomSheet
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

 }
