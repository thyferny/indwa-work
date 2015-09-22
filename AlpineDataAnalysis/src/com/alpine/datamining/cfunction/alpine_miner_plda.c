#include "postgres.h"


#include "funcapi.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/builtins.h"

#include <math.h>
#include <string.h>

PG_FUNCTION_INFO_V1(alpine_plda_count_accum);
PG_FUNCTION_INFO_V1(alpine_plda_count_combine);
PG_FUNCTION_INFO_V1(alpine_plda_gene);
PG_FUNCTION_INFO_V1(getgrandomtopic);
PG_FUNCTION_INFO_V1(alpine_plda_first);




static int64 getgrandomtopic
   (int64 topicnumber, int64 content, int64 lastgeneassign, int64 * globwordtopic,
    int64 * lastgenetopic, int64 * topic_counts, float8 alpha, float8 eta , int64 wordnumber) 
{
	int64 j, temp_glwordtopic, singledoctopic, ret;
	float8 r, cl_prob, total_unpr;
	float8 * topic_prs = (float8 *)palloc(sizeof(float8) * topicnumber); 
	int64 bignumber;
	bignumber=1000000;
	lastgeneassign--;
	total_unpr = 0;
	for (j=0; j!=topicnumber; j++) {
		temp_glwordtopic = globwordtopic[(content-1) * topicnumber + j];
		singledoctopic = lastgenetopic[j];
		if (j == lastgeneassign) {
			temp_glwordtopic--;
			singledoctopic--;
		} 
	cl_prob = (singledoctopic + alpha) * (temp_glwordtopic + eta) /
			  (topic_counts[j] + wordnumber * eta);
		total_unpr += cl_prob;
		topic_prs[j] = total_unpr;
	}
	for (j=0; j!=topicnumber; j++){
		topic_prs[j] = topic_prs[j] / total_unpr;
	}
	r = (rand()%bignumber)/(double)bignumber;
	ret = 1;
	while (true) {
		if (ret == topicnumber || r < topic_prs[ret-1]) break;
		ret++; 
	}


	pfree(topic_prs);
	return ret;
}

Datum
alpine_plda_count_accum(PG_FUNCTION_ARGS)
{
	ArrayType *state;
	int64 * state_array_data;
	ArrayType * column_array;
	ArrayType * assign_array;
	int64 column_size,assign_size,topicnumber,wordnumber;
	int64 result_size;
	int64 size;
	int64 * column_array_data;
	int64 * assign_array_data;
	int64 k;
	if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	state = PG_GETARG_ARRAYTYPE_P(0);
	
	state_array_data = (int64*) ARR_DATA_PTR(state);
	
	column_array = PG_GETARG_ARRAYTYPE_P(1);
	column_size = ARR_DIMS(column_array)[0];
	column_array_data = (int64*) ARR_DATA_PTR(column_array);
	assign_array=PG_GETARG_ARRAYTYPE_P(2);
	assign_size = ARR_DIMS(assign_array)[0];
	assign_array_data = (int64*) ARR_DATA_PTR(assign_array);
	topicnumber=PG_GETARG_INT64(3);
	wordnumber =PG_GETARG_INT64(4);
	if (ARR_DIMS(state)[0] == 1){
	
			result_size = topicnumber*wordnumber;
       	 	size =  result_size * sizeof(int64) + ARR_OVERHEAD_NONULLS(1);
         	state = (ArrayType *) palloc(size);
   
	        SET_VARSIZE(state, size);

       	 	state->ndim = 1;
        	state->dataoffset = 0;
        	state->elemtype = INT8OID;
        	ARR_DIMS(state)[0] = result_size;
        	ARR_LBOUND(state)[0] = 1;
				state_array_data = (int64*) ARR_DATA_PTR(state);
	     	memset(state_array_data, 0,  result_size * sizeof(int64));
	}

	for ( k = 0; k < column_size; k++){
			state_array_data[topicnumber*(column_array_data[k]-1)+assign_array_data[k]-1]=state_array_data[topicnumber*(column_array_data[k]-1)+assign_array_data[k]-1]+1;
 				 	}
        PG_RETURN_ARRAYTYPE_P(state);
}	

Datum
alpine_plda_count_combine(PG_FUNCTION_ARGS)
{
	ArrayType  *state1, *state2, *result;
	int64     *state1Data, *state2Data, *resultData;
	int64         i, size;
	int64         statelen;
	if (PG_ARGISNULL(0))
	{
		if (PG_ARGISNULL(1))
			{
				PG_RETURN_NULL();
			}
		PG_RETURN_ARRAYTYPE_P(PG_GETARG_ARRAYTYPE_P(1));
	}
	if (PG_ARGISNULL(1))
		{
			PG_RETURN_ARRAYTYPE_P(PG_GETARG_ARRAYTYPE_P(0));
		}
	state1 = PG_GETARG_ARRAYTYPE_P(0);	
	state2 = PG_GETARG_ARRAYTYPE_P(1);
	
	if (ARR_NULLBITMAP(state1) || ARR_NULLBITMAP(state2) || 
		ARR_NDIM(state1) != 1 || ARR_NDIM(state2) != 1 || 
		ARR_ELEMTYPE(state1) != INT8OID || ARR_ELEMTYPE(state2) != INT8OID)
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
	
	state1Data = (int64*) ARR_DATA_PTR(state1);
	state2Data = (int64*) ARR_DATA_PTR(state2);
	
	if (ARR_DIMS(state1)[0] != ARR_DIMS(state2)[0]) 
	{
		ereport(ERROR, 
				(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
				 errmsg("preliminary segment-level calculation function \"%s\" called with invalid parameters",
					format_procedure(fcinfo->flinfo->fn_oid)),
				 errdetail("The independent-variable array is not of constant width.")));
	}
	statelen = ARR_DIMS(state1)[0];
	
	size = statelen * sizeof(int64) + ARR_OVERHEAD_NONULLS(1);
	result = (ArrayType *) palloc(size);
	SET_VARSIZE(result, size);
	result->ndim = 1;
	result->dataoffset = 0;
	result->elemtype = INT8OID;
	ARR_DIMS(result)[0] = statelen;
	ARR_LBOUND(result)[0] = 1;
	resultData = (int64*) ARR_DATA_PTR(result);
	memset(resultData, 0, statelen * sizeof(int64));
	for (i = 0; i < statelen; i++){
		resultData[i] = state1Data[i] + state2Data[i];	
	}

	PG_RETURN_ARRAYTYPE_P(result);
}


Datum
alpine_plda_first(PG_FUNCTION_ARGS)
{
	ArrayType *assign;
	ArrayType *topiccount;
	int64 * assign_array_data;
	int64 * topiccount_array_data;

	Datum values[2];
	int64 column_size,topicnumber;
	int64 temptopic;
	int64 k;
	Datum * arr1;
	bool * isnulls ;
	TupleDesc tuple;
	HeapTuple ret;
	Datum * arr2;
	if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	column_size=PG_GETARG_INT64(0);
	topicnumber=PG_GETARG_INT64(1);
	arr1 = palloc0(column_size * sizeof(Datum));
  	assign = construct_array(arr1,column_size,INT8OID,8,true,'i');
	assign_array_data = (int64 *)ARR_DATA_PTR(assign);
	
	 arr2 = palloc0(topicnumber * sizeof(Datum));
	topiccount = construct_array(arr2,topicnumber,INT8OID,8,true,'i');
	topiccount_array_data = (int64 *)ARR_DATA_PTR(topiccount);
	for ( k = 0; k < column_size; k++){
			temptopic = random() % topicnumber + 1;
			assign_array_data[k] = temptopic;
			topiccount_array_data[temptopic-1]++;		 	
			}
	values[0] = PointerGetDatum(assign);
	values[1] = PointerGetDatum(topiccount);

	
	if (get_call_result_type(fcinfo, NULL, &tuple) != TYPEFUNC_COMPOSITE)
		ereport(ERROR,
			(errcode( ERRCODE_FEATURE_NOT_SUPPORTED ),
			 errmsg( "function returning record called in context "
				 "that cannot accept type record" )));
	tuple = BlessTupleDesc(tuple);
	isnulls = palloc0(2 * sizeof(bool));
	ret = heap_form_tuple(tuple, values, isnulls);
 
	if (isnulls[0] || isnulls[1])
		ereport(ERROR,
			(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
			 errmsg("function \"%s\" produced null results",
				format_procedure(fcinfo->flinfo->fn_oid))));
 PG_RETURN_DATUM(HeapTupleGetDatum(ret));
      
}	



Datum
alpine_plda_gene(PG_FUNCTION_ARGS)
{
	ArrayType *assign;
	ArrayType *allassign;
	ArrayType *wordtopic;
	ArrayType *lastinfo;
	ArrayType *topiccount;
	ArrayType * column_array;
	ArrayType * lastassign;
	int64 * assign_array_data;
	int64 * wordtopic_data;
	int64 * lastinfo_data;
	int64 * lastassign_data;
	int64 * allassign_data;
	int64 * topiccount_array_data;
	int64 * column_array_data;

	Datum values[2];
	int64 column_size,topicnumber,wordnumber;
	int64 temptopic;
	int64 k;
	float8 alpha,beta;
	bool * isnulls ;
	TupleDesc tuple;
	HeapTuple ret;
	Datum * arr1;
	Datum * arr2;
	if (PG_ARGISNULL(0)){
		 PG_RETURN_NULL();
	}
	column_array=PG_GETARG_ARRAYTYPE_P(0);
	allassign=PG_GETARG_ARRAYTYPE_P(1);
	wordtopic=PG_GETARG_ARRAYTYPE_P(2);
	lastassign=PG_GETARG_ARRAYTYPE_P(3);
	lastinfo=PG_GETARG_ARRAYTYPE_P(4);
	alpha=PG_GETARG_FLOAT8(5);
	beta=PG_GETARG_FLOAT8(6);
	wordnumber=PG_GETARG_INT64(7);
	topicnumber=PG_GETARG_INT64(8);
	
	
	column_array_data = (int64*) ARR_DATA_PTR(column_array);
	allassign_data = (int64*) ARR_DATA_PTR(allassign);
	lastinfo_data= (int64*) ARR_DATA_PTR(lastinfo);
	lastassign_data= (int64*) ARR_DATA_PTR(lastassign);
	wordtopic_data=(int64*) ARR_DATA_PTR(wordtopic);
	column_size=ARR_DIMS(column_array)[0];
	arr1 = palloc0(column_size * sizeof(Datum));
  	assign = construct_array(arr1,column_size,INT8OID,8,true,'i');
	assign_array_data = (int64 *)ARR_DATA_PTR(assign);
	
	arr2 = palloc0(topicnumber * sizeof(Datum));
	topiccount = construct_array(arr2,topicnumber,INT8OID,8,true,'i');
	topiccount_array_data = (int64 *)ARR_DATA_PTR(topiccount);
	for ( k = 0; k < column_size; k++){
		temptopic =getgrandomtopic
   		(topicnumber, column_array_data[k], lastassign_data[k], wordtopic_data,
    	lastinfo_data, allassign_data, alpha,  beta ,  wordnumber) ;
		assign_array_data[k] = temptopic;
		topiccount_array_data[temptopic-1]++;		 	
	}
	values[0] = PointerGetDatum(assign);
	values[1] = PointerGetDatum(topiccount);

	
	if (get_call_result_type(fcinfo, NULL, &tuple) != TYPEFUNC_COMPOSITE)
		ereport(ERROR,
			(errcode( ERRCODE_FEATURE_NOT_SUPPORTED ),
			 errmsg( "function returning record called in context "
				 "that cannot accept type record" )));
	tuple = BlessTupleDesc(tuple);
	isnulls = palloc0(2 * sizeof(bool));
	ret = heap_form_tuple(tuple, values, isnulls);
  	if (isnulls[0] || isnulls[1])
		ereport(ERROR,
			(errcode(ERRCODE_INVALID_PARAMETER_VALUE),
			 errmsg("function \"%s\" produced null results",
				format_procedure(fcinfo->flinfo->fn_oid))));
 PG_RETURN_DATUM(HeapTupleGetDatum(ret));
      
}
	
	