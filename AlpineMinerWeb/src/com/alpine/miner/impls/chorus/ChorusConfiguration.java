package com.alpine.miner.impls.chorus;

import com.alpine.miner.impls.resource.ResourceInfo;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author robbie
 * 01/14/2013
 */

public class ChorusConfiguration extends ResourceInfo {
    private static Logger itsLogger = Logger.getLogger(ChorusConfiguration.class);


    private static final String CHORUS_HOST = "chorushost";
    private static final String CHORUS_PORT = "chorusport";
    private static final String CHORUS_AKEY = "chorus_apikey";

    private String host = "";
    private String port = "";
    private String apiKey;

    public ChorusConfiguration() {}

    public ChorusConfiguration(Map<String, String> props) {
        if (props == null) {return;}

        this.setHost((String) props.get(CHORUS_HOST));
        this.setPort((String) props.get(CHORUS_PORT));

        if (props.get(CHORUS_AKEY) == null) {
            this.setApiKey(UUID.randomUUID().toString());
        } else {
            this.setApiKey((String) props.get(CHORUS_AKEY));
        }
    }

    public Map<String, String> returnProps() {
        Map<String,String> props = new HashMap<String,String>();

        props.put(CHORUS_HOST, this.getHost());
        props.put(CHORUS_PORT, this.getPort());
        if (this.getApiKey() != null) {
            props.put(CHORUS_AKEY, this.getApiKey());
        }

        return props;
    }

    public String getURLBase() {
        StringBuilder sb = new StringBuilder(this.getHost());
        sb.append(":").append(this.getPort());
        return sb.toString();
    }

    public String getURLBase(String suffix) {
        StringBuilder sb = new StringBuilder(getURLBase());
        sb.append(suffix);
        return sb.toString();
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
