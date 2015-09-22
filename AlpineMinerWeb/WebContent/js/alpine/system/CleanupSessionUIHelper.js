/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * CleanupSessionUIHelper
 * Author Gary
 */
define(["alpine/system/CleanupSessionManager"], function(cleanSessionManager){

	var constants = {
		MENU_ID: "alpine_system_sessionMgr_menu",
		DIALOG: "alpine_system_sessionMgr_dialg",
		GRID: "alpine_system_sessionMgr_grid",
		BUTTON_REMOVE: "alpine_system_sessionMgr_clean"
	};
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.MENU_ID), "onClick", startup);
		dojo.connect(dijit.byId(constants.BUTTON_REMOVE), "onClick", removeLoginInfo);
		dojo.connect(dijit.byId(constants.DIALOG), "onHide", function(){
			dijit.byId(constants.GRID).selection.deselectAll();
		});
	});
	
	function startup(){
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
		buildGrid(cleanSessionManager.getLoginInfoList());
	}
	
	function buildGrid(data){
		var grid = dijit.byId(constants.GRID);
		var dataStore = new dojo.data.ItemFileWriteStore({
			data: {
				identifer: "loginName",
				items: data
			}
		});
		if(grid == null){
			grid = new dojox.grid.DataGrid({
				store: dataStore,
				query: {"loginName": "*"},
				structure: [
		           	{type: "dojox.grid._CheckBoxSelector"},
		            [
		             	{name: alpine.nls.session_manager_grid_loginname,field: "loginName", width: "50%"},
		             	{name: alpine.nls.session_manager_grid_logintime,field: "loginTime", width: "50%"}
		            ]
				],
				onRowClick: function(){
					
				}
			}, constants.GRID);
			grid.startup();
		}else{
			grid.setStore(dataStore);
		}
	}
	
	function removeLoginInfo(){
		var loginNameArray = new Array();
		var selectedItems = dijit.byId(constants.GRID).selection.getSelected();
		if(selectedItems == null || selectedItems.length == 0){
			return;
		}
		popupComponent.confirm(alpine.nls.session_manager_delete, {
			handle: function(){
				cleanSessionManager.clearUserSession(selectedItems, function(){
					dijit.byId(constants.GRID).removeSelectedRows();
				});
			}
		});
	}
});