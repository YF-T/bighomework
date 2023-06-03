package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 本类为单例类，其作用为存储所有的全局变量
 * 同时，这些全局变量也将做到永久保存
 * 请注意，这些全局变脸只能是int,boolean,String三种类型，其他类型请转为这三种基本类型
 * 程序开头的注释中写了目前已经存储的变量，后续有新的变量需要存储的话请在注释中补上
 * 使用方法：
 * 设置变量：在任意类/函数中调用 GlobalVariable.set(String key, String/int/boolean value);
 * 获取变量：在任意类/函数中调用 GlobalVariable.get(String key, String/int/boolean default);
 */
public class GlobalVariable {
    /**
     * 本程序用到的变量有:
     * username: String, 用户名
     * iflogin: Boolean, 用户是否登录
     * useremail: String, 用户邮箱
     * jwt: String, 用户登录令牌
     * userimageurl: String, 用户头像的url
     * baseurl: String，服务器的IP
     */

    private static GlobalVariable mInstance = new GlobalVariable();//程序启动时立即创建单例
    private SharedPreferences mPreferences;
    private Context context;
    private String sharedPrefFile ="com.example.android.globalvariable";
    static public String defaultImage = "/image/user/abc.jpg";

    private GlobalVariable() {//构造函数私有
    }

    public static GlobalVariable getInstance() {//唯一的访问入口
        return mInstance;
    }

    public void setContext(Context context) {
        mPreferences = context.getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        this.context = context;
    }

    public static SharedPreferences getPreferences() {
        return mInstance.mPreferences;
    }

    public static void set(String key, int value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void set(String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void set(String key, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean get(String key, boolean defValue) {
        return getPreferences().getBoolean(key, defValue);
    }

    public static String get(String key, String defValue) {
        return getPreferences().getString(key, defValue);
    }

    public static int get(String key, int defValue) {
        return getPreferences().getInt(key, defValue);
    }
}
