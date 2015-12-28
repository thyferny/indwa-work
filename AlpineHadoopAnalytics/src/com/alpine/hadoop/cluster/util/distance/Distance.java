

package com.alpine.hadoop.cluster.util.distance;



public interface Distance {
    public <S> double compute(S[] first, S[] second);
}

