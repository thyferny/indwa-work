define(function(){
	
	var mappingInfo = {
		"Apache Hadoop 0.20.2": "http://localhost:8080/AlpineHadoopAgentApache0202/",
		"Cloudera CDH3 Update 4": "http://localhost:8080/AlpineHadoopClientCloudera0.20.2-cdh3u4-2.8.1/"
	};
	
	function _getUrl(hadoopVersion){
		return mappingInfo[hadoopVersion];
	}
	
	return{
		getUrl: _getUrl
	};
});