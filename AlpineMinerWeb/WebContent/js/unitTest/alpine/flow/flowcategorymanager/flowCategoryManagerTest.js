dojo.provide("unitTest.alpine.flow.flowcategorymanager.flowCategoryManagerTest");

baseURL = "/alpinedatalabs";
dojo.require("alpine.flow.FlowCategoryManager");
dojo.require("dojox.json.ref");
dojo.require("alpine.httpService");
ds = new httpService();
toolkit = {
	getValue: function(val){
		return val;
	}
};
doh.register("flowCategoryManagerTest", [
	function testAddCategory(){
		var orignalData = alpine.flow.FlowCategoryManager.getTreeData();
		alpine.flow.FlowCategoryManager.saveCategory({
			name: "Unit_test_category",
			parentKey: "admin"
		}, function(){
			var lastestData = alpine.flow.FlowCategoryManager.getTreeData();
			doh.assertTrue(lastestData.subItems.length > orignalData.subItems.length);
		});
	},
	function testRenameCategory(){
		var orignalData = alpine.flow.FlowCategoryManager.getTreeData();
		var category = orignalData.subItems[0];
		var orignalName = category.name;
		category.name = "Unit_test_name";
		alpine.flow.FlowCategoryManager.updateCategory(category, function(){
			var lastestData = alpine.flow.FlowCategoryManager.getTreeData();
			doh.assertTrue(lastestData.subItems[0].name != orignalData.subItems[0].name);
			
			category.name = orignalName;
			alpine.flow.FlowCategoryManager.updateCategory(category, function(){
				
			});
		});
	},
	function testRemoveCategory(){
		alpine.flow.FlowCategoryManager.removeCategory({
			key: "admin/Unit_test_category"
		}, function(){
			
		});
	}
]);