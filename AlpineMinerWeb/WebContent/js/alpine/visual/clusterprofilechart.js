function fillOutPaneClusterProfileChart(outpane, output) {
 
	var chartData = output.visualData;
	  
	var table = document.createElement('table');
  	
	var tableHeader = document.createElement('tr');
	table.appendChild(tableHeader);
	tableHeader.setAttribute("bgcolor", "#E9E9E9");
	
	var headers = chartData.tableHeader;
	var tableGroupColumnType = new Array();
	for ( var x = 0; x < headers.length; x++) {
		if(x==0){
			tableGroupColumnType[x] = "text" ;
		}else{
			tableGroupColumnType[x] = "svg" ;
		}
		var headStr = headers[x];
		var th = document.createElement('td');
	
		var thContent = document.createTextNode(headStr);
		th.appendChild(thContent);
		tableHeader.appendChild(th);
	}

 
	var models = chartData.models;
	var tableData = new Array();
	//   
	for ( var i = 0; i < models.length; i++) {

		// row list of models, each cell is a visual model
		var rowModelList = models[i];
		var tr = createTableRow(rowModelList,tableData);
		table.appendChild(tr);
	}
 	
	table.setAttribute('border', 1);
	table.setAttribute('cellspacing', "0");
	if(dojo.isIE){
		var contentDiv = document.createElement(TAG_DIV);
		contentDiv.appendChild(table) ;
		outpane.setContent(contentDiv.innerHTML);
	}else{
		outpane.setContent(table);
	}
	output.dojo_cluster_table=table;
	
	output.tableGroupHeader = chartData.tableHeader;
	output.tableGroupColumnType = tableGroupColumnType;//text,svg
	output.tableData = tableData;

}

function fillLegendDiv(div,colorArray,labelArray){
	var surface = dojox.gfx.createSurface(div, 120, colorArray.length*16);
	surface.colorArray=colorArray;
	surface.labelArray=labelArray;
	surface.whenLoaded(  dojo.hitch(this,function(){
		for ( var i = 0; i < surface.colorArray.length; i++) {
			var rect1 = surface.createRect({x:2 , y:2+i*15, width: 14, height: 13, r: 2});
			var textY = 2+i*15+9+2;
			if(dojo.isIE){
				 textY = 2+i*15+6+3;
			}
			var text1 = surface.createText( {x:20, y: textY, text:surface.labelArray[i], align: "start"}).setFill("black");
			rect1.setFill(surface.colorArray[i]).setStroke("black");
			
		}
 
		}));
  	return surface;
}



function fillChartDiv(div,colorArray,numberArray,labelArray){ 
	div.style.width="100%";
    div.style.height="100%";
    var height=16*colorArray.length;
	var surface = dojox.gfx.createSurface(div, 100, height+10);
	surface.colorArray=colorArray;
	surface.numberArray=numberArray;
	surface.whenLoaded(  dojo.hitch(this,function(){
		var startx=10;
		var starty=2;
		
		var sArray=surface.numberArray;
		var sum=0;
		for(var i= 0;i<sArray.length;i++){
			sum=sum*1+1*sArray[i];
		}
		for ( var i = 0; i < surface.colorArray.length; i++) {
			if(sArray[i] == null){
				continue;
			}
			var aheight=height*(sArray[i]/sum);
			var rect1 = surface.createRect({x:startx , y:starty, width: 80, height: aheight});
			rect1.setFill(surface.colorArray[i]).setStroke("black");
			starty=starty+aheight;
		}
 
		}));
	return surface;
}
 

function createTableRow(rowModelList,tableData) {
	var rowData=new Array();
	var tr = document.createElement('tr');
	var colorArray=new Array();
	//i is the legend label..
	var length=rowModelList[1].labels.length;
    var dx = 0.5;
    if (length != 1) {
        dx = 1/(length-1);
    }
	for ( var i = 0; i < length; i++) {   
		colorArray[i]=colorList(i*dx);
	}
	
     for(var j = 0; j <rowModelList.length; j++) {
    	 
      var c = document.createElement('td');
     
      var    m;
      //attribute name
      if(j==0){
    	  rowData.push(rowModelList[j].text);
    	  m=document.createTextNode(rowModelList[j].text);
      }//legend (states)--use gfx...
      else if(j==1){
    	  //gfx suface...
    	//  m = new dijit.layout.ContentPane({},div);
    	   m=document.createElement(TAG_DIV); 
    	 var surface= fillLegendDiv(m,colorArray,rowModelList[j].labels);
    	 rowData.push(surface);
      } 
      else{
     	 var m=document.createElement(TAG_DIV);
     	var surface = fillChartDiv(m,colorArray,rowModelList[j].numbers,rowModelList[1].labels);
     	 rowData.push(surface);
    	  c.setAttribute("width", 120);
    	  c.setAttribute("height", 100);
      }
      
      c.appendChild(m);
      tr.appendChild(c);
     }
     tableData.push(rowData) ;
	return tr;
}
 
 