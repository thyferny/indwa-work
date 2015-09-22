#include <math.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <oci.h>
#include <ociextp.h>

static double getDoubleElemFromColl(OCIEnv *env,OCIError * err, OCIColl * coll, int index){
	sword        status;
	OCIInd       *elemind;
	void * elem_number;
	boolean exists;
	status =  OCICollGetElem(env, err, coll,
			index, &exists, (void**)&elem_number,
			(void **)&elemind);
	double elem = 0;
	OCINumberToReal ( err,
			(OCINumber *)elem_number,
			sizeof(double),
			&elem);
	return elem;
}
static int getIntElemFromColl(OCIEnv *env,OCIError * err, OCIColl * coll, int index){
	sword        status;
	OCIInd       *elemind;
	void * elem_number;
	boolean exists;
	status =  OCICollGetElem(env, err, coll,
			index, &exists, (void**)&elem_number,
			(void **)&elemind);
	int elem = 0;
	OCINumberToInt ( err,
			(OCINumber *)elem_number,
			sizeof(int),
			OCI_NUMBER_SIGNED,
			&elem);
	return elem;
}
static int appendDoubleElemToColl(OCIEnv *env,OCIError * err, OCIColl * coll, double elem){
	OCINumber elem_number;
	sword status = OCINumberFromReal(err, &elem, sizeof(elem), &elem_number);
	//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
	OCICollAppend(env, err, (const void  *)&elem_number,
			NULL, coll);
	return 1;
}
static int assignDoubleElemToColl(OCIEnv *env,OCIError * err, OCIColl * coll, double elem, int index){
	OCINumber elem_number;
	sword status = OCINumberFromReal(err, &elem, sizeof(elem), &elem_number);
	//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
	OCICollAssignElem(env, err, index, (const void  *)&elem_number,
			NULL, coll);
	return 1;
}
static int  alpine_miner_faa2fa(OCIEnv *env,OCIError * err,OCIColl * f_arrayarray, OCIColl ** f_array)
{

	//OCIEnv        *env;
	OCIServer     *srv;
	OCISvcCtx     *svc;
	//OCIError      *err;
	OCIType           *tdo = (OCIType *)0;
	int i = 0;         
	int j = 0; 
	int k = 0;
	OCIColl ** ret = f_array;

	sword        status;
	OCIString    *client_elem;
	boolean      eoc;
	void         *elem;
	void         *elem_null;
	OCIInd       *elemind;


	OCIEnv       *env2;
	OCIError     *err2;
	text         *text_ptr2;
	sword        status2;
	OCIColl     *clients2;
	OCIString    *client_elem2;
	OCIIter      *iterator2;
	boolean      eoc2;
	void         *elem2;
	void         *elem_null2;
	OCIInd       *elemind2;

	OCIColl *inner_coll;
	OCIColl *inner_coll1;
	OCIIter *itr1, *itr2;
	double *instance;
	OCIType *coll_tdo= (OCIType *)0;
	int        errcode; 
	checkerr(err, OCIIterCreate(env, err, f_arrayarray, &itr1));
	for(eoc = FALSE;!OCIIterNext(env, err, itr1, (void  **) &elem,(void  **) &elem_null, &eoc) && !eoc;)
	{
		inner_coll = (OCIColl *)elem;
		inner_coll1 = (OCIColl *)elem;
		checkerr(err, OCIIterCreate(env, err, inner_coll, &itr2));
		for(eoc2 = FALSE;!OCIIterNext(env, err, itr2, (void  **)&elem2,(void  **) &elem_null2, &eoc2) && !eoc2;)
		{
			OCICollAppend(env, err, elem2,
					NULL, *ret);
		}
		checkerr(err, OCIIterDelete(env, err, &itr2));
	}
	checkerr(err, OCIIterDelete(env, err, &itr1));
	return 1;
}
//OCIArray *
int alpine_miner_nn_ca_o(
	OCIExtProcContext *ctx,
	OCIArray * weight_arrayarray,
	OCIArray *  columns_arrayarray, 
	OCIArray * input_range_arrayarray, 
	OCIArray * input_base_arrayarray,
	OCIArray * hidden_node_number,
	int hidden_layer_number,
	float output_range,
	float output_base,
	int output_node_no,
	int normalize,
	int numerical_label,
	OCIArray * weight,
	OCIArray *  columns, 
	OCIArray * input_range, 
	OCIArray * input_base,
	OCIArray *input,
	OCIArray *hidden_node_output,
	OCIArray ** return_array
)
{

	OCIEnv       *env;
	OCIError     *err;
	sword        status;
	void         *elem;
	OCIInd       *elemind;

        OCIServer     *srv;
        OCISvcCtx     *svc;
        sword errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err);
	alpine_miner_faa2fa(env,err,weight_arrayarray ,&weight);
	alpine_miner_faa2fa(env,err,columns_arrayarray ,&columns);
	alpine_miner_faa2fa(env,err,input_range_arrayarray ,&input_range);
	alpine_miner_faa2fa(env,err,input_base_arrayarray ,&input_base);
	int i;
	int j;
	int k;

	int all_hidden_node_count;

	int weight_index;
	int hidden_node_number_index = 0;

	boolean null_data;

	OCIArray ** output = return_array;
	//output = (double*)palloc(output_node_no * sizeof(double));
	//OCIArray * hidden_node_number;
	//hidden_node_number = (int*) palloc(hidden_node_number_count * sizeof(int));

	all_hidden_node_count = 0;
	for (i = 0; i < hidden_layer_number; i++)
	{
		int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, i);
		all_hidden_node_count += hidden_node_number_elem;
	}


        /* construct result array */
	int result_count = output_node_no;
	sb4 columns_count;

	OCICollSize( env, err, columns,
			&columns_count );
	//caculate input 
        if (normalize) 
        {
            i = 0;
            while (i < columns_count)
            {
	    	double input_elem;
		double input_base_elem = getDoubleElemFromColl(env,err,input_base, i);
		double columns_elem = getDoubleElemFromColl(env,err,columns, i); 
		double input_range_elem = getDoubleElemFromColl(env,err,input_range, i);
                if ((input_range_elem) != 0)
                {
                   input_elem = ((columns_elem - input_base_elem)/input_range_elem);
		}
                else
		{
                   input_elem = (columns_elem-input_base_elem);
                }
		appendDoubleElemToColl(env,err,input,input_elem);
		i = i + 1;
            }
	}
        else
	{
		i = 0;
        	while (i < columns_count)
		{
		    double columns_elem = getDoubleElemFromColl(env,err,columns, i); 
		    appendDoubleElemToColl(env,err,input,columns_elem);
               	    i = i + 1;
        	}
	}
	// caculate hidden node output of 1st layer 
        i = 0;
	int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, 0);
        while (i < hidden_node_number_elem)
	{
		double weight_elem = getDoubleElemFromColl(env,err,weight, i*(columns_count + 1)); 
		double hidden_node_output_elem = weight_elem;
                j = 0;
                while (j < columns_count)
		{
			double weight_elem = getDoubleElemFromColl(env,err,weight, 1 + j  + i *(columns_count + 1)); 
			double input_elem = getDoubleElemFromColl(env,err,input, j); 
			hidden_node_output_elem = hidden_node_output_elem + weight_elem * input_elem;
                        j = j + 1;
		}

		if (hidden_node_output_elem < -45.0)
		{
			hidden_node_output_elem = 0;
		}
		else if (hidden_node_output_elem > 45.0)
		{
			hidden_node_output_elem = 1;
		}
		else
		{
            	    hidden_node_output_elem = (1.0/(1.0+exp( -1.0 * hidden_node_output_elem)));
		}
		appendDoubleElemToColl(env,err,hidden_node_output,hidden_node_output_elem);
                i = i + 1;
	}
//       calculate hidden layer 2~last output
	hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, 0);
        weight_index = hidden_node_number_elem * (columns_count + 1) ;

        if (hidden_layer_number > 1)
	{
	        hidden_node_number_index = 0;
	        i = 1;
	        while (i < hidden_layer_number )
		{
			int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, i-1);
			int hidden_node_number_elem_i = getIntElemFromColl(env,err,hidden_node_number, i);
	                hidden_node_number_index = hidden_node_number_index + hidden_node_number_elem;
	                j = 0;
	                while (j < hidden_node_number_elem_i) 
			{
				double weight_elem = getDoubleElemFromColl(env,err,weight, weight_index + (hidden_node_number_elem +1) * j); 
				double hidden_node_output_elem = weight_elem;
	                        k = 0;
	                        while (k < hidden_node_number_elem)
				{ 
					double hidden_node_output_elem_1 = getDoubleElemFromColl(env,err,hidden_node_output, 
						hidden_node_number_index - hidden_node_number_elem + k);
					double weight_elem = getDoubleElemFromColl(env,err,weight, 
						weight_index + (hidden_node_number_elem +1) * j + k + 1);
	                                hidden_node_output_elem = hidden_node_output_elem+hidden_node_output_elem_1*weight_elem;
	                                k = k + 1;
				}
				if (hidden_node_output_elem < -45.0)
				{
					hidden_node_output_elem = 0;
				}
				else if (hidden_node_output_elem > 45.0)
				{
					hidden_node_output_elem = 1;
				}
				else
				{
	                        	hidden_node_output_elem = (1.0/(1+exp(-1.0*hidden_node_output_elem)));
				}
				appendDoubleElemToColl(env,err,hidden_node_output,hidden_node_output_elem);
	                        j = j + 1;
	                }
	                weight_index = weight_index + hidden_node_number_elem_i * (hidden_node_number_elem + 1);
	                i = i + 1;
	       }
        }

	//compute output value of  output node;
        i = 0;
        while (i < output_node_no)
	{
		hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, hidden_layer_number-1);
		double weight_elem = getDoubleElemFromColl(env,err,weight,  
				weight_index + (hidden_node_number_elem+1) * i);
		double output_elem = weight_elem;
                j = 0;
                while (j < hidden_node_number_elem)
		{
			double weight_elem = getDoubleElemFromColl(env,err,weight,  
					1 + j + weight_index + (hidden_node_number_elem+1) * i);
			double hidden_node_output_elem = getDoubleElemFromColl(env,err,hidden_node_output, 
					hidden_node_number_index + j);
                        output_elem = output_elem+hidden_node_output_elem
					* weight_elem;
                        j = j + 1;
                }
		if (numerical_label)
		{
                	output_elem = ((output_elem) * output_range+output_base);
		}
		else
		{
			if (output_elem < -45.0)
			{
				output_elem = 0;
			}
			else if (output_elem > 45.0)
			{
				output_elem = 1;
			}
			else
			{
				output_elem = (1.0/(1+exp(-1.0*output_elem)));
			}
			
		}
		appendDoubleElemToColl(env,err,*output,output_elem);
                i = i + 1;
	}

        return 1;
}


//OCIArray *  
int alpine_miner_nn_ca_change(
	OCIExtProcContext *ctx,
	OCIArray * weight_arrayarray,
	OCIArray *  columns_arrayarray, 
	OCIArray * input_range_arrayarray, 
	OCIArray * input_base_arrayarray,
	OCIArray * hidden_node_number,
	int hidden_layer_number,
	double output_range,
	double output_base,
	int output_node_no,
	int normalize,
	int numerical_label,
	double label,
	int set_size,
	OCIArray * weight,
	OCIArray *  columns, 
	OCIArray * input_range, 
	OCIArray * input_base,
	OCIArray *input,
	OCIArray *hidden_node_output,
	OCIArray * output,
	OCIArray * output_error,
	OCIArray * hidden_node_error,
	OCIArray ** return_array
	)
{

	OCIEnv       *env;
	OCIError     *err;
	sword        status;
	void         *elem;
	OCIInd       *elemind;

        OCIServer     *srv;
        OCISvcCtx     *svc;
        sword errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err);
	alpine_miner_faa2fa(env,err,weight_arrayarray ,&weight);
	alpine_miner_faa2fa(env,err,columns_arrayarray ,&columns);
	alpine_miner_faa2fa(env,err,input_range_arrayarray ,&input_range);
	alpine_miner_faa2fa(env,err,input_base_arrayarray ,&input_base);
	if (set_size <= 0)
	{
		set_size = 1;
	}
	int i;
	int j;
	int k;

	int all_hidden_node_count;

	int weight_index;
	int hidden_node_number_index = 0;

	boolean null_data;


	all_hidden_node_count = 0;
	for (i = 0; i < hidden_layer_number; i++)
	{
		int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, i);
		all_hidden_node_count += hidden_node_number_elem;
	}


        /* construct result array */
	int result_count = output_node_no;
	sb4 columns_count;

	OCICollSize( env, err, columns,
			&columns_count );
	//caculate input 
        if (normalize) 
        {
            i = 0;
            while (i < columns_count)
            {
	    	double input_elem;
		double input_base_elem = getDoubleElemFromColl(env,err,input_base, i);
		double columns_elem = getDoubleElemFromColl(env,err,columns, i); 
		double input_range_elem = getDoubleElemFromColl(env,err,input_range, i);
                if ((input_range_elem) != 0)
                {
                   input_elem = ((columns_elem - input_base_elem)/input_range_elem);
		}
                else
		{
                   input_elem = (columns_elem-input_base_elem);
                }
		appendDoubleElemToColl(env,err,input,input_elem);
		i = i + 1;
            }
	}
        else
	{
		i = 0;
        	while (i < columns_count)
		{
		    double columns_elem = getDoubleElemFromColl(env,err,columns, i); 
		    appendDoubleElemToColl(env,err,input,columns_elem);
               	    i = i + 1;
        	}
	}
	// caculate hidden node output of 1st layer 
        i = 0;
	int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, 0);
        while (i < hidden_node_number_elem)
	{
		double weight_elem = getDoubleElemFromColl(env,err,weight, i*(columns_count + 1)); 
		double hidden_node_output_elem = weight_elem;
                j = 0;
                while (j < columns_count)
		{
			double weight_elem = getDoubleElemFromColl(env,err,weight, 1 + j  + i *(columns_count + 1)); 
			double input_elem = getDoubleElemFromColl(env,err,input, j); 
			hidden_node_output_elem = hidden_node_output_elem + weight_elem * input_elem;
                        j = j + 1;
		}

		if (hidden_node_output_elem < -45.0)
		{
			hidden_node_output_elem = 0;
		}
		else if (hidden_node_output_elem > 45.0)
		{
			hidden_node_output_elem = 1;
		}
		else
		{
            	    hidden_node_output_elem = (1.0/(1.0+exp( -1.0 * hidden_node_output_elem)));
		}
		appendDoubleElemToColl(env,err,hidden_node_output,hidden_node_output_elem);
                i = i + 1;
	}
//       calculate hidden layer 2~last output
	hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, 0);
        weight_index = hidden_node_number_elem * (columns_count + 1) ;

        if (hidden_layer_number > 1)
	{
	        hidden_node_number_index = 0;
	        i = 1;
	        while (i < hidden_layer_number )
		{
			int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, i-1);
			int hidden_node_number_elem_i = getIntElemFromColl(env,err,hidden_node_number, i);
	                hidden_node_number_index = hidden_node_number_index + hidden_node_number_elem;
	                j = 0;
	                while (j < hidden_node_number_elem_i) 
			{
				double weight_elem = getDoubleElemFromColl(env,err,weight, weight_index + (hidden_node_number_elem +1) * j); 
				double hidden_node_output_elem = weight_elem;
	                        k = 0;
	                        while (k < hidden_node_number_elem)
				{ 
					double hidden_node_output_elem_1 = getDoubleElemFromColl(env,err,hidden_node_output, 
						hidden_node_number_index - hidden_node_number_elem + k);
					double weight_elem = getDoubleElemFromColl(env,err,weight, 
						weight_index + (hidden_node_number_elem +1) * j + k + 1);
	                                hidden_node_output_elem = hidden_node_output_elem+hidden_node_output_elem_1*weight_elem;
	                                k = k + 1;
				}
				if (hidden_node_output_elem < -45.0)
				{
					hidden_node_output_elem = 0;
				}
				else if (hidden_node_output_elem > 45.0)
				{
					hidden_node_output_elem = 1;
				}
				else
				{
	                        	hidden_node_output_elem = (1.0/(1+exp(-1.0*hidden_node_output_elem)));
				}
				appendDoubleElemToColl(env,err,hidden_node_output,hidden_node_output_elem);
	                        j = j + 1;
	                }
	                weight_index = weight_index + hidden_node_number_elem_i * (hidden_node_number_elem + 1);
	                i = i + 1;
	       }
        }

	//compute output value of  output node;
        i = 0;
        while (i < output_node_no)
	{
		hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, hidden_layer_number-1);
		double weight_elem = getDoubleElemFromColl(env,err,weight,  
				weight_index + (hidden_node_number_elem+1) * i);
		double output_elem = weight_elem;
                j = 0;
                while (j < hidden_node_number_elem)
		{
			double weight_elem = getDoubleElemFromColl(env,err,weight,  
					1 + j + weight_index + (hidden_node_number_elem+1) * i);
			double hidden_node_output_elem = getDoubleElemFromColl(env,err,hidden_node_output, 
					hidden_node_number_index + j);
                        output_elem = output_elem+hidden_node_output_elem
					* weight_elem;
                        j = j + 1;
                }
		if (numerical_label)
		{
                	output_elem = ((output_elem) * output_range+output_base);
		}
		else
		{
			if (output_elem < -45.0)
			{
				output_elem = 0;
			}
			else if (output_elem > 45.0)
			{
				output_elem = 1;
			}
			else
			{
				output_elem = (1.0/(1+exp(-1.0*output_elem)));
			}
			
		}
		appendDoubleElemToColl(env,err,output,output_elem);
		//appendDoubleElemToColl(env,err,*return_array,output_elem);
                i = i + 1;
	}

	sb4 weight_count = 0;
	OCICollSize( env, err, weight,
				&weight_count );
	result_count = weight_count + 1;
	OCIColl ** result = return_array;
	int index = 0;
	for(index = 0; index < result_count; index++){
		appendDoubleElemToColl(env,err,*result,0);
	}
	
	for(index = 0; index < all_hidden_node_count; index++){
		appendDoubleElemToColl(env,err,hidden_node_error,0);
	}

	double delta = 0.0;
	//compute error of output node
	double total_error = 0.0;
	double direct_output_error = 0.0;
	for(i = 0; i < output_node_no; i++)
	{
		double output_elem = getDoubleElemFromColl(env,err,output,i);  
		double output_error_elem = 0;
		if(numerical_label != 0)
		{
			if (output_range == 0.0)
			{
				direct_output_error = 0.0;
			}
			else
			{
				direct_output_error = (label - output_elem)/output_range;
			}
			output_error_elem = direct_output_error;
		}
		else
		{
			if (((int)label) == i)
			{
				direct_output_error = 1.0 - output_elem;
			}
			else
			{
				direct_output_error = 0.0 - output_elem;
			}
			output_error_elem = direct_output_error * output_elem * (1- output_elem);
		}
		appendDoubleElemToColl(env,err,output_error,output_error_elem);
		total_error += direct_output_error*direct_output_error;
	}
	//return 1;
	//compute hidden_node_error of last layer hidden_node----
	hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, hidden_layer_number-1);
	weight_index = weight_count - output_node_no*(hidden_node_number_elem + 1)  ;
	hidden_node_number_index = all_hidden_node_count - hidden_node_number_elem;
	double error_sum = 0.0;
	for(i = 0; i < hidden_node_number_elem; i++)
	{
		error_sum = 0.0;
		for(k = 0; k < output_node_no; k++)
		{
			double output_error_elem = getDoubleElemFromColl(env,err,output_error,k);  
			double weight_elem = getDoubleElemFromColl(env,err,weight,  
					weight_index + (hidden_node_number_elem
					  + 1)*k + i + 1);
			error_sum = error_sum+output_error_elem*weight_elem;
		}
		double hidden_node_output_elem = getDoubleElemFromColl(env,err,hidden_node_output, 
			hidden_node_number_index + i);
		double hidden_node_error_elem = error_sum*hidden_node_output_elem*(1.0-hidden_node_output_elem);
		assignDoubleElemToColl(env,err,hidden_node_error,hidden_node_error_elem,hidden_node_number_index + i);
	}

	//compute hidden_node_error  of 1 layer to the one before last layer hidden node
	if (hidden_layer_number > 1)
	{
		int hidden_node_number_elem_1 = getIntElemFromColl(env,err,hidden_node_number,hidden_layer_number - 1);
		int hidden_node_number_elem_2 = getIntElemFromColl(env,err,hidden_node_number,hidden_layer_number - 2);
		weight_index = weight_index - (hidden_node_number_elem_2 + 1)*hidden_node_number_elem_1;
		hidden_node_number_index = hidden_node_number_index - hidden_node_number_elem_2;
		for(i = hidden_layer_number - 2; i >= 0; i--)
		{
			int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number,i);
			int hidden_node_number_elem_1 = getIntElemFromColl(env,err,hidden_node_number,i + 1);
			for(j = 0; j < hidden_node_number_elem; j++)
			{
				error_sum = 0.0;
				for (k = 0; k < hidden_node_number_elem_1; k++)
				{
					double hidden_node_error_elem = getDoubleElemFromColl(env,err,hidden_node_error,hidden_node_number_index + hidden_node_number_elem + k);
					double weight_elem = getDoubleElemFromColl(env,err,weight,
						weight_index + (hidden_node_number_elem+1)*(k) + j + 1);
					error_sum = error_sum+hidden_node_error_elem*weight_elem;
				}
				double hidden_node_output_elem = getDoubleElemFromColl(env,err,hidden_node_output,hidden_node_number_index + j);
				double hidden_node_error_elem = error_sum*hidden_node_output_elem*(1.0-hidden_node_output_elem);
				assignDoubleElemToColl(env,err,hidden_node_error,hidden_node_error_elem,hidden_node_number_index + j);
			}
			int hidden_node_number_elem_2 = getIntElemFromColl(env,err,hidden_node_number,i - 1);
			weight_index = weight_index - (hidden_node_number_elem_2+1) * hidden_node_number_elem;
			hidden_node_number_index = hidden_node_number_index - hidden_node_number_elem_2;
		}
	}

	//compute weight change of output node
	double current_change = 0.0;
	double threshold_change = 0.0;
	hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number,hidden_layer_number - 1);
	weight_index = weight_count - (hidden_node_number_elem+ 1)* output_node_no;
	hidden_node_number_index = all_hidden_node_count - hidden_node_number_elem;
	for(i = 0; i < output_node_no; i++)
	{
		double output_error_elem = getDoubleElemFromColl(env,err,output_error,i);  
		delta = 1.0/set_size*output_error_elem;
		threshold_change = delta;
		assignDoubleElemToColl(env,err,*result,threshold_change,weight_index +(hidden_node_number_elem+ 1)*(i));
		for(j = 0; j < hidden_node_number_elem ; j++)
		{
			double hidden_node_output_elem = getDoubleElemFromColl(env,err,hidden_node_output, 
				hidden_node_number_index + j);
			current_change = delta * hidden_node_output_elem;
			assignDoubleElemToColl(env,err,*result,current_change,
			weight_index +(hidden_node_number_elem+ 1)*(i) + 1 +j);
		}
	}

	//compute weight change of hidden node last layer  to  2 layer
	if (hidden_layer_number > 1)
	{
		for(i = hidden_layer_number - 1; i >= 1; i--)
		{
			int hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number,i);
			int hidden_node_number_elem_1 = getIntElemFromColl(env,err,hidden_node_number,i - 1);
			weight_index = weight_index - (hidden_node_number_elem_1+1)*hidden_node_number_elem;
			hidden_node_number_index = hidden_node_number_index - hidden_node_number_elem_1;
			delta = 0.0;
			for (j = 0; j < hidden_node_number_elem; j++)
			{
				double hidden_node_error_elem = getDoubleElemFromColl(env,err,hidden_node_error,hidden_node_number_index + hidden_node_number_elem_1 + j);
				delta = (1.0/set_size*hidden_node_error_elem);
				threshold_change = delta;
				assignDoubleElemToColl(env,err,*result,threshold_change,
					weight_index + (hidden_node_number_elem_1 + 1) * (j));
				for(k = 0; k < hidden_node_number_elem_1; k++)
				{
					double hidden_node_output_elem = getDoubleElemFromColl(env,err,hidden_node_output, 
						hidden_node_number_index + k);
					current_change = delta * hidden_node_output_elem;
					assignDoubleElemToColl(env,err,*result,current_change,
						weight_index + (hidden_node_number_elem_1 + 1) * (j) + 1 +  k);
				}
			}
		}
	}


	//compute weight change of first layer hidden node
	weight_index = 0; 
	hidden_node_number_index = 0;
	delta = 0.0;
	hidden_node_number_elem = getIntElemFromColl(env,err,hidden_node_number, 0);
	for(j = 0; j < hidden_node_number_elem; j++)
	{
		double hidden_node_error_elem = getDoubleElemFromColl(env,err,hidden_node_error,hidden_node_number_index + j);
		delta = 1.0/set_size*hidden_node_error_elem;
		threshold_change = delta;
		assignDoubleElemToColl(env,err,*result,threshold_change,
			weight_index + (columns_count+1)*(j));
		for(k = 0; k < columns_count; k++)
		{
			double input_elem = getDoubleElemFromColl(env,err,input,k);
			current_change = delta*input_elem;
			assignDoubleElemToColl(env,err,*result,current_change,
				weight_index +  (columns_count+1)*(j) + k + 1);
		}
	}

	assignDoubleElemToColl(env,err,*result,total_error,weight_count);

        return 1;
}

