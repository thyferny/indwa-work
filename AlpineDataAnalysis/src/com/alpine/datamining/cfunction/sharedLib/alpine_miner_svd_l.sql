CREATE OR REPLACE FUNCTION alpine_miner_svd_l(input_matrix text,p_name text, q_name text,  m_column text, n_column text, value_column text, num_features integer, init_val double precision)
  RETURNS double precision[] AS
$BODY$
DECLARE 
    j int := 0;
    sql text := '';
    float_temp float := 0;
    alpha float[] ;
    beta float[] ;
BEGIN

    sql := 'CREATE  TEMP TABLE '||p_name||'  WITH (appendonly=true) as select ' || m_column || ' as m_column , 1 as n_column, ' || value_column || ' as val from ' || input_matrix || ' where 0 = 1
     DISTRIBUTED BY (m_column);
    CREATE TEMP TABLE '||q_name||'  WITH (appendonly=true) as select ' || n_column || ' as n_column , 1 as m_column, ' || value_column || ' as val from ' || input_matrix || ' where 0 = 1
     DISTRIBUTED BY (n_column); 
    CREATE TEMP TABLE '||p_name||'1 as select ' || m_column || ' as m_column , ' || value_column || ' as val from ' || input_matrix || ' where 0 = 1
     DISTRIBUTED BY (m_column);
    CREATE TEMP TABLE '||q_name||'1 as select ' || n_column || ' as n_column , ' || value_column || ' as val from ' || input_matrix || ' where 0 = 1
     DISTRIBUTED BY (n_column); ';

    execute sql;

    j := 1;
    execute 'INSERT INTO '||q_name||'1 SELECT distinct ' || n_column || ', '||(init_val)||' FROM ' || input_matrix || ' where ' || n_column || ' is not null';
    execute 'INSERT INTO '||q_name||' SELECT n_column, 1, val FROM '||q_name||'1'; 

    while (j <=  num_features) loop
        if (j = 1) then
            execute 'insert into  '||p_name||'1  select a.' || m_column || ', sum(a.'|| value_column ||' * '||q_name||'.val) from ' || input_matrix || ' a join '||q_name||' on a.' || n_column || ' = '||q_name||'.n_column and '||q_name||'.m_column = 1  group by a.'|| m_column ;
        else
            execute 'insert into '||p_name||'1  select  foo.m_column , foo.val - ('||beta[j - 1]||') * '||p_name||'.val  from  (select a.' || m_column || ' as m_column, sum(a.'|| value_column ||' * '||q_name||'.val) as val from ' || input_matrix || ' a join '||q_name||' on a.'||n_column|| ' = '||q_name||'.n_column and '||q_name||'.m_column = '||j||' group by a.'|| m_column ||') as foo join '||p_name||'  on foo.m_column  = '||p_name||'.m_column  and  '||p_name||'.n_column = '||(j- 1);
        end if;
        execute  'select sqrt(sum(val * val)) from '||p_name||'1 ' into float_temp;
        alpha[j] := float_temp;
	if alpha[j] = 0
	then 
                beta[j] = 0;
		exit;
        end if;

        execute 'INSERT INTO '||p_name||' SELECT  m_column,'||(j)||', (1.0*val/'||alpha[j]||') FROM '||p_name||'1'; 
	execute 'TRUNCATE TABLE '||q_name||'1';
        execute 'insert into '||q_name||'1 select foo.n_column , foo.val - ('||alpha[j]||') * '||q_name||'.val from  (select a.' || n_column || 'as n_column,  sum(a.'|| value_column ||' * '||p_name||'.val) as  val from ' || input_matrix || ' a join '||p_name||' on a.' || m_column || ' = '||p_name||'.m_column and '||p_name||'.n_column = '||j||' group by a.'|| n_column ||') as foo join '||q_name||'  on foo.n_column  = '||q_name||'.n_column and '||q_name||'.m_column = '||j;

        execute  'select sqrt(sum(val * val)) from '||q_name||'1 ' into float_temp;
        beta[j] := float_temp;
	if beta[j] = 0
	then 
		exit;
        end if;
	if (j != num_features) then
	        execute 'INSERT INTO '||q_name||' SELECT n_column, '||(j + 1)||', (1.0*val/'||beta[j]||') FROM '||q_name||'1'; 
	end if;
        sql := 'TRUNCATE TABLE '||p_name||'1';
        execute sql;
        j := j + 1;
    end loop;
    return array_cat(alpha, beta);        
END;
$BODY$
  LANGUAGE 'plpgsql' ;

