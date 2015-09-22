
create or replace function alpine_miner_nb_ca_pro( 
nominal_null BOOLEAN , 
numerical_null BOOLEAN,
nominal_columns_arg varcharArray,
nominal_mapping_count_arg IntegerArray,
nominal_columns_mapping_arg varcharArray,
nominal_columns_pro_arg FloatArray,
dependent_column_mapping_arg varcharArray,
dependent_column_pro_arg FloatArray,
numerical_columns_arg FloatArray,
numerical_columns_pro_arg FloatArray
)
returns FloatArray
begin
declare  i int default 1;
declare  j int default 1;
declare  k int default 1;
declare  nominal_mapping_index int default 0;
declare  pro_column_offset int default 0;
declare  pro_index int default 0;
declare  mapping_index int default 0;
declare  NUMERICAL_pro_LENGTH integer default 3;
declare  max_log_pro float default 0.0;
declare  diff float default 0.0;
declare  pro_sum float default 0.0;    
declare  base float default 0;
declare  pro FloatArray ;
declare leave_loop integer default 0;

	set i = 1;
	while(i <= CARDINALITY(dependent_column_mapping_arg)) do
		set pro[i] = dependent_column_pro_arg[i];
		set i = i + 1;
	end while;
	if  nominal_null != TRUE
	then
		set i = 1;
		while(i <= CARDINALITY(dependent_column_mapping_arg)) do
			set pro_column_offset = 0;
			set pro_index = 0;
			set nominal_mapping_index = 0;
			set j = 1;
			while ( j <= CARDINALITY(nominal_columns_arg)) do
				set pro_index = pro_column_offset + (i - 1)* nominal_mapping_count_arg[j]; 
				set k = 1;
				set leave_loop = 0;
				while( k <= nominal_mapping_count_arg[j] and leave_loop != 1) do
					if(nominal_columns_arg[j] = nominal_columns_mapping_arg[nominal_mapping_index + k])
					then
						set pro[i] =  pro[i] + nominal_columns_pro_arg[pro_index + 1];
						set leave_loop = 1;
					end if;
					set pro_index = pro_index + 1;
					set k = k + 1;
				end while;
				set pro_column_offset = pro_column_offset + CARDINALITY(dependent_column_mapping_arg) * nominal_mapping_count_arg[j];
				set nominal_mapping_index = nominal_mapping_index + nominal_mapping_count_arg[j];
				set j = j + 1;
			end while;
			set i = i + 1;
		end while;
	end if;
  	if (numerical_null != TRUE) then
  		set i = 1;
		while( i <= CARDINALITY(dependent_column_mapping_arg)) do
			set j = 1;
			while(j <= CARDINALITY(numerical_columns_arg)) do
				set pro_index = NUMERICAL_pro_LENGTH * (j - 1) * CARDINALITY(dependent_column_mapping_arg);
				set pro_index = pro_index + NUMERICAL_pro_LENGTH * (i - 1) + 1;
				set base = (numerical_columns_arg[j] - numerical_columns_pro_arg[pro_index])
					/numerical_columns_pro_arg[pro_index + 1];
				set pro[i] = pro[i] - (numerical_columns_pro_arg[pro_index + 2] + 0.5 * base * base);
				set j = j + 1;
			end while;
			set i = i + 1;
		end while;
	end if;

	set i = 1;
	while( i <= CARDINALITY(dependent_column_mapping_arg)) do
		if (i = 1 or max_log_pro  < pro[i]) then
			set max_log_pro = pro[i];
		end if;
		set i = i + 1;
	end while;
	set i = 1;
	while ( i <= CARDINALITY(dependent_column_mapping_arg)) do
		set diff = pro[i] - max_log_pro;
		if diff < -45 then
			set pro[i] = 0.0000001;
		else
			set pro[i] = exp(diff);
		end if;
		set i = i + 1;
	end while;
	set i = 1;
	while( i <= CARDINALITY(dependent_column_mapping_arg)) do
		set pro_sum = pro_sum + pro[i];
		set i = i + 1;
	end while;
	set i = 1;
	while(i <= CARDINALITY(dependent_column_mapping_arg)) do
		set pro[i] = pro[i]/pro_sum;
		set i = i + 1;
	end while; 
	return pro;
end@

create or replace function alpine_miner_nb_ca_deviance  (
nominal_columns_arg varchararray,
nominal_mapping_count_arg Integerarray,
nominal_columns_mapping_arg varchararray,
nominal_columns_pro_arg Floatarray,
dependent_column_arg varchar(32672), 
dependent_column_mapping_arg varchararray,
dependent_column_pro_arg Floatarray,
numerical_columns_arg Floatarray,
numerical_columns_pro_arg Floatarray

)
returns float
begin 
declare	nominal_null BOOLEAN default true;
declare	numerical_null BOOLEAN default true;
declare	pro FloatArray;
declare  i integer default 1;
declare  leave_loop integer default 0;
declare  deviance FLOAT default 0.0;

	if (dependent_column_arg is null or dependent_column_mapping_arg is null or dependent_column_pro_arg is null)
	then
		return null;
	end if;

	if(nominal_columns_arg is not null and cardinality(nominal_columns_arg) >= 1 and nominal_mapping_count_arg is not null and cardinality(nominal_mapping_count_arg) >= 1
		and nominal_columns_mapping_arg is not null and cardinality(nominal_columns_mapping_arg) >= 1 and nominal_columns_pro_arg is not null and cardinality(nominal_columns_pro_arg) >= 1 )
	then
		set nominal_null = false;
	end if;

	if (numerical_columns_arg is not null and cardinality(numerical_columns_arg) >= 1 and numerical_columns_pro_arg is not null and cardinality(numerical_columns_pro_arg) >= 1)
	then
		set numerical_null = false;
	end if;
	set pro = alpine_miner_nb_ca_pro(nominal_null, numerical_null, 
        nominal_columns_arg ,
        nominal_mapping_count_arg ,
        nominal_columns_mapping_arg ,
        nominal_columns_pro_arg ,
        dependent_column_mapping_arg ,
        dependent_column_pro_arg ,
        numerical_columns_arg ,
        numerical_columns_pro_arg);
    set i = 1;
    set leave_loop = 0;
	while( i <= CARDINALITY(dependent_column_mapping_arg) and leave_loop != 1) do
    	if (dependent_column_arg = dependent_column_mapping_arg[i])
   		then
			set deviance = -2.0*ln(pro[i]);
			set leave_loop = 1;
		end if;
		set i = i + 1;
	end while;
	return deviance;

end@


create or replace function alpine_miner_nb_ca_confidence(
nominal_columns_arg varchararray,
nominal_mapping_count_arg Integerarray,
nominal_columns_mapping_arg varchararray,
nominal_columns_pro_arg Floatarray,
dependent_column_mapping_arg varchararray,
dependent_column_pro_arg Floatarray,
numerical_columns_arg Floatarray,
numerical_columns_pro_arg Floatarray)
returns FloatArray
begin 
declare	pro FloatArray;
declare  nominal_null boolean default true;
declare	numerical_null boolean default true;
declare	i integer default 1;
	if (dependent_column_mapping_arg is null or dependent_column_pro_arg is null)
	then
		return null;
	end if;
	if(nominal_columns_arg is not null and cardinality(nominal_columns_arg) >= 1 and nominal_mapping_count_arg is not null and cardinality(nominal_mapping_count_arg) >= 1
		and nominal_columns_mapping_arg is not null and cardinality(nominal_columns_mapping_arg) >= 1 and nominal_columns_pro_arg is not null and cardinality(nominal_columns_pro_arg) >= 1 )
	then
		set nominal_null = false;
	end if;

	if (numerical_columns_arg is not null and cardinality(numerical_columns_arg) >= 1 and numerical_columns_pro_arg is not null and cardinality(numerical_columns_pro_arg) >= 1)
	then
		set numerical_null = false;
	end if;
	set pro = alpine_miner_nb_ca_pro(nominal_null, numerical_null, 
        nominal_columns_arg ,
        nominal_mapping_count_arg ,
        nominal_columns_mapping_arg ,
        nominal_columns_pro_arg ,
        dependent_column_mapping_arg ,
        dependent_column_pro_arg ,
        numerical_columns_arg ,
        numerical_columns_pro_arg);

	return pro;
end@


create or replace procedure alpine_miner_nb_ca_deviance_proc  (
table_name varchar(32672),
where_condition varchar(32672),
nominal_columns_arg varchararray,
nominal_mapping_count_arg Integerarray,
nominal_columns_mapping_arg varchararray,
nominal_columns_pro_arg Floatarray,
dependent_column_arg varchar(32672), 
dependent_column_mapping_arg varchararray,
dependent_column_pro_arg Floatarray,
numerical_columns_arg VARCHARARRAY,
numerical_columns_pro_arg Floatarray,
out deviance float
)
begin
declare i int default 1;--
declare j int default 1;--
declare	nominal_columns varchararray;
declare	dependent_column varchar(32672); 
declare	numerical_columns floatarray;
declare rowVariable1012 nominalrow1012;
DECLARE SQL VARCHAR(32672) DEFAULT '';
DECLARE nominal_columns_count integer default 0;
DECLARE numerical_columns_count integer default 0;
declare column_array varchararray;
declare row_result float default 0;
DECLARE SQLSA VARCHAR(32672);
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;


if (dependent_column_arg is null or dependent_column_mapping_arg is null or dependent_column_pro_arg is null)
then
	return;
end if;
	
set nominal_columns_count = CARDINALITY(nominal_columns_arg);
set numerical_columns_count = CARDINALITY(numerical_columns_arg);
set i = 1;
while( i <= numerical_columns_count) do
	set nominal_columns_arg[nominal_columns_count + i] = numerical_columns_arg[i];
	set i = i + 1;
end while;
set nominal_columns_arg[nominal_columns_count + numerical_columns_count + 1] = dependent_column_arg;
set sql = alpine_miner_get_select_column_sql(table_name, nominal_columns_arg, where_condition); -- not null;TODO
PREPARE SQLSA FROM sql ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_nominalrow_to_array(rowVariable1012, nominal_columns_count + numerical_columns_count + 1);
		set dependent_column = column_array[nominal_columns_count + numerical_columns_count + 1];
		set j = 1;
		while( j <= numerical_columns_count) do
			set numerical_columns[j] = cast(column_array[nominal_columns_count + j] as double);
			set j = j + 1;
		end while;
		set nominal_columns = TRIM_ARRAY(column_array, numerical_columns_count + 1);
		set row_result = 
		alpine_miner_nb_ca_deviance  (
		nominal_columns,
		nominal_mapping_count_arg,
		nominal_columns_mapping_arg,
		nominal_columns_pro_arg,
		dependent_column, 
		dependent_column_mapping_arg,
		dependent_column_pro_arg,
		numerical_columns,
		numerical_columns_pro_arg
		);
		if(i = 1) then
			set deviance = row_result;
		else
			set deviance = deviance + row_result;
		end if;

	END LOOP;
	CLOSE my_cursor ;
end@


create or replace procedure alpine_miner_nb_ca_prediction_proc  (
table_name varchar(32672),
where_condition varchar(32672),
nominal_columns_arg varchararray,
nominal_mapping_count_arg Integerarray,
nominal_columns_mapping_arg varchararray,
nominal_columns_pro_arg Floatarray,
dependent_column_mapping_arg varchararray,
dependent_column_pro_arg Floatarray,
numerical_columns_arg VARCHARARRAY,
numerical_columns_pro_arg Floatarray,
result_table_arg varchar(32672)
)
begin
declare i int default 1;--
declare j int default 1;--
declare	nominal_columns varchararray;
declare	id bigint; 
declare sqlstr VARCHAR(32672) DEFAULT '';
declare	numerical_columns floatarray;
declare rowVariable1012 nominalrow1012;
DECLARE SQL VARCHAR(32672) DEFAULT '';
DECLARE nominal_columns_count integer default 0;
DECLARE numerical_columns_count integer default 0;
declare column_array varchararray;
declare confidence floatarray;
DECLARE SQLSA VARCHAR(32672);
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;


if (dependent_column_mapping_arg is null or dependent_column_pro_arg is null)
then
	return;
end if;
	
set nominal_columns_count = CARDINALITY(nominal_columns_arg);
set numerical_columns_count = CARDINALITY(numerical_columns_arg);
set i = 1;
while( i <= numerical_columns_count) do
	set nominal_columns_arg[nominal_columns_count + i] = numerical_columns_arg[i];
	set i = i + 1;
end while;
set nominal_columns_arg[nominal_columns_count + numerical_columns_count + 1] = 'alpine_miner_id';
set sql = alpine_miner_get_select_column_sql(table_name, nominal_columns_arg, where_condition); -- not null;TODO
PREPARE SQLSA FROM sql ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_nominalrow_to_array(rowVariable1012, nominal_columns_count + numerical_columns_count + 1);
		set id = cast(column_array[nominal_columns_count + numerical_columns_count + 1] as bigint);
		set j = 1;
		while( j <= numerical_columns_count) do
			set numerical_columns[j] = cast(column_array[nominal_columns_count + j] as double);
			set j = j + 1;
		end while;
		set nominal_columns = TRIM_ARRAY(column_array, numerical_columns_count + 1);
		set confidence = 
		alpine_miner_nb_ca_confidence(
		nominal_columns,
		nominal_mapping_count_arg,
		nominal_columns_mapping_arg,
		nominal_columns_pro_arg,
		dependent_column_mapping_arg,
		dependent_column_pro_arg,
		numerical_columns,
		numerical_columns_pro_arg
		);
		set sqlstr = ' insert into '||result_table_arg||' values ( '||id;
		set j = 1;
		while(j <= cardinality(confidence)) do
			set sqlstr = sqlstr ||','|| confidence[j];
			set j = j + 1;
		end while;
		set sqlstr = sqlstr ||')';
		execute immediate  sqlstr ;
	END LOOP;
	CLOSE my_cursor ;
end@


