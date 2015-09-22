  CREATE OR REPLACE PROCEDURE PROC_DROPSCHTABLEIFEXISTS(tablename varchar2) is
  BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE ' || tablename;
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
        RAISE;
      END IF;
  END;
/
 CREATE OR REPLACE FUNCTION ALPINE_MINER_SVD(input_matrix        varchar2,
                                             col_name            varchar2,
                                             row_name            varchar2,
                                             value               varchar2,
                                             num_features        int,
                                             ORIGINAL_STEP       float,
                                             SPEEDUP_CONST       float,
                                             FAST_SPEEDUP_CONST  float,
                                             SLOWDOWN_CONST      float,
                                             NUM_ITERATIONS      int,
                                             MIN_NUM_ITERATIONS  int,
                                             MIN_IMPROVEMENT     float,
                                             IMPROVEMENT_REACHED int,
                                             INIT_VALUE          float,
                                             EARLY_TEMINATE      int,
                                             matrix_u            varchar2,
                                             matrix_v            varchar2,
                                             drop_u              int,
                                             drop_v              int)
   RETURN int AS
   ORIGINAL_STEP_ADJUST float := 0;
   error                float := 0;
   old_error            float := 0;
   keep_ind             int := 1;
   SD_ind               int := 1;
   feature_x            float := 0;
   feature_y            float := 0;
   i                    int := 0;
   j                    int := 0;
   cells                int := 0;
   sqlstr               varchar2(32767) := '';
   step                 float := 0;
   imp_reached          int := 0;
   timestr              varchar2(32767) := '';
   executesql           varchar2(1000) := '';
   PRAGMA AUTONOMOUS_TRANSACTION;
 BEGIN
   execute immediate 'SELECT count(distinct ' || col_name || ') AS c FROM ' ||
                     input_matrix || ' where ' || col_name ||
                     ' is not null'
     into feature_x;
   execute immediate 'SELECT count(distinct ' || row_name || ') AS c FROM ' ||
                     input_matrix || ' where ' || row_name ||
                     ' is not null'
     into feature_y;
   execute immediate 'SELECT count(*) AS c FROM ' || input_matrix
     into cells;
   select to_char(sysdate, 'YYYYMMDDhh24miss') into timestr from dual;
   ORIGINAL_STEP_ADJUST := ORIGINAL_STEP / (feature_x + feature_y) /
                           (cells);
   if (drop_u = 1) then
     sqlstr := 'call PROC_DROPSCHTABLEIFEXISTS( ''' || matrix_u || ''')';
     execute immediate sqlstr;
   end if;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE ' || matrix_u ||
             ' parallel as select 1 as "alpine_feature" , ' || col_name || ', ' ||
             value || ' from ' || input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   if (drop_v = 1) then
     sqlstr := 'call PROC_DROPSCHTABLEIFEXISTS(''' || matrix_v || ''')';
     execute immediate sqlstr;
   end if;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE ' || matrix_v || ' parallel as select ' ||
             row_name || ' , 1 as "alpine_feature" , ' || value || ' from ' ||
             input_matrix || ' where 0 =  1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists( ''e' || timestr || '1'')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE e' || timestr || '1  parallel as select ' ||
             row_name || ' as row_num , ' || col_name || ' as col_num , ' ||
             value || ' as val from ' || input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists(  ''e' || timestr || '2'')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE e' || timestr || '2  parallel as select ' ||
             row_name || ' as row_num , ' || col_name || ' as col_num , ' ||
             value || ' as val from ' || input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists(  ''S' || timestr || '1'')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE S' || timestr || '1  parallel as select ' ||
             col_name || ' as col_num , ' || value || ' as val from ' ||
             input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists(  ''S' || timestr || '2'')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE S' || timestr || '2 parallel as select ' ||
             col_name || ' as col_num , ' || value || ' as val from ' ||
             input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists(  ''D' || timestr || '1'')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE D' || timestr || '1  parallel as select ' ||
             row_name || ' as row_num , ' || value || ' as val from ' ||
             input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists(  ''D' || timestr || '2'')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE D' || timestr || '2 parallel as select ' ||
             row_name || ' as row_num , ' || value || ' as val from ' ||
             input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   sqlstr := 'call proc_droptableifexists(  ''e' || timestr || ''')';
   execute immediate sqlstr;
   executesql := 'alter session force parallel dml';
   execute immediate executesql;
   sqlstr := 'CREATE TABLE e' || timestr || ' parallel as select ' ||
             row_name || ' as row_num , ' || col_name || ' as col_num , ' ||
             value || ' as val from ' || input_matrix || ' where 0 = 1';
   execute immediate sqlstr;
   executesql := 'alter session disable parallel dml';
   execute immediate executesql;
   execute immediate 'INSERT INTO e' || timestr || '1 SELECT ' || row_name || ', ' ||
                     col_name || ', ' || value || ' FROM ' || input_matrix;
   j := 1;
   while (j <= num_features) loop
     sqlstr := 'TRUNCATE TABLE S' || timestr || '1';
     execute immediate sqlstr;
     sqlstr := 'TRUNCATE TABLE S' || timestr || '2';
     execute immediate sqlstr;
     sqlstr := 'TRUNCATE TABLE D' || timestr || '1';
     execute immediate sqlstr;
     sqlstr := 'TRUNCATE TABLE D' || timestr || '2';
     execute immediate sqlstr;
     execute immediate 'INSERT INTO S' || timestr || '1 SELECT distinct ' ||
                       col_name || ', ' || (INIT_VALUE) || ' FROM ' ||
                       input_matrix || ' where ' || col_name ||
                       ' is not null';
     execute immediate 'INSERT INTO D' || timestr || '1 SELECT distinct ' ||
                       row_name || ', ' || (INIT_VALUE) || ' FROM ' ||
                       input_matrix || ' where ' || row_name ||
                       ' is not null';
     SD_ind      := 1;
     i           := 0;
     step        := ORIGINAL_STEP_ADJUST;
     imp_reached := 0;
     while (true) loop
       i      := i + 1;
       sqlstr := 'TRUNCATE TABLE e' || timestr;
       execute immediate sqlstr;
       sqlstr := 'INSERT INTO e' || timestr ||
                 ' SELECT a.row_num, a.col_num, a.val-b.val*c.val FROM e' ||
                 timestr || (keep_ind) || '  a, S' || timestr || (SD_ind) ||
                 '  b, D' || timestr || (SD_ind) ||
                 '  c WHERE a.row_num=c.row_num AND a.col_num=b.col_num';
       execute immediate sqlstr;
       old_error := error;
       execute immediate 'SELECT sqrt(sum(val*val)) AS c FROM e' || timestr
         into error;
       if (((abs(error - old_error) < MIN_IMPROVEMENT) and
          (i >= MIN_NUM_ITERATIONS) and
          ((error < MIN_IMPROVEMENT) or (not (IMPROVEMENT_REACHED = 1)) or
          (imp_reached = 1))) or (NUM_ITERATIONS < i)) then
         exit;
       end if;
       if ((abs(error - old_error) >= MIN_IMPROVEMENT) and (old_error > 0)) then
         imp_reached := 1;
       end if;
       if ((error > old_error) and (old_error != 0)) then
         error  := 0;
         step   := step * SLOWDOWN_CONST;
         SD_ind := mod(SD_ind, 2) + 1;
       else
         if (sqrt((error - old_error) * (error - old_error)) <
            .1 * MIN_IMPROVEMENT) then
           step := step * FAST_SPEEDUP_CONST;
         else
           step := step * SPEEDUP_CONST;
         end if;
         execute immediate 'TRUNCATE TABLE S' || timestr ||
                           (mod(SD_ind, 2) + 1);
         execute immediate 'TRUNCATE TABLE D' || timestr ||
                           (mod(SD_ind, 2) + 1);
         execute immediate 'INSERT INTO S' || timestr ||
                           (mod(SD_ind, 2) + 1) ||
                           ' SELECT a.col_num, avg(b.val)+sum(a.val*c.val)*' ||
                           (step) || ' FROM e' || timestr || '  a, S' ||
                           timestr || (SD_ind) || '  b, D' || timestr ||
                           (SD_ind) ||
                           '  c WHERE a.col_num = b.col_num AND a.row_num=c.row_num GROUP BY a.col_num';
         execute immediate 'INSERT INTO D' || timestr ||
                           (mod(SD_ind, 2) + 1) ||
                           ' SELECT a.row_num, avg(c.val)+sum(a.val*b.val)*' ||
                           (step) || ' FROM e' || timestr || '  a, S' ||
                           timestr || (SD_ind) || '  b, D' || timestr ||
                           (SD_ind) ||
                           '  c WHERE a.col_num = b.col_num AND a.row_num=c.row_num GROUP BY a.row_num';
         SD_ind := mod(SD_ind, 2) + 1;
       end if;
     end loop;
     execute immediate 'TRUNCATE TABLE e' || timestr ||
                       (mod(keep_ind, 2) + 1);
     execute immediate 'INSERT INTO e' || timestr || (mod(keep_ind, 2) + 1) ||
                       ' SELECT a.row_num, a.col_num, (a.val-b.val*c.val) FROM e' ||
                       timestr || (keep_ind) || '  a, S' || timestr ||
                       (SD_ind) || '  b, D' || timestr || (SD_ind) ||
                       '  c WHERE a.col_num = b.col_num AND a.row_num=c.row_num';
     keep_ind := mod(keep_ind, 2) + 1;
     execute immediate 'INSERT INTO ' || matrix_u || ' SELECT ' || (j) ||
                       ', col_num, val FROM S' || timestr || (SD_ind);
     execute immediate 'INSERT INTO ' || matrix_v || ' SELECT row_num, ' || (j) ||
                       ', val FROM D' || timestr || (SD_ind);
     if ((error < MIN_IMPROVEMENT) and (EARLY_TEMINATE = 1)) then
       exit;
     end if;
     error := 0;
     j     := j + 1;
   end loop;
   sqlstr := 'call proc_droptableifexists(  ''e' || timestr || '1'')';
   execute immediate sqlstr;
   sqlstr := 'call proc_droptableifexists( ''e' || timestr || '2'')';
   execute immediate sqlstr;
   sqlstr := 'call proc_droptableifexists( ''S' || timestr || '1'')';
   execute immediate sqlstr;
   sqlstr := 'call proc_droptableifexists( ''S' || timestr || '2'')';
   execute immediate sqlstr;
   sqlstr := 'call proc_droptableifexists( ''D' || timestr || '1'')';
   execute immediate sqlstr;
   sqlstr := 'call proc_droptableifexists( ''D' || timestr || '2'')';
   execute immediate sqlstr;
   sqlstr := 'call proc_droptableifexists( ''e' || timestr || ''')';
   execute immediate sqlstr;
   return 1;
 end;
/
