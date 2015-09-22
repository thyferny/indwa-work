/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * findandreplaceparameterManager.js
 * 
 * Author Will
 * Version 2.7
 * Date 2012-05-15
 */
define(function(){
	
	function init4FindAndReplaceConst(init4FindAndReplaceConst_CallBack){
		 var url4CST =  baseURL+"/main/dataSource/explorer.do?method=getConnSchemaTablesMap&resourceType=Personal"; //+CurrentFlow.type;
		 ds.get(url4CST,init4FindAndReplaceConst_CallBack,null,true);
	}
	
	function getFindAndReplaceQueryResult(/*Object*/searchParamObj,getFindAndReplaceQueryResult_SUCCESS_CALLBACK){
		  var url = baseURL+"/main/findAndReplace.do?method=getFindAndReplaceQueryResult";
		   ds.post(url,searchParamObj,getFindAndReplaceQueryResult_SUCCESS_CALLBACK);
	}
	
	function replaceValueCanReplace_done(/*Object*/replaceParamObj,callback){
		var url = baseURL+"/main/findAndReplace.do?method=replaceFindAndReplaceQueryResult";
		ds.post(url,replaceParamObj,callback);
	}
	
	
	return {
		init4FindAndReplaceConst:init4FindAndReplaceConst,
		getFindAndReplaceQueryResult:getFindAndReplaceQueryResult,
		replaceValueCanReplace_done:replaceValueCanReplace_done
	};
   
});