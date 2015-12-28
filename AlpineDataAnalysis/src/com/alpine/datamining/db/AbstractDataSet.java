
package com.alpine.datamining.db;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alpine.datamining.exception.WrongUsedException;
import com.alpine.datamining.operator.ConsumerProducer;
import com.alpine.datamining.operator.OutputObject;
import com.alpine.datamining.utility.Tools;


public abstract class AbstractDataSet extends OutputObject implements DataSet {

	private static final long serialVersionUID = 2331863831564857092L;

	private Map<String, List<ColumnStats>> statisticsMap = new HashMap<String, List<ColumnStats>>();

    private boolean computedAllStatistics = false;
    
    private boolean computedAllColumnStatistics = false;


	private Map<Double, int[]> idMap = new HashMap<Double, int[]>();
	

    public String getName() {
        return "DataSet";
    }

	public String toString() {
		StringBuffer str = new StringBuffer(Tools.classNameWOPackage(this.getClass()) + ":" + Tools.getLineSeparator());
		str.append(size() + " data," + Tools.getLineSeparator());		
		str.append(getColumns().size() + " regular columns," + Tools.getLineSeparator());
		
		boolean first = true;
		Iterator<Column> s = getColumns().specialColumns();
		while (s.hasNext()) {
			if (first) {
				str.append("special columns = {" + Tools.getLineSeparator());
				first = false;
			}
			Column special = s.next();
			str.append("    " + special.getSpecialName() + " = " + special + Tools.getLineSeparator());
		}
		
		if (!first) {
			str.append("}");
		} else {
			str.append("no special columns" + Tools.getLineSeparator());
		}
        
		return str.toString();
	}

  
	
	public boolean equals(Object o) {
		if (!(o instanceof DataSet)) {
			return false;
		}
		DataSet es = (DataSet) o;
		return getColumns().equals(es.getColumns());
	}

	public ConsumerProducer copy() {
		return (ConsumerProducer)clone();
	}

	public Object clone() {
		try {
			Class<? extends AbstractDataSet> clazz = getClass();
			Constructor<? extends AbstractDataSet> cloneConstructor = clazz.getConstructor(new Class[] { clazz });
			AbstractDataSet result = (AbstractDataSet)cloneConstructor.newInstance(new Object[] { this });
			result.idMap = this.idMap;
			return result;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot clone DataSet: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("'" + getClass().getName() + "' does not implement clone constructor!");
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new RuntimeException("Cannot clone " + getClass().getName() + ": " + e + ". Target: " + e.getTargetException() + ". Cause: " + e.getCause() + ".");
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot clone " + getClass().getName() + ": " + e);
		}
	}

 
	public void calculateAllNumericStatistics() {
		if (isComputedAllStatistics())
		{
			return;
		}
		List<Column> allColumns = new ArrayList<Column>();
		Iterator<Column> a = getColumns().allColumns();
		while (a.hasNext()) {
			allColumns.add(a.next());
		}
		computeStatistics(allColumns);
		setComputedAllStatistics(true);
	}
	
    public void computeStatistics(List<Column> columnList)
    {	
    }
 
	public void computeAllColumnStatistics() throws WrongUsedException {
		if (isRecalculatedAllColumnStatistics())
		{
			return;
		}
		List<Column> allColumns = new ArrayList<Column>();
		Iterator<Column> a = getColumns().allColumns();
		while (a.hasNext()) {
			allColumns.add(a.next());
		}
		computeColumnStatistics(allColumns);
		setRecalculatedAllColumnStatistics(true);
	}


	public void computeColumnStatistics(Column column) throws WrongUsedException {
		List<Column> allColumns = new ArrayList<Column>();
		allColumns.add(column);
		computeColumnStatistics(allColumns);
	}
	

	public void computeColumnStatistics(List<Column> columnList) throws WrongUsedException {
		// do nothing if not desired
		if (columnList.size() == 0) {
			return;
		} else {
			// init statistics
			for (Column column : columnList) {
				Iterator<ColumnStats> stats = column.getAllStats();
				while (stats.hasNext()) {
					ColumnStats columnStats = stats.next();
					columnStats.startCount(column);
				}
			}

			for (Data data : this) {
				for (Column column : columnList) {
					double value = data.getValue(column);
					double weight = 1.0d;
					Iterator<ColumnStats> stats = column.getAllStats();
					while (stats.hasNext()) {
						ColumnStats columnStats = stats.next();
						columnStats.count(value, weight);
					}
				}
			}

			// store cloned statistics
			for (Column column : columnList) {
				List<ColumnStats> statisticsList = statisticsMap.get(column.getName());
				// no stats known for this column at all --> new list
				if (statisticsList == null) {
					statisticsList = new LinkedList<ColumnStats>();
					statisticsMap.put(column.getName(), statisticsList);
				}            

				// in all cases: clear the list before adding new stats (clone of the calculations)
				statisticsList.clear();

				Iterator<ColumnStats> stats = column.getAllStats();
				while (stats.hasNext()) {
					ColumnStats columnStats = (ColumnStats)stats.next().clone();
					statisticsList.add(columnStats);
				}
			}
		}
	}
    

    public double getStatistics(Column column, String statisticsName) {
        return getStatistics(column, statisticsName, null);
    }

    public double getStatistics(Column column, String statisticsName, String statisticsParameter) {
        List<ColumnStats> statisticsList = statisticsMap.get(column.getName());
        if (statisticsList == null)
            return Double.NaN;
        
        for (ColumnStats columnStats : statisticsList) {
            if (columnStats.handleStatistics(statisticsName)) {
                return columnStats.getStatistics(column, statisticsName, statisticsParameter);
            }
        }
        
        return Double.NaN;
    }

	public boolean isComputedAllStatistics() {
		return computedAllStatistics;
	}

	public void setComputedAllStatistics(boolean computedAllStatistics) {
		this.computedAllStatistics = computedAllStatistics;
	}

	public boolean isRecalculatedAllColumnStatistics() {
		return computedAllColumnStatistics;
	}

	public void setRecalculatedAllColumnStatistics(
			boolean recalculatedAllColumnStatistics) {
		this.computedAllColumnStatistics = recalculatedAllColumnStatistics;
	}

	public void setStatisticsCaculatedFlag(boolean caculated)
	{
		setComputedAllStatistics(caculated);
		setRecalculatedAllColumnStatistics(caculated);
	}
 
}
