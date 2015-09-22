/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * dependency.js 
 * Author Gary
 * Jan 14, 2013
 */
(function(){
	window.dojoConfig = {
		isDebug: true,
		parseOnLoad: true,
		baseUrl: "/root/",
	    tlmSiblingOfDojo: false,
	    packages: [
	        { name: "dojo", location: "dojo" },
	        { name: "dijit", location: "dijit" },
	        { name: "dojox", location: "dojox" },
	        { name: "alpine", location: "alpine" }
	    ]
	};
	alpine = {
		USER: "admin"
	};
	baseURL = "/request";
	jstestdriver.console.log("============== dependency complete " + dojoConfig);
})();