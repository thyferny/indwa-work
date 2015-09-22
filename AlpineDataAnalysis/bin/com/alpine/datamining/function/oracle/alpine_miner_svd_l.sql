
create or replace
FUNCTION alpine_miner_svd_l(input_matrix varchar2,p_name varchar2, q_name varchar2,  m_column varchar2, n_column varchar2, value_column varchar2, num_features integer, init_val binary_double)
  RETURN floatarray AS
    i int := 0;
    j int := 0;
    sqlstr varchar2(32767);
    float_temp binary_double := 0;
    alpha floatarray := floatarray();
    beta floatarray  := floatarray();
    executesql varchar2(1000) := '';
    PRAGMA AUTONOMOUS_TRANSACTION ; 
BEGIN
    sqlstr := 'call PROC_DROPSCHTABLEIFEXISTS( '''||p_name||''')';
    execute immediate sqlstr;
    sqlstr := 'call PROC_DROPSCHTABLEIFEXISTS( '''||q_name||''')';
    execute immediate sqlstr;
    sqlstr := 'call PROC_DROPSCHTABLEIFEXISTS( '''||p_name||'1'')';
    execute immediate sqlstr;
    sqlstr := 'call PROC_DROPSCHTABLEIFEXISTS( '''||q_name||'1'')';
    execute immediate sqlstr;
    executesql:='alter session force parallel dml';
    execute immediate executesql;
    sqlstr := 'CREATE  TABLE '||p_name||' parallel as select ' || m_column || ' as m_column , 1 as n_column, ' || 'cast(1.0 as binary_double)' || ' as val from ' || input_matrix || ' where 0 = 1';
    execute immediate sqlstr;
    executesql:='alter session disable parallel dml';
    execute immediate executesql;
    executesql:='alter session force parallel dml';
    execute immediate executesql;
    sqlstr := 'CREATE TABLE '||q_name||'  parallel as select ' || n_column || ' as n_column , 1 as m_column, ' || 'cast(1.0 as binary_double)' || ' as val from ' || input_matrix || ' where 0 = 1'; 
    execute immediate sqlstr;
    executesql:='alter session disable parallel dml';
    execute immediate executesql;
    executesql:='alter session force parallel dml';
    execute immediate executesql;
    sqlstr := 'CREATE TABLE '||p_name||'1 parallel as select ' || m_column || ' as m_column , ' || 'cast(1.0 as binary_double)' || ' as val from ' || input_matrix || ' where 0 = 1';
    execute immediate sqlstr;
    executesql:='alter session disable parallel dml';
    execute immediate executesql;
    executesql:='alter session force parallel dml';
    execute immediate executesql;
    sqlstr := 'CREATE TABLE '||q_name||'1  parallel as select ' || n_column || ' as n_column , ' || 'cast(1.0 as binary_double)' || ' as val from ' || input_matrix || ' where 0 = 1';
    execute immediate sqlstr;
    executesql:='alter session disable parallel dml';
    execute immediate executesql;
    j := 1;
    execute immediate 'INSERT  INTO '||q_name||'1 SELECT distinct ' || n_column || ', '||(init_val)||' FROM ' || input_matrix || ' where ' || n_column || ' is not null';
    execute immediate 'INSERT  INTO '||q_name||' SELECT n_column, 1, val FROM '||q_name||'1'; 

    while (j <=  num_features) loop
       if (j = 1) then
            execute immediate 'insert  into  '||p_name||'1  select a.' || m_column || ', sum(cast(a.'|| value_column ||' as binary_double) * '||q_name||'.val) from ' || input_matrix || ' a join '||q_name||' on a.' || n_column || ' = '||q_name||'.n_column and '||q_name||'.m_column = 1 and a.'|| value_column ||' is not null  group by a.'|| m_column ;
        else
            execute immediate 'insert   into '||p_name||'1  select  foo.m_column , foo.val - cast('||beta(j - 1)||'  as binary_double) * '||p_name||'.val  from  (select a.' || m_column || ' as m_column, sum(cast(a.'|| value_column ||' as binary_double) * '||q_name||'.val)  val from ' || input_matrix || ' a join '||q_name||' on a.'||n_column|| ' = '||q_name||'.n_column and '||q_name||'.m_column = '||j||' and a.'|| value_column ||' is not null group by a.'|| m_column ||')  foo join '||p_name||'  on foo.m_column  = '||p_name||'.m_column  and  '||p_name||'.n_column = '||(j- 1);
        end if;
        execute immediate  'select sqrt(sum(cast(val  as binary_double) * val)) from '||p_name||'1 ' into float_temp;
        alpha.extend();
        alpha(j) := float_temp;
      	if alpha(j) = 0
        then 
          beta.extend();
          beta(j) := 0;
      		exit;
        end if;
       execute immediate 'INSERT INTO '||p_name||' SELECT  m_column,'||(j)||', (cast(val as binary_double)/'||alpha(j)||') FROM '||p_name||'1'; 
       execute immediate 'TRUNCATE TABLE '||q_name||'1';
        execute immediate 'insert into '||q_name||'1 select foo.n_column , foo.val - cast('||alpha(j)||'  as binary_double) * '||q_name||'.val from  (select a.' || n_column || 'as n_column,  sum(a.'|| value_column ||' * '||p_name||'.val)   val from ' || input_matrix || ' a join '||p_name||' on a.' || m_column || ' = '||p_name||'.m_column and '||p_name||'.n_column = '||j||' and a.'|| value_column ||' is not null group by a.'|| n_column ||')  foo join '||q_name||'  on foo.n_column  = '||q_name||'.n_column and '||q_name||'.m_column = '||j;
        execute immediate  'select sqrt(sum(cast(val  as binary_double)* val)) from '||q_name||'1 ' into float_temp;
        beta.extend();
        beta(j) := float_temp;
       if beta(j) = 0
         then 
      		exit;
        end if;
        if (j != num_features) then
          execute immediate 'INSERT INTO '||q_name||' SELECT n_column, '||(j + 1)||', (cast(val as binary_double) /'||beta(j)||') FROM '||q_name||'1'; 
      	end if;
        sqlstr := 'TRUNCATE TABLE '||p_name||'1';
        execute immediate sqlstr;
        j := j + 1;
  end loop;
  execute immediate 'drop table '||p_name||'1';
  execute immediate 'drop table '||q_name||'1';
  for i in 1..beta.count() loop
     alpha.extend();
     alpha(alpha.count()) := beta(i);
  end loop;
  return alpha;   
END;
/
