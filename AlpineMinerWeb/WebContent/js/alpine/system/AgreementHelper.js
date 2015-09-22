/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * AgreementHelper.js 
 * Author Gary
 * Dec 24, 2012
 */
define(["dojo/dom",
        "dijit/registry",
        "dojo/on"], function(dom, registry, on){
	var constants = {
		DIALOG: "alpine_system_agreement_Dialog",
		AGREEMENT_CONTENT: "alpine_system_agreement_content_window",
		BTN_CONTINUE: "alpine_system_agreement_button_continue",
		BTN_AGREE: "alpine_system_agreement_button_agree",
		BTN_PRINT: "alpine_system_agreement_button_print"
	};
	
	var scrollEvent = null;
	
	dojo.ready(function(){
		on(registry.byId(constants.BTN_CONTINUE), "click", _accept);
		on(registry.byId(constants.BTN_AGREE), "change", function(isChanged){
			registry.byId(constants.BTN_CONTINUE).set("disabled", !isChanged);
		});
		on(registry.byId(constants.BTN_PRINT), "click", function(){
			dom.byId(constants.AGREEMENT_CONTENT).contentWindow.print();
		});
	});
	
	function _showAgreement(){
		registry.byId(constants.DIALOG).titleBar.style.display = "none";

		var agreementIframe = dom.byId(constants.AGREEMENT_CONTENT);
		var agreementDoc = agreementIframe.contentDocument;
		scrollEvent = on(agreementDoc, "scroll", _scrollingAgreement);
		
		registry.byId(constants.DIALOG).show();
	}
	
	function _scrollingAgreement(e){
		var agreementDoc = e.target;
		var winScroll = agreementDoc.documentElement.scrollTop || agreementDoc.body.scrollTop;
		if(winScroll >= (agreementDoc.body.offsetHeight - agreementDoc.documentElement.clientHeight)){
			registry.byId(constants.BTN_AGREE).set("disabled", false);
		}
	}
	
	function _accept(){
		scrollEvent.remove();
        registry.byId(constants.BTN_AGREE).set("disabled",true);
		var userName = registry.byId("login").get("value");
		var url = alpine.baseURL + "/main/admin.do?method=acceptAgreement&userName=" + userName;
		ds.get(url, function(data){
			if(data){
				window.location.href = alpine.baseURL + "/alpine/flow/webflowmain.jsp?user=" + userName;
			}
		});
	}
	
	return {
		showAgreement: _showAgreement
	};
});