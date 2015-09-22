dojo.provide("unitTest.alpine.flow.duplicateflow.duplicateTest");
dojo.require("alpine.flow.DuplicateFlowManager");
dojo.require("dojox.json.ref");
dojo.require("alpine.httpService");
flowBaseURL = "/alpinedatalabs/main/flow.do";
ds = new httpService();
doh.register("duplicateFlowTest", [
	function testSaveDuplicateFlow(){
		var newFlowName = "unitTestFlow";
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
		alpine.flow.DuplicateFlowManager.duplicate(newFlowName, flowInfo, function(data){
			doh.assertTrue(true);
		});
	}
]);