package com.absathe.byway;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private Activity activity;
    private CommunicateCoRidersWithMapsInterface mapsInterface;
    public Coriders() {
        // Required empty public constructor
    }
    private Context context;
    private List<CoRiderItem> items = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        // Inflate the layout for this fragment
        View ret = localInflater.inflate(R.layout.fragment_coriders, container, false);
        System.out.println("I am inside onCreateView");
        activity = getActivity();
        RecyclerView recyclerView = ret.findViewById(R.id.coriders_fragment_recyclerview);
        Bundle args = getArguments();
        int mode = args.getInt("MODE", 0);
        if(mode != 0) {
            CoRiderItem example = new CoRiderItem();
            example.setName("THIS IS A PERSON THAT WANTS A RIDE");
            example.setUserId("aPCx42WG3xd3u1vvJj3GvpNd8YV2"); // Sarthak
            items.add(example);
        }
        else {
            CoRiderItem example = new CoRiderItem();
            example.setName("THIS IS A PERSON THAT WANTS TO SHARE RIDE");
            example.setUserId("ntqTwqiPfIgQqMKDpD7ZMNah0vI3"); // Onway
            items.add(example);
        }
        ItemAdapter itemAdapter = new ItemAdapter();
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.withEventHook(new ClickEventHook<CoRiderItem>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if(viewHolder instanceof CoRiderItem.ViewHolder) {
                    System.out.println("I'm returning ViewHolder.itemView");
                    return ((CoRiderItem.ViewHolder)viewHolder).ride;
                }
                return null;
            }
            @Override
            public void onClick(View v, int position, FastAdapter<CoRiderItem> fastAdapter, CoRiderItem item) {
                System.out.println(item.name);
                try {
                    ((CommunicateCoRidersWithMapsInterface)activity).coRiderPicked(item.userId);
                }
                catch(ClassCastException cce) {
                    Log.d("CoRiderFragment", cce.getStackTrace().toString());
                }
            }

        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

    public interface CommunicateCoRidersWithMapsInterface {
        void coRiderPicked(String userID);
    }
}
