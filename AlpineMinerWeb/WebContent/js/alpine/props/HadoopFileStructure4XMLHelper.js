/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopFileStructure4XMLHelper
 * Author: Will
 * Date: 12-10-26
 */
define([
    "alpine/props/HadoopFileStructure4XMLManager",
    "dojo/dom-attr",
    "dojo/dom-style",
    "dojo/dom-class",
    "dojo/query",
    "alpine/props/HadoopFileOperatorPropertyHelper",
    "alpine/props/HadoopFileStructure4LogHelper",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/util/DataTypeUtils"
],function(xmlManager,domAttr,domStyle,domClass,dojoQuery,fileOperatorHelper,logHelper,hdDataTypeUitl,dataTypeUtils){
    var DIALOG = "hadoopFileStructureCfgDlg4XML";
    var BTN_Cancel = "hadoopFileStructure_4XML_Dlg_Btn_Cancel";
    var BTN_Ok = "hadoopFileStructure_4XML_Dlg_Btn_OK";
    var DIALOG_SUB_TITLE = "hadoopPropery_Config_Colum_file_name4xml";
    var DEFINE_COLUMN_GRID_CONTAINER = "hadoop_prop_file_structure_xml_column_define_container";
    var DEFINE_COLUMN_GRID_ID = "hadoop_prop_file_structure_xml_column_define_column_grid";
    //var BTN_ADD = "hadoop_prop_file_structure_xml_add";
    var BTN_DELETE = "hadoop_prop_file_structure_xml_delete";
    var BTN_MOVEUP = "hadoop_prop_file_structure_xml_moveup";
    var BTN_MOVEDOWN = "hadoop_prop_file_structure_xml_movedown";
    var BTN_PREV = "hadoop_prop_file_structure_xml_preview";
    var BTN_LOAD_XML = "hadoop_prop_file_structure_xml_loadData";
    var MASK_ID = "mask_xmlfileStruct_column_define";
    var Hide_treeNode_select = "hadoop_file_struct_xml_tree_node_select";
    var Hide_treeNode_selectModel = null; //save select tree model
    var BTN_select_level = "hadoop_prop_file_structure_xml_tree_select_level";

    var CONTAINER = "hadoop_prop_file_structure_xml_input_container";
    //var ROOT = "hadoop_prop_file_structure_xml_input_root";
    var TreeContainer = "xmlDomTreeContainer";
    var XMLTree_ID="xmlDomTree_ID";
    var PreviewGridContainer = "hadoop_prop_file_structure_xml_preview_grid_container";
    var containMode_ID = "hadoop_prop_file_structure_xml_hidden_containerMode";
    var preview_container_mask = "mask_xmlfileStruct_preview";
    var hidden_CONTAINER_PATH = "hadoop_prop_file_structure_xml_hidden_containerPath";
    var sourceButtonId = null;

    var xmlFileStructureModel = null;

    var eventsHandler = [];
    var contetnPains = [];
    var nodeIDNum = 0;
    var currentProp = null;
    var inputStatus = false;
    var nodeNameCounter = {};
    var MAX_REPEAT_ROW = 25;
    var evaluateValueTypes = {};
    var GUESS_TYPE_MAX = 100;
    //For count node level info
    var nodeLevelInfo = {};


    dojo.ready(function(){
        dojo.connect(dijit.byId(BTN_Cancel),"onClick",btnCancelClick);
        dojo.connect(dijit.byId(BTN_Ok),"onClick",saveXMLFileStructureModel);

        //dojo.connect(dijit.byId(BTN_ADD),"onClick",function(){addItem();});
        dojo.connect(dijit.byId(BTN_DELETE),"onClick",deleteItem);
        dojo.connect(dijit.byId(BTN_MOVEUP),"onClick",moveupItem);
        dojo.connect(dijit.byId(BTN_MOVEDOWN),"onClick",movedownItem);
        dojo.connect(dijit.byId(BTN_LOAD_XML),"onClick",loadXMLData);
        dojo.connect(dijit.byId(BTN_PREV),"onClick",previewData);
        dojo.connect(dijit.byId(DIALOG),"onHide",onHideDestroySomth);

        dojo.connect(dijit.byId(CONTAINER),"onKeyUp",function(event){
            if(this.get("value")!=xmlFileStructureModel.container){
                buildDefineColumnGrid([]);
            }
            clearTreeDomNode();
            destroyPreviewGrid();

            Hide_treeNode_selectModel = null;
            dijit.byId(BTN_select_level).set("disabled",true);
            //empty the container mode
            dojo.byId(containMode_ID).value="";
            //input enter
            if(event.keyCode==dojo.keys.ENTER){
                inputStatus = true;
                loadXMLData();
                destroyPreviewGrid();
            }
        });

        dojo.connect(dijit.byId(BTN_select_level),"onClick",selectLevelBtnClick);
});

    function showStructureCfgXMLDialog(subtitle,prop){
       var dlg = dijit.byId(DIALOG);
       if(null!=dlg){
           dlg.show();
           dojo.style(dlg.containerNode,{width:"850px",height:"650px",overflow:"hidden"});
           domStyle.set(dlg.titleBar,"display","none");
           //dlg.titleBar.style.display = 'none';
           var tooltipValue = subtitle;
           if(subtitle!=null && subtitle.length>30){
               subtitle = subtitle.substring(0,27)+"...";
           }
           domAttr.set(dojo.byId(DIALOG_SUB_TITLE),"innerHTML",subtitle);
           domAttr.set(dojo.byId(DIALOG_SUB_TITLE),"title",tooltipValue);
           dijit.byId(BTN_select_level).set("disabled",true);
           if(null==prop.xmlFileStructureModel){
               prop.xmlFileStructureModel = {};
           }
           sourceButtonId = getSourceButtonId(prop);
           currentProp = prop;
           xmlFileStructureModel = prop.xmlFileStructureModel;
           //
           //destroy preview grids
           destroyPreviewGrid();
           evaluateValueTypes = {};
           //init dialog status
           var dataItems = initDialogStatus();
           buildDefineColumnGrid(dataItems);
           //load tree
           loadXMLData();
       }
    }

    function initDialogStatus(){
//        dijit.byId(ROOT).set("value","");
//        if(xmlFileStructureModel.root!=null){
//            dijit.byId(ROOT).set("value",xmlFileStructureModel.root);
//        }
        dijit.byId(CONTAINER).set("value","");
        if(xmlFileStructureModel.container!=null){
            dijit.byId(CONTAINER).set("value",xmlFileStructureModel.container);
        }

        dojo.byId(containMode_ID).value = "";
        if(xmlFileStructureModel.attrMode!=null){
            dojo.byId(containMode_ID).value = xmlFileStructureModel.attrMode;
        }

        //dojo.byId(hidden_CONTAINER_PATH).value = xmlFileStructureModel.containerJsonPath==null?"":jsonFileStructureModel.containerJsonPath;


        var dataItems = [];
        if(xmlFileStructureModel.columnNameList !=null && xmlFileStructureModel.columnNameList.length>0){
            for(var i=0;i<xmlFileStructureModel.columnNameList.length;i++){
                dataItems.push({
                    id:"item_"+i,
                    columnName:xmlFileStructureModel.columnNameList[i]!=null?xmlFileStructureModel.columnNameList[i]:"",
                    columnType:xmlFileStructureModel.columnTypeList[i]!=null?xmlFileStructureModel.columnTypeList[i]:"",
                    xpathValue:xmlFileStructureModel.xPathList[i]!=null?xmlFileStructureModel.xPathList[i]:"",
                    fromNodeType:""
                }) ;
            }
        }
       return dataItems;
    }

    function btnCancelClick(){
        hideDlg();
    }



    function clearTreeDomNode(){
        if(dojo.byId(TreeContainer).innerHTML!=""){
            dojo.empty(TreeContainer);
        }
    }

    function onHideDestroySomth(){
        eventsHandler = [];
        nodeNameCounter = {};
        evaluateValueTypes = {};
        var tree = dijit.byId(XMLTree_ID);
        if(tree!=null){
            tree.destroyRecursive();
        }
        dojo.empty(TreeContainer);
    }

    function saveXMLFileStructureModel(){
        //validate
//        var rootValue=dijit.byId(ROOT).get("value");
//        var containerValue = dijit.byId(CONTAINER).get("value");
//        if(rootValue==null || ""==dojo.trim(rootValue)){
//            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_xml_root_tip);
//            return false;
//        }
//        if(containerValue==null || ""==dojo.trim(containerValue)){
//            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_xml_container_tip);
//            return false;
//        }
        if(valideateColumnGrid()==false){
              return false;
        }
        alpine.spinner.showSpinner("hadoopFileStructureCfgDlg4XML");
        var gridItems = getGridStoreItems();
        //save model
        //xmlFileStructureModel.root = dijit.byId(ROOT).get("value");
        xmlFileStructureModel.container =dijit.byId(CONTAINER).get("value");
        xmlFileStructureModel.attrMode =dojo.byId(containMode_ID).value;
        xmlFileStructureModel.xPathList =[];
        xmlFileStructureModel.columnNameList =[];
        xmlFileStructureModel.columnTypeList =[];


        if(null!=gridItems && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i]!=null){
                    xmlFileStructureModel.xPathList.push(gridItems[i].xpathValue);
                    xmlFileStructureModel.columnNameList.push(gridItems[i].columnName);
                    xmlFileStructureModel.columnTypeList.push(gridItems[i].columnType);
                }
            }
        }
       //----------------validate dom xpath----------------------
        var hadoopConName = dijit.byId("connName"+ID_TAG).get("value");
        var connectionKey=fileOperatorHelper.getConnectionKey(hadoopConName);
        var path=dijit.byId("hadoopFileName"+ID_TAG).get("value");
        var eidtFlowInfo = alpine.flow.WorkFlowManager.getEditingFlow();
        var flowInfoKey = eidtFlowInfo.key;
        xmlManager.getPreviewData4xml(xmlFileStructureModel,connectionKey,path,flowInfoKey,null,function(){
            //success
            setButtonBaseClassValid(sourceButtonId);
            logHelper.clearStructueModel(currentProp,"xml");
            alpine.spinner.hideSpinner("hadoopFileStructureCfgDlg4XML");
            hideDlg();
        },function(){
            //error
            alpine.spinner.hideSpinner("hadoopFileStructureCfgDlg4XML");
            return false;
        });
        //----------------validate dom xpath end----------------------
    }



    function valideateColumnGrid(){
        var gridItems = getGridStoreItems();
        if(gridItems==null || gridItems.length==0 || haveEmptyValue(gridItems)==true){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_xml_grid_column_tip);
            return false;
        }
        //have different fromNodeType
//        if(haveDifferentNodeType(gridItems)==true){
//            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_xml_column_origin_tip);
//            return false
//        }
        //validate name
        if(isColumnValid(gridItems)==false){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_name_invalid_tip);
            return false;
        }

        //haveRepeat value
        var col = {columnName:""};
        if(haveRepeatColumnName(gridItems,col)==true){
            var errorMsg = alpine.nls.hadoop_prop_file_structure_xml_column_repeat_tip.replace("###","'"+col.columnName+"'");
            popupComponent.alert(errorMsg);
            return false;
        }


        if(hasHadoopKeyWord(gridItems)==true){
            return false;
        }

        //xpath invalidate
        col.columnName = "";
        if(validateXpathValue(gridItems,col)==false){
            var errorMsg = alpine.nls.hadoop_prop_file_structure_column_xpath_value_invalid_tip.replace("###",col.columnName);
            popupComponent.alert(errorMsg);
            return false;
        }
        return true;
    }

    function haveEmptyValue(gridItems){
        if(null!=gridItems && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i]!=null){
                   if(dojo.trim(gridItems[i].xpathValue)==""
                       || dojo.trim(gridItems[i].columnName)==""
                       || dojo.trim(gridItems[i].columnType)==""){
                       return true;
                   }
                }
            }
        }

    }

    function haveRepeatColumnName(gridItems,col){
        var nameList = [];
        if(null!=gridItems && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i]!=null){
                    nameList.push(dojo.trim(gridItems[i].columnName));
                }
            }
        }
        nameList.sort();
        for(i=0;i<nameList.length-1;i++){
            if(nameList[i]==nameList[i+1]){
                col.columnName = nameList[i];
                return true;
            }
        }
    }

    function isColumnValid(gridItems){
        var nameList = [];
        if(null!=gridItems && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i]!=null){
                    nameList.push(dojo.trim(gridItems[i].columnName));
                }
            }
        }
        //nameList.sort();
        for(i=0;i<nameList.length;i++){
            if(/^[a-zA-Z].*/.test(nameList[i])==false){
                return false;
            }
        }
       return true;
    }

    function validateXpathValue(gridItems,col){
        if(gridItems!=null){
            for(var i=0;i<gridItems.length;i++){
               var xpathNodeValues = [];
               var xpathValue = gridItems[i].xpathValue.replace("//","");
                xpathNodeValues = xpathValue.split("/");
                if(dijit.byId(CONTAINER).get("value")!=""){
                    xpathNodeValues.push(dijit.byId(CONTAINER).get("value"));
                }
                if(null!=xpathNodeValues && xpathNodeValues.length>0){
                    xpathNodeValues.sort();
                    for(var j=0;j<xpathNodeValues.length-1;j++){
                        if(xpathNodeValues[j]==xpathNodeValues[j+1]){
                            col.columnName = xpathNodeValues[j];
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    function haveDifferentNodeType(gridItems){
        var fromNodeTypes= [];
        if(gridItems!=null && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i].xpathValue!="" && gridItems[i].xpathValue.indexOf("@")!=-1){
                    fromNodeTypes.push("attr");
                }else{
                    fromNodeTypes.push("node");
                }
            }
        }
        fromNodeTypes.sort();
        for(var i=0;i<fromNodeTypes.length-1;i++){
            if(fromNodeTypes[i]!=fromNodeTypes[i+1]){
                return true;
            }
        }

        return false;
    }

    function previewData(){
       if(valideateColumnGrid()==false){
            return false;
        }
        var gridItems = getGridStoreItems();

        //save model
        var previewModel = {};
        //previewModel.root = dijit.byId(ROOT).get("value");
        previewModel.container =dijit.byId(CONTAINER).get("value");
        previewModel.attrMode = dojo.byId(containMode_ID).value;
        previewModel.xPathList =[];
        previewModel.columnNameList =[];
        previewModel.columnTypeList =[];


        if(null!=gridItems && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i]!=null){
                    previewModel.xPathList.push(gridItems[i].xpathValue);
                    previewModel.columnNameList.push(gridItems[i].columnName);
                    previewModel.columnTypeList.push(gridItems[i].columnType);
                }
            }
        }
        //var connectionKey=current_op.connectionName;
        var hadoopConName = dijit.byId("connName"+ID_TAG).get("value");
        var connectionKey=fileOperatorHelper.getConnectionKey(hadoopConName);
        var path=dijit.byId("hadoopFileName"+ID_TAG).get("value");
        var targetNode = dojo.byId(PreviewGridContainer);
        //destroy preview grids
        destroyPreviewGrid();
        var contentPine =  new dijit.layout.ContentPane({
                content: "&nbsp",
                style: "height:190px;width:98%"
            });
        contetnPains.push(contentPine);
        dojo.place(contentPine.domNode,targetNode,"only");
        dojo.byId(preview_container_mask).style.display="";
        var eidtFlowInfo = alpine.flow.WorkFlowManager.getEditingFlow();
        var flowInfoKey = eidtFlowInfo.key;
		xmlManager.getPreviewData4xml(previewModel,connectionKey,path,flowInfoKey,contentPine,fillOutPaneDataTable);
    }

    function destroyPreviewGrid(){
        if(contetnPains.length>0){
            for(i=0;i<contetnPains.length;i++){
                if(contetnPains[i].destroyRecursive){
                    contetnPains[i].destroyRecursive();
                }
            }
            contetnPains = [];
        }
    }


    function hideDlg(){
        var dlg = dijit.byId(DIALOG);
        if(null!=dlg){
            dlg.hide();
        }
    }

    function buildDefineColumnGrid(dataItems){
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
        if(dojo.byId(MASK_ID).style.display==""){
            dojo.byId(MASK_ID).style.display="none";
        }
        var gridStore = getXMLDefineColumnGridStore(dataItems);
        var gridStructure = getXMLDefineColumnGridStructure();
        if(grid==null){
            grid = new dojox.grid.DataGrid({
                id:DEFINE_COLUMN_GRID_ID,
                store: gridStore,
                structure: gridStructure,
                //selectionMode:"single",
                style:"height:172px;with:390px",
                canSort: function(){return false;},
                onRowClick:function(event){return false;},
                onSelectionChanged:function(){
                    //this.selection.toggleSelect(event.rowIndex);
                   var selected = this.selection.selected;
                   var selectCount = 0;
                    for(var i=0;i<selected.length;i++){
                        if(selected[i]==true){
                            selectCount++;
                        }
                    }
                   if(selectCount==1){
                      dijit.byId(BTN_DELETE).set("disabled",false);
                      dijit.byId(BTN_MOVEUP).set("disabled",false);
                      dijit.byId(BTN_MOVEDOWN).set("disabled",false);
                   }
                   if(selectCount>1){
                       dijit.byId(BTN_DELETE).set("disabled",false);
                       dijit.byId(BTN_MOVEUP).set("disabled",true);
                       dijit.byId(BTN_MOVEDOWN).set("disabled",true);
                   }
                   if(selectCount==0){
                       dijit.byId(BTN_DELETE).set("disabled",true);
                       dijit.byId(BTN_MOVEUP).set("disabled",true);
                       dijit.byId(BTN_MOVEDOWN).set("disabled",true);
                   }

                }

            },dojo.create('div',{style:"width:390px;height:175px"},DEFINE_COLUMN_GRID_CONTAINER));
            grid.startup();
        }else{
            grid.setStore(gridStore);
            grid.render();
        }

    }
     /*
     * dataItems = {
     *  id:number,
     *  columnName:string,
     *  columnType:string,
     *  xpathValue:string
     * }
     * */
    function getXMLDefineColumnGridStore(dataItems){
        if(null==dataItems){dataItems=[];}
        return new dojo.data.ItemFileWriteStore({data: {
            identifier: 'id',
            items: dataItems
        }});
    }

    function getXMLDefineColumnGridStructure(){
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
                    width: '30%',height:'20px'},
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
                    'width': '30%'},
                {'name': alpine.nls.hadoop_prop_file_structure_xml_grid_column_xpath, 'field': 'xpathValue',width: '40%',height:'20px'}
//                {'name': alpine.nls.hadoop_prop_file_structure_xml_grid_column_xpath, 'field': 'xpathValue',editable:true,
//                    //singleClickEdit:true,
//                    type:dojox.grid.cells._Widget,
//                    widgetClass: dijit.form.ValidationTextBox,
//                    widgetProps:{
//                        required:false,
//                        //regExp:"[a-zA-Z][\\w]*",
//                        onChange:function(){
//                            //will to do sth
//                            //console.log(currentEditItem);
//                            //updateConditionGridAliasColumn(currentEditItem,this.get('value'));
//                        }
//                    },
//                    width: '40%',height:'20px'}
            ]];
    }

    function replaceAllInvalidStartChar(replaceChar){
        if(replaceChar!=null && /^[a-zA-Z][\w]*/.test(replaceChar)==false){
            replaceChar = replaceChar.substring(1);
            if(/^[a-zA-Z][\w]*/.test(replaceChar)==false){
                replaceChar = replaceAllInvalidStartChar(replaceChar);
            }
       }
        return replaceChar;
    }

    function addItem(itemObj){
        //alert("add");
        if(itemObj==null){
            itemObj={
                columnName:"",
                columnType:"chararray",
                xpathValue:"",
                fromNodeType:""
            };
        }
        var storeItems = getGridStoreItems();
//        for(var i=0;i<storeItems.length;i++){
//            if(storeItems[i].columnName!=""
//                && storeItems[i].columnName==itemObj.columnName){
//                popupComponent.alert( alpine.nls.hadoop_prop_file_structure_xml_column_exist_tip);
//                return false;
//            }
//        }
        storeItems.push({
            id:storeItems.length,
            columnName:itemObj.columnName,
            columnType:itemObj.columnType,
            xpathValue:itemObj.xpathValue,
            fromNodeType:itemObj.fromNodeType
        });
        var newDataStore = getXMLDefineColumnGridStore(storeItems);
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
        if(null!=grid){
            grid.setStore(newDataStore);
            grid.render();
        }
    }
    function deleteItem(){
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
        if(grid!=null){
            grid.removeSelectedRows();
            grid.render();
        }
    }

    function moveupItem(){
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
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
                        var newDataStore = getXMLDefineColumnGridStore(storeItems);
                        grid.setStore(newDataStore);
                        grid.selection.select(i-1);
                    }
                    break;
                }
            }
        }
    }

    function movedownItem(){
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
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
                        var newDataStore = getXMLDefineColumnGridStore(storeItems);
                        grid.setStore(newDataStore);
                        grid.selection.select(i+1);
                    }
                    break;
                }
            }
        }
    }

    function getGridStoreItems(){
        var storeItems = [];
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
        if(null!=grid && grid.store!=null){
            var realStroeItems = grid.store._arrayOfAllItems;
            if(null!=realStroeItems){
                for(var i=0;i<realStroeItems.length;i++){
                    if(realStroeItems[i]!=null){
                        storeItems.push({
                            id:"item_"+i,
                            columnName:realStroeItems[i].columnName[0],
                            columnType:realStroeItems[i].columnType[0],
                            xpathValue:realStroeItems[i].xpathValue[0],
                            fromNodeType:realStroeItems[i].fromNodeType[0]
                        }) ;
                    }
                }
            }
        }
        return storeItems;
    }



    function loadXMLData(){
        //xmlFileStructureModel.root = dijit.byId(ROOT).get("value");
       //xmlFileStructureModel.container =dijit.byId(CONTAINER).get("value");
//        var rootValue = dijit.byId(ROOT).get("value");
        dojo.empty(TreeContainer);
        dojo.empty("hadoop_prop_file_structure_xml_container_gess_tip");
        var containerValue = dijit.byId(CONTAINER).get("value");
        var hadoopConName = dijit.byId("connName"+ID_TAG).get("value");
        var connectionKey=fileOperatorHelper.getConnectionKey(hadoopConName);
        var path=dijit.byId("hadoopFileName"+ID_TAG).get("value");
        var targetNode = dojo.byId(TreeContainer);
       dojo.byId(MASK_ID).style.display="";
        var model = {
  //          root:rootValue,
    		attrMode:dojo.byId(containMode_ID).value,
            container:containerValue
        };
       xmlManager.getXMLContent(model,connectionKey,path,targetNode,loadXMLDataCallback);
    }

    function loadXMLDataCallback(dataObj){
        dojo.byId(MASK_ID).style.display="none";
        if(null==dataObj
            || ""==dataObj
            || dataObj.childNodes[0].tagName=="error"){
            dojo.byId(TreeContainer).innerHTML=alpine.nls.hadoop_prop_file_structure_xml_load_structure_error_tip;
            return false;
        }
        eventsHandler = [];
        if(dataObj.childNodes!=null && dataObj.nodeType==9){
            //var domStr = buildTreeNodes(dataObj.childNodes[0]);
            var jsonObj = null;
            nodeIDNum = 0; //init id
            nodeNameCounter = {};
            //init level counter
            nodeLevelInfo = {};

            //guess dom tip
            if(dataObj.childNodes[0].nodeName == "alpineguessed"){
                var typeEQ1DOMNode = _getChildNodeTypeEQ1(dataObj.childNodes[0].childNodes);
                jsonObj = _buildXMLDomNodeToJSON(typeEQ1DOMNode,null,0);
                dojo.byId("hadoop_prop_file_structure_xml_container_gess_tip").innerHTML = alpine.nls.hadoop_prop_file_xml_json_guess_node_tip;
            }else{
                jsonObj = _buildXMLDomNodeToJSON(dataObj.childNodes[0],null,0);
            }

            if(jsonObj==null){
                dojo.byId(TreeContainer).innerHTML=alpine.nls.hadoop_prop_file_structure_xml_load_structure_error_tip;
                return false;
            }
            dojo.empty(TreeContainer);
            //validate input value is a tree node
            if(inputStatus==true){
                inputStatus = false;
                var containNodes = [];
                getContainTheParentNode(jsonObj,containNodes);
                var containerNames = [];
                for(var i=0;i<containNodes.length;i++){
                    containerNames.push(containNodes[i].name);
                }
                if(dojo.indexOf(containerNames,dijit.byId(CONTAINER).get("value"))==-1 && dijit.byId(CONTAINER).get("value")!=""){
                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_container_value_invalid_tip);
                    return false;
                }else{
                    for(i=0;i<containNodes.length;i++){
                       if(containNodes[i].name==dijit.byId(CONTAINER).get("value")){
                           dojo.byId(containMode_ID).value = containNodes[i].attrMode;
                           break;
                       }
                    }
                }
            }
            //var datastore =
            var treeStore = new dojo.data.ItemFileReadStore({
                data: {
                    identifier: "nodeID",// refer to ResourceItem
                    label: "name",
                    items: [jsonObj]
                }
            });

            var treeModel = new dijit.tree.TreeStoreModel({
                store: treeStore,
                childrenAttrs : ["childrenNode"],
                mayHaveChildren: function(treeItem){
                    return treeItem.isLeafNode[0]==false && treeItem.childrenNode.length>0;
                },
                query: {
                    "nodeID": "*"
                }
            });
            var tree = dijit.byId(XMLTree_ID);
            if(tree!=null){
                tree.destroyRecursive();
            }
            tree = new dijit.Tree({
                id: XMLTree_ID,
                showRoot: true,
                style : "height: 100%;",
                openOnClick: false,
                persist: false,
                model:treeModel,
                getIconClass: function(item, opened){
                    if(item.isLeafNode[0]==false){
                        return opened ? "dijitIconFolderOpen" : "dijitIconFolderClosed";
                    }else{
                        return "dijitIconFile";
                    }
                },
                onClick: function(treeItem){
                   dojo.byId(Hide_treeNode_select).value = "";
                   Hide_treeNode_selectModel = null;
                   if(treeItem.isLeafNode[0]==false && itemInDifferentLevel(treeItem.name[0])==false){
                       dojo.byId(Hide_treeNode_select).value = treeItem.name[0];
                       Hide_treeNode_selectModel = treeItem;
                       dijit.byId(BTN_select_level).set("disabled",false);
                   }else{
                       dijit.byId(BTN_select_level).set("disabled",true);
                   }
                },
                onDblClick:function(item, node, evt){
                    //console.log(item+"\t"+node+"");
                    var tree = dijit.byId(XMLTree_ID);
                    if(tree!=null && item.isLeafNode[0]==false){
                        if(itemInDifferentLevel(item.name[0])==true){
                            //buildDefineColumnGrid([]);
                            //destroyPreviewGrid();
                            var alertInfo = alpine.nls.hadoop_prop_file_structure_column_xpath_value_invalid_tip.replace("###",item.name[0]);
                            popupComponent.alert(alertInfo);
                            return false;
                        }
                        //validate Container Element
                        dijit.byId(CONTAINER).set("value",item.name[0]);
                        var containerValue = dijit.byId(CONTAINER).get("value");
                        //empty column define grid
                        buildDefineColumnGrid([]);
                        //emtpy preview grid
                        destroyPreviewGrid();

//                        if(""!=containerValue){
//                            if(containerValue != item.name[0]){
//                                popupComponent.alert(alpine.nls.hadoop_prop_file_structure_xml_container_tip);
//                                return false;
//                            }
//                        }

                        _addItemsToColumnGrid(item)
                    }
                }
            }, dojo.create("div",null,TreeContainer));
            tree.startup();
        }
    }

    function itemInDifferentLevel(name){
        if(nodeLevelInfo[name]!=null){
            if(nodeLevelInfo[name].length>1){
                return true;
            }
        }
        return false;
    }

    //get first node noteType==1
    function _getChildNodeTypeEQ1(nodes){
        if(null!=nodes && nodes.length>0){
            for(var i=0;i<nodes.length;i++){
               if(nodes[i].nodeType==1){
                   return nodes[i];
               }
            }
        }
        return null;
    }

    function _addItemsToColumnGrid(item){
        dojo.byId(MASK_ID).style.display="";
        var tree = dijit.byId(XMLTree_ID);
        var containerValue = dijit.byId(CONTAINER).get("value");
        var model = tree.model;
        model.fetchItemByIdentity({identity:item.nodeID[0],onItem:function(fetchItem){
            if(fetchItem!=null){
                //put container node mode
                dojo.byId(containMode_ID).value = item.attrMode[0];
                //get leaf nodes
                var leafNodes = [];
                _getLeafDataItems(fetchItem,leafNodes);
                var forDataGridItems = [];
                for(var i=0;i<leafNodes.length;i++){
                    var xpVal = leafNodes[i].xpathValue;
                    var evaluateValue = dataTypeUtils.getTypeByXPath(evaluateValueTypes,leafNodes[i].xpathValue);
                    if(leafNodes[i].fromNodeType=="node"){
                        leafNodes[i].xpathValue ="//"+xpVal.substring(xpVal.indexOf("/"+containerValue+"/")+containerValue.length+2)+"/text()";
                    }else{ //attribute
                        leafNodes[i].xpathValue ="//"+xpVal.substring(xpVal.indexOf("/"+containerValue+"/")+containerValue.length+2);

                    }

//                    var newName = dijit.byId(CONTAINER).get("value")+leafNodes[i].xpathValue;
                    var newName = leafNodes[i].xpathValue;
                    if(newName!=null){
                        newName = newName.replace(/\/+/ig,"_");
                        newName = newName.replace(/@/ig,"");
                        newName = newName.replace("_text()","");
                    }
                    var columnName = replaceAllInvalidStartChar(newName);
                    forDataGridItems.push(
                        {
                            id:"item_"+i,
                            columnName:columnName,
                            columnType:evaluateValue,
                            xpathValue:leafNodes[i].xpathValue,
                            fromNodeType:leafNodes[i].fromNodeType
                        }
                    );
                }
                buildDefineColumnGrid(forDataGridItems);
            }
        }});
    }

    function hasHadoopKeyWord(dataItems){
        if(dataItems!=null && dataItems.length>0){
            for(var i=0;i<dataItems.length;i++){
                if(hdDataTypeUitl.isHadoopKeyWord(dataItems[i].columnName)== true){
                    var tip = alpine.nls.hadoop_prop_file_structure_column_name_keyword_tip.replace("###","'"+dataItems[i].columnName+"'");
                    popupComponent.alert(tip);
                    return true;
                }
            }
        }
        return false;
    }

    /* moved to dataTypeUtils.getTypeByXPath */
    /*function _getEvaluateValueByXpath(evaluateValueTypes,xpath){
        console.log("getEvaluateValueBuXpath");
        if(evaluateValueTypes[xpath] ==null || evaluateValueTypes[xpath].length == 0){
            return "chararray";
        }
        if(evaluateValueTypes[xpath].length>0){
            if(dojo.indexOf(evaluateValueTypes[xpath],"chararray")!=-1){
                return "chararray";
            }
            if(dojo.indexOf(evaluateValueTypes[xpath],"double")!=-1){
                return "double";
            }
            return "long";
        }
    }*/

    function selectLevelBtnClick(){
       var selectLabel = dojo.byId(Hide_treeNode_select).value;
       //Hide_treeNode_selectModel; //model
        dijit.byId(CONTAINER).set("value",selectLabel);
       if(null!=selectLabel && ""!=selectLabel && null!=Hide_treeNode_selectModel){
           _addItemsToColumnGrid(Hide_treeNode_selectModel);
       }
    }

    function getContainTheParentNode(jsonObj,containerNodes){
        if(jsonObj.isLeafNode==false){
            if(dojo.indexOf(containerNodes,jsonObj.name)==-1){
                containerNodes.push({name:jsonObj.name,mode:jsonObj.attrMode});
            }
            for(var i=0;i<jsonObj.childrenNode.length;i++){
                getContainTheParentNode(jsonObj.childrenNode[i],containerNodes);
            }
        }
    }

    function  _getLeafDataItems(items,leafDatas){
       if(items.isLeafNode[0]==false){
         for(var i=0;i<items.childrenNode.length;i++){
             if(_haveSameXpathValue(leafDatas,items.childrenNode[i])==true){
                 continue;
             }
             _getLeafDataItems(items.childrenNode[i],leafDatas);
         }
       }else{
           leafDatas.push({
              columnName:items.name[0],
              columnType:dataTypeUtils.getDataTypeByValue(items.nodeValue[0],"hd"),
              xpathValue:"//"+items.nodeFullPath[0],
              fromNodeType:items.fromType[0]
          });
       }
    }

    function _haveSameXpathValue(leafDatas,item){
       for(var i=0;i<leafDatas.length;i++){
           if(leafDatas[i].xpathValue == "//"+item.nodeFullPath[0]){
               //update dataType
                return true;
           }
       }
      return false;
    }

    /* Refactored to DataTypeUtils */
    /*function _getDataTypeByValue(value){
        var isNumber = false;
        try {
            if (isNaN(value) == false) {
                isNumber = true;
            }
        } catch (e) {
            isNumber = false;
        }
        var dataType = "chararray";
        if (isNumber == true && value.indexOf(".") == -1) {
            dataType = "long";
        }
        if (isNumber == true && value.indexOf(".") != -1) {
            dataType = "double";
        }
        var reg = /^((\d+.?\d+)[Ee]{1}(\d+))$/ig;
        if(reg.test(value)==true){
            dataType = "double";
        }
        return dataType;
    }*/



    function getParentsNodeName(treeNode){
       var treeNodePath=(treeNode.nodeType!=9)?treeNode.nodeName:"";
        if(treeNode.nodeType!=9){
            treeNodePath = getParentsNodeName(treeNode.parentNode) +"/"+treeNodePath
        }else{
            treeNodePath = treeNodePath+"/";
        }
       return treeNodePath;
    }

    function _buildXMLDomNodeToJSON(domNode,parentPath,levelConter){

        if(domNode.childNodes!=null && domNode.nodeType==1){
            //print
            //console.log(domNode.nodeName);
            var nodeObj ={};
            nodeObj.name = domNode.nodeName;
            //
            addNodeLevelInfo(nodeObj.name,levelConter);

            if(domNode.childNodes.length==1){
                nodeObj.nodeValue = domNode.textContent;
                treeNodeTypes(parentPath,nodeObj.nodeValue);
            }else{
                nodeObj.nodeValue = "";
            }
            if(parentPath==null || ""==parentPath){
                parentPath = domNode.nodeName;
            }

            nodeObj.nodeFullPath=parentPath;

            nodeObj.fromType="node";
            nodeObj.childrenNode = [];
            nodeObj.nodeID=++nodeIDNum;
            nodeObj.isLeafNode = true;
            nodeObj.attrMode = "";
            if(domNode.attributes!=null && domNode.attributes.length>0){
                nodeObj.attrMode = "pure";
                nodeObj.isLeafNode = false;
               // lavel++;
                for(var i=0;i<domNode.attributes.length;i++){
                    treeNodeTypes(parentPath+"/@"+domNode.attributes[i].nodeName,domNode.attributes[i].nodeValue);
                    //console.log(domNode.attributes[i].nodeValue);
                    addNodeLevelInfo(domNode.attributes[i].nodeName,levelConter+1);
                    //
                    nodeObj.childrenNode.push({
                        name : domNode.attributes[i].nodeName,
                        nodeValue : domNode.attributes[i].nodeValue,
                        fromType:"attr",
                        nodeID:++nodeIDNum,
                        isLeafNode:true,
                        nodeFullPath : parentPath+"/@"+domNode.attributes[i].nodeName,
                        childrenNode :[]
                    });
                }
            }
            if(domNode.childNodes!=null && domNode.childNodes.length>0){
                for(var j=0;j<domNode.childNodes.length;j++){
                    if(domNode.childNodes[j].nodeType!=1){
                       continue;
                    }
                    if(nodeNameCounter[parentPath+"/"+domNode.childNodes[j].nodeName]==null){
                        nodeNameCounter[parentPath+"/"+domNode.childNodes[j].nodeName] = 1;
                    }else{
                        nodeNameCounter[parentPath+"/"+domNode.childNodes[j].nodeName]++;
                    }
                    if(nodeNameCounter[parentPath+"/"+domNode.childNodes[j].nodeName]>MAX_REPEAT_ROW){
                        //for data type
                        _buildXMLDomNodeToJSON(domNode.childNodes[j],parentPath+"/"+domNode.childNodes[j].nodeName,levelConter+1);
                      continue;
                    }
                    if(nodeObj.attrMode=="pure"){
                        nodeObj.attrMode = "half";
                    }
                    if(nodeObj.attrMode==""){
                        nodeObj.attrMode = "no";
                    }
                    nodeObj.isLeafNode = false;
                    nodeObj.childrenNode.push(_buildXMLDomNodeToJSON(domNode.childNodes[j],parentPath+"/"+domNode.childNodes[j].nodeName,levelConter+1));
                }
            }
            return nodeObj;
        }else{
            //print when domNode have no child
            if (domNode.nodeType == 1) {
                var nodeObj ={};
                //console.log(domNode.nodeName);
                nodeObj.name = domNode.nodeName;
                nodeObj.fromType="node";
                nodeObj.nodeValue = domNode.textContent;
                if(parentPath==null || ""==parentPath){
                    parentPath = domNode.nodeName;
                }
                nodeObj.nodeID=++nodeIDNum;
                nodeObj.nodeFullPath=parentPath;
                nodeObj.childrenNode = [];
                nodeObj.isLeafNode = true;

                addNodeLevelInfo(nodeObj.name,levelConter);

                treeNodeTypes(parentPath,domNode.textContent);

                if (domNode.attributes != null && domNode.attributes.length > 0) {
                    nodeObj.isLeafNode = false;
                    for (var i = 0; i < domNode.attributes.length; i++) {
                        treeNodeTypes(parentPath+"/"+domNode.nodeName+"/@"+domNode.attributes[i].nodeName,domNode.attributes[i].nodeValue);
                       // colose.log(domNode.attributes[i].nodeValue);
                        addNodeLevelInfo(domNode.attributes[i].nodeName,levelConter+1);

                        nodeObj.childrenNode.push({
                            name : domNode.attributes[i].nodeName,
                            nodeValue : domNode.attributes[i].nodeValue,
                            fromType:"attr",
                            nodeID:++nodeIDNum,
                            isLeafNode:true,
                            nodeFullPath : parentPath+"/"+domNode.nodeName+"/@"+domNode.attributes[i].nodeName,
                            childrenNode :[]
                        });
                    }
                }
                return nodeObj;
            }
        }

    }

    function treeNodeTypes(key,value){
        if(evaluateValueTypes["//"+key]==null){
            evaluateValueTypes["//"+key] = [];
        }
        if(evaluateValueTypes["//"+key].length<GUESS_TYPE_MAX){
            evaluateValueTypes["//"+key].push(dataTypeUtils.getDataTypeByValue(value,"hd"));
        }
    }

    function addNodeLevelInfo(lavelName,level){
        if(nodeLevelInfo[lavelName]==null){
            nodeLevelInfo[lavelName]=[];
        }
        if(dojo.indexOf(nodeLevelInfo[lavelName],level)==-1){
            nodeLevelInfo[lavelName].push(level);
        }
    }

    return{
        showStructureCfgXMLDialog:showStructureCfgXMLDialog

    }

});