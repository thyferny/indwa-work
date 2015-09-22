var barChartGraphicsHolder = new Array();


function addLegend(chart, node) {
    //dojox.charting.widget.Legend
    var legend = new dojox.charting.widget.SelectableLegend({
        chart:chart    }, node);
    return legend;
}

/** using the data like this:
 * "visualData":{ "categoryName":"quarter", "scopeName":"region",
 * "valueName":"sales","items": [
 *
 addSeries("Series A", [2, 1, 0.5, 1, 2]).
 addSeries("Series B", [2, 1, 0.5, 1, 2]).
 addSeries("Series C", [1, 0.5, 1, 2, 3]).
 addSeries("Series D", [0.7, 1.5, 1.2, 1.25, 3]).
 *
 *
 */

/**
 * Called when an element in the scope is checked or unchecked.
 * Depending the viewing state, will choose the correct transition to run.
 * @param parameters
 */
function filterChart(parameters) {
    var panelid = parameters.panelid;
    //need to know whether already group or stack.
    if (getBarChartState(panelid) == "group") {
        group2group(parameters);
    } else {
        stack2stack(parameters);
    }
}

/**
 * Used when you select or deselect an element in the scope while viewing in grouped mode.
 * @param parameters
 * @param parameters
 */
function group2group(parameters) {
    var panelid = parameters.panelid;
    var items = getBarChartData(panelid).visualData.series;
    var group = d3.selectAll("#chart").filter(function (d, i) {
        return this.name === parameters.classname;
    });
    var x = function (d) {
        return d.x * parameters.width / parameters.mx;
    };

    group.select("#group").attr("class", "first active");
    group.select("#stack").attr("class", "last");

    group.selectAll("g.layer rect")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .attr("y", function (d) {
            var h = 0;
            if (items[d.itemid].active == true)
                h = parameters.y2(d);
            if (h >= 0)
            {
                return parameters.yscalegroup(0) - h;
            } else
            {
                return parameters.yscalegroup(0);
            }
        })
        .attr("height", function (d, i) {
            if (items[d.itemid].active == true)
                return Math.abs(parameters.y2(d));
            else
                return 0;

        })
        .each("end", transitionEnd);

    function transitionEnd() {
        d3.select(this)
            .transition()
            .duration(500)
            .attr("height", function (d, i) {
                if (items[d.itemid].active == true)
                    return  Math.abs(parameters.y2(d));
                else
                    return 0;
            });
    }
}

/**
 * Used when you transition the graph from stacked to grouped
 * @param parameters
 */
function transitionGroup(parameters) {
    var panelid = parameters.panelid;
    var items = getBarChartData(panelid).visualData.series;
    var group = d3.selectAll("#chart").filter(function (d, i) {
        return this.name === parameters.classname;
    });
    saveBarChartState(panelid, "group");

    var x = function (d) {
        return d.x * parameters.width / parameters.mx;
    };
    group.select("#group")
        .attr("class", "last active");

    group.select("#stack")
        .attr("class", "first");

    group.selectAll("g.layer rect")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .attr("x", function (d, i) {
            return parameters.x({x:.9 * ~~(i / parameters.m) / parameters.n});
        })
        .attr("width", parameters.x({x:.9 / parameters.n}))
        .each("end", transitionEnd);


    function transitionEnd() {
        d3.select(this)
            .transition()
            .duration(500)
            .attr("y", function (d) {
                var h = 0;
                if (items[d.itemid].active == true)
                    h = parameters.y2(d);
                if (h >= 0)
                {
                    return parameters.yscalegroup(0) - h;
                } else
                {
                   return parameters.yscalegroup(0);
                }
            })
            .attr("height", function (d, i) {
                if (items[d.itemid].active == true)
                    return Math.abs(parameters.y2(d));
                else
                    return 0;
            });
    }

    group.selectAll(".ylinestack")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("stroke-opacity", 0.0);

    group.selectAll(".ylinegroup")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("stroke-opacity", 1.0);

    group.selectAll(".ytickstack")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("opacity", 0.0);

    group.selectAll(".ytickgroup")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("opacity", 1.0);
}

/**
 * Helper function that creates new set of y-values that's dependent on what's currently checked.
 * After this, call layout.stack if you want to create y0 values
 * @param items
 * @return {*}
 */
function newstack(items) {
    var dataraw2 = [];
    if (items) {
        for (var x = 0; x < items.length; x++) {
            var item = items[x];
            dataraw2[x] = [];

            for (var y = 0; y < item.YValues.length; y++) {
                dataraw2[x][y] = new Object();
                dataraw2[x][y].x = y;
                dataraw2[x][y].itemid = x;
                if (items[x].active) {
                    dataraw2[x][y].y = item.YValues[y];

                } else {
                    dataraw2[x][y].y = 0;
                    dataraw2[x][y].yold = item.YValues[y];
                }
            }
        }
    }

    return barStack(dataraw2);

}

/**
 * Used when you select or deselect an element in the scope while viewing in stacked mode.
 * @param parameters
 */
function stack2stack(parameters) {
    var panelid = parameters.panelid;
    var items = getBarChartData(panelid).visualData.series;
    var stack = d3.selectAll("#chart").filter(function (d, i) {
        return this.name === parameters.classname;
    });

    stack.select("#group")
        .attr("class", "first");

    stack.select("#stack")
        .attr("class", "last active");

    var data2 = newstack(items);

    stack.selectAll("g.layer rect")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .attr("y", function (d, i) {
            var x = d.itemid;
            var y = d.x;
            return parameters.yscale(data2[x][y].y0);
        })
        .attr("height", function (d, i) {
            if (items[d.itemid].active == true)
                return parameters.y0(d) - parameters.y1(d);
            else
                return 0;
        })
        .each("end", transitionEnd);


    function transitionEnd() {

    }
}

/**
 * Used when you transition the graph from grouped to stacked
 * @param parameters
 */
function transitionStack(parameters) {
    var panelid = parameters.panelid;
    var items = getBarChartData(panelid).visualData.series;
    var data2 = newstack(items);
    var stack = d3.selectAll("#chart").filter(function (d, i) {
        return this.name === parameters.classname;
    });
    saveBarChartState(panelid, "stack");

    stack.select("#group")
        .attr("class", "last");

    stack.select("#stack")
        .attr("class", "first active");


    stack.selectAll("g.layer rect")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .attr("y", function (d, i) {
            var x = d.itemid;
            var y = d.x;
            return parameters.yscale(data2[x][y].y0);
        })
        .attr("height",function (d, i) {
            if (items[d.itemid].active == true)
                return parameters.y0(d) - parameters.y1(d);
            else
                return 0;
        }).each("end", transitionEnd);

    function transitionEnd() {
        d3.select(this)
            .transition()
            .duration(500)
            .attr("x", 0)
            .attr("width", parameters.x({x:.9}));
    }

    stack.selectAll(".ylinestack")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("stroke-opacity", 1.0);

    stack.selectAll(".ylinegroup")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("stroke-opacity", 0.0);
    stack.selectAll(".ytickstack")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("opacity", 1.0);

    stack.selectAll(".ytickgroup")
        .transition()
        .duration(500)
        .delay(function (d, i) {
            return (i % parameters.m) * 10;
        })
        .style("opacity", 0.0);


}


/**
 * helper function to deal with negative stacks
 * https://groups.google.com/forum/?fromgroups#!topic/d3-js/lbPKcG6x2Qo
 * @param d
 * @return {*}
 */
function barStack(data) {
    var l = data[0].length
    while (l--) {
        var posBase = 0, negBase = 0;
        data.forEach(function(d) {
            d=d[l]
            d.size = Math.abs(d.y)
            if (d.y<0 || (d.yold && d.yold < 0 && d.y == 0 ))  {
                d.y0 = negBase
                negBase-=d.size
            } else  if (d.y > 0 || (d.yold && d.yold > 0 && d.y == 0))
            {
                d.y0 = posBase = posBase + d.size
            } else
            {
                d.y0 = posBase;
            }
        })
    }
    return data;
}

//////////////////////////////

/**
 * This is called when creating a new bar graph.
 * @param chartDiv
 * @param output
 * @param chartComponent
 */
function fillOutPaneBarChart(chartDiv, output, chartComponent) {
    var isForExport =  isExportingReport;
    var n, m, x, y0, y1, y2, width, height, mx, my,miny, mz, minz;
        chartDiv.id = "chart";

        var selectContainer = dojo.create("div",{style:"overflow:hidden"},chartDiv);
        var svgContainer = dojo.create("div",{id:"chart",style:"overflow:hidden"},chartDiv);
        output.contentPaneId = (output.parentOutPut == null || output.parentOutPut.contentPaneId == null) ? output.out_id : output.parentOutPut.contentPaneId;
        svgContainer.name = "chart_" + output.contentPaneId;
        var panelVariables = new Array();

        var chartData = output.visualData;

        // Display warning if displayed chart does not include all of the data groups...
        if(chartData.errorMessage&&chartData.errorMessage.length!=0){
            var errorsDiv = document.createElement(TAG_DIV);
            errorsDiv.id="err_"+output.contentPaneId;
            svgContainer.appendChild(errorsDiv);
            fillVisualErrorMessages(errorsDiv,chartData.errorMessage);
        }

        var items = chartData.series;
        var maxBarNumbers = countMaxBarNumber(items);
        var dataraw = [];
        var legends = [];
        var legend_labels = [];
        var color = d3.interpolateRgb(barColorStart, barColorEnd);
        var hcolor = d3.interpolateRgb(highlightedBarColorStart, highlightedBarColorEnd);
        if (items) {
            for (var x = 0; x < items.length; x++) {
            	var item = items[x];
                legends.push(createBarLegend(items.length < 2 ? color(0) : color(x / (items.length - 1))));
                legend_labels.push(item.seriesValue);
                items[x].active = true;
                dataraw[x] = [];
                for (var y = 0; y < item.YValues.length; y++) {
                    dataraw[x][y] = new Object();
                    dataraw[x][y].y = item.YValues[y];
                    dataraw[x][y].x = y;
                    dataraw[x][y].itemid = x;
                }
            }
        }

        n = chartData.series.length, // number of layers
        m = chartData.xLabels.length; // number of samples per layer

        var xLabelCharLength = getMaxLabelSize(chartData.xLabels);


        var data = barStack(dataraw);
        //var haveCategory = isHaveCategory(output.visualData);
        //var haveSeries = isHaveSeries(output.visualData);
        if (n >1)
        {
            var stackButton = new dijit.form.Button({
                baseClass: 'primaryButton',
                onClick: function(){
                    transitionStack(getBarChartGraphics(output.contentPaneId));
                },
                label:'Stack',
                disabled:true
            }, dojo.create("div",{},selectContainer));
            var groupButton = new dijit.form.Button({
                baseClass: 'primaryButton',
                onClick: function(){
                    transitionGroup(getBarChartGraphics(output.contentPaneId));
                },
                label:'Group'
            },dojo.create("div",{},selectContainer));
            dojo.connect(stackButton,"onClick",function(){
                groupButton.set('disabled',false);
                stackButton.set('disabled',true);
            });
            dojo.connect(groupButton,"onClick",function(){
                stackButton.set('disabled',false);
                groupButton.set('disabled',true);
            });
        }



        var margin = 20;
        width = chartData.width - margin,
            height = chartData.height - .5 - margin;
        mx = m,
            my = d3.max(data, function (d) {
                return d3.max(d, function (d) {
                    return d.y0 ;
                });
            }),
            miny = d3.min(data, function (d) {
                return d3.min(d, function (d) {
                    return d.y0  + d.y;
                });
            }),
            mz = d3.max(data, function (d) {
                return d3.max(d, function (d) {
                    return d.y;
                });
            }),
            minz = d3.min(data, function (d) {
                return d3.min(d, function (d) {
                    return d.y;
                });
            });
        x = function (d) {
            return d.x * width / mx;
        },
            y0 = function (d) {
                return height - d.y0 * height / (my - miny);
            },
            y1 = function (d) {
                return height - (d.size + d.y0) * height / (my - miny);
            },
            y2 = function (d) {
                return d.y * height / (mz - minz);
            }; // or `my` to not rescale

    if (minz > 0) minz = 0;
    if (miny > 0) miny = 0;
        var yscale = d3.scale.linear().domain([miny, my]).range([ height, 0]), //maps (0,maxy) to (actual height, 0)
            yscalegroup = d3.scale.linear().domain([minz, mz]).range([ height, 0]), //maps (0,maxy) to (actual height, 0)
            xscale = d3.scale.linear().domain([0, mz]).range([0 + margin, width - margin]);

        var paddingLeft = 80;
        var paddingRight = 80;
        var paddingTop = 20;
        var paddingBottom = 80;

        var ticksY = 15;


        var vis_pre = d3.selectAll("#chart").filter(function (d, i) {
                return this.name === svgContainer.name;
            })
                .append("svg")
                .attr("width", width + paddingLeft + paddingRight)//extra 80 on each side, just to leave room for axises
                .attr("height", height + paddingTop + paddingBottom)//extra 20 on top, extra 80 at bottom for axises
            ;
        var vis = vis_pre.append("g")
            .attr("transform", "translate(" + paddingLeft + "," + paddingTop + ")");



        // the horizontal bar lines for stacks
        vis.selectAll(".ylabel")
            .data(yscale.ticks(ticksY))
            .enter().append("line")
            .attr("class","ylinestack")
            .style("stroke", gridColor)
            .attr("x1", -1 * x({x:.05}))
            .attr("x2", width - x({x:.05}))
            .attr("y1", function (d) {
                return  1 * yscale(d)
            })
            .attr("y2", function (d) {
                return  1 * yscale(d)
            });

        // the horizontal bar lines for groups
        vis.selectAll(".ylabelgroup")
            .data(yscalegroup.ticks(ticksY))
            .enter().append("line")
            .attr("class","ylinegroup")
            .style("stroke", gridColor)
            .style("stroke-opacity", 0.0) //starts hidden
            .attr("x1", -1 * x({x:.05}))
            .attr("x2", width - x({x:.05}))
            .attr("y1", function (d) {
                return  1 * yscalegroup(d)
            })
            .attr("y2", function (d) {
                return  1 * yscalegroup(d)
            });


        var layers = vis.selectAll("g.layer")
            .data(data)
            .enter().append("g")
            .style("fill", function (d, i) {
                if (n < 2) return color(0);
                return color(i / (n - 1));

            })
            .attr("basicColor", function (d, i) {
                if (n < 2) return color(0);
                return color(i / (n - 1));
            })
            .attr("class", "layer");

        var bars = layers.selectAll("g.bar")
            .data(function (d) {
                return d;
            })
            .enter().append("g")
            .attr("class", "bar")
            .attr("transform", function (d) {
                return "translate(" + x(d) + ",0)";
            });

        bars.append("line")
            .style("stroke", axisColor)
            .attr("x1", x({x:.95}))
            .attr("x2", x({x:.95}))
            .attr("y1", 0)
            .attr("y2", height);

        if(isForExport){
        	bars.append("rect")
	            .attr("width", x({x:.90}))//width is 90%
	            .attr("x", 0)
	            .attr("y", height)
	            .attr("height", 0)
                .attr("y", function (d) {
                    return yscale(d.y0);
                })
                .attr("height", function (d) {
	                return y0(d) - y1(d);
	            });
        }else{
	        bars.append("rect")
	            .attr("width", x({x:.90}))//width is 90%
	            .attr("x", 0)
	            .attr("y", yscale(0))
	            .attr("height", 0)
	            .transition()
	            .delay(function (d, i) {
	                return i * 10;
	            })
	            .attr("y", function (d) {
                    return yscale(d.y0);
                })
	            .attr("height", function (d) {
                    return y0(d) - y1(d);
	            });
                bars.append("svg:title")
                    .text(function(d, i) {
                        if(/^@@@_no_series_.+/.test(items[d.itemid].seriesValue)==true){
                            return "category: " + items[d.itemid].seriesValue.replace(/^@@@_no_series_/,"") + "\n value: " + d.size;
                        }else if (n==1)
                        {
                            return "value: " + d.size;
                        }
                        else
                        {
                            return "series: " + items[d.itemid].seriesValue + "\n value: " + d.size;
                        }
                    });

        }

        var maxWidthOfBarLabels = 0;
        for (var count =0; count < chartData.xLabels.length; count++)
        {   if(chartData.xLabels[count][1]==null){continue;}
            var tempWidth = chartData.xLabels[count][1].length;
            if (tempWidth > maxWidthOfBarLabels) maxWidthOfBarLabels = tempWidth;
        }

        var shouldRotate = false ;
        if (maxWidthOfBarLabels*6 > width/mx) shouldRotate = true;

        var theOffset = 6;
        if (shouldRotate)
        {
            theOffset = maxWidthOfBarLabels*2.75;
        }

        //the labels for the bins
        if (shouldRotate && chartData.xLableRotatation !=0)
        {
            var labels = vis.selectAll("text.label")
                .data(data[0])
                .enter().append("text")
                .attr("class", "label")
                .attr("x", function(d,i)
                {
                    return x(d) + width/mx/2;
                })
                .attr("y", height + theOffset/2)
                .attr("dy", ".71em")
                .attr("text-anchor", "end")
                .attr("transform",   function(d)
                {
                        return "rotate(" + chartData.xLableRotation + "," +  (x(d)) + "," + (height + theOffset/2) + ")";
                })
                .text(function (d, i) {
                    return chartData.xLabels[i][1];
                });
        }   else
        {
            var labels = vis.selectAll("text.label")
                .data(data[0])
                .enter().append("text")
                .attr("class", "label")
                .attr("x", function(d,i)
                {
                    return x(d) + width/mx/2;
                })
                .attr("y", height + theOffset)
                .attr("dy", ".71em")
                .attr("text-anchor",  "middle")
                .text(function (d, i) {
                    return chartData.xLabels[i][1];
                });
        }





        vis.append("svg:text")
            .attr("class", "axisLabel")
            .text(chartData.xAxisTitle)
            .attr("x", width / 2)
            .attr("y", height + theOffset + 24)
            .attr("text-anchor", "middle")
            .attr("dy", ".35em");


        vis.append("line")
            .style("stroke", gridColor)
            .attr("x1", -1 * x({x:.05}))
            .attr("x2", -1 * x({x:.05}))
            .attr("y1", 0)
            .attr("y2", height);



        var yAxisStack = d3.svg.axis()
                .scale(yscale)
                .ticks(ticksY)
                .orient("left")
            ;


        var ySideStack = vis.append("g")
                .attr("class", "y axis ytickstack")
                .attr("dx", -1 * x({x:.07}))
                .attr("transform", "translate(-" + x({x:.05}) + ",0)")
                .call(yAxisStack)
            ;

        var yAxisGroup = d3.svg.axis()
                .scale(yscalegroup)
                .ticks(ticksY)
                .orient("left")
            ;


        var ySideGroup = vis.append("g")
                .attr("class", "y axis ytickgroup")
                .attr("dx", -1 * x({x:.07}))
                .attr("transform", "translate(-" + x({x:.05}) + ",0)")
                .style("opacity", 0.0)
                .call(yAxisGroup)
            ;


        var maxLength  = getMaxStringLengthFromSide(ySideGroup);
        var maxLength2 = getMaxStringLengthFromSide(ySideStack);
        if (maxLength2 > maxLength) maxLength = maxLength2;
        var padding = (-6 * maxLength) - 26;


        vis.append("svg:text")
            .attr("class", "axisLabel")
            .text(chartData.yAxisTitle)
            .attr("x", padding)
            .attr("y", height / 2)
            .attr("text-anchor", "middle")
            .attr("dy", ".35em")
            .attr("transform", "rotate(-90," + padding + "," + height / 2 + ")")
        ;


    //we've got negative numbers - need to mark off 0.
    if (miny < 0)
    {
        vis.append("line")
            .attr("class","ylinegroup")
            .style("stroke", darkAxisColor)
            .style("stroke-opacity", 0.0)
            .attr("x1", -1 * x({x:.05}))
            .attr("x2", width - x({x:.05}))
            .attr("y1", yscalegroup(0))
            .attr("y2", yscalegroup(0));

        vis.append("line")
            .style("stroke", darkAxisColor)
            .attr("class","ylinestack")
            .attr("x1", -1 * x({x:.05}))
            .attr("x2", width - x({x:.05}))
            .attr("y1", yscale(0))
            .attr("y2", yscale(0));
    }  else
    {
        vis.append("line")
            .style("stroke", axisColor)
            .attr("x1", -1 * x({x:.05}))
            .attr("x2", width - x({x:.05}))
            .attr("y1", height)
            .attr("y2", height);

    }


        panelVariables.n = n;
        panelVariables.m = m;
        panelVariables.x = x;
        panelVariables.y0 = y0;
        panelVariables.y1 = y1;
        panelVariables.y2 = y2;
        panelVariables.width = width;
        panelVariables.height = height;
        panelVariables.mx = mx;
        panelVariables.my = my;
        panelVariables.mz = mz;
        panelVariables.minz = minz;
        panelVariables.classname = svgContainer.name;
        panelVariables.panelid = output.contentPaneId;
        panelVariables.yscalegroup = yscalegroup;
        panelVariables.yscale = yscale;
        saveBarChartGraphics(output.contentPaneId, panelVariables);

        //all the graphics is done - set up the handlers for mouseover and isChecked, and add the checkboxes

        var widthOfEach = 100.0 / items.length + "%";

        var synchronizedMouseOver = function () {
            var checkboxspan = d3.select(this)[0][0];
            var id = checkboxspan.className;
            var panelid = checkboxspan.parentElement.className;
            //first get the class
            var stack = d3.selectAll("#chart").filter(function (d, i) {
                return this.name === "chart_" + panelid;
            });

            var itemgroup = stack.selectAll("g.layer").filter(function (d, i) {
                return (i == id);
            });

            itemgroup.style("fill", function (d, i) {
                    if (n < 2) return hcolor(0);
                    return hcolor(i / (n - 1));
                }

            );
        };
        var synchronizedMouseOut = function () {
            var checkboxspan = d3.select(this)[0][0];
            var id = checkboxspan.className;
            var panelid = checkboxspan.parentElement.className;
            var stack = d3.selectAll("#chart").filter(function (d, i) {
                return this.name === "chart_" + panelid;
            });

            var itemgroup = stack.selectAll("g.layer").filter(function (d, i) {
                return (i == id);
            });

            itemgroup.style("fill", function (d, i) {
                    if (n < 2) return color(0);
                    return color(id / (n - 1));
                }

            );
        }

        var clickedACheckbox = function () {
            var thecheckbox = d3.select(this);
            var thecheckbox2 = this;
            var id = this.id.substring(3);
            var holder = barChartDataHolder[this.value];
            holder.visualData.series[id].active = this.checked;
            filterChart(getBarChartGraphics(this.value));
        }

        if (n > 1)  //no point putting in checkboxes if there's only one element in the scope.
        {
            //chartData.seriesName
            var checkboxes = d3.selectAll("#chart").filter(function (d, i) {
                return this.name === svgContainer.name;
            })
                .append("div")
                .attr("class", output.contentPaneId)
                .style("padding-left", 10)
                .style("padding-top", "10");

             checkboxes.append("div")
                 .style("padding-bottom", 10)
                 .append("label")
                 .attr("class", "axisLabel")
                 .text("Series: " + chartData.seriesName) ;

            var actualCheckboxes = checkboxes.selectAll("text.input")
                    .data(items)
                    .enter().append("div")
                   .style("float", "left")
                    .attr("class", function (d, i) {
                        return i;
                    })
                    .style("padding-right", 20)
                    .style("padding-bottom", 20)
                    .on('mouseover', synchronizedMouseOver)
                    .on("mouseout", synchronizedMouseOut)
                ;

            actualCheckboxes.append("input")
                .attr("type", "checkbox")
                .attr("id", function (d, i) {
                    return "cb_" + i;
                })
                .attr("checked", true)
                .on("click", clickedACheckbox)
                .attr("value", output.contentPaneId);

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
                    if (n < 2) return color(0);
                    return color(i / (n - 1));
                });

            actualCheckboxes.append("label")
                .text(function (d, i) {
                    return items[i].seriesValue;
                })
                .attr("for", function (d, i) {
                    return "cb_" + i;
                });
            

            output.d3_legend = legends;
            output.d3_legendlabel = legend_labels;
        }
        output.d3_svg =  vis_pre[0][0];

        saveBarChartData(output.contentPaneId, output);

        //rebuild height
        if(null!=xLabelCharLength && xLabelCharLength>15){

            vis_pre.attr("height", height + theOffset + 52);

//            var svgDom = dojo.query("#chart>svg");     //NOTE: This doesn't work on Chrome.  Using d3 to set height instead.
//            if(null!=svgDom && svgDom.length==1){
//                svgDom[0].height = 550;
//                svgDom[0].height.baseVal.value = 550;
//                svgDom[0].height.animVal.value = 550;
//            }
        }

 }

function isHaveCategory(visualdata){
    if(visualdata.seriesName!=null && visualdata.xAxisTitle!=null && visualdata.seriesName!=visualdata.xAxisTitle){
          return true;
    }
    if(null!=visualdata && null!=visualdata.opeatorInputs && visualdata.opeatorInputs.length>0){
        var opInputs = visualdata.opeatorInputs;
        for(var i=0;i<opInputs.length;i++){
            if(dojo.trim(opInputs[i][0])=="categoryType"){
              if(dojo.trim(opInputs[i][1])!=""){
                  return true;
              }else{
                  return false;
              }
            }
        }
        return false;
    }else{
        return false;
    }
}

function isHaveSeries(visualdata){
    if(visualdata.seriesName!=null && visualdata.xAxisTitle!=null && visualdata.seriesName!=visualdata.xAxisTitle){
        return true;
    }
    if(null!=visualdata && null!=visualdata.opeatorInputs && visualdata.opeatorInputs.length>0){
        var opInputs = visualdata.opeatorInputs;
        for(var i=0;i<opInputs.length;i++){
            if(dojo.trim(opInputs[i][0])=="scopeDomain"){
                if(dojo.trim(opInputs[i][1])!=""){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }else{
        return false;
    }
}

function countMaxBarNumber(items) {
    var maxBarNumbers = 0;
    if (items) {
        for (var count = 0; count < items.length; count++) {
            var item = items[count];
            if (maxBarNumbers < item.YValues.length) {
                maxBarNumbers = item.YValues.length;
            }

        }
        return maxBarNumbers;

    }
}

function getMaxLabelSize(labelsData){
    var number = 0;
    if(null!=labelsData && labelsData.length>0){
        for(var i=0;i<labelsData.length;i++){
            if(labelsData[i]!=null && labelsData[i][1]!=null && labelsData[i][1].length>number){
                number = labelsData[i][1].length;
            }
        }
    }
    return number
}

var barChartDataHolder = new Array();
var barChartStateHolder = new Array();

/**
 *  initialize checkboxes at bottom of graph
 */
function initCheckboxes(chartDiv, panelId, outputdata) {
    var checkboxContainer = dojo.create("div", {}, chartDiv);
//	var switchSel = new dijit.form.Select({
//		options: [
//			{ label: alpine.nls.chart_barchart_direction_horizontal, value: 'horizontal', selected: true },
//			{ label: alpine.nls.chart_barchart_direction_vertical, value: 'vertical' }
//		]
//	});


    var chk = dojo.create("input", {id:"cbox", type:"checkbox"}, checkboxContainer);
    var lbl = dojo.create("label", {innerHTML:"Check me", "for":"cbox"}, checkboxContainer);
    new dijit.form.CheckBox({}, chk);

    var chk2 = dojo.create("input", {id:"cbox2", type:"checkbox"}, checkboxContainer);
    var lgltest = dojo.create("label", {innerHTML:"2222", "for":"chbox2"}, checkboxContainer);
    var lbl2 = dojo.create("label", {innerHTML:"Check me", "for":"cbox2"}, checkboxContainer);
    new dijit.form.CheckBox({}, chk2);

//	var horizontalOp = document.createElement("option");
//	var verticalOp = document.createElement("option");
//	horizontalOp.value = "";
//	horizontalOp.appendChild(document.createTextNode(alpine.nls.chart_barchart_direction_horizontal));
//	
//	verticalOp.value = "stack";
//	verticalOp.appendChild(document.createTextNode(alpine.nls.chart_barchart_direction_vertical));
//	
//	switchSel.appendChild(verticalOp);
//	switchSel.appendChild(horizontalOp);
//
//	var label = document.createElement("label");
//	label.innerHTML = ("<a>" + alpine.nls.chart_barchart_direction_title + " </a>");
//	checkboxContainer.appendChild(label);
//	checkboxContainer.appendChild(switchSel);
}

/**
 * initialize direction switch
 */
function initializeSwitch(drawPanel, panelId, outputdata) {
    if (!dojo.isIE) return;
    var selectContainer = dojo.create("div", {}, drawPanel);
//	var switchSel = new dijit.form.Select({
//		options: [
//			{ label: alpine.nls.chart_barchart_direction_horizontal, value: 'horizontal', selected: true },
//			{ label: alpine.nls.chart_barchart_direction_vertical, value: 'vertical' }
//		]
//	});

//    if (!dojo.isIE)
//    {
//        selectContainer.innerHTML = "<button class=\"first\" id=\"group\" onclick=\"transitionGroup()\">Group</button><button class=\"last active\" id=\"stack\" onclick=\"transitionStack()\">Stack</button>);
//
//    }



    var currentState = getBarChartState(panelId) == undefined ? "vertical" : getBarChartState(panelId);
    var switchSel = document.createElement("select");

    var horizontalOp = document.createElement("option");
    var verticalOp = document.createElement("option");
    horizontalOp.value = "group";
    horizontalOp.appendChild(document.createTextNode(alpine.nls.chart_barchart_direction_horizontal));

    verticalOp.value = "stack";
    verticalOp.appendChild(document.createTextNode(alpine.nls.chart_barchart_direction_vertical));

    if (currentState == "group") {
        horizontalOp.selected = true;
    } else if (currentState == "vertical") {
        verticalOp.selected = true;
    }

    switchSel.appendChild(verticalOp);
    switchSel.appendChild(horizontalOp);

    var label = document.createElement("label");
    label.innerHTML = ("<a>" + alpine.nls.chart_barchart_direction_title + " </a>");
    selectContainer.appendChild(label);
    selectContainer.appendChild(switchSel);

    function switcherSelChange() {
        var val = switchSel.value;
        var outputdata = getBarChartData(panelId);
        saveBarChartState(panelId, val);
        if (val === "stack") {
            transitionStack(getBarChartGraphics(panelId));
        } else {
            transitionGroup(getBarChartGraphics(panelId));
        }
        //var chartComponent = _buildBarChartByDirection(val, outputdata.visualData);

        //drawPanel.removeChild(drawPanel.children[1]);//remove chart container from root container.

        //var chartContainer = dojo.create("div", {}, drawPanel);//insert a new container into root container
        //fillOutPaneBarChart(chartContainer, outputdata, chartComponent);
    }

    if (window.addEventListener) { // Mozilla, Netscape, Firefox
        switchSel.addEventListener('change', switcherSelChange, false);
    } else {// IE
        switchSel.attachEvent('onchange', switcherSelChange);
    }
//	switchSel.placeAt(dojo.create("div", {style: {height: "300px"}}, selectContainer));
//	result_event_handlers.push(dojo.connect(switchSel, "onChange", switchSel, function(val){
//		var outputdata = getBarChartData(panelId);
//		var chartComponent = _buildBarChartByDirection(val, outputdata.visualData);
//		
//		drawPanel.removeChild(drawPanel.children[1]);//remove chart container from root container.
//
//		var chartContainer = dojo.create("div", {}, drawPanel);//insert a new container into root container
//		fillOutPaneBarChart(chartContainer, outputdata, chartComponent);
//	}));

    saveBarChartData(panelId, outputdata);

    return _buildBarChartByDirection(currentState, outputdata.visualData);//default horizontal
}

function saveBarChartData(paneId, visualData) {
    barChartDataHolder[paneId] = visualData;
}

function getBarChartData(paneId) {
    return barChartDataHolder[paneId];
}

function saveBarChartState(id, stateValue) {
    barChartStateHolder[id] = stateValue;
}

function getBarChartState(id) {
    return barChartStateHolder[id];
}

function getBarChartGraphics(id) {
    var thegraphics = barChartGraphicsHolder[id];
    return    thegraphics;
}
function saveBarChartGraphics(id, graphics) {
    barChartGraphicsHolder[id] = graphics;
}

function _buildBarChartByDirection(direction, visualData) {
    var plot, xAxis, yAxis,
        displayBox = {width:"100%", height:"100%"};
    //for prcesion bar width display
    var plotv, ploth;
    if (null != visualData && visualData.series != null && visualData.series.length < 10) {
        plotv = {
            type:"ClusteredColumns",
            gap:10,
            minBarSize:15,
            maxBarSize:20
        };
        ploth = {
            type:"ClusteredBars",
            gap:10,
            minBarSize:15,
            maxBarSize:20
        };
    } else {
        plotv = {
            type:"ClusteredColumns"
        };
        ploth = {
            type:"ClusteredBars"
        };
    }


    switch (direction) {
        default:
        case "vertical":
            plot = plotv;
            xAxis = {
                title:visualData.xAxisTitle,
                titleFontColor:"green",
                titleOrientation:orientation_away,
                fixLower:"minor",
                //don't show the  max value (than the real max)
                fixUpper:"minor",
                //keep the x axis clean (no minor x value)
                natural:true,
                rotation:visualData.xLableRotation,
                majorTickStep:1,
                htmlLabels:false,
                labels:getXLabels(visualData, true)
            };
            yAxis = {
                title:visualData.yAxisTitle,
                titleFontColor:"green",
                titleOrientation:orientation_axis,
                vertical:true,
                fixLower:"major",
                fixUpper:"major",
                htmlLabels:false,
                includeZero:true
            };
            if (visualData.width && visualData.width != 0) {
                displayBox.width = visualData.width;
                displayBox.height = visualData.height;
            }
            break;
        case "horizontal":
            plot = ploth;
            xAxis = {
                title:visualData.yAxisTitle,
                titleFontColor:"green",
                titleOrientation:orientation_away,
                fixLower:"major",
                fixUpper:"major",
                htmlLabels:false,
                includeZero:true
            };
            yAxis = {
                title:visualData.xAxisTitle,
                titleFontColor:"green",
                titleOrientation:orientation_axis,
                vertical:true,
                fixLower:"minor",
                //don't show the  max value (than the real max)
                fixUpper:"minor",
                //keep the x axis clean (no minor x value)
                natural:true,
                rotation:visualData.xLableRotation,
                majorTickStep:1,
                htmlLabels:false,
                labels:getXLabels(visualData, true)
            };
            if (visualData.width && visualData.width != 0) {
                displayBox.height = visualData.width;
                displayBox.width = visualData.height;
            }
            break;
    }
    return {
        plot:plot,
        xAxis:xAxis,
        yAxis:yAxis,
        displayBox:displayBox
    };
}