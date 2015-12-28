
package com.alpine.datamining.operator;

import com.alpine.datamining.exception.OperatorException;
import com.alpine.datamining.exception.WrongUsedException;
import org.apache.log4j.Logger;

public abstract class Operator {

	private String name;
	private Container inputContainer;
    private static final Logger logger = Logger.getLogger(Operator.class);

    private Parameter parameter;
	public final String getName() {
		return this.name;
	}

	public abstract ConsumerProducer[] apply() throws OperatorException;

	public Container apply(Container input) throws OperatorException {

			if (input == null)
				throw new IllegalArgumentException("Input is null!");
			this.inputContainer = input;
			// actually applying
            ConsumerProducer[] output = null;
            try { 
                output = apply();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
        		if(e instanceof WrongUsedException){
    				throw (OperatorException)e;
    			} 
    			else{
    				throw new OperatorException(e.getLocalizedMessage(), e);
    			}			
            } finally {
            	// set source to the output
            	if (output != null) {
            		for (ConsumerProducer ioObject : output) {
            			if (ioObject != null
            				&&ioObject.getSource() == null) {

            				ioObject.setSource(getName());
            			}
            		}
            	}
            }
     
            Container outputContainer = inputContainer.append(output);
            this.inputContainer = null;
			return outputContainer;
	}

	protected <T extends ConsumerProducer> T getInput(Class<T> cls){
		return inputContainer.get(cls, 0);
	}


	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public String toString() {
		String type = null;

		return name + " (" + type + ")";
	}

}

