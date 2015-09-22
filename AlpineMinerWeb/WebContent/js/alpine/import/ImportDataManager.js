/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * ImportDataUIHelper.js 
 * Author Gary
 * Aug 8, 2012
 */
define(function(){
	var constants = {
		REQUEST_URL: baseURL + "/main/importData.do"
	};
	
	function _importData(config, callback, errorCallback){
		var connName = config.connectionName;
		delete config.connectionName;
		ds.post(constants.REQUEST_URL + "?method=importDataToDB&connectionName=" + connName, config, function(data){
			callback.call(null, data);
		}, function(data){
			errorCallback.call(null, data);
		});
	}
	
	function _abortImport(callback){
		ds.get(constants.REQUEST_URL + "?method=abortImport", function(data){
			callback.call(null, data);
		});
	}
	
	/**
	 * to delete temporary file from server side.
	 */
	function _release(callback){
		columnStructure = new Array();
		ds.get(constants.REQUEST_URL + "?method=deleteSimpleData", function(data){
			callback.call();
		});
	}
	
	/**
	 * args = {
	 * 		onMsg: function
	 * }
	 */
	function _readProgress(args){
//		var socket = new WebSocket(new dojo._Url(document.baseURI.replace(/^http/i,'ws'), baseURL + "/main/importDataLoading.do"));
//		if(socket == null){
//			return;
//		}
//		socket.onopen = function() { 
//			socket.onmessage = function(e){ 
//				var data = event.data;
//				if(data != -1){
//					args.onMsg.call(null, data);
//				}else if(data == -1){
//					socket.close();
//				}
//			};
//			socket.onclose = function(){
//				socket = null;
//			};
//		};
		var socket = dojox.socket({
			url: baseURL + "/main/importDataLoading.do",
			contentType: "plain/text; charset=utf-8",
			error: function(){
				console.log("server is unsupport WebSocket.");
				socketStatus = false;
			}
		});
		socket.on("open", function(){
			socket.on("message", function(event){
				var data = event.data;
				if(data != -1){
					args.onMsg.call(null, data);
				}else if(data == -1){
					socket.close();
				}
		    });
			socket.on("close", function(){
				socket = null;
			});
			socket.send("");
		});
	}
	
	return {
		importData: _importData,
		abortImport: _abortImport,
		readProgress: _readProgress,
		release: _release
	};
});