
function fillOutPaneDataTable(outpane,output,type){
 
	
	var dataTable=output.visualData;
    if(type!=null && type=="xml"){
        dataTable = output.visualData.visualData;
    }
	if(!dataTable){
		return ;
	}
	//column array
	var columns = dataTable.columns;
    //fix column naem is js`s reserved keyword
    //_fixReservedKeyword(columns);

	var compareMap = [];
	
	var layout= new Array();
	//build the dynamic table header	
	for ( var x = 0; x < columns.length; x++) {
		var column = new Object();
		column.name = columns[x];
		column.field = columns[x];
		//column.width=
		layout[x] = column; 
		compareMap[column.field] = visualGridCompare;
	};
	//cast the type for sorting
    dataTable.columnTypes = fixDataType4Hadoop(dataTable.columnTypes,dataTable);
    var columnTypes = dataTable.columnTypes;
	if(columnTypes&&columnTypes.length>0&&dataTable&&dataTable.items&&dataTable.items.length>0){
		var columnIndex= new Array();
		for(var i =0;i<columns.length;i++){
			columnIndex[columns[i]]= i;
		}
		var rows =dataTable.items;
		var type =null;
		var name = null;
		for(var i = 0 ;i<rows.length;i++){
			var row = rows[i] ;
			for(var j = 0 ;j<columns.length;j++){
				 name = columns[j]; 
				 type = columnTypes[columnIndex[name]];
				 // avoid the nullpoint in gson
				  if(type == "number"){
					  
					  if(row[name]){
						  if("N/A"!=row[name]){
							 if(Infinity ==row[name]){
								  row[name] = "Infinity";  //MINERWEB_562 /589
							  }else if(-Infinity ==row[name]){
								  row[name] = "-Infinity";  //MINERWEB_562 /589
							  }else  if(row[name]){
								  if(row[name]==""){
									  
								  }else{
									  if(true==isNaN(row[name])){
										  row[name] ="N/A";
									  }
								  }
							  }  
							  else{
								  row[name] = "N/A";  //MINERWEB_562
							  }
						  }else{
							  row[name] = "N/A";  //MINERWEB_562
						  }
					  }else{
						  row[name] = "N/A";
					  }
				  }else{
					  if(!row[name] || typeof(getValue(row[name]))!="string"){
						  row[name] = "N/A";
					  }
				  }
				  //avoid null point in IE
				  if(type != "number"&&row[name]==""){//MINERWEB_647  0 = "" 
					  row[name]=" ";
				  }
			}
			
		}
	}
	
	
	//data is the json data for the table (must have an array named 'items')
	var model    = new dojo.data.ItemFileReadStore({
		data: dataTable
	});
 
	model.comparatorMap = compareMap;
	
	var gridDomNode = document.createElement("div",{style:"height:100%;width;100%"}) ;

    outpane.setContent(gridDomNode);

	
	var grid = new dojox.grid.DataGrid({
	    id:TAG_PREFIX_OUTPUT_CHILD+ new Date().getTime()+Math.random(),					  
	    store: model,
	    structure: layout
	},   gridDomNode );
	
	
	
	output.dojo_widget = grid;
	
	grid.domNode.style.height="100%";
	outpane.style.height="100%";
	grid.startup();
	grid.resize();
}

function getValue(value){
    return dojo.isArray(value) ? value[0] : value;
}

//function _fixReservedKeyword(columns){
//    if(null!=columns){
//        for(var i=0;i<columns.length;i++){
//         if(dojo.indexOf(["length"],columns[i])!=-1){
//             //columns[i] = columns[i]+"_";
//             columns[i] = columns[i].toUpperCase();
//          }
//        }
//    }
//}

function fixDataType4Hadoop(columnTypes,dataTable){
    var newTypes = [];
    var reg = /^[-]?\d+(\.\d+)?$/;
    var regPercent = /^\d+(\.\d+)?%$/;
    if(null!=columnTypes && columnTypes.length>0){
        for(var i=0;i<columnTypes.length;i++){
                var dataItems = dataTable.items;
                if(null!=dataItems && dataItems.length>0){
                    var rowTypes = [];
                    for(var j=0;j<dataItems.length;j++){
                        var dataItem = dataItems[j];
                        var columnName = dataTable.columns[i];
                        if(reg.test(dataItem[columnName])==true){
                            rowTypes.push("number");
                        }else if(regPercent.test(dataItem[columnName])==true){
                            //add new type
                            rowTypes.push("percentage");
                        }else{
                            rowTypes.push("string");
                        }
                    }
                   if(dojo.indexOf(rowTypes,"string")!=-1){
                       newTypes.push("string");
                   }else if(_allIsGiveType(rowTypes,"percentage")==true){
                       newTypes.push("percentage");
                   }else if(_allIsGiveType(rowTypes,"number")==true){
                       newTypes.push("number");
                   }else{
                       newTypes.push("string");
                   }
                }else{
                    newTypes.push("string");
                }
        }
    }
    return newTypes;
}

function _allIsGiveType(rowTypes,giveType){
    if(null==rowTypes || rowTypes.length==0){
        return false;
    }
    for(var i=0;i<rowTypes.length;i++){
        if(rowTypes[i]!=giveType){
            return false;
        }
    }
    return true;
}


function fillOutPaneDataTable4GridCanDBClick(outpane,output){

    var visualType = output.visualType;
    var dataTable=output.visualData;
    if(!dataTable){
        return ;
    }

    //column array
    var columns = dataTable.columns;
    var compareMap = [];

    var layout= new Array();
    //build the dynamic table header
    for ( var x = 0; x < columns.length; x++) {
        var column = new Object();
        column.name = columns[x];
        column.field = columns[x];
        //column.width=
        layout[x] = column;
        compareMap[column.field] = visualGridCompare;
    };
    //cast the type for sorting
    dataTable.columnTypes = fixDataType4Hadoop(dataTable.columnTypes,dataTable);
    var columnTypes = dataTable.columnTypes;
    if(columnTypes&&columnTypes.length>0&&dataTable&&dataTable.items&&dataTable.items.length>0){
        var columnIndex= new Array();
        for(var i =0;i<columns.length;i++){
            columnIndex[columns[i]]= i;
        }
        var rows =dataTable.items;
        var type =null;
        var name = null;
        for(var i = 0 ;i<rows.length;i++){
            var row = rows[i] ;
            for(var j = 0 ;j<columns.length;j++){
                name = columns[j];
                type = columnTypes[columnIndex[name]];
                // avoid the nullpoint in gson
                if(type == "number"){

                    if(row[name]){
                        if("N/A"!=row[name]){
                            if(Infinity ==row[name]){
                                row[name] = "Infinity";  //MINERWEB_562 /589
                            }else if(-Infinity ==row[name]){
                                row[name] = "-Infinity";  //MINERWEB_562 /589
                            }else  if(row[name]){
                                if(row[name]==""){

                                }else{
                                    row[name] = row[name]*1;
                                    if(true==isNaN(row[name])){
                                        row[name] ="N/A";
                                    }
                                }
                            }
                            else{
                                row[name] = "N/A";  //MINERWEB_562
                            }
                        }else{
                            row[name] = "N/A";  //MINERWEB_562
                        }
                    }else{
                        row[name] = "N/A";
                    }
                }else{
                    if(!row[name]){
                        row[name] = "N/A";
                    }
                }
                //avoid null point in IE
                if(type != "number"&&row[name]==""){//MINERWEB_647  0 = ""
                    row[name]=" ";
                }
            }

        }
    }


    //data is the json data for the table (must have an array named 'items')
    var model    = new dojo.data.ItemFileReadStore({
        data: dataTable
    });

    model.comparatorMap = compareMap;


    var gridDomNode = dojo.create("div",{style:"height:100%;overflow:hidden"});
    var grid = new dojox.grid.DataGrid({
        id:TAG_PREFIX_OUTPUT_CHILD+ new Date().getTime()+Math.random(),
        store: model,
        structure: layout,
        selectionMode: "single",
        style:"height:100%;width:100%",
        onRowDblClick:function(e){
            //var sel = this.selection.getSelected();
            var rowNode = this.getItem(e.rowIndex);
            if(rowNode[alpine.nls.Logistic_Regression_Group_By_Value]!=null){
                var key = rowNode[alpine.nls.Logistic_Regression_Group_By_Value][0];
                //if(visualType==199){
                    getRegression_GroupBy_Module(key,visualType,null,null,true)
               // }else if(visualType==200){
                  // getLinearRegression_GroupBy_Module(key,null,null,true);
                //}
            }



        }
    },   dojo.create("div",{style:"overflow:hidden"},gridDomNode));

    var buttonDOMNode = dojo.create("div",{style:"height:50px;"});
    //show data button
    var showDataDetailBtn =  new dijit.form.Button({
        label: "Show detail",
        baseClass:"dialogButton",
        onClick: function(){
            if(null!=grid){
                var selectItem = grid.selection.getSelected();
                if(selectItem.length>0){
                    var key = selectItem[0][alpine.nls.Logistic_Regression_Group_By_Value][0];
                    getRegression_GroupBy_Module(key,visualType,null,null,true)
                }else{
                    popupComponent.alert(alpine.nls.Logistic_Regression_Group_By_Result_Select_Tip);
                }
            }
        }
    }, dojo.create("div",{style:"height:30px;"},buttonDOMNode));


    var textPaneContainer = dojo.create("div",{style:"overflow:hidden"});
    dojo.place(buttonDOMNode,textPaneContainer,"last");
    dojo.place(gridDomNode,textPaneContainer,"last");



    outpane.setContent(textPaneContainer);

    outpane.chartData = output;
    outpane.preload = true;
    outpane.isRendered = false;


    output.dojo_widget = grid;

    grid.domNode.style.height="100%";
    outpane.style.height="100%";
    outpane.domNode.style.overflow = "hidden";
    grid.startup();
    grid.resize();


        //get max=500 Logistic Regression data to download result
        _addLogisticRegression_GroupBy_DataResult(visualType);


}

function _addLogisticRegression_GroupBy_DataResult(visualType){
    var url;
    if(visualType==199){
       url = baseURL+"/main/visualization/utility.do?method=getLogisticRegressionGroupMode";
    }else if(visualType==200){
        url =  baseURL+"/main/visualization/utility.do?method=getLinearRegressionGroupMode";
    }
    ds_result.get(url,function(data){
        var tabWidgetId;
        if(visualType==199){
            tabWidgetId = dijit.byId(LOGISTIC_REGRESSION_DB_CLICK_TAB_CONTAINER).get('id');
        }
        if(visualType==200){
            tabWidgetId = dijit.byId(LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER).get('id');
        }

        if(null!=tabWidgetId){
            for(var i=0;i<flow_outputs.length;i++){
                if(null!=tabWidgetId && tabWidgetId.indexOf(flow_outputs[i].output.out_id)!=-1){
                    if(null!=data && data.length>0){
                        for(var j=0;j<data.length;j++){
                            flow_outputs[i].output.visualData.push(eval("("+data[j]+")"));
                        }
                    }

            }
            }
        }
    },null,true);

}

function getRegression_GroupBy_Module(key,visualType,succFunc,errorFunc,sync){
    var url;
    if(199==visualType){
         url = baseURL+"/main/visualization/utility.do?method=getLogisticRegressionGroupModelByKey&key="+key;
    }else if(200==visualType){
         url = baseURL+"/main/visualization/utility.do?method=getLinearRegressionGroupModelByKey&key="+key;
    }
    ds_result.get(url,function(data){
        var tab_id = (""+key).replace(/\s/g,"_")+"_tab";
        var tab_widget = dijit.byId(tab_id);
        //addResultTab(tab_id, "", data);
        if(null==tab_widget){
            tab_widget = new dijit.layout.ContentPane({
                id:tab_id,
                title : data.out_title,
                closable : sync
            });
            //
            tab_widget.chartData = data;
            tab_widget.preload = true;
            tab_widget.isRendered = false;
            data.parentOutPut = {};
            data.parentOutPut.contentPaneId = tab_id;

            fillOutPutPan(tab_widget,data);
            if(visualType==199){
                dijit.byId(LOGISTIC_REGRESSION_DB_CLICK_TAB_CONTAINER).addChild(tab_widget);
                dijit.byId(LOGISTIC_REGRESSION_DB_CLICK_TAB_CONTAINER).selectChild(tab_widget,true);
            }

            if(visualType==200){
                dijit.byId(LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER).addChild(tab_widget);
                dijit.byId(LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER).selectChild(tab_widget,true);
            }

        }else{
            if(visualType==199){
                dijit.byId(LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER).selectChild(tab_widget,true);
            }

            if(visualType==200){
                dijit.byId(LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER).selectChild(tab_widget,true);
            }

        }
    },function(){},true);
}


function visualGridCompare(a,b){
	var r = -1;
	if(a === null){
		a = undefined;
	}
	if(b === null){
		b = undefined;
	}
    if(a == "N/A" ){
        a = "0";
    }
    if(b == "N/A" ){
        b = "0";
    }
    //add percentage sort
    if(a!=null && b!=null){
        var regPercent = /^\d+(\.\d+)?%$/;
        if(regPercent.test(a)==true && regPercent.test(b)==true){
            var reg = /%$/g;
            a = parseFloat(a.replace(reg,""));
            b = parseFloat(b.replace(reg,""));
        }
        var numberReg1 = /^[+-]?\d+(\.\d+)?$/;
        var numberReg2 = /^[+-]?\d+(\.\d+[E|e][+-]?\d+)?$/;
        if((numberReg1.test(a)==true || numberReg2.test(a)==true) && (numberReg1.test(b)==true || numberReg2.test(b)==true)){
            a = parseFloat(a);
            b = parseFloat(b);
        }

    }
	if(a == b){
		r = 0;
	}else if( a > b || a == null){
		r = 1;
	}
	return r; //int {-1,0,1}
}