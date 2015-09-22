/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: DataSourceCreateUIHelper
 * Author: Will
 * Date: 13-1-21
 */

define([
    "dojo/dom-attr",
    "dojo/dom-style",
    "dojo/dom-class",
    "dojo/dom",
    "dojo/query",
    "dojo/on",
    "dojo/ready",
    "dijit/registry",
    "dojo/string",
    "dojo/_base/array",
    "alpine/datasourceexplorer/DataSourceExplorerManager",
    "alpine/datasourcemgr/DataSourceHadoopManager",
    "alpine/datasourcemgr/DataSourceDBManager"
],function(domAttr,domStyle,domClass,dom,query,on,ready,registry,strUtil,array,dataSourceMger,dsHDManager,dsDBManager){

   var constants = {
       //for toolbar button create connect
       CREATE_BUTTON: "alpine_datasourceexplorer_create_connection_btn",
       TOOLBAR_DIALOG:"alpine_create_datasource_4toolbutton_Dialog",
       TOOLBAR_DLG_BTN_CANCEL:"alpine_create_datasource_4toolbutton_button_cancel",
       TOOLBAR_DLG_BTN_OK:"alpine_create_datasource_4toolbutton_button_save",
       TOOLBAR_DLG_BTN_TEST:"alpine_create_datasource_4toolbutton_button_test",
       TOOLBAR_DLG_DB_FORM:"alpine_create_datasource_4toolbutton_db_form",
       TOOLBAR_DLG_HD_FORM:"alpine_create_datasource_4toolbutton_hadoop_form",
       TOOLBAR_CONTENT_SWITCHER:"alpine_create_datasource_4toolbutton_contentSwitcher",
       TOOLBAR_DB_CONTAINER:"alpine_create_datasource_4toolbutton_db_content",
       TOOLBAR_HADOOP_CONTAINER:"alpine_create_datasource_4toolbutton_hadoop_content",
       TOOLBAR_DATA_SOURCE_CHANGE:"alpine_create_datasource_4toolbutton_datasourceType",
       TOOLBAR_MAIN_CONTAINER:"alpine_create_datasource_4toolbutton_mainContainer",
       TOOLBAR_CONNECT_NAME:"alpine_create_datasource_4toolbutton_connection_name", //connnection name

       TOOLBAR_DATA_DB_TYPE:"alpine_create_datasource_4toolbutton_dbType", //db type
       TOOLBAR_DATA_DB_SSL:"alpine_create_datasource_4toolbutton_db_usessl", //ssl
       TOOLBAR_DBCONNECT_HOST_NAME:"alpine_create_datasource_4toolbutton_db_host", //DB HOST
       TOOLBAR_DBCONNECT_HOST_PORT:"alpine_create_datasource_4toolbutton_db_port", //port
       TOOLBAR_DBCONNECT_DBName:"alpine_create_datasource_4toolbutton_db_name",//db name
       TOOLBAR_DBCONNECT_USER_NAME:"alpine_create_datasource_4toolbutton_db_username",//db username
       TOOLBAR_DBCONNECT_PASSWORD:"alpine_create_datasource_4toolbutton_db_password",//db password

       TOOLBAR_HDCONNECT_HDFS_Host:"alpine_create_datasource_4toolbutton_hadoop_hdfs_host",
       TOOLBAR_HDCONNECT_HDFS_Port:"alpine_create_datasource_4toolbutton_hadoop_hdfs_port",
       TOOLBAR_HDCONNECT_Job_Host:"alpine_create_datasource_4toolbutton_hadoop_job_host",
       TOOLBAR_HDCONNECT_Job_Port:"alpine_create_datasource_4toolbutton_hadoop_job_port",
       TOOLBAR_HDCONNECT_User_Name:"alpine_create_datasource_4toolbutton_hadoop_userName",
       TOOLBAR_HDCONNECT_Group_Name:"alpine_create_datasource_4toolbutton_hadoop_groupName",
       TOOLBAR_SECRITY_CHANGE:"alpine_create_datasource_4toolbutton_hadoop_securityMode",//
       TOOLBAR_HADOOP_VERSION:"toolbutton_current_hadoop_version", //hadoop version hidden
       TOOLBAR_HADOOP_VERSION_SELECT:"alpine_create_datasource_4toolbutton_hadoop_version",

       //hadoop security
       TOOLBAR_HDFS_Principal:"alpine_create_datasource_4toolbutton_hadoop_hdfsPrincipal",
       TOOLBAR_HDFS_Keytab:"alpine_create_datasource_4toolbutton_hadoop_hdfskeytab",
       TOOLBAR_Mapred_Principal:"alpine_create_datasource_4toolbutton_hadoop_mapredPrincipal",
       TOOLBAR_Mapred_Keytab:"alpine_create_datasource_4toolbutton_hadoop_mapredkeytab"
   };

    //toolbutton dataSourceChangeHandler
    var dataSourceChangeHandler = null;
    var dbTypeSelectHandler = null;
    var windowResizeHandler = null;
    var securityChangeHandler = null;
    //
    var allConnectionNames = [];

    ready(function(){
        on(registry.byId(constants.CREATE_BUTTON),"click",function(){
            _showCreateConnectDlg4ToolBtn();
        });
        on(registry.byId(constants.TOOLBAR_DLG_BTN_CANCEL),"click",_hideCreateConnectDlg4ToolBtn);
        on(registry.byId(constants.TOOLBAR_DLG_BTN_TEST),"click",_testConnection);
        on(registry.byId(constants.TOOLBAR_DLG_BTN_OK),"click",_saveConnectionConfig);
    });

    function _buildAllConnectionInfo(){
        dataSourceMger.getAvailableConnections(function(data){
            if(data!=null){
               for(var i=0;i<data.length;i++){
                   allConnectionNames.push(data[i].label);
               }
            }
        });
    }

    //create connect for tool button begin
    function _showCreateConnectDlg4ToolBtn(){
        var dlg = registry.byId(constants.TOOLBAR_DIALOG);
        if(null!=dlg){
            _buildAllConnectionInfo();
            dlg.titleBar.style.display = "none";
            //regist platform select
            if(dataSourceChangeHandler==null){
                dataSourceChangeHandler = on(registry.byId(constants.TOOLBAR_DATA_SOURCE_CHANGE),"change",function(){
                    changeCreateConnectionType(this.get("value"));
                });
            }
            if(windowResizeHandler==null){
                windowResizeHandler = on(window,"resize",function(){
                    changeCreateConnectionType(registry.byId(constants.TOOLBAR_DATA_SOURCE_CHANGE).get("value"));
                });
            }
            if(securityChangeHandler==null && _isSecurityMode()==true){
                securityChangeHandler = on(registry.byId(constants.TOOLBAR_SECRITY_CHANGE),"change",function(){
                    _changeSecurityInputStatus(this.get("value"));
                });
            }
            dlg.show();
            //ssl status
            initSSLStatus();
            changeCreateConnectionType(registry.byId(constants.TOOLBAR_DATA_SOURCE_CHANGE).get("value"));
            if(_isSecurityMode()==true){
                _changeSecurityInputStatus(registry.byId(constants.TOOLBAR_SECRITY_CHANGE).get("value"));
            }




        }
    }

    function initSSLStatus(){
        var dbTypeSelect = registry.byId(constants.TOOLBAR_DATA_DB_TYPE);
        var dbSSL = registry.byId(constants.TOOLBAR_DATA_DB_SSL);
        if(null!=dbTypeSelect && dbTypeSelect.get("value")=="Greenplum"){
            dbSSL.set("disabled",false);
        }else{
            dbSSL.set("disabled",true);
        }
        if(dbTypeSelectHandler==null){
            dbTypeSelectHandler = on(registry.byId(constants.TOOLBAR_DATA_DB_TYPE),"change",function(){
                if(this.get("value")=="Greenplum"){
                    dbSSL.set("disabled",false);
                }else{
                    dbSSL.set("disabled",true);
                }
            });
        }
    }

    function _hideCreateConnectDlg4ToolBtn(){
        var dlg = dijit.byId(constants.TOOLBAR_DIALOG);
        if(null!=dlg){
            _resetFormStatus();
            destroyEventHandler();
            allConnectionNames = [];
            dlg.hide();
        }
    }

    function destroyEventHandler(){
        if(dataSourceChangeHandler!=null){
            dataSourceChangeHandler.remove();
            dataSourceChangeHandler=null;
        }
        if(dbTypeSelectHandler!=null){
            dbTypeSelectHandler.remove();
            dbTypeSelectHandler = null;
        }
        if(windowResizeHandler!=null){
            windowResizeHandler.remove();
            windowResizeHandler = null;
        }

        if(securityChangeHandler!=null){
            securityChangeHandler.remove();
            securityChangeHandler = null;
        }
    }

    function _resetFormStatus() {
        var dbFrom = dijit.byId(constants.TOOLBAR_DLG_DB_FORM);
        var hdFrom = dijit.byId(constants.TOOLBAR_DLG_HD_FORM);
        if (dbFrom != null) {
            dbFrom.reset();
        }
        if (hdFrom != null) {
            hdFrom.reset();
        }

    }
    // type:HADOOP/DATABASE
    function changeCreateConnectionType(type){
        var stackContainer = dijit.byId(constants.TOOLBAR_CONTENT_SWITCHER);
        registry.byId(constants.TOOLBAR_CONNECT_NAME).set("value","");
        switch (type.toUpperCase()){
            case "HADOOP":
                stackContainer.selectChild(constants.TOOLBAR_HADOOP_CONTAINER);
                dijit.byId(constants.TOOLBAR_DLG_HD_FORM).reset();
                //Apache Hadoop 1.0.2 or Apache Hadoop 1.0.4
                if(_isSecurityMode()==true){
                    _resizeDialog("security");
                }else{
                    _resizeDialog("normal");
                }
                break;
            case "DATABASE":
                stackContainer.selectChild(constants.TOOLBAR_DB_CONTAINER);
                dijit.byId(constants.TOOLBAR_DLG_DB_FORM).reset();
                _resizeDialog("normal");
                break;
        }
    }

    function _resizeDialog(type){
        var mainContainer = registry.byId(constants.TOOLBAR_MAIN_CONTAINER);
        if("security"==type){
            mainContainer.resize({
                h:450,
                w:350
            });
        }else{
            mainContainer.resize({
                h:310,
                w:350
            });
        }
    }

   function _isSecurityMode(){
       var versionValue = dom.byId(constants.TOOLBAR_HADOOP_VERSION).value;
       if(versionValue=="Apache Hadoop 1.0.2" || versionValue=="Apache Hadoop 1.0.4"){
           return true;
       }else{
           return false;
       }
   }

   function _changeSecurityInputStatus(securityValue){
       if(securityValue=="kerberos"){
           registry.byId(constants.TOOLBAR_HDFS_Principal).set("disabled",false);
           registry.byId(constants.TOOLBAR_HDFS_Keytab).set("disabled",false);
           registry.byId(constants.TOOLBAR_Mapred_Principal).set("disabled",false);
           registry.byId(constants.TOOLBAR_Mapred_Keytab).set("disabled",false);
       }else{
           registry.byId(constants.TOOLBAR_HDFS_Principal).set("disabled",true);
           registry.byId(constants.TOOLBAR_HDFS_Keytab).set("disabled",true);
           registry.byId(constants.TOOLBAR_Mapred_Principal).set("disabled",true);
           registry.byId(constants.TOOLBAR_Mapred_Keytab).set("disabled",true);
       }
   }

   function _testConnection(){
       var connType = registry.byId(constants.TOOLBAR_DATA_SOURCE_CHANGE);
       if(_validateForm(connType.get("value"))==false){
           return false;
       }
       if(connType.get("value").toUpperCase()=="HADOOP"){
           var resourceInfo = {
               connection: buildHDConfigInfo()
           };
           dsHDManager.testConnectionForConfig(resourceInfo, function(){
               popupComponent.alert(alpine.nls.MSG_TEST_Connenction_OK);
           }, function(msg){
               if(msg){
                   msg = alpine.nls.MSG_TEST_Connenction_Error + msg;
               }else{
                   msg = alpine.nls.MSG_TEST_Connection_Failure;
               }
               popupComponent.alert(msg);
           }, constants.TOOLBAR_HADOOP_CONTAINER);
       }else{
           var resourceInfo = {
               connection: buildDBConfigInfo()
           };
           dsDBManager.testConnectionForConfig(resourceInfo, function(){
               popupComponent.alert(alpine.nls.MSG_TEST_Connenction_OK);
           }, function(msg){
               if(msg){
                   msg = alpine.nls.MSG_TEST_Connenction_Error + msg;
               }else{
                   msg = alpine.nls.MSG_TEST_Connection_Failure;
               }
               popupComponent.alert(msg);
           }, constants.TOOLBAR_DB_CONTAINER);
       }
   }

   function _saveConnectionConfig(){
       var connType = registry.byId(constants.TOOLBAR_DATA_SOURCE_CHANGE);
       if(_validateForm(connType.get("value"))==false){
           return false;
       }
       var connectName = registry.byId(constants.TOOLBAR_CONNECT_NAME).get("value");
       //save
       var saveHandler = null;
       var configInfo = null;
       var connResourceInfo = {};

       if(connType.get("value").toUpperCase()=="HADOOP"){
           saveHandler = dsHDManager;
           configInfo = buildHDConfigInfo();
           configInfo.connName = connectName;
           connResourceInfo.id = connectName;
           connResourceInfo.type="Personal";
           connResourceInfo.connection=configInfo;
           connResourceInfo.groupName="";
           connResourceInfo.createUser = login;
       }else{
           saveHandler = dsDBManager;
           configInfo = buildDBConfigInfo();
           configInfo.connName = connectName;
           connResourceInfo.id = connectName;
           connResourceInfo.type ="Personal";
           connResourceInfo.connection=configInfo;
           connResourceInfo.groupName="";
           connResourceInfo.createUser = login;
       }
       saveHandler.saveConnectionConfig(connResourceInfo, function(data){
           if(data.error_code){
               popupComponent.alert(data.message);
           }else{
               registry.byId(constants.TOOLBAR_DIALOG).hide();
               alpine.datasourceexplorer.DataSourceExplorerUIHelper.refreshCurrentLevel();
           }
       }, constants.EDITOR_DIALOG_ID);
   }

    function _validateForm(connectType/*Hoadoop or database*/){
       switch (connectType.toUpperCase()){
           case "HADOOP":
              return _validateHadoopConn();
           case "DATABASE":
               return _validateDBConn();
       }
    }

    function _validateHadoopConn(){
        var hdconnectName = registry.byId(constants.TOOLBAR_CONNECT_NAME);
        var hdfsHost = registry.byId(constants.TOOLBAR_HDCONNECT_HDFS_Host);
        var hdfsPort = registry.byId(constants.TOOLBAR_HDCONNECT_HDFS_Port);
        var jobHost = registry.byId(constants.TOOLBAR_HDCONNECT_Job_Host);
        var jobPort = registry.byId(constants.TOOLBAR_HDCONNECT_Job_Port);
        //var version = registry.byId(constants.TOOLBAR_HADOOP_VERSION);
        var securityMode = registry.byId(constants.TOOLBAR_SECRITY_CHANGE);
        var hdfsPrincipal = registry.byId(constants.TOOLBAR_HDFS_Principal);
        var hdfsKeytab = registry.byId(constants.TOOLBAR_HDFS_Keytab);
        var mapreducePrincipal = registry.byId(constants.TOOLBAR_Mapred_Principal);
        var mapreduceKeytab = registry.byId(constants.TOOLBAR_Mapred_Keytab);
        var hd_username = registry.byId(constants.TOOLBAR_HDCONNECT_User_Name);
        var hd_groupName = registry.byId(constants.TOOLBAR_HDCONNECT_Group_Name);
        if(strUtil.trim(hdconnectName.get("value"))==""){
            popupComponent.alert("Please input Connection Name");
            return false;
        }
        //duplicate conn name
        if(isDuplicateName(hdconnectName.get("value"))==true){
            popupComponent.alert("Connection Name is already exists.");
            return false;
        }
        //host host port
        if(strUtil.trim(hdfsHost.get("value"))==""){
            popupComponent.alert("Please input hadoop Host");
            return false;
        }
        if(strUtil.trim(hdfsPort.get("value"))==""){
            popupComponent.alert("Please input hadoop Port");
            return false;
        }
        if(/\d+/.test(hdfsPort.get("value"))==false){
            popupComponent.alert("Please input right hadoop Port value");
            return false
        }
        //job host job port
        if(strUtil.trim(jobHost.get("value"))==""){
            popupComponent.alert("Please input job host");
            return false;
        }
        if(strUtil.trim(jobPort.get("value"))==""){
            popupComponent.alert("Please input job port");
            return false;
        }
        if(/\d+/.test(jobPort.get("value"))==false){
            popupComponent.alert("Please input right job port value");
            return false
        }
        if("kerberos"==securityMode.get("value")){
            if(strUtil.trim(hdfsPrincipal.get("value"))==""){
                popupComponent.alert("Please input HDFS principal");
                return false;
            }
            if(strUtil.trim(hdfsKeytab.get("value"))==""){
                popupComponent.alert("Please input HDFS keytab");
                return false;
            }
            if(strUtil.trim(mapreducePrincipal.get("value"))==""){
                popupComponent.alert("Please input Mapred principal");
                return false;
            }
            if(strUtil.trim(mapreduceKeytab.get("value"))==""){
                popupComponent.alert("Please input Mapred keytab");
                return false;
            }
        }
        if(strUtil.trim(hd_username.get("value"))==""){
            popupComponent.alert("Please input User Name");
            return false;
        }
        if(strUtil.trim(hd_groupName.get("value"))==""){
            popupComponent.alert("Please input Hadoop Group Name");
            return false;
        }

    }

    function _validateDBConn(){
        var dbFrom = registry.byId(constants.TOOLBAR_DLG_DB_FORM);
        var connectName = registry.byId(constants.TOOLBAR_CONNECT_NAME);
        var dbEngine = registry.byId(constants.TOOLBAR_DATA_DB_TYPE);
        var dbHost = registry.byId(constants.TOOLBAR_DBCONNECT_HOST_NAME);
        var dbPort = registry.byId(constants.TOOLBAR_DBCONNECT_HOST_PORT);
        var dbName = registry.byId(constants.TOOLBAR_DBCONNECT_DBName);
        var username = registry.byId(constants.TOOLBAR_DBCONNECT_USER_NAME);
        var passWord = registry.byId(constants.TOOLBAR_DBCONNECT_PASSWORD);
        var useSSL = registry.byId(constants.TOOLBAR_DATA_DB_SSL);
        if(strUtil.trim(connectName.get("value"))==""){
            popupComponent.alert("Please input Connection Name");
            return false;
        }
        //duplicate conn name
        if(isDuplicateName(connectName.get("value"))==true){
            popupComponent.alert("Connection Name is already exists.");
            return false;
        }
        if(strUtil.trim(dbHost.get("value"))==""){
            popupComponent.alert("Please input database Host");
            return false;
        }
        if(strUtil.trim(dbPort.get("value"))==""){
            popupComponent.alert("Please input database Port");
            return false;
        }
        if(/\d+/.test(dbPort.get("value"))==false){
            popupComponent.alert("Please input right port value");
            return false
        }
        if(strUtil.trim(dbName.get("value"))==""){
            popupComponent.alert("Please input database name");
            return false;
        }
        if(strUtil.trim(username.get("value"))==""){
            popupComponent.alert("Please input database user name");
            return false;
        }
        if(strUtil.trim(passWord.get("value"))==""){
            popupComponent.alert("Please input database password");
            return false;
        }

    }

    function isDuplicateName(connName){
      if(allConnectionNames!=null && array.indexOf(allConnectionNames,connName)!=-1){
          return true;
      }
      return false;
    }

    function buildDBConfigInfo(){
        return {
            dbType:  registry.byId(constants.TOOLBAR_DATA_DB_TYPE).get("value"),
            hostname: registry.byId(constants.TOOLBAR_DBCONNECT_HOST_NAME).get("value"),
            port: registry.byId(constants.TOOLBAR_DBCONNECT_HOST_PORT).get("value"),
            dbname: registry.byId(constants.TOOLBAR_DBCONNECT_DBName).get("value"),
            dbuser: registry.byId(constants.TOOLBAR_DBCONNECT_USER_NAME).get("value"),
            password: registry.byId(constants.TOOLBAR_DBCONNECT_PASSWORD).get("value"),
            useSSL: registry.byId(constants.TOOLBAR_DATA_DB_SSL).get("checked") + "",
            jdbcDriverFileName: "temporaryDriver.jar"//just for temporary, it could be changed until adding custom jdbcDriver for each configuration.
        };
    }

    function buildHDConfigInfo(){
        return {
            hdfsHostName: registry.byId(constants.TOOLBAR_HDCONNECT_HDFS_Host).get("value"),
            hdfsPort:registry.byId(constants.TOOLBAR_HDCONNECT_HDFS_Port).get("value"),
            jobHostName: registry.byId(constants.TOOLBAR_HDCONNECT_Job_Host).get("value"),
            jobPort: registry.byId(constants.TOOLBAR_HDCONNECT_Job_Port).get("value"),
            userName: registry.byId(constants.TOOLBAR_HDCONNECT_User_Name).get("value"),
            version: registry.byId(constants.TOOLBAR_HADOOP_VERSION_SELECT).get("value"),
            groupName: registry.byId(constants.TOOLBAR_HDCONNECT_Group_Name).get("value"),
            securityMode:registry.byId(constants.TOOLBAR_SECRITY_CHANGE).get("value"),
            hdfsPrincipal:  registry.byId(constants.TOOLBAR_HDFS_Principal).get("value"),
            hdfsKeyTab: registry.byId(constants.TOOLBAR_HDFS_Keytab).get("value"),
            mapredPrincipal: registry.byId(constants.TOOLBAR_Mapred_Principal).get("value"),
            mapredKeyTab: registry.byId(constants.TOOLBAR_Mapred_Keytab).get("value")
        };
    }

})