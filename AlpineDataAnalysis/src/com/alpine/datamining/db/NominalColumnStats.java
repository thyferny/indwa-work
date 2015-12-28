
package com.alpine.datamining.db;

import java.util.Arrays;



public class NominalColumnStats implements ColumnStats {


	private static final long serialVersionUID = 6651095603345637608L;

	private long mode = -1;
    
    private long maxCounter = 0;

    private long[] scores;
    
    private Column column;
    
    public NominalColumnStats() {}
    
    
    private NominalColumnStats(NominalColumnStats other) {
        this.mode = other.mode;
        this.maxCounter = other.maxCounter;
        if (other.scores != null) {
            this.scores = new long[other.scores.length];
            for (int i = 0; i < this.scores.length; i++)
                this.scores[i] = other.scores[i];
        }
        column = other.column;
    }
    
    
    public Object clone() {
        return new NominalColumnStats(this);
    }
    
    public void startCount(Column column) {
        this.scores = new long[column.getMapping().size()];
        this.mode = -1;
        this.maxCounter = 0;
        this.column = column;
    }
    
    public void count(double doubleIndex, double weight) {
        if (!Double.isNaN(doubleIndex)) {
            int index = (int)doubleIndex;
            if (index >= 0) {
            	if (index >= scores.length) {
            		long[] newScores = new long[index + 1];
            		System.arraycopy(scores, 0, newScores, 0, scores.length);
            		scores = newScores;
            	}
            	scores[index]++;
            	if (scores[index] > maxCounter || (scores[index] == maxCounter && column.getMapping().mapIndex(index).compareTo(column.getMapping().mapIndex((int)mode)) > 0)) {
            		maxCounter = scores[index];
            		mode = index;
            	}
            }
        }
    }
    
    public boolean handleStatistics(String name) {
        return 
            MODE.equals(name) ||
            COUNT.equals(name) ||
            LEAST.equals(name);
    }

    public double getStatistics(Column column, String name, String parameter) {
        if (MODE.equals(name)) {
            return this.mode;
        } else if (COUNT.equals(name)) {
            if (parameter != null) {
                return getValueCount(column, parameter);
            } else {
                return Double.NaN;
            }
        } if (LEAST.equals(name)) {
            long minCounter = Integer.MAX_VALUE;
            long least = 0;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] < minCounter) {
                    minCounter = scores[i];
                    least = i;
                }
            }
            return least;
        } else {
            return Double.NaN;
        }
    }
    
    private long getValueCount(Column column, String value) {
    	if ((column != null) && (column.getMapping() != null)) {
    		int index = column.getMapping().getIndex(value);
    		if (index < 0) {
    			return -1;
    		} else {
    			return scores[index];
    		}
    	} else {
    		return -1;
    	}
    }
    
    public String toString() {
        return "Counts: " + Arrays.toString(scores);
    }
}
