/**
 * User: sasher
 * Date: 11/16/12
 * Time: 4:07 PM
 */
define([
    "dojo/_base/lang",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/on",
    "dijit/popup",
    "alpine/layout/InlineEdit/InlineEditTTDialog"
], function (lang, domConstruct, domClass, on, Popup, Tooltip) {

    function _getNumberOfActiveSeries(boxWhiskerGroups) {
        var count = 0;
        for (var i = 0; i < boxWhiskerGroups.length; i++) {
            //a group is a series of boxwhisker graph
            if (boxWhiskerGroups[i].active) {
                count++;
            }

        }

        return count;
    }


    function _checked(panelId, seriesIndex, checked, totalBoxWidth, boxGroups, totalX, maxXLabelLength, heightOfGraph) {
        var seriesTranslateArray = [];
        var topIndex = boxGroups.length - 1;
        var numActive = 0;
        for (var i = topIndex; i >= 0; i--) {
            seriesTranslateArray[i] = 0;
            if (boxGroups[i].active) numActive++;
            for (var j = topIndex; j > i; j--) {
                if (!seriesTranslateArray[j]) seriesTranslateArray[j] = 0;
                if (!boxGroups[i].active) {
                    seriesTranslateArray[j]++;
                }
            }

        }


        d3.selectAll("line")
            .filter(function () {
                return this.id.indexOf(panelId + "_line_") == 0;
            })
            .style("opacity", 0.0)
            .attr("x1", function (d, i) {
                return (i + 1) * ( totalBoxWidth * numActive);
            })
            .attr("x2", function (d, i) {
                return (i + 1) * ( totalBoxWidth * numActive);
            })
            .transition()
            .delay(1000)
            .style("opacity", 1.0)
        ;


        d3.selectAll("g")
            .filter(function () {
                //return this.id.indexOf(panelId + "_bar_" + seriesIndex)==0;
                return this.id.indexOf(panelId + "_bar_") == 0;
            })
            .transition()
            .duration(1000)
            .attr("transform", function (d) {
                var xIndex = d.xIndex;
                var currentSeries = d.seriesIndex;
                var translationValue = totalBoxWidth * (seriesTranslateArray[currentSeries] + xIndex * (boxGroups.length - numActive));
                return "translate(-" + translationValue + ",0)"
            })
            .style("display", function (d) {
                if (boxGroups[d.seriesIndex].active)
                    return "inline";
                else
                    return "none";
            })
            .style("opacity", function (d) {
                if (boxGroups[d.seriesIndex].active)
                    return 1.0;
                else
                    return 0.0;
            })
        ;


        d3.selectAll("line")
            .filter(function () {
            return this.id.indexOf(panelId + "_horizline") == 0;
        })
            .attr("x2", totalBoxWidth * numActive * totalX);

        d3.selectAll("svg").filter(function (d, i) {
            return this.id === "svg_" + panelId;
        })
            .attr("width", 160 + totalBoxWidth * numActive * totalX)//extra 80 on each side, just to leave room for axises
        ;


        var shouldRotate = false;
        if (maxXLabelLength * 6 > totalBoxWidth * numActive) shouldRotate = true;
        var theOffset = 6;
        var anchor = "middle"
        var xHeaderY = heightOfGraph + theOffset + 24;
        if (shouldRotate) {
            theOffset = maxXLabelLength * 2.75 / 2;
            anchor = "end";
            xHeaderY += maxXLabelLength * 3;
        }

        d3.selectAll("text")
            .filter(function () {
            return this.id.indexOf(panelId + "_xLabel_") == 0;
        })
            .style("opacity", 0.0)
            .attr("y", heightOfGraph + theOffset)
            .attr("text-anchor", anchor)
            .attr("x", function (d, i) {
                return totalBoxWidth * i * numActive + numActive * totalBoxWidth / 2;
            })
            .attr("transform", function (d, i) {
                if (!shouldRotate) return "";
                return "rotate(-30," + (totalBoxWidth * i * numActive) + "," + (heightOfGraph + theOffset / 2) + ")";
            })
            .transition()
            .delay(1000)
            .style("opacity", function () {
                if (numActive > 0)return 1.0;
                else return 0.0;
            }
        )

        ;

        d3.selectAll("text")
            .filter(function () {
            return this.id.indexOf(panelId + "_xHeader_") == 0;
        })
            .style("opacity", 0.0)
            .attr("x", function (d, i) {
                return totalBoxWidth * numActive * totalX / 2;
            })
            .attr("y", function () {
                return xHeaderY;
            })
            .transition()
            .delay(1000)
            .style("opacity", function () {
                if (numActive > 0)return 1.0;
                else return 0.0;
            }
        )

        ;

    }


    function _setActiveGroups(boxWhiskerGroups) {
        var activeBoxGroups = [];
        for (var i = 0; i < boxWhiskerGroups.length; i++) {
            //a group is a series of boxwhisker graph
            var boxwhiskerGroup = boxWhiskerGroups[i];

            if (boxwhiskerGroup.active) {
                activeBoxGroups.push(boxwhiskerGroup);
            }

        }
        return activeBoxGroups;
    }

    function fillOutPaneBoxAndWhisker(outpane, output, panelid) {
        console.log("fillOutPaneBoxAndWhisker");
        var isForExport = isExportingReport;
        var chartData = output.visualData;
        var chartDiv = domConstruct.create("div");

        if (chartData.description) {
            domConstruct.create(TAG_DIV, {innerHTML:"<STRONG>" + chartData.description + "</STRONG>"}, chartDiv);
        }
        if (chartData.errorMessage && chartData.errorMessage.length > 0) {
            var errorsDiv = domConstruct.create(TAG_DIV, {id:"err_" + new Date().getTime()}, chartDiv);
            fillVisualErrorMessages(errorsDiv, chartData.errorMessage);
        }


        var contentDiv = domConstruct.create("div", {}, chartDiv);
        contentDiv.name = "panel_" + panelid;
        var colsDiv = domConstruct.create("div", {}, chartDiv);
        var lengendDiv = domConstruct.create("div", {}, chartDiv);
        outpane.setContent(chartDiv);

        outpane.startup();

        var boxWhiskerModel = output.visualData;
        var heightOfGraph = 400;

        var boxWidth = 40;
        var boxPadding = 15;
        var totalBoxWidth = boxWidth + 2 * boxPadding;
        var minBoxPadding = 10;

        var color = d3.interpolateRgb(barColorStart, barColorEnd);
        var hcolor = d3.interpolateRgb(highlightedBarColorStart, highlightedBarColorEnd);


        var boxWhiskerGroups = boxWhiskerModel.boxWhiskers;

        var numTotalSeries = boxWhiskerGroups.length;
        var numActive = _getNumberOfActiveSeries(boxWhiskerGroups);

        var topPadding = 40;
        ///////////////////////////


        var legends = [];
        var legend_labels = [];
        for (var count=0; count < boxWhiskerModel.boxWhiskers.length; count++)
        {
            legends.push(createBarLegend(boxWhiskerModel.boxWhiskers.length < 2 ? color(0) : color(count / (boxWhiskerModel.boxWhiskers.length - 1))));
            legend_labels.push( boxWhiskerModel.boxWhiskers[count].series);
        }

        if (boxWhiskerModel.boxWhiskers.length > 1)  //no point putting in checkboxes if there's only one element in the scope.
        {
            var clickedACheckbox = function () {
                var id = this.id.substring(3);
                //   var holder = getLineChartGraphics(this.value);
                //  holder.lines[id].active = this.checked;
                boxWhiskerModel.boxWhiskers[id].active = this.checked;
                _checked(panelid, id, this.checked, totalBoxWidth, boxWhiskerModel.boxWhiskers, boxWhiskerModel.xValues.length, maxXLabelLength, heightOfGraph);
                console.log("clicked a checkbox + " + id);
                //   transitionLine(this.value, id);
            }

            var checkboxes = d3.selectAll("div").filter(function (d, i) {
                return this.name === contentDiv.name;
            })
                .append("div")
                .attr("class", output.contentPaneId)
                .style("padding-left", 10)
                .style("padding-top", 10)
                .style("position", "fixed")
                .style("background", "#FFFFFF");


            checkboxes.append("div")
                .style("padding-bottom", 10)
                .append("label")
                .attr("class", "axisLabel")
                .text(alpine.nls.series + ": " + boxWhiskerModel.seriesDomain);

            var actualCheckboxes = checkboxes.selectAll("text.input")
                    .data(boxWhiskerModel.boxWhiskers)
                    .enter().append("div")
                    .style("float", "left")
                    .attr("class", function (d, i) {
                        return i;
                    })
                    .style("padding-right", 20)
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
                    if (numTotalSeries < 2) return color(0);
                    return color(i / (numTotalSeries - 1));
                });

            actualCheckboxes.append("label")
                .text(function (d, i) {
                    return d.series;
                })
                .attr("for", function (d, i) {
                    return "cb_" + i;
                });


            topPadding = 20 + checkboxes[0][0].offsetHeight;
        }

        //set up outer frame for graph
        var vis_pre = d3.selectAll("div").filter(function (d, i) {
                return this.name === contentDiv.name;
            })
                .append("svg")
                .attr("id", "svg_" + panelid)
                .attr("width", 160 + totalBoxWidth * numActive * boxWhiskerModel.xValues.length)//extra 80 on each side, just to leave room for axises
                .attr("height", heightOfGraph + 100 + topPadding)//extra 20 on top, extra 80 at bottom for axises
            ;
        var vis = vis_pre.append("g")
            .attr("transform", "translate(" + 80 + "," + topPadding + ")");
        ;


        // y-axis
        var yScale = d3.scale.linear().domain([chartData.minY, chartData.maxY]).range([ heightOfGraph, 0]);
        var yScaleFactor = (chartData.maxY - chartData.minY, chartData.maxY ) / heightOfGraph;
        var ticksY = 20;

        var yAxis = d3.svg.axis()
            .scale(yScale)
            .ticks(ticksY)
            .orient("left");


        var ySide = vis.append("g")
            .attr("class", "y axis")
            .call(yAxis);


        var xValue = getYAxisLabelXValue(ySide);

        vis.append("svg:text")
            .attr("class", "axisLabel")
            .text(chartData.valueDomain)
            .attr("x", xValue)
            .attr("y", heightOfGraph / 2)
            .attr("text-anchor", "middle")
            .attr("dy", ".35em")
            .attr("transform", "rotate(-90," + xValue + "," + heightOfGraph / 2 + ")")
        ;

        //x-axis.

        var layers = vis.selectAll("g.layer")
            .data(boxWhiskerGroups)
            .enter().append("g")
            .style("stroke", function (d, i) {
                console.log("color for group: " + i);
                if (numTotalSeries < 2) return color(0);
                return color(i / (numTotalSeries - 1));

            })
            .style("fill", function (d, i) {
                console.log("color for group: " + i);
                if (numTotalSeries < 2) return color(0);
                return color(i / (numTotalSeries - 1));

            })
            .style("fill-opacity", .5)
            .attr("basicColor", function (d, i) {
                if (numTotalSeries < 2) return color(0);
                return color(i / (numTotalSeries - 1));
            })
            .attr("class", "layer");

        //breaks between x values
        var vertLines = vis.selectAll("g.vertLines")
            .data(function () {
                return boxWhiskerModel.xValues;
            }).enter();

        vertLines.append("line")
            .attr("id", function () {
                return panelid + "_line_" + i;
            })
            .style("stroke", axisColor)
            .attr("x1", function (d, i) {
                console.log("line " + i + " at: " + (i + 1) * ( totalBoxWidth * numActive));
                return (i + 1) * ( totalBoxWidth * numActive);
            })
            .attr("x2", function (d, i) {
                return (i + 1) * ( totalBoxWidth * numActive);
            })
            .attr("y1", 0)
            .attr("y2", heightOfGraph);


        //now we need the labels

        var maxXLabelLength = getMaxLengthOfStrings(boxWhiskerModel.xValues);
        console.log("maxXLabelLength: " + maxXLabelLength);

        var shouldRotate = false;
        if (maxXLabelLength * 6 > totalBoxWidth * numActive) shouldRotate = true;
        var theOffset = 6;
        var anchor = "middle"
        var xHeaderY = heightOfGraph + theOffset + 24;

        if (shouldRotate) {
            theOffset = maxXLabelLength * 2.75 / 2;
            anchor = "end";
            xHeaderY += maxXLabelLength * 3;
        }

        vis.append("line")
            .attr("id", panelid + "_horizline")
            .style("stroke", axisColor)
            .attr("x1", 0)
            .attr("x2", totalBoxWidth * numActive * boxWhiskerModel.xValues.length)
            .attr("y1", heightOfGraph)
            .attr("y2", heightOfGraph);

        //the labels for the bins
        vertLines.append("text")
            .attr("id", function (d) {
                return panelid + "_xLabel_" + i;
            })
            .attr("class", "label")
            .attr("x", function (d, i) {
                return totalBoxWidth * i * numActive + numActive * totalBoxWidth / 2;
            })
            .attr("y", heightOfGraph + theOffset)
            .attr("dy", ".71em")
            .attr("text-anchor", anchor)
            .attr("transform", function (d, i) {
                if (!shouldRotate) return "";
                return "rotate(-30," + (totalBoxWidth * i * numActive) + "," + (heightOfGraph + theOffset) + ")";
            })
            .text(function (d, i) {
                return d;
            });


        if (boxWhiskerModel.typeDomain && boxWhiskerModel.typeDomain.length > 0) {
            vis.append("svg:text")
                .attr("id", function (d) {
                    return panelid + "_xHeader_" + i;
                })
                .attr("class", "axisLabel")
                .text(boxWhiskerModel.typeDomain)
                .attr("x", totalBoxWidth * numActive * boxWhiskerModel.xValues.length / 2)
                .attr("y", xHeaderY)
                .attr("text-anchor", "middle")
                .attr("dy", ".35em");
        }


        var bars = layers.selectAll("g.bar")
                .data(function (d) {
                    return d.boxWhiskers;
                })
                .enter().append("g")
                .attr("class", "bar")
                .attr("id", function (d) {
                    return panelid + "_bar_" + d.seriesIndex + "_" + d.xIndex;
                })
            ;


        for (var j = 0; j < boxWhiskerGroups.length; j++) {
            var boxWhiskers = boxWhiskerGroups[j].boxWhiskers;
            for (var i = 0; i < boxWhiskers.length; i++) {
                var box = boxWhiskers[i];
                var dojoid = panelid + "_bar_" + box.seriesIndex + "_" + box.xIndex;
                var tt2xd = new Tooltip({

                    innerHTML:"<div class='inlineEditTTDialogContainer'><table class='popupTable'>"
                        + (boxWhiskerGroups.length >= 2 && boxWhiskerGroups[j].series ?
                                "<tr><td class='popupTableLeft'>" + alpine.nls.series + ": </td><td>"
                                    + boxWhiskerGroups[j].series.substring(0,15)
                                    + "</td></tr>"
                                    //+ "<tr><td colspan='2' style='text-align:center;'>---</td></tr>"
                                : "")
                        + "<tr><td class='popupTableLeft'>" + alpine.nls.box_plot_max + ": </td><td>"
                        + box.uwhisker
                        + "</td></tr><tr><td  class='popupTableLeft'>" + alpine.nls.box_plot_75 + ": </td><td>"
                        + box.ubox
                        + "</td></tr><tr><td  class='popupTableLeft'>" + alpine.nls.box_plot_median + ": </td><td>"
                        + box.median
                        + "</td></tr><tr><td  class='popupTableLeft'>" + alpine.nls.box_plot_25 + ": </td><td>"
                        + box.lbox
                        + "</td></tr><tr><td class='popupTableLeft'>" + alpine.nls.box_plot_min + ": </td><td>"
                        + box.lwhisker
                        + "</td></tr>"
                        + "<tr><td colspan='2' style='text-align:center;'>---</td></tr>"
                        + "<tr><td class='popupTableLeft'>" + alpine.nls.box_plot_mean + ": </td><td>"
                        + Math.round(box.mean * 10000) / 10000
                        + "</td></tr></table></div>"
                });


                var openMe = function (dojoid, tt2xd, event) {
                    Popup.open({ popup:tt2xd, padding:{x:10, y:10}, x:event.pageX, y:event.pageY});

                };
                var closeMe = function (tt2xd) {
                    Popup.close(tt2xd);
                };

                on(dojo.byId(dojoid), "mouseover", lang.partial(openMe, dojoid, tt2xd));
                on(dojo.byId(dojoid), "mouseout", lang.partial(closeMe, tt2xd));

            }

        }


        //upper whisker
        bars.append("line")
            .attr("x1", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth / 2);
            })
            .attr("x2", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth / 2);
            })
            .attr("y1", function (d) {
                return yScale(d.uwhisker);
            })
            .attr("y2", function (d) {
                return yScale(d.ubox);
            });


        //lower whisker
        bars.append("line")
            .attr("x1", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth / 2);
            })
            .attr("x2", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth / 2);
            })
            .attr("y1", function (d) {
                return yScale(d.lwhisker);
            })
            .attr("y2", function (d) {
                return yScale(d.lbox);
            });


        //max value
        bars.append("line")
            .attr("x1", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + boxPadding);
            })
            .attr("x2", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth - boxPadding);
            })
            .attr("y1", function (d) {
                return yScale(d.uwhisker);
            })
            .attr("y2", function (d) {
                return yScale(d.uwhisker);
            });


        //min value
        bars.append("line")
            .attr("x1", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + boxPadding);
            })
            .attr("x2", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth - boxPadding);
            })
            .attr("y1", function (d) {
                return yScale(d.lwhisker);
            })
            .attr("y2", function (d) {
                return yScale(d.lwhisker);
            });


        //the box
        bars.append("rect")
            .attr("width", boxWidth)//width is 90%
            .attr("x", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + boxPadding);
            })
            .attr("y", function (d) {
                return yScale(d.ubox);
            })
            .attr("height", function (d) {
                return (d.ubox - d.lbox) / yScaleFactor;
            });

        //median value
        bars.append("line")
            .attr("x1", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + boxPadding);
            })
            .attr("x2", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth - boxPadding);
            })
            .attr("y1", function (d) {
                return yScale(d.median);
            })
            .attr("y2", function (d) {
                return yScale(d.median);
            });

        //mean value
        bars.append("circle")
            .attr("cx", function (d) {
                return totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + totalBoxWidth / 2;
            })
            .attr("cy", function (d) {
                return yScale(d.mean);
            })
            .attr("r", 3)
            .attr("pointer-events", "none");


        //white background box
        bars.append("rect")
            .attr("id", function (d) {
                return panelid + "_bgRect_" + d.seriesIndex + "_" + d.xIndex;
            })
            .attr("width", boxWidth + minBoxPadding)
            .attr("x", function (d) {
                return (totalBoxWidth * d.seriesIndex + totalBoxWidth * d.xIndex * numActive + minBoxPadding);
            })
            .attr("y", function (d) {
                return yScale(d.uwhisker) - 15;
            })
            .attr("height", function (d) {
                return (d.uwhisker - d.lwhisker) / yScaleFactor + 30;
            })
            .style("stroke", "#FFFFFF")
            .style("fill", "#FFFFFF")
            .style("opacity", 0.0);


          output.d3_legend = legends;
          output.d3_legendlabel = legend_labels;
          output.d3_svg =  vis_pre[0][0];
    }


    return {
        fillOutPaneBoxAndWhisker:fillOutPaneBoxAndWhisker
    }

});