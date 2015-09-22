/**
 * 
 */
define(["dojo/on"],function(on){
	var login = alpine.USER;
	var adminBaseURL = baseURL + "/main/admin.do";
	var error_msg = null;

	var constants = {
	    ADMIN_ROLE_IDENTIFIER: "admin",
	    MODELER_ROLE_IDENTIFIER: "modeler"
	};

	function showGroupUserDialog() {
		disable_group_buttons();
		disable_user_buttons();
		loadGroups();
		loadUsers();

        if(alpine.auth_type != "LocalProvider" && alpine.USER != "admin") {
            disable_user_profile_controls(true);
        }
        else {
            disable_user_profile_controls(false);
        }

		dijit.byId("groupUserEditDialog").titleBar.style.display = "none";
        on.once(dijit.byId("groupUserEditDialog"),"hide",function(){
            clear_user_from();
        });
		dijit.byId("groupUserEditDialog").show();
	}


    function disable_user_profile_controls(flag) {
        dijit.byId("update_user_profile_button").set("disabled", flag);
        dijit.byId("user_profile_password").set("disabled", flag);
        dijit.byId("user_profile_password2").set("disabled", flag);
        dijit.byId("user_profile_email").set("disabled", flag);
        dijit.byId("user_profile_notify").set("disabled", flag);
        dijit.byId("user_profile_first").set("disabled", flag);
        dijit.byId("user_profile_last").set("disabled", flag);
        dijit.byId("user_profile_desc").set("disabled", flag);
    }

	function loadGroups() {
		var url = adminBaseURL + "?method=getGroups" + "&user=" + login;
		ds.get(url, createGroupGrid);
	}

	var groupGrid =null;
	var CurrentGroupList = null;
	function createGroupGrid(groupData) {

		if(groupData.error_code){
			handle_error_result(groupData);
			return ;
		}
		CurrentGroupList = groupData;
		generateGroupList(groupData);
		var store = {
			identifier : 'id',
			label : 'id',
			items : groupData
		};
		var groupStore = new dojo.data.ItemFileReadStore({
			data : store
		});

		if(groupGrid==null){
			// set the layout structure:
			var layout = [ {
				field : 'id',
				name : alpine.nls.group_name,
				width : '25%'
		
			}, {
				field : 'description',
				name : alpine.nls.group_desc,
				width : '75%'
			} ];
		
			var panel = dojo.byId("groupgridpane");
			if (panel.firstChild) {
				panel.removeChild(panel.firstChild);
			}
			
			
			var groupGridDomNode = document.createElement('div');
			panel.appendChild(groupGridDomNode);
			// create a new group grid:
			groupGrid = new dojox.grid.DataGrid({
				query : {
					id : '*'
				},
				store : groupStore,
				clientSort : true,
				rowSelector : '10px',
				selectionMode: "single",
				structure : layout
			}, groupGridDomNode);
		

			dojo.connect(groupGrid, "onRowClick", click_group);
			groupGrid.startup();
		}else{
			groupGrid.setStore(groupStore);
			groupGrid.render();
		}
	}

	function click_group(event) {
		
		 //add by Will begin
		var g;
		var userObj =null;
		if(event && event.rowIndex==undefined){
			var currentRowIndex;
			currentRowIndex = groupGrid.focus.rowIndex;		
			g = groupGrid.getItem(currentRowIndex);
			groupGrid.selection.select(currentRowIndex);
		
		}else{
			g = groupGrid.getItem(event.rowIndex);
		}
		//add by Will end

		

		dojo.byId("group_name").value = g.id;
		dojo.byId("group_desc").value = g.description;
		enable_group_update();
	}

	function clear_group_from() {
		dojo.byId("group_name").value = "";
		dojo.byId("group_desc").value = "";
	}
	function create_group() {
		if (!groupForm.validate()) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		var url = adminBaseURL + "?method=createGroup" + "&user=" + login;
		error_msg = alpine.nls.createGroup;
		var g = get_group_data();
		ds.post(url, g, createGroupGrid, error_callback);
	}

	function update_group() {
		if (!groupForm.validate()) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		var url = adminBaseURL + "?method=updateGroup" + "&user=" + login;
		error_msg = alpine.nls.updateGroup;
		var g = get_group_data();
		ds.post(url, g, createGroupGrid, error_callback);
	}

	function delete_group() {
	 
		var url = adminBaseURL + "?method=deleteGroup" + "&user=" + login;
		error_msg = alpine.nls.deleteGroup;
		var g = get_group_data();
		ds.post(url, g, createGroupGrid, error_callback);
	}

	function get_group_data() {
		var g = groupForm.getValues();
		g.description = dojo.byId("group_desc").value;
		disable_group_buttons();
		clear_group_from();
		return g;
	}

	function enable_group_create() {
		dijit.byId("group_create_button").set("disabled", false);
		dijit.byId("group_update_button").set("disabled", true);
		dijit.byId("group_delete_button").set("disabled", true);
	}

	function enable_group_update() {
		dijit.byId("group_create_button").set("disabled", true);
		dijit.byId("group_update_button").set("disabled", false);
		dijit.byId("group_delete_button").set("disabled", false);
	}

	function disable_group_buttons() {
		dijit.byId("group_create_button").set("disabled", true);
		dijit.byId("group_update_button").set("disabled", true);
		dijit.byId("group_delete_button").set("disabled", true);
	}

	function loadUsers() {
		var url = adminBaseURL + "?method=getUsers" + "&user=" + login;
		ds.get(url, createUserGrid);
	}

	var userGrid =null;
	function createUserGrid(userData) {
	 
		if(userData.error_code){
			handle_error_result(obj);
			return ;
		}

		var store = {
			identifier : 'login',
			label : 'login',
			items : userData
		};
		var userStore = new dojo.data.ItemFileReadStore({
			data : store
		});

		if(userGrid==null){
			// set the layout structure:
			var layout = [ {
				field : 'login',
				name : alpine.nls.user_name,
				width : '40%'
		
			}, {
				field : 'lastName',
				name : alpine.nls.user_last,
				width : '30%',
				editable : true
			}, {
				field : 'firstName',
				name : alpine.nls.user_first,
				width : '30%',
				editable : true
			} ];
		
			var panel = dojo.byId("usergridpane");
			if (panel.firstChild) {
				panel.removeChild(panel.firstChild);
			}
			var userGridDomNode= document.createElement('div');
			panel.appendChild(userGridDomNode);
			// create a new grid:
			userGrid = new dojox.grid.DataGrid({
				query : {
					login : '*'
				},
				store : userStore,
				clientSort : true,
				selectionMode: "single",
				rowSelector : '10px',
				structure : layout
			}, userGridDomNode);
		
			
			dojo.connect(userGrid, "onRowClick", click_user);
			userGrid.startup();
		}else{
			userGrid.setStore(userStore);
			userGrid.render();
		}
	}

	function click_user(event) {
		 //add by Will begin
		var items = [];
		var userObj =null;
		if(event && event.rowIndex==undefined){
			var currentRowIndex;
			currentRowIndex = userGrid.focus.rowIndex;		
			items[0] = userGrid.getItem(currentRowIndex);
			userGrid.selection.select(currentRowIndex);
			userObj = userGrid.getItem(currentRowIndex);
		}else{
			items = userGrid.selection.getSelected();
		}
		//add by Will end
		if(items&&items[0]){ 
			if(dojo.indexOf(items,userObj)<0){
				//ctrl to cacel the select
				userObj = items[0];
			}
			generateGroupList(CurrentGroupList);
			userForm.setValues(userObj);
		
			dojo.byId("user_desc").value = userObj.description[0];
			dojo.byId("user_password2").value = userObj.password[0];
			dijit.byId("user_notify").set("checked", userObj.notification[0]);
	        if (dojo.indexOf(userObj.roleSet, constants.ADMIN_ROLE_IDENTIFIER) > -1){
	            dijit.byId("roles_admin").set('checked', true);
	        } else {
	            dijit.byId("roles_admin").set('checked', false);
	        }
	        if (dojo.indexOf(userObj.roleSet, constants.MODELER_ROLE_IDENTIFIER) > -1){
	            dijit.byId("roles_modeler").set('checked', true);
	        } else {
	            dijit.byId("roles_modeler").set('checked', false);
	        }

			enable_user_update();
			if(userObj.login[0]=="admin"){//anybody do not able to delete admin account.
				dijit.byId("user_delete_button").set("disabled", true);	
			}
		}
		else{
			clear_user_from();
			disable_user_buttons();
			}
	}

	function clear_user_from() {
		dojo.byId("user_login").value = "";
		dojo.byId("user_email").value = "";
		dojo.byId("user_desc").value = "";
		dojo.byId("user_password").value = "";
		dojo.byId("user_password2").value = "";
		dojo.byId("user_first").value = "";
		dojo.byId("user_last").value = "";
		dijit.byId("user_notify").set("checked", false);
	    dijit.byId("roles_admin").set("checked", false);
	    dijit.byId("roles_modeler").set("checked", false);
	}

	function create_user() {
		if (!userForm.validate()) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		var url = adminBaseURL + "?method=createUser" + "&user=" + login;
		error_msg = alpine.nls.createUser;
		var u = get_user_data();
		var p1 = dojo.byId("user_password").value;
		var p2 = dojo.byId("user_password2").value;
		if (p1 != p2) {
			dojo.byId("user_password").value = "";
			dojo.byId("user_password2").value = "";
			userForm.validate();
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		disable_user_buttons();
		clear_user_from();
		ds.post(url, u, createUserGrid, error_callback);
	}

	function update_user() {
		if (!userForm.validate()) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		var url = adminBaseURL + "?method=updateUser" + "&user=" + login;
		error_msg = alpine.nls.updateUser;
		var u = get_user_data();
		var p1 = dojo.byId("user_password").value;
		var p2 = dojo.byId("user_password2").value;
		if (p1 != p2) {
			dojo.byId("user_password").value = "";
			dojo.byId("user_password2").value = "";
			userForm.validate();
			popupComponent.alert(alpine.nls.ConnectionValid);
			return;
		}
		disable_user_buttons();
		clear_user_from();
		ds.post(url, u, createUserGrid, error_callback);
	}

	function delete_user() {
		
		var url = adminBaseURL + "?method=deleteUser" + "&user=" + login;
		error_msg = alpine.nls.deleteUser;
		var u = get_user_data();
		
		if(u.login=="admin"){
			//can not delete admin
			return;
		}
		else{
			disable_user_buttons();
			clear_user_from();
			ds.post(url, u, createUserGrid, error_callback);
		}
	}

	function get_user_data() {
	    var u = userForm.getValues();
		u.description = dojo.byId("user_desc").value;
		u.notification = dijit.byId("user_notify").get("checked");
	    u.roleSet = _roleChecksToString(u);
		return u;
	}

	function _roleChecksToString(userinfo) {
	    var roleStringArray = [];
	    if (userinfo.roles_modeler_ck && userinfo.roles_modeler_ck.length == 1){
	        roleStringArray.push(constants.MODELER_ROLE_IDENTIFIER);
	    }
	    if (userinfo.roles_admin_ck && userinfo.roles_admin_ck.length == 1){
	        roleStringArray.push(constants.ADMIN_ROLE_IDENTIFIER);
	    }
	    return roleStringArray;

	}

	function enable_user_create() {
		dijit.byId("user_create_button").set("disabled", false);
		dijit.byId("user_update_button").set("disabled", true);
		dijit.byId("user_delete_button").set("disabled", true);
	}

	function enable_user_update() {
		dijit.byId("user_create_button").set("disabled", true);
		dijit.byId("user_update_button").set("disabled", false);
		dijit.byId("user_delete_button").set("disabled", false);

	}

	function disable_user_buttons() {
		dijit.byId("user_create_button").set("disabled", true);
		dijit.byId("user_update_button").set("disabled", true);
		dijit.byId("user_delete_button").set("disabled", true);
	}

	function generateGroupList(nameList) {

		var sel = dojo.byId("dynamicGroupSel");
		while (sel.firstChild) {
			sel.removeChild(sel.firstChild);
		}

		var n = nameList.length;
		for ( var i = 0; i < n; i++) {
			var c = dojo.doc.createElement('option');
			c.innerHTML = nameList[i].id;
			c.value = nameList[i].id;
			sel.appendChild(c);
		}

		var msel = dijit.byId("dynamicGroupSel");
		if (msel) {
			msel.startup();
		}

		return sel;

	}

	function generateGroupCheckList(groupList) {

		var sel = dojo.byId('groupCheckList');
		while (sel.firstChild) {
			sel.removeChild(sel.firstChild);
		}
		var n = groupList.length;
		for ( var i = 0; i < n; i++) {
			var str = groupList[i].id;
			var id = str + "_ck";
			
			var div1 = document.createElement("div");
			var checkBox = new dijit.form.CheckBox({
				id : id,
				name : "groups",
				value : str,
				checked : false
			}, div1);

			var label = document.createElement("DIV");
			label.innerText = str;
			label.innerHTML = str;

			sel.appendChild(div1);
			sel.appendChild(label);
		}
		var w = dijit.byId('groupCheckList');
		w.startup();
	}

	function error_callback(text, args) {
		
		if (error_msg == null) {
			return;
		}
//		popupComponent.alert(error_msg);
		loadGroups();
		loadUsers();
		error_msg = null;
	}
	
	return {
		showGroupUserDialog: showGroupUserDialog,
		create_group: create_group,
		update_group: update_group,
		delete_group: delete_group,
		enable_group_create: enable_group_create,
		create_user: create_user,
		update_user: update_user,
		delete_user: delete_user,
		enable_user_create: enable_user_create
	};
});