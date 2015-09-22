#include "postgres.h"
#include "fmgr.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/lsyscache.h"
#include <stdio.h>
#include <stdlib.h>
#ifdef PG_MODULE_MAGIC
PG_MODULE_MAGIC;
#endif
#include <math.h>
#include <float.h>

#include "funcapi.h"
#include "utils/builtins.h"

#include <math.h>
#include <string.h>


static const double ALPINE_MINER_THRESH = 30.;
static const double ALPINE_MINER_MTHRESH = -30.;

double alpine_miner_compute_pi(float8 *beta_data, int beta_count, float8 *columns_data, int columns_count, bool add_intercept_arg)
{
	double gx, pi;
	int i;
	double tmp = 0;
	i = 0;
	gx = 0;

	/*compute gx*/
	while (i < beta_count && i < columns_count)
	{
		gx = gx + beta_data[i] * columns_data[i];
		i = i + 1;
	}
	if (add_intercept_arg)
	{
		gx = gx + beta_data[beta_count - 1];
	}

	/*compute pi*/
	//pi = 1.0/(1.0 + exp(-1.0*gx));
	if (gx > ALPINE_MINER_THRESH)
	{
		tmp = 1.0/DBL_EPSILON;
		//pi = 1.0/(1.0+DBL_EPSILON);
	}
	else if (gx < ALPINE_MINER_MTHRESH)
	{
		tmp = DBL_EPSILON;
//		pi = 1.0/(1.0 + 1.0/DBL_EPSILON);
		//pi = DBL_EPSILON;
	}
	else
	{
		tmp = exp(gx);
	}
	//pi = 1.0/(1.0 + tmp);
	pi = tmp/(1.0 + tmp);
	return pi;
}

/* compute hessian matrix*/
float8 * alpine_miner_compute_hessian(int beta_count,float8 * beta_data, float8 *columns_data, float8 *result_data
		,double weight_arg, bool add_intercept_arg, double pi)
{
	double x,y;
	int i = 0;
	int j = 0;
	int ind = 0;;

	while (i < beta_count)
	{
		if ((i == (beta_count - 1)) && add_intercept_arg)
		{
			x = 1.0;
		}
		else
		{
			x = columns_data[i];
		}
		j = i;
		while(j < beta_count)
		{
			if ((j == (beta_count - 1)) && add_intercept_arg)
			{
				y = 1.0;
			}
			else
			{
				y = columns_data[j];
			}
			result_data[ind] = (-x*y*weight_arg*pi*(1.0 - pi) + result_data[ind]);
			ind = ind + 1;
			j = j + 1;
		}
		i = i + 1;
	}
	return result_data;
};
/*
compute_derivative(columns_count,columns_data, &result_data[beta_count *(beta_count+1)/2], columns_nulls,
		&result_nulls[beta_count *(beta_count+1)/2]
		,weight_arg, y_arg, add_intercept_arg, pi);
*/
float8 * alpine_miner_compute_xwz(int columns_count,float8 *columns_data, float8 *result_data
		,double weight_arg, int y, bool add_intercept_arg, double pi)
{
	double foo;
	int i = 0;
	double eta = log(pi/(1 - pi));
	double exp_eta = pi/(1-pi);
	double mu_eta_dev = 0;
	if (eta > ALPINE_MINER_THRESH || eta < ALPINE_MINER_MTHRESH)
	{
		mu_eta_dev = DBL_EPSILON;
	}
	else
	{
		mu_eta_dev = exp_eta/((1+exp_eta)*(1+exp_eta));
	}

	foo = weight_arg * pi*(1-pi)*(eta+(y - pi)/mu_eta_dev);
	while (i < columns_count)
	{
		result_data[i] = ((columns_data[i])*foo  + result_data[i]);
		i = i + 1;
	}
	if (add_intercept_arg)
	{
		result_data[columns_count] = foo + (result_data[columns_count]);
	}
	return result_data;
};
float8 * alpine_miner_compute_derivative(int columns_count,float8 *columns_data, float8 *result_data
		,double weight_arg, int y, bool add_intercept_arg, double pi)
{
	int i = 0;
	double foo = weight_arg * (y - pi);
	while (i < columns_count)
	{
		result_data[i] = ((columns_data[i])*foo  + (result_data[i]) );
		i = i + 1;
	}
	if (add_intercept_arg)
	{
		result_data[columns_count] = (foo  + (result_data[columns_count]));
	}
	return result_data;
};


PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_beta_accum);

Datum
alpine_miner_lr_ca_beta_accum(PG_FUNCTION_ARGS)
{

        ArrayType  *beta_arg, *columns_arg, *result;
        float8     *beta_data, *columns_data, *result_data;
	int         beta_count, columns_count, result_count;

	bool add_intercept_arg;
	double weight_arg;
	int y_arg;
	int times_arg = 0;

	double fitness = 0.0;
	double gx = 0.0;
	double pi = 0.0;
	int size = 0;
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3) || PG_ARGISNULL(4) || PG_ARGISNULL(5) ||PG_ARGISNULL(6)){
		PG_RETURN_NULL();
	}
        result = PG_GETARG_ARRAYTYPE_P(0);
        beta_arg = PG_GETARG_ARRAYTYPE_P(1);
        columns_arg = PG_GETARG_ARRAYTYPE_P(2);

	add_intercept_arg = PG_GETARG_BOOL(3);
	weight_arg = PG_GETARG_FLOAT8(4);
	y_arg = PG_GETARG_INT32(5);
	times_arg = PG_GETARG_INT32(6);


	result_data = (float8*) ARR_DATA_PTR(result);
	beta_data = (float8*) ARR_DATA_PTR(beta_arg);
	columns_data = (float8*) ARR_DATA_PTR(columns_arg);
	
	result_count = ARR_DIMS(result)[0];
	beta_count = ARR_DIMS(beta_arg)[0];
	columns_count = ARR_DIMS(columns_arg)[0];
//	float8 * column_array_data = (float8*) ARR_DATA_PTR(column_array);
	if (result_count == 1){
		result_count = beta_count *(beta_count+1)/2 + beta_count + 1;
       	 	size =  result_count * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
        	result = (ArrayType *) palloc(size);
	        SET_VARSIZE(result, size);
       	 	result->ndim = 1;
        	result->dataoffset = 0;
        	result->elemtype = FLOAT8OID;
        	ARR_DIMS(result)[0] = result_count;
        	ARR_LBOUND(result)[0] = 1;
		result_data = (float8*) ARR_DATA_PTR(result);
        	memset(result_data, 0,  result_count * sizeof(float8));
	}


	if (times_arg == 0)
	{
		//(weights * y + 0.5)/(weights + 1)
		pi = (weight_arg * y_arg + 0.5)/(weight_arg + 1);
	}
	else
	{
		pi = alpine_miner_compute_pi(beta_data,  beta_count, columns_data, columns_count, add_intercept_arg);
	}


	/* compute derivative */
	alpine_miner_compute_hessian(beta_count,beta_data,columns_data, result_data
		,weight_arg, add_intercept_arg,  pi);

	/* compute hessian matrix*/
//datum * compute_derivative(int columns_count,datum *columns_data, datum *result_data,bool *columns_nulls, bool*result_nulls
	//	,double weight_arg, int y, bool add_intercept_arg, double pi)
	alpine_miner_compute_xwz(columns_count,columns_data, &result_data[beta_count *(beta_count+1)/2],
		weight_arg, y_arg, add_intercept_arg, pi);

	if (y_arg == 1)
	{
		fitness = log(pi);
	}
	else
	{
		fitness = log(1.0 - pi);
	}
	fitness *= weight_arg;
	result_data[result_count - 1] = (fitness + (result_data[result_count - 1]));


        PG_RETURN_ARRAYTYPE_P(result);
}

PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_he_accum);

Datum
alpine_miner_lr_ca_he_accum(PG_FUNCTION_ARGS)
{

        ArrayType  *beta_arg, *columns_arg, *result;
        float8     *beta_data, *columns_data, *result_data;
	int         beta_count, columns_count, result_count;
        int         size;

	bool add_intercept_arg;
	double weight_arg;

	double gx = 0.0;
	double pi = 0.0;
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3)||PG_ARGISNULL(4))
	{
		PG_RETURN_NULL();
	}
	result = PG_GETARG_ARRAYTYPE_P(0);
        beta_arg = PG_GETARG_ARRAYTYPE_P(1);
        columns_arg = PG_GETARG_ARRAYTYPE_P(2);
	add_intercept_arg = PG_GETARG_BOOL(3);
	weight_arg = PG_GETARG_FLOAT8(4);

	result_data = (float8*) ARR_DATA_PTR(result);
	beta_data = (float8*) ARR_DATA_PTR(beta_arg);
	columns_data = (float8*) ARR_DATA_PTR(columns_arg);
	
	result_count = ARR_DIMS(result)[0];
	beta_count = ARR_DIMS(beta_arg)[0];
	columns_count = ARR_DIMS(columns_arg)[0];
	if (result_count == 1){
		result_count = beta_count *(beta_count+1)/2;
       	 	size =  result_count * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
        	result = (ArrayType *) palloc(size);
	        SET_VARSIZE(result, size);
       	 	result->ndim = 1;
        	result->dataoffset = 0;
        	result->elemtype = FLOAT8OID;
        	ARR_DIMS(result)[0] = result_count;
        	ARR_LBOUND(result)[0] = 1;
		result_data = (float8*) ARR_DATA_PTR(result);
        	memset(result_data, 0,  result_count * sizeof(float8));
	}


	pi = alpine_miner_compute_pi(beta_data,  beta_count, columns_data, columns_count, add_intercept_arg);

	result_data = alpine_miner_compute_hessian(beta_count,beta_data,columns_data, result_data
			,weight_arg, add_intercept_arg,  pi);

        PG_RETURN_ARRAYTYPE_P(result);
}

PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_derivative);

Datum
alpine_miner_lr_ca_derivative(PG_FUNCTION_ARGS)
{

        ArrayType  *beta_arg, *columns_arg, *result;
        float8     *beta_data, *columns_data, *result_data;
	int         beta_count, columns_count, result_count;

	bool add_intercept_arg;
	double weight_arg;
	int y_arg;
        int         size;

	double gx = 0.0;
	double pi = 0.0;
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3) || PG_ARGISNULL(4)||PG_ARGISNULL(5)){
		PG_RETURN_NULL();
	}
	result = PG_GETARG_ARRAYTYPE_P(0);
        beta_arg = PG_GETARG_ARRAYTYPE_P(1);

        columns_arg = PG_GETARG_ARRAYTYPE_P(2);
	add_intercept_arg = PG_GETARG_BOOL(3);
	weight_arg = PG_GETARG_FLOAT8(4);
	y_arg = PG_GETARG_INT32(5);

	result_data = (float8*) ARR_DATA_PTR(result);
	beta_data = (float8*) ARR_DATA_PTR(beta_arg);
	columns_data = (float8*) ARR_DATA_PTR(columns_arg);
	
	result_count = ARR_DIMS(result)[0];
	beta_count = ARR_DIMS(beta_arg)[0];
	columns_count = ARR_DIMS(columns_arg)[0];
//	float8 * column_array_data = (float8*) ARR_DATA_PTR(column_array);
	if (result_count == 1){
		result_count = beta_count;
       	 	size =  result_count * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
        	result = (ArrayType *) palloc(size);
	        SET_VARSIZE(result, size);
       	 	result->ndim = 1;
        	result->dataoffset = 0;
        	result->elemtype = FLOAT8OID;
        	ARR_DIMS(result)[0] = result_count;
        	ARR_LBOUND(result)[0] = 1;
		result_data = (float8*) ARR_DATA_PTR(result);
        	memset(result_data, 0,  result_count * sizeof(float8));
	}

	pi = alpine_miner_compute_pi(beta_data,  beta_count, columns_data,  columns_count, add_intercept_arg);

	alpine_miner_compute_derivative(columns_count,columns_data, result_data
		,weight_arg, y_arg, add_intercept_arg,  pi);
        PG_RETURN_ARRAYTYPE_P(result);
}


PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_he_de_accum);

Datum
alpine_miner_lr_ca_he_de_accum(PG_FUNCTION_ARGS)
{

        ArrayType  *beta_arg, *columns_arg, *result;
        float8     *beta_data, *columns_data, *result_data;
	int         beta_count, columns_count, result_count;
        int         size;

	bool add_intercept_arg;
	double weight_arg;
	int y_arg;

	double fitness = 0.0;
	double gx = 0.0;
	double pi = 0.0;
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3) || PG_ARGISNULL(4)||PG_ARGISNULL(5)){
		PG_RETURN_NULL();
	}
	result = PG_GETARG_ARRAYTYPE_P(0);
        beta_arg = PG_GETARG_ARRAYTYPE_P(1);
        columns_arg = PG_GETARG_ARRAYTYPE_P(2);
	add_intercept_arg = PG_GETARG_BOOL(3);
	weight_arg = PG_GETARG_FLOAT8(4);
	y_arg = PG_GETARG_INT32(5);

	result_data = (float8*) ARR_DATA_PTR(result);
	beta_data = (float8*) ARR_DATA_PTR(beta_arg);
	columns_data = (float8*) ARR_DATA_PTR(columns_arg);
	
	result_count = ARR_DIMS(result)[0];
	beta_count = ARR_DIMS(beta_arg)[0];
	columns_count = ARR_DIMS(columns_arg)[0];
//	float8 * column_array_data = (float8*) ARR_DATA_PTR(column_array);
	if (result_count == 1){
		result_count = beta_count *(beta_count+1)/2 + beta_count + 1;
       	 	size =  result_count * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
        	result = (ArrayType *) palloc(size);
	        SET_VARSIZE(result, size);
       	 	result->ndim = 1;
        	result->dataoffset = 0;
        	result->elemtype = FLOAT8OID;
        	ARR_DIMS(result)[0] = result_count;
        	ARR_LBOUND(result)[0] = 1;
		result_data = (float8*) ARR_DATA_PTR(result);
        	memset(result_data, 0,  result_count * sizeof(float8));
	}

	pi = alpine_miner_compute_pi(beta_data, beta_count, columns_data, columns_count, add_intercept_arg);

	alpine_miner_compute_hessian(beta_count,beta_data,columns_data, result_data
		,weight_arg, add_intercept_arg,  pi);

	alpine_miner_compute_derivative(columns_count,columns_data, &result_data[beta_count *(beta_count+1)/2]
		,weight_arg, y_arg, add_intercept_arg, pi);

	if (y_arg == 1)
	{
		fitness = log(pi);
	}
	else
	{
		fitness = log(1.0 - pi);
	}
	fitness *= weight_arg;
	result_data[result_count - 1] = fitness + result_data[result_count - 1];

        PG_RETURN_ARRAYTYPE_P(result);
}

PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_fitness);

Datum
alpine_miner_lr_ca_fitness(PG_FUNCTION_ARGS)
{

        ArrayType  *beta_arg, *columns_arg;
        float8     *beta_data, *columns_data;
        int     beta_count, columns_count;

	bool add_intercept_arg;
	double weight_arg;
	int label_value_arg;

	double gx = 0.0;
	double pi = 0.0;
	double fitness = 0.0;
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) || PG_ARGISNULL(3) || PG_ARGISNULL(4)){
		PG_RETURN_NULL();
	}
        beta_arg = PG_GETARG_ARRAYTYPE_P(0);
        columns_arg = PG_GETARG_ARRAYTYPE_P(1);

	add_intercept_arg = PG_GETARG_BOOL(2);
	weight_arg = PG_GETARG_FLOAT8(3);
	label_value_arg = PG_GETARG_INT32(4);

	beta_data = (float8*) ARR_DATA_PTR(beta_arg);
	columns_data = (float8*) ARR_DATA_PTR(columns_arg);
	
	beta_count = ARR_DIMS(beta_arg)[0];
	columns_count = ARR_DIMS(columns_arg)[0];

	pi = alpine_miner_compute_pi(beta_data, beta_count, columns_data, columns_count, add_intercept_arg);

	if (label_value_arg == 1)
	{
		fitness = log(pi);
	}
	else
	{
		fitness = log(1.0 - pi);
	}
	fitness *= weight_arg;

	PG_RETURN_FLOAT8(fitness);
}

PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_pi);

Datum
alpine_miner_lr_ca_pi(PG_FUNCTION_ARGS)
{

        ArrayType  *beta_arg, *columns_arg;
        float8     *beta_data, *columns_data;
        int     beta_count, columns_count;

	bool add_intercept_arg;

	double gx = 0.0;
	double pi = 0.0;

	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2))
	{
		PG_RETURN_NULL();
	}
        beta_arg = PG_GETARG_ARRAYTYPE_P(0);
        columns_arg = PG_GETARG_ARRAYTYPE_P(1);
	add_intercept_arg = PG_GETARG_BOOL(2);

	beta_data = (float8*) ARR_DATA_PTR(beta_arg);
	columns_data = (float8*) ARR_DATA_PTR(columns_arg);
	
	beta_count = ARR_DIMS(beta_arg)[0];
	columns_count = ARR_DIMS(columns_arg)[0];

	pi = alpine_miner_compute_pi(beta_data, beta_count, columns_data, columns_count, add_intercept_arg);

	PG_RETURN_FLOAT8(pi);
}
