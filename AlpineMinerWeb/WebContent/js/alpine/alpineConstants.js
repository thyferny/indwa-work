/**
 * logView.js
 * This is the js file for all constants used by minerweb.
 * @author John zhao
 * 
 * Version  Ver 3.0
 *   
 * Date     2011-7-4    
 *  
 * COPYRIGHT   2010 - 2011 Alpine Solutions. All Rights Reserved.    
 * */ 

var ID_LOG_VIEW="logView";
var ID_RESULT_TAB="tabRoot";

var STYLE_ALIGN_LEFT="left";
var STYLE_POSITION_ABS="absolute";

var MSG_PROCESS_START="process_start";
var MSG_OPERATOR_START="operator_start";
var MSG_OPERATOR_FINISHED="operator_finished";
var MSG_PROCESS_FINISHED="process_finished";
var MSG_PROCESS_STOP="process_stop";
var MSG_PROCESS_ERROR="process_error";

var TAG_PREFIX_OUTPUT_CHILD="out_";
var TAG_DIV="div";

//here must be same as OutPutJSONAdapter.java's constants
var VISUAL_TYPE_DATATABLE = 0;
var VISUAL_TYPE_TEXT = 1;
var VISUAL_TYPE_BARCHART = 2;
var VISUAL_TYPE_TREE = 3;
var VISUAL_TYPE_NETWORK = 4;
var VISUAL_TYPE_CLUSTER = 5;
//multiple tabs
var VISUAL_TYPE_COMPOSITE=6;
//in one tab 
var VISUAL_TYPE_LAYERED=7;
var VISUAL_TYPE_CLUSRTER_CHART=19;
var TYPE_POINT_CHART = 8;
var TYPE_LINE_CHART = 9;
var TYPE_SCATTER_CHART = 10;
 
//var TYPE_CLUSTERPROFILE_CHART=13;
var TYPE_TABLE_GROUPED=14;
var TYPE_BOXANDWHISKER=16;

//these 2 is for report use...
var   TYPE_EMPTY = 17;
var  TYPE_CHART = 18;

var TYPE_SCATTER_MATRIX =28;
var VISUAL_TYPE_SCATT_PREVIEW = 26;
//this used for cluster profileing table...
//if no number means the legned
var TYPE_PIECHART=15;

var orientation_axis = "axis";
var orientation_away="away";

var  NODE_TYPE_NORMAL = 0; 
 
var  NODETYPE_LEAF=1;
var  NODETYPE_THRESHHOLD=2;

var alpine_script_rel_version = "";
