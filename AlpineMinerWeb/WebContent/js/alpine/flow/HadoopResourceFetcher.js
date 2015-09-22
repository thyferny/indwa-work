define(function(){
	/**
	 * get Hadoop file info from operatorUI
	 * @param operator
	 * @returns hadoopFileInfo
	 * 			{
	 * 				connectionName,
	 * 				outputFilePath
	 * 			}
	 */
	function getHadoopFileInfo(operator){
		var hadoopFileInfo = {};
		//get connection name from parents operator's property or itself. 
		//get connection name from operator's property first in order to consistent if edit HadoopFileOperator's property.
		// maybe retrieved a simaple connection name from parent(when user edit FileOperator and submit), so get connection name from connectionName field of current.(TODO maybe it don't work too....) 
		hadoopFileInfo.connectionName = /*getParentPropValueRecrusively(operator, "connName") || */operator.connectionName;//from dataexplorer.js
		hadoopFileInfo.outputFilePath = operator.outputHadoopFilePath;
		return hadoopFileInfo;
	}
	
	return {
		getHadoopFileInfo: getHadoopFileInfo
	};
});