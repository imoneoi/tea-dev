package com.java.guohao;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TabEditDialogFragment extends DialogFragment {

    private ArrayList<String> mInTitles;
    private ArrayList<String> mOutTitles;
    private RecyclerView mInView;
    private RecyclerView mOutView;

    MainFragment mParent;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Chip mChip;

        public ViewHolder(View view) {
            super(view);
            mChip = (Chip) view.findViewById(R.id.tab_edit_chip);
        }

        Chip getChip() {
            return mChip;
        }
    }

    public class TitleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<String> mLocalDataset;

        public TitleAdapter(ArrayList<String> dataset) {
            mLocalDataset = dataset;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chip_tab_edit_dialog, parent, false);
            ViewHolder h = new ViewHolder(view);
            return h;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.getChip().setText(mLocalDataset.get(position));
        }

        @Override
        public int getItemCount() {
            return mLocalDataset.size();
        }
    }


    public TabEditDialogFragment(MainFragment parent) {
        mParent = parent;
        mInTitles = new ArrayList<>();
        mOutTitles = new ArrayList<>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_tab_edit_dialog, null);
        mInView = v.findViewById(R.id.tab_edit_in_view);
        mOutView = v.findViewById(R.id.tab_edit_out_view);

        mInView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        mOutView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        mInView.setAdapter(new TitleAdapter(mInTitles));
        mOutView.setAdapter(new TitleAdapter(mOutTitles));

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mParent.onDialogPositiveClick(TabEditDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        mInTitles.clear();
        mOutTitles.clear();
        for (int i = 0; i < mParent.getInitialTitles().size(); ++i) {
            mInTitles.add(mParent.getInitialTitles().get(i));
        }
        for (int i = 0; i < GlobVar.SUBJECTS.length; ++i) {
            if (!mParent.getInitialTitles().contains(GlobVar.SUBJECTS[i])) {
                mOutTitles.add(GlobVar.SUBJECTS[i]);
            }
        }
    }

    public ArrayList<String> getInTitles() {
        return mInTitles;
    }
}