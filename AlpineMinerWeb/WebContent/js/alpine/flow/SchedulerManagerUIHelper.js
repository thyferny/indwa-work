define(["alpine/flow/SchedulerManager",
        "dojo/_base/lang",
        "dojo/dom-class"], function(remoteHandler, lang, domClass){
	var constants = {
		MENU: "scheduler_button",
		DIALOG: "alpine_flow_scheduler_dialog",
		BTN_ADD_TASK: "alpine_flow_scheduler_addTaskBtn",
		BTN_SUBMIT: "alpine_flow_scheduler_btn_submit",
		TASK_GRID: "alpine_flow_scheduler_task_dataGrid_id",
		TASK_GRID_CONTAINER: "alpine_flow_scheduler_task_dataGrid",

		EDITOR_BTN_REPEAT_TYPE: "alpine_flow_scheduler_editor_repeat_type",
		EDITOR_INTERVAL_AREA: "alpine_flow_scheduler_editor_repeat_interval_area",
		EDITOR_SCHEDULER_AREA: "alpine_flow_scheduler_editor_repeat_scheduler_area",
		EDITOR_TASK_STATUS_AREA: "alpine_flow_scheduler_editor_status_area",
		EDITOR_TASK_STATUS_SWITCH: "alpine_flow_scheduler_editor_status_switch",
		
		EDITOR_FORM: "alpine_flow_scheduler_editor_form",
		EDITOR_TASK_NAME: "alpine_flow_scheduler_editor_task_name",
		EDITOR_FLOW_SELECTED_PANE: "alpine_flow_scheduler_editor_workflowSelectorPane",
		EDITOR_FLOW_SELECTED: "alpine_flow_scheduler_editor_flow",
		EDITOR_FLOW_BUTTON_MOVE_UP: "alpine_flow_scheduler_editor_flow_moveup",
		EDITOR_FLOW_BUTTON_MOVE_DOWN: "alpine_flow_scheduler_editor_flow_movedown",
		EDITOR_FLOW_BUTTON_MOVE_LEFT: "alpine_flow_scheduler_editor_flow_moveleft",
		EDITOR_FLOW_BUTTON_MOVE_RIGHT: "alpine_flow_scheduler_editor_flow_moveright",
		EDITOR_FLOW_ORIGNAL: "alpine_flow_scheduler_editor_flow_orignal",
		EDITOR_FLOW_START_DATE: "alpine_flow_scheduler_editor_execute_startdate",
		EDITOR_FLOW_REPEAT_TYPE: "alpine_flow_scheduler_editor_repeat_type",
		EDITOR_FLOW_REPEAT_INTERVAL_VAL: "alpine_flow_scheduler_editor_repeat_interval_val",
		EDITOR_FLOW_REPEAT_INTERVAL_UNIT: "alpine_flow_scheduler_editor_repeat_interval_unit",
		EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL: "alpine_flow_scheduler_editor_repeat_scheduler_time",
		EDITOR_FLOW_REPEAT_SCHEDULE_WEEK_VAL: "alpine_flow_scheduler_editor_repeat_scheduler_week",
		EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL: "alpine_flow_scheduler_editor_repeat_scheduler_month",

		EDITOR_TERMINATION_RADIO_NEVER: "alpine_flow_scheduler_editor_terminal_never",
		EDITOR_TERMINATION_RADIO_TIMES: "alpine_flow_scheduler_editor_terminal_times",
		EDITOR_TERMINATION_RADIO_DATE: "alpine_flow_scheduler_editor_terminal_date",

		EDITOR_TERMINATION_TIMES_VAL: "alpine_flow_scheduler_editor_terminal_times_val",
		EDITOR_TERMINATION_DATE_VAL: "alpine_flow_scheduler_editor_terminal_date_val",
			
		REPEAT_TYPE_INTERVAL: "interval",
		REPEAT_TYPE_SCHEDULE: "schedule",
		REPEAT_TYPE_SCHEDULER_DAILY: "cron-day",
		REPEAT_TYPE_SCHEDULER_WEEK: "cron-week",
		REPEAT_TYPE_SCHEDULER_MONTH: "cron-month",
		
		TASK_STATUS_SWITCH_SUFFIX: "_statusSwitch",
		TASK_REMOVE_LINK_SUFFIX: "_removeLink",
		
		FLOW_SEPARTOR: "#"
	};
	
	var eventTracker = [];
	var gridWidgetTracker = [];
	
	/*
	 *	key = taskName,
	 *	value = TaskInfo.class
	 */
	var taskInfoMap = {};
	
	var removedTaskList = [];
	
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.MENU), "onClick", _startup);
		dojo.connect(dijit.byId(constants.DIALOG), "onHide", _clean);
		dojo.connect(dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE), "onChange", _changeRepeatSettingArea);
		dojo.connect(dijit.byId(constants.BTN_SUBMIT), "onClick", _submitData);
//		dojo.connect(dojo.byId(constants.EDITOR_TASK_STATUS_SWITCH), "onclick", function(){
//			var taskName = dijit.byId(constants.EDITOR_TASK_NAME).get("value");
//			dojo.style(dojo.byId(constants.EDITOR_TASK_STATUS_AREA), "display", "none");
//		});
		dojo.connect(dijit.byId(constants.EDITOR_FLOW_START_DATE), "onChange", function(val ){
			if(val){
				dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).constraints.min = val;
			}else{
				delete dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).constraints.min;
			}
		});
		dojo.connect(dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL), "onChange", function(val){
			if(val){
				dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max = val;
			}else{
				delete dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max;
			}
		});
		
		dijit.byId(constants.EDITOR_FLOW_SELECTED_PANE).watch("open", function(attr, from, to){
			if(to){
				dijit.byId(constants.EDITOR_FLOW_SELECTED_PANE).set("title", "&nbsp;");
			}else{
				dijit.byId(constants.EDITOR_FLOW_SELECTED_PANE).set("title", _buildFlowSummaryTitle());
			}
//			_resizeDialog(to);
		});
		

		dojo.connect(dijit.byId(constants.EDITOR_TERMINATION_RADIO_NEVER), "onChange", function(val){
			if(val){
				dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).set("disabled", true);
				dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).set("disabled", true);
				delete dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max;
			}
		});
		dojo.connect(dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES), "onChange", function(val){
			if(val){
				dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).set("disabled", false);
				dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).set("disabled", true);
				delete dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max;
			}
		});
		dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).watch("disabled", function(attr, from, to){
			dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).set("disabled", !(!to && this.get("checked")));
		});
		
		dojo.connect(dijit.byId(constants.EDITOR_TERMINATION_RADIO_DATE), "onChange", function(val){
			if(val){
				dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).set("disabled", true);
				dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).set("disabled", false);
				if(dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).get("value")){
					dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max = dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).get("value");
				}else{
					delete dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max;
				}
			}
		});
		dojo.connect(dojo.byId(constants.BTN_ADD_TASK), "onclick", _preAddTask);
		

		dojo.byId(constants.EDITOR_FLOW_SELECTED).ondblclick = function(){
			taskFlowSelect.move(constants.EDITOR_FLOW_SELECTED, constants.EDITOR_FLOW_ORIGNAL);
		};
		dojo.byId(constants.EDITOR_FLOW_ORIGNAL).ondblclick = function(){
			taskFlowSelect.move(constants.EDITOR_FLOW_ORIGNAL, constants.EDITOR_FLOW_SELECTED);
		};
		dojo.connect(dijit.byId(constants.EDITOR_FLOW_BUTTON_MOVE_UP), "onClick", function(){
			taskFlowSelect.changeIndex(-1);
		});
		dojo.connect(dijit.byId(constants.EDITOR_FLOW_BUTTON_MOVE_DOWN), "onClick", function(){
			taskFlowSelect.changeIndex(1);
		});
		dojo.connect(dijit.byId(constants.EDITOR_FLOW_BUTTON_MOVE_LEFT), "onClick", function(){
			taskFlowSelect.move(constants.EDITOR_FLOW_ORIGNAL, constants.EDITOR_FLOW_SELECTED);
		});
		dojo.connect(dijit.byId(constants.EDITOR_FLOW_BUTTON_MOVE_RIGHT), "onClick", function(){
			taskFlowSelect.move(constants.EDITOR_FLOW_SELECTED, constants.EDITOR_FLOW_ORIGNAL);
		});
		
		
		dijit.byId(constants.EDITOR_TASK_NAME).isValid = function(){
			var val = this.get("value");
			var check = this.validator(val);
			if(!check){
				this.invalidMessage = this.messages.invalidMessage;
				return check;
			}
			for(var attr in taskInfoMap){
				if(taskInfoMap[attr].taskName == val){
					this.invalidMessage = alpine.nls.scheduler_validation_duplicate_value;
					return false;
				}
			}
			return true;
		};
		
//		dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).isValid = function(){
//			var val = this.get("value");
//			if(dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value") == constants.REPEAT_TYPE_INTERVAL){
//				this.required = true;
//				return this.validator(val);
//			}else{
//				this.required = false;
//				return true;
//			}
//		};
//		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).isValid = function(){
//			var val = this.get("value");
//			if(dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value") == constants.REPEAT_TYPE_SCHEDULER_DAILY){
//				this.required = true;
//				return this.validator(val);
//			}else{
//				this.required = false;
//				return true;
//			}
//		};
//		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).isValid = function(){
//			var val = this.get("value");
//			if(dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value") == constants.REPEAT_TYPE_SCHEDULER_MONTH){
//				this.required = true;
//				return this.validator(val);
//			}else{
//				this.required = false;
//				return true;
//			}
//		};
//
//		dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).isValid = function(){
//			var val = this.get("value");
//			if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).get("checked")){
//				this.required = true;
//				return this.validator(val);
//			}else{
//				this.required = false;
//				return true;
//			}
//		};
//		dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).isValid = function(){
//			var val = this.get("value");
//			if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_DATE).get("checked")){
//				this.required = true;
//				return val != null && val instanceof Date;
//			}else{
//				this.required = false;
//				return true;
//			}
//		};
	});
	
	function _startup(){
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
		_changeRepeatSettingArea(constants.REPEAT_TYPE_INTERVAL);
		remoteHandler.getAllTasksFromUser(function(data){
			var gridData = [];
			for(var i = 0;i < data.length;i++){
				if(data[i].trigger.startDate){
					data[i].trigger.startDate = new Date(data[i].trigger.startDate);
				}
				if(data[i].trigger.endDate){
					data[i].trigger.endDate = new Date(data[i].trigger.endDate);
				}
				taskInfoMap[data[i].taskName] = data[i];
				gridData.push({
					taskName: data[i].taskName,
					isValid: data[i].trigger.isValid,
					flowCount: _countScheduleFlow(data[i])
				});
			}
			_buildTaskGrid(gridData);
			initUserFlow();
			_resetForm();
//			dijit.byId(constants.EDITOR_TASK_NAME).focus(); // fix Pivotal 41945165
		}, null, constants.DIALOG);
	}
	
	function _clean(){
		var event;
		while((event = eventTracker.pop()) != null){
			dojo.disconnect(event);
		}
		var gridWidget;
		while((gridWidget = gridWidgetTracker.pop()) != null){
			gridWidget.destroyRecursive();
		}
		taskInfoMap = {};
		removedTaskList = [];
	}
	
	function _buildTaskGrid(data){
		var grid = dijit.byId(constants.TASK_GRID);
		var dataStore = new dojo.data.ItemFileWriteStore({
			data: {
				identifier: "taskName",
				items: data
			}
		});
		if(!grid){
			grid = new dojox.grid.DataGrid({
				store: dataStore,
				id: constants.TASK_GRID,
				style: "height: 100%",
				query: {"taskName": "*"},
	            canSort: function(){return false;},
				selectionMode: "single",
				structure: [
	                {
	                	name: "",
	                	field: "isValid", 
	                	width: "30px",
	                    formatter: function(value, rowId, cellId, cellField) {
	                    	var className = value ? "switch_on" : "switch_off";
	                    	var button = dojo.create("div", {
	                    		className: className, 
	                    		lang: toolkit.getValue(this.grid.getItem(rowId).taskName),
	                    		style: "cursor: pointer",
	                    		onclick: "alpine.flow.SchedulerManagerUIHelper.switchTask(this)"
            				});
	                    	return button.outerHTML;
	                    }
	                }, {
	                	name: "",
	                	field: "taskName", 
	                	width: "90%",
	                    formatter: function(value, rowId, cellId, cellField) {
	                    	var rowInfo = this.grid.getItem(rowId);
	                    	var msgTemplate = alpine.nls.scheduler_grid_item_workflow_count;
	                    	msgTemplate = dojo.string.substitute(msgTemplate, [toolkit.getValue(rowInfo.flowCount)]);
	                        return "<B>" + value + "</B><BR/>" + msgTemplate;
	                    }
	                }, {
	                	name: "",
	                	field: "taskName",
	                	width: "50px",
	                	formatter: function(value, rowId, cellId, cellField){
	             			var removeLink = dojo.create("a", {
	             				id: value + constants.TASK_REMOVE_LINK_SUFFIX,
	             				href: "#",
	             				name: value,
	             				style: "display: none;",
	             				onclick: "alpine.flow.SchedulerManagerUIHelper.removeTask(this.name)",
	             				innerHTML: alpine.nls.scheduler_button_remove
	             			});
	             			return removeLink.outerHTML;
	                	}
	                }
				]
			},dojo.create("div", {}, constants.TASK_GRID_CONTAINER));
			grid.onRowMouseOver = function(e){
				if(e.rowIndex == undefined){
	                return;
	            }
	            var currentItem = this.getItem(e.rowIndex);
	            if(!currentItem){
	            	return;// to avoid the exception after delete last item.
	            }
	            dojo.style(dojo.byId(toolkit.getValue(currentItem.taskName) + constants.TASK_REMOVE_LINK_SUFFIX), "display", "block");
        	};
        	grid.onRowMouseOut = function(e){
				if(e.rowIndex == undefined){
	                return;
	            }
	            var currentItem = this.getItem(e.rowIndex);
	            dojo.style(dojo.byId(toolkit.getValue(currentItem.taskName) + constants.TASK_REMOVE_LINK_SUFFIX), "display", "none");
        	};
        	dojo.connect(grid, "onCellClick", function(e){
				if(e.cell.index != 1 || e.rowIndex == undefined){
				    return;
				}
				var row = this.getItem(e.rowIndex);
				var taskName = this.store.getValue(row,"taskName");
				_preUpdateTask(taskName);
        	});
			grid.startup();
		}else{
			grid.setStore(dataStore);
		}
	}
	
	function _changeRepeatSettingArea(repeatVal){
		switch(repeatVal){
		case constants.REPEAT_TYPE_INTERVAL: 
			_switchRepeatPanel(constants.REPEAT_TYPE_INTERVAL);;
			dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).set("disabled", false);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).set("disabled", true);
			break;
		case constants.REPEAT_TYPE_SCHEDULER_DAILY: 
			_switchCronInterval("cron-daily");
			_switchRepeatPanel(constants.REPEAT_TYPE_SCHEDULE);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).set("disabled", true);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).set("disabled", true);
			break;
		case constants.REPEAT_TYPE_SCHEDULER_WEEK: 
			_switchCronInterval("cron-weekly");
			_switchRepeatPanel(constants.REPEAT_TYPE_SCHEDULE);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).set("disabled", true);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).set("disabled", true);
			break;
		case constants.REPEAT_TYPE_SCHEDULER_MONTH: 
			_switchCronInterval("cron-monthly");
			_switchRepeatPanel(constants.REPEAT_TYPE_SCHEDULE);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).set("disabled", false);
			dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).set("disabled", true);
			break;
		}
	}
	
	var taskFlowSelect = {
		originId: constants.EDITOR_FLOW_ORIGNAL,
		checkedId: constants.EDITOR_FLOW_SELECTED,
		separtor: "#",
		
		move: function(fromId, toId){
			var fromSel = dojo.byId(fromId);
			var toSel = dojo.byId(toId);
			for(var i = 0;i < fromSel.options.length;i++){
				if(fromSel.options[i].selected){
					this._moveOption(fromSel, toSel, i);
					i--;
				}
			}
		},
		_moveOption: function(fromSel, toSel, itemIdx){
			var op = fromSel.options[itemIdx];
			var newOp = new Option(op.text, op.value);
			newOp.title = op.text;
			newOp.data = op.data;
			toSel.options.add(newOp);
			fromSel.remove(itemIdx);
		},
		changeIndex: function(offset){
			var checkedOps = dojo.byId(this.checkedId).options;
			var selectedIdx = -1;
			for(var i = 0;i < checkedOps.length;i++){
				if(checkedOps[i].selected){
					if(selectedIdx == -1){
						selectedIdx = i;
					}else{
						return;// only support move single item.
					}
				}
			}
			if(		(selectedIdx == -1) || //avoid selected nothing to click change index.
					(offset < 0 && selectedIdx == 0) || 
					(offset > 0 && selectedIdx == checkedOps.length - 1)){
				return;
			}
			swapNode(checkedOps[selectedIdx + offset], checkedOps[selectedIdx]);
			
			function swapNode(node1,node2){
				var parent = node1.parentNode;
				var t1 = node1.nextSibling;
				var t2 = node2.nextSibling;
				if(t1) 
					parent.insertBefore(node2,t1);
				else 
					parent.appendChild(node2);
				if(t2) 
					parent.insertBefore(node1,t2);
				else 
					parent.appendChild(node1);

			}
		},
		getSelectedItems: function(){
			var selectedList = new Array();
			var checkedOps = dojo.byId(this.checkedId).options;
			for(var i = 0;i < checkedOps.length;i++){
				selectedList.push({
					absolutelyName: checkedOps[i].value,
					simpleName: checkedOps[i].data.name,
					version: checkedOps[i].data.info.version
				});
			}
			return selectedList;
		},
		setValues: function(/*String*/values){
			this.reset();
			var originSel = dojo.byId(this.originId);
			var targetSel = dojo.byId(this.checkedId);
			var valueList = values.split(this.separtor);
			targetSel.options.length = 0;
			for(var idx = 0;idx < valueList.length;idx++){
				var value = valueList[idx];
				for(var i = 0;i < originSel.options.length;i ++){
					if(value == originSel.options[i].value){
						this._moveOption(originSel, targetSel, i);
						break;
					}
				}
			}
			this.move(this.originId, this.checkedId);
		},
		initialize: function(flowData){
			this.originData = flowData;
			
			var originSel = dojo.byId(this.originId);
			var targetSel = dojo.byId(this.checkedId);
			originSel.options.length = 0;
			targetSel.options.length = 0;
			
			dojo.forEach(flowData,function(item){
				var title = alpine.flow.FlowCategoryUIHelper.buildFlowPath(item.info);//.replace(new RegExp(" ","gm"),"");
				var option = new Option(title, item.path);
				option.title = title;
				option.data = item;
				originSel.options.add(option);
			});
		},
		reset: function(){
			this.initialize(this.originData);
		},

		isValid: function(){
			var targetSel = dojo.byId(this.checkedId);
			return targetSel.options.length > 0;
		}
	};
	
	function _switchRepeatPanel(repeatType){
		if(constants.REPEAT_TYPE_INTERVAL == repeatType){
			dojo.style(dojo.byId(constants.EDITOR_INTERVAL_AREA), "display", "");
			dojo.style(dojo.byId(constants.EDITOR_SCHEDULER_AREA), "display", "none");
			dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).set("disabled", false);
		}else{
			dojo.style(dojo.byId(constants.EDITOR_INTERVAL_AREA), "display", "none");
			dojo.style(dojo.byId(constants.EDITOR_SCHEDULER_AREA), "display", "");
			dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).set("disabled", true);
			//if terminate time was selected, then reset it to never.
			if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).get("checked")){
				dijit.byId(constants.EDITOR_TERMINATION_RADIO_NEVER).set("checked", true);
			}
		}
	}

	function _switchCronInterval(cronType){
		dojo.query(".cron-interval").forEach(function(item){
			item.style.display = "none";
		});
		dojo.query("." + cronType).forEach(function(item){
			item.style.display = "";
		});
	}
	
	function _preAddTask(){
		dijit.byId(constants.EDITOR_TASK_NAME)._hasBeenBlurred = true;
		dijit.byId(constants.EDITOR_TASK_NAME).validate();
		if(dijit.byId(constants.EDITOR_TASK_NAME).get("value") == ""){
			return;
		}
		_addOrUpdateTask(function(){
			_resetForm();
			dijit.byId(constants.EDITOR_TASK_NAME).focus();
		});
	}
	
	function _preUpdateTask(taskName){
		_addOrUpdateTask(function(){
			var taskInfo = taskInfoMap[taskName];
			_resetForm();
			_fillTaskForm(taskInfo);
			dijit.byId(constants.EDITOR_FORM).validate();
		});
	}
	
	function _fillTaskForm(taskInfo){
		dijit.byId(constants.EDITOR_TASK_NAME).set("disabled", true);
		dijit.byId(constants.EDITOR_TASK_NAME).set("value", taskInfo.taskName);
		taskFlowSelect.setValues(taskInfo.trigger.flowName);
		dijit.byId(constants.EDITOR_FLOW_START_DATE).set("value", taskInfo.trigger.startDate);
		dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).set("value", taskInfo.trigger.interval);
		dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_UNIT).set("value", taskInfo.trigger.repeatType);
		if(taskInfo.trigger.hour){
			dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).set("value", taskInfo.trigger.hour);
		}
		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).set("value", taskInfo.trigger.dayOfMonth);
		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_WEEK_VAL).set("value", taskInfo.trigger.dayOfWeek);
		if(taskInfo.trigger.repeatType == constants.REPEAT_TYPE_INTERVAL){
			dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).set("value", taskInfo.trigger.repeatType);
		}else{
			dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).set("value", taskInfo.trigger.frequence);
		}
		switch(taskInfo.trigger.terminalType){
		case "never":
			dijit.byId(constants.EDITOR_TERMINATION_RADIO_NEVER).set("checked", true);
			break;
		case "times":
			dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).set("checked", true);
			dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).set("value", taskInfo.trigger.repeatTimes);
			break;
		case "date": 
			dijit.byId(constants.EDITOR_TERMINATION_RADIO_DATE).set("checked", true);
			dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).set("value", taskInfo.trigger.endDate);
			break;
		}
		
		if(taskInfo.trigger.isValid){
			dojo.style(dojo.byId(constants.EDITOR_TASK_STATUS_AREA), "display", "none");
		}else{
			dojo.style(dojo.byId(constants.EDITOR_TASK_STATUS_AREA), "display", "block");
		}
		dijit.byId(constants.EDITOR_FLOW_SELECTED_PANE).set("open", taskInfo.trigger.flowName == "");
	}
	
	function _buildFlowSummaryTitle(){
		var flowNameList = taskFlowSelect.getSelectedItems();
		var container = "<div>";
		var length = flowNameList.length > 10 ? 10 : flowNameList.length;
		for(var i = 0;i < length;i++){
			container += flowNameList[i].absolutelyName;
			container += "<br/>";
		}
		if(length == 10){
			container += "...";
		}else if(length == 0){
			container += "&nbsp;";
		}
		return container + "</div>";
	}
	
	function _resetForm(){
		dijit.byId(constants.EDITOR_TASK_NAME).set("disabled", false);
		dijit.byId(constants.EDITOR_TASK_NAME).reset();
		taskFlowSelect.reset();
		dijit.byId(constants.EDITOR_FLOW_SELECTED_PANE).set("title", "&nbsp;");
		dijit.byId(constants.EDITOR_FLOW_SELECTED_PANE).set("open", true);
		dijit.byId(constants.EDITOR_FLOW_START_DATE).reset();
		dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).reset();
		dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).reset();
		dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_UNIT).reset();
		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).reset();
		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).reset();
		dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_WEEK_VAL).reset();
		dijit.byId(constants.EDITOR_TERMINATION_RADIO_NEVER).reset();
		dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).reset();
		dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).reset();
		dijit.byId(constants.EDITOR_TERMINATION_RADIO_DATE).reset();
		dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).reset();
		delete dijit.byId(constants.EDITOR_FLOW_START_DATE).constraints.max;
		delete dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).constraints.min;
	}
	
	function _addOrUpdateTask(onComplete){
		var taskName = dijit.byId(constants.EDITOR_TASK_NAME).get("value");
		if(taskName == "" || !isEditorChange()){
			onComplete.call();
			return;
		}
		var taskName = _syncEditorToMap();
		_refreshGrid(taskInfoMap[taskName], onComplete);
	}
	
	function _syncEditorToMap(){
		var taskInfo = _buildTaskInfo();
		var orignalTask = taskInfoMap[taskInfo.taskName] || {
			trigger: {
				isValid: true
			}
		};
		if(!dijit.byId(constants.EDITOR_FORM).validate() || !_validateTask(taskInfo)){
			orignalTask.trigger.isValid = false;
		}
		taskInfoMap[taskInfo.taskName] = {
			taskName: taskInfo.taskName,
			trigger: lang.mixin(orignalTask.trigger, taskInfo.trigger)
		};
		return taskInfo.taskName;
	}
	
	function _buildTaskInfo(){
		var flowList = taskFlowSelect.getSelectedItems();
		var flowNames = new Array();
		var flowPurelyNames = new Array();
		var flowVersions = new Array();
		for(var i = 0;i < flowList.length;i++){
			flowNames.push(flowList[i].absolutelyName);
			flowPurelyNames.push(flowList[i].simpleName);
			flowVersions.push(flowList[i].version);
		}
		var flowName = flowNames.join(taskFlowSelect.separtor);
		var flowPurelyName = flowPurelyNames.join(taskFlowSelect.separtor);
		var flowVersion = flowVersions.join(taskFlowSelect.separtor);
		var repeatType = dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value") == constants.REPEAT_TYPE_INTERVAL ? 
							constants.REPEAT_TYPE_INTERVAL : constants.REPEAT_TYPE_SCHEDULE;
		var scheduleFrequence = null;
		if(repeatType != constants.REPEAT_TYPE_INTERVAL){
			scheduleFrequence = dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value");
		}
		var terminalType = null;
		if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_NEVER).get("checked")){
			terminalType = "never";
		}else if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).get("checked")){
			terminalType = "times";
		}else if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_DATE).get("checked")){
			terminalType = "date";
		}
		
		return {
			taskName: dijit.byId(constants.EDITOR_TASK_NAME).get("value"), 
			trigger: {
				taskName: dijit.byId(constants.EDITOR_TASK_NAME).get("value"),
				flowName: flowName,
				flowPurelyName: flowPurelyName,
				flowVersion: flowVersion,
				name: dijit.byId(constants.EDITOR_TASK_NAME).get("value"),
				type: repeatType,
				startDate: dijit.byId(constants.EDITOR_FLOW_START_DATE).get("value"),
				//for interval
				interval: dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).get("value"),
				repeatType: dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_UNIT).get("value"),
				//for schedule
				frequence: scheduleFrequence,
				hour: dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).get("value"),
				dayOfMonth: dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).get("value"),
				dayOfWeek: dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_WEEK_VAL).get("value"),
				
				terminalType: terminalType,
				repeatTimes: dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).get("value"),
				endDate: dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).get("value")
			}
		};
	}
	
	function _refreshGrid(row, onComplete){
		var store = dijit.byId(constants.TASK_GRID).store;
		store.fetchItemByIdentity({
			identity: row.taskName,
			onItem: function(item){
				if(item){
					_updateRow(item, row);
				}else{
					_addRow(row);
				}
				store.save({
					onComplete: function(){
						if(onComplete){
							onComplete.call();
						}
					}
				});
			}
		});
		
		function _addRow(newData){
			var row = {
				taskName: newData.taskName,
				isValid: newData.trigger.isValid,
				flowCount: _countScheduleFlow(newData)
			};
			dijit.byId(constants.TASK_GRID).store.newItem(row);
		}
		function _updateRow(rowItem, newData){
			dijit.byId(constants.TASK_GRID).store.setValue(rowItem, "isValid", newData.trigger.isValid);
			dijit.byId(constants.TASK_GRID).store.setValue(rowItem, "flowCount", _countScheduleFlow(newData));
		}
	}
	
	// initialize personal flows for editor panel
	function initUserFlow(){
		var flowInfoList = remoteHandler.getFlowFromUser();
		taskFlowSelect.initialize(flowInfoList);
	}
	
	function _countScheduleFlow(taskInfo){
		var flowCount = 0;
		if(taskInfo.trigger.flowName != ""){
			flowCount = taskInfo.trigger.flowName.split(constants.FLOW_SEPARTOR).length;
		}
		return flowCount;
	}
	
	function _removeTask(taskName){
		var store = dijit.byId(constants.TASK_GRID).store;
		store.fetchItemByIdentity({
			identity: taskName,
			onItem: function(item){
				if(!item){
					return;
				}
				var removedTask = taskInfoMap[taskName];
				delete taskInfoMap[taskName];
				removedTaskList.push(removedTask);
				dijit.byId(constants.TASK_GRID).store.deleteItem(item);
				store.save({
					onComplete: function(){
						if(dijit.byId(constants.EDITOR_TASK_NAME).get("value") == taskName){
							_resetForm();
						}
					}
				});
			}
		});
	}
	
	function isEditorChange(){
		var taskName = dijit.byId(constants.EDITOR_TASK_NAME).get("value");
		var storedInfo = taskInfoMap[taskName];

		var flowList = taskFlowSelect.getSelectedItems();
		var flowNames = new Array();
		var flowPurelyNames = new Array();
		var flowVersions = new Array();
		for(var i = 0;i < flowList.length;i++){
			flowNames.push(flowList[i].absolutelyName);
			flowPurelyNames.push(flowList[i].simpleName);
			flowVersions.push(flowList[i].version);
		}
		var flowName = flowNames.join(taskFlowSelect.separtor);
		var flowPurelyName = flowPurelyNames.join(taskFlowSelect.separtor);
		var flowVersion = flowVersions.join(taskFlowSelect.separtor);
		var type = dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value") == constants.REPEAT_TYPE_INTERVAL ? 
							constants.REPEAT_TYPE_INTERVAL : constants.REPEAT_TYPE_SCHEDULE;
		var scheduleFrequence = null;
		if(type != constants.REPEAT_TYPE_INTERVAL){
			scheduleFrequence = dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value");
		}
		var terminalType = null;
		if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_NEVER).get("checked")){
			terminalType = "never";
		}else if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_TIMES).get("checked")){
			terminalType = "times";
		}else if(dijit.byId(constants.EDITOR_TERMINATION_RADIO_DATE).get("checked")){
			terminalType = "date";
		}
		if(_isEmpty(dijit.byId(constants.EDITOR_TASK_NAME).get("value"))
			&& flowList.length == 0
			&& _isEmpty(dijit.byId(constants.EDITOR_FLOW_START_DATE).get("value"))
			&& isNaN(dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).get("value"))
			&& isNaN(dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).get("value"))
			&& isNaN(dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).get("value"))
			&& _isEmpty(dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).get("value"))){
			return false;
		}

		if(!storedInfo){
			return true;
		}
		if(storedInfo.taskName != dijit.byId(constants.EDITOR_TASK_NAME).get("value")){
			return true;
		}else if(storedInfo.trigger.flowName != flowName){
			return true;
		}else if(storedInfo.trigger.flowPurelyName != flowPurelyName){
			return true;
		}else if(storedInfo.trigger.flowVersion != flowVersion){
			return true;
		}else if(storedInfo.trigger.name != dijit.byId(constants.EDITOR_TASK_NAME).get("value")){
			return true;
		}else if(storedInfo.trigger.type != type){
			return true;
		}else if(storedInfo.trigger.startDate != dijit.byId(constants.EDITOR_FLOW_START_DATE).get("value")){
			return true;
		}else if(storedInfo.trigger.frequence != scheduleFrequence){
			return true;
		}else if(storedInfo.trigger.terminalType != terminalType){
			return true;
		}
		
		switch(dijit.byId(constants.EDITOR_BTN_REPEAT_TYPE).get("value")){
		case constants.REPEAT_TYPE_INTERVAL:
			if(!(isNaN(storedInfo.trigger.interval) && isNaN(dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).get("value")))
					&& storedInfo.trigger.interval != dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_VAL).get("value")){
				return true;
			}else if(storedInfo.trigger.repeatType != dijit.byId(constants.EDITOR_FLOW_REPEAT_INTERVAL_UNIT).get("value")){
				return true;
			}
			break;
		case constants.REPEAT_TYPE_SCHEDULER_WEEK:
			if(storedInfo.trigger.dayOfWeek != dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_WEEK_VAL).get("value")){
				return true;
			}else if(storedInfo.trigger.hour != dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).get("value")){
				return true;
			}
			break;
		case constants.REPEAT_TYPE_SCHEDULER_MONTH:
			if(!(isNaN(storedInfo.trigger.dayOfMonth) && isNaN(dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).get("value")))
					&& storedInfo.trigger.dayOfMonth != dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_MONTHLY_VAL).get("value")){
				return true;
			}else if(storedInfo.trigger.hour != dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).get("value")){
				return true;
			}
			break;
		case constants.REPEAT_TYPE_SCHEDULER_DAILY: 
			if(storedInfo.trigger.hour != dijit.byId(constants.EDITOR_FLOW_REPEAT_SCHEDULE_DAILY_VAL).get("value")){
				return true;
			}
			break;
		}
		
		if(terminalType == "times"){
			if(!(isNaN(storedInfo.trigger.repeatTimes) && isNaN(dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).get("value")))
					&& storedInfo.trigger.repeatTimes != dijit.byId(constants.EDITOR_TERMINATION_TIMES_VAL).get("value")){
				return true;
			}
		}else if(terminalType == "date"){
			if(storedInfo.trigger.endDate != dijit.byId(constants.EDITOR_TERMINATION_DATE_VAL).get("value")){
				return true;
			}
		}
		
		return false;
	}
	
	function _isEmpty(val){
		return val == null || val =="";
	}
	
	function _isNumber(val){
		return /^[1-9][\d]*$/.test(val);
	}
	
	function _validateTask(taskInfo){
		if(taskInfo.taskName == ""
			|| !/^[\w]{1,20}$/.test(taskInfo.taskName)){
			return false;
		}else if(_isEmpty(taskInfo.trigger.flowName)){
			return false;
		}else if(taskInfo.trigger.startDate != null && !taskInfo.trigger.startDate instanceof Date){
			return false;
		}else if(taskInfo.trigger.type == constants.REPEAT_TYPE_INTERVAL 
				&& !_isNumber(taskInfo.trigger.interval)){
			return false;
		}
		if(taskInfo.trigger.frequence != null){
			switch(taskInfo.trigger.frequence){
			case constants.REPEAT_TYPE_SCHEDULER_WEEK:
			case constants.REPEAT_TYPE_SCHEDULER_DAILY:
				if(!_isNumber(taskInfo.trigger.hour) 
						|| taskInfo.trigger.hour < 0
						|| taskInfo.trigger.hour > 23){
					return false;
				}
				break;
			case constants.REPEAT_TYPE_SCHEDULER_MONTH:
				if(!_isNumber(taskInfo.trigger.hour) 
						|| taskInfo.trigger.hour < 0
						|| taskInfo.trigger.hour > 23){
					return false;
				}else if(!_isNumber(taskInfo.trigger.dayOfMonth)
							|| taskInfo.trigger.dayOfMonth < 1
							|| taskInfo.trigger.dayOfMonth > 31){
					return false;
				}
				break;
			}
		}
		switch(taskInfo.trigger.terminalType){
		case "times":
			if(!_isNumber(taskInfo.trigger.repeatTimes)){
				return false;
			}
			break;
		case "date":
			if(taskInfo.trigger.endDate == null 
					|| !taskInfo.trigger.endDate instanceof Date
					|| (taskInfo.trigger.startDate != null && taskInfo.trigger.endDate.getTime() < taskInfo.trigger.startDate.getTime())){
				return false;
			}
			break;
		}
		return true;
	}
	
//	function _resizeDialog(isOriginalSize){
//		var layer = dijit.byId(constants.DIALOG).getChildren()[0];
//		var newSize = {
//			w: layer.initWidth,
//			h: isOriginalSize ? 600 : 350
//		};
//		layer.resize(newSize);
//	}
	
	function _submitData(){
		_addOrUpdateTask(function(){
			var taskInfoSet = new Array(); 
			for(var taskName in taskInfoMap){
				taskInfoSet.push(taskInfoMap[taskName]);
			}
			remoteHandler.saveTaskInfo(taskInfoSet, function(){
				dijit.byId(constants.DIALOG).hide();
			}, constants.DIALOG);
		});
	}
	
	function _switchTask(switchNode){
		var taskName = switchNode.lang;
		var turnTo = !taskInfoMap[taskName].trigger.isValid;
		if(taskName == dijit.byId(constants.EDITOR_TASK_NAME).get("value")){
			_syncEditorToMap();
		}
		if(turnTo && !_validateTask(taskInfoMap[taskName])){
			var messageTemplate = alpine.nls.scheduler_validation_cannot_turn_enable;
			var msg = dojo.string.substitute(messageTemplate, [taskName]);
			popupComponent.alert(msg);
			return;
		}
		taskInfoMap[taskName].trigger.isValid = turnTo;
		switchNode.className = taskInfoMap[taskName].trigger.isValid ? "switch_on" : "switch_off";
	}
	
	return {
		removeTask: _removeTask,
		switchTask: _switchTask
	};
});