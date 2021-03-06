#include "postgres.h"


#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"
//#include "executor/executor.h" // Greenplum requires this for GetAttributeByNum()

#include <math.h>
#include <string.h>


/* Indicate "version 1" calling conventions for all exported functions. */

PG_FUNCTION_INFO_V1(alpine_miner_float8_mregr_accum);


/**
 * @internal
 * @brief Transition state for multi-linear regression functions.
 */
typedef struct {
    ArrayType   *stateAsArray;
    float8      *len;
    float8      *Xty;
    float8      *XtX;

    ArrayType   *newX;
    float8      *newXData;
} MRegrAccumState;


/* Prototypes for static functions */

static bool alpine_miner_float8_mregr_accum_get_state(PG_FUNCTION_ARGS,
                                         MRegrAccumState *outState);

static bool
alpine_miner_float8_mregr_accum_get_state(PG_FUNCTION_ARGS,
                             MRegrAccumState *outState)
{
    float8      *stateData;
	int         len, statelen;	
	
	/* We should be strict, but it doesn't hurt to be paranoid */
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2))
        return false;
	
	outState->stateAsArray = PG_GETARG_ARRAYTYPE_P(0);	
    outState->newX = PG_GETARG_ARRAYTYPE_P(2);
	
	/* Ensure that both arrays are single dimensional float8[] arrays */
	if (ARR_NULLBITMAP(outState->stateAsArray) ||
        ARR_NDIM(outState->stateAsArray) != 1 || 
		ARR_ELEMTYPE(outState->stateAsArray) != FLOAT8OID ||
		ARR_NDIM(outState->newX) != 1 ||
        ARR_ELEMTYPE(outState->newX) != FLOAT8OID)
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("transition function \"%s\" called with invalid parameters",
					format_procedure(fcinfo->flinfo->fn_oid))));
	
	/* Only callable as a transition function */
	if (!(fcinfo->context && IsA(fcinfo->context, AggState)))
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("transition function \"%s\" not called from aggregate",
					format_procedure(fcinfo->flinfo->fn_oid))));
	
	/* newX with nulls will be ignored */
	if (ARR_NULLBITMAP(outState->newX))
		return false;
	
	/*
	 * If length(state) == 1 then it is an unitialized state, extend as
	 * needed, we use this instead of NULL so that we can declare the
	 * function as strict.
	 */
	len = ARR_DIMS(outState->newX)[0];
	statelen = 1 + (3*len + len*len)/2;
	if (ARR_DIMS(outState->stateAsArray)[0] == 1)
	{
		int size = statelen * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);
		outState->stateAsArray = (ArrayType *) palloc(size);
		SET_VARSIZE(outState->stateAsArray, size);
		outState->stateAsArray->ndim = 1;
		outState->stateAsArray->dataoffset = 0;
		outState->stateAsArray->elemtype = FLOAT8OID;
		ARR_DIMS(outState->stateAsArray)[0] = statelen;
		ARR_LBOUND(outState->stateAsArray)[0] = 1;
		stateData = (float8*) ARR_DATA_PTR(outState->stateAsArray);
		memset(stateData, 0, statelen * sizeof(float8));
		stateData[0] = len;
	}
	
	/* 
	 * Contents of 'state' are as follows:
	 *   [0]     = len(X[])
	 *   [1]     = count
	 *   [2]     = sum(y)
	 *   [3]     = sum(y*y)
	 *   [4:N]   = sum(X'[] * y) 
	 *   [N+1:M] = sum(X[] * X'[])
	 *   N       = 3 + len(X)
	 *   M       = N + len(X)*len(X)
	 */
	outState->len = (float8*) ARR_DATA_PTR(outState->stateAsArray);
    outState->Xty = outState->len + 1;
    outState->XtX = outState->len + 1 + len;

	outState->newXData  = (float8*) ARR_DATA_PTR(outState->newX);
	
	/* It is an error if the number of indepent variables is not constant */
	if (*outState->len != len)
	{
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("transition function \"%s\" called with invalid parameters",
					format_procedure(fcinfo->flinfo->fn_oid)),
				 errdetail("The independent-variable array is not of constant width.")));
	}
	
	/* Something is seriously fishy if our state has the wrong length */
	if (ARR_DIMS(outState->stateAsArray)[0] != statelen)
	{
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("transition function \"%s\" called with invalid parameters",
					format_procedure(fcinfo->flinfo->fn_oid))));
	}
    
	/* Okay... All's good now do the work */
    return true;    
}


/**
 * Transition function used by multi-linear regression aggregates.
 */
Datum
alpine_miner_float8_mregr_accum(PG_FUNCTION_ARGS)
{
    bool            goodArguments;
    MRegrAccumState state;
    float8          newY;
	int             len, i,j, k;

	goodArguments = alpine_miner_float8_mregr_accum_get_state(fcinfo, &state);
	if (!goodArguments) {
        if (PG_ARGISNULL(0))
			PG_RETURN_NULL();
        else
            PG_RETURN_ARRAYTYPE_P(PG_GETARG_ARRAYTYPE_P(0));
	}
   	newY = PG_GETARG_FLOAT8(1);

    len = (int) *state.len;
	for (i = 0; i < len; i++)
		state.Xty[i] += newY * state.newXData[i];
	
	/* Compute the matrix X[] * X'[] and add it in */
	k = 0;
	for (i = 0; i < len; i++)
		for (j = i; j < len; j++){
			state.XtX[k] += state.newXData[i] * state.newXData[j];
			k++;
	}
	PG_RETURN_ARRAYTYPE_P(state.stateAsArray);
}
