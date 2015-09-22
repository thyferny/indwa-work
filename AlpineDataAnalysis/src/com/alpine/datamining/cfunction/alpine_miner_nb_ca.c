#include "postgres.h"
#include "fmgr.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/lsyscache.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

PG_FUNCTION_INFO_V1(alpine_miner_nb_ca_deviance);
PG_FUNCTION_INFO_V1(alpine_miner_nb_ca_confidence);
PG_FUNCTION_INFO_V1(alpine_miner_nb_ca_prediction);

enum alpine_miner_nb_array_args_index{
	alpine_miner_nominal_columns_arg_index,
	alpine_miner_nominal_columns_mapping_count_arg_index,
	alpine_miner_nominal_columns_mapping_arg_index,
	alpine_miner_nominal_columns_probability_arg_index,
	alpine_miner_dependent_column_mapping_arg_index,
	alpine_miner_dependent_column_probability_arg_index,
	alpine_miner_numerical_columns_arg_index,
	alpine_miner_numerical_columns_probability_arg_index
};

typedef struct _alpine_miner_nb_ArrayType_arg_type
{
	Oid         eltype;
	int16       typlen;
	bool        typbyval;
	char        typalign;
	ArrayType *arg;
	Datum *data;
	bool *nulls;
	int count;
} alpine_miner_nb_ArrayType_arg_type ;

typedef struct _alpine_miner_nb_args_type
{
	alpine_miner_nb_ArrayType_arg_type args[8];
} alpine_miner_nb_args_type;

bool alpine_miner_deconstruct_array(ArrayType  *arg,Datum **data, bool **nulls,int *count)
{

	Oid         eltype;
	int16       typlen;
	bool        typbyval;
	char        typalign;
	int i;

	eltype = ARR_ELEMTYPE(arg);
	get_typlenbyvalalign(eltype, &typlen, &typbyval, &typalign);
	deconstruct_array(arg, eltype, 
			typlen, typbyval, typalign,
			data, nulls, count);
	for (i = 0; i < (*count); i++)
	{
		if ((*nulls)[i])
		{
			return true;
		}
	}
	return false;
}

bool alpine_miner_deconstruct_array_ext(alpine_miner_nb_ArrayType_arg_type * arg)
{
	int i = 0;
	arg->eltype = ARR_ELEMTYPE(arg->arg);
	get_typlenbyvalalign(arg->eltype, &(arg->typlen), &(arg->typbyval), &(arg->typalign));
	deconstruct_array(arg->arg, arg->eltype, 
			arg->typlen, arg->typbyval, arg->typalign,
			&(arg->data), &(arg->nulls), &(arg->count));
	for (i = 0; i < (arg->count); i++)
	{
		if ((arg->nulls)[i])
		{
			return true;
		}
	}
	return false;
}

bool alpine_miner_nb_get_array_args(bool nominal_null, bool numerical_null, alpine_miner_nb_args_type * nb_args)
{
	bool null_data = false;
	int i = 0;
	if(!nominal_null)
	{
		for(i = alpine_miner_nominal_columns_arg_index; i <= alpine_miner_nominal_columns_probability_arg_index; i++)
		{
			null_data = alpine_miner_deconstruct_array_ext(&(nb_args->args)[i]);
			if (null_data)
			{
				return false;
			}
		}
	}

	for(i = alpine_miner_dependent_column_mapping_arg_index; i <= alpine_miner_dependent_column_probability_arg_index; i++)
	{
		null_data = alpine_miner_deconstruct_array_ext(&(nb_args->args)[i]);
		if (null_data)
		{
			return false;
		}
	}
	if(!numerical_null)
	{
		for(i = alpine_miner_numerical_columns_arg_index; i <= alpine_miner_numerical_columns_probability_arg_index; i++)
		{
			null_data = alpine_miner_deconstruct_array_ext(&(nb_args->args)[i]);
			if (null_data)
			{
				return false;
			}
		}
	}
	return true;

}

double *  alpine_miner_nb_ca_probability(bool nominal_null, bool numerical_null, alpine_miner_nb_args_type * nb_args, double * probability)
{
	int i = 0;
	int j = 0;
	int k = 0;
	int nominal_mapping_index = 0;
	int probability_column_offset = 0;

	int probability_index = 0;
	int mapping_index = 0;

	bool null_data;
	int NUMERICAL_PROBABILITY_LENGTH = 3;
	double max_log_probability = 0.0;
	double  probability_sum = 0.0;
	for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count;i++)
	{
		probability[i] = DatumGetFloat8((nb_args->args)[alpine_miner_dependent_column_probability_arg_index].data[i]);
	}
	//deal with nominal columns
	if(!nominal_null)
	{
		for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count;i++)
		{
			probability_column_offset = 0;
			probability_index = 0;
			nominal_mapping_index = 0;
			for (j = 0; j < (nb_args->args)[alpine_miner_nominal_columns_arg_index].count; j++)
			{
				probability_index = probability_column_offset + i *DatumGetInt32((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].data[j]); 
				for (k = 0; k < DatumGetInt32((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].data[j]); k++)
				{
					if(VARSIZE(DatumGetTextP((nb_args->args)[alpine_miner_nominal_columns_arg_index].data[j])) - VARHDRSZ == VARSIZE(DatumGetTextP((nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].data[nominal_mapping_index + k])) - VARHDRSZ&&
							strncmp(VARDATA(DatumGetTextP((nb_args->args)[alpine_miner_nominal_columns_arg_index].data[j])), VARDATA(DatumGetTextP((nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].data[nominal_mapping_index + k])), VARSIZE(DatumGetTextP((nb_args->args)[alpine_miner_nominal_columns_arg_index].data[j])) - VARHDRSZ) == 0)
					{
						probability[i] += DatumGetFloat8((nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].data[probability_index]);
						break;
					}
					probability_index ++;
				}
				probability_column_offset += (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count * DatumGetInt32((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].data[j]);
				nominal_mapping_index += DatumGetInt32((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].data[j]);
			}
		}
	}
	//deal with numerical columns
	if (!numerical_null)
	{
		for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count;i++)
		{
			probability_column_offset = 0;
			for(j = 0; j <  (nb_args->args)[alpine_miner_numerical_columns_arg_index].count; j++)
			{
				double base;
				probability_index = NUMERICAL_PROBABILITY_LENGTH * j * (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count;
				probability_index += NUMERICAL_PROBABILITY_LENGTH * i;
				base = (DatumGetFloat8((nb_args->args)[alpine_miner_numerical_columns_arg_index].data[j]) - DatumGetFloat8((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].data[probability_index]))
					/DatumGetFloat8((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].data[probability_index + 1]);
				probability[i] -= DatumGetFloat8((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].data[probability_index + 2]) + 0.5 * base * base;
			}
		}
	}

	for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++){
		if (i == 0 || max_log_probability  < probability[i])
		{
			max_log_probability = probability[i];
		}
	}

	for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++){
		double diff = probability[i] - max_log_probability;
		if (diff < -45)
		{
			probability[i] = 0.0000001;
		}
		else
		{
			probability[i] = exp(diff);
		}
	}
	for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++){
		probability_sum += probability[i];
	}

	for (i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++){
		probability[i] /=probability_sum;
	}
	return probability;
}
	Datum
alpine_miner_nb_ca_deviance(PG_FUNCTION_ARGS)
{

	//distributionProperty[column][labelMapping][columnMapping];
	//distributionProperty[column][labelMapping][INDEX_MEAN,INDEX_STANDARD_DEVIATION,INDEX_LOG_FACTOR];

	alpine_miner_nb_args_type * nb_args = (alpine_miner_nb_args_type *) palloc(sizeof(alpine_miner_nb_args_type));

	bool nominal_null = true;
	bool numerical_null = true;
	double * probability;
	int i = 0;
	double deviance = 0.0;
	text * dependent_column_arg = PG_GETARG_TEXT_P(4);
	if (PG_ARGISNULL(4) || PG_ARGISNULL(5) || PG_ARGISNULL(6))
	{
		PG_RETURN_NULL();
	}
	if(!PG_ARGISNULL(0) && !PG_ARGISNULL(1) && !PG_ARGISNULL(2) && !PG_ARGISNULL(3))
	{
		nominal_null = false;
	}

	if (!PG_ARGISNULL(7) && !PG_ARGISNULL(8))
	{
		numerical_null = false;
	}

	if (!nominal_null)
	{
		(nb_args->args)[alpine_miner_nominal_columns_arg_index].arg = PG_GETARG_ARRAYTYPE_P(0);
		(nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].arg= PG_GETARG_ARRAYTYPE_P(1);
		(nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].arg= PG_GETARG_ARRAYTYPE_P(2);
		(nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].arg= PG_GETARG_ARRAYTYPE_P(3);
	}
	(nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].arg= PG_GETARG_ARRAYTYPE_P(5);
	(nb_args->args)[alpine_miner_dependent_column_probability_arg_index].arg= PG_GETARG_ARRAYTYPE_P(6);
	if (!numerical_null)
	{
		(nb_args->args)[alpine_miner_numerical_columns_arg_index].arg= PG_GETARG_ARRAYTYPE_P(7);
		(nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].arg= PG_GETARG_ARRAYTYPE_P(8);
	}

	
	if(!alpine_miner_nb_get_array_args(nominal_null, numerical_null, nb_args))
	{
		PG_RETURN_NULL();
	}

	probability = (double *) palloc((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count*sizeof(double)) ;
	for ( i = 0 ; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++)
	{
		probability[i] = 0.0;
	}

	alpine_miner_nb_ca_probability(nominal_null, numerical_null, nb_args, probability);

	for(i = 0; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++)
	{
		if (VARSIZE(dependent_column_arg)-VARHDRSZ == VARSIZE(DatumGetTextP((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].data[i]))-VARHDRSZ
				&& strncmp(VARDATA(dependent_column_arg), VARDATA(DatumGetTextP((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].data[i])), VARSIZE(dependent_column_arg)-VARHDRSZ) == 0)
		{
			deviance = -2.0*log(probability[i]);
			break;
		}
	}
	if(!nominal_null)
	{
		pfree((nb_args->args)[alpine_miner_nominal_columns_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].nulls);
	}
	pfree((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].data);
	pfree((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].nulls);
	pfree((nb_args->args)[alpine_miner_dependent_column_probability_arg_index].data);
	pfree((nb_args->args)[alpine_miner_dependent_column_probability_arg_index].nulls);
	if (!numerical_null)
	{
		pfree((nb_args->args)[alpine_miner_numerical_columns_arg_index].data);
		pfree((nb_args->args)[alpine_miner_numerical_columns_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].data);
		pfree((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].nulls);
	}
	pfree(probability);
	PG_RETURN_FLOAT8(deviance);
}

	Datum
alpine_miner_nb_ca_confidence(PG_FUNCTION_ARGS)
{

	//distributionProperty[column][labelMapping][columnMapping];
	//distributionProperty[column][labelMapping][INDEX_MEAN,INDEX_STANDARD_DEVIATION,INDEX_LOG_FACTOR];

	alpine_miner_nb_args_type * nb_args = (alpine_miner_nb_args_type *) palloc(sizeof(alpine_miner_nb_args_type));

	bool nominal_null = true;
	bool numerical_null = true;
	double * probability;
	int i = 0;

	ArrayType * result;
	Oid         result_eltype;
	int16 result_typlen;
	bool result_typbyval;
	char result_typalign;

	int result_count;
	Datum * result_data;
	bool * result_nulls;

	int ndims, *dims, *lbs;
	if (PG_ARGISNULL(4) || PG_ARGISNULL(5))
	{
		PG_RETURN_NULL();
	}
	if(!PG_ARGISNULL(0) && !PG_ARGISNULL(1) && !PG_ARGISNULL(2) && !PG_ARGISNULL(3))
	{
		nominal_null = false;
	}

	if (!PG_ARGISNULL(6) && !PG_ARGISNULL(7))
	{
		numerical_null = false;
	}

	if (!nominal_null)
	{
		(nb_args->args)[alpine_miner_nominal_columns_arg_index].arg = PG_GETARG_ARRAYTYPE_P(0);
		(nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].arg= PG_GETARG_ARRAYTYPE_P(1);
		(nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].arg= PG_GETARG_ARRAYTYPE_P(2);
		(nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].arg= PG_GETARG_ARRAYTYPE_P(3);
	}
	(nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].arg= PG_GETARG_ARRAYTYPE_P(4);
	(nb_args->args)[alpine_miner_dependent_column_probability_arg_index].arg= PG_GETARG_ARRAYTYPE_P(5);
	if (!numerical_null)
	{
		(nb_args->args)[alpine_miner_numerical_columns_arg_index].arg= PG_GETARG_ARRAYTYPE_P(6);
		(nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].arg= PG_GETARG_ARRAYTYPE_P(7);
	}


	if(!alpine_miner_nb_get_array_args(nominal_null, numerical_null, nb_args))
	{
		PG_RETURN_NULL();
	}

	probability = (double *) palloc((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count*sizeof(double)) ;
	for ( i = 0 ; i < (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count; i++)
	{
		probability[i] = 0.0;
	}

	alpine_miner_nb_ca_probability(nominal_null, numerical_null, nb_args, probability);


	// get output array element type //
	result_eltype = FLOAT8OID;
	get_typlenbyvalalign(result_eltype, &result_typlen, &result_typbyval, &result_typalign);

	// construct result array //
	result_count = (nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].count;
	result_data = (Datum *)palloc(result_count * sizeof(Datum));
	result_nulls = (bool *)palloc(result_count * sizeof(bool));
	for (i = 0; i < result_count; i++)
	{
		result_data[i] = Float8GetDatum(probability[i]);
		result_nulls[i] = false;
	}

	ndims = 1;
	dims = (int *) palloc(sizeof(int));
	dims[0] = result_count;
	lbs = (int *) palloc(sizeof(int));
	lbs[0] = 1;

	result = construct_md_array((void *)result_data, result_nulls, ndims, dims,
			lbs, result_eltype, result_typlen, result_typbyval, result_typalign);

	if(!nominal_null)
	{
		pfree((nb_args->args)[alpine_miner_nominal_columns_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_count_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_mapping_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].data);
		pfree((nb_args->args)[alpine_miner_nominal_columns_probability_arg_index].nulls);
	}
	pfree((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].data);
	pfree((nb_args->args)[alpine_miner_dependent_column_mapping_arg_index].nulls);
	pfree((nb_args->args)[alpine_miner_dependent_column_probability_arg_index].data);
	pfree((nb_args->args)[alpine_miner_dependent_column_probability_arg_index].nulls);
	if (!numerical_null)
	{
		pfree((nb_args->args)[alpine_miner_numerical_columns_arg_index].data);
		pfree((nb_args->args)[alpine_miner_numerical_columns_arg_index].nulls);
		pfree((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].data);
		pfree((nb_args->args)[alpine_miner_numerical_columns_probability_arg_index].nulls);
	}
	pfree(result_data);
	pfree(result_nulls);
	pfree(probability);
	pfree(dims);
	pfree(lbs);
	PG_RETURN_ARRAYTYPE_P(result);
}


	Datum
alpine_miner_nb_ca_prediction(PG_FUNCTION_ARGS)
{


	ArrayType *confidence_column_arg, *dependent_column_mapping_arg;
	Datum *confidence_column_data, *dependent_column_mapping_data;
	bool *confidence_column_nulls, *dependent_column_mapping_nulls;
	int confidence_column_count, dependent_column_mapping_count;

	int i;
	int j;
	int k;

	bool null_data;
	double max_log_probability = 0.0;
	int index = 0;

	if (PG_ARGISNULL(0) || PG_ARGISNULL(1))
	{
		PG_RETURN_NULL();
	}
	confidence_column_arg = PG_GETARG_ARRAYTYPE_P(0);
	null_data = alpine_miner_deconstruct_array(confidence_column_arg, &confidence_column_data, &confidence_column_nulls,&confidence_column_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}
	dependent_column_mapping_arg = PG_GETARG_ARRAYTYPE_P(1);
	null_data = alpine_miner_deconstruct_array(dependent_column_mapping_arg, &dependent_column_mapping_data, &dependent_column_mapping_nulls,&dependent_column_mapping_count);
	if (null_data)
	{
		PG_RETURN_NULL();
	}

	if (confidence_column_count != dependent_column_mapping_count || confidence_column_count <= 0)
	{
		PG_RETURN_NULL();
	}

	for (i = 0; i < confidence_column_count; i++){
		if (i == 0 || max_log_probability  < DatumGetFloat8(confidence_column_data[i]))
		{
			max_log_probability = DatumGetFloat8(confidence_column_data[i]);
			index = i;
		}
	}
	pfree(confidence_column_data);
	pfree(confidence_column_nulls);
	pfree(dependent_column_mapping_data);
	pfree(dependent_column_mapping_nulls);
	PG_RETURN_TEXT_P(DatumGetTextP(dependent_column_mapping_data[index]));
}
