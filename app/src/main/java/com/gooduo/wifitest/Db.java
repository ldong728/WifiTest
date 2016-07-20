package com.gooduo.wifitest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/7/1.
 */
public class Db extends SQLiteOpenHelper {
    public static String DB_NAME="chihiros_db";
    public static String GROUP_TBL="GROUP_TBL";
    public static String DEVICE_TBL="DEVICE_TBL";
    public static String CODE_TBL="CODE_TBL";
    public static String USER_TBL="USER_TBL";
    public static String U_ID="U_ID";
    public static String U_EMAIL="U_EMAIL";
    public static String U_PHONE="U_PHONE";
    public static String G_ID="G_ID";
    public static String G_NAME="G_NAME";
    public static String D_MAC="D_MAC";
    public static String D_SSID="D_SSID";
    public static String D_IP="D_IP";
    public static String D_TYPE="D_TYPE";
    public static String C_ID="C_ID";
    public static String C_D_ID="C_D_ID";
    public static String C_TYPE="C_TYPE";
    public static String C_CODE="C_CODE";
    public static String TYPE_AUTO="TYPE_AUTO";
    public static String TYPE_MANUAL="TYPE_MANUAL";
    public static String TYPE_CLOUD="TYPE_CLOUD";
    public static String TYPE_FLASH="TYPE_FLASH";
    public static String TYPE_MOON="TYPE_MOON";

    private int mCurrentId;



    public Db(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String str="CREATE TABLE IF NOT EXISTS USER_TBL U_ID integer primary ky autoincrement,U_EMAIL text,U_PHONE text";
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS GROUP_TBL G_ID integer primary key autoincrement,G_NAME text,U_ID integer";
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS DEVICE_TBL D_MAC text primary ,D_SSID text,G_ID text,D_IP text,D_TYPE text";
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS CODE_TBL C_ID integer primary key autoincrement,C_CODE BLOB,C_TYPE text";
        db.execSQL(str);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

    }


}
