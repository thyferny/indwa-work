dojo.provide("unitTest.alpine.flow.recentlyhistorymanage.recentlyHistoryManagerTest");
dojo.require("alpine.flow.RecentlyHistoryFlowManager");
dojo.require("dojox.json.ref");
dojo.require("alpine.httpService");
flowBaseURL = "/alpinedatalabs/main/flow.do";
ds = new httpService();
doh.register("recentlyHistoryManagerTest", [
	function testPushFlow2History(){
		var orignalHistory = alpine.flow.RecentlyHistoryFlowManager.loadHistoryByUser("admin");
		var flowInfo = {
			comments: " ",
			createTime: 1336718583834,
			createUser: "admin",
			groupName: "",
			id: "tset",
			key: "C:\Documents and Settings\alpine\ALPINE_DATA_REPOSITORY\flow\Personal\admin\tset",
			modifiedTime: 1336718755358,
			modifiedUser: "admin",
			tag: "TOP",
			type: "Personal",
			version: "1",
			xmlString: ""
		};
		alpine.flow.RecentlyHistoryFlowManager.pushFlow2History(flowInfo, function(data){
			var historyRecords = alpine.flow.RecentlyHistoryFlowManager.loadHistoryByUser("admin");
			doh.assertTrue(orignalHistory.length < historyRecords.length);
		});
	},
	function testRemoveFlowFromHistory(){

		var orignalHistory = alpine.flow.RecentlyHistoryFlowManager.loadHistoryByUser("admin");
		var flowInfo = {
			comments: " ",
			createTime: 1336718583834,
			createUser: "admin",
			groupName: "",
			id: "tset",
			key: "C:\Documents and Settings\alpine\ALPINE_DATA_REPOSITORY\flow\Personal\admin\tset",
			modifiedTime: 1336718755358,
			modifiedUser: "admin",
			tag: "TOP",
			type: "Personal",
			version: "1",
			xmlString: ""
		};
		alpine.flow.RecentlyHistoryFlowManager.removeFlowFromHistory(flowInfo, function(){
			var historyRecords = alpine.flow.RecentlyHistoryFlowManager.loadHistoryByUser("admin");
			doh.assertTrue(orignalHistory.length > historyRecords.length);
		});
	},
	function testRemoveAllHistoryFromUser(){
		alpine.flow.RecentlyHistoryFlowManager.removeAllHistoryFromUser("admin", function(){
			var historyRecords = alpine.flow.RecentlyHistoryFlowManager.loadHistoryByUser("admin");
			doh.assertTrue(historyRecords.length == 0);
		});
	}
]);