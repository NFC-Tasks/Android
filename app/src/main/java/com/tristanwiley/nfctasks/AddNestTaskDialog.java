package com.tristanwiley.nfctasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class AddNestTaskDialog extends DialogFragment {

    public static final String ARG_TAG = "argTag";

    public static AddNestTaskDialog NewInstance(String tagName) {
        AddNestTaskDialog dialog = new AddNestTaskDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tagName);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_nest_task, container, false);

        final EditText mTargetValue = (EditText) view.findViewById(R.id.target_value);
        Button saveButton = (Button) view.findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTargetValue.getText().toString().isEmpty()) {
                    NFDataSource dataSource = new NFDataSource(getActivity());
                    dataSource.open();
                    dataSource.insertNestTask(getArguments().getString(ARG_TAG), Long.parseLong(mTargetValue.getText().toString()));
                    dismiss();
                }
            }
        });

        return view;
    }
}
