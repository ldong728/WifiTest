package com.gooduo.wifitest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/7/1.
 */
public class Db extends SQLiteOpenHelper {
    public final static String DB_NAME="chihiros_db";
    public final static String GROUP_TBL="GROUP_TBL";
    public final static String DEVICE_TBL="DEVICE_TBL";
    public final static String CODE_TBL="CODE_TBL";
    public final static String USER_TBL="USER_TBL";
    public final static String U_ID="U_ID";
    public final static String U_NAME="U_NAME";
    public final static String U_EMAIL="U_EMAIL";
    public final static String U_PHONE="U_PHONE";
    public final static String U_PASD="U_PASD";
    public final static String U_DEFAULT="UDEFAULT";
    public final static String G_ID="G_ID";
    public final static String G_INF="G_INF";
    public final static String G_NAME="G_NAME";
    public final static String GROUP_TYPE_LOCAL="local";
    public final static String GROUP_TYPE_ONLINE="online";
    public final static String D_MAC="D_MAC";
    public final static String D_SSID="D_SSID";
    public final static String D_IP="D_IP";
    public final static String D_TYPE="D_TYPE";
    public final static String D_NAME="D_name";
    public final static String C_ID="C_ID";
    public final static String C_TYPE="C_TYPE";
    public final static String G_TYPE="G_TYPE";
    public final static String C_CODE="C_CODE";
    public final static String TYPE_AUTO="TYPE_AUTO";
    public final static String TYPE_MANUAL="TYPE_MANUAL";
    public final static String TYPE_CLOUD="TYPE_CLOUD";
    public final static String TYPE_FLASH="TYPE_FLASH";
    public final static String TYPE_MOON="TYPE_MOON";
    private int mCurrentUserId;
    private int mCurrentGroupId;



    public Db(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.i("godlee", "db");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String str="CREATE TABLE IF NOT EXISTS USER_TBL (U_ID integer primary key autoincrement,U_NAME text,U_EMAIL text,U_PHONE text,U_PASD text,U_DEFAULT tinyint(1))";
        db.execSQL(str);
        Log.i("godlee","created");
        str="CREATE TABLE IF NOT EXISTS GROUP_TBL (G_ID integer primary key autoincrement,G_NAME text,U_ID integer,G_INF text,G_TYPE text)";
        Log.i("godlee","create");
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS DEVICE_TBL (D_MAC text primary key,D_SSID text,G_ID text,D_TYPE text,D_NAME text)";
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS CODE_TBL (C_ID integer primary key autoincrement,G_ID integer,C_TYPE text,C_CODE BLOB,UNIQUE(G_ID,C_TYPE))";
        db.execSQL(str);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("godlee", "new version" + newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.i("godlee", "dataBase Open");
        Cursor cursor=db.query(USER_TBL,new String[]{U_ID},"U_DEFAULT=?",new String[]{""+1},null,null,null,"limit 1");
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            mCurrentUserId=cursor.getInt(0);
        }else{
            mCurrentUserId=-1;
        }
    }

    public int addUser(String name,String mail,String phone,String pasd){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(U_DEFAULT,0);
        db.update(USER_TBL, cv, null, null);
        cv.clear();
        cv.put(U_NAME, name);
        cv.put(U_EMAIL, mail);
        cv.put(U_PHONE, phone);
        cv.put(U_PASD,pasd);
        cv.put(U_DEFAULT,1);
        db.insert(USER_TBL,null,cv);
        Cursor cursor = db.rawQuery("select last_insert_rowid() from "+USER_TBL,null);
        int strid=-1;
        if(cursor.moveToFirst())
            strid = cursor.getInt(0);
        cursor.close();
        db.close();
        mCurrentUserId=strid;
        return strid;
    }
    public void chooseUser(int userId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(U_DEFAULT,0);
        db.update(USER_TBL, cv, null, null);
        cv.clear();
        cv.put(U_DEFAULT, 1);
        db.update(USER_TBL, cv, "U_ID", new String[]{"" + userId});
        db.close();
        mCurrentUserId=userId;
    }
    public JSONObject[] getUserList(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor=db.query(USER_TBL,null,null,null,null,null,null);
        JSONObject[] returnData=getStringQuery(cursor);
        db.close();
        return returnData;
    }

    public int addGroup(String groupName,String type,String inf){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(G_NAME,groupName);
        cv.put(U_ID, mCurrentUserId);
        cv.put(G_TYPE, type);
        cv.put(G_INF, inf);
        db.insert(GROUP_TBL, null, cv);
        Cursor cursor = db.rawQuery("select last_insert_rowid() from "+GROUP_TBL,null);

        int strid=-1;
        if(cursor.moveToFirst())
            strid = cursor.getInt(0);
        cursor.close();
        db.close();
        return strid;
    }
    public JSONObject getGroupInf(){
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=db.query(GROUP_TBL, null, "G_ID=?,U_ID=?", new String[]{"" + mCurrentGroupId, "" + mCurrentUserId}, null, null, null);
        cursor.moveToFirst();
        JSONObject obj= getStringRow(cursor);
        cursor.close();
        db.close();
        return obj;
    }
    public JSONObject[] getGroupList(String type){
        String selection="U_ID=?";
        String[] selectionArgs=new String[]{""+mCurrentUserId};
        switch(type){
            case GROUP_TYPE_LOCAL:
                selection="U_ID=?,G_TYPE=?";
                selectionArgs=new String[]{GROUP_TYPE_LOCAL};
                break;
            case GROUP_TYPE_ONLINE:
                selection="U_ID=?,G_TYPE=?";
                selectionArgs=new String[]{GROUP_TYPE_ONLINE};
                break;
            default:
                break;
        }
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=db.query(GROUP_TBL, null, selection, selectionArgs, null, null, "G_TYPE asc");
        JSONObject[] returnData=getStringQuery(cursor);
        db.close();
        return returnData;
    }

    public void addDevice(String mac,String SSID,String type,String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(D_MAC,mac);
        cv.put(D_SSID,SSID);
        cv.put(G_ID,mCurrentGroupId);
        cv.put(G_TYPE,type);
        cv.put(G_NAME, name);
        db.replace(DEVICE_TBL, null, cv);
        db.close();
    }



    public byte[] getCode(String type){
        String selection="G_ID=?,C_TYPE=?";
        String[] selectionArg=new String[]{""+ mCurrentGroupId,type};
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=db.query(CODE_TBL, new String[]{C_CODE}, selection, selectionArg, null, null, null);
        if(cursor.getCount()>1){
            Log.e("godlee", "dataBase have Error");
        }
        if(1==cursor.getCount()){
            cursor.moveToFirst();
            byte[] code=cursor.getBlob(0);
            cursor.close();
            db.close();
            return code;
        }else{
            cursor.close();
            db.close();
            return null;
        }
    }

    public JSONObject[] getDeviceList(){
        SQLiteDatabase db=getReadableDatabase();
        String selection="G_ID=?";
        String[] selectionArg=new String[]{""+ mCurrentGroupId};
        Cursor cursor=db.query(DEVICE_TBL, null, selection, selectionArg, null, null, null);
        JSONObject[] returnData=getStringQuery(cursor);
        db.close();
        return returnData;
    }

    public int getGroupId() {
        return mCurrentGroupId;
    }

    public void setGroupId(int mCurrentId) {
        this.mCurrentGroupId = mCurrentId;
    }
    public int getUserId() {
        return this.mCurrentUserId;
    }
    public void saveCode(String type, byte[] code){
        Log.i("godlee", "replaceStart");
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(C_TYPE,type);
        cv.put(C_CODE,code);
        cv.put(G_ID, mCurrentGroupId);
        db.replace(CODE_TBL, null, cv);
        Log.i("godlee", "replace"+Tool.bytesToHexString(code));
        db.close();
    }
    private JSONObject getStringRow(Cursor cursor){
        JSONObject obj=new JSONObject();
        try{
            for(int i=0; i<cursor.getColumnCount();i++){
                obj.accumulate(cursor.getColumnName(i),cursor.getString(i));
            }
            return obj;
        }catch(JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
    private JSONObject[] getStringQuery(Cursor cursor){
        if(cursor.getCount()>0){
            JSONObject[] data=new JSONObject[cursor.getCount()];
            cursor.moveToFirst();
            int index=0;
            while(cursor.getPosition()!=cursor.getCount()){
                data[index]=getStringRow(cursor);
                cursor.moveToNext();
                index++;
            }
            cursor.close();
            return data;
        }else{
            cursor.close();
            return null;
        }
    }


    }






