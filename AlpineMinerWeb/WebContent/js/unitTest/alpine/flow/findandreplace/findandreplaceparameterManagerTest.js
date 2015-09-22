dojo.provide("unitTest.alpine.flow.findandreplace.findandreplaceparameterManagerTest");
dojo.require("alpine.flow.FindandreplaceparameterManager");
dojo.require("alpine.httpService");
dojo.require("dojox.json.ref");  
baseURL= "/AlpineMinerWeb";
ds = new httpService();
doh.register("findandreplaceparameterManagerTest", [

	function testGetFindAndReplaceQueryResult(){
		var searchObj = {};
		
		searchObj.flowInfo =null;
		searchObj.ignoreCase =true;
		searchObj.parameterValue = "*";
		searchObj.paramterName = "schemaName";
		searchObj.searchScope = "all";
		
		alpine.flow.FindandreplaceparameterManager.getFindAndReplaceQueryResult(searchObj,function(data){
			doh.assertTrue(data.length>0);
		});
	},
	function testReplaceValueCanReplace_done(){
		var replaceParamObj = {};
		replaceParamObj.flowInfo=null;
		replaceParamObj.replaceList = [{flowPath:"admin\\@va", operName:"DB Table", parameterName:"schemaName"},{flowPath:"admin\\aa", operName:"DB Table", parameterName:"schemaName"}];
		replaceParamObj.replaceValue =	"demo";
		replaceParamObj.searchScope =	"all";
		alpine.flow.FindandreplaceparameterManager.replaceValueCanReplace_done(replaceParamObj,function(data){
			doh.assertEqual(data.replaceNum, 2);
		});
	}
]);