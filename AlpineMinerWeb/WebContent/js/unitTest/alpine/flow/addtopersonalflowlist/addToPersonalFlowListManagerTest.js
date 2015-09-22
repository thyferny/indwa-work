dojo.provide("unitTest.alpine.flow.addtopersonalflowlist.addToPersonalFlowListManagerTest");
dojo.require("alpine.flow.AddToPersonalFlowListManager");
dojo.require("alpine.httpService");
dojo.require("dojox.json.ref");  
flowBaseURL= "/AlpineMinerWeb/main/flow.do";
login = "admin";
ds = new httpService();
doh.register("addToPersonalFlowListManagerTest", [

    function testLoad_flow_tree(){
    	alpine.flow.AddToPersonalFlowListManager.load_flow_tree(function(data){
    		data.length>0
    	});
    },                                             
    function testCopyFlowListHandle(){
    	var flowList = [{"version":"1","comments":"test test test test test test test test test ","id":"sample_2-0-aggregate","key":"C:\\Documents and Settings\\Administrator\\ALPINE_DATA_REPOSITORY\\flow\\Public\\sample_2-0-aggregate","createUser":"admin","modifiedUser":"admin","groupName":"","createTime":1328529884746,"modifiedTime":1328528959304,"type":"Public","tmpPath":""},{"version":"2","comments":"test test test test test test test test test ","id":"sample_2-0-exploration-a","key":"C:\\Documents and Settings\\Administrator\\ALPINE_DATA_REPOSITORY\\flow\\Public\\sample_2-0-exploration-a","createUser":"admin","modifiedUser":"admin","groupName":"","createTime":1332142568062,"modifiedTime":1332142568109,"type":"Public","tmpPath":""}];
    	alpine.flow.AddToPersonalFlowListManager.copyFlowListHandle(0,flowList,function(data){
    		doh.assertEqual(data.newFlowVersion, 1);
    	},null);
    },                                             
    function testDelete_flow_list(){
    	var flowList = [{"version":"3","comments":"eee","id":"barchart239","key":"C:\\Documents and Settings\\Administrator\\ALPINE_DATA_REPOSITORY\\flow\\Public\\barchart239","createUser":"admin","modifiedUser":"admin","groupName":"","createTime":1337239529828,"modifiedTime":1337239529843,"type":"Public","tmpPath":""}];
    	alpine.flow.AddToPersonalFlowListManager.delete_flow_list("Group",flowList,function(data){
    		doh.assertTrue(data.length>0);
    	},null);
    }                                  
]);