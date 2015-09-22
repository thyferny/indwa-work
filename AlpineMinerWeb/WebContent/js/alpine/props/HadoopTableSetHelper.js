/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopTableSetHelper
 * Author: Will
 * Date: 12-8-30
 */
/*
define(["alpine/props/HadoopDataTypeUtil","alpine/flow/HadoopOperatorsDataExplorerManager"],function(hdDataTypeUtil,dataExplorerMgmt){
    var tableSetConfigDlg = "hadoopTableSetConfigDlg";
    var tableSetConfigDlgBtn_OK = "hadoopTableSet_dlg_btn_OK";
    var tableSetConfigDlgBtn_Cancel="hadoopTableSet_dlg_btn_Cancel";
    var tableSetSelectGridContainer = "hadoop_select_tableset_column_grids_container";
    var tableSetConditionGridContainer = "hadoop_tableset_condition_container";
    var tableSetConditionGrid = "hadoop_tableset_condition_grid";
    var tableSetConditionBtn_Delete = "hadoop_tableset_condition_delete_btn";

    var tableSetModel = null;
    var inputFileInfos = [];
    var selectGridWidgets=[];
    var mappingGridWidets = [];
    var columnOrder = {};
    var currentMappingRow = -1;
    var currentMappingCell = -1;
    var currentCellValue = "";
    var onChangeStatusChecked = false;
    var onChangeStatusUnchecked = false;
    var sourceButtonId = null;





    dojo.ready(function(){
        dojo.connect(dijit.byId(tableSetConfigDlgBtn_OK),"onClick",saveHadoopTableSetModel);
        dojo.connect(dijit.byId(tableSetConfigDlgBtn_Cancel),"onClick",cancelHadoopTableSetModel);
        dojo.connect(dijit.byId(tableSetConfigDlg),"onHide",_onHideDestroySomething);
        //dojo.connect(dijit.byId(tableSetConditionBtn_Delete),"onclick",deleteMappingSelect);
    });

    function initHadoopTableSetVariable(propValue){
        sourceButtonId =getSourceButtonId(propValue);
        tableSetModel = dojo.clone(propValue.hadoopUnionModel);
        inputFileInfos = CurrentOperatorDTO.inputFileInfos;
    }

    function showHadoopTableSetCfgDlg(){
        if(inputFileInfos!=null && inputFileInfos.length==1){
            popupComponent.alert(alpine.nls.hadoop_join_table_num_error);
            //hideJoinConfigDlg();
            return false;
        }
        var dlg = dijit.byId(tableSetConfigDlg);
        if(dlg!=null){
        	dlg.titleBar.style.display = "none";
            dlg.show();
        }
        _buildSelectColumnGrids();
        _buildColumnMappingGrid();
    };

    function _buildSelectColumnGrids(){
        if(null!=inputFileInfos && inputFileInfos.length>0){
            dojo.empty(tableSetSelectGridContainer);
            for(var i=0;i<inputFileInfos.length;i++){
                var _legend = null;
                var _fieldset = null;
                var operatorInfo = alpine.flow.OperatorManagementManager.getOperatorPrimaryInfo(inputFileInfos[i].operatorUUID);
                dataExplorerMgmt.getCurrentOperatorPropertys4hdJoin_Set(operatorInfo,function(data){
                       var status =  _isStoreResults(data);
                       if(status==false && data.classname!="HadoopFileOperator"){
                           inputFileInfos[i].hadoopFileName = operatorInfo.name;
                       }
                });

                var ledgetTitle = inputFileInfos[i].hadoopFileName;
                var ledgetValue = inputFileInfos[i].hadoopFileName;
                if(null!=ledgetValue && ledgetValue.length>25){
                    ledgetValue = ledgetValue.substring(0,23)+"...";
                }
                if((i+1)%2==0){
                    _fieldset = dojo.create('fieldset',{className:"Even"},dojo.byId(tableSetSelectGridContainer));
                    _legend = dojo.create('legend',{innerHTML:ledgetValue,title:ledgetTitle},_fieldset);
                    _buildSelectColumnGrid(_fieldset,inputFileInfos[i]);
                }else{
                    _fieldset = dojo.create('fieldset',{},dojo.byId(tableSetSelectGridContainer));
                    _legend = dojo.create('legend',{innerHTML:ledgetValue,title:ledgetTitle},_fieldset);
                    _buildSelectColumnGrid(_fieldset,inputFileInfos[i]);
                }
            }
        }
    };

    function _isStoreResults(data){
        var isStoreResults = false;
        if(null!=data && null!=data.propertyList){
            for(var i=0;i<data.propertyList.length;i++){
                var prop = data.propertyList[i];
                if(prop.name=="storeResults" && prop.value=="true"){
                    isStoreResults = true;
                    break;
                }else if(prop.name=="storeResults" && prop.value=="false"){
                    isStoreResults = false;
                    break;
                }
            }
        }else{
            isStoreResults = false;
        }

       return isStoreResults

    };

    function _buildSelectColumnGrid(container,fileInfo){
        var store = getSelectColumnGridDataStore(fileInfo);
        var layout = getSelectColumnGridLayout();
        var grid = new dojox.grid.DataGrid({
                store: store,
                structure: layout,
                style:"height:90%;with:100%",
                canSort: function(){return false;},
                onDeselected:function(rowIndex){
                    if(onChangeStatusUnchecked == true){
                        onChangeStatusUnchecked = false
                    }else{
                        var grid = this;
                        _updateMappingGridColumnSelected(grid,rowIndex);
                    }
                },
                onSelected:function(rowIndex){
                    if(onChangeStatusChecked==true){
                        onChangeStatusChecked=false;
                    }else{
                        var grid = this;
                        _updateMappingGridColumnSelected(grid,rowIndex);
                    }

                },
                onRowClick:function(event){
                    this.selection.toggleSelect(event.rowIndex);
                }

            },
            dojo.create('div',null,container));

        //dojo.connect(grid,"onCellDblClick",selectGridCellDblClickCallback);
        grid.startup();
        selectGridWidgets.push(grid);

    };

    function selectGridCellDblClickCallback(event){
         currentMappingRow = event.rowIndex;
         currentMappingCell = event.cellIndex;
        if(currentMappingCell>0){
            var grid = this;
            var rowItem = grid.getItem(currentMappingRow);
            var fileId = "";
            for(var prop in columnOrder){
                if(prop!=null){
                    if(columnOrder[prop]==(currentMappingCell-1)){
                        fileId = prop;
                        break;
                    }
                }
            }
            currentCellValue = rowItem[fileId+"_name"];
        }

    };
    function updateCellIndex(event){
        currentMappingCell = event.cellIndex;
        currentMappingRow =  event.rowIndex;
        if(currentMappingCell>0){
            var grid = this;
            var rowItem = grid.getItem(currentMappingRow);
            var fileId = "";
            for(var prop in columnOrder){
                if(prop!=null){
                    if(columnOrder[prop]==(currentMappingCell-1)){
                        fileId = prop;
                        break;
                    }
                }
            }
            currentCellValue = rowItem[fileId+"_name"];
        }
    }

    function getSelectColumnGridDataStore(fileInfo){
        var dataItems = [];
        if(null!=fileInfo && fileInfo.columnInfo!=null && null!=fileInfo.columnInfo.columnNameList){
            var nameList = fileInfo.columnInfo.columnNameList;
            var typeList = fileInfo.columnInfo.columnTypeList;
            for(var i=0;i<nameList.length;i++){
                dataItems.push({
                    id:"col_"+i,
                    columnName:nameList[i],
                    columnType:typeList[i],
                    fileInfo:fileInfo.hadoopFileName,
                    fileId:fileInfo.operatorUUID
                });
            }

        };
        return  new dojo.data.ItemFileReadStore({data: {
            identifier: 'id',
            items: dataItems
        }});

    };

    function getSelectColumnGridLayout(){
        return [{type: "dojox.grid._CheckBoxSelector"},
            [
                {'name': alpine.nls.hadoop_tableset_selecttable_column_available, 'field': 'columnName', 'width': '40%'},
                {'name': alpine.nls.hadoop_tableset_selecttable_column_type, 'field': 'columnType',width: '60%'}
            ]];
    };

    function _buildColumnMappingGrid(){
        dojo.empty(tableSetConditionGridContainer);
        var layout = _buildColumnMappingGridLayout();
        var store = _buildColumnMappingDataStore();
        var gridWidth = 150+180*inputFileInfos.length;
        var grid = new dojox.grid.DataGrid({
                id:tableSetConditionGrid,
                store: store,
                structure: layout,
                style:"height:90%;with:"+gridWidth+"px",
                canSort: function(){return false;},
                onRowClick:function(event){return false;}

            },
        dojo.create('div',null,dojo.byId(tableSetConditionGridContainer)));
        dojo.connect(grid,"onCellDblClick",selectGridCellDblClickCallback);
        dojo.connect(grid,"onCellClick",updateCellIndex);

        grid.startup();
        mappingGridWidets.push(grid);
        //selectGridWidges.push(grid);
    };

    function _buildColumnMappingDataStore(){
        var dataItems = [];
        if(null!=tableSetModel && null!=tableSetModel.outputColumns){
            var outColumns = tableSetModel.outputColumns;
            for(var i=0;i<outColumns.length;i++){
                if(null!=outColumns[i]){
                    var mappingObj = {
                        id:"row_"+i,
                        columnName:outColumns[i].columnName,
                        columnType:outColumns[i].columnType
                    };
                    var mapping = outColumns[i].mappingColumns;
                    if(null!=mapping){
                        var orderMappingObj = [];
                        for(var j=0;j<inputFileInfos.length;j++){
                            if(null!=mapping[j]){
                                var columnType = _getColumnType(mapping[j].operatorModelID,mapping[j].columnName);
                                var colName = "";
                                if(null == columnType || columnType == ""){
                                    colName = "";
                                }else{
                                    colName = mapping[j].columnName;
                                }
                                orderMappingObj.push(
                                    {
                                        operatorModelID:mapping[j].operatorModelID,
                                        columnName:colName,
                                        columnType:columnType,
                                        columnOrder:columnOrder[mapping[j].operatorModelID]
                                    }
                                );
                            }else{
                                orderMappingObj.push(
                                    {
                                        operatorModelID:inputFileInfos[j].operatorUUID,
                                        columnName:"",
                                        columnType:"",
                                        columnOrder:columnOrder[inputFileInfos[j].operatorUUID]
                                    }
                                );

                            }
                        }
                        orderMappingObj.sort(_sortOrderMappingObj);
                        //console.log(orderMappingObj);
                        _updateSelectColumGridStatus(orderMappingObj);
                        for(j=0;j<orderMappingObj.length;j++){
                            mappingObj[orderMappingObj[j].operatorModelID]=orderMappingObj[j].operatorModelID;
                            mappingObj[orderMappingObj[j].operatorModelID+"_name"] = orderMappingObj[j].columnName;
                            mappingObj[orderMappingObj[j].operatorModelID+"_type"] = orderMappingObj[j].columnType;
                        }
                    }
                    dataItems.push(mappingObj)
                }
            }

        }
        return  new dojo.data.ItemFileWriteStore({data: {
            identifier: 'id',
            items: dataItems
        }});

    };

    function _getColumnType(modelID,columnName){
        var columnType = ""
        if(selectGridWidgets!=null){
           var grid = null;
          for(var i=0;i<selectGridWidgets.length;i++){
              var gridItems = selectGridWidgets[i].store._arrayOfAllItems;
              if(gridItems[0]!=null && null!=gridItems[0].fileId && modelID==gridItems[0].fileId[0]){
                  grid = selectGridWidgets[i];
                  break;
              }
          }
          if(null!=grid){
              var items = grid.store._arrayOfAllItems;
              for(var j=0;j<items.length;j++){
                  if(items[j]!=null && items[j].columnType!=null && columnName==items[j].columnName[0]){
                      columnType = items[j].columnType[0];
                      break;
                  }
              }

          }
        }
        return columnType;

    };

    function _sortOrderMappingObj(obj1,obj2){
        if(obj1.columnOrder>obj2.columnOrder){
            return 1;
        }else if(obj1.columnOrder<obj2.columnOrder){
            return -1;
        }else{
            return 0
        }
    };

    function _updateSelectColumGridStatus(mappingObjs){
         if(null!=mappingObjs){
             for(var i=0;i<selectGridWidgets.length;i++){
                 var grid = selectGridWidgets[i];
                 var gridItems = selectGridWidgets[i].store._arrayOfAllItems;
                 if(null!=gridItems){
                     for(var j=0;j<gridItems.length;j++){
                         var gridItem = gridItems[j];
                         for(var k=0;k<mappingObjs.length;k++){
                             if(gridItem.fileId[0] == mappingObjs[k].operatorModelID && gridItem.columnName[0]==mappingObjs[k].columnName){
                                 grid.selection.setSelected(j,true);
                             }
                         }
                     }
                 }

             }

         }
    }

    function _buildColumnMappingGridLayout(){
        var dataGridLayout =[{type: "dojox.grid._CheckBoxSelector"},
            [
                {'name': alpine.nls.hadoop_tableset_columnmapping_outputcolumn, 'field': 'columnName',editable:true,
                    type:dojox.grid.cells._Widget,
                    widgetClass: dijit.form.ValidationTextBox,
                    widgetProps:{
                        required:true,
                        regExp:"[a-zA-Z][\\w]*",
                        onChange:function(){
                            //will to do sth
                            //console.log(currentEditItem);
                            //updateConditionGridAliasColumn(currentEditItem,this.get('value'));
                        }
                    },
                    //'width': '20%'
                    'width': '150px'
                }
            ]];
        if(null!=inputFileInfos && inputFileInfos.length>0){
            columnOrder={};
            for(var i=0;i<inputFileInfos.length;i++){
                var options = _buildOptions(inputFileInfos[i]);
                dataGridLayout[1].push(
                    {
                        name:inputFileInfos[i].hadoopFileName,
                        field:inputFileInfos[i].operatorUUID+"_name",
                        editable:true,
                        type:dojox.grid.cells._Widget,
                        widgetClass: dijit.form.Select,
                        widgetProps:{
                            required:false,
                            options:options,
                            style:"width:100%",
                            //regExp:"[\\w]+",
                            onChange:mappingOptionOnChange
                        },
                        width:"180px"
                    }
                );
                columnOrder[inputFileInfos[i].operatorUUID]=i;
            }

        }

        return dataGridLayout;

    }

    function _buildOptions(inputFileInfo){
        var options = [{
            label:" ",
            value:"",
            valueType:"",
            title:""
        }];
        if(null!=inputFileInfo && inputFileInfo.columnInfo!=null){
            var nameList = inputFileInfo.columnInfo.columnNameList;
            var typeList = inputFileInfo.columnInfo.columnTypeList;
            for(var i=0;i<nameList.length;i++){
                options.push({
                    label:nameList[i],
                    value:nameList[i],
                    valueType:typeList[i],
                    title:nameList[i]
                })
            }
        }
        return options;
    };

    function mappingOptionOnChange(){
        if(currentMappingRow!=-1 && currentMappingCell!=-1){
            onChangeStatusChecked = true;
            onChangeStatusUnchecked = true;
            var mappingGrid = dijit.byId(tableSetConditionGrid);
            var columnId = "";
            for(var prop in columnOrder){
                if((currentMappingCell-1)==columnOrder[prop]){
                    columnId = prop;
                    break;
                }
            }


                if (columnId != "") {
                    var mappingGridDataItems = mappingGrid.store._arrayOfAllItems;
                    var selectGrid = _getSelectGridInSelectGridWidgets(columnId);
                    var columnValues = [];
                    for (var i = 0; i < mappingGridDataItems.length; i++) {
                        var colValue = mappingGridDataItems[i][columnId + "_name"][0];
                        if (null == colValue) {
                            continue;
                        }
                        if(i==currentMappingRow){
                            colValue = this.value;
                        }
                        columnValues.push(colValue);
                    }

                    if (dojo.indexOf(columnValues, this.value) != -1) {
                        //popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_select_alert_tip);
                        //var rowIndex = dojo.indexOf(columnValues,this.value);
                        //this.set("value","");
                        var valueCount = 0;
                        for(var k=0;k<columnValues.length;k++){
                            if(columnValues[k]==this.value){
                                valueCount++
                            }
                        }

                        if (currentCellValue != "" && valueCount>1) {
                            this.set("value", currentCellValue[0]);
                            //update type


                        }
                        var dataType = _getColumnType(columnId,this.value);
                        _updateMappingGridDataTypeByID(columnId,dataType,currentMappingRow);
                        _updateSelectColumGridStatusForChangeSelect(selectGrid, columnValues);
                    } else {
                        var selectValue = this.get("value");
                        if (null != selectValue) {
                            columnValues.push(selectValue);
                            //update type
                            var dataType = _getColumnType(columnId,selectValue);
                            _updateMappingGridDataTypeByID(columnId,dataType,currentMappingRow);
                            _updateSelectColumGridStatusForChangeSelect(selectGrid, columnValues);
                        }


                    }

                }

        }
    };

    function _updateMappingGridDataTypeByID(columnId,dataType,rowNumber){
          var mappingGrid = dijit.byId(tableSetConditionGrid);
          if(null!=mappingGrid){
              var rowItems = mappingGrid.store._arrayOfAllItems;
              if(null!=rowItems && null!=rowItems[rowNumber] && null!=rowItems[rowNumber][columnId+"_type"]){
                  rowItems[rowNumber][columnId+"_type"][0]=dataType;
              }
          }
    };

    function _getSelectGridInSelectGridWidgets(fileId){
        var grid = null;
        if(null!=selectGridWidgets){
              for(var i=0;i<selectGridWidgets.length;i++){
                  var gridDataItem = selectGridWidgets[i].store._arrayOfAllItems;
                  if(gridDataItem[0].fileId!=null && gridDataItem[0].fileId[0] == fileId){
                      grid = selectGridWidgets[i];
                      break;
                  }
              }
        }
       return grid;
    };

    function _updateSelectColumGridStatusForChangeSelect(selectGrid,columnValue){
        if(selectGrid!=null){
            var gridItems = selectGrid.store._arrayOfAllItems;
            if(null!=gridItems){
                for(var i=0;i<gridItems.length;i++){
                    if(dojo.indexOf(columnValue,gridItems[i].columnName[0])!=-1){
                        selectGrid.selection.setSelected(i,true);
                    }else{
                        selectGrid.selection.deselect(i);
                    }

                }
            }
        }

    };

    function _updateMappingGridColumnSelected(grid,rowIndex){
        if(null!=grid){
            //grid.render();
            var selectStatusArray = grid.selection.selected;
            //alert(selectStatusArray[rowIndex]);
            var rowItem = grid.store._arrayOfAllItems[rowIndex];
            //var rowItem = grid.getItem(rowIndex);
            var fileId = rowItem.fileId[0];
            var mappingGrid = dijit.byId(tableSetConditionGrid);
            if(mappingGrid!=null){
                var gridItems = mappingGrid.store._arrayOfAllItems;

                for(var i=0;i<gridItems.length;i++){
                     var gridItem = gridItems[i];

                    if(selectStatusArray[rowIndex]==null && gridItem[fileId+"_name"][0]==rowItem.columnName[0]){
                        gridItem[fileId+"_name"][0]="";
                        gridItem[fileId+"_type"][0]="";
                        break;
                    }else if(selectStatusArray[rowIndex]==true && gridItem[fileId+"_name"][0]==""){
                        gridItem[fileId+"_name"][0]=rowItem.columnName[0];
                        gridItem[fileId+"_type"][0]=rowItem.columnType[0];
                        break;
                    }
                }
                var inMappingGrid = false;
                for(var j=0;j<gridItems.length;j++){
                      var itm = gridItems[j];
                      if(null!=itm[fileId+"_name"] && itm[fileId+"_name"][0]==rowItem.columnName[0]) {
                          inMappingGrid=true;
                      }
                }

                if(inMappingGrid==false && selectStatusArray[rowIndex]==true){
                    var newGridItem = {
                        id:["row_"+gridItems.length],
                        columnType:[rowItem.columnType[0]],
                        columnName:[rowItem.columnName[0]]
                    }
                    for(var prop in columnOrder){
                        if(null!=prop){
                            if(prop==rowItem.fileId[0]){
                                var columnType  = _getColumnType(rowItem.fileId[0],rowItem.columnName[0]);
                                newGridItem[rowItem.fileId[0]]=[rowItem.fileId[0]];
                                newGridItem[rowItem.fileId[0]+"_name"]=[rowItem.columnName[0]];
                                newGridItem[rowItem.fileId[0]+"_type"]=[columnType];
                            }else{
                                newGridItem[prop] = [""];
                                newGridItem[prop+"_name"] =[""];
                                newGridItem[prop+"_type"] =[""];
                            }
                        }
                    }

                    gridItems.push(newGridItem);
                }
                var newStore = buildNewTableMappingDataStore(gridItems);
                mappingGrid.setStore(newStore);
                mappingGrid.render();
            }
        }
    };


    function buildNewTableMappingDataStore(gridItems){
        var dataItems = [];
        if(gridItems!=null){
            for(var i=0;i<gridItems.length;i++){
                var gridItem = gridItems[i];
                if(null!=gridItem){
                    var status = true;
                    for(var prop in columnOrder){
                       if(null!=prop && ""!=prop){
                           status = status && (null!=gridItem[prop+"_name"]) && (gridItem[prop+"_name"][0]=="");
                       }
                    }
                    if(status==true){
                        gridItems.splice(i,1);
                    }
                }
            }
          //dataItems
          for(i=0;i<gridItems.length;i++){
              var dataItem = {
                  id:"row_"+i,
                  columnName:gridItems[i].columnName[0],
                  columnType:gridItems[i].columnType[0]
              }
             var orderColumnObjs = [];
              for(var p in columnOrder){
                  if(p!=null && ""!=p){
                      orderColumnObjs.push({
                          operatorModelID:p,
                          columnName:gridItems[i][p+"_name"],
                          columnType:gridItems[i][p+"_type"],
                          columnOrder:columnOrder[p]
                      });
                  }
              }
              orderColumnObjs.sort(_sortOrderMappingObj);
              for(var j=0;j<orderColumnObjs.length;j++){
                  dataItem[orderColumnObjs[j].operatorModelID] = orderColumnObjs[j].operatorModelID;
                  dataItem[orderColumnObjs[j].operatorModelID+"_name"] = orderColumnObjs[j].columnName;
                  dataItem[orderColumnObjs[j].operatorModelID+"_type"] = orderColumnObjs[j].columnType;
              }
              dataItems.push(dataItem);
          }
        }
        return  new dojo.data.ItemFileWriteStore({data: {
            identifier: 'id',
            items: dataItems
        }});

    };

    function deleteMappingSelect(){
        var grid = dijit.byId(tableSetConditionGrid);
        var selectedStatus = grid.selection.selected;
        for(var i=0;i<selectedStatus.length;i++){
            if(null!=selectedStatus[i] && selectedStatus[i]==true){
                var gridItem = grid.getItem(i);
                grid.rowRemoved(i);
            }

        }
    }

    function saveHadoopTableSetModel(){
        if(false==_validateEmptyValue()){
            popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_empty_alert_tip);
            return false;
        }
        if(false==_validateDataType()){
           popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_type_alert_tip);
           return false;
        }

        if(false == _validateOutPutColumn()){
            return false;
        }

        var rowItems = null;
        if(false ==_updateDataType()){
            popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_type_alert_tip);
            return false;
        }else{
            rowItems = _updateDataType();
        }
        if(null!=rowItems){
            // save model
            // update union files
            var unionFiles = [];
            for(var i=0;i<inputFileInfos.length;i++){
                if(inputFileInfos[i]!=null){
                    unionFiles.push(
                        {
                            file:inputFileInfos[i].hadoopFileName,
                            operatorModelID:inputFileInfos[i].operatorUUID
                        }
                    )
                }
            }
            tableSetModel.unionFiles = dojo.clone(unionFiles);
            //update
            var outputColumns = [];
            for(var j=0;j<rowItems.length;j++){
                var outputColumn = {
                    columnName:rowItems[j].columnName[0],
                    columnType:rowItems[j].columnType[0],
                    mappingColumns:[]
                };
                for(var p in columnOrder){
                    var mappingColumn = {
                        columnName:rowItems[j][p+"_name"][0],
                        operatorModelID:rowItems[j][p][0]
                    }
                    outputColumn.mappingColumns.push(mappingColumn);
                }
                outputColumns.push(outputColumn);
            }
            tableSetModel.outputColumns = dojo.clone(outputColumns);
        }
        setButtonBaseClassValid(sourceButtonId);
        hideHadoopTableSetDlg();
    };

    function _validateEmptyValue(){
         var mappingGrid = dijit.byId(tableSetConditionGrid);
         var storeItems = mappingGrid.store._arrayOfAllItems;
         if(null!=storeItems){
            for(var i=0;i<storeItems.length;i++){
                var dataItem = storeItems[i];
                for(var proName in columnOrder){
                    if(dataItem.columnName[0]==null || dataItem.columnName[0]=="" ||
                       dataItem[proName+"_name"]==null || dataItem[proName+"_name"]==""){
                        return false;
                    }
                }
            }
            return true;
        }else{
             return false;
         }

    };

    function _validateDataType(){
        var mappingGrid = dijit.byId(tableSetConditionGrid);
        var storeItems = mappingGrid.store._arrayOfAllItems;
        var rowsDataType=[];
        for(var i=0;i<storeItems.length;i++){
           var rowDataType = [];
            for(var p in columnOrder){
                rowDataType.push(storeItems[i][p+"_type"][0]);
            }
            rowsDataType.push(rowDataType);
        }

        for(var j=0;j<rowsDataType.length;j++){
            var rowType = rowsDataType[j];
            for(var k=0;k<(rowType.length-1);k++){
                if(hdDataTypeUtil.isSimilarType(rowType[k],rowType[k+1])==false){
                    return false;
                }
            }
        }
        return true;

    };

    function _validateOutPutColumn(){
       var mappingGrid = dijit.byId(tableSetConditionGrid);
       var outputColumns = [];
       if(mappingGrid!=null){
           var rowItems = mappingGrid.store._arrayOfAllItems;
           if(null!=rowItems){
               for(var i=0;i<rowItems.length;i++){
                   var itm = rowItems[i];
                   outputColumns.push(itm.columnName[0]);
               }
           }
       }
        outputColumns.sort();
        for(i=0;i<rowItems.length;i++){
            if(null==outputColumns[i] || ""==outputColumns[i]){
                popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_output_column_empty_tip);
                return false;
            }
        }
        var reg = /^[a-zA-Z][\w]*//*
;
        for(i=0;i<rowItems.length;i++){
            if(reg.test(outputColumns[i])==false){
                popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_output_column_start_number_tip);
                return false;
            }
        }
        for(i=0;i<rowItems.length-1;i++){
            if(outputColumns[i]==outputColumns[i+1]){
                popupComponent.alert(alpine.nls.hadoop_tableset_columnmapping_output_column_same_tip);
                return false;
            }
        }
       return true;
    };

    function _updateDataType(){
        var mappingGrid = dijit.byId(tableSetConditionGrid);
        var rowItems = mappingGrid.store._arrayOfAllItems;
        if(null!=mappingGrid && null!=rowItems){
            if(null!=rowItems){
               for(var i=0;i<rowItems.length;i++){
                   var item = rowItems[i];
                   var itemTypes = [];
                   for(var p in columnOrder){
                       itemTypes.push(item[p+"_type"][0]);
                   }
                   var rowType = _guessColumnType(itemTypes);
                   if("MatchTypeError"==rowType || ""==rowType){
                       return false;
                   }
                   item.columnType[0] = rowType;
               }
            }
            return rowItems
        }else{
            return false;
        }


    };

    function _guessColumnType(columItems){
        var HADOOPDATATYPE = hdDataTypeUtil.hadoopDatatype;
        var  hasCharArray = false;
        var  hasNumric = false;
        for (var i=0;i<columItems.length;i++) {
            if (columItems[i].toLowerCase() == HADOOPDATATYPE.CHARARRAY) {
                hasCharArray = true;
            } else if (columItems[i].toLowerCase() == HADOOPDATATYPE.INT
                || columItems[i].toLowerCase() == HADOOPDATATYPE.FLOAT
                || columItems[i].toLowerCase() == HADOOPDATATYPE.LONG
                || columItems[i].toLowerCase() == HADOOPDATATYPE.DOUBLE) {
                hasNumric = true;
            }
        }

        if (hasCharArray == true && hasNumric == true) {
            return "MatchTypeError"
        }

        if (hasCharArray == true && hasNumric == false) {
            return HADOOPDATATYPE.CHARARRAY;
        } else {
            var baseType = HADOOPDATATYPE.BYTEARRAY;
            for (var j=0;j<columItems.length;j++) {
                if (columItems[j]== HADOOPDATATYPE.INT
                    && baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY) {
                    baseType = HADOOPDATATYPE.INT;
                } else if( columItems[j]==HADOOPDATATYPE.LONG &&
                    (baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY || baseType.toLowerCase()==HADOOPDATATYPE.INT)) {
                    baseType = HADOOPDATATYPE.LONG;
                } else if( columItems[j]==HADOOPDATATYPE.FLOAT &&
                    (baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY
                    || baseType.toLowerCase()==HADOOPDATATYPE.LONG
                    || baseType.toLowerCase()==HADOOPDATATYPE.INT)) {
                    baseType = HADOOPDATATYPE.FLOAT;
                } else if( columItems[j]==HADOOPDATATYPE.DOUBLE &&
                    (baseType.toLowerCase()==HADOOPDATATYPE.BYTEARRAY
                    || baseType.toLowerCase()==HADOOPDATATYPE.LONG
                    || baseType.toLowerCase()==HADOOPDATATYPE.FLOAT
                    || baseType.toLowerCase()==HADOOPDATATYPE.INT)) {
                    baseType = HADOOPDATATYPE.DOUBLE;
                }

            }
            return baseType;
        }

    };


    function cancelHadoopTableSetModel(){
        hideHadoopTableSetDlg();
    };

    function hideHadoopTableSetDlg(){
        var dlg = dijit.byId(tableSetConfigDlg);
        if(dlg!=null){
            dlg.hide();
        }
    };

    function _onHideDestroySomething(){
        if(null!=selectGridWidgets){
            for(var i=0;i<selectGridWidgets.length;i++){
                if(null!=selectGridWidgets[i]){
                    selectGridWidgets[i].destroyRecursive();
                }
            }
            selectGridWidgets=[];
        }
        if(null!=mappingGridWidets){
            for(i=0;i<mappingGridWidets.length;i++){
                if(null!=mappingGridWidets[i]){
                    mappingGridWidets[i].destroyRecursive();
                }
            }
            mappingGridWidets = [];
        }
    };

    function _destroyTableSetModel(){
        tableSetModel=null;
    }

    function getTableSetModel(){
        return tableSetModel;
    };

    function clearTableSetModel(){
        tableSetModel = null;
    };

    return {
        initHadoopTableSetVariable:initHadoopTableSetVariable,
        showHadoopTableSetCfgDlg:showHadoopTableSetCfgDlg,
        getTableSetModel:getTableSetModel,
        clearTableSetModel:clearTableSetModel
    }
});*/
