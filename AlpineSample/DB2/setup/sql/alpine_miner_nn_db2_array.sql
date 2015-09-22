create or replace FUNCTION alpine_miner_nn_ca_output(  
weight_arg FloatArray,  
columns_arg FloatArray,  
input_range_arg FloatArray,  
input_base_arg FloatArray,  
hidden_node_number_arg IntegerArray,  
hidden_layer_number_arg integer,  
output_range_arg float,  
output_base_arg float,  
output_node_no_arg integer,  
normalize_arg integer ,  
numerical_label_arg integer,	
inout all_hidden_node_count integer,	
inout input FloatArray,  
inout output FloatArray,	
inout hidden_node_output FloatArray,  
inout columns_count integer,  
inout hidden_node_number_count integer,  
inout weight_count integer
)
RETURNS FloatArray
BEGIN 
declare  i integer default 1;
declare	j integer default 1;
declare	k integer default 1;
declare 	weight_index integer default 0;
declare	hidden_node_number_index integer default 0;
declare  result_count  integer default 0;
declare  result_data FloatArray ;
	if (weight_arg is null or columns_arg is null or input_range_arg is null or input_base_arg is null or hidden_node_number_arg is null or hidden_layer_number_arg is null or output_range_arg is null or output_base_arg is null or output_node_no_arg is null or normalize_arg is null or numerical_label_arg is null)
	then
		return null;
	end if;
  set columns_count = cardinality(columns_arg);
  set hidden_node_number_count = cardinality(hidden_node_number_arg);
  set weight_count  = cardinality(weight_arg);
  set i = 1;
	set all_hidden_node_count = 0;
	while i <= hidden_layer_number_arg do
		set all_hidden_node_count = all_hidden_node_count + hidden_node_number_arg[i];
		set i = i + 1;
	end while;

	set i = 1;
	set result_count = output_node_no_arg;
  if (normalize_arg = 1)  then
       set i = 1;
       while  i <= columns_count do
          if ((input_range_arg[i]) != 0) then
              set input[i] = (columns_arg[i]-input_base_arg[i])/input_range_arg[i];
          else
              set input[i] = columns_arg[i]-input_base_arg[i];
          end if;
          set i = i + 1;
        end while;
  else
      set i = 1;
    	while  i <= columns_count do
         	    set input[i] = columns_arg[i];
         	    set i = i + 1;
    	end while;
	end if;

  set i = 1;
  while  i <= hidden_node_number_arg[1]  do
          set hidden_node_output[i] = weight_arg[1+(i-1)*(columns_count + 1)];
          set j = 1;
          while  j <= columns_count do
                  set hidden_node_output[i] = hidden_node_output[i]+input[j]*weight_arg[1 + j  + (i-1) *(columns_count + 1)];
                  set j = j + 1;
          end while;

          if (hidden_node_output[i] < -45.0) then
            set hidden_node_output[i] = 0;
          elseif (hidden_node_output[i] > 45.0) then
            set hidden_node_output[i] = 1;
          else
            set hidden_node_output[i] = (1.0/(1.0+exp( -1.0 * hidden_node_output[i])));
          end if;
          set i = i + 1;
  end while;
  set weight_index = hidden_node_number_arg[1] * (columns_count + 1) ;

  if (hidden_layer_number_arg > 1) then
    set hidden_node_number_index = 0;
    set i = 2;
    while  i <= hidden_layer_number_arg  do
            set hidden_node_number_index = hidden_node_number_index + hidden_node_number_arg[i - 1];
            set j = 1;
            while  j <= hidden_node_number_arg[i]  do
                    set hidden_node_output[hidden_node_number_index + j] = weight_arg[weight_index + 1 + (hidden_node_number_arg[i - 1] +1) * (j-1)];
                    set k = 1;
                    while  k <= hidden_node_number_arg[i - 1]  do
                           set  hidden_node_output[hidden_node_number_index + j] = hidden_node_output[hidden_node_number_index + j]+hidden_node_output[hidden_node_number_index - hidden_node_number_arg[i - 1] + k]*weight_arg[weight_index + (hidden_node_number_arg[i - 1] +1) * (j-1) + k + 1];
                            set k = k + 1;
                    end while;
                    if (hidden_node_output[hidden_node_number_index + j] < -45.0) then
                      set hidden_node_output[hidden_node_number_index + j] = 0;
                    elseif (hidden_node_output[hidden_node_number_index + j] > 45.0) then
                      set hidden_node_output[hidden_node_number_index + j] = 1;
                    else
                      set hidden_node_output[hidden_node_number_index + j] = (1.0/(1+exp(-1.0*hidden_node_output[hidden_node_number_index+j])));
                    end if;
                    set j = j + 1;
            end while;
            set weight_index = weight_index + hidden_node_number_arg[i] * (hidden_node_number_arg[i - 1] + 1);
            set i = i + 1;
   end while;
  end if;

  set i = 1;
  while  i <= output_node_no_arg do
          set output[i] = weight_arg[weight_index + 1 + (hidden_node_number_arg[hidden_layer_number_arg]+1) * (i - 1)];
          set j = 1;
          while  j <= hidden_node_number_arg[hidden_layer_number_arg]  do
                  set output[i] = output[i]+hidden_node_output[hidden_node_number_index + j] * weight_arg[1 + j + weight_index  + (hidden_node_number_arg[hidden_layer_number_arg]+1) * (i - 1) ];
                  set j = j + 1;
          end while;
          if (numerical_label_arg = 1) then
                        set output[i] = (output[i] * output_range_arg+output_base_arg);
          else
            if (output[i] < -45.0) then
              set output[i] = 0;
            elseif (output[i] > 45.0) then
              set output[i] = 1;
            else
              set output[i] = (1.0/(1+exp(-1.0*output[i])));
            end if;
          end if;
          set i = i + 1;
  end while;
	set i = 1;
	while i <= output_node_no_arg do
		set result_data[i] = output[i];
		set i = i + 1;
	end while;

  return result_data;
end@

create or replace FUNCTION alpine_miner_nn_ca_o(
weight_arg FloatArray,  
columns_arg FloatArray,  
input_range_arg FloatArray,  
input_base_arg FloatArray, 
hidden_node_number_arg IntegerArray, 
hidden_layer_number_arg integer,  
output_range_arg float, 
output_base_arg float, 
output_node_no_arg integer, 
normalize_arg integer , 
numerical_label_arg integer)
RETURNS floatArray 
BEGIN
declare	all_hidden_node_count integer default 0;
declare	input FloatArray ;
declare  output FloatArray ;
declare	hidden_node_output FloatArray;
declare  columns_count integer default 0;
declare  hidden_node_number_count integer default 0;
declare  weight_count  integer default 0;
declare  result_data FloatArray;
	set  hidden_node_number_count = cardinality(hidden_node_number_arg);

  	set columns_count = cardinality(columns_arg);
  	set weight_count = cardinality(weight_arg);

  set result_data = alpine_miner_nn_ca_output(  weight_arg ,  columns_arg ,  input_range_arg ,  input_base_arg ,  hidden_node_number_arg ,  hidden_layer_number_arg ,  output_range_arg ,  output_base_arg ,  output_node_no_arg ,  normalize_arg  ,  numerical_label_arg ,	all_hidden_node_count,	input ,  output ,	hidden_node_output,  columns_count,  hidden_node_number_count,  weight_count);
  return result_data;
end@

create or replace FUNCTION alpine_miner_nn_ca_change(  
weight_arg FloatArray,  
columns_arg FloatArray,  
input_range_arg FloatArray,  
input_base_arg FloatArray,  
hidden_node_number_arg IntegerArray,  
hidden_layer_number_arg integer,  
output_range_arg float,  
output_base_arg float,  
output_node_no_arg integer,  
normalize_arg integer ,  
numerical_label_arg integer,  
label_arg float,  
set_size_arg float)
RETURNS	 FloatArray
BEGIN 
declare  i integer  default  1;
declare	j integer  default  1;
declare	k integer  default  1;
declare	all_hidden_node_count integer  default  0;

declare	weight_index integer  default  0;
declare	hidden_node_number_index integer  default  0;
declare	input FloatArray;
declare  output FloatArray;
declare  temp_output FloatArray;
declare	hidden_node_output FloatArray;
declare  columns_count integer default  0;
declare  hidden_node_number_count integer default  0;
declare  weight_count  integer default  0;
declare  result_count  integer default  0;
declare  result_data FloatArray;
declare  set_size float  default  0;
declare  error_sum float  default  0.0;
declare	delta float  default  0.0;
declare	total_error float  default  0.0;
declare	output_error FloatArray ;
declare	direct_output_error float  default  0.0;

declare	hidden_node_error FloatArray;
declare	current_change float default  0.0;
declare	threshold_change float default  0.0;

	if (weight_arg is null or columns_arg is null or input_range_arg is null or input_base_arg is null or hidden_node_number_arg is null or hidden_layer_number_arg is null or output_range_arg is null or output_base_arg is null or output_node_no_arg is null or normalize_arg is null or numerical_label_arg is null or label_arg is null or set_size_arg is null)
	then
		return null;
	end if;
    set hidden_node_number_count = cardinality(hidden_node_number_arg);
  	set columns_count = cardinality(columns_arg);
  	set weight_count = cardinality(weight_arg);

  set hidden_node_number_count = cardinality(hidden_node_number_arg);
  set weight_count  = cardinality(weight_arg);
  set i = 1;
	set all_hidden_node_count = 0;
	while i <= hidden_layer_number_arg do
		set all_hidden_node_count = all_hidden_node_count + hidden_node_number_arg[i];
		set i = i + 1;
	end while;

	set i = 1;
	set result_count = output_node_no_arg;
  if (normalize_arg = 1)  then
       set i = 1;
       while  i <= columns_count do
          if ((input_range_arg[i]) != 0) then
              set input[i] = (columns_arg[i]-input_base_arg[i])/input_range_arg[i];
          else
              set input[i] = columns_arg[i]-input_base_arg[i];
          end if;
          set i = i + 1;
        end while;
  else
      set i = 1;
    	while  i <= columns_count do
         	    set input[i] = columns_arg[i];
         	    set i = i + 1;
    	end while;
	end if;

  set i = 1;
  while  i <= hidden_node_number_arg[1]  do
          set hidden_node_output[i] = weight_arg[1+(i-1)*(columns_count + 1)];
          set j = 1;
          while  j <= columns_count do
                  set hidden_node_output[i] = hidden_node_output[i]+input[j]*weight_arg[1 + j  + (i-1) *(columns_count + 1)];
                  set j = j + 1;
          end while;

          if (hidden_node_output[i] < -45.0) then
            set hidden_node_output[i] = 0;
          elseif (hidden_node_output[i] > 45.0) then
            set hidden_node_output[i] = 1;
          else
            set hidden_node_output[i] = (1.0/(1.0+exp( -1.0 * hidden_node_output[i])));
          end if;
          set i = i + 1;
  end while;
  set weight_index = hidden_node_number_arg[1] * (columns_count + 1) ;

  if (hidden_layer_number_arg > 1) then
    set hidden_node_number_index = 0;
    set i = 2;
    while  i <= hidden_layer_number_arg  do
            set hidden_node_number_index = hidden_node_number_index + hidden_node_number_arg[i - 1];
            set j = 1;
            while  j <= hidden_node_number_arg[i]  do
                    set hidden_node_output[hidden_node_number_index + j] = weight_arg[weight_index + 1 + (hidden_node_number_arg[i - 1] +1) * (j-1)];
                    set k = 1;
                    while  k <= hidden_node_number_arg[i - 1]  do
                           set  hidden_node_output[hidden_node_number_index + j] = hidden_node_output[hidden_node_number_index + j]+hidden_node_output[hidden_node_number_index - hidden_node_number_arg[i - 1] + k]*weight_arg[weight_index + (hidden_node_number_arg[i - 1] +1) * (j-1) + k + 1];
                            set k = k + 1;
                    end while;
                    if (hidden_node_output[hidden_node_number_index + j] < -45.0) then
                      set hidden_node_output[hidden_node_number_index + j] = 0;
                    elseif (hidden_node_output[hidden_node_number_index + j] > 45.0) then
                      set hidden_node_output[hidden_node_number_index + j] = 1;
                    else
                      set hidden_node_output[hidden_node_number_index + j] = (1.0/(1+exp(-1.0*hidden_node_output[hidden_node_number_index+j])));
                    end if;
                    set j = j + 1;
            end while;
            set weight_index = weight_index + hidden_node_number_arg[i] * (hidden_node_number_arg[i - 1] + 1);
            set i = i + 1;
   end while;
  end if;

  set i = 1;
  while  i <= output_node_no_arg do
          set output[i] = weight_arg[weight_index + 1 + (hidden_node_number_arg[hidden_layer_number_arg]+1) * (i - 1)];
          set j = 1;
          while  j <= hidden_node_number_arg[hidden_layer_number_arg]  do
                  set output[i] = output[i]+hidden_node_output[hidden_node_number_index + j] * weight_arg[1 + j + weight_index  + (hidden_node_number_arg[hidden_layer_number_arg]+1) * (i - 1) ];
                  set j = j + 1;
          end while;
          if (numerical_label_arg = 1) then
                        set output[i] = (output[i] * output_range_arg+output_base_arg);
          else
            if (output[i] < -45.0) then
              set output[i] = 0;
            elseif (output[i] > 45.0) then
              set output[i] = 1;
            else
              set output[i] = (1.0/(1+exp(-1.0*output[i])));
            end if;
          end if;
          set i = i + 1;
  end while;

	if (set_size_arg < 1) then
		set set_size = 1;
    else
        set set_size = set_size_arg;
	end if;

	set result_count = weight_count + 1;

	set i = 1;
	while i <= output_node_no_arg  do
		if(numerical_label_arg = 1) then
			if (output_range_arg = 0.0) then
				set direct_output_error = 0.0;
			else
				set direct_output_error = (label_arg - output[i])/output_range_arg;
			end if;
			set output_error[i] = direct_output_error;
		else
			if ((label_arg) = (i - 1)) then
				set direct_output_error = 1.0 - output[i];
			else
				set direct_output_error = 0.0 - output[i];
			end if;
			set output_error[i] = direct_output_error * output[i] * (1- output[i]);
		end if;
		set total_error = total_error + direct_output_error*direct_output_error;
		set i = i + 1;
	end while;

	set weight_index = weight_count - output_node_no_arg*(hidden_node_number_arg[hidden_layer_number_arg] + 1)  ;
	set hidden_node_number_index = all_hidden_node_count - hidden_node_number_arg[hidden_layer_number_arg];
	set i = 1;
	
	while i <= hidden_node_number_arg[hidden_layer_number_arg]  do
		set error_sum = 0.0;
		set k = 1;
		while k <= output_node_no_arg  do
			set error_sum = error_sum+output_error[k]*weight_arg[weight_index + (hidden_node_number_arg[hidden_layer_number_arg] + 1)*(k - 1) + i + 1];
			set k = k + 1;
			
		end while;
		set hidden_node_error[hidden_node_number_index + i] = error_sum*hidden_node_output[hidden_node_number_index + i]*(1.0-hidden_node_output[hidden_node_number_index + i]);
		set i = i + 1;
	end while;

	if (hidden_layer_number_arg > 1) then
		set weight_index = weight_index - (hidden_node_number_arg[hidden_layer_number_arg - 1] + 1)*hidden_node_number_arg[hidden_layer_number_arg];
		set hidden_node_number_index = hidden_node_number_index - hidden_node_number_arg[hidden_layer_number_arg - 1];
		set i = hidden_layer_number_arg - 1;
		while i >= 1  do
			set j = 1;
			while j <= hidden_node_number_arg[i]  do
				set error_sum = 0.0;
				set k = 1;
				while k <= hidden_node_number_arg[i + 1]  do
					set error_sum = error_sum+hidden_node_error[hidden_node_number_index + hidden_node_number_arg[i] + k]*weight_arg[weight_index + (hidden_node_number_arg[i]+1)*(k - 1) + j + 1];
					set k = k + 1;
				end while;
				set hidden_node_error[hidden_node_number_index + j] = error_sum*hidden_node_output[hidden_node_number_index + j]*(1-hidden_node_output[hidden_node_number_index + j]);
				set j = j + 1;
			end while;
      if (i != 1) then
        set weight_index = weight_index - (hidden_node_number_arg[i - 1]+1) * hidden_node_number_arg[i];
        set hidden_node_number_index = hidden_node_number_index - hidden_node_number_arg[i - 1];
      else
        set weight_index = weight_index - (columns_count+1) * hidden_node_number_arg[i];
        set hidden_node_number_index = 0;
      end if;
      set i = i - 1;
		end while;
	end if;
	set weight_index = weight_count - (hidden_node_number_arg[hidden_layer_number_arg]+ 1)* output_node_no_arg;
	set hidden_node_number_index = all_hidden_node_count - hidden_node_number_arg[hidden_layer_number_arg];

	set i = 1;
	while i <= output_node_no_arg  do
		set delta = 1.0/set_size*output_error[i];
		set threshold_change = delta;
		set result_data[weight_index + 1 +(hidden_node_number_arg[hidden_layer_number_arg]+ 1)*(i - 1)] = (threshold_change);

		set j = 1;
		while j <= hidden_node_number_arg[hidden_layer_number_arg]  do
			set current_change = delta * hidden_node_output[hidden_node_number_index + j];
			set result_data[weight_index +(hidden_node_number_arg[hidden_layer_number_arg]+ 1)*(i - 1) + 1 +j] = (current_change);

			set j = j + 1;
		end while;
		set i = i + 1;
	end while;
	if (hidden_layer_number_arg > 1) then
		set i = hidden_layer_number_arg;
		while i >= 2   do
			set weight_index = weight_index - (hidden_node_number_arg[i - 1]+1)*hidden_node_number_arg[i];
			set hidden_node_number_index = hidden_node_number_index - hidden_node_number_arg[i - 1];
			set delta = 0.0;
			set j = 1;
			while j <= hidden_node_number_arg[i]  do
				set delta = (1.0/set_size*hidden_node_error[hidden_node_number_index + hidden_node_number_arg[i - 1] + j]);
				set threshold_change = delta;
				set result_data[weight_index + 1+ (hidden_node_number_arg[i - 1] + 1) * (j - 1)] = (threshold_change);
				set k = 1;
				while k <= hidden_node_number_arg[i - 1] do
					set current_change = delta * hidden_node_output[hidden_node_number_index + k];
					set result_data[weight_index + (hidden_node_number_arg[i - 1] + 1) * (j - 1) + 1 +  k] = (current_change);
					set k = k + 1;
				end while;
				set j = j + 1;
			end while;
			set i = i - 1;
		end while;
	end if;
	set weight_index = 0; 
	set hidden_node_number_index = 0;
	set delta = 0.0;
	set j = 1;
	while j <= hidden_node_number_arg[1] do
		set delta = 1.0/set_size*hidden_node_error[hidden_node_number_index + j];
		set threshold_change = delta;
		set result_data[weight_index + 1 + (columns_count+1)*(j - 1)] = threshold_change;
		set k = 1;
		while k <= columns_count do
			set current_change = delta*input[k];
			set result_data[weight_index +  (columns_count+1)*(j - 1) + k + 1] = (current_change);
			set k = k + 1;
		end while;
		set j = j + 1;
	end while;
	set result_data[weight_count + 1] = (total_error);
  return (result_data);
end@



create or replace procedure alpine_miner_nn_ca_change_proc(
table_name VARCHAR(32672), 
label VARCHAR(32672), 
wherestr VARCHAR(32672), 

weight_arg FloatArray,  
columns_arg varcharArray,  
input_range_arg FloatArray,  
input_base_arg FloatArray,  
hidden_node_number_arg IntegerArray,  
hidden_layer_number_arg integer,  
output_range_arg float,  
output_base_arg float,  
output_node_no_arg integer,  
normalize_arg integer ,  
numerical_label_arg integer,  
--label_arg float,
set_size_arg float,
out result floatarray)

begin
    
    DECLARE sqlstr VARCHAR(32672);
    
    DECLARE fa floatArray;
    DECLARE labelvalue FLOAT;
    DECLARE cap FLOAT;
    DECLARE i int DEFAULT 0;
    DECLARE j int DEFAULT 0;
    DECLARE i1 INTEGER;
    DECLARE i2 INTEGER;
    DECLARE RETURN_VAL floatarray;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	
	set column_count = CARDINALITY(columns_arg);
	set columns_arg[column_count + 1] = label;
	set sql = alpine_miner_get_select_column_sql(table_name, columns_arg, wherestr);
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
		set labelvalue = column_array[column_count + 1];
		set fa  = TRIM_ARRAY(column_array, 1);
        set RETURN_VAL = alpine_miner_nn_ca_change(  
				weight_arg,  
				fa,  
				input_range_arg ,  
				input_base_arg,  
				hidden_node_number_arg,  
				hidden_layer_number_arg,  
				output_range_arg,  
				output_base_arg,  
				output_node_no_arg,  
				normalize_arg,  
				numerical_label_arg,  
				labelvalue,  
				set_size_arg); 
		if ( i = 1) then 
			set result = RETURN_VAL;
		else
			set j = 1;
			while(j <= cardinality(result)) do
				set result[j] = result[j] + RETURN_VAL[j];
				set j = j + 1;
			end while;
		end if;
	END LOOP;
	CLOSE my_cursor ;
end@

create or replace procedure alpine_miner_nn_ca_deviance_proc(
table_name VARCHAR(32672), 
label VARCHAR(32672), 
wherestr VARCHAR(32672), 
weight_arg FloatArray,  
columns_arg varcharArray,  
input_range_arg FloatArray,  
input_base_arg FloatArray,  
hidden_node_number_arg IntegerArray,  
hidden_layer_number_arg integer,  
output_range_arg float,  
output_base_arg float,  
output_node_no_arg integer,  
normalize_arg integer ,  
numerical_label_arg integer,  
dependent_column_mapping_arg VARCHARARRAY,
out result float)
begin
    DECLARE sqlstr VARCHAR(32672);
    DECLARE leave_loop INTEGER DEFAULT 0;
    DECLARE fa floatArray;
    DECLARE labelvalue FLOAT;
    DECLARE deviance FLOAT;
    declare dependent_column varchar(32672);
	declare numerical_columns floatarray;
    DECLARE cap FLOAT;
    DECLARE i int DEFAULT 0;
    DECLARE j int DEFAULT 0;
    DECLARE i1 INTEGER;
    DECLARE i2 INTEGER;
    DECLARE RETURN_VAL floatarray;
	declare   rowVariable1012 nominalrow1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array varchararray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	
	set i = 1;
	set column_count = CARDINALITY(columns_arg);
	set columns_arg[column_count + 1] = label;
	set sql = alpine_miner_get_select_column_sql(table_name, columns_arg, wherestr);
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
    	
		set column_array = alpine_miner_nominalrow_to_array(rowVariable1012, column_count + 1);
		set dependent_column = column_array[column_count + 1];
		set j = 1;
		while( j <= column_count) do
			set fa[j] = cast(column_array[j] as double);
			set j = j + 1;
		end while;

        set RETURN_VAL = alpine_miner_nn_ca_o(  
				weight_arg,  
				fa,  
				input_range_arg ,  
				input_base_arg,  
				hidden_node_number_arg,  
				hidden_layer_number_arg,  
				output_range_arg,  
				output_base_arg,  
				output_node_no_arg,  
				normalize_arg,  
				numerical_label_arg
				); 
		set j = 1;
	    set leave_loop = 0;
		while( j <= CARDINALITY(dependent_column_mapping_arg) and leave_loop != 1) do

	    	if (dependent_column = dependent_column_mapping_arg[j])
	    	
	   		then
				set deviance = -2.0*ln(RETURN_VAL[j]);
				set leave_loop = 1;
			end if;
			set j = j + 1;
		end while;
		if ( i = 1) then 
			set result = deviance;
		else
			set result = result + deviance;
		end if;
	END LOOP;
	CLOSE my_cursor ;
end@

create or replace procedure alpine_miner_nn_ca_r_square_proc(
table_name VARCHAR(32672), 
dependent_column_arg varchar(32672),
wherestr VARCHAR(32672), 
weight_arg FloatArray,  
columns_arg varcharArray,  
input_range_arg FloatArray,  
input_base_arg FloatArray,  
hidden_node_number_arg IntegerArray,  
hidden_layer_number_arg integer,  
output_range_arg float,  
output_base_arg float,  
output_node_no_arg integer,  
normalize_arg integer ,  
numerical_label_arg integer,
--dependent_column_arg varchar(32672),
dependent_column_avg_arg float,
out result float)

begin
    declare SSerror float;
    declare SStotal float;
    DECLARE sqlstr VARCHAR(32672);
    
    DECLARE fa floatArray;
    DECLARE labelvalue FLOAT;
    DECLARE cap FLOAT;
    DECLARE i int DEFAULT 0.0;
    DECLARE i1 INTEGER;
    DECLARE i2 INTEGER;
    DECLARE RETURN_VAL floatarray;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	
	set i = 1;
	set column_count = CARDINALITY(columns_arg);
	set columns_arg[column_count + 1] = dependent_column_arg;
	set sql = alpine_miner_get_select_column_sql(table_name, columns_arg, wherestr);
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;
--		// Rsquare = 1-SSerror/SStotal = 1 - sum(Yi - Yihat)/sum(Yi - Ybar)

fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;

		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count + 1);

		set labelvalue = column_array[column_count + 1];
		set fa  = TRIM_ARRAY(column_array, 1);

        set RETURN_VAL = alpine_miner_nn_ca_o(  
				weight_arg,  
				fa,  
				input_range_arg ,  
				input_base_arg,  
				hidden_node_number_arg,  
				hidden_layer_number_arg,  
				output_range_arg,  
				output_base_arg,  
				output_node_no_arg,  
				normalize_arg,  
				numerical_label_arg
				); 

		if ( i = 1) then 
			set SSerror = (RETURN_VAL[1] - labelvalue)*(RETURN_VAL[1] - labelvalue);
			set SStotal = (dependent_column_avg_arg - labelvalue)*(dependent_column_avg_arg - labelvalue);
		else
			set SSerror = SSerror + (RETURN_VAL[1] - labelvalue)*(RETURN_VAL[1] - labelvalue);
			set SStotal = SStotal + (dependent_column_avg_arg - labelvalue)*(dependent_column_avg_arg - labelvalue);
		end if;

	END LOOP;
	if SStotal <> 0 then
		set result = 1.0 - SSerror/SStotal;
	else
		set result = 0;
	end if;
	CLOSE my_cursor ;
end@

create or replace procedure alpine_miner_nn_ca_predict_proc(-- alpine_miner_nn_ca_o_proc

table_name VARCHAR(32672), 
temp_table_name varchar(32672),                                             
wherestr VARCHAR(32672), 
weight_arg FloatArray,
columns_arg varchararray,
input_range_arg FloatArray,  
input_base_arg FloatArray,  
hidden_node_number_arg IntegerArray,  
hidden_layer_number_arg integer,  
output_range_arg float,  
output_base_arg float,  
output_node_no_arg integer,  
normalize_arg integer,
numerical_label_arg integer
)
begin
    DECLARE p FLOAT;
    DECLARE id bigint;
    DECLARE sqlstr VARCHAR(32672);
    DECLARE fa floatArray;
    DECLARE i int DEFAULT 0;
    DECLARE j INTEGER DEFAULT 0;
    DECLARE RETURN_VAL FLOATARRAY;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	

	set i = 1;
	set column_count = CARDINALITY(columns_arg);
	set columns_arg[column_count + 1] = 'alpine_miner_id';
	set sql = alpine_miner_get_select_column_sql(table_name, columns_arg, wherestr); 
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;

fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count+1);
		set id = column_array[column_count + 1];
		set fa = TRIM_ARRAY(column_array, 1);
        set RETURN_VAL = alpine_miner_nn_ca_o(  
				weight_arg,  
				fa,  
				input_range_arg ,  
				input_base_arg,  
				hidden_node_number_arg,  
				hidden_layer_number_arg,  
				output_range_arg,  
				output_base_arg,  
				output_node_no_arg,  
				normalize_arg,  
				numerical_label_arg
				); 
		set sqlstr = ' insert into '||temp_table_name||' values ( '||id;
		set j = 1;
		while(j <= cardinality(RETURN_VAL)) do
			set sqlstr = sqlstr ||','|| RETURN_VAL[j];
			set j = j + 1;
		end while;
		set sqlstr = sqlstr ||')';
		execute immediate  sqlstr ;
	END LOOP;
	CLOSE my_cursor ;
END@

