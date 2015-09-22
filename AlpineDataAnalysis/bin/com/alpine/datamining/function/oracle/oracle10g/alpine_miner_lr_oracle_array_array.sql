create or replace
FUNCTION 
alpine_miner_compute_der( column_count integer,column FloatArray, result_data in out FloatArray ,weight_arg binary_double, y integer,add_intercept_arg integer, pi binary_double)
return FloatArray
as
i int := 1;
foo binary_double := 0;
begin
	foo := weight_arg * (y - pi);
	while (i <= column_count) loop
		result_data(i) := (column(i))*foo;
		i := i + 1;
	end loop;
	if (add_intercept_arg = 1) then
		result_data(column_count + 1) := foo;
	end if;
	return result_data;
end;

/
create or replace FUNCTION 
alpine_miner_compute_hessian(beta_count int ,beta FloatArray, column FloatArray, result_data in out FloatArray
		,weight_arg binary_double, add_intercept_arg integer, pi binary_double)
return floatArray
as
	i int := 1;
	j int := 1;
	ind int := 1;
	x binary_double := 0;
	y binary_double := 0;
begin

	while (i <= beta_count) loop
		if ((i = (beta_count)) and add_intercept_arg = 1) then
			x := 1.0;
		else
			x := (column(i));
		end if;
		j := i;
		while(j <= beta_count) loop
			if ((j = (beta_count)) and add_intercept_arg = 1) then
				y := 1.0;
			else
				y := (column(j));
			end if;
			result_data(ind) := (-x*y*weight_arg*pi*(1.0 - pi));
			ind := ind + 1;
			j := j + 1;
		end loop;
		i := i + 1;
	end loop;
	return result_data;
end;
/

create or replace FUNCTION 
alpine_miner_compute_pi(beta FloatArray, column FloatArray, add_intercept_arg integer)
return binary_double
as
	pi binary_double := 0;
	i int := 1;
	column_count int := column.count();
	beta_count int := beta.count();
	gx binary_double := 0;
	tmp binary_double := 0;
begin

	while (i <= beta_count and i <= column_count) loop
		gx := gx + (beta(i)) * (column(i));
		i := i + 1;
	end loop;
	if add_intercept_arg = 1 then
		gx := gx + (beta(beta_count));
	end if;

	if (gx > 30) then
		tmp := 1.0/2.2204460492503131e-16;
	elsif (gx < -30) then
		tmp := 2.2204460492503131e-16;
	else
		tmp := exp(gx);
	end if;
	pi := tmp/(1.0 + tmp);
	return pi;
end;
/
create or replace FUNCTION 
alpine_miner_compute_xwz(column_count int ,column FloatArray, result_data in out FloatArray
		, weight_arg binary_double,  y integer, add_intercept_arg integer, pi binary_double)
return FloatArray
as
i integer := 1;
eta binary_double := 0;
exp_eta binary_double := 0;
mu_eta_dev binary_double := 0;
foo binary_double := 0;
begin

	eta := ln(pi/(1 - pi));
	exp_eta := pi/(1-pi);
	mu_eta_dev := 0;
	if (eta > 30 or eta < -30) then
		mu_eta_dev := 2.2204460492503131e-16;
	else
		mu_eta_dev := exp_eta/((1+exp_eta)*(1+exp_eta));
	end if;

	foo := weight_arg * pi*(1-pi)*(eta+(y - pi)/mu_eta_dev);
	while (i <= column_count) loop
		result_data(i) := (column(i))*foo;
		i := i + 1;
	end loop;
	if (add_intercept_arg = 1) then
		result_data(column_count + 1) := foo;
	end if;
	return result_data;
end;
/
	
	create or replace FUNCTION alpine_miner_lr_ca_beta(beta_arrayarray floatArrayArray,column_arrayarray floatArrayArray,add_intercept integer, weight binary_double, y int, times int) 
RETURN floatArray
as
  i integer := 1;
	fitness binary_double:= 0.0;
	gx binary_double:= 0.0;
	pi binary_double:= 0.0;
  beta FloatArray ;
  column FloatArray ;
  beta_count integer := 0;
  column_count integer := 0;
  result_count integer := 0;
  result_data FloatArray := FloatArray();
  result_data_xwz FloatArray := FloatArray();
begin
	if (beta_arrayarray is null or column_arrayarray is null or add_intercept is null or weight is null or y is null or times is null) then
		return null;
	end if;
  beta := alpine_miner_faa2fa(beta_arrayarray);
  column := alpine_miner_faa2fa(column_arrayarray);
  beta_count := beta.count();
  column_count := column.count();
	result_count := beta_count *(beta_count+1)/2 + beta_count + 1;
  --DBMS_OUTPUT.PUT_LINE(result_count);
  --DBMS_OUTPUT.PUT_LINE(column_count);

	for i in 1..result_count loop
		result_data.extend();
	end loop;
 	for i in 1..column_count loop
		result_data_xwz.extend();
	end loop;
  if add_intercept = 1 then
    result_data_xwz.extend();
  end if;
	if times = 0 then
		pi := (weight * y + 0.5)/(weight + 1);
	else
		pi := alpine_miner_compute_pi(beta,  column, add_intercept);
	end if;
  --DBMS_OUTPUT.PUT_LINE('pi:'||pi);

	result_data := alpine_miner_compute_hessian(beta_count,beta,column, result_data,weight, add_intercept, pi);
	result_data_xwz := alpine_miner_compute_xwz(column_count,column, result_data_xwz ,weight, y, add_intercept, pi);
  --DBMS_OUTPUT.PUT_LINE('result_data:'||result_data(1));
  --DBMS_OUTPUT.PUT_LINE('result_data_xwz:'||result_data_xwz(1));

  for i in 1..column_count loop
    result_data(i+beta_count *(beta_count+1)/2) := result_data_xwz(i);
  end loop;
  if add_intercept = 1 then
    result_data(column_count + 1 +beta_count *(beta_count+1)/2) := result_data_xwz(column_count + 1);
  end if;
	if (y = 1) then
		fitness := ln(pi);
	else
		fitness := ln(1.0 - pi);
	end if;
	fitness := fitness * weight;
	result_data(result_count) := fitness;
	return result_data;
end;
/
	create or replace FUNCTION alpine_miner_lr_ca_derivative(beta floatArray, column floatArray,add_intercept integer, weight binary_double, y binary_double) return FloatArray
as
i integer := 1;
	gx binary_double := 0.0;
	pi binary_double := 0.0;
	result_data FloatArray := FloatArray();
	result_count int := 0;
beta_count integer := beta.count();
column_count integer := column.count();
begin
	if (beta is null or column is null or add_intercept is null or weight is null or y is null) then
		return null;
	end if;
  result_count := beta_count;

	for i in 1..result_count loop
		result_data.extend();
	end loop;
	pi := alpine_miner_compute_pi(beta,  column, add_intercept);

	result_data := alpine_miner_compute_der(column_count,column, result_data ,weight, y, add_intercept,  pi);
    return result_data;
end;
/
	create or replace FUNCTION alpine_miner_lr_ca_fitness(beta_arrayarray floatArrayArray,column_arrayarray FloatArrayArray,add_intercept integer, weight binary_double, label_value int)
RETURN binary_double
as
        i integer := 1;
	fitness binary_double:= 0.0;
	gx binary_double:= 0.0;
	pi binary_double:= 0.0;

	beta FloatArray;
	column FloatArray; 
	beta_count integer := 0;
	column_count integer := 0;
begin
	if (beta_arrayarray is null or column_arrayarray is null or add_intercept is null or weight is null or label_value is null) then
		return null;
	end if;
	
  	beta := alpine_miner_faa2fa(beta_arrayarray);
	column := alpine_miner_faa2fa(column_arrayarray);
	pi := alpine_miner_compute_pi(beta, column,  add_intercept);

	if (label_value = 1) then
		fitness := ln(pi);
	else
		fitness := ln(1.0 - pi);
	end if;
	fitness := fitness * weight;

	return fitness;
end;
/
	create or replace FUNCTION alpine_miner_lr_ca_he(beta_arrayarray floatArrayArray,column_arrayarray FloatArrayArray,add_intercept integer, weight binary_double) return FloatArray
as
i integer := 1;
	gx binary_double := 0.0;
	pi binary_double := 0.0;
	result_count integer := 0;

	beta FloatArray;
	column FloatArray; 
	beta_count integer := 0;
	column_count integer := 0;
	result FloatArray := FloatArray();
begin
	if (beta_arrayarray is null or column_arrayarray is null or add_intercept is null or weight is null) then
		return null;
	end if;

  	beta := alpine_miner_faa2fa(beta_arrayarray);
	column := alpine_miner_faa2fa(column_arrayarray);
	beta_count := beta.count();
	result_count := beta_count *(beta_count+1)/2;
  for i in 1..result_count loop
    result.extend();
  end loop;
	pi := alpine_miner_compute_pi(beta,  column,  add_intercept);
	result := alpine_miner_compute_hessian(beta_count,beta,column, result
		,weight, add_intercept,  pi);
	return result;
end;
/
	create or replace FUNCTION alpine_miner_lr_ca_he_de(beta_arrayarray floatArrayArray,column_arrayarray FloatArrayArray,add_intercept integer, weight binary_double, y int) 
return floatArray
as
  i integer := 1;
	fitness binary_double:= 0.0;
	gx binary_double:= 0.0;
	pi binary_double:= 0.0;
	beta FloatArray;
	column FloatArray;
	beta_count integer := 0;
	column_count integer := 0;
  result_count integer := 0;
  result_data FloatArray := FloatArray();
  result_data_der FloatArray := FloatArray();
begin
	if (beta_arrayarray is null or column_arrayarray is null or add_intercept is null or weight is null or y is null) then
		return null;
	end if;

  	beta := alpine_miner_faa2fa(beta_arrayarray);
	column := alpine_miner_faa2fa(column_arrayarray);
	beta_count  := beta.count();
	column_count := column.count();
	result_count := beta_count *(beta_count+1)/2 + beta_count + 1;
	for i in 1..result_count loop
		result_data.extend();
	end loop;
  for i in 1..column_count loop
		result_data_der.extend();
	end loop;
  if add_intercept = 1 then
    result_data_der.extend();
  end if;
	pi := alpine_miner_compute_pi(beta,  column, add_intercept);
	result_data := alpine_miner_compute_hessian(beta_count,beta,column, result_data
		,weight, add_intercept,  pi);

	result_data_der := alpine_miner_compute_der(column_count,column, result_data_der
		,weight, y, add_intercept, pi);
  for i in 1..column_count loop
    result_data(i+beta_count *(beta_count+1)/2) := result_data_der(i);
  end loop;
  if add_intercept = 1 then
    result_data(column_count + 1 + beta_count*(beta_count+1)/2) := result_data_der(column_count + 1);
  end if;
	if (y = 1) then
		fitness := ln(pi);
	else
		fitness := ln(1.0 - pi);
	end if;
	fitness := fitness *weight;
	result_data(result_count) := (fitness);
	return result_data;
end;
/
	create or replace FUNCTION alpine_miner_lr_ca_pi(beta_arrayarray floatArrayArray,column_arrayarray FloatArrayArray,add_intercept integer)
RETURN binary_double
as
        i integer := 1;
	fitness binary_double:= 0.0;
	gx binary_double:= 0.0;
	pi binary_double:= 0.0;
	beta FloatArray;
	column FloatArray;

begin
	if (beta_arrayarray is null or column_arrayarray is null or add_intercept is null) then
		return null;
	end if;
  	beta := alpine_miner_faa2fa(beta_arrayarray);
	column := alpine_miner_faa2fa(column_arrayarray);
	pi := alpine_miner_compute_pi(beta,  column, add_intercept);
	return pi;
end;
/

