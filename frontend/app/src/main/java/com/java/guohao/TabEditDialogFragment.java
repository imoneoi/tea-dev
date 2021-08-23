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
import java.util.Collection;
import java.util.Collections;

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
        private ArrayList<String> mOppositeDataset;
        private TitleAdapter mOppositeAdapter;

        public TitleAdapter(ArrayList<String> dataset, ArrayList<String> oppositeDataset) {
            mLocalDataset = dataset;
            mOppositeDataset = oppositeDataset;
        }

        public void setOppositeAdapter(TitleAdapter oppositeAdapter) {
            mOppositeAdapter = oppositeAdapter;
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
            holder.getChip().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Chip c = v.findViewById(R.id.tab_edit_chip);
                    int x = mLocalDataset.indexOf(c.getText().toString());
                    mLocalDataset.remove(x);
                    notifyItemRemoved(x);
                    mOppositeDataset.add(c.getText().toString());
                    mOppositeAdapter.notifyItemInserted(mOppositeDataset.size() - 1);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLocalDataset.size();
        }
    }

    public class TitleCallback extends ItemTouchHelper.SimpleCallback {
        private ArrayList<String> mDataset;
        private TitleAdapter mAdapter;

        TitleCallback(ArrayList<String> dataset, TitleAdapter adapter) {
            super(ItemTouchHelper.UP | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT, 0);
            mDataset = dataset;
            mAdapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            if (from < to) {
                for (int i = from; i < to; ++i) {
                    Collections.swap(mDataset, i, i + 1);
                }
            } else {
                for (int i = from; i > to; --i) {
                    Collections.swap(mDataset, i, i - 1);
                }
            }
            mAdapter.notifyItemMoved(from, to);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
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
        TitleAdapter inAdapter = new TitleAdapter(mInTitles, mOutTitles);
        TitleAdapter outAdapter = new TitleAdapter(mOutTitles, mInTitles);
        inAdapter.setOppositeAdapter(outAdapter);
        outAdapter.setOppositeAdapter(inAdapter);
        mInView.setAdapter(inAdapter);
        mOutView.setAdapter(outAdapter);
        (new ItemTouchHelper(new TitleCallback(mInTitles, inAdapter))).attachToRecyclerView(mInView);
        (new ItemTouchHelper(new TitleCallback(mOutTitles, outAdapter))).attachToRecyclerView(mOutView);

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
        mInTitles.addAll(mParent.getInitialTitles());
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