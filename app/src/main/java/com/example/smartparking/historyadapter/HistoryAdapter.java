package com.example.smartparking.historyadapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparking.NearestAdapter.SlotItem;
import com.example.smartparking.R;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context context;
    private ArrayList<HistoryItem> historyList;
    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position, View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public HistoryAdapter(Context context, ArrayList<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @NotNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_history, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HistoryAdapter.ViewHolder holder, int position) {

        HistoryItem historyItem = historyList.get(position);
        holder.hist_slotView.setText("Slot: " + Long.toString(historyItem.getSlotId()));
        holder.hist_addrView.setText("Address: " + historyItem.getAddress());
        holder.hist_detailView.setText(historyItem.getSlotDetail());
        holder.hist_imageView.setImageDrawable(context.getResources().getDrawable(historyItem.getSlotImg()));
        holder.hist_vehicleView.setText(SlotItem.vehicles[historyItem.getVehicle_type()] + " parking");
        holder.removeButton.setEnabled(true);
        holder.removeButton.setTextColor(context.getResources().getColor(R.color.white));
        holder.removeButton.setBackgroundColor(context.getResources().getColor(R.color.OrangeRed));
        holder.removeButton.setText("Remove");

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView hist_slotView;
        public TextView hist_addrView;
        public TextView hist_detailView;
        public TextView hist_vehicleView;
        public ImageView hist_imageView;
        public Button removeButton;

        public ViewHolder(@NonNull @NotNull View itemView, OnItemClickListener listener) {
            super(itemView);

            // itemView.setOnClickListener();
            hist_slotView = itemView.findViewById(R.id.hist_slotView);
            hist_addrView = itemView.findViewById(R.id.hist_addrView);
            hist_detailView = itemView.findViewById(R.id.hist_detailView);
            hist_vehicleView = itemView.findViewById(R.id.hist_vehicleView);
            hist_imageView = itemView.findViewById(R.id.hist_imageView);
            removeButton = itemView.findViewById(R.id.removeButton);

            // removeButton.setOnClickListener(new RemoveHandler(this));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAbsoluteAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAbsoluteAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position, itemView);
                        }
                    }
                }
            });

        }

    }

}
