/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * ImportFlowHelper
 * 
 * Author Will
 * Version 2.7
 * Date 2012-05-21
 */

define(["alpine/flow/ImportFlowManager",
        "alpine/flow/WorkFlowManager",
        "alpine/flow/WorkFlowUIHelper"],function(importFlowMgmt, workFlowManager, workFlowUIHelper){
	
	var div_import_flow_files = "div_import_flow_files";
	var validateFileList = [];
	var fileConter = 0;
	dojo.ready(function(){
		dojo.connect(dijit.byId('import_flow_button2'),"onClick",open_import_flow_dlg);
		//dojo.connect(dijit.byId('button_implort_flow_add_file'),"onClick",addUploadFiles);
		
		dojo.connect(dijit.byId('import_flow_dlg'),"onHide",closeImportFlowDlg);
		dojo.connect(dijit.byId('remove_last_import_flow'),"onClick",deleteSelectImportFile);
		dojo.connect(dijit.byId('btn_close_import_flow_dlg'),"onClick",close_import_flow_dlg);
		dojo.connect(dijit.byId('btn_doFlowUploadWithOpen'),"onClick",function(){
			doFlowUpload(1,true);
		});
		dojo.connect(dijit.byId('btn_doFlowUpload'),"onClick",function(){
			doFlowUpload(1,false);
		});
		
	});
	
	function registFileInputOnchangeEvent(fileInput){
		dojo.connect(fileInput,"onchange",function(){
			//validate file extension name
			if(this.value!=null){
				//alert("have valide value do not destory!");
				if(validFileName(this.value)==false){
                    this.value = "";
					popupComponent.alert(alpine.nls.MSG_Please_selectFlow_file);
					//dojo.destroy(fileInput);	
					return false;
				}else{
					//validate value
					//validate repeat data
					if(_hasRepeatData(this.value)==false && validateFileList.length<5){
                        var removeFakePath = this.value.replace("C:\\fakepath\\", ""); //don't display C:\fakepath\
						validateFileList.push({
                            uploadFileName_:this.value,
                            uploadFileNameZ_:removeFakePath
                        });
						updateFileuploadGrid();
						//move file input to 
						var fileInputsContainer = dojo.byId("div_import_flow_files");
						dojo.place(fileInput,fileInputsContainer,"last");
						
						var newfileInput = dojo.create("input",{type:"file",name:"uploadFile"+(fileConter++),title:"",style:"width:85px;height:35px;position: absolute;top: 5px;left: 0px;cursor: pointer;filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0; opacity: 0;"},dojo.byId('fileImportBtnContainer'));
                        if(dojo.isFF){
                            newfileInput.style.cssText = "width:85px;height:35px;position: absolute;cursor: pointer;top:-1px;left:-135px;filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0; opacity: 0;";
                        }
                        registFileInputOnchangeEvent(newfileInput);
					}else{
						//has repeat data
						if(validateFileList.length==5){
							popupComponent.alert(alpine.nls.import_file_maxfile_num);
						}else{
							popupComponent.alert(alpine.nls.import_file_error_tip_duplicate);
						}
                        /* destroy and recreate so onchange fires if picking the same file again (for instance after choose 5, then 6th, then delete 1, and choose 6th again */
						dojo.destroy(fileInput);
                        var newfileInput = dojo.create("input",{type:"file",name:"uploadFile"+(fileConter++),title:"",style:"width:85px;height:35px;position: absolute;top: 5px;left: 0px;cursor: pointer;filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0; opacity: 0;"},dojo.byId('fileImportBtnContainer'));
                        if(dojo.isFF){
                            newfileInput.style.cssText = "width:85px;height:35px;position: absolute;cursor: pointer;top:-1px;left:-135px;filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0; opacity: 0;";
                        }
                        registFileInputOnchangeEvent(newfileInput);
						return false;
					}
				}
				if(validateFileList.length == 1){
					dijit.byId("btn_doFlowUploadWithOpen").set("disabled", false);
				}else{
					dijit.byId("btn_doFlowUploadWithOpen").set("disabled", true);
				}
			}else{
				//
				popupComponent.alert(alpine.nls.MSG_Please_selectFlow_file);
				dojo.destroy(fileInput);
			}
		});
	}
	
	function open_import_flow_dlg(){
		
		validateFileList = [];
		dojo.empty(div_import_flow_files);
		buildFileUploadListGrid();
		
		var fileInput = dojo.create("input",{type:"file",name:"uploadFile"+(fileConter++),title:"",style:"width:85px;height:35px;position: absolute;top: 5px;left: 0px;cursor: pointer;filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0; opacity: 0;"},dojo.byId('fileImportBtnContainer'));
		if(dojo.isFF){
            fileInput.style.cssText = "width:85px;height:35px;position: absolute;cursor: pointer;top:-1px;left:-135px;filter:alpha(opacity=0);-moz-opacity:0;-khtml-opacity: 0; opacity: 0;";
        }
        registFileInputOnchangeEvent(fileInput);
		dijit.byId('import_flow_dlg').titleBar.style.display = "none";
		dijit.byId('import_flow_dlg').show();
		dojo.byId("import_flow_comments").value=" ";
		clear_import_flow_Selection();
		//add_more_import_flow();
		if(dojo.isFF){
			dojo.byId("import_flow_comments").rows=2;
			dojo.byId("import_flow_comments").cols=3;
			dojo.style("import_flow_comments_content", "height", "140px");
			dijit.byId("import_flow_container").layout();
		}else if (dojo.isIE){
			dojo.byId("import_flow_comments").rows=3;
			dojo.byId("import_flow_comments").cols=43;
			
			dojo.byId("import_flow_comments").cols=43;
			dojo.style("import_flow_comments_content", "height", "150px");
			dijit.byId("import_flow_container").layout();
		}
		else if(dojo.isWebKit){//safari
			dojo.byId("import_flow_comments").rows = 5;
		 
			dojo.style("import_flow_comments_content", "height", "160px");
			dojo.style("import_flow_comments", "width", "300px");
			dijit.byId("import_flow_container").layout();
			
		}
		dijit.byId("btn_doFlowUploadWithOpen").set("disabled", true);
	}
	/*
	function addUploadFiles(){
		cleanEmptyValueFileUploadInput();
		var fileInputsContainer = dojo.byId("div_import_flow_files");
		var fileInput = dojo.create("input",{type:"file"},fileInputsContainer);
		//var fileInput = dojo.create("input",{type:"file",style:"visibility:hidden",name:"file_"+(fileConter++)},fileInputsContainer);
		//fileInput.click();
		
		if(dojo.isIE){
		}else{
			fileInput.focus();
		}
		fileInput.click();
		
		fileInput.blur();
		
		if(dojo.isIE){
			if(fileInput.value!=null && ""!=fileInput.value){
				//alert("have valide value do not destory!");
				if(validFileName(fileInput.value)==false){
					popupComponent.alert(alpine.nls.MSG_Please_selectFlow_file);
					dojo.destroy(fileInput);	
					return false;
				}else{
					//validate value
					//validate repeat data	
					if(_hasRepeatData(fileInput.value)==false && validateFileList.length<5){
						validateFileList.push({uploadFileName:fileInput.value});
						updateFileuploadGrid();
					}else{
						//has repeat data
						if(validateFileList.length==5){
							popupComponent.alert(alpine.nls.import_file_maxfile_num);
						}else{
							popupComponent.alert(alpine.nls.import_file_error_tip_duplicate);
						}
						//alert('repeat data');
						dojo.destroy(fileInput);
						return false;
					}
					
				}
			}else{
				//
				popupComponent.alert(alpine.nls.MSG_Please_selectFlow_file);
				dojo.destroy(fileInput);
			}
		}else{
			
		}
		
	};
	*/
	
	function _hasRepeatData(newValue){
		if(null!=validateFileList && validateFileList.length>0){
			for ( var i = 0; i < validateFileList.length; i++) {
				if(validateFileList[i].uploadFileName_ == newValue){
				   return true;
				   //break;
				}
			}
		  
		}
		return false;
	}
	
	function cleanEmptyValueFileUploadInput(){
		var fileInputs =_getFileInputs();
		dojo.forEach(fileInputs,function(itm,idx){
		    if(null==itm.value || dojo.trim(itm.value)==""){
		    	dojo.destroy(itm);
		    }else if(validFileName(itm.value)==false){
		    	dojo.destroy(itm);
		    }
		});
		
	};
	
	function validFileName(inputValue){
		inputValue = dojo.trim(inputValue.substring(inputValue.lastIndexOf("."),inputValue.length));
		if(null!=inputValue && ".afm"==inputValue.toLowerCase()){			
			return true;
		}
		return false;
	};
	
	function _getFileInputs(){
		var uploadFileContainer = dojo.byId(div_import_flow_files);
		return dojo.query("input[type=file]",uploadFileContainer);
	};
	
	function buildFileUploadListGrid(){
		var gridStore = new dojo.data.ItemFileReadStore({
	    	data:{
			  	  identifier:"uploadFileName_",
			  	  items:[]
			      }
	         }); 
		var gridLayout =  [
		         	{type: "dojox.grid._CheckBoxSelector"},
		        	[
		        	 	{name:alpine.nls.Flow_Name, field: "uploadFileNameZ_",width:"100%"}
		            ]
			      ];
		var grid = dijit.byId("flow_import_grid");
		if(null==grid){
		var grid = new dojox.grid.DataGrid({
			id:'flow_import_grid',
			store:gridStore,
			structure:gridLayout,
			style:"heigth:150px;width:470px;",
			query: {"uploadFileName_": "*"}, //??
			onRowClick : function(event){
					   //this.selection.toggleSelect(event.rowIndex);
					   var inIndex = event.rowIndex;
					   // this.edit.rowClick(event);
					    this.selection._beginUpdate();
						this.selection.toggleSelect(inIndex);
						this.selection._endUpdate();
					   //var statusArray = grid.selection.selected;
			},
			canSort: function(){return false;}
			},dojo.create("div",{style:"height:150px;width:470px"},dojo.byId("flow_import_grid_container")));
		  grid.startup();
		}
	};
	function updateFileuploadGrid(){
		var gird = dijit.byId('flow_import_grid');
		  if(null!=gird){
			  var gridStore = new dojo.data.ItemFileReadStore({
				  data:{
				  identifier:"uploadFileName_",
				  items:validateFileList
			  }
			  }); 
			  gird.setStore(gridStore);
			  gird.render();
		  }
	};
	
	function closeImportFlowDlg(){
		validateFileList = [];
		fileConter = 0;
		openFileName = "";
		var grid = dijit.byId('flow_import_grid');
		  if(null!=grid){
			  grid.destroyRecursive();
			  grid = null;
		  }
	};
	
	function deleteSelectImportFile(){
		var grid = dijit.byId("flow_import_grid");
		if(null!=grid){
			var selectItem = grid.selection.getSelected();
			
			if(selectItem.length>0){
			  //clean div
			  //clean array
			 for ( var i = 0; i < selectItem.length; i++) {
				 if(null!=selectItem[i]){
					 _cleanFileInputFromDivAndArray(selectItem[i].uploadFileName_[0]);
				 }
			 }
			 updateFileuploadGrid();
				
			}else{
				popupComponent.alert(alpine.nls.import_file_delete_tip);
			}
			if(validateFileList.length == 1){
				dijit.byId("btn_doFlowUploadWithOpen").set("disabled", false);
			}else{
				dijit.byId("btn_doFlowUploadWithOpen").set("disabled", true);
			}
		}
	};
	
	function _cleanFileInputFromDivAndArray(fileName){
		if(null!=fileName){
			var fileInputs =_getFileInputs();
			if(null!=fileInputs && fileInputs.length>0){
			   for ( var i = 0; i < fileInputs.length; i++) {
				    if(fileInputs[i].value==fileName){
				    	dojo.destroy(fileInputs[i]);
				    	break;
				    }
			    }
			}
			if(null!=validateFileList && validateFileList.length>0){
				for ( var j = 0; j < validateFileList.length; j++) {
					if(validateFileList[j].uploadFileName_==fileName){
						validateFileList.splice(j,1);
					}
				}
			}
		}
	};
	
	function close_import_flow_dlg (){
		//reset it   		 
		dijit.byId('import_flow_dlg').hide();
	};
    var uploadFileName=null;
	function doFlowUpload(x,isOpen){
		
			var import_flow_comments = dojo.byId("import_flow_comments").value;
		 
			var allFileInput =_getFileInputs();
			if(allFileInput == null || allFileInput.length == 0){
				return;
			}
			
			var hasError=false;

			// run here, everything is fine, but names of import flow. So Just check names of import is same to current opened flow.
			for(var i=0;i<allFileInput.length;i++){
				var uploadFlowName = allFileInput[i].value; 
				if(dojo.isIE&&uploadFlowName.lastIndexOf("\\")>-1){
					uploadFlowName = uploadFlowName.substring(uploadFlowName.lastIndexOf("\\")+1);
				}
				openFileName = uploadFlowName;
				var flowInfoId = uploadFlowName.substring(0,uploadFlowName.length-4);
				//one of added flow is opened,so have to close it
				if (alpine.flow.WorkFlowManager.isEditing({
					id: flowInfoId,
					categories: ""
				})){
//					if(isOpen ==false){
						if(alpine.flow.WorkFlowManager.isDirty()){
							hasError = true;// if run here just confirm user if save old flow or not. And run import process.
							popupComponent.saveConfirm(alpine.nls.update_not_saved,{
								handle: function(){
									var saveFlowCallback = function(){
										importFlowHandle(openFileName);
										alpine.flow.WorkFlowUIHelper.release();
									};
									//save_flow();
                                    alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
								}
							},{
								handle: function(){
									// just clean screen, if click button with name is abort.
									alpine.flow.WorkFlowUIHelper.release();
									importFlowHandle(openFileName);
								}
							});
						}else{
							//popupComponent.alert("Current opened flow data will be changed.System will close it");
							alpine.flow.WorkFlowUIHelper.release();
						}
//					}
				}
			}
			
			if(hasError==false){
				importFlowHandle(openFileName);
			}
			//import flow handle
			function importFlowHandle(openFileName){
				dijit.byId("import_flow_dlg").hide();
				if(!import_flow_comments||import_flow_comments == ""){
					import_flow_comments = " " ;
				}
                var  url =baseURL+"/main/flow/import_flow.do?method=importFlow" + "&user=" + login +"&comments="+import_flow_comments;
                ds.upload(url, "frmIO_flow", function(data)
                {
                    if(data.error_code){
                        //hide_import_flow_progress_bar();
                        handle_error_result(data);
                        return ;
                    }
                    else{
                        //no need to refreh , because the flow list will refresh each time it is opened
                        //	popupComponent.alert("Flow import successful.");
                        //MINERWEB-271 	[the button of import don't work] import flow don't reaction
                        if(isOpen==true){
                            if (alpine.flow.WorkFlowManager.isDirty()){
                                //hide_import_flow_progress_bar();
                                //confirm if current flow is modified, but not in the import list.
                                popupComponent.saveConfirm(alpine.nls.update_not_saved,{
                                    handle: function(){
                                        var saveFlowCallback = function(){
                                            alpine.flow.WorkFlowUIHelper.release();
                                            openImpportFlow(openFileName);
                                        };
                                        //save_flow();
                                        alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
                                    }
                                },{
                                    handle: function(){
                                        //if abort current flow, need cancle it first.

                                        var url = flowBaseURL + "?method=cancelUpdate" + "&user=" + login;
                                        error_msg = "";
                                        ds.post(url, workFlowManager.getEditingFlow(), null, null);
                                        alpine.flow.WorkFlowUIHelper.release();
                                        dijit.byId("cancel_flow_button").set("disabled", true);
                                        alpine.flow.WorkFlowUIHelper.setDirty(false);
//											current_flow_id = null;
                                        alpine.flow.WorkFlowManager.storeEditingFlow(null);
                                        alpine.flow.WorkFlowUIHelper.setEditingFlowLabel("");
                                        //then open it...
                                        openImpportFlow(openFileName);
                                    }
                                });

//										if(confirm(alpine.nls.update_not_saved) == true) {
//											clear_flow_display("Personal");
//
//										}else{//upload ok, will not open it...
//											hide_import_flow_progress_bar();
//											return;
//										}
                            }else{
                                alpine.flow.WorkFlowUIHelper.release();
                                //alert(openFileName);
                                openImpportFlow(openFileName);
                            }
                        }
                        else{
//									build_personal_flow_tree();
//									dijit.byId("open_flow_button").openDropDown();
                            alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree();
                            //hide_import_flow_progress_bar();
                        }
                    }

                }, null, null, x);
				//show_import_flow_progress_bar();
//				var td = dojo.io.iframe.send({
//					url:encodeURI(url) ,
//					form: "frmIO_flow",
//					method: "post",
//					content: {fnx:x},
//					timeoutSeconds: 60,
//					preventCache: true,
//					handleAs: "html",
//					handle: function(res, ioArgs){
// 						if(res&&res.body&&res.body.innerHTML){
//                            var data = eval("(" + res.body.innerHTML + ")");//res.body.innerHTML.evalJSON();
//							if(data.error_code){
//								//hide_import_flow_progress_bar();
//								handle_error_result(data);
//								return ;
//							}
//							else{
//								//no need to refreh , because the flow list will refresh each time it is opened
//							//	popupComponent.alert("Flow import successful.");
//								//MINERWEB-271 	[the button of import don't work] import flow don't reaction
//								if(isOpen==true){
//									if (alpine.flow.WorkFlowManager.isDirty()){
//										//hide_import_flow_progress_bar();
//										//confirm if current flow is modified, but not in the import list.
//										popupComponent.saveConfirm(alpine.nls.update_not_saved,{
//											handle: function(){
//												var saveFlowCallback = function(){
//													alpine.flow.WorkFlowUIHelper.release();
//													openImpportFlow(openFileName);
//												};
//												//save_flow();
//                                                alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
//											}
//										},{
//											handle: function(){
//												//if abort current flow, need cancle it first.
//
//											var url = flowBaseURL + "?method=cancelUpdate" + "&user=" + login;
//											error_msg = "";
//											ds.post(url, workFlowManager.getEditingFlow(), null, null);
//											alpine.flow.WorkFlowUIHelper.release();
//											dijit.byId("cancel_flow_button").set("disabled", true);
//											alpine.flow.WorkFlowUIHelper.setDirty(false);
////											current_flow_id = null;
//											alpine.flow.WorkFlowManager.storeEditingFlow(null);
//											alpine.flow.WorkFlowUIHelper.setEditingFlowLabel("");
//											//then open it...
//											openImpportFlow(openFileName);
//											}
//										});
//
////										if(confirm(alpine.nls.update_not_saved) == true) {
////											clear_flow_display("Personal");
////
////										}else{//upload ok, will not open it...
////											hide_import_flow_progress_bar();
////											return;
////										}
//									}else{
//										alpine.flow.WorkFlowUIHelper.release();
//										//alert(openFileName);
//										openImpportFlow(openFileName);
//									}
//								}
//								else{
////									build_personal_flow_tree();
////									dijit.byId("open_flow_button").openDropDown();
//									alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree();
//									//hide_import_flow_progress_bar();
//								}
//							}
//						}
//
//					},
//					error: function (res,ioArgs) {
//						//hide_import_flow_progress_bar();
//						popupComponent.alert(alpine.nls.MSG_Upload_Error+res);
//					}
//				});
				
				function openImpportFlow(uploadFileName){
					alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree();
					
					//find the flow info
					if(dojo.isIE&&uploadFileName.lastIndexOf("\\")>-1){
						uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
					}
					var flowName =uploadFileName.substring(0,uploadFileName.length-4);
					if(dojo.isSafari || dojo.isChrome){
						flowName = flowName.substring(flowName.lastIndexOf("\\") + 1, flowName.length);
					}
					//alert(uploadFileName);
					//alert(flowName);
					//replace the blank..
					flowName=flowName.replace(/\ /g,"_");
					alpine.flow.FlowCategoryUIHelper.fetchFlowInfoByName(flowName, function(items){
						for(var i=0;i<items.length;i++){
							var flowInfo =items[i] ;
							if(flowInfo.id[0].toUpperCase() == flowName.toUpperCase()
									&& !flowInfo.categories){
								
								workFlowUIHelper.openWorkFlow(flowInfo);
//								hide_import_flow_progress_bar();
								break;
							
							}
						}
					});
					
					//open it 
//					dijit.byId("btn_tree_delete_flow").set("disabled",true);
//					dijit.byId("btn_tree_history_flow").set("disabled",true);
//					var url = flowBaseURL + "?method=getFlows"
//					+ "&user=" + login 	+ "&type=" + "Personal";
//					var callback =  flow_tree_personal_cb_add;
//					ds.get(url, callback);
				}
			}
		}
    
	
	
});