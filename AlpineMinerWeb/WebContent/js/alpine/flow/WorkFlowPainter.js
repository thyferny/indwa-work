/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowPainter.js 
 * Author Gary
 * Jul 3, 2012
 */
define([
    "dojox/gfx",
    "dojox/gfx/fx",
    "dojox/gfx/Mover",
    "alpine/operatorexplorer/OperatorUtil",
    "alpine/flow/OperatorManagementManager",
    "alpine/flow/OperatorLinkManager",
    "dojo/fx"
], function(gfx, gfxFx, Mover, operatorUtil, operatorHandler, linkHandler, fx){
	
	var constants = {
		//ICON_ROOT: "../../images/icons/",
		EAR_ICON: {
//			IMAGE: "../../images/interface/ears.png", 
//			WIDTH: 55,
			FILL: "#A1A1A1",
			STROKE: "#555555",
            HOVER: "#00A1E5",
			WIDTH: 20,
			HEIGHT: 12
		},
		GAP: 10,//gap from shape to text
		DEF_FONT: {
			family: "opensans",
			size: "8pt", 
			weight: "normal",
			color: "#000000"
		},
		DEF_LINE: {
			color: "#d7d7d7",
			width: 2
		},
		SELECTED_LINE: {
			color: "#00AEEF",
			width: 2
		},
        DRAG_LINE: {
            color: "#000000",
            width: 2
        },
		CANVAS_OFFSETTOP: 110,
		LINK_ADJUST: 32,
		ACTIVITY_TARGET: "activityTargetPoint"
	};

	var surfaceCache = [];
	
	//below variable need to clean.
	
	/**
	 * key = paneId
	 * value = {
	 * 		operatorUnitMap: {
	 * 			key: op.uid,
	 * 			value: operatorUnit{
	 * 				icon: Shape
	 * 				label: Shape 
	 * 				ears: {
	 * 					l: left ear,
	 * 					r: right ear
	 * 				}
	 * 			}
	 * 		},
	 * 		operatorLinkSet: {
	 * 			linkUnit{
	 * 				link: Object,
	 * 				line: Shape,
	 * 				arrow: Shape,
	 * 				selectArea: Shape,
	 * 				controlPoints: [controlPoint, sourcePoint, targetPoint](Shape)
	 * 			}
	 * 		},
	 * 		width: width of canvas
	 * 		height: height of canvas
	 * 		eventHandler: [],
	 * 		halfIconSize: integer,
	 * 		onMove: function
	 * }
	 */
	var paneDrawInfoMap = [];
	
	var isDraggingArrow = false;
	
	var draggingTargetOperatorUid = null;// this will be fill id when mouse move in target operator, and will be reset when mouse move out of target operator
	
	var connectingEvents = [];
	var linkControlEvents = [];
	
	var tooltipRefShape = null;
	
	dojo.declare("AlpineIlluminator.Mover", Mover, {
	    onMouseMove: function(event) {
	    	if(isDraggingArrow == true){
	    		return;
	    	}
	        var x = event.clientX;
	        var y = event.clientY;
	        var navigationWidth = dijit.byId("personalFlowTree").domNode.offsetWidth;
			var limitSize = {
				xMin: navigationWidth + 50,
				xMax: dojo.byId(this.host.paneId).offsetWidth + navigationWidth - 50,
				yMin: constants.CANVAS_OFFSETTOP,
				yMax: dojo.byId(this.host.paneId).offsetHeight
			};
	        
	        if (x > limitSize.xMin
	            & y > limitSize.yMin
	            & x < limitSize.xMax
	            & y < limitSize.yMax) {
	            //Modify By Will
//	            var pftOffsetWidth = dijit.byId("personalFlowTree").domNode.offsetWidth;
//	            if(this.shape && this.shape.children){
//	                var textNode = this.shape.children[1];
//	                var imgNode = this.shape.children[0];
//	                
//	                if(dojo.isIE){
//	                    var textNodeWidth = textNode.rawNode.offsetWidth!=null?textNode.rawNode.offsetWidth:80;
//	                    if(textNode!=null && (x-textNodeWidth)<=pftOffsetWidth){
//	                        x=pftOffsetWidth+textNodeWidth;
//	                    }
//	                }else{
//	                    //can not acquire the svg textNode clientWidth
//	                    //console.log(textNode.rawNode.length);
//	                    if(textNode!=null && (x-80)<=pftOffsetWidth){
//	                        x=pftOffsetWidth+80;
//	                    }
//	                }
//	            }
	            
	            var latestX = x - this.lastX,
	            	latestY = y - this.lastY;
	            
	            // move the rectangle by applying a translation
	            this.shape.applyLeftTransform({
	                dx: latestX,
	                dy: latestY
	            });
	            
//	            update_links(this.host.uid, x - this.lastX, y - this.lastY);
	            _moveLinkFollowOperator({
	            	paneId: this.host.paneId,
	            	operatorUid: this.host.uid,
	            	offsetPoint: {
	            		x: latestX,
	            		y: latestY
	            	}
	            });
	            this.lastX = x;
	            this.lastY = y;
	            
	            paneDrawInfoMap[this.host.paneId].onMove.call(null, {
	            	operatorUid: this.host.uid,
	            	x: latestX,
	            	y: latestY
	            });
				_displayOperatorTooltip(this.host.paneId, this.host.uid, false);
	        }
	        dojo.stopEvent(event);
	    }
	});
	
	/**
	 * draw a operator icon and text to canvas.
	 * below are details for arguments
	 * 
	 * canvasInfo -- Object
	 *  	paneId -- canvas id.
	 *  	width -- width of canvas.
	 *  	height -- height of canvas.
	 * op -- Object. operator paint information
	 * 		uid
	 * 		x
	 * 		y
	 * 		icon
	 * 		width
	 * 		height
	 * 		label
	 * font -- optional
	 * 		family
	 * 		size
	 * 		weight
	 * 		color
	 * 
	 * bindEvent -- boolean 	will be bind below events if true
	 * onClick(op)
	 * onDblClick(op)
	 * onMouseOver(op)
	 * onMouseOut(op)
	 * onDblClickLabel(op)
	 * 
	 * ableMove -- boolean 
	 * onMove({
	 * 		operatorUid: operator uid for current moved,
        	x: latestX,
        	y: latestY
	 * })
	 * 		
	 */
	function paintOperator(args){
		var surface = _getSurface(args.canvasInfo);
		var paneInfo = _getCurrentPaneInfo(args.canvasInfo.paneId);
		_initPaneInfo(args);
		
		var operatorPaintInfo = args.op;
		var font = args.font;
		var operatorUnit = surface.createGroup();


		var opShape = surface.createImage({
			x: operatorPaintInfo.x, 
			y: operatorPaintInfo.y, 
			width: operatorPaintInfo.width,
			height: operatorPaintInfo.height,
            src: operatorPaintInfo.src
		});
		var opLabel = surface.createText({
			x: operatorPaintInfo.x + _getHalfIconSize(args.canvasInfo.paneId),
            y: operatorPaintInfo.y + operatorPaintInfo.height + constants.GAP,
            text: operatorPaintInfo.label,
            align: "middle"
		});
		operatorUnit.add(opShape);
		operatorUnit.add(opLabel);
		
		_setupFont(opLabel, font);
		
		if(args.bindEvent == true){
			var eventHandlerSet = [];
			paneInfo.eventHandler = eventHandlerSet;
			_bindOperatorEvent(eventHandlerSet, operatorUnit, operatorPaintInfo, args);
		}
		if(args.ableMove == true){
			_setupMoveable(operatorUnit, args);
		}
		
		var currentPaneInfo = _getCurrentPaneInfo(args.canvasInfo.paneId);
		currentPaneInfo.operatorUnitMap[operatorPaintInfo.uid] = {
			/*private*/ _shapeGroup: operatorUnit,
			icon: operatorUnit.children[0],
			label: operatorUnit.children[1]
		};
		return opShape;
	}
	
	function _setupFont(labelShape, font){
		var _font = dojo.clone(constants.DEF_FONT);
		dojo.safeMixin(_font, font);
//		font = font || constants.DEF_FONT;
		labelShape.setFont({
			family: _font.family, 
			size: _font.size, 
			weight: _font.weight
		}).setFill(_font.color);
	}
	
	/**
	 * bind callback to shape event
	 * 		onClick(op)
	 * 		onDblClick(op)
	 * 		onMouseOver(op)
	 * 		onMouseOut(op)
	 */
	function _bindOperatorEvent(eventHandlerSet, operatorUnit, callbackArg, eventCallbackSet){
		if(eventCallbackSet.onClick && typeof eventCallbackSet.onClick == "function"){
			eventHandlerSet.push(operatorUnit.children[0].connect("onclick", function(e){
				eventCallbackSet.onClick.call(null, callbackArg, e);
				dojo.stopEvent(e);
			}));
		}
		if(eventCallbackSet.onDblClick && typeof eventCallbackSet.onDblClick == "function"){
			eventHandlerSet.push(operatorUnit.children[0].connect("ondblclick", function(e){
				eventCallbackSet.onDblClick.call(null, callbackArg, e);
				dojo.stopEvent(e);
			}));
		}
		if(eventCallbackSet.onMouseOver && typeof eventCallbackSet.onMouseOver == "function"){
			eventHandlerSet.push(operatorUnit.children[0].connect("onmouseover", function(e){
				_displayOperatorTooltip(eventCallbackSet.canvasInfo.paneId, callbackArg.uid, true);
				eventCallbackSet.onMouseOver.call(null, callbackArg, e);
			}));
		}
		if(eventCallbackSet.onMouseOut && typeof eventCallbackSet.onMouseOut == "function"){
			eventHandlerSet.push(operatorUnit.children[0].connect("onmouseout", function(e){
				eventCallbackSet.onMouseOut.call(null, callbackArg, e);
				_displayOperatorTooltip(eventCallbackSet.canvasInfo.paneId, callbackArg.uid, false);
			}));
		}
		if(eventCallbackSet.onDblClickLabel && typeof eventCallbackSet.onDblClickLabel == "function"){
			eventHandlerSet.push(operatorUnit.children[1].connect("ondblclick", function(e){
				eventCallbackSet.onDblClickLabel.call(null, callbackArg, e);
				dojo.stopEvent(e);
			}));
		}
	}
	
	function _setupMoveable(operatorUnit, args){
		var handler = new gfx.Moveable(operatorUnit, {
			mover: AlpineIlluminator.Mover
		});
		
		handler.uid = args.op.uid;
		handler.paneId = args.canvasInfo.paneId;
		paneDrawInfoMap[args.canvasInfo.paneId].onMove = args.onMove;
	}
	
	function _getHalfIconSize(paneId){
		var currPane = _getCurrentPaneInfo(paneId);
		return currPane.halfIconSize;
	}
	
	function _initPaneInfo(paintInfo){
		var currPane = _getCurrentPaneInfo(paintInfo.canvasInfo.paneId);
		if(!currPane.width){
			if(!paintInfo){
				throw "operatorPaintInfo is null";
			}
			currPane.width = paintInfo.canvasInfo.width;
			currPane.height = paintInfo.canvasInfo.height;
			currPane.halfIconSize = paintInfo.op.width / 2;
		}
	}
	
	/**
	 * find operator shape unit
	 * 
	 * return {
	 * 		icon: Shape
	 * 		label: Shape 
	 * 		ears: {
	 * 			l: left ear,
	 * 			r: right ear
	 * 		}
	 * }
	 * 
	 */
	function fineOperatorUnit(paneId, operatorId, callbackFn){
		var operatorUnit = _getCurrentPaneInfo(paneId).operatorUnitMap[operatorId];
		callbackFn.call(null, operatorUnit);
	}
	
	/**
	 * To update Operator icon and label visualization.
	 * both icon and label are optional arguments. if not passed, will use previous.
	 * args = {
	 * 		paneId,
	 * 		operatorId,
	 * 		icon(optional): {
	 * 			root -- folder path what is img storaged.(optional)
	 * 			img	--	a image name e.g. test.png
	 * 		},
	 * 		label(optional): {
	 * 			family: "Arial", 
	 *			size: "8pt", 
	 *			weight: "normal",
	 *			color: "#000000",
	 *			text: "label content"
	 * 		}
	 * }
	 */
	function updateOperatorVisualization(args){
		fineOperatorUnit(args.paneId, args.operatorId, function(operatorUnit){
			if(args.icon){
				//var rootPath = args.icon.root || constants.ICON_ROOT;
				operatorUnit.icon.setShape({
	        		src: args.icon.src
	        		//src: rootPath + args.icon.img
	        	});
			}
			if(args.label){
				var previousFont = operatorUnit.label.getFont();
				if(args.label.text){
					operatorUnit.label.shape.text = args.label.text;
					operatorUnit.label.setShape(operatorUnit.label.shape);
					delete args.label.text;
				}
				dojo.safeMixin(previousFont, args.label);
				operatorUnit.label.setFont(previousFont);
				if(args.label.color){
					operatorUnit.label.setFill(args.label.color);
				}
			}
		});
	}
	
	/**
	 * remove operator icon, label.
	 */
	function _removeOperatorUnit(paneId, operatorId){
		fineOperatorUnit(paneId, operatorId, function(operatorUnit){
			operatorUnit.icon.removeShape();
			operatorUnit.label.removeShape();
			delete _getCurrentPaneInfo(paneId).operatorUnitMap[operatorId];
		});
	}
	
	function _displayOperatorTooltip(paneId, operatorUid, display){
		if(operatorHandler.getOperatorPrimaryInfo(operatorUid).description){
			if(display == true){
				fineOperatorUnit(paneId, operatorUid, function(shapeUnit){
					var operatorPrimaryInfo = operatorHandler.getOperatorPrimaryInfo(operatorUid);
					var message = operatorHandler.getOperatorPrimaryInfo(operatorUid).description;
					tooltipRefShape = dojo.clone(shapeUnit.icon.shape);
					tooltipRefShape.x = operatorPrimaryInfo.x;
					tooltipRefShape.y = operatorPrimaryInfo.y;
					
					tooltipRefShape.x += dijit.byId("personalFlowTree").domNode.offsetWidth;
					tooltipRefShape.x -= dojo.byId(paneId).scrollLeft;
					tooltipRefShape.y += 70;//offset from top
					tooltipRefShape.y -= dojo.byId(paneId).scrollTop;

					message = message.replace(/</g, "&lt;");
					message = message.replace(/>/g, "&gt;");
					message = message.replace(/\n/g, "<br/>");
					
					dijit.showTooltip(message, tooltipRefShape);
				});
			}else{
				if(tooltipRefShape){
					dijit.hideTooltip(tooltipRefShape);
					tooltipRefShape = null;
				}
			}
		}
	}
	
	//----------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * paint link
	 * below are details for arguments
	 * 
	 * paneId
	 * style		--optional
	 * 		color,
	 * 		width
	 * link: {
	 * 		sourceId,
	 * 		targetId,
	 * 		startPoint: {
	 * 			x,y
	 * 		},
	 * 		endPoint: {
	 * 			x,y
	 * 		}
	 * }
	 * onSelect function
	 */
	function paintLink(args){
		var lineStyle = dojo.safeMixin(dojo.clone(constants.DEF_LINE), args.style);
		var line = _createLine({
			paneId: args.paneId,
			style: lineStyle,
			startPoint: args.link.startPoint,
			endPoint: args.link.endPoint
		});
		var selectArea = _createLine({
			paneId: args.paneId,
			style: {
				color: [255, 255, 255, 0.1],
				width: 10
			},
			startPoint: args.link.startPoint,
			endPoint: args.link.endPoint
		});
		var arrow = _createArrow({
			paneId: args.paneId,
			style: lineStyle,
			line: line
		});
		
		var linkInfo = {
			link: args.link,
			line: line,
			selectArea: selectArea,
			arrow: arrow
		};
		
		selectArea.connect("onclick", function(e){
			if(args.onSelect != null && typeof args.onSelect == "function"){
				args.onSelect.call(null, {
					paneId: args.paneId,
					sourceId: linkInfo.link.sourceId,
					targetId: linkInfo.link.targetId
				});
			}
			dojo.stopEvent(e);
		});
		
		//keep link, line, arrow info to cache.
		paneDrawInfoMap[args.paneId].operatorLinkSet.push(linkInfo);
	}
	
	/**
	 * update link style
	 * 
	 * args = {
	 * 		line--Shape,
	 * 		arrow--Shape,
	 * 		style:{
	 * 			color,
	 *			width
	 * 		}
	 * }
	 */
	function _updateLinkVisualization(args){
		var strokeStyle = {};
		if(args.style){
			if(args.style.color){
				args.line.setFill(args.style.color);
				args.arrow.setFill(args.style.color);
				strokeStyle.color = args.style.color;
			}
			if(args.style.width){
				strokeStyle.width = args.style.width;
			}
			args.line.setStroke(strokeStyle);
			args.arrow.setStroke(strokeStyle);
		}
	}
	
	/**
	 * move link to position where follow operator
	 * paneId
	 * operatorUid
	 * offsetPoint: {
	 * 		x,
	 * 		y
	 * }
	 */
	function _moveLinkFollowOperator(args){
		var operatorLinkSet = paneDrawInfoMap[args.paneId].operatorLinkSet;
		for(var i = 0;i < operatorLinkSet.length;i++){
			if(operatorLinkSet[i].link.targetId == args.operatorUid){
				operatorLinkSet[i].link.endPoint.x += args.offsetPoint.x;
				operatorLinkSet[i].link.endPoint.y += args.offsetPoint.y;
			}else if(operatorLinkSet[i].link.sourceId == args.operatorUid){
				operatorLinkSet[i].link.startPoint.x += args.offsetPoint.x;
				operatorLinkSet[i].link.startPoint.y += args.offsetPoint.y;
			}
			var points = _buildLinePoints(dojo.safeMixin({
				paneId: args.paneId
			}, operatorLinkSet[i].link));
			_updateLine({
				paneId: args.paneId,
				line: operatorLinkSet[i].line,
				selectArea: operatorLinkSet[i].selectArea,
				startPoint: points.startPoint,
				endPoint: points.endPoint
			});
			_updateArrow({
				line: operatorLinkSet[i].line,
				arrow: operatorLinkSet[i].arrow
			});
			_moveLinkPoints(operatorLinkSet[i]);
		}
	}
	
	/**
	 * paint an activity link
	 * below are details for arguments
	 * paneId
	 * style
	 * 		color
	 * link: {
	 * 		sourceId,
	 * 
	 * 		endPointX,
	 * 		endPointY
	 * }
	 * 		
	 */
	function _paintActivityLink(args){
		var style = dojo.clone(constants.DRAG_LINE);
		dojo.safeMixin(style, args.style);
		//var startShape = paneDrawInfoMap[args.paneId].operatorUnitMap[args.link.sourceId].icon.shape;
		var opPrimaryInfo = operatorHandler.getOperatorPrimaryInfo(args.link.sourceId);
		var drawCanvas = dojo.byId(args.paneId);
        var argsCopy = {
			paneId: args.paneId,
			style: style,
			link: {
				sourceId: args.link.sourceId,
				targetId: constants.ACTIVITY_TARGET,
				startPoint: {
					x: opPrimaryInfo.x,
					y: opPrimaryInfo.y
				},
				endPoint: {
					x: args.link.endPointX - dojo.byId(args.paneId).parentNode.parentNode.offsetLeft + drawCanvas.scrollLeft,
					y: args.link.endPointY - dojo.byId(args.paneId).parentNode.parentNode.offsetTop - 45 + drawCanvas.scrollTop//45 is icon size.
				}
			}
		};
        paintLink(argsCopy);
        _makeLinkMoveable({
        	paneId: args.paneId,
        	latestPoint: {
        		x: args.link.endPointX,
        		y: args.link.endPointY
        	}
        });
	}
	
	/**
	 * make link moveable
	 * 
	 * args = {
	 * 		paneId,
	 * 		sourceId,
	 *		targetId,
	 *		isReverse--true|false, true: reconnect source operator. 
	 * 		latestPoint: {
	 * 			x,y
	 * 		},
	 * }
	 */
	function _makeLinkMoveable(args){
		var operatorLinkSet = paneDrawInfoMap[args.paneId].operatorLinkSet;
		if(args.isReverse != null){
			for(var i = 0;i < operatorLinkSet.length;i++){
				if(operatorLinkSet[i].link.sourceId == args.sourceId 
						&& operatorLinkSet[i].link.targetId == args.targetId){
					if(args.isReverse == true){
						operatorLinkSet[i].link.sourceId = constants.ACTIVITY_TARGET;
					}else{
						operatorLinkSet[i].link.targetId = constants.ACTIVITY_TARGET;
					}
				}
			}
		}
		
		connectingEvents.push(dojo.connect(dojo.byId(args.paneId), "onmousemove", function(mouseInfo){
			var latestX = mouseInfo.clientX - dojo.byId(args.paneId).parentNode.parentNode.offsetLeft + dojo.byId(args.paneId).scrollLeft;
				latestY = mouseInfo.clientY - dojo.byId(args.paneId).parentNode.parentNode.offsetTop - 43 + dojo.byId(args.paneId).scrollTop;//43 is banner height.
			_moveLinkFollowPoint({
				paneId: args.paneId,
				targetPoint: {
					x: latestX,
					y: latestY
				}
			});
		}));
	}
	
	/**
	 * move link follow a point 
	 * below are details for args
	 * 
	 * paneId
	 * targetPoint: {
	 * 		x,
	 * 		y
	 * }
	 */
	function _moveLinkFollowPoint(args){
		var operatorLinkSet = paneDrawInfoMap[args.paneId].operatorLinkSet;
		var index = 0;
		var isMoveEndPoint = true;
		for(var i = 0;i < operatorLinkSet.length;i++){
			index = i;
			if(operatorLinkSet[i].link.targetId == constants.ACTIVITY_TARGET){
				operatorLinkSet[i].link.endPoint.x = args.targetPoint.x;
				operatorLinkSet[i].link.endPoint.y = args.targetPoint.y;
				isMoveEndPoint = true;
				break;
			}else if(operatorLinkSet[i].link.sourceId == constants.ACTIVITY_TARGET){
				operatorLinkSet[i].link.startPoint.x = args.targetPoint.x;
				operatorLinkSet[i].link.startPoint.y = args.targetPoint.y;
				isMoveEndPoint = false;
				break;
			}
		}
		var points = _buildLinePoints(dojo.safeMixin({
			paneId: args.paneId
		}, operatorLinkSet[index].link));

	    var track = _buildMoveRadius({
	    	startPoint: isMoveEndPoint ? operatorLinkSet[index].link.startPoint : args.targetPoint,
	    	endPoint: isMoveEndPoint ? args.targetPoint : operatorLinkSet[index].link.endPoint,
	    	radius: isMoveEndPoint ? -10 : 10
	    });
	    track.x += args.targetPoint.x;
	    track.y += args.targetPoint.y;
		
		var updateLineArgs = {
			paneId: args.paneId,
			line: operatorLinkSet[index].line,
			selectArea: operatorLinkSet[index].selectArea
		};
		if(isMoveEndPoint){
			updateLineArgs.startPoint = points.startPoint;
			updateLineArgs.endPoint = track;
		}else{
			updateLineArgs.startPoint = track;
			updateLineArgs.endPoint = points.endPoint;
		}
		_updateLine(updateLineArgs);
		_updateArrow({
			line: operatorLinkSet[index].line,
			arrow: operatorLinkSet[index].arrow
		});
		_moveLinkPoints(operatorLinkSet[index]);
	}
	

	/**
	 * remove link by sourceId and targetId
	 * args = {
	 * 		paneId, 
	 * 		judgementAbleToRemove(function)	-- arg1 = sourceId, arg2 = targetId
	 * }
	 */
	function _removeLink(args){
		var operatorLinkSet = paneDrawInfoMap[args.paneId].operatorLinkSet;
		for(var i = 0;i < operatorLinkSet.length;i++){
			var operatorLink = operatorLinkSet[i];
			var ableToRemove = args.judgementAbleToRemove.call(null, operatorLink.link.sourceId, operatorLink.link.targetId);
			if(ableToRemove == true){
				operatorLink.line.removeShape();
				operatorLink.arrow.removeShape();
				operatorLink.selectArea.removeShape();
				if(operatorLink.controlPoints){
					for(var j = 0;j < operatorLink.controlPoints.length;j++){
						operatorLink.controlPoints[j].removeShape();
					}
				}
				linkHandler.removeLinkInfo(operatorLink.link.sourceId, operatorLink.link.targetId);//to delete link info from link cache.
                operatorLinkSet.splice(i--, 1);
			}
		}
	}
	
	/**
	 * paint line
	 * 
	 * args include below attribute
	 * paneId
	 * style
	 * 		color,
	 * 		width
	 * startPoint
	 * 		x
	 * 		y
	 * endPoint
	 * 		x
	 * 		y
	 */
	function _createLine(args){
		var points = _buildLinePoints(args);
		
	    var line = _getSurface(args).createLine({
	        x1 : points.startPoint.x,
	        y1 : points.startPoint.y,
	        x2 : points.endPoint.x,
	        y2 : points.endPoint.y
	    });
	    line.setFill(args.style.color);
	    line.setStroke({
	        color : args.style.color,
	        width : args.style.width
	    });
	    return line;
	}
	
	/**
	 * update line
	 * 
	 * args include below attribute
	 * paneId
	 * line	(Shape)
	 * style -- optional
	 * 		color
	 * startPoint
	 * 		x
	 * 		y
	 * endPoint
	 * 		x
	 * 		y
	 */
	function _updateLine(args){
		args.line.setShape({
			x1 : args.startPoint.x,
	        y1 : args.startPoint.y,
	        x2 : args.endPoint.x,
	        y2 : args.endPoint.y
	    });
		args.selectArea.setShape({
			x1 : args.startPoint.x,
	        y1 : args.startPoint.y,
	        x2 : args.endPoint.x,
	        y2 : args.endPoint.y
	    });
	}
	
	/**
	 * build start x, y and end x, y
	 * paneId
	 * 
	 * startPoint
	 * 		x
	 * 		y
	 * endPoint
	 * 		x
	 * 		y
	 * @return {
	 * 		startPoint: {
	 * 			x,y
	 * 		},
	 * 		endPoint: {
	 * 			x,y
	 * 		}
	 *  }
	 */
	function _buildLinePoints(args){
		var startPoint = args.startPoint,
			endPoint = args.endPoint;
	    var halfIconSize = _getHalfIconSize(args.paneId);
	    var track = _buildMoveRadius({
	    	startPoint: args.startPoint,
	    	endPoint: args.endPoint,
	    	radius: constants.LINK_ADJUST
	    });
	    var sx = startPoint.x + halfIconSize + track.x;
	    var sy = startPoint.y + halfIconSize + track.y;
	    var ex = endPoint.x + halfIconSize - track.x;
	    var ey = endPoint.y + halfIconSize - track.y;
	    return {
	    	startPoint: {
	    		x: sx,
	    		y: sy
	    	},
	    	endPoint: {
	    		x: ex,
	    		y: ey
	    	}
	    };
	}
	
	/**
	 * build the point around other point by radius
	 * 
	 * args = {
	 * 		startPoint: {
	 * 			x,y
	 * 		},
	 * 		endPoint: {
	 * 			x,y
	 * 		}
	 * 		radius
	 * }
	 */
	function _buildMoveRadius(args){
		var track = {};
	    if (args.startPoint.x == args.endPoint.x) {
	    	track.x = 0;
	    	if(args.startPoint.y > args.endPoint.y){//upping arrow
		    	track.y = 0 - args.radius;
	    	}else{
		    	track.y = args.radius;
	    	}
	    }else if (args.startPoint.y == args.endPoint.y) {
	    	track.x = args.radius;
	    	track.y = 0;
	    }else {
	        var t = (args.endPoint.y - args.startPoint.y) / (args.endPoint.x - args.startPoint.x);
	        var angle = Math.atan(t);
	        track.x = args.radius * Math.cos(angle);
	        track.y = args.radius * Math.sin(angle);
	    }
	    if (args.startPoint.x > args.endPoint.x) {
	    	track.x = 0 - track.x;
	    	track.y = 0 - track.y;
	    }
	    return track;
	}
	
	/**
	 * 
	 * paneId
	 * style
	 * 		color,
	 * 		width
	 * line
	 */
	function _createArrow(args){
		var points = _buildArrowPoints(args.line);
	    var arrow = surfaceCache[args.paneId].createPolyline(points);
	    arrow.setFill(args.style.color);
	    arrow.setStroke({
	        color : args.style.color,
	        width : args.style.width
	    });
	    return arrow;
	}

	/**
	 * 
	 * paneId
	 * line	(Shape)
	 * arrow	(Shape)
	 */
	function _updateArrow(args){
		var points = _buildArrowPoints(args.line);
		args.arrow.setShape(points);
	}
	
	/**
	 * build arrow points
	 * line
	 */
	function _buildArrowPoints(line){
		var sx = line.shape.x1,
			sy = line.shape.y1,
			ex = line.shape.x2,
			ey = line.shape.y2;
	    var len = Math.sqrt((ex - sx) * (ex - sx) + (ey - sy) * (ey - sy));
	    if (len == 0) {
	        return;
	    }
	    var t = (ey - sy) / (ex - sx);
	    var angle = Math.atan(t);
	    var x, y;
	    var len1 = Math.min(10, len - 2);
	    var len2 = Math.min(3, len / 2 - 1);
	    if (ex < sx) {
	        x = (len1 - len) * Math.cos(angle) + sx;
	        y = (len1 - len) * Math.sin(angle) + sy;
	    } else{
	        x = (len - len1) * Math.cos(angle) + sx;
	        y = (len - len1) * Math.sin(angle) + sy;
	    }
	    var x3 = x + len2 * Math.sin(angle);
	    var y3 = y - len2 * Math.cos(angle);
	    var x4 = x - len2 * Math.sin(angle);
	    var y4 = y + len2 * Math.cos(angle);
	    return [{
	        x : ex,
	        y : ey
	    }, {
	        x : x3,
	        y : y3
	    }, {
	        x : x4,
	        y : y4
	    } ];
	}
	//----------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * heighlight publish for giving Operator
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * 		onStartConnect--function
	 * 		onEndConnect--function (connectionInfo = {
	 *				sourceOperatorUid,
	 *				targetOperatorUid,
	 *				operatorInfoList
	 * 			})//if release mouse on any operator will be null 
	 * }
	 */
	function _setupPublish(args){
		var surface = surfaceCache[args.paneId];
        //var operatorBasicInfo = operatorUtil.getOperatorObjectByKey(args.className);
        fineOperatorUnit(args.paneId, args.operatorUid, function(operatorUnit){
            var iconShape = operatorUnit.icon.shape;
        	var x = iconShape.x + iconShape.width / 2;//use icon center point.
        	var y = iconShape.y + (iconShape.height) / 2 - constants.EAR_ICON.HEIGHT / 2;//use icon centerpoint to minus half of ears icon height
            var rEar = surface.createPath().moveTo(x, y)
									.setAbsoluteMode(false)
									.lineTo(7, 0)
									.smoothCurveTo(6, 6, 0, constants.EAR_ICON.HEIGHT)
                                    .lineTo(-20,0)
                                    .lineTo(0, -constants.EAR_ICON.HEIGHT)
                                    .lineTo(13, 0)
                                    .lineTo(0, constants.EAR_ICON.HEIGHT)
                                    .setFill(constants.EAR_ICON.FILL)
                                    .setStroke({ color: constants.EAR_ICON.STROKE, width: 1 });
           var lEar = surface.createPath().moveTo(x, y)
									.setAbsoluteMode(false)
									.lineTo(-7, 0)
									.smoothCurveTo(-6, 6, 0, constants.EAR_ICON.HEIGHT)
									.lineTo(20, 0)
									.lineTo(0, -constants.EAR_ICON.HEIGHT)
									.lineTo(-13, 0)
									.lineTo(0, constants.EAR_ICON.HEIGHT)
									.setFill(constants.EAR_ICON.FILL)
									.setStroke({ color: constants.EAR_ICON.STROKE, width: 1 });

            lEar.connect("onmousedown", function (mouseInfo){
                isDraggingArrow = true;
        		connectingEvents.push(dojo.connect(dojo.byId(args.paneId), "onmouseup", function(){
        			_endConnectHandler();
        		}));
                args.onStartConnect.call(null, mouseInfo);
                
            });
            rEar.connect("onmousedown", function (mouseInfo){
                isDraggingArrow = true;
        		connectingEvents.push(dojo.connect(dojo.byId(args.paneId), "onmouseup", function(){
        			_endConnectHandler();
        		}));
                args.onStartConnect.call(null, mouseInfo);
            });
            _animateEarOnHover(rEar);
            _animateEarOnHover(lEar);
            if(!operatorUnit.ears){
                operatorUnit.ears = {};
            }
            operatorUnit.ears.l = lEar;
            operatorUnit.ears.r = rEar;
            operatorUnit._shapeGroup.add(lEar);
            operatorUnit._shapeGroup.add(rEar);
            lEar.moveToBack();
            rEar.moveToBack();
            fx.combine([_animateEar(lEar, true), _animateEar(rEar, false)]).play();
        });

        function _endConnectHandler(){
			isDraggingArrow = false;
			_removeLink({
				paneId: args.paneId,
				judgementAbleToRemove: function(sourceId, targetId){
					return targetId == constants.ACTIVITY_TARGET;
				}
			});
			args.onEndConnect.call(null, args.operatorUid, draggingTargetOperatorUid);
            draggingTargetOperatorUid = null;
			//release resources of dragging
			var event = null;
			while((event = connectingEvents.pop()) != undefined){
				dojo.disconnect(event);
			}
        }

        function _animateEarOnHover(ear) {
            ear.connect("onmouseover",function (mouseInfo){
                gfxFx.animateStroke({
                    shape: ear,
                    duration: 100,
                    color: {end:constants.EAR_ICON.HOVER},
                    width: {end:2}
                }).play();
            });
            ear.connect("onmouseout",function (mouseInfo){
                gfxFx.animateStroke({
                    shape: ear,
                    duration: 300,
                    color: {end: constants.EAR_ICON.STROKE},
                    width: {end:1}
                }).play();
            });
        }
	}

	/**
	 * heightlight subscribe for giving Operator
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * 		className,
	 * }
	 */
	function _setupSubscribe(args){
//		var surface = surfaceCache[args.paneId];
		fineOperatorUnit(args.paneId, args.operatorUid, function(operatorUnit){
			
			connectingEvents.push(operatorUnit.icon.connect("onmouseover", operatorUnit, function(){
				this.icon.setShape({
	        		src: operatorUtil.getTargetImageSourceByKey(args.className)
	        	});
				draggingTargetOperatorUid = args.operatorUid;
			}));

			connectingEvents.push(operatorUnit.icon.connect("onmouseout", operatorUnit, function(){
				this.icon.setShape({
	        		src: operatorUtil.getStandardImageSourceByKey(args.className)
	        		//src: constants.ICON_ROOT + operatorUtil.getOperatorObjectByKey(args.className).icon
	        	});
				draggingTargetOperatorUid = null;
			}));
		});
	}
	
	/**
	 * fade out operator.
	 * @param args = {
	 * 		paneId,
	 * 		operatorUid,
	 * 		className
	 * }
	 */
	function _setupUnableConnectOperator(args){
		fineOperatorUnit(args.paneId, args.operatorUid, function(operatorUnit){
			operatorUnit.icon.setShape({
        		src: operatorUtil.getFadedImageSourceByKey(args.className)
        	});

			connectingEvents.push(operatorUnit.icon.connect("onmouseover", operatorUnit, function(){
				this.icon.setShape({
	        		src: operatorUtil.getFadedImageSourceByKey(args.className)
	        	});
			}));

			connectingEvents.push(operatorUnit.icon.connect("onmouseout", operatorUnit, function(){
				this.icon.setShape({
	        		src: operatorUtil.getFadedImageSourceByKey(args.className)
	        	});
			}));
		});
	}
	
	/**
	 * discard publish from giving Operator
	 * make publisher to normal
	 * 
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * }
	 */
	function _discardConnectState(args){
		if(!args.operatorUid){
			return;
		}
		fineOperatorUnit(args.paneId, args.operatorUid, function(operatorUnit){
			if(!operatorUnit){
				return;
			}
			var ears = operatorUnit.ears;
			if(!ears){
				return;
			}

			ears.l.removeShape();
			ears.r.removeShape();
			operatorUnit._shapeGroup.remove(ears.l);
			operatorUnit._shapeGroup.remove(ears.r);
			delete operatorUnit.ears;
		});
	}
	
	/**
	 * reset operator icon to normal state
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * 		className
	 * }
	 */
	function _resetOperatorIcon(args){
		fineOperatorUnit(args.paneId, args.operatorUid, function(operatorUnit){
			operatorUnit.icon.setShape({
                src: operatorUtil.getStandardImageSourceByKey(args.className)
        		//src: constants.ICON_ROOT + operatorUtil.getOperatorObjectByKey(args.className).icon
        	});
		});
	}
	
	function _animateEar(animateShape, isReverse){
		var proviousSize = 0;
		var anim = new dojo.Animation({
			duration: 300,
			easing: dojo.getObject("dojo.fx.easing.backOut"),
			curve: [0, 20]
		});
		 
		dojo.connect(anim, "onAnimate", function(size){
			animateShape.applyLeftTransform({
				dx: isReverse ? -(size - proviousSize) : size - proviousSize
			});
			proviousSize = size;
		});
		return anim;
	}

	//----------------------------------------------------------------------------------------------------------------------------
	

	/**
	 * args = {
	 * 		paneId,
	 * 		operatorUid,
	 * 		originalSourceId,
	 * 		originalTargetId,
	 * 		onEndConnect function arguments(currentOperatorId, targetId, originalSourceId, originalTargetId) if terminal the targetId will null.
	 * }
	 */
	function _reconnectListener(args){
		connectingEvents.push(dojo.connect(dojo.byId(args.paneId), "onmouseup", function(){
			isDraggingArrow = false;
			_removeLink({
				paneId: args.paneId,
				judgementAbleToRemove: function(sourceId, targetId){
					return targetId == constants.ACTIVITY_TARGET || sourceId == constants.ACTIVITY_TARGET;
				}
			});
			args.onEndConnect.call(null, args.operatorUid, draggingTargetOperatorUid, args.originalSourceId, args.originalTargetId);
            draggingTargetOperatorUid = null;
			//release resources of dragging
			var event = null;
			while((event = connectingEvents.pop()) != undefined){
				dojo.disconnect(event);
			}
		}));
	}
	
	function _selectLink(paneId, sourceId, targetId){
		var linkUnit = _getLinkUnit(paneId, sourceId, targetId);
		if(!linkUnit){
			return;
		}
		_updateLinkVisualization({
			line: linkUnit.line,
			arrow: linkUnit.arrow,
			style: constants.SELECTED_LINE
		});
	}
	
	/**
	 * reset link and release resources.
	 */
	function _resetLink(paneId, sourceId, targetId){
		var linkUnit = _getLinkUnit(paneId, sourceId, targetId);
		if(!linkUnit){
			return;
		}
		_updateLinkVisualization({
			line: linkUnit.line,
			arrow: linkUnit.arrow,
			style: constants.DEF_LINE
		});
		_removeControlPoints(paneId, sourceId, targetId);
	}
	
	function _removeControlPoints(paneId, sourceId, targetId){
		var linkUnit = _getLinkUnit(paneId, sourceId, targetId);
		if(!linkUnit || !linkUnit.controlPoints){
			return;
		}
		var controlPoint = null;
		while((controlPoint = linkUnit.controlPoints.pop()) != null){
			controlPoint.removeShape();
		}
		var event = null;
		while((event = linkControlEvents.pop()) != null){
			dojo.disconnect(event);
		}
	}
	
	function _getLinkUnit(paneId, sourceId, targetId){
		for(var i = 0;i < paneDrawInfoMap[paneId].operatorLinkSet.length;i++){
			if(paneDrawInfoMap[paneId].operatorLinkSet[i].link.sourceId == sourceId
					&& paneDrawInfoMap[paneId].operatorLinkSet[i].link.targetId == targetId){
				return paneDrawInfoMap[paneId].operatorLinkSet[i];
			}
		}
		return null;
	}
	
	/**
	 * create move point at two side and delete point at center.
	 * args = {
	 * 		paneId
	 * 		sourceId,
	 * 		targetId,
	 * 		onStartSourceMove function argument(targetId, linkInfo, event)
	 * 		onStartTargetMove function argument(sourceId, linkInfo, event)
	 * 		onEndSourceMove function argument(currentOperatorId, targetId, originalSourceId, originalTargetId)
	 * 		onEndTargetMove function argument(currentOperatorId, targetId, originalSourceId, originalTargetId)
	 * 		onDelete
	 * }
	 */
	function _attachControlPoints(args){
		var linkUnit = null;
		for(var i = 0;i < paneDrawInfoMap[args.paneId].operatorLinkSet.length;i++){
			if(paneDrawInfoMap[args.paneId].operatorLinkSet[i].link.sourceId == args.sourceId
					&& paneDrawInfoMap[args.paneId].operatorLinkSet[i].link.targetId == args.targetId){
				linkUnit = paneDrawInfoMap[args.paneId].operatorLinkSet[i];
				break;
			}
		}
		if(!linkUnit){
			return;
		}
		
		var lineShape = linkUnit.line.shape;
		var originalSourceId = args.sourceId,
			originalTargetId = args.targetId;
		
		var controlPoint = _createPoint(args.paneId, {
			x: Math.abs(lineShape.x1 - lineShape.x2) / 2 + Math.min(lineShape.x1, lineShape.x2),
			y: Math.abs(lineShape.y1 - lineShape.y2) / 2 + Math.min(lineShape.y1, lineShape.y2)
		});
		var sourcePoint = _createPoint(args.paneId, {
			x: Math.abs(lineShape.x1),
			y: Math.abs(lineShape.y1)
		});
		var targetPoint = _createPoint(args.paneId, {
			x: Math.abs(lineShape.x2),
			y: Math.abs(lineShape.y2)
		});
		linkUnit.controlPoints = [controlPoint, sourcePoint, targetPoint];
		_bindPointEvents(controlPoint, "onclick", function(e){
			args.onDelete();
			dojo.stopEvent(e);
		});
		_bindPointEvents(sourcePoint, "onmousedown", function (event){
			isDraggingArrow = true;
			args.onStartSourceMove.call(null, args.targetId, linkUnit.link, event);
			_reconnectListener({
				paneId: args.paneId,
				operatorUid: args.targetId,
				originalSourceId: originalSourceId,
				originalTargetId: originalTargetId,
				onEndConnect: args.onEndSourceMove
			});
		});
		_bindPointEvents(targetPoint, "onmousedown", function (event){
			isDraggingArrow = true;
			args.onStartTargetMove.call(null, args.sourceId, linkUnit.link, event);
			_reconnectListener({
				paneId: args.paneId,
				operatorUid: args.sourceId,
				originalSourceId: originalSourceId,
				originalTargetId: originalTargetId,
				onEndConnect: args.onEndTargetMove
			});
		});
	}
	
	function _bindPointEvents(pointShape, event, listener){
		if(listener && typeof listener == "function"){
			linkControlEvents.push(pointShape.connect(event, listener));
		}
	}
	
	/**
	 * move points by line position.
	 */
	function _moveLinkPoints(linkUnit){
		var lineShape = linkUnit.line.shape;
		if(!linkUnit.controlPoints || linkUnit.controlPoints.length == 0){
			return;
		}
		linkUnit.controlPoints[0].setShape({
			cx: Math.abs(lineShape.x1 - lineShape.x2) / 2 + Math.min(lineShape.x1, lineShape.x2),
			cy: Math.abs(lineShape.y1 - lineShape.y2) / 2 + Math.min(lineShape.y1, lineShape.y2)
		});
		linkUnit.controlPoints[1].setShape({
			cx: Math.abs(lineShape.x1),
			cy: Math.abs(lineShape.y1)
		});
		linkUnit.controlPoints[2].setShape({
			cx: Math.abs(lineShape.x2),
			cy: Math.abs(lineShape.y2)
		});
	}
	
	/**
	 * create a point as arguments
	 * paneId
	 * position = {x, y}
	 * return 
	 */
	function _createPoint(paneId, position){
		return _getSurface({
			paneId: paneId
		}).createCircle({
			cx: position.x, 
			cy: position.y, 
			r: 3
		}).setFill(constants.SELECTED_LINE.color);
	}
	
	//----------------------------------------------------------------------------------------------------------------------------
	function _getSurface(canvasInfo){
		var surface = surfaceCache[canvasInfo.paneId];
		if(!surface){
			var width = 3000;//create enough size to pane
			var height = 2000;//create enough size to pane
			surface = gfx.createSurface(dojo.byId(canvasInfo.paneId), width, height);
			surfaceCache[canvasInfo.paneId] = surface;
		}
		return surface;
	}
	
	function _getCurrentPaneInfo(paneId){
		var paneInfo = paneDrawInfoMap[paneId];
		if(!paneInfo){
			paneInfo = {
				operatorUnitMap: [],
				operatorLinkSet: []
			};
			paneDrawInfoMap[paneId] = paneInfo;
		}
		return paneInfo;
	}
	
	function release(paneId){
		if(!paneDrawInfoMap[paneId]){
			return;
		}
		halfIconSize = null;
		tooltipRefShape = null;
		surfaceCache[paneId].clear();
		surfaceCache[paneId].destroy();
		delete surfaceCache[paneId];
		
		var events = paneDrawInfoMap[paneId].eventHandler;
		if(events){
			var event = null;
			while((event = events.pop()) != undefined){
				dojo.disconnect(event);
			}
		}
		delete paneDrawInfoMap[paneId];

        draggingTargetOperatorUid = null;
		//release resources of dragging
		var event = null;
		while((event = connectingEvents.pop()) != undefined){
			dojo.disconnect(event);
		}
	}
	
	return {
		DEFAULT_LABEL: dojo.clone(constants.DEF_FONT),
		paintOperator: paintOperator,
		paintLink: paintLink,
		fineOperatorUnit: fineOperatorUnit,
		updateOperatorVisualization: updateOperatorVisualization,
		setupPublish: _setupPublish,
		setupSubscribe: _setupSubscribe,
		setupUnableConnectOperator: _setupUnableConnectOperator,
		discardConnectState: _discardConnectState,
		resetOperatorIcon: _resetOperatorIcon,
		removeOperatorUnit: _removeOperatorUnit,
		removeLink: _removeLink,
		paintActivityLink: _paintActivityLink,
		makeLinkMoveable: _makeLinkMoveable,
		attachControlPoints: _attachControlPoints,
		selectLink: _selectLink,
		resetLink: _resetLink,
		removeControlPoints: _removeControlPoints,
		release: release
	};
});