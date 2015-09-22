define(["alpine/datasourcemgr/DataSourceHadoopManager",
        "dojo/on"], function(persistenceManager, on){
	var constants = {
		EDITOR_HADOOP_CONTENT: "alpine_datasource_config_editor_hadoop_content",
		
		EDITOR_HADOOP_FORM: "alpine_datasource_config_editor_hadoop_form",
		
		EDITOR_INPUT_HADOOP_HDFS_HOST_ID: "alpine_datasource_hadoop_config_editor_hdfs_host",
		EDITOR_INPUT_HADOOP_HDFS_PORT_ID: "alpine_datasource_hadoop_config_editor_hdfs_port",
		EDITOR_INPUT_HADOOP_JOB_HOST_ID: "alpine_datasource_hadoop_config_editor_job_host",
		EDITOR_INPUT_HADOOP_JOB_PORT_ID: "alpine_datasource_hadoop_config_editor_job_port",
		EDITOR_INPUT_HADOOP_VERSION_ID: "alpine_datasource_hadoop_config_editor_version",
		EDITOR_INPUT_HADOOP_USERNAME_ID: "alpine_datasource_hadoop_config_editor_userName",
		EDITOR_INPUT_HADOOP_GROUPNAME_ID: "alpine_datasource_hadoop_config_editor_groupName",
		EDITOR_INPUT_HADOOP_SECURITY_MODE: "alpine_datasource_hadoop_config_editor_securityMode",
		EDITOR_INPUT_HADOOP_HDFS_PRINCIPAL: "alpine_datasource_hadoop_config_editor_hdfsPrincipal",
		EDITOR_INPUT_HADOOP_HDFS_KEYTAB: "alpine_datasource_hadoop_config_editor_hdfskeytab",
		EDITOR_INPUT_HADOOP_MAPRED_PRINCIPAL: "alpine_datasource_hadoop_config_editor_mapredPrincipal",
		EDITOR_INPUT_HADOOP_MAPRED_KEYTAB: "alpine_datasource_hadoop_config_editor_mapredkeytab",
        //for toolbutton create connection hadoop version
        TOOLBAR_BUTTON_HADOOP_VERSION_SELECT:"alpine_create_datasource_4toolbutton_hadoop_version"
	};
	
	dojo.ready(function(){
		initEditorVersionWidget(persistenceManager.getAllVersions());
		on(dijit.byId(constants.EDITOR_INPUT_HADOOP_SECURITY_MODE), "change", function(val){
			var showSecurityConfig = val == "kerberos";
			dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PRINCIPAL).set("disabled", !showSecurityConfig);
			dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_KEYTAB).set("disabled", !showSecurityConfig);
			dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_PRINCIPAL).set("disabled", !showSecurityConfig);
			dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_KEYTAB).set("disabled", !showSecurityConfig);
		});
	});
	
	function resetEditorWidgets(){
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_HOST_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PORT_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_JOB_HOST_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_JOB_PORT_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_USERNAME_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_VERSION_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_GROUPNAME_ID).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_SECURITY_MODE).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PRINCIPAL).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_KEYTAB).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_PRINCIPAL).reset();
		dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_KEYTAB).reset();
	}
	
	function fillDataToWidgets(data){
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_HOST_ID).set("value", data.connection.hdfsHostName);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PORT_ID).set("value", data.connection.hdfsPort);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_JOB_HOST_ID).set("value", data.connection.jobHostName);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_JOB_PORT_ID).set("value", data.connection.jobPort);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_USERNAME_ID).set("value", data.connection.userName);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_VERSION_ID).set("value", data.connection.version);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_GROUPNAME_ID).set("value", data.connection.groupName);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_SECURITY_MODE).set("value", data.connection.securityMode);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PRINCIPAL).set("value", data.connection.hdfsPrincipal);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_KEYTAB).set("value", data.connection.hdfsKeyTab);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_PRINCIPAL).set("value", data.connection.mapredPrincipal);
		dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_KEYTAB).set("value", data.connection.mapredKeyTab);
	}
	
	function buildConfigInfo(){
		return {
			hdfsHostName: dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_HOST_ID).get("value"),
			hdfsPort: dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PORT_ID).get("value"),
			jobHostName: dijit.byId(constants.EDITOR_INPUT_HADOOP_JOB_HOST_ID).get("value"),
			jobPort: dijit.byId(constants.EDITOR_INPUT_HADOOP_JOB_PORT_ID).get("value"),
			userName: dijit.byId(constants.EDITOR_INPUT_HADOOP_USERNAME_ID).get("value"),
			version: dijit.byId(constants.EDITOR_INPUT_HADOOP_VERSION_ID).get("value"),
			groupName: dijit.byId(constants.EDITOR_INPUT_HADOOP_GROUPNAME_ID).get("value"),
			securityMode: dijit.byId(constants.EDITOR_INPUT_HADOOP_SECURITY_MODE).get("value"),
			hdfsPrincipal: dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_PRINCIPAL).get("value"),
			hdfsKeyTab: dijit.byId(constants.EDITOR_INPUT_HADOOP_HDFS_KEYTAB).get("value"),
			mapredPrincipal: dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_PRINCIPAL).get("value"),
			mapredKeyTab: dijit.byId(constants.EDITOR_INPUT_HADOOP_MAPRED_KEYTAB).get("value")
		};
	}
	
	function testConnect(){
		var resourceInfo = {
			connection: buildConfigInfo()
		};
		persistenceManager.testConnectionForConfig(resourceInfo, function(){
			popupComponent.alert(alpine.nls.MSG_TEST_Connenction_OK);
		}, function(msg){
			if(msg){
				msg = alpine.nls.MSG_TEST_Connenction_Error + msg;
			}else{
				msg = alpine.nls.MSG_TEST_Connection_Failure;
			}
			popupComponent.alert(msg);
		}, constants.EDITOR_HADOOP_CONTENT);
	}
	
	function initEditorVersionWidget(/*String[]*/versions){
		for(var i = 0;i < versions.length;i++){
			dijit.byId(constants.EDITOR_INPUT_HADOOP_VERSION_ID).addOption({
				value: versions[i],
				label: versions[i]
			});
           //for toolbutton create connection ui
           dijit.byId(constants.TOOLBAR_BUTTON_HADOOP_VERSION_SELECT).addOption({
               value: versions[i],
               label: versions[i]
           });
		}
	}
	
	function validate(){
		return dijit.byId(constants.EDITOR_HADOOP_FORM).validate();
	}
	
	return {
		resetEditorWidgets: resetEditorWidgets,
		fillDataToWidgets: fillDataToWidgets,
		testConnect: testConnect,
		buildConfigInfo: buildConfigInfo,
		loadDataSourceConfig: persistenceManager.getConnectionConfig,
		saveResourceInfo: persistenceManager.saveConnectionConfig,
		updateResourceInfo: persistenceManager.updateConnectionConfig,
		validate: validate
	};
});