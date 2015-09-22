/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopFileStructure4JSONHelper
 * Author: Will
 * Date: 12-11-9
 */
define([
    "dojo/dom-attr",
    "dojo/dom-style",
    "dojo/on",
    "alpine/props/HadoopFileStructure4LogHelper",
    "alpine/props/HadoopFileStructure4JSONManager",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/util/DataTypeUtils"
],
function(domAttr,domStyle,on,logHelper,jsonManager,hdDataTypeUitl,dataTypeUtils){
    var DIALOG_ID = "hadoopFileStructureCfgDlg4json";
    var SUB_TITLE = "hadoopPropery_Config_Colum_file_name4json";
    var OK_BTN = "hadoopFileStructure_4json_Dlg_Btn_OK";
    var Cancel_BTN = "hadoopFileStructure_4json_Dlg_Btn_Cancel";
    var LoadTree_Btn = "hadoop_prop_file_structure_json_loadData";
    var Container_Input = "hadoop_prop_file_structure_json_input_container";
    var TreeContainer = "jsonDomTreeContainer";
    var TreeId = "jsonDomTreeID";
    var DEFINE_COLUMN_GRID_ID = "hadoop_prop_file_structure_JSON_column_define_column_grid";
    var MASK_ID = "mask_jsonfileStruct_column_define";
    var DEFINE_COLUMN_GRID_CONTAINER = "hadoop_prop_file_structure_json_column_define_container";
    var CONTAINER = "hadoop_prop_file_structure_json_input_container";
    var BTN_DELETE = "hadoop_prop_file_structure_json_delete";
    var BTN_MOVEUP = "hadoop_prop_file_structure_json_moveup";
    var BTN_MOVEDOWN = "hadoop_prop_file_structure_json_movedown";
    var BTN_PREV = "hadoop_prop_file_structure_json_preview";
    var PreviewGridContainer ="hadoop_prop_file_structure_json_preview_grid_container";
    var previewMask = "mask_JSONfileStruct_preview";
    var treeSelectLabel = "hadoop_file_struct_json_tree_node_select";
    var BTN_selectLevel = "hadoop_prop_file_structure_json_tree_select_level";
//    var SELECT_TYPE = "hadoop_prop_file_structure_json_container_type";
    var hidden_JSON_TYPE = "hadoop_prop_file_structure_json_type";
    var hidden_CONTAINER_PATH = "hadoop_prop_file_structure_json_containerpath"; //record container path
    var treeSelectItemModel = null;

    /*
     String root;
     String container;
     List<String> jsonPathList;
     List<String> columnNameList;
     List<String> columnTypeList;
    * */
    var jsonFileStructureModel = null;
    var currentProp = null;
    var nodeID = 0;
    var inputStatus = false;
    var contetnPains = [];
    var MAX_COUNT = 20;
    var Max_Array_Repeat = 40;
    var leaf_counter = {};
    var leaf_type_objects = {};
    var max_leaf_repeat = 40;
    var level_select4_stl = [];
    var node_level_info = {}; //for same name in different level

    dojo.ready(function(){
       dojo.connect(dijit.byId(OK_BTN),"onClick",btnOKClick);
       dojo.connect(dijit.byId(Cancel_BTN),"onClick",btnCancelClick);
       dojo.connect(dijit.byId(LoadTree_Btn),"onClick",loadJsonData);
        dojo.connect(dijit.byId(BTN_DELETE),"onClick",deleteItem);
        dojo.connect(dijit.byId(BTN_MOVEUP),"onClick",moveupItem);
        dojo.connect(dijit.byId(BTN_MOVEDOWN),"onClick",movedownItem);

        dojo.connect(dijit.byId(BTN_PREV),"onClick",previewData);

        dojo.connect(dijit.byId(CONTAINER),"onKeyUp",function(event){
            if(this.get("value")!=jsonFileStructureModel.container){
                buildDefineColumnGrid([]);
            }
            clearTreeDomNode();
            dojo.byId(hidden_JSON_TYPE).value = "sts";
            if(event.keyCode==dojo.keys.ENTER){
                inputStatus = true;
                loadJsonData();
               // destroyPreviewGrid();
            }
        });

        dojo.connect(dijit.byId(BTN_selectLevel),"onClick",selectLevelBtnClick);
    });

    function showFileStructure4JsonDialog(subtitle, prop){
        var dlg = dijit.byId(DIALOG_ID);
        if (null != dlg) {
            dlg.show();
            dojo.style(dlg.containerNode, {width:"850px", height:"650px", overflow:"hidden"});
            domStyle.set(dlg.titleBar, "display", "none");
            var tooltipValue = subtitle;
            if(subtitle!=null && subtitle.length>30){
                subtitle = subtitle.substring(0,27)+"...";
            }
            domAttr.set(dojo.byId(SUB_TITLE),"innerHTML",subtitle);
            domAttr.set(dojo.byId(SUB_TITLE),"title",tooltipValue);
            sourceButtonId = getSourceButtonId(prop);
            currentProp = prop;
            if(null==prop.jsonFileStructureModel){
                prop.jsonFileStructureModel = {};
            }
            jsonFileStructureModel = prop.jsonFileStructureModel;

            dojo.byId(treeSelectLabel).value = "";
            dijit.byId(BTN_selectLevel).set("disabled",true);

            var dataItems = [];
            dataItems = initJSONDlgStatus();
            buildDefineColumnGrid(dataItems);
            dojo.empty(TreeContainer);
            loadJsonData();
            destroyPreviewGrid();

        }
    }

    function initJSONDlgStatus(){
        dijit.byId(CONTAINER).set("value",jsonFileStructureModel.container==null?"":jsonFileStructureModel.container);
        dojo.byId(hidden_JSON_TYPE).value = jsonFileStructureModel.jsonDataStructureType==null?"sts":jsonFileStructureModel.jsonDataStructureType;
        dojo.byId(hidden_CONTAINER_PATH).value = jsonFileStructureModel.containerJsonPath==null?"":jsonFileStructureModel.containerJsonPath;
        var dataItems = [];
        var nameList = jsonFileStructureModel.columnNameList;
        var typeList = jsonFileStructureModel.columnTypeList;
        var jsonPathList  = jsonFileStructureModel.jsonPathList==null?[]:jsonFileStructureModel.jsonPathList;
        if(null!=nameList && nameList.length>0){
          for(var i=0;i<nameList.length;i++){
              var dataType  = typeList[i];
              if(dataType==null){
                  dataType  = "chararray";
              }
              var itemObj = {
                  id:"item_"+i,
                  columnName:nameList[i],
                  columnType:dataType,
                  xpathValue:jsonPathList[i]==null?"":jsonPathList[i],
                  fullXPathValue:jsonPathList[i]==null?"":jsonPathList[i]
              };
              dataItems.push(itemObj);
          }
        }
        //
        if(dojo.byId(hidden_JSON_TYPE).value == "stl" && dijit.byId(CONTAINER).get("value")==""){
            //level_select4_stl = [];
            if(dataItems!=null && dataItems.length>0){
                for(var i=0;i<dataItems.length;i++){
                    var firstLevel = dataItems[i].xpathValue.replace(/^\/\//,"").split("/")[0];
                    if(dojo.indexOf(level_select4_stl,firstLevel)==-1){
                        level_select4_stl.push(firstLevel);
                    }
                }
            }
        }else if(dojo.byId(hidden_JSON_TYPE).value == "stl" && dijit.byId(CONTAINER).get("value")!=""){
           level_select4_stl = [];
           level_select4_stl.push(dijit.byId(CONTAINER).get("value"));
       }
        return dataItems;
    }

    function hiedDialog(){
        var dlg = dijit.byId(DIALOG_ID);
        leaf_type_objects = {};
        leaf_counter = {};
        inputStatus = false;
        level_select4_stl = [];
        if(null != dlg){
            dlg.hide();
        }
    }

    function btnOKClick(){
        if(saveJSONFileStrutureModel()==false){
            return false;
        }

        hiedDialog();
    }

    function btnCancelClick(){
        hiedDialog();
    }

    function saveJSONFileStrutureModel(){
        // validate structure
        var columnGrid = getGridStoreItems();
        if(validateJSONModelConfig(columnGrid)==false){
            return false;
        }
        alpine.spinner.showSpinner("hadoopFileStructureCfgDlg4json");
        //
        //jsonFileStructureModel.root ="";
        jsonFileStructureModel.container = dijit.byId(Container_Input).get("value");
        jsonFileStructureModel.jsonDataStructureType = dojo.byId(hidden_JSON_TYPE).value;
        jsonFileStructureModel.containerJsonPath = dojo.byId(hidden_CONTAINER_PATH).value;

        //jsonFileStructureModel.jsonPathList=[];
        jsonFileStructureModel.columnNameList=[];
        jsonFileStructureModel.columnTypeList=[];
        jsonFileStructureModel.jsonPathList = [];
        for(var i=0;i<columnGrid.length;i++){
            jsonFileStructureModel.columnNameList.push(columnGrid[i].columnName);
            jsonFileStructureModel.columnTypeList.push(columnGrid[i].columnType);
            jsonFileStructureModel.jsonPathList.push(columnGrid[i].xpathValue);

        }
        logHelper.clearStructueModel(currentProp,"json");
        setButtonBaseClassValid(sourceButtonId);
        alpine.spinner.hideSpinner("hadoopFileStructureCfgDlg4json");
        return true;
    }

    function validateJSONModelConfig(columnGrid){
        if(columnGrid.length==0){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_xml_grid_column_tip);
            return false;
        }

        if(columnGrid.length!=0 && isColumnValid(columnGrid)==false){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_name_invalid_tip);
            return false;
        }
        if(hasEmptyColumnName()==true || hasSameColumnName()==true){
            //popupComponent.alert("Column define invalid!Please check.");
            return false;
        }
        if(hasHadoopKeyWord(columnGrid)==true){
            return false;
        }
        //path have same node name
        return true;

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

    function hasEmptyColumnName(){
        var columnGrid = getGridStoreItems();
        if(columnGrid.length==0){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_4json_define_tip);
            return true;
        }
        for(var i=0;i<columnGrid.length;i++){
            if(columnGrid[i].columnName==""){
                popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_4json_empty_tip);
                return true;
            }
        }
        return false;
    }

    function hasSameColumnName(){
        var columnGrid = getGridStoreItems();
        if(columnGrid.length==0){
            //popupComponent.alert("Please define columns");
            return true;
        }
        var columnNames = [];
        for(var i=0;i<columnGrid.length;i++){
            columnNames.push(columnGrid[i].columnName);
        }
        columnNames.sort();
        for(i=0;i<columnNames.length-1;i++){
            if(columnNames[i]==columnNames[i+1]){
                var msg = alpine.nls.hadoop_prop_file_structure_xml_column_repeat_tip.replace("###","'"+columnNames[i]+"'");
                popupComponent.alert(msg);
                return true;
            }
        }
        return false;
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

    function getEmptyModel(){
        jsonFileStructureModel.root ="";
        jsonFileStructureModel.container = "";
        jsonFileStructureModel.jsonPathList=[];
        jsonFileStructureModel.columnNameList=[];
        jsonFileStructureModel.columnTypeList=[];
    }

    function loadJsonData(){
        var containerValue = dijit.byId(Container_Input).get("value");
        var hadoopConName = dijit.byId("connName"+ID_TAG).get("value");
        var connectionKey=alpine.props.HadoopFileOperatorPropertyHelper.getConnectionKey(hadoopConName);
        var path=dijit.byId("hadoopFileName"+ID_TAG).get("value");
        dojo.byId(MASK_ID).style.display="";
        var dataExplorerModel = {};
        dataExplorerModel.connectKey =  connectionKey;
        dataExplorerModel.path = path;
        var jsonModel = {
            //          root:rootValue,
             //= jsonFileStructureModel.jsonDataStructureType
            jsonDataStructureType:dojo.byId(hidden_JSON_TYPE).value,
            container:containerValue,
            containerJsonPath:dojo.byId(hidden_CONTAINER_PATH).value==null?"":dojo.byId(hidden_CONTAINER_PATH).value
        };

        jsonManager.getJSONFileContent(jsonModel,connectionKey,path,loadJsonDataCallback);
    }

    function selectLevelBtnClick(){
        var selectLabel = dojo.byId(treeSelectLabel).value;
        dijit.byId(CONTAINER).set("value",selectLabel);
        if(null!=selectLabel && ""!=selectLabel && null!=treeSelectItemModel){
            _addItemsToColumnGrid(treeSelectItemModel);
        }
    }

    function loadJsonDataCallback(data){
      //console.log(data);
      //clear level select status
      dojo.byId(treeSelectLabel).value = "";
      dijit.byId(BTN_selectLevel).set("disabled",true);

      dojo.empty("hadoop_prop_file_structure_json_container_tip");

       if("stl"==data.sturctureType){

       }else{
          dijit.byId(Container_Input).set("disabled",false);
       }
      dojo.byId(hidden_JSON_TYPE).value = data.sturctureType;

      dojo.byId(MASK_ID).style.display="none";
      if(data==null || data.error!=null){
          dojo.byId(TreeContainer).innerHTML=alpine.nls.hadoop_prop_file_structure_xml_load_structure_error_tip;
          //dojo.byId(hidden_JSON_TYPE).value = data.
          inputStatus = false;
          return false;
      }
        nodeID = 1
        leaf_counter = {}; //clean leaf counter obj
        leaf_type_objects = {}; //add same path types. types.length<=20;
        var jsonData = {};
        if(dijit.byId(CONTAINER).get("value")!="" && data["AlpineLineJSONVirtualRoot"]==null){
            jsonData[dijit.byId(CONTAINER).get("value")] = data[dijit.byId(CONTAINER).get("value")];
            if(jsonData[dijit.byId(CONTAINER).get("value")]==null){
                dojo.byId(TreeContainer).innerHTML=alpine.nls.hadoop_prop_file_structure_xml_load_structure_error_tip;
                return false;
            }
        }else{
            if(data.guessedContainer!=null && data.guessedContainer!=""){
                jsonData[data.guessedContainer] = data[data.guessedContainer];
                dijit.byId(CONTAINER).set("value",data.guessedContainer);
                dojo.byId("hadoop_prop_file_structure_json_container_tip").innerHTML = alpine.nls.hadoop_prop_file_xml_json_guess_node_tip;
            }else if(data.AlpineLineJSONVirtualRoot !=null && data.AlpineLineJSONVirtualRoot!=""){
                jsonData = data.AlpineLineJSONVirtualRoot;
            }else{
                jsonData = data.AlpineJSONVirtualRoot;
            }
        }

        node_level_info = {};

        var objToTree =  _buildObj4Tree(jsonData,null,0);
        leaf_type_objects = _groupBy_leaf_type_objects();
        if(objToTree.length>0){
            if(inputStatus==true){
                inputStatus = false;
                var containNodes = []
                getContainTheParentNode(objToTree[0],containNodes);
                if(dojo.indexOf(containNodes,dijit.byId(CONTAINER).get("value"))==-1 && dijit.byId(CONTAINER).get("value")!=""){
                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_json_select_level_tip);
                    return false;
                }
            }
            buildTree(objToTree)
        }
    }

    function _groupBy_leaf_type_objects(){
        var prepareObj = {};
        for(var key in leaf_type_objects){
            var newKey = key.replace(/\/[\d]+\//gm,"/*/");
            if(prepareObj[newKey] == null){
                prepareObj[newKey] = [];
            }
                prepareObj[newKey].push(leaf_type_objects[key][0]);
        }
        return prepareObj
    }

    function getContainTheParentNode(jsonObj,containerNodes){
        if(jsonObj.isLeafNode==false){
            if(dojo.indexOf(containerNodes,jsonObj.name)==-1){
                containerNodes.push(jsonObj.name);
            }
            for(var i=0;i<jsonObj.childrenNode.length;i++){
                getContainTheParentNode(jsonObj.childrenNode[i],containerNodes);
            }
        }
    }

    function buildTree(jsonObj){
        var treeStore = new dojo.data.ItemFileReadStore({
            data: {
                identifier: "nodeID",// refer to ResourceItem
                label: "name",
                items: jsonObj
            }
        });

        //var treeModel = new dijit.tree.TreeStoreModel({
        var treeModel = new dijit.tree.ForestStoreModel({
            store: treeStore,
            childrenAttrs : ["childrenNode"],
            mayHaveChildren: function(treeItem){
                return treeItem.isLeafNode[0]==false && treeItem.childrenNode.length>0;
            },
            query: {
                "nodeID": "*"
            }
        });
        var tree = dijit.byId(TreeId);
        if(tree!=null){
            tree.destroyRecursive();
        }
        tree = new dijit.Tree({
            id: TreeId,
            showRoot: false,
            style : "height: 100%;",
            openOnClick: false,
            persist: false,
            model:treeModel,
            getIconClass: function(item, opened){
                if(item == this.model.root){
                    return null;
                }
                if(item.isLeafNode[0]==false){
                    return opened ? "dijitIconFolderOpen" : "dijitIconFolderClosed";
                }else{
                    return "dijitIconFile";
                }
            },
            onClick:function(treeItem){
                dojo.byId(treeSelectLabel).value = "";
                treeSelectItemModel = null;
                if(treeItem.isLeafNode[0]==false && treeItem.parentNodeType[0]!="array"){
                    dojo.byId(treeSelectLabel).value = treeItem.name[0];
                    treeSelectItemModel = treeItem;
                    dijit.byId(BTN_selectLevel).set("disabled",false);
                }else{
                    dijit.byId(BTN_selectLevel).set("disabled",true);
                }
            },
            onDblClick:function(item, node, evt){
                //console.log(item+"\t"+node+"");
                if(item.parentNodeType[0]=="array"){
                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tree_click_4json_tip);
                    return  false;
                }
                //var repeatLevelName = haveSameNameInDifLevel()
                if("stl"!=dojo.byId(hidden_JSON_TYPE).value && haveSameNameInParent(item.name[0],item.nodeLevel[0])==true){
                    var alertInfo = alpine.nls.hadoop_prop_file_structure_column_xpath_value_invalid_tip.replace("###",item.name[0]);
                    popupComponent.alert(alertInfo);
                    return false;
                }
                var tree = dijit.byId(TreeId);
                if(tree!=null && item.isLeafNode[0]==false){
                    dijit.byId(CONTAINER).set("value",item.name[0]);
                    var containerValue = dijit.byId(CONTAINER).get("value");
                    _addContainerNameInCounter(item.name[0]);
                    _addItemsToColumnGrid(item)
                }
            }
        }, dojo.create("div",null,TreeContainer));
        tree.startup();
    }

    function _addContainerNameInCounter(nodeName){
        if(dojo.indexOf(level_select4_stl,nodeName)==-1){
            level_select4_stl.push(nodeName);
        }
    }

    function _addItemsToColumnGrid(item){
        dojo.byId(MASK_ID).style.display="";
        //var containerValue = dijit.byId(CONTAINER).get("value");
        var tree = dijit.byId(TreeId);
        var model = tree.model;
        model.fetchItemByIdentity({identity:item.nodeID[0],onItem:function(fetchItem){
            if(fetchItem!=null){
                var leafNodes = [];
                var containerPath = "";
                if(fetchItem.xpathValue[0]!=null && fetchItem.xpathValue[0]!=""){
                    containerPath = fetchItem.xpathValue[0].replace(/^\//,"");
                }
                dojo.byId(hidden_CONTAINER_PATH).value =containerPath; //add container path
                _getAllLeafItems(fetchItem,leafNodes);
                var forDataGridItems = [];
                var jsonType = dojo.byId(hidden_JSON_TYPE).value;
                var gridItems = getGridStoreItems();
                for(var i=0;i<leafNodes.length;i++){
                    //var xpVal = leafNodes[i].xpathValue
                    //leafNodes[i].xpathValue ="//"+xpVal.substring(xpVal.indexOf("/"+containerValue+"/")+containerValue.length+2)+"/text()";

                    var newName = dijit.byId(CONTAINER).get("value")+leafNodes[i].xpathValue;

                    //var newName = leafNodes[i].xpathValue;
                    if(newName!=null){
                        newName = newName.replace(/\/+/ig,"_");
                        newName = newName.replace(/_\*/ig,"");
                        newName = newName.replace("_text()","");
                        //newName = newName.replace("_text()","");
                    }
                    var columnName = replaceAllInvalidStartChar(newName);

                    if(/^[a-zA-Z].*/.test(columnName.replace(dijit.byId(CONTAINER).get("value")+"_",""))==true){
                        columnName = columnName.replace(dijit.byId(CONTAINER).get("value")+"_","");
                    }

                    if(gridItems.length==0 && "stl"==jsonType){
                        forDataGridItems.push(
                            {
                                id:"item_"+i,
                                columnName:columnName,
                                columnType:leafNodes[i].columnType,
                                xpathValue:leafNodes[i].xpathValue,
                                fullXPathValue:leafNodes[i].fullXPath
                            }
                        );
                    }else if(gridItems.length>0 && "stl"==jsonType && !(level_select4_stl.length==1 && level_select4_stl[0]==dijit.byId(Container_Input).get("value"))){
                        forDataGridItems.push(
                            {
                                id:"item_"+i,
                                columnName:columnName,
                                columnType:leafNodes[i].columnType,
                                xpathValue:leafNodes[i].fullXPath,
                                fullXPathValue:leafNodes[i].fullXPath
                            });
                    }else{
                        forDataGridItems.push(
                            {
                                id:"item_"+i,
                                columnName:columnName,
                                columnType:leafNodes[i].columnType,
                                xpathValue:leafNodes[i].xpathValue,
                                fullXPathValue:leafNodes[i].fullXPath
                            }
                        );
                    }

                }

                if(null!=jsonType && ""!=jsonType
                    && "stl"==jsonType && !(level_select4_stl.length==1 && level_select4_stl[0]==dijit.byId(Container_Input).get("value"))){
                    if(forDataGridItems==null){
                        forDataGridItems = [];
                    }
                    //
                    if(gridItems.length>0 && (level_select4_stl.length>1 ||
                        (dijit.byId(Container_Input).get("value")!=""
                            && dijit.byId(Container_Input).get("value")!=dojo.byId(treeSelectLabel).value))){
                            dijit.byId(Container_Input).set("value","");
                            dojo.byId(hidden_CONTAINER_PATH).value = "";
                            for (i = 0; i < gridItems.length; i++) {
                                gridItems[i].xpathValue = gridItems[i].fullXPathValue;
                            }
                    }
                    var fullXPathIndex = [];
                    for (i = 0; i < gridItems.length; i++) {
                        fullXPathIndex.push(gridItems[i].fullXPathValue);
                    }
                    for(i=0;i<forDataGridItems.length;i++){
                        if(dojo.indexOf(fullXPathIndex,forDataGridItems[i].fullXPathValue)==-1){
                            forDataGridItems[i].id = "item_"+ gridItems.length;
                            gridItems.push(forDataGridItems[i]);
                        }
                    }
                    forDataGridItems = gridItems;

                }
                buildDefineColumnGrid(forDataGridItems);
            }
        }});
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

    function _getAllLeafItems(items,leafDatas){
        if(items.isLeafNode[0]==false){
            for(var i=0;i<items.childrenNode.length;i++){
                _getAllLeafItems(items.childrenNode[i],leafDatas);
            }
        }else{
            var data = {};
            data.columnName = items.name[0];
            items.xpathValue[0] = items.xpathValue[0].replace(/\/[\d]+\//gm,"/*/");
            data.columnType = dataTypeUtils.getTypeByXPath(leaf_type_objects,items.xpathValue[0]);
            var containerValue = dijit.byId(Container_Input).get("value");
            data.fullXPath  = "/"+items.xpathValue[0]; //for stl
            if(items.xpathValue[0].indexOf("/"+containerValue+"/")!=-1){
                data.xpathValue = "//"+items.xpathValue[0].substring(items.xpathValue[0].indexOf("/"+containerValue+"/")+containerValue.length+2);
            }else{
                data.xpathValue = "/"+items.xpathValue[0];
            }
            if(_haveSameNameAndXpath(leafDatas,data)==false){
                leafDatas.push(data);
            }

        }
    }

    /* Refactored to dataTypeUtils.getTypeByXPath */
    /*function _getEvaluateValueByXpath(evaluateValueTypes,xpath){
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

    function _haveSameNameAndXpath(items,data){
       if(null!=items && null!=data && items.length>0){
           for(var i=0;i<items.length;i++){
               if(items[i].xpathValue==data.xpathValue){
                   return true;
               }
           }
       }
       return false;
    }

    function hasDuplicationData(leafDatas,item){
       if(item!=null && leafDatas!=null && leafDatas.length>0){
           for(var i=0;i<leafDatas.length;i++){
               if(leafDatas[i].columnName==item.columnName && leafDatas[i].columnType==item.columnType){
                   return true;
               }
           }
       }
        return false
    }

    function _buildObj4Tree(data,path,nodeLevel) {
        var fields = [];
        if(path==null){
            path = "";
        }
        var parentNodeType = "object";
        if(data instanceof Array){
            parentNodeType = "array";
        }
        for(var key in data){
            if(key!=null){
                if(parentNodeType == "array" && parentNodeType!="array" && key>Max_Array_Repeat ){
                    continue;
                }
                addNodeLevelInfo(key,nodeLevel);

                if(data[key] instanceof Array || data[key] instanceof Object){
                    //console.log("node:"+key);
                    var subFields = _buildObj4Tree(data[key],path+"/"+key,(nodeLevel+1));
                    var nodeType = "object";
                    if(data[key] instanceof Array){
                        nodeType = "array";
                        //array have all simple type
                      if(isAllSimpleTypesInArray(data[key])==true && parentNodeType!="array"){

                          if(leaf_type_objects[path+"/"+key]==null){
                              leaf_type_objects[path+"/"+key] = [];
                          }

                          leaf_type_objects[path+"/"+key].push(dataTypeUtils.getDataTypeByValue(data[key],"hd"));

                          var name = key;
                          name = (path + "/" + key).replace(/\//g, "_");
                          if (name.indexOf("_") == 0) {
                              name = name.substring(1);
                          }
                          fields.push({
                              nodeID:++nodeID,
                              name:name,
                              nodeValue:data[key].join(","),
                              // columnType:columnType,
                              isLeafNode:true,
                              xpathValue:path + "/" + key,
                              nodeType:"leaf",
                              parentNodeType:parentNodeType,
                              nodeLevel:nodeLevel,
                              childrenNode:[]
                          });


                          continue;
                      }
                    }
                    // not simple
                    fields.push({
                        nodeID:++nodeID,
                        name: key,
                        nodeValue: "",
                        //columnType:"",
                        xpathValue: path+"/"+key,
                        isLeafNode:false,
                        nodeType:nodeType,
                        parentNodeType:parentNodeType,
                        nodeLevel:nodeLevel,
                        childrenNode: subFields
                    });
                }else{
                    //console.log("leaf:"+key);
                    if (path == null) {
                        path = "";
                    }
                    //var columnType = ####
                    var name = key;
                    if (parentNodeType == "array") {
                        name = (path + "/" + key).replace(/\//g, "_");
                        if (name.indexOf("_") == 0) {
                            name = name.substring(1);
                        }
                    }

                    if (leaf_type_objects[path + "/"+key] == null) {
                        leaf_type_objects[path + "/"+key] = [];
                    }

                    leaf_type_objects[path + "/"+key].push(dataTypeUtils.getDataTypeByValue(data[key],"hd"));

                    fields.push({
                        nodeID:++nodeID,
                        name:name,
                        nodeValue:data[key],
                        // columnType:columnType,
                        isLeafNode:true,
                        xpathValue:path + "/" + key,
                        nodeType:"leaf",
                        parentNodeType:parentNodeType,
                        nodeLevel:nodeLevel,
                        childrenNode:[]
                    });
                }
            }
        }
        return fields;
    }

    function addNodeLevelInfo(name, level) {
        if (/^\d+$/.test(name) == false) {
            if (node_level_info[name] == null) {
                node_level_info[name] = [];
            }
            if (dojo.indexOf(node_level_info[name], level) == -1) {
                node_level_info[name].push(level);
            }
        }
    }

    function haveSameNameInDifLevel(){
        var name = "";
        if(node_level_info!=null){
           for(var key in node_level_info){
               if(key!=null){
                   if(node_level_info[key].length>1){
                       name = key;
                       break;
                   }
               }
           }
        }
        return name;
    }

    function haveSameNameInParent(nodeName,currentLevel){
        if(node_level_info[nodeName]!=null){
           for(var i=0;i<node_level_info[nodeName].length;i++){
               if(node_level_info[nodeName][i]==currentLevel && i>0){
                   return true;
               }
           }
        }
        return false
    }

    function isAllSimpleTypesInArray(array){
        var allSimple = true;
        for(var i=0;i<array.length;i++){
            if(array[i] instanceof Array || array[i] instanceof Object){
                allSimple = false;
                break;
            }
        }
        return allSimple;
    }

    //--------------- define column grid
    function buildDefineColumnGrid(dataItems){
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
        if(dojo.byId(MASK_ID).style.display==""){
            dojo.byId(MASK_ID).style.display="none";
        }
        var gridStore = getJSONDefineColumnGridStore(dataItems);
        var gridStructure = getJSONDefineColumnGridStructure();
        if(grid==null){
            grid = new dojox.grid.DataGrid({
                id:DEFINE_COLUMN_GRID_ID,
                store: gridStore,
                structure: gridStructure,
                //selectionMode:"single",
                style:"height:186px;with:100%",
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

            },dojo.create('div',{style:"width:390px;height:188px"},DEFINE_COLUMN_GRID_CONTAINER));
            grid.startup();
        }else{
            grid.selection.deselectAll();
            grid.setStore(gridStore);
            grid.render();
        }

    };
    /*
     * dataItems = {
     *  id:number,
     *  columnName:string,
     *  columnType:string,
     *  xpathValue:string
     * }
     * */
    function getJSONDefineColumnGridStore(dataItems){
        if(null==dataItems){dataItems=[];}
        return new dojo.data.ItemFileWriteStore({data: {
            identifier: 'id',
            items: dataItems
        }});
    }

    function getJSONDefineColumnGridStructure(){
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
                    width: '100%',height:'20px'},
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
                    'width': '100%'},
                {'name': alpine.nls.hadoop_prop_file_structure_json_grid_column_xpath, 'field': 'xpathValue',width:"100%",height:'20px'}
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

    function deleteItem(){
        var grid = dijit.byId(DEFINE_COLUMN_GRID_ID);
        if(grid!=null){
            grid.removeSelectedRows();
            grid.render();
            var storeItems = getGridStoreItems();
            if(storeItems!=null && storeItems.length==0){
                level_select4_stl = [];

            }
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
                        var newDataStore = getJSONDefineColumnGridStore(storeItems);
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
                        var newDataStore = getJSONDefineColumnGridStore(storeItems);
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
                            fullXPathValue:realStroeItems[i].fullXPathValue[0]
                        }) ;
                    }
                }
            }
        }
        return storeItems;
    }


    function clearTreeDomNode(){
        if(dojo.byId(TreeContainer).innerHTML!=""){
            dojo.empty(TreeContainer);
        }
    }

    function previewData(){
//        if(valideateColumnGrid()==false){
//            return false;
//        }
        var gridItems = getGridStoreItems();
        if(validateJSONModelConfig(gridItems)==false){
            return false;
        }
        //save model
        var previewModel = {};
        //previewModel.root = dijit.byId(ROOT).get("value");
        previewModel.container =dijit.byId(CONTAINER).get("value");
        previewModel.containerJsonPath = dojo.byId(hidden_CONTAINER_PATH).value;
        previewModel.jsonDataStructureType = dojo.byId(hidden_JSON_TYPE).value;

        //previewModel.xPathList =[];
        previewModel.columnNameList =[];
        previewModel.columnTypeList =[];
        previewModel.jsonPathList = [];


        if(null!=gridItems && gridItems.length>0){
            for(var i=0;i<gridItems.length;i++){
                if(gridItems[i]!=null){
                    //previewModel.xPathList.push(gridItems[i].xpathValue);
                    previewModel.columnNameList.push(gridItems[i].columnName);
                    previewModel.columnTypeList.push(gridItems[i].columnType);
                    previewModel.jsonPathList.push(gridItems[i].xpathValue);
                }
            }
        }
        //var connectionKey=current_op.connectionName;
        var hadoopConName = dijit.byId("connName"+ID_TAG).get("value");
        var connectionKey=alpine.props.HadoopFileOperatorPropertyHelper.getConnectionKey(hadoopConName);
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
        dojo.byId(previewMask).style.display = "";
        var dataExplorerModel = {};
        dataExplorerModel.connectKey = connectionKey;
        dataExplorerModel.path = path;
        jsonManager.getPreviewData4JSON(previewModel,connectionKey,path,contentPine,fillOutPaneDataTable);

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

    return {
        showFileStructure4JsonDialog:showFileStructure4JsonDialog
    }

});