/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * AuditUIHelper
 * Author Gary
 */
define(["alpine/system/AuditManager"], function(auditManager){
	var constants = {
		MENU: "user_logs_button",
		DIALOG: "alpine_system_auditmanager_Dialog",
		QUERY_CATEGORY: "alpine_system_auditmanager_query_category",
		GRID: "alpine_system_auditmanager_grid",
		BUTTON_CLEAR: "alpine_system_auditmanager_button_clear"
	};
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.MENU), "onClick", openAuditDialog);
		dojo.connect(dijit.byId(constants.QUERY_CATEGORY), "onChange", switchCategory);
		dojo.connect(dijit.byId(constants.BUTTON_CLEAR), "onClick", clearPersonalLog);
	});
	
	function openAuditDialog(){
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
		initAuditCategory(alpine.USER);
		auditManager.loadAuditLogs(alpine.USER, buildAuditGrid);//default load audit logs of current user.
	}

	function initAuditCategory(initalizeUser){
		auditManager.loadCategories(function(data){
			var categorySel = dijit.byId(constants.QUERY_CATEGORY);
			dojo.forEach(categorySel.getOptions(),function(option,i){
				categorySel.removeOption(option);
			});
			dojo.forEach(data,function(item){
				categorySel.addOption({
					label: item.label,
					value: item.value
				});
			});
			categorySel.set("value",initalizeUser);
		});
	}

	function switchCategory(){
		var category = dijit.byId(constants.QUERY_CATEGORY).get("value");
		auditManager.loadAuditLogs(category, buildAuditGrid);
	}

	function buildAuditGrid(datas){
		if(datas.error_code){
			handle_error_result(data);
			return ;
		}
		var grid = dijit.byId(constants.GRID);
		var dataStore = new dojo.data.ItemFileReadStore({
			data: {
				identifer: "sequence",
				items: datas
			}
		});
		if(!grid){
			grid = new dojox.grid.DataGrid({
				store: dataStore,
				query: {"sequence": "*"},
				structure: [
				           //	{type: "dojox.grid._CheckBoxSelector"},
				            [
				             	{name: alpine.nls.audit_grid_title_category,field: "user", width: "20%"},
				             	{name: alpine.nls.audit_grid_title_time,field: "dateTime", width: "30%"},
				             	{name: alpine.nls.audit_grid_title_action,field: "action", width: "20%"},
				             	{name: alpine.nls.audit_grid_title_detail,field: "actionDetail", width: "30%"}
				            ]
				]
			},constants.GRID);
			grid.startup();
		}else{
			grid.setStore(dataStore);
		}
	}

	function clearPersonalLog(){
		var categoryVal = dijit.byId(constants.QUERY_CATEGORY).get("value");
		auditManager.clearPersonalLog(categoryVal, function(){
			var grid = dijit.byId(constants.GRID);
			if(grid){
				var dataStore = new dojo.data.ItemFileReadStore({
					data: {
						identifer: "sequence",
						items: []
					}
				});	
			  grid.setStore(dataStore);
			}
		});
	}	
});
