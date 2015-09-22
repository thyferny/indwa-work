(function(){
	var constants = {
		MENU_ID: "alpine_system_licenseInfo_menu",
		DIALOG: "alpine_system_licenseInfo_dialog",
		PRODUCT_VERSION: "license_displayer_product_version_value",
		EXPIRE_DATE: "license_displayer_expire_date_value",
		LIMIT: "license_displayer_limit_user_count_value",
		LIMIT_MODELER: "license_displayer_limit_modeler_count_value",
        CUSTOMER_ID: "license_displayer_customer_UUID"
    };
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.MENU_ID), "onClick", startup);
		
		getLicenseInfo(alertExpire);
	});
	
	function startup(){
		dijit.byId(constants.DIALOG).show();
		getLicenseInfo(renderData);

	}
	
	function getLicenseInfo(fn){
		ds.get(baseURL + "/main/admin.do?method=getLicenseInfo", function(data){
			fn.call(null, data);
		});
	}
	
	function renderData(licenseData){
		dojo.byId(constants.PRODUCT_VERSION).innerHTML = licenseData.productID;
		dojo.byId(constants.EXPIRE_DATE).innerHTML = licenseData.expireDate;
		dojo.byId(constants.LIMIT).innerHTML = licenseData.limitUserCount;
		dojo.byId(constants.LIMIT_MODELER).innerHTML = licenseData.limitModelerCount;
        dojo.byId(constants.CUSTOMER_ID).innerHTML = licenseData.customerUUID;
    }
	
	function alertExpire(data){
		var alertThreshold = 259200000;//3 days
		var oneDay = 86400000;
		var expireDate = dojo.date.locale.parse(data.expireDate, {datePattern: "yyyy-MM-dd", selector: "date"});
		if(expireDate == null){
			//forever or something
			return;
		}
		var now = new Date().getTime();
		var expireTime = expireDate.getTime() - now;
		if(expireTime > 0 && expireTime <= alertThreshold){
			var msg = dojo.string.substitute(alpine.nls.license_displayer_expire_alert, {
				expired: Math.ceil(expireTime / oneDay)
			});
			popupComponent.alert(msg);
		}
	}
})();