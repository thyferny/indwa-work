#include <math.h>
#include <float.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <oci.h>
#include <ociextp.h>

static const double ALPINE_MINER_THRESH = 30.;
static const double ALPINE_MINER_MTHRESH = -30.;


void checkerr(err, status)
	OCIError *err;
	sword status;
{
	text errbuf[512];
	sb4 errcode = 0;

	switch (status)
	{
		case OCI_SUCCESS:
			break;
		case OCI_SUCCESS_WITH_INFO:
			(void) printf("Error - OCI_SUCCESS_WITH_INFO\n");
			break;
		case OCI_NEED_DATA:
			(void) printf("Error - OCI_NEED_DATA\n");
			break;
		case OCI_NO_DATA:
			(void) printf("Error - OCI_NODATA\n");
			break;
		case OCI_ERROR:
			(void) OCIErrorGet((dvoid *)err, (ub4) 1, (text *) NULL, &errcode,
					   errbuf, (ub4) sizeof(errbuf), OCI_HTYPE_ERROR);
			(void) printf("Error - %.*s\n", 512, errbuf);
			break;
		case OCI_INVALID_HANDLE:
			(void) printf("Error - OCI_INVALID_HANDLE\n");
			break;
		case OCI_STILL_EXECUTING:
			(void) printf("Error - OCI_STILL_EXECUTE\n");
			break;
		case OCI_CONTINUE:
			(void) printf("Error - OCI_CONTINUE\n");
			break;
		default:
			break;
	}
}


int  alpine_miner_faa2fa1(OCIExtProcContext *ctx, OCIColl * f_arrayarray, OCIColl ** f_array)
{
	OCIEnv        *env;
	OCIServer     *srv;
	OCISvcCtx     *svc;
	OCIError      *err;
	sword errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	return alpine_miner_faa2fa(env,err, f_arrayarray, f_array);
}
int  alpine_miner_faa2fa(OCIEnv *env,OCIError * err,OCIColl * f_arrayarray, OCIColl ** f_array)
{
	//OCIEnv        *env;
	OCIServer     *srv;
	OCISvcCtx     *svc;
	//OCIError      *err;
	OCIType           *tdo = (OCIType *)0;
	int i = 0;         
	int j = 0; 
	int k = 0;
	OCIColl ** ret = f_array;

	sword        status;
	OCIString    *client_elem;
	OCIIter      *iterator;
	boolean      eoc;
	void         *elem;
	void         *elem_null;
	OCIInd       *elemind;


	OCIEnv       *env2;
	OCIError     *err2;
	text         *text_ptr2;
	sword        status2;
	OCIColl     *clients2;
	OCIString    *client_elem2;
	OCIIter      *iterator2;
	boolean      eoc2;
	void         *elem2;
	void         *elem_null2;
	OCIInd       *elemind2;

	OCIColl *inner_coll;
	OCIColl *inner_coll1;
	OCIIter *itr1, *itr2;
	double *instance;
	OCIType *coll_tdo= (OCIType *)0;
	int        errcode; 
	checkerr(err, OCIIterCreate(env, err, f_arrayarray, &itr1));
	for(eoc = FALSE;!OCIIterNext(env, err, itr1, (void  **) &elem,(void  **) &elem_null, &eoc) && !eoc;)
	{
		inner_coll = (OCIColl *)elem;
		inner_coll1 = (OCIColl *)elem;
		checkerr(err, OCIIterCreate(env, err, inner_coll, &itr2));
		for(eoc2 = FALSE;!OCIIterNext(env, err, itr2, (void  **)&elem2,(void  **) &elem_null2, &eoc2) && !eoc2;)
		{
			OCICollAppend(env, err, elem2,
					NULL, *ret);
		}
		checkerr(err, OCIIterDelete(env, err, &itr2));
	}
	checkerr(err, OCIIterDelete(env, err, &itr1));
	return 1;
}

double alpine_miner_compute_pi(OCIEnv *env, OCIError * err, OCIColl *beta, OCIColl *columns, int add_intercept_arg)
{
	double gx, pi;
	int i;
	i = 0;
	gx = 0;

	//OCIEnv       *env;
	//OCIError     *err;
	OCISvcCtx     *svc;
	sword        status;
	OCIIter      *iterator;
	boolean      eoc;
	void         *elem;
	OCIInd       *elemind;

	//int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	status = OCIIterCreate(env, err, columns, &iterator);
//	return 0.1;

	int index = 0;
	for (eoc = FALSE; !OCIIterNext(env, err, iterator, &elem,
				(void  **) &elemind, &eoc) && !eoc; index++)
	{
		boolean exists;
		void * elem_temp;
		OCICollGetElem(env, err, columns,
				index, &exists, (void**)&elem_temp,
				(void **)&elemind);
		double column_elem;
		OCINumber*
			instance = (OCINumber *)elem_temp;
		OCINumberToReal ( err,
				instance,
				sizeof(double),
				&column_elem);
		OCICollGetElem(env, err, beta,
				index, &exists, (void**)&elem_temp,
				(void **)&elemind);
		double beta_elem = 0;
		instance = (OCINumber *)elem_temp;
		OCINumberToReal ( err,
				instance,
				sizeof(double),
				&beta_elem);
		gx = gx + beta_elem * column_elem;

	}

	//return 0.2;
	if (status != OCI_SUCCESS)
	{
		/* handle error */
	}
	status = OCIIterDelete(env, err, &iterator);
	if (add_intercept_arg)
	{
		sb4 size;
		OCICollSize( env, err, beta,
				&size );
		boolean exists;
		void * elem_temp;
		status = OCICollGetElem(env, err, beta,
				size - 1, &exists, (void**)&elem_temp,
				(void **)&elemind);
		OCINumber* instance = (OCINumber *)elem_temp;
		double value = 0;
		OCINumberToReal ( err,
				(OCINumber *)elem_temp,
				//instance,
				sizeof(double),
				&value);
		gx = gx + value;
	}

	/*compute pi*/
	//pi = 1.0/(1.0 + exp(-1.0*gx));
	double tmp = 0;
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
	//return 0.4;
	return pi;
}

// create or replace function
double alpine_miner_lr_ca_pi(OCIExtProcContext *ctx,OCIColl *beta, OCIColl *columns, int add_intercept_arg, OCIColl *beta_array, OCIColl *columns_array)
{
	OCIEnv       *env;
	OCIError     *err;
	OCISvcCtx     *svc;
	int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	alpine_miner_faa2fa(env, err,beta, &beta_array);
	alpine_miner_faa2fa(env, err, columns, &columns_array);
	//return 0.1;
	return alpine_miner_compute_pi(env, err,beta_array, columns_array, add_intercept_arg);
}

/* compute hessian matrix*/
//OCIColl * 
int alpine_miner_compute_hessian(OCIEnv *env,OCIError * err,OCIColl *beta, OCIColl *columns, OCIColl ** return_array,
		double weight_arg, int add_intercept_arg, double pi)
{
	int i = 0;
	int j = 0;
	int ind = 0;;
	double x,y;

//	OCIEnv       *env;
//	OCIError     *err;
	OCISvcCtx     *svc;
	sword        status;
	double    *beta_elem;
	OCIIter      *iterator;
	boolean      eoc;
	void         *elem;
	OCIInd       *elemind;

	sb4 beta_count;

	//int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 


	OCICollSize(env, err, beta,
			&beta_count);

	while (i < beta_count)
	{
		if ((i == (beta_count - 1)) && add_intercept_arg)
		{
			x = 1.0;
		}
		else
		{
			boolean exists;
			void * elem_temp;
			OCICollGetElem(env, err, columns,
					i, &exists, (void **)&elem_temp,
					(void **)&elemind);
	
			OCINumber*
			instance = (OCINumber *)elem_temp;
			OCINumberToReal ( err,
                        instance,
                        sizeof(double),
                        &x);

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
				boolean exists;
				void * elem_temp;
				OCICollGetElem(env, err, columns,
						j, &exists, (void**)&elem_temp,
						(void **)&elemind);
				OCINumber*
					instance = (OCINumber *)elem_temp;
				OCINumberToReal ( err,
						instance,
						sizeof(double),
						&y);
			}
			double ret = -x*y*weight_arg*pi*(1.0 - pi);
			OCINumber elem;
			status = OCINumberFromReal(err, &ret, sizeof(ret), &elem);
			//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
			OCICollAppend(env, err, (const void  *)&elem,
					NULL, *return_array);
			ind = ind + 1;
			j = j + 1;
		}
		i = i + 1;
	}
	return 1;
};
//OCIColl * 
int alpine_miner_compute_xwz(OCIEnv *env,OCIError * err,OCIColl *columns, OCIColl **return_array,
		double weight_arg, int y, int add_intercept_arg, double pi )
{
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

	double foo = weight_arg * pi*(1-pi)*(eta+(y - pi)/mu_eta_dev);

	//OCIEnv       *env;
//	OCIError     *err;
	OCISvcCtx     *svc;
	sword        status;
	double    *beta_elem;
	OCIIter      *iterator;
	boolean      eoc;
	void         *elem;
	OCIInd       *elemind;

	sb4 columns_count;
	//int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	OCICollSize(env, err, columns,
			&columns_count);

	while (i < columns_count)
	{
		boolean exists;
		void * elem_temp;
		OCICollGetElem(env, err, columns,
				i, &exists, (void **)&elem_temp,
				(void **)&elemind);
		double column_elem;
		OCINumber*
			instance = (OCINumber *)elem_temp;
		OCINumberToReal ( err,
				instance,
				sizeof(double),
				&column_elem);
		double ret = column_elem * foo;
		OCINumber elem;
		status = OCINumberFromReal(err, &ret, sizeof(ret), &elem);
		//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
		OCICollAppend(env, err, (const void  *)&elem,
				NULL, *return_array);
		i = i + 1;
	}
	if (add_intercept_arg)
	{
		double ret = foo;
		OCINumber elem;
		status = OCINumberFromReal(err, &ret, sizeof(ret), &elem);
		//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
		OCICollAppend(env, err, (const void  *)&elem,
				NULL, *return_array);
	}
	return 1;

};
//OCIColl * 
	int alpine_miner_compute_derivative(OCIEnv *env,OCIError * err,OCIColl *columns, OCIColl ** return_array 
		,double weight_arg, int y, int add_intercept_arg, double pi)
{
	int i = 0;
	double foo = weight_arg * (y - pi);

	//OCIEnv       *env;
	//OCIError     *err;
	OCISvcCtx     *svc;
	sword        status;
	double    *beta_elem;
	OCIIter      *iterator;
	boolean      eoc;
	void         *elem;
	OCIInd       *elemind;

	sb4 columns_count;
	//int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	OCICollSize(env, err, columns,
			&columns_count);

	while (i < columns_count)
	{
		boolean exists;
		void * elem_temp;
		OCICollGetElem(env, err, columns,
				i, &exists, (void **)&elem_temp,
				(void **)&elemind);
		double column_elem;
		OCINumber*
			instance = (OCINumber *)elem_temp;
		OCINumberToReal ( err,
				instance,
				sizeof(double),
				&column_elem);
		double ret = column_elem * foo;
		OCINumber elem;
		status = OCINumberFromReal(err, &ret, sizeof(ret), &elem);
		//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
		OCICollAppend(env, err, (const void  *)&elem,
				NULL, *return_array);
		i = i + 1;
	}
	if (add_intercept_arg)
	{
		double ret = foo;

		OCINumber elem;
		status = OCINumberFromReal(err, &ret, sizeof(ret), &elem);
		//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
		OCICollAppend(env, err, (const void  *)&elem,
				NULL, *return_array);
	}
	return 1;
};

//PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_he);

//	OCIColl *
int alpine_miner_lr_ca_he(OCIExtProcContext *ctx, OCIColl * beta, OCIColl * columns, int add_intercept_arg, double weight_arg,OCIColl * beta_array, OCIColl * columns_array, OCIColl ** return_array)
{
	if (beta == NULL || columns == NULL)
	{
		return 0;
	}

	int         i;
	OCIEnv        *env;
        OCIServer     *srv;
        OCISvcCtx     *svc;
        OCIError      *err;
        sword errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	alpine_miner_faa2fa(env,err, beta,&beta_array);
	alpine_miner_faa2fa(env,err, columns,&columns_array);

	double gx = 0.0;
	double pi = 0.0;

	//result_count = beta_count *(beta_count+1)/2;
	//return_array = (Datum *)palloc(result_count * sizeof(Datum));
	pi = alpine_miner_compute_pi(env,err, beta_array, columns_array, add_intercept_arg);
	alpine_miner_compute_hessian(env,err, beta_array, columns_array, return_array
			,weight_arg, add_intercept_arg,  pi);

	return 1;
}

//PG_FUNCTION_INFO_V1(alpine_miner_lr_ca_beta);

//	OCIColl * 
int alpine_miner_lr_ca_beta(OCIExtProcContext *ctx,OCIColl * beta, OCIColl * columns, int add_intercept_arg, double weight_arg, int y_arg, int times_arg, OCIColl * beta_array, OCIColl * columns_array, OCIColl ** return_array)
{
	if (beta == NULL || columns == NULL)
	{
		return 0;
	}
	OCISvcCtx     *svc;
	OCIEnv       *env;
	OCIError     *err;
	OCIInd       *elemind;
	int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 

	alpine_miner_faa2fa(env,err, beta, &beta_array);
	alpine_miner_faa2fa(env,err, columns,&columns_array);
	int         i;

	double fitness = 0.0;
	double gx = 0.0;
	double pi = 0.0;


	/* construct result array */
	//	result_count = beta_count *(beta_count+1)/2 + beta_count + 1;
	//       return_array = (Datum *)palloc(result_count * sizeof(Datum));

	if (times_arg == 0)
	{
		//(weights * y + 0.5)/(weights + 1)
		pi = (weight_arg * y_arg + 0.5)/(weight_arg + 1);
	}
	else
	{
		pi = alpine_miner_compute_pi(env,err, beta_array, columns_array, add_intercept_arg);
	}

	/* compute derivative */
	alpine_miner_compute_hessian(env, err,beta_array,columns_array, return_array
			,weight_arg, add_intercept_arg,  pi);

	alpine_miner_compute_xwz(env,err, columns_array, return_array, 
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


	OCINumber elem;
	sword status = OCINumberFromReal(err, &fitness, sizeof(fitness), &elem);
	//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
	OCICollAppend(env, err, (const void  *)&elem,
			NULL, *return_array);

	return 1;
}

//create or replace function alpine_miner_lr_ca_he_de
	//OCIColl *
int alpine_miner_lr_ca_he_de(OCIExtProcContext *ctx,OCIColl *beta, OCIColl *columns, int add_intercept,double weight_arg, int y_arg , OCIColl * beta_array, OCIColl * columns_array, OCIColl ** return_array)
{

	if (beta == NULL || columns == NULL)
	{
		return 0;
	}

	OCIEnv       *env;
	OCIError     *err;
	OCISvcCtx     *svc;
	//OCIInd       *elemind;
	int errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	int         i;
	double fitness = 0.0;
	double gx = 0.0;
	double pi = 0.0;

	alpine_miner_faa2fa(env,err, beta, &beta_array);
	alpine_miner_faa2fa(env,err, columns,&columns_array);

	pi = alpine_miner_compute_pi(env,err, beta_array, columns_array, add_intercept);


	/* compute derivative */
	alpine_miner_compute_hessian(env,err, beta_array, columns_array, return_array
			,weight_arg, add_intercept,  pi);

	alpine_miner_compute_derivative(env, err,columns_array, return_array, 
			weight_arg, y_arg, add_intercept, pi);

	if (y_arg == 1)
	{
		fitness = log(pi);
	}
	else
	{
		fitness = log(1.0 - pi);
	}
	fitness *= weight_arg;

	OCINumber elem;
	sword tatus = OCINumberFromReal(err, &fitness, sizeof(fitness), &elem);
	//if (status != OCI_SUCCESS)  /* handle error from OCINumberFromReal */;
	OCICollAppend(env, err, (const void  *)&elem,
			NULL, *return_array);

	return 1;
}

// create or replace function alpine_miner_lr_ca_fitness() 
	double
alpine_miner_lr_ca_fitness(OCIExtProcContext *ctx,OCIColl *beta, OCIColl *columns, int add_intercept,double weight_arg, int label_value_arg, OCIColl * beta_array, OCIColl *columns_array)
{
	if (beta == NULL || columns == NULL)
	{
		return 0.0;
	}
	OCIEnv        *env;
	OCIServer     *srv;
	OCISvcCtx     *svc;
	OCIError      *err;
	sword errcode = OCIExtProcGetEnv(ctx, &env, &svc, &err); 
	alpine_miner_faa2fa(env, err,beta, &beta_array);
	alpine_miner_faa2fa(env, err,columns, &columns_array);
	double pi = alpine_miner_compute_pi(env, err,beta_array, columns_array, add_intercept);
	
	double fitness;
	if (label_value_arg == 1)
	{
		fitness = log(pi);
	}
	else
	{
		fitness = log(1.0 - pi);
	}
	fitness *= weight_arg;
	return fitness;
}



