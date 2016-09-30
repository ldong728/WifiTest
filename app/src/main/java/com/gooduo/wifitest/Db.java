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
    public final static String U_DEFAULT="U_DEFAULT";
    public final static String U_SN="U_SN";
    public final static String G_ID="G_ID";
    public final static String G_INF="G_INF";
    public final static String G_NAME="G_NAME";
    public final static String G_SSID="G_SSID";
    public final static String G_SSID_PASD="G_SSID_PASD";
    public final static String GROUP_TYPE_LOCAL="local";
    public final static String GROUP_TYPE_ONLINE="online";
    public final static String D_MAC="D_MAC";
    public final static String D_SSID="D_SSID";
    public final static String D_IP="D_IP";
    public final static String D_TYPE="D_TYPE";
    public final static String D_NAME="D_NAME";
    public final static String C_ID="C_ID";
    public final static String C_TYPE="C_TYPE";
    public final static String G_TYPE="G_TYPE";
    public final static String C_CODE="C_CODE";
    public final static String TYPE_AUTO="TYPE_AUTO";
    public final static String TYPE_MANUAL="TYPE_MANUAL";
    public final static String TYPE_CLOUD="TYPE_CLOUD";
    public final static String TYPE_FLASH="TYPE_FLASH";
    public final static String TYPE_MOON="TYPE_MOON";
    public final static String SYN="SYN";
//    public WebSocketController mWsc;
    private int mCurrentUserId=-1;

    public String getmCurrentUsn() {
        return mCurrentUsn;
    }

    private String mCurrentUsn;
    private int mCurrentGroupId;




    public Db(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
//        Log.i("godlee", "db");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String str="CREATE TABLE IF NOT EXISTS USER_TBL (U_ID integer primary key autoincrement,U_NAME text,U_EMAIL text,U_PHONE text,U_PASD text,U_DEFAULT integer,U_SN text,SYN integer)";
        db.execSQL(str);
        Log.i("godlee","created");
        str="CREATE TABLE IF NOT EXISTS GROUP_TBL (G_ID integer primary key autoincrement,G_NAME text,U_ID integer,G_INF text,G_TYPE text,G_SSID text,G_SSID_PASD text,SYN integer)";
        Log.i("godlee","create");
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS DEVICE_TBL (D_MAC text primary key,D_SSID text,G_ID integer,D_TYPE text,D_NAME text,SYN integer)";
        db.execSQL(str);
        str="CREATE TABLE IF NOT EXISTS CODE_TBL (C_ID integer primary key autoincrement,G_ID integer,C_TYPE text,C_CODE BLOB,SYN integer,UNIQUE(G_ID,C_TYPE))";
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
        if(-1==mCurrentUserId){
            Log.i("godlee","not hav userInf,read from db");
            Cursor cursor=db.query(USER_TBL, new String[]{U_ID,U_SN}, U_DEFAULT + "=?", new String[]{"" + 1}, null, null, null, null);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                mCurrentUserId=cursor.getInt(0);
                mCurrentUsn=cursor.getString(1);
            }else{
                Log.i("godlee","no userInf in Db");
                mCurrentUserId=-1;
            }
            cursor.close();
        }

    }

    public int addUser(String name,String mail,String phone,String pasd){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        String sql="update "+USER_TBL+" set "+U_DEFAULT+"=0";


        db.execSQL(sql);
        cv.put(U_NAME, name);
        cv.put(U_EMAIL, mail);
        cv.put(U_PHONE, phone);
        cv.put(U_PASD,pasd);
        cv.put(U_DEFAULT,1);
        cv.put(SYN,0);
        db.insert(USER_TBL, null, cv);
        Log.i("godlee","userInf put Db ok");
        Cursor cursor = db.rawQuery("select last_insert_rowid() from "+USER_TBL,null);
        int strid=-1;
        int count=cursor.getCount();
        if(cursor.moveToFirst())
            strid = cursor.getInt(0);
        cursor.close();
        db.close();
        mCurrentUserId=strid;
        Log.i("godlee","get added UserId="+mCurrentUserId);
        return strid;
    }
    public String chooseUser(int userId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(U_DEFAULT,0);
        db.update(USER_TBL, cv, null, null);
        cv.clear();
        cv.put(U_DEFAULT, 1);
        db.update(USER_TBL, cv, U_ID, new String[]{"" + userId});
        Cursor sCursor=db.query(USER_TBL,new String[]{U_SN},U_ID,new String[]{U_ID},null,null,null);
        sCursor.moveToFirst();
        String sn=sCursor.getString(0);
        sCursor.close();
        db.close();

        mCurrentUserId=userId;
        return sn;
    }
    public void synUser(String uSn){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(U_SN, uSn);
        cv.put(SYN,1);
        db.update(USER_TBL, cv, U_ID+"=?", new String[]{"" + mCurrentUserId});
        db.close();
    }
    public String signIn(String mail,String pasd){
        String selection=U_EMAIL+"=? and "+U_PASD+"=?";
        String[] selectionArg=new String[]{mail,pasd};
        String[] userInf=getValue(USER_TBL,new String[]{U_ID},selection,selectionArg);
        if(null==userInf)return null;
        int userId=Integer.parseInt(userInf[0]);
        return chooseUser(userId);
    }
    public JSONObject[] getUserList(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor=db.query(USER_TBL, null, null, null, null, null, null);
        JSONObject[] returnData=getStringQuery(cursor);
        db.close();
        return returnData;
    }

    public int addGroup(String groupName,String inf){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(G_NAME,groupName);
        cv.put(U_ID, mCurrentUserId);
        cv.put(G_TYPE, GROUP_TYPE_LOCAL);
        cv.put(G_INF, inf);
        db.insert(GROUP_TBL, null, cv);
        Log.i("godlee","group add ok");
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
        Cursor cursor=db.query(GROUP_TBL, null, G_ID + "=? and " + U_ID + "=?", new String[]{"" + mCurrentGroupId, "" + mCurrentUserId}, null, null, null);
        cursor.moveToFirst();
        JSONObject obj= getStringRow(cursor);
        cursor.close();
        db.close();
        return obj;
    }
    public void changeGroupType(String ssid,String pasd){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(G_SSID,ssid);
        cv.put(G_SSID_PASD,pasd);
        cv.put(G_TYPE, GROUP_TYPE_ONLINE);
        db.update(GROUP_TBL, cv, G_ID + "=?", new String[]{"" + mCurrentGroupId});
        db.close();
    }
    public void mergeGroup(int targetGid){

        SQLiteDatabase db = getWritableDatabase();
        String selection="G_ID=?";
        String[] selectionArg=new String[]{""+ mCurrentGroupId};
        Cursor sCursor=db.query(DEVICE_TBL, new String[]{D_MAC}, selection, selectionArg, null, null, null);
        JSONObject[] deviceList=getStringQuery(sCursor);
        try{
            for(JSONObject obj : deviceList){
                String dMac=obj.getString(D_MAC);

            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        ContentValues cv = new ContentValues();
        cv.put(G_ID, mCurrentGroupId);

    }
    public JSONObject[] getGroupList(String type){
        String selection="U_ID=?";
        String[] selectionArgs=new String[]{""+mCurrentUserId};

        switch(type){
            case GROUP_TYPE_LOCAL:
                selection=U_ID+"=? and "+G_TYPE+"=?";
                selectionArgs=new String[]{""+mCurrentUserId,GROUP_TYPE_LOCAL};
                break;
            case GROUP_TYPE_ONLINE:
                selection=U_ID+"=? and "+G_TYPE+"=?";
                selectionArgs=new String[]{""+mCurrentUserId,GROUP_TYPE_ONLINE};
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
        cv.put(D_TYPE,type);
        cv.put(D_NAME, name);
        db.replace(DEVICE_TBL, null, cv);
        Log.i("godlee","device add to db ok,mac:"+mac+", SSID:"+SSID);
        db.close();
    }



    public byte[] getCode(String type){
        String selection="G_ID=? and C_TYPE=?";
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

    public void setCode(String type,byte[] code){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(G_ID,mCurrentGroupId);
        cv.put(C_TYPE,type);
        cv.put(C_CODE,code);
        db.replace(CODE_TBL,null,cv);
    }
    public String[] getValue(String tableName,String[] columes,String selection,String[] selectionArg){
        SQLiteDatabase db=getReadableDatabase();
        Cursor cusor=db.query(tableName,columes,selection,selectionArg,null,null,null);
        if(cusor.moveToFirst()){
            String[] value=new String[cusor.getColumnCount()];
            for(int i=0; i<cusor.getColumnCount(); i++){
                value[i]=cusor.getString(i);
            }
            cusor.close();
            db.close();
            return value;
        }else{
            cusor.close();
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
    public JSONObject getUserInf(){
        SQLiteDatabase db=getReadableDatabase();
        String selection=U_ID+"=?";
        String[] selectionArgs=new String[]{""+mCurrentUserId};
        Cursor cursor=db.query(USER_TBL,null,selection,selectionArgs,null,null,null);
        JSONObject obj=getStringRow(cursor);
        db.close();
        return obj;
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
        if(cursor.getCount()>0){
            if(cursor.isBeforeFirst())cursor.moveToFirst();
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
        }else{
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






