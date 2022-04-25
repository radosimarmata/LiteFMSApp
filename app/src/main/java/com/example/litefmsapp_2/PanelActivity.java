package com.example.litefmsapp_2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.Geofence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PanelActivity extends AppCompatActivity {
    Button btn_logout;
    TextView txt_shift, txt_vehicle_name, txt_assigment, txt_lat, txt_long;
    private MapView map = null;
    private Context context;
    LocationListener locationListenerGPS;
    LocationManager locationManager;
    DBmain dBmain;
    IMapController mapController;
    MyLocationNewOverlay myLocation;
    Drawable truk;
    Bitmap icTruk;
    List<GeoPoint> list_line, list_polygon ,list_circle;
    int type_map;
    String name_map, color_map, hexColor;
    Polyline line;
    Polygon polygon, p_circle;
    Marker marker;
    Float radius, longitude, latitude, x, y;
    GeoPoint g_circle, startMap;
    Double mylat, mylong;
    Time stop_tm, start_tm;
    String url_get_map,url_message, url_assigment, url_trk_log, url_operator_log, id_log, nrp, opr_name, unit_name, unit_id, id_shift, shift_id, shift_ds, location, assigment, msg_id, msg_text, msg_time,id_shift_logout;
    int timemsg;
    Boolean msg_show;
    ArrayList<HashMap<String, String>> list_map, list_assigment, list_inbox;
    JSONArray jsonLocation, jsonAssigment, jsonMessage;
    private RequestQueue requestQueue, requestAssigment, requestMessage, postQueue;
    private StringRequest mapRequest, assigmentRequest, messageRequest;
    Handler locHandler, assHandler, msgHandler, map2Handler;
    Runnable runLoc, runAss, runMsg, runMap;
    JsonObjectRequest sendLog;
    JSONObject dataLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        context = this;
        timemsg = 1000;
        btn_logout = findViewById(R.id.btn_logout);
        txt_shift = findViewById(R.id.txt_shift);
        txt_vehicle_name = findViewById(R.id.txt_vehicle_name);
//        txt_location = findViewById(R.id.txt_location);
        txt_assigment = findViewById(R.id.txt_assigment);
        txt_lat = findViewById(R.id.txt_lat);
        txt_long = findViewById(R.id.txt_long);

        truk = ResourcesCompat.getDrawable(getResources(), R.drawable.truk, null);
        icTruk = ((BitmapDrawable) truk).getBitmap();

        dBmain = new DBmain(this);
        locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                mylat=location.getLatitude();
                mylong=location.getLongitude();
                startMap = new GeoPoint(mylat, mylong);
                mapController.setCenter(startMap);
                txt_lat.setText("Lat : " + mylat);
                txt_long.setText("Long : " + mylong);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListenerGPS);
        isLocationEnabled();

//      Get from Login
        nrp = (String) getIntent().getStringExtra("nrp");
        opr_name = (String) getIntent().getStringExtra("Opr_name");
        shift_id = (String) getIntent().getStringExtra("shift_id");
        shift_ds = (String) getIntent().getStringExtra("shift_desc");
        unit_name = (String) getIntent().getStringExtra("unit_name");
        unit_id = (String) getIntent().getStringExtra("unit_id");

        url_get_map = getString(R.string.api_map);
        url_trk_log = getString(R.string.api_trklog) + "/" + unit_id;
        url_assigment = getString(R.string.api_fleet_dt) + "/" + unit_name;
        url_message = getString(R.string.api_msg) + unit_name;
        url_operator_log =getString(R.string.api_operator_log);

        btn_logout.setText(opr_name);
        txt_shift.setText(shift_ds + " ("+ shift_id + ")");
        txt_vehicle_name.setText(unit_name);

        createMap();

        requestQueue = Volley.newRequestQueue(PanelActivity.this);

        locHandler = new Handler();
        assHandler = new Handler();
        msgHandler = new Handler();
        map2Handler = new Handler();
        msg_show = false;
        postQueue = Volley.newRequestQueue(PanelActivity.this);

        getAssg();
        getMsg();
        cekMap();
        logOpr();

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SweetAlertDialog alertDialog = new SweetAlertDialog(PanelActivity.this, SweetAlertDialog.WARNING_TYPE);
                alertDialog.setTitleText("Apakah Anda yakin akan keluar ?")
                            .setCancelText("Tidak")
                            .setConfirmText("Ya")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    Cursor ls = dBmain.getShift();
                                    ls.moveToFirst();
                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    String time_logout = DateFormat.format("HH:mm:ss", timestamp).toString();
                                    Time ns = Time.valueOf(time_logout);
                                    while (!ls.isAfterLast())
                                    {

                                        if(ls.getInt(0) == 2){
                                            stop_tm = Time.valueOf(ls.getString(3) + (1000 * 60 * 60 * 24));
                                        }else{
                                            stop_tm = Time.valueOf(ls.getString(3));
                                        }
                                        start_tm = Time.valueOf(ls.getString(2));

                                        if(ns.after(start_tm) && ns.before(stop_tm))
                                        {
                                            id_shift_logout = ls.getString(0);
                                        }
                                        ls.moveToNext();
                                    }
                                    dBmain.insert_tbl_opr_log(nrp, unit_id, String.valueOf(timestamp), id_shift_logout, "0", "0");
                                    logOpr();
                                    sDialog.dismissWithAnimation();
                                    locHandler.removeCallbacks(runLoc);
                                    assHandler.removeCallbacks(runAss);
                                    msgHandler.removeCallbacks(runMsg);
                                    map2Handler.removeCallbacks(runMap);
                                    Intent intent = new Intent(PanelActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                    startActivity(intent);
                                }
                            })
                            .show();
            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (map != null) {
            map.onResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (map != null) {
            map.onPause();
        }
    }

    private void logOpr()
    {
        Cursor lopr = dBmain.getOprLog("0");
        dataLog = new JSONObject();
        lopr.moveToFirst();
        if(DetectConnection.checkInternetConnection(PanelActivity.this))
        {
            while (!lopr.isAfterLast())
            {
                id_log = lopr.getString(0);
                try{
                    dataLog.put("nrp", lopr.getString(1));
                    dataLog.put("vehicle_id", lopr.getString(2));
                    dataLog.put("time_stamp", lopr.getString(3));
                    dataLog.put("shift_id", lopr.getString(4));
                    dataLog.put("status", lopr.getString(5));
                }catch (JSONException e){
                    e.printStackTrace();
                }
                sendLog = new JsonObjectRequest(Request.Method.POST, url_operator_log, dataLog,
                        new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                postQueue.add(sendLog);
                dBmain.update_tbl_opr_log(id_log, "1");
                lopr.moveToNext();
            }

        }

    }

    private void isLocationEnabled() {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
    }

    private void getAssg()
    {
        runAss = new Runnable() {
            @Override
            public void run() {
                logOpr();
                if(DetectConnection.checkInternetConnection(PanelActivity.this)){
                    list_assigment = new ArrayList<HashMap<String, String>>();
                    assigmentRequest = new StringRequest(Request.Method.GET, url_assigment, response ->{
                        try {
                            JSONObject jsonObject = new JSONObject((response));
                            jsonAssigment = jsonObject.getJSONArray("results");
                            for(int i=0; i<jsonAssigment.length();i++){
                                assigment = "Sumber : "
                                        +jsonAssigment.getJSONObject(i).getString("source")
                                        + " | Tujuan : "
                                        + jsonAssigment.getJSONObject(i).getString("destination");
                            }
                        }catch (JSONException e){
//                        e.printStackTrace();
                        }
                    },volleyError -> System.out.println(volleyError.getMessage()));
                    if(requestAssigment==null){
                        requestAssigment = Volley.newRequestQueue(PanelActivity.this);
                        requestAssigment.add(assigmentRequest);
                    }else{
                        requestAssigment.add(assigmentRequest);
                    }
                    txt_assigment = findViewById(R.id.txt_assigment);
                    txt_assigment.setText(assigment);
                }
                assHandler.postDelayed(this, 10000);
            }
        };
        assHandler.postDelayed(runAss, 10000);
    }

    private void getMsg()
    {
        runMsg = new Runnable() {
            @Override
            public void run() {
                if(DetectConnection.checkInternetConnection(PanelActivity.this)){
                    list_inbox = new ArrayList<HashMap<String, String>>();
                    messageRequest = new StringRequest(Request.Method.GET, url_message, response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            jsonMessage = jsonObject.getJSONArray("results");
                            if(jsonMessage.length()>0) {
                                for (int i = 0; i < jsonMessage.length(); i++) {
                                    msg_text = jsonMessage.getJSONObject(i).getString("msg_text");
                                    msg_time = jsonMessage.getJSONObject(i).getString("msg_time");
                                    msg_id = jsonMessage.getJSONObject(i).getString("id");
                                    msg_time = msg_time.replace("Z", "").replace("T", " ");
                                }
                                Date date_msg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(msg_time);
                                Date date_now = new Date();

                                long minutesd = TimeUnit.MILLISECONDS.toMinutes(date_msg.getTime());
                                long minutesn = TimeUnit.MILLISECONDS.toMinutes(date_now.getTime());
                                long cek_10min = minutesn - minutesd;
                                if(cek_10min<= 10){
                                    timemsg = (int) ((11 - cek_10min)*60000);
                                    msg_show = true;
                                }
                                else{
                                    timemsg = 1000;
                                    msg_show = false;
                                    msg_text = null;
                                    JSONObject postData = new JSONObject();
                                    try{
                                        postData.put("msg_status", 2);
                                        postData.put("id", msg_id);
                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url_message, postData, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                        }
                                    });
                                    postQueue.add(jsonObjectRequest);
                                }
                            }
                            else
                            {
                                timemsg = 1000;
                                msg_show = false;
                                msg_text = null;
                            }
                        }catch (JSONException | ParseException e){
                            e.printStackTrace();
                        }
                    },volleyError -> System.out.println(volleyError.getMessage()) );

                    if(requestMessage == null){
                        requestMessage = Volley.newRequestQueue(PanelActivity.this);
                        requestMessage.add(messageRequest);
                    }else{
                        requestMessage.add(messageRequest);
                    }

                    if(msg_show){
                        msg_show = false;
                        JSONObject postData = new JSONObject();
                        try{
                            postData.put("msg_status", 0);
                            postData.put("id", msg_id);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url_message, postData, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                        postQueue.add(jsonObjectRequest);
                        SweetAlertDialog alertDialog = new SweetAlertDialog(PanelActivity.this, SweetAlertDialog.NORMAL_TYPE);
                        alertDialog.setTitleText(msg_text);
                        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        });
                        alertDialog.show();
                    }
                }
                msgHandler.postDelayed(this, timemsg);
            }
        };
        msgHandler.postDelayed(runMsg, timemsg);
    }

    private void cekMap()
    {
        runMap = new Runnable() {
            @Override
            public void run() {
                if(DetectConnection.checkInternetConnection(PanelActivity.this)){
                    list_map = new ArrayList<HashMap<String, String>>();
                    mapRequest = new StringRequest(Request.Method.GET, url_get_map, response -> {
                        try {
                            ArrayList lisx = new ArrayList();
                            JSONObject jsonObject = new JSONObject((response));
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                String id =json.getString("id");
                                lisx.add(id);

                            }
                            Cursor rk = dBmain.getMapList();
                            ArrayList idmap = new ArrayList();
                            if(rk.moveToFirst())
                            {
                                while (!rk.isAfterLast())
                                {
                                    idmap.add(rk.getString(0));
                                    rk.moveToNext();
                                }
                            }
                            Collections.sort(lisx);
                            Collections.sort(idmap);
                            boolean bool = lisx.equals(idmap);
                            if(!bool)
                            {
                                dBmain.delete_tbl_map();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject json = jsonArray.getJSONObject(i);
                                    String id =json.getString("id");
                                    String name =json.getString("name");
                                    String description =json.getString("description");
                                    String rid =json.getString("rid");
                                    String p =json.getString("p");
                                    String t =json.getString("t");
                                    String c =json.getString("c");
                                    String b =json.getString("b");
                                    dBmain.insert_tbl_map(id, name,description, rid, p, t, c, b);
                                }
                                Cursor nmap = dBmain.getMapList();
                                if(nmap.moveToFirst())
                                {
                                    map.getOverlays().clear();
                                    createMap();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, volleyError -> System.out.println("Error"));
                    requestQueue.add(mapRequest);
                }
                map2Handler.postDelayed(this,1000);
            }
        };
        map2Handler.postDelayed(runMap, 1000);
    }

    private void createMap()
    {
        map = findViewById(R.id.mapView);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(17.0D);

        dBmain = new DBmain(this);
        Cursor rs = dBmain.getMap();
        if(rs.moveToNext())
        {
            while (!rs.isAfterLast())
            {
                type_map = Integer.parseInt(rs.getString(5));
                name_map = rs.getString(1);
                color_map = rs.getString(6);
                long l = Long.parseLong(color_map);
                hexColor =Long.toHexString(l);
                try {
                    if(type_map == 2)
                    {
//                      Polygon
                        polygon = new Polygon();
                        list_polygon = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(rs.getString(4));
                        for(int i=0; i < jsonArray.length(); i++){
                            JSONObject json = jsonArray.getJSONObject(i);
                            longitude = Float.valueOf(json.getString("x"));
                            latitude = Float.valueOf(json.getString("y"));
                            list_polygon.add(new GeoPoint(latitude, longitude));
                        }

                        polygon.setPoints(list_polygon);
                        polygon.setFillColor(Color.parseColor("#" + hexColor));
                        polygon.setStrokeWidth(0f);
                        polygon.setTitle(name_map);
                        polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
                        map.getOverlays().add(polygon);

                        String item = String.valueOf(list_polygon.get((list_polygon.size()/2)+1));
                        String[] geoPolygon = item.split(",");
                        x = Float.valueOf(geoPolygon[0]);
                        y = Float.valueOf(geoPolygon[1]);
                        marker = new Marker(map);
                        marker.setIcon(null);
                        marker.setInfoWindow(null);
                        marker.setTextIcon(name_map);
                        marker.setPosition(new GeoPoint(x, y));
                        map.getOverlays().add(marker);

                    }else if(type_map == 3)
                    {
//                      Circle
                        p_circle = new Polygon();
                        list_circle = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(rs.getString(4));
                        for(int i=0; i < jsonArray.length(); i++){
                            JSONObject json = jsonArray.getJSONObject(i);
                            longitude = Float.valueOf(json.getString("x"));
                            latitude = Float.valueOf(json.getString("y"));
                            radius = Float.valueOf(json.getString("r"));
                            list_circle.add(new GeoPoint(latitude, longitude));
                            g_circle = new GeoPoint(latitude, longitude);
                        }
                        p_circle.setPoints(Polygon.pointsAsCircle(g_circle, radius));
                        p_circle.setFillColor(Color.parseColor("#" + hexColor));
                        p_circle.setStrokeWidth(0);
                        p_circle.getOutlinePaint().setColor(polygon.getFillPaint().getColor());
                        marker = new Marker(map);
//                        marker.setIcon(getDrawable(R.drawable.ic_location));
                        marker.setIcon(null);
                        marker.setInfoWindow(null);
                        marker.setTextIcon(name_map);
                        marker.setPosition(g_circle);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        map.getOverlays().add(p_circle);
                        map.getOverlays().add(marker);

                    }else if(type_map==1)
                    {
//                      Line
                        line = new Polyline();
                        list_line = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(rs.getString(4));
                        for(int i=0; i < jsonArray.length(); i++){
                            JSONObject json = jsonArray.getJSONObject(i);
                            longitude = Float.valueOf(json.getString("x"));
                            latitude = Float.valueOf(json.getString("y"));
                            list_line.add(new GeoPoint(latitude, longitude));
                        }
                        line.setPoints(list_line);
                        line.setGeodesic(true);
                        line.setColor(Color.parseColor("#" + hexColor));
                        line.getOutlinePaint().setStrokeWidth(30f);
                        line.setWidth(15f);
                        line.getPaint().setStrokeCap(Paint.Cap.ROUND);
                        map.getOverlays().add(line);

                        JSONArray arrayBoundary = new JSONArray(rs.getString(7));
                        for(int i=0; i < arrayBoundary.length(); i++){
                            JSONObject json = arrayBoundary.getJSONObject(i);
                            longitude = Float.valueOf(json.getString("cen_x"));
                            latitude = Float.valueOf(json.getString("cen_y"));
                        }
                        marker = new Marker(map);
                        marker.setIcon(null);
                        marker.setInfoWindow(null);
                        marker.setTextIcon(name_map);
                        marker.setPosition(new GeoPoint(latitude, longitude));
                        map.getOverlays().add(marker);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                rs.moveToNext();

            }
        }

//      Lokasi User
        myLocation = new MyLocationNewOverlay(map);
        myLocation.enableFollowLocation();
        myLocation.setDirectionArrow(icTruk, icTruk);
        myLocation.enableMyLocation();
        myLocation.setDrawAccuracyEnabled(true);
        map.getOverlays().add(myLocation);
    }

}