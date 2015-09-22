
#include "postgres.h"


#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"

#include <math.h>
#include <string.h>



PG_FUNCTION_INFO_V1(alpine_miner_pcaresult);



Datum
alpine_miner_pcaresult(PG_FUNCTION_ARGS)
{
	ArrayType *qvalues;
	ArrayType *state;
	float8 * state_array_data;
	ArrayType *rowdata;
	float8 * rowdata_data;
	float8 * qvalues_data;
	float8 * variData;
	ArrayType * column_array;
	int rowdata_size,qvalues_size;
	int column_size;
	float8 * column_array_data;
	int i;
	int k ;
	int j;
        int palloc_size;
	ArrayType    *vari;
	if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	if (PG_ARGISNULL(1))
		PG_RETURN_NULL();

	rowdata = PG_GETARG_ARRAYTYPE_P(0);
	qvalues = PG_GETARG_ARRAYTYPE_P(1);


	
	rowdata_size = ARR_DIMS(rowdata)[0];
	qvalues_size = ARR_DIMS(qvalues)[0];
	rowdata_data = (float8*) ARR_DATA_PTR(rowdata);
	qvalues_data = (float8*) ARR_DATA_PTR(qvalues);


	

	
	column_size=qvalues_size/rowdata_size;

	palloc_size= column_size * sizeof(float8) + ARR_OVERHEAD_NONULLS(1);

	vari =(ArrayType *) palloc(palloc_size);
	SET_VARSIZE(vari , palloc_size);
	vari ->ndim=1;
	vari ->dataoffset = 0;
	vari ->elemtype = FLOAT8OID;
	ARR_DIMS(vari)[0]= column_size;
	ARR_LBOUND(vari)[0]=1;
	variData = (float8*) ARR_DATA_PTR(vari);
	memset(variData,0,column_size * sizeof(float8));
	for (i=0;i<column_size;i++)
	{
		
		variData[i]=0;
		for (j=0;j<rowdata_size;j++)
	{	
			variData[i]+=rowdata_data[j]*qvalues_data[i*rowdata_size+j];}
	}
	j=ARR_DIMS(vari)[0];

        PG_RETURN_ARRAYTYPE_P(vari);
}	


