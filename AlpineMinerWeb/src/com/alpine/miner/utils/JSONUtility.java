/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * TableDataOutPutJSONAdapter.java
 * 
 * Author john zhao
 * 
 * Version 3.0
 * 
 * Date July 5, 2011
 */

package com.alpine.miner.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.result.VisualAdapterLinearRegression;
import com.alpine.miner.impls.result.VisualAdapterLogisticRegression;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.alpine.datamining.api.AnalyticNodeMetaInfo;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.miner.impls.report.FlowResultGenerator;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelEmpty;
import com.alpine.miner.workflow.output.visual.VisualizationModelLayered;

/**
 * This is a utility class for the json use.It can transfor the visulization
 * model to json string for the front-end use.
 * */
public class JSONUtility {

	public static final String OUT_KEY_TAB_TITLE = "out_title";
	public static final String NODE_META_INFO = "node_meta_info";
	public static final String OPRATOR_INPUT = "operator_input";
	
	public static final String OUT_KEY_TAB_ID = "out_id";

	public static final String OUT_KEY_ITEMS = "items";
	public static final String OUT_KEY_COLUMNS = "columns";
	public static final String OUT_KEY_COLUMN_TYPES = "columnTypes" ;

	public static final String OUT_KEY_VISUAL_DATA = "visualData";
	// this is for the tabname of the composite out put...

	public static final String OUT_KEY_VISUAL_TYPE = "visualType";
	private static final String OUT_KEY_VISUAL_KEYLABEL = "keyLabel";
	private static final String OUT_KEY_VISUAL_KEYS = "keys";
	private static final String OUT_KEY_TABLE_NAME = "tableName";
	private static final String OUT_KEY_NEED_REPORT = "isGenerateReport";  

	//
	public static JSONArray toJSONArray(String[] values) {
		JSONArray jsonArray = new JSONArray();
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				if (value.equals("null") || value.trim().equals("null")) {
					value = "null value";
				}

				jsonArray.add(i, value);

			}
		}
		return jsonArray;
	}

	public static String toJSONString(VisualizationModel visualModel,Locale locale) {
		if (visualModel == null) {
			return "";
		}
		JSONObject json = toJSONObject(visualModel,  locale);

		return json.toString();
	}

	public static JSONObject toJSONObject(VisualizationModel visualModel,Locale locale) {
		JSONObject json;
		if (visualModel == null) {
			return null;
		}
		// use NODE_META_INFO
		AnalyticNodeMetaInfo nodeMetaInfo = visualModel
				.getAnalyticNodeMetaInfo();
		
		String[][] nodeMetaInfoProperties = FlowResultGenerator.getNodeMetaInfoProperties(nodeMetaInfo,locale);
		// set null to save the memory,
		visualModel.setAnalyticNodeMetaInfo(null);

		if (visualModel instanceof VisualizationModelDataTable) {
			VisualizationModelDataTable visualModelTable = (VisualizationModelDataTable) visualModel;
			json = toTableJson(visualModelTable);

		} else if (visualModel instanceof VisualizationModelLayered) {
			VisualizationModelLayered visualModelLayered = (VisualizationModelLayered) visualModel;
			json = toLayeredJson(visualModelLayered,  locale);

		} else if (visualModel instanceof VisualizationModelComposite) {
			VisualizationModelComposite visualModelComposite = (VisualizationModelComposite) visualModel;
			json = toCompositeJson(visualModelComposite,  locale);

		}// for report use only
		else if (visualModel instanceof VisualizationModelEmpty) {
			json = new JSONObject();
		}

		else {// simple json
			json = new JSONObject();

			json.accumulate(OUT_KEY_VISUAL_DATA,
					JSONObject.fromObject(visualModel));
			json.accumulate(OUT_KEY_VISUAL_TYPE,
					visualModel.getVisualizationType());

		}
		json.accumulate(OUT_KEY_NEED_REPORT, visualModel.isNeedGenerateReport());
		json.accumulate(OUT_KEY_TAB_ID,
				System.currentTimeMillis() + Math.random());
		json.accumulate(OUT_KEY_TAB_TITLE, visualModel.getTitle());

		// for report use, this is only for each node ,not for composited each
		// output
		if(nodeMetaInfoProperties!=null){
			json.accumulate(NODE_META_INFO,
				JSONArray.fromObject(nodeMetaInfoProperties));
		}
		String[][] operatorInputs = visualModel.getOpeatorInputs();
		if(operatorInputs!=null){
		
			json.accumulate(OPRATOR_INPUT,	JSONArray.fromObject(operatorInputs));
			//enhance the performance
			visualModel.setOpeatorInputs(null);

		}


		return json;
	}

	private static JSONObject toCompositeJson(
			VisualizationModelComposite visualModelComposite,Locale locale) {
		List<VisualizationModel> models = visualModelComposite.getModels();
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		if (models != null) {
			for (Iterator<VisualizationModel> iterator = models.iterator(); iterator
					.hasNext();) {
				VisualizationModel visualModel = iterator.next();
				if (visualModel != null) {
					jsonArray.add(toJSONObject(visualModel,    locale));
				}

			}
		}
		json.accumulate(OUT_KEY_VISUAL_TYPE, VisualizationModel.TYPE_COMPOSITE);
		json.accumulate(OUT_KEY_VISUAL_DATA, jsonArray);
		return json;
	}

	private static JSONObject toLayeredJson(
			VisualizationModelLayered visualModelLayered,Locale locale) {
		List<String> keys = visualModelLayered.getKeys();
		HashMap<String, VisualizationModel> modelMap = visualModelLayered
				.getModelMap();

		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		if (keys != null) {
			for (Iterator<String> iterator = keys.iterator(); iterator
					.hasNext();) {
				JSONObject modelJSON = new JSONObject();
				String key = iterator.next();
				if (modelMap.get(key) != null) {
					modelJSON.accumulate(key, toJSONObject(modelMap.get(key),  locale));
					jsonArray.add(modelJSON);
				}
			}
		}
		json.accumulate(OUT_KEY_VISUAL_TYPE, visualModelLayered.getVisualizationType());
		json.accumulate(OUT_KEY_VISUAL_KEYLABEL,
				visualModelLayered.getKeyLable());
		json.accumulate(OUT_KEY_VISUAL_KEYS, keys);
		json.accumulate(OUT_KEY_VISUAL_DATA, jsonArray);
		
		return json;
	}

	private static JSONObject toTableJson(
			VisualizationModelDataTable visualModelTable) {

		DataTable dataTable = visualModelTable.getDataTable();

		JSONObject json = new JSONObject();

        //modify by Will
        if(visualModelTable.getVisualizationType()== VisualAdapterLogisticRegression.FOR_GROUP_BY_TEXT_TABLE_TYPE){
            json.accumulate(OUT_KEY_VISUAL_TYPE, VisualAdapterLogisticRegression.FOR_GROUP_BY_TEXT_TABLE_TYPE);
        }else if(visualModelTable.getVisualizationType()== VisualAdapterLinearRegression.FOR_GROUP_BY4_LINEAR_TEXT_TABLE_TYPE){
            json.accumulate(OUT_KEY_VISUAL_TYPE,VisualAdapterLinearRegression.FOR_GROUP_BY4_LINEAR_TEXT_TABLE_TYPE);
        }else{
            json.accumulate(OUT_KEY_VISUAL_TYPE, VisualizationModel.TYPE_DATATABLE);
        }

		// could be an empty array
		JSONObject dataTableJSON = getDataTableJSONObject(dataTable);
		json.accumulate(OUT_KEY_VISUAL_DATA, dataTableJSON);

		return json;
	}

	private static JSONObject getDataTableJSONObject(DataTable dataTable) {
		JSONObject tableJSON = new JSONObject();
		String[] columns = dataTable.getColumnNameString();
		if(dataTable.getTableName()!=null){
			String tableName=dataTable.getTableName();
			if(dataTable.getSchemaName()!=null&&tableName.indexOf(dataTable.getSchemaName()+".") <0){
				tableName=dataTable.getSchemaName() +"." + tableName;
			}
			tableJSON.accumulate(OUT_KEY_TABLE_NAME, tableName);
		}
		tableJSON.accumulate(OUT_KEY_COLUMNS, JSONUtility.toJSONArray(columns));
		String[] columnTypes =dataTable.getColumnTypeString();
		tableJSON.accumulate(OUT_KEY_COLUMN_TYPES, JSONUtility.toJSONArray(columnTypes));

		List<DataRow> rows = dataTable.getRows();
		JSONArray rowsJSON = new JSONArray();
		if (rows != null && rows.size() > 0) {
		
			for (Iterator<DataRow> iterator = rows.iterator(); iterator
					.hasNext();) {
				DataRow dataRow = (DataRow) iterator.next();

				JSONObject rowJSON = new JSONObject();

				for (int i = 0; i < columns.length; i++) {
                    try{
                        String value = dataRow.getData(i);
                        if (columns[i].equals("null")
                                || columns[i].trim().equals("null")) {
                            rowJSON.put("null value", value);
                        } else {
                            rowJSON.put(columns[i], value);
                        }

                    }catch (IndexOutOfBoundsException e){

                    }

				}
				rowsJSON.add(rowJSON);

			}

			// data is the json data for the table (must have an array named
			// 'items')

			

		}
		//avoid the nullpoint in dojo...
		tableJSON.accumulate(OUT_KEY_ITEMS, rowsJSON);
		return tableJSON;
	}






}
