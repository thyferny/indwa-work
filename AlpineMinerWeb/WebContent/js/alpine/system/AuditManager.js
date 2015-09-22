/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * AuditManager
 * Author Gary
 */
define(function(){
	function loadCategories(fn){
		var url = baseURL + "/main/auditManager.do?method=loadCategories";
		ds.get(url, function(data){
			if(data.error_code){
				handle_error_result(data);
				return ;
			}
			fn.call(null, data);
		});
	}
	
	function loadAuditLogs(category, fn){
		var url = baseURL + "/main/auditManager.do?method=loadAudits&category=" + category;
		ds.get(url,fn);
	}
	
	function clearPersonalLog(categoryVal, fn){
		var urlVal = baseURL + "/main/auditManager.do?method=removeAudit&category="+categoryVal;
		
		ds.get(urlVal, fn);
	}
	
	return {
		/**
		 * load all of audit categories(means users)
		 */
		loadCategories: loadCategories,
		
		/**
		 * load audit logs from category
		 */
		loadAuditLogs: loadAuditLogs,
		
		/**
		 * remove all of log from category
		 */
		clearPersonalLog: clearPersonalLog
	};
});





