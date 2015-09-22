/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: OperatorMenuHelper
 * Author: Will
 * Date: 12-7-27
 */

define([
    "alpine/system/PermissionUtil",
    "alpine/flow/WorkFlowRunnerUIHelper",
    "dijit/Menu",
    "alpine/dataexplorer/DataExplorerHelper"
], function(permissionUtil, runnerHelper, Menu, dataExplorerHelper){

    var constants = {
        CANVAS: "FlowDisplayPanelPersonal"
    };

    //right-click menus should not open when the flow is running - we must override _openMyself
    //MINERWEB-1201
    dojo.declare('OpMenu',[Menu],{
        _openMyself: function(args){
            if (runnerHelper.isFlowRunning()) {
            	return;
            }
            this.inherited(arguments);
        }
    });

    var blankMenu = null;
    var canvas = null;
    /**
     * call when open flow editor
     */
    function _setupBlankMenu(paneId){
    	if(!permissionUtil.checkPermission("OPERATOR_EDIT")){
    		return;
    	}
    	canvas = paneId;
    	blankMenu = new dijit.Menu({});
    	blankMenu.bindDomNode(dojo.byId(canvas));
    	dojo.connect(blankMenu, "_openMyself", blankMenu, function(){
    		var removedItem = null;
    		while((removedItem = this.getChildren().pop()) != null){
                this.removeChild(removedItem);
            }
        	this.addChild(new dijit.MenuItem({
        		label: alpine.nls.copy_paste_menu_paste,
        		disabled: !alpine.flow.CopyManager.hasCopied(),
        		onClick: function() {
        			alpine.flow.CopyManager.paste();
        		}
        	}));
        	this.startup();
    	});
    }
    
    /**
     * call unit close flow editor
     */
    function _releaseBlankMenu(){
    	if(blankMenu){
        	blankMenu.unBindDomNode(dojo.byId(canvas));
        	blankMenu = null;
    	}
    	canvas = null;
    }
    
    function addOperatorMenu(operator,bindNode) {
        var pMenu = new OpMenu({});
        pMenu.bindDomNode(bindNode);
        dojo.connect(bindNode, "oncontextmenu", function(e){
            var removedItem;
            if(alpine.flow.OperatorManagementUIHelper.getSelectOperatorUidArray().length > 1){
                while((removedItem = pMenu.getChildren().pop()) != null){
                	pMenu.removeChild(removedItem);
                }
            	_buildMultipleSelMenu(pMenu);
            }else{
                alpine.flow.OperatorManagementUIHelper.selectOperator(operator.uid);
                while((removedItem = pMenu.getChildren().pop()) != null){
                	pMenu.removeChild(removedItem);
                }
            	_buildMenuItem(operator, pMenu, true);
            }
            pMenu.focusFirstChild();
            pMenu.startup();
            
        	pMenu._openMyself({
        		target: bindNode,
        		coords: {
        			x: e.pageX,
        			y: e.pageY
        		}
        	});
        });
        return pMenu;
    };

    function _buildMultipleSelMenu(pMenu){
        if(permissionUtil.checkPermission("OPERATOR_EDIT")){
            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.copy_paste_menu_copy,
                onClick : function() {
                    alpine.flow.CopyManager.copy();
                }
            }));
            pMenu.addChild(new dijit.MenuItem({
        		label : alpine.nls.outputtitlebar_delete,
        		onClick : function() {
        			alpine.flow.OperatorManagementUIHelper.deleteSelectedOperators();
        		}
        	}));
        }
    }

    function _buildMenuItem(operator, pMenu, isRightClick){

        if(operator.operatorType == "DB"){
            _buildDatabaseMenu(operator, pMenu, isRightClick);
        }else{
            _buildHadoopMenu(operator, pMenu, isRightClick);
        }

        if (isRightClick) { /*This should not show in the explore menu*/
            if(permissionUtil.checkPermission("OPERATOR_EDIT")){
                pMenu.addChild(new dijit.MenuSeparator());
                pMenu.addChild(new dijit.MenuItem({
                    label : alpine.nls.copy_paste_menu_copy,
                    onClick : function() {
                        alpine.flow.CopyManager.copy();
                    }
                }));
                pMenu.addChild(new dijit.MenuItem({
                    label : alpine.nls.outputtitlebar_delete,
                    onClick : function() {
                        alpine.flow.OperatorManagementUIHelper.deleteSelectedOperators();
                    }
                }));
            }
        }
    }

    function _buildDatabaseMenu(operator, pMenu, isRightClick){

        if (operator.classname == "ModelOperator") {
            var needModelReplace = false;
            var parents = alpine.flow.OperatorManagementManager.getPreviousOperators(operator.uid);
            // only stand alone model will have the popup menu
            if (!parents || parents.length < 1) {
                needModelReplace=true;
            } else {
                pMenu.addChild(new dijit.MenuItem({
                    label : alpine.nls.step_run,
                    onClick : function() {
                    	runnerHelper.runToOperator(operator);
                    }
                }));
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.clear_step_run_result,
	                disabled: !_ableToShowClearStepRunMenuItem(operator.uid),
	                onClick : function() {
	                	runnerHelper.clearStepRunResult(operator);
	                }
	            }));
            }
            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Replace_Model,
                // disabled: true,
                onClick : function() {
                    showModelReplaceDialog(alpine.flow.WorkFlowManager.getEditingFlow().categories, alpine.flow.WorkFlowManager.getEditingFlow().id, operator.name,needModelReplace,operator.modelType);
                }
            }));
            pMenu.addChild(new dijit.MenuItem({
                label: alpine.nls.onlinehelp_menu_title,
                onClick: function(){
                    alpine.onlinehelp.OnlineHelp.showOperatorHelp(baseURL,operator.classname);
                }
            }));

        } else {

	        //all operator except DBTable get StepRun in RightClick
	        if (operator.classname != "DbTableOperator" && isRightClick) {
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.step_run,
	                onClick : function() {
	                	runnerHelper.runToOperator(operator);
	                }
	            }));
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.clear_step_run_result,
	                disabled: !_ableToShowClearStepRunMenuItem(operator.uid),
	                onClick : function() {
	                	runnerHelper.clearStepRunResult(operator);
	                }
	            }));
	            pMenu.addChild(new dijit.MenuSeparator());
	        }
	        if (needDataExplorMenu(operator) == true) {
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Data_Explorer,
	                // disabled: true,
	                onClick : function() {
                        dataExplorerHelper.showTableData(operator);
	                }
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.show_table_metadata,
	                onClick : function() {
                        dataExplorerHelper.show_table_metadata(operator, constants.CANVAS);
	                }
	            }));
	
	            if ( operator.classname == "DbTableOperator" && isRightClick ) {
	
	                pMenu.addChild(new dijit.MenuItem({
	                    //iconClass:"dijitIconUndo",
	                    label : alpine.nls.dbresource_menu_name,
	                    onClick : function() {
                            alpine.dbconnection.dbMetadataCacheManager.startup();
	                    }
	                }));
	            }
	
	            pMenu.addChild(new dijit.MenuSeparator());
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Scatter_Plot_Chart,
	                onClick : function() {
                        dataExplorerHelper.showScatterChart(operator);
	                }
	            }));
	            //Add by Will for Scat Plot Martix
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Scat_Plot_Martix,
	                onClick : function() {
                        dataExplorerHelper.showScatPlotMartix(operator);
	                }
	            }));
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Univariate_Plot_Chart,
	                onClick : function() {
                        dataExplorerHelper.showUnivariateChart(operator);
	                }
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Box_and_Wisker_Chart,
	                onClick : function() {
                        dataExplorerHelper.showBoxAndWiskerChart(operator);
	                }
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Histogram_Chart,
	                onClick : function() {
                        dataExplorerHelper.showHistogramChart(operator);
	                }
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Bar_Chart,
	                onClick : function() {
                        dataExplorerHelper.showBarChart(operator);
	                }
	            }));
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.TimeSeries_Chart,
	                onClick : function() {
                        dataExplorerHelper.showTimeSeriesChart(operator);
	                }
	            }));
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Summary_Statistics,
	                onClick : function() {
                        dataExplorerHelper.showSummarayStatistics(operator);
	                }
	
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Correlation_Analysis,
	                onClick : function() {
                        dataExplorerHelper.showCorrelationAnalysis(operator);
	                }
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Frequency_Analysis,
	                onClick : function() {
                        dataExplorerHelper.showFrequencyAnalysis(operator);
	                }
	            }));
	
	            if (isRightClick) {
	                pMenu.addChild(new dijit.MenuSeparator());
	            }
	
	        }else if(operator.classname == "PivotOperator"){
	        	pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Data_Explorer,
	                // disabled: true,
	                onClick : function() {
                        dataExplorerHelper.showTableData(operator);
	                }
	            }));
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.show_table_metadata,
	                onClick : function() {
                        dataExplorerHelper.show_table_metadata(operator, constants.CANVAS);
	                }
	            }));
	        }else if (!isRightClick) {
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.no_explore_actions,
	                disabled: true,
	                onClick : function() {
	                    return;
	                }
	            }));
	        }
	
	        if (isRightClick) {
	
	            pMenu.addChild(new dijit.MenuItem({
	                label : alpine.nls.Properties_popup,
	                onClick : function() {
	                    open_property_dialog(operator);
	                }
	            }));
	
	            //add by Will sub-flow Operator
	            if(operator.classname == "SubFlowOperator"){
	                pMenu.addChild(new dijit.MenuItem({
	                    label : alpine.nls.subflow_menu_edit_subflow,
	                    onClick : function() {
	                        rightMenuEditSubFlow(operator);
	                    }
	                }));
	                pMenu.addChild(new dijit.MenuSeparator());
	            }
	            //help menu
	            pMenu.addChild(new dijit.MenuItem({
	                label: alpine.nls.onlinehelp_menu_title,
	                onClick: function(){
                        alpine.onlinehelp.OnlineHelp.showOperatorHelp(baseURL,operator.classname);
	                }
	            }));
	        }
        }
    }

    function _buildHadoopMenu(operator, pMenu, isRightClick){

        //all operator except HadoopFile get StepRun in RightClick
        if (operator.classname != "HadoopFileOperator" && isRightClick) {
            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.step_run,
                onClick : function() {
                	runnerHelper.runToOperator(operator);
                }
            }));
            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.clear_step_run_result,
                disabled: !_ableToShowClearStepRunMenuItem(operator.uid),
                onClick : function() {
                	runnerHelper.clearStepRunResult(operator);
                }
            }));
        }

        if (_needsHadoopDataExplorer(operator)) {
            if (operator.classname != "HadoopFileOperator" && isRightClick) {
                pMenu.addChild(new dijit.MenuSeparator());
            }

            pMenu.addChild(new dijit.MenuItem({
                label:alpine.nls.hadoop_prop_right_menu_file_explorer,
                onClick:function(){
                    alpine.flow.HadoopOperatorsDataExplorerHelper.show_hadoop_data_explorer(operator);
                }
            }));

            pMenu.addChild(new dijit.MenuSeparator());

            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Scat_Plot_Martix,
                onClick : function() {
                    dataExplorerHelper.showScatPlotMartix(operator);
                }
            }));

            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Bar_Chart,
                onClick : function() {
                    dataExplorerHelper.showBarChart(operator);
                }
            }));
            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Box_and_Wisker_Chart,
                onClick : function() {
                    dataExplorerHelper.showBoxAndWiskerChart(operator);
                }
            }));

            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Histogram_Chart,
                onClick : function() {
                    dataExplorerHelper.showHistogramChart(operator);
                }
            }));

            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Frequency_Analysis,
                onClick : function() {
                    dataExplorerHelper.showFrequencyAnalysis(operator);
                }
            }));

            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Summary_Statistics,
                onClick : function() {
                    dataExplorerHelper.showSummarayStatistics(operator);
                }
            }));

        }
        if (isRightClick) {
            pMenu.addChild(new dijit.MenuSeparator());

            //edit operator properties
            pMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.Properties_popup,
                onClick : function() {
                    open_property_dialog(operator);
                }
            }));
            //help menu
            pMenu.addChild(new dijit.MenuItem({
                label: alpine.nls.onlinehelp_menu_title,
                onClick: function(){
                    alpine.onlinehelp.OnlineHelp.showOperatorHelp(baseURL,operator.classname);
                }
            }));
        }
    }


    function _getSelectedOperatorMenu() {
            var pMenu = new dijit.Menu({});
            _buildMenuItem(alpine.flow.OperatorManagementUIHelper.getSelectedOperator(), pMenu, false) ;
           return pMenu;
    }
    
    function _needDatabaseDataExplorer(operatorPrimaryInfo){
    	if (operatorPrimaryInfo.hasDbTableInfo == true || operatorPrimaryInfo.classname == "PivotOperator") {
            return true;
        }
        return false;
    }

    function _needsHadoopDataExplorer(operator) {
        if (operator.classname == "HadoopFileOperator" || 
    		operator.classname == "CopytoHadoopOperator" ||
    		operator.classname == "HadoopLogisticRegressionPredictOperator" ||
            operator.classname == "HadoopLinearRegressionPredictOperator" ||
            operator.classname == "HadoopNaiveBayesPredictOperator" ||
            operator.classname == "HadoopKmeansOperator" ||
            operator.classname == "HadoopSampleSelectorOperator" ||
            operator.classname == "HadoopPigExecuteOperator" ||
            (
        		(
	                operator.classname == "HadoopAggregateOperator" ||
	                operator.classname == "HadoopVariableOperator" ||
	                operator.classname == "HadoopRowFilterOperator" ||
	                operator.classname == "HadoopUnionOperator" ||
	                operator.classname == "HadoopJoinOperator" ||
	                operator.classname == "HadoopReplaceNullOperator" ||
	                operator.classname == "HadoopNormalizationOperator"||
	                //operator.classname == "HadoopSampleSelectorOperator" ||
	                operator.classname == "HadoopPivotOperator" ||
	                operator.classname == "HadoopColumnFilterOperator" ||
	                operator.classname == "SubFlowOperator"

                ) && operator.storeResult == true
             )){
            return true;
        } else {
            return false;
        }
    }

    function _genericNeedsDataExplorer(operatorPrimaryInfo) {
        var opTypeCheck = operatorPrimaryInfo.operatorType;
        if (operatorPrimaryInfo.className == "CopyToDBOperator") opTypeCheck = "DB"; //Hadoop operator but outputs a table
        if (operatorPrimaryInfo.className == "CopyToHadoopOperator") opTypeCheck = "HADOOP"; //DB but outputs a Hadoop file
        if (opTypeCheck == "DB" ) {
            return _needDatabaseDataExplorer(operatorPrimaryInfo);
        } else {
            return _needsHadoopDataExplorer(operatorPrimaryInfo);
        }
    }
    
    function _ableToShowClearStepRunMenuItem(currentOperatorUid){
    	return runnerHelper.canBeCleanRunResult(currentOperatorUid);
    }

    return {
        addOperatorMenu:addOperatorMenu,
        getSelectedOperatorMenu:_getSelectedOperatorMenu,
        genericNeedsDataExplorer:_genericNeedsDataExplorer,
        setupBlankMenu: _setupBlankMenu,
        releaseBlankMenu: _releaseBlankMenu
    };
});