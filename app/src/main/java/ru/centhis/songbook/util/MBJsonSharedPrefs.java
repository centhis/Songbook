package ru.centhis.songbook.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.centhis.songbook.activities.TextSongActivity;

public class MBJsonSharedPrefs implements IPreferences{

    private static final String TAG = MBJsonSharedPrefs.class.getName();

    String filePath;
    JSONObject mJSONObject;

    public MBJsonSharedPrefs(String filePath){
        this.filePath = filePath;
        try {
            if(!MBFileUtils.fileExists(filePath)){
                MBFileUtils.createFile(filePath, "{}");
            }
            String json = MBFileUtils.readFile(filePath);
            mJSONObject = new JSONObject(json);
        } catch (Exception e){
            Log.e(TAG, "MBJsonSharedPrefs: ", e);
        }
    }

    @Override
    public boolean contains(String key) {
        if (mJSONObject == null)
            return false;
        return mJSONObject.has(key);
    }

    @Override
    public int getInt(String key, int defValue) {
        return tryParseInt(getContentByKey(key), defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        String value = getContentByKey(key);
        return value==null?defValue:value;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        String value = getContentByKey(key);
        return value==null?defValue:value.equals("t");
    }

    @Override
    public void putInt(String key, int value) {
        putContentByKey(key, String.valueOf(value));
    }

    @Override
    public void putString(String key, String value) {
        putContentByKey(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        putContentByKey(key, value?"t":"f");
    }

    @Override
    public void put(String key, int value) {
        putInt(key, value);
    }

    @Override
    public void put(String key, String value) {
        putString(key, value);
    }

    @Override
    public void put(String key, boolean value) {
        putBoolean(key, value);
    }

    public void commit(){
        if(mJSONObject == null)
            return;
        try {
            MBFileUtils.writeToFile(mJSONObject.toString(), filePath);
        } catch (IOException e){
            Log.e(TAG, "commit: ", e);
        }
    }

    public void apply(){
        MBThreadUtils.doOnBackground(new Runnable() {
            @Override
            public void run() {
                commit();
            }
        });
    }


    private int tryParseInt(String strVal, int defValue){
        if (strVal == null)
            return defValue;
        try {
            return Integer.parseInt(strVal);
        } catch (Exception e){
            return defValue;
        }
    }
    private String getContentByKey(String key){
        if (mJSONObject == null)
            return null;
        try {
            return (String)mJSONObject.get(key);
        } catch (JSONException e){
            Log.e(TAG, "getContentByKey: ", e);
            return null;
        }
    }

    private void putContentByKey(String key, String content){
        if (mJSONObject == null)
            return;
        try {
            mJSONObject.put(key, content);
        } catch (JSONException e){
            Log.e(TAG, "putContentByKey: ", e);
        }
    }
}
