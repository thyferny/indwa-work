/**
 * User: robbie
 * Date: 10/15/12
 * (c) Alpine Data Labs 2012
 */

define([
    'dojo/_base/declare',
    'dojo/_base/event',
    'dijit/TooltipDialog',
    'dojo/text!alpine/layout/InlineEdit/InlineEditTTDialog.html'
], function(declare, event, TooltipDialog, template){

    return declare([TooltipDialog],{

       templateString: template,

       baseClass: 'inlineEditTTDialog'

   });

});