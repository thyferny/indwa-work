/**
*
* ClassName Distance.java
*
* Version information: 1.00
*
* Aug 20, 2012
* 
* COPYRIGHT (C) Alpine Data Labs. All Rights Reserved.
*
*/

package com.alpine.hadoop.cluster.util.distance;

/**
 * @author Jonathan
 *  
 */

public interface Distance {
    public <S> double compute(S[] first, S[] second);
}

