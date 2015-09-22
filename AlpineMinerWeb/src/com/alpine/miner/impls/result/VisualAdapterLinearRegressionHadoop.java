/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * VisualAdapterLinearRegression.java
 * 
 * Author john zhao
 * 
 * Version 1.0
 * 
 * Date July 5, 2011
 */
package com.alpine.miner.impls.result;

import com.alpine.datamining.api.AnalysisException;
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.AnalyticProcessListener;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.db.trainer.LinearRegressionTrainer;
import com.alpine.datamining.api.impl.hadoop.models.LinearRegressionHadoopModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.output.DataRow;
import com.alpine.datamining.api.impl.output.DataTable;
import com.alpine.datamining.api.impl.visual.TextTable;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.datamining.operator.regressions.LinearRegressionGroupGPModel;
import com.alpine.datamining.operator.regressions.LinearRegressionModelDB;
import com.alpine.datamining.utility.NormDistributionUtility;
import com.alpine.datamining.utility.Tools;
import com.alpine.miner.impls.controller.DBUtil;
import com.alpine.miner.impls.dataexplorer.DataExplorerManagerImpl;
import com.alpine.miner.impls.result.nls.VisualNLS;
import com.alpine.miner.impls.web.resource.AbstractAnalyticProcessListener;
import com.alpine.miner.impls.web.resource.WebRunAnalyticProcessListener;
import com.alpine.miner.workflow.output.visual.*;
import com.alpine.util.AlpineUtil;
import com.alpine.utility.db.TableColumnMetaInfo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class VisualAdapterLinearRegressionHadoop extends AbstractOutPutVisualAdapter implements OutPutVisualAdapter {
	 

	public static final VisualAdapterLinearRegressionHadoop INSTANCE = new VisualAdapterLinearRegressionHadoop();

    public static final int FOR_GROUP_BY4_LINEAR_TEXT_TABLE_TYPE = 200;

    public static Map<String,Map<String,VisualizationModelComposite>> linearRegressionGroupByModelMap = new HashMap<String, Map<String, VisualizationModelComposite>>();



    @Override
	public VisualizationModel toVisualModel(AnalyticOutPut analyzerOutPut,Locale locale)
			throws RuntimeException {
	 	 
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			EngineModel	model = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
            List<VisualizationModel> models= new ArrayList<VisualizationModel>();
            String name=analyzerOutPut.getAnalyticNode().getName();
			if(model==null){
				return null;
			}

            String message =   getVtextText((LinearRegressionHadoopModel)model.getModel()).toString();

            VisualizationModelText textModel = new  VisualizationModelText(

                    VisualNLS.getMessage(VisualNLS.MESSAGE_TITLE,locale), 	message);//message);
            models.add(textModel);


            DataTable dataTable=getDataTable((LinearRegressionHadoopModel)model.getModel());
            VisualizationModelDataTable  tableModel=new VisualizationModelDataTable(
                    VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale), dataTable);
            models.add( tableModel);
            //
            LinearRegressionHadoopModel linearDbModel = (LinearRegressionHadoopModel)model.getModel();
            VisualizationModelScatter residualChartModel = new VisualizationModelScatter(
                    VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUALPLOT_TITLE,
                            locale), null);
            VisualizationModelScatter normalProbabilityPlotModel = new VisualizationModelScatter(
                    VisualLanguagePack.getMessage(
                            VisualLanguagePack.Q_Q_PLOT_TITLE,
                            locale), null);
            try {

                fillResidualChartModel(residualChartModel,linearDbModel, locale);
                fillResidualNormalProbablityChartModel(normalProbabilityPlotModel,linearDbModel, locale);
            } catch (AnalysisException e) {
                e.printStackTrace();
            }
            if(residualChartModel.getPointGroups()!=null && residualChartModel.getPointGroups().size()>0){
                //residualChartModel is scatter plot with line
                models.add(residualChartModel);
            }
            if(normalProbabilityPlotModel.getPointGroups()!=null && normalProbabilityPlotModel.getPointGroups().size()>0){
                //residualChartModel is scatter plot with line
                models.add(normalProbabilityPlotModel);
            }



			VisualizationModelComposite  visualModel= new VisualizationModelComposite(name	,models);
			return visualModel;
		}
		else{
			return null ;
		}
		
	}

    private void buildGruopByModuls(List<VisualizationModel> models,LinearRegressionHadoopModel linearRegressionModelHadoop,String keyValue,Locale locale){
        DataTable dataTable=getDataTable(linearRegressionModelHadoop);
        VisualizationModelDataTable  tableModel=new VisualizationModelDataTable(
                VisualNLS.getMessage(VisualNLS.DATA_TITLE,locale), dataTable);
        models.add( tableModel);
        //
        LinearRegressionHadoopModel linearDbModel = linearRegressionModelHadoop;
        VisualizationModelScatter residualChartModel = new VisualizationModelScatter(
                VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUALPLOT_TITLE,
                        locale), null);
        VisualizationModelScatter normalProbabilityPlotModel = new VisualizationModelScatter(
                VisualLanguagePack.getMessage(
                        VisualLanguagePack.Q_Q_PLOT_TITLE,
                        locale), null);
        try {

            fillResidualChartModel(residualChartModel,linearDbModel, locale);
            fillResidualNormalProbablityChartModel(normalProbabilityPlotModel,linearDbModel, locale);
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
        if(residualChartModel.getPointGroups()!=null && residualChartModel.getPointGroups().size()>0){
            //residualChartModel is scatter plot with line
            models.add(residualChartModel);
        }
        if(normalProbabilityPlotModel.getPointGroups()!=null && normalProbabilityPlotModel.getPointGroups().size()>0){
            //residualChartModel is scatter plot with line
            models.add(normalProbabilityPlotModel);
        }
    }

    private DataTable getVtext4Table(List<LinearRegressionHadoopModel> linearRegressionModelHadoops, List<String> keys, Locale locale) {
        DataTable table= new DataTable();
        List<TableColumnMetaInfo> columnMetaInfoList = new ArrayList<TableColumnMetaInfo>();
        columnMetaInfoList.add(new TableColumnMetaInfo(VisualNLS.getMessage(VisualNLS.Logistic_Regression_Group_By_Value,locale),"string"));
        columnMetaInfoList.add(new TableColumnMetaInfo("R2","float"));
        columnMetaInfoList.add(new TableColumnMetaInfo("S","float"));
        table.setColumns(columnMetaInfoList);
        List<DataRow> dataRows = new ArrayList<DataRow>();
        if(null!=linearRegressionModelHadoops && linearRegressionModelHadoops.size()>0){
           Iterator<LinearRegressionHadoopModel> itor = linearRegressionModelHadoops.iterator();
            int i=0;
            while (itor.hasNext()){
                LinearRegressionHadoopModel modelDB= itor.next();
                DataRow row = new DataRow(new String[]{
                        keys.get(i),
                        com.alpine.utility.tools.AlpineMath.doubleExpression(modelDB.getR2()),
                        com.alpine.utility.tools.AlpineMath.doubleExpression(modelDB.getS())
                });
                i=i+1;
                dataRows.add(row);
            }
            table.setRows(dataRows);
        }
        return table;
    }
 

    private void fillResidualNormalProbablityChartModel(
			VisualizationModel residualChartModel,
            LinearRegressionHadoopModel model, Locale locale)
			throws AnalysisException {
		VisualizationModelScatter scatterModel = (VisualizationModelScatter) residualChartModel;
		if(null!=model && model.getResiduals()!=null && model.getResiduals().size()>0){
			//scatterModel.setVisualizationType(VisualizationModel.TYPE_SCATTER_CHART);
            scatterModel.setVisualizationType(11);
			scatterModel.sethGrid(true);
			scatterModel.setvGrid(true);
			scatterModel.setWidth(1800);
			scatterModel.setHeight(600);
			scatterModel.setxAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.SAMPLE_QUANTILE,locale));
			scatterModel.setyAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.THEORY_QUANTILE,locale));
			List<VisualPointGroup> pointGroups = new ArrayList<VisualPointGroup>();
			VisualPointGroup points = new VisualPointGroup();
			
			double[][] data = new double[2][model.getResiduals().size()];
			
			for (int i = 0; i < model.getResiduals().size(); i++) {
			double[] row = model.getResiduals().get(i);
			double f1 = row[1];
			data[0][i] = f1;
			}
			Arrays.sort(data[0]);
			for (int i = 0; i < model.getResiduals().size(); i++) {
			data[1][i] = NormDistributionUtility.normDistributionQuantile((i+0.5)/model.getResiduals().size(),model.getS());
			}

			MaxMinAxisValue maxMin =new MaxMinAxisValue(); 
			if(null!=model.getResiduals().get(0)){
				maxMin.setMaxX(model.getResiduals().get(0)[0]);
				maxMin.setMinX(model.getResiduals().get(0)[0]);
				maxMin.setMaxY(model.getResiduals().get(0)[1]);
				maxMin.setMinY(model.getResiduals().get(0)[1]);
			}
			for (int len = 0; len < model.getResiduals().size(); len++) {
				double f1 = data[0][len];
				double f2 = data[1][len];
				maxMin.compareXY(f1, f2);
				String x = String.valueOf(f1);
				String y = String.valueOf(f2);
				points.addVisualPoint(new VisualPoint(x, y));
			}
			
			//For precision
			String[] title4Precision= new String[2];
			buildPoints4Precision(points,title4Precision,maxMin);
			scatterModel.setxAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.SAMPLE_QUANTILE,locale)+title4Precision[0]);
			scatterModel.setyAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.THEORY_QUANTILE,locale)+title4Precision[1]);
			
			pointGroups.add(points);
			scatterModel.setPointGroups(pointGroups);
			
			 
			DataExplorerManagerImpl.drawScatterLine(scatterModel, maxMin, points) ;
		}
		
	}

	private void fillResidualChartModel(VisualizationModel residualChartModel,
                                        LinearRegressionHadoopModel model, Locale locale)
			throws AnalysisException {
		VisualizationModelScatter scatterModel = (VisualizationModelScatter) residualChartModel;
		if(null!=model && model.getResiduals()!=null && model.getResiduals().size()>0){
//			scatterModel.setVisualizationType(VisualizationModel.TYPE_SCATTER_CHART);
            scatterModel.setVisualizationType(11);
			scatterModel.setWidth(1800);
			scatterModel.setHeight(600);
			scatterModel.sethGrid(true);
			scatterModel.setvGrid(true);
			scatterModel.setxAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.FITVALUE,locale));
			scatterModel.setyAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUAL,locale));
			List<VisualPointGroup> pointGroups = new ArrayList<VisualPointGroup>();
			VisualPointGroup points = new VisualPointGroup();
			MaxMinAxisValue maxMin =new MaxMinAxisValue(); 
			//double[][] data = new double[2][model.getResiduals().size()];
			if(null!=model.getResiduals().get(0)){
				maxMin.setMaxX(model.getResiduals().get(0)[0]);
				maxMin.setMinX(model.getResiduals().get(0)[0]);
				maxMin.setMaxY(model.getResiduals().get(0)[1]);
				maxMin.setMinY(model.getResiduals().get(0)[1]);
			}
			for (int len = 0; len < model.getResiduals().size(); len++) {
				double[] dr = model.getResiduals().get(len);
				double f1 = dr[0];
				double f2 = dr[1];
				String x = String.valueOf(f1);
				String y = String.valueOf(f2);
				maxMin.compareXY(f1, f2);
				points.addVisualPoint(new VisualPoint(x, y));
			}
			//For precision
			String[] title4Precision= new String[2];
			buildPoints4Precision(points,title4Precision,maxMin);
			scatterModel.setxAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.FITVALUE,locale)+title4Precision[0]);
			scatterModel.setyAxisTitle(VisualLanguagePack.getMessage(VisualLanguagePack.RESIDUAL,locale)+title4Precision[1]);
			pointGroups.add(points);
			scatterModel.setPointGroups(pointGroups);

            VisualLine line = new VisualLine(points.getLabel());
            line.addVisualPoint(new VisualPoint(String.valueOf(maxMin.getMinX()),"0"));
            line.addVisualPoint(new VisualPoint(String.valueOf(maxMin.getMaxX()),"0"));
            scatterModel.addVisualLine(line);

//           DataExplorerManagerImpl.drawScatterLine4Regression(scatterModel, maxMin, points) ;
		}
	}

	private DataTable getDataTable(LinearRegressionHadoopModel model) {
		DataTable table= new DataTable();
		 
		Double[] coefficients = model.getCoefficients();
        String[] coefficientsNames = new String[coefficients.length];
        Map<String, Double> coefficientMap = model.getCoefficientsMap();
        //
//        Set<String> keys= coefficientMap.keySet();
//        Iterator<String> itor = keys.iterator();
//        while (itor.hasNext()){
//            String key = itor.next();
//            for(int i=0;i<coefficients.length;i++){
//                if(coefficientMap.get(key).equals(coefficients[i])){
//                    coefficientsNames[i] = key;
//                    break;
//                }
//            }
//        }
        int index=0;
        for(String coefficientsName:model.getCoefficientsMap().keySet()){
        	coefficientsNames[index]=coefficientsName;
        	index++;
        }

		double[] se = model.getSe();
		double[] t = model.getT();
		double[] p = model.getP();
  
		List<TableColumnMetaInfo> columns = new ArrayList<TableColumnMetaInfo>();
		columns.add(new TableColumnMetaInfo(ATTRIBUTE, ""));
		columns.add(new TableColumnMetaInfo(COEFFICIENT, DBUtil.TYPE_NUMBER));
		
		 columns.add(new TableColumnMetaInfo(SE, DBUtil.TYPE_NUMBER));
		 columns.add(new TableColumnMetaInfo(T_STATISTICS, DBUtil.TYPE_NUMBER));
		 columns.add(new TableColumnMetaInfo(P_VALUE, DBUtil.TYPE_NUMBER));
		table.setColumns(columns);
 
		List<DataRow> rows = new ArrayList<DataRow>();
        List<DataRow> otherRowList = new ArrayList<DataRow>();
		for (int i = 0; i < coefficients.length; i++) {
			DataRow row = new DataRow();
            if("intercept".equalsIgnoreCase(coefficientsNames[i])==true){
                row.setData(new String[] {coefficientsNames[i],
                        AlpineMath.powExpression(coefficients[i]),
                        AlpineMath.powExpression(se[i]),
                        AlpineMath.powExpression(t[i]),
                        AlpineMath.powExpression(p[i]) });
                rows.add(row);
            }else{
                row.setData(new String[] {coefficientsNames[i],
                        AlpineMath.powExpression(coefficients[i]),
                        AlpineMath.powExpression(se[i]),
                        AlpineMath.powExpression(t[i]),
                        AlpineMath.powExpression(p[i]) });
                otherRowList.add(row);
            }
	    }
        rows.addAll(otherRowList);
		
		table.setRows(rows);
		return table;
	}
  

	/**
	 * @param model
	 * @return
	 */
	public static TextTable getVTextTable(LinearRegressionHadoopModel model) {
		String[] attributeNames=model.getColumnNames();
		 Double[] coefficients = model.getCoefficients();
		 	double[] se = model.getSe();
	    	double[] t = model.getT();
	    	double[] p = model.getP();
	    	
		TextTable table= new TextTable();
		
		table.addLine(new String[]{ATTRIBUTE,COEFFICIENT,SE,T_STATISTICS,P_VALUE});
		for (int i = 0; i < attributeNames.length; i++) {
	 
				table.addLine(new String[]{attributeNames[i],
						String.valueOf(coefficients[i]),String.valueOf(se[i])
						,String.valueOf(t[i]),String.valueOf(p[i])});
 
		
	}
		return table;
	}

	/**
	 * @param model
	 * @return
	 */
	public   StringBuffer getVtextText(LinearRegressionHadoopModel model) {
		String[] attributeNames=model.getColumnNames();
		 Double[] coefficients = model.getCoefficients();
	    	
		StringBuffer result = new StringBuffer();
		boolean first = true;
		int index = 0;
        result.append(model.getSpecifyColumn()+ " = ");
		//result.append(model.getLabel().getName()+ " = ");
		result.append(Tools.getLineSeparator());
		
		for (int i = 0; i < attributeNames.length; i++) {
				result.append(model.getCoefficientString(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(coefficients[index])), first) + " * " + attributeNames[i]);
				index++;
				first = false;
				result.append(Tools.getLineSeparator());
		}
		result.append(model.getCoefficientString(Double.parseDouble(com.alpine.utility.tools.AlpineMath.doubleExpression(coefficients[coefficients.length - 1])), first)+Tools.getLineSeparator());
		result.append(Tools.getLineSeparator());
		result.append(R2 +
				": "+com.alpine.utility.tools.AlpineMath.doubleExpression(model.getR2()));
		result.append(Tools.getLineSeparator());

		if (Double.isNaN(model.getS()))
		{
			result.append(Tools.getLineSeparator());
			result.append("data size too small!");
			result.append(Tools.getLineSeparator());
			return result ;
		}
		result.append(S +
				": "+com.alpine.utility.tools.AlpineMath.doubleExpression(model.getS()));
		return result;
	}
	
	/**
	 * @param points
	 * @param title4Precision
	 * @param  maxMin
	 */
	private void buildPoints4Precision(VisualPointGroup points, String[] title4Precision, MaxMinAxisValue maxMin){
		if (points.getPoints().size() > 0) {
			List<VisualPoint> pt = points.getPoints();
		
			float n = com.alpine.utility.tools.AlpineMath.adjustUnits(maxMin.getMinX(),maxMin.getMaxX());
			float m = com.alpine.utility.tools.AlpineMath.adjustUnits(maxMin.getMinY(),maxMin.getMaxY());
			
			MathContext mc = new MathContext(1, RoundingMode.HALF_UP);
			BigDecimal nn = new BigDecimal(n,mc);
			BigDecimal mm = new BigDecimal(m,mc);
			NumberFormat  df = new DecimalFormat("0.0E0");
			int[] for1precision = null;
			if(n==1|| m==1){
				for1precision = this.getMinScientificNotationNumber(pt);
			}
			
			if(n!=1){
			   title4Precision[0] = " ("+String.valueOf(nn)+")";
			}else{
				if(null!=for1precision && for1precision.length==2){
					BigDecimal tempXLabel = new BigDecimal(Math.pow(10, for1precision[0]),mc);
					if(for1precision[0]!=1 && 1!=tempXLabel.intValue()){
						title4Precision[0] = " ("+df.format(tempXLabel)+") ";
					}else{
						title4Precision[0] = "";
					}
				}else{
					title4Precision[0]="";
				}
			}
			if(m!=1){
				title4Precision[1] = " ("+String.valueOf(mm)+")";
			}else{
				if(null!=for1precision && for1precision.length==2){
					BigDecimal tempYLabel = new BigDecimal(Math.pow(10, for1precision[1]),mc);
					if(for1precision[1]!=1 && 1!=tempYLabel.intValue()){
						title4Precision[1] = " ("+df.format(tempYLabel)+") ";
					}else{
						title4Precision[1] = "";
					}
				}else{
					title4Precision[1] ="";
				}
			}
			
			for (int i = 0; i < pt.size(); i++) {
				float xValue = 0;
				float yValue = 0;
				try {
					xValue = Float.valueOf(pt.get(i).getX());
					if(Float.isNaN(xValue)==true){
						xValue = 0;
					}
				} catch (NumberFormatException e) {
					xValue = 0;
				}
				try {
					yValue = Float.valueOf(pt.get(i).getY());
					if(Float.isNaN(yValue)==true){
						yValue =0;
					}
				} catch (NumberFormatException e) {
					yValue = 0;
				}
				if(n!=1){
					pt.get(i).setX(String.valueOf(xValue/n));
				}else if(n==1){
					BigDecimal decimalX = new BigDecimal(xValue);
					if(null!=for1precision && for1precision.length==2 && for1precision[0]<0){
						decimalX = decimalX.movePointRight(Math.abs(for1precision[0]));
					}
					decimalX = decimalX.setScale(2, RoundingMode.HALF_UP);
					pt.get(i).setX(String.valueOf(decimalX));
				}
				if(m!=1){
					pt.get(i).setY(String.valueOf(new BigDecimal(yValue,mc)));
				}else if(m==1){
					BigDecimal decimalY = new BigDecimal(yValue);
					if(null!=for1precision && for1precision.length==2 && for1precision[1]<0){
						decimalY = decimalY.movePointRight(Math.abs(for1precision[1]));
					}
					decimalY = decimalY.setScale(3, RoundingMode.HALF_UP);
					pt.get(i).setY(String.valueOf(decimalY));
				}
			}
		}
		
	}
	
	private int[] getMinScientificNotationNumber(List<VisualPoint> pt){
		int value[] = new int[2];
		List<Integer> xPowNumList = new ArrayList<Integer>();
		List<Integer> yPowNumList = new ArrayList<Integer>();
		if(null!=pt){
			
		
		   for (Iterator iterator = pt.iterator(); iterator.hasNext();) {
			VisualPoint visualPoint = (VisualPoint) iterator.next();
				try {
					if(visualPoint.getX().toUpperCase().indexOf("E")!=-1){
						String[] num = visualPoint.getX().toUpperCase().split("E");
						if(null!=num && num.length==2){
							xPowNumList.add(Integer.valueOf(num[1]));
						}
					}
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if(visualPoint.getY().toUpperCase().indexOf("E")!=-1){
						String[] num = visualPoint.getY().toUpperCase().split("E");
						if(null!=num && num.length==2){
							yPowNumList.add(Integer.valueOf(num[1]));
						}
					}
					
				} catch (Exception e) {
					// TODO: handle exception
				}
		  }
		}
		
		Collections.sort(xPowNumList);
		Collections.sort(yPowNumList);
		if(xPowNumList.size()>0){
			value[0] = xPowNumList.get(xPowNumList.size()-1);
		}else{
			value[0] = 0;
		}
		if(yPowNumList.size()>0){
			value[1] = yPowNumList.get(yPowNumList.size()-1);
		}else{
			value[1] = 0;
		}
		return value;
	}
	
}
