CREATE OR REPLACE FUNCTION ALPINE_MINER_KMEANS_DISTANCE(distancetype  integer,
                                                        column_name   VarcharArray,
                                                        column_number integer,
                                                        k             integer)
  returns clob 
MODIFIES SQL DATA
BEGIN
  DECLARE caculate_array CLOB(255K) default ' ';
  DECLARE temp1          CLOB(255K) default ' ';
  DECLARE temp2          CLOB(255K) default ' ';
  DECLARE temp3          CLOB(255K) default ' ';
  DECLARE  temp4          CLOB(255K) default ' ';
  DECLARE i              integer;
  DECLARE j              integer;

  if distancetype = 1 then
   set  i = 1;
    while i < (k + 1) do
      set j = 1;
     call dbms_lob.append_clob(caculate_array, '(');
      while j < column_number do
       call dbms_lob.append_clob(caculate_array,
                        '(double(x."' || column_name[j] || '")-y."k' || i ||
                        column_name[j] || '")*(double(x."' || column_name[j] ||
                        '")-y."k' || i || column_name[j] || '")+');
       set  j = j + 1;
      end while;
      if i = k then
       call dbms_lob.append_clob(caculate_array,
                        '(double(x."' || column_name[j] || '")-y."k' || i ||
                        column_name[j] || '")*(double(x."' || column_name[j] ||
                        '")-y."k' || i || column_name[j] || '")) as d' ||
                        (i - 1));
      else
       call dbms_lob.append_clob(caculate_array,
                        '(double(x."' || column_name[j] || '")-y."k' || i ||
                        column_name[j] || '")*(double(x."' || column_name[j] ||
                        '")-y."k' || i || column_name[j] || '")) as d' ||
                        (i - 1) || ',');
      end if;
     set i = i + 1;
    end while;
  elseif distancetype = 2 then
    set i = 1;
    while i < (k + 1) do
      set j = 1;
      set temp1 = '(';
      set temp2 = '(';
      while j < column_number do
        set temp1 = temp1 || '(y."k' || i || column_name[j] || '"*ln(double(y."k' || i ||
                 column_name[j] || '")/x."' || column_name[j] || '"))+';
       set  temp2 = temp2 || '(double(y."k' || i || column_name[j] || '")-x."' ||
                 column_name[j] || '")+';
       set  j     = j + 1;
      end while;
     set  temp1 = temp1 || '(y."k' || i || column_name[j] || '"*ln(double(y."k' || i ||
               column_name[j] || '")/x."' || column_name[j] || '")))';
      set temp2 = temp2 || '(double(y."k' || i || column_name[j] || '")-x."' ||
               column_name[j] || '"))';
     set  temp3 = '(' || temp1 || '-' || temp2 || ')';
      if i = k then
       set temp3 = temp3 || 'as d' || (i - 1);
      else
       set  temp3 = temp3 || 'as d' || (i - 1) || ',';
      end if;
      call dbms_lob.append_clob(caculate_array, temp3);
    set   i = i + 1;
    end while;
  elseif distancetype = 3 then
   set  i = 1;
    while i < (k + 1) do
     set  j = 1;
      if (i = 1) then
      set   caculate_array = caculate_array || '(';
      else
       call dbms_lob.append_clob(caculate_array, '(');
      end if;
     while j < column_number do
      call   dbms_lob.append_clob(caculate_array,
                        '(y."k' || i || column_name[j] || '"*(log(double(y."k' || i ||
                        column_name[j] || '")/x."' || column_name[j] ||
                        '")/log(2.0)))+');
       set  j = j + 1;
      end while;
      if i = k then
        call dbms_lob.append_clob(caculate_array,
                        '(y."k' || i || column_name[j] || '"*(log(double(y."k' || i ||
                        column_name[j] || '")/x."' || column_name[j] ||
                        '")/log(2.0)))) as d' || (i - 1));
      else
       call dbms_lob.append_clob(caculate_array,
                        '(y."k' || i || column_name[j] || '"*(log(double(y."k' || i ||
                        column_name[j] || '")/x."' || column_name[j] ||
                        '")/log(2.0)))) as d' || (i - 1) || ',');
      end if;
    set  i = i + 1;
    end while;
  elseif distancetype = 4 then
   set i = 1;
    while i < (k + 1) do
     set j = 1;
      call dbms_lob.append_clob(caculate_array, '(');
      while j < column_number do
      call  dbms_lob.append_clob(caculate_array,
                        '(abs((double(x."' || column_name[j] || '"))-(y."k' || i ||
                        column_name[j] || '"))/abs((x."' || column_name[j] ||
                        '"*1.0)+(y."k' || i || column_name[j] || '")))+');
      set  j = j + 1;
      end while;
      if i = k then
     call   dbms_lob.append_clob(caculate_array,
                        '(abs((double(x."' || column_name[j] || '"))-(y."k' || i ||
                        column_name[j] || '"))/abs((x."' || column_name[j] ||
                        '"*1.0)+(y."k' || i || column_name[j] ||
                        '")))) as d' || (i - 1));
      else
       call  dbms_lob.append_clob(caculate_array,
                        '(abs((double(x."' || column_name[j] || '"))-(y."k' || i ||
                        column_name[j] || '"))/abs((double(x."' || column_name[j] ||
                        '"))+(y."k' || i || column_name[j] ||
                        '")))) as d' || (i - 1) || ',');
      end if;
     set  i = i + 1;
    end while;
  elseif distancetype = 5 then
   set  i = 1;
    while i < (k + 1) do
    set  j = 1;
   call   dbms_lob.append_clob(caculate_array, '(');
      while j < column_number do
       call dbms_lob.append_clob(caculate_array,
                        'abs((double(x."' || column_name[j] || '"))-(y."k' || i ||
                        column_name[j] || '"))+');
      set  j = j + 1;
      end while;
      if i = k then
      call  dbms_lob.append_clob(caculate_array,
                        'abs((double(x."' || column_name[j] || '"))-(y."k' || i ||
                        column_name[j] || '"))) as d' || (i - 1));
      else
      call  dbms_lob.append_clob(caculate_array,
                        'abs((double(x."' || column_name[j] || '"))-(y."k' || i ||
                        column_name[j] || '"))) as d' || (i - 1) || ',');
      end if;
    set  i = i + 1;
    end while;
  elseif distancetype = 6 then
  set   i = 1;
    while i < (k + 1) do
    set   j     = 1;
     set  temp1 = '(';
     set temp2 = '(';
    set  temp3 = '(';
      while j < column_number do
     set   temp1 = temp1 || '(double(x."' || column_name[j] || '")*y."k' || i ||
                 column_name[j] || '")+';
     set   temp2 = temp2 || '(double(x."' || column_name[j] || '")*x."' ||
                 column_name[j] || '")+';
     set   temp3 = temp3 || '(double(y."k' || i || column_name[j] || '")*y."k' || i ||
                 column_name[j] || '")+';
     set   j     = j + 1;
      end while;
     set  temp1 = temp1 || '(double(x."' || column_name[j] || '")*y."k' || i ||
               column_name[j] || '"))';
     set  temp2 = temp2 || '(double(x."' || column_name[j] || '")*x."' ||
               column_name[j] || '"))';
      set temp3 = temp3 || '(double(y."k' || i || column_name[j] || '")*y."k' || i ||
               column_name[j] || '"))';
      if i = k then
     set    temp4 = 'acos(case when (' || temp1 || '/(sqrt(' || temp2 ||
                 ')*sqrt(' || temp3 || ')))>1 then 1 when (' || temp1 ||
                 '/(sqrt(' || temp2 || ')*sqrt(' || temp3 ||
                 ')))<-1 then -1 else (' || temp1 || '/(sqrt(' || temp2 ||
                 ')*sqrt(' || temp3 || '))) end ) as d' || (i - 1);
      else
      set  temp4 = 'acos(case when (' || temp1 || '/(sqrt(' || temp2 ||
                 ')*sqrt(' || temp3 || ')))>1 then 1 when (' || temp1 ||
                 '/(sqrt(' || temp2 || ')*sqrt(' || temp3 ||
                 ')))<-1 then -1 else (' || temp1 || '/(sqrt(' || temp2 ||
                 ')*sqrt(' || temp3 || '))) end ) as d' || (i - 1) || ',';
      end if;
     call dbms_lob.append_clob(caculate_array, temp4);
     set  i = i + 1;
    end while;
  elseif distancetype = 7 then
    set i = 1;
    while i < (k + 1) do
     set  j     = 1;
     set  temp1 = '(';
     set  temp2 = '(';
     set temp3 = '(';
      while j < column_number do
      set  temp1 = temp1 || '(double(x."' || column_name[j] || '"))+';
      set   temp2 = temp2 || '(double(y."k' || i || column_name[j] || '"))+';
      set  temp3 = temp3 || '(double(x."' || column_name[j] || '")*y."k' || i ||
                 column_name[j] || '")+';
      set  j     = j + 1;
      end while;
     set temp1 = temp1 || '(x."' || column_name[j] || '"))';
     set temp2 = temp2 || '(y."k' || i || column_name[j] || '"))';
     set  temp3 = temp3 || '(x."' || column_name[j] || '"*y."k' || i ||
               column_name[j] || '"))';
      if i = k then
       set  temp4 = '(-2*' || temp3 || '/(' || temp1 || '+' || temp2 ||
                 ')) as d' || (i - 1);
      else
      set  temp4 = '(-2*' || temp3 || '/(' || temp1 || '+' || temp2 ||
                 ')) as d' || (i - 1) || ',';
      end if;
     call dbms_lob.append_clob(caculate_array, temp4);
    set  i = i + 1;
    end while;
  elseif distancetype = 8 then
   set i = 1;
    while i < (k + 1) do
    set  j = 1;
    call  dbms_lob.append_clob(caculate_array, '-(');
      while j < column_number do
     call   dbms_lob.append_clob(caculate_array,
                        '(double(x."' || column_name[j] || '")*y."k' || i ||
                        column_name[j] || '")+');
   set   j = j + 1;
      end while;
      if i = k then
     call   dbms_lob.append_clob(caculate_array,
                        '(double(x."' || column_name[j] || '")*y."k' || i ||
                        column_name[j] || '")) as d' || (i - 1));
      else
     call   dbms_lob.append_clob(caculate_array,
                        '(double(x."' || column_name[j] || '")*y."k' || i ||
                        column_name[j] || '")) as d' || (i - 1) || ',');
      end if;
     call dbms_lob.append_clob(caculate_array, temp4);
     set  i = i + 1;
    end while;
  elseif distancetype = 9 then
   set i = 1;
    while i < (k + 1) do
    set  j     = 1;
     set temp1 = '(';
    set  temp2 = '(';
    set  temp3 = '(';
      while j < column_number do
       set  temp1 = temp1 || '(double(x."' || column_name[j] || '"))+';
       set  temp2 = temp2 || '(double(y."k' || i || column_name[j] || '"))+';
       set  temp3 = temp3 || '(double(x."' || column_name[j] || '")*y."k' || i ||
                 column_name[j] || '")+';
       set  j  = j + 1;
      end while;
      set temp1 = temp1 || '(x."' || column_name[j] || '"))';
      set temp2 = temp2 || '(y."k' || i || column_name[j] || '"))';
      set temp3 = temp3 || '(x."' || column_name[j] || '"*y."k' || i ||
               column_name[j] || '"))';
      if i = k then
      set   temp4 = '(-' || temp3 || '/(' || temp1 || '+' || temp2 || '-' ||
                 temp3 || ')) as d' || (i - 1);
      else
      set   temp4 = '(-' || temp3 || '/(' || temp1 || '+' || temp2 || '-' ||
                 temp3 || ')) as d' || (i - 1) || ',';
      end if;
     call  dbms_lob.append_clob(caculate_array, temp4);
     set i = i + 1;
    end while;
  end if;

  return caculate_array ;
end@




CREATE or replace PROCEDURE   alpine_miner_kmeans(
table_name               varchar(1000),
table_name_withoutschema varchar(1000),
column_number            integer,
id                       varchar(1000),
tempid                  varchar(1000),
clustername             varchar(1000),
k                        integer,
max_run                  integer,
max_iter                 integer,
distance                 integer,
column_name              VarcharArray,
OUT result Floatarray )
 LANGUAGE SQL
BEGIN
DECLARE  temptablename varchar(1000);
DECLARE  executesql         CLOB(255K) default ' ' ;
DECLARE  selectsql            CLOB(255K) default ' ' ;
DECLARE  insertsql            CLOB(255K) default ' ' ;
DECLARE  column_array     CLOB(255K) default ' ' ;
DECLARE  avg_array          CLOB(255K) default ' ' ;
DECLARE   caculate_array  CLOB(255K) default ' ' ;
DECLARE  d_array             CLOB(255K) default ' ' ;
DECLARE  x_array             CLOB(255K) default ' ' ;
DECLARE  d_array1           CLOB(255K) default ' ' ;
DECLARE  column_all         CLOB(255K) default ' ' ;
DECLARE  columnname      CLOB(255K) default ' ' ;
DECLARE  comp_sql          CLOB(255K) default ' ' ;
DECLARE  alpine_id           varchar(30);
DECLARE   result1             varchar(30);
DECLARE  i                        integer default 0;
DECLARE  j                        integer default 0;
DECLARE  none_stable        integer default 0;
DECLARE tmp_res_1  varchar(30);
DECLARE tmp_res_2  varchar(30);
DECLARE tmp_res_3  varchar(30);
DECLARE tmp_res_4  varchar(30);
DECLARE run                    integer default 1;
DECLARE  tempint             integer default 1;
DECLARE sampleid integer default 0;
DECLARE nullflag   integer default 0;
DECLARE tempsum  float ;

DECLARE stmt1 STATEMENT;
DECLARE stmt2 STATEMENT;
DECLARE curs1 CURSOR FOR stmt1;
DECLARE curs2 CURSOR FOR stmt2;
--commit;
set temptablename = table_name_withoutschema;

call PROC_DROPTEMPTABLEIFEXISTS(temptablename||'copy');

  if id = 'null' then
   set selectsql =  ' select ' || table_name ||
                  '.*,ROWNUMBER() over () ' || tempid ||
                  ' from ' || table_name || ' where ';
    set alpine_id  = tempid;
  else
    set selectsql =' select * from ' || table_name ||
                  ' where ';
    set alpine_id = id;
  end if;

 set i = 1;
  while i < (column_number) do
   call dbms_lob.append_clob(selectsql,
                    ' "' || column_name[i] || '" is not null and ');
   set i = i + 1;
  end while;
 call dbms_lob.append_clob(selectsql, ' "' || column_name[i] || '" is not null');

call dbms_lob.append_clob(executesql,'create table ' || temptablename ||'copy as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||'copy';
call dbms_lob.append_clob(insertsql,selectsql);

execute immediate insertsql;
commit;
-------create random table begin---------

 set i = 2;
  while i < (column_number + 1) do
   set  column_array = column_array || ',"' || column_name[i] || '"';
   set i  = i + 1;
  end while;
  set executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename ||
                '_random_new'')';
  execute immediate executesql;

 set selectsql = ' select tablek1.seq sample_id,0 as stable,';
 set  column_all = '  ';
  set i = 1;
  while i < (k + 1) do
  set   j = 1;
    while j < (column_number + 1) do
   call  dbms_lob.append_clob(column_all,
                      'trunc("k' || i || '' || column_name[j] || '",10) "k' || i || '' ||
                      column_name[j] || '",');
      set j = j + 1;
    end while;
  set  i = i + 1;
  end while;
call  dbms_lob.append_clob(selectsql, column_all || '0 as iter from ');
 set  i = 1;
  while i < (k + 1) do
    call dbms_lob.append_clob(selectsql, '(select ');
   set j = 1;
    while j < (column_number + 1) do
    call  dbms_lob.append_clob(selectsql,
                      '"' || column_name[j] || '" "k' || i ||
                      column_name[j] || '",');
     set j = j + 1;
    end while;
    if i = 1 then
   call   dbms_lob.append_clob(selectsql,
                      ' ROWNUMBER() over () -1 as seq from (select ' ||
                      temptablename || 'copy.* from ' || temptablename ||
                      'copy  order by rand() ) fetch first ' || (max_run) || ' rows only ) tablek' || i ||
                      ' inner join ');
    else
      if i = k then
     call   dbms_lob.append_clob(selectsql,
                      ' ROWNUMBER() over () -1 as seq from (select ' ||
                      temptablename || 'copy.* from ' || temptablename ||
                      'copy  order by rand() ) fetch first ' || (max_run) || ' rows only ) tablek' || i ||
                        ' on tablek' || (i - 1) || '.seq=tablek' || i ||
                        '.seq');
      else
    call    dbms_lob.append_clob(selectsql,
                      ' ROWNUMBER() over () -1 as seq from (select ' ||
                      temptablename || 'copy.* from ' || temptablename ||
                      'copy  order by rand() ) fetch first ' || (max_run) || ' rows only )  tablek' || i ||
                        ' on tablek' || (i - 1) || '.seq=tablek' || i ||
                        '.seq inner join ');
      end if;
    end if;
    set i = i + 1;
  end while;

set executesql =' ';


call dbms_lob.append_clob(executesql,'create table ' || temptablename ||'_random_new as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;
SET insertsql = 'insert into  ' || temptablename ||'_random_new';
call dbms_lob.append_clob(insertsql,selectsql);

execute immediate insertsql;
commit;
-------create random table end------------
outloop:
-------loop begin---------
 while run <= max_iter do
    set tmp_res_1 = to_char('tmp_res_1' || run);
    set tmp_res_2 = to_char('tmp_res_2' || run);
    set tmp_res_3 = to_char('tmp_res_3' || run);
    set tmp_res_4 = to_char('tmp_res_4' || run);

    set i = 2;
    set avg_array = 'trunc(avg(double("' || column_name[1] || '")),10) "' ||
                 column_name[1] || '"';
    while i < (column_number + 1) do
      set avg_array = avg_array || ',trunc(avg(double("' || column_name[i] ||
                   '")),10) "' || column_name[i] || '"';
      set i = i + 1;
    end while;


    set i = 0;
    set j = 0;
    set d_array  = 'case ';
    set d_array1 = 'case ';
    while i < k - 1 do
      set j = i + 1;
      set d_array  = d_array || ' when d' || i || '<=d' || j;
      set d_array1 = d_array1 || ' when d' || i || '<=d' || j;
      set j = j + 1;
      while j < k do
        set d_array  = d_array || ' and d' || i || '<=d' || j;
        set d_array1 = d_array1 || ' and d' || i || '<=d' || j;
        set j = j + 1;
      end while;
      set d_array  = d_array || ' then ' || i;
      set d_array1 = d_array1 || ' then d' || i;
      set i = i + 1;
    end while;
    set d_array  = d_array || ' else ' || (k - 1) || ' end';
    set d_array1= d_array1 || ' else d' || (k - 1) || ' end';


 set caculate_array = ' ';
  set  columnname  = 'array[';
   set  i = 1;
    while i < column_number do
      set columnname = columnname || '''' || column_name[i] || ''',';
      set i  = i + 1;
    end while;
    set columnname = columnname || '''' || column_name[i] || ''']';
    set caculate_array = ALPINE_MINER_KMEANS_DISTANCE(distance,column_name , column_number , k);

          -----------create table2 begin---------------------
   set executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename ||tmp_res_2 || ''')';
    execute immediate executesql;
    set selectsql =' select sample_id,' || alpine_id || ', ' ||
                  d_array || ' as cluster_id from ( select sample_id,' ||
                  alpine_id || ', ' || caculate_array || ' from ' ||
                  temptablename || 'copy x inner join ' || temptablename ||
                  '_random_new y on y.stable=0) foo';
 set executesql='';
call dbms_lob.append_clob(executesql,'create table ' || temptablename ||tmp_res_2 ||' as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');
execute immediate executesql;
commit;
SET insertsql = 'insert into  ' || temptablename ||tmp_res_2;
call dbms_lob.append_clob(insertsql,selectsql);
execute immediate insertsql;
commit;
-----------create table2 end---------------------

-----------create table1 begin---------------------
   set  executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename || tmp_res_1 || ''')';
   execute immediate executesql;
    set selectsql = ' select sample_id, cluster_id, ' ||
                  avg_array || ' from ' || temptablename || tmp_res_2 ||
                  ' x,' || temptablename || 'copy y where x.' || alpine_id ||
                  '=y.' || alpine_id || ' group by sample_id,cluster_id ';
 set executesql='';

call dbms_lob.append_clob(executesql,'create table ' || temptablename ||tmp_res_1 ||' as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||tmp_res_1;
call dbms_lob.append_clob(insertsql,selectsql);

execute immediate insertsql;
commit;

 -----------create table1 end---------------------
  set comp_sql = '(case when ';
   set  i = 1;
    while i < (k + 1) do
      set j = 1;
      while j < column_number + 1 do
        if i = k and j = column_number then
          set comp_sql = comp_sql || 'x."k' || i || column_name[j] || '"=y."k' || i ||
                      column_name[j] || '"';
        else
          set comp_sql = comp_sql || 'x."k' || i || column_name[j] || '"=y."k' || i ||
                      column_name[j] || '" and ';
        end if;
        set j = j + 1;
      end while;
      set i = i + 1;
    end while;
    set comp_sql   = comp_sql || ' then 1 else 0 end ) as stable';

-----------create table4 begin---------------------
    set executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename || tmp_res_4 || ''')';
    execute immediate executesql;

    set selectsql = ' select tablek1.sample_id,0 as stable,';
    set column_all = ' ';
    set i = 1;
    while i < (k + 1) do
     set  j = 1;
      while j < (column_number + 1) do
       set  column_all = column_all || '"k' || i || column_name[j] || '",';
       set  j = j + 1;
      end while;
      set i = i + 1;
    end while;
    call dbms_lob.append_clob(selectsql, column_all || ' 0 as iter from ');
    set i = 1;
    while i < (k + 1) do
    call  dbms_lob.append_clob(selectsql, '(select ');
     set  j = 1;
      while j < (column_number + 1) do
      call  dbms_lob.append_clob(selectsql,
                        '"' || column_name[j] || '" "k' || i ||
                        column_name[j] || '",');
       set j = j + 1;
      end while;
      if i = 1 then
       call dbms_lob.append_clob(selectsql,
                        ' sample_id from ' || temptablename || tmp_res_1 ||
                        ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                        ' inner join ');
      else
        if i = k then
        call  dbms_lob.append_clob(selectsql,
                          ' sample_id from ' || temptablename || tmp_res_1 ||
                          ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                          ' on tablek' || (i - 1) || '.sample_id=tablek' || i ||
                          '.sample_id');
        else
         call dbms_lob.append_clob(selectsql,
                          ' sample_id from ' || temptablename || tmp_res_1 ||
                          ' where cluster_id=' || (i - 1) || ')  tablek' || i ||
                          ' on tablek' || (i - 1) || '.sample_id=tablek' || i ||
                          '.sample_id inner join ');
        end if;
      end if;
     set i = i + 1;
    end while;
   set  executesql = 'create table ' || temptablename || tmp_res_4 ||
                  ' as (' || selectsql || ' ) definition only ';
    execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||tmp_res_4;
call dbms_lob.append_clob(insertsql,selectsql);
execute immediate insertsql;
commit;
-----------create table4 end---------------------

-----------create table3 begin---------------------
   set x_array = '';
   set  i = 1;
    while i < (k + 1) do
      set j = 1;
      while j < (column_number + 1) do
        set x_array = x_array || 'x."k' || i || column_name[j] || '",';
        set j = j + 1;
      end while;
      set i = i + 1;
    end while;
   set  executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename ||tmp_res_3 || ''')';
    execute immediate executesql;

    set selectsql = ' select x.sample_id, ' || comp_sql || ',' ||
                  x_array || run || ' as iter from  ' || temptablename ||
                  tmp_res_4 || ' x, ' || temptablename ||
                  '_random_new  y where x.sample_id=y.sample_id  ';
set executesql=' ';

call dbms_lob.append_clob(executesql,'create table ' || temptablename ||tmp_res_3 ||' as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||tmp_res_3;
call dbms_lob.append_clob(insertsql,selectsql);
execute immediate insertsql;
commit;

    set insertsql = 'insert into ' || temptablename || tmp_res_3 ||
                  ' (select a.* from ' || temptablename ||
                  '_random_new a left join ' || temptablename || tmp_res_3 ||
                  ' b on a.sample_id=b.sample_id ';
    call dbms_lob.append_clob(insertsql, ' where b.sample_id is null)');
    execute immediate insertsql;
commit;
    set executesql = 'drop table ' || temptablename || '_random_new';
    execute immediate executesql;
    set executesql = 'rename table ' || temptablename || tmp_res_3 ||
                  ' to ' || temptablename || '_random_new';

    execute immediate executesql;
    set executesql ='select count(*)  from  ' || temptablename ||
                      '_random_new where stable=0';
    PREPARE stmt2 FROM executesql;
    OPEN curs2;
    FETCH FROM curs2 INTO none_stable;
    CLOSE curs2;


   if none_stable = 0 then
     leave outloop;
    end if;
    set run = run + 1;
-----------create table3 end---------------------

end while;
-------loop end---------

-----drop temp table start--------------
set i=1;
 while i < (run+1) do
    set tmp_res_1 = to_char('tmp_res_1' || i);
   call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ(temptablename ||tmp_res_1);
    set tmp_res_2 = to_char('tmp_res_2' || i);
  call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ(temptablename ||tmp_res_2);
    set tmp_res_3 = to_char('tmp_res_3' || i);
  call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ(temptablename ||tmp_res_3);
    set tmp_res_4 = to_char('tmp_res_4' || i);
  call PROC_DROPTABLEIFEXISTSWITHOUTDOUBLEQ(temptablename ||tmp_res_4);
set i = i + 1;
end while;
-----drop temp table end--------------


set executesql ='select count(*) from ' || temptablename ||'_random_new y where y.stable=1';

    PREPARE stmt2 FROM executesql;
    OPEN curs2;
    FETCH FROM curs2 INTO tempint;
    CLOSE curs2;

  if tempint <> 0 then
  set executesql = 'select sample_id,len from ( select sample_id,len,ROWNUMBER() over(order by len) as seq from ( select sample_id,avg(double(len)) as len from ( select sample_id,' ||
                alpine_id || ', ' || d_array1 ||
                ' as len from ( select sample_id, ' || alpine_id || ', ' ||
                caculate_array || ' from ' || temptablename ||
                'copy x inner join ' || temptablename ||
                '_random_new y on y.stable=1 ) as t ) as a group by sample_id ) as b ) as z where seq=1';
    PREPARE stmt2 FROM executesql;
    OPEN curs2;
    FETCH FROM curs2 INTO result[1],result[2];
    CLOSE curs2;

   set sampleid = result[1];
  else
    set result[1]=0;
    set result[2]=0;
  end if;

  if sampleid is null then
    set sampleid = 0;
    set nullflag = 1;
  end if;

  set result1 = 'result1';
  set executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename ||result1 || ''')';

  execute immediate executesql;

  set selectsql = ' select * from  ' || temptablename ||
                    '_random_new  where sample_id =' || sampleid;

set executesql=' ';
call dbms_lob.append_clob(executesql,'create table ' || temptablename ||result1 ||' as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||result1;
call dbms_lob.append_clob(insertsql,selectsql);
execute immediate insertsql;
commit;


  if nullflag = 1 then
    set executesql = 'select len from ( select sample_id,len,ROWNUMBER() over(order by len) as seq from ( select sample_id,avg(double(len)) as len from ( select sample_id,' ||
                  alpine_id || ', ' || d_array1 ||
                  ' as len from ( select sample_id, ' || alpine_id || ', ' ||
                  caculate_array || ' from ' || temptablename ||
                  'copy x inner join ' || temptablename ||
                  '_random_new y on y.stable=0 ) as t )as a group by sample_id ) as b ) as z where seq=1';

    PREPARE stmt2 FROM executesql;
    OPEN curs2;
    FETCH FROM curs2 INTO tempsum;
    CLOSE curs2;
   set  result[2] = tempsum;
  end if;


  set executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename ||'result2'')';
  execute immediate executesql;

  set selectsql = ' select ' || temptablename ||
                    'copy.*,0 ' || temptablename || 'copy_flag from ' ||
                    temptablename || 'copy';

set executesql=' ';
call dbms_lob.append_clob(executesql,'create table ' || temptablename ||'result2 as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||'result2';
call dbms_lob.append_clob(insertsql,selectsql);
execute immediate insertsql;
commit;


 set  executesql = 'call PROC_DROPTEMPTABLEIFEXISTS(''' || temptablename ||
                'table_name_temp'')';
  execute immediate executesql;


  set selectsql = 'select ' || alpine_id || ' as temp_id,' || d_array ||
                ' as ' || clustername || ' from ( select x.' || alpine_id || ',' ||
                caculate_array || ' from ' || temptablename ||
                'result2 x inner join ' || temptablename || result1 ||
                ' y on x.' || temptablename || 'copy_flag=0 ) foo ';

set executesql=' ';
call dbms_lob.append_clob(executesql,'create table ' || temptablename ||'table_name_temp as(');

call dbms_lob.append_clob(executesql,selectsql);

call dbms_lob.append_clob(executesql,') definition only ');

execute immediate executesql;
commit;

SET insertsql = 'insert into  ' || temptablename ||'table_name_temp ';
call dbms_lob.append_clob(insertsql,selectsql);
execute immediate insertsql;
commit;

set result[1] = run;

return 0;
end@

