/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DatabaseExplorerUIHelper
 * Author Will & Robbie & Gary
 */
define(function(){
	
	var activeNodeId = "alpine_layout_breadcrumbnavigation_ActiveID";
	var nullFn = function(){};
	
	var minCrumbWidth = 5;
	
	function calculateWordWidth(words){
		//return words.length * 7
		
	}
	
	dojo.declare("alpine.layout.BreadcrumbNavigation", [dijit._Widget, dijit._Templated], {
		
		templatePath: dojo.moduleUrl("alpine.layout","BreadcrumbNavigation_template.html"),
		//style: 'width: 300px',
		rootLabel: '',
		rootClickFn: null,
		
		postCreate: function(){
			this.inherited(arguments);
		},
		
		_initBreadcrumb: function(){
			
			dojo.query('.crumbContainer span>a').forEach(function(itm, idx){
				itm.onmouseover = function(){
					var anim = dojo.animateProperty({
						node: itm.parentNode,
						duration: 600,
						properties: {
							width: {
								end: itm.scrollWidth
							}
						}	   
					});
					anim.play();
				};
				itm.onmouseout = function(){
					var anim = dojo.animateProperty({
						node: itm.parentNode,
						duration: 600,
						properties: {
							width: {
								end: minCrumbWidth
							}
						}
					});
					anim.play();
				};
			});
		},
		
		_animCrumbOpen: function(node, onComplete){
			var anim = dojo.animateProperty({
				node: node.parentNode,
				duration: 600,
				properties: {
					width: {
						end: node.offsetWidth
					}
				}	   
			});
			if(onComplete){
				dojo.connect(anim, "onEnd", this, onComplete);
			}
			anim.play();
		},
		
		_animCrumbClose: function(node, onComplete){
			var anim = dojo.animateProperty({
				node: node,
				duration: 600,
				properties: {
					width: {
						end: minCrumbWidth
					}
				}
			});
			if(onComplete){
				dojo.connect(anim, "onEnd", this, onComplete);
			}
			anim.play();
		},
		
		_onCrumbClick: function(event){
			this._rebuildBreadcrumb(event.target);
		},
		
		_onRootClick: function(event){
			this._rebuildBreadcrumb(event.target);
			if(this.rootClickFn){
				this.rootClickFn.call(null, event.target);
			}
		},
		
		/**
		 * 
		 * @param node
		 * @param includeSelf default false
		 */
		_rebuildBreadcrumb: function(node, /*boolean*/includeSelf){
			includeSelf = includeSelf || false;
			var children = this.breadcrumbRootNode.children;
			var startIdx = 1;
			var previousLinkNode = null;
			if(node.parentElement.parentElement != this.breadcrumbRootNode){// nodes, except first node.
				startIdx = dojo.indexOf(children, node.parentElement.parentElement) + (includeSelf ? 0 : 1);////Because node is linkNode. linkNode -> spanNode -> liNode. To +1 avoid itself.
				previousLinkNode = this.breadcrumbRootNode.children[startIdx - 1].children[0].children[0];
				dojo.removeAttr(node.parentElement, "class");
				dojo.attr(node.parentElement, "id", activeNodeId);
				node.onmouseover = nullFn;
				node.onmouseout = nullFn;
			}
			while(children.length > startIdx){
				try{
					dojo.destroy(children[startIdx]);
				}catch(e){
					console.log(e);
				}
			}
			if(previousLinkNode){//except first node.
				this._animCrumbOpen(previousLinkNode);
			}
		},
		
		/**
		 * to add one new breadcrumb node to breadcrumb container.
		 * @param crumb include:
		 * 			label
		 */
		appendCrumb: function(crumb){
			if(dojo.byId(activeNodeId)){
				dojo.addClass(dojo.byId(activeNodeId), "display");
				this._animCrumbClose(dojo.byId(activeNodeId), function(){
					dojo.removeAttr(dojo.byId(activeNodeId), "id");
					this._appendNewCrumb(crumb);
				});
			}else{
				this._appendNewCrumb(crumb);
			}
		},
		
		removeCrumb: function(node){
			this._rebuildBreadcrumb(node, true);
		},
		
		
		_appendNewCrumb: function(crumb){
			var liNode = dojo.create("li", {}, this.breadcrumbRootNode),
				spanNode = dojo.create("span", {id: activeNodeId, style: "width: " + minCrumbWidth + "px;"}, liNode),
				linkNode = dojo.create("a", {href: "#", style: "white-space: nowrap;"}, spanNode);
				linkNode.innerHTML = crumb.label;
				dojo.connect(linkNode, "onclick", this, this._onCrumbClick);
				dojo.connect(linkNode, "onclick", this, function(event){
					var index = dojo.indexOf(this.breadcrumbRootNode.children, event.target.parentElement.parentElement);
					crumb.onCrumbClick.call(null, event.target, index);
				});
			this.breadcrumbRootNode.appendChild(liNode);
			this._initBreadcrumb();
			this._animCrumbOpen(linkNode);
		}
	});
});