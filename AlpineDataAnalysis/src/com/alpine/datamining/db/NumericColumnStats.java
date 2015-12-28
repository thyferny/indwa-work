package com.alpine.datamining.db;




public class NumericColumnStats implements ColumnStats {

	private static final long serialVersionUID = 7906000233180028610L;

	private double sum = 0.0d;

    private double squaredSum = 0.0d;

    private int valueCounter = 0;

    private long mode = -1;  
    private long maxCounter = 0;
    private long[] scores;
    private Column column = null;
    
    public NumericColumnStats() {}
    
    
    private NumericColumnStats(NumericColumnStats other) {
        this.sum = other.sum;
        this.squaredSum = other.squaredSum;
        this.valueCounter = other.valueCounter;

        if ( other.column != null &&  other.column.isCategory()){
	        this.mode = other.mode;
	        this.maxCounter = other.maxCounter;
	        if (other.scores != null) {
	            this.scores = new long[other.scores.length];
	            for (int i = 0; i < this.scores.length; i++)
	                this.scores[i] = other.scores[i];
	        }
	        column = other.column;
        }

    }
    
    public Object clone() {
        return new NumericColumnStats(this);
    }
    
    public void startCount(Column column) {
        this.sum = 0.0d;
        this.squaredSum = 0.0d;
        this.valueCounter = 0;
        if (column != null && column.isCategory()){
        
	        this.column = column;
	        
	        this.scores = new long[column.getMapping().size()];
	        this.mode = -1;
	        this.maxCounter = 0;
	    }
    }
    
    public void count(double value, double weight) {
        if (!Double.isNaN(value)) {
            sum += value;
            squaredSum += value * value;
            valueCounter++;
        }
        
        if (column != null && column.isCategory()){
	        if (!Double.isNaN(value)) {
	        	String valueString = null;
	        	if (column.isNumerical()){
	        		valueString = String.valueOf((int)value);
	        	}else{
	        		valueString = String.valueOf(value);
	        	}
	            int index = column.getMapping().mapString(valueString);
	            if (index >= 0) {
	            	// more values than before? Increase Array size...
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
    }

    public boolean handleStatistics(String name) {
        return 
            AVERAGE.equals(name) ||
            VARIANCE.equals(name) ||
            SUM.equals(name)||
            COUNT.endsWith(name)
            ||            MODE.equals(name) ||
            //COUNT.equals(name) ||
            LEAST.equals(name);
    }
    
    public double getStatistics(Column column, String name, String parameter) {
    if (this.column != null && this.column.isCategory()){
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
    	if (AVERAGE.equals(name)) {
            return this.sum / this.valueCounter;
        } else if (VARIANCE.equals(name)) {
        	if (valueCounter <= 1) {
        		return 0;
        	}
        	return (squaredSum - (sum * sum) / valueCounter) / (valueCounter - 1);
        } else if (SUM.equals(name)) {
            return this.sum;
        } else if (COUNT.equals(name)){
        	return this.valueCounter;
        }else {
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

}
