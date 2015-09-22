var lineChartGraphicsHolder = new Array();

function getLineChartGraphics(id) {
    return lineChartGraphicsHolder[id];
}
function saveLineChartGraphics(id, graphics) {
    lineChartGraphicsHolder[id] = graphics;
}

function transitionLine(panelid, checkboxid)
{
      var parameters = getLineChartGraphics(panelid);
    var vis = d3.selectAll("div").filter(function (d, i) {
        return this.name === parameters.divName;
    });

    vis.selectAll("path")
        .filter(".line")
        .transition()
        .duration(250)
        .style("opacity", function(d,i)
        {
            if (i == checkboxid)
                return 1.0;
            else if (parameters.lines[i].active)
                return 1.0;
            else
                return 0.0;
        })
        .transition()
        .delay(250)
        .duration(1000)
        .attr("d", function (d,i) {
            if (parameters.lines[i].active)
            return parameters.line(d);
            else
            return parameters.flatline(d);
        })
        .transition()
        .delay(1250)
        .duration(500)
        .style("opacity", function (d, i) {
            if (parameters.lines[i].active)
                return 1.0;
            else
                return 0.0;
        });
        //.style("stroke-opacity", 1.0);
    vis.selectAll("g.layer circle")
        .transition()
        .duration(250)
        .style("opacity", function(d,i)
        {
            if (d.layoutid == checkboxid)
                return 1.0;
            else if (parameters.lines[d.layoutid].active)
                return 1.0;
            else
                return 0.0;        })
        .transition()
        .delay(250)
        .duration(1000)
        .attr("cy", function (d,i) {
            if (parameters.lines[d.layoutid].active)
                return parameters.yscale(d.y);
            else
                return parameters.height;
        })
        .transition()
        .delay(1250)
        .duration(500)
        .style("opacity", function (d, i) {
            if (parameters.lines[d.layoutid].active)
                return 1.0;
            else
                return 0.0;
        }) ;
}


function fillOutPaneLine( outpane,output, panelid){
    var isForExport =  isExportingReport;
    var chartData=output.visualData;
    var contentDiv= document.createElement(TAG_DIV);
    outpane.setContent(contentDiv);
    var xUnits=chartData.xAxisUnits;
    var yUnits=chartData.yAxisUnits;
    var lines =chartData.lines;
    var chartDiv= document.createElement(TAG_DIV);
    contentDiv.name = "panel_" + panelid;

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

    //Put the data in the form that d3 likes
    var data = [];
    if (!lines) lines = [];
    var numLines = lines.length;
    var legends = [];
    var legend_labels = [];
    for (var count=0; count < numLines; count++)
    {
        legends.push(createLineLegend(lineChartColors[count % lineChartColors.length]));
        legend_labels.push(lines[count].label);
        lines[count].active = true;
        for (var count2=0; count2 < lines[count].points.length; count2++)
        {
            lines[count].points[count2].layoutid = count;
        }
        data.push(lines[count].points);
    }
    var timeSeries = false;
    //first check to see if it's a time series
    if (chartData.xLabels && chartData.xLabels.length > 0)
    {
        //ticksX = chartData.xLabels.length;
        var xLabels = [];
        var firstArray = chartData.xLabels[0];
        if (firstArray.length == 3)
        {
            timeSeries = true;

        }
        for (var count=0; count < chartData.xLabels.length; count++)
        {
            var currentLong = +chartData.xLabels[count][2];
            xLabels[count] = new Date(currentLong);
        }
    }

    var paddingLeft = 80;
    var paddingRight = 80;
    var paddingTop = 20;
    var paddingBottom = 80;


    var maxy = d3.max(data, function (d) { return d3.max(d, function (d) { return +d.y; }); });
    var miny = d3.min(data, function (d) { return d3.min(d, function (d) { return +d.y; }); });
    var minY = (chartData.minY ? +chartData.minY : miny);
    var maxY = (chartData.maxY ? +chartData.maxY : maxy);
    var maxx = d3.max(data, function (d) { return d3.max(d, function (d) { return +d.x; }); });
    var minx = d3.min(data, function (d) { return d3.min(d, function (d) { return +d.x; }); });
    var maxX = (chartData.maxX ? +chartData.maxX : maxx);
    var minX = (chartData.minX ? +chartData.minX : minx);


    var height = (chartData.height ? +chartData.height : (outpane._contentBox.h - paddingLeft - paddingRight ));
    var width = (chartData.width ? +chartData.width : (outpane._contentBox.w - paddingTop - paddingBottom));

    var xscale = d3.scale.linear().domain([minX, maxX]).range([0, width]);
    var yscale = d3.scale.linear().domain([minY, maxY]).range([ height, 0]);
    if(minY==maxY){
        yscale = d3.scale.linear().domain([minY, minY+1]).range([ height, 0]);
    }

    if (timeSeries)
    {
        var timemaxx = d3.max(chartData.xLabels, function (d) { console.log(d[2]); return d[2];});
        var timeminx = d3.min(chartData.xLabels, function (d) { console.log(d[2]); return d[2];});

        var xscaletime = d3.time.scale().domain([timeminx, timemaxx]).range([0, width]);

    }



    var ticksY = 20;
    var ticksX = 10;



    var vis_pre = d3.selectAll("div").filter(function (d, i) {
        return this.name === contentDiv.name;
    })
        .append("svg")
        .attr("width", width + paddingLeft + paddingRight)//extra 80 on each side, just to leave room for axises
        .attr("height", height + paddingTop + paddingBottom)//extra 20 on top, extra 80 at bottom for axises
        ;
        var vis = vis_pre.append("g")
        .attr("transform", "translate(" + paddingLeft + "," + paddingTop + ")");


    //first we'll set up the axis

    if (!timeSeries)
    {
        var xAxis = d3.svg.axis()
            .scale(xscale)
            .ticks(ticksX)
            .orient("bottom");
    }   else
    {
        var xAxis = d3.svg.axis()
            .scale(xscaletime)
            .ticks(ticksX)
            .orient("bottom");


    }


    var yAxis = d3.svg.axis()
        .scale(yscale)
        .ticks(ticksY)
        .orient("left");


    var xside = vis.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    var yside = vis.append("g")
        .attr("class", "y axis")
        .call(yAxis);


    //okay, this is a hack.  Let's figure out the max size of the yticks.
    var maxLength  = getMaxStringLengthFromSide(yside);
   var xvalue = (-6 * maxLength) - 14;

    vis.append("svg:text")
        .attr("class", "axisLabel")
        .text(chartData.yAxisTitle)
        .attr("x", xvalue)
        .attr("y", height / 2)
        .attr("text-anchor", "middle")
        .attr("dy", ".35em")
       .attr("transform", "rotate(-90," + xvalue + "," + height / 2 + ")")
    ;

    var yvalue =  + height  + 25;

    vis.append("svg:text")
        .attr("class", "axisLabel")
        .text(chartData.xAxisTitle)
        .attr("x", width/2)
        .attr("y", yvalue)
        .attr("text-anchor", "middle")
        .attr("dy", ".35em")
    ;
    // the horizontal bar lines
    vis.selectAll(".ylabel")
        .data(yscale.ticks(ticksY))
        .enter().append("line")
        .style("stroke", gridColor)
        .attr("x1",  0)
        .attr("x2", width)
        .attr("y1", function (d) {
            return  1 * yscale(d)
        })
        .attr("y2", function (d) {
            return  1 * yscale(d)
        });

    // the vertical bar lines

    if (timeSeries)
    {
        vis.selectAll(".xlabel")
            .data(xscaletime.ticks(ticksX))
            .enter().append("line")
            .style("stroke", gridColor)
            .attr("x1",  function (d) {
                return  1 * xscaletime(d)
            })
            .attr("x2",  function (d) {
                return  1 * xscaletime(d)
            })
            .attr("y1", 0)
            .attr("y2", height);

    }
    else
    {
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
            .attr("y2", height);


    }

    //okay, now we have to put the lines on.

    //basic line function, takes a bunch of obj with obj.x, obj.y defined
    if (chartData.markers)   //if you want to mark individual points with edges
    {
        var line = d3.svg.line()
            .x(function(d) { return xscale(d.x); })
            .y(function(d) { return yscale(d.y); })
        ;
        var flatline =   d3.svg.line()
            .x(function(d) { return xscale(d.x); })
            .y(function(d) { return height; })
        ;
    }    else
    {
        var line = d3.svg.line()
            .x(function(d) { return xscale(d.x); })
            .y(function(d) { return yscale(d.y); })
            //.interpolate("basis");
        ;

        var flatline =   d3.svg.line()
            .x(function(d) { return xscale(d.x); })
            .y(function(d) { return height; })
            //.interpolate("basis");
        ;
    }


    //this is for a cool transition - this is just a flat line at the bottom of the screen
    var duration = 1000;
//    if (isForExport)     duration =0;


    if (isForExport)
    {
        vis.selectAll(".line")
            .data(data)
            .enter().append("path")
            .attr("class", "line")
            .style("stroke", function(d, i) {
                return lineChartColors[i % lineChartColors.length];
            })
            .style("stroke-width", 3)
            .style("fill", "none")
            .attr("d", line);
    }
    else
    {
        vis.selectAll(".line")
            .data(data)
            .enter().append("path")
            .attr("class", "line")
            .style("stroke", function(d, i) {
                return lineChartColors[i % lineChartColors.length];
            })
            .style("stroke-width", 3)
            .style("fill", "none")
            .attr("d", flatline)
            .transition()
            .duration(duration)
            .attr("d", line);
   }

    if (chartData.markers)
    {
        var layers = vis.selectAll("g.layer")
            .data(data)
            .enter().append("g")
            .attr("class", "layer")
            .style("fill", function(d, i) {
                return lineChartColors[i % lineChartColors.length];
            });

        if (isForExport)
        {
            var dot = layers.selectAll("g.circle")
                .data(function (d) {
                    return d;
                })
                .enter().append("svg:circle")
                .attr("class", "circle")
                .attr("cx", function(d,i) { return xscale(d.x); })
                .attr("r", 3)
                .attr("pointer-events", "none")
                .attr("cy", function(d,i) { return yscale(d.y); })
        }   else
        {
            var dot = layers.selectAll("g.circle")
                .data(function (d) {
                    return d;
                })
                .enter().append("svg:circle")
                .attr("class", "circle")
                .attr("cx", function(d,i) { return xscale(d.x); })
                .attr("cy", function(d,i) { return height; })
                .attr("r", 3)
                .attr("pointer-events", "none")
                .transition()
                .duration(duration)
                .attr("cy", function(d,i) { return yscale(d.y); })
        }



    }
//    vis.selectAll(".symbol")
//        .data(data)
//        .enter().append("path")
//        .attr("class", "svg:symbol")
//        .size( function(d) { return 3000; })
//        .type( function(d) { return d3.svg.symbolTypes[1]; })
//        .style("fill", "black");

    if (numLines > 1)  //no point putting in checkboxes if there's only one element in the scope.
    {

        var clickedACheckbox = function () {
            var id = this.id.substring(3);
            var holder = getLineChartGraphics(this.value);
            holder.lines[id].active = this.checked;
            transitionLine(this.value, id);
        }

        //chartData.seriesName
       var checkboxes = d3.selectAll("div").filter(function (d, i) {
           return this.name === contentDiv.name;
       })
           .append("div")
            .attr("class", panelid)
           .style("padding-left", paddingLeft)
            .style("padding-top", "2");

    var actualCheckboxes = checkboxes.selectAll("text.input")
                .data(lines)
                .enter().append("div")
                .style("float", "left")
                .attr("class", function (d, i) {
                    return i;
                })
                .style("padding-right", 20)
                .style("padding-bottom", 20)
            ;

        actualCheckboxes.append("input")
            .attr("type", "checkbox")
            .attr("id", function (d, i) {
                return "cb_" + i;
            })
            .attr("checked", true)
            .attr("value", panelid)
            .on("click", clickedACheckbox)
            ;

        actualCheckboxes.append("span")
            .attr("id", function (d, i) {
                return "span_" + i;
            })
            .attr("num", function (d, i) {
                return  i;
            })
            .style("padding-left", 10)
            .style("margin-left", 2)
            .style("margin-right", 2)
            .style("background-color", function (d, i) {
                return lineChartColors[i % lineChartColors.length];
            });
        actualCheckboxes.append("label")
            .text(function (d, i) {
                return lines[i].label;
            })
            .attr("for", function (d, i) {
                return "cb_" + i;
            });

        
        output.d3_legend = legends;
        output.d3_legendlabel = legend_labels;
    }
    var panelVariables = new Array();
    panelVariables.line = line;
    panelVariables.flatline = flatline;
    panelVariables.lines = lines;
    panelVariables.xscale = xscale;
    panelVariables.yscale = yscale;
    panelVariables.width = width;
    panelVariables.height = height;
    panelVariables.divName = contentDiv.name;
    saveLineChartGraphics(panelid, panelVariables) ;


    //need to wait 1500 ms before setting

    // or do we create a different svg for the html result?  (wihtout transitions)
    //lots of copy and paste, but yes.
    //any way to clone the dom object and clean it up? (remove transitions)

    // i think.
//    var deferred = new deferred();//do transation....
//    deferred.addCallback(function(){
        output.d3_svg =  vis_pre[0][0];
//
//    });

    //deferred is on a separate thread, right?  do we put on same thread? make js wait? not thrilling, but might work.




}
