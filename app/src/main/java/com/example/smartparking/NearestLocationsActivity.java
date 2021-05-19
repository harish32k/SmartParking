package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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
    private ProgressBar progressBar;

    private double latitude;
    private double longitude;
    private long location_id;
    private int vehicle_type;
    private SlotItem slotItem;

    public ArrayList<SlotItem> slotItemArrayList;
    public ArrayList<SlotItem> currentList;
    public ArrayList<SlotItem> newList;


    private volatile boolean threadRunning = true;
    private volatile boolean shouldExecuteOnResume;
    private BackgroundRunnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_locations);
        Intent intent = getIntent();
        shouldExecuteOnResume = false;


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
        // Log.d("LocationTest", msg);
        String msg1 = "Location id: " + location_id + ", Vehicle type: " + vehicle_class;
        // Log.d("LocationTest", msg1);
        Toast.makeText(this, msg + " " + msg1, Toast.LENGTH_LONG).show();


        // Initialize recyclerview
        recyclerView = findViewById(R.id.hist_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.nearestProgressBar);
        progressBar.setVisibility(View.VISIBLE);

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

        int MY_SOCKET_TIMEOUT_MS=15000;

        myJsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(myJsonArrayRequest);
    }

    public void populateRecyclerView() {
        progressBar.setVisibility(View.GONE);
        recyclerViewAdapter = new RecyclerViewAdapter(this,
                currentList);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public void startBackgroundTask() {
        // boolean stopThread = false;
        // newList = new ArrayList<>();
        Log.d("backgroundTask", "Starting background task of retrieving slots.");
        runnable = new BackgroundRunnable();
        new Thread(runnable).start();
    }

    public class BackgroundRunnable implements Runnable {
        private static final String TAG = "BackgroundRunnable";
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

            if(threadRunning) {
                handler.postDelayed(new Repeater(handler), 10000);
            } else {
                return;
            }

        }


    }

    public class Repeater implements Runnable {

        private Runnable thisRunnable = this;
        private Handler handler;

        public Repeater(Handler threadHandler) {
            handler = threadHandler;
        }

        @Override
        public void run() {
            getNearestSlotsInBackground(latitude, longitude, location_id, vehicle_type);
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

            Log.d("backgroundTask" , myRequest.toString());

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

                                        // Log.d(TAG, "new list: ");
                                        // printList(newList);
                                        Log.d("backgroundTask" ,
                                                "Response length: " + Integer.toString(newList.size()));

                                        recyclerViewAdapter.updateSlots(newList);
                                        if(threadRunning) {
                                            handler.postDelayed(thisRunnable, 5000);
                                        }


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


            int MY_SOCKET_TIMEOUT_MS=15000;

            myJsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(myJsonArrayRequest);
        }
    }


    public void setRunning(boolean runThread) {
        threadRunning = runThread;
        if(threadRunning) {
            runnable.run();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("myThread", "pressed back, stopping");

        if(runnable != null) {
            setRunning(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("myThread", "paused, stopping");


        if(runnable != null) {
            setRunning(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(shouldExecuteOnResume){

            if(runnable != null) {
                setRunning(true);
            }
            Log.d("myThread", "resumed, starting");
        } else{
            shouldExecuteOnResume = true;
        }

    }
}