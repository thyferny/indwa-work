CREATE OR REPLACE FUNCTION alpine_miner_rf_inittra(schemaname   varchar2,
                                                         tablename    varchar2,
                                                         stamp        varchar2,
                                                         dependcolumn varchar2 )
  RETURN integer is
  
  
  rownumber   integer;
  
  peoso       binary_double;
 
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  commit;
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."pnew' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."dn' || stamp || '"'')';
  execute immediate 'alter session force parallel dml';
  execute immediate 'Create  table ' || schemaname || '."dn' || stamp ||
                    '" parallel  as (select ' || schemaname || '.' ||
                    tablename || '.* from ' || schemaname || '.' ||
                    tablename || ' where ' || dependcolumn ||
                    ' is not null)';
  execute immediate 'alter session disable parallel dml';
  execute immediate 'select count(*)   from ' || schemaname || '."dn' ||
                    stamp || '"'
    into rownumber;
  peoso := 1.0 / rownumber;
  execute immediate 'Create  table ' || schemaname || '."pnew' || stamp ||
                    '"    as (select ' || schemaname || '."dn' || stamp ||
                    '".*, row_number()over(order by 1) "alpine_randomforest_id", ' ||
                    peoso || ' "alpine_randomforest_peoso", rownum*' || peoso ||
                    ' "alpine_randomforest_totalpeoso" from ' || schemaname ||
                    '."dn' || stamp || '") ';

  RETURN rownumber;
end alpine_miner_rf_inittra;

/

CREATE OR REPLACE FUNCTION alpine_miner_rf_sample(schemaname varchar2,
                                                        tablename  varchar2,
                                                        stamp      varchar2,
                                                        partsize   integer)
  RETURN varchar2 is
  tempstring varchar2(32767);
  rownumber  integer;
  TYPE crt IS REF CURSOR;
  myrecord   crt;
  partnumber integer;
  splitpeoso Numberarray := Numberarray();
  maxpeoso   number;
  temppeoso  number;
  i          integer;
  executesql varchar2(32767);
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  commit;
  execute immediate 'select count(*) from ' || schemaname || '.' ||
                    tablename
    into rownumber;
  execute immediate ' select max("alpine_randomforest_totalpeoso")  from ' ||
                    schemaname || '.' || tablename
    into maxpeoso;
  if partsize >= rownumber then
    partnumber := 1;
  else
    if mod(rownumber, partsize) = 0 then
      partnumber := rownumber / partsize;
    else
      partnumber := trunc(rownumber / partsize) + 1;
    end if;
  end if;
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."s' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."r' || stamp || '"'')';
  execute immediate 'create table ' || schemaname || '."r' || stamp ||
                    '"      as select SYS.DBMS_RANDOM.VALUE(0,' || maxpeoso ||
                    ')  "alpine_miner_randomforest_r" from ' || schemaname || '.' ||
                    tablename || ' order by SYS.DBMS_RANDOM.VALUE(0,' ||
                    maxpeoso || ')';
  if partnumber = 1 then
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    execute immediate 'create table ' || schemaname || '."s' || stamp ||
                      '"  parallel  as select * from ' || schemaname || '.' ||
                      tablename || ' join ' || schemaname || '."r' || stamp ||
                      '" on ' || schemaname || '.' || tablename ||
                      '."alpine_randomforest_totalpeoso"  >= ' || schemaname ||
                      '."r' || stamp || '"."alpine_miner_randomforest_r" and ' ||
                      schemaname || '.' || tablename ||
                      '."alpine_randomforest_peoso" > (' || schemaname || '.' ||
                      tablename || '."alpine_randomforest_totalpeoso"-' ||
                      schemaname || '."r' || stamp ||
                      '"."alpine_miner_randomforest_r")';
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    commit;
  else
    tempstring := ' select "alpine_randomforest_totalpeoso" as peoso from ' ||
                  schemaname || '.' || tablename ||
                  ' where mod("alpine_randomforest_id",' || partsize ||
                  ')=0 order by peoso';
    i          := 1;
    splitpeoso.extend();
    splitpeoso(i) := 0;
    open myrecord for tempstring;
    loop
      FETCH myrecord
        INTO temppeoso;
      EXIT WHEN myrecord%NOTFOUND;
      i := i + 1;
      splitpeoso.extend();
      splitpeoso(i) := temppeoso;
    end loop;
    if splitpeoso(i) != maxpeoso then
      i := i + 1;
      splitpeoso.extend();
      splitpeoso(i) := maxpeoso;
    end if;
    i          := 1;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    tempstring := 'create table ' || schemaname || '."s' || stamp ||
                  '"  parallel as select * from  ( select * from ' ||
                  schemaname || '.' || tablename ||
                  ' where "alpine_randomforest_totalpeoso">' || splitpeoso(i) ||
                  ' and  "alpine_randomforest_totalpeoso"<=' ||
                  splitpeoso(i + 1) || ')   foo' || i ||
                  ' join (select * from "r' || stamp ||
                  '" where "alpine_miner_randomforest_r" >' || splitpeoso(i) ||
                  ' and  "alpine_miner_randomforest_r"<=' || splitpeoso(i + 1) ||
                  ')   foor' || i || ' on foo' || i ||
                  '."alpine_randomforest_totalpeoso" >=foor' || i ||
                  '."alpine_miner_randomforest_r" and foo' || i ||
                  '."alpine_randomforest_peoso" > (foo' || i ||
                  '."alpine_randomforest_totalpeoso"-foor' || i ||
                  '."alpine_miner_randomforest_r") ';
    execute immediate tempstring;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    commit;
    for i in 2 .. partnumber loop
      tempstring := '  insert into  ' || schemaname || '."s' || stamp ||
                    '"   select * from ( select * from ' || schemaname || '.' ||
                    tablename || ' where "alpine_randomforest_totalpeoso">' ||
                    splitpeoso(i) || ' and  "alpine_randomforest_totalpeoso"<=' ||
                    splitpeoso(i + 1) || ')   foo' || i ||
                    ' join (select * from "r' || stamp ||
                    '" where "alpine_miner_randomforest_r" >' || splitpeoso(i) ||
                    ' and "alpine_miner_randomforest_r"<=' || splitpeoso(i + 1) ||
                    ')   foor' || i || ' on foo' || i ||
                    '."alpine_randomforest_totalpeoso" >=foor' || i ||
                    '."alpine_miner_randomforest_r" and foo' || i ||
                    '."alpine_randomforest_peoso" > (foo' || i ||
                    '."alpine_randomforest_totalpeoso"-foor' || i ||
                    '."alpine_miner_randomforest_r") ';
      execute immediate 'select count(*) from ' || schemaname || '."s' ||
                        stamp || '"'
        into rownumber;
      execute immediate tempstring;
    end loop;
  end if;
  tempstring := 's' || stamp;
  commit;
  RETURN tempstring;
end alpine_miner_rf_sample;



/

CREATE OR REPLACE FUNCTION alpine_miner_rf_initpre(schemaname   varchar2,
                                                         tablename    varchar2,
                                                         stamp        varchar2,
                                                         infor        varchar2array)
  RETURN integer is
  tempstring clob;
  i          integer := 0;
  idtable    varchar2(32767);
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  commit;
  if schemaname is null then
    idtable := '"id' || stamp || '"';
  else
    idtable := schemaname || '."id' || stamp || '"';
  end if;
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || idtable || ''')';
  execute immediate 'Create  table ' || idtable || ' as (select ' ||
                    tablename ||
                    '.*,row_number()over(order by 1) "alpine_randomforest_id" from ' ||
                    tablename || ')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || tablename ||
                    ''')';
  execute immediate 'Create  table ' || tablename || ' as select * from ' ||
                    idtable;
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || idtable || ''')';
  tempstring := 'update ' || tablename || ' set "C(' || infor(1) || ')"=0';
  i          := 0;
  for i in 2 .. infor.count() loop
    dbms_lob.append(tempstring, ', "C(' || infor(i) || ')"=0');
  end loop;
  execute immediate to_char(tempstring);
  commit;
  RETURN i;
end alpine_miner_rf_initpre;


/
CREATE OR REPLACE FUNCTION alpine_miner_rf_prere(tablename    varchar2,
                                                       dependcolumn varchar2,
                                                       infor        varchar2array,
                                                       isnumeric    int)
  RETURN binary_double AS
  rownumber   integer;
  classnumber integer;
  sqlan       clob;
  sql2        clob;
    i           integer := 0;
  PRAGMA AUTONOMOUS_TRANSACTION;
  err binary_double;
BEGIN
  commit;
  classnumber := infor.count();
  sqlan       := 'update ' || tablename || ' set  "P(' || dependcolumn ||
                 ')" = CASE';
  sql2        := '(';
  i           := classnumber;
  while i > 1 loop
    dbms_lob.append(sql2, '  "C(' || infor(i) || ')" ,');
    i := i - 1;
  end loop;
  dbms_lob.append(sql2, '"C(' || infor(1) || ')" )');
  for i in 1 .. classnumber loop
    dbms_lob.append(sqlan, ' WHEN "C(' || infor(i) || ')"=greatest');
    dbms_lob.append(sqlan, sql2);
    dbms_lob.append(sqlan, ' THEN ');
    if isnumeric = 1 then
      dbms_lob.append(sqlan, infor(i));
    else
      dbms_lob.append(sqlan, '''' || infor(i) || '''');
    end if;
  end loop;
  dbms_lob.append(sqlan, ' END ');
  execute immediate to_char(sqlan);
  err := err / rownumber;
  commit;
  RETURN err;
end alpine_miner_rf_prere;


/

CREATE OR REPLACE FUNCTION alpine_miner_rf_prestep(tablename     varchar2,
                                                         temptablename varchar2,
                                                        
                                                          infor         varchar2array)
  RETURN binary_double is
  
  sqlan       clob;
   i           integer := 0;
  PRAGMA AUTONOMOUS_TRANSACTION;
 BEGIN
  commit;
  sqlan := 'update ' || tablename || '  set "C(' || infor(1) || ')"=  "C(' ||
           infor(1) || ')"+ (select "C(' || infor(1) || ')"  from ' ||
           temptablename || '  where ' || tablename ||
           '."alpine_randomforest_id" = ' || temptablename ||
           '."alpine_randomforest_id")  ';
  for i in 2 .. infor.count() loop
    dbms_lob.append(sqlan,
                    ', "C(' || infor(i) || ')"=  "C(' || infor(i) ||
                    ')"+ (select "C(' || infor(i) || ')"  from ' ||
                    temptablename || '  where ' || tablename ||
                    '."alpine_randomforest_id" = ' || temptablename ||
                    '."alpine_randomforest_id")  ');
  end loop;
  execute immediate to_char(sqlan);
  commit;
  RETURN 1;
end alpine_miner_rf_prestep;

/

 


