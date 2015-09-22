var alpine_system_licenseUpdate;
(function(){
	var constants = {
		DIALOG: "system_licenseupdate_dialog",
		FORM: "system_licenseupdate_form",
		ADMIN_PWD: "system_licenseupdate_input_adminPwd",
		LICENSE: "system_licenseupdate_input_license",
		BUTTON_OK: "system_licenseupdate_button_save",
		BUTTON_CLOSE: "system_licenseupdate_button_close"
	};

    //var ds = new httpService();
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.BUTTON_OK), "onClick", commit);
		dojo.connect(dijit.byId(constants.BUTTON_CLOSE), "onClick", function(){
			dijit.byId(constants.FORM).reset();
			dijit.byId(constants.DIALOG).hide();
		});
	});
	
	function startup(){
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
	}
	
	function commit(){
		var isValid = dijit.byId(constants.FORM).validate();
		if(!isValid){
			return;
		}
		ds.post(alpine.baseURL + "/main/admin.do?method=updateLicense", {
			adminPwd: dijit.byId(constants.ADMIN_PWD).get("value"),
			license: dijit.byId(constants.LICENSE).get("value")
		},function(){
			dijit.byId(constants.DIALOG).hide();
		});
	}
	alpine_system_licenseUpdate = {
		startup: startup
	};
})();
