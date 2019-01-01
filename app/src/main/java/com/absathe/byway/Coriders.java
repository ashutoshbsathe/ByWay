package com.absathe.byway;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Coriders extends BottomSheetDialogFragment {

    public static Coriders newInstance(int mode) {
        /*
         * Mode = 0 means SHARE mode
         */
        Bundle args = new Bundle();
        args.putInt("MODE", mode);
        Coriders fragment = new Coriders();
        fragment.setArguments(args);
        return fragment;
    }

    public Coriders() {
        // Required empty public constructor
    }

    private List<CoRiderItem> items = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_coriders, container, false);
        RecyclerView recyclerView = ret.findViewById(R.id.coriders_fragment_recyclerview);
        Bundle args = getArguments();
        int mode = args.getInt("MODE", 0);
        if(mode != 0) {
            CoRiderItem example = new CoRiderItem();
            example.setName("THIS IS A PERSON THAt WANTS A RIDE");
            items.add(example);
        }
        else {
            CoRiderItem example = new CoRiderItem();
            example.setName("THIS IS A PERSON THAt WANTS TO SHARE RIDE");
            items.add(example);
        }
        ItemAdapter itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);
        itemAdapter.add(items);

        return ret;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // Do something with your dialog like setContentView() or whatever
        return dialog;
    }

}
