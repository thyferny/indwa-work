/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: SystemUpdateHelper
 * Author: Will
 * Date: 12-12-7
 */

define([ "dojo/dom-attr",
    "dojo/dom-style",
    "dojo/on",
    "dijit/registry",
    "alpine/system/SystemUpdateManager"
],function(attrDOM,styleDOM,on,
    registry,sysManager){

    var constants = {
        MENU_BUTTON: "alpine_system_systemupdate_menu",
        DIALOG_ID:"apline_system_update_dialog",
        DIALOG_OK:"system_update_Dlg_Btn_OK",
        DIALOG_CANCEL:"system_update_Dlg_Btn_Cancel",
        UPDATE_LIST_CONTAINER:"available_update_list_container",
        CURRENT_RUNNING_LIST_CONTAINER:"current_running_flow_list_container",
        UPDATE_LIST_GRID_CONTAINER:"available_update_list_grid_container",
        RUNNING_LIST_GRID_CONTAINER:"current_running_flow_list_grid_container",
        UPDATE_LIST_ID:"available_update_list_grid",
        RUNNING_LIST_ID:"current_running_flow_list_grid",
        STACK_CONTAINER:"system_update_stack_container",
        TIP_DIALOG:"apline_system_restart_dialog",
        TIP_MESSAGE:"system_update_restart_tip_container_tip",
        UPDAE_TIME_OUT:120
    };

    var updateModel = null;

    dojo.ready(function () {

        if (alpine.system.PermissionUtil.checkPermission("UPDATE_SYSTEM") == true) {
            on(registry.byId(constants.MENU_BUTTON), "click", showSysUpdateDlg);
            on(registry.byId(constants.DIALOG_OK), "click", clickOK);
            on(registry.byId(constants.DIALOG_CANCEL), "click", clickCancel);
            alpine.system.SystemUpdateManager.haveNewUpdateVersion(function (data) {
//                if (data.hasNewVersion == true) {
//                    var accept = {label:alpine.nls.system_update_tip_button_ok, handle:alpine.system.SystemUpdateHelper.showSysUpdateDlg};
//                    var cancel = {label:alpine.nls.system_update_tip_button_cancel};
//                    popupComponent.confirm(alpine.nls.system_update_tip_info, "System update tip", accept, cancel, "width:400px;");
//                }
                if(data!=null && data.hasNewVersion==true){
                    var message = alpine.nls.system_update_tip_info+"<br />"+'<a href="#" onclick="alpine.system.SystemUpdateHelper.showSysUpdateDlg();return false;">'+alpine.nls.system_update_tip_button_ok+'</a>';
                    message = message +"&nbsp;&nbsp;&nbsp;"+'<a href="#" onclick="return false;">'+alpine.nls.system_update_tip_button_cancel+'</a>';
                    dojo.publish("toasterMessage_ru", [{
                        message:message,
                        type: "message",
                        duration: "30000"
                    }]);
                }

            }, null);
        }
    });

    function showSysUpdateDlg(){
        sysManager.getUpdateFileInfos(_getFileInfoCallback, null, constants.DIALOG_ID);
    }

    //update list
    function _buildUpdateListGrid(dataItems){
        var gridLayout = _buildUpdateGrid_Layout();
        var gridStore = _buildUpdateGrid_Stroe(dataItems);
        var grid = dijit.byId(constants.UPDATE_LIST_ID);
        if(grid==null){
            grid = new dojox.grid.DataGrid({
                id:constants.UPDATE_LIST_ID,
                store: gridStore,
                structure: gridLayout,
                selectionMode:"single",
                style:"height:290px;with:458px",
                canSort: function(){return false;}
                //onRowClick:function(event){return false;}
            },dojo.create('div',{style:"height:290px;with:458px"},constants.UPDATE_LIST_GRID_CONTAINER));
            grid.startup();
        }else{
            grid.setStore(gridStore);
            grid.render();
        }
    }

    function _buildUpdateInfo(data){
        if(data!=null){
            updateModel = data;
            dojo.byId("system_update_grid_title_version").innerHTML =dojo.trim(data.version);
            dojo.byId("system_update_grid_title_date").innerHTML =dojo.trim(data.pubDate);
            dojo.byId("system_update_grid_title_descrip").innerHTML = dojo.trim(data.descrip);
        }
    }

    function _buildUpdateGrid_Layout(){
      return [//{type: "dojox.grid._CheckBoxSelector"},
          [
              {'name': "Version", 'field': 'fileVersion',width:"100%",height:'20px'},
              {'name': "Publish Date", 'field': 'pubDate',width:"100%",height:'20px'},
              {'name': "Descrip", 'field': 'fileDescrip',width:"100%",height:'20px'}

          ]];
    }
   /*
   * dataitems:[{
   * id:"",
   * fileName,
   * fileVersion
   * pubDate,
   * fileDescrip
   * },...]
   * */
    function _buildUpdateGrid_Stroe(dataItems){
        if(null==dataItems){dataItems=[];}
        return new dojo.data.ItemFileWriteStore({data: {
            identifier: 'id',
            items: dataItems
        }});
    }
    //running grid
    function _buildRunningList(dataItems) {
        var layout = _buildRunningList_layout();
        var gridStore = _buildRunningList_store(dataItems);
        var grid = dijit.byId(constants.RUNNING_LIST_ID);
        if (grid == null) {
            grid = new dojox.grid.DataGrid({
                id:constants.RUNNING_LIST_ID,
                store:gridStore,
                structure:layout,
                selectionMode:"single",
                style:"height:290px;with:458px",
                canSort:function () {
                    return false;
                },
                onRowClick:function (event) {
                    return false;
                }
            }, dojo.create('div', {style:"height:290px;with:458px"}, constants.RUNNING_LIST_GRID_CONTAINER));
            grid.startup();
        } else {
            grid.setStore(gridStore);
            grid.render();
        }

    }

    function _buildRunningList_layout(){
        return [//{type: "dojox.grid._CheckBoxSelector"},
            [
                {'name': "Owner", 'field': 'fileOwner',width:"100%",height:'20px'},
                {'name': "Flow Name", 'field': 'fileName',width:"100%",height:'20px'}
            ]];
    }

    function _buildRunningList_store(dataItems){
        if(null==dataItems){dataItems=[];}
        return new dojo.data.ItemFileWriteStore({data: {
            identifier: 'id',
            items: dataItems
        }});
    }

    function _getFileInfoCallback(data){
//         var fileInfos = _getFileInfos(data);
//        _buildUpdateListGrid(fileInfos);
    	if(data != null){
            var dlg = dijit.byId(constants.DIALOG_ID);
            if(dlg!=null){
                dlg.show();
                styleDOM.set(dlg.titleBar, "display", "none");
            }
             dijit.byId(constants.DIALOG_OK).set("disabled",false);
             clearGridInfos();
             //dijit.byId(constants.STACK_CONTAINER).forward();
             dijit.byId(constants.STACK_CONTAINER).selectChild(constants.UPDATE_LIST_CONTAINER,true);
//             dijit.byId(constants.STACK_CONTAINER).selectChild(constants.CURRENT_RUNNING_LIST_CONTAINER,true);
            _buildUpdateInfo(data);
    	}else{
    		popupComponent.alert(alpine.nls.system_update_tip_none_update);
    	}
    }

    /*
    * {
     "name": "aaa",
     "version": "1.1",
     "pubDate": "2012-12-01",
     "descrip": "aaaaaa"
     },
    * */
    function _getFileInfos(data){
        var infos = [];
        if(data!=null && data.length>0){
            for(var i=0;i<data.length;i++){
                if(data[i]!=null){
                    infos.push(
                        {
                            id:"item_"+i,
                            fileName:data[i].name,
                            fileVersion:data[i].version,
                            pubDate:data[i].pubDate,
                            fileDescrip:data[i].descrip
                        }
                    );
                }
            }
        }
        return infos;
    }

    function _getCurrentRunFlowInfoCallback(data){
        var dataItems = _getRunFlowDataItem(data);
        _buildRunningList(dataItems);
    }

    function _getRunFlowDataItem(data){
        var dataItems = [];
        var conter = 0;
        if(data!=null){
           for(var key in data){
               if(key!=null && data[key]!=null && data[key].length>0){
                 for(var i=0;i<data[key].length;i++){
                     dataItems.push(
                         {
                             id:"item"+conter++,
                             fileOwner:key,
                             fileName:data[key][i]
                         }
                     );
                 }
               }
           }
        }
        return dataItems;
    }

    function hideDialog(){
        var dlg = dijit.byId(constants.DIALOG_ID);
        if(dlg!=null){
            dlg.hide();
            updateModel = null;
            //styleDOM.set(dlg.titleBar, "display", "none");
        }
    }

    function clearGridInfos(){
        //_buildUpdateListGrid([]);
        _buildRunningList([]);
    }

    function clickOK(){
        //update system
        //var updateGrid = dijit.byId(constants.UPDATE_LIST_ID);

//        if(updateGrid!=null){
//            var selectItem = updateGrid.selection.getSelected();
//            if(null!=updateModel){
//                if(selectItem.length==0){
//                  popupComponent.alert(alpine.nls.system_update_select_item_tip)
//                   return false;
//                }
                sysManager.getCurrentRunFlowInfo(function(data){
                    //also judged data
                    _getCurrentRunFlowInfoCallback(data);
                    var runingGrid = dijit.byId(constants.RUNNING_LIST_ID);
                    var storeItems = runingGrid.store._arrayOfAllItems;
                    if(storeItems.length>0){
                        dijit.byId(constants.STACK_CONTAINER).selectChild(constants.CURRENT_RUNNING_LIST_CONTAINER,true);
                        //refresh
                        dijit.byId(constants.DIALOG_OK).set("disabled",true);
                        popupComponent.alert(alpine.nls.system_update_fail_tip_alert);
                        return false;
                    }
                    if(storeItems.length==0){
                       //get update serve
//                        var updateModel = {};
//                        updateModel.name = toolkit.getValue(selectItem[0].fileName);
//                        updateModel.version = toolkit.getValue(selectItem[0].fileVersion);
//                        updateModel.pubDate = toolkit.getValue(selectItem[0].pubDate);
//                        updateModel.descrip = toolkit.getValue(selectItem[0].fileDescrip);
                        sysManager.execuSystemUpdate(updateModel,systemUpdateOKCallback,systemUpdateErrorCallback, constants.DIALOG_ID);
                    }
                }, null, constants.DIALOG_ID);
//            }
//        }
        //hideDialog();
    }

    function systemUpdateOKCallback(data){
         if(data.info=="success"){
        	 dijit.byId(constants.DIALOG_ID).hide();
             var dlg = dijit.byId(constants.TIP_DIALOG);
             dlg.show();
             styleDOM.set(dlg.titleBar, "display", "none");
             var i = 0;
             var myProgressBar = new dijit.ProgressBar({
                 style: "width: 120px"
             }).placeAt(dojo.byId("system_update_restart_tip_container_tip"));
            var handler = setInterval(function(){
                 myProgressBar.set("value", i++);
                 if(myProgressBar.get("value")==120){
                     clearInterval(handler);
                     window.location.href = baseURL;
                 }
             }, 1000);
         }
    }


    function systemUpdateErrorCallback(){

    }

    function clickCancel(){
        hideDialog();
    }

    return {
        showSysUpdateDlg:showSysUpdateDlg
    };
});