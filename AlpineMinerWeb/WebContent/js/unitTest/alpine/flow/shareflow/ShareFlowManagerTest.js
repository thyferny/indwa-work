dojo.provide("unitTest.alpine.flow.shareflow.ShareFlowManagerTest");
dojo.require("alpine.flow.ShareFlowManager");
dojo.require("alpine.httpService");
dojo.require("dojox.json.ref");  
baseURL= "/AlpineMinerWeb";
ds = new httpService();
doh.register("ShareFlowManagerTest", [
    
	function testShare_a_flow(){
		var url = baseURL+"/main/flow.do?method=shareFlow&user=admin&type=Public&name=Public";
		var CurrentFlow ={"xmlString":"","tmpPath":"C:\\DOCUME~1\\ADMINI~1\\LOCALS~1\\Temp\\\\62c929a1-1247-4be5-bbcb-4efda003696abarchart_1.afm","tag":"TOP","comments":"eee","version":"1","id":"barchart_1","createUser":"admin","createTime":1336018137640,"modifiedUser":"admin","modifiedTime":1336018137640,"type":"Personal","groupName":"","key":"C:\\Documents and Settings\\Administrator\\ALPINE_DATA_REPOSITORY\\flow\\Personal\\admin\\barchart_1"};
		alpine.flow.ShareFlowManager.share_a_flow(url,CurrentFlow,function(data){
			doh.assertEqual(data.message,"success");
		},null);
	}
	
]);