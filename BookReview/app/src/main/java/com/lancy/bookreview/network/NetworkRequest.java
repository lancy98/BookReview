package com.lancy.bookreview.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NetworkRequest {

    private RequestQueue requestQueue;
    private NetworkRequest.NetworkResponse responseObject;
    public String URL;

    public NetworkRequest(Context context, String URL, NetworkRequest.NetworkResponse completionHandler) {
        requestQueue = Volley.newRequestQueue(context);
        responseObject = completionHandler;
        this.URL = URL;

        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.GET,
                URL,
                successListener(),
                errorListener()
        );

        requestQueue.add(jsonObjectRequest);
    }

    private Response.Listener<String > successListener() {
        Response.Listener<String> successObject = new Response.Listener<String>() {
            @Override
            public void onResponse(String responseString) {
                responseObject.networkResponse(URL, responseString, null);
            }
        };

        return successObject;
    }

    private Response.ErrorListener errorListener() {
        Response.ErrorListener errorObject = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseObject.networkResponse(URL, null, error);
            }
        };

        return  errorObject;
    }

    public interface NetworkResponse {
        public void networkResponse(String url, String responseString, VolleyError error);
    }
}