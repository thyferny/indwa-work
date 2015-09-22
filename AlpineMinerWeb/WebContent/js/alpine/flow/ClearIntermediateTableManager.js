/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * ClearIntermediateTableManager
 * Author Gary
 */
define(["alpine/flow/WorkFlowRunnerUIHelper"], function(runnerHelper){
	/**
	 * drop intermediate table
	 * asynch function
	 */
	function dropIntermediateTables(clearTableArray, callbackFunction, errorFunction, contentPanelId){
		var result;
		var tableArray = new Array();
		dojo.forEach(clearTableArray,function(item){
			tableArray.push({
				id: item.id,
				uuid: item.uuid,
				operatorName: item.operatorName,
				connectionName: item.connectionName,
				schemaName: item.schemaName,
				tableName: item.tableName,
				outputType: item.outputType,
				resourceType: item.resourceType
			});
		});
		ds.post(baseURL + "/main/dataexplorer.do?method=clearInterspaceTable&runnerUUID=" + runnerHelper.getRunningID(),
		tableArray, callbackFunction,errorFunction, false, contentPanelId);
		//return result;
	}
	
	return {
		dropIntermediateTables: dropIntermediateTables
	};
});