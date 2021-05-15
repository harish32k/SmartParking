package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartparking.NearestAdapter.SlotItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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
        setContentView(R.layout.activity_show_slot);
        Intent intent = getIntent();
        slotId = intent.getLongExtra("park_id", 1);
        uid = 1;


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
                            if(available == 1) {
                                imgView.setImageDrawable(getResources().getDrawable(R.drawable.available));
                            } else {
                                imgView.setImageDrawable(getResources().getDrawable(R.drawable.occupied));
                            }

                            if(isBookmarked == 1) {
                                bookmarkButton.setText("Remove Bookmark");
                                bookmarkButton.setOnClickListener(new RemoveBookmark());
                            } else {
                                bookmarkButton.setText("Bookmark");
                                bookmarkButton.setOnClickListener(new BookmarkSlot());
                            }

                            Log.d("coordinates",latitude + ", " + longitude);

                            mapsButton.setEnabled(true);
                            bookmarkButton.setEnabled(true);
                        } catch (JSONException e) {
                            Log.d("error1",e.toString());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        Log.d("error2",error.toString());
                        error.printStackTrace();
                    }
                });


        queue.add(jsonObjectRequest);


    }

    public void onMapsClick(View v) {
        String address = latitude + "," + longitude;
        /*String url = "https://www.google.com/maps/search/?api=1&query="+address;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);*/



        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+ address); // https://stackoverflow.com/a/39444675
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
                            Log.d("VolleyError",error.toString());
                            error.printStackTrace();
                        }
                    });
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
                            Log.d("VolleyError",error.toString());
                            error.printStackTrace();
                        }
                    });
            queue.add(jsonObjectRequest);
        }
    }

}