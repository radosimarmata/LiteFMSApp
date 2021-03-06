package com.example.litefmsapp_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.sql.Timestamp;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    Button btn_login;
    EditText input_nrp;
    String input_nrp_val, id_shift, shift_id, shift_ds, opr_name, unit_name, creator_id, device_id, url_device,r_id, r_device_id, r_mac_address;
    DBmain dBmain;
    private RequestQueue getDevice;
    Time stop_tm, start_tm;
    ImageButton img_logo;
    JSONObject jsonDevice;
    JSONArray arrayDevice;
    StringRequest stringReq;
    SweetAlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_login = findViewById(R.id.btn_login);
        input_nrp = findViewById(R.id.input_nrp);
        img_logo = findViewById(R.id.img_logo);

        img_logo.setImageResource(R.drawable.logo);

        device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        url_device = getString(R.string.api_device) + device_id;
        getDevice = Volley.newRequestQueue(MainActivity.this);

        dBmain = new DBmain(this);
        Cursor tes = dBmain.gettbldevice();
        if(!tes.moveToFirst())
        {
            if(DetectConnection.checkInternetConnection(MainActivity.this))
            {
                stringReq = new StringRequest(Request.Method.GET, url_device, response -> {
                    try {
                        jsonDevice = new JSONObject(response);
                        arrayDevice = jsonDevice.getJSONArray("results");
                        if(arrayDevice.length()>=1)
                        {
                            for (int i = 0; i < arrayDevice.length(); i++) {
                                r_device_id = arrayDevice.getJSONObject(i).getString("device_id");
                            }
                            dBmain.insert_tbl_device(r_id, r_device_id);
                        }else{
                            finish();
                            System.exit(0);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }, volleyError -> System.out.println(volleyError.getMessage()));
                getDevice.add(stringReq);
            }else{
                alertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
                alertDialog.setTitleText("Oops...")
                        .setContentText("Tidak ada Koneksi")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                finish();
                                System.exit(0);
                            }
                        })
                        .show();

            }
        }

        shift_id = null;
        shift_ds = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input_nrp_val = input_nrp.getText().toString();
                if(input_nrp_val.equals("admin"))
                {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }else{
                    Cursor rs = dBmain.operatorLogin(input_nrp_val);
                    if(rs.getCount()>0)
                    {
                        rs.moveToFirst();
                        opr_name = rs.getString(2);
                        Cursor lu = dBmain.get_tbl_unit();
                        lu.moveToFirst();
                        unit_name = lu.getString(2);
                        creator_id = lu.getString(3);
                        Cursor ls = dBmain.getShift();
                        ls.moveToFirst();
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String time_login = DateFormat.format("HH:mm:ss", timestamp).toString();
                        Time ns = Time.valueOf(time_login);
                        while (!ls.isAfterLast())
                        {
                            if(ls.getInt(0) == 2){
                                stop_tm = Time.valueOf(ls.getString(3) + (1000 * 60 * 60 * 24));
                            }else{
                                stop_tm = Time.valueOf(ls.getString(3));
                            }
                            start_tm = Time.valueOf(ls.getString(2));

                            if(ns.after(start_tm) && ns.before(stop_tm) && shift_ds == null)
                            {
                                shift_ds = ls.getString(4);
                                shift_id = ls.getString(1);
                                id_shift = ls.getString(0);
                            }
                            ls.moveToNext();
                        }

                        dBmain.insert_tbl_opr_log(input_nrp_val, lu.getString(1), String.valueOf(timestamp), id_shift, "1", "0");
                        Intent intent = new Intent(MainActivity.this, PanelActivity.class);
                        intent.putExtra("nrp", input_nrp_val);
                        intent.putExtra("Opr_name", opr_name);
                        intent.putExtra("shift_desc", shift_ds);
                        intent.putExtra("shift_id", shift_id);
                        intent.putExtra("unit_name", unit_name);
                        intent.putExtra("creator_id", creator_id);
                        intent.putExtra("unit_id", lu.getString(1));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
                        pDialog.setTitleText("Oops...").setContentText("NRP Tidak Terdaftar").show();
                    }

                }
            }
        });
    }

}