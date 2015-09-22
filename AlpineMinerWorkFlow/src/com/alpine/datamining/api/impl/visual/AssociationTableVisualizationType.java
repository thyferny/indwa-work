/**
 * ClassName AssociationTableVisualizationType.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-10
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;
/**
 * jimmy
 */
import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.EngineModel;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutAssociationRule;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutTrainModel;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.operator.fpgrowth.AssociationRule;
import com.alpine.datamining.operator.fpgrowth.AssociationRules;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.tools.AlpineMath;

public class AssociationTableVisualizationType extends TableVisualizationType {

	@Override
	public VisualizationOutPut generateOutPut(AnalyticOutPut analyzerOutPut) {
		Object obj = null;
		if(analyzerOutPut instanceof AnalyzerOutPutTrainModel){
			obj = ((AnalyzerOutPutTrainModel)analyzerOutPut).getEngineModel() ;
		}
		EngineModel model = (EngineModel) obj;
		AssociationRules rules = null;
		if(model != null){
			rules = (AssociationRules)model.getModel();
		}
		TableEntity tableEntity = null;
		tableEntity = new TableEntity();
		if (rules!= null) {
//			DecimalFormat formatter = new DecimalFormat("#.###");
			tableEntity.setColumn(new String[]{
					VisualLanguagePack.getMessage(VisualLanguagePack.PREMISE,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.CONCLUSION,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.CONFIDENCE,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.SUPPORT,locale)
			});
			for (int i=0; i<rules.getNumberOfRules(); i++) {
				AssociationRule rule = rules.getRule(i);
				tableEntity.addItem(new String[]{
						rule.toPremiseString(),
						rule.toConclusionString(),
						AlpineMath.doubleExpression(rule.getConfidence()),
						AlpineMath.doubleExpression(rule.getTotalSupport())
					});
			}
			tableEntity.setColumnBar(new String[]{VisualLanguagePack.getMessage(VisualLanguagePack.CONFIDENCE,locale),
					VisualLanguagePack.getMessage(VisualLanguagePack.SUPPORT,locale)});
		}
		DataTableVisualizationOutPut dataTableOutput = new DataTableVisualizationOutPut(tableEntity);
		dataTableOutput.setName(analyzerOutPut.getAnalyticNode().getName());
		return dataTableOutput;
	}
}
