function fillOutPaneBoxAndWhisker(outpane, output) {
	 //output -> VisualizationModelBoxWhisker
	if(output.visualData ){
		var chartDiv = document.createElement(TAG_DIV);
		 
		var lengendDiv = document.createElement(TAG_DIV);
		var colsDiv = document.createElement(TAG_DIV);

		lengendDiv.id = "cols_legend"+output.out_id;
		colsDiv.id = "cols"+output.out_id;
	 
		chartDiv.appendChild(colsDiv);
		chartDiv.appendChild(lengendDiv);
		outpane.setContent(chartDiv);
		
		outpane.startup();
		
		var boxWhiskerModel=output.visualData;
		  // initialize chart and create axes
	    var chart = new dojox.charting.Chart(colsDiv);

	    chart.addPlot("default", {type: "BoxAndWhisker", markers: true});
	    chart.addAxis("x", {
	    	title:boxWhiskerModel.typeDomain ,
	    	titleOrientation: orientation_away,
	    	min: boxWhiskerModel.minX,
	    	max:boxWhiskerModel.maxX, 
	    	natural : true,
	    	htmlLabels: false,
	    	labels:boxWhiskerModel.xLabels	  } );
 
	    chart.addAxis("y",{
	    	title:boxWhiskerModel.valueDomain ,
	    	titleOrientation: orientation_axis,
	    	vertical: true, 
	    	htmlLabels: false,
	    	min: boxWhiskerModel.minY, 
	    	max: boxWhiskerModel.maxY});
	 
	    var width = 0.2;
	   
	 	var  boxWhiskerGroups=boxWhiskerModel.boxWhiskers;
	 	for(var i=0;i<boxWhiskerGroups.length;i++){
	 		//a group is a series of boxwhisker graph
			var boxwhiskerGroup=boxWhiskerGroups[i];
	 
			var boxWhiskers=boxwhiskerGroup.boxWhiskers;
			var fill = randColor(1,i);
			var stroke = {color:fill};  
			boxwhiskerGroup.fillColor=fill;
			 
			for(var j=0;j<boxWhiskers.length;j++){
				boxwhisker=boxWhiskers[j];
//				var xValue=boxwhisker.type;
//				//if(boxWhiskerModel.seriesType != "number"){
//				xValue= getSeriesXValue(xValue,boxWhiskerModel.xLabels);
//				//}
				chart.addSeries(boxwhisker.type+"_"+i+"_"+j,
			    [{lwhisker: boxwhisker.lwhisker, lbox:boxwhisker.lbox, median:boxwhisker.median, 
			    	  ubox: boxwhisker.ubox, uwhisker:boxwhisker.uwhisker, outliers: boxwhisker.outliers,mean:boxwhisker.mean}],
			      {stroke:stroke, fill:fill , center:boxwhisker.xValue  , width:width}); 
				
			 }
	 	 
	 }
  
	var descDiv = document.createElement("div");
	chartDiv.appendChild(descDiv);
	//here is the description for legend
		 
//	if(boxWhiskerModel.typeDomain&&boxWhiskerModel.typeDomain!=""){
    if(boxWhiskerModel.seriesDomain!=null && boxWhiskerModel.seriesDomain!=""){
        typeDiv=document.createElement("div");
        typeDiv.innerHTML = ("<a>series = " + boxWhiskerModel.seriesDomain+" </a>");
        descDiv.appendChild(typeDiv);

//	}
        var legendHTML="";
        legendDiv=document.createElement("div");
        legendHTML= "<table></tr>";
        //draw the lengend by gfx...
        for(var i=0;i<boxWhiskerGroups.length;i++){
            var boxwhiskerGroup=boxWhiskerGroups[i];
            legendHTML=legendHTML+"<td> <div id =boxwshikerlegend"+i+"></div></td><td>"+boxwhiskerGroup.series+"</td><td>  </td>" ;
        }
        legendHTML=legendHTML+"</tr></table>" ;
        legendDiv.innerHTML = legendHTML;
        descDiv.appendChild(legendDiv);
        for(var i=0;i<boxWhiskerGroups.length;i++){
            var boxwhiskerGroup=boxWhiskerGroups[i];
            var fillColor= boxwhiskerGroup.fillColor;
            var surface = dojox.gfx.createSurface(dojo.byId("boxwshikerlegend"+i), 20, 20);
            var rect1 = surface.createRect({x:0 , y:0, width: 18, height: 18, r: 2});
            rect1.setFill(fillColor).setStroke(fillColor);
        }
    }

	var tooltip = new dojox.charting.action2d.Tooltip(chart, "default");

	chart.render();
	output.dojo_surface=chart.surface;
	
}
	
	function    getSeriesXValue(xValue,Xlabels){
		for(var i=0;i<Xlabels.length;i++){
			if(Xlabels[i].text==xValue){
				return Xlabels[i].value;
			}
			
		}
	} 
 
}