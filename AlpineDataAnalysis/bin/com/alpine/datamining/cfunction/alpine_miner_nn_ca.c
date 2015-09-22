#include "postgres.h"
#include "fmgr.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/lsyscache.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

bool alpine_miner_deconstruct_array(ArrayType  *arg,Datum **data, bool **nulls,int *count);

PG_FUNCTION_INFO_V1(alpine_miner_nn_ca_o);

Datum
alpine_miner_nn_ca_o(PG_FUNCTION_ARGS)
{

	ArrayType  *weight_arg, *columns_arg, *input_range_arg, *input_base_arg,*hidden_node_number_arg, *result;
    Datum     *weight_data, *columns_data, *input_range_data, *input_base_data, *hidden_node_number_data, *result_data;
    bool       *weight_nulls, *columns_nulls, *input_range_nulls, *input_base_nulls, *hidden_node_number_nulls,*result_nulls;
	int         weight_count, columns_count, input_range_count, input_base_count, hidden_node_number_count,result_count ;
    Oid         result_eltype;
	int16 result_typlen;
	bool result_typbyval;
	char result_typalign;

	double output_range_arg,output_base_arg;
	int hidden_layer_number_arg, output_node_no_arg;
	bool normalize_arg, numerical_label_arg ;
        int         ndims, *dims, *lbs;

	int i;
	int j;
	int k;

	int all_hidden_node_count;

	int weight_index;
	int hidden_node_number_index = 0;

	bool null_data;
	double * input;
	double * output;
	int * hidden_node_number;
	double *weight;
	double *hidden_node_output;
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3) || PG_ARGISNULL(4) || PG_ARGISNULL(5) || PG_ARGISNULL(6) || PG_ARGISNULL(7) || PG_ARGISNULL(8) || PG_ARGISNULL(9) || PG_ARGISNULL(10))
	{
		PG_RETURN_NULL();
	}

        /* get weight_arg args */
        weight_arg = PG_GETARG_ARRAYTYPE_P(0);

	null_data = alpine_miner_deconstruct_array(weight_arg, &weight_data, &weight_nulls,&weight_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

        columns_arg = PG_GETARG_ARRAYTYPE_P(1);
	null_data = alpine_miner_deconstruct_array(columns_arg, &columns_data, &columns_nulls,&columns_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

    /* get input_range_arg args */
    input_range_arg = PG_GETARG_ARRAYTYPE_P(2);
	null_data = alpine_miner_deconstruct_array(input_range_arg, &input_range_data, &input_range_nulls,&input_range_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

    /* get input_base_arg args */
    input_base_arg = PG_GETARG_ARRAYTYPE_P(3);
	null_data = alpine_miner_deconstruct_array(input_base_arg, &input_base_data, &input_base_nulls,&input_base_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

    /* get hidden_node_number_arg args */
    hidden_node_number_arg = PG_GETARG_ARRAYTYPE_P(4);
	null_data = alpine_miner_deconstruct_array(hidden_node_number_arg, &hidden_node_number_data, &hidden_node_number_nulls,&hidden_node_number_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

	hidden_layer_number_arg= PG_GETARG_INT32(5);

	output_range_arg = PG_GETARG_FLOAT8(6);
	output_base_arg = PG_GETARG_FLOAT8(7);
	output_node_no_arg  = PG_GETARG_INT32(8);
	normalize_arg = PG_GETARG_BOOL(9); 
	numerical_label_arg = PG_GETARG_BOOL(10); 

#ifdef ALPINE_DEBUG 
	elog(WARNING,"%f",DatumGetFloat8(columns_data[0]));
#endif
	input =  (double*)palloc(columns_count * sizeof(double));;




	output = (double*)palloc(output_node_no_arg * sizeof(double));
	hidden_node_number = (int*) palloc(hidden_node_number_count * sizeof(int));


	weight = (double*)palloc(weight_count * sizeof(double));
	for (i = 0; i < weight_count; i++)
	{
		weight[i] = DatumGetFloat8(weight_data[i]);
	}
	all_hidden_node_count = 0;
	for (i = 0; i < hidden_layer_number_arg; i++)
	{
		hidden_node_number[i] = DatumGetInt32(hidden_node_number_data[i]);
		all_hidden_node_count += hidden_node_number[i];
	}

	hidden_node_output = (double*)palloc(all_hidden_node_count * sizeof(double));


        /* get output array element type */
	result_eltype = FLOAT8OID;
        get_typlenbyvalalign(result_eltype, &result_typlen, &result_typbyval, &result_typalign);

        /* construct result array */
	result_count = output_node_no_arg;
        result_data = (Datum *)palloc(result_count * sizeof(Datum));
        result_nulls = (bool *)palloc(result_count * sizeof(bool));
	for (i = 0; i < result_count; i++)
	{
		result_nulls[i] = false;
	}

	//caculate input 
        if (normalize_arg) 
        {
            i = 0;
            while (i < columns_count)
            {
                if (DatumGetFloat8(input_range_data[i]) != 0)
                {
                   input[i] = ((DatumGetFloat8(columns_data[i])-DatumGetFloat8(input_base_data[i]))/DatumGetFloat8(input_range_data[i]));
		}
                else
		{
                   input[i] = (DatumGetFloat8(columns_data[i])-DatumGetFloat8(input_base_data[i]));
                }
#ifdef ALPINE_DEBUG 
		elog(WARNING, "input:%f", input[i]);
#endif
		i = i + 1;
            }
	}
        else
	{
		i = 0;
        	while (i < columns_count)
		{
               	    input[i] = DatumGetFloat8(columns_data[i]);
               	    i = i + 1;
        	}
	}

	// caculate hidden node output of 1st layer 
        i = 0;
        while (i < hidden_node_number[0])
	{
                hidden_node_output[i] = weight[0+i*(columns_count + 1)];
                j = 0;
                while (j < columns_count)
		{
                        hidden_node_output[i] = hidden_node_output[i]+input[j]*weight[1 + j  + i *(columns_count + 1)];
#ifdef ALPINE_DEBUG 
			elog(WARNING,"hiddensum[%d] input[%d] %f weight[%d] %f", i, j,input[j] , 1+j  +(i)*(columns_count +1), weight[1+j +i*(columns_count + 1)]);
#endif
                        j = j + 1;
		}

		if (hidden_node_output[i] < -45.0)
		{
			hidden_node_output[i] = 0;
		}
		else if (hidden_node_output[i] > 45.0)
		{
			hidden_node_output[i] = 1;
		}
		else
		{
            	    hidden_node_output[i] = (1.0/(1.0+exp( -1.0 * hidden_node_output[i])));
		}
#ifdef ALPINE_DEBUG 
		 elog(WARNING,"hidden[%d] %f", i, hidden_node_output[i]);
#endif
                i = i + 1;
	}
//       calculate hidden layer 2~last output
        weight_index = hidden_node_number[0] * (columns_count + 1) ;

        if (hidden_layer_number_arg > 1)
	{
	        hidden_node_number_index = 0;
	        i = 1;
	        while (i < hidden_layer_number_arg )
		{
	                hidden_node_number_index = hidden_node_number_index + hidden_node_number[i - 1];
	                j = 0;
	                while (j < hidden_node_number[i]) 
			{
	                        hidden_node_output[hidden_node_number_index + j] = weight[weight_index + (hidden_node_number[i - 1] +1) * j];
	                        k = 0;
	                        while (k < hidden_node_number[i - 1])
				{ 
	                                hidden_node_output[hidden_node_number_index + j] = hidden_node_output[hidden_node_number_index + j]+hidden_node_output[hidden_node_number_index - hidden_node_number[i - 1] + k]*weight[weight_index + (hidden_node_number[i - 1] +1) * j + k + 1];
	                                k = k + 1;
				}
				if (hidden_node_output[hidden_node_number_index + j] < -45.0)
				{
					hidden_node_output[hidden_node_number_index + j] = 0;
				}
				else if (hidden_node_output[hidden_node_number_index + j] > 45.0)
				{
					hidden_node_output[hidden_node_number_index + j] = 1;
				}
				else
				{
	                        	hidden_node_output[hidden_node_number_index + j] = (1.0/(1+exp(-1.0*hidden_node_output[hidden_node_number_index+j])));
				}
	                        j = j + 1;
	                }
	                weight_index = weight_index + hidden_node_number[i] * (hidden_node_number[i - 1] + 1);
	                i = i + 1;
	       }
        }

	//compute output value of  output node;
        i = 0;
        while (i < output_node_no_arg)
	{
                output[i] = weight[weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i];
#ifdef ALPINE_DEBUG 
		elog(WARNING,"weightindex:%d,weight[%d] %f",weight_index, weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i, output[i]);
#endif
                j = 0;
                while (j < hidden_node_number[hidden_layer_number_arg-1])
		{
#ifdef ALPINE_DEBUG 
			elog(WARNING,"ouput[%d]:%f ,hidden_node_number_index:%d,j:%d, weight[%d]:%f",i,output[i], hidden_node_number_index ,j,1+j + weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i , weight[1 + j + weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i ]);
#endif
                        output[i] = output[i]+hidden_node_output[hidden_node_number_index + j]
					* weight[1 + j + weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i ];
                        j = j + 1;
                }
#ifdef ALPINE_DEBUG 
		 elog(WARNING,"ouputsum[%d] %f", i, output[i]);
#endif
		if (numerical_label_arg)
		{
                	output[i] = ((output[i]) * output_range_arg+output_base_arg);
#ifdef ALPINE_DEBUG 
			elog(WARNING,"output:%f, %f,%f",output[i],output_range_arg,output_base_arg);
#endif
		}
		else
		{
			if (output[i] < -45.0)
			{
				output[i] = 0;
			}
			else if (output[i] > 45.0)
			{
				output[i] = 1;
			}
			else
			{
				output[i] = (1.0/(1+exp(-1.0*output[i])));
			}
		}
#ifdef ALPINE_DEBUG 
		 elog(WARNING,"ouputsum2[%d] %f", i, output[i]);
#endif
                i = i + 1;
	}



	ndims = 1;
	dims = (int *) palloc(sizeof(int));
	dims[0] = result_count;
	lbs = (int *) palloc(sizeof(int));
	lbs[0] = 1;
	for (i = 0; i < output_node_no_arg; i++)
	{
		result_data[i] = Float8GetDatum(output[i]);
#ifdef ALPINE_DEBUG 
		elog(WARNING,"output[%d]:%f",i, output[i]);
#endif
	}

	result = construct_md_array((void *)result_data, result_nulls, ndims, dims,
			lbs, result_eltype, result_typlen, result_typbyval, result_typalign);

//	pfree(result);
	pfree(weight_data);
	pfree(columns_data);
	pfree(input_range_data);
	pfree(input_base_data);
	pfree(hidden_node_number_data);
	pfree(result_data);
	pfree(weight_nulls);
	pfree(columns_nulls);
	pfree(input_range_nulls);
	pfree(input_base_nulls);
	pfree(hidden_node_number_nulls);
	pfree(result_nulls);
	pfree(input);
	pfree(output);
	pfree(lbs);
	pfree(dims);
	pfree(hidden_node_output);
	pfree(weight);
	pfree(hidden_node_number);

        PG_RETURN_ARRAYTYPE_P(result);
}



PG_FUNCTION_INFO_V1(alpine_miner_nn_ca_change);

Datum
alpine_miner_nn_ca_change(PG_FUNCTION_ARGS)
{
    ArrayType  *weight_arg, *columns_arg, *input_range_arg, *input_base_arg,*hidden_node_number_arg, *result;
    Datum     *weight_data, *columns_data, *input_range_data, *input_base_data, *hidden_node_number_data, *result_data;
    bool       *weight_nulls, *columns_nulls, *input_range_nulls, *input_base_nulls, *hidden_node_number_nulls,*result_nulls;
	int         weight_count, columns_count, input_range_count, input_base_count, hidden_node_number_count,result_count ;
        Oid         result_eltype;
	int16 result_typlen;
	bool result_typbyval;
	char result_typalign;

	double output_range_arg,output_base_arg, learning_rate_ar, label_arg;
	int hidden_layer_number_arg, output_node_no_arg, set_size_arg;
	bool normalize_arg, numerical_label_arg ;
        int         ndims, *dims, *lbs;

	int i;
	int j;
	int k;

	int all_hidden_node_count;

	int weight_index;
	int hidden_node_number_index = 0;

	bool null_data;
	double * input;
	double * output;
	int * hidden_node_number;
	double *weight;
	double *hidden_node_output;

	double delta = 0.0;
	double total_error = 0.0;
	double * output_error;
	double direct_output_error = 0.0;

	double * hidden_node_error;
	double error_sum = 0.0;

	double current_change = 0.0;
	double threshold_change = 0.0;

	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3) || PG_ARGISNULL(4) || PG_ARGISNULL(5) || PG_ARGISNULL(6) || PG_ARGISNULL(7) || PG_ARGISNULL(8) || PG_ARGISNULL(9) || PG_ARGISNULL(10) || PG_ARGISNULL(11) || PG_ARGISNULL(12)){
		PG_RETURN_NULL();
	}

        /* get weight_arg args */
        weight_arg = PG_GETARG_ARRAYTYPE_P(0);

	null_data = alpine_miner_deconstruct_array(weight_arg, &weight_data, &weight_nulls,&weight_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

        columns_arg = PG_GETARG_ARRAYTYPE_P(1);
	null_data = alpine_miner_deconstruct_array(columns_arg, &columns_data, &columns_nulls,&columns_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

        /* get input_range_arg args */
        input_range_arg = PG_GETARG_ARRAYTYPE_P(2);
	null_data = alpine_miner_deconstruct_array(input_range_arg, &input_range_data, &input_range_nulls,&input_range_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

        /* get input_base_arg args */
        input_base_arg = PG_GETARG_ARRAYTYPE_P(3);
	null_data = alpine_miner_deconstruct_array(input_base_arg, &input_base_data, &input_base_nulls,&input_base_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

        /* get hidden_node_number_arg args */
        hidden_node_number_arg = PG_GETARG_ARRAYTYPE_P(4);
	null_data = alpine_miner_deconstruct_array(hidden_node_number_arg, &hidden_node_number_data, &hidden_node_number_nulls,&hidden_node_number_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

	hidden_layer_number_arg= PG_GETARG_INT32(5);

	output_range_arg = PG_GETARG_FLOAT8(6);
	output_base_arg = PG_GETARG_FLOAT8(7);
	output_node_no_arg  = PG_GETARG_INT32(8);
	normalize_arg = PG_GETARG_BOOL(9); 
	numerical_label_arg = PG_GETARG_BOOL(10); 
	label_arg = PG_GETARG_FLOAT8(11);
	set_size_arg = PG_GETARG_INT32(12);
	if (set_size_arg <= 0)
	{
		set_size_arg = 1;
	}

#ifdef ALPINE_DEBUG 
	elog(WARNING,"%f",DatumGetFloat8(columns_data[0]));
#endif
	input =  (double*)palloc(columns_count * sizeof(double));;

	output = (double*)palloc(output_node_no_arg * sizeof(double));
	hidden_node_number = (int*) palloc(hidden_node_number_count * sizeof(int));

	weight = (double*)palloc(weight_count * sizeof(double));
	for (i = 0; i < weight_count; i++)
	{
		weight[i] = DatumGetFloat8(weight_data[i]);
	}
	all_hidden_node_count = 0;
	for (i = 0; i < hidden_layer_number_arg; i++)
	{
		hidden_node_number[i] = DatumGetInt32(hidden_node_number_data[i]);
		all_hidden_node_count += hidden_node_number[i];
	}

	hidden_node_output = (double*)palloc(all_hidden_node_count * sizeof(double));


	//caculate input 
        if (normalize_arg) 
        {
            i = 0;
            while (i < columns_count)
            {
                if (DatumGetFloat8(input_range_data[i]) != 0)
                {
                   input[i] = ((DatumGetFloat8(columns_data[i])-DatumGetFloat8(input_base_data[i]))/DatumGetFloat8(input_range_data[i]));
		}
                else
		{
                   input[i] = (DatumGetFloat8(columns_data[i])-DatumGetFloat8(input_base_data[i]));
                }
#ifdef ALPINE_DEBUG 
		elog(WARNING, "input:%f", input[i]);
#endif
		i = i + 1;
            }
	}
        else
	{
		i = 0;
        	while (i < columns_count)
		{
               	    input[i] = DatumGetFloat8(columns_data[i]);
               	    i = i + 1;
        	}
	}

	// caculate hidden node output of 1st layer 
        i = 0;
        while (i < hidden_node_number[0])
	{
                hidden_node_output[i] = weight[0+i*(columns_count + 1)];
                j = 0;
                while (j < columns_count)
		{
                        hidden_node_output[i] = hidden_node_output[i]+input[j]*weight[1 + j  + i *(columns_count + 1)];
#ifdef ALPINE_DEBUG 
			elog(WARNING,"hiddensum[%d] input[%d] %f weight[%d] %f", i, j,input[j] , 1+j  +(i)*(columns_count +1), weight[1+j +i*(columns_count + 1)]);
#endif
                        j = j + 1;
		}

		if (hidden_node_output[i] < -45.0)
		{
			hidden_node_output[i] = 0;
		}
		else if (hidden_node_output[i] > 45.0)
		{
			hidden_node_output[i] = 1;
		}
		else
		{
            	    hidden_node_output[i] = (1.0/(1.0+exp( -1.0 * hidden_node_output[i])));
		}
#ifdef ALPINE_DEBUG 
		 elog(WARNING,"hidden[%d] %f", i, hidden_node_output[i]);
#endif
                i = i + 1;
	}
//       calculate hidden layer 2~last output
        weight_index = hidden_node_number[0] * (columns_count + 1) ;

        if (hidden_layer_number_arg > 1)
	{
	        hidden_node_number_index = 0;
	        i = 1;
	        while (i < hidden_layer_number_arg )
		{
	                hidden_node_number_index = hidden_node_number_index + hidden_node_number[i - 1];
	                j = 0;
	                while (j < hidden_node_number[i]) 
			{
	                        hidden_node_output[hidden_node_number_index + j] = weight[weight_index + (hidden_node_number[i - 1] +1) * j];
	                        k = 0;
	                        while (k < hidden_node_number[i - 1])
				{ 
	                                hidden_node_output[hidden_node_number_index + j] = hidden_node_output[hidden_node_number_index + j]+hidden_node_output[hidden_node_number_index - hidden_node_number[i - 1] + k]*weight[weight_index + (hidden_node_number[i - 1] +1) * j + k + 1];
	                                k = k + 1;
				}
				if (hidden_node_output[hidden_node_number_index + j] < -45.0)
				{
					hidden_node_output[hidden_node_number_index + j] = 0;
				}
				else if (hidden_node_output[hidden_node_number_index + j] > 45.0)
				{
					hidden_node_output[hidden_node_number_index + j] = 1;
				}
				else
				{
	                        	hidden_node_output[hidden_node_number_index + j] = (1.0/(1+exp(-1.0*hidden_node_output[hidden_node_number_index+j])));
				}
	                        j = j + 1;
	                }
	                weight_index = weight_index + hidden_node_number[i] * (hidden_node_number[i - 1] + 1);
	                i = i + 1;
	       }
        }

	//compute output value of  output node;
        i = 0;
        while (i < output_node_no_arg)
		{
                output[i] = weight[weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i];
#ifdef ALPINE_DEBUG 
		elog(WARNING,"weightindex:%d,weight[%d] %f",weight_index, weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i, output[i]);
#endif
                j = 0;
                while (j < hidden_node_number[hidden_layer_number_arg-1])
		{
#ifdef ALPINE_DEBUG 
			elog(WARNING,"ouput[%d]:%f ,hidden_node_number_index:%d,j:%d, weight[%d]:%f",i,output[i], hidden_node_number_index ,j,1+j + weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i , weight[1 + j + weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i ]);
#endif
                        output[i] = output[i]+hidden_node_output[hidden_node_number_index + j]
					* weight[1 + j + weight_index + (hidden_node_number[hidden_layer_number_arg-1]+1) * i ];
                        j = j + 1;
				}
#ifdef ALPINE_DEBUG 
		 elog(WARNING,"ouputsum[%d] %f", i, output[i]);
#endif
		if (numerical_label_arg)
		{
                	output[i] = ((output[i]) * output_range_arg+output_base_arg);
#ifdef ALPINE_DEBUG 
			elog(WARNING,"output:%f, %f,%f",output[i],output_range_arg,output_base_arg);
#endif
		}
		else
		{
			if (output[i] < -45.0)
			{
				output[i] = 0;
			}
			else if (output[i] > 45.0)
			{
				output[i] = 1;
			}
			else
			{
				output[i] = (1.0/(1+exp(-1.0*output[i])));
			}
		}
#ifdef ALPINE_DEBUG 
		 elog(WARNING,"ouputsum2[%d] %f", i, output[i]);
#endif
                i = i + 1;
	}

	/* get output array element type */
	result_eltype = FLOAT8OID;
	get_typlenbyvalalign(result_eltype, &result_typlen, &result_typbyval, &result_typalign);

	/* construct result array */
	result_count = weight_count + 1;
	result_data = (Datum *)palloc(result_count * sizeof(Datum));
	result_nulls = (bool *)palloc(result_count * sizeof(bool));
	for (i = 0; i < result_count; i++)
	{
		result_nulls[i] = false;
	}
	//compute error of output node
	output_error = (double *)palloc(output_node_no_arg * sizeof(double));
	for(i = 0; i < output_node_no_arg; i++)
	{
		if(numerical_label_arg)
		{
			if (output_range_arg == 0.0)
			{
				direct_output_error = 0.0;
			}
			else
			{
				direct_output_error = (label_arg - output[i])/output_range_arg;
			}
			output_error[i] = direct_output_error;
		}
		else
		{
			if (((int)label_arg) == i)
			{
				direct_output_error = 1.0 - output[i];
			}
			else
			{
				direct_output_error = 0.0 - output[i];
			}
#ifdef ALPINE_DEBUG 
		elog(WARNING,"label_arg :%f %d %d", label_arg, i, (((int)label_arg) == i));
#endif
			output_error[i] = direct_output_error * output[i] * (1- output[i]);
		}
		total_error += direct_output_error*direct_output_error;
#ifdef ALPINE_DEBUG 
		elog(WARNING,"output_error[%d] %f  totalerror:%f", i, output_error[i], total_error);
#endif
	}

	//compute hidden_node_error of last layer hidden_node----
	hidden_node_error = (double*)palloc(all_hidden_node_count * sizeof(double));
	weight_index = weight_count - output_node_no_arg*(hidden_node_number[hidden_layer_number_arg - 1] + 1)  ;
	hidden_node_number_index = all_hidden_node_count - hidden_node_number[hidden_layer_number_arg - 1];
	for(i = 0; i < hidden_node_number[hidden_layer_number_arg - 1]; i++)
	{
		error_sum = 0.0;
		for(k = 0; k < output_node_no_arg; k++)
		{
			error_sum = error_sum+output_error[k]*weight[weight_index + (hidden_node_number[hidden_layer_number_arg - 1] + 1)*k + i + 1];
#ifdef ALPINE_DEBUG 
		elog(WARNING,"output_error[%d]:%f,weight[%d]:%f",k,output_error[k],weight_index + (hidden_node_number[hidden_layer_number_arg - 1] + 1)*k + i + 1, weight[weight_index + (hidden_node_number[hidden_layer_number_arg - 1] + 1)*k + i + 1]);
#endif
		}
		hidden_node_error[hidden_node_number_index + i] = error_sum*hidden_node_output[hidden_node_number_index + i]*(1.0-hidden_node_output[hidden_node_number_index + i]);
#ifdef ALPINE_DEBUG 
		elog(WARNING,"hidden_node_error[%d] %f ", hidden_node_number_index+i, hidden_node_error[hidden_node_number_index + i]);
#endif
	}

	//compute hidden_node_error  of 1 layer to the one before last layer hidden node
	if (hidden_layer_number_arg > 1)
	{
		weight_index = weight_index - (hidden_node_number[hidden_layer_number_arg - 2] + 1)*hidden_node_number[hidden_layer_number_arg - 1];
		hidden_node_number_index = hidden_node_number_index - hidden_node_number[hidden_layer_number_arg - 2];
		for(i = hidden_layer_number_arg - 2; i >= 0; i--)
		{
			for(j = 0; j < hidden_node_number[i]; j++)
			{
				error_sum = 0.0;
				for (k = 0; k < hidden_node_number[i + 1]; k++)
				{
					error_sum = error_sum+hidden_node_error[hidden_node_number_index + hidden_node_number[i] + k]*weight[weight_index + (hidden_node_number[i]+1)*(k) + j + 1];
#ifdef ALPINE_DEBUG 
		elog(WARNING,"i:%d j:%d k:%d; hidden_node_error[%d]:%f,weight[%d]:%f",i,j,k,hidden_node_number_index + hidden_node_number[i] + k, hidden_node_error[hidden_node_number_index + hidden_node_number[i] + k], weight_index + (hidden_node_number[i]+1)*(k) + j + 1,weight[weight_index + (hidden_node_number[i]+1)*(k) + j + 1]);
#endif
				}
				hidden_node_error[hidden_node_number_index + j] = error_sum*hidden_node_output[hidden_node_number_index + j]*(1-hidden_node_output[hidden_node_number_index + j]);
#ifdef ALPINE_DEBUG 
                elog(WARNING,"hidden_node_error[%d] %f ", hidden_node_number_index+j, hidden_node_error[hidden_node_number_index + j]);
#endif
			}
			weight_index = weight_index - (hidden_node_number[i - 1]+1) * hidden_node_number[i];
			hidden_node_number_index = hidden_node_number_index - hidden_node_number[i - 1];
		}
	}

	//compute weight change of output node
	weight_index = weight_count - (hidden_node_number[hidden_layer_number_arg - 1]+ 1)* output_node_no_arg;
	hidden_node_number_index = all_hidden_node_count - hidden_node_number[hidden_layer_number_arg - 1];
	for(i = 0; i < output_node_no_arg; i++)
	{
		delta = 1.0/set_size_arg*output_error[i];
		threshold_change = delta;
		result_data[weight_index +(hidden_node_number[hidden_layer_number_arg - 1]+ 1)*(i)] = Float8GetDatum(threshold_change);
#ifdef ALPINE_DEBUG 
                elog(WARNING, " result_data [%d] %f ", weight_index +(hidden_node_number[hidden_layer_number_arg - 1]+ 1)*(i), threshold_change);
#endif

		for(j = 0; j < hidden_node_number[hidden_layer_number_arg - 1] ; j++)
		{
			current_change = delta * hidden_node_output[hidden_node_number_index + j];
			result_data[weight_index +(hidden_node_number[hidden_layer_number_arg - 1]+ 1)*(i) + 1 +j] = Float8GetDatum(current_change);
#ifdef ALPINE_DEBUG 
                elog(WARNING, " result_data [%d] %f , i:%d j:%d output_error[%d]:%f, hidden_node_output[%d]:%f", weight_index +(hidden_node_number[hidden_layer_number_arg - 1]+ 1)*(i) + 1+ j, current_change, i,j,i,output_error[i],hidden_node_number_index + j, hidden_node_output[hidden_node_number_index + j]);
#endif
		}
	}

	//compute weight change of hidden node last layer  to  2 layer
	if (hidden_layer_number_arg > 1)
	{
		for(i = hidden_layer_number_arg - 1; i >= 1; i--)
		{
			weight_index = weight_index - (hidden_node_number[i - 1]+1)*hidden_node_number[i];
			hidden_node_number_index = hidden_node_number_index - hidden_node_number[i - 1];
			delta = 0.0;
			for (j = 0; j < hidden_node_number[i]; j++)
			{
				delta = (1.0/set_size_arg*hidden_node_error[hidden_node_number_index + hidden_node_number[i - 1] + j]);
				threshold_change = delta;
				result_data[weight_index + (hidden_node_number[i - 1] + 1) * (j)] = Float8GetDatum(threshold_change);
#ifdef ALPINE_DEBUG 
                elog(WARNING, " result_data [%d] %f ", weight_index + (hidden_node_number[i - 1] + 1) * (j), threshold_change);
#endif
				for(k = 0; k < hidden_node_number[i - 1]; k++)
				{
					current_change = delta * hidden_node_output[hidden_node_number_index + k];
					result_data[weight_index + (hidden_node_number[i - 1] + 1) * (j) + 1 +  k] = Float8GetDatum(current_change);
#ifdef ALPINE_DEBUG 
                elog(WARNING, " result_data [%d] %f i:%d, j:%d k:%d hidden_node_error[%d]:%f hidden_node_output[%d]:%f", weight_index + (hidden_node_number[i - 1] + 1) * (j) + 1 +  k, current_change,i,j,k,hidden_node_number_index + hidden_node_number[i - 1] + j, hidden_node_error[hidden_node_number_index + hidden_node_number[i - 1] + j], hidden_node_number_index + k,hidden_node_output[hidden_node_number_index + k]);
#endif
				}
			}
		}
	}


	//compute weight change of first layer hidden node
	weight_index = 0; 
	hidden_node_number_index = 0;
	delta = 0.0;
	for(j = 0; j < hidden_node_number[0]; j++)
	{
		delta = 1.0/set_size_arg*hidden_node_error[hidden_node_number_index + j];
		threshold_change = delta;
		result_data[weight_index + (columns_count+1)*(j)] = Float8GetDatum(threshold_change);
#ifdef ALPINE_DEBUG 
                elog(WARNING, " result_data [%d] %f ", weight_index + (columns_count+1)*(j), threshold_change);
#endif
		for(k = 0; k < columns_count; k++)
		{
			current_change = delta*input[k];
			result_data[weight_index +  (columns_count+1)*(j) + k + 1] = Float8GetDatum(current_change);
#ifdef ALPINE_DEBUG 
                elog(WARNING, " result_data [%d] %f j:%d k:%d hidden_node_error[%d]:%f input[%d]:%f", weight_index +  (columns_count+1)*(j) + k + 1, current_change,j,k,hidden_node_number_index + j, hidden_node_error[hidden_node_number_index + j], k, input[k]);
#endif
		}
	}

	result_data[weight_count] = Float8GetDatum(total_error);
	ndims = 1;
	dims = (int *) palloc(sizeof(int));
	dims[0] = result_count;
	lbs = (int *) palloc(sizeof(int));
	lbs[0] = 1;

	result = construct_md_array((void *)result_data, result_nulls, ndims, dims,
			lbs, result_eltype, result_typlen, result_typbyval, result_typalign);

//	pfree(result);
	pfree(weight_data);
	pfree(columns_data);
	pfree(input_range_data);
	pfree(input_base_data);
	pfree(hidden_node_number_data);
	pfree(result_data);
	pfree(weight_nulls);
	pfree(columns_nulls);
	pfree(input_range_nulls);
	pfree(input_base_nulls);
	pfree(hidden_node_number_nulls);
	pfree(result_nulls);
	pfree(input);
	pfree(output);
	pfree(lbs);
	pfree(dims);
	pfree(hidden_node_output);
	pfree(weight);
	pfree(hidden_node_number);

	pfree(hidden_node_error);
	pfree(output_error);

        PG_RETURN_ARRAYTYPE_P(result);
}
