package com.report.factory;

import com.appium.utils.AppiumDevice;
import com.events.TestEventBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportEventManager {

    public void getReportEventJson(AppiumDevice appiumDevice,
                                   String testStatus, String testCaseName,
                                   String testResult)
            throws JsonProcessingException {
        JSONObject obj = new JSONObject();
        JSONArray ja = new JSONArray();
        obj.put("status", testStatus);
        obj.put("testresult",testResult);
        obj.put("testcasename", testCaseName);
        String value = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(appiumDevice);
        ja.put(value);
        obj.put("deviceinfo", JSON.parse(value.replace("/\r?\n|\r/g", "")));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(obj);
        System.out.println(jsonOutput);
        TestEventBus.getInstance().publish(jsonOutput);
    }
}
