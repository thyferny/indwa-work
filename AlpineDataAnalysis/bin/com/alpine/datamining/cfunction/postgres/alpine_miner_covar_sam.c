
#include "postgres.h"


#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"

#include <math.h>
#include <string.h>



PG_FUNCTION_INFO_V1(alpine_miner_covar_sam_accum);

PG_FUNCTION_INFO_V1(alpine_miner_covar_sam_final);

Datum
alpine_miner_covar_sam_accum(PG_FUNCTION_ARGS)
{
	ArrayType *state;
	float8 * state_array_data;
	ArrayType * column_array;
	int column_size;
	float8 * column_array_data;
	int i;
	int k ;
	int j;

	if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	state = PG_GETARG_ARRAYTYPE_P(0);
	
	state_array_data = (float8*) ARR_DATA_PTR(state);
	
	column_array = PG_GETARG_ARRAYTYPE_P(1);
	column_size = ARR_DIMS(column_array)[0];
	column_array_data = (float8*) ARR_DATA_PTR(column_array);
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
	
		k	= 0;
        for ( i = 0; i < column_size; i++){
			for(j = i; j < column_size; j++){
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
alpine_miner_covar_sam_final(PG_FUNCTION_ARGS){
		ArrayType *state ;
		ArrayType *result;
		float8 * resultData;
		float8 * state_array_data;
		int total_length;
		int column_size;
		float8 row_size;
		int result_size;
		int size;
		int k=0;
		int i,j;
		int sam_row_size;

		if (PG_ARGISNULL(0))
			PG_RETURN_NULL();
		state = PG_GETARG_ARRAYTYPE_P(0);

		state_array_data = (float8*) ARR_DATA_PTR(state);
		total_length=ARR_DIMS(state)[0];


		column_size=state_array_data[total_length-1];
		row_size=state_array_data[total_length-2];
		result_size=column_size*(column_size+1)/2;
		size = result_size * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
		result = (ArrayType *) palloc(size);
		SET_VARSIZE(result, size);
		result->ndim = 1;
		result->dataoffset = 0;
		result->elemtype = FLOAT8OID;
		ARR_DIMS(result)[0] = result_size;
		ARR_LBOUND(result)[0] = 1;
		resultData = (float8*) ARR_DATA_PTR(result);
		memset(resultData, 0, result_size * sizeof(float8));

		 sam_row_size=row_size-1;
		 if(sam_row_size<=0)
		sam_row_size=1;
		  k=0;
		for (  i = 0; i < column_size; i++){
				for(  j = i; j < column_size; j++){
		         	resultData[k] =state_array_data[k]/sam_row_size- state_array_data[result_size+i] * state_array_data[result_size+j]/row_size/sam_row_size;
				k++;
			}
		}
		PG_RETURN_ARRAYTYPE_P(result);

}
