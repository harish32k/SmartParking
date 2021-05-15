package com.example.smartparking.apputil;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.smartparking.NearestAdapter.SlotItem;

import java.util.ArrayList;

public class MyDiffUtilCallback extends DiffUtil.Callback {

    ArrayList<SlotItem> oldList = new ArrayList<>();
    ArrayList<SlotItem> newList = new ArrayList<>();

    public MyDiffUtilCallback(ArrayList<SlotItem> oldList, ArrayList<SlotItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return true;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        int result = oldList.get(oldItemPosition).compareTo(newList.get(newItemPosition));
        if(result == 0) return true;
        return false;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        SlotItem oldItem = oldList.get(oldItemPosition);
        SlotItem newItem = newList.get(newItemPosition);

        Bundle bundle = new Bundle();

        if(oldItem.getSlotId() != newItem.getSlotId()) {
            bundle.putLong("slotId", newItem.getSlotId());
            bundle.putInt("vehicle_type", newItem.getVehicle_type());
            bundle.putString("detail", newItem.getSlotDetail());
        }

        if(bundle.size() == 0) { return null; }
        return bundle;
    }
}
