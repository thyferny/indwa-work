dojo.provide("unitTest.alpine.system.profile.ProfileManagerTest");
baseURL = "/AlpineMinerWeb";
adminBaseURL = baseURL+"/main/admin.do";
login = "admin";
alpine = {
		USER: "admin"		
};
dojo.require("alpine.system.ProfileManager");
dojo.require("dojox.json.ref"); 
dojo.require("alpine.httpService");


ds = new httpService();
doh.register("ProfileManagerTest", [
	function testOpen_reset_pass_dlg(){
		alpine.system.ProfileManager.open_reset_pass_dlg(function(data){
			doh.assertTrue(data.length>0);
		},null);
	},
	function testUpdate_user_profile(){
		var userObj = {"password":"1","email":"nanan_zhao@163.com","notification_ck":["on"],"firstName":"will","lastName":"zhao","description":"admin","submit":"","reset":"","groups":["Business","IT","Marketing"],"login":"admin","notification":true};
		alpine.system.ProfileManager.update_user_profile(userObj,function(data){
			doh.assertTrue(data.length>0);
		},null);
	}
]);