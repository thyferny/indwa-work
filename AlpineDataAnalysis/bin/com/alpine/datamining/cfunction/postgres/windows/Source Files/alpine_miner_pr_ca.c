#include "postgres.h"
#include "fmgr.h"
#include "utils/array.h"

/**********************************
 * The Dot product of two vectors
 **********************************/

Datum alpine_miner_dot_product( PG_FUNCTION_ARGS);

PG_FUNCTION_INFO_V1( alpine_miner_dot_product );

Datum
alpine_miner_dot_product( PG_FUNCTION_ARGS)
{
	ArrayType  *ax = PG_GETARG_ARRAYTYPE_P(0);
	ArrayType  *ay = PG_GETARG_ARRAYTYPE_P(1);
	
	float8		*x, *y, z = 0.0;
	int			i, *dimx, *dimy;
	
	/* Sanity check: does it look like an array at all? */
	if (ARR_NDIM(ax) <= 0 || ARR_NDIM(ax) > MAXDIM)
		PG_RETURN_NULL();
	if (ARR_NDIM(ay) <= 0 || ARR_NDIM(ay) > MAXDIM)
		PG_RETURN_NULL();

	// Assign variables for the input arrays 
	x = (float8 *) ARR_DATA_PTR( ax);
	y = (float8 *) ARR_DATA_PTR( ay);	

	// Read arrays dimensions 
	dimx = ARR_DIMS( ax);
	dimy = ARR_DIMS( ay);

	// Run the calculation
	for (i=0; i < dimx[0] && i < dimy[0]; i++) {
		z = z + (x[i] * y[i]);
	}
	
	PG_RETURN_FLOAT8( z);
}

/******************************* 
 * Novel product check:
 * If the 2nd vector has any non-zero values 
 * on positions which the 1st vector has 0s 
 * then the result is True
 *******************************/

Datum alpine_miner_has_novel_product( PG_FUNCTION_ARGS);

PG_FUNCTION_INFO_V1( alpine_miner_has_novel_product );

Datum
alpine_miner_has_novel_product( PG_FUNCTION_ARGS)
{
	ArrayType  *ax = PG_GETARG_ARRAYTYPE_P(0);
	ArrayType  *ay = PG_GETARG_ARRAYTYPE_P(1);
	
	float8		*x, *y;
	int			i, *dimx, *dimy;
	
	/* Sanity check: does it look like an array at all? */
	if (ARR_NDIM(ax) <= 0 || ARR_NDIM(ax) > MAXDIM)
		PG_RETURN_NULL();
	if (ARR_NDIM(ay) <= 0 || ARR_NDIM(ay) > MAXDIM)
		PG_RETURN_NULL();
	
	// Assign variables for the input arrays 
	x = (float8 *) ARR_DATA_PTR( ax);
	y = (float8 *) ARR_DATA_PTR( ay);	
	
	// Read arrays dimensions 
	dimx = ARR_DIMS( ax);
	dimy = ARR_DIMS( ay);
	
	// Check one by one
	for (i=0; i < dimx[0] && i < dimy[0]; i++) {
		if (x[i]==0.0 && y[i]!=0.0) {
			PG_RETURN_INT32( 1);
		}
	}
	
	PG_RETURN_INT32( 0);
}
