define(["alpine/system/ProfileManager"],function(profileManager){

	var CurrentUser = null;
	dojo.ready(function(){
		dojo.connect(dijit.byId('reset_passworod_button'),"onClick",open_reset_pass_dlg);
		dojo.connect(dijit.byId('update_user_profile_button'),"onClick",function(){
			return update_user_profile();
		});
	});

	
	function update_user_profile() {
		if (!user_profileForm.validate()) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		
		error_msg = alpine.nls.updateUser;
		var u = user_profileForm.getValues();
		var p1 = dojo.byId("user_profile_password").value;
		var p2 = dojo.byId("user_profile_password2").value;
		if (p1 != p2) {
			dojo.byId("user_profile_password").value = "";
			dojo.byId("user_profile_password2").value = "";
			user_profileForm.validate();
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}

		u.groups = CurrentUser.groups;
		u.login = login;
		u.notification = dijit.byId("user_profile_notify").get("checked");
		//ds.post(url, u, reset_ok, reset_error);
		profileManager.update_user_profile(u,reset_ok,reset_error);
	}

	function reset_ok(obj) {
		if(obj&&obj.error_code){
			handle_error_result(obj);
			return ;
		}
		dijit.byId('reset_pass_dlg').hide();
	}
	
	function reset_error(text, args) {
//		var str = dojox.json.ref.toJson(text);
//		popupComponent.alert(str);
	}

	function open_reset_pass_dlg() {
		profileManager.open_reset_pass_dlg(init_profile_from);
	}

	function init_profile_from(userData) {
		if (userData) {
			CurrentUser = userData;
			user_profileForm.setValues(userData);
			dijit.byId("user_profile_notify").set("checked", userData.notification);
			dojo.byId("user_profile_password2").value = userData.password;
			dijit.byId('reset_pass_dlg').titleBar.style.display = "none";
			dijit.byId('reset_pass_dlg').show();
		}
	}
});
