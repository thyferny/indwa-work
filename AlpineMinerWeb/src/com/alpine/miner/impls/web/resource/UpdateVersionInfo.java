package com.alpine.miner.impls.web.resource;


/**
 * ClassName: UpdateVersionInfo
 * <p/>
 * Data: 12-12-11
 * <p/>
 * Author: Will
 * <p/>
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 */
public class UpdateVersionInfo {

    private String name;
    private String version;
    private String pubDate;

    public String getDescrip() {
        return descrip;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    private String descrip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UpdateVersionInfo(){}
    public UpdateVersionInfo(String name,String version,String pubDate,String descrip){
        this.name = name;
        this.version = version;
        this.pubDate = pubDate;
        this.descrip = descrip;
    }
}
