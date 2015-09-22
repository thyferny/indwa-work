#include "postgres.h"


#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"

#include <math.h>
#include <string.h>


PG_FUNCTION_INFO_V1(alpine_miner_em_getp);
PG_FUNCTION_INFO_V1(alpine_miner_em_getmaxsub);
 
Datum
alpine_miner_em_getp(PG_FUNCTION_ARGS)
{
	ArrayType *mu, *sigma, *column_value;
	float8 alpha;
	float8 result;
	float8 sigmaValue;
	float8 * mu_array_data, * sigma_array_data,* column_array_data;
	int column_size,i,j,k;
 
		if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	column_value=PG_GETARG_ARRAYTYPE_P(0);
	mu=PG_GETARG_ARRAYTYPE_P(1);
	sigma=PG_GETARG_ARRAYTYPE_P(2);
	alpha=PG_GETARG_FLOAT8(3);
	column_size= ARR_DIMS(column_value)[0];
	
	
	mu_array_data = (float8*) ARR_DATA_PTR(mu);
	sigma_array_data = (float8*) ARR_DATA_PTR(sigma);
	column_array_data= (float8*) ARR_DATA_PTR(column_value);
	
	
	result=0;
	sigmaValue=1;
	for ( k = 0; k < column_size; k++){
		result+=(column_array_data[k]-mu_array_data[k])*(column_array_data[k]-mu_array_data[k])/sigma_array_data[k];
		sigmaValue*=sigma_array_data[k];
	}
	 
	result=alpha*exp(-0.5*result)/sqrt(sigmaValue);
	PG_RETURN_FLOAT8(result);
}	



Datum
alpine_miner_em_getmaxsub(PG_FUNCTION_ARGS)
{
	ArrayType *firstarray, *secondarray;
	float8 tempsub;
	float8 result;
	float8 * first_array_data, * second_array_data;
	int column_size,i,j,k;
  
		if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	 
	firstarray=PG_GETARG_ARRAYTYPE_P(0);
	secondarray=PG_GETARG_ARRAYTYPE_P(1);
	 
	column_size= ARR_DIMS(firstarray)[0];
	
	 
	first_array_data = (float8*) ARR_DATA_PTR(firstarray);
	second_array_data = (float8*) ARR_DATA_PTR(secondarray);
	
	result=0;
 
	for ( k = 0; k < column_size; k++){
	
		tempsub=fabs(first_array_data[k]-second_array_data[k]);
	 
		if (tempsub>result)
			{ result=tempsub; }
		}
	PG_RETURN_FLOAT8(result);
}	




