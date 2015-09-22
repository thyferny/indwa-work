/**
 * 
 * @param args = {
 * 		label: text on the tooltip
 * 		connectTo: the shape, which attached tooltip and created by surface.
 * 		container: the node of container
 * }
 * @return
 */
function GfxTooptip(args){
	this.container = args.container;
	this.text = args.label;
	this.graphics = args.connectTo;
	this.connectShap = args.connectTo.getShape();

	dojo.connect(this.graphics.getEventSource(), "onmouseenter", this, showTooltip);
	dojo.connect(this.graphics.getEventSource(), "onmouseleave", this, hideTooltip);
	
	function showTooltip(evt){
		this.currentShape = dojo.clone(this.connectShap);
		switch(this.graphics.getShape().type){
		case "circle":
			this.currentShape.x = this.currentShape.cx;
//			this.currentShape.y = this.currentShape.cy;	//version 1
//			this.currentShape.y += 30;	//version 1
			this.currentShape.y = getMousePosition(evt);	//version 2
			this.currentShape.width = this.currentShape.r;
			this.currentShape.height = this.currentShape.r;
			break;
		case "rect":
			this.currentShape.y = getMousePosition(evt);
			break;
		}
		var shape = this.currentShape;
		shape.x = Math.round(shape.x - this.container.scrollLeft);
//		shape.y = Math.round(shape.y - this.container.scrollTop);	//version 1
		shape.y = Math.round(shape.y);	//version 2
		shape.w = Math.ceil(shape.width);
		shape.h = Math.ceil(shape.height);
		dijit.showTooltip(this.text, shape);
		
	}

	function hideTooltip(){
		dijit.hideTooltip(this.currentShape);
	}

	function getMousePosition(event){
//		if(!dojo.isFF){
//			getMousePosition = function(event){
//				return event.clientY;
//			};
//		}else{
//			getMousePosition = function(event){
//				return event.clientY - event.rangeOffset;
//			};
//		}
//		return getMousePosition.call(null, event);
		return event.clientY;
	}
}