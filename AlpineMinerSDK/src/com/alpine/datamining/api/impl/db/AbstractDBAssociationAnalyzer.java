/**
 * ClassName FPGrowthDBAnalyzer.java
 *
 * Version information: 1.00
 *
 * Data: 2010-4-21
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.db;

import com.alpine.datamining.api.AnalyticOutPut;
import com.alpine.datamining.api.impl.algoconf.AbstractAssociationConfig;
import com.alpine.datamining.api.impl.output.AnalyzerOutPutAssociationRule;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.Container;
import com.alpine.datamining.operator.Operator;
import com.alpine.datamining.operator.fpgrowth.AssociationRuleGenerator;
import com.alpine.datamining.operator.fpgrowth.AssociationRuleGeneratorParameter;
import com.alpine.datamining.operator.fpgrowth.AssociationRules;
import com.alpine.datamining.utility.OperatorUtil;

/**
 * @author John Zhao
 *
 */
public abstract class AbstractDBAssociationAnalyzer extends AbstractDBAnalyzer{

	protected AnalyticOutPut generateRules(AbstractAssociationConfig config, Container result)
	throws OperatorException {
		AssociationRuleGeneratorParameter parameter = new AssociationRuleGeneratorParameter();
	Operator ruleGenerator = OperatorUtil.createOperator(AssociationRuleGenerator.class);
	parameter.setMinConfidence(Double.parseDouble(config.getMinConfidence()));
//	ruleGenerator.setParameter(AssociationRuleGenerator.PARAMETER_MIN_CONFIDENCE ,config.getMinConfidence());
//	ruleGenerator.setParameter(AssociationRuleGenerator.PARAMETER_CRITERION, config.getRuleCriterion());
	ruleGenerator.setParameter(parameter);
	Container associationRuleGeneratorResult = ruleGenerator.apply(result);
	ConsumerProducer[] ioObjects= associationRuleGeneratorResult.getArrays(); 
	AssociationRules rules=(AssociationRules)ioObjects[0];
	return new AnalyzerOutPutAssociationRule(rules);
}


}
