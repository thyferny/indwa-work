/**
 * logView.js
 * This is the js file for logview of the process result.
 * @author John zhao
 * 
 * Version  Ver 3.0
 *   
 * Date     2011-7-4    
 *  
 * COPYRIGHT   2010 - 2011 Alpine Solutions. All Rights Reserved.    
 * */ 

function setLogContent(data,index){
	
	var div = document.createElement("DIV"); 
	dojo.byId(ID_LOG_VIEW).appendChild(div);
	div.id = data.operatorname+"_logdiv";
//	div.style.position=STYLE_POSITION_ABS;
//	div.style.top=index * 14;
//	div.style.left=8;
	div.align=STYLE_ALIGN_LEFT;
	var name = data.operatorname == "null" ? "" : data.operatorname;
	var message = data.logmessage;
	 

	div.innerHTML = " [" + data.date +"] " + "  " + name + "  " + message;
  

	//append error message
	if(data.message == MSG_PROCESS_ERROR)
	{
		var div1 = document.createElement("DIV"); 
		dojo.byId(ID_LOG_VIEW).appendChild(div1);
		div1.id = data.operatorname+"_errdiv";
	//	div1.style.position=STYLE_POSITION_ABS;
	//	div1.style.top=(index+1) * 14;
		
		//red is error
		div1.style.color="FF0000";
	//	div1.style.left=8;
		div1.align=STYLE_ALIGN_LEFT;

		div1.innerHTML = data.errMessage;
	
	}
}

