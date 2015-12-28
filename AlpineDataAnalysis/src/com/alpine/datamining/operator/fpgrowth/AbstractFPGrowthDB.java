
package com.alpine.datamining.operator.fpgrowth;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alpine.datamining.db.DBSource;
import com.alpine.datamining.db.DBTable;
import com.alpine.datamining.db.DataSet;
import com.alpine.datamining.db.DatabaseSourceParameter;
import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.utility.DatabaseConnection;
import com.alpine.datamining.utility.OperatorUtil;
import org.apache.log4j.Logger;



public abstract class AbstractFPGrowthDB  {

	protected Statement st;

	protected Map<String, Item> stringItemMapping = new HashMap<String, Item>();

	protected String positiveValue;

	protected int threshold = 0;

	protected int minTotalSupport = 0;
	
	protected boolean useArray = false;

	protected DatabaseConnection databaseConnection;

	// use this to avoid too many object created
	protected DBSource commonDataSource;

	protected FPGrowthParameter para;
    private static final Logger itsLogger = Logger.getLogger(AbstractFPGrowthDB.class);
    protected String columnName;
//	int arrayLength = 0;
	

	public AbstractFPGrowthDB() {
		super();
	}
	
	public abstract ConsumerProducer[] apply(DataSet dataSet, FPGrowthParameter para) throws OperatorException ;

	protected void initParameters(DataSet dataSet)
			throws OperatorException {

		threshold = para.getTableSizeThreshold();
		double minSupport = para.getSupport();
		minTotalSupport = (int) Math.ceil(minSupport * dataSet.size());
		positiveValue = para.getPositiveValue();
//		positiveValueStringRep = StringHandler.escQ(positiveValue);
		columnName = para.getColumnName();
		useArray = para.isUseArray();
		commonDataSource = OperatorUtil
				.createOperator(DBSource.class);
		
		DatabaseSourceParameter parameter = new DatabaseSourceParameter();

		parameter.setWorkOnDatabase(true);
		// here be careful, if the DB System should be configured?
		parameter.setDatabaseSystem(((DBTable) dataSet.getDBTable()).getDatabaseConnection().getProperties().getName());//????????????????
		// each time create data set will reset the table name
		parameter.setTableName(((DBTable) dataSet.getDBTable())
						.getTableName());
		parameter.setUrl(((DBTable) dataSet.getDBTable()).getUrl());
		parameter.setUsername(((DBTable) dataSet.getDBTable())
						.getUserName());
		parameter.setPassword(((DBTable) dataSet.getDBTable())
						.getPassword());
		commonDataSource.setParameter(parameter);

	}
	protected DataSet retrieveDataSet(String tableName, boolean on)
			throws Exception {

		DataSet dataSet = commonDataSource
				.createDataSetUsingExitingDBConnection(databaseConnection,
						tableName, false);

		return dataSet;
	}



	public String getExpression() {
		return " "+para.getExpression()+" ";
	}

	protected StringBuffer getPositiveValueString(Boolean isNominal) {
		StringBuffer sb = new StringBuffer();
		if (isNominal)
		{
			sb.append("'");
		}
		sb.append(positiveValue);
		if (isNominal)
		{
			sb.append("'");
		}
		return sb;
	}

	protected void mineTree(FPTree tree, ItemSets rules,
			int recursionDepth, ItemSet conditionalItems) {
		if (true == (treeIsEmpty(tree, recursionDepth))) {
			return;
		}
		// recursivly mine tree
		Map<Item, Header> headerTable = tree.getHeaderTable();
		Iterator<Map.Entry<Item, Header>> headerIterator = headerTable
				.entrySet().iterator();
		while (headerIterator.hasNext()) {
			Map.Entry<Item, Header> headerEntry = headerIterator.next();
			Item item = headerEntry.getKey();
			Header itemHeader = headerEntry.getValue();
			// check for minSupport
			long itemSupport = itemHeader.getFrequencies().getFrequency(
					recursionDepth);
			if (itemSupport < minTotalSupport) {
				continue;
			}
			// run over sibling chain
			for (FPTreeNode node : itemHeader.getSiblingChain()) {
				// and propagate frequency to root
				long frequency = node.getFrequency(recursionDepth);
				// if frequency is positiv
				if (frequency <= 0) {
					continue;
				}
				FPTreeNode currentNode = node.getFather();
				while (currentNode != tree) {
					// increase node frequency
					currentNode
							.increaseFrequency(recursionDepth + 1, frequency);
					// increase item frequency in headerTable
					headerTable.get(currentNode.getNodeItem()).getFrequencies()
							.increaseFrequency(recursionDepth + 1, frequency);
					// go up in tree
					currentNode = currentNode.getFather();
				}

			}
			ItemSet recursivConditionalItems = (ItemSet) conditionalItems
					.clone();
			item = stringItemMapping.get(((BooleanColumnItem) item)
					.getName());
			// add item to conditional items
			recursivConditionalItems.addItem(item, itemSupport);
			// add this conditional items to frequentSets
			rules.addFrequentSet(recursivConditionalItems);
			// recursivly mine new tree
			mineTree(tree, rules, recursionDepth + 1, recursivConditionalItems);

			// run over sibling chain for poping frequency stack
			for (FPTreeNode node : itemHeader.getSiblingChain()) {
				// and remove propagation of frequency
				FPTreeNode currentNode = node.getFather();
				while (currentNode != tree) {
					// pop frequency
					currentNode.popFrequency(recursionDepth + 1);
					// go up in tree
					currentNode = currentNode.getFather();
				}
			}
			// pop frequencies of every header table on current recursion depth
			for (Header currentItemHeader : headerTable.values()) {
				currentItemHeader.getFrequencies().popFrequency(
						recursionDepth + 1);
			}
		}
	}



	private boolean treeIsEmpty(FPTree tree, int recursionDepth) {
		// tree is empty if every child of rootnode has frequency of 0 on top of
		// stack
		for (FPTreeNode node : tree.getChildren().values()) {
			if (node.getFrequency(recursionDepth) > 0) {
				return false;
			}
		}
		return true;
	}


}
