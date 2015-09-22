
function SideBar(/*id*/nodeId, box, childId){
	this.nodeId = nodeId;
	this.show = function(){
		dojo.style(dojo.byId(nodeId), 'zIndex', 500);
		
		var standby = dijit.byId("rootStandBy").show();
		dojo.byId(nodeId).style.display = "";
		dojox.fx.wipeTo(dojo.mixin({
			node: nodeId, 
			delay: 100 
		},{
			width: box.w
		})).play();
		dijit.byId(childId).resize();
	};
	this.close = function(){
		dijit.byId("rootStandBy").hide();
		dojox.fx.wipeTo(dojo.mixin({
				node: nodeId, 
				delay: 100,
				onEnd: function(){
                    if (dijit.byId("sidePropertyContainer")) dijit.byId("sidePropertyContainer").domNode.scrollTop = 0;  //reset to top of scrollbar.
                    dojo.byId(nodeId).style.display = "none";
                }
			},{
				width: 1
			})).play();
	};
}