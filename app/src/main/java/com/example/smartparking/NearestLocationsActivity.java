package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.smartparking.MyJsonLibrary.MyJsonArrayRequest;
import com.example.smartparking.NearestAdapter.RecyclerViewAdapter;
import com.example.smartparking.NearestAdapter.SlotItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearestLocationsActivity extends AppCompatActivity {
    private static final String TAG = "NearestLocationsActivit";
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private double latitude;
    private double longitude;
    private long location_id;
    private int vehicle_type;
    private SlotItem slotItem;


    public ArrayList<SlotItem> slotItemArrayList;
    public ArrayList<SlotItem> currentList;
    public ArrayList<SlotItem> newList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_locations);
        Intent intent = getIntent();

        // get location and vehicle info from previous activity
        latitude = intent.getDoubleExtra("latitude", 0.00);
        longitude = intent.getDoubleExtra("longitude", 0.00);
        location_id = intent.getLongExtra("locality", 1);
        vehicle_type = intent.getIntExtra("vehicle", 1);

        //set vehicle
        String[] vehicles = {"Bike", "Car/Auto", "Lorry/Heavy"};
        String vehicle_class = vehicles[vehicle_type];

        //display toast message to show data from previous activity
        String msg = "latitude: " + latitude + ", longitude: " + longitude;
        Log.d("LocationTest", msg);
        String msg1 = "Location id: " + location_id + ", Vehicle type: " + vehicle_class;
        Log.d("LocationTest", msg1);
        Toast.makeText(this, msg + " " + msg1, Toast.LENGTH_LONG).show();


        // Initialize recyclerview
        recyclerView = findViewById(R.id.hist_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getNearestSlots(latitude, longitude, location_id, vehicle_type);
        // Log.d("currentList", Integer.toString(currentList.size()));
        // for(int i=0; i<currentList.size(); i++) {
        // Log.d("currentList", currentList.get(i).toString());
        // }
    }

    public void getNearestSlots(double latitude, double longitude, long location_id, int vehicle_type) {
        // get data from API
        // create request queue, set url, and create json request
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://iot-sp.herokuapp.com/get-nearest-slots";
        JSONObject myRequest = new JSONObject();
        try {
            myRequest.put("latitude", latitude);
            myRequest.put("longitude", longitude);
            myRequest.put("locality", location_id);
            myRequest.put("vehicle", vehicle_type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("getNearestSlots: " , myRequest.toString());

        currentList = new ArrayList<>();
        //slotItemArrayList.add(new SlotItem(1 , 1, "Hi\nHello\nHow are you\nOk"));

        MyJsonArrayRequest myJsonArrayRequest =
                new MyJsonArrayRequest(Request.Method.POST, url, myRequest,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject jsonObject = response.getJSONObject(i);
                                        //Log.d("myApp", "The response is: " + jsonObject.toString());
                                        slotItem = SlotItem.fromJSON(jsonObject, vehicle_type);
                                        currentList.add(slotItem);
                                    }
                                    populateRecyclerView();
                                    startBackgroundTask();
                                } catch (JSONException e) {
                                    //Log.d("myApp", "There is a JSONException: " + e.toString());
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("myApp", "There is a VolleyError: " + error.toString());
                            }
                        }
                );

        queue.add(myJsonArrayRequest);
    }

    public void populateRecyclerView() {
        recyclerViewAdapter = new RecyclerViewAdapter(this,
                currentList);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public void startBackgroundTask() {
        // boolean stopThread = false;
        // newList = new ArrayList<>();
        BackgroundRunnable runnable = new BackgroundRunnable();
        new Thread(runnable).start();
    }

    public class BackgroundRunnable implements Runnable {
        private static final String TAG = "BackgroundRunnable";
        public boolean looping = true;
        public Handler handler;

        BackgroundRunnable() {
            handler = new Handler();
        }

        public void printList(ArrayList<SlotItem> myList) {
            for(int i=0; i<myList.size(); i++) {
                Log.d(TAG, myList.get(i).toString());
            }
        }

        @Override
        public void run() {
            handler.postDelayed(new Runnable() {

                private Runnable thisRunnable = this;

                @Override
                public void run() {
                    getNearestSlotsInBackground(latitude, longitude, location_id, vehicle_type);
                    // SystemClock.sleep(5000);
                }

                public void getNearestSlotsInBackground(double latitude, double longitude, long location_id, int vehicle_type) {
                    // get data from API
                    // create request queue, set url, and create json request
                    RequestQueue queue = Volley.newRequestQueue(NearestLocationsActivity.this);
                    String url = "https://iot-sp.herokuapp.com/get-nearest-slots";
                    JSONObject myRequest = new JSONObject();
                    try {
                        myRequest.put("latitude", latitude);
                        myRequest.put("longitude", longitude);
                        myRequest.put("locality", location_id);
                        myRequest.put("vehicle", vehicle_type);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("getNearestSlots: " , myRequest.toString());

                    // Log.d(TAG, "getNearestSlotsInBackground: " + myRequest.toString());
                    MyJsonArrayRequest myJsonArrayRequest =
                        new MyJsonArrayRequest(Request.Method.POST, url, myRequest,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    newList = new ArrayList<>();
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject jsonObject = response.getJSONObject(i);
                                            slotItem = SlotItem.fromJSON(jsonObject, vehicle_type);
                                            newList.add(slotItem);
                                        }
                                        Log.d(TAG, "new list: ");
                                        printList(newList);
                                        recyclerViewAdapter.updateSlots(newList);
                                        handler.postDelayed(thisRunnable, 5000);

                                    } catch (JSONException e) {
                                        Log.d("myApp", "There is a JSONException/Interrupted exception" +
                                                ": " + e.toString());
                                        e.printStackTrace();
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("myApp", "There is a VolleyError: " + error.toString());
                                }
                            }
                        );

                    queue.add(myJsonArrayRequest);
                }
            }, 10000);
        }

    }
}