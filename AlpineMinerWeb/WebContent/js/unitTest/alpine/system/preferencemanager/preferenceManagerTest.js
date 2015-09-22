dojo.provide("unitTest.alpine.system.preferencemanager.preferenceManagerTest");
dojo.require("alpine.system.PreferenceManager");
dojo.require("dojox.json.ref"); 
dojo.require("alpine.httpService");
preferenceBaseURL = "/AlpineMinerWeb/main/preference.do";
ds = new httpService();
doh.register("preferenceManagerTest", [
	function testLoad_preference_tree(){
		alpine.system.PreferenceManager.load_preference_tree(function(data){
			doh.assertTrue(data.length>0);
		},null);
	},
	function testSavePreferenceData(){
		var submitData = {"id":"alg","preferenceItems":{"distinct_value_count":"100000","va_distinct_value_count":"99","decimal_precision":"6"}};
		alpine.system.PreferenceManager.savePreferenceData(submitData,function(data){
			doh.assertEqual(data.message,"success");
		},null);
	},
	function testRestorePreferenceData(){
		alpine.system.PreferenceManager.restorePreferenceData("alg",function(data){
			doh.assertEqual(data.id,"alg");
		},null);
	}
]);