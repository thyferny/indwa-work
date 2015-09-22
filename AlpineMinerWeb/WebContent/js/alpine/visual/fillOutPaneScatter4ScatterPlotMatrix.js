
function fillOutPaneScatter4ScatterPlotMatrix( outpane,output, panelid){
 

	var chartData=output.visualData;
	var contentDiv= document.createElement(TAG_DIV);
    contentDiv.name = "popuppanel_" + panelid;
    contentDiv.id = "popuppanel_" + panelid;

    outpane.setContent(contentDiv);


    var pointGroups =chartData.pointGroups;
    if (!pointGroups) pointGroups = []; //just in case, to prevent errors.
    var numLayers = pointGroups.length;

    //now put the data in the form that d3 likes
    var data = [];
    for (var count=0; count < numLayers; count++)
    {
        data.push(pointGroups[count].points);
    }

    var maxy = d3.max(data, function (d) { return d3.max(d, function (d) { return +d.y; }); });
    var maxx = d3.max(data, function (d) { return d3.max(d, function (d) { return +d.x; }); });

    var miny = d3.min(data, function (d) { return d3.min(d, function (d) { return +d.y; }); });
    var minx = d3.min(data, function (d) { return d3.min(d, function (d) { return +d.x; }); });


    var paddingLeft = 30;
    var paddingRight = 20;
    var paddingTop = 20;
    var paddingBottom = 80;
    var popupHeight=400;
    var popupWidth = 750;

    var yscale = d3.scale.linear().domain([miny, maxy]).range([ popupHeight, 0]),
        xscale = d3.scale.linear().domain([minx, maxx]).range([0, popupWidth]);


    var vis_prev = d3.selectAll("div").filter(function (d, i) {
        return this.id === contentDiv.id;
    })
        .append("svg")
        .attr("width", popupWidth + paddingLeft + paddingRight)//leaving room for axises and padding
        .attr("height", popupHeight + paddingTop + paddingBottom)//leaving room for axises and padding
        ;
     var vis = vis_prev.append("g")
        .attr("transform", "translate(" + paddingLeft + "," + paddingTop + ")");

    var color = d3.interpolateRgb(scatterStartColor, scatterEndColor);




    var ticksY=15;
    var ticksX=20;

    var xAxis = d3.svg.axis()
        .scale(xscale)
        .ticks(ticksX)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(yscale)
        .ticks(ticksY)
        .orient("left");


    var xside = vis.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + popupHeight + ")")
        .call(xAxis);

    var yside = vis.append("g")
        .attr("class", "y axis")
        .call(yAxis);

    var maxLength  = getMaxStringLengthFromSide(yside);
    var xvalue = (-6 * maxLength) - 14;

    vis.append("svg:text")
        .attr("class", "axisLabel")
        .text(chartData.yAxisTitle)
        .attr("x", xvalue)
        .attr("y", popupHeight / 2)
        .attr("text-anchor", "middle")
        .attr("dy", ".35em")
        .attr("transform", "rotate(-90," + xvalue + "," + popupHeight / 2 + ")")
    ;


    vis.append("svg:text")
        .attr("class", "axisLabel")
        .text(chartData.xAxisTitle)
        .attr("x", popupWidth/2)
        .attr("y", popupHeight + 25)
        .attr("text-anchor", "middle")
        .attr("dy", ".35em")
    ;

    // the horizontal bar lines
    vis.selectAll(".ylabel")
        .data(yscale.ticks(ticksY))
        .enter().append("line")
        .style("stroke", gridColor)
        .attr("x1",  0)
        .attr("x2", popupWidth)
        .attr("y1", function (d) {
            return  1 * yscale(d)
        })
        .attr("y2", function (d) {
            return  1 * yscale(d)
        });

    // the vertical bar lines
    vis.selectAll(".xlabel")
        .data(xscale.ticks(ticksX))
        .enter().append("line")
        .style("stroke", gridColor)
        .attr("x1",  function (d) {
            return  1 * xscale(d)
        })
        .attr("x2",  function (d) {
            return  1 * xscale(d)
        })
        .attr("y1", 0)
        .attr("y2", popupHeight);



    //don't know if this will have more than one layer, but building in the structure just in case
    var layers = vis.selectAll("g.layer")
        .data(data)
        .enter().append("g")
        .attr("class", "layer")
        .style("fill", function(d, i) {
            if (numLayers < 2) return color(0);
            return color(i / (numLayers - 1));
        });



    var dot = layers.selectAll("g.circle")
        .data(function (d) {
            return d;
        })
        .enter().append("svg:circle")
        .attr("class", "circle")
        .attr("cx", function(d) { return xscale(d.x); })
        .attr("cy", function(d) { return yscale(d.y); })
        .attr("r", 2)
        .style("fill-opacity", .5)
        .attr("pointer-events", "none");


//now we need the fitting line
    var visualLines =chartData.visualLines;
    var dataLine = null;
    if(visualLines!=null && visualLines.length>0){
        for(var lineCount = 0;lineCount<visualLines.length;lineCount++){
            dataLine = visualLines[lineCount].points;
            if(dataLine!=null){
                vis.append("line")
                    .attr("class", "fittingLine")
                    .style("stroke", scatterLineColor)
                    .attr("x1", xscale(dataLine[0].x))
                    .attr("x2", xscale(dataLine[1].x))
                    .attr("y1",  yscale(dataLine[0].y))
                    .attr("y2", yscale(dataLine[1].y));
            }
        }
    }
 var newLeftPadding = paddingLeft + maxLength*6;
    vis_prev.attr("width", newLeftPadding + paddingRight + popupWidth);
    vis.attr("transform", "translate(" + newLeftPadding + "," + paddingTop + ")");
    output.d3_svg =  vis_prev[0][0];

}


function scatterPlotChart4OperatorMenu( outpane,output, panelid){
    var chartData=output.visualData;
    var contentDiv= document.createElement(TAG_DIV);
    contentDiv.name = "popuppanel_" + panelid;
    contentDiv.id = "popuppanel_" + panelid;

    outpane.setContent(contentDiv);


    var pointGroups =chartData.pointGroups;
    if (!pointGroups) pointGroups = []; //just in case, to prevent errors.
    var numLayers = pointGroups.length;

    //now put the data in the form that d3 likes
    var data = [];
    var color = ["#3fc0c3","#70c058","#c663a6",
        "#5fBBD8","#Df648f","#5fD0D3","#90D078","#D683B6",
        "#7fDDD8","#Ef84Af","#7fE0E3","#B0E098","#E6A3C6",
        "#9fFFF8","#FfA4Cf","#9fF0F3","#D0F0A8","#F6C3D6","#3f9998","#Cf446f"];

    var legends = [];
    var legend_labels = [];
    var legendType = [];
    for (var count=0; count < numLayers; count++)
    {
        legends.push(createBarLegend(color[count%color.length]));
        legend_labels.push(pointGroups[count].label);
        legendType.push("circle");
        pointGroups[count].active = true;
        for (var count2=0; count2 < pointGroups[count].points.length; count2++)
        {
            pointGroups[count].points[count2].layoutid = count;
        }

        data.push(pointGroups[count].points);
    }

    var maxy = d3.max(data, function (d) { return d3.max(d, function (d) { return +d.y; }); });
    var maxx = d3.max(data, function (d) { return d3.max(d, function (d) { return +d.x; }); });

    var miny = d3.min(data, function (d) { return d3.min(d, function (d) { return +d.y; }); });
    var minx = d3.min(data, function (d) { return d3.min(d, function (d) { return +d.x; }); });


    var paddingLeft = 90;
    var paddingRight = 20;
    var paddingTop = 20;
    var paddingBottom = 80;
    var popupHeight=400;
    var popupWidth = 730;

    var yscale = d3.scale.linear().domain([miny, maxy]).range([ popupHeight, 0]),
        xscale = d3.scale.linear().domain([minx, maxx]).range([0, popupWidth]);


    var vis_prev = d3.selectAll("div").filter(function (d, i) {
            return this.id === contentDiv.id;
        })
            .append("svg")
            .attr("width", popupWidth + paddingLeft + paddingRight)//leaving room for axises and padding
            .attr("height", popupHeight + paddingTop + paddingBottom)//leaving room for axises and padding
        ;
    var vis = vis_prev.append("g")
        .attr("transform", "translate(" + paddingLeft + "," + paddingTop + ")");

    //var color = d3.interpolateRgb(scatterStartColor, scatterEndColor);




    var ticksY=15;
    var ticksX=20;

    var xAxis = d3.svg.axis()
        .scale(xscale)
        .ticks(ticksX)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(yscale)
        .ticks(ticksY)
        .orient("left");


    var xside = vis.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + popupHeight + ")")
        .call(xAxis);

    var yside = vis.append("g")
        .attr("class", "y axis")
        .call(yAxis);

    var maxLength  = getMaxStringLengthFromSide(yside);
    var xvalue = (-6 * maxLength) - 14;

    vis.append("svg:text")
        .attr("class", "axisLabel")
        .text(chartData.yAxisTitle)
        .attr("x", xvalue)
        .attr("y", popupHeight / 2)
        .attr("text-anchor", "middle")
        .attr("dy", "-28px")
        .attr("transform", "rotate(-90," + xvalue + "," + popupHeight / 2 + ")")
        //.attr("style","margin-right:15px;")
    ;


    vis.append("svg:text")
        .attr("class", "axisLabel")
        .text(chartData.xAxisTitle)
        .attr("x", popupWidth/2)
        .attr("y", popupHeight + 25)
        .attr("text-anchor", "middle")
        .attr("dy", "20px")
    ;

    // the horizontal bar lines
    vis.selectAll(".ylabel")
        .data(yscale.ticks(ticksY))
        .enter().append("line")
        .style("stroke", gridColor)
        .attr("x1",  0)
        .attr("x2", popupWidth)
        .attr("y1", function (d) {
            return  1 * yscale(d)
        })
        .attr("y2", function (d) {
            return  1 * yscale(d)
        });

    // the vertical bar lines
    vis.selectAll(".xlabel")
        .data(xscale.ticks(ticksX))
        .enter().append("line")
        .style("stroke", gridColor)
        .attr("x1",  function (d) {
            return  1 * xscale(d)
        })
        .attr("x2",  function (d) {
            return  1 * xscale(d)
        })
        .attr("y1", 0)
        .attr("y2", popupHeight);



    //don't know if this will have more than one layer, but building in the structure just in case
    var layers = vis.selectAll("g.layer")
        .data(data)
        .enter().append("g")
        .attr("class", "layer")
        .style("fill", function(d, i) {
            if (numLayers < 2) return color[0];
            return color[i % color.length];
        });



    var dot = layers.selectAll("g.circle")
        .data(function (d) {
            return d;
        })
        .enter().append("svg:circle")
        .attr("class", "circle")
        .attr("cx", function(d) { return xscale(d.x); })
        .attr("cy", function(d) { return yscale(d.y); })
        .attr("r", 4)
        .style("fill-opacity", .7)
        .attr("pointer-events", "none")
        .attr("layoutid",function(d){return d.layoutid;})
        ;


//now we need the fitting line
    var visualLines =chartData.visualLines;
    var dataLine = null;
    if(visualLines!=null && visualLines.length>0){
        for(var lineCount = 0;lineCount<visualLines.length;lineCount++){
            dataLine = visualLines[lineCount].points;

            legends.push(createLineLegend(color[lineCount % color.length]));
            legend_labels.push(visualLines[lineCount].label);
            legendType.push("line");
            visualLines[lineCount].active = true;
            for (var count2=0; count2 < visualLines[lineCount].points.length; count2++)
            {
                visualLines[lineCount].points[count2].layoutid = "line_"+legendType.length-1;
            }

            if(dataLine!=null){
                vis.append("line")
                    .attr("class", "fittingLine")
                    .style("stroke", color[lineCount])
                    .attr("x1", xscale(dataLine[0].x))
                    .attr("x2", xscale(dataLine[1].x))
                    .attr("y1",  yscale(dataLine[0].y))
                    .attr("y2", yscale(dataLine[1].y))
                    .attr("layoutid",(legendType.length-1))
                  ;
            }
        }
    }
   if(null!=legends && legends.length>=2){
       fillLegend4ScatterRightMenu(legends,legend_labels,legendType,contentDiv);
   }

    output.d3_svg =  vis_prev[0][0];
}

function fillLegend4ScatterRightMenu(legends,legendLabels,legendType,panel){
    var legendDiv = dojo.create("div",{id:"scatter_legend",width:"100%",heigth:"100px"});
    dojo.place(legendDiv,panel,"last");
    if(null!=legends && null!=legendLabels && legendLabels.length>0 && legends.length>0){
        for(var i=0;i<legends.length;i++){
            var span = dojo.create("span",{style:"margin:5px;"},legendDiv);
            var input = dojo.create("input",{type:"checkbox",checked:"checked",onclick:toggleLegend,legendId:i,legendType:legendType[i]},span);
            dojo.create("label",{innerHTML:legends[i],style:"heigth:18px;vertical-align:middle"},span);
            dojo.create("label",{innerHTML:legendLabels[i],style:"heigth:18px;vertical-align:middle;margin-left:3px"},span);
        }
    }
}

function toggleLegend(){
    var legendId = this.legendId==null?this.getAttribute("legendId"):this.legendId;
    var legendType = this.legendType==null?this.getAttribute("legendType"):this.legendType;
    //alert(legendId+":"+legendType);
    if(legendType=="circle"){
        if(this.checked==true){
            d3.selectAll("circle.circle").filter(function (d, i) {
                var lengendId = d.layoutid+"";
                return lengendId === legendId;
            }).style("fill-opacity",.7);
        }else{
            d3.selectAll("circle.circle").filter(function (d, i) {
                var lengendId = d.layoutid+"";
                return lengendId === legendId;
            }).style("fill-opacity",0);
        }

    }else{
        if(this.checked==true){
           var lines = d3.selectAll("line.fittingLine");
            if(null!=lines && null!=lines[0] && lines[0].length>0){
                for(var i=0;i<lines[0].length;i++){
                    var layoutId = (lines[0][i].layoutid==null)?lines[0][i].getAttribute("layoutid"): lines[0][i].layoutid;
                    if(layoutId == legendId){
                        lines[0][i].style.opacity=0.7;
                }
                }
            }
        }else{
            var lines = d3.selectAll("line.fittingLine");
            if(null!=lines && null!=lines[0] && lines[0].length>0){
                for(var i=0;i<lines[0].length;i++){
                    var layoutId = (lines[0][i].layoutid==null)?lines[0][i].getAttribute("layoutid"): lines[0][i].layoutid;
                    if(layoutId == legendId){
                        lines[0][i].style.opacity=0;
                    }
                }
            }
        }
    }

}