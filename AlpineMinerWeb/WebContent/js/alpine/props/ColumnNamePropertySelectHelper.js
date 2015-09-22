/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * File: ColumnNamePropertySelectHelper
 * Author: Will / robbie
 * Date: 12-12-12
 */
define([
    "dojo/_base/array",
    "dijit/registry",
    "alpine/layout/ColumnSelect/ColumnSelect"
],function(array, registry, ColumnSelect){

    var dependentColumnId = "dependentColumn"+ID_TAG;
    var columnNameProp = null;
    var dependColumnValue = null;
    var currentPropertyName = "";
    var sourceButtonId =null;

    function initColumnData(prop) {
        if(CurrentOperatorDTO.classname == "CustomizedOperator"){
            if(null==prop.value){
                prop.value="";
            }
            if(null!=prop.value){
                prop.selected = prop.value.split(",");
            }

        }
        if(null==columnNameProp){
            columnNameProp = {};
        }
        columnNameProp[prop.name.replace(/[\s]+/g,"")] = prop;
    }

    function initDependentColumnValue(){
        if(null!=CurrentOperatorDTO.propertyList){
            var propList = CurrentOperatorDTO.propertyList;
            dependColumnValue = null;
            for(var i=0;i<propList.length;i++){
                if("dependentColumn" == propList[i].name || "idColumn"== propList[i].name){
                    dependColumnValue = propList[i].value;
                    break;
                }
            }
        }
    }

    function showColumnSelectionDialog(prop){

        var dependColumn = registry.byId(dependentColumnId);
        if(null!=dependColumn && (dependColumn.get("value")==null || dependColumn.get("value")=="")){
            popupComponent.alert(alpine.nls.select_dep_column_first);
            return false;
        }

        currentPropertyName = prop.name.replace(/[\s]+/g,"");
        sourceButtonId = prop.name+ID_TAG;

        var dataItems = _prepareDataItem();
        var selectedItems = _prepareSelectedItems();
        var requiredCols = _getNumberOfRequiredColumns();

        new ColumnSelect({
            dataItems: dataItems,
            selectedItems: selectedItems,
            requiredCols: requiredCols,
            okButtonFn: function(selectedItems) {
                saveColumnNameConfig(selectedItems);
            }
        });
    }

    function _prepareDataItem(){
        var dataItem = [];
        if(null!=columnNameProp[currentPropertyName] && null!=columnNameProp[currentPropertyName].fullSelection){
            var allDatas = columnNameProp[currentPropertyName].fullSelection;

            for(var i=0;i<allDatas.length;i++){
                if(dependColumnValue == allDatas[i]){
                    continue;
                }
                dataItem.push({
                    colName:allDatas[i] ,
                    id: allDatas[i]
                });
            }
        }
        return dataItem;
    }

    function _prepareSelectedItems() {
        var selectedItems = [];
        if(null!=columnNameProp[currentPropertyName] && null!=columnNameProp[currentPropertyName].selected){
            selectedItems = columnNameProp[currentPropertyName].selected;
        }
        return selectedItems;
    }

    function _getNumberOfRequiredColumns() {
        var num = 0;
        var operaterName = CurrentOperatorDTO.classname;
        if(operaterName=="ScatterMatrixOperator" ||
            operaterName =="HadoopScatterPlotMatrixOperator") {
            num = 2;
        }
        return num;
    }

    function saveColumnNameConfig(selectedValues){
        //console.log(colSelect._returnSelected());
        if(selectedValues.length==0 &&
            CurrentOperatorDTO.classname != "LinearRegressionOperator"
            && CurrentOperatorDTO.classname != "LogisticRegressionOperator"
            && CurrentOperatorDTO.classname != "HadoopLinearRegressionOperator"
            && CurrentOperatorDTO.classname != "HadoopLogisticRegressionOperator" ){
            popupComponent.alert(alpine.nls.column_name_select_tip);
            registry.byId(sourceButtonId).set('baseClass', "workflowButtonInvalid");
            registry.byId(sourceButtonId).focus();
            return false;
        }
        columnNameProp[currentPropertyName].selected  = selectedValues;
        columnNameProp[currentPropertyName].value = selectedValues.join(",");

        _change4RegressionGroupBy(selectedValues);
        changeBtnStyle();
        _validateBtnStauts4Regression();
    }

    function changeBtnStyle(){
        if(null!=columnNameProp && columnNameProp[currentPropertyName].selected.length>0){
            columnNameProp[currentPropertyName].valid = true;
            setButtonBaseClassValid(sourceButtonId) ;
            registry.byId(sourceButtonId).focus();
        }
    }

    function _change4RegressionGroupBy(selectedValues){
        if((CurrentOperatorDTO.classname == "LinearRegressionOperator"
            || CurrentOperatorDTO.classname == "LogisticRegressionOperator")){
            var groupby = registry.byId("splitModelGroupByColumn"+ID_TAG);
            if(null!=groupby && groupby.get("disabled")==false){
                var groupbyValue = groupby.get("value");
                if(array.indexOf(selectedValues,groupbyValue)!=-1){
                    groupby.set("value","");
                }
            }
        }
    }

    function _validateBtnStauts4Regression(){
        if(CurrentOperatorDTO.classname == "LinearRegressionOperator"
            || CurrentOperatorDTO.classname == "LogisticRegressionOperator"
            || CurrentOperatorDTO.classname == "HadoopLinearRegressionOperator"
            || CurrentOperatorDTO.classname == "HadoopLogisticRegressionOperator"){

            //var interactionNum = 0;
            var selectColumnNum = 0;

            var interactionNum = _getInteractionColumnsNum();
            if(null!=columnNameProp[currentPropertyName].selected){
                selectColumnNum = columnNameProp[currentPropertyName].selected.length;
            }
            var isStepWise_false = registry.byId("isStepWise"+ID_TAG+"false");
            //var isStepWise_true = registry.byId("isStepWise"+ID_TAG+"true")
            if(null==isStepWise_false){
                if(interactionNum>0 || selectColumnNum>0){
                    _setInteractionBtnValid();
                    _setColumnBtnValid4Regression();
                }else{
                    _setInteractionBtnInvalid();
                    _setColumnBtnInvalid4Regression();
                }
            }else{
                if(null!=isStepWise_false && isStepWise_false.get("checked")==true){
                    if(interactionNum>0 || selectColumnNum>0){
                        _setInteractionBtnValid();
                        _setColumnBtnValid4Regression();
                    }else{
                        _setInteractionBtnInvalid();
                        _setColumnBtnInvalid4Regression();
                    }
                }else{
                    if(interactionNum==0){
                        _setInteractionBtnInvalid();
                    }else{
                        _setInteractionBtnValid();
                    }

                    if(selectColumnNum==0){
                        _setColumnBtnInvalid4Regression();
                    }else{
                        _setColumnBtnValid4Regression();
                        _setInteractionBtnValid();
                    }
                }
            }

        }
    }

    function _validateBtnStautsByIsStepWiseChange(){
        if(CurrentOperatorDTO.classname == "LinearRegressionOperator"
            || CurrentOperatorDTO.classname == "LogisticRegressionOperator"){

            var interactionNum = _getInteractionColumnsNum();
            var selectColumnNum = _getColumnsNum();

            var isStepWise_false = registry.byId("isStepWise"+ID_TAG+"false");
            //var isStepWise_true = registry.byId("isStepWise"+ID_TAG+"true")
            if(null!=isStepWise_false && isStepWise_false.get("checked")==true){
                if(interactionNum>0 || selectColumnNum>0){
                    _setInteractionBtnValid();
                    _setColumnBtnValid4Regression();
                }else{
                    _setInteractionBtnInvalid();
                    _setColumnBtnInvalid4Regression();
                }
            }else{
                if(interactionNum==0){
                    _setInteractionBtnInvalid();
                }else{
                    _setInteractionBtnValid();
                }

                if(selectColumnNum==0){
                    _setColumnBtnInvalid4Regression();
                }else{
                    _setColumnBtnValid4Regression();
                    _setInteractionBtnValid();
                }
            }
        }
    }

    function _getInteractionColumnsNum(){
        var num = 0;
        if(CurrentOperatorDTO!=null && CurrentOperatorDTO.propertyList!=null){
            for(var i=0;i<CurrentOperatorDTO.propertyList.length;i++){
                var pValue = CurrentOperatorDTO.propertyList[i];
                if(null!=pValue && pValue.name=="interActionModel"){
                    if(pValue.interActionModel!=null && pValue.interActionModel.interActionItems!=null){
                        num = pValue.interActionModel.interActionItems.length;
                    }
                }
            }

        }
        return num;

    }

    function _getColumnsNum(){
        var num = 0;
        if(CurrentOperatorDTO!=null && CurrentOperatorDTO.propertyList!=null){
            for(var i=0;i<CurrentOperatorDTO.propertyList.length;i++){
                var pValue = CurrentOperatorDTO.propertyList[i];
                if(null!=pValue && pValue.name=="columnNames"){
                    if(pValue.selected!=null){
                        num = pValue.selected.length;
                        break;
                    }
                }
            }

        }
        return num;

    }

    function _getColumnNameProps(){
        return columnNameProp;
    }

    function setDependColumnValue(value){
        dependColumnValue = value;
    }


    return {
        initColumnData:initColumnData,
        initDependentColumnValue:initDependentColumnValue,
        showColumnSelectionDialog:showColumnSelectionDialog,
        getColumnNameProps:_getColumnNameProps,
        setDependColumnValue:setDependColumnValue,
        validateBtnStautsByIsStepWiseChange:_validateBtnStautsByIsStepWiseChange
    }

});