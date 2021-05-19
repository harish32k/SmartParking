package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartparking.MyJsonLibrary.MyJsonArrayRequest;
import com.example.smartparking.NearestAdapter.SlotItem;
import com.google.android.gms.common.internal.ReflectedParcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.smartparking.NotificationApp.CHANNEL_1_ID;

public class ShowSlotActivity extends AppCompatActivity {

    private double latitude;
    private double longitude;
    long slotId;
    long uid;
    int vehicle_type;
    String detail;
    int available;
    private String locality;
    private long pincode;
    private String zone;
    private String dname;
    private int isBookmarked;
    public static final String[] vehicles = SlotItem.vehicles;
    public static final String[] status = {"Occupied", "Available", "Occupied"};

    private TextView slotIdView;
    private TextView vehicleView;
    private TextView zoneView;
    private TextView pincodeView;
    private TextView districtView;
    private TextView availableView;
    private TextView localityView;
    private ImageView imgView;
    private TextView detailTextView;
    private Button bookmarkButton;
    private Button mapsButton;


    private volatile boolean threadRunning = true;
    private ShowSlotActivity thisContext;
    private MyRunnable runnable;
    private NotificationManagerCompat notificationManager;
    private int prevStatus;

//    long slotId;
//    int vehicle_type;
//    String detail;
//    int available;
//    private String locality;
//    private long pincode;
//    private String zone;
//    private String dname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisContext = this;
        setContentView(R.layout.activity_show_slot);
        Intent intent = getIntent();
        slotId = intent.getLongExtra("park_id", 1);
        uid = 1;
        notificationManager = NotificationManagerCompat.from(this);



        slotIdView = findViewById(R.id.show_slotIdView);
        vehicleView = findViewById(R.id.show_vehicleView);
        zoneView = findViewById(R.id.show_zoneView);
        pincodeView = findViewById(R.id.show_pincodeView);
        districtView = findViewById(R.id.show_districtView);
        availableView = findViewById(R.id.show_availableView);
        localityView = findViewById(R.id.show_localityView);
        imgView = findViewById(R.id.show_imgView);
        detailTextView = findViewById(R.id.show_detailTextView);
        bookmarkButton = findViewById(R.id.bookmarkButton);
        mapsButton = findViewById(R.id.mapsButton);


        slotIdView.setText("Slot ID: " + Long.toString(slotId));

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://iot-sp.herokuapp.com/user-slot-info";

        HashMap<String, Long> params = new HashMap<>();
        params.put("park_id", slotId);
        params.put("uid", uid);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            latitude = response.getDouble("latitude");
                            longitude = response.getDouble("longitude");

                            vehicle_type = response.getInt("vehicle_type");
                            detail = response.getString("detail");
                            available = response.getInt("available");
                            locality = response.getString("locality");
                            pincode = response.getLong("pincode");
                            zone = response.getString("zone");
                            dname = response.getString("dname");
                            isBookmarked = response.getInt("is_bookmarked");

                            vehicleView.setText(vehicles[vehicle_type] + " parking");
                            zoneView.setText("Postal-zone: " + zone);
                            pincodeView.setText("Pincode: " + pincode);
                            districtView.setText("District: " + dname);
                            availableView.setText(status[available]);
                            localityView.setText("Locality: " + locality);
                            detailTextView.setText(detail);
                            prevStatus = available;
                            if (available == 1) {
                                imgView.setImageDrawable(getResources().getDrawable(R.drawable.available));
                            } else {
                                imgView.setImageDrawable(getResources().getDrawable(R.drawable.occupied));
                            }

                            if (isBookmarked == 1) {
                                bookmarkButton.setText("Remove Bookmark");
                                bookmarkButton.setOnClickListener(new RemoveBookmark());
                            } else {
                                bookmarkButton.setText("Bookmark");
                                bookmarkButton.setOnClickListener(new BookmarkSlot());
                            }

                            Log.d("coordinates", latitude + ", " + longitude);

                            mapsButton.setEnabled(true);
                            bookmarkButton.setEnabled(true);

                            startBackgroundTask();
                        } catch (JSONException e) {
                            Log.d("error1", e.toString());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.d("error2", error.toString());
                        error.printStackTrace();
                    }
                });


        int MY_SOCKET_TIMEOUT_MS=15000;

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);


    }


    public void onMapsClick(View v) {
        String address = latitude + "," + longitude;
        /*String url = "https://www.google.com/maps/search/?api=1&query="+address;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);*/


        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address); // https://stackoverflow.com/a/39444675
        // Uri.parse("geo:"+address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public class BookmarkSlot implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            bookmarkButton.setEnabled(false);
            RequestQueue queue = Volley.newRequestQueue(ShowSlotActivity.this);
            String url = "https://iot-sp.herokuapp.com/bookmark-slot";

            HashMap<String, Long> params = new HashMap<>();
            params.put("park_id", slotId);
            params.put("uid", uid);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            bookmarkButton.setOnClickListener(new RemoveBookmark());
                            bookmarkButton.setText("Remove Bookmark");
                            bookmarkButton.setEnabled(true);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("VolleyError", error.toString());
                            error.printStackTrace();
                        }
                    });

            int MY_SOCKET_TIMEOUT_MS=15000;

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public class RemoveBookmark implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            bookmarkButton.setEnabled(false);
            RequestQueue queue = Volley.newRequestQueue(ShowSlotActivity.this);
            String url = "https://iot-sp.herokuapp.com/remove-bookmark";

            HashMap<String, Long> params = new HashMap<>();
            params.put("park_id", slotId);
            params.put("uid", uid);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            bookmarkButton.setOnClickListener(new BookmarkSlot());
                            bookmarkButton.setText("Bookmark");
                            bookmarkButton.setEnabled(true);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("VolleyError", error.toString());
                            error.printStackTrace();
                        }
                    });

            int MY_SOCKET_TIMEOUT_MS=15000;

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    public void startBackgroundTask() {
        runnable = new MyRunnable();
        new Thread(runnable).start();
        Log.d("slotBackground", "Starting slot background task");
    }

    public class MyRunnable implements Runnable {
        private static final String TAG = "MyRunnable";
        public Handler handler;

        MyRunnable() {
            handler = new Handler();
        }

        @Override
        public void run() {

            if (threadRunning) {
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

            Log.d("slotBackground", "Checking status...");
            checkSlotStatus();
            // SystemClock.sleep(5000);
        }

        public void checkSlotStatus() {
            RequestQueue queue = Volley.newRequestQueue(thisContext);
            String url = "https://iot-sp.herokuapp.com/get-slot-info";

            HashMap<String, Long> params = new HashMap<>();
            params.put("park_id", slotId);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        available = response.getInt("available");
                                        availableView.setText(status[available]);
                                        if (available == 1) {
                                            imgView.setImageDrawable(getResources().getDrawable(R.drawable.available));
                                        } else {
                                            imgView.setImageDrawable(getResources().getDrawable(R.drawable.occupied));
                                        }

                                        checkStatusChange();
                                    } catch (JSONException e) {
                                        Log.d("error1", e.toString());
                                        e.printStackTrace();
                                    }

                                    if (threadRunning) {
                                        handler.postDelayed(thisRunnable, 5000);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    VolleyLog.e("Error: ", error.getMessage());
                                    Log.d("error2", error.toString());
                                    error.printStackTrace();
                                }
                            });

            int MY_SOCKET_TIMEOUT_MS=15000;

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(jsonObjectRequest);

        }

        public void checkStatusChange() {


            if(prevStatus != available) {

                Intent activityIntent = new Intent(thisContext, ShowSlotActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent contentIntent = PendingIntent.getActivity(thisContext, 0,
                        activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                int img;
                String message;
                String title = "Slot: " + Long.toString(slotId) + " status update!";
                if(available == 1) {
                    img = R.drawable.available;
                    message = "Slot: " + Long.toString(slotId) +" is free now.";
                }
                else{
                    img = R.drawable.occupied;
                    message = "Slot: " + Long.toString(slotId) +" is occupied.";
                }
                Notification notification =
                        new NotificationCompat.Builder(thisContext, CHANNEL_1_ID)
                                .setSmallIcon(img)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setContentIntent(contentIntent)
                                .build();
                notificationManager.notify(1, notification);

            }
            prevStatus = available;
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
        Log.d("slotThread", "pressed back, stopping");
        setRunning(false);
        Toast.makeText(this, "Background slot tracking stopped", Toast.LENGTH_SHORT).show();
    }
}

