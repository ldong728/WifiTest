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
    public static String LIGHT_TBL="LIGHT_TBL";
    public static String G_ID="G_ID";
    public static String G_NAME="G_NAME";
    public static String D_MAC="D_MAC";
    public static String D_SSID="D_SSID";
    public static String D_G_ID="D_G_ID";
    public static String D_IP="D_IP";
    public static String D_TYPE="D_TYPE";
    public static String C_ID="C_ID";
    public static String C_D_ID="C_D_ID";
    public static String C_TYPE="C_TYPE";
    public static String C_CODE="C_CODE";



    public Db(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

    }
}
