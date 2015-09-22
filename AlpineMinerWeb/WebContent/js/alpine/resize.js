dojo.ready(function(){
	dijit.registry.byClass("dijit.Dialog").forEach(function(widget){
		dojo.connect(widget,"onShow",widget,function(){
			resizeDialog(this);
		});
	});
});

function autoResize(){
	window.clearTimeout(resizeHandle.tid);
	resizeHandle.tid = window.setTimeout(resizeHandle, 100);
}

function resizeHandle(){
	dijit.registry.byClass("dijit.Dialog").filter(function(widget){
		return widget.open;
	}).forEach(resizeDialog);
}

function resizeDialog(widget){
	var child = widget.getChildren()[0];
	if(!child || !(child instanceof dijit.layout.BorderContainer || 
					child instanceof dijit.layout.LayoutContainer || 
					child instanceof dijit.layout.AccordionContainer ||
					child instanceof dijit.layout.StackContainer ||
					child instanceof dijit.layout.TabContainer || 
					child instanceof dijit.layout.ContentPane)//for a dialog, which any Container in it.
	){
		return;
	}
	var handle = null;

	handle = child;
	
	var initWidth = handle.initWidth;
	var initHeight = handle.initHeight;
	if(!initWidth || !initHeight){
		var s = handle.style.replace(new RegExp("px","gm"),"");
		if(s.charAt(s.length - 1) == ";"){
			s = s.substring(0, s.length - 1);
		}
		var sArray = s.split(";");
		for(var i = 0; i < sArray.length; i++){
			if(sArray[i].toLowerCase().indexOf("width") != -1){
				handle.initWidth = dojo.trim(sArray[i].split(":")[1]);
			}else if(sArray[i].toLowerCase().indexOf("height") != -1){
				handle.initHeight = dojo.trim(sArray[i].split(":")[1]);
			}
		}
	}
	resizeWidget(handle);
}

function resizeWidget(widget){
	var body = dojo.window.getBox();
	var newSize = {
		w: widget.initWidth,
		h: widget.initHeight
	};
	if(body.w < widget.initWidth){
		newSize.w = body.w * 0.8;
	}
	if(body.h < widget.initHeight){
		newSize.h = body.h * 0.8;
	}
	
	if(widget.position){
		console.log(widget.position);
	}
	
	widget.resize(newSize);
	try{
		widget.layout();
	}catch(e){
		//ignore
	}
}