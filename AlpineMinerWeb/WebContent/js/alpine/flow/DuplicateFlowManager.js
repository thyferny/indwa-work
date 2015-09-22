/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DuplicateFlowManager
 * Author Gary
 */
define(function(){
	function saveDuplicateFlow(newFlowName, flowContent, fn, callbackpanelid){
		var url = flowBaseURL + "?method=duplicateFlow" + "&newFlowName=" + newFlowName;
		ds.post(url, flowContent, function(obj) {
			if (obj.error_code&&obj.error_code!=0){
				if (obj.error_code == 3) {
					popupComponent.alert(alpine.nls.duplicateflow_alert_newflowisexist, function(){
						progressBar.closeLoadingBar();
					});
					return;
				} else if (obj.error_code == 4) {
					popupComponent.alert(alpine.nls.duplicateflow_alert_copyerror, function(){
						progressBar.closeLoadingBar();
					});
					return;
				}else{
					popupComponent.alert(obj.message, function(){
						progressBar.closeLoadingBar();
					});
					return;
				} 
			}else {
				fn.call(null);
			}
		}, function(text, arg) {
//			popupComponent.alert(text);
			progressBar.closeLoadingBar();
		}, false, callbackpanelid);
	}
	
	return {
		duplicate: saveDuplicateFlow
	};
});