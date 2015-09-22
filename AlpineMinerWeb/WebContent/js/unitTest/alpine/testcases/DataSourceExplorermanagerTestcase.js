
var DataSourceExplorermanager = AsyncTestCase("DataSourceExplorermanager");

DataSourceExplorermanager.prototype.setUp = function(){
	dojo.require("alpine.datasourceexplorer.DataSourceExplorerManager");
	
//	Utility.doLogin({
//		login: "admin",
//		password: "admin"
//	});
};

DataSourceExplorermanager.prototype.test_getAvailableConnections = function(queue){
	var dataLength = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.getAvailableConnections(callbacks.add(function(data){
			dataLength = data.length;
		}));
	});
	queue.call(function(){
		assertTrue(dataLength > 0);
	});
};

DataSourceExplorermanager.prototype.test_getSchemasByConnection = function(queue){
	var dataLength = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.getSchemasByConnection("Connection Demo", null, callbacks.add(function(outerCallback, data){
			dataLength = data.length;
		}));
	});
	queue.call(function(){
		assertTrue(dataLength > 0);
	});
};

DataSourceExplorermanager.prototype.test_getTableViewBySchema = function(queue){
	var dataLength = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.getTableViewBySchema("Connection Demo", "demo", null, callbacks.add(function(outerCallback, data){
			dataLength = data.length;
		}));
	});
	queue.call(function(){
		assertTrue(dataLength > 0);
	});
};

DataSourceExplorermanager.prototype.test_getNextFileInHadoop = function(queue){
	var dataLength = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.getNextFileInHadoop("Personal\\admin\\hadoop.hdc", null, null, callbacks.add(function(outerCallback, data){
			dataLength = data.length;
		}));
	});
	queue.call(function(){
		assertTrue(dataLength > 0);
	});
};

DataSourceExplorermanager.prototype.test_createSubFolder = function(queue){
	var foundFolder = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.createSubFolder("Personal\\admin\\hadoop.hdc", null, "unitTest", callbacks.add(function(){
			alpine.datasourceexplorer.DataSourceExplorerManager.getNextFileInHadoop("Personal\\admin\\hadoop.hdc", null, null, callbacks.add(function(outerCallback, data){
				foundFolder = false;
				for(var i = 0;i < data.length;i++){
					if(data[i].label == "unitTest"){
						foundFolder = true;
						break;
					}
				}
			}));
		}));
	});
	queue.call(function(){
		assertTrue(foundFolder);
	});
};

DataSourceExplorermanager.prototype.test_deleteResource = function(queue){
	var foundFolder = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.deleteResource("Personal\\admin\\hadoop.hdc", "/unitTest", function(){
			alpine.datasourceexplorer.DataSourceExplorerManager.getNextFileInHadoop("Personal\\admin\\hadoop.hdc", null, null, function(outerCallback, data){
				foundFolder = false;
				for(var i = 0;i < data.length;i++){
					if(data[i].label == "unitTest"){
						foundFolder = true;
						break;
					}
				}
			});
		});
	});
	queue.call(function(){
		assertTrue(!foundFolder);
	});
};

DataSourceExplorermanager.prototype.test_loadHadoopProperty = function(queue){
	var returnData = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.loadHadoopProperty("Personal\\admin\\hadoop.hdc", "/csv/accociation.csv", callbacks.add(function(data){
			returnData = data;
		}));
	});
	queue.call(function(){
		assertNotNull(returnData);
	});
};

DataSourceExplorermanager.prototype.test_checkHDFileisExists = function(queue){
	var returnData = null;
	queue.call(function(callbacks){
		alpine.datasourceexplorer.DataSourceExplorerManager.checkHDFileisExists("Personal\\admin\\hadoop.hdc", "/csv/accociation.csv", callbacks.add(function(data){
			returnData = data;
		}));
	});
	queue.call(function(){
		assertTrue(returnData.result);
	});
};