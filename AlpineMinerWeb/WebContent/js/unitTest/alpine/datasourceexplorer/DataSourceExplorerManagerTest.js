dojo.provide("unitTest.alpine.datasourceexplorer.DataSourceExplorerManagerTest");
dojo.require("alpine.httpService");
dojo.require("dojox.json.ref");  

baseURL= "/alpinedatalabs";
ds = new httpService();
progressBar = {
	closeLoadingBar: function(){
		//nothing to do. just make on error in httpService could be work.
	}
};
alpine = {
	spinner: {
		showSpinner: function(){},
		hideSpinner: function(){}
	}	
};

dojo.require("alpine.datasourceexplorer.DataSourceExplorerManager");
doh.register("DataSourceExplorerManagerTest", [
    {
    	name: "test getAvailableConnections",
    	setUp: function(){
    		//to do something befor run testcase.
    		console.debug("befor test.");
    	},
    	runTest: function(){
			var deferred = new doh.Deferred();
//			deferred.getTestCallback(callback);
			alpine.datasourceexplorer.DataSourceExplorerManager.getAvailableConnections(function(data){
				doh.assertTrue(data.length > 0);
				deferred.callback(true);
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	tearDown: function(){
			// cleanup to do after runTest.
    		console.debug("After test.");
    	},
    	timeout: 3000
    },
    {
    	name: "test getSchemasByConnection",
    	runTest: function(){
    		var deferred = new doh.Deferred();
//			deferred.getTestCallback(callback);
			alpine.datasourceexplorer.DataSourceExplorerManager.getSchemasByConnection("Connection Demo", null, function(outerCallback, data){
				doh.assertTrue(data.length > 0);
				deferred.callback(true);
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	timeout: 3000
    },
    {
    	name: "test getTableViewBySchema",
    	runTest: function(){
    		var deferred = new doh.Deferred();
//			deferred.getTestCallback(callback);
			alpine.datasourceexplorer.DataSourceExplorerManager.getTableViewBySchema("Connection Demo", "demo", null, function(outerCallback, data){
				doh.assertTrue(data.length > 0);
				deferred.callback(true);
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	timeout: 3000
    },
    {
    	name: "test getNextFileInHadoop",
    	runTest: function(){
    		var deferred = new doh.Deferred();
//			deferred.getTestCallback(callback);
			alpine.datasourceexplorer.DataSourceExplorerManager.getNextFileInHadoop("Personal\\admin\\hadoop.hdc", null, null, function(outerCallback, data){
				doh.assertTrue(data.length > 0);
				deferred.callback(true);
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	timeout: 3000
    },{
    	name: "test createSubFolder",
    	runTest: function(){
    		var deferred = new doh.Deferred();
			alpine.datasourceexplorer.DataSourceExplorerManager.createSubFolder("Personal\\admin\\hadoop.hdc", null, "unitTest", function(){
				alpine.datasourceexplorer.DataSourceExplorerManager.getNextFileInHadoop("Personal\\admin\\hadoop.hdc", null, null, function(outerCallback, data){
					var foundFolder = false;
					for(var i = 0;i < data.length;i++){
						if(data[i].label == "unitTest"){
							foundFolder = true;
						}
					}
					doh.assertTrue(foundFolder);
					deferred.callback(true);
				});
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	timeout: 3000
    },{
    	name: "test deleteResource",
    	runTest: function(){
    		var deferred = new doh.Deferred();
			alpine.datasourceexplorer.DataSourceExplorerManager.deleteResource("Personal\\admin\\hadoop.hdc", "/unitTest", function(){
				alpine.datasourceexplorer.DataSourceExplorerManager.getNextFileInHadoop("Personal\\admin\\hadoop.hdc", null, null, function(outerCallback, data){
					var foundFolder = false;
					for(var i = 0;i < data.length;i++){
						if(data[i].label == "unitTest"){
							foundFolder = true;
						}
					}
					doh.assertTrue(!foundFolder);
					deferred.callback(true);
				});
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	timeout: 3000
    },{
    	name: "test loadHadoopProperty",
    	runTest: function(){
    		var deferred = new doh.Deferred();
			alpine.datasourceexplorer.DataSourceExplorerManager.loadHadoopProperty("Personal\\admin\\hadoop.hdc", "/demo/accociation.csv", function(data){
				console.log(data);
				doh.assertTrue(true);
				deferred.callback(true);
			}, function(e){
				deferred.errback(e);
			});
			return deferred;
    	},
    	timeout: 3000
    }
]);