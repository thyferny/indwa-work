/**
 * ClassName AssociationRules.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-29
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.operator.fpgrowth;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.alpine.datamining.db.Column;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.Model;
import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.DataType;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.Tools;
import com.alpine.utility.db.DataSourceInfoGreenplum;
import com.alpine.utility.db.DataSourceInfoPostgres;
import com.alpine.utility.db.IMultiDBUtility;
import com.alpine.utility.db.MultiDBUtilityFactory;
import org.apache.log4j.Logger;
import com.alpine.utility.tools.StringHandler;

/**
 * A set of {@link AssociationRule}s which can be constructed from frequent item sets.
 * 
 * @author Eason
 */
public class AssociationRules extends OutputObject implements Iterable<AssociationRule>, Model{
    private static Logger itsLogger= Logger.getLogger(AssociationRules.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = -8195196047323913352L;

	private static final int MAXIMUM_NUMBER_OF_RULES_IN_OUTPUT = 100;

	private static final String File_Extension = "asr";

	private static final String File_Description =   "Association Rules";
	
	private List<AssociationRule> associationRules = new ArrayList<AssociationRule>();
	
	private DataSet trainingDataSet;
	
	private String positiveValue;
	
	public String[] UPDATE= new String[1];
	
	public AssociationRules(DataSet trainingDataSet){
		this.trainingDataSet = trainingDataSet;
	}

	public void addItemRule(AssociationRule rule) {
		associationRules.add(rule);
	}

	public int getNumberOfRules() {
		return associationRules.size();
	}
	
	public AssociationRule getRule(int index) {
		return associationRules.get(index);
	}
	
	public String getExtension() {
		return File_Extension;
	}

	public String getFileDescription() {
		return File_Description;
	}

	public String toResultString() {
		return toString(-1);
	}
	
	public String toString() {
		return toString(MAXIMUM_NUMBER_OF_RULES_IN_OUTPUT);
	}
	
	public String toString(int maxNumber) {
		Collections.sort(associationRules);
		StringBuffer buffer = new StringBuffer("Association Rules" + Tools.getLineSeparator());
		int counter = 0;
		for (AssociationRule rule : associationRules) {
			if ((maxNumber >= 0) && (counter > maxNumber)) {
				buffer.append("... " + (associationRules.size() - maxNumber) + " other rules ...");
				break;
			}
			buffer.append(rule.toString());
			buffer.append(Tools.getLineSeparator());
			counter++;
		}
		return buffer.toString();
	}
	
    
	public Iterator<AssociationRule> iterator() {
		return associationRules.iterator();
	}
	
	public Item[] getAllConclusionItems() {
		SortedSet<Item> conclusions = new TreeSet<Item>();
		for (AssociationRule rule : this) {
			Iterator<Item> i = rule.getConclusionItems();
			while (i.hasNext()) {
				conclusions.add(i.next());
			}
		}
		Item[] itemArray = new Item[conclusions.size()];
		conclusions.toArray(itemArray);
		return itemArray;
	}
	public DataSet apply(DataSet dataSet) throws OperatorException
	{
		String dbType = ((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName();
		IMultiDBUtility multiDBUtility = MultiDBUtilityFactory.createConnectionInfo(dbType);
		String tableName=((DBTable) dataSet.getDBTable()).getTableName();
		String applyName = "AlpineMinerAR";
		String applyNameQ = StringHandler.doubleQ(applyName);
		UPDATE[0] = applyName;
		StringBuffer add = new StringBuffer();
		String textType = null;
		if (dbType.endsWith(DataSourceInfoPostgres.dBType)||dbType.endsWith(DataSourceInfoGreenplum.dBType)){
			textType = "text";
		}else{
			textType = "varchar2(4000)";
		}
		add.append("alter table ").append(tableName).append(" add ").append(applyNameQ).append(textType);
		StringBuffer update=new StringBuffer("update ");
		update.append(tableName).append(" set (").append(applyNameQ).append(")=(");
		StringBuffer ARString = getARString();
		StringBuffer predictedString = new StringBuffer();
		int textColumn = 0;
		Iterator<Column> iter = trainingDataSet.getColumns().iterator();
		if (iter.next().isNominal()){
			textColumn = 1;
		}
		StringBuffer columnText = new StringBuffer();
		StringBuffer columnDouble = new StringBuffer();
		if (textColumn == 1){
			columnText.append(multiDBUtility.stringArrayHead()).append(getColumns()).append(multiDBUtility.stringArrayTail());
			columnDouble.append("null");
		}else{
			columnText.append("null");
			columnDouble.append(multiDBUtility.floatArrayHead()).append(getColumns()).append(multiDBUtility.floatArrayTail());
		}
		predictedString.append("alpine_miner_ar_predict(").append(textColumn).append(",").append(columnDouble).append(",").append( columnText ).append(",'").append(positiveValue).append("','").append(ARString).append("')");
		update.append(predictedString).append(")");
		DatabaseConnection databaseConnection = ((DBTable) dataSet
				.getDBTable()).getDatabaseConnection();
	
		Statement st = null;
		try {
			st = databaseConnection.createStatement(false);
			itsLogger.debug("AssociationRules.apply():sql="+add);
			st.execute(add.toString());
			itsLogger.debug("AssociationRules.apply():sql="+update);
			st.execute(update.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OperatorException(e.getLocalizedMessage());
		}
		return dataSet;
	}
	private StringBuffer getColumns(){
		StringBuffer ret = new StringBuffer();
		boolean first = true;
		for (Column column: trainingDataSet.getColumns()){
			if (first){
				first = false;
			}else{
				ret.append(",");
			}
			if (column.getValueType() == DataType.BOOLEAN){
				ret.append(" (case when ").append(StringHandler.doubleQ(column.getName())).append(" is true then 't' else 'f' end)");
			}else{
				ret.append(StringHandler.doubleQ(column.getName()));
			}
		}
		return ret;
	}
	private StringBuffer getARString(){
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int i = 1;
		for (Column column: trainingDataSet.getColumns()){
			map.put(column.getName(), i);
			i++;
		}
		StringBuffer predicted = new StringBuffer();
		Iterator<AssociationRule> iter = associationRules.iterator();
		HashSet<String> conclustionSet = new HashSet<String>();
		boolean firstAR = true;
		while(iter.hasNext())
		{
			conclustionSet.clear();
			StringBuffer premiseStr = new StringBuffer();
			StringBuffer conclusionStr = new StringBuffer();
			AssociationRule ar = iter.next();
			Iterator<Item> premise = ar.getPremiseItems();
			Iterator<Item> conclusion = ar.getConclusionItems();
			if (ar.getConclusion().size() == 1)
			{
				boolean first = true;
				while(premise.hasNext())
				{
					if(first){
						first = false;
					}else{
						premiseStr.append("|");
					}
					String premiseItem = premise.next().toString();
					premiseStr.append(map.get(premiseItem));
				}
				first = true;
				while(conclusion.hasNext())
				{
					String conclusionItem = conclusion.next().toString();
					conclusionStr.append(conclusionItem);
					conclusionStr.append("|");
					conclusionStr.append(map.get(conclusionItem));
				}
				if (firstAR){
					firstAR = false;
				}else{
					predicted.append(";");
				}
				predicted.append(premiseStr).append(":").append(conclusionStr);	
			}
		}
		return predicted;
	}


	public DataSet getTrainingDataSet() {
		return trainingDataSet;
	}

	public void setTrainingDataSet(DataSet trainingDataSet) {
		this.trainingDataSet = trainingDataSet;
	}

	public String getPositiveValue() {
		return positiveValue;
	}

	public void setPositiveValue(String positiveValue) {
		this.positiveValue = positiveValue;
	}
}
