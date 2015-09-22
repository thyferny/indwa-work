define(function(){
	function share_a_flow(/*String*/url, /*Object*/currentFlow, /*Function*/share_a_flow_callback_Success,/*Function*/share_a_flow_callback_Error,callbackpanelid){
		ds.post(url, currentFlow, share_a_flow_callback_Success,share_a_flow_callback_Error, false, callbackpanelid);
	};
	
	return {
		share_a_flow:share_a_flow
	};

});