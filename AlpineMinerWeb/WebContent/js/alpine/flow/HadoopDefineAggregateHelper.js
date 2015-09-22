/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * findandreplaceparameterHelper
 * 
 * Author Will
 * Version 2.7
 * Date 2012-05-15
 */
define(["alpine/props/HadoopDataTypeUtil"],function(hdDataTypeUtil){
    var defineDlg = "alpine_props_hadoop_define_agg_dialog";
    var columnWidthType = [];
    var aggObj = null;

    dojo.ready(function(){
        dojo.connect(dijit.byId('alpine_props_hadoop_define_agg_dialog_Btn_Done'),"onClick",_btnDone4hdAgg);
        dojo.connect(dijit.byId('alpine_props_hadoop_define_agg_dialog_Btn_Cancel'),"onClick",_btnCancel4hdAgg);
        dojo.connect(dijit.byId(defineDlg),"onHide",_destroySometh);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_to_right'),"onClick",_btnFuncGroupMove_right);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_to_left'),"onClick",_btnFuncGroupMove_left);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_group_moveup'),"onClick",_btnFuncGroupMove_up);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_group_movedown'),"onClick",_btnFuncGroupMove_down);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_group_clear'),"onClick",_btnFuncGroupMove_clear);
        dojo.connect(dijit.byId('hadoop_aggFunctionSelect'),'onChange',_initAggregateDropList4Column);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_method_to_right'),'onClick',_btnAddAggMethodToRight);
        dojo.connect(dijit.byId('hadoop_agg_Alias'),"onChange",_updateAliasValue);
        dojo.connect(dijit.byId('alpine_props_hadoop_agg_method_clear'),'onClick',_deleteAggMethodGridItm);

    });

    function setAggObj(agg){
        aggObj = agg;
    };

    function getAggObj(){
           return aggObj
    };

    function showDefineAggDialog(prop){
        var dlg = dijit.byId(defineDlg);
        if(null!=dlg){
        	dlg.titleBar.style.display = "none";
            dlg.show();
        }
        /*
        if(null!=prop){
            aggObj =  dojo.clone(prop);
        }
        */
        _inintColumnWidthType();
        _initGroupByColumnList();
        _initAggregateDropList4Column();
        _initHadoopAggMethodGrid();
        //console.log(aggObj);
    };

    function _inintColumnWidthType(){
         if(null!=CurrentOperatorDTO && null!=CurrentOperatorDTO.inputFileInfos){
             var columnNames = CurrentOperatorDTO.inputFileInfos[0].columnInfo.columnNameList;
             var columnTypes = CurrentOperatorDTO.inputFileInfos[0].columnInfo.columnTypeList;
             if(null!=columnNames && null!=columnTypes && columnNames.length>0 && columnNames.length==columnTypes.length){
                 columnWidthType = [];
                 for(var i=0;i<columnNames.length;i++){
                     columnWidthType.push({"columnName":columnNames[i],"columnType":columnTypes[i]});
                 }
             }
         }
    };

    function _initGroupByColumnList(){
        if(null!=aggObj && columnWidthType!=null && columnWidthType.length>0){


            var groupByList = null;
            if(null!=aggObj.aggregateFieldsModel && null!=aggObj.aggregateFieldsModel.groupByFieldList){
                groupByList = aggObj.aggregateFieldsModel.groupByFieldList;
                dojo.empty('hadoop_agg_groupby_src_select');
                dojo.empty('hadoop_agg_groupby_dst_select');
                //src list
                for(var i=0;i<columnWidthType.length;i++){
                    if(dojo.indexOf(groupByList,columnWidthType[i].columnName)==-1){
                        dojo.create('option',{style:"height:18px;",className:"hadoopGroupByOption",value:columnWidthType[i].columnName,innerHTML:columnWidthType[i].columnName},dojo.byId('hadoop_agg_groupby_src_select'));
                    }
                }
                //dst list
                for(var j=0;j<groupByList.length;j++){
                        dojo.create('option',{style:"height:18px;",className:"hadoopGroupByOption",value:groupByList[j],innerHTML:groupByList[j]},dojo.byId('hadoop_agg_groupby_dst_select'));
                }
            }



        }
    };

    function _btnFuncGroupMove_right(){
        var srcList = dojo.byId('hadoop_agg_groupby_src_select');
        var dstList = dojo.byId('hadoop_agg_groupby_dst_select');
        var srcOptions = dojo.query(".hadoopGroupByOption",srcList);
        var srcOpLen = srcOptions.length;
        for(var i=0;i<srcOpLen;i++){
            if(srcOptions[i].selected==true){
                dojo.place(srcOptions[i],dstList,"last");
            }
        }

        _setGroupbyListFoucs("hadoop_agg_groupby_src_select");

    };

    function _btnFuncGroupMove_left(){
        var srcList = dojo.byId('hadoop_agg_groupby_src_select');
        var dstList = dojo.byId('hadoop_agg_groupby_dst_select');
        var dstOptions = dojo.query(".hadoopGroupByOption",dstList);
        var dstOpLen = dstOptions.length;
        for(var i=0;i<dstOpLen;i++){
            if(dstOptions[i].selected==true){
                dojo.place(dstOptions[i],srcList,"last");
            }
        }
        _setGroupbyListFoucs("hadoop_agg_groupby_dst_select");
    };

    function _btnFuncGroupMove_up(){
        var dstList = dojo.byId('hadoop_agg_groupby_dst_select');
        var dstOptions = dojo.query(".hadoopGroupByOption",dstList);
        var dstOpLen = dstOptions.length;
        for(var i=0;i<dstOpLen;i++){
            if(dstOptions[i].selected==true){
               if(dstOptions[i].previousSibling!=null && dstOptions[i].previousSibling.nodeName.toLowerCase()=="option"){
                   var refNode = dstOptions[i].previousSibling;
                   dojo.place(dstOptions[i],refNode,"before");
               }

            }
        }
    };

    function _btnFuncGroupMove_down(){
        var dstList = dojo.byId('hadoop_agg_groupby_dst_select');
        var dstOptions = dojo.query(".hadoopGroupByOption",dstList);
        var dstOpLen = dstOptions.length;
        for(var i=0;i<dstOpLen;i++){
            if(dstOptions[i].selected==true){
                if(dstOptions[i].nextSibling !=null && dstOptions[i].nextSibling .nodeName.toLowerCase()=="option"){
                    var refNode = dstOptions[i].nextSibling ;
                    dojo.place(dstOptions[i],refNode,"after");
                }

            }
        }

    };

    function _btnFuncGroupMove_clear(){
        var dstList = dojo.byId('hadoop_agg_groupby_dst_select');
        var srcList = dojo.byId('hadoop_agg_groupby_src_select');
        var dstOptions = dojo.query(".hadoopGroupByOption",dstList);
        var dstOpLen = dstOptions.length;
        for(var i=0;i<dstOpLen;i++){
               if(dstOptions[i].selected==true && _isInLists(srcList,dstOptions[i])==true){
                   dojo.place(dstOptions[i],srcList,"last");
               }else if(dstOptions[i].selected==true && _isInLists(srcList,dstOptions[i])==false){
                   dojo.byId('hadoop_agg_groupby_dst_select').removeChild(dstOptions[i]);
               }
        }

    };

    function _isInLists(domList,dom){
        if(null!=domList){
            for(var i=0;i<domList.length;i++){
                if(domList[i].value==dom.value){
                    return true;
                }
            }
        }
        return false;
    };

    function _setGroupbyListFoucs(id){
        var columnList = dojo.query(".hadoopGroupByOption",id);
        if(null!=columnList && columnList.length>0){
            columnList[0].selected = true;
        }

    }

    function _initHadoopAggMethodGrid(){
        var grid = dijit.byId('hadoop_agg_method_grid');
        var itemList = [];
        var aggMethodList = aggObj.aggregateFieldsModel.aggregateFieldList;
        if(null==aggMethodList){
            aggMethodList=[];
        }
        for(var i=0;i<aggMethodList.length;i++){
            itemList.push({
                columnName:"col_"+i+"_"+new Date().getTime()+"_"+Math.random(),
                aggregateExpression:aggMethodList[i].aggregateExpression,
                alias:aggMethodList[i].alias,
                dataType:aggMethodList[i].dataType
           });
        }
        var data = {
            identifier:"columnName",
            items:itemList
        }
        var store = new dojo.data.ItemFileReadStore({data: data});
        if(null==grid){
            var layout = [[
                {'name': alpine.nls.hadoop_aggregate_method_grid_column1, 'field': 'aggregateExpression', 'width': '50%'},
                {'name': alpine.nls.hadoop_aggregate_method_grid_column2, 'field': 'alias', 'width': '50%'}

            ]];
            var grid = new dojox.grid.DataGrid({
                    id: 'hadoop_agg_method_grid',
                    store: store,
                    style:"height:190px",
                    structure: layout,
                    selectionMode: "single"
                },
                dojo.create('div',null,'hadoop_agg_method_grid_container'));
            dojo.connect(grid,"onRowClick",_gridOnRowClick);
            grid.startup();
        }else{
           grid.setStore(store);
           grid.render();

        }

    };

    function _gridOnRowClick(event){
        var grid = dijit.byId('hadoop_agg_method_grid');
        var aliasText = dijit.byId('hadoop_agg_Alias');
        var hiddenRowIndex = dojo.byId('hadoop_agg_selected_row_idx');
        var rowItem = grid.getItem(event.rowIndex);
        hiddenRowIndex.value = event.rowIndex;
        //grid.selection.select(event.rowIndex);
        aliasText.set('value',rowItem['alias']);

    };

    function _updateAliasValue(){
        var grid = dijit.byId('hadoop_agg_method_grid');
        var aliasText = dijit.byId('hadoop_agg_Alias');
        var hiddenRowIndex = dojo.byId('hadoop_agg_selected_row_idx');
        if(null==hiddenRowIndex.value || hiddenRowIndex.value=="" ){
            return;
        }
        var selectItem = grid.getItem(parseInt(hiddenRowIndex.value));
        var arrayItems = grid.store._arrayOfAllItems;
        if(null!=selectItem && null!=arrayItems && arrayItems.length>0){
           for(var i=0;i<arrayItems.length;i++){
             if(selectItem.columnName[0]==arrayItems[i].columnName[0]){
                 arrayItems[i].alias[0] = (aliasText.get('value')!=""&& null!=aliasText.get('value'))?aliasText.get('value'):selectItem.alias[0];
                 var data = {
                     identifier:"columnName",
                     items:arrayItems
                 };
                 var store = new dojo.data.ItemFileReadStore({data: data});
                 grid.setStore(store);
                 grid.render();
                 break;
             }
           }
        }
    }

    function _initAggregateDropList4Column(){
        var aggSelect = dijit.byId('hadoop_aggFunctionSelect');
        if(null!=aggSelect && null!=columnWidthType){
            var needType = _getColumnType(aggSelect.get('value'));
            _buildAggregateDropList4Column(needType);
        }

    };

    function _buildAggregateDropList4Column(needType){
          if(null!=needType && needType=="NotNum"){
           dojo.empty("hadoop_aggFunctionSelect_column_name");
           for(var i=0;i<columnWidthType.length;i++){
               dojo.create("option",{className:"hadoopAggMethodColumn",innerHTML:columnWidthType[i].columnName,value:columnWidthType[i].columnName},dojo.byId('hadoop_aggFunctionSelect_column_name'));
           }
          }else{
            dojo.empty("hadoop_aggFunctionSelect_column_name");
            for(var i=0;i<columnWidthType.length;i++){
                  if(hdDataTypeUtil.isNumberType(columnWidthType[i].columnType)==true){
                      dojo.create("option",{className:"hadoopAggMethodColumn",innerHTML:columnWidthType[i].columnName,value:columnWidthType[i].columnName},dojo.byId('hadoop_aggFunctionSelect_column_name'));
                  }
            }
          }
        _registHadoopAggMethodColumn_dbclick();
    };

    function _registHadoopAggMethodColumn_dbclick(){
        var items = dojo.query("option.hadoopAggMethodColumn",dojo.byId("hadoop_aggFunctionSelect_column_name"));
        if(null!=items && items.length>0){
            dojo.forEach(items,function(itm,idx){
                dojo.connect(itm,"ondblclick",function(){
                    _hadoopAggMethodColumn_dbclick(itm);
                });
            });
        }
    };

    function _btnAddAggMethodToRight(){
        var items = dojo.query("option.hadoopAggMethodColumn",dojo.byId("hadoop_aggFunctionSelect_column_name"));
        if(null!=items && items.length>0){
            for(var i=0;i<items.length;i++){
                if(items[i].selected==true){
                    _hadoopAggMethodColumn_dbclick(items[i]);
                }
            }
        }

    };

    function _hadoopAggMethodColumn_dbclick(itm){
        var  selectValue= dijit.byId('hadoop_aggFunctionSelect');
        var grid = dijit.byId('hadoop_agg_method_grid');
        var newDataItem = {};
        var dataItems = [];

        for(var i=0;i<columnWidthType.length;i++){
            if(columnWidthType[i].columnName == itm.value){
                var arrayItems = grid.store._arrayOfAllItems;
                if(null!=arrayItems && arrayItems.length>0){
                    for(var j=0;j<arrayItems.length;j++){
                        dataItems.push({
                            columnName:"col_"+j+"_"+new Date().getTime()+"_"+Math.random(),
                            aggregateExpression:arrayItems[j].aggregateExpression,
                            alias:arrayItems[j].alias,
                            dataType:arrayItems[j].dataType
                        });
                    }
                }
                newDataItem.columnName="col_"+(j+1)+"_"+new Date().getTime()+"_"+Math.random();
                newDataItem.aggregateExpression =selectValue.get('value')+"("+columnWidthType[i].columnName+")";
                newDataItem.alias = selectValue.get('value')+"_"+columnWidthType[i].columnName;
                newDataItem.dataType = _getAggDataType(columnWidthType[i].columnType, selectValue.get('value'));
                dataItems.push(newDataItem);
                var data = {
                    identifier:"columnName",
                    items:dataItems
                };
                var store = new dojo.data.ItemFileReadStore({data: data});
                grid.setStore(store);
                break;
            }
        }

    }

    function _getColumnType(aggName){
       if(null!=aggName && aggName=="COUNT"){
             return "NotNum";
       }else{
           return "Num";
       }
    };

    function _deleteAggMethodGridItm(){
        var hiddenRowIndex = dojo.byId('hadoop_agg_selected_row_idx');
        if(null==hiddenRowIndex.value || hiddenRowIndex.value=="" ){
            return;
        }
        var grid = dijit.byId('hadoop_agg_method_grid');
        var selectItem = grid.getItem(parseInt(hiddenRowIndex.value));
        var arrayItems = grid.store._arrayOfAllItems;
        if(null!=selectItem && null!=arrayItems && arrayItems.length>0){
            for(var i=0;i<arrayItems.length;i++){
                if(selectItem.columnName[0]==arrayItems[i].columnName[0]){
                    arrayItems.splice(i,1);
                    var data = {
                        identifier:"columnName",
                        items:arrayItems
                    };
                    var store = new dojo.data.ItemFileReadStore({data: data});
                    grid.setStore(store);
                    grid.render();
                    hiddenRowIndex.value = "";
                    dijit.byId('hadoop_agg_Alias').set("value","");

                    break;
                }
            }
        }

    };

    function _btnDone4hdAgg(){
        //validate alias repeat
        var grid = dijit.byId('hadoop_agg_method_grid');
        var arrayItems = grid.store._arrayOfAllItems;
        if(null!=arrayItems){
            //alias should not have same name
            var alias = [];
            for(var l=0;l<arrayItems.length;l++){
                alias.push(arrayItems[l].alias[0]);
            }
            alias.sort();
            for(var k=0;k<alias.length;k++){
                if(alias[k]==alias[k+1]){
                    popupComponent.alert(alpine.nls.hadoop_aggregate_method_grid_column2_repeat);
                    return false;
                }
            }
        }


        var module = aggObj.aggregateFieldsModel;
        //module.groupByFieldList;

        module.groupByFieldList = [];

        //
        dojo.empty(module.groupByFieldList);
        var groupByFields = dojo.query("#hadoop_agg_groupby_dst_select>.hadoopGroupByOption");
        if(null!=groupByFields){
            for(var i=0;i<groupByFields.length;i++){
                module.groupByFieldList.push(groupByFields[i].value);
            }
        }
        //module.aggregateFieldList;
        if(null!=module.aggregateFieldList){
            module.aggregateFieldList=[];
        }
        if(null!=arrayItems){
            module.aggregateFieldList=[];
            for(var j=0;j<arrayItems.length;j++){
                module.aggregateFieldList.push(
                    {
                        aggregateExpression:arrayItems[j].aggregateExpression[0],
                        alias:arrayItems[j].alias[0],
                        dataType:arrayItems[j].dataType[0]

                    }
                );
            }
        }
        if(module.groupByFieldList.length==0 || module.aggregateFieldList.length==0){
            setButtonBaseClassInvalid(getSourceButtonId(aggObj));
        }else{
            setButtonBaseClassValid(getSourceButtonId(aggObj));
        }
        _hideDefineAggDlg();
    };

    function _btnCancel4hdAgg(){
        _hideDefineAggDlg();
    };

    function _hideDefineAggDlg(){
        var dlg = dijit.byId(defineDlg);
        if(null!=dlg){
            dlg.hide();
        }
    };
    function _destroySometh(){
         //alert('destroy ...');
        //aggObj=null;
    };

    function _getAggDataType(colType, aggType) {
        if (aggType == "COUNT") {
            return hdDataTypeUtil.hadoopDatatype.LONG;
        } else if (aggType == "AVG") {
            return hdDataTypeUtil.hadoopDatatype.DOUBLE;
        } else { return colType; }
    }

    return {
        showDefineAggDialog:showDefineAggDialog,
        setAggObj:setAggObj,
        getAggObj:getAggObj
    }


});