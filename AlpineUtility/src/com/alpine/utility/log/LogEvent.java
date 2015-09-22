package com.alpine.utility.log;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;

public class LogEvent

{
    JSONObject logEventInfo;
    long timestamp;
    String message;
    String messagedetails;
    String userid;
    //HashMap<String, String> extravalues;
    JSONArray extravalues;

    public LogEvent()
    {
        logEventInfo = new JSONObject();
    }

    public void setTimestamp(long timestamp) {
        logEventInfo.put("log_timestamp", timestamp);
    }

    public void setMessage(String message) {
        logEventInfo.put("message_type", message);
        this.message = message;
    }

    public void setMessagedetails(String messagedetails) {
        logEventInfo.put("message_details", messagedetails);
    }

    public void setUserid(String userid) {
        logEventInfo.put("user_id", userid);
    }

    public void setCustomerId(String customerId) {
        logEventInfo.put("customer_id", customerId);
    }

    public void addExtra(String key, String value)
    {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("value",value);
        if (extravalues == null)
        {
            extravalues = new JSONArray();
        }
        extravalues.add(obj);
    }

    public JSONObject getLogEventInfo()
    {
        if (extravalues != null)
        {
            logEventInfo.put("extra_infos", extravalues);
        }
        return logEventInfo;
    }





}
