CREATE OR REPLACE FUNCTION alpine_miner_adaboost_changep(schemaname              varchar2,
                                                         tablename               varchar2,
                                                         stamp                   varchar2,
                                                         dependcolumnq           varchar2,
                                                         dependentcolumnreplaceq varchar2,
                                                         dependentcolumnstr      varchar2,
                                                         dependinfor             varchar2array)
  RETURN binary_double  is
  rownumber   binary_double;
  totalpeoso  binary_double;
  i           integer;
  classnumber integer;
  wrongnumber binary_double;
  err         binary_double;
  maxerror    binary_double;
  PRAGMA AUTONOMOUS_TRANSACTION;
  c     binary_double := 0;
  sqlan clob;
BEGIN
  commit;
  execute immediate 'alter table ' || schemaname || '."tp' || stamp ||
                    '"  add("notsame" int)';
  execute immediate 'update  ' || schemaname || '."tp' || stamp ||
                    '" set "notsame" = CASE WHEN ' || dependentcolumnstr ||
                    ' = "P(' || dependentcolumnreplaceq ||
                    ')"  THEN 0 ELSE 1 END';
  commit;
  execute immediate 'select count(*) from ' || schemaname || '."tp' ||
                    stamp || '" where "notsame" =1'
    into wrongnumber;
  execute immediate 'select count(*) from ' || schemaname || '.' ||
                    tablename || ''
    into rownumber;
  err := wrongnumber / rownumber;
  execute immediate 'select "count" from ( select ' || dependcolumnq ||
                    ', count(*) "count" from ' || schemaname || '.' ||
                    tablename || ' group by ' || dependcolumnq ||
                    '  order by "count" desc ) where rownum <=1'
    into maxerror;
  commit;
  maxerror := maxerror / rownumber;
  IF err >= maxerror THEN
    c := 0.001;
    execute immediate 'update ' || schemaname || '."pnew' || stamp ||
                      '"  set "alpine_adaboost_peoso"=' || 1 / rownumber ||
                      ',"alpine_adaboost_totalpeoso"=rownum*' ||
                      1 / rownumber;
    commit;
  ELSIF err = 0 THEN
    c := 3;
    execute immediate 'update ' || schemaname || '."pnew' || stamp ||
                      '"  set "alpine_adaboost_peoso"=' || 1 / rownumber ||
                      ',"alpine_adaboost_totalpeoso"=rownum*' ||
                      1 / rownumber;
    commit;
  ELSE
    c          := ln((1 - err) / err);
    c          := c / 2;
    totalpeoso := 0;
    execute immediate 'alter table ' || schemaname || '."pnew' || stamp ||
                      '"  add ("notsame" int)';
    commit;
    execute immediate 'update  ' || schemaname || '."pnew' || stamp ||
                      '"  set "notsame" = (select  ' || schemaname ||
                      '."tp' || stamp || '"."notsame" from ' || schemaname ||
                      '."tp' || stamp || '" where ' || schemaname ||
                      '."pnew' || stamp || '"."alpine_adaboost_id" = ' ||
                      schemaname || '."tp' || stamp ||
                      '"."alpine_adaboost_id")';
    commit;
    execute immediate 'update  ' || schemaname || '."pnew' || stamp ||
                      '" set  "alpine_adaboost_peoso" = "alpine_adaboost_peoso"*exp(' || c ||
                      '*"notsame") ';
    commit;
--    execute immediate 'select sum("alpine_adaboost_peoso") from ' ||
 --                     schemaname || '."pnew' || stamp || '"'
 --     into totalpeoso;
 --   commit;
 --   execute immediate 'update  ' || schemaname || '."pnew' || stamp ||
 --                     '" set  "alpine_adaboost_peoso" = "alpine_adaboost_peoso"/' ||
 --                     totalpeoso;
 --   commit;
    execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                      '."sp' || stamp || '"'')';
    execute immediate 'Create  table ' || schemaname || '."sp' || stamp ||
                      '" as select "alpine_adaboost_id", "alpine_adaboost_peoso", sum("alpine_adaboost_peoso")over (order by "alpine_adaboost_id" ) alpine_sum_peoso from ' ||
                      schemaname || '."pnew' || stamp || '"';
    commit;
    execute immediate 'update  ' || schemaname || '."pnew' || stamp ||
                      '" set  "alpine_adaboost_totalpeoso" = (select alpine_sum_peoso from ' ||
                      schemaname || '."sp' || stamp || '" where ' ||
                      schemaname || '."pnew' || stamp ||
                      '"."alpine_adaboost_id" = ' || schemaname || '."sp' ||
                      stamp || '"."alpine_adaboost_id")';
    commit;
    execute immediate 'alter table ' || schemaname || '."pnew' || stamp ||
                      '"  drop column "notsame"';
  END IF;
  commit;
  classnumber := dependinfor.count();
  i           := 2;
  sqlan       := 'update ' || schemaname || '."p' || stamp || '" set "C(' ||
                 dependinfor(1) || ')" ="C(' || dependinfor(1) ||
                 ')" + (select "C(' || dependinfor(1) || ')" from  ' ||
                 schemaname || '."tp' || stamp || '"  where ' || schemaname ||
                 '."p' || stamp || '"."alpine_adaboost_id" = ' ||
                 schemaname || '."tp' || stamp ||
                 '"."alpine_adaboost_id")*' || c;
  while i <= classnumber loop
    dbms_lob.append(sqlan,
                    ' , "C(' || dependinfor(i) || ')" ="C(' ||
                    dependinfor(i) || ')" + (select "C(' || dependinfor(i) ||
                    ')" from  ' || schemaname || '."tp' || stamp ||
                    '"  where ' || schemaname || '."p' || stamp ||
                    '"."alpine_adaboost_id" = ' || schemaname || '."tp' ||
                    stamp || '"."alpine_adaboost_id") *' || c);
    i := i + 1;
  end loop;
  execute immediate sqlan;
  commit;
  RETURN(c);
END;

/



CREATE OR REPLACE FUNCTION alpine_miner_adaboost_initpre(schemaname   varchar2,
                                                         tablename    varchar2,
                                                         stamp        varchar2,
                                                         dependcolumn varchar2,
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
                    '.*,row_number()over(order by 1) "alpine_adaboost_id" from ' ||
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
  execute immediate tempstring;
  commit;
  RETURN i;
end alpine_miner_adaboost_initpre;

/

CREATE OR REPLACE FUNCTION alpine_miner_adaboost_inittra(schemaname   varchar2,
                                                         tablename    varchar2,
                                                         stamp        varchar2,
                                                         dependcolumn varchar2,
                                                         dependinfor  varchar2array)
  RETURN integer is
  sqlan       clob;
  sqlan1      clob;
  rownumber   integer;
  classnumber integer;
  peoso       binary_double;
  i           integer := 0;
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
                    '"    as (select ' || schemaname || '."dn' ||
                    stamp ||
                    '".*, row_number()over(order by 1) "alpine_adaboost_id", ' ||
                    peoso || ' "alpine_adaboost_peoso", rownum*' || peoso ||
                    ' "alpine_adaboost_totalpeoso" from ' || schemaname ||
                    '."dn' || stamp || '") ';
  classnumber := dependinfor.count();
  
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS( ''' || schemaname ||
                    '."tp' || stamp || '"'')';
  execute immediate 'alter session force parallel dml';
  execute immediate 'CREATE TABLE ' || schemaname || '."tp' || stamp ||
                    '" parallel   as select * from  ' || schemaname ||
                    '."pnew' || stamp || '" ';
  execute immediate 'alter session disable parallel dml';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."p' || stamp || '"'')';
  execute immediate 'alter session force parallel dml';
  execute immediate 'CREATE TABLE ' || schemaname || '."p' || stamp ||
                    '"  parallel as select * from  "pnew' || stamp || '" ';
  execute immediate 'alter session disable parallel dml';
  execute immediate 'alter table ' || schemaname || '."p' || stamp ||
                    '"  drop column  "alpine_adaboost_peoso" ';
  execute immediate 'alter table ' || schemaname || '."p' || stamp ||
                    '"  drop column  "alpine_adaboost_totalpeoso" ';
  i      := 2;
  sqlan  := 'alter table ' || schemaname || '."p' || stamp || '" add ("C(' ||
            dependinfor(1) || ')"   binary_double';
  sqlan1 := 'update ' || schemaname || '."p' || stamp || '" set "C(' ||
            dependinfor(1) || ')"=0';
  while i <= classnumber loop
    dbms_lob.append(sqlan,
                    ',"C(' || dependinfor(i) || ')"   binary_double');
    dbms_lob.append(sqlan1, ' ,"C(' || dependinfor(i) || ')" =0 ');
    i := i + 1;
  end loop;
  dbms_lob.append(sqlan, ')');
  execute immediate sqlan;
  execute immediate sqlan1;
  commit;
  execute immediate 'alter session force parallel dml';
  execute immediate 'create table ' || schemaname || '."s' || stamp ||
                    '"   as select * from ' || schemaname || '."pnew' ||
                    stamp || '"';
  execute immediate 'alter session disable parallel dml';
  commit;
  RETURN rownumber;
end alpine_miner_adaboost_inittra;
/


CREATE OR REPLACE FUNCTION alpine_miner_adaboost_prere(tablename    varchar2,
                                                       dependcolumn varchar2,
                                                       infor        varchar2array,
                                                       isnumeric    int)
  RETURN binary_double AS
  rownumber   integer;
  classnumber integer;
  sqlan       clob;
  sql2        clob;
  peoso       binary_double;
  totalpeoso  binary_double;
  tempstring  varchar2(32767);
  classstring varchar2array;
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
  execute immediate sqlan;
  err := err / rownumber;
  commit;
  RETURN err;
end;
/



CREATE OR REPLACE FUNCTION alpine_miner_adaboost_prestep(tablename     varchar2,
                                                         temptablename varchar2,
                                                         dependcolumn  varchar2,
                                                         c             binary_double,
                                                         infor         varchar2array)
  RETURN binary_double  is
  rownumber   integer;
  classnumber integer;
  sqlan       clob;
  peoso       binary_double;
  totalpeoso  binary_double;
  tempstring  varchar2(32767);
  classstring varchar2array;
  i           integer := 0;
  PRAGMA AUTONOMOUS_TRANSACTION;
  err binary_double;
BEGIN
  commit;
  sqlan := 'update ' || tablename || '  set "C(' || infor(1) || ')"=  "C(' ||
           infor(1) || ')"+ (select "C(' || infor(1) || ')"  from ' ||
           temptablename || '  where ' || tablename ||
           '."alpine_adaboost_id" = ' || temptablename ||
           '."alpine_adaboost_id")*' || c || ' ';
  for i in 2 .. infor.count() loop
    dbms_lob.append(sqlan,
                    ', "C(' || infor(i) || ')"=  "C(' || infor(i) ||
                    ')"+ (select "C(' || infor(i) || ')"  from ' ||
                    temptablename || '  where ' || tablename ||
                    '."alpine_adaboost_id" = ' || temptablename ||
                    '."alpine_adaboost_id")*' || c || ' ');
  end loop;
  execute immediate sqlan;
  commit;
  RETURN c;
end;
/



CREATE OR REPLACE FUNCTION alpine_miner_adaboost_sample(schemaname varchar2,
                                                        tablename  varchar2,
                                                        stamp      varchar2,
                                                        partsize   integer)
  RETURN varchar2 is
  tempstring varchar2(32767);
  rownumber integer;
 
   TYPE crt IS REF CURSOR;
  myrecord crt;
  partnumber integer;
  splitpeoso Numberarray:=Numberarray();
  maxpeoso number;
  temppeoso number;
  i        integer;
  executesql varchar2(32767);
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  commit;
  execute immediate 'select count(*) from '||schemaname||'.'||tablename into rownumber;
  execute immediate ' select max("alpine_adaboost_totalpeoso")  from '||schemaname||'.'||tablename into maxpeoso;
   
 	if partsize>= rownumber
	then 
		partnumber:=1;
	else 
		if mod(rownumber ,partsize)=0
		then
			partnumber:= rownumber/partsize;
		else 
			partnumber:=trunc(rownumber/partsize)+1;
		end if;
	end if;

  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."s' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."r' || stamp || '"'')';
 
  execute immediate 'create table ' || schemaname || '."r' || stamp ||
                    '"      as select SYS.DBMS_RANDOM.VALUE(0,'||maxpeoso||')  "alpine_miner_adaboost_r" from ' ||
                    schemaname || '.' || tablename || ' order by SYS.DBMS_RANDOM.VALUE(0,'||maxpeoso||')';
  
  if partnumber=1
  then
    executesql := 'alter session force parallel dml';
   execute immediate executesql;
  execute immediate 'create table ' || schemaname || '."s' || stamp ||
                    '"  parallel  as select * from ' || schemaname || '.' ||
                    tablename || ' join ' || schemaname || '."r' || stamp ||
                    '" on ' || schemaname || '.' || tablename ||
                    '."alpine_adaboost_totalpeoso"  >= ' || schemaname ||
                    '."r' || stamp || '"."alpine_miner_adaboost_r" and ' || schemaname || '.' ||
                    tablename || '."alpine_adaboost_peoso" > (' ||
                    schemaname || '.' || tablename ||
                    '."alpine_adaboost_totalpeoso"-' || schemaname || '."r' ||
                    stamp || '"."alpine_miner_adaboost_r")';
    executesql := 'alter session disable parallel dml';
   execute immediate executesql;                  
    commit;                
  else 
  	tempstring:=' select "alpine_adaboost_totalpeoso" as peoso from '||schemaname||'.'||tablename||' where mod("alpine_adaboost_id",'||partsize||')=0 order by peoso';
 
    i:=1;
    splitpeoso.extend();
    splitpeoso(i):=0;
     open myrecord for tempstring;
  loop
   FETCH myrecord INTO temppeoso;
   EXIT WHEN myrecord%NOTFOUND;
   i:=i+1;
    splitpeoso.extend();
   splitpeoso(i):=temppeoso;
      
   end loop;
   if splitpeoso(i)!=maxpeoso
   then 
  
      i:=i+1;
       splitpeoso.extend();
      splitpeoso(i):=maxpeoso;
        
   end if;
   
   
   
   i:=1;
    executesql := 'alter session force parallel dml';
   execute immediate executesql;
   	tempstring:='create table '||schemaname||'."s'||stamp||'"  parallel as select * from  ( select * from '||schemaname||'.'||tablename||' 
			where "alpine_adaboost_totalpeoso">'||splitpeoso(i)||' and  "alpine_adaboost_totalpeoso"<='||splitpeoso(i+1)||')   foo'||i||' join (select * from "r'||stamp||'" where "alpine_miner_adaboost_r"
			>'||splitpeoso(i)||' and  "alpine_miner_adaboost_r"<='||splitpeoso(i+1)||')   foor'||i||' on foo'||i||'."alpine_adaboost_totalpeoso" >=foor'||i||'."alpine_miner_adaboost_r" and foo'||i||'."alpine_adaboost_peoso" > 
		(foo'||i||'."alpine_adaboost_totalpeoso"-foor'||i||'."alpine_miner_adaboost_r") ';
  		execute  immediate tempstring;
       executesql := 'alter session disable parallel dml';
   execute immediate executesql;                  
    commit;      
   
      		for i in 2..partnumber loop
			tempstring:= '  insert into  '||schemaname||'."s'||stamp||'"   select * from ( select * from '||schemaname||'.'||tablename||' 
  			where "alpine_adaboost_totalpeoso">'||splitpeoso(i)||' and  "alpine_adaboost_totalpeoso"<='||splitpeoso(i+1)||')   foo'||i||' join (select * from "r'||stamp||'" where "alpine_miner_adaboost_r" 
  			>'||splitpeoso(i)||' and "alpine_miner_adaboost_r"<='||splitpeoso(i+1)||')   foor'||i||' on foo'||i||'."alpine_adaboost_totalpeoso" >=foor'||i||'."alpine_miner_adaboost_r" and foo'||i||'."alpine_adaboost_peoso" > 
 			(foo'||i||'."alpine_adaboost_totalpeoso"-foor'||i||'."alpine_miner_adaboost_r") ';
		    execute  immediate 'select count(*) from '||schemaname||'."s'||stamp||'"' into  rownumber;
  			execute immediate  tempstring;
     
   		end loop;
 	end if;    
  tempstring := 's' || stamp;
  commit;
  RETURN tempstring;
end alpine_miner_adaboost_sample;


/



CREATE OR REPLACE FUNCTION alpine_miner_adaboost_cleanre(schemaname varchar2,
                                                         stamp      varchar2)
  return integer is
  rownumber   integer;
  classnumber integer;
  sqlan       varchar2(32767);
  sql2        varchar2(32767);
  i           integer := 0;
  err         binary_double;
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."pnew' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."dn' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."pnew' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS( ''' || schemaname ||
                    '."tp' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."p' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."s' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                    '."r' || stamp || '"'')';
  execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname ||
                      '."sp' || stamp || '"'')';
  return 0;
end;
/


