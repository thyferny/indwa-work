
  CREATE OR REPLACE FUNCTION alpine_miner_contains (myarray text[], element text)
returns integer
as $$
begin
	for i in 1..alpine_miner_get_array_count(myarray) loop
		if myarray[i] = element then
			return 1;
		end if;
	end loop;
	return 0;
end;
$$ LANGUAGE plpgsql ;

CREATE or replace FUNCTION  alpine_miner_instr(varchar, varchar) RETURNS integer AS $$
DECLARE
    pos integer;
BEGIN
    pos:= alpine_miner_instr($1, $2, 1);
    RETURN pos;
END;
$$ LANGUAGE plpgsql ;


CREATE or replace FUNCTION  alpine_miner_instr(string varchar, string_to_search varchar, beg_index integer)
RETURNS integer AS $$
DECLARE
    pos integer DEFAULT 0;
    temp_str varchar;
    beg integer;
    length integer;
    ss_length integer;
BEGIN
    IF beg_index > 0 THEN
        temp_str := substring(string FROM beg_index);
        pos := position(string_to_search IN temp_str);

        IF pos = 0 THEN
            RETURN 0;
        ELSE
            RETURN pos + beg_index - 1;
        END IF;
    ELSE
        ss_length := char_length(string_to_search);
        length := char_length(string);
        beg := length + beg_index - ss_length + 2;

        WHILE beg > 0 LOOP
            temp_str := substring(string FROM beg FOR ss_length);
            pos := position(string_to_search IN temp_str);

            IF pos > 0 THEN
                RETURN beg;
            END IF;

            beg := beg - 1;
        END LOOP;

        RETURN 0;
    END IF;
END;
$$ LANGUAGE plpgsql;

create or replace function alpine_miner_get_array_count(myarray text[])
returns integer as $$
declare
	array_dims_result text;
	begin_pos int := 1;
	end_pos int := 1;
	temp_str text;
begin
	select array_dims(myarray) into array_dims_result;
	begin_pos := alpine_miner_instr(array_dims_result, ':')+1;
	end_pos := alpine_miner_instr(array_dims_result, ']') ;
	temp_str := substring(array_dims_result from begin_pos for (end_pos - begin_pos));
	return to_number(temp_str,'99999999999999999999');
end;
$$ language plpgsql;


create or replace function alpine_miner_get_array_countI(myarray integer[])
returns integer as $$
declare
	array_dims_result text;
	begin_pos int := 1;
	end_pos int := 1;
	temp_str text;
begin

	select array_dims(myarray) into array_dims_result;
	begin_pos := alpine_miner_instr(array_dims_result, ':')+1;
	end_pos := alpine_miner_instr(array_dims_result, ']') ;
	temp_str := substring(array_dims_result from begin_pos for (end_pos - begin_pos));
	return to_number(temp_str,'99999999999999999999');
end;
$$ language plpgsql;


create or replace function alpine_miner_get_array_counti(myarray bigint[])
returns integer as $$
declare
	array_dims_result text;
	begin_pos int := 1;
	end_pos int := 1;
	temp_str text;
begin

	select array_dims(myarray) into array_dims_result;
	begin_pos := alpine_miner_instr(array_dims_result, ':')+1;
	end_pos := alpine_miner_instr(array_dims_result, ']') ;
	temp_str := substring(array_dims_result from begin_pos for (end_pos - begin_pos));
	return to_number(temp_str,'99999999999999999999');
end;
$$ language plpgsql;


create or replace function alpine_miner_get_array_countF(myarray float[])
returns integer as $$
declare
	array_dims_result text;
	begin_pos int := 1;
	end_pos int := 1;
	temp_str text;
begin

	select array_dims(myarray) into array_dims_result;
	begin_pos := alpine_miner_instr(array_dims_result, ':')+1;
	end_pos := alpine_miner_instr(array_dims_result, ']') ;
	temp_str := substring(array_dims_result from begin_pos for (end_pos - begin_pos));
	return to_number(temp_str,'99999999999999999999');
end;
$$ language plpgsql;


  CREATE OR REPLACE FUNCTION alpine_miner_split 
(
    p_list text,
    p_del text 
) returns text[]
as $$
declare
    l_idx   integer;
    l_list   text := p_list;
    l_value    text;
    result text[];
    result_count int := 0;
begin
    loop
        l_idx := alpine_miner_instr(l_list,p_del);
        result_count := result_count + 1;
        if l_idx > 0 then
            result[result_count]:= (substr(l_list,1,l_idx-1));
            l_list := substr(l_list,l_idx+length(p_del));
        else
            result[result_count] := l_list;
            exit;
        end if;
    end loop;
    return result;
end;
$$ LANGUAGE plpgsql ;

  CREATE OR REPLACE FUNCTION alpine_miner_ar_predict (text_attribute integer, attribute_double float[], attribute_text text[], positive text, ar text)-- ar clob)
returns text
as $$
declare
	--sqlstr text := '';
	i int := 0;
	result text := null;
	result_array text[];
	premise_conclusion_array text[];
	premise_array text[];
	conclusion_array text[];
	ar_array text[];
	--arribute_length integer := 0;
	conclusion_ok integer := 1;
	premise_str text;
	conclusion_str text;
	result_array_count integer := 1;
BEGIN
		if ar = '' or ar is null then
			return null;
		end if;
		ar_array := alpine_miner_split(ar, ';');
		for i in 1..alpine_miner_get_array_count(ar_array) loop
			premise_conclusion_array := alpine_miner_split(ar_array[i], ':');
			premise_str := premise_conclusion_array[1];
			premise_array := alpine_miner_split(premise_str, '|');
			conclusion_str := premise_conclusion_array[2];
			conclusion_array := alpine_miner_split(conclusion_str, '|');
			conclusion_ok := 1;
				if(text_attribute = 1) then
					if attribute_text[TO_NUMBER(conclusion_array[2],'99999999999999999999')] = positive then
						continue;
					end if;
				else
					if attribute_double[TO_NUMBER(conclusion_array[2],'99999999999999999999')] = positive then
						continue;
					end if;
				end if;

			for j in 1.. alpine_miner_get_array_count(premise_array) loop
				if(text_attribute = 1) then
					if attribute_text[TO_NUMBER(premise_array[j],'99999999999999999999')] !=  positive then
						conclusion_ok := 0;
						exit;
					end if;
				else
					if attribute_double[TO_NUMBER(premise_array[j],'99999999999999999999')] !=  positive then
						conclusion_ok := 0;
						exit;
					end if;
				end if;
			end loop;
			if conclusion_ok = 1 then
				if result_array is null or alpine_miner_contains(result_array, conclusion_array[1])  = 0 then
					result_array[result_array_count] := conclusion_array[1];
					result_array_count := result_array_count + 1;
					if result is not null then
						result := result||',';
					else
						result := '';
					end if;
					result := result || conclusion_array[1];
				end if;
			end if;
		end loop;
	RETURN result;
END;
$$ LANGUAGE plpgsql ;
