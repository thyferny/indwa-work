
  CREATE OR REPLACE FUNCTION ALPINE_MINER_CONTAINS (array varchar2array, element varchar2)
return integer
as
begin
	for i in 1..array.count() loop
		if array(i) = element then
			return 1;
		end if;
	end loop;
	return 0;
end;
/
 

  CREATE OR REPLACE FUNCTION ALPINE_MINER_SPLIT 
(
    p_list varchar2,
    p_del varchar2 := ','
) return varchar2array
is
    l_idx    pls_integer;
    l_list    varchar2(32767) := p_list;
    l_value    varchar2(32767);
    result varchar2array := varchar2array();
begin
    loop
        l_idx := instr(l_list,p_del);
        if l_idx > 0 then
            result.extend();
            result(result.count()):= (substr(l_list,1,l_idx-1));
            l_list := substr(l_list,l_idx+length(p_del));
        else
            result.extend();
            result(result.count()) := l_list;
            exit;
        end if;
    end loop;
    return result;
end;
/
 

create or replace
FUNCTION ALPINE_MINER_AR_PREDICT (text_attribute integer, attribute_double FloatArray, attribute_text varchar2Array, positive varchar2, ar varchar2)-- ar clob)
return varchar2
as
	--sqlstr varchar2(4000) := '';
	i int := 0;
	result varchar2(4000) := null;
	result_array varchar2array := varchar2array();
	premise_conclusion_array varchar2array := varchar2array();
	premise_array varchar2array := varchar2array();
	conclusion_array varchar2array := varchar2array();
	ar_array varchar2array := varchar2array();
	--arribute_length integer := 0;
	conclusion_ok integer := 1;
	premise_str varchar2(4000);
	conclusion_str varchar2(4000);
BEGIN
    if ar = ''  or ar is null then
      return null;
    end if;
		ar_array := alpine_miner_split(ar, ';');
		for i in 1..ar_array.count() loop
			premise_conclusion_array := alpine_miner_split(ar_array(i), ':');
			premise_str := premise_conclusion_array(1);
			premise_array := alpine_miner_split(premise_str, '|');
			conclusion_str := premise_conclusion_array(2);
			conclusion_array := alpine_miner_split(conclusion_str, '|');
			conclusion_ok := 1;
			if text_attribute = 1 then
				if attribute_text(TO_NUMBER(conclusion_array(2))) = positive then
					GOTO label_continue;
				end if;
			else
				if attribute_double(TO_NUMBER(conclusion_array(2)))=TO_NUMBER(positive) then
					GOTO label_continue;
				end if;
			end if;

			for j in 1.. premise_array.count() loop
				if  text_attribute = 1 then
					if attribute_text(TO_NUMBER(premise_array(j))) !=  positive then
						conclusion_ok := 0;
						exit;
					end if;
				else
					if attribute_double(TO_NUMBER(premise_array(j)))!=  TO_NUMBER(positive) then
						conclusion_ok := 0;
						exit;
					end if;
				end if;
			end loop;
			if conclusion_ok = 1 then
				if alpine_miner_contains(result_array, conclusion_array(1))  = 0 then
					result_array.extend();
					result_array(result_array.count()) := conclusion_array(1);
					if result is not null then
						result := result||',';
					end if;
					result := result || conclusion_array(1);
				end if;
			end if;
		    <<label_continue>>
    		NULL;
		end loop;
	RETURN result;
END;

/
 
