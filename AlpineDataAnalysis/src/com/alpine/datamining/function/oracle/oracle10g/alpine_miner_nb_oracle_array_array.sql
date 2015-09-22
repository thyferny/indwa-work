
create or replace function alpine_miner_nb_ca_pro( 
nominal_null BOOLEAN , 
numerical_null BOOLEAN,
nominal_columns_arg Varchar2Array,
nominal_mapping_count_arg IntegerArray,
nominal_columns_mapping_arg Varchar2Array,
nominal_columns_pro_arg FloatArray,
dependent_column_mapping_arg Varchar2Array,
dependent_column_pro_arg FloatArray,
numerical_columns_arg FloatArray,
numerical_columns_pro_arg FloatArray
)
return FloatArray
as
  i int := 1;
  j int := 1;
  k int := 1;
  nominal_mapping_index int := 0;
  pro_column_offset int := 0;
  pro_index int := 0;
  mapping_index int := 0;
  NUMERICAL_pro_LENGTH integer := 3;
  max_log_pro binary_double := 0.0;
  diff binary_double := 0.0;
  pro_sum binary_double := 0.0;    
  base binary_double := 0;
  pro FloatArray := FloatArray();
begin
	for i  in  1 .. dependent_column_mapping_arg.count() loop
    pro.extend();
		pro(i) := dependent_column_pro_arg(i);
	end loop;
	if not nominal_null
	then
		for i in 1 .. dependent_column_mapping_arg.count() loop
			pro_column_offset := 0;
			pro_index := 0;
			nominal_mapping_index := 0;
			for j in 1..nominal_columns_arg.count() loop
				pro_index := pro_column_offset + (i - 1)* nominal_mapping_count_arg(j); 
				for k in  1.. nominal_mapping_count_arg(j) loop
					if(nominal_columns_arg(j) = nominal_columns_mapping_arg(nominal_mapping_index + k ))
					then
						pro(i) :=  pro(i) + nominal_columns_pro_arg(pro_index + 1);
						EXIT ; -- when (1 == 1);
					end if;
					pro_index := pro_index + 1;
				end loop;
				pro_column_offset := pro_column_offset + dependent_column_mapping_arg.count() * nominal_mapping_count_arg(j);
				nominal_mapping_index := nominal_mapping_index + nominal_mapping_count_arg(j);
			end loop;
		end loop;
	end if;
  	if (not numerical_null) then
		for i in 1..dependent_column_mapping_arg.count() loop
			for j in 1..numerical_columns_arg.count() loop
				pro_index := NUMERICAL_pro_LENGTH * (j - 1) * dependent_column_mapping_arg.count();
				pro_index := pro_index + NUMERICAL_pro_LENGTH * (i - 1) + 1;
				base := (numerical_columns_arg(j) - numerical_columns_pro_arg(pro_index))
					/numerical_columns_pro_arg(pro_index + 1);
				pro(i) := pro(i) - (numerical_columns_pro_arg(pro_index + 2) + 0.5 * base * base);
			end loop;
		end loop;
	end if;
  
	for i in 1..dependent_column_mapping_arg.count() loop
		if (i = 1 or max_log_pro  < pro(i)) then
			max_log_pro := pro(i);
		end if;
	end loop;

	for i in 1..dependent_column_mapping_arg.count() loop
		diff := pro(i) - max_log_pro;
		if diff < -45 then
			pro(i) := 0.0000001;
		else
			pro(i) := exp(diff);
		end if;
	end loop;
	for i in 1..dependent_column_mapping_arg.count() loop
		pro_sum := pro_sum + pro(i);
	end loop;

	for i in 1 .. dependent_column_mapping_arg.count() loop
		pro(i) := pro(i)/pro_sum;
	end loop; 
	return pro;
end;
/

create or replace function alpine_miner_nb_ca_deviance  (
nominal_columns_aa_arg Varchar2ArrayArray,
nominal_mapping_count_aa_arg IntegerArrayArray,
nominal_columns_mapping_aa_arg Varchar2ArrayArray,
nominal_columns_pro_aa_arg FloatArrayArray,
dependent_column_arg varchar2, 
dependent_column_map_aa_arg Varchar2ArrayArray,
dependent_column_pro_aa_arg FloatArrayArray,
numerical_columns_aa_arg FloatArrayArray,
numerical_columns_pro_aa_arg FloatArrayArray

)
return binary_double
as 
	nominal_null BOOLEAN := true;
	numerical_null BOOLEAN := true;
	pro FloatArray := FloatArray();
  i integer := 1;
  deviance BINARY_DOUBLE := 0.0;
  nominal_columns_arg Varchar2Array;
  nominal_mapping_count_arg IntegerArray;
  nominal_columns_mapping_arg Varchar2Array;
  nominal_columns_pro_arg FloatArray;
  dependent_column_mapping_arg Varchar2Array;
  dependent_column_pro_arg FloatArray;
  numerical_columns_arg FloatArray;
  numerical_columns_pro_arg FloatArray;
begin
	if (dependent_column_arg is null or dependent_column_map_aa_arg is null or dependent_column_pro_aa_arg is null)
	then
		return null;
	end if;

	if(nominal_columns_aa_arg is not null and nominal_mapping_count_aa_arg is not null and nominal_columns_mapping_aa_arg is not null and nominal_columns_pro_aa_arg is not null)
	then
		nominal_null := false;
		nominal_columns_arg := alpine_miner_v2aa2v2a(nominal_columns_aa_arg);
		nominal_mapping_count_arg := alpine_miner_iaa2ia(nominal_mapping_count_aa_arg);
		nominal_columns_mapping_arg := alpine_miner_v2aa2v2a(nominal_columns_mapping_aa_arg);
		nominal_columns_pro_arg := alpine_miner_faa2fa(nominal_columns_pro_aa_arg);
	end if;

	if (numerical_columns_aa_arg is not null and numerical_columns_pro_aa_arg is not null)
	then
		numerical_null := false;
		numerical_columns_arg  := alpine_miner_faa2fa(numerical_columns_aa_arg);
		numerical_columns_pro_arg := alpine_miner_faa2fa(numerical_columns_pro_aa_arg);
	end if;

	dependent_column_mapping_arg := alpine_miner_v2aa2v2a(dependent_column_map_aa_arg);
	dependent_column_pro_arg := alpine_miner_faa2fa(dependent_column_pro_aa_arg);

	pro := alpine_miner_nb_ca_pro(nominal_null, numerical_null, 
        nominal_columns_arg ,
        nominal_mapping_count_arg ,
        nominal_columns_mapping_arg ,
        nominal_columns_pro_arg ,
        dependent_column_mapping_arg ,
        dependent_column_pro_arg ,
        numerical_columns_arg ,
        numerical_columns_pro_arg);

	for i in 1..dependent_column_mapping_arg.count() loop
    	if (dependent_column_arg = dependent_column_mapping_arg(i))
   		then
			deviance := -2.0*ln(pro(i));
			exit;
		end if;
	end loop;
	return deviance;

end;
/

create or replace function alpine_miner_nb_ca_confidence(
nominal_columns_aa_arg Varchar2ArrayArray,
nominal_mapping_count_aa_arg IntegerArrayArray,
nominal_columns_mapping_aa_arg Varchar2ArrayArray,
nominal_columns_pro_aa_arg FloatArrayArray,
dependent_column_map_aa_arg Varchar2ArrayArray,
dependent_column_pro_aa_arg FloatArrayArray,
numerical_columns_aa_arg FloatArrayArray,
numerical_columns_pro_aa_arg FloatArrayArray)
return FloatArray
as 
	pro FloatArray := FloatArray() ;
  nominal_null boolean := true;
	numerical_null boolean := true;
	i integer := 1;
	nominal_columns_arg Varchar2Array;
	nominal_mapping_count_arg IntegerArray;
	nominal_columns_mapping_arg Varchar2Array;
	nominal_columns_pro_arg FloatArray;
	dependent_column_mapping_arg Varchar2Array;
	dependent_column_pro_arg FloatArray;
	numerical_columns_arg FloatArray;
	numerical_columns_pro_arg FloatArray;
begin
	if (dependent_column_map_aa_arg is null or dependent_column_pro_aa_arg is null)
	then
		return null;
	end if;
	if(nominal_columns_aa_arg is not null and nominal_mapping_count_aa_arg is not null and nominal_columns_mapping_aa_arg is not null and nominal_columns_pro_aa_arg is not null) then
		nominal_null := false;
		nominal_columns_arg := alpine_miner_v2aa2v2a(nominal_columns_aa_arg);
		nominal_mapping_count_arg := alpine_miner_iaa2ia(nominal_mapping_count_aa_arg);
		nominal_columns_mapping_arg := alpine_miner_v2aa2v2a(nominal_columns_mapping_aa_arg);
		nominal_columns_pro_arg := alpine_miner_faa2fa(nominal_columns_pro_aa_arg);
	end if;
	if (numerical_columns_aa_arg is not null and  numerical_columns_pro_aa_arg is not null) then
		numerical_null := false;
		numerical_columns_arg  := alpine_miner_faa2fa(numerical_columns_aa_arg);
		numerical_columns_pro_arg := alpine_miner_faa2fa(numerical_columns_pro_aa_arg);
	end if;
	dependent_column_mapping_arg := alpine_miner_v2aa2v2a(dependent_column_map_aa_arg);
	dependent_column_pro_arg := alpine_miner_faa2fa(dependent_column_pro_aa_arg);
	pro := alpine_miner_nb_ca_pro(nominal_null, numerical_null,
        nominal_columns_arg ,
        nominal_mapping_count_arg ,
        nominal_columns_mapping_arg ,
        nominal_columns_pro_arg ,
        dependent_column_mapping_arg ,
        dependent_column_pro_arg ,
        numerical_columns_arg ,
        numerical_columns_pro_arg 
	);
	return pro;
end;
/

create or replace function alpine_miner_nb_ca_prediction(
confidence_column_arg FloatArray, 
dependent_column_map_aa_arg Varchar2ArrayArray) 
return varchar2
as 
    max_log_probability binary_double := 0.0;
    j integer := 1;
    dependent_column_mapping_arg Varchar2Array;
begin
	if (confidence_column_arg is null) then
		return null;
	end if;
	if (dependent_column_map_aa_arg is null) then
		return null;
	end if;
	dependent_column_mapping_arg := alpine_miner_v2aa2v2a(dependent_column_map_aa_arg);

	if (confidence_column_arg.count() != dependent_column_mapping_arg.count() or confidence_column_arg.count() <= 0) then
		return null;
	end if;

	for i in 1 .. confidence_column_arg.count() loop
		if (i = 1 or max_log_probability  < confidence_column_arg(i)) then
			max_log_probability := confidence_column_arg(i);
			j := i;
		end if;
	end loop;
	return dependent_column_mapping_arg(j);
end;
/
