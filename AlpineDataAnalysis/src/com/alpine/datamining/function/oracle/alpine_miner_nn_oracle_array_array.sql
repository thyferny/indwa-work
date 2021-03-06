create or replace FUNCTION alpine_miner_nn_ca_output(  weight_arg FloatArray,  columns_arg FloatArray,  input_range_arg FloatArray,  input_base_arg FloatArray,  hidden_node_number_arg IntegerArray,  hidden_layer_number_arg integer,  output_range_arg binary_double,  output_base_arg binary_double,  output_node_no_arg integer,  normalize_arg integer ,  numerical_label_arg integer,	all_hidden_node_count in out integer,	input in out FloatArray,  output  in out FloatArray,	hidden_node_output in out FloatArray,  columns_count in out integer,  hidden_node_number_count in out integer,  weight_count in out  integer)
RETURN FloatArray
AS 
  i integer := 1;
	j integer := 1;
	k integer := 1;
 	weight_index integer := 0;
	hidden_node_number_index integer := 0;
  result_count  integer:= 0;
  result_data FloatArray := FloatArray();
begin

	if (weight_arg is null or columns_arg is null or input_range_arg is null or input_base_arg is null or hidden_node_number_arg is null or hidden_layer_number_arg is null or output_range_arg is null or output_base_arg is null or output_node_no_arg is null or normalize_arg is null or numerical_label_arg is null   or 	all_hidden_node_count is null or  input  is null or  output  is null or	hidden_node_output is null or  columns_count is null or  hidden_node_number_count is null or  weight_count is null)
	then
		return null;
	end if;
  columns_count := columns_arg.count();
  hidden_node_number_count := hidden_node_number_arg.count();
  weight_count  := weight_arg.count();
	for i in 1..columns_count loop
		input.extend();
	end loop;

	for i in 1..output_node_no_arg loop
		output.extend();
	end loop;
	all_hidden_node_count := 0;
	for i in 1..hidden_layer_number_arg loop
		all_hidden_node_count := all_hidden_node_count + hidden_node_number_arg(i);
	end loop;

	for i in 1..all_hidden_node_count loop
		hidden_node_output.extend();
	end loop;

	result_count := output_node_no_arg;
	for i in 1..result_count loop
		result_data.extend();
	end loop;

  if (normalize_arg = 1)  then
       i := 1;
       while  i <= columns_count loop
          if ((input_range_arg(i)) != 0) then
              input(i) := (columns_arg(i)-input_base_arg(i))/input_range_arg(i);
          else
              input(i) := columns_arg(i)-input_base_arg(i);
          end if;
          i := i + 1;
        end loop;
  else
      i := 1;
    	while  i <= columns_count loop
         	    input(i) := (columns_arg(i));
         	    i := i + 1;
    	end loop;
	end if;

  i := 1;
  while  i <= hidden_node_number_arg(1)  loop
          hidden_node_output(i) := weight_arg(1+(i-1)*(columns_count + 1));
          j := 1;
          while  j <= columns_count loop
                  hidden_node_output(i) := hidden_node_output(i)+input(j)*weight_arg(1 + j  + (i-1) *(columns_count + 1));
                  j := j + 1;
          end loop;

          if (hidden_node_output(i) < -45.0) then
            hidden_node_output(i) := 0;
          elsif (hidden_node_output(i) > 45.0) then
            hidden_node_output(i) := 1;
          else
            hidden_node_output(i) := (1.0/(1.0+exp( -1.0 * hidden_node_output(i))));
          end if;
          i := i + 1;
  end loop;
  weight_index := hidden_node_number_arg(1) * (columns_count + 1) ;

  if (hidden_layer_number_arg > 1) then
    hidden_node_number_index := 0;
    i := 2;
    while  i <= hidden_layer_number_arg  loop
            hidden_node_number_index := hidden_node_number_index + hidden_node_number_arg(i - 1);
            j := 1;
            while  j <= hidden_node_number_arg(i)  loop
                    hidden_node_output(hidden_node_number_index + j) := weight_arg(weight_index + 1 + (hidden_node_number_arg(i - 1) +1) * (j-1));
                    k := 1;
                    while  k <= hidden_node_number_arg(i - 1)  loop
                            hidden_node_output(hidden_node_number_index + j) := hidden_node_output(hidden_node_number_index + j)+hidden_node_output(hidden_node_number_index - hidden_node_number_arg(i - 1) + k)*weight_arg(weight_index + (hidden_node_number_arg(i - 1) +1) * (j-1) + k + 1);
                            k := k + 1;
                    end loop;
                    if (hidden_node_output(hidden_node_number_index + j) < -45.0) then
                      hidden_node_output(hidden_node_number_index + j) := 0;
                    elsif (hidden_node_output(hidden_node_number_index + j) > 45.0) then
                      hidden_node_output(hidden_node_number_index + j) := 1;
                    else
                      hidden_node_output(hidden_node_number_index + j) := (1.0/(1+exp(-1.0*hidden_node_output(hidden_node_number_index+j))));
                    end if;
                    j := j + 1;
            end loop;
            weight_index := weight_index + hidden_node_number_arg(i) * (hidden_node_number_arg(i - 1) + 1);
            i := i + 1;
   end loop;
  end if;

  i := 1;
  while  i <= output_node_no_arg loop
          output(i) := weight_arg(weight_index + 1 + (hidden_node_number_arg(hidden_layer_number_arg)+1) * (i - 1));
          j := 1;
          while  j <= hidden_node_number_arg(hidden_layer_number_arg)  loop
                  output(i) := output(i)+hidden_node_output(hidden_node_number_index + j) * weight_arg(1 + j + weight_index  + (hidden_node_number_arg(hidden_layer_number_arg)+1) * (i - 1) );
                  j := j + 1;
          end loop;
          if (numerical_label_arg = 1) then
                        output(i) := (output(i) * output_range_arg+output_base_arg);
          else
            if (output(i) < -45.0) then
              output(i) := 0;
            elsif (output(i) > 45.0) then
              output(i) := 1;
            else
              output(i) := (1.0/(1+exp(-1.0*output(i))));
            end if;
          end if;
          i := i + 1;
  end loop;

	for i in 1..output_node_no_arg loop
		result_data(i) := output(i);
	end loop;

  return result_data;
end;
/
create or replace FUNCTION alpine_miner_nn_ca_o(weight_arrayarray_arg FloatArrayArray,  columns_arrayarray_arg FloatArrayArray,  input_range_arrayarray_arg FloatArrayArray,  input_base_arrayarray_arg FloatArrayArray, hidden_node_number_arg IntegerArray, hidden_layer_number_arg integer,  output_range_arg binary_double, output_base_arg binary_double, output_node_no_arg integer, normalize_arg integer , numerical_label_arg integer)
    RETURN floatArray 
    AS 
	all_hidden_node_count integer := 0;
	input FloatArray := FloatArray();
  output FloatArray := FloatArray();
	hidden_node_output FloatArray := FloatArray();
  columns_count integer:= 0;
  hidden_node_number_count integer:= hidden_node_number_arg.count();
  weight_count  integer:= 0;
  result_data FloatArray := FloatArray();
	weight_arg FloatArray;
	columns_arg FloatArray;
	input_range_arg FloatArray;
	input_base_arg FloatArray;
begin
	if (weight_arrayarray_arg is null or columns_arrayarray_arg is null or input_range_arrayarray_arg is null or input_base_arrayarray_arg is null or hidden_node_number_arg is null or hidden_layer_number_arg is null or output_range_arg is null or output_base_arg is null or output_node_no_arg is null or normalize_arg is null or numerical_label_arg is null)
	then
		return null;
	end if;
	weight_arg := alpine_miner_faa2fa(weight_arrayarray_arg);
	columns_arg := alpine_miner_faa2fa(columns_arrayarray_arg);
	input_range_arg := alpine_miner_faa2fa(input_range_arrayarray_arg);
	input_base_arg := alpine_miner_faa2fa(input_base_arrayarray_arg);

  	columns_count := columns_arg.count();
  	weight_count := weight_arg.count();

  result_data := alpine_miner_nn_ca_output(  weight_arg ,  columns_arg ,  input_range_arg ,  input_base_arg ,  hidden_node_number_arg ,  hidden_layer_number_arg ,  output_range_arg ,  output_base_arg ,  output_node_no_arg ,  normalize_arg  ,  numerical_label_arg ,	all_hidden_node_count,	input ,  output ,	hidden_node_output,  columns_count,  hidden_node_number_count,  weight_count);
  return result_data;
end;
/

create or replace FUNCTION alpine_miner_nn_ca_change(  weight_arrayarray_arg FloatArrayArray,  columns_arrayarray_arg FloatArrayArray,  input_range_arrayarray_arg FloatArrayArray,  input_base_arrayarray_arg FloatArrayArray,  hidden_node_number_arg IntegerArray,  hidden_layer_number_arg integer,  output_range_arg binary_double,  output_base_arg binary_double,  output_node_no_arg integer,  normalize_arg integer ,  numerical_label_arg integer,  label_arg binary_double,  set_size_arg int)
RETURN FloatArray
AS 
  i integer := 1;
	j integer := 1;
	k integer := 1;
	all_hidden_node_count integer := 0;

	weight_index integer := 0;
	hidden_node_number_index integer := 0;
	input FloatArray := FloatArray();
  output FloatArray := FloatArray();
	hidden_node_output FloatArray := FloatArray();
  columns_count integer:= 0;
  hidden_node_number_count integer:= hidden_node_number_arg.count();
  weight_count  integer:= 0;
  result_count  integer:= 0;
  result_data FloatArray := FloatArray();
  set_size integer := 0;
  error_sum binary_double := 0.0;
	delta binary_double := 0.0;
	total_error binary_double := 0.0;
	output_error FloatArray := FloatArray();
	direct_output_error binary_double := 0.0;

	hidden_node_error FloatArray := FloatArray();
	current_change binary_double:= 0.0;
	threshold_change binary_double:= 0.0;
	weight_arg FloatArray;
	columns_arg FloatArray;
	input_range_arg FloatArray;
	input_base_arg FloatArray;

begin

	if (weight_arrayarray_arg is null or columns_arrayarray_arg is null or input_range_arrayarray_arg is null or input_base_arrayarray_arg is null or hidden_node_number_arg is null or hidden_layer_number_arg is null or output_range_arg is null or output_base_arg is null or output_node_no_arg is null or normalize_arg is null or numerical_label_arg is null or label_arg is null or set_size_arg is null)
	then
		return null;
	end if;

	weight_arg := alpine_miner_faa2fa(weight_arrayarray_arg);
	columns_arg := alpine_miner_faa2fa(columns_arrayarray_arg);
	input_range_arg := alpine_miner_faa2fa(input_range_arrayarray_arg);
	input_base_arg := alpine_miner_faa2fa(input_base_arrayarray_arg);
  	columns_count := columns_arg.count();
  	weight_count := weight_arg.count();

  output := alpine_miner_nn_ca_output(  weight_arg ,  columns_arg ,  input_range_arg ,  input_base_arg ,  hidden_node_number_arg ,  hidden_layer_number_arg ,  output_range_arg ,  output_base_arg ,  output_node_no_arg ,  normalize_arg  ,  numerical_label_arg ,	all_hidden_node_count,	input ,  output ,	hidden_node_output,  columns_count,  hidden_node_number_count,  weight_count);

	if (set_size_arg <= 0) then
		set_size := 1;
  else
    set_size := set_size_arg;
	end if;

	result_count := weight_count + 1;
	for i in 1..result_count loop
		result_data.extend();
	end loop;

	for i in 1..output_node_no_arg loop
		output_error.extend();
	end loop;
	for i in 1..output_node_no_arg  loop
		if(numerical_label_arg = 1) then
			if (output_range_arg = 0.0) then
				direct_output_error := 0.0;
			else
				direct_output_error := (label_arg - output(i))/output_range_arg;
			end if;
			output_error(i) := direct_output_error;
		else
			if ((label_arg) = (i - 1)) then
				direct_output_error := 1.0 - output(i);
			else
				direct_output_error := 0.0 - output(i);
			end if;
			output_error(i) := direct_output_error * output(i) * (1- output(i));
		end if;
		total_error := total_error + direct_output_error*direct_output_error;
	end loop;

	for i in 1..all_hidden_node_count loop
		hidden_node_error.extend();
	end loop;

	weight_index := weight_count - output_node_no_arg*(hidden_node_number_arg(hidden_layer_number_arg) + 1)  ;
	hidden_node_number_index := all_hidden_node_count - hidden_node_number_arg(hidden_layer_number_arg);
	for i in 1..hidden_node_number_arg(hidden_layer_number_arg)  loop
		error_sum := 0.0;
		for k in 1..output_node_no_arg  loop
			error_sum := error_sum+output_error(k)*weight_arg(weight_index + (hidden_node_number_arg(hidden_layer_number_arg) + 1)*(k - 1) + i + 1);
		end loop;
		hidden_node_error(hidden_node_number_index + i) := error_sum*hidden_node_output(hidden_node_number_index + i)*(1.0-hidden_node_output(hidden_node_number_index + i));
	end loop;

	if (hidden_layer_number_arg > 1) then
		weight_index := weight_index - (hidden_node_number_arg(hidden_layer_number_arg - 1) + 1)*hidden_node_number_arg(hidden_layer_number_arg);
		hidden_node_number_index := hidden_node_number_index - hidden_node_number_arg(hidden_layer_number_arg - 1);
		for i in REVERSE hidden_layer_number_arg - 1..1  loop
			for j in 1..hidden_node_number_arg(i)  loop
				error_sum := 0.0;
				for k in 1..hidden_node_number_arg(i + 1)  loop
					error_sum := error_sum+hidden_node_error(hidden_node_number_index + hidden_node_number_arg(i) + k)*weight_arg(weight_index + (hidden_node_number_arg(i)+1)*(k - 1) + j + 1);
				end loop;
				hidden_node_error(hidden_node_number_index + j) := error_sum*hidden_node_output(hidden_node_number_index + j)*(1-hidden_node_output(hidden_node_number_index + j));
			end loop;
      if (i != 1) then
        weight_index := weight_index - (hidden_node_number_arg(i - 1)+1) * hidden_node_number_arg(i);
        hidden_node_number_index := hidden_node_number_index - hidden_node_number_arg(i - 1);
      else
        weight_index := weight_index - (columns_count+1) * hidden_node_number_arg(i);
        hidden_node_number_index := 0;
      end if;
		end loop;
	end if;

	weight_index := weight_count - (hidden_node_number_arg(hidden_layer_number_arg)+ 1)* output_node_no_arg;
	hidden_node_number_index := all_hidden_node_count - hidden_node_number_arg(hidden_layer_number_arg);

	for i in 1..output_node_no_arg  loop
		delta := 1.0/set_size*output_error(i);
		threshold_change := delta;
		result_data(weight_index + 1 +(hidden_node_number_arg(hidden_layer_number_arg)+ 1)*(i - 1)) := (threshold_change);

		for j in 1..hidden_node_number_arg(hidden_layer_number_arg)  loop
			current_change := delta * hidden_node_output(hidden_node_number_index + j);
			result_data(weight_index +(hidden_node_number_arg(hidden_layer_number_arg)+ 1)*(i - 1) + 1 +j) := (current_change);
		end loop;
	end loop;

	if (hidden_layer_number_arg > 1) then
		for i in reverse hidden_layer_number_arg..2   loop
			weight_index := weight_index - (hidden_node_number_arg(i - 1)+1)*hidden_node_number_arg(i);
			hidden_node_number_index := hidden_node_number_index - hidden_node_number_arg(i - 1);
			delta := 0.0;
			for j in 1..hidden_node_number_arg(i)  loop
				delta := (1.0/set_size*hidden_node_error(hidden_node_number_index + hidden_node_number_arg(i - 1) + j));
				threshold_change := delta;
				result_data(weight_index + 1+ (hidden_node_number_arg(i - 1) + 1) * (j - 1)) := (threshold_change);
				for k in 1..hidden_node_number_arg(i - 1)  loop
					current_change := delta * hidden_node_output(hidden_node_number_index + k);
					result_data(weight_index + (hidden_node_number_arg(i - 1) + 1) * (j - 1) + 1 +  k) := (current_change);
				end loop;
			end loop;
		end loop;
	end if;

	weight_index := 0; 
	hidden_node_number_index := 0;
	delta := 0.0;
	for j in 1..hidden_node_number_arg(1)  loop
		delta := 1.0/set_size*hidden_node_error(hidden_node_number_index + j);
		threshold_change := delta;
		result_data(weight_index + 1 + (columns_count+1)*(j - 1)) := threshold_change;
		for k in 1..columns_count  loop
			current_change := delta*input(k);
			result_data(weight_index +  (columns_count+1)*(j - 1) + k + 1) := (current_change);
		end loop;
	end loop;
	result_data(weight_count + 1) := (total_error);
  return (result_data);
end;
/
