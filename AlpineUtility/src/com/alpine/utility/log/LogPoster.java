package com.alpine.utility.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;

public class LogPoster {
    private static LogPoster instance;
    private static final long period = 5*60*1000;  //every five minutes
    //private static final long period = 20*1000;   //every 20 sec, for debug
    private static final String AlpineKey = "sguYLzM3CpulFLo3dNVO";
    private static final String AlpineLoggerURLString = "https://alpinelogger.herokuapp.com/message/log";

    private static final int THRESHOLD = 1000;

    public static final String WORKFLOW_RUN_MANUAL = "Workflow Run by manual";
    public static final String WORKFLOW_RUN_SCHEDULE = "Workflow Run by schedule";
    public static final String WORKFLOW_RUN_COMMAND = "Workflow Run by command line";
    public static final String OPERATOR_EXPLORATION = "Operator exploration";
    public static final String Flow_Create = "Flow Create";
    public static final String User_Create = "User Create";
    public static final String Datasource_Create = "Datasource Create";
    public static final String Operator_Execute = "Operator Execute";

    public static final String Operator_Type = "Operator Type";
    public static final String Operator_Exec_Time = "Operator Execute Length";


    public static LogPoster getInstance() {
        if (instance == null)
            instance = new LogPoster();
        return instance;
    }

    private ArrayList<LogEvent> events;
    private Timer timer;
    HttpAsyncClient client;
    private String customerid;

    private boolean active = false;

    private LogPoster() {
        events = new ArrayList<LogEvent>();
    }

    public void startup(String customerid, String optoutinfo)
    {
        this.customerid = customerid;
        this.active = !Boolean.parseBoolean(optoutinfo);


        if (active)
        {
            //System.out.println("starting the log poster...................................");
            startClient();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    sendOffEvents();
                }
            }, 0, period);  //every five minutes
        }   else
        {
            //System.out.println("Customer has opted out - do not start");
        }

    }


    private void startClient()
    {
        try
        {
            client = new DefaultHttpAsyncClient();
        }    catch (Exception e)
        {
            e.printStackTrace();
            client = null;
        }

        if (client != null)
        {
            client.start();
        }

    }

    public LogEvent createEvent(String messageType, String messageDetails, String userId)
    {
        LogEvent e = new LogEvent();
        e.setMessage(messageType);
        if (messageDetails != null) e.setMessagedetails(messageDetails);
        if (userId != null) e.setUserid(userId);
        e.setTimestamp(System.currentTimeMillis());

        return e;
    }

    public synchronized void createAndAddEvent(String messageType,String userId)
    {
        createAndAddEvent(messageType, (String) null, userId);
    }

    public synchronized void createAndAddEvent(String messageType, String[] messageDetails, String userId)
    {
        if (!active) return;
        if (messageDetails == null || messageDetails.length == 0)
        {
            createAndAddEvent(messageType, userId);
            return;
        }
        if (messageDetails.length == 1)
        {
            createAndAddEvent(messageType, messageDetails[0], userId);
            return;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < messageDetails.length; i++) {
            result.append( messageDetails[i] );
            result.append( "," );
        }
        createAndAddEvent(messageType, result.toString(), userId);
    }
    public synchronized void createAndAddEvent(String messageType, String messageDetails, String userId)
    {
        if (!active) return;
        LogEvent e = new LogEvent();
        e.setMessage(messageType);
        if (messageDetails != null) e.setMessagedetails(messageDetails);
        if (userId != null) e.setUserid(userId);
        e.setTimestamp(System.currentTimeMillis());
        addEvent(e);
    }

    public synchronized void addEvent(LogEvent event) {
        if (!active) return;
        event.setCustomerId(this.customerid);
        if (events.size() < THRESHOLD)
            events.add(0,event);
    }

    public synchronized void sendOffEvents() {
        if (!active) return;
        int size = events.size();
        if (size == 0) {
            return;
        }

        JSONArray j = new JSONArray();
        for (int i = size - 1; i >= 0; i--) {
            LogEvent event = events.remove(i);
           j.add(event.getLogEventInfo());
        }

        try
        {
        sendJsonToServer(j);
        } catch (Exception e)
        {
            //e.printStackTrace();
        }


    }
    
    // this is for command line.
    public void sendEvent(LogEvent event){
    	JSONArray jsonArray = new JSONArray();
    	 event.setCustomerId(this.customerid);
    	 jsonArray.add(event.getLogEventInfo());
    	try {
            //System.out.println("will send to server: " + jsonArray.toString());
            JSONObject objToSend = new JSONObject();
            objToSend.put("alpine_key", AlpineKey);
            objToSend.put("logs", jsonArray);
            if (client == null) startClient();
            if (client == null)
            {
               // System.out.println("starting http client failed... aborting send");
                return;
            }
            try {
                HttpPost httpPost = new HttpPost(AlpineLoggerURLString);
                httpPost.setEntity(new StringEntity(objToSend.toString()));
                httpPost.addHeader("Content-Type", "application/json");
                httpPost.addHeader("Accept", "application/json");
                final List<String> exitCount = new ArrayList<String>();
                client.execute(httpPost,new FutureCallback<HttpResponse>() {
    				
    				@Override
    				public void failed(Exception arg0) {
    					exitCount.add("");
    				}
    				
    				@Override
    				public void completed(HttpResponse arg0) {
    					exitCount.add("");
    				}
    				
    				@Override
    				public void cancelled() {
    					exitCount.add("");
    				}
    			});
                while(exitCount.size() == 0){
                	Thread.sleep(1000);
                }

            } catch (Exception e) {
            	System.out.println(e);
                //System.out.println("oh well!");
                //e.printStackTrace();
            }
		} catch (Exception e) {
			
		}
    }

    /**
     * Sends log messages to our server
     * Note that if this fails, we don't do anything - the log messages are lost, but that's okay
     * @param jsonArray
     * @throws Exception
     */
    private void sendJsonToServer(JSONArray jsonArray) throws Exception{
        if (jsonArray == null) return;
        //System.out.println("will send to server: " + jsonArray.toString());
        JSONObject objToSend = new JSONObject();
        objToSend.put("alpine_key", AlpineKey);
        objToSend.put("logs", jsonArray);
        if (client == null) startClient();
        if (client == null)
        {
           // System.out.println("starting http client failed... aborting send");
            return;
        }
        try {
            HttpPost httpPost = new HttpPost(AlpineLoggerURLString);
            httpPost.setEntity(new StringEntity(objToSend.toString()));
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept", "application/json");
            client.execute(httpPost,null);
//            System.out.println(objToSend.toString());

        } catch (Exception e) {
//        	System.out.println(e);
            //System.out.println("oh well!");
            //e.printStackTrace();
        }
    }

    public void close() {
       // System.out.println("closing...");
        if (timer != null) timer.cancel();
        try
        {
            if (client != null) client.shutdown();
        } catch (Exception e)
        {
            //e.printStackTrace();
        }
        instance = null;
    }

}
