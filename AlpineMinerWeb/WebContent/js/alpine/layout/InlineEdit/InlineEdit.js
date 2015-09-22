/**
 * User: robbie
 * Date: 10/30/12
 * (c) Alpine Data Labs 2012
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
    "dijit/registry",
    "dijit/form/Button"
],
    function(ready, dom, domConstruct, domClass, query, on, declare, lang, array, Memory, registry, Button) {

        return declare([],{

            /*
             * required mixins:
             * @mixin: tableContainer
             * @mixin: defaultItemValues -or- _getDefaultItem
             * @mixin: _fillNewRow
             *
             * */

            /*
             * recommended mixins:
             * @mixin: newRowButtonId -or- registerListeners
             *
             * */

            tableContainer: "",

            defaultItemValues: {},

            newRowButtonId: "",

            /*
             * dom Placement
             * "first", "last"
             */
            rowPlacement: "first",

            tableBody: "",

            tableRow: "",

            UIDS: {
                DEL: "deleteCol"
            },

            suffix: {
                body: "Body",
                row: "_row_"
            },

            newCounter: 2000,

            widgetTracker: [],

            listeners: [],


            constructor: function (args) {
                lang.mixin(this, args);
                this.clean();
                this.registerListeners();
            },

            /*
             * registerListeners()
             * by default, connects _addRow to mixed in constants.BTN_ADD_NEW
             * if more listeners needed, mixin a new registerListeners
             * this is not called automatically, should be called manually after mixin constants/registerListeners
             *
             * */
            registerListeners: function() {
                if (this.newRowButtonId) {
                    var handle = on(registry.byId(this.newRowButtonId), "click", lang.hitch(this, this._addRow));
                    this.listeners.push(handle);
                }
            },

            /*
             * _createTable(headerInfo, dataItems)
             * Builds the Inline Edit table
             * @param headerInfo: array of objects containing properties of the header tds - eg. innerHTML
             * @param dataItems: array of objects for each row (already configured row properties)
             * */
            createTable: function (headerInfo, dataItems) {
                this.tableBody = this.tableContainer + this.suffix.body;
                this.tableRow = this.tableContainer + this.suffix.row;
                var table = domConstruct.create("table",{className:"inlineEditTable", style:"width:95%;"},this.tableContainer);
                this._createTableHeader(table,headerInfo);
                var tableBodyDom = domConstruct.create("tbody",{id:this.tableBody ,className:"inlineEditTableBody"},table);
                this._createRows(tableBodyDom, dataItems);
            },

            /*
             * _createTableHeader(table,headerInfo)
             * called by _createTable
             * @param table
             * @param headerInfo
             *
             * */
            _createTableHeader: function (table,headerInfo) {
                var thead = domConstruct.create("thead",{className:"inlineEditTableHead"},table);
                for (var i=0;i<headerInfo.length;i++) {
                    domConstruct.create("td",headerInfo[i],thead);
                }
            },

            /*
             *  _createRows(tableBodyDom, items)
             *  called by _createTable
             *  @param tableBodyDom: tbody dom
             *  @param items: array or objects for each row
             *
             * */
            _createRows: function (tableBodyDom, items) {
                var item, row;
                if (!items.length || items.length == 0) {
                    this._addRow();
                } else {
                    for (var i=0;i<items.length;i++) {
                        item = items[i];
                        row = domConstruct.create("tr",{id:this.tableRow+i},tableBodyDom);
                        this._fillNewRow(item,row,i);
                    }
                }
            },

            /*
             *  _returnRows()
             *  called by _createTable
             *  @param tableBodyDom: tbody dom
             *  @param items: array or objects for each row
             *
             * */
            returnRows: function () {
                return query('tr',dom.byId(this.tableBody));
            },

            /*
             *  _putWidgetInCol(row,widget)
             *  utility for putting a widget in a td in a row
             *  @param row: row dom to place widget
             *  @param widget: widget to place
             *
             * */
            putWidgetInCol: function (row,widget) {
                var td = domConstruct.create("td", {}, row);
                domConstruct.place(widget.domNode,td,"only");
            },

            /*
             *  _putDomInCol(row,domNode)
             *  utility for putting a domNode in a td in a row
             *  @param row: row dom to place widget
             *  @param domNode: domNode to place
             *
             * */
            putDomInCol: function (row,domNode) {
                var td = domConstruct.create("td", {}, row);
                domConstruct.place(domNode,td,"only");
            },

            /*
             *  addToTracker(widget)
             *  utility for putting a widget in the Tracker
             *  @param widget: widget to place
             *
             * */
            addToTracker: function (widget) {
                this.widgetTracker.push(widget);
            },

            /*
             *  addToListeners(handle)
             *  utility for putting dojo/on handles in a tracker
             *  @param handle: result of an 'on' statement
             *
             * */
            addToListeners: function (handle) {
                this.listeners.push(handle);
            },
            /*
             *  _deleteRow(rowIndex)
             *  utility to delete row from on click of inline delete button
             *  @param rowIndex: row to delete by id index
             *
             * */
            _deleteRow: function (rowIndex) {
                array.forEach(registry.findWidgets(dom.byId(this.tableRow + rowIndex)), function(widget) {
                    widget.destroyRecursive();
                });
                domConstruct.destroy(dom.byId(this.tableRow + rowIndex));
            },

            /*
             *  _addRow()
             *  utility to add a new row to the top of the editing table
             *  mixin in new rowPlacement to control row placement (likely "last" or a number)
             *  see pos: http://dojotoolkit.org/reference-guide/1.7/dojo/place.html#dojo-place
             *  calls _getDefaultItem and _fillNewRow
             *
             * */
            _addRow: function () {
                var row = domConstruct.create("tr",{id:this.tableRow+this.newCounter},dom.byId(this.tableBody),this.rowPlacement);
                var item = this._getDefaultItem();
                this._fillNewRow(item,row,this.newCounter);
                this.newCounter++;
            },

            /*
             *  _getDefaultItem
             *  utility to retrieve an object for filling a new row
             *  mixin defaultItemValues or _getDefaultItem
             *
             * */
            _getDefaultItem: function () {
                return this.defaultItemValues;
            },

            /*
             *  _createDeleteButton(row,rowIndex)
             *  for histogram - create delete row columns
             *  useful in general for other operators
             *  @param row: row dom to place delete button widget
             *  @param rowIndex: rowIndex for onClick delete row
             *	@param onClick: callback on click button
             * */
            createDeleteButton: function (row,rowIndex, onClick) {
                var inlineEdit = this;
                var deleteBtn = new Button({
                    uniqueType: this.UIDS.DEL,
                    label: alpine.nls.inline_edit_delete_row,
                    baseClass: "linkButton",
                    tabIndex: "-1",
                    onClick: function() {
                    	if(onClick){
                    		onClick.call(this, row, rowIndex);
                    	}
                        inlineEdit._deleteRow(rowIndex);
                    }
                });
                this.putWidgetInCol(row,deleteBtn);
            },

            /*
             *  _clean
             *  utility to empty the table container and destroy any widgets in widgetTracker
             *  for other custom cleaning mixin _customClean
             *
             * */
            clean: function () {
                array.forEach(registry.findWidgets(dom.byId(this.tableContainer)),function(widget){
                    widget.destroyRecursive();
                });
                domConstruct.empty(dom.byId(this.tableContainer));
                array.forEach(this.widgetTracker, function(widget){
                    widget.destroyRecursive();
                });
                this.widgetTracker = [];
                array.forEach(this.listeners, function(handle){
                    handle.remove();
                });
                this.listeners = [];

                this.newCounter = 2000;

                this._customClean();
            },


            /*
             *  _fillNewRow(item, row, rowIndex)
             *  tell _addRow how to create a new row, e.g. a single function for each column type
             *  @param item: row property object
             *  @param row: row dom
             *  @param rowIndex: unique row id
             *
             * */
            _fillNewRow: function (item, row, rowIndex) {
                /* fill me with lang.mixin! */
            },

            _customClean: function () {
                /* fill me with lang.mixin! */
            },

            rebuildRows: function (rowObjects) {
                array.forEach(registry.findWidgets(dom.byId(this.tableBody)),function(widget){
                    widget.destroyRecursive();
                });
                domConstruct.empty(dom.byId(this.tableBody));
                this._createRows(dom.byId(this.tableBody),rowObjects);

            }

        });

    });
