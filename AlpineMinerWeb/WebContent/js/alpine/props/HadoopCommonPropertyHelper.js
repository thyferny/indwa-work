define(["alpine/props/HadoopFileExplorerHelper",
        "alpine/props/HadoopCommonPropertyManager"], function(fileExplorerHelper, remoteHandler){

    var connectionKey = null;
	/**
	 * build hadoop file explorer widget. The args have to include below fields:
	 * id--the property id which in property.js
	 * container-- to contain property widgets
	 * value--initial value of property
	 * explorerButtonLabel--the label of button
	 * includeFile-- include file item if true.
	 * checkPermission-- true|false
	 * readonly--true|false	default is true
	 * onCompleteInitialize--call this function when initialized. has two arguments first is Array of widget, and second is Array of event handler.
	 * onClickOkayButton-- call this function when click OK button.
	 */
	function _buildHadoopFileExplorer(args){
		var widgets = [];
		var events = [];
        connectionKey = null;//reset parameter connection name.
		hadoopPickInput = new dijit.form.ValidationTextBox({
			id: args.id,
			required: true,
			style: "width:100px;",
			readonly: args.readonly != null ? args.readonly : true,
			value: args.value
		},dojo.create("div", {}, args.container));
		
		
		hadoopFileExplorerTrigger = new dijit.form.Button({
            id: "btn_" + args.id,
            label: args.explorerButtonLabel,
            baseClass:"workflowButton"
        },dojo.create("div", {}, args.container));
		
		events.push(dojo.connect(hadoopFileExplorerTrigger, "onClick", function(){
        	var hadoopConnKey = _getConnectionKey();
        	if(hadoopConnKey == ""){
              popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_conn_tip);
               return false;
        	}

            if(null!=dijit.byId("connName"+ID_TAG) && dijit.byId("connName"+ID_TAG).get("value")==""){
                popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_conn_tip);
                return false;
            }

        	fileExplorerHelper.startup(hadoopConnKey, function(selectedItem){
        		if(args.checkPermission && !remoteHandler.checkHasPermission(_getConnectionKey(), selectedItem.key)){
            		//just giving warning message.
        			popupComponent.alert(alpine.nls.hadoop_prop_choose_file_select_no_permission);
        		}
    			hadoopPickInput.set("value", selectedItem.key);
    			if(args.onClickOkayButton){
    				return args.onClickOkayButton.call(null, selectedItem);
    			}
    			return true;
        	}, args.includeFile);
		}));
//		if(args.onClickOkayButton && args.readonly == false){
//			hadoopPickInput.isValid = function(){
//				return args.onClickOkayButton.call(null, this.get("value"));
//			};
//		}
		widgets.push(hadoopPickInput);
		widgets.push(hadoopFileExplorerTrigger);
		if(args.onCompleteInitialize){
			args.onCompleteInitialize.call(null, widgets, events);
		}
	}

	function _getConnectionKey(){
        if(connectionKey == null){
            return alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(current_op).connectionName;
        }else{
        	return connectionKey;
        }
	}
	
    function _setConnectionKey(connKey){
        connectionKey = connKey;
    }
	
	return {
		buildHadoopFileExplorer: _buildHadoopFileExplorer,
        setConnectionKey: _setConnectionKey
	};
});