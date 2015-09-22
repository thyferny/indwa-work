
#include "postgres.h"


#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"

#include <math.h>
#include <string.h>



PG_FUNCTION_INFO_V1(alpine_miner_covar_sam_accum);
PG_FUNCTION_INFO_V1(alpine_miner_covar_sam_combine);
PG_FUNCTION_INFO_V1(alpine_miner_covar_sam_final);

Datum
alpine_miner_covar_sam_accum(PG_FUNCTION_ARGS)
{
	
	if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	ArrayType *state = PG_GETARG_ARRAYTYPE_P(0);
	
	float8 * state_array_data = (float8*) ARR_DATA_PTR(state);
	
	ArrayType * column_array = PG_GETARG_ARRAYTYPE_P(1);
	int column_size = ARR_DIMS(column_array)[0];
	float8 * column_array_data = (float8*) ARR_DATA_PTR(column_array);
	if (ARR_DIMS(state)[0] == 1){
			
			int result_size = column_size * ( column_size + 1)/2 + column_size+2;
       	 	int size =  result_size * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
        	state = (ArrayType *) palloc(size);
	        SET_VARSIZE(state, size);
       	 	state->ndim = 1;
        	state->dataoffset = 0;
        	state->elemtype = FLOAT8OID;
        	ARR_DIMS(state)[0] = result_size;
        	ARR_LBOUND(state)[0] = 1;
			state_array_data = (float8*) ARR_DATA_PTR(state);
	     	memset(state_array_data, 0,  result_size * sizeof(float8));
	}
		int k = 0;
		int i;
        for ( i = 0; i < column_size; i++){
			for(int j = i; j < column_size; j++){
                	state_array_data[k] += column_array_data[i] * column_array_data[j];
			k++;
		}
	}
	for( i = 0; i < column_size; i++){
		state_array_data[k + i] += column_array_data[i];
	}
	state_array_data[k+i]++;
	state_array_data[k+i+1]=column_size;
        PG_RETURN_ARRAYTYPE_P(state);
}	

Datum
alpine_miner_covar_sam_combine(PG_FUNCTION_ARGS)
{
	ArrayType  *state1, *state2, *result;
	float8     *state1Data, *state2Data, *resultData;
	int         i, size;
	int         len, statelen;
	
	if (PG_ARGISNULL(0))
	{
		if (PG_ARGISNULL(1))
			PG_RETURN_NULL();
		PG_RETURN_ARRAYTYPE_P(PG_GETARG_ARRAYTYPE_P(1));
	}
	if (PG_ARGISNULL(1))
		PG_RETURN_ARRAYTYPE_P(PG_GETARG_ARRAYTYPE_P(0));
	
	state1 = PG_GETARG_ARRAYTYPE_P(0);	
	state2 = PG_GETARG_ARRAYTYPE_P(1);
	
	if (ARR_NULLBITMAP(state1) || ARR_NULLBITMAP(state2) || 
		ARR_NDIM(state1) != 1 || ARR_NDIM(state2) != 1 || 
		ARR_ELEMTYPE(state1) != FLOAT8OID || ARR_ELEMTYPE(state2) != FLOAT8OID)
	{
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("preliminary segment-level calculation function \"%s\" called with invalid parameters",
					format_procedure(fcinfo->flinfo->fn_oid))));
	}
	
	if (ARR_DIMS(state1)[0] == 1)
		PG_RETURN_ARRAYTYPE_P(state2);
	if (ARR_DIMS(state2)[0] == 1)
		PG_RETURN_ARRAYTYPE_P(state1);
	
	state1Data = (float8*) ARR_DATA_PTR(state1);
	state2Data = (float8*) ARR_DATA_PTR(state2);
	
	if (ARR_DIMS(state1)[0] != ARR_DIMS(state2)[0]) 
	{
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("preliminary segment-level calculation function \"%s\" called with invalid parameters",
					format_procedure(fcinfo->flinfo->fn_oid)),
				 errdetail("The independent-variable array is not of constant width.")));
	}
	statelen = ARR_DIMS(state1)[0];
	
	size = statelen * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
	result = (ArrayType *) palloc(size);
	SET_VARSIZE(result, size);
	result->ndim = 1;
	result->dataoffset = 0;
	result->elemtype = FLOAT8OID;
	ARR_DIMS(result)[0] = statelen;
	ARR_LBOUND(result)[0] = 1;
	resultData = (float8*) ARR_DATA_PTR(result);
	memset(resultData, 0, statelen * sizeof(float8));
	
	resultData[0] = len;
	for (i = 0; i < statelen-1; i++){
		resultData[i] = state1Data[i] + state2Data[i];	
	}
	resultData[i]=state1Data[i];
	PG_RETURN_ARRAYTYPE_P(result);
}


Datum
alpine_miner_covar_sam_final(PG_FUNCTION_ARGS){
		if (PG_ARGISNULL(0))
		PG_RETURN_NULL();
		ArrayType *state = PG_GETARG_ARRAYTYPE_P(0);
		ArrayType * result;
		float8 *resultData;
		float8 * state_array_data = (float8*) ARR_DATA_PTR(state);
		int total_length=ARR_DIMS(state)[0];
		int column_size=state_array_data[total_length-1];
		float8 row_size=state_array_data[total_length-2];
		int result_size=column_size*(column_size+1)/2;
		int size = result_size * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
		result = (ArrayType *) palloc(size);
		SET_VARSIZE(result, size);
		result->ndim = 1;
		result->dataoffset = 0;
		result->elemtype = FLOAT8OID;
		ARR_DIMS(result)[0] = result_size;
		ARR_LBOUND(result)[0] = 1;
		resultData = (float8*) ARR_DATA_PTR(result);
		memset(resultData, 0, result_size * sizeof(float8));

		float8 sam_row_size=row_size-1;
		if(sam_row_size<=0)
		sam_row_size=1;
		int k=0;
		for (int i = 0; i < column_size; i++){
				for(int j = i; j < column_size; j++){
		         	resultData[k] =state_array_data[k]/sam_row_size- state_array_data[result_size+i] * state_array_data[result_size+j]/row_size/sam_row_size;
				k++;
			}
		}
		PG_RETURN_ARRAYTYPE_P(result);

}
