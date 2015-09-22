
function fillOutPaneLine( outpane,output){
	var chartData=output.visualData;
	var contentDiv= document.createElement(TAG_DIV);
	outpane.setContent(contentDiv);
	var xUnits=chartData.xAxisUnits;
	var yUnits=chartData.yAxisUnits;
	var lines =chartData.lines;
	var chartDiv= document.createElement(TAG_DIV);
	
	 if(chartData.description){
		var descDiv= document.createElement(TAG_DIV);
		descDiv.innerHTML="<STRONG>"+chartData.description+"</STRONG>";
		contentDiv.appendChild(descDiv);
 
 	}
	 if(chartData.errorMessage && chartData.errorMessage.length > 0){
			var errorsDiv = document.createElement(TAG_DIV);
			errorsDiv.id="err_"+ new Date().getTime();
			contentDiv.appendChild(errorsDiv);	
			fillVisualErrorMessages(errorsDiv,chartData.errorMessage) ;
		}
	contentDiv.appendChild(chartDiv);
	var lengendDiv= document.createElement(TAG_DIV);
	
	contentDiv.appendChild(lengendDiv);
	if(chartData.width&&chartData.width!=0){
	 	
		chartDiv.style.width=chartData.width;//		 
		chartDiv.style.height=chartData.height;//
 
		lengendDiv.style.width=chartData.width;//	
		lengendDiv.style.height="100%";//			 
		 
	}else{ 
		chartDiv.style.width="100%";//		
		chartDiv.style.height="100%";//
		contentDiv.style.width="100%";
		contentDiv.style.height="100%";
	}
	//make it count the size!!
 
	

	var xLabels=getXLabels(chartData,false);
	var yLabels=getYLabels(chartData,false);
	
	var xAxis={
			
			title: chartData.xAxisTitle, 
			titleFontColor: "green",
			titleOrientation: orientation_away, 
		 	labels : xLabels,
		 	rotation:chartData.xLableRotation,
		//	min: 1*chartData.xMin,
		//	max: 1*chartData.xMax,
//			majorTickStep: 0.1, minorTickStep: 0.05, 
			stroke: "black",//color of the axis
			htmlLabels: false,
			majorTick: {stroke: "black", length: 3}, minorTick: {stroke: "black", length: 3}
			};
	
	var yAxis={	
			//	labels: chartData.yLabels,
			title: chartData.yAxisTitle,
			titleFontColor: "red",
			stroke: "black",//"grey",
			vertical: true,
			//natural: true,
			labels : yLabels,
			rotation:chartData.yLableRotation,
			maxLabelCharCount:10,
			trailingSymbol:"...",
		//	min: 1*chartData.yMin,
		//	max: 1*chartData.yMax,
			//fixLower: "major", fixUpper: "major"
		//	majorTickStep: 0.1, minorTickStep: 0.05,  
			htmlLabels: false,
			majorTick: {stroke: "black", length:3}, minorTick: {stroke: "black", length: 3}
			};
	
	
	addSpecialParameter4Axis(xAxis,yAxis,chartData);
	var linechart = new dojox.charting.Chart(chartDiv, {
		
		//title: chartData.description, 
	//	titlePos: "top", 
	//	titleGap: 10,
	//	titleFont: "normal normal normal 12pt Arial",
	//	titleFontColor: "orange"
 			
	});
	linechart.setTheme(dojox.charting.themes.Bahamation);
	//PlotKit.blue PlotKit.orange
	//setTheme(dojox.charting.themes.PlotKit.blue).
	linechart.addAxis("x",xAxis );
	linechart.addAxis("y", yAxis);
	
	
	linechart.theme.chart.fill=[245,245,245];//"lightgrey";
	linechart.theme.plotarea.fill=[245,245,245];//"lightgrey";
	
	//use this to control the grid line... 
	linechart.theme.axis.majorTick={color:"grey",width:0.5,length:6};
	linechart.theme.axis.minorTick={color:"white",width:0.5,length:3};

 
	var lineMarkers=false;
	if(chartData.markers&&chartData.markers==true)	{
		lineMarkers=true;
	}

	linechart.addPlot("default", {type: "Lines",markers: lineMarkers  });
	linechart.addPlot("grid", {type:"Grid", hMinorLines:chartData.hGrid,vMinorLines:chartData.vGrid});
	
	//linechart.addSeries("Series A", [400, 800, 200, 600, 600], {stroke: {color: "red"}, fill: "lightpink"}).
	//addSeries("Series B", [200, 300, 500, 700, 800], {stroke: {color: "blue"}, fill: "lightblue"}).
	if(lines){
		for ( var i = 0; i < lines.length; i++) {
			addLineSeries(lines[i],linechart) ;
		}
		//dojox.charting.widget.Legend--- simple legend
	 	
	 	
		new dojox.charting.action2d.Tooltip(linechart, "default");
	}
	linechart.render(); 
 	var legend = new dojox.charting.widget.SelectableLegend({
 		chart : linechart}, lengendDiv);
 
	output.dojo_surface=linechart.surface;
	output.dojo_surface_legend=legend._surfaces;
	
	//for any chart 
	
	setOutputLegendLabels(output,legend) ;
}

function addLineSeries(line,chart ){
	//line.label;
	//line.points;
 	if(line.color){//special for ROC, for the green 
 		chart.addSeries(line.label, line.points,{ stroke: {color: line.color},fill:line.color});
 	}else{
 		chart.addSeries(line.label, line.points );
 	}
}