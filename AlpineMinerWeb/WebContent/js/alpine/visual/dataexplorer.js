
//TODO:de_refrecen_column_delect_id is used in property.js and DataExplorerHelper.js
var de_refrecen_column_delect_id="de_refrecen_column_select_id";


//TODO: getDBInfo is used in FindandreplaceparameterHelper.js and DataExplorerHelper.js
function getDBInfo(operator){

	var dbinfo={};
	if(operator.classname=="StratifiedSamplingOperator"
		||operator.classname=="RandomSamplingOperator"){
		return dbinfo;
	}
	var modifySource =false;

	if(!dbinfo.connection){
		dbinfo.connection=getParentPropValueRecrusively(operator,"dbConnectionName");
	}
	//the original one, not changed...
	if(!dbinfo.connection){
		dbinfo.connection=operator.connectionName;
	}

	
	if(!dbinfo.schema){
		dbinfo.schema = operator.outputSchema;
	}
	
	if(!dbinfo.table){
		dbinfo.table = operator.outputTable;
	}

	//get dbconnection and schema from parent
	if(operator.classname=="IntegerToTextOperator"&&modifySource==true){
		delete 	dbinfo.schema;
		delete 	dbinfo.table;
	}
	if(!dbinfo.schema){
		dbinfo.schema=getParentPropValueRecrusively(operator,"schemaName");
	}
	
	if(!dbinfo.schema){
		dbinfo.schema=getParentPropValueRecrusively(operator,"outputSchema");
	}
	
 
	
	//this is for table join use
	dbinfo.opuid=operator.uid;
	
	if (dbinfo.table instanceof Array){
		dbinfo.table=dbinfo.table[dbinfo.table.length - 1];//get last one in the list, because the last table info is output table in sub flow.
	} 
	
	if (dbinfo.schema instanceof Array){
		dbinfo.schema=dbinfo.schema[dbinfo.schema.length - 1];
	}
	
	return dbinfo;
}


//TODO: getParentPropValueRecrusively is called by getDBInfo in dataexplorer.js
function getParentPropValueRecrusively(operator,param_name){
//	var parents = getParanetOperators(operator);
	var parents = alpine.flow.OperatorManagementManager.getPreviousOperators(operator.uid);
	if(!parents){
		return null;
	}
	for(var i=0;i<parents.length;i++){
		var value=getPropValue(parents[i],param_name);
		if(!value){
			value=getParentPropValueRecrusively(parents[i],param_name);
		}
		if(value){
			return value;
		}
	}
	return null;

    function getPropValue(operator,param_name){
        if(operator.operatorDTO&&operator.operatorDTO.propertyList){
            var propertyArray=operator.operatorDTO.propertyList;
            for ( var x = 0; x < propertyArray.length; x++) {
                var prop=propertyArray[x];
                //prop could be null
                if (prop&&prop.name==param_name){
                    return prop.value;
                }
            }
        }
        return null;
    }
}

function getVisualizationModel(requestUrl,containerID){
	//progress bar
	//show_progress_bar_dilog();
 //   require(["alpine/spinner"], function(spinner){
    ds.get(requestUrl, function(data)
    {
        if(!data.error_code || data.error_code == 0){
            showVislualizationModel(data,containerID);

        } else
        {
            var msg = data.message;
            if(!msg){
                msg = alpine.nls.message_unknow_error;
            }
            popupComponent.alert(msg,function(){
                window.close();
            });

        }
        //append pane..
        //then create the datagrid...
    },function(){
    	window.close();
    } , false, containerID);
}

function showVislualizationModel(data,containerID){ 
	//var contentDiv= document.createElement(TAG_DIV);
	
	var  contentDiv =dojo.byId(containerID);
//	div.appendChild(contentDiv);
//	div.style.width="100%";
//	div.style.height="100%";	
	contentDiv.style.width="100%";
	contentDiv.style.height="100%";	
	
	var contentDivPane=    new dijit.layout.ContentPane({	
	} ,contentDiv);
	contentDivPane.style.width="100%";
	contentDivPane.style.height="100%";
	contentDivPane.preload=true;
 
	// new tab container in outpane
	var tabContainer = new dijit.layout.TabContainer({
//		id:TAG_PREFIX_OUTPUT_CHILD+data.out_id,
		style : "height: 100%; width: 100%;"
	}, document.createElement(TAG_DIV));
	//keep the order is important...
    //MINERWEB-1107 add by Will
    LOGISTIC_REGRESSION_DB_CLICK_TAB_CONTAINER = tabContainer.id;
    LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER = tabContainer.id;

	contentDivPane.setContent(tabContainer.domNode);

		
	if(data.visualType==VISUAL_TYPE_COMPOSITE){
		var firstPane=null;
		var subOutPut = data.visualData;
		tabContainer.chartData =data;
		for ( var x = 0; x < subOutPut.length; x++) {
			var output = subOutPut[x];
			if(x == 0){//avoid show the progress twice
				output.is_firt_comp_tab=true;
				}
			var subpane = createSubTab4CompositeOut(tabContainer, output.out_id + "_" + x, output.out_title, output);
			tabContainer.addChild(subpane);
			subpane.style.width="100%";
			subpane.style.height="100%";
			subpane.preload = false;
			tabContainer.startup();
			contentDivPane.startup();
			if(x == 0){//avoid show the progress twice
				fillOutPutPan(subpane,output );
			}
			if(x==0){
				firstPane=subpane;
			}
			//tabContainer.selectChild(subpane);
		
		}
		//tabContainer.selectChild(firstPane);
	}else{
		data.is_firt_comp_tab=true;
		var subpane = createSubTab4CompositeOut(tabContainer, data.out_id + "_" + 0, data.out_title, data);
		//avoid show the progress twice
		
		tabContainer.addChild(subpane);
		subpane.style.width="100%";
		subpane.style.height="100%";
		subpane.preload = true;
		tabContainer.startup();
		contentDivPane.startup();
		//only one tab, so have to fill it
		fillOutPutPan(subpane,data );

	}
}