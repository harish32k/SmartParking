package com.example.smartparking.NearestAdapter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.smartparking.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SlotItem implements Comparable{
    long slotId;
    String slotDetail;
    int vehicle_type;
    int slotImg;
    int available;

    public static final String[] vehicles = {"Bike", "Car/Auto", "Lorry/Heavy"};

    public SlotItem(long slotId, int vehicle_type, String slotDetail) {
        this.slotId = slotId;
        this.vehicle_type = vehicle_type;
        this.slotDetail = slotDetail;
        this.available = 1;
        this.slotImg = R.drawable.available;
    }


    public String getVehicle() {
        return vehicles[vehicle_type];
    }


    public long getSlotId() {
        return slotId;
    }

    public void setSlotId(long slotId) {
        this.slotId = slotId;
    }

    public String getSlotDetail() {
        return slotDetail;
    }

    public void setSlotDetail(String slotDetail) {
        this.slotDetail = slotDetail;
    }

    public int getSlotImg() {
        return slotImg;
    }

    public void setSlotImg(int slotImg) {
        this.slotImg = slotImg;
    }

    public int getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(int vehicle_type) {
        this.vehicle_type = vehicle_type;
    }


    public static SlotItem fromJSON(JSONObject jsonObject, int vehicle_type) {
        String detail = null;
        long park_id = 0;
        try {
            detail = jsonObject.getString("detail");
            park_id = jsonObject.getLong("park_id");
            SlotItem slotItem = new SlotItem(park_id, vehicle_type, detail);
            // Log.d("myApp1", slotItem.toString());
            return slotItem;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new SlotItem(park_id, vehicle_type, detail);
    }

    @Override
    public String toString() {
        return "SlotItem{" +
                "slotId=" + slotId +
                ", slotDetail='" + slotDetail + '\'' +
                ", vehicle_type=" + vehicle_type +
                ", slotImg=" + slotImg +
                ", available=" + available +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        SlotItem slotItem = (SlotItem) o;
        if(slotItem.getSlotId() == this.getSlotId()) { return 0; } return 1;
    }
}
