
create or replace TYPE alpine_miner_svm_model AS object(
       inds int,
       cum_err binary_double,
       epsilon binary_double,
       rho binary_double,
       b   binary_double,
       nsvs int,
       ind_dim int,
       weights floatarray,
       individuals floatarray
);
/

create or replace TYPE alpine_miner_svm_model_faa AS object(
       inds int,
       cum_err binary_double,
       epsilon binary_double,
       rho binary_double,
       b   binary_double,
       nsvs int,
       ind_dim int,
       weights floatarrayarray,
       individuals floatarrayarray
);
/

create or replace FUNCTION alpine_miner_dot_kernel(x floatarray, y floatarray) RETURN binary_double AS
	len int;
	ret binary_double := 0;
BEGIN
	if x.count() < y.count() then
		len := x.count();
	else
		len := y.count();
	end if;
	FOR i in 1..len LOOP
	    ret := ret + x(i)*y(i);
	END LOOP;
	RETURN ret;
END;
/

CREATE OR REPLACE FUNCTION alpine_miner_polynomial_kernel(x floatarray, y floatarray, degree int) RETURN binary_double AS
BEGIN
	RETURN power(alpine_miner_dot_kernel(x,y),degree);
END;
/


create or replace
FUNCTION alpine_miner_gaussian_kernel(x floatarray, y floatarray, gamma binary_double) RETURN binary_double AS
	i int;
	len int;
	temp binary_double;
	diff floatarray := floatarray();
BEGIN
	if (x.count() < y.count) then
		len := x.count();
	else 
		len := y.count();
	end if;
	diff := floatarray();
	for i in 1..len loop
		diff.extend();
		diff(i) := x(i) - y(i);
	end loop;
	temp := -1.0 * gamma * alpine_miner_dot_kernel(diff,diff);
	if (temp < -30) then
		temp := -30;
	elsif(temp > 30) then
		temp := 30;
	end if;
	RETURN exp(temp);
END;
/
create or replace FUNCTION alpine_miner_kernel(x floatarray, y floatarray, kernel_type int, degree int, gamma binary_double) RETURN binary_double AS

	len INT;
BEGIN
	if kernel_type = 1 then
		RETURN alpine_miner_dot_kernel(x, y);
	else
		if kernel_type = 2 then
			return alpine_miner_polynomial_kernel(x, y, degree);
		else
			if kernel_type = 3 then
				return alpine_miner_gaussian_kernel(x, y, gamma);
			else
				return alpine_miner_dot_kernel(x, y);
			end if;
		end if;
	end if;


END;
/
create or replace FUNCTION
alpine_miner_svs_predict_fa(svs alpine_miner_svm_model, ind floatarray, kernel_type int, degree int, gamma binary_double)
RETURN binary_double AS

	ret binary_double := 0;
	individual floatArray ;
	i int;
	j int;
BEGIN
	FOR i IN 1..svs.nsvs LOOP
	    individual := floatArray();
	    FOR j IN 1..svs.ind_dim LOOP
	        individual.extend();
		individual(j) := svs.individuals(svs.ind_dim*(i - 1) + j);
	    END LOOP;
	    ret := ret + svs.weights(i) * alpine_miner_kernel(individual, ind , kernel_type, degree, gamma);
        END LOOP;
	RETURN ret;
END;
/

create or replace FUNCTION
alpine_miner_svs_predict(svs_faa alpine_miner_svm_model_faa, ind floatarrayarray, kernel_type int, degree int, gamma binary_double)
RETURN binary_double AS

	ret binary_double := 0;
	svs alpine_miner_svm_model;
	weights floatarray;
	individuals floatarray;
	ind_fa floatarray;
BEGIN
	weights := alpine_miner_faa2fa(svs_faa.weights);
	individuals := alpine_miner_faa2fa(svs_faa.individuals);
	ind_fa :=  alpine_miner_faa2fa(ind);
    	svs := alpine_miner_svm_model(svs_faa.inds, svs_faa.cum_err, svs_faa.epsilon, svs_faa.rho, svs_faa.b, svs_faa.nsvs, svs_faa.ind_dim, weights, individuals);
	RETURN alpine_miner_svs_predict_fa(svs, ind_fa , kernel_type, degree, gamma);
END;
/

create or replace
FUNCTION alpine_miner_online_sv_reg(table_name VARCHAR2,ind varchar2,label varchar2,wherecond varchar2array, kernel_type int, degree int, gamma binary_double, eta binary_double, slambda binary_double, nu binary_double)
RETURN alpine_miner_svm_model AS
  TYPE crt IS REF CURSOR;
	p binary_double;
	diff binary_double;
	error binary_double;
	weight binary_double;
  svs alpine_miner_svm_model := null;
  sqlstr varchar2(32767);
  c1 crt;
  fa floatArray;
  faa floatArrayArray;
  labelvalue binary_double;
  cap binary_double;
  i int := 0;
  j int := 0;
  wherestr varchar2(32767):= '';

BEGIN
  for i in 1..wherecond.count() loop
  	if(i != 1) then
  		wherestr := wherestr||' and ';
  	end if;
  	wherestr := wherestr||wherecond(i)||' is not null ';
  end loop;
  wherestr := wherestr||' and '||label||' is not null ';
  sqlstr := 'select '||ind||','||label||' from '||table_name||' where '||wherestr;
  svs := alpine_miner_svm_model(0,0,0,0,1,0,0,floatarray(),floatarray());
  open c1 for sqlstr;
  i := 0;
  j := 0;
  LOOP

  FETCH c1 INTO faa, labelvalue;
  EXIT WHEN c1%NOTFOUND;

  fa := alpine_miner_faa2fa(faa);
  IF j = 0 THEN
  svs.ind_dim := fa.count();
  END IF;

    p := alpine_miner_svs_predict_fa(svs, fa , kernel_type, degree, gamma);

    diff := labelvalue - p;
    error := abs(diff);
    svs.inds := svs.inds + 1;
    svs.cum_err := svs.cum_err + error;

    cap := 0.1 + 1 / (1 - eta * slambda);

    IF (error > svs.epsilon) THEN
        FOR i IN 1..svs.nsvs LOOP
	  if (abs(svs.weights(i)) < (cap + 0.1) * 2.2250738585072014e-308) then 
            svs.weights(i) := 0;
	  else 
            svs.weights(i) := svs.weights(i) * (1 - eta * slambda);
          end if;
        END LOOP;
        weight := eta;
        IF (diff < 0) THEN weight := -1 * weight; END IF;
        svs.nsvs := svs.nsvs + 1;
        svs.weights.extend();
        svs.weights(svs.nsvs) := weight;
	FOR i IN 1..fa.count() LOOP
          svs.individuals.extend();
          svs.individuals(svs.individuals.count()) := fa(i);
	END LOOP;
        svs.epsilon := svs.epsilon + (1 - nu) * eta;
    ELSE
        svs.epsilon := svs.epsilon - eta * nu;
    END IF;
  j := j + 1;
  END LOOP;
	return svs;
END;
/

create or replace
FUNCTION alpine_miner_online_sv_cl(table_name VARCHAR2,ind varchar2,label varchar2,wherecond varchar2array, kernel_type int, degree int, gamma binary_double,eta binary_double, nu binary_double)
RETURN alpine_miner_svm_model AS
  TYPE crt IS REF CURSOR;
	p binary_double;
	svs alpine_miner_svm_model := null;
  sqlstr varchar2(32767);
  c1 crt;
  fa floatArray;
  faa floatArrayArray;
  labelvalue binary_double;
  i int := 0;
  j int := 0;
  wherestr varchar2(32767):= '';

BEGIN
  for i in 1..wherecond.count() loop
  	if(i != 1) then
  		wherestr := wherestr||' and ';
  	end if;
  	wherestr := wherestr||wherecond(i)||' is not null ';
  end loop;
  wherestr := wherestr||' and '||label||' is not null ';
  sqlstr := 'select '||ind||','||label||' from '||table_name||' where '||wherestr;
  svs := alpine_miner_svm_model(0,0,0,0,1,0,0,floatarray(),floatarray());
  open c1 for sqlstr;
  i := 0;
  j := 0;
  LOOP

  FETCH c1 INTO faa, labelvalue;
  EXIT WHEN c1%NOTFOUND;

  fa := alpine_miner_faa2fa(faa);
  IF j = 0 THEN
  svs.ind_dim := fa.count();
  END IF;
	p := labelvalue * (alpine_miner_svs_predict_fa(svs, fa, kernel_type, degree, gamma) + svs.b);
	svs.inds := svs.inds + 1;
	IF p < 0 THEN
	    svs.cum_err := svs.cum_err + 1;
        END IF;

	IF (p <= svs.rho) THEN
	    FOR i IN 1..svs.nsvs LOOP
		  if (abs(svs.weights(i)) < 1.15 * 2.2250738585072014e-308) then
			  svs.weights(i) := 0;
		  else
	    	          svs.weights(i) := svs.weights(i) * (1 - 0.1 * eta);
		  end if;  
            END LOOP;

	    svs.nsvs := svs.nsvs + 1;
        svs.weights.extend();
        svs.weights(svs.nsvs) := labelvalue * eta;
	FOR i IN 1..fa.count() LOOP
          svs.individuals.extend();
          svs.individuals(svs.individuals.count()) := fa(i);
	END LOOP;
	    svs.b := svs.b + eta * labelvalue;
	    svs.rho := svs.rho - eta * (1 - nu);
	ELSE
	    svs.rho := svs.rho +  eta * nu;
    END IF;
  j := j + 1;
  END LOOP;
	return svs;
END;
/

create or replace
FUNCTION alpine_miner_online_sv_nd(table_name VARCHAR2,ind varchar2, wherecond varchar2array, kernel_type int, degree int, gamma binary_double, eta binary_double, nu binary_double)
RETURN alpine_miner_svm_model AS
  TYPE crt IS REF CURSOR;
	p binary_double;
	svs alpine_miner_svm_model := null;
  sqlstr varchar2(32767);
  c1 crt;
  fa floatArray;
  faa floatArrayArray;
  i int := 0;
  j int := 0;
  wherestr varchar2(32767):= '';

BEGIN
  for i in 1..wherecond.count() loop
  	if(i != 1) then
  		wherestr := wherestr||' and ';
  	end if;
  	wherestr := wherestr||wherecond(i)||' is not null ';
  end loop;

  sqlstr := 'select '||ind||' from '||table_name||' where '||wherestr;
  svs := alpine_miner_svm_model(0,0,0,0,0,0,0, floatarray(), floatarray());
  open c1 for sqlstr;
  i := 0;
  j := 0;
  LOOP

  FETCH c1 INTO faa;
  EXIT WHEN c1%NOTFOUND;

  fa := alpine_miner_faa2fa(faa);
  IF j = 0 THEN
  svs.ind_dim := fa.count();
  END IF;
	p := alpine_miner_svs_predict_fa(svs, fa, kernel_type, degree, gamma);
	svs.inds := svs.inds + 1;

	IF (p < svs.rho) THEN
	    FOR i IN 1..svs.nsvs LOOP
		  if (svs.weights(i) < 1.15 * 2.2250738585072014e-308) then
			  svs.weights(i) := 0;
		  else
	    	          svs.weights(i) := svs.weights(i) * (1 - 0.1 * eta);
		  end if; 
            END LOOP;

	    svs.nsvs := svs.nsvs + 1;
	    svs.weights.extend();
        svs.weights(svs.nsvs) := eta;
	FOR i IN 1..fa.count() LOOP
          svs.individuals.extend();
          svs.individuals(svs.individuals.count()) := fa(i);
	END LOOP;
	    svs.rho := svs.rho - eta * (1 - nu);
	ELSE
	    svs.rho := svs.rho + eta * nu;
    END IF;
  j := j + 1;
  END LOOP;
	return svs;
END;
/



