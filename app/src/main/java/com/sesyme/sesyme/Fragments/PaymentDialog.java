package com.sesyme.sesyme.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.sesyme.sesyme.R;

public class PaymentDialog extends AppCompatDialogFragment {

    private paymentDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //noinspection ConstantConditions
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_payment, null);

        final RadioGroup rgPayment = dialogView.findViewById(R.id.rg_payment);
        Button now = dialogView.findViewById(R.id.bt_pay_now);

        now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String version = onRadioButtonClicked(dialogView, rgPayment);
                listener.OnButtonClicked(version);
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (paymentDialogListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int dialogWidth = 600;
            int dialogHeight = 1000;

            getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    public void setOnClickListener(paymentDialogListener listener){
        this.listener = listener;
    }

    public interface paymentDialogListener{
        void OnButtonClicked(String subscription);
    }

    private String onRadioButtonClicked(View root, RadioGroup view) {
        String choice = null;
        // Is the button now checked?
        RadioButton button = root.findViewById(view.getCheckedRadioButtonId());
        boolean checked;
        if (button != null) {
            checked = button.isChecked();
        }else {
            checked = false;
        }
        // Check which radio button was clicked
        if (button != null) {
            switch (button.getId()) {
                case R.id.rb_free_trial:
                    if (checked)
                        choice = "Free";
                    break;
                case R.id.rb_premium:
                    if (checked)
                        choice = "Paid";
                    break;
            }
        }
        return choice;
    }
}
