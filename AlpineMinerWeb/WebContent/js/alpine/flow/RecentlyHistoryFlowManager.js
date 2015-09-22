/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * RecentlyHistoryFlowManager
 * Author Gary
 */
define(function(){
	
	/**
	 * push a flow into history container of current user
	 * invoked when open a flow
	 */
	function pushFlow2History(flow, fn){
		var url = flowBaseURL + "?method=pushNewFlowHistory";
		ds.post(url,flow,
		function(data){
			if(fn){
				fn.call(data);
			}
		});
	}

	/**
	 * remove opened history from container of current user
	 */
	function removeFlowFromHistory(flow, fn){
		var url = flowBaseURL + "?method=removeFlowHistory";
		ds.post(url,flow,
		function(data){
			if(fn){
				fn.call(data);
			}
		});
	}
	
	/**
	 * load opened history by user login name
	 * @return opened history records
	 */
	function loadHistoryByUser(userName){
		var url = flowBaseURL + "?method=getFlowHistoryByUser&userName=" + userName;
		var result;
		ds.get(url, function(data){
			result = data;
		}, null, true);
		return result;
	}
	
	/**
	 * remove all of opened history records from container of user
	 */
	function removeAllHistoryFromUser(userName, fn, errorFn){
		var url = flowBaseURL + "?method=clearFlowHistory&userName=" + userName;
		ds.get(url,
		function(data){
			fn.call(null, data);
		},function(text,arg){
			errorFn.call(null, text);
		});
	}
	
	return {
		pushFlow2History: pushFlow2History,
		removeFlowFromHistory: removeFlowFromHistory,
		loadHistoryByUser: loadHistoryByUser,
		removeAllHistoryFromUser: removeAllHistoryFromUser
	};
});