/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * File: ColumnSelect
 * Author: robbie
 * Date: 12-12-12
 */

define([
    "dojo/ready",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/query",
    "dojo/on",
    "dojo/_base/declare",
    "dojo/_base/lang",
    "dojo/_base/array",
    "dojo/store/Memory",
    "dojo/data/ObjectStore",
    "dijit/registry",
    "dojox/grid/EnhancedGrid"
],function(ready, dom, domConstruct, domClass, query, on, declare, lang, array, Memory, ObjectStore, registry, EnhancedGrid){

    return declare([],{

        dialogId: "columnSelectionDialog",

        tableContainer: "columnDialogTableHolder",

        gridId: "colNamesItemGrid4Property",

        filterId: "dataexplorer_columns_filter_for_property",

        dataItems: [],

        selectedItems: [],

        requiredCols: 0,

        allButton: "column_selectall",

        noneButton: "column_selectnone",

        okButton: "btn_ok_4_property_columnName_select",

        cancelButton: "btn_cancel_4_property_columnName_select",

        widgetTracker: [],

        listeners: [],

        fields: 0,

        selAll: 0,

        selNone: 0,
        
        validate: function(){return true;},

        constructor: function(args) {
            //console.log("-----ColumnSelect.js constructor-----");
            this.clean();
            lang.mixin(this, args);
            this.registerListeners();
            registry.byId(this.dialogId).show();
            this._addToDialog();
        },

        okButtonFn: function() {},

        cancelButtonFn: function() {},

        _addToDialog: function() {
            //console.log("------ _addToDialog --------");

            var structure = this._getStructure();
            var store = this._getStore(this.dataItems);
            var grid = new EnhancedGrid({
                    id: this.gridId,
                    store: store,
                    structure: structure,
                    style:"height:100%;width:100%;",
                    query: {"colName": "*"},
                    queryOptions: {ignoreCase:true},
                    rowsPerPage:5000,
                    keepSelection: true,
                    onSelectionChanged: lang.hitch(this, this._selectionChanged),
                    onSelected: lang.hitch(this, this._onSelected),
                    onDeselected: lang.hitch(this, this._onDeselected),
                    canSort: function(){return false;},
                    plugins: {
                        indirectSelection: {
                            headerSelector:false,
                            width:"18px",
                            styles:"text-align: center;",
                            _onClick: function(e){
                                this._selectRow(e);
                            }
                        }
                    }
                }, domConstruct.create("div",{style:"height:100%;"},dom.byId(this.tableContainer))
            );
            grid.startup();
            //console.log("--- find selected cols ---");
            if (this.selectedItems.length > 0) {
                this._applySelection(this.selectedItems);
            } else {
                this._selectionChanged(); // if nothing is selected, confirm ok btn status
            }
        },

        clean: function() {
            //console.log("--- clean ---")
            //destroy grid
            if(registry.byId(this.gridId)) {
                registry.byId(this.gridId).destroyRecursive();
            }

            //remove listeners
            array.forEach(this.listeners, function(handle){
                handle.remove();
            });
            this.listeners = [];

            //clean filter
            registry.byId(this.filterId).set('value',"");

            //custom clean
            this._customClean();
        },

        _customClean: function () {
            /* fill me with lang.mixin! */
        },

        _addListener: function(handle) {
            this.listeners.push(handle);
        },

        registerListeners: function() {
            var handle;
            if(this.allButton) {
                handle = on(registry.byId(this.allButton), "click", lang.hitch(this, this._selectAll));
                this._addListener(handle);
            }
            if(this.noneButton) {
                handle = on(registry.byId(this.noneButton), "click", lang.hitch(this, this._selectNone));
                this._addListener(handle);
            }
            if(this.filterId) {
                handle = on(registry.byId(this.filterId), "change", lang.hitch(this, this._filterGrid));
                this._addListener(handle);
            }
            handle = on(registry.byId(this.okButton), "click", lang.hitch(this, this._saveAndHideDialog));
            this._addListener(handle);
            handle = on(registry.byId(this.cancelButton), "click", lang.hitch(this, this._hideDialog));
            this._addListener(handle);
            if (registry.byId(this.dialogId)) {
                handle = on(registry.byId(this.dialogId), "hide", lang.hitch(this,this.clean));
                this._addListener(handle);
            }
        },

        _applySelection: function(selectedItems) {
            //console.log("apply selection");
            var grid = registry.byId(this.gridId);
            //var allItems = grid.store.objectStore.data;
            for (var k=0; k<this.dataItems.length; k++) {
                if (array.indexOf(selectedItems,this.dataItems[k].colName) != -1) {
                    grid.rowSelectCell.toggleRow(k,true);
                }
            }
        },

        _selectAll: function() {
            //console.log("========select all=========");
            var filterBox = registry.byId(this.filterId);
            if (filterBox.get('value')) {
                //this.selAll = 1; //this will trigger toggleAllSelection after the filter is reset
                //filterBox.set('value',"");
                this._selectAllFilterData();
            } else {
                this._toggleAllSection(true);
            }
        },

        _selectNone: function() {
            //console.log("========select none=========");
            var filterBox = registry.byId(this.filterId);
            if (filterBox.get('value')) {
                this.selNone = 1; //this will trigger toggleAllSelection after the filter is reset
                filterBox.set('value',"");
            } else {
                this._toggleAllSection(false);
            }
        },

        _toggleAllSection: function(allOrNone) {
            registry.byId(this.gridId).rowSelectCell.toggleAllSelection(allOrNone);
        },

        //select all for filter data
        _selectAllFilterData: function(){
            var grid = registry.byId(this.gridId);
            if(grid!=null){
               var fileterItems  = grid._by_idx;
              if(fileterItems!=null && fileterItems.length>0){
                  for(var i=0;i<fileterItems.length;i++){
                      var index = grid.getItemIndex(fileterItems[i].item);
                      grid.selection.setSelected(index,true);
                  }
              }
            }
        },

        _selectionChanged: function() {
            //console.log("--- ColumnSelect _selectionChanged ---");
            if (this.fields >= this.requiredCols) {
                this._disableOkButton(false);
            } else {
                this._disableOkButton(true);
            }
        },

        _onSelected: function() {
            //console.log("--- _onSelected ---");
            this.fields++;
        },

        _onDeselected: function() {
            //console.log("--- _onDeselected ---");
            if (this.fields > 0) {this.fields--;}
        },

        _disableOkButton: function(disabled) {
            registry.byId(this.okButton).set('disabled',disabled);
        },

        _filterGrid: function(inputValue) {
            //console.log("----- filter grid -----");
            if (registry.byId(this.gridId)) {
                registry.byId(this.gridId).filter({
                    "colName": inputValue + "*"
                }, true);
            }
            if (this.selAll) {
                this._toggleAllSection(true);
                this.selAll = 0;
            } else if (this.selNone) {
                this._toggleAllSection(false);
                this.selNone = 0;
            }
        },

        _returnSelected: function() {
            //console.log("------ returnSelected ------");
            this._filterGrid(""); // must clear the filter to get all selected
            var selectedObjects = registry.byId(this.gridId).selection.getSelected();
            var colArray = [];
            array.forEach(selectedObjects, function(obj){
                colArray.push(obj.colName);
            });
            return colArray;
        },

        _getStructure: function() {
            return [[{name:"", field: "colName", width: "100%"}]];
        },

        _getStore: function(dataItems) {
            if(null==dataItems){ dataItems = []; }
            return new ObjectStore({ objectStore:new Memory({data:dataItems}) });
        },

        _saveAndHideDialog: function() {
        	if(!this.validate(this._returnSelected())){
        		return;
        	}
            this.okButtonFn(this._returnSelected());
            registry.byId(this.dialogId).hide();
        },

        _hideDialog: function() {
            this.cancelButtonFn();
            registry.byId(this.dialogId).hide();
        }

    });

});