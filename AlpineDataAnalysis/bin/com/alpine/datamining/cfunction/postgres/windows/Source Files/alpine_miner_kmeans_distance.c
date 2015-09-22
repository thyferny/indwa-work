#include "postgres.h"
#include "fmgr.h"
#include "catalog/pg_type.h"
#include "utils/array.h"
#include "utils/lsyscache.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define LOG2   0.69315

float  alpine_miner_min_result(float a[],int k)
{
 	int i;
	float min=a[0];
	float temp;
	for(i=0;i<k;i++)
		{
			if(min>a[i])
				{
					min=a[i];
				}
		}
	return min;
}

float  alpine_miner_resultEuclideanDistance(Datum *sample_data, int sample_count, Datum *data_data,  int data_count,int k,int distanceMode)
{
	int i;
	int j;

	float sample;
	float data;
	float distance;
	float temp1;
	float temp2;
	float temp3;

	float *result;
	result =  (float*)palloc(k * sizeof(float));
	//const float LOG2=log(2);
	i=0;
	j=0;
	
	while(i<k)
		{
			j=data_count*i;
			distance=0.0;
			temp1=0.0;
			temp2=0.0;
			temp3=0.0;
			while(j<data_count*(i+1))
				{
					sample=DatumGetFloat8(sample_data[j]);
					data=DatumGetFloat8(data_data[j%data_count]);
					
					if(distanceMode==1)
					{
						distance=distance+(sample-data)*(sample-data);
					}
					else if(distanceMode==2)
					{
						temp1=temp1+(sample*logf(sample/data));
						temp2=temp2+(sample-data);
					}
					else if(distanceMode==3)
					{
						distance=distance+(sample*(logf(sample/data)/LOG2));
					}
					else if(distanceMode==4)
					{
						distance=distance+fabsf(sample-data)/fabsf(sample+data);
					}
					else if(distanceMode==5)
					{
						distance=distance+fabsf(sample-data);
					}
					else if(distanceMode==6)
					{
						temp1=temp1+(sample*data);
						temp2=temp2+(sample*sample);
						temp3=temp3+(data*data);
					}else if(distanceMode==7)
					{
						temp1=temp1+(sample*data);
						temp2=temp2+(sample);
						temp3=temp3+(data);
					}
					else if(distanceMode==8)
					{
						distance=distance+(sample*data);
					}
					else if(distanceMode==9)
					{
						temp1=temp1+(sample*data);
						temp2=temp2+(sample);
						temp3=temp3+(data);
					}
					j++;
				}
			if(distanceMode==1)
             {
                distance=sqrtf(distance);
             }
			if(distanceMode==2)
			{
				distance=temp1-temp2;
			}
			if(distanceMode==6)
			{
				distance=acosf(temp1/(sqrtf(temp2)*sqrtf(temp3)));
			}
			if(distanceMode==7)
			{
				distance=(-2*(temp1/(temp2+temp3)));
			}
			if(distanceMode==9)
			{
				distance=(-temp1/(temp2+temp3-temp1));
			}
			result[i]=distance;
			i++;
		}

	return alpine_miner_min_result(result,k);

}

int  alpine_miner_min_loop(float a[],int k)
{
 	int i;
	int j;
	//elog(WARNING,"a[0]%f", a[0]);
	//elog(WARNING,"a[1]%f", a[1]);
	//elog(WARNING,"a[2]%f", a[2]);
	float temp;
	float min=a[0];
		j=0;

	for(i=0;i<k;i++)
		{
			if(min>a[i])
				{
					min=a[i];
					j=i;
				}
		}
	return j;
}

int  alpine_miner_loopEuclideanDistance(Datum *sample_data, int sample_count, Datum *data_data,  int data_count,int k,int distanceMode)
{
	int i;
	int j;
	float sample;
	float data;
	float distance;
	float temp1;
	float temp2;
	float temp3;

	//const float LOG2=log(2);

	float *result;
	result =  (float*)palloc(k * sizeof(float));
	
	i=0;
	j=0;

	while(i<k)
		{
			j=data_count*i;
			distance=0.0;
			temp1=0.0;
			temp2=0.0;
			temp3=0.0;
			while(j<data_count*(i+1))
				{
					sample=DatumGetFloat8(sample_data[j]);
					data=DatumGetFloat8(data_data[j%data_count]);

					if(distanceMode==1)
					{
						distance=distance+(sample-data)*(sample-data);					
					}
					else if(distanceMode==2)
					{
						temp1=temp1+(sample*logf(sample/data));
						temp2=temp2+(sample-data);
					}
					else if(distanceMode==3)
					{	
						distance=distance+(sample*(logf(sample/data)/LOG2));
					}
					else if(distanceMode==4)
					{
						distance=distance+fabsf(sample-data)/fabsf(sample+data);
					}
					else if(distanceMode==5)
					{
						distance=distance+fabsf(sample-data);
					}
					else if(distanceMode==6)
					{
						temp1=temp1+(sample*data);
						temp2=temp2+(sample*sample);
						temp3=temp3+(data*data);
					}
					else if(distanceMode==7)
					{
						temp1=temp1+(sample*data);
						temp2=temp2+(sample);
						temp3=temp3+(data);
					}
					else if(distanceMode==8)
					{
						distance=distance+(sample*data);
					}
					else if(distanceMode==9)
					{
						temp1=temp1+(sample*data);
						temp2=temp2+(sample);
						temp3=temp3+(data);
					}
					j++;
				}	
			if(distanceMode==2)
			{
				distance=temp1-temp2;
			}
			if(distanceMode==6)
			{
				distance=acosf(temp1/(sqrtf(temp2)*sqrtf(temp3)));
			}
			if(distanceMode==7)
			{
				distance=(-2*(temp1/(temp2+temp3)));
			}
			if(distanceMode==9)
			{
				distance=(-temp1/(temp2+temp3-temp1));
			}
			result[i]=distance;
			i++;
		}

	return alpine_miner_min_loop(result,k);
}



PG_FUNCTION_INFO_V1(alpine_miner_kmeans_distance_loop);
Datum
alpine_miner_kmeans_distance_loop(PG_FUNCTION_ARGS)
{
	 ArrayType  *sample_arg, *data_arg;
	 Datum     *sample_data, *data_data;
	 Oid         sample_eltype,data_eltype;
	 int16       sample_typlen, data_typlen;
	 bool        sample_typbyval, data_typbyval;
	 char        sample_typalign, data_typalign;
	 bool       *sample_nulls, *data_nulls;
	 int         sample_count, data_count,cluster,k,distanceMode;
	 
	 
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2) ||PG_ARGISNULL(3))
	{
		PG_RETURN_NULL();
	}
	 /* get sample_arg args */
	 sample_arg=PG_GETARG_ARRAYTYPE_P(0);
	  /* get sample_arg array element type */
	   sample_eltype = ARR_ELEMTYPE(sample_arg);

	  get_typlenbyvalalign(sample_eltype, &sample_typlen, &sample_typbyval, &sample_typalign);

	  deconstruct_array(sample_arg, sample_eltype, 
		sample_typlen, sample_typbyval, sample_typalign,
		&sample_data, &sample_nulls, &sample_count);

	    /* get data_arg args */
        data_arg = PG_GETARG_ARRAYTYPE_P(1);
	/* get data_arg array element type */	
	data_eltype = ARR_ELEMTYPE(data_arg);

	get_typlenbyvalalign(data_eltype, &data_typlen, &data_typbyval, &data_typalign);

	 deconstruct_array(data_arg, data_eltype, 
		data_typlen, data_typbyval, data_typalign,
		&data_data, &data_nulls, &data_count);

	 k=PG_GETARG_INT32(2);
	 distanceMode=PG_GETARG_INT32(3);

	 cluster=alpine_miner_loopEuclideanDistance(sample_data,sample_count,data_data,data_count,k,distanceMode);

	 pfree(sample_data);
        pfree(sample_nulls);
        pfree(data_data);
        pfree(data_nulls);

	PG_RETURN_INT32(cluster);
}


PG_FUNCTION_INFO_V1(alpine_miner_kmeans_distance_result);
Datum
alpine_miner_kmeans_distance_result(PG_FUNCTION_ARGS)
{


	 ArrayType  *sample_arg, *data_arg;
	 Datum     *sample_data, *data_data;
	 Oid         sample_eltype,data_eltype;
	 int16       sample_typlen, data_typlen;
	 bool        sample_typbyval, data_typbyval;
	 char        sample_typalign, data_typalign;
	 bool       *sample_nulls, *data_nulls;
	 int         sample_count, data_count,k,distanceMode;
	 float       len;
	 
	if (PG_ARGISNULL(0) || PG_ARGISNULL(1) || PG_ARGISNULL(2)||PG_ARGISNULL(3) )
	{
		PG_RETURN_NULL();
	}

	 /* get sample_arg args */
	 sample_arg=PG_GETARG_ARRAYTYPE_P(0);
	  /* get sample_arg array element type */
	   sample_eltype = ARR_ELEMTYPE(sample_arg);

	  get_typlenbyvalalign(sample_eltype, &sample_typlen, &sample_typbyval, &sample_typalign);

	  deconstruct_array(sample_arg, sample_eltype, 
		sample_typlen, sample_typbyval, sample_typalign,
		&sample_data, &sample_nulls, &sample_count);

	    /* get data_arg args */
        data_arg = PG_GETARG_ARRAYTYPE_P(1);
	/* get data_arg array element type */	
	data_eltype = ARR_ELEMTYPE(data_arg);

	get_typlenbyvalalign(data_eltype, &data_typlen, &data_typbyval, &data_typalign);

	 deconstruct_array(data_arg, data_eltype, 
		data_typlen, data_typbyval, data_typalign,
		&data_data, &data_nulls, &data_count);

	 k=PG_GETARG_INT32(2);
	 distanceMode=PG_GETARG_INT32(3);

	 len=alpine_miner_resultEuclideanDistance(sample_data,sample_count,data_data,data_count,k,distanceMode);

	 pfree(sample_data);
        pfree(sample_nulls);
        pfree(data_data);
        pfree(data_nulls);

	PG_RETURN_FLOAT8(len);
}





