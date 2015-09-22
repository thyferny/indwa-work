/**
 * User: robbie
 * Date: 10/13/12
 * (c) Alpine Data Labs 2012
 */

define([
    "dojo/_base/declare",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
    "dijit/_WidgetsInTemplateMixin",
    "alpine/flow/WorkFlowVariableReplacer",
    "dojo/text!alpine/layout/HistogramTypeWidget/HistogramTypeWidget.html"],
    function(declare, WidgetBase, TemplatedMixin, WidgetsInTemplateMixin, workFlowVariableReplacer, template){

        return declare([WidgetBase, TemplatedMixin, WidgetsInTemplateMixin],{

            templateString: template,

            I18N: {
                BIN_NUM: '',
                BIN_WIDTH: '',
                OR: ''
            },

            binWidth: "",

            binNumber: "",

            binType: "",

            buildRendering: function() {
                /* Set for internationalization before rendering */
                this.I18N = {
                    BIN_NUM: alpine.nls.histogram_widget_binnumber,
                    BIN_WIDTH: alpine.nls.histogram_widget_binwidth,
                    OR: alpine.nls.histogram_widget_or
                };
                this.inherited(arguments);
            },

            postCreate: function() {
                this._setDisplayValue();

                /* For Bin Width TextBox */
                this.connect(this.binWidthTextBox,"onKeyUp",function(e){
                    var value = this.binWidthTextBox.get('value');
                    this.binType = 1;
                    this.binWidth = value;
                    this.binNumber = "";
                    this._setDisplayValue();
                });
                this.binWidthTextBox.set('isValid', function(){
                    var val = workFlowVariableReplacer.replaceVariable(this.get('value'));
                    var patrn = /^\d*(\.\d*)?((e|E)\+\d+)?$/;
                    var isEmpty = (!val || val=="") ? true : false;
                    return (patrn.test(val) && val > 0) || isEmpty;
                });

                /* For Bin Number TextBox */
                this.connect(this.binNumberTextBox,"onKeyUp",function(e){
                    var value = this.binNumberTextBox.get('value');
                    this.binType = 0;
                    this.binWidth = "";
                    this.binNumber = value;
                    this._setDisplayValue();
                });
                this.binNumberTextBox.set('isValid', function(){
                    var val = workFlowVariableReplacer.replaceVariable(this.get('value'));
                    var patrn = /^[\d]*$/;
                    var isEmpty = (!val || val=="") ? true : false;
                    return (patrn.test(val) && val >= 2 && val <= 100) || isEmpty;
                });

            },

            _setDisplayValue: function() {
                if (this.binType == 0) {
                    this.binWidth = "";
                } else if (this.binType == 1) {
                    this.binNumber = "";
                }
                this.binWidthTextBox.set('value',this.binWidth);
                this.binNumberTextBox.set('value',this.binNumber);
            },

            validateContent: function() {
                var hasContent = this.binWidth + this.binNumber !== "";
                var formValid = this.form.validate();
                return hasContent && formValid;
            },


            /*
            *   getCustomLabel
            *   function called from InlineEditDropDown
            *
            * */
            getCustomLabel: function() {
                return (this.binType == 0) ? this.binNumber + " " + alpine.nls.histogram_type_bins : alpine.nls.histogram_type_width + " " + this.binWidth;
            }

        });
    }
);