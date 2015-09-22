var color_network_link=[120, 154, 201];//"red";
//var color_network_node=[30, 130, 255, 0.4];
//color_network_node_threshhold=[210, 20, 225, 0.3];
var color_network_node_stroke=[120, 154, 201];//[100, 100, 255, 0.8];
var color_network_node_text="#4F81BD";//"blue";



var network_node_r=25;

function drawNetworkLink(surface,startRect,endRect,link){
	var x1=startRect.shape.cx+network_node_r;
	var y1=startRect.shape.cy;
	var x2=endRect.shape.cx-network_node_r;
	var y2=endRect.shape.cy;
	surface.createLine({x1: x1, y1: y1, x2: x2, y2: y2}).setStroke(color_network_link);
	 
}




function fillOutPaneNetWorkChart( outpane,output){
 
	
	var networkData=output.visualData;
	var contentDiv= document.createElement(TAG_DIV);
	outpane.setContent(contentDiv);
	 	
	var allNodes=networkData.allChildNodes;
	var allLinks=networkData.links;
	var maxX=countMaxX(allNodes);
	var maxY=countMaxY(allNodes);
	var nodeList=new Array(); 
	//create a surface x=x*100,y=y*100;
	var surface = dojox.gfx.createSurface(contentDiv, maxX*200+100, maxY*80+100);
    var group = surface.createGroup();
    
    
    //TODO:description......
    var description=networkData.description;
    group.createText( {x: 10, y: 20, text:description, align: "start"}).setFill(color_network_node_text).setFont({ family: "arial", size:"15pt", weight: "bold"});
    var xlables=networkData.xLabels;
    for( var i = 0; i < xlables.length; i++) {
    	group.createText( {x: 200*(i+1)-10, y: 55,
    		text:xlables[i][1], align: "start"}).setFill(color_network_node_text).setFont({ family: "arial", size:"10pt", weight: "bold"});
    	   	
		
	}
    //draw the x title first...
     
	for ( var i = 0; i < allNodes.length; i++) {
		var node=allNodes[i];
		var x1=200*node.xGrid+10;
			var y1= 80*node.yGrid+10;
		 
			var nodeColor={
				type: "radial",
				cx: x1 + network_node_r * 0.4,
				cy: y1 - network_node_r * 0.4,
				r: 25,
				colors: [
					{ offset: 0.5,   color: "#DDE9FF" },
					{ offset: 1, color: "#AECAFF" }
				]
			};
			if(node.nodeType==NODETYPE_THRESHHOLD){
				nodeColor={
					type: "radial",
					cx: x1 + network_node_r * 0.4,
					cy: y1 - network_node_r * 0.4,
					r: 25,
					colors: [
						{ offset: 0.5,   color: "#6897D4" },
						{ offset: 1, color: "#4F81BD" }
					]
				};
			}
		var circle = group.createCircle({cx:x1 , cy:y1, r:network_node_r}).
		setFill(nodeColor).setStroke(color_network_node_stroke);
	
		 
 	
 		nodeList[i]=circle;
 		var tooltipText=node.label;
// 		var tooltipArray=[tooltipText ];
 		var maxLength=0;
 		var index=i;

 		var tooltipStr = tooltipText + "<BR/>";
 		for ( var x = 0; x < allLinks.length; x++) {
 			var link = allLinks[x];
			if(link.endIndex==index){
				var  startNode=allNodes[link.startIndex];
				var text=startNode.label+": "+link.label;
//				tooltipArray[tooltipArray.length]=text;
				tooltipStr += text;
				tooltipStr += "<BR/>";
				if(maxLength<text.length){ 
					maxLength=text.length;
				}
			
			}
		}
 		
		var length=20;
		if(maxLength>length){
			length=maxLength;
		}
		var width = (length)*8+15;

		new GfxTooptip({
			label: tooltipStr,
			connectTo: circle,
			container: outpane.domNode
		});
		
//		new dijit.Tooltip({
//		    connectId: [circle.getNode()],
//		    label: tooltipStr
//		}).startup();
//		var tooltip= new alpine.visual.gfxToolTip({
//			parentNode:circle,
//			 width:width,
//			 height:25+tooltipArray.length*15,
//			 textArray:tooltipArray
//			 
//		});
//		
//      fillToolTip(tooltip,tooltipArray);
		
//		var nodeLabel = node.label;
//	  	
//		var text1 = group.createText( {x: x1+5, y: y1+20, text:nodeLabel, align: "start"}).setFill("red");
//		//these two is for the tool tipuse
//		text1.nodeTooltip=node.toolTip;
//		text1.nodeLabel=nodeLabel;		
//		rect1.text=text1   ;
		
//		rect1.connect("onmouseover",rect1,showToolTip);
//		rect1.connect("onmouseout",rect1,hideToolTip);
 
//		
	}
	

	for ( var i = 0; i < allLinks.length; i++) {
		var link = allLinks[i];
		var startIndex=link.startIndex;
		var endIndex=link.endIndex;
		var startRect=nodeList[startIndex];
		var endRect=nodeList[endIndex];
		
		drawNetworkLink(surface,startRect,endRect,link);
		
	}
	
	
	output.dojo_surface=surface;
}
 
 