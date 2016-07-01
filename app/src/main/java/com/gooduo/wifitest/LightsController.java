package com.gooduo.wifitest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2016/6/30.
 */
public class LightsController {
    public static int COLOR_NUM=7;
    public static int RY=0;
    public static int CW=1;
    public static int DR=2;
    public static int UV=3;
    public static int V=4;
    public static int B=5;
    public static int G=6;
    Light[] mLightsGroup=new Light[COLOR_NUM];



    public LightsController(){
        for(int i=0;i<7;i++){
            mLightsGroup[i]=new Light(i+1);
        }
    }

    public byte[] createCode(){

//        List sList=new ArrayList(Arrays.asList(mLightsGroup[0]));
//        for(int i=1;i<COLOR_NUM;i++){
//            sList.addAll(Arrays.asList(mLightsGroup[i]));
//        }
//        Object[] sData = sList.toArray();
//        return sData;
        return null;



    }
    public byte[] createCode(int color){
        return mLightsGroup[color].getAutoCode();
    }

    public byte[] set(int color,int time,int level){
      mLightsGroup[color].setPoint(time,level);
        return mLightsGroup[color].getAutoCode();
    }

    public byte[] unset(int color,int time){
        mLightsGroup[color].removeKey(time);
        return  mLightsGroup[color].getAutoCode();
    }

    public void displayTemp(){
        for(Light l:mLightsGroup){
            l.disply();
        }
    }

}
