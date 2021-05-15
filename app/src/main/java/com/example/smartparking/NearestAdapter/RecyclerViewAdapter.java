package com.example.smartparking.NearestAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparking.R;
import com.example.smartparking.ShowSlotActivity;
import com.example.smartparking.apputil.MyDiffUtilCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<SlotItem> slotItemList;

    public RecyclerViewAdapter(Context context, ArrayList<SlotItem> slotItemList) {
        this.context = context;
        this.slotItemList = slotItemList;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_nearest, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        SlotItem slotItem = slotItemList.get(position);
        holder.sid.setText("Slot: " + slotItem.getSlotId());
        holder.vehicle.setText(slotItem.getVehicle()+ " parking");
        holder.detail.setText(slotItem.getSlotDetail());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerViewAdapter.ViewHolder holder, int position, @NonNull @NotNull List<Object> payloads) {
        if(payloads.isEmpty())
        super.onBindViewHolder(holder, position, payloads);
        else {
            Bundle bundle = (Bundle) payloads.get(0);
            for (String key: bundle.keySet()) {

                holder.sid.setText("Slot: " + bundle.getLong("slotId"));
                holder.vehicle.setText(SlotItem.vehicles[bundle.getInt("vehicle_type")]+ " parking");
                holder.detail.setText(bundle.getString("detail"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return slotItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View
            .OnClickListener{
        public TextView sid;
        public TextView vehicle;
        public TextView detail;
        public ImageView img;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            sid = itemView.findViewById(R.id.hist_slotView);
            vehicle = itemView.findViewById(R.id.hist_vehicleView);
            detail = itemView.findViewById(R.id.hist_detailView);
            img = itemView.findViewById(R.id.hist_imageView);
        }

        @Override
        public void onClick(View v) {
            int position = this.getAbsoluteAdapterPosition();
            SlotItem slotItem = slotItemList.get(position);
            Intent intent = new Intent(context, ShowSlotActivity.class);
            intent.putExtra("park_id", slotItem.getSlotId());
            context.startActivity(intent);

            Log.d("ClickFromViewHolder", "Clicked");
        }
    }

    public void updateSlots(ArrayList<SlotItem> newSlots) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffUtilCallback(slotItemList, newSlots));
        diffResult.dispatchUpdatesTo(this);
        slotItemList.clear();
        slotItemList.addAll(newSlots);
    }
}
