/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * InspectHadoopFileProperty
 * Author Gary
 */
define(function(){
	var constants = {
		DIALOG: "alpine_datasourceexplorer_InspectHadoopFileProperty_Dialog",
		LABEL_NAME: "alpine_datasourceexplorer_InspectHadoopFileProperty_name",
		LABEL_OWNER: "alpine_datasourceexplorer_InspectHadoopFileProperty_owner",
		LABEL_GROUP: "alpine_datasourceexplorer_InspectHadoopFileProperty_group",
		LABEL_PERMISSION: "alpine_datasourceexplorer_InspectHadoopFileProperty_permission",
		LABEL_MODIFICATION_TIME: "alpine_datasourceexplorer_InspectHadoopFileProperty_modificationTime",
		LABEL_ACCESS_TIME: "alpine_datasourceexplorer_InspectHadoopFileProperty_accessTime",
		LABEL_SIZE: "alpine_datasourceexplorer_InspectHadoopFileProperty_size",
		LABEL_BLOCK_SIZE: "alpine_datasourceexplorer_InspectHadoopFileProperty_blockSize"
	};
	
	function _openHadoopFileProperty(hadoopFileProperty){
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
        var displayName = hadoopFileProperty.name;
        if(displayName!=null && displayName.length>25){
            displayName = displayName.substring(0,23)+"...";
        }
		dojo.byId(constants.LABEL_NAME).innerHTML =displayName;
        dojo.byId(constants.LABEL_NAME).title = hadoopFileProperty.name;
		dojo.byId(constants.LABEL_OWNER).innerHTML = hadoopFileProperty.owner;
		dojo.byId(constants.LABEL_GROUP).innerHTML = hadoopFileProperty.group;
		dojo.byId(constants.LABEL_PERMISSION).innerHTML = hadoopFileProperty.permission;
		dojo.byId(constants.LABEL_MODIFICATION_TIME).innerHTML = hadoopFileProperty.modificationTime;
		dojo.byId(constants.LABEL_ACCESS_TIME).innerHTML = hadoopFileProperty.accessTime;
		dojo.byId(constants.LABEL_SIZE).innerHTML = hadoopFileProperty.size;
		dojo.byId(constants.LABEL_BLOCK_SIZE).innerHTML = hadoopFileProperty.blockSize;
	}
	
	return {
		openHadoopFileProperty: _openHadoopFileProperty
	};
});