dojo.provide("unitTest.alpine.flow.myflowresult.myFlowResultManagerTest");
dojo.require("alpine.flow.MyFlowResultManager");
dojo.require("alpine.httpService");
dojo.require("dojox.json.ref");  
baseURL= "/AlpineMinerWeb";
ds = new httpService();
doh.register("myFlowResultManagerTest", [

	function testShowFlowResultsDialog(){
		var searchObj = {};
		alpine.flow.MyFlowResultManager.showFlowResultsDialog(function(data){
			doh.assertTrue(data.length>0);
		});
	}
	/*,
	function testPerform_delete_flow_result(){
		var deleteFlowArray = [{"key":["barchart239"],"id":["0.42070511915944164"]},{"key":["c_child"],"id":["0.019606686534348883"]},{"key":["scatter"],"id":["0.615961560479335"]},{"key":["scatter"],"id":["0.6216143989737132"]}];
		alpine.flow.MyFlowResultManager.perform_delete_flow_result(replaceParamObj,function(data){
			doh.assertTrue(data 2);
		});
	}
	*/
]);