create or replace 
FUNCTION 
alpine_miner_compute_der( column_count integer,column FloatArray ,weight_arg float, y integer,add_intercept_arg integer, pi float)
returns FloatArray
begin
declare i int default 1;
declare foo float default 0; 
declare result_data FloatArray;

	set foo = weight_arg * (y - pi);
	while (i <= column_count) do
		set result_data[i] = (column[i])*foo;
		set i = i + 1;
	end while;
	if (add_intercept_arg = 1) then
		set result_data[column_count + 1] = foo;
	end if;
	return result_data;
end@

create or replace FUNCTION 
alpine_miner_compute_hessian(beta_count int ,beta FloatArray, column FloatArray
		,weight_arg float, add_intercept_arg integer, pi float)
returns floatArray
begin
declare	i int default 1;
declare	j int default 1;
declare	ind int default 1;
declare	x float default 0;
declare	y float default 0;
declare result_data floatarray;


	while (i <= beta_count) do
		if ((i = (beta_count)) and add_intercept_arg = 1) then
			set x = 1.0;
		else
			set x = (column[i]);
		end if;
		set j = i;
		while(j <= beta_count) do
			if ((j = (beta_count)) and add_intercept_arg = 1) then
				set y = 1.0;
			else
				set y = (column[j]);
			end if;
			set result_data[ind] = (-x*y*weight_arg*pi*(1.0 - pi));
			set ind = ind + 1;
			set j = j + 1;
		end while;
		set i = i + 1;
	end while;
	return result_data;
end@
create or replace FUNCTION 
alpine_miner_compute_pi(beta FloatArray, column FloatArray, add_intercept_arg integer)
returns float
begin
declare	pi float default 0;
declare	i int default 1;
declare	column_count int default 0;
declare	beta_count int default 0;
declare	gx float default 0;
declare	tmp float default 0;
	set column_count = CARDINALITY(column);
	set	beta_count = CARDINALITY(beta);


	while (i <= beta_count and i <= column_count) do
		set gx = gx + (beta[i]) * (column[i]);
		set i = i + 1;
	end while;
	if add_intercept_arg = 1 then
		set gx = gx + (beta[beta_count]);
	end if;

	if (gx > 30) then
		set tmp = 1.0/2.2204460492503131e-16;
	elseif (gx < -30) then
		set tmp = 2.2204460492503131e-16;
	else
		set tmp = exp(gx);
	end if;
	set pi = tmp/(1.0 + tmp);
	return pi;
end@
create or replace FUNCTION 
alpine_miner_compute_xwz(column_count int ,column FloatArray
		, weight_arg float,  y integer, add_intercept_arg integer, pi float)
returns FloatArray
begin
declare i integer default 1;
declare eta float default 0;
declare exp_eta float default 0;
declare mu_eta_dev float default 0;
declare foo float default 0;
declare result_data  FloatArray;

	set eta = ln(pi/(1 - pi));
	set exp_eta = pi/(1-pi);
	set mu_eta_dev = 0;
	if (eta > 30 or eta < -30) then
		set mu_eta_dev = 2.2204460492503131e-16;
	else
		set mu_eta_dev = exp_eta/((1+exp_eta)*(1+exp_eta));
	end if;

	set foo = weight_arg * pi*(1-pi)*(eta+(y - pi)/mu_eta_dev);
	while (i <= column_count) do
		set result_data[i] = (column[i])*foo;
		set i = i + 1;
	end while;
	if (add_intercept_arg = 1) then
		set result_data[column_count + 1] = foo;
	end if;
	return result_data;
end@
	
	create or replace FUNCTION alpine_miner_lr_ca_beta(beta floatArray,column floatArray,add_intercept integer, weight float, y int, times int) 
RETURNS floatArray
BEGIN
DECLARE i integer DEFAULT 1;
DECLARE	fitness float DEFAULT 0.0;
DECLARE	gx float DEFAULT 0.0;
DECLARE	pi float DEFAULT 0.0;
DECLARE  beta_count integer DEFAULT 0;
DECLARE  column_count integer DEFAULT 0;
DECLARE  result_count integer DEFAULT 0;
DECLARE  result_data FloatArray ;
DECLARE  result_data_xwz FloatArray ;
	if (beta is null or column is null or add_intercept is null or weight is null or y is null or times is null) then
		return null;
	end if;
  set beta_count = CARDINALITY(beta);
  set column_count = CARDINALITY(column);
	set result_count = beta_count *(beta_count+1)/2 + beta_count + 1;
	if times = 0 then
		set pi = (weight * y + 0.5)/(weight + 1);
	else
		set pi = alpine_miner_compute_pi(beta,  column, add_intercept);
	end if;

	set result_data = alpine_miner_compute_hessian(beta_count,beta,column, weight, add_intercept, pi);
	set result_data_xwz = alpine_miner_compute_xwz(column_count,column, weight, y, add_intercept, pi);
	set i = 1;
  while(i <= column_count) do
    set result_data[i+beta_count *(beta_count+1)/2] = result_data_xwz[i];
	set i = i + 1;
  end while;
  if add_intercept = 1 then
    set result_data[column_count + 1 +beta_count *(beta_count+1)/2] = result_data_xwz[column_count + 1];
  end if;
	if (y = 1) then
		set fitness = ln(pi);
	else
		set fitness = ln(1.0 - pi);
	end if;
	set fitness = fitness * weight;
	set result_data[result_count] = fitness;
	return result_data;
end@
	create or replace FUNCTION alpine_miner_lr_ca_derivative(beta floatArray, column floatArray,add_intercept integer, weight float, y float) returns FloatArray
begin
declare i integer default 1;
declare	gx float default 0.0;
declare	pi float default 0.0;
declare	result_data FloatArray ;
declare	result_count int default 0;
declare beta_count integer default 0;
declare column_count integer default 0;
	set beta_count = CARDINALITY(beta);
	set column_count = CARDINALITY(column);

	if (beta is null or column is null or add_intercept is null or weight is null or y is null) then
		return null;
	end if;
	set result_count = beta_count;

	set pi = alpine_miner_compute_pi(beta,  column, add_intercept);

	set result_data = alpine_miner_compute_der(column_count,column, weight, y, add_intercept,  pi);
    return result_data;
end@
	create or replace FUNCTION alpine_miner_lr_ca_fitness(beta floatArray,column floatArray,add_intercept integer, weight float, label_value int)
RETURNS float
begin
declare        i integer default 1;
declare	fitness float default 0.0;
declare	gx float default 0.0;
declare	pi float default 0.0;

declare	beta_count integer default 0;
declare	column_count integer default 0;

	if (beta is null or column is null or add_intercept is null or weight is null or label_value is null) then
		return null;
	end if;
	
	set pi = alpine_miner_compute_pi(beta, column,  add_intercept);

	if (label_value = 1) then
		set fitness = ln(pi);
	else
		set fitness = ln(1.0 - pi);
	end if;
	set fitness = fitness * weight;

	return fitness;
end@
	create or replace FUNCTION alpine_miner_lr_ca_he(beta floatArray,column floatArray,add_intercept integer, weight float) returns FloatArray
begin
declare i integer default 1;
declare	gx float default 0.0;
declare	pi float default 0.0;
declare	result_count integer default 0;
declare	beta_count integer default 0;
declare	column_count integer default 0;
declare	result FloatArray ;

	if (beta is null or column is null or add_intercept is null or weight is null) then
		return null;
	end if;

	set beta_count = CARDINALITY(beta);
	set result_count = beta_count *(beta_count+1)/2;
	set pi = alpine_miner_compute_pi(beta,  column,  add_intercept);
	set result = alpine_miner_compute_hessian(beta_count,beta,column, weight, add_intercept,  pi);
	return result;
end@
	create or replace FUNCTION alpine_miner_lr_ca_he_de(beta floatArray,column FloatArray,add_intercept integer, weight float, y int) 
returns floatArray
begin
declare  i integer default 1;
declare	fitness float default 0.0;
declare	gx float default 0.0;
declare	pi float default 0.0;
declare	beta_count integer default 0;
declare	column_count integer default 0;
declare  result_count integer default 0;
declare  result_data FloatArray ;
declare  result_data_der FloatArray ;

	if (beta is null or column is null or add_intercept is null or weight is null or y is null) then
		return null;
	end if;

	set beta_count  = CARDINALITY(beta);
	set column_count = CARDINALITY(column);
	set result_count = beta_count *(beta_count+1)/2 + beta_count + 1;
	set pi = alpine_miner_compute_pi(beta,  column, add_intercept);
	set result_data = alpine_miner_compute_hessian(beta_count,beta,column
		,weight, add_intercept,  pi);

	set result_data_der = alpine_miner_compute_der(column_count,column
		,weight, y, add_intercept, pi);
	set i = 1;
  	while ( i <= column_count) do
    	set result_data[i+beta_count *(beta_count+1)/2] = result_data_der[i];
  		set i = i + 1;
  	end while;
  if add_intercept = 1 then
    set result_data[column_count + 1 + beta_count*(beta_count+1)/2] = result_data_der[column_count + 1];
  end if;
	if (y = 1) then
		set fitness = ln(pi);
	else
		set fitness = ln(1.0 - pi);
	end if;
	set fitness = fitness *weight;
	set result_data[result_count] = (fitness);
	return result_data;
end@
	create or replace FUNCTION alpine_miner_lr_ca_pi(beta floatArray,column FloatArray,add_intercept integer)
RETURNS float
begin
declare        i integer default 1;
declare	fitness float default 0.0;
declare	gx float default 0.0;
declare	pi float default 0.0;


	if (beta is null or column is null or add_intercept is null) then
		return null;
	end if;
	set pi = alpine_miner_compute_pi(beta,  column, add_intercept);
	return pi;
end@



create or replace procedure alpine_miner_lr_ca_beta_proc(table_name varchar(32672), where_condition varchar(32672),beta floatArray,column varcharArray,add_intercept integer,  y_name varchar(32672),  times int, out result_data floatarray) 
BEGIN
declare i int default 1;--
declare j int default 1;--
declare y float default 1;--
declare weight float default 1.0;
declare   rowVariable1012 row1012;
DECLARE row_result floatarray;
DECLARE SQL VARCHAR(32672) DEFAULT '';
DECLARE beta_count integer default 0;
DECLARE column_count integer default 0;
DECLARE beta_sql varchar(32672) default '';
declare column_array floatarray;

DECLARE SQLSA VARCHAR(32672);
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;
	if (beta is null or column is null or add_intercept is null or y_name is null or times is null) then
		return ;
	end if;

set i = 1;
set beta_count = CARDINALITY(beta);
set column_count = CARDINALITY(column);
set column[column_count + 1] = y_name;
set sql = alpine_miner_get_select_column_sql(table_name, column, where_condition); -- not null;TODO
PREPARE SQLSA FROM sql ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count + 1);
		set y = column_array[column_count + 1];
		set column_array  = TRIM_ARRAY(column_array, 1);
		set row_result = alpine_miner_lr_ca_beta(beta ,column_array ,add_intercept, weight , y , times);
		if(i = 1) then
			set result_data = row_result;
		else
			set j = 1;
			while( j <= CARDINALITY(row_result)) do
				set result_data[j] = result_data[j] + row_result[j];
				set j = j + 1;
			end while;
		end if;

	END LOOP;
	CLOSE my_cursor ;
end@
create or replace procedure alpine_miner_lr_ca_fitness_proc(table_name varchar(32672), where_condition varchar(32672), beta floatArray,column varcharArray,add_intercept integer, label_value varchar(32672), out fitness float)
begin
declare i int default 1;--
declare j int default 1;--
declare y double default 1;--
declare weight float default 1.0;
declare   rowVariable1012 row1012;
DECLARE SQL VARCHAR(32672) DEFAULT '';
DECLARE beta_count integer default 0;
DECLARE column_count integer default 0;
DECLARE beta_sql varchar(32672) default '';
declare column_array floatarray;
declare row_result float default 0;
DECLARE SQLSA VARCHAR(32672);
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;

	if (beta is null or column is null or add_intercept is null or label_value is null) then
		return ;
	end if;

set i = 1;
set beta_count = CARDINALITY(beta);
set column_count = CARDINALITY(column);
set column[column_count + 1] = label_value;
set sql = alpine_miner_get_select_column_sql(table_name, column, where_condition); -- not null;TODO
PREPARE SQLSA FROM sql ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count + 1);
		set y = column_array[column_count + 1];
		set column_array  = TRIM_ARRAY(column_array, 1);
		set row_result = alpine_miner_lr_ca_fitness(beta ,column_array ,add_intercept, 1.0, y);
		if(i = 1) then
			set fitness = row_result;
		else
			set fitness = fitness + row_result;
		end if;

	END LOOP;
	CLOSE my_cursor ;
end@


create or replace procedure alpine_miner_lr_ca_he_proc(table_name varchar(32672),  where_condition varchar(32672),beta floatArray,column varcharArray,add_intercept integer, out result_data floatarray) 
begin
declare i int default 1;--
declare j int default 1;--
declare weight float default 1.0;
declare   rowVariable1012 row1012;
DECLARE row_result floatarray;
DECLARE SQL VARCHAR(32672) DEFAULT '';
DECLARE beta_count integer default 0;
DECLARE column_count integer default 0;
DECLARE beta_sql varchar(32672) default '';
declare column_array floatarray;

DECLARE SQLSA VARCHAR(32672);
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;

	if (beta is null or column is null or add_intercept is null) then
		return ;
	end if;

set i = 1;
set beta_count = CARDINALITY(beta);
set column_count = CARDINALITY(column);
set sql = alpine_miner_get_select_column_sql(table_name, column, where_condition); -- not null;TODO
PREPARE SQLSA FROM sql ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count);
 		set row_result =  alpine_miner_lr_ca_he(beta ,column_array,add_intercept, 1.0) ;
		if(i = 1) then
			set result_data = row_result;
		else
			set j = 1;
			while( j <= CARDINALITY(row_result)) do
				set result_data[j] = result_data[j] + row_result[j];
				set j = j + 1;
			end while;
		end if;

	END LOOP;
	CLOSE my_cursor ;

end@
create or replace procedure alpine_miner_lr_ca_he_de_proc(table_name varchar(32672),  where_condition varchar(32672),beta floatArray,column varcharArray,add_intercept integer, y_name varchar(32672), out result_data floatarray) 
begin
declare i int default 1;--
declare j int default 1;--
declare y double default 1;--
declare weight float default 1.0;
declare   rowVariable1012 row1012;
DECLARE row_result floatarray;
DECLARE SQL VARCHAR(32672) DEFAULT '';
DECLARE beta_count integer default 0;
DECLARE column_count integer default 0;
DECLARE beta_sql varchar(32672) default '';
declare column_array floatarray;

DECLARE SQLSA VARCHAR(32672);
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;
	if (beta is null or column is null or add_intercept is null or y_name is null) then
		return ;
	end if;

set i = 1;
set beta_count = CARDINALITY(beta);
set column_count = CARDINALITY(column);
set column[column_count + 1] = y_name;
set sql = alpine_miner_get_select_column_sql(table_name, column, where_condition); -- not null;TODO
PREPARE SQLSA FROM sql ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count + 1);
		set y = column_array[column_count + 1];
		set column_array  = TRIM_ARRAY(column_array, 1);
		set row_result = alpine_miner_lr_ca_he_de(beta ,column_array ,add_intercept , 1.0,y);
		if(i = 1) then
			set result_data = row_result;
		else
			set j = 1;
			while( j <= CARDINALITY(row_result)) do
				set result_data[j] = result_data[j] + row_result[j];
				set j = j + 1;
			end while;
		end if;

	END LOOP;
	CLOSE my_cursor ;

end@




