define(["alpine/system/PreferenceManager"],function(preferenceMgmt){
	    //List of preference,loaded when page is loaded
		var currentPreferences=null;
		
		var currentPreference_Open =null;
		var previousPreference_Open = null;
	     //Tree widget 
		var preference_tree=null;
		
	    //TreeItem
		var currentPrefTreeItem_Open = null;

        var constants = {
           DIALOG_ID: "preferenceEditDialog",
           MAIN_ID: "FlowDisplayPanelPersonal"
        };
		
		//regist event
		dojo.ready(function(){
			dojo.connect(dijit.byId('prefrenece_button'),"onClick",showPreferenceDialog);
			dojo.connect(dijit.byId('preferenceEditDialog'),"onHide",closePreferenceEditDialog);
			dojo.connect(dijit.byId('preferenceEditPanel_alg'),"onShow",select_alg_profiles);
			dojo.connect(dijit.byId('preferenceEditPanel_sys'),"onShow",select_sys_profiles);
			dojo.connect(dijit.byId('preferenceEditPanel_db'),"onShow",select_db_profiles);
			dojo.connect(dijit.byId('preferenceEditPanel_ui'),"onShow",select_ui_profiles);
			
			dojo.connect(dijit.byId('preference_alg_save_data_btn'),"onClick",function(){return savePreferenceData('alg');});
			dojo.connect(dijit.byId('preference_alg_restore_data_btn'),"onClick",function(){return restorePreferenceData();});
			
			dojo.connect(dijit.byId('preference_ui_save_data_btn'),"onClick",function(){return savePreferenceData('ui');});
			dojo.connect(dijit.byId('preference_ui_restore_data_btn'),"onClick",function(){return restorePreferenceData();});

			dojo.connect(dijit.byId('preference_db_save_data_btn'),"onClick",function(){return savePreferenceData('db');});
			dojo.connect(dijit.byId('preference_db_restore_data_btn'),"onClick",function(){return restorePreferenceData();});
			
			dojo.connect(dijit.byId('preference_sys_save_data_btn'),"onClick",function(){return savePreferenceData('sys');});
			dojo.connect(dijit.byId('preference_sys_restore_data_btn'),"onClick",function(){return restorePreferenceData();});
			
			dojo.connect(dijit.byId('perference_dlg_done_button'),"onClick",donePreferenceEditDialog);
		});
		
		
		
		
		function closePreferenceEditDialog(){
			currentPrefTreeItem_Open = null; 
			currentPreferences=null;
			
			currentPreference_Open =null;
			previousPreference_Open = null;
		}
		
		function showPreferenceDialog() {
			//progress bar will be closed after the data return from server in the callback... 
			//progressBar.showLoadingBar();
			preferenceMgmt.load_preference_tree(load_prefe_tree_callback,load_prefe_tree_callback_error,constants.MAIN_ID);
			
		}
		
		function seletePrefTreeNode(tree,item){
			tree.set('selectedItem', item);
			open_preference_tree(item,true) ;
		}
		
		
		function load_prefe_tree_callback_error(text, args){
			//progressBar.closeLoadingBar();
//			popupComponent.alert(text);
			
		}
		
		function load_prefe_tree_callback(obj) {
			
			//progressBar.closeLoadingBar();
			
			if(obj&&obj.error_code){
				handle_error_result(obj);
				
				return ;
			}
			
			
			
			//array of preference
			currentPreferences=obj;
			if(!obj||obj.length == 0){
				return;
			}
			dijit.byId("preferenceEditDialog").titleBar.style.display = "none";
			dijit.byId("preferenceEditDialog").show();
			
			if(preference_tree==null){
				
				var items = new Array();
				
				items[0]={id:"alg",label: prefNameMap["alg"] };
				items[1]={id:"sys",label: prefNameMap["sys"] };
				items[2]={id:"db",label: prefNameMap["db"] };
				items[3]={id:"ui",label: prefNameMap["ui"] };
	//	for(var i=0;i<obj.length;i++){
	//		items[i]={id:obj[i].id,label: prefNameMap[obj[i].id] };
	//	}
				var store = {
						identifier : "id",
						label : "label",
						items : items
				};
				var readerStore = new dojo.data.ItemFileWriteStore( {
					data : store
				});
				
				var treeModel = new dijit.tree.ForestStoreModel( {
					store : readerStore,
					query : {id : "*"},
					rootId : "root_Id_",
					rootLabel : "",
					style : "height: 100%",
					childrenAttrs : [ "children" ]
				});
				
				
				var parent = dojo.byId("Preference_Tree_Pane");
				if (parent.firstChild){
					parent.removeChild(parent.firstChild);
				}
				
				var treeDomNode =  document.createElement("div") ;
				parent.appendChild(treeDomNode);
				
				preference_tree = new dijit.Tree( {
					model : treeModel,
                    showRoot: false
				},treeDomNode);
				
				preference_tree.onClick=function(item, node, evt){ 
					if(item.id == "root_Id") {
						return;
					}
					seletePrefTreeNode(preference_tree,item);
				};
				preference_tree.startup();
				
			}
			initPreferenceUI(obj);
			
			var items=preference_tree.get("model").store._arrayOfTopLevelItems ;
			
			if(items&&items[0]){
				seletePrefTreeNode(preference_tree,items[0]);
			}
			
		}
		
		function initAlgPrefUI(algPreference){
			var distinct_value_count = algPreference.preferenceItems["distinct_value_count"];
			var va_distinct_value_count = algPreference.preferenceItems["va_distinct_value_count"];
			var decimal_precision =  algPreference.preferenceItems["decimal_precision"] ;
			dojo.byId("distinct_value_count").value = distinct_value_count;
			dojo.byId("va_distinct_value_count").value = va_distinct_value_count;
			
			dijit.byId("distinct_value_count").validate();
			dijit.byId("va_distinct_value_count").validate();
			
			dojo.byId("decimal_precision").value = decimal_precision;
		}
		function initDBPrefUI(dbPreference){
			var connection_timeout = dbPreference.preferenceItems["connection_timeout"];
			dojo.byId("connection_timeout").value = connection_timeout;
			var hd_local_data_size_threshold = dbPreference.preferenceItems["hd_local_data_size_threshold"];
			dijit.byId("hd_local_data_size_threshold").set("value", hd_local_data_size_threshold);

			var add_outputtable_prefix = dbPreference.preferenceItems["add_outputtable_prefix"];
			
			if(add_outputtable_prefix=="false"){
				dijit.byId("add_outputtable_prefix").set("checked", false);
			}else if(add_outputtable_prefix=="true"){
				dijit.byId("add_outputtable_prefix").set("checked", true);
			}
			
			dijit.byId("connection_timeout").validate();
		}
		function initUIPrefUI(uiPreference){
			
			var value = uiPreference.preferenceItems["max_table_lines"];
			dojo.byId("max_table_lines").value = value;
			dijit.byId("max_table_lines").validate();
			
			value=  uiPreference.preferenceItems["max_scatter_points"];
			dojo.byId("max_scatter_points").value = value;
			dijit.byId("max_scatter_points").validate();
			
			value = uiPreference.preferenceItems["max_timeseries_points"];
			dojo.byId("max_timeseries_points").value = value;
			dijit.byId("max_timeseries_points").validate();
			
			value = uiPreference.preferenceItems["max_cluster_points"];
			dojo.byId("max_cluster_points").value = value;
			dijit.byId("max_cluster_points").validate();
			
			
		}
		
		function initSYSPrefUI(sysPreference){
			
			var debug_level = sysPreference.preferenceItems["debug_level"];
			dojo.byId("debug_level").value = debug_level;
			
		}
		
		
		function initPreferenceUI(preferences){
			for(var i =0 ;i<preferences.length;i++){
				var preference = preferences[i];
				if(preference.id=="alg"){
					initAlgPrefUI(preference);	
				} else if(preference.id=="ui"){
					initUIPrefUI(preference);
				} else if(preference.id=="db"){
					initDBPrefUI(preference);
				}
				else if(preference.id=="sys"){
					initSYSPrefUI(preference);
				}
				
			}
			
		}
		
		function isPreferenceChanged(preference){
			if (!preference) {
				return false;
			}
			var changed = false;
			if(preference.id=="alg"){
				if(dojo.byId("distinct_value_count").value != preference.preferenceItems["distinct_value_count"]
			    	||dojo.byId("va_distinct_value_count").value != preference.preferenceItems["va_distinct_value_count"]
				    ||dojo.byId("decimal_precision").value != preference.preferenceItems["decimal_precision"]){
					changed = true;
				}
			}else if(preference.id=="db"){
				if(dojo.byId("connection_timeout").value != preference.preferenceItems["connection_timeout"]
				|| dijit.byId("hd_local_data_size_threshold").get("value") != preference.preferenceItems["hd_local_data_size_threshold"]
				||(""+dojo.byId("add_outputtable_prefix").checked)!= preference.preferenceItems["add_outputtable_prefix"]){
					changed = true;
				}
			} 
			else if(preference.id=="ui"){
				if(dojo.byId("max_table_lines").value != preference.preferenceItems["max_table_lines"]
				                                                                                                                                                    
				                                                                                                                                                    ||dojo.byId("max_scatter_points").value != preference.preferenceItems["max_scatter_points"]
				                                                                                                                                                                                                                          ||dojo.byId("max_timeseries_points").value != preference.preferenceItems["max_timeseries_points"]
				                                                                                                                                                                                                                                                                                                   ||dojo.byId("max_cluster_points").value != preference.preferenceItems["max_cluster_points"]
				                                                                                                                                                                                                                                                                                                                                                                         
				){
					changed = true;
				}
			}	else if(preference.id=="sys"){
				if(dojo.byId("debug_level").value != preference.preferenceItems["debug_level"]){
					changed = true;
				}
			}
			return changed;
			
		}
		
		function donePreferenceEditDialog(){
			var items=preference_tree.get("model").store._arrayOfTopLevelItems ;
			var allchanged = false;
			var preferenceId;
			for(var i =0 ;i<items.length;i++){
				var id = items[i].id;
				var preference = findPreferenceByID(id); 
				var achanged= isPreferenceChanged(preference); 
				if(achanged == true){
					allchanged = true;
					preferenceId = id;
					break;
				}
			}
			
			if(allchanged == true){
				popupComponent.saveConfirm(alpine.nls.MSG_save_change_confirmation, {
					handle: function(){
					//save successful
					if(savePreferenceData(preferenceId)==true){
						dijit.byId('preferenceEditDialog').hide();
					}
				}
				
				},{
					handle: function(){
					dijit.byId('preferenceEditDialog').hide();
				}
				},{
					hidden: true
				});
			}else{
				dijit.byId('preferenceEditDialog').hide();
			}
			
		}
		
		function savePreferenceIfChanged(changed, originalData){
			if(changed == true){
				popupComponent.saveConfirm(alpine.nls.MSG_save_change_confirmation, {
					handle: function(){
					savePreferenceData(originalData.id);
				}
				},{
					handle: function(){
					if(originalData.id=="alg"){
						initAlgPrefUI(originalData);
					}else if(originalData.id=="db"){
						initDBPrefUI(originalData);
					}else if(originalData.id=="ui"){
						initUIPrefUI(originalData);
					}
					else if(originalData.id=="sys"){
						initSYSPrefUI(originalData);
					}
				}
				},{
					hidden: true
				});
			}
		}
		
		function open_preference_tree(item){
			if(!item||item.id=="root_Id_"||
					(currentPrefTreeItem_Open!=null&&item.id==currentPrefTreeItem_Open.id)){
				return ;
			}
			previousPreference_Open=currentPreference_Open;
			
			if(currentPrefTreeItem_Open != null){
				// make sure the user have to save the edit ,if he make 
				
				var preference = findPreferenceByID(currentPrefTreeItem_Open.id[0]); 
				var changed= isPreferenceChanged(preference); 
				savePreferenceIfChanged(changed, preference) ;
				
				
			}
			
			currentPrefTreeItem_Open=item;
			
			currentPreference_Open=findPreferenceByID(item.id);
			dijit.byId("preference_container").selectChild(dijit.byId("preferenceEditPanel_"+currentPrefTreeItem_Open.id[0]));
	//	if(currentPrefTreeItem_Open.id[0]=="alg"){
	//		dijit.byId("preference_container").selectChild(dijit.byId("preferenceEditPanel_alg"));
	//	}else if(currentPrefTreeItem_Open.id[0]=="db"){
	//		dijit.byId("preference_container").selectChild(dijit.byId("preferenceEditPanel_db"));
	//	}else if(currentPrefTreeItem_Open.id[0]=="ui"){
	//		dijit.byId("preference_container").selectChild(dijit.byId("preferenceEditPanel_ui"));
	//	}
	//	else if(currentPrefTreeItem_Open.id[0]=="sys"){
	//		dijit.byId("preference_container").selectChild(dijit.byId("preferenceEditPanel_sys"));
	//	}
			
		}
		
		function findPreferenceByID(id){
			if(currentPreferences){ 
				for(var i=0;i<currentPreferences.length;i++){
					if(id==currentPreferences[i].id){
						return currentPreferences[i];
					}
				}
			}
			return null;
		}
		
		
		function savePreferenceCallback(obj) {
			//progressBar.closeLoadingBar();
			if(obj&&obj.error_code){
				handle_error_result(obj);
			}
		}
		
		function validatePreference(){
			if (!dijit.byId("distinct_value_count").isValid())
				return false;
			if(!dijit.byId("hd_local_data_size_threshold").isValid())
				return false;
			if (!dijit.byId("va_distinct_value_count").isValid())
				return false;
			if (!dijit.byId("connection_timeout").isValid())
				return false;
			if (!dijit.byId("max_table_lines").isValid())
				return false;
			
			if (!dijit.byId("max_scatter_points").isValid())
				return false;
			
			if (!dijit.byId("max_timeseries_points").isValid())
				return false;
			if (!dijit.byId("max_cluster_points").isValid())
				return false;
			
			
			return true;
		}
		
		function savePreferenceData(preferenceId) { 
			
			if(validatePreference()){
				
				var submitData = {
						id: preferenceId
				};
				submitData.preferenceItems = fill_Preference_fromUI(submitData);
				
	//		currentPreference_Open.preferenceItems=fill_Preference_fromUI(currentPreference_Open);
				
				//progressBar.showLoadingBar();
				
				for(var i = 0;i < currentPreferences.length;i++){
					if(currentPreferences[i].id == submitData.id){
						currentPreferences[i] = submitData;
					}
				}
				preferenceMgmt.savePreferenceData(submitData, savePreferenceCallback, savePreferenceCallback_error, constants.DIALOG_ID);
				return true;
			} else{
				popupComponent.alert(alpine.nls.MSG_Please_finish_input);
				return false;
			}
		}
		
		function restorePreferenceData (){
			//progressBar.showLoadingBar();
			preferenceMgmt.restorePreferenceData(currentPrefTreeItem_Open.id,restorePreferenceCallback,savePreferenceCallback_error, constants.DIALOG_ID);
		}
		
		function restorePreferenceCallback(preference) {
			//progressBar.closeLoadingBar(); //initPreferenceUI
			if(preference.error_code){
				handle_error_result(preference);
				return ;
			}
			
			if(preference.id=="alg"){
				initAlgPrefUI(preference);
			}else if(preference.id=="db"){
				initDBPrefUI(preference);
			}else if(preference.id=="ui"){
				initUIPrefUI(preference);
			}
			else if(preference.id=="sys"){
				initSYSPrefUI(preference);
			}
			
			
			
		}
		
		function savePreferenceCallback_error(text, args){
			
			//progressBar.closeLoadingBar();
//			popupComponent.alert(text);
		} 
		
		function fill_Preference_fromUI(preference) { 
			
			
			var preferenceItems = new Object;
			if(preference.id=="alg"){
				preferenceItems.distinct_value_count = dojo.byId("distinct_value_count").value ; 
				preferenceItems.va_distinct_value_count = dojo.byId("va_distinct_value_count").value ; 
				preferenceItems.decimal_precision = dojo.byId("decimal_precision").value ; 
				
			}else if(preference.id=="db"){
				preferenceItems.connection_timeout = dojo.byId("connection_timeout").value ;
				preferenceItems.hd_local_data_size_threshold = dijit.byId("hd_local_data_size_threshold").get("value");
				preferenceItems.add_outputtable_prefix = ""+dojo.byId("add_outputtable_prefix").checked; 
				
			} 
			else if(preference.id=="ui"){
				//make it a string , otherwise a Boolean will cause equals problem
				preferenceItems.max_table_lines = dojo.byId("max_table_lines").value ;  
				
				preferenceItems.max_scatter_points = dojo.byId("max_scatter_points").value ;
				preferenceItems.max_timeseries_points = dojo.byId("max_timeseries_points").value ;
				preferenceItems.max_cluster_points = dojo.byId("max_cluster_points").value ;
				
			}
			else if(preference.id=="sys"){
				preferenceItems.debug_level = dojo.byId("debug_level").value ;  
			} 	
			
			
			return preferenceItems;
			
		}
		
		function select_alg_profiles(){
			select_profiles_treenode("alg") ;
		}
		
		function select_sys_profiles(){
			select_profiles_treenode("sys") ;
		}
		
		function select_ui_profiles(){
			select_profiles_treenode("ui") ;
		}
		
		function select_db_profiles(){
			select_profiles_treenode("db") ;
		}
		
		function select_profiles_treenode(name){
			if(preference_tree){
				
				var items=preference_tree.get("model").store._arrayOfTopLevelItems ;
				for(var i =0 ;i<items.length;i++){
					if(items[i]&&items[i].id==name){
						seletePrefTreeNode(preference_tree,items[i]);
					}
				}
			}
		}
		
});

