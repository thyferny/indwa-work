/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterNaiveBayes.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.operator.bayes.NBModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.workflow.output.visual.VisualizationModel;
import com.alpine.miner.workflow.output.visual.VisualizationModelComposite;
import com.alpine.miner.workflow.output.visual.VisualizationModelDataTable;
import com.alpine.miner.workflow.output.visual.VisualizationModelText;
import com.alpine.utility.tools.AlpineMath;

public class VisualAdapterNaiveBayes extends AbstractOutPutVisualAdapter
		implements OutPutVisualAdapter {

	public static final VisualAdapterNaiveBayes INSTANCE = new VisualAdapterNaiveBayes();

	@Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {

		EngineModel model = null;
		if (analyzerOutPut instanceof AnalyzerOutPutTrainModel) {
			model = ((AnalyzerOutPutTrainModel) analyzerOutPut).getEngineModel();
		}
		if (model == null){
			return null;
			}
		 
		String message = toVText((NBModel) model.getModel());
		 
 
		String name = analyzerOutPut.getAnalyticNode().getName();
		VisualizationModelText textModel = new VisualizationModelText(VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale),
				message);// message);

		List<VisualizationModel> models = new ArrayList<VisualizationModel>();
		models.add(textModel);
		List dataTables = getDataTable((NBModel) model.getModel(),  locale);
		for (Iterator iterator = dataTables.iterator(); iterator.hasNext();) {
			DataTable dataTable = (DataTable) iterator.next();
			VisualizationModelDataTable tableModel = new VisualizationModelDataTable(
					dataTable.getTableName(), dataTable);
			models.add(tableModel);

		}

		VisualizationModelComposite visualModel = new VisualizationModelComposite(
				name, models);

		return visualModel;

	}

	// numeric type have only one table
	private List<DataTable> getDataTable(NBModel model,Locale locale) {
		List<DataTable> tables = new ArrayList<DataTable>();
		DataTable numericAttrTable = new DataTable();
		numericAttrTable.setTableName(VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale));
		int numberOfClasses = model.getNumberOfClasses();
		String[] classValues = model.getClassValues();
		double[][][] distributionProperties = model.getDistributionProperties();
		String[] attributeNames = model.getColumneNames();
		boolean[] nominal = model.getNominal();
		setColumns(numericAttrTable, new String[] { ATTRIBUTE, CLASS,
				MEAN, STANDARD_DEVIANTION },new String[]{ DBUtil.TYPE_CATE,DBUtil.TYPE_CATE,DBUtil.TYPE_NUMBER,DBUtil.TYPE_NUMBER  });
 

		List<DataRow> numericRos = new ArrayList<DataRow>();

		for (int i = 0; i < model.getNumberOfColumns(); i++) {
			if (nominal[i] == false) {

				for (int j = 0; j < numberOfClasses; j++) {
					String[] items = new String[4];
					items[0] = attributeNames[i];
					items[1] = classValues[j];
					items[2] = AlpineMath
							.doubleExpression(distributionProperties[i][j][model.INDEX_MEAN]);
					items[3] = AlpineMath
							.doubleExpression(distributionProperties[i][j][model.INDEX_STANDARD_DEVIATION]);
					DataRow row = new DataRow();
					row.setData(items);
					numericRos.add(row);
				}
			} else {// categry type attribute...
				tables.add(createCateGoryTable(model, i));
			}
		}
		numericAttrTable.setRows(numericRos);
		tables.add(numericAttrTable);
		return tables;
	}

	private DataTable createCateGoryTable(NBModel model, int i) {
		String[][] attributeValues = model.getColumnValues();
		double[][][] weightSums = model.getWeightSums();
		String[] attributeNames = model.getColumneNames();
		String[] classValues = model.getClassValues();
		double[][][] distributionProperties = model.getDistributionProperties();
		DataTable te = new DataTable();

		int colNo = weightSums[i][0].length;
		String[] header = null;
		if (weightSums[i][0][weightSums[i][0].length - 1] == 0) {
			header = new String[colNo + 1];
		} else {
			header = new String[colNo + 2];
		}

		header[0] = ATTRIBUTE;
		header[1] = CLASS;
		for (int k = 0; k < weightSums[i][0].length; k++) {
			if (k == weightSums[i][0].length - 1) {
				if (weightSums[i][0][k] == 0) {
					continue;
				}
			}
			header[k + 2] = attributeValues[i][k];
		}

		String[] colTypes = new String[header.length];
		for(int n =0;n<header.length;n++){
			colTypes[n]=DBUtil.TYPE_CATE;
		}
		setColumns(te, header,colTypes);

		te.setTableName(attributeNames[i]);

		List<DataRow> rows = new ArrayList<DataRow>();
		for (int j = 0; j < model.getNumberOfClasses(); j++) {
			String[] items = null;

			items = new String[colNo + 2];

			items[0] = attributeNames[i];
			items[1] = classValues[j];
			for (int k = 0; k < weightSums[i][j].length; k++) {
				if (k == weightSums[i][j].length - 1) {
					if (weightSums[i][j][k] == 0) {
						continue;
					}
				}
				items[k + 2] = AlpineMath.doubleExpression(Math
						.exp(distributionProperties[i][j][k]));
			}

			DataRow row = new DataRow();
			row.setData(items);
			rows.add(row);

		}
		te.setRows(rows);
		return te;
	}

	/**
	 * @param model
	 * @return
	 */
	private String toVText(NBModel model) {

		StringBuffer buffer = new StringBuffer();
		String[] classValues = model.getClassValues();

		int numberOfClasses = model.getNumberOfClasses();

		double[] priors = model.getPriors();
		buffer.append(CLASS_PRIORS).append(Tools.getLineSeparator());
		for (int i = 0; i < numberOfClasses; i++) {

			buffer.append(PRIORS +
					"(" + classValues[i] + "):")
					.append(AlpineMath.doubleExpression(Math.exp(priors[i])))
					.append(Tools.getLineSeparator());
		}

//		if (model.getLabel().isNominal() && model.isCalculateDeviance()) {
		if (Double.isNaN(model.getNullDeviance())==false && Double.isNaN(model.getDeviance())==false) {
			double nullDeviance = model.getNullDeviance();
			double deviance = model.getDeviance();
			buffer.append(NULL_DEVIANCE +":")
					.append(AlpineMath.doubleExpression(nullDeviance))
					.append(Tools.getLineSeparator()).append(DEVIANCE +	":")
					.append(AlpineMath.doubleExpression(deviance))
					.append(Tools.getLineSeparator());
		}
		return buffer.toString();

	}

	/**
	 * @param model
	 * @return
	 */
	public StringBuffer getVtextText(LinearRegressionModelDB model) {
		String[] attributeNames = model.getColumnNames();
		Double[] coefficients = model.getCoefficients();

		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
		result.append(model.getLabel().getName() + " = ");
		result.append(Tools.getLineSeparator());

		for (int i = 0; i < attributeNames.length; i++) {
			result.append(model.getCoefficientString(Double
					.parseDouble(com.alpine.utility.tools.AlpineMath
							.doubleExpression(coefficients[index])), first)
					+ " * " + attributeNames[i]);
			index++;
			first = false;
			result.append(Tools.getLineSeparator());
		}
		result.append(model.getCoefficientString(
				Double.parseDouble(com.alpine.utility.tools.AlpineMath
						.doubleExpression(coefficients[coefficients.length - 1])),
				first)
				+ Tools.getLineSeparator());
		result.append(Tools.getLineSeparator());
		result.append(R2 +
				": "
				+ com.alpine.utility.tools.AlpineMath.doubleExpression(model
						.getR2()));
		result.append(Tools.getLineSeparator());

		if (Double.isNaN(model.getS())) {
			result.append(Tools.getLineSeparator());
			result.append("data size too small!");
			result.append(Tools.getLineSeparator());
			return result;
		}
		result.append(S +
				": "
				+ com.alpine.utility.tools.AlpineMath.doubleExpression(model
						.getS()));
		return result;
	}
}
