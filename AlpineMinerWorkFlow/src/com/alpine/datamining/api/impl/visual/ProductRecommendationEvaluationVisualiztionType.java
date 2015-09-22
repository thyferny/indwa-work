package com.alpine.datamining.api.impl.visual;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.recommendation.RecommendationEvaluationOutPut;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.api.utility.AlpineMath;
import com.alpine.miner.view.ui.dataset.TableEntity;

public class ProductRecommendationEvaluationVisualiztionType extends
		TextVisualizationType {

	private static final String NULL = "null";

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		if(!(analyzerOutPut instanceof RecommendationEvaluationOutPut))return null;
		RecommendationEvaluationOutPut output=(RecommendationEvaluationOutPut)analyzerOutPut;
		Double[] result=output.getResult();
		if(result==null)return null;
		
		TextTable table= new TextTable();
		String[] titleArray=new String[]{
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_TRN,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_TRAPEV,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_TRAPOV,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_TRIVP,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_NTRN,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_NTRAPEV,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_NTRAPOV,locale),
				VisualLanguagePack.getMessage(VisualLanguagePack.PRE_NTRIVP,locale)
				};
		table.addLine(titleArray);
		String[] resultArray=new String[8];
		for(int i=0;i<result.length;i++){
			if(result[i]==null){
				resultArray[i]=NULL;
			}else{
				resultArray[i]=AlpineMath.powExpression(result[i]);
			}
		}
		table.addLine(resultArray);

		TableEntity te = new TableEntity();
		for(int i=0;i<table.getLines().size();i++){
			if(i==0){
				te.setColumn(table.getLines().get(i));
			}else{
				te.addItem(table.getLines().get(i));
			}
		}
		te.setSystem(output.getDataAnalyzer().getAnalyticSource().getDataSourceType());
		DataTableVisualizationOutPut tableOutput = new DataTableVisualizationOutPut(te);
		tableOutput.setName(analyzerOutPut.getAnalyticNode().getName());
		return tableOutput;
	}

}
