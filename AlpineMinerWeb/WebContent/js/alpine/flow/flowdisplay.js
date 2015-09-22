/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 *
 * flowdisplay.js
 *
 * Author sam_zang
 *
 * Version 3.0
 *
 * Date Aug 19, 2011
 */


//var CurrentOperator_uuid = null;
var icon_size = 45;
var half_icon_size = icon_size / 2;
var link_adjust = 32;
var arrow_size = 10;
var font_size = "9pt";
if (dojo.isFF) {
    font_size = "8pt";
}
var x_off;
var y_off;
var x_off_personal;
var y_off_personal;
//var ShapeList = null;
//var CurrentFlowDTO = null;
//this is for add public and group flow dialog
var CurrentFlow_Group = null ;
var CurrentAddingFlow_Version = null ;

//following is for memory leaking
var flowTreeMenu =null;
var flowTreeMenuHandlers = new Array();

//var latestStepRanOperatorUid = null;

//var current_flow_surface =new Array();
//var current_flow_menus = new Array();
//var current_flow_handlers = new Array();

function open_flow_callback(type, obj) {
    progressBar.closeLoadingBar();
    if (obj.error_code&&obj.error_code!=0) {
        if (obj.error_code == -1) {
            popupComponent.alert(alpine.nls.no_login,"",  function(){
                window.top.location.pathname = loginURL;
            });
            return;
        }
        else {
            var msg = alpine.nls.flow_not_found;
            if (obj.message) {
                msg = obj.message;
            }
            popupComponent.alert(msg);
            clear_flow_display(type);// must not be personal
            return;
        }

    }

//    if (type == "Personal") {
////        CurrentFlow = obj.flowInfo;
//        operatorLinkList = obj.links;
////        operatorList = obj.result;
//        CurrentFlowDTO = obj;
//        //save the current flow
////        save_current_flow(CurrentFlow);
//        alpine.flow.WorkFlowManager.storeEditingFlow(obj.flowInfo);
////		var str = "  /  " + CurrentFlow.id + " [" + CurrentFlow.version + "]";
////        var title = alpine.flow.FlowCategoryUIHelper.buildFlowPath(CurrentFlow);
//        alpine.flow.WorkFlowUIHelper.setEditingFlowLabel(displayTitle);
//    }else{
        CurrentFlow_Group= obj.flowInfo;
        //other wise it will be top
        CurrentFlow_Group.tag = "CHILD" ;
        CurrentAddingFlow_Version = CurrentFlow_Group.version*1;
        //update the flow info
        if(CurrentFlow_Group&&CurrentFlow_Group.comments){
            dojo.byId("add_flow_comments").value=CurrentFlow_Group.comments;
        }else{
            dojo.byId("add_flow_comments").value=" ";
        }

        if(CurrentFlow_Group&&CurrentFlow_Group.version ){
            dojo.byId("add_flow_version").innerHTML=CurrentFlow_Group.version ;
        }else{
            dojo.byId("add_flow_version").innerHTML="";
        }


        if(CurrentFlow_Group&&CurrentFlow_Group.modifiedUser ){
            dojo.byId("add_flow_publisher").innerHTML=CurrentFlow_Group.modifiedUser ;
        }else{
            dojo.byId("add_flow_publisher").innerHTML="";
        }

        if(CurrentFlow_Group&&CurrentFlow_Group.modifiedTime ){
            var dateTime = new Date(CurrentFlow_Group.modifiedTime ) ;//from time mills
            dateTime = alpine_format_date(dateTime);
            dojo.byId("add_flow_publish_time").innerHTML=dateTime;
        }else{
            dojo.byId("add_flow_publish_time").innerHTML="";
        }



//    }
    //when create the model popup menu, it will use this...

    draw_flow_display(type, obj.result, obj.links);
    dojo.publish("/operatorExplorer/switchWorkflowEditor", [true]);
    return true;
}

var limits = {};

function draw_flow_display(type, opList, linkList) {

    var start = new Date();
    var ableMove;
    var bindEvent;
    var bindMenu;
//    if (type == "Personal") {
////        ShapeList = new Array();
//        ableMove = true;
//        bindEvent = true;
//        bindMenu = true;
//    }else{
        ableMove = false;
        bindEvent = false;
        bindMenu = false;
//    }
//    var vs = dojo.window.getBox();
    var max_x = 0;
    var max_y = 0;
    var len_x = 0;
    var min_x = 10000;
    var min_y = 10000;

    for ( var i = 0; i < opList.length; i++) {
        var op = opList[i];
        if (op.x > max_x) {
            max_x = op.x + icon_size;
            len_x = getCharLenght(op.name) * 8;
            max_x = max_x + len_x;
        }

        if (op.y > max_y) {
            max_y = op.y + icon_size;
        }

        if (op.x < min_x) {
            min_x = op.x;
        }

        if (op.y < min_y) {
            min_y = op.y;
        }
    }

    x_off = x_offset - min_x;
    y_off = y_offset - min_y;

    max_x = max_x + x_offset + len_x;
    max_y = max_y + y_offset + 100;

    var vs = dojo.window.getBox();
    if (max_x < vs.w - 50) {
        max_x = vs.w - 50;
    }
    if (max_y < vs.h - 120) {
        max_y = vs.h - 120;
    }
    
    flowDisplayPanel = dojo.byId("FlowDisplayPanel" + type);
//    var surface = dojox.gfx.createSurface(flowDisplayPanel, max_x, max_y);
//    current_flow_surface[type] = surface;

    dojo.style(flowDisplayPanel, "width", max_x + 5);
    dojo.style(flowDisplayPanel, "height", max_y + 5);

    for ( var i = 0; i < opList.length; i++) {
        var op = opList[i];
        //var operatorIconInfo = alpine.operatorexplorer.OperatorUtil.getOperatorObjectByKey(op.classname);
        var operatorShape = alpine.flow.WorkFlowPainter.paintOperator({
        	canvasInfo: {
        		paneId: "FlowDisplayPanel" + type,
        		width: max_x,
        		height: max_y
        	},
        	op: {
        		uid: op.uid,
        		x: op.x,
        		y: op.y,
        		//icon: operatorIconInfo.icon,
                src: alpine.operatorexplorer.OperatorUtil.getStandardImageSourceByKey(op.classname),
        		width: icon_size,
        		height: icon_size,
        		label: op.name
        	},
        	ableMove: ableMove,
        	bindEvent: bindEvent,
        	onClick: function(op){
        		alpine.flow.OperatorManagementUIHelper.selectOperator(op.uid);
        	},
        	onDblClick: _bindArgToOpenPropEditor(op),
        	onMouseOver: function(op){
        		alpine.flow.OperatorManagementUIHelper.focusOperator(op.uid);
        	},
        	onMouseOut: function(op){
        		alpine.flow.OperatorManagementUIHelper.unFocusOperator(op.uid);
        	},
        	onMove: function(moveInfo){
        		setDirty(true);
        		var operatorInfo = alpine.flow.OperatorManagementManager.getOperatorPrimaryInfo(moveInfo.operatorUid);
        		operatorInfo.x += moveInfo.x;
        		operatorInfo.y += moveInfo.y;
//        	    for ( var i = 0; i < operatorList.length; i++) {
//        	        var op = operatorList[i];
//        	        if (op.uid == moveInfo.operatorUid) {
//        	            op.x = op.x + moveInfo.x;
//        	            op.y = op.y + moveInfo.y;
//        	            break;
//        	        }
//        	    }
        	}
        });
    }

    for ( var i = 0; i < linkList.length; i++) {
    	var linkInfo = {
			sourceId: linkList[i].sourceid,
			targetId: linkList[i].targetid,
			startPoint: {
				x: linkList[i].x1,
				y: linkList[i].y1
			},
			endPoint: {
				x: linkList[i].x2,
				y: linkList[i].y2
			}
    	};
//        alpine.flow.OperatorLinkManager.pushLinkInfo(linkInfo);
    	alpine.flow.WorkFlowPainter.paintLink({
    		paneId: "FlowDisplayPanel" + type,
    		link: linkInfo
    	});
    }
    

//    for ( var i = 0; i < opList.length; i++) {
//        var op = opList[i];
//        createOperatorShape(type, op, surface, x_off, y_off);
//    }
//
//    for ( var i = 0; i < linkList.length; i++) {
//        createlink(surface, linkList[i]);
//        drawlink(linkList[i], x_off, y_off);
//    }

//    if (type == "Personal") {
//        x_off_personal = x_off;
//        y_off_personal = y_off;
//        dijit.byId("cancel_flow_button").set("disabled", false);
//
//        alpine.flow.OperatorManagementUIHelper.validateOperators();
//        limits = { xmin: half_icon_size,
//            xmax: max_x - icon_size,
//            ymin: icon_size * 2,
//            ymax: max_y + half_icon_size };
//    }
    console.log("Time to open flow: ",  (new Date() - start) );
}

function _bindArgToOpenPropEditor(arg){
	return function(){
		open_property_dialog.call(null, arg);
	};
}

//dojo.declare("alpineminer.web.Mover", dojox.gfx.Mover, {
//    onMouseMove: function(event) {
//
//        var x = event.clientX;
//        var y = event.clientY;
//
//        if (x > limits.xmin
//            & y > limits.ymin
//            & x < limits.xmax
//            & y < limits.ymax) {
//            //Modify By Will
//            var pftOffsetWidth = dijit.byId("personalFlowTree").domNode.offsetWidth;
//            if(this.shape && this.shape.children && this.shape.children.length==2){
//                var textNode = this.shape.children[1];
//                var imgNode = this.shape.children[0];
//                if(dojo.isIE){
//                    var textNodeWidth = textNode.rawNode.offsetWidth!=null?textNode.rawNode.offsetWidth:80;
//                    if(textNode!=null && (x-textNodeWidth)<=pftOffsetWidth){
//                        x=pftOffsetWidth+textNodeWidth;
//                    }
//                }else{
//                    //can not acquire the svg textNode clientWidth
//                    //console.log(textNode.rawNode.length);
//                    if(textNode!=null && (x-80)<=pftOffsetWidth){
//                        x=pftOffsetWidth+80;
//                    }
//                }
//            }
//            // move the rectangle by applying a translation
//            this.shape.applyLeftTransform({
//                dx: x - this.lastX,
//                dy: y - this.lastY
//            });
//            update_links(this.host.uid, x - this.lastX, y - this.lastY);
//            this.lastX = x;
//            this.lastY = y;
//        }
//        dojo.stopEvent(event);
//    }
//});

//function createOperatorShape(type, op, surface, x_off, y_off) {
//    var top = op.y + y_off;
//    var left = op.x + x_off;
//    var img = iconpath + op.icon;
//
//
//    var imageShape = surface.createImage(
//        { x:left, y:top, width:icon_size, height:icon_size, src: img});
//    var opName= op.name;
//    var originalName=op.name;
//    if(dojo.isIE){
//        if(op.name&&op.name.length>30){
//            opName=op.name.substring(0,27)+"...";
//        }
//    }
//    var textShape = surface.createText(
//        { x:left + half_icon_size,
//            y:top + icon_size + 10,
//            text:opName,
//            align:"middle"}).setFont(
//        { family:"Arial", size:font_size, weight:"normal" }).setFill("#000000");
//
//    if(originalName && originalName.length>30){
//        if(dojo.isIE){
//            imageShape.rawNode.setAttribute("title",originalName);
//        }
//    }
//
//    if (type == "Personal") {
//
//        var handler = imageShape.connect('onclick', imageShape, function() {
//            select_operator(op);
//        });
//        current_flow_handlers.push(handler);
//
//        var handler = imageShape.connect('ondblclick', imageShape, function() {
//            open_property_dialog(op);
//        });
//        current_flow_handlers.push(handler);
//
//        var handler = imageShape.connect('onmouseover', imageShape, function() {
//            onmouseover_handler(op);
//        });
//        current_flow_handlers.push(handler);
//
//        var handler = imageShape.connect('onmouseout', imageShape, function() {
//            onmouseout_handler(op);
//        });
//        current_flow_handlers.push(handler);
//
//
//        add_operator_menu(op, imageShape.getNode());
//
//
//        var data = {};
//        data.uuid = op.uid;
//        data.name = op.name;
//        data.img = imageShape;
//        data.text = textShape;
//        data.top = top;
//        data.left = left;
//        data.icon_current = iconpath + op.icon;
//        data.icon = iconpath + op.icon;
//        data.icon_s = iconpath + op.icons;
//        data.icon_a = iconpath + op.icons;
//        data.surface = surface;
//
//        ShapeList[ShapeList.length] = data;
//
//        var group = surface.createGroup();
//        group.add(imageShape);
//        group.add(textShape);
//        var m = new dojox.gfx.Moveable(group, { mover: alpineminer.web.Mover });
//        m.uid = op.uid;
//    }
//}

//function getShapeDataByID(id) {
//    if (!ShapeList || !ShapeList.length) {
//        return null;
//    }
//    for ( var i = 0; i < ShapeList.length; i++) {
//        var op = ShapeList[i];
//        if (id == op.uuid) {
//            return op;
//        }
//    }
//    return null;
//}

//function getShapeDataByName(name) {
//    if (!ShapeList || !ShapeList.length) {
//        return null;
//    }
//    for ( var i = 0; i < ShapeList.length; i++) {
//        var op = ShapeList[i];
//        if (name == op.name) {
//            return op;
//        }
//    }
//    return null;
//}

//function update_operator_status(id, status) {
//	alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", id, function(operatorUnit){
//	    if (status == true) {
//	    	operatorUnit.label.setFont(
//	            { family:"Arial", size:font_size, weight:"normal" }).setFill("#000000");
//	    }
//	    else {
//	    	operatorUnit.label.setFont(
//	            { family:"Arial", size:font_size, weight:"normal" }).setFill("red");
//	    }
//	});
	
//    var op = getShapeDataByID(id);
//    var label = op.text;
//    if (status == true) {
//        label.setFont(
//            { family:"Arial", size:font_size, weight:"normal" }).setFill("#000000");
//    }
//    else {
//        label.setFont(
//            { family:"Arial", size:font_size, weight:"normal" }).setFill("red");
//    }

//}

//function select_operator(op) {
//    update_flow_status(operatorList);
//    if (is_running == true) {
//        return;
//    }
//
//    var data = getShapeDataByID(op.uid);
//    if (data != null) {
//        reset_operator_icons();
//        CurrentOperator_uuid = op.uid;
//        enable_step_run_flow();
//        enable_open_properties();
//        data.icon_current = data.icon_s;
////        data.img.setShape({src: data.icon_s});
//        alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", op.uid, function(operatorUnit){
//        	operatorUnit.icon.setShape({
//        		src: data.icon_s
//        	});
//        	operatorUnit.label.setFont({
//                family:"Arial",
//                weight:"bold"
//            });
//    	});
//    }
//}

//function onmouseover_handler(op) {
//    var data = getShapeDataByID(op.uid);
//    if (data != null) {
////        data.img.setShape({src: data.icon_a});
//        alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", op.uid, function(operatorUnit){
//        	operatorUnit.icon.setShape({
//        		src: data.icon_a
//        	});
//    	});
//    }
//
//}

//function onmouseout_handler(op) {
//    var data = getShapeDataByID(op.uid);
//    if (data != null) {
////        data.img.setShape({src: data.icon_current});
//        alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", op.uid, function(operatorUnit){
//        	operatorUnit.icon.setShape({
//        		src: data.icon_current
//        	});
//    	});
//    }
//
//}

function needDataExplorMenu(operator){
    return operator.hasDbTableInfo;
}

//function addParentOprators(parentOperatorList,child){
//    var index = 0;
//    for ( var i = 0; i < operatorLinkList.length; i++) {
//        var link = operatorLinkList[i];
//        if (link.targetid == child.uid) {
//            var operator= getOperatorByUid(link.sourceid);
//            if(parentOperatorList.indexOf(operator)<0){
//                parentOperatorList.push(operator);
//                addParentOprators(parentOperatorList,operator);
//            }
//
//        }
//    }
//}

//function step_run_to_operator(operator) {
//
//    if (!alpine.flow.WorkFlowManager.isEditing() ||operator==null) {
//        return;
//    }
//
//    var parentOperatorList= alpine.flow.OperatorManagementManager.getAllParentOperators(operator.uid, true);
//
////    addParentOprators(parentOperatorList,operator);
////    parentOperatorList.push(operator);
//
//    if (parentOperatorList.length>0 && 
//    		alpine.flow.OperatorManagementUIHelper.validateOperators(parentOperatorList) == false) {
//        popupComponent.alert(alpine.nls.invalid_flow);
//        return;
//    }
//    if(parentOperatorList.length == 1
//    		&& operator.uid ==parentOperatorList[0].uid
//    		&& operator.classname != "SQLExecuteOperator"
//    		&& operator.classname != "SubFlowOperator"){
//    	popupComponent.alert(alpine.nls.step_run_single_operator_error_message);
//    	return;
//    }
//
//    disable_run_flow();//TODO need replace to new api
//    if(uuid == null){
//    	uuid = Math.random();
//    }
//    var callbackFn = null;
//    if(dojo.isSafari){
//        dojo.publish('/opener/callOpen');
//    }else{
//        callbackFn = run_flow_callback;
//    }
////	latestStepRanOperatorUid = operator.uid;
//    var url = flowBaseURL + "?method=stepRunFlow"
//        + "&uuid=" + uuid
//        + "&user=" + login
//        + "&operatorUUID=" + operator.uid;
//    ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(), callbackFn, function(){
//    	is_running = false;
//		alpine.flow.OperatorManagementManager.forEachOperatorInfo(function(operatorInfo){
//			alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid, true);
//		});
//    	enable_step_run_flow();//TODO need replace to new api
//    });
//
//}

//function clearStepRunResult(operator){
//	
//	var url = flowBaseURL + "?method=clearStepRunResult&runUUid=" + uuid + "&operatorName=" + operator.name;
//	ds.get(url, null, null, false, "FlowDisplayPanelPersonal");
//}



/*function reset_operator_icons() {
//    for ( var i = 0; i < ShapeList.length; i++) {
//        var op = ShapeList[i];
//        set_unselected_image(op);
//    }

//	for(var i = 0;i < operatorList.length;i++){
//		var operatorInfo = operatorList[i];
//		alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid, true);
//	}
	alpine.flow.OperatorManagementManager.forEachOperatorInfo(function(operatorInfo){
		alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid);
	});
    
    disable_step_run_flow();
    disable_open_properties();
    LastOperatorName = null;
}*/

//function update_flow_status(list) {
//    var flow_status = true;
//    if (list == null || list.length == 0) {
//        return false;
//    }
//
//    for (var i = 0; i < list.length; i++) {
//        var id = list[i].uid;
//        var status = list[i].isValid;
//        if (status == false) {
//            flow_status = false;
//        }
//        update_operator_status(id, status);
//    }
//    return flow_status;
//}

//function createlink(surface, link) {
//    var line = surface.createLine({
//        x1 : 0,
//        y1 : 0,
//        x2 : 1,
//        y2 : 1
//    });
//    line.setFill(new dojo.Color("black"));
//    line.setStroke({
//        color : "#00CCFF",
//        width : 1
//    });
//
//    link.line = line;
//
//    var points = [ {
//        x : 0,
//        y : 0
//    }, {
//        x : 1,
//        y : 0
//    }, {
//        x : 0,
//        y : 1
//    } ];
//    var arrow = surface.createPolyline(points);
//    arrow.setFill(new dojo.Color("00CCFF"));
//    arrow.setStroke({
//        color : "00CCFF",
//        width : 1
//    });
//
//    link.arrow = arrow;
//}

//function drawlink(link, xoff, yoff) {
//    var dx;
//    var dy;
//
//    if (link.x1 == link.x2) {
//        dx = 0;
//        dy = half_icon_size;
//    }
//    else if (link.y1 == link.y2) {
//        dx = half_icon_size;
//        dy = 0;
//    }
//    else {
//        var t = (link.y2 - link.y1) / (link.x2 - link.x1);
//        var angle = Math.atan(t);
//
//        dx = link_adjust * Math.cos(angle);
//        dy = link_adjust * Math.sin(angle);
//    }
//
//    if (link.x1 > link.x2) {
//        dx = 0 - dx;
//        dy = 0 - dy;
//    }
//
//    var x1 = link.x1 + half_icon_size + dx + xoff;
//    var y1 = link.y1 + half_icon_size + dy + yoff;
//
//    var x2 = link.x2 + half_icon_size - dx + xoff;
//    var y2 = link.y2 + half_icon_size - dy + yoff;
//
//    link.line.setShape({
//        x1 : x1,
//        y1 : y1,
//        x2 : x2,
//        y2 : y2
//    });
//
//    var len = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
//    if (len == 0) {
//        return;
//    }
//    var t = (y2 - y1) / (x2 - x1);
//    var angle = Math.atan(t);
//    var x, y;
//
//    var len1 = Math.min(10, len - 2);
//    var len2 = Math.min(3, len / 2 - 1);
//
//    if (x2 < x1) {
//        x = (len1 - len) * Math.cos(angle) + x1;
//        y = (len1 - len) * Math.sin(angle) + y1;
//    } else if (x2 >= x1) {
//        x = (len - len1) * Math.cos(angle) + x1;
//        y = (len - len1) * Math.sin(angle) + y1;
//    }
//
//    var x3 = x + len2 * Math.sin(angle);
//    var y3 = y - len2 * Math.cos(angle);
//
//    var x4 = x - len2 * Math.sin(angle);
//    var y4 = y + len2 * Math.cos(angle);
//
//    var points = [ {
//        x : x2,
//        y : y2
//    }, {
//        x : x3,
//        y : y3
//    }, {
//        x : x4,
//        y : y4
//    } ];
//    link.arrow.setShape(points);
//}

function getCharLenght(str) {
    var realLength = 0, len = str.length, charCode = -1;
    for ( var i = 0; i < len; i++) {
        charCode = str.charCodeAt(i);
        if (charCode >= 0 && charCode <= 128) {
            realLength += 1;
        } else {
            realLength += 2;
        }
    }
    return realLength;
};

//var LastOperatorName = null;
//function setImgs(operatorname) {
//
//    if (LastOperatorName && LastOperatorName != operatorname) {
////        var op = getShapeDataByName(LastOperatorName);
////        set_selected_image(op);
//    	var operatorUid = alpine.flow.OperatorManagementManager.getOperatorUidByName(LastOperatorName);
//    	alpine.flow.OperatorManagementUIHelper.focusOperator(operatorUid);
//        LastOperatorName = operatorname;
//    }
//
//    var currentOperatorUid = alpine.flow.OperatorManagementManager.getOperatorUidByName(operatorname);
//    if(currentOperatorUid){
//        alpine.flow.OperatorManagementUIHelper.toggleOperatorFocus(currentOperatorUid);
//    }
////    var current_op = getShapeDataByName(operatorname);
////    if(current_op){
////        toggle_image(current_op);
////        return true;
////    }
////    return false;
//}

//function toggle_image(op) {
//    // toggle image
//    if (op != null) {
//        if (op.icon_current == op.icon_s) {
//            op.icon_current = op.icon;
//        }
//        else {
//            op.icon_current = op.icon_s;
//        }
//        alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", op.uuid, function(operatorUnit){
//        	operatorUnit.icon.setShape({
//        		src: op.icon_current
//        	});
//    	});
////        op.img.setShape({src: op.icon_current});
//    }
//}

//function set_selected_image(op) {
//
//    if (op != null) {
//        op.icon_current = op.icon_s;
//
//        alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", op.uuid, function(operatorUnit){
//        	operatorUnit.icon.setShape({
//        		src: op.icon_current
//        	});
//    	});
////        op.img.setShape({src: op.icon_current});
//    }
//}

//function set_selected_image_byname(operatorname) {
//    if (LastOperatorName && LastOperatorName != operatorname) {
////        var op = getShapeDataByName(LastOperatorName);
////        set_selected_image(op);
//    	
//    	var operatorUid = alpine.flow.OperatorManagementManager.getOperatorUidByName(LastOperatorName);
//    	alpine.flow.OperatorManagementUIHelper.focusOperator(operatorUid);
//    	
//        LastOperatorName = operatorname;
//    }else{
////        var current_op = getShapeDataByName(operatorname);
////        set_selected_image(current_op);
//    	var operatorUid = alpine.flow.OperatorManagementManager.getOperatorUidByName(operatorname);
//    	if(operatorUid){
//        	alpine.flow.OperatorManagementUIHelper.focusOperator(operatorUid);
//    	}
//    	
//    }
//}

//function set_unselected_image(op) {
//    if (op != null) {
//        op.icon_current = op.icon;
//        alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", op.uuid, function(operatorUnit){
//        	operatorUnit.icon.setShape({
//        		src: op.icon_current
//        	});
//    	});
////        op.img.setShape({src: op.icon_current});
//    }
//}

//function doubleSize(shape) {
//    sharp.applyTransform(dojox.gfx.matrix.scale({x:2, y:2}));
//}

//function rotate2(shape, x, y) {
//    var rotateFx = new dojox.gfx.fx.animateTransform({
//        duration : 1000,
//        shape : shape,
//        transform : [ {
//            name : 'rotategAt',
//            start : [ 0, x, y ],
//            end : [ 360, x, y ]
//        } ]
//    });
//
//    rotateFx.play();
//}

//function update_links(id, dx, dy) {
//    setDirty(true);
//    for ( var i = 0; i < operatorLinkList.length; i++) {
//        var link = operatorLinkList[i];
//        if (link.targetid == id) {
//            link.x2 = link.x2 + dx;
//            link.y2 = link.y2 + dy;
//            drawlink(link, x_off_personal, y_off_personal);
//        }
//        else if (link.sourceid == id) {
//            link.x1 = link.x1 + dx;
//            link.y1 = link.y1 + dy;
//            drawlink(link, x_off_personal, y_off_personal);
//        }
//    }
//    for ( var i = 0; i < operatorList.length; i++) {
//        var op = operatorList[i];
//        if (op.uid == id) {
//            op.x = op.x + dx;
//            op.y = op.y + dy;
//            break;
//        }
//    }
//}

//function getParanetOperators(child) {
//    var parents = new Array();
//    var index = 0;
//    for ( var i = 0; i < operatorLinkList.length; i++) {
//        var link = operatorLinkList[i];
//        if (link.targetid == child.uid) {
//            parents[index] = getOperatorByUid(link.sourceid);
//            index++;
//        }
//    }
//
//    return parents;
//}

//function getOperatorByUid(uid) {
//    for ( var i = 0; i < operatorList.length; i++) {
//        if (operatorList[i].uid == uid) {
//            return operatorList[i];
//        }
//    }
//}

//function hasChildrenOperators(op) {
//
////    for ( var i = 0; i < operatorLinkList.length; i++) {
////        var link = operatorLinkList[i];
////        if (link.sourceid == op.uuid) {
////            return true;
////        }
////    }
////
////    return false;
//    //TODO remove it when able to be update invoker in property.js
//    return alpine.flow.OperatorManagementManager.hasChildrenOperator(op.uuid);
//}
//moved to FlowVersionHistoryUIHelper
/*function show_flow_history (){

    var flow_list = new Array();
    var idx = 0;
    var items = alpine.flow.FlowCategoryUIHelper.getSelectedFlows();//CurrentFlowTree['Personal'].selectedItems;
    if (items||items.length&&items.length>0) {
        var flowInfos = new Array();
        for(var i = 0;i<items.length;i++){
            var item = items[i];
            var flowInfo = tree_item_toFlow_info(item) ;
            flowInfos.push(flowInfo) ;
        }

        //progressBar.showLoadingBar();
        var  url =baseURL+"/main/flow/version.do?method=getFlowVersionInfos"  ;
        ds.post(url, flowInfos, show_flow_history_callback, null,false, "flow_category_tree");


    }


}*/
//moved to FlowVersionHistoryUIHelper
/*function show_flow_history_callback(flowHistoryList){
    progressBar.closeLoadingBar();
    dijit.byId('flowHistoryDialog').show();
    dijit.byId('flowHistoryDialog').resize(600, 300);
    dojo.html.set(dojo.byId("down_a_flow_label"), "");
    var flowHistoryTable = dijit.byId('flowHsitoryTable');
    initFlowHistoryListTable(flowHistoryList,flowHistoryTable);

}*/

 //moved to FlowVersionHistoryUIHelper
/*function initFlowHistoryListTable(flowHistoryList ,flowHistoryTable){

    //fill the flowversion info into the table
    if(!flowHistoryList ||flowHistoryList .length==0){
        popupComponent.alert(alpine.nls.history_not_found);
        return;
    }
    for(var i=0;i<flowHistoryList.length;i++){
        var info = flowHistoryList[i];
        if(info.modifiedTime){
            info.modifiedTime = alpine_format_date(new Date(info.modifiedTime));
        }
    }

    //for sorting ...
    for(var i=0;i<flowHistoryList.length;i++){
        if(flowHistoryList[i].version&&flowHistoryList[i].version!=""){
            flowHistoryList[i].version=flowHistoryList[i].version*1;
        }
    }

    var dataTable = {
        items : flowHistoryList
    };
    // our test data store for this example:
    var flowHistoryStore = new dojo.data.ItemFileWriteStore({
        data : dataTable
    });


    // this will make the edit ok
    flowHistoryTable.setStore(flowHistoryStore);



    if(flowHistoryStore._arrayOfTopLevelItems
        &&flowHistoryStore._arrayOfTopLevelItems.length>0){
        flowHistoryTable.selection.select(0);
        flowHistoryTable.updateRow(0);

    }else{
        flowHistoryTable.selection.deselectAll();

    }
    // Call startup, in order to render the grid:
    flowHistoryTable.render();

    //dojo.connect(flowHistoryTable, "onRowDblClick",  perform_open_flow_history );
} */

//moved to FlowVersionHistoryUIHelper
/*function select_flow_history(){
    var flowHistoryTable = dijit.byId('flowHsitoryTable');
    var items = flowHistoryTable.selection.getSelected();
    //only download one by one...
    if(items&&items.length==1){
        dijit.byId("flow_history_download_id").set("disabled",false);
        dijit.byId("replace_flow_by_history").set("disabled",false);
    }else{
        dijit.byId("flow_history_download_id").set("disabled",true);
        dijit.byId("replace_flow_by_history").set("disabled",true);
    }

}*/

//moved to FlowVersionHistoryUIHelper
/*function perform_download_flow_history(){
    // item is modelInfo
    var flowHistoryTable = dijit.byId('flowHsitoryTable');
    var items = flowHistoryTable.selection.getSelected();
    if(!items||items.length==0){
        popupComponent.alert(alpine.nls.please_select_a_Flow);
    }else{
        var  url =baseURL+"/main/flow/version.do?method=downLoadFlowVersions"  ;
        for(var i=0;i<items.length;i++){

            var info =tree_item_toFlow_info(items[i]) ;
            //make sure the json work...
            info.modifiedTime =0;
            ds.post(url, info, downLoadFlowVersions_callback);

        }

    }

    function downLoadFlowVersions_callback(data){

        var download_url = baseURL + "/temp_flow/"+login+"/" + data;

        //modify by will begin
        var servlet_url = baseURL+"/CommonFileDownLoaderServlet?downloadFileName="+data+"&tempType=temp_flow&filePath=/"+login+"/";
        window.location.href = servlet_url;
        return false;
        //modify by will begin

//        var str = alpine.nls.save_as
//            + "<a href="
//            + download_url
//            + "><u><i><font color='blue'>" + data + "</font></i></u></a>";
//
//        dojo.html.set(dojo.byId("down_a_flow_label"), str);
    }
}*/

//function resize_buttons() {
//    var tool_btn_size = "220px";
//    dojo.style("datasourceConnections_button","width",tool_btn_size);
//    if (login == "admin") {
//        dojo.style("prefrenece_button","width",tool_btn_size);
//        dojo.style("security_button","width",tool_btn_size);
//        dojo.style("groupusers_button","width",tool_btn_size);
//
//        dojo.style("btn_edit_mail_config","width",tool_btn_size);
//        dojo.style("udf_button","width",tool_btn_size);
//        dojo.style("alpine_system_licenseInfo_menu","width",tool_btn_size);
//        dojo.style("alpine_system_sessionMgr_menu", "width", tool_btn_size);
//    }
//
//    dojo.style("reset_passworod_button","width",tool_btn_size);
//    dojo.style("scheduler_button","width",tool_btn_size);
//    dojo.style("user_logs_button","width",tool_btn_size);
//
//    var ation_btn_size = "130px";
//    dojo.style("recentlyHistoryButton","width",ation_btn_size);
//    dojo.style("my_result_button","width",ation_btn_size);
//    dojo.style("export_flow_button","width",ation_btn_size);
//    dojo.style("share_flow_button","width",ation_btn_size);
//    dojo.style("btn_tree_duplicate_flow","width",ation_btn_size);
//    dojo.style("flowVariable_setting_button","width",ation_btn_size);
//    dojo.style("find_replace_parameter_value_btn","width",ation_btn_size);
//
//}

//MOVED TO WelcomeHelper.js
//function check_auth_type() {
//    var btn = dijit.byId("groupusers_button");
//    if (btn) {
//        if(alpine.auth_type == "LocalProvider") {
//            btn.set("disabled", false);
//        }
//        else {
//            btn.set("disabled", true);
//        }
//    }
//    btn = dijit.byId("reset_passworod_button");
//    if (btn) {
//    	// in LDAP & AD authenticator, only admin's information is stored in local. so prevent update except admin.
//        if(alpine.auth_type != "LocalProvider" && alpine.USER != "admin") {
////			btn.set("disabled", true);
//            disable_user_profile_controls(true);
//        }
//        else {
////			btn.set("disabled", false);
//            disable_user_profile_controls(false);
//        }
//    }
//
//}

//MOVED TO UserGroupManagementUIHelper.js
//function disable_user_profile_controls(flag) {
//    dijit.byId("update_user_profile_button").set("disabled", flag);
//    dijit.byId("user_profile_password").set("disabled", flag);
//    dijit.byId("user_profile_password2").set("disabled", flag);
//    dijit.byId("user_profile_email").set("disabled", flag);
//    dijit.byId("user_profile_notify").set("disabled", flag);
//    dijit.byId("user_profile_first").set("disabled", flag);
//    dijit.byId("user_profile_last").set("disabled", flag);
//    dijit.byId("user_profile_desc").set("disabled", flag);
//}

function get_user() {
    return alpine.USER;
}


var CurrentFlowTree = new Array();
function flow_tree_callback(obj, type) {
    flowTreeMenuHandlers[type] =new Array;
    //obj is list of flow info
    if (obj.error_code ) {
        handle_error_result(obj) ;
        return ;
    }

    var store = {
        identifier : "key",
        label : "id",
        items : obj
    };

    var FlowTreeStore = new dojo.data.ItemFileReadStore( {
        data : store
    });

    var treeModel = new dijit.tree.ForestStoreModel( {
        store : FlowTreeStore,
        query : {
            tag : "TOP"
        },
        rootId : "root_Id",
        rootLabel : "",
        childrenAttrs : [ "children" ]
    });

    var treeDomNode =dojo.create("div");
    //make sure it is ok...
    alpine.flow.AddToPersonalFlowListHelper.destroy_flow_tree(type) ;

    var parent = dojo.byId(type);
    parent.appendChild(treeDomNode);
    var treeId = type + "_generated_flow_tree_";



    CurrentFlowTree[type] = new dijit.Tree( {
        id : treeId,
        style : "height: 100%;",
        showRoot : false,
        model : treeModel
    },treeDomNode);
    CurrentFlowTree[type].startup();


    var clickerHandler = dojo.connect(CurrentFlowTree[type], "onClick",   function(item,node,evt){
        //MINERWEB-306 Delete and show history should be disabled when there is no flow selected.
        if(item.type != "Personal" ){
            if(!CurrentFlow_Group){
                //refresh the comments,current no flow opened
                if(item.tag[0]!="TOP"&&item.comments&&item.comments[0]){
                    dojo.byId("add_flow_comments").value=item.comments[0];
                }else{
                    dojo.byId("add_flow_comments").value=" ";
                }
                if(item.tag[0]!="TOP"&&item.version&&item.version[0]){
                    dojo.byId("add_flow_version").innerHTML=item.version[0];
                }else{
                    dojo.byId("add_flow_version").innerHTML="";
                }


                if(item.tag[0]!="TOP"&&item.modifiedUser&&item.modifiedUser[0]){
                    dojo.byId("add_flow_publisher").innerHTML=item.modifiedUser[0];
                }else{
                    dojo.byId("add_flow_publisher").innerHTML="";
                }

                if(item.tag[0]!="TOP"&&item.modifiedTime&&item.modifiedTime[0]){
                    var dateTime = new Date(item.modifiedTime[0]) ;//from time mills
//					dateTime=dateTime.toLocaleString();
                    dateTime=alpine_format_date(dateTime);
                    dojo.byId("add_flow_publish_time").innerHTML=dateTime;
                }else{
                    dojo.byId("add_flow_publish_time").innerHTML="";
                }

            }
        }

//		if(item) {
//			dijit.byId("btn_tree_delete_flow").set("disabled",false);
//			dijit.byId("btn_tree_history_flow").set("disabled",false);
//		 
//		}
    });



    flowTreeMenuHandlers[type].push(clickerHandler) ;

    var dbClickerHandler = dojo.connect(CurrentFlowTree[type], "onDblClick",  function(item) {
        if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
            return;
        }
        var flow = tree_item_toFlow_info(item);
//        if(item.type == "Personal"){
//            if(alpine.flow.alpine.flow.WorkFlowManager.isEditing(item)){
////				dijit.byId("open_flow_button").closeDropDown();
//                return;
//            }
//        }
        open_flow(type, flow);
    });

    flowTreeMenuHandlers[type].push(dbClickerHandler) ;


    if(type == "Group") {
        flowTreeMenu = new dijit.Menu({
            targetNodeIds : [ treeId ]
        });
        if(alpine.system.PermissionUtil.checkPermission("DELETE_FLOW_FROM_PUBLIC")){
            flowTreeMenu.addChild(new dijit.MenuItem({
                label : alpine.nls.delete_flow_tip,
                onClick : function() {
                    delete_flow_list("Group");
                }
            }));
        }
        flowTreeMenu.addChild(new dijit.MenuItem({
            label : alpine.nls.open_flow_tip,
            onClick : function(evt) {
                var items=CurrentFlowTree[type].selectedItems;

                if(items&&items.length>0){
                    var item = items[0];
                    if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
                        return;
                    }
                    var flow = tree_item_toFlow_info(item);

                    open_flow("Group", flow);
                }
            }
        }));

        flowTreeMenu.addChild(new dijit.MenuItem({
            label : alpine.nls.open_flow_history_tip,
            onClick : function(evt) {
                var items=CurrentFlowTree[type].selectedItems;

                if(items&&items.length>0){
                    var item = items[0];
                    if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
                        return;
                    }
                    var flow = tree_item_toFlow_info(item);
                    alpine.flow.FlowVersionHistoryUIHelper.open_flow_history_dlg_foradd(flow);
                }
            }
        }));




        var flowTreeMenuHandler= dojo.connect(flowTreeMenu, "_openMyself", CurrentFlowTree[type], function(e) {

            var tn = dijit.getEnclosingWidget(e.target);

            //diable and enable menu
            if(!tn||!tn.item||tn.item.children){//fix bug with IE, which bug name is MINERWEB-330 in JIRA
                //if can enter here, must be click right click with any node
                if(alpine.system.PermissionUtil.checkPermission("DELETE_FLOW_FROM_PUBLIC")){
                    flowTreeMenu.getChildren()[0].set('disabled', true);
                    flowTreeMenu.getChildren()[1].set('disabled', true);
                    flowTreeMenu.getChildren()[2].set('disabled', true);

                }else{
                    flowTreeMenu.getChildren()[0].set('disabled', true);
                    flowTreeMenu.getChildren()[1].set('disabled', true);
                }
                return;
            }

            //make select
            var items= this.get('selectedItems');
            if(!items|| items.length==0){
                this.set('selectedItem', tn.item);
            }else{
                if(dojo.indexOf(items, tn.item) == -1){
                    this.set('selectedItem', tn.item);
                }
            }
            items= this.get('selectedItems');

            if(alpine.system.PermissionUtil.checkPermission("DELETE_FLOW_FROM_PUBLIC")){
                flowTreeMenu.getChildren()[0].set('disabled', false);
                if(items.length>1){
                    flowTreeMenu.getChildren()[1].set('disabled', true);
                    flowTreeMenu.getChildren()[2].set('disabled', true);
                }else{
                    flowTreeMenu.getChildren()[1].set('disabled', false);
                    flowTreeMenu.getChildren()[2].set('disabled', false);
                }
            }
            else{
                if(items.length>1){
                    flowTreeMenu.getChildren()[1].set('disabled', true);
                    flowTreeMenu.getChildren()[0].set('disabled', true);
                }else{
                    flowTreeMenu.getChildren()[1].set('disabled', false);
                    flowTreeMenu.getChildren()[0].set('disabled', false);
                }
            }


        });
        flowTreeMenuHandlers[type].push(flowTreeMenuHandler) ;
        flowTreeMenu.startup();
    }


    //this is for file upload to get the flow name
    if (type=="Personal"){
    }
    else {
        CurrentFlow_Group=null;

        //init the dialog...
        dijit.byId("PublicGroupFlowDialog").show();
        dojo.byId("add_flow_comments").value="";

        dojo.byId("add_flow_version").innerHTML="";
        dojo.byId("add_flow_publisher").innerHTML="";
        dojo.byId("add_flow_publish_time").innerHTML="";

        if (dojo.isIE){
            dojo.byId("add_flow_comments").rows=2;
            dojo.byId("add_flow_comments").cols=40;
        }

    }
}

// delete flow list
function delete_flow_list(type) {
    var items = type == "Personal" ? alpine.flow.FlowCategoryUIHelper.getSelectedFlows() : CurrentFlowTree[type].selectedItems;
    popupComponent.confirm(alpine.nls.delete_flow_confirm_tip, {
        handle: function(){
            var flow_list = new Array();
            var idx = 0;

            if (items.length) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item !== null && item.id[0] != item.key[0]) {
                        flow_list[idx++] = tree_item_toFlow_info(item);
                        //remove flow opened record from Container of history
                        alpine.flow.RecentlyHistoryFlowManager.removeFlowFromHistory(item);
                    }
                }
            }
            if (flow_list.length) {
                var callback = null;
                if (type == "Personal") {
                    callback = alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree;
                }
                else {
                    callback = flow_tree_group_cb;
                }
                var url = flowBaseURL + "?method=deleteFlowList"
                    + "&user=" + login
                    + "&type=" + type;
                error_msg = alpine.nls.delete_flow_error;
                ds.post(url, flow_list, callback, error_callback);
                if (alpine.flow.WorkFlowManager.isEditing()) {
                    for (var j = 0; j < flow_list.length; j++) {
                        if (alpine.flow.WorkFlowManager.isEditing(flow_list[j])) {
                        	alpine.flow.WorkFlowUIHelper.release();
                        	alpine.flow.FlowCategoryUIHelper.removeWorkFlowEditingTrail();
                            break;
                        }
                    }
                }
            }
        }
    });
}

// copy flow list
//var copy_and_open_item = null;


//function copy_flow_list(type) {
//    if (type != 0) {//add and open
//        if (alpine.flow.WorkFlowManager.isDirty()){
//            popupComponent.saveConfirm(alpine.nls.update_not_saved,{
//                handle: function(){
//                    save_flow_addition_callback = function(){
//                        copyFlowListHandle();
//                    };
//                    //save_flow();
//                    alpine.flow.WorkFlowUIHelper.saveWorkFlow();
//                }
//            },{
//                handle: function(){
//                    copyFlowListHandle();
//                }
//            });
//        }else{
//            copyFlowListHandle();
//
//        }
//    }else{
//        copyFlowListHandle();
//    }
//    function copyFlowListHandle(){
//        var copy_flow_list = new Array();
//        var idx = 0;
//        var items = CurrentFlowTree['Group'].selectedItems;
//        if (items.length) {
//            for (var i = 0; i < items.length; i++) {
//                var item = items[i];
//                if (item !== null && item.id[0] != item.key[0]) {
//
//                    copy_flow_list[idx] = tree_item_toFlow_info(item);
//                    //get the opened version
//                    //added check whether CurrentFlow_Group is null or not, to fix JIRA MINERWEB-568
//                    if(CurrentFlow_Group && copy_flow_list[idx].key==CurrentFlow_Group.key){
//                        copy_flow_list[idx].version = CurrentAddingFlow_Version;
//                    }
//                    copy_flow_list[idx].comments =dojo.byId("add_flow_comments").value;
//                    if(!copy_flow_list[idx].comments||copy_flow_list[idx].comments==""){
//                        copy_flow_list[idx].comments= " ";
//                    }
//                    idx=idx+1;
//
//                }
//            }
//        }
//
//        if (copy_flow_list.length > 0) {
//            var canContinue = true;
//            var url = flowBaseURL + "?method=copyFlowList"
//                + "&user=" + login
//                + "&type=" + type;
//            error_msg = alpine.nls.copy_flow_error;
//
//            for(var i =0 ;i<copy_flow_list.length;i++){
//                if (alpine.flow.WorkFlowManager.isEditing(copy_flow_list[i])){
//                    if(alpine.flow.WorkFlowManager.isDirty()){
//                        canContinue = false;
//                        popupComponent.saveConfirm(alpine.nls.update_not_saved,{
//                            handle: function(){
//                                save_flow_addition_callback = function(){
////                                    clear_flow_display("Personal");
//                                	alpine.flow.WorkFlowUIHelper.release();
//                                    //copied flow is current flow with edit, then replace latest version to screen.
//                                    ds.post(url, copy_flow_list, copy_flow_list_callback, error_callback);
//                                    copy_and_open_item = null;
//                                    var flow = copy_flow_list[0];
//                                    flow.type = "Personal";
//                                    flow.modifiedUser = login;
//                                    copy_and_open_item = flow;
////                                    CurrentFlow = null;
//                                };
//                                //save_flow();
//                                alpine.flow.WorkFlowUIHelper.saveWorkFlow();
//                            }
//                        },{
//                            handle: function(){
//                                //if abort current flow with edit, just clean screen.
////                                clear_flow_display("Personal");
//                            	alpine.flow.WorkFlowUIHelper.release();
//                                ds.post(url, copy_flow_list, copy_flow_list_callback, error_callback);
//                            }
//                        });
//                    }else{
////                        clear_flow_display("Personal");
//                    	alpine.flow.WorkFlowUIHelper.release();
//                    }
//                }
//            }
//            if(canContinue){
//                ds.post(url, copy_flow_list, copy_flow_list_callback, error_callback);
//                copy_and_open_item = null;
//                if (type != 0) {
//                    var flow = copy_flow_list[copy_flow_list.length -1];
//                    flow.type = "Personal";
//                    flow.modifiedUser = login;
//                    copy_and_open_item = flow;
////                    CurrentFlow = null;
//                    alpine.flow.WorkFlowUIHelper.release();
//                }
//            }
//        }
//    }
//}
//
//function copy_flow_list_callback(obj) {
//    if (obj.error_code&&obj.error_code!=0) {
//        if (obj.error_code == -1) {
//            popupComponent.alert(alpine.nls.no_login, function(){
//                window.top.location.pathname = loginURL;
//            });
//            return;
//        }
//        else if (obj.error_code == 3) {
//            popupComponent.alert(alpine.nls.flow_exist_error);
//            return;
//        }
//        else {
//            var msg = alpine.nls.flow_not_found;
//            if (obj.message) {
//                msg = obj.message;
//            }
//            popupComponent.alert(msg);
//            return;
//        }
//    }
//    clear_flow_display("Group");
//    destroy_flow_tree('Group');
//    dijit.byId('PublicGroupFlowDialog').hide();
//
//
//    if (copy_and_open_item != null) {
//        if(obj.newFlowVersion){
//            copy_and_open_item.version = obj.newFlowVersion;
//        }
//        //open the crroct version ...
////        clear_flow_display("Personal");
////        open_flow("Personal", copy_and_open_item);
//        alpine.flow.WorkFlowUIHelper.release();
//        alpine.flow.WorkFlowUIHelper.openWorkFlow(copy_and_open_item);
//        
//    }
//    window.setTimeout(alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree, 2000);
//}

//function setDirty(flag) {
//    dirtyFlag = flag;
//
//    dijit.byId("save_flow_button").set("disabled", !flag);
//    if(CurrentFlow){
//        var str = dojo.byId("current_flow_label").innerHTML;
//        if(str.lastIndexOf("*") == str.length - 1){
//            str = str.substr(0, str.length - 2);
//        }
//        if(flag==true){
//            dojo.html.set(dojo.byId("current_flow_label"), str + " *");
//        }else{
//            dojo.html.set(dojo.byId("current_flow_label"), str);
//        }
//    }
//}

//var dirtyFlag = false;

//var isClearDisplay =false;
//function save_flow( ) {
//    if(alpine.flow.WorkFlowManager.getEditingFlow().comments==""||!alpine.flow.WorkFlowManager.getEditingFlow().comments){
//        CurrentFlow.comments = " " ;
//    }
//
//    dojo.byId("save_flow_comments").value=CurrentFlow.comments;
//
//    dijit.byId("save_flow_dlg").show();
//
//    if (dojo.isIE){
//        dojo.byId("save_flow_comments").rows=4;
//        dojo.byId("save_flow_comments").cols=38;
//    }
//}
//function save_flow_with_comments(){
//
//    var url = flowBaseURL + "?method=completeUpdate" + "&user=" + login;
//    error_msg = "";
//    var data = {};
//
//    CurrentFlow.comments= dojo.byId("save_flow_comments").value;
//    if(!CurrentFlow.comments||CurrentFlow.comments == ""){
//        CurrentFlow.comments=" " ;
//    }
//    data.flowInfo = CurrentFlow;
//    data.result = new Array();
//    var list = CurrentFlowDTO.result;
//    for (var i = 0; i < list.length; i++) {
//        var item = {};
//        item.name = list[i].name;
//        item.x = list[i].x;
//        item.y = list[i].y;
//        data.result[i] = item;
//    }
//    //progressBar.showLoadingBar();
//    ds.post(url, data, save_flow_callback,  error_callback);
//    CurrentFlow.tmpPath = "";
//    setDirty(false);
//    dijit.byId("save_flow_dlg").hide();
//}

//function save_flow_callback(obj) {
//    progressBar.closeLoadingBar();
//    CurrentFlow = obj;
////    var str = alpine.flow.FlowCategoryUIHelper.buildFlowPath(CurrentFlow);
////    save_current_flow(CurrentFlow);
//    alpine.flow.WorkFlowManager.storeEditingFlow(obj);
//    alpine.flow.WorkFlowUIHelper.setEditingFlowLabel(alpine.flow.FlowCategoryUIHelper.buildDisplayFlowPath(CurrentFlow));
//    if(isClearDisplay==true){
//        clear_flow_display("Personal") ;
//        isClearDisplay=false;
//    }
//    //update personal flow tree, in order to ensure other function make sense. e.g. show flow history, open flow. Because they dependence on information of flow in the tree.
//    alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree();
//
//    if(save_flow_addition_callback){
//        //call
//        save_flow_addition_callback();
//        save_flow_addition_callback= null;
//    }
//}

//function cancel_flow() {
//    if (CurrentFlow == null) {
//        return;
//    }
//    if(alpine.flow.WorkFlowManager.isDirty()){
//        popupComponent.saveConfirm(alpine.nls.update_not_saved, {
//            handle: function(){
//                isClearDisplay=true;
//                save_flow_addition_callback = function(){
//                    clear_flow_display("Personal");
//                    dijit.byId("cancel_flow_button").set("disabled", true);
//                    setDirty(false);
////                    current_flow_id = null;
//                    alpine.flow.WorkFlowManager.storeEditingFlow(null);
//                };
//                save_flow();
//            }
//        },{
//            handle: function(){
//                cancelFlowHandle();
//            }
//        });
//    }else{
//        cancelFlowHandle();
//    }
//}

//function cancelFlowHandle(){
//    var url = flowBaseURL + "?method=cancelUpdate" + "&user=" + login;
//    error_msg = "";
//    ds.post(url, CurrentFlow, copy_flow_list_callback, error_callback);
//    alpine.flow.WorkFlowUIHelper.release();
//    dijit.byId("cancel_flow_button").set("disabled", true);
//    setDirty(false);
////    current_flow_id = null;
//    alpine.flow.WorkFlowManager.storeEditingFlow(null);
//    alpine.flow.WorkFlowUIHelper.setEditingFlowLabel("");
//    dojo.publish("/operatorExplorer/switchWorkflowEditor", [false]);
//}

function user_logout() {
    var url = baseURL + "/main/admin.do?method=logout";
    var user = {login : alpine.USER};

    if (alpine.flow.WorkFlowManager.isEditing() && alpine.flow.WorkFlowManager.isDirty()) {
        popupComponent.saveConfirm(alpine.nls.update_not_saved, {
            handle: function(){
                var saveFlowCallback = function(){
                    ds.post(url, user, user_logout_callback, null);
                };
                //save_flow();
                alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
            }
        }, {
            handle: function(){
            	alpine.flow.WorkFlowManager.cancelEditingFlow(function(){
            		ds.post(url, user, user_logout_callback, null);
            	});
            }
        });
    }else{
        ds.post(url, user, user_logout_callback, null);

    }
}

function user_logout_callback() {
    window.top.location.pathname = logoutURL;
}

function tree_item_toFlow_info(item) {
    var info = {};
    info.version=item.version[0];
    if(item.comments){
        info.comments=item.comments[0];
    }
    info.id = item.id[0];
    info.key = item.key[0];
    info.createUser = item.createUser[0];
    info.modifiedUser = item.modifiedUser[0];
    if(item.groupName) {
        info.groupName = item.groupName[0];
    }
    info.createTime = item.createTime[0];
    info.modifiedTime = item.modifiedTime[0];
    info.type = item.type[0];
    info.tmpPath = "";
    if(item.categories){
        info.categories = item.categories;
    }
    if(item.tmpPath) {
        info.tmpPath = item.tmpPath[0];
    }
    return info;
}

var ShareNameList = null;
function generate_select_list(nameList) {
    if (nameList.error_code ) {
        handle_error_result(nameList);
        return;
    }
    var list = new Array();
    list[0] = "Public";
    for (var i = 0; i < nameList.length; i++) {
        list[i+1] = nameList[i];
    }

    ShareNameList = list;
    var id = "share_flow_radio_button_table";
    var parent = dijit.byId(id);
    dojo.forEach(parent.getOptions(),function(option,i){
        parent.removeOption(option);
    });

    parent.removeOption(list);
    for(var idx = 0;idx < list.length;idx++){
        parent.addOption({
            label: list[idx],
            value: list[idx]
        });
    }
//	var tmpprop = {};
//	tmpprop.name = id;
//	tmpprop.value = "Public";
//	tmpprop.displayName = id;
//	tmpprop.fullSelection = list;
//	generate_input_choice(parent, tmpprop);
}

//function enable_flow_buttons(flag) {
//    dijit.byId("export_flow_button").set("disabled", !flag);
//    dijit.byId("share_flow_button").set("disabled", !flag);
//    dijit.byId("btn_tree_duplicate_flow").set("disabled", !flag);
//    dijit.byId("flowVariable_setting_button").set("disabled", !flag);
//    //dijit.byId("find_replace_parameter_value_btn").set("disabled", !flag);
//
//    if (flag == true) {
//        enable_run_flow();
//    }
//    else {
//        disable_run_flow();
//        dijit.byId("stop_flow").set("disabled", true);
//        setDirty(false);
//    }
//}
//var save_flow_addition_callback = null;
function open_flow(type, item, fn) {
//	dijit.byId("open_flow_button").closeDropDown();
//    if (type == "Personal") {
//        if (alpine.flow.WorkFlowManager.isEditing(item)) {
//            return;
//        }
//        alpine.flow.RecentlyHistoryFlowManager.pushFlow2History(item);
////		if (cancel_flow() == false){
////			return;									
////		}
//        if(alpine.flow.WorkFlowManager.isDirty()){
//            popupComponent.saveConfirm(alpine.nls.update_not_saved, {
//                handle: function(){
//                    isClearDisplay=true;
//                    save_flow_addition_callback=openFlowHandle;
//                    save_flow();
//                }
//            },{
//                handle: function(){
//                    cancelFlowHandle();
//                    openFlowHandle();
//                }
//            });
//        }else{
//            openFlowHandle();
//        }
//    }else{
        openFlowHandle();
//    }

    function openFlowHandle(){
        clear_flow_display(type);
       // progressBar.showLoadingBar();

//        uuid = null;
        var url = flowBaseURL + "?method=getFlowData" + "&user=" + login;
        var callback = null;
//        if (type == "Personal") {
//
//            callback = open_flow_edit_callback;
//        }
//        else {
            callback = open_flow_preview_callback;
//        }
        if(fn){
            fn.call(null);
        }
        ds.post(url, item, callback, null, false, "PublicGroupFlowContainer");

    }
}

//function open_flow_edit_callback(obj) {
//    var res = open_flow_callback("Personal", obj);
//    //true or false...
//    enable_flow_buttons(res);
//}

function open_flow_preview_callback(obj) {
    open_flow_callback("Group", obj);
}

//function build_personal_flow_tree() {
//    //MINERWEB-306 Delete and show history should be disabled when there is no flow selected.
//// 	dijit.byId("btn_tree_delete_flow").set("disabled",true);
////	dijit.byId("btn_tree_history_flow").set("disabled",true);
//    load_flow_tree("Personal");
//
//}

function open_public_group_flow() {
    // dijit.byId("open_flow_button").closeDropDown();
    load_flow_tree("Group");
}

function clear_flow_display(type) {
    var disp = dojo.byId("FlowDisplayPanel" + type);
//    if(current_flow_surface[type]){
//        current_flow_surface[type].clear();
//        current_flow_surface[type].destroy();
//        current_flow_surface[type]=null;
//    }
    alpine.flow.WorkFlowPainter.release("FlowDisplayPanel" + type);
//    alpine.flow.OperatorManagementUIHelper.release();


    while (disp.firstChild) {
        disp.removeChild(disp.firstChild);
    }

    disp.innerHTML="";
//    if (type == "Personal") {
//
//        //flow popupmenue
//        if(current_flow_menus&&type){
//            for(var i=0;i<current_flow_menus.length;i++){
//                if(current_flow_menus[i]){
//                    current_flow_menus[i].destroyRecursive();
//                    current_flow_menus[i]=null;
//                }
//            }
//            current_flow_menus=new Array();
//        }
//
////        if(current_flow_handlers){
////            for(var i=0;i<current_flow_handlers.length;i++){
////                if(current_flow_handlers[i]){
////                    dojo.disconnect(current_flow_handlers[i]);
////                    current_flow_handlers[i]=null;
////                }
////            }
////            current_flow_handlers=new Array();
////        }
//
////        if(ShapeList){
////            for(var i=0;i<ShapeList.length;i++){
////                if(ShapeList[i]){
////                    ShapeList[i]=null;
////                }
////            }
////
////            ShapeList=null;
////        }
//
//        enable_flow_buttons(false);
//        CurrentFlow = null;
//        CurrentOperator_uuid = null;
//
//        //added by zy...
//        setDirty(false);
////        current_flow_id = null;
//        alpine.flow.WorkFlowManager.storeEditingFlow(null);
//        alpine.flow.WorkFlowUIHelper.setEditingFlowLabel("");
//    }
}


//function destroy_flow_tree( type) {
//    var treeId = type + "_generated_flow_tree_";
//    var t = dijit.byId(treeId);
//    if (t) {
//        dijit.registry.remove(treeId);
//        t.destroyRecursive();
//
//        if(type=="Group"){
//            flowTreeMenu= null;
//        }
//
//        if(flowTreeMenuHandlers[type]){
//            for(var i=0;i<flowTreeMenuHandlers[type].length;i++){
//                if(flowTreeMenuHandlers[type][i]){
//                    dojo.disconnect(flowTreeMenuHandlers[type][i]);
//                    flowTreeMenuHandlers[type][i]=null;
//                }
//            }
//            flowTreeMenuHandlers[type]=new Array();
//        }
//
//
//    }
//
//    var parent = dojo.byId(type);
//    if (parent && parent.firstChild){
//        parent.removeChild(parent.firstChild);
//        parent.innerHTML="";
//    }
//    CurrentFlowTree[type] = null;
//}

//function save_current_flow(currentFlowObj){
//    var cfID=alpine.flow.FlowCategoryUIHelper.buildFlowPath(CurrentFlow);
//    var fwID=alpine.flow.FlowCategoryUIHelper.buildDefaultWorkFlowPath(CurrentFlow);
//    current_flow_id = cfID;
//    alpine.flow.FlowCategoryUIHelper.storeFlowPath(fwID,login);
//}

//function is_flow_loaded(flowId){
//    return current_flow_id == flowId;
//}
/*function open_flow_history_dlg_foradd (){
    var flow_list = new Array();
    var idx = 0;
    var items = CurrentFlowTree['Group'].selectedItems;
    if (items||items.length&&items.length>0) {
        var flowInfos = new Array();
        //items.length always == 0
        for(var i = 0;i<items.length;i++){
            var item = items[i];
            var flowInfo = tree_item_toFlow_info(item) ;
            flowInfos.push(flowInfo) ;
            if(CurrentFlow_Group&&CurrentFlow_Group.key == item.key[0]){
                //just the current opened item's version
            }else{
                CurrentAddingFlow_Version = flowInfo.version;
            }
        }
        console.log("THINK THIS IS NOT BEING USED!!!!!!!!!!!!!!!!!!!");
        //progressBar.showLoadingBar();
        var  url =baseURL+"/main/flow/version.do?method=getFlowVersionInfos"  ;
        ds.post(url, flowInfos, show_flow_history_foradd_callback, null, false, "flow_category_tree");

    }

}*/
//Moved to FLowVersionHistoryUIHelper
/*function open_flow_history_dlg_foradd_for_addtopersonalflow(flowInfos){
    //progressBar.showLoadingBar();
    var  url =baseURL+"/main/flow/version.do?method=getFlowVersionInfos"  ;
    ds.post(url, flowInfos, show_flow_history_foradd_callback4_addtopersonal, null, false, "GroupContainer");
}*/

//Moved to FLowVersionHistoryUIHelper
/*function select_flow_history_add(){
    var flowHistoryTable = dijit.byId('flowHsitoryTable_Add');
    var items = flowHistoryTable.selection.getSelected();
    //only download one by one...
    if(items&&items.length==1){
        dijit.byId("flow_history_open_id").set("disabled",false);
        //dijit.byId("flow_history_open_menu").set("disabled",false);
    }else{
        dijit.byId("flow_history_open_id").set("disabled",true);
        //dijit.byId("flow_history_open_menu").set("disabled",true);
    }

}*/

//Moved to FLowVersionHistoryUIHelper
/*function perform_flow_history_open_when_add(){

    dijit.byId('flowHistoryDialog_Add').hide();
    var flowHistoryTable = dijit.byId('flowHsitoryTable_Add');
    var items=flowHistoryTable.selection.getSelected();
    if(items &&items.length>0){
        var item = items[0];
        var flow = tree_item_toFlow_info(item);
        flow.modifiedTime =0;
        open_flow("Group", flow);

    }

}*/
//Moved to FLowVersionHistoryUIHelper
/*function show_flow_history_foradd_callback4_addtopersonal(flowHistoryList){

    dijit.byId('flowHistoryDialog_Add').show();
    dijit.byId('flowHistoryDialog_Add').resize(600, 300);

    var flowHistoryTable = dijit.byId('flowHsitoryTable_Add');
    initFlowHistoryListTable(flowHistoryList,flowHistoryTable);

    //init the selectio1 ;

    var items = flowHistoryTable.store._arrayOfTopLevelItems;
    flowHistoryTable.selection.deselectAll();
    if(items &&items.length>0){
        flowHistoryTable.selection.select(items.length-1);
    }
    progressBar.closeLoadingBar();

}*/
//Moved to FLowVersionHistoryUIHelper
/*function show_flow_history_foradd_callback(flowHistoryList){

    dijit.byId('flowHistoryDialog_Add').show();
    dijit.byId('flowHistoryDialog_Add').resize(600, 300);

    var flowHistoryTable = dijit.byId('flowHsitoryTable_Add');
    initFlowHistoryListTable(flowHistoryList,flowHistoryTable);

    //init the selectio...


    var version = CurrentAddingFlow_Version*1 ;

    var items = flowHistoryTable.store._arrayOfTopLevelItems;
    flowHistoryTable.selection.deselectAll();
    if(items &&items.length>0){
        for(var i =0 ;i<items.length;i++){
            var item = items[i];
            if(item.version[0]==version){
                flowHistoryTable.setStore(flowHistoryTable.store);
                flowHistoryTable.updateRow(i);
                flowHistoryTable.selection.select(i);
                break;
            }
        }
    }
    progressBar.closeLoadingBar();

}*/


//Moved to FlowVersionHistoryUIHelper
/*function replace_current_flow_by_history(){
    dijit.byId('flowHistoryDialog').hide();

    var flowHistoryTable = dijit.byId('flowHsitoryTable');
    var items=flowHistoryTable.selection.getSelected();
    if(items &&items.length>0){
        var item = items[0];
        var flow = tree_item_toFlow_info(item);


        flow.modifiedTime =0;


        var  url =baseURL+"/main/flow/version.do?method=replaceWithVersion"  ;
        ds.post(url, flow, make_newversion_callback);

    }




    //1 check the open status, if open, close it
    //2 call the service to make a history and use tge old content as the current version
    //3 re_show the tree, if opened, reopen it ...
}*/

//Moved to FlowVersionHistoryUIHelper
/*
function make_newversion_callback(result){
    //progressBar.closeLoadingBar();
    //result is the new flowInfo
    if(result&&result.version){
        var make_version_reopen=false;
        if(alpine.flow.WorkFlowManager.isEditing(result)){
			if (alpine.flow.WorkFlowManager.isDirty()) {
				popupComponent.confirm(alpine.nls.update_not_saved,{
					handle: function(){
						var saveFlowCallback = function(){
							alpine.flow.WorkFlowUIHelper.release();
							alpine.flow.WorkFlowUIHelper.openWorkFlow(result);
						};
						//save_flow();
                        alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
					}
				},{
					handle: function(){
						alpine.flow.WorkFlowUIHelper.release();
						alpine.flow.WorkFlowUIHelper.openWorkFlow(result);
					}
				});
			}else{
				alpine.flow.WorkFlowUIHelper.release();
				alpine.flow.WorkFlowUIHelper.openWorkFlow(result);
//	            open_flow("Personal", result);
			}
        }
        window.setTimeout(alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree, 2000);
    }else{
        popupComponent.alert(alpine.nls.error+":"+result.message);	}
}*/
