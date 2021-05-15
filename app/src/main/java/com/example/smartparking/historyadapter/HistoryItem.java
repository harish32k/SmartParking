package com.example.smartparking.historyadapter;

import com.example.smartparking.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.smartparking.NearestAdapter.SlotItem.vehicles;

public class HistoryItem {
    private long slotId;
    private String slotDetail;
    private int vehicle_type;
    private int slotImg;
    private int available;
    private String address;

    public HistoryItem(long slotId, String slotDetail, int vehicle_type, int available, String address) {
        this.slotId = slotId;
        this.slotDetail = slotDetail;
        this.vehicle_type = vehicle_type;
        this.available = available;
        this.address = address;
        setSlotImg();
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

    public int getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(int vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public int getSlotImg() {
        return slotImg;
    }

    public void setSlotImg() {
        if(available == 1) {
            slotImg = R.drawable.available;
        } else {
            slotImg = R.drawable.occupied;
        }
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
        setSlotImg();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static HistoryItem fromJSON(JSONObject jsonObject) {
        long slotId = 0;
        String slotDetail = null;
        int vehicle_type = 0;
        int available = 0;
        String address = null;
        String dname;
        String locality;
        long pincode;
        String zone;


        try {
            slotId = jsonObject.getLong("park_id");
            slotDetail = jsonObject.getString("detail");
            vehicle_type = jsonObject.getInt("vehicle_type");
            available = jsonObject.getInt("available");
            dname = jsonObject.getString("dname");
            locality = jsonObject.getString("locality");
            pincode = jsonObject.getLong("pincode");
            zone = jsonObject.getString("zone");
            address = "District: " + dname + ", Zone: " + zone +
                    ", Pincode: " + Long.toString(pincode) + ", Locality: " +
                    locality;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new HistoryItem(slotId, slotDetail, vehicle_type, available, address);
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "slotId=" + '\'' + slotId + '\'' +
                '}';
    }
}
