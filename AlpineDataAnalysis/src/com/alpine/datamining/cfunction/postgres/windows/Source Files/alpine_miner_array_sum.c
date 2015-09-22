#include "postgres.h"
#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"
//#include "executor/executor.h"
#include <math.h>
#include <string.h>

PG_FUNCTION_INFO_V1(alpine_miner_float_array_sum_accum);

typedef struct {
	ArrayType   *stateAsArray;
	float8      *sum;
	ArrayType   *newX;
	float8      *newXData;
	int          len;
} 
float_array_sum_accum_state;

	static bool float_array_sum_accum_get_state(PG_FUNCTION_ARGS,float_array_sum_accum_state *outState)
{
	float8      *stateData;
	int         len, statelen,size;	

	if (PG_ARGISNULL(0) || PG_ARGISNULL(1))
		return false;

	outState->stateAsArray = PG_GETARG_ARRAYTYPE_P(0);	
	outState->newX = PG_GETARG_ARRAYTYPE_P(1);

	if(ARR_NDIM(outState->newX) != 1 ||
			ARR_ELEMTYPE(outState->newX) != FLOAT8OID)
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("transition function \"%s\" called with invalid parameters, %d %d ",
					 format_procedure(fcinfo->flinfo->fn_oid), ARR_NDIM(outState->stateAsArray), ARR_NDIM(outState->newX) )));

	if (!(fcinfo->context && IsA(fcinfo->context, AggState)))
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("transition function \"%s\" not called from aggregate",
					 format_procedure(fcinfo->flinfo->fn_oid))));

	if (ARR_NULLBITMAP(outState->newX))
	{
		return false;
	}
	len = ARR_DIMS(outState->newX)[0];
	outState->len = len;
	statelen = len;
	size=0;
	////elog(WARNING,"1: %d %d %d ",ARR_NDIM(outState->stateAsArray),  len, ARR_DIMS(outState->stateAsArray)[0]);
	if (ARR_NDIM(outState->stateAsArray) == 0)
	{
		size = statelen * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
		outState->stateAsArray = (ArrayType *) palloc(size);
		SET_VARSIZE(outState->stateAsArray, size);
		outState->stateAsArray->ndim = 1;
		outState->stateAsArray->dataoffset = 0;
		outState->stateAsArray->elemtype = FLOAT8OID;
		ARR_DIMS(outState->stateAsArray)[0] = statelen;
		ARR_LBOUND(outState->stateAsArray)[0] = 1;
		stateData = (float8*) ARR_DATA_PTR(outState->stateAsArray);
		memset(stateData, 0, statelen * sizeof(float8));
		outState->sum = (float8*) ARR_DATA_PTR(outState->stateAsArray);
		//elog(WARNING,"2: %d %d %d ",ARR_NDIM(outState->stateAsArray),  len, ARR_DIMS(outState->stateAsArray)[0]);
	}
	else
	{
		if(ARR_DIMS(outState->stateAsArray)[0] >= statelen)
		{
			outState->sum = (float8*) ARR_DATA_PTR(outState->stateAsArray);
		}
		else
		{
			int i = 0;
			ArrayType *	stateAsArray = outState->stateAsArray;
			float8	*sum =  (float8*) ARR_DATA_PTR(outState->stateAsArray);
			size = statelen * sizeof(float8) + ARR_OVERHEAD_NONULLS(1); 
			outState->stateAsArray = (ArrayType *) palloc(size);
			SET_VARSIZE(outState->stateAsArray, size);
			outState->stateAsArray->ndim = 1;
			outState->stateAsArray->dataoffset = 0;
			outState->stateAsArray->elemtype = FLOAT8OID;
			ARR_DIMS(outState->stateAsArray)[0] = statelen;
			ARR_LBOUND(outState->stateAsArray)[0] = 1;
			stateData = (float8*) ARR_DATA_PTR(outState->stateAsArray);
			memset(stateData, 0, statelen * sizeof(float8));
			//elog(WARNING,"3: %d %d %d ",ARR_NDIM(outState->stateAsArray),  len, ARR_DIMS(outState->stateAsArray)[0]);
			outState->sum = (float8*) ARR_DATA_PTR(outState->stateAsArray);
			for(i = 0; i < ARR_DIMS(stateAsArray)[0]; i++)
			{
				outState->sum[i] = sum[i];
			}
			//elog(WARNING,"4: %d %d %d ",ARR_NDIM(outState->stateAsArray),  len, ARR_DIMS(outState->stateAsArray)[0]);
		}
	}
	//elog(WARNING,"5: %d %d %d ",ARR_NDIM(outState->stateAsArray),  len, ARR_DIMS(outState->stateAsArray)[0]);
	outState->newXData  = (float8*) ARR_DATA_PTR(outState->newX);

	return true;   
	}


	Datum
alpine_miner_float_array_sum_accum(PG_FUNCTION_ARGS)
{
	bool            goodArguments;
	float_array_sum_accum_state state;
	int             len, i,j;

	goodArguments = float_array_sum_accum_get_state(fcinfo, &state);
	if (!goodArguments) 
	{
		if (PG_ARGISNULL(0))
			PG_RETURN_NULL();
		else
			PG_RETURN_ARRAYTYPE_P(PG_GETARG_ARRAYTYPE_P(0));
	}

	len = state.len;
	for (i = 0; i < len; i++)
		state.sum[i] += state.newXData[i];
	PG_RETURN_ARRAYTYPE_P(state.stateAsArray);
	}

