dojo.provide("unitTest.alpine.system.auditmanager.auditManagerTest");
dojo.require("alpine.system.AuditManager");
dojo.require("alpine.httpService");
baseURL = "/alpinedatalabs";
ds = new httpService();
doh.register("auditManagerTest", [
	function testLoadCategories(){
		alpine.system.AuditManager.loadCategories(function(data){
			doh.assertTrue(data.length > 0);
		});
	},
	function testLoadAuditLogs(assert){
		alpine.system.AuditManager.loadAuditLogs("admin", function(data){
			doh.assertTrue(data.length > 0);
		});
	}
]);