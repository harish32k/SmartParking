package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartparking.MyJsonLibrary.MyJsonArrayRequest;
import com.example.smartparking.NearestAdapter.RecyclerViewAdapter;
import com.example.smartparking.NearestAdapter.SlotItem;
import com.example.smartparking.historyadapter.HistoryAdapter;
import com.example.smartparking.historyadapter.HistoryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity {

    private long uid;
    private ArrayList<HistoryItem> myList;
    private HistoryItem historyItem;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecyclerView = findViewById(R.id.hist_recyclerView);
        historyRecyclerView.setHasFixedSize(true);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.hist_swipeLayout);

        uid = 1;
        getHistory();


    }

    public void getHistory() {
        // get data from API
        // create request queue, set url, and create json request

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://iot-sp.herokuapp.com/user-history";
        JSONObject myRequest = new JSONObject();
        try {
            myRequest.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("History: " , myRequest.toString());

        myList = new ArrayList<>();

        MyJsonArrayRequest myJsonArrayRequest =
                new MyJsonArrayRequest(Request.Method.POST, url, myRequest,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject jsonObject = response.getJSONObject(i);
                                        Log.d("myApp", "The response is: " + jsonObject.toString());
                                        historyItem = HistoryItem.fromJSON(jsonObject);
                                        myList.add(historyItem);
                                    }
                                    populateRecyclerView();
                                    // startBackgroundTask();
                                } catch (JSONException e) {
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

        historyAdapter = new HistoryAdapter(HistoryActivity.this, myList);
        historyRecyclerView.setAdapter(historyAdapter);
        HistoryActivity thisContext = this;
        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                HistoryItem item = myList.get(position);
                Toast.makeText(thisContext, item.toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(thisContext, ShowSlotActivity.class);
                intent.putExtra("park_id", item.getSlotId());
                thisContext.startActivity(intent);

                Log.d("ClickFromViewHolder", "Clicked");
                // Log.d("mytoast", "onItemClick: " + item.toString());
            }

            @Override
            public void onDeleteClick(int position, View itemView) {
                HistoryItem item = myList.get(position);
                Button removeButton = itemView.findViewById(R.id.removeButton);
                removeButton.setEnabled(false);
                removeButton.setBackgroundColor(getResources().getColor(R.color.DarkGray));
                removeButton.setTextColor(getResources().getColor(R.color.DimGray));
                removeButton.setText("Deleting");

                deleteItem(item, removeButton);
            }


            public void deleteItem(HistoryItem item, Button removeButton ) {
                long slotId = item.getSlotId();
                RequestQueue queue = Volley.newRequestQueue(thisContext);
                String url = "https://iot-sp.herokuapp.com/remove-history-item";
                JSONObject myRequest = new JSONObject();

                HashMap<String, Long> params = new HashMap<>();
                params.put("park_id", slotId);
                params.put("uid", uid);

                Log.d("History: " , myRequest.toString());

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                removeButton.setText("Deleted");
                                removeButton.setBackgroundColor(getResources().getColor(R.color.black));
                                removeButton.setTextColor(getResources().getColor(R.color.white));
                                Toast.makeText(thisContext, "Deleted: " + item.toString(), Toast.LENGTH_SHORT).show();
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


        });

        swipeRefreshLayout.setOnRefreshListener(new GetUpdatedData(thisContext));

    }

    public class GetUpdatedData implements SwipeRefreshLayout.OnRefreshListener{
        public HistoryActivity thisContext;
        public GetUpdatedData(HistoryActivity thisContext) {
            this.thisContext = thisContext;
        }

        @Override
        public void onRefresh() {
            // get data from API
            // create request queue, set url, and create json request

            RequestQueue queue = Volley.newRequestQueue(thisContext);
            String url = "https://iot-sp.herokuapp.com/user-history";
            JSONObject myRequest = new JSONObject();
            try {
                myRequest.put("uid", uid);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("History: " , myRequest.toString());


            MyJsonArrayRequest myJsonArrayRequest =
                    new MyJsonArrayRequest(Request.Method.POST, url, myRequest,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        myList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject jsonObject = response.getJSONObject(i);
                                            Log.d("myApp", "The response is: " + jsonObject.toString());
                                            historyItem = HistoryItem.fromJSON(jsonObject);
                                            myList.add(historyItem);
                                        }
                                        updateRecyclerView();
                                        // startBackgroundTask();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("myApp", "There is a VolleyError: " + error.toString());
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                    );


            queue.add(myJsonArrayRequest);
        }

        public void updateRecyclerView() {
            historyAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

}