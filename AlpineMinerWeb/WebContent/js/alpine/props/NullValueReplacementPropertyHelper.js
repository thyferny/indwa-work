/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: NullValueReplacementPropertyHelper
 * Author: Will
 * Date: 12-8-10
 */
define(["alpine/flow/WorkFlowVariableReplacer"], function(variableReplacer){
    var nullValueReplaceDlgId = "nullValueReplaceColumnSelectionDialog";
    var gridContainerId = "nullValueReplaceGridContainer";
    var dlgOKBtn = "btn_ok_4_property_nullvaluereplace_columnName_select";
    var dlgCancelBtn = "btn_cancel_4_property_nullvaluereplace_columnName_select";
    var checkAllId  = "nullValueChangeChkbox_All";
    var replaceForm = "nullValueReplaceColumnForm";

    var propertyID = "";

    //
    var replaceModel = null;
    var widgetsGroup = [];

    dojo.ready(function(){
        dojo.connect(dijit.byId(dlgOKBtn),"onClick",saveNullvaluereplacement);
        dojo.connect(dijit.byId(dlgCancelBtn),"onClick",cancelNullvaluereplacement);
        dojo.connect(dijit.byId(nullValueReplaceDlgId),"onHide",destroyOnDlgHide);
    });

    function initNullvaluereplacement(prop){
        replaceModel = dojo.clone(prop);
    }
    function showNullvaluereplacementDlg(prop){
        var dlg = dijit.byId(nullValueReplaceDlgId);
        if(dlg!=null){
            dlg.show();
        }
        propertyID = prop.name+ID_TAG;
        buildNullValueReplacementGrid();
    };

    function saveNullvaluereplacement(){
        var valueForm = dijit.byId(replaceForm);
        if(valueForm.validate()==false){
            return false;
        }
        var nameValueMap = [];
        if(null!=widgetsGroup){
            for(var i=0;i<widgetsGroup.length;i++){
                var widgetObj = widgetsGroup[i];
                if(widgetObj.widgetCheck.get("checked")==true){
                    nameValueMap.push({
                         columnName:widgetObj.widgetCheck.get("value"),
                         value:widgetObj.widgetInput.get("value")
                        });
                }
            }
        }

        if(CurrentOperatorDTO.classname == "HadoopReplaceNullOperator"){
            for(var i=0;i<nameValueMap.length;i++){
                if(false==_validateHadoopNullValueReplaceMent(nameValueMap[i].columnName,nameValueMap[i].value)){
                    popupComponent.alert(alpine.nls.hadoop_null_value_replace_data_type_error);
                    return false;
                }
            }
        }else{
            //db type
            for(var i=0;i<nameValueMap.length;i++){
                if(false==_validateDBNullValueReplaceMent(nameValueMap[i].columnName,nameValueMap[i].value)){
                    popupComponent.alert(alpine.nls.hadoop_null_value_replace_data_type_error);
                    return false;
                }
            }
        }
        replaceModel.selected = [];
        if(null==replaceModel.nullReplacementModel){
            replaceModel.nullReplacementModel = {};
        }
        replaceModel.nullReplacementModel.nullReplacements = [];
        for(i=0;i<nameValueMap.length;i++){
            replaceModel.selected.push(nameValueMap[i].columnName);
            replaceModel.nullReplacementModel.nullReplacements.push(nameValueMap[i]);
        }
        if(null == replaceModel.selected || replaceModel.selected.length==0){
            setButtonBaseClassInvalid(propertyID) ;
        }else{
            setButtonBaseClassValid(propertyID) ;
        }

        dijit.byId(propertyID).focus();
        hideDlg();
    };


    function cancelNullvaluereplacement(){
        hideDlg();
    };

    function hideDlg(){
        var dlg = dijit.byId(nullValueReplaceDlgId);
        if(dlg!=null){
            dlg.hide();
        }
    };

    function buildNullValueReplacementGrid(){
        //Modify by Will
        if(replaceModel==null){
            return ;
        }
        dojo.empty(gridContainerId);
        var tableGrid = dojo.create("table",{width:"95%",align:"center",style:"text-align:left", cellSpacing: "10"},gridContainerId);
        _budildTableHead(tableGrid);
        _buildTableBody(tableGrid);
    };

    function _budildTableHead(tableGrid){
        var list = replaceModel.fullSelection;
        var selected = replaceModel.selected;

        var theadRow = dojo.create("thead",null,tableGrid);
        var checkTH = dojo.create("td",{title:alpine.nls.nullValueChangeGrid_col_selectAll},theadRow);
        if(dijit.byId(checkAllId)!=null){
            dijit.byId(checkAllId).destroyRecursive();
        }
        new dijit.form.CheckBox({id:checkAllId,title:alpine.nls.nullValueChangeGrid_col_selectAll}, dojo.create("div",null,checkTH));
        if(null!=dijit.byId(checkAllId)){
            //set status
            if(null!=list && null!=selected && list.length==selected.length){
                dijit.byId(checkAllId).set("checked",true);
            }
            //set onclick check status
            dojo.connect(dijit.byId(checkAllId),"onClick",function(){
                var chks = dojo.query("input[type='checkbox']",tableGrid);
                if(null!=chks && null!=chks.length && chks.length>0){
                    if(dijit.byId(checkAllId).checked){
                        for(var i=0;i<chks.length;i++){
                            if(chks[i].id!=checkAllId && !chks[i].checked){
                                chks[i].click();

                            }
                        }
                    }
                    if(!dijit.byId(checkAllId).checked){
                        for(var j=0;j<chks.length;j++){
                            if(chks[j].id!=checkAllId && chks[j].checked){
                                chks[j].click();

                            }
                        }
                    }

                }
            });
        }
         dojo.create("td",{innerHTML:alpine.nls.select_columns,style:"font-weight:bold"},theadRow);
         dojo.create("td",{innerHTML:alpine.nls.nullValueChangeGrid_col_value,style:"font-weight:bold"},theadRow);
    };

    function _buildTableBody(tableGrid){
        var tbl = dojo.create("tbody", {}, tableGrid);
        var list = replaceModel.fullSelection;
        var selected = replaceModel.selected;
        if(null==selected){
            selected = [];
        }
        if(null!=list && null!=selected){
            for(var i=0;i<list.length;i++){
               var row = dojo.create("tr",null,tbl);
               _addRowcontent(row,list[i],selected);
            }
        }
    };

    function _addRowcontent(row,name,selected){

        var checked = false;
        if (null!=selected) {
            for ( var j = 0; j < selected.length; j++) {
                if (name == selected[j]) {
                    checked = true;
                    break;
                }
            }
        }
        var checkBox = new dijit.form.CheckBox({
            name : "Select Columns",
            value : name,
            checked : checked
        }, dojo.create("div",null,dojo.create("td",null,row)));

        dojo.connect(checkBox,"onChange",function(){_checkboxClickCallback(row);});

        dojo.create("label", {
            innerHTML : name
        }, dojo.create("td",null,row));

        var rep_value = null;
        var replaceMentObj = getNullReplacementModelObjByName(name);
        if(null!=replaceMentObj && null!=replaceMentObj.value){
            rep_value = replaceMentObj.value;
        }
        var columnType = null;
        //get type
        var columnName = name;
        columnType = _getColumnTypeByName(columnName);
        if(CurrentOperatorDTO.classname == "HadoopReplaceNullOperator"){
            if(/^@\w+/.test(rep_value)==false){
                if (alpine.props.HadoopDataTypeUtil.isNumberType(columnType) == true) {
                  if (isNaN(rep_value) == true || rep_value == null) {
                        rep_value = "0";
                    }
                } else {
                    if (rep_value == null || (rep_value.substring(0, 1) != "'" && rep_value.substring(rep_value.length - 1) != "'")) {
                        rep_value = "''";
                    }
                }
            }/*else{
                rep_value = variableReplacer.replaceVariable(rep_value);
            }*/
        }
        if(CurrentOperatorDTO.classname == "ReplaceNullOperator"){
            if(/^@\w+/.test(rep_value)==false){
                if (alpine.props.HadoopDataTypeUtil.isNumberType4DB(columnType) == true) {
                    if (isNaN(rep_value) == true || rep_value == null) {
                        rep_value = "0";
                    }
                } else {
                    if (rep_value == null || (rep_value.substring(0, 1) != "'" && rep_value.substring(rep_value.length - 1) != "'")) {
                        rep_value = "''";
                    }
                }
            }/*else{
                rep_value = variableReplacer.replaceVariable(columnValue);
            }*/
        }


        var data = new dijit.form.ValidationTextBox({
            style:"width: 80px",
            required:true,
            trim:true,
            value:rep_value,
            baseClass:"basicTextBox",
            isNumbertype:alpine.props.HadoopDataTypeUtil.isNumberType(columnType)
        }, dojo.create("div", null, dojo.create("td", null, row)));

        if (checked == false) {
            data.set("disabled", true);
        }
        var handler = dojo.connect(checkBox, 'onChange', null, function (evt) {
            var checked = checkBox.checked;
            data.set("disabled", !checked);
            if(checked == true && ""==data.get("value")){
                if(this.isNumbertype==true){
                    data.set("value","0");
                }else{
                    data.set("value","''");
                }
            }
        });

        widgetsGroup.push({
            widgetCheck:checkBox,
            widgetInput:data
        });

    };

    function _getColumnTypeByName(columnName){
        var columnType = "";
           if(CurrentOperatorDTO.classname == "HadoopReplaceNullOperator"){
               if (null != columnName) {
                   var inputInfos = CurrentOperatorDTO.inputFileInfos;
                   for (var ii = 0; ii < inputInfos.length; ii++) {
                       var columnNames = inputInfos[ii].columnInfo.columnNameList;
                       var columnTypes = inputInfos[ii].columnInfo.columnTypeList;
                       if (dojo.indexOf(columnNames, columnName) != -1) {
                           columnType = columnTypes[dojo.indexOf(columnNames, columnName)];
                           break;
                       }
                   }
               }
           }

           if(CurrentOperatorDTO.classname == "ReplaceNullOperator"){
               if(null!=columnName){
                   var inputTables= CurrentOperatorDTO.inputTableInfos;
                   var status = false;
                   for(var jj=0;jj<inputTables.length;jj++){
                       var fieldColumns = inputTables[jj].fieldColumns;
                       for(var kk=0;kk<fieldColumns.length;kk++){
                           if(fieldColumns[kk][0]==columnName){
                               columnType = fieldColumns[kk][1];
                               status = true;
                               break;
                           }
                       }
                       if(status==true){
                           break;
                       }
                   }
               }
           }


        return columnType;
    };

    function _checkboxClickCallback(row){
        //modify by Will begin
        var tableGrid = row.parentNode.parentNode
        if(null!=tableGrid && null!=tableGrid.nodeName && tableGrid.nodeName.toUpperCase()=="TABLE"){
            var chks = dojo.query("input[type='checkbox']",tableGrid);
            var checkStatus = false;
            var checkedNum = 0;
            if(null!=chks && null!=chks.length && chks.length>0){
                for ( var i = 0; i < chks.length; i++) {
                    if(chks[i].checked==true && chks[i].id!=checkAllId){
                        checkedNum++;
                    }
                }
                if(checkedNum==(chks.length-1)){
                    dijit.byId(checkAllId).set("checked",true);
                }else{
                    dijit.byId(checkAllId).set("checked",false);
                }
            }
        }
    };

    function getNullReplacementModelObjByName(columnName){
        if(null!=replaceModel && null!=replaceModel.nullReplacementModel && null!=replaceModel.nullReplacementModel.nullReplacements){
            var replaceMents = replaceModel.nullReplacementModel.nullReplacements;
            for(var i=0;i<replaceMents.length;i++){
                  if(replaceMents[i].columnName == columnName){
                      return replaceMents[i];
                  }
            }
        }

        return null;

    }


    function _validateHadoopNullValueReplaceMent(columnName,columnValue){
        var columnType = "";
        if(null!=columnName){
            var inputInfos = CurrentOperatorDTO.inputFileInfos;
            for(var ii=0;ii<inputInfos.length;ii++){
                var columnNames = inputInfos[ii].columnInfo.columnNameList;
                var columnTypes = inputInfos[ii].columnInfo.columnTypeList;
                if(dojo.indexOf(columnNames,columnName)!=-1){
                    columnType = columnTypes[dojo.indexOf(columnNames,columnName)];
                    break;
                }
            }
            if(/^@\w+/.test(columnValue)==true){
                columnValue = variableReplacer.replaceVariable(columnValue);
            }else{
                if(alpine.props.HadoopDataTypeUtil.isNumberType(columnType)==true){
                    if(isNaN(columnValue)==true){
                        return false;
                    }
                }else{
                    if(columnValue.substring(0,1)!="'" || columnValue.substring(columnValue.length-1)!="'"){
                        return false;
                    }
                }
            }
            return true;
        }else{
            return false;
        }
    };
    function _validateDBNullValueReplaceMent(columnName,columnValue){
        var columnType = "";
        if(null!=columnName){
            var inputInfos = CurrentOperatorDTO.inputTableInfos;
            for(var ii=0;ii<inputInfos.length;ii++){
                var columnNames = [];
                var columnTypes = [];
                for(var j=0;j<inputInfos[ii].fieldColumns.length;j++){
                    columnNames.push(inputInfos[ii].fieldColumns[j][0]);
                    columnTypes.push(inputInfos[ii].fieldColumns[j][1]);
                }

                if(dojo.indexOf(columnNames,columnName)!=-1){
                    columnType = columnTypes[dojo.indexOf(columnNames,columnName)];
                    break;
                }
            }
            if(/^@\w+/.test(columnValue)==true){
                columnValue = variableReplacer.replaceVariable(columnValue);
            }
            if(alpine.props.HadoopDataTypeUtil.isNumberType4DB(columnType)==true){
                if(isNaN(columnValue)==true){
                    return false;
                }
            }else{
                if(columnValue.substring(0,1)!="'" || columnValue.substring(columnValue.length-1)!="'"){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    };

    function getNullValueReplaceModel(){
          return replaceModel
    };

    function destroyOnDlgHide(){
        widgetsGroup = [];
    };


    function _destroyNullValueReplaceModel(){
        replaceModel = null;
    };

    return {
        initNullvaluereplacement:initNullvaluereplacement,
        showNullvaluereplacementDlg:showNullvaluereplacementDlg,
        destroyNullValueReplaceModel:_destroyNullValueReplaceModel,
        getNullValueReplaceModel:getNullValueReplaceModel

    };


});