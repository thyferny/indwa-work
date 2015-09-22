/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: SampleSizeModelConfigHelper
 * Author: Will
 * Date: 12-7-12
 */
define(function(){
    var dlgId = "sampleSizeModelConfig";
    var okBtn = "sampleSize_cfg_btn_ok";
    var cancelBtn = "sampleSize_cfg_btn_cancel";
    var gridContainer = "sample_size_config_grid_container";
    var gridId = "sample_size_config_grid";
    var sampleSizeModel = null;  //sampleIdList sampleSizeList

    var sourceButtonId = null;

    dojo.ready(function(){
        dojo.connect(dijit.byId(dlgId),"onHide",_destroySomeObj);
        dojo.connect(dijit.byId(okBtn),"onClick",saveSampleSizeConfig);
        dojo.connect(dijit.byId(cancelBtn),"onClick",hidenSampleSizeModelConfigDlg);
    });

    function init(prop){
        sourceButtonId = getSourceButtonId(prop);
        createEmptyDataGrid();
        setGridRows();
    };

    function showSampleSizeModelConfigDlg(prop){
          var dlg = dijit.byId(dlgId);
        if(null!=dlg){
        	dlg.titleBar.style.display = "none";
            dlg.show();
        }
        init(prop);
    };

    function clonePorperty4SampleSizeModel(prop){
        sampleSizeModel = dojo.clone(prop.sampleSizeModelUI);
    };

    function createEmptyDataGrid(){
        var grid = dijit.byId(gridId);
        var items = [];
        if(sampleSizeModel!=null && sampleSizeModel.sampleSizeList!=null){
            for(var i=0;i<sampleSizeModel.sampleSizeList.length;i++){
                items.push({
                    id:i,
                    size:sampleSizeModel.sampleSizeList[i]
                })
            }
        }
        if(null==grid){
            var data = {
                identifier: 'id',
                items: items
            };
            var store = new dojo.data.ItemFileWriteStore({data: data});
            var column2Name  = "";
            if(dijit.byId("sampleSizeType__prop_form_value__Percentage").get("checked")==true){
                column2Name = alpine.nls.sample_size_config_grid_column_percent;
            }else{
                column2Name = alpine.nls.sample_size_config_grid_column_rownumber;
            }
            var layout = [[
                {'name': alpine.nls.sample_size_config_grid_column_1, 'field': 'id', 'width': '15%'},
                {'name': column2Name, 'field': 'size',editable:true,
                    type:dojox.grid.cells._Widget,
                    widgetClass: dijit.form.NumberTextBox,
                    widgetProps:{
                        required:false,
                        constraints:{min:0},
                        onBlur:function(){
                            if(this.value==null || isNaN(this.value)==true){
                                this.set('value',0);
                            }
                        }

                    },
                    width: '85%'}
            ]];

            var grid = new dojox.grid.DataGrid({
                    id: gridId,
                    store: store,
                    structure: layout,
                    selectionMode:"single",
                    style:"height:100%;with:100%"
                },
            dojo.create('div',null,dojo.byId(gridContainer)));
            grid.startup();
        }
    };

    function setGridRows(){
        var numStr =  getSampleNumberValue();
        var num = 0;
        if(null!=numStr){
            try{
                num = parseInt(numStr);
            }catch(e){}
            var items =[];
            if(null!=sampleSizeModel && null!=sampleSizeModel.sampleIdList && null!=sampleSizeModel.sampleSizeList && num==sampleSizeModel.sampleIdList.length &&  sampleSizeModel.sampleIdList.length==sampleSizeModel.sampleSizeList.length){
                for(var j=0;j<num;j++){
                    items.push({id:(j+1),size:sampleSizeModel.sampleSizeList[j]});
                }
            }else{
                for(var i=0;i<num;i++){
                    items.push({id:(i+1),size:""});
                }

            }

            var data = {
                identifier: 'id',
                items: items
            };
            var store = new dojo.data.ItemFileWriteStore({data: data});

            var grid = dijit.byId(gridId);
            if(grid!=null){
                grid.setStore(store);
                grid.render();
            }


        }
    };

    function getSampleSizeTypeValue(){
        var percentageId = "sampleSizeType"+ID_TAG+"Percentage";
        var rowId = "sampleSizeType"+ID_TAG+"ROW";

        if(dijit.byId(percentageId)!=null && dijit.byId(percentageId).get('checked')==true){
              return "percent";
        }
        if(dijit.byId(rowId)!=null && dijit.byId(rowId).get('checked')==true){
              return "row";
        }
        return null;
    };

    function getSampleNumberValue(){
        var sampleCountId = "sampleCount"+ID_TAG;
        if(dijit.byId(sampleCountId)!=null){
            return dijit.byId(sampleCountId).get('value');
        }else{
            return null;
        }
    };

    function clearSampleSizeModel(){
        if(null!=sampleSizeModel){
            sampleSizeModel.sampleIdList=[];
            sampleSizeModel.sampleSizeList=[];

            //update sampleModel btn status
            _setBtnStyleInvalid();
        }
    };

    function _setBtnStyleValid(){
        dijit.byId("sampleSize"+ID_TAG).set('baseClass', "workflowButton");;
        dijit.byId("sampleSize"+ID_TAG).focus();
    };

    function _setBtnStyleInvalid(){
        dijit.byId("sampleSize"+ID_TAG).set('baseClass', "workflowButtonInvalid");
        dijit.byId("sampleSize"+ID_TAG).focus();
    }

    function destroySampleSizeModel(){
        sampleSizeModel = null;
    };

    function getSampleSizeModel(){
        return sampleSizeModel;
    };

    function saveSampleSizeConfig(){
        var grid = dijit.byId(gridId);
        if(grid!=null){
         var dataStore = grid.store;
         if(dataStore!=null){
             var allItems = dataStore._arrayOfAllItems;
             if(_validateSampleSize(allItems)==false){
                 popupComponent.alert(alpine.nls.sample_size_config_error_tip_size_null);
                 return false;
             }
             if( (CurrentOperatorDTO.classname =="RandomSamplingOperator" || CurrentOperatorDTO.classname =="StratifiedSamplingOperator")
                  && _validatePercentValue(allItems)==false){
                 //popupComponent.alert(alpine.nls.sample_size_config_error_tip_percent_error_tip);
                 return false;
             }else if(CurrentOperatorDTO.classname =="HadoopRandomSamplingOperator" && _validate4HadoopRandomSamplingSizePercentValue(allItems)==false){
                 return false;
             }

             _saveSampleSizeModule(allItems);
             setButtonBaseClassValid(sourceButtonId);
         }


        }
        hidenSampleSizeModelConfigDlg();
    };

    function _validateSampleSize(items){
         var status = true;
         if(null!=items){
             for(var i=0;i<items.length;i++){
                 var sizeValue = items[i].size[0];
                 if(null==sizeValue || ""==sizeValue){
                     status =  false;
                     break;
                 }else if(parseFloat(sizeValue)<0){
                     status = false;
                     break;
                 }
             }

          }else{
             status = false;
         }
        return status;
    };

    function validateSampleSizeModelEmpty(){
        if(sampleSizeModel==null){

            return false;
        }
        if(sampleSizeModel.sampleSizeList==null){
           // popupComponent.alert(alpine.nls.sample_size_model_need_config);
            return false;
        }
        if(sampleSizeModel.sampleSizeList.length==0){
           // popupComponent.alert(alpine.nls.sample_size_model_need_config);
            return false;
        }
        for(var i=0;i<sampleSizeModel.sampleSizeList.length;i++){
            var sampleSize = sampleSizeModel.sampleSizeList[i];
            if(null==sampleSize || sampleSize=="" || sampleSize==0){
               // popupComponent.alert(alpine.nls.sample_size_model_need_config);
                return false;
            }
            /*else if(parseFloat(sampleSize)<0){
                return false;
            }*/
        }
        return true;
    }

    function _validatePercentValue(items){
        var percentSum = 0;
        if(getSampleSizeTypeValue()=="percent" && items!=null){
            var disJoint = "disjoint__prop_form_value__true";
            var replacement = "replacement__prop_form_value__true";
            if(CurrentOperatorDTO.classname=="RandomSamplingOperator" && dijit.byId(replacement).get("checked")==false){
                if((dijit.byId(disJoint).get("checked")==true)){
                    for(var i=0;i<items.length;i++){
                        var sizeValue = items[i].size[0];
                        if(null!=sizeValue || ""!=sizeValue){
                            percentSum =  percentSum + parseFloat(sizeValue);
                        }
                    }
                    if(percentSum>100){
                        popupComponent.alert(alpine.nls.sample_size_config_error_tip_percent_error_tip2);
                        return false;
                    }
                }else{
                    for(var i=0;i<items.length;i++){
                        var sizeValue = items[i].size[0];
                        if(null!=sizeValue || ""!=sizeValue){
                            if(parseFloat(sizeValue)>100){
                                popupComponent.alert(alpine.nls.sample_size_config_error_tip_percent_error_tip1);
                                return false;
                            }
                        }
                    }
                }
            }else if(CurrentOperatorDTO.classname=="StratifiedSamplingOperator"){
                if((dijit.byId(disJoint).get("checked")==true)){
                    for(var i=0;i<items.length;i++){
                        var sizeValue = items[i].size[0];
                        if(null!=sizeValue || ""!=sizeValue){
                            percentSum =  percentSum + parseFloat(sizeValue);
                        }
                    }
                    if(percentSum>100){
                        popupComponent.alert(alpine.nls.sample_size_config_error_tip_percent_error_tip2);
                        return false;
                    }
                }else{
                    for(var i=0;i<items.length;i++){
                        var sizeValue = items[i].size[0];
                        if(null!=sizeValue || ""!=sizeValue){
                            if(parseFloat(sizeValue)>100){
                                popupComponent.alert(alpine.nls.sample_size_config_error_tip_percent_error_tip1);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    };

    function _validate4HadoopRandomSamplingSizePercentValue(items){
        if(getSampleSizeTypeValue()=="percent" && items!=null){
            for(var i=0;i<items.length;i++){
                var itm = items[i];
                if(itm.size[0]>100){
                    popupComponent.alert(alpine.nls.sample_size_config_error_tip_percent_error_tip1);
                    return false;
                }
            }
        }
        return true;
    }

    function _saveSampleSizeModule(items){
        if(null==sampleSizeModel){
            sampleSizeModel={};
        }

        sampleSizeModel.sampleIdList = [];
        sampleSizeModel.sampleSizeList=[];

        if(null!=items){
            for(var i=0;i<items.length;i++){
                var sizeValue = items[i].size[0];
                sampleSizeModel.sampleIdList.push(i+1);
                sampleSizeModel.sampleSizeList.push(sizeValue);
            }
        }


    }

    function hidenSampleSizeModelConfigDlg(){
        var dlg = dijit.byId(dlgId);
        if(null!=dlg){
            dlg.hide();
        }
    };

    function _destroySomeObj(){
        var grid = dijit.byId(gridId);
        if(null!=grid){
            grid.destroyRecursive();
            grid = null;
        }

    }

    return {
        showSampleSizeModelConfigDlg:showSampleSizeModelConfigDlg,
        clonePorperty4SampleSizeModel:clonePorperty4SampleSizeModel,
        clearSampleSizeModel:clearSampleSizeModel,
        destroySampleSizeModel:destroySampleSizeModel,
        getSampleSizeModel:getSampleSizeModel,
        validateSampleSizeModelEmpty:validateSampleSizeModelEmpty
    }

});