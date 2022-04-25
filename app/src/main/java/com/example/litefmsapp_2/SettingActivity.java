package com.example.litefmsapp_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingActivity extends AppCompatActivity {
    Button btn_cancel, btn_update, btn_save;
    String url_get_vehicle, url_get_map, url_get_operator, url_get_shift;
    EditText input_vehicle;
    ImageButton img_logo;

    DBmain dBmain;

    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    ArrayList<HashMap<String, String>> list_vehicle, list_map, list_operator, list_shift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_update = findViewById(R.id.btn_update);
        btn_save = findViewById(R.id.btn_submit);
        input_vehicle = findViewById(R.id.input_vehicle);
        img_logo = findViewById(R.id.img_logo);

        img_logo.setImageResource(R.drawable.logo);

        url_get_vehicle = getString(R.string.api_vehicle);
        url_get_map = getString(R.string.api_map);
        url_get_operator = getString(R.string.api_operator);
        url_get_shift = getString(R.string.api_shift);

        dBmain = new DBmain(this);

        Cursor rs = dBmain.get_tbl_unit();
        rs.moveToFirst();
        input_vehicle.setText(rs.getString(2));

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("UPDATE");
                if(!DetectConnection.checkInternetConnection(SettingActivity.this)){
                    SweetAlertDialog pDialog = new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.ERROR_TYPE);
                    pDialog.setTitleText("Tidak ada koneksi");
                    pDialog.show();
                }else{
                    dBmain.delete_tbl_vehicle();
                    dBmain.delete_tbl_map();
                    dBmain.delete_tbl_operator();
                    dBmain.delete_tbl_shift();
                    requestQueue = Volley.newRequestQueue(SettingActivity.this);
//                  Get Vehicle
                    list_vehicle = new ArrayList<HashMap<String, String>>();
                    stringRequest = new StringRequest(Request.Method.GET, url_get_vehicle, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject((response));
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject json = jsonArray.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put("id", json.getString("id"));
                                    map.put("name", json.getString("name"));
                                    map.put("vehicle_id", json.getString("vehicle_id"));
                                    map.put("egi_id", json.getString("egi_id"));
                                    map.put("eq_class_id", json.getString("eq_class_id"));
                                    list_vehicle.add(map);
                                    String id = list_vehicle.get(i).get("id");
                                    String name = list_vehicle.get(i).get("name");
                                    String vehicle_id = list_vehicle.get(i).get("vehicle_id");
                                    String egi_id = list_vehicle.get(i).get("egi_id");
                                    String eq_class_id = list_vehicle.get(i).get("eq_class_id");
                                    dBmain.insert_tbl_vehicle(id, vehicle_id, name, egi_id, eq_class_id);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
                    requestQueue.add(stringRequest);
//                  Get Map
                    list_map = new ArrayList<HashMap<String, String>>();
                    stringRequest = new StringRequest(Request.Method.GET, url_get_map, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject((response));
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
                    requestQueue.add(stringRequest);
//                  Get Operator
                    list_operator = new ArrayList<HashMap<String, String>>();
                    stringRequest = new StringRequest(Request.Method.GET, url_get_operator, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject((response));
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject json = jsonArray.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put("id", json.getString("id"));
                                    map.put("nrp", json.getString("nrp"));
                                    map.put("name", json.getString("name"));
                                    map.put("company_id", json.getString("company_id"));
                                    list_operator.add(map);
                                    String id = list_operator.get(i).get("id");
                                    String nrp = list_operator.get(i).get("nrp");
                                    String name = list_operator.get(i).get("name");
                                    String company_id = list_operator.get(i).get("company_id");
                                    dBmain.insert_tbl_operator(id, nrp, name, company_id);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
                    requestQueue.add(stringRequest);
//                  Get Shift
                    list_shift = new ArrayList<HashMap<String, String>>();
                    stringRequest = new StringRequest(Request.Method.GET, url_get_shift, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject((response));
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject json = jsonArray.getJSONObject(i);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put("id", json.getString("id"));
                                    map.put("name", json.getString("name"));
                                    map.put("start_time", json.getString("start_time"));
                                    map.put("stop_time", json.getString("stop_time"));
                                    map.put("description", json.getString("description"));
                                    list_shift.add(map);
                                    String id = list_shift.get(i).get("id");
                                    String name = list_shift.get(i).get("name");
                                    String start_time = list_shift.get(i).get("start_time");
                                    String stop_time = list_shift.get(i).get("stop_time");
                                    String description = list_shift.get(i).get("description");
                                    System.out.println("id : " + id);
                                    dBmain.insert_tbl_shift(id, name, start_time,stop_time ,description);
                                }
                                Cursor rs_shift = dBmain.getShift();
                                if(rs_shift.moveToFirst())
                                {
                                    SweetAlertDialog pDialog = new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                                    pDialog.setTitleText("Data berhasil diperbaharui");
                                    pDialog.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
                    requestQueue.add(stringRequest);
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor rs = dBmain.getVehicleID(input_vehicle.getText().toString());
                if(rs.moveToFirst()){
                    dBmain.updatetbl_unit(1,rs.getString(1), rs.getString(2));
                    SweetAlertDialog pDialog = new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                    pDialog.setTitleText("Data berhasil di simpan");
                    pDialog.show();
                }else{
                    SweetAlertDialog pDialog = new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.ERROR_TYPE);
                    pDialog.setTitleText("Unit tidak terdaftar");
                    pDialog.show();
                }

            }
        });
    }
}