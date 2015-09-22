define(function(){
	function getPrefix(){
		var url = preferenceBaseURL + "?method=getPrefix",
			result = "";
        ds.get(url,function(data)
        {
        	if(data){
                result = alpine.USER + "_";
        	}
        }, null, true, null );
//		dojo.xhrGet({
//			url : encodeURI(url),
//			sync : true,
//			preventCache : true,
//			headers : {
//				"Content-Type" : "plain/text; charset=utf-8",
//				"Accept" : "plain/text",
//				"TIME_STAMP": alpine.TS,
//				"USER_INFO": alpine.USER
//			},
//			load : function(text, args) {
//				if(args&&args.xhr&&args.xhr.status!=200){
//					popupComponent.alert(alpine.nls.can_not_connect_server);
//					return;
//				}
//				if (text && text != "") {
//					var obj = eval("(" + text + ")");
//
//					if (obj.error_code == -1) {
//						popupComponent.alert(alpine.nls.no_login, "",function() {
//							window.top.location.pathname = loginURL;
//						});
//					}
//					else if (obj.error_code == -2) {
//						popupComponent.alert(alpine.nls.session_ended, "",function() {
//							window.top.location.pathname = loginURL;
//						});
//					} else {
//						if(obj){
//							result = alpine.USER + "_";
//						}
//					}
//				}
//			}
//			,
//			error : function(text, args) {
//				if(args&&args.xhr&&args.xhr.status!=200){
//					popupComponent.alert(alpine.nls.can_not_connect_server);
//					return;
//				}
//				console.error(text);
//				text = buildErrorMsg(text);
//
//				popupComponent.alert(alpine.nls.error + ":" + text);
//
//			}
//		});
		return result;
	};
	
	function load_preference_tree(load_prefe_tree_callback,load_prefe_tree_callback_error, callbackPanelId) {
		var url = preferenceBaseURL + "?method=getPreferences";
		ds.get(url, load_prefe_tree_callback,load_prefe_tree_callback_error, false, callbackPanelId);
	};
	
	function restorePreferenceData(/*String*/currentPrefTreeItem_Open_id,/*Function*/restorePreferenceCallback,/*Function*/savePreferenceCallback_error, callbackPanelId){
		var	 requestUrl = preferenceBaseURL + "?method=getPreferencesDefaultValue&type="+currentPrefTreeItem_Open_id;
		ds.get(requestUrl,  restorePreferenceCallback, savePreferenceCallback_error, false, callbackPanelId);
	};
	
	function savePreferenceData(/*Object*/submitData,/*Fucntion*/savePreferenceCallback,/*Fucntion*/savePreferenceCallback_error, callbackPanelId){
		var	 requestUrl = preferenceBaseURL + "?method=updatePreference";
		ds.post(requestUrl, submitData, savePreferenceCallback, savePreferenceCallback_error, false, callbackPanelId);
	};
	
	return {
		getPrefix:getPrefix,
		load_preference_tree:load_preference_tree,
		savePreferenceData:savePreferenceData,
		restorePreferenceData:restorePreferenceData
	};
	
});