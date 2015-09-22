
CREATE OR REPLACE LIBRARY alpine_miner IS '${ORACLE_HOME}/lib/alpine_miner.so';
/
CREATE OR REPLACE FUNCTION alpine_miner_faa2fa_c( 
		arrayarray IN floatarrayarray, 
		array IN OUT floatarray) 
RETURN binary_integer AS LANGUAGE C 
NAME "alpine_miner_faa2fa1" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
		CONTEXT,  
		arrayarray   OCICOLL,
		array   OCICOLL,
		RETURN   int);

/

create or replace function alpine_miner_faa2fa(f_arrayarray FloatArrayArray)
return FloatArray
as
f_array FloatArray := FloatArray();
i int;
begin
	i := alpine_miner_faa2fa_c(f_arrayarray, f_array);
	return f_array;
end;
/
CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_pi_c( 
		beta_arrayarray IN floatarrayarray,  
		columns_arrayarray IN floatarrayarray,
		add_intercept IN binary_integer,
		beta_array IN floatarray,  
		columns_array IN floatarray
)  
RETURN double precision AS LANGUAGE C 
NAME "alpine_miner_lr_ca_pi" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
		CONTEXT,  
		beta_arrayarray   OCICOLL,
		columns_arrayarray   OCICOLL,
		add_intercept int,
		beta_array   OCICOLL,
		columns_array   OCICOLL,
		RETURN DOUBLE); 
/
CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_pi( 
		beta_arrayarray IN floatarrayarray,  
		columns_arrayarray IN floatarrayarray,
		add_intercept IN binary_integer
)
  
RETURN double precision
as
begin
        return alpine_miner_lr_ca_pi_c(
		beta_arrayarray,  
		columns_arrayarray ,
		add_intercept,
		floatarray(),  
		floatarray()
		) ;
end;
/


create or replace FUNCTION alpine_miner_lr_ca_fitness_c(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision, label_value binary_integer, beta_array floatarray, columns_array floatarray) return double precision as language C
NAME "alpine_miner_lr_ca_fitness" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
		CONTEXT,  
		beta_arrayarray   OCICOLL,
		columns_arrayarray   OCICOLL,
		add_intercept int,
		weight DOUBLE,
		label_value int,
		beta_array   OCICOLL,
		columns_array   OCICOLL,
		RETURN DOUBLE); 
/

create or replace FUNCTION alpine_miner_lr_ca_fitness(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision, label_value binary_integer) return double precision
as
begin
        return alpine_miner_lr_ca_fitness_c(
                beta_arrayarray,
                columns_arrayarray ,
                add_intercept,
		weight,
		label_value,
                floatarray(),
                floatarray()
                ) ;
end;
/
create or replace FUNCTION alpine_miner_lr_ca_he_c(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision, beta_array floatarray,columns_array floatarray, return_array IN OUT floatarray) return binary_integer as language C
NAME "alpine_miner_lr_ca_he" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
		CONTEXT,  
		beta_arrayarray   OCICOLL,
		columns_arrayarray   OCICOLL,
		add_intercept int,
		weight DOUBLE,
		beta_array   OCICOLL,
		columns_array  OCICOLL,
		return_array   OCICOLL,
		RETURN int); 
/
create or replace FUNCTION alpine_miner_lr_ca_he(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision) return FloatArray  
as
f_array FloatArray := FloatArray();
i int;
begin
	i := alpine_miner_lr_ca_he_c(beta_arrayarray ,columns_arrayarray,add_intercept , weight ,floatarray(),floatarray(), f_array);
	return f_array;
end;
/

create or replace FUNCTION alpine_miner_lr_ca_he_de_c(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision , y binary_integer, beta_array floatarray,columns_array floatarray, return_array IN OUT floatarray) return binary_integer as language C
NAME "alpine_miner_lr_ca_he_de" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
		CONTEXT,  
		beta_arrayarray   OCICOLL,
		columns_arrayarray   OCICOLL,
		add_intercept int,
		weight DOUBLE,
		y int,
		beta_array   OCICOLL,
		columns_array  OCICOLL,
		return_array   OCICOLL,
		RETURN int); 
/
create or replace FUNCTION alpine_miner_lr_ca_he_de(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision, y binary_integer) return FloatArray  
as
f_array FloatArray := FloatArray();
i int;
begin
	i := alpine_miner_lr_ca_he_de_c(beta_arrayarray ,columns_arrayarray,add_intercept , weight ,y, floatarray(),floatarray(), f_array);
	return f_array;
end;
/

create or replace FUNCTION alpine_miner_lr_ca_beta_c(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision, y binary_integer, times binary_integer, beta_array floatarray,columns_array floatarray, return_array IN OUT floatarray) return binary_integer as language C
NAME "alpine_miner_lr_ca_beta" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
		CONTEXT,  
		beta_arrayarray   OCICOLL,
		columns_arrayarray   OCICOLL,
		add_intercept int,
		weight DOUBLE,
		y int,
		times int,
		beta_array   OCICOLL,
		columns_array  OCICOLL,
		return_array   OCICOLL,
		RETURN int); 
/
create or replace FUNCTION alpine_miner_lr_ca_beta(beta_arrayarray floatArrayArray,columns_arrayarray FloatArrayArray,add_intercept binary_integer, weight double precision, y binary_integer, times binary_integer) return FloatArray  
as
f_array FloatArray := FloatArray();
i int;
begin
	i := alpine_miner_lr_ca_beta_c(beta_arrayarray ,columns_arrayarray,add_intercept , weight , y , times, floatarray(),floatarray(), f_array);
	return f_array;
end;
/

create or replace FUNCTION alpine_miner_nn_ca_o_c(
weight_arrayarray_arg FloatArrayArray,  
columns_arrayarray_arg FloatArrayArray,  
input_range_arrayarray_arg FloatArrayArray,  
input_base_arrayarray_arg FloatArrayArray, 
hidden_node_number_arg IntegerArray, 
hidden_layer_number_arg binary_integer,  
output_range_arg DOUBLE PRECISION, 
output_base_arg DOUBLE PRECISION, 
output_node_no_arg binary_integer, 
normalize_arg binary_integer , 
numerical_label_arg binary_integer, 
weight_array FloatArray,  
columns_array FloatArray,  
input_range_array FloatArray,  
input_base_array FloatArray, 
input FloatArray,
hidden_node_output FloatArray,
return_array IN OUT FloatArray
)
RETURN binary_integer AS LANGUAGE C 
NAME "alpine_miner_nn_ca_o" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
CONTEXT,  
weight_arrayarray_arg OCICOLL,  
columns_arrayarray_arg OCICOLL,  
input_range_arrayarray_arg OCICOLL,  
input_base_arrayarray_arg OCICOLL, 
hidden_node_number_arg OCICOLL, 
hidden_layer_number_arg int,  
output_range_arg double, 
output_base_arg double, 
output_node_no_arg int, 
normalize_arg int , 
numerical_label_arg int, 
weight_array OCICOLL,  
columns_array OCICOLL,  
input_range_array OCICOLL,  
input_base_array OCICOLL, 
input OCICOLL,
hidden_node_output OCICOLL,
return_array OCICOLL,
RETURN   int);
/
create or replace FUNCTION alpine_miner_nn_ca_o(
weight_arrayarray_arg FloatArrayArray,  
columns_arrayarray_arg FloatArrayArray,  
input_range_arrayarray_arg FloatArrayArray,  
input_base_arrayarray_arg FloatArrayArray, 
hidden_node_number_arg IntegerArray, 
hidden_layer_number_arg integer,  
output_range_arg float, 
output_base_arg float, 
output_node_no_arg integer, 
normalize_arg integer , 
numerical_label_arg integer)
    RETURN floatArray 
    AS 
	result_array FloatArray := FloatArray();
	i integer;
begin
i := alpine_miner_nn_ca_o_c(
weight_arrayarray_arg ,  
columns_arrayarray_arg ,  
input_range_arrayarray_arg ,  
input_base_arrayarray_arg , 
hidden_node_number_arg, 
hidden_layer_number_arg ,  
output_range_arg , 
output_base_arg , 
output_node_no_arg , 
normalize_arg  , 
numerical_label_arg , 
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
result_array
);
  return result_array;
end;
/
create or replace FUNCTION alpine_miner_nn_ca_change_c(
weight_arrayarray_arg FloatArrayArray,  
columns_arrayarray_arg FloatArrayArray,  
input_range_arrayarray_arg FloatArrayArray,  
input_base_arrayarray_arg FloatArrayArray, 
hidden_node_number_arg IntegerArray, 
hidden_layer_number_arg binary_integer,  
output_range_arg DOUBLE PRECISION, 
output_base_arg DOUBLE PRECISION, 
output_node_no_arg binary_integer, 
normalize_arg binary_integer , 
numerical_label_arg binary_integer, 
label_arg double precision,
set_size_arg binary_integer,
weight_array FloatArray,  
columns_array FloatArray,  
input_range_array FloatArray,  
input_base_array FloatArray, 
input FloatArray,
hidden_node_output FloatArray,
output FloatArray,
output_error FloatArray,
hidden_node_error FloatArray,
return_array IN OUT FloatArray
)
RETURN binary_integer AS LANGUAGE C 
NAME "alpine_miner_nn_ca_change" 
LIBRARY alpine_miner 
WITH CONTEXT 
PARAMETERS ( 
CONTEXT,  
weight_arrayarray_arg OCICOLL,  
columns_arrayarray_arg OCICOLL,  
input_range_arrayarray_arg OCICOLL,  
input_base_arrayarray_arg OCICOLL, 
hidden_node_number_arg OCICOLL, 
hidden_layer_number_arg int,  
output_range_arg double, 
output_base_arg double, 
output_node_no_arg int, 
normalize_arg int , 
numerical_label_arg int, 
label_arg double,
set_size_arg int,
weight_array OCICOLL,  
columns_array OCICOLL,  
input_range_array OCICOLL,  
input_base_array OCICOLL, 
input OCICOLL,
hidden_node_output OCICOLL,
output OCICOLL,
output_error OCICOLL,
hidden_node_error OCICOLL,
return_array OCICOLL,
RETURN   int);
/
create or replace FUNCTION alpine_miner_nn_ca_change(
weight_arrayarray_arg FloatArrayArray,  
columns_arrayarray_arg FloatArrayArray,  
input_range_arrayarray_arg FloatArrayArray,  
input_base_arrayarray_arg FloatArrayArray, 
hidden_node_number_arg IntegerArray, 
hidden_layer_number_arg integer,  
output_range_arg float, 
output_base_arg float, 
output_node_no_arg integer, 
normalize_arg integer , 
numerical_label_arg integer,
label_arg float,
set_size_arg integer
)
    RETURN floatArray 
    AS 
	result_array FloatArray := FloatArray();
	i integer;
begin

i := alpine_miner_nn_ca_change_c(
weight_arrayarray_arg ,  
columns_arrayarray_arg ,  
input_range_arrayarray_arg ,  
input_base_arrayarray_arg , 
hidden_node_number_arg, 
hidden_layer_number_arg ,  
output_range_arg , 
output_base_arg , 
output_node_no_arg , 
normalize_arg  , 
numerical_label_arg , 
label_arg,  
set_size_arg,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
floatarray() ,
result_array
);
  return result_array;
end;
/


