/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopPigExecuteFileStructure
 * Author: Will
 * Date: 12-10-10
 */
define(["alpine/props/HadoopDataTypeUtil"],function(hdTypeUtil){
 var DIALOG_ID = "hadoopPigExecuteFileStructureCfgDlg";
 var DIALOG_BTN_OK = "hadoopPigExecuteFileStructure_Dlg_Btn_OK";
 var DIALOG_BTN_CANCEL = "hadoopPigExecuteFileStructure_Dlg_Btn_Cancel";

 var QUOTECHAR = "hadoop_pigexecute_quote";
 var ESCAPECHAR = "hadoop_pigexecute_escape";
 var OTHERVALUE = "hadoop_pigexecute_other_value";
 var INCLUDEHEADER = "hadoop_pigexecute_includeHeader";
 var OHTERVALUE = "hadoop_pigexecute_other_value";
 var GRIDCONTAINER = "columnDefineGridContainer";
 var hadoopDataType = hdTypeUtil.getAllHadoopTypes();
 //var hadoopDataType = ["chararray","int","long","float","double","bytearray"];

 var BTN_ADD = "hadoop_pigexecute_column_add";
 var BTN_DELETE = "hadoop_pigexecute_column_delete";
 var BTN_MOVEUP = "hadoop_pigexecute_column_moveup";
 var BTN_MOVEDOWN = "hadoop_pigexecute_column_movedown";

 var GRID_ID = "pigExecuteColumnDefineGrid";


 var fileStructrueModel = null;

 dojo.ready(function(){
     dojo.connect(dijit.byId(DIALOG_BTN_OK),"onClick",okBtn);
     dojo.connect(dijit.byId(DIALOG_BTN_CANCEL),"onClick",cancelBtn);

     dojo.connect(dijit.byId(QUOTECHAR),"onKeyUp",validateEscape_Quote);
     //dojo.connect(dijit.byId(QUOTECHAR),"onChange",validateEscape_Quote);
     dojo.connect(dijit.byId(ESCAPECHAR),"onKeyUp",validateEscape_Quote);
     //dojo.connect(dijit.byId(ESCAPECHAR),"onChange",validateEscape_Quote);
     dojo.connect(dijit.byId(OTHERVALUE),"onKeyUp",validateEscape_Quote);

     dojo.forEach(dojo.query("input[name='Pig_Separator_Type']"),function(idx,itm){
         dojo.connect(itm,"onchange",changeOtherStatus);
     });

     dojo.connect(dijit.byId(BTN_ADD),"onClick",addItem);
     dojo.connect(dijit.byId(BTN_DELETE),"onClick",deleteItem);
     dojo.connect(dijit.byId(BTN_MOVEUP),"onClick",moveupItem);
     dojo.connect(dijit.byId(BTN_MOVEDOWN),"onClick",movedownItem);
 });

 function showDialog(prop){
     var resultsLocation = dijit.byId("resultsLocation"+ID_TAG).get("value");
     var resultsName = dijit.byId("resultsName"+ID_TAG).get("value");
     if(resultsLocation==null || resultsName==null || dojo.trim(resultsLocation)=="" || dojo.trim(resultsName)==""){
         popupComponent.alert(alpine.nls.hadoop_pigexecute_choose_file_tip);
         return false;
     }

     var dlg = dijit.byId(DIALOG_ID);
     if(null!=dlg){
    	 dlg.titleBar.style.display = "none";
         dlg.show();
     }
     //
     if(null!=prop && null==prop.csvFileStructureModel){
         prop.csvFileStructureModel = initEmptyStructModel();
     }
     fileStructrueModel = prop.csvFileStructureModel;
     var dataItems = [];
     if(fileStructrueModel!=null && fileStructrueModel.columnNameList!=null && fileStructrueModel.columnTypeList!=null){
         for(var i=0;i<fileStructrueModel.columnNameList.length;i++){
             dataItems.push({
                 id:"item_"+i,
                 columnName:fileStructrueModel.columnNameList[i],
                 columnType:fileStructrueModel.columnTypeList[i]
             });
         }
     }
     initStatus();
     buildColumnGrid(dataItems);
 };

 function hideDialog(){
     var dlg = dijit.byId(DIALOG_ID);
     if(null!=dlg){
         dlg.hide();
     }
 };

  function initEmptyStructModel(){
        fileStructrueModel =null;
        if(fileStructrueModel==null){
            fileStructrueModel={}
        }
        if(fileStructrueModel.isFirstLineHeader==null){
            fileStructrueModel.isFirstLineHeader="";
        }
        if(null==fileStructrueModel.delimiter || fileStructrueModel.delimiter==""){
            fileStructrueModel.delimiter="Tab";
        }
        if(null==fileStructrueModel.columnNameList){
            fileStructrueModel.columnNameList=[];
        }
        if(null==fileStructrueModel.columnTypeList){
            fileStructrueModel.columnTypeList = [];
        }
        return fileStructrueModel;
    };

 function cancelBtn(){
     hideDialog();
 };

function initStatus(){
    var locationValue = dijit.byId("resultsLocation"+ID_TAG).get("value");
    var fileName = dijit.byId("resultsName"+ID_TAG).get("value");
    if(locationValue!=null && fileName!=null){
        dojo.byId("pigExecFileNameLabel").innerHTML=locationValue+"/"+fileName;
    }
    var escapeValue = fileStructrueModel.escapChar;
    var quoteValue = fileStructrueModel.quoteChar;
    if(escapeValue==null){
        escapeValue = "";
    }
    if(quoteValue==null){
        quoteValue = "";
    }
    dijit.byId(ESCAPECHAR).set("value",escapeValue);
    dijit.byId(QUOTECHAR).set("value",quoteValue);

    if(fileStructrueModel.isFirstLineHeader!=null && fileStructrueModel.isFirstLineHeader=="True"){
        dijit.byId(INCLUDEHEADER).set("checked",true);
    }else{
        dijit.byId(INCLUDEHEADER).set("checked",false);
    }

    var delimiters = dojo.query("td input[name='Pig_Separator_Type']");

    if(delimiters!=null && delimiters.length>0){
        for(var i=0;i<delimiters.length;i++){
            delimiters[i].checked=false;
            if(delimiters[i].value==fileStructrueModel.delimiter){
                delimiters[i].checked=true;
                if(delimiters[i].checked==true && delimiters[i].value=="Other"){
                      dijit.byId(OHTERVALUE).set("disabled",false);
                      dijit.byId(OHTERVALUE).set("value",fileStructrueModel.other);
                }
            }
        }
    }
}


 function okBtn(){
     var escape = dijit.byId(ESCAPECHAR).get("value");
     var quote = dijit.byId(QUOTECHAR).get("value");
     var storeItems = getGridStoreItems();
 //validate
     if(!(escape==null && quote==null)
         && !(escape=="" && quote=="")
         && !(escape.length==1 && quote.length==1)){
         popupComponent.alert(alpine.nls.hadoop_prop_file_structure_escap_quote_char_tip);
         return false;
     }

     if((escape.length==1 && quote.length==1) && escape==quote){
         popupComponent.alert(alpine.nls.hadoop_prop_file_structure_escap_quote_equal_tip);
         return false;
     }

     if(dojo.byId(OHTERVALUE).disabled==false && (null==dojo.byId(OHTERVALUE).value || dojo.byId(OHTERVALUE).value=="")){
         popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tip_Other);
         return false;
     }

     if(_validateColumnData(storeItems)==false){
         popupComponent.alert(alpine.nls.hadoop_pigexecute_grid_empty_tip);
         return false;
     }

     var validateArray = dojo.clone(storeItems);
     validateArray.sort(function(arg1,arg2){
         if(arg1.columnName>arg2.columnName){
              return 1;
         }else if(arg1.columnName==arg2.columnName){
             return 0;
         }else{
             return -1;
         }
     });
     for(var i=0;i<validateArray.length-1;i++){
         if(validateArray[i].columnName==validateArray[i+1].columnName){
             popupComponent.alert(alpine.nls.hadoop_pigexecute_grid_column_same_tip);
             return false;
         }
     }

 //set value
     fileStructrueModel.escapChar = escape;
     fileStructrueModel.quoteChar = quote;
     fileStructrueModel.other = "";

     if(dijit.byId(INCLUDEHEADER)!=null && dijit.byId(INCLUDEHEADER).get("checked")==true){
         fileStructrueModel.isFirstLineHeader = "True";
     }else{
         fileStructrueModel.isFirstLineHeader = "False";
     }

     var delimiters = dojo.query("td input[name='Pig_Separator_Type']");

     if(delimiters!=null && delimiters.length>0){
         for(var i=0;i<delimiters.length;i++){
             if(delimiters[i].checked==true && delimiters[i].value!="Other"){
                 fileStructrueModel.delimiter = delimiters[i].value;
             }else if(delimiters[i].checked==true && delimiters[i].value=="Other"){
                 fileStructrueModel.delimiter = delimiters[i].value;
                 fileStructrueModel.other = dijit.byId(OHTERVALUE).value;
             }
         }
     }


     if(null!=storeItems && storeItems.length>0){
         //validate columnName dup
         fileStructrueModel.columnNameList = [];
         fileStructrueModel.columnTypeList = [];
         for(var i=0;i<storeItems.length;i++){
             fileStructrueModel.columnNameList.push(storeItems[i].columnName);
             fileStructrueModel.columnTypeList.push(storeItems[i].columnType);
         }
     }

     updateBtnValid();
     hideDialog();
 }

 function updateBtnValid(){
     var btn = dijit.byId("pigExecuteFileStructure"+ID_TAG);
    if(null!=btn){
        setButtonBaseClassValid("pigExecuteFileStructure"+ID_TAG);
    }
 }

 function _validateColumnData(dataItems){
   if(null==dataItems || dataItems.length==0){
       return false;
   }
   if(dataItems.length>0){
     for(var i=0;i<dataItems.length;i++){
         if(dataItems[i].columnName==""){
             return false;
         }
     }
   }
   return true;
 }

 function  validateEscape_Quote(event){
        var inputValue = this.get("value");
        if(null!=inputValue && ""!=inputValue){
            var reg = /^[\W]$/;
            if(reg.test(inputValue)==false){
                this.set("value","");
            }
        }
        //dojo.stopEvent(event);
 };

 function getColumnGridStructure(){
     return [{type: "dojox.grid._CheckBoxSelector"},
         [

             {'name': alpine.nls.table_column_grid_head_columnName, 'field': 'columnName',editable:true,
                 //singleClickEdit:true,
                 type:dojox.grid.cells._Widget,
                 widgetClass: dijit.form.ValidationTextBox,
                 widgetProps:{
                     required:false,
                     regExp:"[a-zA-Z][\\w]*",
                     onChange:function(){
                         //will to do sth
                         //console.log(currentEditItem);
                         //updateConditionGridAliasColumn(currentEditItem,this.get('value'));
                     }
                 },
                 width: '50%',height:'20px'},
             {'name': alpine.nls.table_column_grid_head_columnType, 'field': 'columnType',editable:true,
             type:dojox.grid.cells._Widget,
             widgetClass:dijit.form.Select,
             widgetProps:{
                 required:false,
                 //regExp:"[a-zA-Z][\\w]*",
                 options:[
                     {label:"chararray",value:"chararray"},
                     {label:"int",value:"int"},
                     {label:"long",value:"long"},
                     {label:"float",value:"float"},
                     {label:"double",value:"double"},
                     {label:"bytearray",value:"bytearray"}
                 ],
                 baseClass:"greyDropdownButton",
                 style:"width:150px;"
             },
             'width': '50%'}
         ]];
 };
function getSelectOption(){
    var selectOptions = [];
    for(var i=0;i<hadoopDataType.length;i++){
        selectOptions.push({
            label:selectOptions[i],
            value:selectOptions[i]
        });
    }
    return selectOptions;
}
function getColumnGridStore(dataItems){
    if(null==dataItems){dataItems=[];}
    return new dojo.data.ItemFileWriteStore({data: {
        identifier: 'id',
        items: dataItems
    }});
};

function buildColumnGrid(dataItems){
    var gridStructure = getColumnGridStructure();
    var gridStore = getColumnGridStore(dataItems);

    var grid = dijit.byId(GRID_ID);
    if(grid==null){
        grid = new dojox.grid.DataGrid({
            id:GRID_ID,
            store: gridStore,
            structure: gridStructure,
            //selectionMode:"single",
            style:"height:298px;with:100%",
            canSort: function(){return false;},
            onRowClick:function(event){return false;},
            onSelectionChanged:function(){
                //this.selection.toggleSelect(event.rowIndex);
            }

        },dojo.create('div',null,GRIDCONTAINER));
        grid.startup();
    }else{
        grid.setStore(gridStore);
    }

};

function changeOtherStatus(event){
    if(event.target.value=="Other"){
        dijit.byId(OHTERVALUE).set("disabled",false);
    }else if(event.target.value=="Tab" ||
        event.target.value=="Comma" ||
        event.target.value=="Semicolon" ||
        event.target.value=="Space" ){
        dijit.byId(OHTERVALUE).set("disabled",true);
        //dojo.byId("hadoop_pigexecute_other_value").disabled = "disabled";
    }
};
 function addItem(){
   //alert("add");
     var storeItems = getGridStoreItems();
     storeItems.push({
         id:storeItems.length,
         columnName:"",
         columnType:"chararray"
     });
     var newDataStore = getColumnGridStore(storeItems);
     var grid = dijit.byId(GRID_ID);
     if(null!=grid){
         grid.setStore(newDataStore);
         grid.render();
     }
 };
 function deleteItem(){
     var grid = dijit.byId(GRID_ID);
     if(grid!=null){
         grid.removeSelectedRows();
         grid.render();
     }
 };

 function moveupItem(){
     var grid = dijit.byId(GRID_ID);
     var selectStatus = grid.selection.selected;
     var selectItem = grid.selection.getSelected();
     if(null!=selectItem && selectItem.length==1 && selectItem[0]!=null){
         for(var i=0;i<selectStatus.length;i++){
             if(selectStatus[i]==true){
                 if(i>0){
                     var storeItems = getGridStoreItems();
                     var tempValue = storeItems[i-1];
                     storeItems[i-1] = storeItems[i];
                     storeItems[i] = tempValue;
                     var newDataStore = getColumnGridStore(storeItems);
                     grid.setStore(newDataStore);
                     grid.selection.select(i-1);
                 }
                 break;
             }
         }
     }
 };

 function movedownItem(){
     var grid = dijit.byId(GRID_ID);
     var selectStatus = grid.selection.selected;
     var selectItem = grid.selection.getSelected();
     if(null!=selectItem && selectItem.length==1 && selectItem[0]!=null){
         var storeItems = getGridStoreItems();
         for(var i=0;i<storeItems.length;i++){
             if(selectStatus[i]==true){
                 if(i<(storeItems.length-1)){
                     var tempValue = storeItems[i+1];
                     storeItems[i+1] = storeItems[i];
                     storeItems[i] = tempValue;
                     var newDataStore = getColumnGridStore(storeItems);
                     grid.setStore(newDataStore);
                     grid.selection.select(i+1);
                 }
                 break;
             }
         }
     }
 };

 function getGridStoreItems(){
     var storeItems = [];
     var grid = dijit.byId(GRID_ID);
     if(null!=grid && grid.store!=null){
         var realStroeItems = grid.store._arrayOfAllItems;
         if(null!=realStroeItems){
             for(var i=0;i<realStroeItems.length;i++){
                 if(realStroeItems[i]!=null){
                     storeItems.push({
                          id:"item_"+i,
                         columnName:realStroeItems[i].columnName[0],
                         columnType:realStroeItems[i].columnType[0]
                     }) ;
                 }
             }
         }
     }
     return storeItems;
 }

 return {
     showExecuteFileStructureDlg:showDialog
 };
});