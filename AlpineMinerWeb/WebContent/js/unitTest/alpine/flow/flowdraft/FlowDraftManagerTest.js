dojo.provide("unitTest.alpine.flow.flowDraft.FlowDraftManagerTest");
dojo.require("alpine.httpService");
alpine = {
	spinner: {
		showSpinner: function(){},
		hideSpinner: function(){}
	},
	baseURL: "/alpinedatalabs"
};
ds = new httpService();
dojo.require("alpine.flow.FlowDraftManager");
doh.register("FlowDraftManagerTest", [
	function testHasDraft(){
		alpine.flow.FlowDraftManager.checkDraftExist(function(data){
			doh.assertTrue(!data);
		});
	}, 
	function testGetDraftInfo(){
		alpine.flow.FlowDraftManager.getDraftFlowDTO(function(data){
			doh.assertTrue(data);
		});
	}
]);