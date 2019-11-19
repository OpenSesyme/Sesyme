package com.sesyme.sesyme.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDialogFragment;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.data.Methods;

import org.jetbrains.annotations.NotNull;

public class AttachmentDialog extends AppCompatDialogFragment {

    private attachmentDialogListener listener;
    private Methods methods;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //noinspection ConstantConditions
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.attachment_dialog, null);

        builder.setView(dialogView);
        LinearLayout image = dialogView.findViewById(R.id.image_attach);
        LinearLayout pdf = dialogView.findViewById(R.id.pdf_attach);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnItemClicked(R.id.image_attach);
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnItemClicked(R.id.pdf_attach);
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (attachmentDialogListener) context;
            methods = new Methods(context);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int dialogWidth;
            int dialogHeight;
            if (methods == null) {
                dialogWidth = 600;
                dialogHeight = 500;
            }else {
                dialogWidth = methods.dpToPx(280);
                dialogHeight = methods.dpToPx(165);
            }

            getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    public interface attachmentDialogListener{
        void OnItemClicked(int itemId);
    }
}
