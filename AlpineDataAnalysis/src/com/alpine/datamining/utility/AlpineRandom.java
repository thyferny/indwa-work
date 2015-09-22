/**
 * ClassName AlpineRandom
 *
 * Version information: 1.00
 *
 * Data: 2010-3-25
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.utility;

import java.util.Random;

/**
 * The random number generator. This should be used for all random
 * purposes to ensure that two runs of the same process setup provide the
 * same results.
 * 
 * @author Eason
 */
public class AlpineRandom extends Random {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8418195771661098489L;

	/**
	 * random number generator using the random number generator seed
	 * specified for the root operator (ProcessRootOperator).
	 */
	private static AlpineRandom randomGenerator = new AlpineRandom(2001);

	/** Initializes the random number generator without a seed. */
	private AlpineRandom() {
		super();
	}

	/** Initializes the random number generator with the given <code>seed</code> */
	public AlpineRandom(long seed) {
		super(seed);
	}

	/** Returns the  random number generator if the seed is negative and a new RandomGenerator
     *  with the given seed if the seed is positive or zero.  */
	public static AlpineRandom getRandomGenerator(int seed) {
        return getRandomGenerator(null, seed);
    }
    
	/** Returns the random number generator if the seed is negative and a new RandomGenerator
	 *  with the given seed if the seed is positive or zero.  */
	public static AlpineRandom getRandomGenerator(Process process, int seed) {
        if (seed < 0) {
        	if (randomGenerator == null) { // might happen
        		init(process);
            }
            return randomGenerator;
        } else {
            return new AlpineRandom(seed);
        }
	}

	/**
	 * Instantiates the random number generator and initializes it with
	 * the random number generator seed
	 */
	public static void init(Process process) {
		long seed = 2001;
        if (process != null) {
            try {
                seed = 2001;//process.getRootOperator().getParameterAsInt(ProcessRootOperator.PARAMETER_RANDOM_SEED);
            } catch (Exception e) {
                seed = 2001;
            }
        }
		if (seed == -1) // could be from process parameter
			randomGenerator = new AlpineRandom();
		else
			randomGenerator = new AlpineRandom(seed);
	}


}
