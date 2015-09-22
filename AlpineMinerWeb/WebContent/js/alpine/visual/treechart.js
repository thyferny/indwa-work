var arrowSize = {'height':10, 'width':3};
var treeRectWidth=90;
var treeRectHeight=30;


var color_tree_node = [79, 129, 189, 1];//[30, 130, 255, 0.4];
var color_clapse_icon_open = "#AECAFF";//[30,230, 15, 1];
var color_clapse_icon_close = "#CCCCCC";//[230,10, 15, 1];
var color_tree_node_leaf = {
    type:"linear",
    x1: 0,
    y1: 0,
    x2: 0,
    y2: 500,
    colors: [
        { offset: 0.3,   color: "#DDE9FF" },
        { offset: 0.5, color: "#AECAFF" }
    ]
};//[210, 20, 225, 0.3];
var color_tree_node_text="blue";
var color_tree_node_stroke = [120, 154, 201, 1];//[100, 100, 255, 0.8];
var color_tree_link = [120, 154, 201, 0.9];//[10, 10, 255, 1];
var color_tree_link_text = "#4F81BD";//"red";



function fillOutPaneTreeChart( outpane,output){
    var treeData=output.visualData;
    var contentDiv= document.createElement(TAG_DIV);
    outpane.setContent(contentDiv);
    var allNodes=treeData.allChildNodes;

    var rectList=new Array();
    if(treeData.errorMessage&&treeData.errorMessage.length!=0){
        var errorsDiv = document.createElement(TAG_DIV);
        errorsDiv.id="err_"+output.contentPaneId;
        contentDiv.appendChild(errorsDiv);
        fillVisualMessages(errorsDiv,treeData.errorMessage,"0000DD");
    }
    
//create a surface x=x*100,y=y*100;
    var surface = dojox.gfx.createSurface(contentDiv,output.visualData.maxX-10, output.visualData.maxY-10);
    var group = surface.createGroup();

    for ( var i = 0; i < allNodes.length; i++) {
        var node=allNodes[i];
//var x1=100*node.xGrid+10;
//var y1= 80*node.yGrid+10;
        var x1=node.xGrid;
        var y1= node.yGrid;

        var nodeLabel = node.label;

//these two is for the tool tipuse
// text1.nodeLabel=nodeLabel;

        var rect1 = group.createRect({x:x1 , y:y1, width: treeRectWidth, height: treeRectHeight, r: 5});
        var text1 = group.createText( {x: x1+treeRectWidth /2, y: y1+20, text:nodeLabel, align: "middle"}).setFill("white").setFont({ family:"Arial", size:"10pt", weight: "bold"});
//rect1.setFill(node.color).setStroke("blue");
        var shapeTooltipScope = group.createRect({x:x1 , y:y1, width: treeRectWidth, height: treeRectHeight, r: 5}).setFill([0, 0, 0, 0]);

        var nodeColor = null;
      // fix Pivotal 41079907
      //because FireFox has a bug with that. https://bugzilla.mozilla.org/show_bug.cgi?id=539436
        if(dojo.isFF && output.visualData.maxX-10 > 32760){
            if(node.nodeType==NODETYPE_LEAF){
                nodeColor = "#DDE9FF";
                text1.setFill("black");
            }else{
                nodeColor = "#6897D4";
            }
        }else{
            if(node.nodeType==NODETYPE_LEAF){
                nodeColor = {
                    type:"linear",
                    x1: x1,
                    y1: y1,
                    x2: x1,
                    y2: y1 + treeRectHeight,
                    colors: [
                        { offset: 0.3,   color: "#DDE9FF" },
                    	{ offset: 0.5, color: "#AECAFF" }
                    ]
                };
                text1.setFill("black");
            }else{
            	nodeColor = {
                    type:"linear",
                    x1: x1,
                    y1: y1,
                    x2: x1,
                    y2: y1 + treeRectHeight,
                    colors: [
                        { offset: 0.3,   color: "#6897D4" },
                        { offset: 0.5, color: "#4F81BD" }
                    ]
                };
            }
        }
        rect1.setFill(nodeColor).setStroke(color_tree_node_stroke);

        rect1.text=text1;
        rectList[i]=rect1;
        if(node.toolTip){
            new GfxTooptip({
                label: node.toolTip,
                connectTo: shapeTooltipScope,
                container: outpane.domNode
            });
        }
    }
    var allLinks=treeData.links;
    for ( var i = 0; i < allLinks.length; i++) {
        var link = allLinks[i];
        var startIndex=link.startIndex;
        var endIndex=link.endIndex;
        var startRect=rectList[startIndex];
        var endRect=rectList[endIndex];
        drawTreeLink(surface,startRect,endRect,link ,i+1);
    }

    drawClapseIconRect(rectList,surface);

    output.dojo_surface=surface;
//this is for report use, need expand all when generate report
    output.rectList=rectList;
}

function open_all_tree_node(rectList){
    if(!rectList||rectList.length==0){
        return ;
    }
    for ( var i = 0; i < rectList.length; i++) {
        var rect1=rectList[i];

        if(rect1.iconRect){
            if(rect1.clapse!="open"){
                var iconColor=color_clapse_icon_open;
                rect1.clapse="open";
                rect1.iconRect.setFill(iconColor).setStroke(color_tree_node_stroke);

                if(rect1.linelinks!=null){
                    for(var i = 0; i < rect1.linelinks.length; i++) {
                        var line=rect1.linelinks[i];

                        showHideLine(line);

                    }
                }
            }
        }
    }
}


function drawClapseIconRect(rectList,surface){
    for ( var i = 0; i < rectList.length; i++) {
        var rect1=rectList[i];

        if(rect1.linelinks&&rect1.linelinks.length>0){
            var cx=rect1.shape.x+treeRectWidth/2;
            var cy=rect1.shape.y+treeRectHeight;
            rect1.iconRect=drawClapseIcon(surface,cx,cy);
            rect1.iconRect.connect("onclick",rect1,showOrHideChild);
            rect1.clapse="open";
        }
    }
}


function drawClapseIcon (surface,cx,cy){
    var iconRect = surface.createRect({x:cx-7 , y:cy, width: 14, height: 8, r: 2});
    iconRect.setFill(color_clapse_icon_open).setStroke(color_tree_node_stroke);
    return iconRect;
// iconRect.moveToTop();
}



function showOrHideChild(){

    var iconColor;
    if(this.clapse=="open"){
        iconColor=color_clapse_icon_close;
        this.clapse="close";
    }else{
        iconColor=color_clapse_icon_open;
        this.clapse="open";
    }
    if(this.iconRect){
        this.iconRect.setFill(iconColor).setStroke(color_tree_node_stroke);
    }

    if(this.linelinks!=null){
        for(var i = 0; i < this.linelinks.length; i++) {
            var line=this.linelinks[i];

            showHideLine(line);

        }
    }
}

function showHideNode(rect,displayValue){
    if(!rect){
        return;
    }


    setDisplayValue(rect,displayValue);
    setDisplayValue(rect.text,displayValue);

    if(rect.iconRect){

        setDisplayValue(rect.iconRect,displayValue);
    }
//for the rect...
    if(rect.linelinks!=null&&rect.clapse=="open"){
        for ( var i = 0; i < rect.linelinks.length; i++) {
            var line=rect.linelinks[i];

            showHideLine(line,displayValue);

        }
    }

}

function showHideLine(line,displayValue){
    if(!displayValue){
        if(dojo.isIE){
            displayValue =
                line.rawNode.style.display;
        }
        else{
            displayValue =
                line.rawNode.getAttributeNS(null, 'display');
        }

        if ('none' != displayValue)
        {
            displayValue = 'none';
        }
        else
        {
            if(dojo.isIE){
                displayValue = 'block';//blck for ie
            }else{
                displayValue = 'inline';//blcck for ie
            }
        }
    }
    showHideNode(
        line.target,displayValue);
// line.domNode.style.display="none";//block


    setDisplayValue(line,displayValue);

    for ( var i = 0; i < line.arrawLines.length; i++) {

        setDisplayValue(line.arrawLines[i],displayValue);
    }

    setDisplayValue(line.text,displayValue);


}
function setDisplayValue(shape,displayValue){
    if(dojo.isIE){
        shape.rawNode.style.display=displayValue;
    }else{
        shape.rawNode.setAttributeNS(null, 'display', displayValue);
    }
}
function hideToolTip(){
    if(this.text!=null){

        if(this.text.rawNode.textContent!=this.text.nodeLabel){
            this.text.rawNode.textContent=this.text.nodeLabel;
        }else{
            if(this.rawNode.textContent!=this.nodeLabel){
                this.rawNode.textContent=this.nodeLabel;
            }
        }
    }
}





function drawTreeLink(surface,startRect,endRect,link,index ){
    var x1=startRect.shape.x+treeRectWidth/2;
    var y1=startRect.shape.y+treeRectHeight;
    var x2=endRect.shape.x+treeRectWidth/2;
    var y2=endRect.shape.y - 20 ;
    var line=surface.createLine({x1: x1, y1: y1, x2: x2, y2: y2}).setStroke(color_tree_link);
    if(!startRect.linelinks){
        startRect.linelinks= new Array();
    }
    startRect.linelinks[startRect.linelinks.length]=line;
    drawArrow(surface,x1,x2,y1,y2,line);
    var textX = endRect.shape.x + treeRectWidth/2;//(x1+x2)/2;
    var textY = y2 + 15;//(y1+y2)/2;
// if(index&&index%2==0){
// textY=textY+14;
// }
    var label = link.label;
    label=label.replace(/<=/g,"\u2264");  //pivotal 34430665: The equals sign messes up the batik svg parser.
    label=label.replace(/>=/g,"\u2265");
    label=label.replace(/=/g,"\uA78A");
    //label="\""+label+"\"";    //pivotal xxx : remove quotes
    var text1 = surface.createText( {x:textX, y:textY, text: label, align: "middle"})
        .setFill(color_tree_link_text)
        .setFont({ family:"Arial", size:"10pt" });
    line.text=text1;
    line.target=endRect;
    line.start=startRect;
}

function drawArrow(surface,x1,x2,y1,y2,line){
    var len = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    var t = (y2-y1)/(x2-x1);
    var angle = Math.atan(t);
    var x, y ;
    var len1 = Math.min( arrowSize.height, len - 2);
    var len2 = Math.min( arrowSize.width, len/2 - 1);
    if(x2 < x1){
        x = (len1 - len) * Math.cos(angle) + x1;
        y = (len1 - len) * Math.sin(angle) + y1;
    } else if(x2 >= x1){
        x = (len - len1) * Math.cos(angle) + x1;
        y = (len - len1) * Math.sin(angle) + y1;
    }
    var x3 = x + len2 * Math.sin(angle);
    var y3 = y - len2 * Math.cos(angle);
    var x4 = x - len2 * Math.sin(angle);
    var y4 = y + len2 * Math.cos(angle);
    line.arrawLines=new Array();
    line.arrawLines[0]=surface.createLine({x1: x3, y1: y3, x2: x2, y2: y2}).setStroke( color_tree_link);
    line.arrawLines[1]=surface.createLine({x1: x4, y1: y4, x2: x2, y2: y2}).setStroke(color_tree_link);

}