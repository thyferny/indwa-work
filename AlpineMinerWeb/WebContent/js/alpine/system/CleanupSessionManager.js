/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * CleanupSessionManager
 * Author Gary
 */
define(function(){
	function getLoginInfoList(){
		var result;
		ds.get(baseURL + "/main/admin.do?method=getLoginInfoList", function(data){
			result = data;
		}, null, true);
		return result;
	}
	
	function clearUserSession(selectedItems, fn){
		var loginNameArray = new Array();
		for(var i = 0;i < selectedItems.length;i++){
			loginNameArray.push(toolkit.getValue(selectedItems[i].loginName));
		}
		ds.post(baseURL + "/main/admin.do?method=removeLoginInfo", loginNameArray, function(data){
			fn.call();
		});	
	}
	
	return {
		getLoginInfoList: getLoginInfoList,
		clearUserSession: clearUserSession
	};
});