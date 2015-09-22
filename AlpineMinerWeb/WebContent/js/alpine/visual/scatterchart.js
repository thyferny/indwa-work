
function fillOutPaneScatter( outpane,output){
 
	
	var chartData=output.visualData;
	var contentDiv= document.createElement(TAG_DIV);
	outpane.setContent(contentDiv);
	var xUnits=chartData.xAxisUnits;
	var yUnits=chartData.yAxisUnits;
	var pointGroups =chartData.pointGroups;
	var chartDiv= document.createElement(TAG_DIV);
	contentDiv.appendChild(chartDiv);
	var lengendDiv= document.createElement(TAG_DIV);
	
	var buildPointStyleFn;
	
	if(chartData.width&&chartData.width!=0){
	 	
		chartDiv.style.width=chartData.width;//		 
		chartDiv.style.height=chartData.height;//
 
		lengendDiv.style.width=chartData.width;//	
	 
		 
	}else{ 
		chartDiv.style.width="100%";//		
		chartDiv.style.height="100%";//
		contentDiv.style.width="100%";
		contentDiv.style.height="100%";
	}
	
	contentDiv.appendChild(lengendDiv);
 
	var xLabels=getXLabels(chartData,false);
	var yLabels=getYLabels(chartData,false);
	
	var scatterchart = new dojox.charting.Chart(chartDiv, {
		
		title: chartData.description, 
		titlePos: "top", 
		titleGap: 10,
		titleFont: "normal normal normal 12pt Arial",
		titleFontColor: "orange"
	});
	
	if(chartData.sourceOperatorClass == "KMeansOperator"){
		scatterchart.sourceOperatorClass = "KMeansOperator";
		var customColor = ["#3f9998","#Cf446f","#3fc0c3","#70c058","#c663a6",
                           "#5fBBD8","#Df648f","#5fD0D3","#90D078","#D683B6",
                           "#7fDDD8","#Ef84Af","#7fE0E3","#B0E098","#E6A3C6",
                           "#9fFFF8","#FfA4Cf","#9fF0F3","#D0F0A8","#F6C3D6"];
		var theme = new dojox.charting.Theme({
			markers: {
				CIRCLE:   "m-3,0 c0,-4 6,-4 6,0 m-6,0 c0,4 6,4 6,0"
			},
			colors: customColor
		});
		scatterchart.setTheme(theme);
		
		buildPointStyleFn = function(index){
			if(index % 2 != 0){// if true, means this point is centered. then make different with other.
				return {
					plot: "default",
					stroke: {
						color: customColor[index - 1 % customColor.length]
					},
					fill: customColor[index - 1 % customColor.length],
					marker: "m-3,0 c0,-8 12,-8 12,0 m-12,0 c0,8 12,8 12,0"
				};
			}else{
				return {
					plot: "default",
					stroke: {
						color: customColor[index % customColor.length]
					},
					fill: customColor[index % customColor.length]
				};
			}
		};
	}else{
		scatterchart.setTheme(dojox.charting.themes.Bahamation);
		buildPointStyleFn = function(index){
			return {plot: "default"};
		};
	}
	
//	if(chart.sourceOperatorClass && chart.sourceOperatorClass == "KMeansOperator"){//spacial for cluster center point 
// 		chart.addSeries(pointGroup.label, pointGroup.points,{ plot: "default",
// 		//	stroke: { width: 6}});
// 			stroke: {color: pointGroup.color , width: 10},fill:pointGroup.color});
// 	}else{
// 		chart.addSeries(pointGroup.label, pointGroup.points,{plot: "default"} );
// 	}
	
	//var rotate = xLabels.length>10?-30:0;
	var xAxis=  {
			title: chartData.xAxisTitle, 
			titleFontColor: "green",
			titleFont: "normal normal normal 12pt Arial",
			titleOrientation: orientation_away,
			majorTick: {stroke: "black", length: 10},
			minorTick: {stroke: "black", length: 2},
//			rotation:chartData.xLableRotation,
			rotation:-60,
			stroke: "grey",//"grey",
			htmlLabels: false,
		 	labels : xLabels
			};



	
	var yAxis = {	
			title: chartData.yAxisTitle,
			titleFont: "normal normal normal 12pt Arial",
			titleFontColor: "green",
			titleGap:38,
			vertical: true,
			
			rotation:chartData.xLableRotation,
			stroke: "grey",//"grey",
			majorTick: {stroke: "black", length: 10},
			minorTick: {stroke: "black", length: 3},
			htmlLabels: false,
			labels : yLabels
		};
	
	addSpecialParameter4Axis(xAxis,yAxis,chartData);
	 
	scatterchart.addAxis("y", yAxis);
	scatterchart.addAxis("x",xAxis);
 
	scatterchart.theme.chart.fill=[245,245,245];
	scatterchart.theme.plotarea.fill=[245,245,245];
	
	//use this to control the grid line... 
	scatterchart.theme.axis.majorTick={color:"grey",width:0.5,length:6};
	scatterchart.theme.axis.minorTick={color:"white",width:0.5,length:3};
	
	scatterchart.addPlot("default", {type: "Scatter",markers: true});
	
	
	if(pointGroups){
		for ( var i = 0; i < pointGroups.length; i++) {
			//MINERWEB-1036 begin
            //var reg = /^\".+Value$/;
			//if(pointGroups[i].label!=null && ""!=pointGroups[i].label && reg.test(pointGroups[i].label)==false){
				//pointGroups[i].label = "\""+pointGroups[i].label+"\" "+alpine.nls.scatter_plot_legend_SCATTERMATRIX_VALUE;
				pointGroups[i].label = pointGroups[i].label;
			//}else{
				//pointGroups[i].label = alpine.nls.scatter_plot_legend_SCATTERMATRIX_VALUE;
			//}
			//MINERWEB-1036 end
			addScatterSeries(pointGroups[i],scatterchart, buildPointStyleFn(i)) ;
		}
	 	 	
	}
	var visualLines =chartData.visualLines;
	
	if(visualLines){
		//this is for line
		scatterchart.addPlot("lines", {type: "Lines",markers: false  });
		
		for ( var i = 0; i < visualLines.length; i++) {
			//if(visualLines[i].label!=null && ""!=visualLines[i].label){
				//visualLines[i].label = "\""+visualLines[i].label+"\" "+alpine.nls.scatter_plot_legend_SCATTERMATRIX_LINE;
				visualLines[i].label = visualLines[i].label;
			//}else{
				//visualLines[i].label = alpine.nls.scatter_plot_legend_SCATTERMATRIX_LINE;
			//}
			
	 		scatterchart.addSeries(visualLines[i].label, visualLines[i].points , {plot: "lines"});
			 
		}
  	 	
	}
	scatterchart.addPlot("grid", {type:"Grid", hMinorLines:chartData.hGrid,vMinorLines:chartData.vGrid});
	if(pointGroups  ){
		new dojox.charting.action2d.Tooltip(scatterchart, "default");
		//new dojox.charting.action2d.Tooltip(scatterchart, "lines");
		scatterchart.render(); 
		
		//dojox.charting.widget.Legend--- simple legend
		//dojox.charting.widget.SelectableLegend
		//this line code must after the render. else will error...
	 	var legend = new dojox.charting.widget.SelectableLegend({
	 		chart : scatterchart}, lengendDiv);
	 	
	 	//For legend look feel
	 	dojo.query("#"+legend.id+" td").forEach(function(itm,idx){
	 	    itm.style.width = "110px";
	 	});
	 	/*
	 	var tdNum = dojo.query("#"+legend.id+" td");
	 	if(null!=tdNum && tdNum>0){
	 		var tableWidth = tdNum*120;
	 		dojo.style(legend.id,{width:tableWidth+"px"});
	 	}
	 	*/
	 	dojo.query("#"+legend.id+" .dojoxLegendText").style({wordWrap:"break-word", wordBreak:"break-all", display:"block"});
//		dojo.query("#"+legend.id+" .dojoxLegendText").forEach(function(itm,idx){
//			var labelValue = itm.innerHTML;
//
//			if(null!=labelValue && ""!=labelValue){
//				itm.title = labelValue;
//				if(labelValue.length>18){
//					itm.innerHTML = labelValue.substring(0,15)+"...";
//				}
//			}
//		});
	 	
		output.dojo_surface_legend=legend._surfaces;
		
		//for any chart 
		
		setOutputLegendLabels(output,legend) ;
	}
	output.dojo_surface = scatterchart.surface;
 
}

function addScatterSeries(pointGroup,chart, buildPointStyle){
	chart.addSeries(pointGroup.label, pointGroup.points,buildPointStyle);
	
// 	if(chart.sourceOperatorClass && chart.sourceOperatorClass == "KMeansOperator"){//spacial for cluster center point 
// 		chart.addSeries(pointGroup.label, pointGroup.points,{ plot: "default",
// 		//	stroke: { width: 6}});
// 			stroke: {color: pointGroup.color , width: 10},fill:pointGroup.color});
// 	}else{
// 		chart.addSeries(pointGroup.label, pointGroup.points,{plot: "default"} );
// 	}
}