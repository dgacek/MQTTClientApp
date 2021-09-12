package pl.polsl.mqttclientapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;
import java.util.List;

public class ConnectionSetupDialogFragment extends DialogFragment {

    public interface SetupDialogListener {
        public void onDialogPositiveClick(ConnectionSetupDialogFragment dialog);
        public void onDialogNegativeClick(ConnectionSetupDialogFragment dialog);
    }

    private SetupDialogListener listener;
    private String brokerUri;
    private List<String> topics;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SetupDialogListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(getActivity().toString() + " must implement SetupDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.connection_setup_dialog, null);

        builder.setView(dialogView)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        brokerUri = dialogView.findViewById(R.id.txtinBrokerUri).toString();
                        topics = Arrays.asList(dialogView.findViewById(R.id.txtinmlTopics).toString().split("\n"));
                        listener.onDialogPositiveClick(ConnectionSetupDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(ConnectionSetupDialogFragment.this);
                    }
                });
        return builder.create();
    }

    public String getBrokerUri() {
        return brokerUri;
    }

    public List<String> getTopics() {
        return topics;
    }
}
