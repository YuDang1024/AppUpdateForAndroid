package com.appupdata.updata.DownLoadBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DownLoadBean implements Serializable {
    public String title;
    public String content;
    public String url;
    public String md5;
    public String versionCode;
    public String updateLevel;

    public static String getJson(String jsonStr) {
        if (jsonStr != null && jsonStr.startsWith("\ufeff")) {
            jsonStr = jsonStr.substring(jsonStr.indexOf("{"),
                    jsonStr.lastIndexOf("}") + 1);
        }
        return jsonStr;
    }

    public static DownLoadBean parse(String respone)
    {
        try {
            respone = getJson(respone);
            JSONObject repJson = new JSONObject(respone);
            String title = repJson.optString("title");
            String content = repJson.optString("content");
            String url = repJson.optString("url");
            String md5 = repJson.optString("md5");
            String versionCode = repJson.optString("versionCode");
            // HIGH MEDIUM LOW
            String updateLevel = repJson.optString("updateLevel");

            DownLoadBean bean = new DownLoadBean();
            bean.title = title;
            bean.content = content;
            bean.url = url;
            bean.md5 = md5;
            bean.versionCode = versionCode;
            bean.updateLevel = updateLevel;

            return bean;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
