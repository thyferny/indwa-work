dojo.provide("unitTest.alpine.system.cleanupsession.cleanupSessionTest");
dojo.require("alpine.system.CleanupSessionManager");
dojo.require("dojox.json.ref");
dojo.require("alpine.httpService");
dojo.require("alpine.utility");
baseURL = "/alpinedatalabs";
ds = new httpService();
doh.register("cleanupSessionTest", [
	function testLoadUserSession(){
		alpine.system.CleanupSessionManager.getLoginInfoList(function(data){
			doh.assertTrue(data.length > 0);
		});
	},
	function testLoadAuditLogs(){
		alpine.system.CleanupSessionManager.clearUserSession({
			loginName: "admin"
		}, function(){
			
		});
	}
]);