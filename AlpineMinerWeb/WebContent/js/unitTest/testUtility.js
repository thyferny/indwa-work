/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * testUtility.js 
 * Author Gary
 * Jan 15, 2013
 */
var Utility = {

	/**
	 * userInfo = {
	 * 		login,
	 * 		password
	 * }
	 */
	doLogin: function(userInfo){
		var url = baseURL + "/main/admin.do?method=login";
		ds.post(url, userInfo, function(data){
			
			if(data.error_code == 5){// need accept the agreement.
				var url = baseURL + "/main/admin.do?method=acceptAgreement&userName=" + userInfo.login;
				ds.get(url, function(data){
					// login successful.
				});
			}
		});
	}
};