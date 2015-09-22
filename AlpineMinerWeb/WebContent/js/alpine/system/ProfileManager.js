define(function(){
	
	var adminBaseURL = baseURL + "/main/admin.do";
	var login = alpine.USER;
	//var login = "admin";

    function open_reset_pass_dlg(init_profile_from) {
		var url = adminBaseURL + "?method=getUserInfoByLoginName" + "&loginName=" + login;
		ds.get(url, init_profile_from);
	}
	
	function update_user_profile(/*Object*/userObj,callback_OK,callback_Error){
		var url = adminBaseURL + "?method=updateUser" + "&user=" + login;
		ds.post(url, userObj, callback_OK, callback_Error);
	}
	
	return {
		open_reset_pass_dlg:open_reset_pass_dlg,
		update_user_profile:update_user_profile
	};
});