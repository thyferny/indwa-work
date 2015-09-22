var alpine_props_storageParameters = {};
(function(){
	var constants = {
		DIALOG: "props_storageParameter_dialog",
		DATABASE_SWITCHER: "props_storageParameter_database_section",
		BUTTON_SAVE: "props_storageParameter_button_save",
		BUTTON_CANCEL: "props_storageParameter_button_cancel",
		PG_APPENDONLY_YES: "props_storageParameter_pg_appendOnly_y",
		PG_APPENDONLY_NO: "props_storageParameter_pg_appendOnly_n",
		PG_COLUMNARSTORAGE_YES: "props_storageParameter_pg_columnarStorage_y",
		PG_COLUMNARSTORAGE_NO: "props_storageParameter_pg_columnarStorage_n",
		PG_COMPRESSION_YES: "props_storageParameter_pg_compression_y",
		PG_COMPRESSION_NO: "props_storageParameter_pg_compression_n",
		PG_COMPRESSION_LEVEL: "props_storageParameter_pg_compression_level",
		PG_DISTRIBUTION_RANDOM: "props_storageParameter_pg_distribution_random",
		PG_DISTRIBUTION_ASSIGMENT: "props_storageParameter_pg_distribution_assigment",
		PG_DISTRIBUTION_ASSIGMENT_COLUMNS: "props_storageParameter_pg_distribution_assigment_columns"
	};
	var storageParam = null;
    var sourceButtonId = null;
	
	dojo.ready(function(){
		
		dojo.connect(dijit.byId(constants.DIALOG), "onHide", function(){
			//TODO release
			resetPGWidgets();
			storageParam = null;
		});
		dojo.connect(dijit.byId(constants.BUTTON_CANCEL), "onClick", function(){
			dijit.byId(constants.DIALOG).hide();
		});
		
		dojo.connect(dijit.byId(constants.BUTTON_SAVE), "onClick", function(){
			if(!validatePG()){
				return;
			}
			commitParams();
            setButtonBaseClassValid(sourceButtonId);
			dijit.byId(constants.DIALOG).hide();
		});
		
		createPGRelation();
	});
	
	function startup(paramModel, sourceBtnId){
        sourceButtonId = sourceBtnId;
		storageParam = paramModel;
        dijit.byId(constants.DIALOG).titleBar.style.display = 'none';
		dijit.byId(constants.DIALOG).show();
        var dbTypeCheck = (storageParam.databaseType == "Greenplum") ? "PostgreSQL" : storageParam.databaseType;
		dijit.byId(constants.DATABASE_SWITCHER).selectChild(constants.DATABASE_SWITCHER + "_" + dbTypeCheck);
        console.log("Storage Param" + storageParam);
		if(storageParam.originalModel){
			if(storageParam.databaseType == "PostgreSQL" || storageParam.databaseType == "Greenplum"){
				initPGFields(storageParam.originalModel);
			}
		}
	}
	
	function commitParams(){
		var model = {};
		buildPGFields(model);
		storageParam.originalModel = model;
	}
	
	/************************************	PG	************************************/
	function initPGFields(originalModel){
		if(originalModel.isAppendOnly){
			dijit.byId(constants.PG_APPENDONLY_YES).set("checked", true);
		}else{
			dijit.byId(constants.PG_APPENDONLY_NO).set("checked", true);	
		}
		if(originalModel.isColumnarStorage){
			dijit.byId(constants.PG_COLUMNARSTORAGE_YES).set("checked", true);
		}else{
			dijit.byId(constants.PG_COLUMNARSTORAGE_NO).set("checked", true);
		}

		if(originalModel.isCompression){
			dijit.byId(constants.PG_COMPRESSION_YES).set("checked", true);
			dijit.byId(constants.PG_COMPRESSION_LEVEL).set("value", originalModel.compressionLevel);
		}else{
			dijit.byId(constants.PG_COMPRESSION_NO).set("checked", true);
		}
		if(originalModel.isDistributedRandomly){
			dijit.byId(constants.PG_DISTRIBUTION_RANDOM).set("checked", true);
		}else{
			dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT).set("checked", true);
			dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT_COLUMNS).set("value", originalModel.distributColumns.join());
		}
	}
	
	function resetPGWidgets(){
		dijit.byId(constants.PG_APPENDONLY_YES).reset();
		dijit.byId(constants.PG_APPENDONLY_NO).reset();
		dijit.byId(constants.PG_COLUMNARSTORAGE_YES).reset();
		dijit.byId(constants.PG_COLUMNARSTORAGE_NO).reset();
		dijit.byId(constants.PG_COMPRESSION_YES).reset();
		dijit.byId(constants.PG_COMPRESSION_NO).reset();
		dijit.byId(constants.PG_DISTRIBUTION_RANDOM).reset();
		dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT).reset();
		dijit.byId(constants.PG_COMPRESSION_LEVEL).reset();
		dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT_COLUMNS).reset();
	}
	
	function createPGRelation(){
		dojo.connect(dijit.byId(constants.PG_APPENDONLY_YES), "onChange", function(val){
			dijit.byId(constants.PG_COLUMNARSTORAGE_YES).set("disabled", !val);
			dijit.byId(constants.PG_COLUMNARSTORAGE_NO).set("disabled", !val);
			dijit.byId(constants.PG_COMPRESSION_YES).set("disabled", !val);
			dijit.byId(constants.PG_COMPRESSION_NO).set("disabled", !val);
			if(!val){//reset relation widgets if choose no 
				dijit.byId(constants.PG_COLUMNARSTORAGE_YES).reset();
				dijit.byId(constants.PG_COLUMNARSTORAGE_NO).reset();
				dijit.byId(constants.PG_COMPRESSION_YES).reset();
				dijit.byId(constants.PG_COMPRESSION_NO).reset();
			}
		});
		dojo.connect(dijit.byId(constants.PG_COMPRESSION_YES), "onChange", function(val){
			dijit.byId(constants.PG_COMPRESSION_LEVEL).set("disabled", !val);
		});
		dojo.connect(dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT), "onChange", function(val){
			dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT_COLUMNS).set("disabled", !val);
		});
	}

	function validatePG(){
		var allright = true;
		dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT_COLUMNS)._hasBeenBlurred = true;
		allright &= dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT_COLUMNS).validate();
		return allright;
	}
	
	function buildPGFields(model){
		model.isAppendOnly = dijit.byId(constants.PG_APPENDONLY_YES).get("checked");
		model.isColumnarStorage = dijit.byId(constants.PG_COLUMNARSTORAGE_YES).get("checked");
		model.isCompression = dijit.byId(constants.PG_COMPRESSION_YES).get("checked");
		model.isDistributedRandomly = dijit.byId(constants.PG_DISTRIBUTION_RANDOM).get("checked");
		model.compressionLevel = dijit.byId(constants.PG_COMPRESSION_LEVEL).get("value");
		model.distributColumns = dijit.byId(constants.PG_DISTRIBUTION_ASSIGMENT_COLUMNS).get("value").split(",");
		
	}
	/************************************	PG	************************************/
	alpine_props_storageParameters.startup = startup;
})();
