create or replace type pcaSumImpl as object
(
  varraysum Numberarray,
  static function ODCIAggregateInitialize(fs IN OUT pcaSumImpl) 
    return number,
  member function ODCIAggregateIterate(self IN OUT pcaSumImpl, 
    value IN Numberarray ) return number,
  member function ODCIAggregateTerminate(self IN pcaSumImpl, 
    returnValue OUT Numberarray, flags IN number) return number,
  member function ODCIAggregateMerge(self IN OUT pcaSumImpl, 
    fs2 IN pcaSumImpl) return number
);
/ 
    
create or replace type body pcaSumImpl is
static function ODCIAggregateInitialize(fs IN OUT pcaSumImpl)
return number is
arraysum Numberarray := Numberarray();
begin
  fs:=pcaSumImpl(Numberarray());
  fs.varraysum := Numberarray();

 
  return ODCIConst.Success;
end;

member function ODCIAggregateIterate(self IN OUT pcaSumImpl,
    value IN Numberarray ) return number is
i integer := 0;
j integer := 0;
k integer := 1;
begin
    if self.varraysum.count() = 0 then
    self.varraysum.extend();
    self.varraysum(1):=0;
    self.varraysum.extend(((value.count()+1)*value.count()/2-1),1);
  end if;
  for i in 1..value.count() loop
    for j in i..value.count() loop
      self.varraysum(k):=self.varraysum(k)+value(i)*value(j);
      k:=k+1;
    end loop;
  end loop;
  return ODCIConst.Success;
end;

member function ODCIAggregateTerminate(self IN pcaSumImpl,
    returnValue OUT Numberarray, flags IN number)
return number is
begin
   returnValue := self.varraysum;
  return ODCIConst.Success;
end;

member function ODCIAggregateMerge(self IN OUT pcaSumImpl, fs2 IN pcaSumImpl)
return number is
i integer :=0;
mincount integer := 0;
begin
    if self.varraysum.count() < fs2.varraysum.count() then

    for i in 1..self.varraysum.count() loop
      self.varraysum(i) := self.varraysum(i) + fs2.varraysum(i);
    end loop;
     for i in (self.varraysum.count() + 1) .. fs2.varraysum.count() loop
        self.varraysum.extend();
        self.varraysum(i) :=   fs2.varraysum(i);
    end loop;
    else 
     for i in 1..self.varraysum.count() loop
      self.varraysum(i) := self.varraysum(i) + fs2.varraysum(i);
    end loop;
    end if;
  return ODCIConst.Success;
end;
 end ;
 
/
 
create or replace
FUNCTION pcaSum (input Numberarray ) RETURN Numberarray 
 PARALLEL_ENABLE  AGGREGATE USING pcaSumImpl;
/


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
  maxcolumn    integer:=500;
  tempnumber   integer;
  sql1         varchar2(32767);
  sql4         varchar2(32767);
  sql2         varchar2(32767);
  sql3         varchar2(32767);
  sqltemp      varchar2(32767);
  sql5        varchar2(32767);
  lastresult   floatarray := floatarray();
  resultarray       floatarray := floatarray();
  temparray    floatarray:=floatarray();
  notnull      varchar2(32767);
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
   sql1:=sql1||', ' || infor(i) || ' binary_double';
    notnull:=notnull|| ' and ' || infor(i) || ' is not null';
  end loop;
  sql1:=sql1||',"alpine_pcadataindex" integer,"alpine_pcaevalue" binary_double,"alpine_pcacumvl" binary_double,"alpine_pcatotalcumvl" binary_double) ';
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
         sql2:=sql2||'sum(' || infor(i) || '*' || infor(j) || ')';      
      temparray.extend();
        sqltemp:='select floatarray(';
        sqltemp:=sqltemp||sql2;
        sqltemp:=sqltemp||') from ';
         sqltemp:=sqltemp||tablename;
         sqltemp:=sqltemp||notnull;
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
          sql2:=sql2||'sum(' || infor(i) || '*' || infor(j) || ')';
          else   
          sql2:=sql2||'sum(' || infor(i) || '*' || infor(j) || '),';
           end if;
         end if;
       if i = columnnumber and j = columnnumber then
        sql3:=sql3||'avg(' || infor(i) || ')';
      else
        if j = columnnumber then
         sql3:=sql3||'avg(' || infor(i) || '),';
          
        end if;
      end if;
    end loop;
  end loop;
   if tempnumber != 0 
  then
   sql4:='select floatarray(';
  sql4:=sql4||sql2;
  sql4:=sql4||') from ';
  sql4:=sql4||tablename;
  sql4:=sql4||notnull;
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


CREATE OR REPLACE FUNCTION ALPINE_MINER_PCARESULT_ONE(computedata floatarray,
                                                          pcadata     floatarray)
  RETURN binary_double AS
  j           integer;
  PRAGMA AUTONOMOUS_TRANSACTION;
  columnsize  integer;
  tempvalue  binary_double;
BEGIN
  columnsize := computedata.count;


    tempvalue := 0;
    for j in 1 .. columnsize loop
      tempvalue := tempvalue +
                   computedata(j) * pcadata(j);
    end loop;

  RETURN tempvalue;
END ALPINE_MINER_PCARESULT_ONE;
/

CREATE OR REPLACE FUNCTION ALPINE_MINER_PCARESULT(tablename    varchar2,
                                                  infor        varchar2array,
                                                  outtablename varchar2,
                                                  remainname   varchar2array,
                                                  valuename    varchar2,
                                                  pcanumber    integer,
                                                  dropif       varchar2)
  RETURN integer AS
  i            integer;
  k            integer;
  remainnumber integer;
  randomnumber integer;
  PRAGMA AUTONOMOUS_TRANSACTION;
  totalsql  varchar2(32767);
 
  remainsql varchar2(32767);
  TYPE crt IS REF CURSOR;
  temparray     floatarray := floatarray();
  pcavaluearray floatarrayarray := floatarrayarray();
  myrecord      crt;
 
  sqlan         varchar2(32767);
  infosql       varchar2(32767);
BEGIN
  commit;
  execute immediate 'alter session force parallel dml';
  
  execute immediate 'select abs(sys.dbms_random.random()) from dual' into randomnumber;
  execute immediate 'create table "Alpine_PCA'||randomnumber||'"  as select '||tablename||'.*, rownum as alpine_PCA_rowid from  '||tablename;
  infosql:=alpine_varray_to_string(infor, ',');
  if remainname is not null then
    remainnumber := remainname.count;
    remainsql    := ' ';
    for i in 1 .. remainnumber loop
      dbms_lob.append(remainsql, remainname(i)||',' );
    end loop;
  else
    remainsql := ' ';
  end if;
  IF dropif = 'yes' THEN
    execute immediate 'call PROC_DROPSCHTABLEIFEXISTS(''' || outtablename ||
                      ''')';
  END IF;
  totalsql := 'create table    ' || outtablename || ' parallel as select ';
  i        := 1;
  sqlan    := ' select floatarray(' || infosql ||
              ') as F from ' || valuename ||
              ' order by   "alpine_pcadataindex"   ';
  k        := 1;
  open myrecord for sqlan;
  loop
    FETCH myrecord
      INTO temparray;
    
      pcavaluearray.extend();
      pcavaluearray(k) := temparray;
      k := k + 1;
   
    EXIT WHEN myrecord%NOTFOUND;
  end loop;
  
  totalsql:=totalsql||remainsql||' alpine_PCA_rowid,  ALPINE_MINER_PCARESULT_ONE(floatarray(' ||infosql|| '),
                                   floatarray(' || alpine_farray_to_string(pcavaluearray(1), ',')||')) "attribute' || i || '"';
  for i in 2 .. pcanumber loop
  totalsql:=totalsql||',0.0  "attribute' || i || '"';
  
  end loop;
  totalsql:=totalsql||' from  "Alpine_PCA'||randomnumber||'"';
  dbms_output.put_line(totalsql);
  execute immediate totalsql;
  for i in 2 .. pcanumber loop
     
      totalsql := 'update ' || outtablename || ' set "attribute' || i || '"=( select ALPINE_MINER_PCARESULT_ONE(floatarray(' ||infosql|| '),
                                   floatarray(' || alpine_farray_to_string(pcavaluearray(i), ',')||')) from "Alpine_PCA'||randomnumber||'" 
                                   where ' || outtablename || '.alpine_PCA_rowid="Alpine_PCA'||randomnumber||'".alpine_PCA_rowid)';
    execute immediate totalsql;
  end loop;

  execute immediate ' alter table ' || outtablename || ' drop column  alpine_PCA_rowid ';
  execute immediate ' drop table "Alpine_PCA'||randomnumber||'"';
   execute immediate 'alter session disable parallel dml';
  commit;
  RETURN i;
END ALPINE_MINER_PCARESULT;
/

