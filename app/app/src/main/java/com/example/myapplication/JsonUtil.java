package com.example.myapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class JsonUtil {

    public static ArrayList<Object> jsonArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object item = jsonArray.get(i);
            if (item instanceof JSONObject) {
                item = jsonObjectToHashMap((JSONObject) item);
            } else if (item instanceof JSONArray) {
                item = jsonArrayToArrayList((JSONArray) item);
            }
            arrayList.add(item);
        }
        return arrayList;
    }

    public static HashMap<String, Object> jsonObjectToHashMap(JSONObject jsonObject) throws JSONException {
        HashMap<String, Object> hashMap = new HashMap<>();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                value = jsonObjectToHashMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToArrayList((JSONArray) value);
            }
            hashMap.put(key, value);
        }
        return hashMap;
    }

    public static JSONArray arrayListToJsonArray(ArrayList<Object> arrayList) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Object item : arrayList) {
            if (item instanceof HashMap) {
                item = hashMapToJsonObject((HashMap<String, Object>) item);
            } else if (item instanceof ArrayList) {
                item = arrayListToJsonArray((ArrayList<Object>) item);
            }
            jsonArray.put(item);
        }
        return jsonArray;
    }

    public static JSONArray stringArrayListToJsonArray(ArrayList<String> arrayList) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Object item : arrayList) {
            jsonArray.put(item);
        }
        return jsonArray;
    }

    public static JSONObject hashMapToJsonObject(HashMap<String, Object> hashMap) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (String key : hashMap.keySet()) {
            Object value = hashMap.get(key);
            if (value instanceof HashMap) {
                value = hashMapToJsonObject((HashMap<String, Object>) value);
            } else if (value instanceof ArrayList) {
                value = arrayListToJsonArray((ArrayList<Object>) value);
            }
            jsonObject.put(key, value);
        }
        return jsonObject;
    }
}
