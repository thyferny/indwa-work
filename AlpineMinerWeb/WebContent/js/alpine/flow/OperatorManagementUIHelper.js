/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * OperatorManagement.js 
 * Author Gary
 * Jul 5, 2012
 */
define(["alpine/flow/OperatorManagementManager",
        "alpine/flow/WorkFlowPainter",
        "alpine/operatorexplorer/OperatorUtil",
        "alpine/flow/OperatorLinkUIHelper",
        "alpine/flow/OperatorRenameUIHelper",
        "dojo/_base/array",
        "alpine/flow/OperatorMenuHelper",
        "alpine/system/PermissionUtil",
        "alpine/flow/WorkFlowEditToolbarHelper"
    ], function(operatorHandler, painter, operatorUtil, linkHandler, renameEditor, array, operatorMenuHelper, permissionUtil, editToolbarHelper){
	
	var constants = {
		CANVAS: "FlowDisplayPanelPersonal",
		DEFAULT_ICON_SIZE: 45,
		PASTE_OPERATOR_BTN: "alpine_flow_operator_paste_btn",
		datasourceOperatorClasses: {
			DB_TABLE: "DbTableOperator",
			HADOOP_FILE: "HadoopFileOperator"
		}
	};
	
	/**
	 * include below attribute:
	 * name,
	 * operatorClass,
	 * hasDefaultVal,
	 * 
	 * some data source property
	 */
	var draggingItem = null;
	
	var operatorStatus = [];//store operator focus status, it is an Array for pair of key/value. key = operatorUid, value = true(focus)/false(ordinary)
	var selectOperatorUidArray = [];
    var _joinToArray = function(arrays){
        var a = arrays[0];
        for(var i = 1; i < arrays.length; ++i){
            a = a.concat(arrays[i]);
        }
        return a;
    };
	dojo.ready(function(){
		var canvas = new dojox.grid.enhanced.plugins.GridSource(dojo.byId(constants.CANVAS), {
			isSource: false,
			onDropExternal: function(source, nodes, copy){
                if(source instanceof alpine.layout.GridDnDSource){
                    var ranges = array.map(nodes, function(node){
                        return source.getItem(node.id).data;
                    });
                    var item = source.getItem(nodes[0].id);
                    var grid = item.dndPlugin.grid;
                    var type = item.type[0];
                    var range;
                    try{
                        switch(type){
                            case "grid/cells":
                                nodes[0].innerHTML = this.getCellContent(grid, ranges[0].min, ranges[0].max) || "";
                                this.onDropGridCells(grid, ranges[0].min, ranges[0].max);
                                break;
                            case "grid/rows":
                                range = _joinToArray(ranges);
                                nodes[0].innerHTML = this.getRowContent(grid, range) || "";
                                this.onDropGridRows(grid, range);
                                break;
                            case "grid/cols":
                                range = _joinToArray(ranges);
                                nodes[0].innerHTML = this.getColumnContent(grid, range) || "";
                                this.onDropGridColumns(grid, range);
                                break;
                        }
                        if(this.insertNodesForGrid){
                            this.selectNone();
                            this.insertNodes(true, [nodes[0]], this.before, this.current);
                        }
                        item.dndPlugin.onDragOut(!copy);
                    }catch(e){
                        console.warn("GridSource.onDropExternal() error:",e);
                    }
                }else{
                    this.inherited(arguments);
                }
            }

		});
		dojo.connect(canvas, "onDropGridRows", function(grid, rowIndexes){
			var currentItem = grid.getItem(rowIndexes[0]);
			draggingItem = {
				name: toolkit.getValue(currentItem.label),
				operatorClass: toolkit.getValue(currentItem.operatorClass || currentItem.key),//adaptor get Operator class from Data source(operatorClass) and Operator(key)
				property: {
					hasDefaultVal: false
				}
			};
			if(grid.id == "alpine_datasourceexplorer_display_grid"){// means dragging item from data source.
				dojo.safeMixin(draggingItem.property, _getDatasourceProperty(currentItem));
				draggingItem.property.hasDefaultVal = true;
			}

			draggingItem.initialMenu = false;
		});
		dojo.connect(canvas, "onDndDrop", function(source, nodes, copy, target, event){
			if(source == target){
				return;
			}
			if(alpine.datasourceexplorer.HadoopExplorerUIHelper.getCurrentConnKey() != null	//it's a hadoop file
					&& /^_|[&|?|=|&|\"|\'|:|\[|\]|\{|\}|,]+/.test(draggingItem.name)){
				popupComponent.alert(alpine.nls.operator_add_error_invalid_name);
				return;
			}
			var operatorCanvas = dojo.byId(constants.CANVAS);
			var positionX = event.clientX - target.node.parentNode.parentNode.offsetLeft + operatorCanvas.scrollLeft;
			var positionY = event.clientY - target.node.parentNode.parentNode.offsetTop - 43 + operatorCanvas.scrollTop;//43 is banner height.
			_addOperator({
				position: {
					x: positionX,
					y: positionY
				},
				targetId: target.node.id,
				operator: draggingItem
			});
			
			
			this.onDndCancel();
		});
		canvas.startup();
	});
	
	/**
	 * get property from Data source
	 * @return
	 */
	function _getDatasourceProperty(dsRecord){
		var properties = {
			connectionName: toolkit.getValue(dsRecord.connectionName),
			entityName: toolkit.getValue(dsRecord.entityName)
		};
		switch(toolkit.getValue(dsRecord.operatorClass)){
		case constants.datasourceOperatorClasses.DB_TABLE:
			properties.schemaName = toolkit.getValue(dsRecord.schemaName);
			break;
		case constants.datasourceOperatorClasses.HADOOP_FILE:
			properties.filePath = toolkit.getValue(dsRecord.filePath);
			break;
		}
		return properties;
	}
	
	/**
	 * 
	 * args: {
	 * 		position: {
	 * 			x,y
	 * 		},
	 * 		targetId,
	 * 		operator: {
	 * 			name,
	 * 			operatorClass,
	 * 			initialMenu		true/false
	 * 			property	if operator dragging from data source should be have this attribute.
	 * 		}
	 * }
	 */
	function _addOperator(args){
		args.operator.name = operatorHandler.generateOperatorLabel(args.operator.name);//generate the operator label.
		operatorHandler.getOperatorBasicInfo(args.operator.operatorClass, function(operatorInfo){
            var operatorPrimaryInfo = {
        		uuid: operatorInfo.uid,
        		x: args.position.x,
        		y: args.position.y,
        		name: args.operator.name,
        		operatorClass: args.operator.operatorClass            		
        	};
            dojo.safeMixin(operatorPrimaryInfo, args.operator.property);
            //store new Operator to flow.

            operatorHandler.storeOperator(operatorPrimaryInfo, function(opInfo){
    			_fillOperatorInfo(constants.CANVAS, opInfo, args.operator.initialMenu || false);
    			_validateOperators();
            }, args.targetId);
            alpine.flow.WorkFlowUIHelper.setDirty(true);
		}, function(){
			console.err("you got some error");
		});
	}
	
	/**
	 * invoked when loaded flow information, to build operator and set it up.
	 * @param	operatorPrimaryInfo
	 * 			showMenu = true|false
	 */
	function _fillOperatorInfo(paneId, operatorPrimaryInfo, showMenu){
//		if(generateName == true){
//            operatorPrimaryInfo.name = operatorHandler.generateOperatorLabel(operatorPrimaryInfo.name);//reset the operator label.
//		}
		operatorHandler.pushLabelToPool(operatorPrimaryInfo.name);
		var operatorShape = _buildOperator(paneId, operatorPrimaryInfo);
		
		operatorHandler.storeOperatorPrimaryInfo(operatorPrimaryInfo);
        operatorMenuHelper.addOperatorMenu(operatorPrimaryInfo, operatorShape.getNode());
	}
	
	/**
	 * build Operator and put it in cache when load a flow.
	 * operatorParam: {
	 * 		String uid;
	 * 		String name;
	 * 		String classname;
	 * 		int x;
	 * 		int y;
	 * }
	 */
	function _buildOperator(paneId, operatorParam){
		//var operatorBasicInfo = operatorUtil.getOperatorObjectByKey(operatorParam.classname);
		var operatorShape = painter.paintOperator({
			canvasInfo: {
				paneId: paneId
			},
			op: {
				uid: operatorParam.uid,
				x: operatorParam.x,
				y: operatorParam.y,
                src: operatorUtil.getStandardImageSourceByKey(operatorParam.classname),
				//icon: operatorBasicInfo.icon,
				width: constants.DEFAULT_ICON_SIZE,
				height: constants.DEFAULT_ICON_SIZE,
				label: operatorParam.name
			},
			bindEvent: true,
			ableMove: true,
			onClick: function(op, e){
        		if(!_ableToSelected()){
        			return;
        		}
        		var isMultiple;
        		if(dojo.isMac){
        			isMultiple = e.metaKey;
        		}else{
        			isMultiple = e.ctrlKey;
        		}
				_selectOperator(op.uid, isMultiple);
			},
        	onDblClick: function(op){
        		if(!_ableToSelected()){
        			return;
        		}
        		var operatorInfo = operatorHandler.getOperatorPrimaryInfo(op.uid);
        		open_property_dialog(operatorInfo);//TODO need to refactor
        	},
        	onMouseOver: function(op){
        		_focusOperator(op.uid);
        	},
        	onMouseOut: function(op){
        		_unFocusOperator(op.uid);
        	},
        	onMove: function(moveInfo){
        		alpine.flow.WorkFlowUIHelper.setDirty(true);
        		var operatorInfo = operatorHandler.getOperatorPrimaryInfo(moveInfo.operatorUid);
        		operatorInfo.x += moveInfo.x;
        		operatorInfo.y += moveInfo.y;
        		renameEditor.moveEditor(operatorInfo.uid, {
        			x: moveInfo.x,
        			y: moveInfo.y
        		});
        	},
        	onDblClickLabel: function(op){
        		if(!_ableToSelected()){
        			return;
        		}
        		renameEditor.setupLabelEditor({
        			paneId: paneId,
        			operatorUid: op.uid
        		});
        	}
		});
		return operatorShape;
	}
	
	function _gridDragAdaptor(evt){
    	var dnd = this.plugin("dndop");
    	var selector = this.plugin("selector");
    	var gridItem = this.getItem(evt.rowIndex);
    	if(toolkit.getValue(gridItem.isDir) 
    			|| alpine.flow.WorkFlowRunnerUIHelper.isFlowRunning() 
    			|| !permissionUtil.checkPermission("OPERATOR_EDIT")){
    		return;// if dragging item is a dir in hadoop, then prevent it to drag.
    	}
    	selector.select("row", evt.rowIndex, evt.cell.index);
    	dnd._dndRegion = dnd._getDnDRegion(evt.rowIndex, evt.cell.index);
    }
	
	function _focusOperator(operatorUid){
		var operatorInfo = operatorHandler.getOperatorPrimaryInfo(operatorUid);
		painter.updateOperatorVisualization({
			paneId: constants.CANVAS,
			operatorId: operatorUid,
			icon: {
				src: operatorUtil.getSelectedImageSourceByKey(operatorInfo.classname)
			}
		});
		operatorStatus[operatorUid] = true;
    }

    function _unFocusOperator(operatorUid){
        if (dojo.indexOf(selectOperatorUidArray, operatorUid) != -1) {
            return;
        }
        var operatorInfo = operatorHandler.getOperatorPrimaryInfo(operatorUid);
        //var operatorBasicInfo = operatorUtil.getOperatorObjectByKey(operatorInfo.classname);
        painter.updateOperatorVisualization({
            paneId: constants.CANVAS,
            operatorId: operatorUid,
            icon: {
                src: operatorUtil.getStandardImageSourceByKey(operatorInfo.classname)
                //img: operatorBasicInfo.icon
            }
        });
        operatorStatus[operatorUid] = false;
    }

    /**
     * select operator 
     * @param	operatorUid
     * 			isMultiple	-- optional default is false
     */
	function _selectOperator(operatorUid, isMultiple){
		isMultiple = (isMultiple == null || typeof isMultiple != "boolean")? false : isMultiple;
		//do not let user select operators while flow is running or the operator already selected only.
        if(alpine.flow.WorkFlowRunnerUIHelper.isFlowRunning() == true 
        		|| (selectOperatorUidArray.length == 1 && selectOperatorUidArray[0] == operatorUid)){     
			return;
		}
        //unselect all links
        linkHandler.resetSelectedLink(constants.CANVAS);
        
        for(var i = 0;i < selectOperatorUidArray.length;i++){
    		linkHandler.inactiveOperator({
    			paneId: constants.CANVAS,
    			operatorUid: selectOperatorUidArray[i]
    		});
    		// if it is single click then clear other selected operator status.
            if(!isMultiple){
	    		_resetOperator(selectOperatorUidArray[i--]);
            }
        }
		var operatorInfo = operatorHandler.getOperatorPrimaryInfo(operatorUid);
		var operatorBasicInfo = operatorUtil.getOperatorObjectByKey(operatorInfo.classname);
        if(isMultiple){
        	editToolbarHelper.setupMultipleOperatorToolbar();
            //click operator is already selected(means click it again to remove it from selected list)
            var currentSelectIndex = dojo.indexOf(selectOperatorUidArray, operatorUid);
            if(currentSelectIndex != -1){
        		_resetOperator(selectOperatorUidArray[currentSelectIndex]);
        		return;
            }
        }else{
        	editToolbarHelper.setupOperatorToolbar(operatorBasicInfo, operatorInfo);
			_validateOperators();//update other Operator's label to ordinary status.
			//bind keyboard event to operator
			alpine.flow.WorkFlowUIHelper.bindDeleteKeyBoardEvent(true);
        }
		
		painter.updateOperatorVisualization({
			paneId: constants.CANVAS,
			operatorId: operatorUid,
			icon: {
                src: operatorUtil.getSelectedImageSourceByKey(operatorInfo.classname)
				//img: operatorUtil.getSelectedImageSourceByKey(operatorInfo.classname)
			},
			label: {
				family:"opensansbold",
				weight:"normal"
			}
		});
		
		operatorStatus[operatorUid] = true;
		selectOperatorUidArray.push(operatorUid);

		if(operatorUtil.isTerminalByKey(operatorInfo.classname) || !permissionUtil.checkPermission("OPERATOR_EDIT")){
			return;// if operator is a terminal operator or user don't have edit operator ability, it cannot be do connect.
		}
		if(!isMultiple){
			linkHandler.activeOperator({
				paneId: constants.CANVAS,
				operatorUid: operatorUid,
	            className: operatorInfo.classname,
				onConnectFail: function(){
					_resetOperator(operatorUid);
				},
				onConnectComplete: function(connectionInfo){
					operatorHandler.updateOperatorPrimaryInfo(connectionInfo.operatorInfoList);
					_validateOperators(connectionInfo.operatorInfoList);
					_resetOperator(connectionInfo.sourceOperatorUid);
					
					alpine.flow.WorkFlowUIHelper.setDirty(true);
				}
			});
		}
	}
	
	/**
	 * reset the Operator to initialize status.
	 */
	function _resetOperator(operatorUid){
		if(!operatorUid){
			return;
		}
		var operatorInfo = operatorHandler.getOperatorPrimaryInfo(operatorUid);
		if(!operatorInfo){
			return;//means the operator is removed from work flow.
		}
		//var operatorBasicInfo = operatorUtil.getOperatorObjectByKey(operatorInfo.classname);
		var normalFont = painter.DEFAULT_LABEL;
		delete normalFont.color;
		painter.updateOperatorVisualization({
			paneId: constants.CANVAS,
			operatorId: operatorUid,
			icon: {
                src: operatorUtil.getStandardImageSourceByKey(operatorInfo.classname)
				//img: operatorBasicInfo.icon
			},
			label: normalFont
		});
		linkHandler.inactiveOperator({
			paneId: constants.CANVAS,
			operatorUid: operatorUid
		});
		operatorStatus[operatorUid] = false;
		for(var i = 0;i < selectOperatorUidArray.length;i++){
			if(selectOperatorUidArray[i] == operatorUid){
				selectOperatorUidArray.splice(i, 1);
			}
		}
		editToolbarHelper.resetOperatorToolbar();
	}
	
	function _toggleOperatorFocus(operatorUid){
		if(!_getOperatorStatus(operatorUid)){
			_focusOperator(operatorUid);
		}else{
            _unFocusOperator(operatorUid);
		}
	}
	
	function _getOperatorStatus(operatorUid){
		return operatorStatus[operatorUid] || false;// if not able to found status for operator, the operator must be un-focus yet.
	}
	
	/**
	 * validate whether operator is valid or not. 
	 * @param operatorArray	-- optional, if not supported, will validate all of Operators in current flow  
	 */
	function _validateOperators(operatorArray){
		var isAllValid = true;
		if(operatorArray){
			for(var i = 0;i < operatorArray.length;i++){
				isAllValid &= validateOperator(operatorArray[i]);
			}
		}else{
			operatorHandler.forEachOperatorInfo(function(operatorInfo){
				isAllValid &= validateOperator(operatorInfo);
			});
		}
		return isAllValid;
		
		function validateOperator(operatorInfo){
			var isValid = operatorInfo.isValid || operatorInfo.valid || false;
			if(isValid){
				painter.updateOperatorVisualization({
					paneId: constants.CANVAS,
					operatorId: operatorInfo.uid,
					label: {
						weight: "normal",
						color: "#000000"
					}
				});
			}else{
				painter.updateOperatorVisualization({
					paneId: constants.CANVAS,
					operatorId: operatorInfo.uid,
					label: {
						weight: "normal",
						color: "red"
					}
				});
			}
			return isValid;
		}
	}
	
	function _ableToSelected(){
		return !renameEditor.isRenaming() && !alpine.flow.WorkFlowRunnerUIHelper.isFlowRunning();
	}
	
	function _deleteOperators(){
		if(selectOperatorUidArray.length == 0){
			return;
		}
		operatorHandler.removeOperator(alpine.flow.WorkFlowManager.getEditingFlow(), selectOperatorUidArray, function (operatorSet){
			for(var i = 0;i < selectOperatorUidArray.length;i++){
				var currentOpUid = selectOperatorUidArray[i];
				linkHandler.inactiveOperator({
					paneId: constants.CANVAS,
					operatorUid: currentOpUid
				});
				painter.removeOperatorUnit(constants.CANVAS, currentOpUid);
				painter.removeLink({
					paneId: constants.CANVAS,
					judgementAbleToRemove: function(sourceId, targetId){
						return sourceId == currentOpUid || targetId == currentOpUid;
					}
				});
			}
			selectOperatorUidArray = [];
			_validateOperators(operatorSet);
            alpine.flow.WorkFlowUIHelper.setDirty(true);
    		editToolbarHelper.resetOperatorToolbar();
		}, function(err){
			console.log(err);
		},constants.CANVAS );
	}
	
	function _renameSelectedOperator(){
        var selOp = _getSelectedOperator();
        renameEditor.setupLabelEditor({
            operatorUid: selOp.uid,
            paneId: constants.CANVAS
        });
	}
	
	function _getSelectedOperator(nullOK){
        if(selectOperatorUidArray.length == 0 ){
			if (nullOK) {
                return null;
            } else {
                throw "No operator is selected";
            }
		}
		return operatorHandler.getOperatorPrimaryInfo(selectOperatorUidArray[0]);
	}
	
	function _getSelectOperatorUidArray(){
		return dojo.clone(selectOperatorUidArray);
	}
	
	function _resetSelectedOperators(){
		 var selectOpUids = _getSelectOperatorUidArray();
		for(var i = 0;i < selectOpUids.length;i++){
			_resetOperator(selectOpUids[i]);
		};
	}
	
	function _release(){
		operatorStatus = [];
		selectOperatorUidArray = [];
		operatorHandler.release();
		renameEditor.release();
    }
	
	return {
		addOperator: _addOperator,
		gridDragAdaptor: _gridDragAdaptor,
		focusOperator: _focusOperator,
        unFocusOperator: _unFocusOperator,
		selectOperator: _selectOperator,
		resetOperator: _resetOperator,
		toggleOperatorFocus: _toggleOperatorFocus,
		validateOperators: _validateOperators,
		fillOperatorInfo: _fillOperatorInfo,
		getSelectedOperator: _getSelectedOperator,
		renameSelectedOperator: _renameSelectedOperator,
		getSelectOperatorUidArray: _getSelectOperatorUidArray,
		deleteSelectedOperators: _deleteOperators,
		resetSelectedOperators: _resetSelectedOperators,
		release: _release
	};
});