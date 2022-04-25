package com.example.litefmsapp_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBmain extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "litefmsmobilev2.db";
    private static final String TABLE_UNIT = "tbl_unit";
    private static final String TABLE_VEHICLE = "tbl_vehicle";
    private static final String TABLE_MAP = "tbl_map";
    private static final String TABLE_OPERATOR = "tbl_operator";
    private static final String TABLE_OPR_LOG = "tbl_opr_log";
    private static final String TABLE_SHIFT = "tbl_shift";
    private static final int DATABASE_VERSION = 1;

    public DBmain(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table " + TABLE_UNIT + "(id integer primary key, vehicle_id Varchar , name text);";
        db.execSQL(query);
        String insert_tbl_unit = "INSERT INTO "+TABLE_UNIT+" (id, vehicle_id, name) VALUES ('1', '24436215', 'DT08');";
        db.execSQL(insert_tbl_unit);
        String q_vehicle = "create table " + TABLE_VEHICLE + "(id integer primary key, vehicle_id varchar, name text, egi_id varchar, eq_class_id varchar);";
        db.execSQL(q_vehicle);
        String q_map = "create table " + TABLE_MAP + " (id integer primary key, name varchar, description text, rid varchar, p text, t varchar, c varchar, b varchar);";
        db.execSQL(q_map);
        String q_opr = "create table " + TABLE_OPERATOR + "(id integer primary key, nrp TEXT , name TEXT, company_id INTEGER);";
        db.execSQL(q_opr);
        String q_shift = "create table " + TABLE_SHIFT + "(id integer primary key, name TEXT , start_time TIME, stop_time TIME, description TEXT);";
        db.execSQL(q_shift);
        String q_oprlog = "create table " + TABLE_OPR_LOG + "(id integer primary key, nrp TEXT , vehicle_id varchar, time_stamp DATETIME, shift_id TEXT, stat_log varchar, flag varchar);";
        db.execSQL(q_oprlog);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String query = "drop table if exists " + TABLE_UNIT + "";
        db.execSQL(query);
        onCreate(db);
    }

    public Cursor get_tbl_unit()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tbl_unit where id=1", null);
        return res;
    }

    public boolean updatetbl_unit(Integer id, String vehicle_id, String name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("vehicle_id", vehicle_id);
        contentValues.put("name", name);
        db.update("tbl_unit", contentValues, "id = ?", new String[]{ Integer.toString(id)});
        return true;
    }

    public long insert_tbl_vehicle(String id, String vehicle_id, String name, String egi_id, String eq_class_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("vehicle_id", vehicle_id);
        contentValues.put("name", name);
        contentValues.put("egi_id", egi_id);
        contentValues.put("eq_class_id", eq_class_id);
        long i = db.insert("tbl_vehicle", null, contentValues );
        return i;
    }
    public Cursor getVehicleID(String v_name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tbl_vehicle where name = '"+ v_name +"'", null);
        return res;
    }
    public int delete_tbl_vehicle()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = db.delete("tbl_vehicle", null, null);
        return count;
    }
    public long insert_tbl_map(String id, String name, String description, String rid, String p, String t, String c, String b)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("description", description);
        contentValues.put("rid", rid);
        contentValues.put("p", p);
        contentValues.put("t", t);
        contentValues.put("c", c);
        contentValues.put("b", "[" + b + "]");
        long i = db.insert("tbl_map", null, contentValues );
        return i;
    }
    public Cursor getMap()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tbl_map", null);
        return res;
    }
    public Cursor getMapList()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT id FROM tbl_map", null);
        return res;
    }
    public int delete_tbl_map()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = db.delete("tbl_map", null, null);
        return count;
    }
    public long insert_tbl_operator(String id, String nrp, String name, String company_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("nrp", nrp);
        contentValues.put("name", name);
        contentValues.put("company_id", company_id);
        long i = db.insert("tbl_operator", null, contentValues );
        return i;
    }
    public int delete_tbl_operator()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = db.delete("tbl_operator", null, null);
        return count;
    }

    public Cursor operatorLogin(String i_nrp)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tbl_operator where nrp= '"+i_nrp+"'", null);
        return res;
    }

    public long insert_tbl_shift(String id, String name, String start_time, String stop_time, String description)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("start_time", start_time);
        contentValues.put("stop_time", stop_time);
        contentValues.put("description", description);
        long i = db.insert("tbl_shift", null, contentValues );
        return i;
    }
    public int delete_tbl_shift()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = db.delete("tbl_shift", null, null);
        return count;
    }
    public Cursor getShift()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tbl_shift", null);
        return res;
    }

    public Cursor getOprLog(String flag)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tbl_opr_log WHERE flag = " + flag, null);
        return res;
    }
    public long insert_tbl_opr_log(String nrp, String vehicle_id, String time_stamp, String shift_id, String stat_log, String flag)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("nrp", nrp);
        contentValues.put("vehicle_id", vehicle_id);
        contentValues.put("time_stamp", time_stamp);
        contentValues.put("shift_id", shift_id);
        contentValues.put("stat_log", stat_log);
        contentValues.put("flag", flag);
        long i = db.insert("tbl_opr_log", null, contentValues );
        return i;
    }
    public boolean update_tbl_opr_log(String id, String flag)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("flag", flag);
        db.update("tbl_opr_log", contentValues, "id = ?", new String[]{id});
        return true;
    }
    public int delete_tbl_opr_log(String stat_log)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int del = db.delete("tbl_opr_log", "stat_log = ?", new String[]{stat_log});
        return del;
    }

}
