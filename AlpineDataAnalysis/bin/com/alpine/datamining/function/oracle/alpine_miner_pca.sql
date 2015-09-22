create or replace function alpine_miner_initpca(tablename    varchar2,
                                                valueoutname varchar2,
                                                infor        varchar2array,
                                                createway    varchar2,
                                                dropif       varchar2)
  return floatarray is
  columnnumber binary_double;
  rownumber    binary_double;
  i            integer;
  j            integer;
  k            integer;
  numberindex  integer;
  totalindex   integer;
  frequency    integer:=0;
  maxcolumn    integer:=900;
  tempnumber   integer;
  sql1         clob;
  sql4         clob;
  sql2         clob;
  sql3         clob;
  sqltemp      clob;
  sql5        clob;
  lastresult   floatarray := floatarray();
  resultarray       floatarray := floatarray();
  temparray    floatarray:=floatarray();
  notnull      clob;
  PRAGMA AUTONOMOUS_TRANSACTION;
begin
  commit;
  
  notnull      := ' where ' || infor(1) || ' is not null ';
  columnnumber := infor.count;
 
  IF dropif = 'yes' THEN
    execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || valueoutname ||
                      ''')';
  END IF;
  sql1 := 'create table ' || valueoutname || '( ' || infor(1) ||
          '  binary_double';
  sql2 := ' ';
  sql3 := ' ';
  for i in 2 .. columnnumber loop
    dbms_lob.append(sql1, ', ' || infor(i) || ' binary_double');
    dbms_lob.append(notnull, ' and ' || infor(i) || ' is not null');
  end loop;
  dbms_lob.append(sql1,
                  ',"alpine_pcadataindex" integer,"alpine_pcaevalue" binary_double,"alpine_pcacumvl" binary_double,"alpine_pcatotalcumvl" binary_double) ');
  execute immediate sql1;
  execute immediate 'select count(*) from ' || tablename || notnull
    into rownumber;
  totalindex := 0;
  tempnumber:=0;
  for i in 1 .. columnnumber loop
    for j in i .. columnnumber loop
        tempnumber:=tempnumber+1;
        totalindex:=totalindex+1; 
         if tempnumber>= maxcolumn
        then
        frequency:=frequency+1;
         dbms_lob.append(sql2, 'sum(' || infor(i) || '*' || infor(j) || ')');      
      temparray.extend();
        sqltemp:='select floatarray(';
        dbms_lob.append(sqltemp,sql2);
         dbms_lob.append(sqltemp,') from ');
         dbms_lob.append(sqltemp,tablename);
         dbms_lob.append(sqltemp,notnull);
     execute immediate sqltemp into temparray;
            for k in 1..tempnumber loop
             resultarray.extend();
               resultarray((frequency-1)*maxcolumn+k):=temparray(k);
           end loop;
           sql2:=' ';
           if totalindex!=(columnnumber+1)*columnnumber/2
           then tempnumber:=0;
           end if;
          else   
          if totalindex=(columnnumber+1)*columnnumber/2
          then 
          dbms_lob.append(sql2, 'sum(' || infor(i) || '*' || infor(j) || ')');
          else   
          dbms_lob.append(sql2, 'sum(' || infor(i) || '*' || infor(j) || '),');
           end if;
         end if;
       if i = columnnumber and j = columnnumber then
        dbms_lob.append(sql3, 'avg(' || infor(i) || ')');
      else
        if j = columnnumber then
          dbms_lob.append(sql3, 'avg(' || infor(i) || '),');
          
        end if;
      end if;
    end loop;
  end loop;
   if tempnumber != 0 
  then
   sql4:='select floatarray(';
  dbms_lob.append(sql4,sql2);
   dbms_lob.append(sql4,') from ');
   dbms_lob.append(sql4,tablename);
   dbms_lob.append(sql4,notnull);
  execute immediate sql4
    into temparray;
    
     for k in 1..tempnumber loop
             resultarray.extend();
                resultarray((frequency)*maxcolumn+k):=temparray(k);
           end loop;
           end if;
         
           
           
     execute immediate 'select floatarray(' || sql3  || ') from ' ||
                    tablename || notnull
    into temparray;
    j:=(frequency)*maxcolumn+tempnumber;
    for  i in 1.. columnnumber loop
         resultarray.extend();
          resultarray(j+i):=temparray(i);
      end loop;
    commit;
  numberindex := 0;
  IF createway = 'cov-sam' THEN
    for i in 1 .. columnnumber loop
      for j in i .. columnnumber loop
        numberindex := numberindex + 1;
        lastresult.extend;
        lastresult(numberindex) := (resultarray(numberindex) -
                                   rownumber * resultarray(totalindex + i) *
                                   resultarray(totalindex + j)) /
                                   (rownumber - 1);
      end loop;
    end loop;
    RETURN lastresult;
  else
    IF createway = 'cov-pop' THEN
      for i in 1 .. columnnumber loop
        for j in i .. columnnumber loop
          numberindex := numberindex + 1;
          lastresult.extend;
          lastresult(numberindex) := (resultarray(numberindex) -
                                     rownumber * resultarray(totalindex + i) *
                                     resultarray(totalindex + j)) / (rownumber);
        end loop;
      end loop;
        RETURN lastresult;
     else
      IF createway = 'corr' THEN
        for i in 1 .. columnnumber loop
          for j in i .. columnnumber loop
            numberindex := numberindex + 1;
            lastresult.extend;
            lastresult(numberindex) := (resultarray(numberindex) -
                                       rownumber * resultarray(totalindex + i) *
                                       resultarray(totalindex + j)) /
                                       (sqrt(resultarray((2 * columnnumber + 2 - i) *
                                                    (i - 1) / 2 + 1) -
                                             rownumber *
                                             resultarray(totalindex + i) *
                                             resultarray(totalindex + i)) *
                                       sqrt(resultarray((2 * columnnumber + 2 - j) *
                                                    (j - 1) / 2 + 1) -
                                             rownumber *
                                             resultarray(totalindex + j) *
                                             resultarray(totalindex + j)));
          end loop;
        end loop;
          RETURN lastresult;
      end if;
    end if;
  end if;
END alpine_miner_initpca;

/

CREATE OR REPLACE FUNCTION ALPINE_MINER_PCARESULT(tablename    varchar2,
                                                  infor        varchar2array,
                                                  outtablename varchar2,
                                                  remainname   varchar2array,
                                                  valuename    varchar2,
                                                  pcanumber    integer,
                                                  dropif       varchar2)
  RETURN integer AS
  columnnumber integer;
  i            integer;
  j            integer;
  remainnumber integer;
  PRAGMA AUTONOMOUS_TRANSACTION;
  totalsql  clob;
  sumsql    clob;
  remainsql clob;
  tempvalue binary_double;
BEGIN
  commit;
  execute immediate 'alter session force parallel dml';
  if remainname is not null then
    remainnumber := remainname.count;
    remainsql    := ' ';
    for i in 1 .. remainnumber loop
      dbms_lob.append(remainsql, ',' || remainname(i));
    end loop;
  else
    remainsql := ' ';
  end if;
  columnnumber := infor.count;
  IF dropif = 'yes' THEN
    execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || outtablename ||
                      ''')';
  END IF;
  totalsql := 'create table    ' || outtablename || ' parallel as select';
  i        := 1;
  sumsql   := ' ';
  while i <= pcanumber loop
    execute immediate '(select ' || infor(1) || ' from ' || valuename ||
                      ' where  ' || valuename || '."alpine_pcadataindex"=' ||
                      (i - 1) || ')'
      into tempvalue;
    dbms_lob.append(sumsql,
                    ' ' || tablename || '.' || infor(1) || '* (' ||
                    tempvalue || ')');
    j := 2;
    while j <= columnnumber loop
      execute immediate '(select ' || infor(j) || ' from ' || valuename ||
                        ' where  ' || valuename ||
                        '."alpine_pcadataindex"=' || (i - 1) || ')'
        into tempvalue;
      dbms_lob.append(sumsql,
                      '+ ' || tablename || '.' || infor(j) || '*(' ||
                      tempvalue || ')');
      j := j + 1;
    end loop;
    IF i = pcanumber then
      dbms_lob.append(sumsql, '   "attribute' || i || '"');
    ELSE
      dbms_lob.append(sumsql, '   "attribute' || i || '" ,');
    END IF;
    i := i + 1;
  end loop;
  dbms_lob.append(totalsql, sumsql);
  dbms_lob.append(totalsql, remainsql);
  dbms_lob.append(totalsql, ' from ' || tablename);
  execute immediate totalsql;
  execute immediate 'alter session disable parallel dml';
  commit;
  RETURN i;
END ALPINE_MINER_PCARESULT;

/
