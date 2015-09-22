/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * WorkBenchNavigation
 * Author Robbie & Gary
 */
define(["dojo/_base/lang","dojo/_base/array",
        "dojo/dom-geometry",
        "dojo/_base/connect",
        "dojo/dom-style", 
        "dojo/dom-class", 
        "dojo/_base/fx",
        "alpine/AnimUtil",
        "alpine/system/PermissionUtil"], function(lang, arrayUtil, domGeom, connectUtil, domStyle, domClass, baseFx, animUtil, permissionUtil){
    var constants = {
        BUTTON_WORKFLOW: "alpine_layout_navigation_workflowBtn",
        BUTTON_DATASOURCE: "alpine_layout_navigation_dataSourceBtn",
        BUTTON_OPERATOR: "alpine_layout_navigation_operatorBtn",
        TAB_CONTAINER: "alpine_layout_navigation_tab_container",
        TAB_WORKFLOW: "alpine_layout_navigation_workflow_pane",
        TAB_DATASOURCE: "alpine_layout_navigation_datasource_pane",
        TAB_OPERATOR: "alpine_layout_navigation_operator_pane"
    };

    dojo.ready(function(){
        dojo.byId(constants.BUTTON_WORKFLOW).onclick = function(){
            dijit.byId(constants.TAB_CONTAINER).selectChild(constants.TAB_WORKFLOW);
            dojo.addClass(constants.BUTTON_WORKFLOW, "workbenchNaviButtonSelected");
            dojo.removeClass(constants.BUTTON_DATASOURCE, "workbenchNaviButtonSelected");
            dojo.removeClass(constants.BUTTON_OPERATOR, "workbenchNaviButtonSelected");

        };
        dojo.byId(constants.BUTTON_DATASOURCE).onclick = function(){
            dijit.byId(constants.TAB_CONTAINER).selectChild(constants.TAB_DATASOURCE);
            dojo.removeClass(constants.BUTTON_WORKFLOW, "workbenchNaviButtonSelected");
            dojo.addClass(constants.BUTTON_DATASOURCE, "workbenchNaviButtonSelected");
            dojo.removeClass(constants.BUTTON_OPERATOR, "workbenchNaviButtonSelected");
        };
        dojo.byId(constants.BUTTON_OPERATOR).onclick = function(){
            dijit.byId(constants.TAB_CONTAINER).selectChild(constants.TAB_OPERATOR);
            dojo.removeClass(constants.BUTTON_WORKFLOW, "workbenchNaviButtonSelected");
            dojo.removeClass(constants.BUTTON_DATASOURCE, "workbenchNaviButtonSelected");
            dojo.addClass(constants.BUTTON_OPERATOR, "workbenchNaviButtonSelected");
        };
        
        if(!permissionUtil.checkPermission("OPERATOR_EDIT")){
        	dojo.byId(constants.BUTTON_OPERATOR).disabled = true;
        	dojo.byId(constants.BUTTON_DATASOURCE).disabled = true;
        }
    });

    dojo.declare('alpine.layout.WorkBenchNavigation', [dojox.layout.ExpandoPane], {
        workflowButtonLabel: '',
        dataSourceButtonLabel: '',
        operatorButtonLabel: '',

        templateString: dojo.cache("alpine", "layout/workBenchNavi_template.html"),
        _setupAnims: function(){
            // summary: Create the show and hide animations
            arrayUtil.forEach(this._animConnects, connectUtil.disconnect);

            var _common = {
                    node:this.domNode,
                    duration:this.duration
                },
                isHorizontal = this._isHorizontal,
                showProps = {},
                hideProps = {},
                dimension = isHorizontal ? "height" : "width"
                ;

            showProps[dimension] = {
                end: this._showSize
            };
            hideProps[dimension] = {
                end: this._closedSize
            };
            var thistop =  domGeom.getMarginBox(this.domNode).t.toString();

            this._showAnim = dojo.fx.slideTo(lang.mixin(_common,{
                top:  thistop,
                left: { end: 0, start:-270 }
            }));

           this._hideAnim = dojo.fx.slideTo(lang.mixin(_common,{
                top: thistop,
                left:-270
        }));

            this._animConnects = [
                connectUtil.connect(this._showAnim, "beforeBegin", this, "_showStart"),
                connectUtil.connect(this._showAnim, "onEnd", this, "_showEnd"),
                connectUtil.connect(this._hideAnim, "onEnd", this, "_hideEnd")
            ];
        },

        _showStart:function(){
            this.cwrapper.style.opacity = "1";
            this.domNode.style.width="300px";
            this.domNode.style.left="-270px";
            this.resize();
        }
,
        _showWrapper: function() {
           domClass.remove(this.domNode, "dojoxExpandoClosed");
        }
        ,
		_hideWrapper: function(){
			domClass.add(this.domNode, "workbenchNaviClosed");
		}
        ,
        _hideEnd: function(){
            // summary: Callback for the hide animation - "close"

            // every time we hide, reset the "only preview" state
            this.cwrapper.style.opacity = "0";
            this.domNode.style.width="30px";
            if(!this._isonlypreview){
                setTimeout(lang.hitch(this._container, "layout"), 25);
            }else{
                this._previewShowing = false;
            }
            this._isonlypreview = false;

        },
        _showEnd: function(){
            //this.domNode.style.width="300px";
            domClass.remove(this.domNode, "workbenchNaviClosed");
			if(!this._isonlypreview){
				setTimeout(lang.hitch(this._container, "layout"), 15);
			}else{
				this._previewShowing = true;
				this.resize();
			}
		}

    });
});