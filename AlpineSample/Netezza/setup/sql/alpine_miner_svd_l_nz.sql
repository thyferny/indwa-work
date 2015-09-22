CREATE OR REPLACE PROCEDURE alpine_miner_svd_l(
text,
text, 
text,  
text, 
text, 
text, 
integer, 
double,
text)


language nzplsql
returns int
as
BEGIN_PROC

DECLARE

input_matrix ALIAS FOR $1;
p_name ALIAS FOR $2;
q_name ALIAS FOR $3;
m_column ALIAS FOR $4;
n_column ALIAS FOR $5;
value_column ALIAS FOR $6;
num_features ALIAS FOR $7;
init_val ALIAS FOR $8;
result_table ALIAS FOR $9;

temp_int int := 0;
temp_double double := 0;
i int := 0;
j int := 0;
sql text := '';
float_temp double := 0;
alpha_array varray(200000) of double ;
beta_array varray(200000) of double ;
alpha_count int := 0;
beta_count int := 0;
myrecord record;

BEGIN

    sql := 'CREATE  TEMP TABLE '||p_name||'  as select ' || m_column || ' as m_column , 1 as n_column, ' || '0.1::double' || ' as val from ' || input_matrix || ' where 0 = 1;
    CREATE TEMP TABLE '||q_name||'  as select ' || n_column || ' as n_column , 1 as m_column, ' || '0.1::double' || ' as val from ' || input_matrix || ' where 0 = 1;
    CREATE TEMP TABLE '||p_name||'1 as select ' || m_column || ' as m_column , ' || '0.1::double' || ' as val from ' || input_matrix || ' where 0 = 1;
    CREATE TEMP TABLE '||q_name||'1 as select ' || n_column || ' as n_column , ' || '0.1::double' || ' as val from ' || input_matrix || ' where 0 = 1;';

    execute immediate sql;

    j := 1;
    execute immediate 'INSERT INTO '||q_name||'1 SELECT distinct ' || n_column || ', '||(init_val)||' FROM ' || input_matrix || ' where ' || n_column || ' is not null';
    execute immediate 'INSERT INTO '||q_name||' SELECT n_column, 1, val FROM '||q_name||'1'; 

    while (j <=  num_features) loop
        if (j = 1) then
            execute immediate 'insert into  '||p_name||'1  select a.' || m_column || ', sum(a.'|| value_column ||'::double * '||q_name||'.val::double) from ' || input_matrix || ' a join '||q_name||' on a.' || n_column || ' = '||q_name||'.n_column and '||q_name||'.m_column = 1 and a.'|| value_column ||' is not null group by a.'|| m_column ;
        else
            temp_double := beta_array(j - 1);
            execute immediate 'insert into '||p_name||'1  select  foo.m_column , foo.val - ('||temp_double||')::double * '||p_name||'.val  from  (select a.' || m_column || ' as m_column, sum(a.'|| value_column ||'::double * '||q_name||'.val) as val from ' || input_matrix || ' a join '||q_name||' on a.'||n_column|| ' = '||q_name||'.n_column and '||q_name||'.m_column = '||j||' and a.'|| value_column ||' is not null  group by a.'|| m_column ||') as foo join '||p_name||'  on foo.m_column  = '||p_name||'.m_column  and  '||p_name||'.n_column = '||(j- 1);
        end if;
        for myrecord in  execute 'select sqrt(sum(val::double * val)) as value from '||p_name||'1 ' loop
		float_temp := myrecord.value;
        end loop;
        alpha_array(j) := float_temp;
        alpha_count := j;
	if alpha_array(j) = 0
	then 
                beta_array(j) = 0;
		exit;
        end if;

        temp_double := alpha_array(j);
        sql := 'INSERT INTO '||p_name||' SELECT  m_column,'||j||', 1.0::double*val/'||temp_double||' FROM '||p_name||'1';
        execute immediate 'INSERT INTO '||p_name||' SELECT  m_column,'||(j)||', (1.0::double*val/'||temp_double||') FROM '||p_name||'1'; 
	execute immediate 'TRUNCATE TABLE '||q_name||'1';
        temp_double := alpha_array(j);
        execute immediate 'insert into '||q_name||'1 select foo.n_column , foo.val - ('||temp_double||')::double * '||q_name||'.val from  (select a.' || n_column || 'as n_column,  sum(a.'|| value_column ||'::double * '||p_name||'.val) as  val from ' || input_matrix || ' a join '||p_name||' on a.' || m_column || ' = '||p_name||'.m_column and '||p_name||'.n_column = '||j||'  and a.'|| value_column ||' is not null  group by a.'|| n_column ||') as foo join '||q_name||'  on foo.n_column  = '||q_name||'.n_column and '||q_name||'.m_column = '||j;

        for myrecord in execute 'select sqrt(sum(val::double * val)) as value from '||q_name||'1 ' loop
                float_temp := myrecord.value;
        end loop;
        beta_array(j) := float_temp;
        beta_count := j;
	if beta_array(j) = 0
	then 
		exit;
        end if;
	if (j != num_features) then
                temp_double := beta_array(j);
	        execute immediate 'INSERT INTO '||q_name||' SELECT n_column, '||(j + 1)||', (1.0::double*val/'||temp_double||') FROM '||q_name||'1'; 
	end if;
        sql := 'TRUNCATE TABLE '||p_name||'1';
        execute immediate sql;
        j := j + 1;
    end loop;
    for i in 1.. alpha_count loop
        temp_double := alpha_array(i);
        execute immediate 'insert into '||result_table||' values ('||i||','||temp_double||')';
    end loop;
    for i in 1.. beta_count loop
        temp_double := beta_array(i);
        execute immediate 'insert into '||result_table||' values ('||(i+alpha_count)||','||temp_double||')';
    end loop;

    return 0;
END;
END_PROC;


