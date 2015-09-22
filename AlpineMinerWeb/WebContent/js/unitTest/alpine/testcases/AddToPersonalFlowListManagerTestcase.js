/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * AddToPersonalFlowListManagerTestcase.js 
 * Author Gary
 * Jan 17, 2013
 */
var addToPersonalFlowManagerTestCase = AsyncTestCase("addToPersonalFlowManagerTest");
var addToPersonalTestflowInfo = null;
var login = null;
addToPersonalFlowManagerTestCase.prototype.setUp = function(){
	dojo.require("alpine.flow.AddToPersonalFlowListManager");
	flowBaseURL = baseURL + "/main/flow.do";
	login = "admin";
};

addToPersonalFlowManagerTestCase.prototype.tearDown = function(){
	login = null;
	addToPersonalTestflowInfo = null;
};

addToPersonalFlowManagerTestCase.prototype.test_loadFlowTreeData = function(queue){
	var flowSize = 0;
	queue.call(function(callbacks){
		alpine.flow.AddToPersonalFlowListManager.load_flow_tree(callbacks.add(function(data){
			flowSize = data.length;
    	}));
	});
	
	queue.call(function(callbacks){
		assertTrue(flowSize > 0);
	});
};

addToPersonalFlowManagerTestCase.prototype.test_addFlowToPersonal = function(queue){
	var isNewFlow = false;
	queue.call(function(callbacks){
		alpine.flow.AddToPersonalFlowListManager.load_flow_tree(callbacks.add(function(data){
			for(var i = 0;i < data.length;i++){
				if(data[i].children == null){
					addToPersonalTestflowInfo = data[i];
					break;
				}
			}
    	}));
	});
	
	queue.call(function(callbacks){
		alpine.flow.AddToPersonalFlowListManager.copyFlowListHandle(0, [addToPersonalTestflowInfo], callbacks.add(function(data){
			isNewFlow = data.newFlowVersion == 1;
		}));
	});
	
	queue.call(function(callbacks){
		alpine.flow.AddToPersonalFlowListManager.delete_flow_list("Personal", [addToPersonalTestflowInfo], callbacks.add(function(data){
			
		}));
	});
	
	queue.call(function(callbacks){
		assertTrue(isNewFlow);
	});
};

addToPersonalFlowManagerTestCase.prototype.test_deleteFlow = function(queue){
	var stillExist = false;
	queue.call(function(callbacks){
		alpine.flow.AddToPersonalFlowListManager.load_flow_tree(callbacks.add(function(data){
			for(var i = 0;i < data.length;i++){
				if(data[i].children == null){
					addToPersonalTestflowInfo = data[i];
					break;
				}
			}
    	}));
	});
	queue.call(function(callbacks){
		alpine.flow.AddToPersonalFlowListManager.delete_flow_list("Personal", [addToPersonalTestflowInfo], callbacks.add(function(data){
			for(var i = 0;i < data.length;i++){
				if(data[i].key == addToPersonalTestflowInfo.key){
					stillExist = true;
				}
			}
		}));
	});
	
	queue.call(function(callbacks){
		assertFalse(stillExist);
	});
};