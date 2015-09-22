define(function(){
	var blacklist = [];
	
	dojo.ready(function(){
		blacklist = alpine.userInfo.blacklist;
	});
	
	function _checkPermission(permissionCode){
		return !blacklist[permissionCode];
	}
	
	return {
		checkPermission: _checkPermission
	};
});