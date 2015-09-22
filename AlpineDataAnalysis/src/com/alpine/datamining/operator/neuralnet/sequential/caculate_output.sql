
create or replace function neuralnetwork_compute_numerical_output(weight numeric[], column_names numeric[], input_range numeric[], input_base numeric[], column_count integer,output_range numeric, output_base numeric, hidden_layer_number integer, hidden_node_number integer[], output_node_no integer, normalize boolean , numerical_label boolean)
returns numeric  as 
$body$

declare

-- input node
input numeric[];

-- hidden node
hidden_node_output numeric[];
--hidden_node_error_sql numeric[];

--output node
output numeric[];

i integer;
j integer;
k integer;

--count of weight of input node, hidden node and output node
all_weight_count integer;
-- hidden node count
all_hidden_node_count integer;

-- index of weight[] weight_change[]
weight_index integer;
-- index of hidden node
hidden_node_number_index integer := 0;

begin

        all_weight_count := 0;
        all_hidden_node_count := 0;

        if (normalize) 
        then 
            i := 1;
            while i <= column_count loop
                if input_range[i] != 0
                then
                   input[i] := ((column_names[i]-input_base[i])/input_range[i]);
                else
                   input[i] := (column_names[i]-input_base[i]);
                end if;
                --raise notice 'input[%] %', i, input[i];
                i := i + 1;
            end loop;  
        else
                i := 1;
                while i <= column_count loop
                    input[i] := column_names[i];
                    i := i + 1;
                end loop;
        end if;

/*
        all_weight_count := 0;
        all_hidden_node_count := 0;

        all_weight_count:= (column_count + 1)*hidden_node_number[1];
        all_hidden_node_count :=  hidden_node_number[1];

        i := 2;
        while ( i <= hidden_layer_number) loop
                all_weight_count:=all_weight_count+(hidden_node_number[i - 1] + 1)*hidden_node_number[i];
                all_hidden_node_count := all_hidden_node_count + hidden_node_number[i];
                i := i+1;
        end loop;
*/
       -- all_weight_count := all_weight_count + (hidden_node_number[hidden_layer_number] + 1)*output_node_number;


--        --raise notice '%' , '--calculate hidden layer 1 output string';
        i := 1;
        while i <= hidden_node_number[1] loop
                hidden_node_output[i] := weight[(i - 1)*(column_count+1) + 1];
                --raise notice 'hiddensum[%] weigiht[%] %', i, (i - 1)*(column_count+1) + 1,weight[(i - 1)*(column_count+1) + 1];
                j := 1;
                while j <= column_count loop
                        hidden_node_output[i] := hidden_node_output[i]+input[j]*weight[j + 1 +(i - 1)*(column_count + 1)];
                        --raise notice 'hiddensum[%] input % weight[%] %', i, input[j] ,j + 1 +(i - 1)*(column_count +1), weight[j + 1 +(i - 1)*(column_count + 1)];
                        j := j + 1;
                end loop;
/*
                double result = 0.0d;
                if (weightedSum < -45.0d) {
                        result = 0;
                } else if (weightedSum > 45.0d) {
                        result = 1;
                } else {
                        result = 1 / (1 + Math.exp((-1) * weightedSum));
                }
*/

                --raise notice 'hiddensum[%] %', i, hidden_node_output[i];
                if (hidden_node_output[i] < -45.0)
                then
                        hidden_node_output[i] = 0;
                elsif (hidden_node_output[i] > 45.0)
                then
                        hidden_node_output[i] = 1;
                else
                    hidden_node_output[i] := (1.0/(1.0+exp( -1.0 * hidden_node_output[i])));
                end if;
                --raise notice 'hidden[%] %', i, hidden_node_output[i];
                i := i + 1;
        end loop;


--        --raise notice '%' , '-- calculate hidden layer 2~last output;';
        weight_index := hidden_node_number[1] * (column_count+1) ;

        if (hidden_layer_number > 1)
        then
                hidden_node_number_index := 0;
                i := 2;
                while i <= hidden_layer_number loop
                        hidden_node_number_index := hidden_node_number_index + hidden_node_number[i - 1];
                        j := 1;
                        while (j <= hidden_node_number[i]) loop
                                hidden_node_output[hidden_node_number_index + j] := weight[weight_index + (hidden_node_number[i - 1] +1) * (j - 1) + 1];
                                k := 1;
                                while (k <= hidden_node_number[i - 1]) loop
                                        hidden_node_output[hidden_node_number_index + j] := hidden_node_output[hidden_node_number_index + j]+hidden_node_output[hidden_node_number_index - hidden_node_number[ i - 1] + k]*weight[weight_index + (hidden_node_number[i - 1] +1) * (j - 1) + 1 + k];
                                        k := k + 1;
                                end loop;

                                if (hidden_node_output[hidden_node_number_index + j] < -45.0)
                                then
                                        hidden_node_output[hidden_node_number_index + j] = 0;
                                elsif (hidden_node_output[hidden_node_number_index + j] > 45.0)
                                then
                                        hidden_node_output[hidden_node_number_index + j] = 1;
                                else
                                        hidden_node_output[hidden_node_number_index + j] := (1.0/(1+exp(-1.0*hidden_node_output[hidden_node_number_index+j])));
                                end if;
                                --raise notice 'hidden[%] %', hidden_node_number_index + j, hidden_node_output[i];
                                j := j + 1;
                        end loop;
                        weight_index := weight_index + hidden_node_number[i] * (hidden_node_number[i - 1] + 1);
                        i := i + 1;
                end loop;
        end if;

--        --raise notice '%' , '--- compute output value of  output node;';
        i := 1;
--        while i <= output_node_number loop
                output[output_node_no] := weight[weight_index + (hidden_node_number[hidden_layer_number]+1) * (output_node_no - 1) +1];
                        --raise notice 'ouputsum[%]  weight[%] %', i, weight_index + (hidden_node_number[hidden_layer_number]+1) * (output_node_no - 1) +1, weight[ weight_index + (hidden_node_number[hidden_layer_number]+1) * (output_node_no - 1) +1];
                j := 1;
                while j <= hidden_node_number[hidden_layer_number] loop
                        output[output_node_no] := (output[output_node_no]+hidden_node_output[hidden_node_number_index + j])
                                        * weight[j + weight_index + (hidden_node_number[hidden_layer_number]+1) * (output_node_no - 1) +1];
                        --raise notice 'ouputsum[%] value % weight[%] %', j, hidden_node_output[hidden_node_number_index + j], j + weight_index + (hidden_node_number[hidden_layer_number]+1) * (output_node_no - 1)+1 , weight[j + weight_index + (hidden_node_number[hidden_layer_number]+1) * (output_node_no - 1) +1];
                        j := j + 1;
                end loop;
                --raise notice 'ouputsum[%] %', i, output[output_node_no];
                if (numerical_label)
                then
                        output[output_node_no] := ((output[output_node_no]) * output_range+output_base);
                else

                        if (output[output_node_no] < -45.0)
                        then
                                output[output_node_no] = 0;
                        elsif (output[output_node_no] > 45.0)
                        then
                                output[output_node_no] = 1;
                        else
                                output[output_node_no] := (1.0/(1+exp(-1.0*output[output_node_no])));
                        end if;
                end if;

         --       i := i + 1;
       -- end loop;


        return output[output_node_no];
end;
$body$
language 'plpgsql' immutable;
