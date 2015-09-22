function fillOutPaneScatterMatrix(outpane, output) {
 
	var chartData = output.visualData;
	  
	var table = document.createElement('table');
	table.setAttribute('border', 1);
	table.setAttribute('cellspacing', "0");
	var tbody = document.createElement("tbody");
	
	table.appendChild(tbody);
	if(dojo.isIE){
		//var contentDiv = document.createElement(TAG_DIV);
		//contentDiv.appendChild(table) ;
		outpane.setContent(table);
	}else{
		outpane.setContent(table);
	}
	 
  
	var models = chartData.models;
	
	var tableGroupCellType = new Array();
	var tableData =  new Array();
	

	//   
	for ( var i = 0; i < models.length; i++) {
		// row list of models, each cell is a visual model
		var rowTypeArray = new Array();
		var rowDataArray= new Array();
		var rowModelList = models[i];
		createTableRow4ScatterMatrixChart(rowModelList ,tbody,rowDataArray,rowTypeArray,i,models);
		tableGroupCellType.push(rowTypeArray);
		tableData.push(rowDataArray);
		//tbody.appendChild(tr);
	}
	 
	
	
//	output.tableGroupColumnType = tableGroupColumnType;//text,svg
	output.tableGroupCellType = tableGroupCellType;//text,svg
	output.tableData = tableData;
	
	//MINERWEB-1035 	Scatter plot matrix font size improvement 
	//_buildNumberTextStyle();
}
 

function createTableRow4ScatterMatrixChart(rowModelList,tbody,rowDataArray,rowTypeArray,i,models) {
	var tr = document.createElement('tr');
	tbody.appendChild(tr);

	//i is the legend label..

	for ( var j = 0; j < rowModelList.length; j++) {

		var td = document.createElement('td');
		td.setAttribute("width", 120);
		td.setAttribute("height", 100);
		tr.appendChild(td);
		var contentDiv = document.createElement(TAG_DIV);		
		
		td.appendChild(contentDiv);
		if(rowModelList[j].visualizationType==VISUAL_TYPE_TEXT){
			var textValue= rowModelList[j].text;
			var textNumber;
			var accuracy = CONST_scatterMatrixAccuracy!=null?CONST_scatterMatrixAccuracy:4;
			if(i!=j){
					try{
						textNumber = parseFloat(textValue).toFixed(accuracy);
						contentDiv.innerHTML=(textNumber)?textNumber:textValue;
						//if(dojo.hasClass(contentDiv,'scatter-matric-text')==false){
						//	   dojo.addClass(contentDiv,'scatter-matric-text');
						//}
					}catch(e){
					}
					//td.title= "Correlation between \""+models[i][i].text+"\" and \""+models[j][j].text+"\":"+textValue;
			        var tempchar = alpine.nls.scatter_plot_matrix_txt_tooltip.replace("#1#","\""+models[i][i].text+"\"");
			            tempchar = tempchar.replace("#2#","\""+models[j][j].text+"\"")+rowModelList[j].text;
			        td.title = tempchar;
			}
			if(i==j){
                   if(dojo.isFF){
                      // if(textValue!=null && textValue.length>18){
                      //     contentDiv.innerHTML=textValue.substring(0,15)+"...";
                      // }
                      dojo.style(contentDiv,{
                          wordWrap:"break-word",
                          wordBreak:"break-all",
                          display:"block"
                      });

                   }else{
                       dojo.style(contentDiv,{
                           wordWraprap:"break-word",
                           wordBreak:"break-all"
                       });
                   }
                    contentDiv.innerHTML=textValue;
					td.title = textValue;
			}
			
		
			
			contentDiv.style.textAlign="center";			
			
			rowTypeArray.push("text");
			rowDataArray.push(textValue);
		}else{
			if(i>j){
				//td.title = "Scatter Plot between [\""+models[j][j].text+"\" , \""+models[i][i].text+"]\"." +
				//		" Click to see full size image.";	
			     var tempchar = alpine.nls.scatter_plot_matrix_img_tooltip.replace("#1#","\""+models[j][j].text+"\"");
		            tempchar = tempchar.replace("#2#","\""+models[i][i].text+"\"");
		        td.title = tempchar+alpine.nls.scatter_plot_matrix_img_click_tooltip;
				
			}
			rowTypeArray.push("svg");
			var output={
					visualType :rowModelList[j].visualizationType,
					visualData:rowModelList[j]
			};
			//output = {"pointGroups":[{"color":"","label":"","points":[{"x":"1.7318016E7","y":"1.0"},{"x":"1.790388E7","y":"2.0"},{"x":"1.7726822E7","y":"3.0"},{"x":"1.7140372E7","y":"4.0"},{"x":"1.7618738E7","y":"5.0"},{"x":"1.6064012E7","y":"6.0"},{"x":"1.8041844E7","y":"7.0"},{"x":"2.088764E7","y":"8.0"},{"x":"1.8548682E7","y":"9.0"},{"x":"1.7986072E7","y":"10.0"},{"x":"1.6920922E7","y":"11.0"},{"x":"1.7076984E7","y":"12.0"},{"x":"1.8464764E7","y":"13.0"},{"x":"1.8613604E7","y":"14.0"},{"x":"1.9544666E7","y":"15.0"},{"x":"1.781964E7","y":"16.0"},{"x":"160075.0","y":"17.0"},{"x":"1.9076746E7","y":"18.0"},{"x":"2.0181738E7","y":"19.0"},{"x":"1.6881742E7","y":"20.0"},{"x":"1.6943696E7","y":"21.0"},{"x":"1.896772E7","y":"22.0"},{"x":"1.8198388E7","y":"23.0"},{"x":"1.5987315E7","y":"24.0"},{"x":"1829841.0","y":"25.0"},{"x":"1.9008536E7","y":"26.0"},{"x":"1.8451934E7","y":"27.0"},{"x":"1.8376308E7","y":"28.0"},{"x":"1.7862056E7","y":"29.0"},{"x":"1.7744674E7","y":"30.0"},{"x":"1.8396116E7","y":"31.0"},{"x":"1.5966832E7","y":"32.0"},{"x":"1.942722E7","y":"33.0"},{"x":"1.8356212E7","y":"34.0"},{"x":"1.8910096E7","y":"35.0"},{"x":"1.8522956E7","y":"36.0"},{"x":"1.6884244E7","y":"37.0"},{"x":"1.8702028E7","y":"38.0"},{"x":"1.9642884E7","y":"39.0"},{"x":"1.6917918E7","y":"40.0"},{"x":"1760868.0","y":"41.0"},{"x":"1.8778624E7","y":"42.0"},{"x":"1.8620196E7","y":"43.0"},{"x":"1831435.0","y":"44.0"},{"x":"1840598.0","y":"45.0"},{"x":"1.8175328E7","y":"46.0"},{"x":"1.8280258E7","y":"47.0"},{"x":"1.8191168E7","y":"48.0"},{"x":"1.7872704E7","y":"49.0"},{"x":"1.8437544E7","y":"50.0"},{"x":"1.7074528E7","y":"51.0"},{"x":"1706108.0","y":"52.0"},{"x":"1.6893336E7","y":"53.0"},{"x":"1.9548704E7","y":"54.0"},{"x":"1.7869044E7","y":"55.0"},{"x":"1740433.0","y":"56.0"},{"x":"1.8705688E7","y":"57.0"},{"x":"2.1365256E7","y":"58.0"},{"x":"2.033586E7","y":"59.0"},{"x":"1.7293048E7","y":"60.0"},{"x":"1.7790132E7","y":"61.0"},{"x":"1.7451988E7","y":"62.0"},{"x":"1.8242548E7","y":"63.0"},{"x":"1.7200024E7","y":"64.0"},{"x":"1.7679986E7","y":"65.0"},{"x":"1.9612896E7","y":"66.0"},{"x":"1.8744594E7","y":"67.0"},{"x":"1.7444412E7","y":"68.0"},{"x":"1.7622864E7","y":"69.0"},{"x":"1.8909644E7","y":"70.0"},{"x":"1.8163632E7","y":"71.0"},{"x":"1.7656568E7","y":"72.0"},{"x":"1.8393084E7","y":"73.0"},{"x":"1815391.0","y":"74.0"},{"x":"2.0471602E7","y":"75.0"},{"x":"1.8025548E7","y":"76.0"},{"x":"1.8852594E7","y":"77.0"},{"x":"1.6839654E7","y":"78.0"},{"x":"1.9284108E7","y":"79.0"}]}]};
			var surface =fillOutPaneScatterPreview(contentDiv, output); 
			rowDataArray.push(surface);
			//fillOutPaneScatterPreview(contentDiv,output);
		}
	}
}
 
 


function fillOutPaneScatterPreview(outpane,output){
	
	var chartData = output.visualData;
	
	outpane.style.width="118px";
	outpane.style.height="98px";
	outpane.style.background="#ffffff";
	outpane.style.cursor="pointer";
	//var chartContent = dojo.create("div",{style:"width:100%;height:100%;cursor:pointer;overflow:visible;background:#FFFFFF"});
	outpane.innerHTML = "";
	var nodeTitle = "Scatter Plot Matrix ";
	if(outpane.parentNode!=null && outpane.parentNode.nodeName!=null && outpane.parentNode.nodeName.toUpperCase()=="TD" &&  outpane.parentNode.title!=null){
		nodeTitle = outpane.parentNode.title;
	}
	//outpane.appendChild(chartContent);
	
	 dojo.connect(outpane,"click",function(){
		  dijit.byId("scattermatrixchartdlg").show();
		  dijit.byId("scattermatrixchartdlg").set("title",nodeTitle.replace(alpine.nls.scatter_plot_matrix_img_click_tooltip,""));
		  
		  var contentDiv = document.createElement(TAG_DIV);
		  contentDiv.style.height = "500px";
		  contentDiv.style.width = "800px";
		  contentDiv.style.overflow = "visible";
		  dojo.place(contentDiv,dijit.byId("scattermatrixchartContainer").domNode,"only");
		  if(dijit.byId("scattermatrixchartdlg").containerNode!=null){
			  dijit.byId("scattermatrixchartdlg").containerNode.style.overflow = "visible";
		  }
		  var outpane = new dijit.layout.ContentPane({style:"width:800px,height:500px;overflow:visible;overflow:visible;"}, contentDiv);
		  output.visualData.width="800px";
		  output.visualData.height="500px";		  
		  fillOutPaneScatter4ScatterPlotMatrix(outpane, output);
	});
	
		//outpane.set("content",contentDiv);
		
//			outpane.appendChild(contentDiv);
//			var xUnits=chartData.xAxisUnits;
//			var yUnits=chartData.yAxisUnits;
			
			var pointGroups =chartData.pointGroups;
			var chartDiv= document.createElement(TAG_DIV);
			chartDiv.style.width="100%";
			chartDiv.style.height="100%";
			
			outpane.appendChild(chartDiv);
			//var lengendDiv= document.createElement(TAG_DIV);
			
			var buildPointStyleFn;
			
		
			
			var scatterchart = new dojox.charting.Chart(chartDiv, {
				
			//	title: chartData.description, 
				titlePos: "top", 
				//titleGap: 10,
				titleFont: "normal normal normal 12pt Arial",
				titleFontColor: "orange"
			});
			
			
			  
				buildPointStyleFn = function(index){
					return {plot: "default"};
				};
		 
		 
			scatterchart.setTheme(dojox.charting.themes.Bahamation);
			scatterchart.theme.chart.fill=[245,245,245];
			scatterchart.theme.plotarea.fill=[245,245,245];
			
			//use this to control the grid line... 
			scatterchart.theme.axis.majorTick={color:"grey",width:0.5,length:6};
			scatterchart.theme.axis.minorTick={color:"white",width:0.5,length:3};
			
			scatterchart.addPlot("default", {type: "Scatter",markers: true});
			
			
			if(pointGroups){
				for ( var i = 0; i < pointGroups.length; i++) {
					addScatterSeries(pointGroups[i],scatterchart, buildPointStyleFn(i)) ;
				}
			 	 	
			}
			var visualLines =chartData.visualLines;
			
			if(visualLines){
				//this is for line
				scatterchart.addPlot("lines", {type: "Lines",markers: false  });
				
				for ( var i = 0; i < visualLines.length; i++) {
			 		scatterchart.addSeries(visualLines[i].label+"_line", visualLines[i].points , {plot: "lines"});
					 
				}
		  	 	
			}
			
			if(pointGroups){
				try{
					scatterchart.render(); 
				}catch(e){
					console.log(e);
				}
			}
			//output.dojo_surface = scatterchart.surface;
			return scatterchart.surface;
 
}
/*
function _buildNumberTextStyle(){
	var textLabes = dojo.query(".scatter-matric-text");
	var textNumbers = [];
	if(null!=textLabes && textLabes.length>1){
		for ( var i = 0; i < textLabes.length; i++) {
			var textLabel = textLabes[i];
			try {
				textNumbers.push(parseFloat(dojo.trim(textLabel.innerHTML)));
			} catch (e) {
				
			}
		}
		textNumbers.sort();
		var maxNum=0,minNum=0;
		if(textNumbers.length>1){
			maxNum = textNumbers[textNumbers.length-1];
			minNum = textNumbers[0];
		}
		
		dojo.forEach(textLabes,function(itm,idx){
			if(itm.innerHTML!=null && itm.innerHTML==maxNum){
				dojo.addClass(itm,"bigger");
			}
			if(itm.innerHTML!=null && itm.innerHTML==minNum){
				dojo.addClass(itm,"smaller");
			}
			
		});
		
	
	}
}
*/