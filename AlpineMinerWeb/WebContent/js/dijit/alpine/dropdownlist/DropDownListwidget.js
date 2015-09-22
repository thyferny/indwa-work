dojo.provide("dijit.alpine.dropdownlist.DropDownListwidget");

//dijit\alpine\dropdownlist

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

//create widget
dojo.declare(
"dijit.alpine.dropdownlist.DropDownListwidget",[dijit._Widget, dijit._Templated],{

	templatePath:dojo.moduleUrl("dijit.alpine.dropdownlist","DropDownList_templates.html"),

	domNode:this.domNode,

	baseClass: "dropDownList",
	
	//delete btn call web service
	deleteBtnCallService:null,		
	
	pointArrowIconClickHandler:null,
	
	enable:true,
	
	_currentContentValue:"",
	
    deleteJarCallURL:"",
	
	postCreate:function(){
	 this.inherited(arguments);
	 //console.log("DropDownListwidget postCreate...");
     
	 //set dom  seeming
	 this._setDOMWidth.call(this, this.domNode);
	 
	 //regist dom event
	 this.pointArrowIconClickHandler = dojo.connect(this.pointArrowIcon,"click",this,function(event){
		if(this.pointDropDownListContainern){
			this._reSetDOMStyle();
			this.pointDropDownListContainern.style.display = "block";
		}
		this._stopBubble(event);
	 });

	

	//hide list item
	dojo.connect(document,"click",this,function(event){
		var container = this.pointDropDownListContainern;
	    var domtarget = event.target||event.srcElement;
		if(container && container.style.display!=undefined && container.style.display=="block" && domtarget && domtarget.id!=undefined){
		  if(!dojo.hasClass(domtarget,"preventListHide")){
			  container.style.display = "none";
		  }
		}
		this._stopBubble(event);
	});
	
    },
	startup:function(){
	   this.inherited(arguments);
	   //console.log("DropDownListwidget startup...");   
	  
    },
    _stopBubble:function(event){
    	if(event&&event.stopPropagation){
			event.stopPropagation();
		}else{
		     window.event.cancelBubble=true;
		}
    
    },
    _stopDefault:function(event){
    	event.preventDefault ? event.preventDefault() : event.returnValue = false;
    },
    _setDOMWidth:function(wigtnode){
    	//set reasonable width and background
    	if(wigtnode){
    		if(wigtnode.style && wigtnode.style.width){
    			this.currentContent.style.width=wigtnode.style.width;
    			this.pointArrowIcon.style.right = "10px";
    			this.pointDropDownListContainern.style.width=wigtnode.style.width;
    		}
    		if(wigtnode.style && wigtnode.style.background){
    			this.currentContent.style.background=wigtnode.style.background;
    		}
    	}
    }
    ,
    buildDropDownListItem:function(listItem,userName){
    	var _that = this;
    	if(listItem && listItem.length!=undefined && listItem.length>0){
    		var dropDownList = this.dropDownList;
    		if(dropDownList){
    			dropDownList.innerHTML="";
    			if(listItem.length!=undefined && listItem.length>0){
    				//set dropDownListContainer height
    				var height = listItem.length*27;
    				_that.pointDropDownListContainern.style.height=height+"px";
    			   
    			}else{
    				return ;
    			}
    			for ( var len = 0; len < listItem.length; len++) {
    				var dropDownListItem = dojo.create("div",null,dropDownList);
    				dojo.addClass(dropDownListItem,"drop-down-list-item preventListHide");
    				//<a href="#" class="listItemVal">a link</a><span class="itemDeleteIcon preventListHide" value=""></span>
    				var listItemVal = dojo.create("a",{href:"#",innerHTML:((listItem[len].length>22)?(listItem[len].substring(0,22)+"..."):(listItem[len])),title:listItem[len]},dropDownListItem);
    				dojo.addClass(listItemVal,"listItemVal");
    				if(userName!=null && "ADMIN"==userName.toUpperCase()){
    					var itemDeleteIcon = dojo.create("span",null,dropDownListItem);
    					dojo.addClass(itemDeleteIcon,"itemDeleteIcon preventListHide");    
    				}
    			}
    		 
    		 //regist item click
    		 dojo.query(".listItemVal",dropDownList).connect("click",function(event){
    			    _that._stopDefault(event);
    			    _that._stopBubble(event);
    				if(_that.currentContentValue){
    					_that.setCurrentContentValue(this.title);
    					_that.pointDropDownListContainern.style.display = "none";
    				}
    		 });
    		 if(userName!=null && "ADMIN"==userName.toUpperCase()){
    			 dojo.query(".itemDeleteIcon",dropDownList).connect("click",function(event){
    				 _that._stopDefault(event);
    				 _that._stopBubble(event);
    				 //console.log("delete btn click....");
    				 //do delete JDBC jar file
    				 var jarName = this.previousSibling.innerHTML;
    				 var listItem = this.parentNode;
    				 if(null!=jarName && ""!=jarName && _that.deleteBtnCallService!=null){
    					 var url = _that.deleteJarCallURL+"&driverFileName="+jarName;
    					 popupComponent.confirm(alpine.nls.DB_JDBC_DELETE_CONFIRM,{
    						 handle: function(){
    						     progressBar.showLoadingBar();
        						   _that.deleteBtnCallService(url,function(obj){
        							 //remove item        							
            					   _that._deleteBtnCallServiceCallBack.call(_that,obj,listItem,jarName);
            					 });
        					 }
    					 });
    				 }
    			 });
    		 }
    			
    		}
    	}
    },
    isValid:function(){
    	if(!this.enable){
    		return true;
    	}
    	if(!this.currentContentValue || !this.currentContentValue.innerHTML==null || ""==this.currentContentValue.innerHTML){
    		this._setInvalidDOMStyle();
    		return false;
    	}
    	this._reSetDOMStyle();
    	return true;
    },
    _setInvalidDOMStyle:function(){
    	if(this.currentContent){
    		this.currentContent.style.background = "#F9F7BA";
    		this.currentContent.style.border ="1px solid #F3D118";
    	}
    	
    },
    _setDisEnableDOMStyle:function(){
    	if(this.currentContent){
    		this.currentContent.style.background = "#EEF3FA";
    		this.currentContent.style.border ="1px solid #D5D6EB";
    	}
    	
    },
    _reSetDOMStyle:function(){
    	this.currentContent.style.background = this.domNode.style.background;  	
    	this.currentContent.style.border = "1px solid #7f9db9";  	
    },
    setDelBtnCallWebService:function(callfunc){
       if(callfunc && callfunc instanceof Function){
    	this.deleteBtnCallService = callfunc;
       }
    },
    _deleteBtnCallServiceCallBack:function(obj,listItem,jarName){
    	 progressBar.closeLoadingBar();
        if(obj!=null && obj.message!=null && obj.message!="success"){
        	popupComponent.alert(obj.message);
        	return;
        }
        //delete success 
        if(null!=listItem){
        	this.dropDownList.removeChild(listItem);
       	    //selected not save: clear selected
			if(this.currentContentValue.innerHTML==jarName){
				this.setCurrentContentValue("");
			 }
        	var listHeight = parseInt(this.pointDropDownListContainern.style.height);
        	if(listHeight!=undefined && listHeight>28){
        		this.pointDropDownListContainern.style.height = (listHeight-28)+"px";
        	}
        	this.pointDropDownListContainern.style.display = "none";
        	
        }
    },
    confirmCurrentValueInItemList:function(currentVal){
    	var iscontainVal = false;
    	var listItem = dojo.query(".listItemVal",this.domNode);
    	for ( var len = 0; len < listItem.length; len++) {
			if(listItem[len].title==currentVal){
				iscontainVal = true;
				this.setCurrentContentValue(currentVal);
				break;
			}
		}    	
    	if(!iscontainVal){
    		this.setCurrentContentValue("");
    	}
    	
    },
    setCurrentContentValue:function(value){
    	this._currentContentValue = value;
    	//set html display
    	if(undefined!=value && value.length!=undefined && value.length>26){
    		this.currentContentValue.innerHTML = value.substring(0,22)+"...";
    		this.currentContentValue.title = value ;
    	}else{
    		this.currentContentValue.innerHTML = value;
    		
    	}
    	
    },
    getCurrentContentValue:function(){
    	return this._currentContentValue!=undefined?this._currentContentValue:"";
    },
    setDropDownEnable:function(enable){
    	if(enable){
    	  this.enable = enable;
    	  if(this.pointArrowIconClickHandler!=null){
    		  dojo.disconnect(this.pointArrowIconClickHandler);
    		  this.pointArrowIconClickHandler = dojo.connect(this.pointArrowIcon,"click",this,function(event){
    			  if(this.pointDropDownListContainern){
    				  this._reSetDOMStyle();
    				  this.pointDropDownListContainern.style.display = "block";
    			  }
    			  this._stopBubble(event);
    		  });
    	  }
    	}else{
    		this.enable = enable;
    		if(this.pointArrowIconClickHandler!=null){
      		  dojo.disconnect(this.pointArrowIconClickHandler);
      		  //set dom style disenable
      		  this._setDisEnableDOMStyle();
      	   }
    	}    	
    }
    
});