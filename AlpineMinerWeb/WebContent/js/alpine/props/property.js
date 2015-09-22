/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * 
 * property.js
 * 
 * Author sam_zang
 * 
 * Version 3.0
 * 
 * Date Dec 3, 2011
 */
var propSideBar;
//use this to avoid the memory leaking
var property_event_handlers = new Array();
var property_widgets = new Array();
var sp_property_event_handlers = new Array();
var sp_property_widgets = new Array();



var ID_TAG = "__prop_form_value__";

var CurrentOperatorDTO = null;
var current_op = null ;
var CurrentColumnList = null;
//var CurrentBinProperty = null;
var CurrentReplacementProperty = null;
var CurrentWhereClause = null;
var CurrentDependentColumn = null;
var DependentColumnRequired = false;

//property for dialog
var current_tableJoin = null;
var current_hiddenLayer = null;
var current_interaction_columns = null;

var CANVAS_ID = "FlowDisplayPanelPersonal";
var DIALOG_ID = "propertyFormDialogCont";

dojo.ready(function(){
	propSideBar = new SideBar("propertyFormDialog", {w: 430}, "propertyFormDialogCont");
});

function open_property_dialog(op) {
	clear_property_dialog();
	if (op.classname &&op.classname == "ModelOperator"){
		return;
	}
	alpine.flow.WorkFlowVariableReplacer.init();//initialize variables of flow.
	//progressBar.showLoadingBar();
	current_op=op;
	CurrentColumnList = null;
//	CurrentBinProperty = null;
	CurrentReplacementProperty = null;
	CurrentDependentColumn = null;
	CurrentWhereClause = null;
	DependentColumnRequired = false;
	

	var dlgid = op.name;
//	var propertyFormDialog = dijit.byId("propertyFormDialog");
//
//	propertyFormDialog.title = op.name;

	var url = baseURL + "/main/property.do?method=getPropertyData" + "&uuid="
			+ op.uid + "&user=" + login;
	ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(), get_property_data_callback, null, false, CANVAS_ID);

	var img = dojo.byId("property_operator_icon");
	img.src = alpine.operatorexplorer.OperatorUtil.getStandardImageSourceByKey(op.classname);
	// var title = op.name + " (Class: " + op.classname + ")";
	var title = op.name;
	dojo.html.set(dojo.byId("property_operator_name"), title);
}

function get_property_data_callback(opDTO) {
	var start = new Date();
	//progressBar.closeLoadingBar();
	if (opDTO.error_code) {
		if (opDTO.error_code == -1) {
			popupComponent.alert(alpine.nls.no_login, function(){
				window.top.location.pathname = loginURL;	
			});
			return;
		}
		else {
			var msg = alpine.nls.flow_not_found;
			if (opDTO.message) {
				msg = opDTO.message;
			}
			popupComponent.alert(msg);
//			clear_flow_display("Personal");
//			dijit.byId("cancel_flow_button").set("disabled", true);
			return;
		}
	}
	CurrentOperatorDTO = opDTO;
	alpine.flow.WorkFlowManager.storeEditingFlow(opDTO.flowInfo);
	// sort_list_by_name(CurrentOperatorDTO.propertyList);

	initialize_property_table("propertyTable", opDTO.propertyList, opDTO.invalidPropertyList);

    if (opDTO.isValid == false) {
        //add by Will MINERWEB-975
        var reasonMsg = "";
        var reasonMsgStatus = false;
        for ( var j = 0; j < opDTO.propertyList.length; j++) {
            if(opDTO.propertyList[j]!=null && opDTO.propertyList[j].name!=null && opDTO.propertyList[j].name=="dbConnectionName"){
                if(opDTO.propertyList[j].fullSelection!=null && opDTO.propertyList[j].fullSelection.length==0){
                    reasonMsgStatus = true;
                    reasonMsg=alpine.nls.Possible_reasons_dbconnect;
                    break;
                }
            }

        }
        current_op.isValid = false;
        alpine.flow.OperatorManagementUIHelper.validateOperators([{
            uid: opDTO.uuid,
            isValid: false
        }]);
    }   else
    {
        current_op.isValid = true;
        alpine.flow.OperatorManagementUIHelper.validateOperators([{
            uid: opDTO.uuid,
            isValid: true
        }]);
    }
    if (reasonMsgStatus)
    {
        popupComponent.alert(reasonMsg, function(){
            openPropDlgHandle();
        });
    }else{
        openPropDlgHandle();
    }
	
	function openPropDlgHandle(){
		dijit.byId("propertyForm").startup();
		dijit.byId("propertyForm").validate();

//		var dlg = dijit.byId("propertyFormDialog");
		applyCustomeRules(opDTO);
//		dlg.startup();
//		dlg.show();
		

		//disconnect the delete operator and connection event.
		alpine.flow.WorkFlowUIHelper.bindDeleteKeyBoardEvent(false);
        propSideBar.show();
    }
}

function initialize_property_table(table_name, propertyList, invalidPropertyNames) {
	var tbl = dojo.byId(table_name);
	var tbody = dojo.create("tbody", {}, tbl);
	for ( var i = 0; i < propertyList.length; i++) {
		var prop = propertyList[i];
        var propName = prop.name;
        if (invalidPropertyNames &&  dojo.indexOf(invalidPropertyNames,propName) > -1)
        {
            prop.valid = false;
        } else
        {
            prop.valid = true;
        }

		// Whether to display dicIndexColumn for plda Add By Will
		if(false==needShowDisplayProperty(prop)){
		   continue;
		}
		if (prop) {
			// filter out custom popup properties.
			if (prop.type!=null && (prop.type =="PT_UNKNOWN")) {
				break;
			}else{
				addProperty(tbody, prop);
			}
		}
	}			
}


function applyCustomeRules(op) {
	var handler =null;
	switch (op.classname) {

	case "RandomSamplingOperator":
		// if replacement is true then disable
		// consistent, disjoint and keyColumn
		var id = "replacement" + ID_TAG;
		var rep_true = dijit.byId(id + "true");
		check_RandomSamplingOperator();
		
		handler = dojo.connect(rep_true, "onChange", null, function() {
			check_RandomSamplingOperator();
		});			
		property_event_handlers.push(handler);
		
		id = "consistent" + ID_TAG;
		var con_true = dijit.byId(id + "true");
		
		property_event_handlers.push(dojo.connect(dijit.byId("randomSeed" + ID_TAG), "onBlur", dijit.byId("randomSeed" + ID_TAG), function(){
			if(this.get("value") != ""){
				con_true.set("checked", true);
			}
		}));
		
		handler = dojo.connect(con_true, "onChange", null, function() {
			check_RandomSamplingOperator();
		});	
		property_event_handlers.push(handler);
		break;
		
	case "StratifiedSamplingOperator":
		// if consistent is true enable keyColumn								
		var id = "consistent" + ID_TAG;
		var con_true = dijit.byId(id + "true");
		var con_false = dijit.byId(id + "false");
		var keyc = dijit.byId("keyColumnList" + ID_TAG);
		if (con_true.get("disabled") == false) {
			if (con_true.checked == true) {
				keyc.set("disabled", false);
			}
			else {
				keyc.set("disabled", true);
			}
		}
		
		property_event_handlers.push(dojo.connect(dijit.byId("randomSeed" + ID_TAG), "onBlur", dijit.byId("randomSeed" + ID_TAG), function(){
			if(this.get("value") != ""){
				con_true.set("checked", true);
			}
		}));
		
		handler = dojo.connect(con_true, "onChange", null, function() {
			if (con_true.get("disabled") == true) {
				return;
			}
			if (con_true.checked == true) {
				keyc.set("disabled", false);
			}
			else {
				keyc.set("disabled", true);
			}
		});
		property_event_handlers.push(handler);
		break;

	case "NormalizationOperator":
		var keyValue = "Range-Transformation";
		var id = "method" + ID_TAG;
	
		var btn = dijit.byId(id);
		var minValue = dijit.byId("rangeMin" + ID_TAG);
		var maxValue = dijit.byId("rangeMax" + ID_TAG);
		if (dojo.byId(id).value != keyValue) {
			minValue.set("disabled", true);
			maxValue.set("disabled", true);
		}
		handler = dojo.connect(btn, "onChange", null, function(value) {
			if (value == keyValue) {
				minValue.set("disabled", false);
				maxValue.set("disabled", false);
			}
			else {
				minValue.set("disabled", true);
				maxValue.set("disabled", true);
				minValue.set("value", "");
				maxValue.set("value", "");
			}
		});
		property_event_handlers.push(handler);
		break;

	case "IntegerToTextOperator":
		
		var id = "modifyOriginTable" + ID_TAG;
		var mod_true = dijit.byId(id + "true");
		check_IntegerToTextOperator();
		handler = dojo.connect(mod_true, "onChange", null, function() {
			check_IntegerToTextOperator();
		});
		property_event_handlers.push(handler);
		break;

	case "SVMClassificationOperator":
	case "SVMRegressionOperator":
	case "SVMNoveltyDetectionOperator":
		
		var id = "kernelType" + ID_TAG;
		var kernelType = dijit.byId(id);

		check_kernelType();
		handler = dojo.connect(kernelType, "onChange", null, function() {
			check_kernelType();
		});
		property_event_handlers.push(handler);
		break;

	case "SVDLanczosCalculatorOperator":
		dijit.byId("UmatrixTableF" + ID_TAG).set("disabled", true);
		dijit.byId("rowNameF" + ID_TAG).set("disabled", true);
		dijit.byId("UfeatureColumn" + ID_TAG).set("disabled", true);
        dijit.byId("UdependentColumnF" + ID_TAG).set("disabled", true);
		dijit.byId("colNameF" + ID_TAG).set("disabled", true)
		dijit.byId("VfeatureColumn" + ID_TAG).set("disabled", true);
		dijit.byId("VdependentColumnF" + ID_TAG).set("disabled", true);
		dijit.byId("singularValuefeatureColumn" + ID_TAG).set("disabled", true);
		dijit.byId("singularValuedependentColumnF" + ID_TAG).set("disabled", true);
		dijit.byId("VmatrixTableF" + ID_TAG).set("disabled", true);
		dijit.byId("singularValueTableF" + ID_TAG).set("disabled", true);

		var id = "crossProduct" + ID_TAG;
		var cp_true = dijit.byId(id + "true");
		check_SVDLanczosCalculatorOperator();
		handler = dojo.connect(cp_true, "onChange", null, function() {
			check_SVDLanczosCalculatorOperator();
		});
		property_event_handlers.push(handler);
		break;

	case "SVDLanczosOperator":
		var depCol = dijit.byId("dependentColumn" + ID_TAG);
		var colName = dijit.byId("colName" + ID_TAG);
		var rowName = dijit.byId("rowName" + ID_TAG);
		
		handler = dojo.connect(depCol, "onChange", null, function(value) {
			check_SVDLanczosOperator("dependentColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(colName, "onChange", null, function(value) {
			check_SVDLanczosOperator("colName");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(rowName, "onChange", null, function(value) {
			check_SVDLanczosOperator("rowName");
		});
		property_event_handlers.push(handler);
		break;

	case "ProductRecommendationOperator":
		var c1 = dijit.byId("customerIDColumn" + ID_TAG);
		var c2 = dijit.byId("customerValueColumn" + ID_TAG);
		var c3 = dijit.byId("customerProductColumn" + ID_TAG);
		dijit.byId("simThreshold" + ID_TAG).set("requeired", "true");
		handler = dojo.connect(c1, "onChange", null, function() {
			check_ProductRecommendationOperator("customerIDColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(c2, "onChange", null, function() {
			check_ProductRecommendationOperator("customerValueColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(c3, "onChange", null, function() {
			check_ProductRecommendationOperator("customerProductColumn");
		});
		property_event_handlers.push(handler);
		
		break;
				
	case "ProductRecommendationEvaluationOperator":
		var r1 = dijit.byId("recommendationIdColumn" + ID_TAG);
		var r2 = dijit.byId("recommendationProductColumn" + ID_TAG);
		var pre1 = dijit.byId("preIdColumn" + ID_TAG);
		var pre2 = dijit.byId("preValueColumn" + ID_TAG);
		var post1 = dijit.byId("postIdColumn" + ID_TAG);
		var post2 = dijit.byId("postProductColumnn" + ID_TAG);
		var post3 = dijit.byId("postValueColumn" + ID_TAG);
		handler = dojo.connect(r1, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("recommendationIdColumn");
		});
		property_event_handlers.push(handler);
		
		handler = 	dojo.connect(r2, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("recommendationProductColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(pre1, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("preIdColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(pre2, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("preValueColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(post1, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("postIdColumn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(post2, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("postProductColumnn");
		});
		property_event_handlers.push(handler);
		
		handler = dojo.connect(post3, "onChange", null, function() {
			check_ProductRecommendationEvaluationOperator("postValueColumn");
		});
		property_event_handlers.push(handler);
		
		break;
		
	case "LinearRegressionOperator":
	case "LogisticRegressionOperator":
		var st = dijit.byId("isStepWise" + ID_TAG + "true");
		if (st == null) {
			return;
		}
		check_stepwise(op);
		handler =dojo.connect(st, "onChange", null, function(value) {
			check_stepwise(op);
		});
		property_event_handlers.push(handler);
		break;
		
	case "PivotOperator":
		// If it has child operator disable useArray.
		if (alpine.flow.OperatorManagementManager.hasChildrenOperator(op.uuid) == true) {
			var id = "useArray" + ID_TAG;
			var useArray_true = dijit.byId(id + "true");
			var useArray_false = dijit.byId(id + "false");

			useArray_true.set("checked", true);	
			useArray_true.set("disabled", true);	
			useArray_false.set("disabled", true);
		}
		break;
	case "KMeansOperator":
    case "HadoopKmeansOperator":
        dijit.byId("idColumn" + ID_TAG).set("required", false);
		break;
		
	case "ScatterPlotOperator": 
		dijit.byId("categoryColumn" + ID_TAG).set("required",false);
		notEqualsPropertyText(dijit.byId("columnX" + ID_TAG), [dijit.byId("columnY" + ID_TAG), dijit.byId("categoryColumn" + ID_TAG)]);
		notEqualsPropertyText(dijit.byId("columnY" + ID_TAG), [dijit.byId("columnX" + ID_TAG)], dijit.byId("categoryColumn" + ID_TAG));
		notEqualsPropertyText(dijit.byId("categoryColumn" + ID_TAG), [dijit.byId("columnX" + ID_TAG)], dijit.byId("columnY" + ID_TAG));
		break;

        case "HadoopBoxAndWiskerOperator":
        case "BoxAndWhiskerOperator":
		dijit.byId("typeDomain" + ID_TAG).set("required",false);
		dijit.byId("seriesDomain" + ID_TAG).set("required",false);
		//notEqualsPropertyText(dijit.byId("analysisValueDomain" + ID_TAG), [dijit.byId("typeDomain" + ID_TAG), dijit.byId("seriesDomain" + ID_TAG)]);
		//notEqualsPropertyText(dijit.byId("typeDomain" + ID_TAG), [dijit.byId("analysisValueDomain" + ID_TAG), dijit.byId("seriesDomain" + ID_TAG)]);
		//notEqualsPropertyText(dijit.byId("seriesDomain" + ID_TAG), [dijit.byId("analysisValueDomain" + ID_TAG), dijit.byId("typeDomain" + ID_TAG)]);
		break;
    case "HadoopTimeSeriesOperator":
	case "TimeSeriesOperator": 
		dijit.byId("groupColumn" + ID_TAG).set("required",false);
		break;
	case "HadoopRowFilterOperator":
    case "HadoopAggregateOperator":
    case "HadoopVariableOperator":
    case "HadoopJoinOperator":
    //case "HadoopUnionOperator": //dont need this any more 
    case "HadoopReplaceNullOperator":
    case "HadoopNormalizationOperator":
    case "HadoopPivotOperator":
    case "HadoopColumnFilterOperator":
		var defaultHide = !dijit.byId("storeResults" + ID_TAG + "true").get("value");
		dijit.byId("btn_resultsLocation" + ID_TAG).set("disabled", defaultHide);
		dijit.byId("resultsLocation" + ID_TAG).set("disabled", defaultHide);
		dijit.byId("resultsName" + ID_TAG).set("disabled", defaultHide);

        dijit.byId("override"+ID_TAG+"Yes").set("disabled", defaultHide);
        dijit.byId("override"+ID_TAG+"No").set("disabled", defaultHide);

        //enable/disable Explore btn based on storeResult when close propSideBar (PIVOTAL-36990811)
        var handle = dojo.connect(dijit.byId("rootStandBy"), "onHide", function() {
            if (dijit.byId("storeResults" + ID_TAG + "true")){
                if (dijit.byId("operator_explore_button")){
                    var findPrimaryInfo = alpine.flow.OperatorManagementUIHelper.getSelectedOperator(true);
                    if (findPrimaryInfo) {
                        dijit.byId("operator_explore_button").set("disabled", !findPrimaryInfo.storeResult);
                    }
                }
            }
            dojo.disconnect(handle);
        });

		dojo.connect(dijit.byId("storeResults" + ID_TAG + "true"), "onChange", function(val){
			dijit.byId("btn_resultsLocation" + ID_TAG).set("disabled", !val);
			dijit.byId("resultsLocation" + ID_TAG).set("disabled", !val);
			dijit.byId("resultsName" + ID_TAG).set("disabled", !val);

            dijit.byId("override"+ID_TAG+"Yes").set("disabled", !val);
            dijit.byId("override"+ID_TAG+"No").set("disabled", !val);
		});

        if("HadoopNormalizationOperator"==op.classname){
            var keyValue = "Range-Transformation";
            var id = "method" + ID_TAG;
            var btn = dijit.byId(id);
            var minValue = dijit.byId("rangeMin" + ID_TAG);
            var maxValue = dijit.byId("rangeMax" + ID_TAG);
            if (dojo.byId(id).value != keyValue) {
                minValue.set("disabled", true);
                maxValue.set("disabled", true);
            }
            handler = dojo.connect(btn, "onChange", null, function(value) {
                if (value == keyValue) {
                    minValue.set("disabled", false);
                    maxValue.set("disabled", false);
                }
                else {
                    minValue.set("disabled", true);
                    maxValue.set("disabled", true);
                    minValue.set("value", "");
                    maxValue.set("value", "");
                }
            });
            property_event_handlers.push(handler);
        }

		break;
    case "RandomForestOperator":
    	dijit.byId("nodeColumnNumber" + ID_TAG).isValid = function(){
			var value = this.get("value");
    		var columnLength = null;
    		var validate = true;
    		this.invalidMessage = this.messages.invalidMessage;// replace to default error message.
    		if(!this.validator(value)){
    			return false;
    		}
    		value = parseInt(value);
    		if(isNaN(value) || value < 1){
    			return false;
    		}
    		for(var i = 0;i < CurrentOperatorDTO.propertyList.length;i++){
    			if(CurrentOperatorDTO.propertyList[i].name == "columnNames"){
    				columnLength = CurrentOperatorDTO.propertyList[i].selected.length;
    			}
    		}
    		if(columnLength == 0){
    			validate = false;
    			this.invalidMessage = alpine.nls.random_forest_operator_tip_no_column_selected;
    		}else if(value > columnLength){
    			validate = false;
    			this.invalidMessage = alpine.nls.random_forest_operator_tip_validate_nodeColumnNumber_fail;
    		}
    		return validate;
    	};
    	break;
    }
	
	storagePropertyRule(op);
    regressionGroupByRule(op);
}

function notEqualsPropertyText(textBox, relativeTextBox){

	textBox._hasBeenBlurred = true;
	textBox.isValid = function(isFocused){
		var val = textBox.get("value");
		var validate = true;
		for(var i = 0;i < relativeTextBox.length;i++){
			if(isFocused){
				relativeTextBox[i].validate();
			}
			validate &= (val != relativeTextBox[i].get("value"));
		}
		return validate;
	};
}

function storagePropertyRule(op){
	var outputTypeViewInput = dijit.byId("outputType" + ID_TAG + "VIEW");
	if(outputTypeViewInput != null){
		if(op.inputTableInfos == null || (
            //op.inputTableInfos[0].system != "PostgreSQL" &&
            op.inputTableInfos[0].system !=  "Greenplum")){
			dijit.byId("StorageParameters" + ID_TAG).set("disabled", true);
		}else{
			dijit.byId("StorageParameters" + ID_TAG).set("disabled", outputTypeViewInput.get("checked"));//set init status
			property_event_handlers.push(dojo.connect(outputTypeViewInput, "onChange", function(val){
				dijit.byId("StorageParameters" + ID_TAG).set("disabled", val);
			}));
		}
	}
}

function regressionGroupByRule(op){
    if(dijit.byId("splitModelGroupByColumn"+ID_TAG)!=null){
        if(op.inputTableInfos == null || (op.inputTableInfos[0].system != "PostgreSQL" && op.inputTableInfos[0].system != "Greenplum" )){
            dijit.byId("splitModelGroupByColumn" + ID_TAG).set("value", "");
            dijit.byId("splitModelGroupByColumn" + ID_TAG).set("disabled", true);
        }
       if(op.inputTableInfos != null){
            var st = dijit.byId("isStepWise" + ID_TAG + "true");
            if(null!=st && st.get("checked") == true && ( op.inputTableInfos[0].system != "PostgreSQL" || op.inputTableInfos[0].system !=  "Greenplum" )){
                dijit.byId("splitModelGroupByColumn" + ID_TAG).set("disabled", true);
            }else if(null!=st && st.get("checked") == false && ( op.inputTableInfos[0].system == "PostgreSQL" || op.inputTableInfos[0].system ==  "Greenplum" )){
                dijit.byId("splitModelGroupByColumn" + ID_TAG).set("disabled", false);
            }
        }
    }

}

function check_stepwise(op) {
	var st = dijit.byId("isStepWise" + ID_TAG + "true");
	var v1 = dijit.byId("stepWiseType" + ID_TAG);
	var v2 = dijit.byId("criterionType" + ID_TAG);
	var v3 = dijit.byId("checkValue" + ID_TAG);
    var v4 = dijit.byId("splitModelGroupByColumn"+ID_TAG);
	if (st.get("checked") == true) {
		v1.set("disabled", false);
		v2.set("disabled", false);
		v3.set("disabled", false);
        if(null!=v4){
                v4.set("disabled",true);
                v4.set("value","");
        }
		if(!v1.get("value")||""==v1.get("value")){
			v1.set("value","FORWARD");//default value
		}
		if(!v3.get("value")||NaN==v3.get("value")){
			v3.set("value",0.05);//default value
		}
	}
	else {
		v1.set("disabled", true);
		v2.set("disabled", true);
		v3.set("disabled", true);
        if(null!=v4){
            if(op.inputTableInfos == null || (op.inputTableInfos[0].system != "PostgreSQL" && op.inputTableInfos[0].system !=  "Greenplum")){
                v4.set("disabled",true);
                v4.set("value","");
            }else{
                v4.set("disabled",false);
            }
        }
	}
}

function check_SVDLanczosOperator(value) {
	var c1 = dojo.byId("dependentColumn" + ID_TAG).value;
	var c2 = dojo.byId("colName" + ID_TAG).value;
	var c3 = dojo.byId("rowName" + ID_TAG).value;
	if ((c1 != null && c1 != "" && c1 == c2) || 
			(c2 != null && c2 != ""  && c2 == c3) ||	
			(c3 != null && c3 != ""  && c3 == c1))  {
		popupComponent.alert(alpine.nls.ConnectionValid);
		dijit.byId(value + ID_TAG).set("value", "");
	}
}

function check_ProductRecommendationOperator(value) {
	var c1 = dojo.byId("customerIDColumn" + ID_TAG).value;
	var c2 = dojo.byId("customerValueColumn" + ID_TAG).value;
	var c3 = dojo.byId("customerProductColumn" + ID_TAG).value;
	if ((c1 != null && c1 != "" && c1 == c2) || 
			(c2 != null && c2 != ""  && c2 == c3) ||	
			(c3 != null && c3 != ""  && c3 == c1)) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		dojo.byId(value + ID_TAG).value = "";
	}
}

function check_ProductRecommendationEvaluationOperator(value) {
	var r1 = dojo.byId("recommendationIdColumn" + ID_TAG).value;
	var r2 = dojo.byId("recommendationProductColumn" + ID_TAG).value;
	var pre1 = dojo.byId("preIdColumn" + ID_TAG).value;
	var pre2 = dojo.byId("preValueColumn" + ID_TAG).value;
	var post1 = dojo.byId("postIdColumn" + ID_TAG).value;
	var post2 = dojo.byId("postProductColumnn" + ID_TAG);
	var post3 = dojo.byId("postValueColumn" + ID_TAG).value;
	
	switch (value) {
	case "recommendationIdColumn":
	case "recommendationProductColumn":
		if (r1 != null && r1 == r2) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			dojo.byId(value + ID_TAG).value = "";
		}
		break;
	case "preIdColumn":
	case "preValueColumn":
		if (pre1 != null && pre1 == pre2) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			dojo.byId(value + ID_TAG).value = "";
		}
		break;
	case "postIdColumn":
	case "postProductColumnn":
	case "postValueColumn":
		if ((post1 != null && post1 == post2)
				|| (post2 != null && post2 == post3)
				|| (post3 != null && post3 == post1)) {
			popupComponent.alert(alpine.nls.ConnectionValid);
			dojo.byId(value + ID_TAG).value = "";
		}
		break;
	}
}
		
function check_SVDLanczosCalculatorOperator() {
	var id = "crossProduct" + ID_TAG;
	var cp_true = dijit.byId(id + "true");
	
	var target1 = dijit.byId("keyColumn" + ID_TAG);
	var target2 = dijit.byId("keyValue" + ID_TAG);	

	if (cp_true.checked == true) {
		target1.set("disabled", true);
		target2.set("disabled", true);
	}
	else {
		target1.set("disabled", false);
		target2.set("disabled", false);
	}							
}

function check_RandomSamplingOperator() {	
	var id = "replacement" + ID_TAG;
	var rep_true = dijit.byId(id + "true");
	var rep_false = dijit.byId(id + "false");

	id = "consistent" + ID_TAG;
	var con_true = dijit.byId(id + "true");
	var con_false = dijit.byId(id + "false");

	id = "disjoint" + ID_TAG;
	var dis_true = dijit.byId(id + "true");
	var dis_false = dijit.byId(id + "false");
	var keyc = dijit.byId("keyColumnList" + ID_TAG);
		
	if (rep_true.checked == true) {
		con_true.set("disabled", true);
		con_false.set("disabled", true);
		dis_true.set("disabled", true);
		dis_false.set("disabled", true);
		keyc.set("disabled", true);
	}
	else {
		con_true.set("disabled", false);
		con_false.set("disabled", false);
		dis_true.set("disabled", false);
		dis_false.set("disabled", false);
		keyc.set("disabled", false);
	}
	
	if (con_true.get("disabled") == false) {
		if (con_true.checked == true) {
			keyc.set("disabled", false);
            //randomSeed
           dijit.byId("randomSeed"+ID_TAG).set("required",true);

		}
		else {
			keyc.set("disabled", true);
            dijit.byId("randomSeed"+ID_TAG).set("required",false)
		}
	}
}

function check_IntegerToTextOperator() {
	var id = "modifyOriginTable" + ID_TAG;
	var mod_true = dijit.byId(id + "true");
	
	var target1 = dijit.byId("outputSchema" + ID_TAG);
	var target2 = dijit.byId("outputTable" + ID_TAG);
	var target3 = dijit.byId("outputType" + ID_TAG + "TABLE");
	var target4 = dijit.byId("outputType" + ID_TAG + "VIEW");
	var target5 = dijit.byId("dropIfExist" + ID_TAG + "Yes");
	var target6 = dijit.byId("dropIfExist" + ID_TAG + "No");
	var target7 = dijit.byId("StorageParameters" + ID_TAG);
	
	if (mod_true.checked == true) {
		target1.set("disabled", true);
		target2.set("disabled", true);
		target3.set("disabled", true);
		target4.set("disabled", true);
		target5.set("disabled", true);
		target6.set("disabled", true);
		target7.set("disabled", true);
	}
	else {
		target1.set("disabled", false);
		target2.set("disabled", false);
		target3.set("disabled", false);
		target4.set("disabled", false);
		target5.set("disabled", false);
		target6.set("disabled", false);
		target7.set("disabled", false);
	}						
}

function check_kernelType() {
	var id = "kernelType" + ID_TAG;
	var kernelTypeValue = dojo.byId(id).value;
	
	var degree = dijit.byId("degree" + ID_TAG);
	var gamma = dijit.byId("gamma" + ID_TAG);

	switch (kernelTypeValue) {
	case "dot product":
		degree.domNode.value = "";
		gamma.domNode.value = "";
		degree.set("disabled", true);
		gamma.set("disabled", true);
		break;
		
	case "polynomial":
		gamma.domNode.value = "";
		degree.set("disabled", false);
		gamma.set("disabled", true);
		break;
		
	case "gaussian":
		degree.domNode.value = "";
		degree.set("disabled", true);
		gamma.set("disabled", false);
		break;
	}
}

function addProperty(tbl, prop) {
	var row = dojo.create("tr", null, tbl);

	var label = dojo.create("td", {
		style : "width: 160px;max-width:160px;padding-left: 5px;"
	}, row);
    var displayName =  prop.displayName;

    displayName = _addCustomLabel(prop);

	dojo.create("label", {
		innerHTML : "<b>" + displayName + "</b>"
	}, label);
	label.align = "right";


	var val = dojo.create("td",  {style : "padding-right: 5px;"}, row);
	val = dojo.create("div", null, val);
	var id = prop.name + ID_TAG;
	
	switch (prop.type) {
	case "PT_CHOICE":
	case "PT_BOOLEAN":
		generate_input_choice(val, prop);
		break;
    case "PT_CUSTOM_NAME_HD_JOIN":
        alpine.props.JoinHadoopPropertyHelper.initHadoopJoinModelAndInputFilesVariable(prop);
        add_popup(val, prop,
            alpine.nls.hadoop_define_join_condition_btn,
            alpine.props.JoinHadoopPropertyHelper.showHadoopJoinDialog);
        break;
    case "PT_CUSTOM_NAME_HD_CSVFILESTRUCTURE":
            alpine.props.HadoopFileOperatorPropertyHelper.initFileSturctureModelAndContent(prop);
            add_popup(val, prop, prop.displayName, function(){
                var hadoopFileName = dijit.byId('hadoopFileName'+ID_TAG);
                var hadoopConnect  = dijit.byId('connName'+ID_TAG);
                if(null==hadoopConnect || hadoopConnect.get('value')==null || hadoopConnect.get('value')==""){
                    popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_conn_tip);
                    return false;
                }
                if(null!=hadoopFileName && hadoopFileName.get('value')!=null && hadoopFileName.get('value')!=""){
                    switch (alpine.props.HadoopFileOperatorPropertyHelper.getFileFormatType()){
                        case "text":
                            alpine.props.HadoopFileOperatorPropertyHelper.initColumnConfigData(hadoopFileName.get('value'),prop);
                            break;
                        case "apache":
                           alpine.props.HadoopFileStructure4LogHelper.showFileStructure4LogDialog(hadoopFileName.get('value'),prop);
                            //alpine.props.HadoopFileOperatorPropertyHelper.initColumnConfigData(hadoopFileName.get('value'),prop);
                            break;

                        case "json":
                           // alpine.props.HadoopFileOperatorPropertyHelper.initColumnConfigData(hadoopFileName.get('value'),prop);
                            alpine.props.HadoopFileStructure4JSONHelper.showFileStructure4JsonDialog(hadoopFileName.get('value'),prop);
                            // popupComponent.alert("json");
                            break;
                        case "xml":
                            //alpine.props.HadoopFileOperatorPropertyHelper.initColumnConfigData(hadoopFileName.get('value'),prop);
                            alpine.props.HadoopFileStructure4XMLHelper.showStructureCfgXMLDialog(hadoopFileName.get('value'),prop);
                            // popupComponent.alert("xml");
                            break;
                    }
                }else{
                    popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_file_tip);
                }

            });
        break;
    case "PT_CUSTOM_PIG_EXEC_FILESTRUCTURE":
        add_popup(val, prop,alpine.nls.pig_define_struct_button,function(pro){
        	var resultLocation = dijit.byId("resultsLocation"+ID_TAG).get("value");
        	var fileName = dijit.byId("resultsName" + ID_TAG).get("value");
        	var fileFullName = resultLocation + "/" + fileName;
        	fileFullName = alpine.flow.WorkFlowVariableReplacer.replaceVariable(fileFullName);
        	alpine.datasourceexplorer.DataSourceExplorerManager.checkHDFileisExists(current_op.connectionName, fileFullName, function(data){
        		if(data.result == true){
        			alpine.props.HadoopFileOperatorPropertyHelper.initFileSturctureModelAndContent(pro, current_op.connectionName, fileFullName);
        			alpine.props.HadoopFileOperatorPropertyHelper.initColumnConfigData(fileFullName,pro, function(hadoopFileStructureModel){
        				pro.csvFileStructureModel = hadoopFileStructureModel;
        			});
        			
        		}else{
        			alpine.props.HadoopPigExecuteFileStructureHelper.showExecuteFileStructureDlg(pro);
        		}
        	}, null, "propertyFormDialog");
	       	 
        });
        break;
    case "PT_CUSTOM_PIG_EXEC_SCRIPT":
    	add_popup(val, prop,alpine.nls.pig_edit_scirpt_button,function(prop){
    		alpine.props.PigExecuteScriptPropertyHelper.showPigScriptDialog(prop, function(scriptModel){
    			prop.hadoopPigExecuteScriptModel = scriptModel;
    		});
        });
    	break;
    case "PT_CUSTOM_NAME_HD_FILENAME":
        alpine.props.HadoopCommonPropertyHelper.buildHadoopFileExplorer({
    		id: id,
    		container: val,
    		value: prop.value,
    		explorerButtonLabel: alpine.nls.hadoop_prop_choose_file_btn,
    		includeFile: true,
    		checkPermission: false,
    		readonly: false,
    		onCompleteInitialize: function(widgets, events){
    			property_widgets = property_widgets.concat(widgets);
    			property_event_handlers = property_event_handlers.concat(events);
    		},
    		onClickOkayButton: function(selectedItem){
    			var canCloseDialog = true;
    			if(CurrentOperatorDTO.classname == "HadoopFileOperator"){
            		alpine.props.HadoopFileOperatorPropertyHelper.dbClick4SelectHadoopFile();
            		if(/^_|[&|?|=|&|\"|\'|:|\[|\]|\{|\}|,]+/.test(selectedItem.name)){
            			popupComponent.alert(alpine.nls.operator_add_error_invalid_name);
            			canCloseDialog = false;
            		}
            	}
    			return canCloseDialog;
    		}
    	});
        property_event_handlers.push(dojo.connect(dijit.byId(id), "onChange", function(newVal){
        	this.set("title", newVal);

            var pList = CurrentOperatorDTO.propertyList;
            var pStructure = null;
            for(var i=0;i<pList.length;i++){
                if(pList[i].name=="hadoopFileStructure"){
                    pStructure = pList[i];
                    break;
                }
            }
            var fileType = "csv";
            if(null!=pStructure){
                for(var p in pStructure){
                     if(p == "CSVFileStructureModel" ){
                         fileType = "csv";
                     }else if(p == "xmlFileStructureModel"){
                         fileType = "xml";
                     }else if(p == "AlpineLogFileStructureModel"){
                         fileType = "alpineLog";
                     }else if(p == "json"){
                         fileType = "json";
                     }
                }
            }
           switch (fileType){
               case "csv":
                   var model = alpine.props.HadoopFileOperatorPropertyHelper.getCurrentFileStruceModel();
                   if(null!=model && dijit.byId("hadoopFileFormat"+ID_TAG).get("value")=="Text File"){
//                       alpine.props.HadoopFileOperatorPropertyHelper.resetFileStructModel(model);
                       alpine.props.HadoopFileOperatorPropertyHelper.getHadoopFilecontent();
                   }
                   break;
               case "xml":
                   if(null==pStructure.xmlFileStructureModel){
                       pStructure.xmlFileStructureModel = {};
                   }else{
                       pStructure.xmlFileStructureModel.columnNameList = [];
                       pStructure.xmlFileStructureModel.columnTypeList = [];
                       pStructure.xmlFileStructureModel.xPathList = [];
                       pStructure.xmlFileStructureModel.container = "";
                       pStructure.xmlFileStructureModel.root = "";
                   }

                   break;
               case "apacheLog":
                   break;
               case "json":
                   break;
           }
        }));
//        
//        
//        var choose_hadoopFileBtn = dijit.byId("btn_"+id);
//        var choose_hadoopFileInput = dijit.byId(id);
//        if(!choose_hadoopFileInput){
//            choose_hadoopFileInput = new dijit.form.ValidationTextBox({id:id,required:true,type:"text",style:"width:160px;",title:prop.value,value:displayValue},dojo.create("div",{},val));
//            
//        }
//        if(!choose_hadoopFileBtn){
//            choose_hadoopFileBtn = new dijit.form.Button({
//                baseClass: getButtonBaseClass(prop),
//                id:"btn_"+id,
//                label:alpine.nls.hadoop_prop_choose_file_btn,
//                onClick:function(){
//                	var hadoopConName;
//                	var callBackFunction = null;
//                	if(CurrentOperatorDTO.classname == "HadoopFileOperator"){
//                		hadoopConName = dijit.byId("connName" + ID_TAG).get("value");
//                		callBackFunction = alpine.props.HadoopFileOperatorPropertyHelper.dbClick4SelectHadoopFile;
//                	}else{
//                		hadoopConName = alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(current_op).connectionName;
//                        callBackFunction = function(hadoopConn){
//                        	choose_hadoopFileInput.set("value", hadoopConn.key);
//                        	choose_hadoopFileInput.set("title", hadoopConn.key);
//                        };
//                	}
//                    if(hadoopConName != ""){
//                        alpine.props.HadoopFileOperatorPropertyHelper.showChooseHadoopFileDlg(callBackFunction, hadoopConName);
//                    }else{
//                        popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_conn_tip);
//                        return false;
//                    }
//
//                }
//
//            },dojo.create("div",{},val));
//        }
//        property_widgets.push(choose_hadoopFileBtn);
        break;
    case "PT_CUSTOM_NAME_HD_CONNECTIONNAME":
        alpine.props.HadoopFileOperatorPropertyHelper.initHadoopConnections();
        generate_filtering_select(val, prop);
            break;
    case "PT_CUSTOM_NAME_HD_FORMAT":
       // alpine.props.HadoopFileOperatorPropertyHelper.initHadoopConnections();
        generate_filtering_select(val, prop);
        break;
        
    case "PT_HD_FILE_EXPLORER": 
    	alpine.props.HadoopCommonPropertyHelper.buildHadoopFileExplorer({
    		id: id,
    		container: val,
    		value: prop.value,
    		explorerButtonLabel: alpine.nls.hadoop_prop_choose_file_btn,
    		includeFile: false,
    		checkPermission: true,
    		onCompleteInitialize: function(widgets, events){
    			property_widgets = property_widgets.concat(widgets);
    			property_event_handlers = property_event_handlers.concat(events);
    		}
    	});
    	break;
	case "PT_SINGLE_SELECT":
        if((CurrentOperatorDTO.classname == "PivotOperator" || CurrentOperatorDTO.classname == "HadoopPivotOperator") && "pivotColumn" == prop.name){
            var propList = CurrentOperatorDTO.propertyList;
            if(propList!=null){
                for(var i=0;i<propList.length;i++){
                   if(propList[i].name=="groupByColumn"){
                       var groupByItems = propList[i].store.items;
                       if(null!=groupByItems && groupByItems.length>0){
                           var deletePosition = -1;
                           for(var j=0;j<groupByItems.length;j++){
                               if(groupByItems[j].name!=null && groupByItems[j].name==prop.value){
                                   deletePosition = j;
                                   break;
                               }
                           }
                           if(-1!=deletePosition){
                               groupByItems.splice(deletePosition,1);
                           }
                       }
                   }
                }
            }
        }
		generate_filtering_select(val, prop);
		break;

	case "PT_MULTI_SELECT":
		//generate_checkbox_panel(val, prop);
        alpine.props.ColumnNamePropertySelectHelper.initColumnData(prop);
        alpine.props.ColumnNamePropertySelectHelper.initDependentColumnValue();
        add_popup(val,prop,alpine.nls.select_columns,alpine.props.ColumnNamePropertySelectHelper.showColumnSelectionDialog);
		break;
		
	//case "PT_CUSTOM_BIN":
		//CurrentBinProperty = prop;
		//generate_checkbox_panel(val, prop);
	//	break;
    case "PT_CUSTOM_SAMPLE_SIZE":
       alpine.flow.SampleSizeModelConfigHelper.clonePorperty4SampleSizeModel(prop);
      add_popup(val,prop,alpine.nls.sample_size_btn4_define,alpine.flow.SampleSizeModelConfigHelper.showSampleSizeModelConfigDlg);
      break;
		
	case "PT_HISTOGRAM":
		add_popup(val, prop, 
				alpine.nls.key_columns, 
				function(prop){
					if(!prop.columnBinsModel){
						prop.columnBinsModel = {columnBins: []};
					}
                    alpine.props.histogramPropertyHelper.showHistogramDialog(prop,getSourceButtonId(prop),function(columnBins){
                        prop.columnBinsModel.columnBins = columnBins;
                        //refresh the selected field.
                        var currentSelectColumns = new Array();
                        for(var i = 0;i < columnBins.length;i++){
                            currentSelectColumns.push(columnBins[i].columnName);
                        }
                        prop.selected = currentSelectColumns;
                    });
				});
		break;
		
        case "PT_CUSTOM_REPLACEMENT":
		    //CurrentReplacementProperty = prop;
		    //generate_checkbox_panel(val, prop);
            //alpine.props.NullValueReplacementPropertyHelper.initNullvaluereplacement(prop);
            if ("HadoopReplaceNullOperator" == CurrentOperatorDTO.classname) { // Hadoop dialog has groupBy/aggregate
                add_popup(val,prop,prop.displayName,function(prop){
                    alpine.props.nullValueReplacementHelper.showNVRDialog(prop,getSourceButtonId(prop));
                });
            } else { // DB dialog does not have groupBy/agg (yet)
                alpine.props.NullValueReplacementPropertyHelper.initNullvaluereplacement(prop);
                add_popup(val,prop,prop.displayName,alpine.props.NullValueReplacementPropertyHelper.showNullvaluereplacementDlg);
            }
            break;

	case "PT_CUSTOM_WHERECLAUSE":
    case "PT_TEXT":
            CurrentWhereClause = prop;
        if(prop.value==null || prop.value==""){
            prop.valid = false;
        }
        var label = alpine.nls.edit_filter;
        if(CurrentOperatorDTO.classname == "SQLExecuteOperator"){
        	label = alpine.nls.sql_execute_prop_button;
        }
        if(CurrentOperatorDTO.classname == "HadoopPigExecuteOperator"){
            label = alpine.nls.hadoop_pig_execute_label;
        }
		add_popup(val, prop,label,alpine.props.RowFilterPropertyHelper.showRowFilterDialog);
		break;

	case "PT_CUSTOM_TABLEJOIN":
		generate_tablejoin_edit_panel(val, prop);
		break;
		

	case "PT_CUSTOM_AGG_COLUMN":
        aggregateColumnChecker.release("PT_CUSTOM_AGG_COLUMN");
        if("HadoopAggregateOperator"==CurrentOperatorDTO.classname){
            alpine.flow.HadoopDefineAggregateHelper.setAggObj(dojo.clone(prop));
            add_popup(val, prop,
                alpine.nls.edit_aggregate_columns,
                alpine.flow.HadoopDefineAggregateHelper.showDefineAggDialog);
        }else{
            add_popup(val, prop,
                alpine.nls.edit_aggregate_columns,
                showAggregateColumnEditDialog);
            if(prop.aggregateFieldsModel && prop.aggregateFieldsModel.aggregateFieldList){
                aggregateColumnChecker.initializeItems(prop.type, prop.aggregateFieldsModel.aggregateFieldList);
            }
        }

		break;

	case "PT_CUSTOM_AGG_WINDOW":
        aggregateColumnChecker.release("PT_CUSTOM_AGG_WINDOW");
		add_popup(val, prop, 
				alpine.nls.edit_agg_win_columns, 
				showAggregateWindowEditDialog);
		if(prop.windowFieldsModel && prop.windowFieldsModel.windowFieldList){
			aggregateColumnChecker.initializeItems(prop.type, prop.windowFieldsModel.windowFieldList);
		}
		break;

	case "PT_CUSTOM_AGG_GROUPBY":
        aggregateColumnChecker.release("PT_CUSTOM_AGG_GROUPBY");
		add_popup(val, prop, 
				alpine.nls.edit_agg_groupby, 
				showAggregateGroupByEditDialog);
		if(prop.aggregateFieldsModel && prop.aggregateFieldsModel.groupByFieldList){
			aggregateColumnChecker.initializeItems(prop.type, prop.aggregateFieldsModel.groupByFieldList);
		}
		break;

	case "PT_CUSTOM_VAR_FIELDLIST":
		add_popup(val, prop, 
				alpine.nls.var_derived_edit_title, 
				//showVariableDerivedEditDialog);
            alpine.props.VariablesDefine4VarOPHelper.showDefineVarDialog);
		break;

	case "PT_CUSTOM_VAR_QUANTILE":
		add_popup(val, prop, 
				alpine.nls.variable_quantile_edit_title, 
				showVariableQuantileEditDialog);
		break;

	case "PT_CUSTOM_NEURAL_HIDDEN_LAYERS":
		generate_hiddenlayer_edit_panel(val, prop);
		break;

	case "PT_CUSTOM_INTERACTION_COLUMNS":
		generate_intactionColumns_edit_panel(val, prop);
		break;
		
	case "PT_CUSTOM_COHORTS":
		add_popup(val, prop, 
				alpine.nls.cohort_edit_button, 
				showCohortsDialog);
		break;

	case "PT_CUSTOM_ADABOOST":
		add_popup(val, prop, 
				alpine.nls.adaboost_param_button, 
				alpine.props.adaboost.showAdaboostDialog);
		break;
	case "PT_INT":
        
		var widget = new dijit.form.ValidationTextBox({
			id : id,
			required : true,
			trim : true,
			value : prop.value,
			isValid: function(){
				var value = this.get("value");
				if(!this.validator(value)){
					return false;
				}
				value = alpine.flow.WorkFlowVariableReplacer.replaceVariable(value);
				if(isNaN(value) || value.length >= 35){
					return false;
				}
				var validate = true;
				validate &= /^[\\+|-]?[\d]*$/.test(value);

				if(/* for int >= 2 */
                    id == "topicNumber"+ID_TAG ||
                    id == "k" + ID_TAG || //k-means
                    id == "numFeatures" + ID_TAG || //SVD
                    id == "split_Number"+ID_TAG //k-means
                  ){
					validate &= parseInt(value) >=2;
                }else if(id=="clusterNumber"+ID_TAG || id=="maxIterationNumber"+ID_TAG){/*for int >1*/
                    validate &= parseInt(value) >1;
                }else if(/* for int >= 1 */
                		 id == "forestSize"+ID_TAG ||  //random forest
//                		 id == "nodeColumnNumber"+ID_TAG ||	//random forest
                    id == "iterationNumber"+ID_TAG || //logR
		        	id == "max_generations" + ID_TAG ||  //logR
                    id == "minimal_size_for_split" + ID_TAG || //CART
                    id == "minimal_leaf_size" + ID_TAG || //CART
                    id == "max_runs" + ID_TAG || //k-means
                    id == "max_optimization_steps" + ID_TAG || //k-means
                    id == "training_cycles" + ID_TAG || //neural network
                    id == "fetchSize" + ID_TAG || //neural network
                    id == "degree" + ID_TAG || //svm regression
                    id == "threshold" + ID_TAG || //time series
                    id == "aheadNumber" + ID_TAG ||//time series prediction
                    id == "p" + ID_TAG || // time series
                    id == "q" + ID_TAG ||// time series
                    id == "initClusterSize"+ID_TAG || //em clustering initClusterSize
                    id == "categoryLimit" + ID_TAG ||// hadoop decision tree
                    id == "numericalGranularity" + ID_TAG // hadoop decision tree
                    ){
		        	validate &= parseInt(value) > 0;
		        } else if (/* for int >= 0 */
                    id == "tableSizeThreshold" + ID_TAG ||//association
                    id == "number_of_prepruning_alternatives" + ID_TAG || //CART
                    id == "size_threshold_load_data" + ID_TAG  ||//CART
                    id == "d" + ID_TAG // time series
                    ){
                    validate &= parseInt(value) >= 0;
                } else if (/* for int >= -1 */
                    id == "local_random_seed" + ID_TAG //neural network
                    ){
                    validate &= parseInt(value) >= -1;
                } else if (/* for int >= 1 or = -1 */
                    id == "maximal_depth" + ID_TAG //CART
                    && CurrentOperatorDTO.classname !="RandomForestOperator"
                    ){
                    var pInt = parseInt(value);
                    validate &= (pInt == -1 || pInt >0);
                }else if(CurrentOperatorDTO.classname =="RandomForestOperator" && id == "maximal_depth" + ID_TAG ){
                    /*for int>=1 and int<=20*/
                    var intValue = parseInt(value);
                    validate &= (intValue >0 && intValue<=20);
                }else if(CurrentOperatorDTO.classname =="HadoopTimeSeriesOperator" && id=="lengthOfWindow"+ID_TAG){
                    var intValue = parseInt(value);
                    validate &=(intValue>0 && intValue<500000);
                }
				return validate;
			}
		}, val);
        //for sample size
		if(prop.name=="sampleCount"){
             dojo.connect(widget,"onChange", alpine.flow.SampleSizeModelConfigHelper.clearSampleSizeModel);
        }
		property_widgets.push(widget);
		break;
		
	case "PT_DOUBLE":
		 
		var widget = createPropertyNumberTextBox(id,prop.value,val);
		property_widgets.push(widget);
		break;

	case "PT_CUSTOM_WOE": 
		add_popup(val, prop, 
				alpine.nls.woe_setting_button, 
				WOE_Setting.showWoeSetting);
		break;
//	case "PT_TEXT":
//		add_popup_text(val, prop,
//				alpine.nls.woe_setting_text,
//				prop.value);
//		break;
	case "PT_CUSTOM_TABLESET":
		/*add_popup(val, prop,
				alpine.nls.tableset_config_title, function(props){
			alpine_props_dataset.showConfigDialog(props);
		});*/
        add_popup(val,prop,alpine.nls.tableset_config_title,function(props){
            alpine.props.setOperatorPropertyHelper.showDialog(props);
        });
		break;
        case "PT_CUSTOM_HD_TABLESET":
        //alpine.props.HadoopTableSetHelper.initHadoopTableSetVariable(prop);
        /*add_popup(val,prop,alpine.nls.tableset_config_title,function(propValue){
            alpine.props.HadoopTableSetHelper.showHadoopTableSetCfgDlg(propValue);
        });*/
        add_popup(val,prop,alpine.nls.tableset_config_title,function(prop){
            alpine.props.setOperatorPropertyHelper.showHadoopDialog(prop);
        });
        break;
	case "PT_OUTPUT_CREATION_PARAMETER": 
		add_popup(val, prop, alpine.nls.output_creation_param_title, function(props){
			alpine_props_storageParameters.startup(props.outputCreationParamModel, getSourceButtonId(props));
		});
		if(prop.outputCreationParamModel.databaseType != "PostgreSQL" && prop.outputCreationParamModel.databaseType !=  "Greenplum"){
			dijit.byId(prop.name + ID_TAG).set("disabled", true);
		}
		break;
	case "PT_CUSTOM_SUBFLOWPATH":
		//Add by Will for subflow		
		buildSubFlowPathSelectList(val,prop,alpine.flow.WorkFlowManager.getEditingFlow(),null);
		break;
	case "PT_CUSTOM_TABLEMAPING":
		var mappingItems = [];
		if(prop!=null && prop.subflowTableMappingModel!=null && prop.subflowTableMappingModel.mappingItems!=null && prop.subflowTableMappingModel.mappingItems.length>0){
			mappingItems = prop.subflowTableMappingModel.mappingItems;
			INPUTTABLEMAPPING = mappingItems;
		}else{
			INPUTTABLEMAPPING=[];
		}
		add_popup(val, prop, 
				prop.displayName, function(props){
			         buildTableMappingDlg(props);
                });
		break;
	case "PT_CUSTOM_EXITOPERATOR":
		//Add by Will for subflow
		buildExitOperatorSelectList(val,prop,null);
		break;
	case "PT_CUSTOM_SUBFLOWVARIABLE":
		//biild subflow_variablelist	
		
		if(null!=prop && prop.flowVariable!=null && null!=prop.flowVariable.variables!=null && prop.flowVariable.variables.length>0){
			SUBFLOWVARIABLE = prop.flowVariable.variables;
		}
		add_popup(val, prop, prop.displayName, alpine.props.SubflowVariablePropHelper.showSubFlwoVariableDlg);
		break;
	case "PT_CUSTOM_UNIVARIATE_MODEL": 
		add_popup(val, prop, alpine.nls.table_column_grid_head_columnName, function(props){
			var operatorTmp = dojo.clone(current_op);
			operatorTmp.outputSchema = CurrentOperatorDTO.inputTableInfos[0].schema;//just for adapter to getColumnNames
			operatorTmp.outputTable = CurrentOperatorDTO.inputTableInfos[0].table;//just for adapter to getColumnNames
			getColumnNames(operatorTmp,function(operator ,columnNames){
				var referenceColumn = dijit.byId(de_refrecen_column_delect_id).get("value");
				var columnNameArray = [];
				   for(var i = 0; i < columnNames.length; i++) {
					   var tempValue = columnNames[i];
					   if(tempValue != null && tempValue.columnName != null){
						   columnNameArray.push(tempValue.columnName[0]);
					   }		  
					}
				props.univariateModel.analysisColumns = columnNameArray;
				props.univariateModel.referenceColumn = referenceColumn;
			},"number",true,true,false, function(){
				if(!props.univariateModel){
					props.univariateModel = {};
					return;
				}
				dijit.byId(de_refrecen_column_delect_id).set("value", props.univariateModel.referenceColumn);
				for(var i = 0;i <dijit.byId("columnNamesItemsGrid").rowCount;i++){
					var row = dijit.byId("columnNamesItemsGrid").getItem(i);
					for(var j = 0;j < props.univariateModel.analysisColumns.length;j++){
						if(props.univariateModel.analysisColumns[j] == toolkit.getValue(row.columnName)){
							dijit.byId("columnNamesItemsGrid").selection.setSelected(i,true);
							break;
						}
					}
				}
			});
		});
		break;
    case "PT_CUSTOM_NOTE":
        var textarea = new dijit.form.SimpleTextarea({
            id:prop.name+ID_TAG,
            name: "myarea",
            rows: "4",
            cols: "50",
            style: "width:100%;"
        }, val);
        textarea.set("value",prop.value);
        property_widgets.push(textarea);
            break;
	case "PT_UNKNOWN":
	default:
		var widget =new dijit.form.ValidationTextBox({
			id : id,
			required : true,
			trim : true,
			value : prop.value
		}, val);
		property_widgets.push(widget);
		break;
	}
	
 
	property_widgets.push(dijit.byId(id));

	 
}

function _addCustomLabel(prop){
    var displayName = prop.displayName;
    if(prop.name!=null && (prop.name=="tableSetConfig" || prop.name=="hadoopUnionModel")){
        displayName =alpine.nls.tableset_config_btn_label;
    }
    if((CurrentOperatorDTO.classname=="CopytoHadoopOperator" || CurrentOperatorDTO.classname=="CopyToDBOperator") &&
        (prop.name!=null && ((prop.name=="connName") || (prop.name=="dbConnectionName")))
        ){
        displayName =alpine.nls.hadoop_copytohadoop_connName;
    }
    if((CurrentOperatorDTO.classname=="CopytoHadoopOperator"  || CurrentOperatorDTO.classname=="CopyToDBOperator") &&
        (prop.name!=null && ((prop.name=="resultsLocation") ||(prop.name=="schemaName")))
        ){
        displayName =alpine.nls.hadoop_copytohadoop_resultsLocation;
    }

    if(CurrentOperatorDTO.classname=="HadoopPigExecuteOperator" && prop.name=="pigScript"){
        displayName = alpine.nls.hadoop_pig_execute_pigScript;
    }
    if(CurrentOperatorDTO.classname=="HadoopPigExecuteOperator" && prop.name=="pigExecuteFileStructure"){
        displayName = alpine.nls.hadoop_pig_execute_pigExecuteFileStructure;
    }
//    if(CurrentOperatorDTO.classname=="HadoopTimeSeriesOperator" && prop.name=="timeFormat"){
//        displayName = alpine.nls.time_series_date_format;
//    }
    return displayName;
}

function createPropertyNumberTextBox(propID,value,parentWidget){
	function  validate_number_text(value, fn){
		var isValid = true;
		isValid &= !isNaN(value);
		isValid &= fn.call(this, parseFloat(value));
		return isValid;
	}
	var required = true;
	if ((propID == "targetCohort" + ID_TAG)||
			(propID == "randomSeed" + ID_TAG)||
			(propID == "initValue" + ID_TAG)||
			(propID == "minImprovement" + ID_TAG)||
			(propID == "originalStep" + ID_TAG)||
			(propID == "slowdownConst" + ID_TAG)||
			(propID == "speedupConst" + ID_TAG)	) {
		
		required = false; 
	}
	//slambda,simThreshold
	//cvd---fastSpeedupConst,minImprovement,speedupConst,slowdownConst ,initValue  is number 
	var validateFunction = function(v){
		return validate_number_text(v, function(){
			return true;
		});
	};
	//(0,1)
	var validateFunction_0_1 = function(v){
		if((v*1)<=0||(v*1)>=1){
			return false;
		}else{
			return true;
		}
	};
	
	var validateFunction_0_11 = function(v){
		if((v*1)<=0||(v*1)>1){
			return false;
		}else{
			return true;
		}
	};

    var validateFunction_0_05 = function(v){
		if((v*1)<=0||(v*1)>0.5){
			return false;
		}else{
			return true;
		}
	};

	var validateFunction_00_11 = function(v){
		if((v*1)<0||(v*1)>1){
			return false;
		}else{
			return true;
		}
	};
	
	//(0,ifinity)
	var validateFunction_0_Infinity = function(v){
		return v > 0;
	};
	//[0,infinity)
	var validateFunction_00_Infinity = function(v){
		if((v*1)<0 ){
			return false;
		}else{
			return true;
		}
	};

	var checkSvmEtaSlambda = function(v1, v2){
		var res = v1 * v2;
		return res > 0 && res <= 1;
	};
	
	if(propID == "eta" + ID_TAG){
		validateFunction = function(v){
			var validate = true;
			validate &= validateFunction_0_11(v);
			if(dijit.byId("slambda" + ID_TAG) != null){
				validate &= checkSvmEtaSlambda(v, alpine.flow.WorkFlowVariableReplacer.replaceVariable(dijit.byId("slambda" + ID_TAG).get("value")));
				if(dijit.byId("eta" + ID_TAG).get("focused")){//avoid recursion invoke
					dijit.byId("slambda" + ID_TAG).validate();
				}
			}
			return validate;
		};
	}
	if(propID == "slambda" + ID_TAG){
		validateFunction = function(v){
			var validate = true;
			validate &= (v > 0);
			if(dijit.byId("eta" + ID_TAG) != null){
				validate &= checkSvmEtaSlambda(v, alpine.flow.WorkFlowVariableReplacer.replaceVariable(dijit.byId("eta" + ID_TAG).get("value")));
				if(dijit.byId("slambda" + ID_TAG).get("focused")){//avoid recursion invoke
					dijit.byId("eta" + ID_TAG).validate();
				}
			}
			return validate;
		};
	}
	
	//(0,1)
	if(propID == "minConfidence" + ID_TAG
			||propID == "learning_rate" + ID_TAG
			||propID == "momentum" + ID_TAG
			||propID == "minSupport" + ID_TAG
			||propID == "percent" + ID_TAG
//			||propID == "slambda" + ID_TAG
			){
		validateFunction=validateFunction_0_1; 
	}
	//(0,1]
	else if(propID == "learning_rate" + ID_TAG
			||propID == "nu" + ID_TAG	){
		validateFunction=validateFunction_0_11;
	}else if(propID == "confidence" + ID_TAG){ //(0,0.5]
        validateFunction=validateFunction_0_05;
    }//[0,1]
	else if(propID == "randomSeed" + ID_TAG||
			propID == "checkValue" + ID_TAG	){
		validateFunction=validateFunction_00_11;
	}//(0,infinity)
	else if(propID == "epsilon" + ID_TAG
			||propID == "error_epsilon" + ID_TAG
			||propID == "gamma" + ID_TAG){
		validateFunction=validateFunction_0_Infinity;
	}//[0,infinity)
	else if(propID == "minimal_gain" + ID_TAG||
			propID == "scoreThreshold" + ID_TAG||
			propID == "targetCohort" + ID_TAG){
		validateFunction=validateFunction_00_Infinity;
	}
	
	var widget =new dijit.form.ValidationTextBox({
		id : propID,	
		required : required,
		isValid : function(){
			var value = this.get("value");
			if(!this.validator(value)){
				return false;
			}
			value = alpine.flow.WorkFlowVariableReplacer.replaceVariable(value);
			return validate_number_text(value, validateFunction);
		},
		trim : true,
		value : value
	}, parentWidget);
	return widget;
}

// This switch need to match the cases of addProperty() function
// Also handle custom popup properties.
function get_current_operator_data(propertyList,isUpdate) {
	for ( var i = 0; i < propertyList.length; i++) {
		var prop = propertyList[i];
		if (prop == null) {
			continue;
		}
		var value = "";

		var id = prop.name + ID_TAG;
		//for plda dicIndexColumn not show reason add by Will
		if(prop.name=="dicIndexColumn" && null==dijit.byId(id)){
		   continue;
		}
		switch (prop.type) {
		//MINERWEB-781
		case "PT_CUSTOM_AGG_COLUMN":
            if("HadoopAggregateOperator"==CurrentOperatorDTO.classname){
               var aggObj =  alpine.flow.HadoopDefineAggregateHelper.getAggObj();
                prop.aggregateFieldsModel = aggObj.aggregateFieldsModel;
                alpine.flow.HadoopDefineAggregateHelper.setAggObj(null);
            }else{
                if(prop.aggregateFieldsModel){
                    for ( var k = 0; k < propertyList.length; k++) {
                        var propx = propertyList[k];
                        if (propx == null) {
                            continue;
                        }
                        if(propx.type=="PT_CUSTOM_AGG_GROUPBY"){
                            if(propx.aggregateFieldsModel&&propx.aggregateFieldsModel.groupByFieldList){
                                prop.aggregateFieldsModel.groupByFieldList=dojo.clone(propx.aggregateFieldsModel.groupByFieldList);
                            }
                        }

                    }
                }
            }

			break;
		case "PT_BOOLEAN":
		case "PT_CHOICE":
			var list = prop.fullSelection;
			for (var j = 0; j < list.length; j++) {
				var button_id = prop.name + ID_TAG + list[j];
				var btn = dojo.byId(button_id);
				if (btn.checked == true) {
					value = btn.value;
					break;
				}
			}
			break;

		case "PT_MULTI_SELECT":
		case "PT_CUSTOM_COHORTS":
		case "PT_TEXT":
			value = prop.value;
			break;
        case "PT_CUSTOM_NOTE":
            value = dijit.byId(id).get("value");
             break;

		case "PT_CUSTOM_WHERECLAUSE":
			value = CurrentWhereClause.value;
			break;

//		case "PT_CUSTOM_BIN":
//			value = CurrentBinProperty.value;
//			break;
        case "PT_CUSTOM_SAMPLE_SIZE":
            if(null==propertyList[i].sampleSizeModelUI){
                propertyList[i].sampleSizeModelUI={};
            }
            var sampleSizeModel = alpine.flow.SampleSizeModelConfigHelper.getSampleSizeModel();
            propertyList[i].sampleSizeModelUI.sampleIdList = sampleSizeModel.sampleIdList;
            propertyList[i].sampleSizeModelUI.sampleSizeList = sampleSizeModel.sampleSizeList;
            if(isUpdate==true){
                alpine.flow.SampleSizeModelConfigHelper.destroySampleSizeModel();
            }
           break;

		case "PT_CUSTOM_REPLACEMENT":
            if ("HadoopReplaceNullOperator" == CurrentOperatorDTO.classname) {
                /* do not need to do anything here
                propertyList[i] = alpine.props.nullValueReplacementHelper.getNVRModel();
                if(isUpdate==true){
                    alpine.props.nullValueReplacementHelper.destroyNVRModel();
                }*/
            } else {
                propertyList[i] = alpine.props.NullValueReplacementPropertyHelper.getNullValueReplaceModel();
                if(isUpdate==true){
                    alpine.props.NullValueReplacementPropertyHelper.destroyNullValueReplaceModel();
                }
            }
			break;
		case "PT_CUSTOM_SUBFLOWVARIABLE":
//			if(null!=SUBFLOWVARIABLE){
//				if(propertyList[i].flowVariable==null){
//					propertyList[i].flowVariable = {};
//				}
//				propertyList[i].flowVariable.variables = SUBFLOWVARIABLE;
//			}
			break;
		case "PT_CUSTOM_TABLEMAPING":
			if(null==prop.subflowTableMappingModel){
				prop.subflowTableMappingModel = {};
			}
			if(null!=INPUTTABLEMAPPING){
				propertyList[i].subflowTableMappingModel.mappingItems = INPUTTABLEMAPPING;
			}
			 
			break;
        case "PT_CUSTOM_NAME_HD_CSVFILESTRUCTURE"	:
            if(propertyList[i].csvFileStructureModel!=undefined){
                propertyList[i].csvFileStructureModel = alpine.props.HadoopFileOperatorPropertyHelper.getCurrentFileStruceModel();
                if(isUpdate==true){
                    alpine.props.HadoopFileOperatorPropertyHelper.clearSturctModelAndContent();
                }
            }
            break;
        case "PT_CUSTOM_NAME_HD_JOIN":
               var joinModel = alpine.props.JoinHadoopPropertyHelper.getHadoopJoinModel();
                if(joinModel!=null){
                    propertyList[i].hadoopJoinModel = joinModel;
                    if(isUpdate==true){
                        alpine.props.JoinHadoopPropertyHelper.clearHadoopJoinModel();
                    }
                }
                break;
        case "PT_CUSTOM_HD_TABLESET":
               break;
        case "PT_CUSTOM_PIG_EXEC_SCRIPT":
        	break;
		case "PT_UNKNOWN":
			break;
		default:
			value = dojo.byId(id).value;
			break;
		}
		
//		if(prop.type.indexOf("PT_DOUBLE")==0){
//			if(dijit.byId(id)&&){
//				value=dijit.byId(id).value;
//				
//			}
//			
//	}
		//FIXED MINERWEB-602
		if((CurrentOperatorDTO.classname =="RandomSamplingOperator"
			||CurrentOperatorDTO.classname =="StratifiedSamplingOperator") && value == "" && prop.name == "randomSeed"){
				value = " ";
		}else if(
				CurrentOperatorDTO.classname =="TimeSeriesOperator" && value == "" && prop.name == "groupColumn"){
			value = " ";
		}else if(CurrentOperatorDTO.classname =="PivotOperator" && value == "" && prop.name == "aggregateColumn"){
			value = " ";
		}else if(CurrentOperatorDTO.classname =="SQLExecuteOperator" && value == "" && prop.name == "dbConnectionName"){
			value = " ";
		}else if(CurrentOperatorDTO.classname =="ProductRecommendationOperator" && value == "" && prop.name == "targetCohort"){
			value = " ";
		}else if(CurrentOperatorDTO.classname=="LogisticRegressionOperator" && value=="" && prop.name=="criterionType"){
			value = " ";
		}else if(CurrentOperatorDTO.classname=="LinearRegressionOperator" && value=="" && prop.name=="criterionType"){
		   value = " ";
	    }
		propertyList[i].value = value;
	}
	
}


function update_property_data() {
	if (!dijit.byId("propertyForm").validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	
	//validate dependent relationship	
	if(CurrentOperatorDTO.classname=="PLDATrainerOperator" && !validatePLDARelationship(CurrentOperatorDTO.propertyList)){
		//popupComponent.alert(alpine.nls.plda_column_same);
		return;	
	}
   if(CurrentOperatorDTO.classname=="PLDAPredictOperator" && !validatePLDAPredictRelationship()){
	   return;
   }

   if((CurrentOperatorDTO.classname=="RandomSamplingOperator" || CurrentOperatorDTO.classname=="StratifiedSamplingOperator")&& alpine.flow.SampleSizeModelConfigHelper.validateSampleSizeModelEmpty()==false){
       popupComponent.alert(alpine.nls.sample_size_model_need_config);
       return;
   }

   if((CurrentOperatorDTO.classname == "BarChartAnalysisOperator"
       || "HadoopBarChartOperator"==CurrentOperatorDTO.classname)){
     var pList = CurrentOperatorDTO.propertyList;
     var categoryType = dijit.byId("categoryType"+ID_TAG).get("value");
     var scopeDomain = dijit.byId("scopeDomain"+ID_TAG).get("value");
     var valueDomain = dijit.byId("valueDomain"+ID_TAG).get("value");

     if(scopeDomain!=null
         && categoryType!=null && categoryType=="" && scopeDomain==""){
         popupComponent.alert(alpine.nls.barchart_Series_Category_set_tip);
         return ;
     }
     if(categoryType==scopeDomain && scopeDomain==valueDomain && valueDomain!=""){
         popupComponent.alert(alpine.nls.anlysis_column_same);
         return ;
     }
     if(categoryType==scopeDomain){
         popupComponent.alert(alpine.nls.anlysis_column_same_Category_Series);
         return;
     }
   }

   if(CurrentOperatorDTO.classname=="HadoopFileOperator"){
         var btn = dijit.byId("hadoopFileStructure"+ID_TAG);
       if(null!=btn && btn.get("baseClass")=="workflowButtonInvalid"){
           popupComponent.alert(alpine.nls.hadoop_prop_file_structure_need_config_tip);
           return ;
       }
   }
	
   if(CurrentOperatorDTO.classname=="HadoopNormalizationOperator" || CurrentOperatorDTO.classname=="NormalizationOperator"){
       var maxVal = dijit.byId("rangeMax"+ID_TAG).get("value");
       var minVal = dijit.byId("rangeMin"+ID_TAG).get("value");
       var status = true;
       try{
         if(parseFloat(maxVal)<=parseFloat(minVal)){
             status = false;
         }
       }catch (e){
           status = false;
       }
       if(status==false){
           popupComponent.alert(alpine.nls.range_erro_tip);
           return false;
       }
   }

	get_current_operator_data(CurrentOperatorDTO.propertyList,true);
	CurrentOperatorDTO.flowInfo.modifiedTime = alpine.flow.WorkFlowManager.getEditingFlow().modifiedTime;
	CurrentOperatorDTO.flowInfo.modifiedUser = login;
	var url = baseURL + "/main/property.do?method=updatePropertyData" + "&user=" + login;
	//progressBar.showLoadingBar();
	//As inputFileInfo include FileStructure.class, and it is an interface, so delete inputFileInfos when submit data to avoid gson deserialize problem.
	delete CurrentOperatorDTO.inputFileInfos; 
	ds.post(url, CurrentOperatorDTO, update_property_data_callback, null, false, DIALOG_ID);
	

}

function update_property_data_callback(obj) {
	//progressBar.closeLoadingBar();
	
	if (obj.error_code&&obj.error_code!=0) {
		if (obj.error_code == -1) {
			popupComponent.alert(alpine.nls.no_login, function(){
				window.top.location.pathname = loginURL;	
			});
			return;
		}
		else {
			var msg = alpine.nls.flow_not_found;
			if (obj.message) {
				msg = obj.message;
			}
			popupComponent.alert(msg);
//			clear_flow_display("Personal");
//			dijit.byId("cancel_flow_button").set("disabled", true);
			return;
		}
	}
	alpine.flow.WorkFlowManager.getEditingFlow().modifiedTime = obj.flowInfo.modifiedTime;
    	

	//update the flow's real propertyList --MinerWeb-67
	//otherwise the dataexplorer always get the old version
	var op = current_op;
	if(op ){
		if(!op.operatorDTO){
			op.operatorDTO = {propertyList:null} ;
		}
		op.operatorDTO.propertyList = CurrentOperatorDTO.propertyList;
	}
//	dijit.byId('propertyFormDialog').hide();
	replaceOperatorListFields(obj);
	
	//MINERWEB-1010
	alpine.flow.OperatorManagementUIHelper.validateOperators();
	
	//if pivot, and its 'useArray' become to false delete all of connections with its children.
	if(CurrentOperatorDTO.classname == "PivotOperator" && CurrentOperatorDTO.propertyList){
		for(var i = 0;i < CurrentOperatorDTO.propertyList.length;i++){
			if(CurrentOperatorDTO.propertyList[i].name == "useArray" 
				&& CurrentOperatorDTO.propertyList[i].value == "false"){
				var childConnections = new Array();
				alpine.flow.OperatorLinkManager.forEachLink(function(link){
					if(link.sourceId == CurrentOperatorDTO.uuid){
						childConnections.push({
							sourceId: link.sourceId,
							targetId: link.targetId
						});
					}
				});
				alpine.flow.OperatorLinkUIHelper.batchDeleteLinkHandler("FlowDisplayPanelPersonal", childConnections);
			}
		}
	}
	//if current operator is hadoop operator and able to output result, we delete link with next operator is learner or copy to db operator. 
	// you have to update both this place and the function which named 'rebuildWorkflow' in ResourceFlowManager.class and the function which named 'validateInputLink' in LinkManagement.class
	if(current_op.operatorType == "HADOOP" && CurrentOperatorDTO.propertyList){
		for(var i = 0;i < CurrentOperatorDTO.propertyList.length;i++){
			if(CurrentOperatorDTO.propertyList[i].name == "storeResults" 
				&& CurrentOperatorDTO.propertyList[i].value == "false"){
				var childConnections = new Array();
				var nextOperators = alpine.flow.OperatorManagementManager.getNextOperators(CurrentOperatorDTO.uuid);
				for(var j = 0;j < nextOperators.length;j++){
					var nextOperator = nextOperators[j];
					if(nextOperator.classname == "HadoopLinearRegressionOperator"
						|| nextOperator.classname == "CopyToDBOperator"
						|| nextOperator.classname == "HadoopKmeansOperator"
						|| nextOperator.classname == "HadoopLogisticRegressionOperator"
						|| nextOperator.classname == "HadoopLinearRegressionPredictOperator"
						|| nextOperator.classname == "HadoopLogisticRegressionPredictOperator"
                        || nextOperator.classname == "HadoopNaiveBayesOperator"
                        || nextOperator.classname == "HadoopNaiveBayesPredictOperator"
						|| nextOperator.classname == "HadoopROCOperator"
							|| nextOperator.classname == "HadoopUnionOperator"
						|| nextOperator.classname == "HadoopLiftOperator"
                        || nextOperator.classname == "HadoopConfusionOperator"
						|| nextOperator.classname == "HadoopGoodnessOfFitOperator"
						|| nextOperator.classname == "HadoopDecisionTreeOperator"
						|| nextOperator.classname == "HadoopDecisionTreePredictOperator"
						|| nextOperator.classname == "HadoopVariableSelectionAnalysisOperator"){
						childConnections.push({
							sourceId: CurrentOperatorDTO.uuid,
							targetId: nextOperator.uid
						});
					}
				}
				alpine.flow.OperatorLinkUIHelper.batchDeleteLinkHandler("FlowDisplayPanelPersonal", childConnections);
			}
		}
	}
	
	CurrentOperatorDTO = null;

	alpine.flow.WorkFlowVariableReplacer.finalize();
	//connect the delete operator and connection event.
	alpine.flow.WorkFlowUIHelper.bindDeleteKeyBoardEvent(true);
	propSideBar.close();

	//this is real change, not cange the same value...
	if(obj.isPropertyChanged==true){
		alpine.flow.WorkFlowUIHelper.setDirty(true);
	}

}

function replaceOperatorListFields(flowInfo){
	//Replace updated operator in operatorList make sure other function can access latest version of operator.--MINERWEB-411

	alpine.flow.OperatorManagementManager.updateOperatorPrimaryInfo(flowInfo.result);
//	for(var j = 0;j < flowInfo.result.length;j++){
//		var newItem = flowInfo.result[j];
//		dojo.safeMixin(item, newItem);
//		item.connectionName = newItem.connectionName;
//		item.isValid = newItem.isValid;
		
//		item.outputTable = newItem.outputTable;
//		item.outputSchema = newItem.outputSchema;
//		item.outputType = newItem.outputType;
//		item.hasDbTableInfo = newItem.hasDbTableInfo;
//		item.interTableList = newItem.interTableList;
		
//		item.outputHadoopFilePath = newItem.outputHadoopFilePath;
//	}
}

function generate_input_choice(val, prop) {
	while (val.firstChild) {
		val.removeChild(val.firstChild);
	}	
	var list = prop.fullSelection;
    //default value
    if(prop.value==null || prop.value == ""){
        prop.value = list[list.length-1];
    }
	var tbl = dojo.create("table", {
		style : {
			position : "relative"
		}
	}, val);
	var tbody = dojo.create("tbody", {
		style : {
			position : "relative"
		}
	}, tbl);
	var row = dojo.create("tr", null, tbody);

	for (var j = 0; j < list.length; j++) {
		var item = list[j];
		if (item == null) {
			continue;
		}
		var button_id = prop.name + ID_TAG + list[j];
		var label_id = button_id + "label";

		var obj = dijit.byId(button_id);
		if (obj) {
			obj.destroyRecursive(false);
			dijit.registry.remove(button_id);
		}

		obj = dijit.byId(label_id);
		if (obj) {
			obj.destroyRecursive(false);
			dijit.registry.remove(label_id);
		}

		var checked = false;
		if (list[j] == prop.value) {
			checked = true;
		}
		obj = dojo.create("td", null, row);
		obj = dojo.create("div", null, obj);
		var btn = new dijit.form.RadioButton({
			id : button_id,
			checked : checked,
			value : list[j],
			name : prop.displayName
		}, obj);
        dojo.connect(btn,"onChange",function(){
            if(this.id=="useArray"+ID_TAG+"true"){
                if(this.get("checked")==true){
                    var propertyList = CurrentOperatorDTO.propertyList;
                    var columnNameObj = null;
                    for(var j=0;j<propertyList.length;j++){
                        if(propertyList[j].name == "columnNames"){
                            columnNameObj = propertyList[j];
                            break;
                        }
                    }
                    var inputTableInfo = CurrentOperatorDTO.inputTableInfos[0];
                    if(null!=columnNameObj && inputTableInfo!=null && null!=inputTableInfo.fieldColumns){
                        columnNameObj.fullSelection = [];
                        columnNameObj.selected = [];
                        columnNameObj.valid = false;
                        setButtonBaseClassInvalid(getSourceButtonId(columnNameObj));
                        for(var k=0;k<inputTableInfo.fieldColumns.length;k++){
                           var column = inputTableInfo.fieldColumns[k];
                           if(column[1]=="array"){
                               columnNameObj.fullSelection.push(column[0]);
                           }
                       }
                    }
                }
            }

            if(this.id=="useArray"+ID_TAG+"false"){
                if(this.get("checked")==true){
                    var propertyList = CurrentOperatorDTO.propertyList;
                    var columnNameObj = null;
                    for(var j=0;j<propertyList.length;j++){
                        if(propertyList[j].name == "columnNames"){
                            columnNameObj = propertyList[j];
                            break;
                        }
                    }
                    var inputTableInfo = CurrentOperatorDTO.inputTableInfos[0];
                    if(null!=columnNameObj && inputTableInfo!=null && null!=inputTableInfo.fieldColumns){
                        columnNameObj.fullSelection = [];
                        columnNameObj.selected = [];
                        columnNameObj.valid = false;
                        setButtonBaseClassInvalid(getSourceButtonId(columnNameObj));
                        for(var k=0;k<inputTableInfo.fieldColumns.length;k++){
                            var column = inputTableInfo.fieldColumns[k];
                            columnNameObj.fullSelection.push(column[0]);

                        }
                    }
                }
            }
            if((CurrentOperatorDTO.classname=="LinearRegressionOperator"
                ||CurrentOperatorDTO.classname=="LogisticRegressionOperator")
              && (this.id == "isStepWise"+ID_TAG+"true"
                 || this.id == "isStepWise" +ID_TAG+"false")
               ){
                alpine.props.ColumnNamePropertySelectHelper.validateBtnStautsByIsStepWiseChange();
            }
        });
		property_widgets.push(btn);
		obj = dojo.create("td", {
			style : "width: auto;", nowrap: "nowrap"
		}, row);
		dojo.create("label", {
			innerHTML : list[j]
		}, obj);
	}
}

function sort_list_by_name(items) {
	if (items && items.length > 1) {
		items.sort( function(a, b) {
			if (a.name < b.name) { 
				return -1;
			}
			else if (a.name > b.name) {
			 	return 1; 
			} 
			else { 
			 	return 0; 
			}
		});
	}
}

//function addEmptyValue(storeModel){
//  if(null!=storeModel && storeModel.items!=null && storeModel.items.length>0){
//      storeModel.items.splice(0,0,{name:"　"});
//  }
//}

function generate_filtering_select(parent, prop) {

	if (prop.store && prop.store.items) {
        //add for copytodb @defaultschema begin
        if("schemaName"==prop.name && CurrentOperatorDTO.classname == "CopyToDBOperator"){
            var deleteIndex = -1;
            for(var i=0;i<i<prop.store.items.length;i++){
                if(prop.store.items[i].name == "@default_schema"){
                    //prop.store.items.splice(i,1);
                    deleteIndex = i;
                    break;
                }
            }
            if(deleteIndex!=-1){
                prop.store.items.splice(deleteIndex,1);
            }
        }
//        if((prop.name=="scopeDomain" || prop.name=="categoryType")
//            && (CurrentOperatorDTO.classname == "BarChartAnalysisOperator"
//                || "HadoopBarChartOperator"==CurrentOperatorDTO.classname)){
//            addEmptyValue(prop.store);
//        }
        //end
		sort_list_by_name(prop.store.items);
	}else{//an empty options
		prop.store={
			identifier:	"name",				
			items:[],				
			label:	"name"
		};
	}
	var nameStore = new dojo.data.ItemFileReadStore({
		data : prop.store
		
	});

    if(("UfeatureColumn"==prop.name || "VfeatureColumn"==prop.name || "singularValuefeatureColumn"==prop.name) && null==prop.value){
        prop.value = "alpine_feature";
    }

	var id = prop.name + ID_TAG;
	while (parent.firstChild) {
		parent.removeChild(parent.firstChild);
	}
	var fselDomNode = dojo.create("div") ;
	parent.appendChild(fselDomNode);
	var fsel = new dijit.form.FilteringSelect({
		id : id,
		name : id,
		store : nameStore,
		sort : true,
		searchAttr : "name",
		value : prop.value,
        baseClass : "greyDropdownButton"
	}, fselDomNode);
    // hadoop file type
      if(CurrentOperatorDTO.classname == "HadoopFileOperator"
          && prop.name == "hadoopFileFormat"
          && getHadoopFileName()!=""
          && isFileStructureInvalide()==true){
          var fileName = getHadoopFileName();
         if(fileName!=null){
             if (fileName.lastIndexOf(".") != -1) {
                 var suffix = fileName.substring(fileName.lastIndexOf(".")+1);
                 switch (suffix.toLowerCase()) {
                     case "json":
                         fsel.set("value", "JSON");
                         break;
                     case "xml":
                         fsel.set("value", "XML");
                         break;
                     case "csv":
                         fsel.set("value", "Text File");
                         break;
                     case "log":
                         fsel.set("value", "Log File");
                         break;
                 }
             }
         }
      }

    //
	property_widgets.push(fsel);
	if(prop.name == "criterionType" || "splitModelGroupByColumn"==prop.name){
		//can be null
		fsel.set("required",false);
	}

    if(prop.name=="aggregateColumn" && (CurrentOperatorDTO.classname=="HadoopPivotOperator" || CurrentOperatorDTO.classname=="PivotOperator")){
        fsel.set("required",false);
    }
    if(prop.name=="exitOperator" && CurrentOperatorDTO.classname=="SubFlowOperator"){
        fsel.set("required",false);
    }

    if((prop.name=="categoryType" || prop.name=="scopeDomain")
        && (CurrentOperatorDTO.classname == "BarChartAnalysisOperator"
        || "HadoopBarChartOperator"==CurrentOperatorDTO.classname)){
        fsel.set("required",false);
    }
    if((prop.name=="dbConnectionName")
        && CurrentOperatorDTO.classname == "SQLExecuteOperator"){
        if(CurrentOperatorDTO.inputTableInfos!=null && CurrentOperatorDTO.inputTableInfos[0]!=null){
            fsel.set("required",false);
        }else{
            fsel.set("required",true);
        }
    }

    //validate when variabel change for outputSchema
    if(prop.name == "outputSchema"){
        var invalideList = CurrentOperatorDTO.invalidPropertyList;
        if(null!=invalideList){
            for(var k=0;k<invalideList.length;k++){
                if(invalideList[k]=="outputSchema"){
                    fsel.set("value","");
                    break;
                }
            }
        }
    }

    fsel.startup();
	property_widgets.push(fsel) ;

var handler = null;
	if (prop.name == "dependentColumn") {
		CurrentDependentColumn = prop.value;
		DependentColumnRequired = true;
		handler = dojo.connect(fsel, "onChange", function(value) {
			CurrentDependentColumn = value;
            alpine.props.ColumnNamePropertySelectHelper.setDependColumnValue(value);
			//change column selected status Add By Will
			var propertyList = CurrentOperatorDTO.propertyList;
			var selectedIndex = -1;
			var propertyListIndex = -1;
			if(null!=propertyList && propertyList.length!=null && propertyList.length>0){
				for ( var i = 0; i < propertyList.length; i++) {
					if(propertyList[i].name=="columnNames" && propertyList[i].selected!=null && propertyList[i].selected.length!=null){
						propertyListIndex = i;
						for ( var j = 0; j < propertyList[i].selected.length; j++) {
							if(propertyList[i].selected[j]==CurrentDependentColumn){
								//propertyList[i].selected.splice(j,1);
								selectedIndex = j;
							}
						}
					}
				}
				//alert(selectedIndex);
				//alert(propertyListIndex);
				if(selectedIndex!=-1 && propertyListIndex!=-1){
					propertyList[propertyListIndex].selected.splice(selectedIndex,1);
					propertyList[propertyListIndex].value = propertyList[propertyListIndex].selected.join(",");
                    alpine.props.ColumnNamePropertySelectHelper.initColumnData(propertyList[propertyListIndex]);
				}
				
			}
            //group by
            if(dijit.byId('splitModelGroupByColumn'+ID_TAG)!=null
                && dijit.byId("splitModelGroupByColumn"+ID_TAG).get("disabled")==false
                && dijit.byId('splitModelGroupByColumn'+ID_TAG).get("value")==CurrentDependentColumn){
                dijit.byId('splitModelGroupByColumn'+ID_TAG).set("value","");

            }
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "idColumn") {
		CurrentDependentColumn = prop.value;
		handler = dojo.connect(fsel, "onChange", function(value) {
			//CurrentDependentColumn = value;
            var CurrentDependentColumn = value;
            alpine.props.ColumnNamePropertySelectHelper.setDependColumnValue(value);
            //change column selected status Add By Will
            var propertyList = CurrentOperatorDTO.propertyList;
            var selectedIndex = -1;
            var propertyListIndex = -1;
            if(null!=propertyList && propertyList.length!=null && propertyList.length>0){
                for ( var i = 0; i < propertyList.length; i++) {
                    if(propertyList[i].name=="idColumn" && propertyList[i].selected!=null && propertyList[i].selected.length!=null){
                        propertyListIndex = i;
                        for ( var j = 0; j < propertyList[i].selected.length; j++) {
                            if(propertyList[i].selected[j]==CurrentDependentColumn){
                                //propertyList[i].selected.splice(j,1);
                                selectedIndex = j;
                            }
                        }
                    }
                }
                //alert(selectedIndex);
                //alert(propertyListIndex);
                if(selectedIndex!=-1 && propertyListIndex!=-1){
                    propertyList[propertyListIndex].selected.splice(selectedIndex,1);
                    propertyList[propertyListIndex].value = propertyList[propertyListIndex].selected.join(",");
                    alpine.props.ColumnNamePropertySelectHelper.initColumnData(propertyList[propertyListIndex]);
                }
            }
		});
		property_event_handlers.push(handler);
	}else if(prop.name=="pivotColumn"){
        handler = dojo.connect(fsel,"onChange",function(){
            //alert(this.value);
            var propList = CurrentOperatorDTO.propertyList;
            var groupProperty = null;
            for(var i=0;i<propList.length;i++){
                if(propList[i].name=="groupByColumn"){
                    groupProperty = propList[i];
                    break;
                }
            }
            var groupSelect = dijit.byId("groupByColumn"+ID_TAG);
            if(null!=groupSelect){
                var storeItem = [];
                if(groupProperty!=null && groupProperty.fullSelection!=null && groupProperty.fullSelection.length>0){
                    for(var j=0;j<groupProperty.fullSelection.length;j++){
                        if(groupProperty.fullSelection[j]!=this.value){
                            storeItem.push({name:groupProperty.fullSelection[j]});
                        }
                    }
                    var store = new dojo.data.ItemFileReadStore({
                        data : {
                            identifier:	"name",
                            items:storeItem,
                            label:	"name"
                        }
                    });
                    groupSelect.set("store",store);
                    groupSelect.startup();
                    groupSelect.set("value","");
                }
            }

        });

    }
	else if (prop.name == "dbConnectionName") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			update_schema_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "schemaName") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			value = alpine.flow.WorkFlowVariableReplacer.replaceVariable(value);
			update_table_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "customerTable") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			update_customer_table_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "selectionTable") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			update_selectdTable_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "recommendationTable") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			update_recommendationTable_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "preTable") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			update_preTable_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if (prop.name == "postTable") {
		handler = dojo.connect(fsel, "onChange", function(value) {
			update_postTable_list(value);
		});
		property_event_handlers.push(handler);
	}
	else if(prop.name == "dictionarySchema"){
		//add by Will
		handler = dojo.connect(fsel, "onChange", function(value) {
			var connnectName = CurrentOperatorDTO.inputTableInfos[0].connectionName;
			value = alpine.flow.WorkFlowVariableReplacer.replaceVariable(value);
			update_table_list4PLDA(value,connnectName,update_table_list4PLDA_callback);
		});
		property_event_handlers.push(handler);
	}else if(prop.name == "dictionaryTable"){
		//add by Will
		handler = dojo.connect(fsel, "onChange", function(value) {
			var connnectName = CurrentOperatorDTO.inputTableInfos[0].connectionName;
			var schemaName = dijit.byId("dictionarySchema"+ID_TAG).get("value");
			var tableName = dijit.byId(prop.name+ID_TAG).get("value");
			
			var dicIndexColumn = dijit.byId("dicIndexColumn"+ID_TAG);
			if(null!=dicIndexColumn){
				//type mast be int
				update_columns4PLDADicIndexColumn(connnectName,schemaName,tableName,update_columns4PLDA_callBack);
			}else{
				//gp pg oracle type mast be array else all
				update_columns4PLDADicContentColumn(connnectName,schemaName,tableName,update_columns4PLDA_callBack );
			}
			
		});
		property_event_handlers.push(handler);
	
	}//else if(prop.name == "hadoopConnetionName"){
	else if(prop.name == "connName" && prop.type=="PT_CUSTOM_NAME_HD_CONNECTIONNAME"){
        handler = dojo.connect(fsel,"onChange",function(){
            var hadoopFileName = dijit.byId('hadoopFileName'+ID_TAG);
            if(null!=hadoopFileName){
                hadoopFileName.set('value','');
            }
            var resultsLocation = dijit.byId("resultsLocation"+ID_TAG);
            if((null!=resultsLocation || null!=hadoopFileName) && this.get("value")!=""){
               var key =  alpine.props.HadoopFileOperatorPropertyHelper.getConnectionKey(this.get("value"));
               if(null!=key){
                   alpine.props.HadoopCommonPropertyHelper.setConnectionKey(key);
               }
            }
        });
        property_event_handlers.push(handler);
    }else if(prop.name=="splitModelGroupByColumn"){
        //for logisticRegression splitModelGroupByColumn
       handler = dojo.connect(fsel,"onChange",function(){
           if(this.get('value')==CurrentDependentColumn){
               this.set('value',"");
               popupComponent.alert(alpine.nls.split_model_group_by_column_error_tip);
           }
           if((CurrentOperatorDTO.classname == "LinearRegressionOperator"
               || CurrentOperatorDTO.classname == "LogisticRegressionOperator")){
              var columnsProperty = alpine.props.ColumnNamePropertySelectHelper.getColumnNameProps();
               for(var p in columnsProperty){
                   if(null!=columnsProperty[p] && null!= columnsProperty[p].selected){
                       if(dojo.indexOf(columnsProperty[p].selected,this.get('value'))!=-1){
                           this.set('value',"");
                           popupComponent.alert(alpine.nls.split_model_group_by_column_error_tip_1);
                       }
                   }
               }
          }
       });
       property_event_handlers.push(handler);

    }else if(prop.name=="hadoopFileFormat"){
        alpine.props.HadoopFileOperatorPropertyHelper.setFileFormatType(prop.value);
        handler = dojo.connect(fsel,"onChange",function(){
            var model = alpine.props.HadoopFileOperatorPropertyHelper.getCurrentFileStruceModel();
            if(null!=model){
//                if(this.get("value")=="Text File"){
//                    alpine.props.HadoopFileOperatorPropertyHelper.resetFileStructModel(model);
//                    alpine.props.HadoopFileOperatorPropertyHelper.getHadoopFilecontent();
//                }
                alpine.props.HadoopFileOperatorPropertyHelper.setFileFormatType(this.get("value"));
            }
        });
        property_event_handlers.push(handler);
    }
	return fsel;
}

function getHadoopFileName(){
  var fileName="";
  if(CurrentOperatorDTO.propertyList!=null && CurrentOperatorDTO.propertyList.length>0){
      for(var i=0;i<CurrentOperatorDTO.propertyList.length;i++){
          if(CurrentOperatorDTO.propertyList[i].name == "hadoopFileName"){
              fileName = CurrentOperatorDTO.propertyList[i].value;
              break;
          }
      }
  }
  return fileName;
}

function isFileStructureInvalide(){
    if(CurrentOperatorDTO.invalidPropertyList!=null && CurrentOperatorDTO.invalidPropertyList.length>0){
         if(dojo.indexOf(CurrentOperatorDTO.invalidPropertyList,"hadoopFileStructure")!=-1){
             return true;
         }
    }
    return false;
}

function update_schema_list(conn) {
	if (conn == "") {
		return;
	}
	var url = baseURL + "/main/dataSource/explorer.do?method=getSchemaByConnection"
	 	+ "&dbConnectionName=" + conn
	//progressBar.showLoadingBar();
	ds.get(url, update_schema_list_callback,property_error_callback, false, DIALOG_ID);
}

function property_error_callback(){
	//progressBar.closeLoadingBar();
	error_callback();
}

function update_schema_list_callback(list) {
	//progressBar.closeLoadingBar();
	if(!list){
		return;
	}
	if (list.error_code&&list.error_code!=0) {
		if (list.error_code == -1) {
			popupComponent.alert(alpine.nls.no_login, function(){
				window.top.location.pathname = loginURL;	
			});
			return;
		}
		else {
			var msg = "";
			if (list.message) {
				msg = list.message;
			}
			popupComponent.alert(msg);
			 
			return;
		}			
	}

	var id = "schemaName" + ID_TAG;
	var fsel = dijit.byId(id);
	if (fsel != null && dojo.byId(id)) {
       /* if(CurrentOperatorDTO.classname=="CopyToDBOperator"){
            list.push("@default_schema");
        }*/   
		
		
		var store = make_store(list);
		fsel.set("value", "");		
		fsel.set("store", store);
		fsel.startup();
		update_table_list_callback(new Array());
	}
	else {
		list.push("@default_schema");// for time series prediction
		var store = make_store(list);
		id = "outputSchema" + ID_TAG;
		fsel = dijit.byId(id);
		if (fsel != null && dojo.byId(id)) {			
			fsel.set("value", "");
			fsel.set("store",store);
			fsel.startup();
		}
	}
	
}

function update_table_list(schema) {
	if(!schema || schema == null || schema == ""){
		return;
	}
	var id = "dbConnectionName" + ID_TAG;
	var conn = dojo.byId(id).value;
	var url = baseURL + "/main/dataSource/explorer.do?method=getTableViewNames"
		 + "&user=" + login
		 + "&conn=" + conn
		 + "&schema=" + schema
		 + "&resourceType=" + alpine.flow.WorkFlowManager.getEditingFlow().type;
	//progressBar.showLoadingBar();
	ds.get(url, update_table_list_callback, property_error_callback, false, DIALOG_ID);
}




function update_table_list_callback(list) {
	//progressBar.closeLoadingBar();
	var id = "tableName" + ID_TAG;
	var fsel = dijit.byId(id);
	if (fsel == null) {
		return;
	}
	var store = make_store(list);
	update_single_select_store(id, store);
}

function update_customer_table_list(value) {
	var list = get_column_values_from_table(value);
	var store = make_store(list);
	var ids = ["customerIDColumn", 
				"customerValueColumn", 
				"customerProductColumn", 
				"customerProductCountColumn"];
	for (var i = 0; i < ids.length; i++) {
		update_single_select_store(ids[i] + ID_TAG, store);
	}
}
		
function update_selectdTable_list(value) {
	var list = get_column_values_from_table(value);
	var store = make_store(list);
	var id = "selectionIDColumn" + ID_TAG;
	update_single_select_store(id, store);
}

function update_recommendationTable_list(value) {
	var list = get_column_values_from_table(value);
	var store = make_store(list);
	var id = "recommendationIdColumn" + ID_TAG;
	update_single_select_store(id, store);
	id = "recommendationProductColumn" + ID_TAG;
	update_single_select_store(id, store);
}

function update_preTable_list(value) {
	var list = get_column_values_from_table(value);
	var store = make_store(list);
	var id = "preIdColumn" + ID_TAG;
	update_single_select_store(id, store);
	id = "preValueColumn" + ID_TAG;
	update_single_select_store(id, store);
}

function update_postTable_list(value) {
	var list = get_column_values_from_table(value);
	var store = make_store(list);
	var id = "postIdColumn" + ID_TAG;
	update_single_select_store(id, store);
	id = "postProductColumn" + ID_TAG;
	update_single_select_store(id, store);
	id = "postValueColumn" + ID_TAG;
	update_single_select_store(id, store);
}


function get_column_values_from_table(value) {
	// the value is in the form of:
	//     "demo"."abandonment"
	// use the schema and table name to find the columns.
	var ret = new Array();
	var data = CurrentOperatorDTO.inputTableInfos;
	if (!data || data.length == 0) {
		return ret;
	}

	var list = null;
	for (var i = 0; i < data.length; i++) {
		var name = '"' + data[i].schema + '"."' + data[i].table + '"';
		if (name == value) {
			list = data[i].fieldColumns;
		}
	}

	if (list == null) {
		return ret;
	}
	
	for (var i = 0; i < list.length; i++) {
		ret[i] = list[i][0];
	}
	return ret;
}


function update_single_select_store(id, store) {
       var fsel = dijit.byId(id);
       fsel.set("value", "");
       fsel.set("store", store);
       fsel.startup();
   }

function make_store(list) {
	list.sort();
	var itemList = new Array();
	for (var i = 0; i < list.length; i++) {
		itemList[i] = { name : list[i] };
	}
	var store = {
			identifier : 'name',
			label : 'name',
			items : itemList
		};
	var nameStore = new dojo.data.ItemFileReadStore({
		data : store
		
	});		
	return nameStore;
}

function getButtonBaseClass(prop)
{

    var baseClass = "workflowButton";
    if (!prop || !prop.valid) baseClass = "workflowButtonInvalid" ;
    return baseClass;
}


function setButtonBaseClassValid(btnID)
{
    dijit.byId(btnID).set('baseClass', "workflowButton");
    dijit.byId(btnID).focus();

}

function setButtonBaseClassInvalid(btnID)
{
    dijit.byId(btnID).set('baseClass', "workflowButtonInvalid");
    dijit.byId(btnID).focus();

}

function getSourceButtonId(prop)
{
    if (prop) return prop.name + ID_TAG;
    return ID_TAG;
}


function generate_tablejoin_edit_panel(parent, prop) {
	current_tableJoin = prop;
	var id = prop.name + ID_TAG;
	var btn = new dijit.form.Button({
		id : id,
		label : alpine.nls.hadoop_define_join_condition_btn,
        baseClass: getButtonBaseClass(prop)
	}, parent);
	property_widgets.push(btn) ;
	var handler =dojo.connect(btn, 'onClick', null, function() {
		//showTableJoinDialog();
        alpine.props.JoinTablePropertyHelper.showTableJoinDialog(current_op);

	});
	property_event_handlers.push(handler);
}

function generate_hiddenlayer_edit_panel(parent, prop) {
	current_hiddenLayer = prop;
	var id = prop.name + ID_TAG;
	var btn = new dijit.form.Button({
		id : id,
		label : prop.displayName,
        baseClass: getButtonBaseClass(prop)
	}, parent);
	property_widgets.push(btn) ;
	var handler = dojo.connect(btn, 'onClick', null, function() {
		showHiddenLayerDialog();
	});
	property_event_handlers.push(handler);
}

function generate_intactionColumns_edit_panel(parent, prop) {
	current_interaction_columns = prop;
	var id = prop.name + ID_TAG;
	var btn = new dijit.form.Button({
		id : id,
		label : prop.displayName,
        baseClass: getButtonBaseClass(prop)
	}, parent);
	property_widgets.push(btn) ;
	var handler = dojo.connect(btn, 'onClick', null, function() {
		showInterActionColumnsDialog();
	});
	property_event_handlers.push(handler);
}

//function add_popup_text(parent, prop, label, func) {
//	var id = prop.name + ID_TAG;
//	var btn = new dijit.form.Button({
//		id : id,
//		label : label,
//        baseClass: getButtonBaseClass(prop)
//	}, parent);
//	property_widgets.push(btn) ;
//
//	var handler = 	dojo.connect(btn, 'onClick', null, function() {
//		dijit.byId("sql_executor_dialog").show();
//		dojo.byId("sql_executor_label").innerHTML = prop.displayName;
//		dijit.byId("sql_executor_text").set("value",prop.value?prop.value:" ");
//	});
//	var submitEventHandler = dojo.connect(dijit.byId("sql_executor_text_submit"), "onClick", dijit.byId("sql_executor_text_submit"), function(){
//
//		var sql_executor_text = dijit.byId("sql_executor_text").get("value") ;
//
//		if(null!=sql_executor_text && ""!=sql_executor_text && sql_executor_text.length>1){
//			prop.value = sql_executor_text;
//            setButtonBaseClassValid(id);
//			dijit.byId("sql_executor_dialog").hide();
//
//		}else{
//			popupComponent.alert(alpine.nls.var_sql_text_tip);
//		}
//	});
//	var cancelEventHandler = dojo.connect(dijit.byId("sql_executor_text_cancel"),"onClick",dijit.byId("sql_executor_text_cancel"),function(){
//		dijit.byId("sql_executor_dialog").hide();
//	});
//
//	property_event_handlers.push(handler);
//	property_event_handlers.push(submitEventHandler);
//	property_event_handlers.push(cancelEventHandler);
//}

function create_default_column_list(store, parent_id, callback) {
    if (store && store.items) {
        sort_list_by_name(store.items);
    }
    var layout = [ {
        field : 'name',
        name : alpine.nls.avaibale_columns,
        width : 'auto'

    } ];

    var nameStore = new dojo.data.ItemFileReadStore({
        data : store
    });
    var panel = dojo.byId(parent_id);
    if (panel.firstChild) {
        panel.removeChild(panel.firstChild);
    }

    var domNode =document.createElement('div');
    panel.appendChild(domNode);
    var grid = new dojox.grid.DataGrid({
        query : {
            name : '*'
        },
        store : nameStore,
        clientSort : true,
        rowSelector : '10px',
        structure : layout
    }, domNode);


    var handler = dojo.connect(grid, "onRowDblClick", callback);
    sp_property_event_handlers.push(handler);
    grid.startup();
    sp_property_widgets.push(grid) ;
    return grid;
}

function add_popup(parent, prop, label, func) {
	var id = prop.name + ID_TAG;
    var btn = new dijit.form.Button({
        baseClass: getButtonBaseClass(prop),
		id : id,
		label : label
	}, parent);
	property_widgets.push(btn) ;
	var handler = 	dojo.connect(btn, 'onClick', null, function() {
		func(prop);
	});
	property_event_handlers.push(handler);
}






//Add by Will begin
function _buildListObject(list){
 var objList = [];
 if(list && list.length!=null && list.length>0){
   for ( var len = 0; len < list.length; len++) {
	   if(CurrentDependentColumn!=null && list[len]==CurrentDependentColumn){
	      continue;
	   }
	   objList.push({"colName":list[len]});
   }
 }
 return objList;
}

function clear_sp_property_dialog(){
	if(sp_property_event_handlers){
		for(var i=0;i<sp_property_event_handlers.length;i++){
			if(sp_property_event_handlers[i]){
				dojo.disconnect(sp_property_event_handlers[i]);		
			}
		}
		sp_property_event_handlers=new Array();
	}
	
	if(sp_property_widgets){
		for(var i=0;i<sp_property_widgets.length;i++){
			if(sp_property_widgets[i]){
				
				dijit.registry.remove(sp_property_widgets[i].id);
				sp_property_widgets[i].destroyRecursive();
			}
		}
		sp_property_widgets=new Array();
	}
}


function clear_property_dialog(){
	
	var tbl = dojo.byId("propertyTable");
	 
	while (tbl.firstChild) {
		tbl.removeChild(tbl.firstChild);
	}
 
	if(property_event_handlers){
		for(var i=0;i<property_event_handlers.length;i++){
			if(property_event_handlers[i]){
				dojo.disconnect(property_event_handlers[i]);		
			}
		}
		property_event_handlers=new Array();
	}
	
	if(property_widgets){
		for(var i=0;i<property_widgets.length;i++){
			if(property_widgets[i]){
				dijit.registry.remove(property_widgets[i].id);
				property_widgets[i].destroyRecursive();
				
			}
		}
		property_widgets=new Array();
	}
	
	
	  CurrentOperatorDTO = null;
  current_op = null ;
  CurrentColumnList = null;
  //CurrentBinProperty = null;
  CurrentReplacementProperty = null;
  CurrentWhereClause = null;
  CurrentDependentColumn = null;
  DependentColumnRequired = false;

//property for dialog
  current_tableJoin = null;
  current_hiddenLayer = null;
  current_interaction_columns = null;
  VquantileCurrentModel = null;
  
	  AggProperty = null;

  CurrentAggregateFieldsModel = null;
    CurrentAboostDataList = null;
    CurrentAdaboostOp = null;
 
CurrentGroupByFieldsModel = null;

  var_quantileStore =null;
  var_derivedStore = null;

  VDCurrentModel = null;
  VarColumnList = null;
  

  CurrentWindowFieldsModel = null;
  AggColumnList = null;

  agg_windowStore = null;
  CurrentAboostModel =null;
  clear_sp_property_dialog();
  
  
}