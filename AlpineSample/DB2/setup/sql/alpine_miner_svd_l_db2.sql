create or replace PROCEDURE alpine_miner_svd_l_proc(input_matrix varchar(32672),
                                              p_name       varchar(32672),
                                              q_name       varchar(32672),
                                              m_column     varchar(32672),
                                              n_column     varchar(32672),
                                              value_column varchar(32672),
                                              num_features integer,
                                              init_val     double precision,
                                              out alpha floatarray  )
BEGIN
declare  i          int default 0;
declare  j          int default 0;
declare  sqlstr     varchar(32672);
declare  float_temp double default 0.0;
declare  beta       floatarray ;
declare  executesql varchar(32672) default '';
DECLARE execStr VARCHAR(4000);
DECLARE stmt STATEMENT;
DECLARE curs CURSOR FOR stmt;
    

  set sqlstr = 'call PROC_DROPTABLEIFEXISTS( ''' || p_name || ''')';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'call PROC_DROPTABLEIFEXISTS( ''' || q_name || ''')';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'call PROC_DROPTABLEIFEXISTS( ''' || p_name || '1'')';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'call PROC_DROPTABLEIFEXISTS( ''' || q_name || '1'')';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'CREATE  TABLE ' || p_name || '  as (select ' ||
            m_column || ' as m_column , 1 as n_column, ' || 'double(1.0)' ||
            ' as val from ' || input_matrix || ') definition only';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'CREATE TABLE ' || q_name || '  as (select ' ||
            n_column || ' as n_column , 1 as m_column, ' || 'double(1.0)' ||
            ' as val from ' || input_matrix || ') definition only';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'CREATE TABLE ' || p_name || '1  as (select ' ||
            m_column || ' as m_column , ' || 'double(1.0)' ||
            ' as val from ' || input_matrix || ') definition only';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'CREATE TABLE ' || q_name || '1   as (select ' ||
            n_column || ' as n_column , ' || 'double(1.0)' ||
            ' as val from ' || input_matrix || ') definition only';
  execute immediate sqlstr;
  commit;
  set executesql = 'INSERT  INTO ' || q_name || '1 SELECT distinct ' ||
                    n_column || ', ' || (init_val) || ' FROM ' ||
                    input_matrix || ' where ' || n_column || ' is not null';
  execute immediate executesql;
  commit;
  set executesql = 'INSERT  INTO ' || q_name ||
                    ' SELECT n_column, 1, val FROM ' || q_name || '1';
  execute immediate executesql;
  commit;

  set j = 1;
LOOP_LABEL:
	while (j <= num_features) DO
    if (j = 1) then
      set executesql = 'insert  into  ' || p_name || '1  select a.' ||
                        m_column || ', sum(double(a.' || value_column || ') * ' ||
                        q_name || '.val) from ' || input_matrix ||
                        ' a join ' || q_name || ' on a.' || n_column ||
                        ' = ' || q_name || '.n_column and ' || q_name ||
                        '.m_column = 1 and a.' || value_column || ' is not null group by a.' || m_column ;
      execute immediate executesql;
      commit;
    else
      set executesql = 'insert   into ' || p_name ||
                        '1  select  foo.m_column , foo.val - double(' ||
                        beta[j - 1] || ') * ' || p_name ||
                        '.val  from  (select a.' || m_column ||
                        ' as m_column, sum(double(a.' || value_column || ') * ' ||
                        q_name || '.val)  val from ' || input_matrix ||
                        ' a join ' || q_name || ' on a.' || n_column ||
                        ' = ' || q_name || '.n_column and ' || q_name ||
                        '.m_column = ' || j || '  and a.' || value_column || ' is not null group by a.' || m_column ||
                        ')  foo join ' || p_name || '  on foo.m_column  = ' ||
                        p_name || '.m_column  and  ' || p_name ||
                        '.n_column = ' || (j - 1);
      execute immediate executesql;
      commit;
    end if;
        SET execStr = 'select sqrt(sum(double(val) * double(val))) from ' || COALESCE(p_name, '') || '1 ';
        PREPARE stmt FROM execStr;
        OPEN curs;
        FETCH FROM curs INTO float_temp;
        CLOSE curs;
    set alpha[j] = float_temp;
    if alpha[j] = 0 then
      set beta[j] = 0;
      leave LOOP_LABEL;
    end if;
    set executesql = 'INSERT INTO ' || p_name || ' SELECT  m_column,' || j ||
                      ', (1.0*double(val)/' || alpha[j] || ') FROM ' || p_name || '1';
    execute immediate executesql;
    commit;
    set executesql = 'delete from ' || q_name || '1 ';
    execute immediate executesql;
    commit;
    set executesql = 'insert into ' || q_name ||
                      '1 select foo.n_column , foo.val - double(' || alpha[j] ||
                      ') * ' || q_name || '.val from  (select a.' ||
                      n_column || 'as n_column,  sum(double(a.' || value_column ||
                      ') * ' || p_name || '.val)   val from ' ||
                      input_matrix || ' a join ' || p_name || ' on a.' ||
                      m_column || ' = ' || p_name || '.m_column and ' ||
                      p_name || '.n_column = ' || j || '  and a.' || value_column || ' is not null group by a.' ||
                      n_column || ')  foo join ' || q_name ||
                      '  on foo.n_column  = ' || q_name || '.n_column and ' ||
                      q_name || '.m_column = ' || j;
    execute immediate executesql;
    commit;
        SET execStr = 'select sqrt(sum(double(val) * double(val))) from ' || COALESCE(q_name, '') || '1 ';
        PREPARE stmt FROM execStr;
        OPEN curs;
        FETCH FROM curs INTO float_temp;
        CLOSE curs;
    set beta[j] = float_temp;
    if beta[j] = 0 then
            LEAVE LOOP_LABEL;
    end if;
    
    if (j != num_features) then
    set executesql = 'INSERT INTO ' || q_name || ' SELECT n_column, ' ||
                        (j + 1) || ', (1.0*double(val)/' || beta[j] || ') FROM ' ||
                        q_name || '1';
      execute immediate executesql;
      commit;
    end if;
    set sqlstr = 'delete from ' || p_name || '1 ';
    execute immediate sqlstr;
    commit;
    SET j = j + 1;
  end WHILE;

  SET sqlstr = 'call PROC_DROPTABLEIFEXISTS( ''' || p_name || '1'')';
  execute immediate sqlstr;
  commit;
  SET sqlstr = 'call PROC_DROPTABLEIFEXISTS( ''' || q_name || '1'')';
  execute immediate sqlstr;
  commit;
  set i = 1;
  while(i <= cardinality(beta)) do
    set alpha[cardinality(alpha) + 1] = beta[i];
    set i = i + 1;
  end while;
END@

