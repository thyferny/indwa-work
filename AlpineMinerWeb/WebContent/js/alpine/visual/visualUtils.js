
function fillToolTip(tooltip,tooltipArray){
//	if(dojo.isIE){
		var tooltipText="";
	 
		for ( var j = 0; j < tooltipArray.length; j++) {
			tooltipText=tooltipText+"<tr><td>"+tooltipArray[j]+"</td></tr>";
		}
		 
		var div = document.createElement("div");
		div.innerHTML="<table border=1 bordercolor=red cellpadding=1 rules =none cellspacing=0 bgcolor='yellow'>"+tooltipText+"</table>";
 		tooltip.domNode.appendChild(div);
	//}
}

function getRand(from, to){
	return Math.random() * (to - from) + from;
}

function randColor(alpha,sed){
	var red   = Math.floor(getRand(0, 255)),
		green = Math.floor(getRand(0, 255)),
		blue  = Math.floor(getRand(0, 255)),
		opacity = alpha ? getRand(0.1, 0.5) : 1;
		if(sed%3==1){
			blue=blue/10;
		}else if(sed%3==2){
			green=green/10;
		}else{
			red=red/10;
		}
	return [red, green, blue, opacity];
}

function colorList(x){
    var colorFn = _interpolateColors(clusterColorStart, clusterColorEnd);
    return colorFn(x);


    function _interpolateColors(a, b) {
        a = d3.rgb(a);
        b = d3.rgb(b);
        var ar = a.r, ag = a.g, ab = a.b, br = b.r - ar, bg = b.g - ag, bb = b.b - ab;
        return function(t) {
            return [Math.round(ar + br * t),Math.round(ag + bg * t), Math.round(ab + bb * t), 255];
        };
    }
    /*var r = 0;
    var g = 0;
    var b = 0;
    if (x >= 0.0 && x < 0.2) {
        x = x / 0.2;
        r = 0.0;
        g = x;
        b = 1.0;
    } else if (x >= 0.2 && x < 0.4) {
        x = (x - 0.2) / 0.2;
        r = 0.0;
        g = 1.0;
        b = 1.0 - x;
    } else if (x >= 0.4 && x < 0.6) {
        x = (x - 0.4) / 0.2;
        r = x;
        g = 1.0;
        b = 0.0;
    } else if (x >= 0.6 && x < 0.8) {
        x = (x - 0.6) / 0.2;
        r = 1.0;
        g = 1.0 - x;
        b = 0.0;
    } else if (x >= 0.8 && x <= 1.0) {
        x = (x - 0.8) / 0.2;
        r = 1.0;
        g = 0.0;
        b = x;
    }
    return [r*230,g*230,b*230,50];*/

}


function countMaxX(allNodes){
	var maxX=0;
	for ( var i = 0; i < allNodes.length; i++) {
		if(allNodes[i].xGrid>maxX){
			maxX=allNodes[i].xGrid;
		}
	}
	return maxX;
}
 
function countMaxY(allNodes){
	var maxY=0;
	for ( var i = 0; i < allNodes.length; i++) {
		if(allNodes[i].yGrid>maxY){
			maxY=allNodes[i].yGrid;
		}
	}
	return maxY;
}


function getXLabels(chartData, includeZeroZero) {

	if(!chartData||!chartData.xLabels){
		return null;
	}
	// labels for x
	var xValues = chartData.xLabels;
 	return createLabels(xValues,includeZeroZero);
}

function getYLabels(chartData, includeZeroZero) {

	if(!chartData||!chartData.yLabels){
		return null;
	}
	// labels for x
	var yValues = chartData.yLabels;
	 	return createLabels(yValues,includeZeroZero);
	}

function createLabels(values,includeZeroZero){ 
	var xLabels = new Array();
	var i = 0;
	var delta = 0;
	if (includeZeroZero) {
		// here generate the x labels
		xLabels[0] = {
			value : 0,
			text : ""
		};
		i = 1;
		delta = 1;
	}
	for (; i < values.length+delta; i++) {
		xLabels[i] = {
			value : values[i - delta][0],
			text : values[i - delta][1]
		};
	}
	return xLabels;
	
}

function addSpecialParameter4Axis(xAxis,yAxis,chartData){
	if(chartData.xMajorTickStep){
		xAxis.majorTickStep=1*chartData.xMajorTickStep;
	}
	if(chartData.xMinorTickStep){
		xAxis.minorTickStep=1*chartData.xMinorTickStep;
	}

	if(chartData.minX){
		xAxis.min=1*chartData.minX;
	}
	if(chartData.maxX){
		xAxis.max=1*chartData.maxX;
	}

	if(chartData.minY){
		yAxis.min=1*chartData.minY;
	}
	if(chartData.maxY){
		yAxis.max=1*chartData.maxY;
	}
	if(chartData.yMajorTickStep){
		yAxis.majorTickStep=1*chartData.yMajorTickStep;
	}
	if(chartData.yMinorTickStep){
		yAxis.minorTickStep=1*chartData.yMinorTickStep;
	}
}

function getMaxStringLengthFromSide(theSide)
{
    var maxLength  = 0;
    if(theSide!=null && theSide.node!=null && theSide.node()!=null){
        var nodes = theSide.node().childNodes;
        if (nodes)
        {
            for (var count=0; count< nodes.length;count++)
            {
                var newLength =   nodes[count].textContent.length;
                if (newLength > maxLength) maxLength = newLength;
            }
        }
    }
    return maxLength;

}

function getYAxisLabelXValue(ySide)
{
    var lengthOfTick = getMaxStringLengthFromSide(ySide);
    if (lengthOfTick < 3) lengthOfTick= 4;
    return  (-6 * lengthOfTick) - 14;
}

function getMaxLengthOfStrings(values)
{
    var maxWidthOfStrings = 0;
    for (var count =0; count < values.length; count++)
    {   if(values[count]==null){continue;}
        var tempWidth = values[count].length;
        if (tempWidth > maxWidthOfStrings) maxWidthOfStrings = tempWidth;
    }

    return maxWidthOfStrings;

}

function createLineLegend(theColor)
{
   return  "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\" overflow=\"hidden\" width=\"18\" height=\"18\"><defs></defs><line fill=\"none\" fill-opacity=\"0\" stroke=\"" + theColor + "\" stroke-opacity=\"1\" stroke-width=\"1.5\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-miterlimit=\"4\" x1=\"0\" y1=\"9\" x2=\"18\" y2=\"9\" stroke-dasharray=\"none\" style=\"stroke:"+theColor+"\" ></line></svg>";
}


function createBarLegend(theColor)
{
    return "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\" overflow=\"hidden\" width=\"18\" height=\"18\"><defs></defs><rect fill=\"" + theColor + "\" fill-opacity=\"1\" stroke=\"" + theColor + "\" stroke-opacity=\"1\" stroke-width=\"1.5\" stroke-linecap=\"butt\" stroke-linejoin=\"miter\" stroke-miterlimit=\"4\" x=\"2\" y=\"2\" width=\"14\" height=\"14\" ry=\"0\" rx=\"0\" fill-rule=\"evenodd\" stroke-dasharray=\"none\" ></rect></svg>";
}

