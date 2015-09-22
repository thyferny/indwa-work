
create or replace function alpine_miner_array_avg(arraydata FloatArray, arraysize integer)
return FloatArray as
	newarraydata FloatArray:=FloatArray();
BEGIN
	for i in 1..arraydata.count() loop
		newarraydata.extend();
		newarraydata(i):=arraydata(i)/arraysize;
	end loop;
	return newarraydata;
end;


/

create or replace function alpine_miner_em_getp(columnarray Varchar2Array, mu FloatArray,sigma FloatArray,alpha binary_double)
return binary_double as
	peoso binary_double;
	sigmaValue binary_double;
begin
	peoso:=0;
	sigmaValue:=1;
	for i in 1..columnarray.count() loop
		peoso:=peoso+(columnarray(i)-mu(i))*(columnarray(i)-mu(i))/sigma(i);
		sigmaValue:=sigmaValue*sigma(i);
	end loop;
	peoso:=alpha*exp(-0.5*peoso)/sqrt(sigmaValue);
	return peoso;
end;


/


create or replace function alpine_miner_em_getmaxsub(firstarray FloatArray,secondarray FloatArray) 
return binary_double as
	tempsub binary_double;
	maxsub binary_double;
begin
	maxsub:=0;
	for i in 1..firstarray.count() loop
		tempsub:=abs(firstarray(i)-secondarray(i));
		if tempsub>maxsub then maxsub:=tempsub;
		end if;
	end loop;
	return maxsub;
end;

/

create or replace function floatarraydiv(arraydata FloatArray, arraysize integer)
return FloatArray as
  newarraydata FloatArray:=FloatArray();
BEGIN
  for i in 1..arraydata.count() loop
    newarraydata.extend();
    newarraydata(i):=arraydata(i)/arraysize;
  end loop;
  return newarraydata;
end;

/

create or replace function GETFARANGE(farray       FloatArray,
                                                       elementfrom integer,
                                                       elementend integer)
  return varchar2 as
  newdatastring varchar2(32767);
  i integer;
  j integer;
begin
  j:=1;
  i:=1;
  while(i<=farray.count()) loop
  if (i >=elementfrom and i <= elementend) then
    if j=1 then 
       newdatastring:=farray(i);
       j:=j+1;
       else
       newdatastring:=newdatastring||','||farray(i);
       j:=j+1;
    end if;
  end if;
  i:=i+1;
  end loop;
  return newdatastring;
end;


/

CREATE OR REPLACE FUNCTION farray_cat(firstarray floatarray,secondarray floatarray)
RETURN floatarray as
newdataarray FloatArray:=FloatArray();
i integer;

begin
i:=1;

 while(i<=firstarray.count()) loop
  newdataarray.extend();
  newdataarray(i):=firstarray(i);
  i:=i+1;
 end loop;
 i:=1;
 while(i<=secondarray.count()) loop
  newdataarray.extend();
  newdataarray(firstarray.count()+i):=secondarray(i);
  i:=i+1;
 end loop;
 return newdataarray;
end;

/

CREATE OR REPLACE FUNCTION array_to_string(varchararray varchar2array,split varchar2)
RETURN varchar2 as
newstring varchar2(32767);
i integer;
begin
 i:=1;
 while(i<=varchararray.count()) loop
  if i=1
  then
  newstring:=varchararray(i);
  else
  newstring:=newstring||split||varchararray(i);
  end if;
  i:=i+1;
 end loop;
 return newstring;
end;

/

create or replace function alpine_miner_em_train(tablename varchar2, columnname Varchar2Array, clusternumber integer, clustersize integer, maxiteration integer, epsilon binary_double, temptable Varchar2,tempbytemp Varchar2)
  RETURN Floatarray AS

sqlexecute Varchar2(32767);
alpha Floatarray:=Floatarray();
mu Floatarray:=Floatarray();
sigma Floatarray:=Floatarray();
prealpha Floatarray:=Floatarray();
premu  Floatarray:=Floatarray();
presigma Floatarray:=Floatarray();
maxsubalpha binary_double;
maxsubmu binary_double;
maxsubsigma binary_double;
maxsubvalue binary_double;
test Floatarray:=Floatarray();
columnsize integer;
tempmu Varchar2(32767);
tempsigma Varchar2(32767);
tempalpha Varchar2(32767);
sumsql varchar2(32767);
tempiteration integer;
stop integer;
i integer;
j integer;
k integer;
TYPE crt IS REF CURSOR;
myrecord crt;
tempa Floatarray:=Floatarray();
tempmuf Floatarray:=Floatarray();
tempsigmaf Floatarray:=Floatarray();
tempalphaf Floatarray:=Floatarray();
Apan Varchar2(32767);
temppeoso binary_double;
PRAGMA AUTONOMOUS_TRANSACTION;
begin

  columnsize:=columnname.count();

  sqlexecute:= ' select floatarraydiv(arraydata,cnt) as a from (
  select count(*) as cnt,floatarraysum(floatarray('||array_to_string(columnname,',')||')) as arraydata from
    (select mod( rownum,'||clusternumber||' ) as clusterid,'||array_to_string(columnname,',')||'
    from (
      select dbms_random.value as rand,'||array_to_string(columnname,',')||' from '||tablename||' where '
      ||array_to_string(columnname,' is not null and ')||' is not null order by rand
      )
    where rownum<='||clusternumber||'*'||clustersize ||') group by clusterid)';

  i:=1;
  k:=1;
  j:=0;

  open myrecord for sqlexecute;
  loop
      FETCH myrecord INTO tempa;
      EXIT WHEN myrecord%NOTFOUND;
    if j=0
    then
    mu:= tempa;
    sumsql:='(alpine_miner_em_p'||k;
    j:=1;
    else
    mu:= farray_cat(mu,tempa);
    sumsql:=sumsql||'+alpine_miner_em_p'||k;
    end if;
    alpha.extend();
    alpha(k):=1.0/clusternumber;
    k:=k+1;
    i:=i+1;
    end loop;


  sumsql:=sumsql||') as alpine_miner_em_sum ';
  for i in 1.. columnsize*clusternumber loop
  sigma.extend();
    sigma(i):=1;
  end loop;

  sqlexecute:='create table '||tempbytemp||' as ( select  '||tablename||'.*';
  for i in 1..  clusternumber loop
	
    sqlexecute:=sqlexecute||',alpine_miner_em_getp(Varchar2Array('||array_to_string(columnname,',')||'),floatarray('||GETFARANGE(mu,(i-1)*columnsize+1,(i*columnsize))||'),floatarray('||GETFARANGE(sigma,(i-1)*columnsize+1,(i*columnsize))||'),'||alpha(i)||') as alpine_miner_em_p'||i;
  
  end loop;
  sqlexecute:=sqlexecute||' from '||tablename||')';
  Apan:='call dbms_output.put_line('''||sqlexecute||''')';
  
  execute immediate 'alter session force parallel dml';
  execute immediate Apan;
  execute immediate sqlexecute;
  execute immediate 'alter session disable parallel dml';

  sqlexecute:='create table '||temptable||' as (select '||tempbytemp||'.* , '||sumsql||' from '||tempbytemp||')';
  
  execute immediate 'alter session force parallel dml';
  execute immediate sqlexecute;
  execute immediate 'alter session disable parallel dml';

  tempiteration:=2;
  stop:=0;
  while stop=0 and tempiteration<=maxiteration loop

  prealpha:=alpha;
  premu:=mu;
  presigma:=sigma;
  for i in 1..  clusternumber loop
    if i=1
    then
    tempalpha:=' floatarray(avg(alpine_miner_em_p1/alpine_miner_em_sum)';

    for j in 1 ..columnsize loop
      if j=1 then
      tempmu:=' floatarray(sum('||columnname(j)||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
      else
      tempmu:=tempmu||',sum('||columnname(j)||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
      end if;
    end loop;
    else
    tempalpha:=tempalpha||',avg(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
    for j in 1 ..columnsize loop

      tempmu:=tempmu||',sum('||columnname(j)||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';

    end loop;

    end if;
  end loop;
  tempalpha:=tempalpha||') ';
  tempmu:=tempmu||')';

  sqlexecute:=' select  '||tempalpha||' as alpha from '||temptable;
  i:=1;
  open myrecord for sqlexecute;
  loop
      FETCH myrecord INTO tempalphaf;
      EXIT WHEN myrecord%NOTFOUND;
    alpha:=tempalphaf;
    i:=i+1;
  end loop;
  sqlexecute:=' select  '||tempmu||' as mu from '||temptable;
  i:=1;
  open myrecord for sqlexecute;
  loop
      FETCH myrecord INTO tempmuf;
      EXIT WHEN myrecord%NOTFOUND;
    mu:=tempmuf;
    i:=i+1;
  end loop;

    k:=1;
    for i in 1..  clusternumber loop
      for j in 1 ..columnsize loop
        if i=1
        then
          if j=1 then

          tempsigma:=' floatarray(sum(('||columnname(j)||'- '||mu(k)||')*('||columnname(j)||'- '||mu(k)||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
          else

          tempsigma:=tempsigma||',sum(('||columnname(j)||'- '||mu(k)||')*('||columnname(j)||'- '||mu(k)||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
          end if;
        else
        tempsigma:=tempsigma||',sum(('||columnname(j)||'- '||mu(k)||')*('||columnname(j)||'- '||mu(k)||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
        end if;
        k:=k+1;
      end loop;
    end loop;
  tempsigma:=tempsigma||') ';
    sqlexecute:=' select '||tempsigma||' as sigma  from '||temptable;
  i:=1;
  open myrecord for sqlexecute;
  loop
      FETCH myrecord INTO tempsigmaf;
      EXIT WHEN myrecord%NOTFOUND;
    sigma:=tempsigmaf;
    i:=i+1;
  end loop;


  maxsubalpha:=alpine_miner_em_getmaxsub(prealpha,alpha);
  maxsubmu:=alpine_miner_em_getmaxsub(premu,mu);
  maxsubsigma:=alpine_miner_em_getmaxsub(presigma,sigma);

  if maxsubalpha>maxsubmu and maxsubalpha>maxsubsigma
  then maxsubvalue:=maxsubalpha;
  else
  if maxsubmu>maxsubalpha and maxsubmu>maxsubsigma
  then maxsubvalue:=maxsubmu;
  else
  if maxsubsigma>maxsubalpha and maxsubsigma>maxsubmu
  then maxsubvalue:=maxsubsigma;
  end if;
  end if;
  end if;
  if epsilon>maxsubvalue
  then
  stop:=1;
  end if;
  tempiteration:=tempiteration+1;
  end loop;

RETURN farray_cat(farray_cat(alpha,mu),sigma);

END;


/



CREATE OR REPLACE FUNCTION alpine_miner_em_predict(outputtable   VARCHAR2,
                                                   predicttable  VARCHAR2,
                                                   columnname    VARCHAR2array,
                                                   modelinfo     FloatArray,
                                                   clusternumber integer,
												   temptablename   VARCHAR2) 
RETURN integer AS
  sqlexecute VARCHAR2(32767);
  alpha      FloatArray:=FloatArray();
  mu         FloatArray:=FloatArray();
  sigma      FloatArray:=FloatArray();
  columnsize integer;
  sqlsum        VARCHAR2(32767);
  sqlmax        VARCHAR2(32767);
  casewhen   VARCHAR2(32767);
  updatesql  VARCHAR2(32767);
  resultsql  VARCHAR2(32767);
  i          integer;
  j          integer;
  k          integer;
  
  Apan Varchar2(32767);
  PRAGMA AUTONOMOUS_TRANSACTION;

BEGIN

  columnsize := columnname.count();

  for i in 1 .. clusternumber loop
    alpha.extend();
    alpha(i):= modelinfo(i);
    for j in 1 .. columnsize loop
    mu.extend();
      mu((i - 1) * columnsize + j) := modelinfo(clusternumber + (i - 1) * columnsize + j);
    sigma.extend();
      sigma((i - 1) * columnsize + j) := modelinfo (clusternumber +clusternumber * columnsize +(i - 1) * columnsize + j);
    end loop;
  end loop;

  for i in 1 .. clusternumber loop
    if i = 1 then
      sqlmax       := 'greatest("C(alpine_miner_emClust' || i || ')"';
      sqlsum       := '("C(alpine_miner_emClust' || i || ')"';
      updatesql := ' set "C(alpine_miner_emClust' || i ||
                   ')"="C(alpine_miner_emClust' || i || ')"/alpine_em_sum ';
    else
      sqlmax       := sqlmax || ',"C(alpine_miner_emClust' || i || ')"';
      sqlsum       := sqlsum || '+"C(alpine_miner_emClust' || i || ')"';
      updatesql := updatesql || ',"C(alpine_miner_emClust' || i ||
                   ')"="C(alpine_miner_emClust' || i || ')"/alpine_em_sum ';
    end if;
  end loop;

  sqlmax      := sqlmax || ')';
  sqlsum      := sqlsum || ') as alpine_em_sum';
  casewhen := ' case ';
  for i in 1 .. clusternumber loop
    casewhen := casewhen || ' when "C(alpine_miner_emClust' || i || ')"=' || sqlmax ||
                ' then  ' || i || ' ';
  
  end loop;

  casewhen := casewhen || ' end ';

  sqlexecute	:=	' create  table ' || temptablename ||' parallel as (select  '|| predicttable ||'.*';	  
  for i in 1 .. clusternumber loop
  
    sqlexecute:=sqlexecute||',alpine_miner_em_getp(Varchar2Array('||array_to_string(columnname,',')||'),floatarray('||GETFARANGE(mu,(i-1)*columnsize+1,(i*columnsize))||'),floatarray('||GETFARANGE(sigma,(i-1)*columnsize+1,(i*columnsize))||'),'||alpha(i)||') as "C(alpine_miner_emClust' || i ||')"';
  end loop;
  sqlexecute := sqlexecute || ' from ' || predicttable || ') ' ;
  execute immediate 'alter session force parallel dml';
  execute immediate sqlexecute;
  execute immediate 'alter session disable parallel dml';
  
  sqlexecute  := ' create  table ' || outputtable||
              ' parallel as select ' || temptablename ||'.* , ' || sqlsum || ', ' || casewhen ||
              ' alpine_em_cluster from '|| temptablename;

  execute immediate 'alter session force parallel dml';
  execute immediate sqlexecute;
  execute immediate 'alter session disable parallel dml';
        
  sqlexecute := ' update ' || outputtable || updatesql;
  execute immediate sqlexecute;
  sqlexecute := ' alter table ' || outputtable || ' drop column alpine_em_sum';
  execute immediate sqlexecute;
  return 1;
end;



/