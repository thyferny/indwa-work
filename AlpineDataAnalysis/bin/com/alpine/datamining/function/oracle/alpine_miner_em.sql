
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

create or replace function alpine_miner_em_getp(columnarray varchar2array, mu FloatArray,sigma FloatArray,alpha binary_double)
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
  newdatastring:='';
  while(i<=farray.count()) loop
  if (i >=elementfrom and i <= elementend) then
    if j=1 then
	   newdatastring:=newdatastring||farray(i);
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
RETURN clob as
newstring clob;
i integer;
begin
 i:=1;
 while(i<=varchararray.count()) loop
  if i=1
  then
  newstring:=varchararray(i);
  else
  dbms_lob.append(newstring, split||varchararray(i));
  end if;
  i:=i+1;
 end loop;
 return newstring;
end;

/

create or replace function alpine_miner_em_train(tablename varchar2, columnname varchar2array, clusternumber integer, clustersize integer, maxiteration integer, epsilon binary_double, temptable varchar2,tempbytemp varchar2)
  RETURN Floatarray AS

sqlexecute clob;
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
tempmu clob;
tempsigma clob;
tempalpha clob;
sumsql clob;
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
Apan clob;
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
	dbms_lob.append(sumsql,'+alpine_miner_em_p'||k);
    end if;
    alpha.extend();
    alpha(k):=1.0/clusternumber;
    k:=k+1;
    i:=i+1;
    end loop;

  dbms_lob.append(sumsql,') as alpine_miner_em_sum ');

  for i in 1.. columnsize*clusternumber loop
  sigma.extend();
    sigma(i):=1;
  end loop;

  sqlexecute:='create table '||tempbytemp||' as ( select  '||tablename||'.*';
  for i in 1..  clusternumber loop
	dbms_lob.append(sqlexecute,',alpine_miner_em_getp(varchar2array('||array_to_string(columnname,',')||'),floatarray('||GETFARANGE(mu,(i-1)*columnsize+1,(i*columnsize))||'),floatarray('||GETFARANGE(sigma,(i-1)*columnsize+1,(i*columnsize))||'),'||alpha(i)||') as alpine_miner_em_p'||i);
  end loop;
  sqlexecute:=sqlexecute||' from '||tablename||')';

  execute immediate 'alter session force parallel dml';
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
	  dbms_lob.append(tempmu,',sum('||columnname(j)||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)');
      end if;
    end loop;
    else
	dbms_lob.append(tempalpha,',avg(alpine_miner_em_p'||i||'/alpine_miner_em_sum)');

    for j in 1 ..columnsize loop
		dbms_lob.append(tempmu,',sum('||columnname(j)||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)');
    end loop;

    end if;
  end loop;
  dbms_lob.append(tempalpha,') ');
  dbms_lob.append(tempmu,') ');

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

    for i in 1..  clusternumber loop
      for j in 1 ..columnsize loop
		k:=(i-1)*columnsize+j;
        if i=1
        then
          if j=1 then

          tempsigma:=' floatarray(sum(('||columnname(j)||'- '||mu(k)||')*('||columnname(j)||'- '||mu(k)||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
          else
			dbms_lob.append(tempsigma,',sum(('||columnname(j)||'- '||mu(k)||')*('||columnname(j)||'- '||mu(k)||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)');
          end if;
        else
		dbms_lob.append(tempsigma,',sum(('||columnname(j)||'- '||mu(k)||')*('||columnname(j)||'- '||mu(k)||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)');
        end if;
      end loop;
    end loop;
	dbms_lob.append(tempsigma,') ');

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


CREATE OR REPLACE FUNCTION alpine_miner_em_predict(outputtable   varchar2,
                                                   predicttable  varchar2,
                                                   columnname    varchar2array,
                                                   modelinfo     FloatArray,
                                                   clusternumber integer,
												   temptablename   varchar2) 
RETURN integer AS
  sqlexecute clob;
  alpha      FloatArray:=FloatArray();
  mu         FloatArray:=FloatArray();
  sigma      FloatArray:=FloatArray();
  columnsize integer;
  sqlsum        clob;
  sqlmax        clob;
  casewhen   clob;
  updatesql  clob;
  resultsql  clob;
  i          integer;
  j          integer;
  k          integer;
  
  Apan clob;
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
	dbms_lob.append(sqlmax,',"C(alpine_miner_emClust' || i || ')"');
	dbms_lob.append(sqlsum,'+"C(alpine_miner_emClust' || i || ')"');
	dbms_lob.append(updatesql,',"C(alpine_miner_emClust' || i ||')"="C(alpine_miner_emClust' || i || ')"/alpine_em_sum ');
    end if;
  end loop;

  dbms_lob.append(sqlmax,')');
  dbms_lob.append(sqlsum,') as alpine_em_sum');

  casewhen := ' case ';
  for i in 1 .. clusternumber loop
	dbms_lob.append(casewhen,' when "C(alpine_miner_emClust' || i || ')"=' || sqlmax ||' then  ' || i || ' ');
  end loop;
	dbms_lob.append(casewhen,' end ');

  sqlexecute	:=	' create  table ' || temptablename ||' parallel as (select  '|| predicttable ||'.*';	  
  for i in 1 .. clusternumber loop
	dbms_lob.append(sqlexecute,',alpine_miner_em_getp(varchar2array('||array_to_string(columnname,',')||'),floatarray('||GETFARANGE(mu,(i-1)*columnsize+1,(i*columnsize))||'),floatarray('||GETFARANGE(sigma,(i-1)*columnsize+1,(i*columnsize))||'),'||alpha(i)||') as "C(alpine_miner_emClust' || i ||')"');

  end loop;
  dbms_lob.append(sqlexecute,' from ' || predicttable || ') ');

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