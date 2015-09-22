create or replace function alpine_em_getp(columndata Float, mu Float,sigma Float)
returns float 
begin
	declare peoso float default 0.0;
	set peoso=(columndata-mu)*(columndata-mu)/sigma;
	return peoso;
end@


create or replace function alpine_miner_em_getmaxsub(firstarray FloatArray,secondarray FloatArray) 
returns float 
begin
	declare tempsub float default 0.0;
	declare maxsub float default 0.0;
	declare array_count int default 0;
	declare i int default 1;
	set array_count = CARDINALITY(firstarray);
	
	while (i <= array_count) do
		set tempsub=abs(firstarray[i]-secondarray[i]);
		if tempsub>maxsub then set maxsub=tempsub;
		end if;
		set i=i+1;
	end while;
	return maxsub;
end@


create or replace function GETFARANGE(farray       FloatArray,
                                                       elementfrom integer,
                                                       elementend integer)
returns FloatArray
begin
	declare newdataarray FloatArray;
	declare i int default 1;
	declare j int default 1;
	declare array_count int default 0;
	set array_count = CARDINALITY(farray);

	while(i<=array_count) do
	if (i >=elementfrom and i <= elementend) then
		set newdataarray[j]=farray[i];
		set j=j+1;
	end if;
	set i=i+1;
	end while;
	return newdataarray;
end@


create or replace function farray_cat(firstarray floatarray,secondarray floatarray)
returns FloatArray 

begin
	declare newdataarray FloatArray;
	declare i int default 1;
	declare farray_count int default 0;
	declare sarray_count int default 0;
	set farray_count = CARDINALITY(firstarray);
	set sarray_count = CARDINALITY(secondarray);

	while(i<=farray_count) do
		set newdataarray[i]=firstarray[i];
		set i=i+1;
	end while;
	set i=1;
	while(i<=sarray_count) do
		set newdataarray[farray_count+i]=secondarray[i];
		set i=i+1;
	end while;
	return newdataarray;
end@


create or replace procedure alpine_miner_em_train(
													tablename varchar(32672), 
													columnname VarcharArray, 
													clusternumber integer, 
													clustersize integer, 
													maxiteration integer, 
													epsilon float, 
													temptable varchar(32672),
													tempbytemp varchar(32672),
													sigmas Floatarray,
													out resultarray Floatarray)
begin
declare sqlexecute varchar(32672);
declare sqlinsert varchar(32672);
declare notnullsql varchar(32672);
declare alpha Floatarray;
declare mu Floatarray;
declare sigma Floatarray;
declare sigmaValue Float default 1.0;
declare prealpha Floatarray;
declare premu  Floatarray;
declare presigma Floatarray;
declare maxsubalpha float;
declare maxsubmu float;
declare maxsubsigma float;
declare maxsubvalue float;
declare meanpeoso float;
declare test Floatarray;
declare columnsize integer;
declare tempmu varchar(32672);
declare tempsigma varchar(32672);
declare tempalpha varchar(32672);
declare sumsql varchar(32672);
declare tempiteration integer;
declare stop integer;
declare i integer default 1;
declare j integer default 0;
declare k integer default 1;
declare flag integer default 1;
declare tempa Floatarray;
declare Apan varchar(32672);
declare temppeoso float;
 
DECLARE SQLSA VARCHAR(32672);
declare rowVariable1012 row1012; 

DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE my_cursor CURSOR WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found SET at_end = 1;
if (tablename is null or columnname is null or clusternumber is null or temptable is null) then
	return ;
end if;
 
set columnsize=CARDINALITY(columnname);
set sqlexecute='select ';
while(i<=columnsize) do
if i=1 then
	set sqlexecute=sqlexecute||'avg(double(foo1.'||columnname[i]||')) as c'||i;
	set notnullsql=tablename||'.'||columnname[i]||' is not null ';
	else
	set sqlexecute=sqlexecute||',avg(double(foo1.'||columnname[i]||')) as c'||i;
	set notnullsql=notnullsql||' and '||tablename||'.'||columnname[i]||' is not null ';
end if;
set i=i+1;
end while;
while(i<=1012) do
set sqlexecute=sqlexecute||',null as c'||i;
set i=i+1;
end while;
set sqlexecute=sqlexecute||' from (select mod(row_number()over(),'||clusternumber||') as clusterid,foo.* from (select rand() as rand,'||tablename||'.* from '||tablename||' where '||notnullsql||' order by rand) as foo) as foo1 group by foo1.clusterid';

PREPARE SQLSA FROM sqlexecute ;
OPEN  my_cursor ;
set i = 0;
fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
		if at_end <> 0 then LEAVE fetch_loop;
    	end if;
		if i=1 then
		set mu = alpine_miner_row_to_array(rowVariable1012, columnsize);
		set sumsql='(alpine_miner_em_p'||k;
		else
		set mu= farray_cat(mu,alpine_miner_row_to_array(rowVariable1012, columnsize));
		set sumsql=sumsql||'+alpine_miner_em_p'||k;
		end if;
		set alpha[k]=1.0/clusternumber;
		set k=k+1;
	end LOOP;
close my_cursor;
		

set sumsql=sumsql||') as alpine_miner_em_sum ';
set i=1;
while(i<=columnsize*clusternumber) do
set sigma[i]=sigmas[i];
set i=i+1;
end while;


set sqlexecute='create table '||tempbytemp||' as ';
set sqlinsert='( select '||tablename||'.*';
set i=1;
while(i<=clusternumber) do
set j=1;
set sqlinsert=sqlinsert||','||alpha[i]||'*exp(-0.5*(';
	while(j<=columnsize) do
	if j=1 then
	set sigmaValue=sigma[(i-1)*columnsize+j];
	set sqlinsert=sqlinsert||'alpine_em_getp('||columnname[j]||','||mu[(i-1)*columnsize+j]||','||sigma[(i-1)*columnsize+j]||')';
	else
	set sigmaValue=sigmaValue*sigma[(i-1)*columnsize+j];
	set sqlinsert=sqlinsert||'+alpine_em_getp('||columnname[j]||','||mu[(i-1)*columnsize+j]||','||sigma[(i-1)*columnsize+j]||')';
	end if;
	set j=j+1;
	end while;
set sqlinsert=sqlinsert||'))/sqrt('||sigmaValue||') as alpine_miner_em_p'||i;
set i=i+1;
end while;
set sqlinsert=sqlinsert||' from '||tablename||' where '||notnullsql||' )';
set sqlexecute=sqlexecute||sqlinsert||' definition only';

  execute immediate sqlexecute;

  set sqlexecute='insert into '||tempbytemp||' '||sqlinsert;
  execute immediate sqlexecute;
  set sqlexecute='create table '||temptable||' as (select '||tempbytemp||'.* , '||sumsql||' from '||tempbytemp||') definition only';
  execute immediate sqlexecute;
  set sqlexecute='insert into '||temptable||' (select '||tempbytemp||'.* , '||sumsql||' from '||tempbytemp||')';

  execute immediate sqlexecute;
  set meanpeoso=1.0/clusternumber;
set sqlexecute='update '||temptable||' set alpine_miner_em_p1=(case when alpine_miner_em_p1<1e-22 and alpine_miner_em_sum<1e-20 then '||meanpeoso||' else alpine_miner_em_p1 end)';
  set i=2;
while(i<=clusternumber) do
  set sqlexecute=sqlexecute||',alpine_miner_em_p'||i||'=(case when alpine_miner_em_p'||i||'<1e-22 and alpine_miner_em_sum<1e-20 then '||meanpeoso||' else alpine_miner_em_p'||i||' end)';
  set i=i+1;
end while;

  execute immediate sqlexecute;
  
   set sqlexecute='update '||temptable||' set alpine_miner_em_sum=alpine_miner_em_p1';
  set i=2;
while(i<=clusternumber) do
  set sqlexecute=sqlexecute||'+alpine_miner_em_p'||i;
  set i=i+1;
end while;

  execute immediate sqlexecute;

  set tempiteration=2;
  set stop=0;

while(stop=0 and tempiteration<=maxiteration) do

	set prealpha=alpha;
	set premu=mu;
	set presigma=sigma;
	set i=1;
	set k=1;
	while(i<=clusternumber) do
		if i=1
		then
			set tempalpha=' avg(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||i;
			set j=1;
			while(j<=columnsize) do 
				if j=1 then
				set tempmu=' sum('||columnname[j]||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||k;
				else
				set tempmu=tempmu||',sum('||columnname[j]||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||k;
				end if;
				set k=k+1;
				set j=j+1;
			end while;
		else
			set tempalpha=tempalpha||',avg(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
			set j=1;
			while(j<=columnsize) do 
				set tempmu=tempmu||',sum('||columnname[j]||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||k;
			set j=j+1;
			set k=k+1;
			end while;
		end if;
		set i=i+1;
	end while;
 
	while(i<=1012) do
	set tempalpha=tempalpha||',null as c'||i;
	set i=i+1;
	end while;

	set i=columnsize*clusternumber+1;
	while(i<=1012) do
	set tempmu=tempmu||',null as c'||i;
	set i=i+1;
	end while;
	
	set sqlexecute=' select '||tempalpha||' from '||temptable;
	set at_end=0;
	PREPARE SQLSA FROM sqlexecute ;
	OPEN  my_cursor ;
	set i = 0;
	fetch_loop1:
		LOOP
			set i = i + 1;
			fetch my_cursor into rowVariable1012;
			if at_end <> 0 then LEAVE fetch_loop1;
			end if;
			if i=1 then
			set alpha = alpine_miner_row_to_array(rowVariable1012, clusternumber);
			end if;
		end LOOP;
	close my_cursor;
	
	set sqlexecute=' select  '||tempmu||' from '||temptable;
	set at_end=0;
	PREPARE SQLSA FROM sqlexecute ;
	OPEN  my_cursor ;
	set i = 0;
	fetch_loop2:
		LOOP
			set i = i + 1;
			fetch my_cursor into rowVariable1012;
			if at_end <> 0 then LEAVE fetch_loop2;
			end if;
			if i=1 then
			set mu = alpine_miner_row_to_array(rowVariable1012, clusternumber*columnsize);
			end if;
		end LOOP;
	close my_cursor;
	
 
    set k=1;
	set i=1;
	set j=1;
	while(i<=clusternumber) do
		set j=1;
		while(j<=columnsize) do 
      
			if i=1
			then
				if j=1 then
				set tempsigma=' sum(('||columnname[j]||'- '||mu[k]||')*('||columnname[j]||'- '||mu[k]||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||k;
				else
				set tempsigma=tempsigma||',sum(('||columnname[j]||'- '||mu[k]||')*('||columnname[j]||'- '||mu[k]||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||k;
				end if;
			else
			set tempsigma=tempsigma||',sum(('||columnname[j]||'- '||mu[k]||')*('||columnname[j]||'- '||mu[k]||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum) as c'||k;
			end if;
			set k=k+1;
			set j=j+1;
		end while;
		set i=i+1;
    end while;
	
	
	set i=columnsize*clusternumber+1;
	while(i<=1012) do
	set tempsigma=tempsigma||',null as c'||i;
	set i=i+1;
	end while;
	
 
    set sqlexecute=' select '||tempsigma||' from '||temptable;
	set at_end=0;
	PREPARE SQLSA FROM sqlexecute ;
	OPEN  my_cursor ;
	set i = 0;
	fetch_loop3:
		LOOP
			set i = i + 1;
			fetch my_cursor into rowVariable1012;
			if at_end <> 0 then LEAVE fetch_loop3;
			end if;
			if i=1 then
			set sigma = alpine_miner_row_to_array(rowVariable1012, clusternumber*columnsize);
			end if;
		end LOOP;
	close my_cursor;
	
	set maxsubalpha=alpine_miner_em_getmaxsub(prealpha,alpha);
	set maxsubmu=alpine_miner_em_getmaxsub(premu,mu);
	set maxsubsigma=alpine_miner_em_getmaxsub(presigma,sigma);

	if maxsubalpha>maxsubmu and maxsubalpha>maxsubsigma
	then set maxsubvalue=maxsubalpha;
	else
		if maxsubmu>maxsubalpha and maxsubmu>maxsubsigma
		then set maxsubvalue=maxsubmu;
		else
			if maxsubsigma>maxsubalpha and maxsubsigma>maxsubmu
			then set maxsubvalue=maxsubsigma;
			end if;
		end if;
	end if;
	if epsilon>maxsubvalue
	then
	set stop=1;
	end if;
	set tempiteration=tempiteration+1;
	
end while;

	set resultarray=farray_cat(farray_cat(alpha,mu),sigma);

END@
 

CREATE OR REPLACE procedure alpine_miner_em_predict(outputtable   VARCHAR(32672),
                                                   predicttable  VARCHAR(32672),
                                                   columnname    VARCHARarray,
                                                   alpha     FloatArray,
												   mu     FloatArray,
												   sigma     FloatArray,
                                                   clusternumber integer,
												   temptablename   VARCHAR(32672)) 
BEGIN
  declare sqlexecute VARCHAR(32672);
  declare sqlinsert VARCHAR(32672);
  declare modelsize integer default 0;
  declare sqlsum        VARCHAR(32672);
  declare sqlmax        VARCHAR(32672);
  declare casewhen   VARCHAR(32672);
  declare updatesql  VARCHAR(32672);
  declare resultsql  VARCHAR(32672);
  declare i          integer default 1;
  declare j          integer default 1;
  declare k          integer default 1;
  declare columnsize          integer default 0;
  declare sigmaValue          float default 0;
  
  declare Apan Varchar(32672);

  set columnsize=CARDINALITY(columnname);


	set i=1;
	while(i<=clusternumber) do
    if i = 1 then
		set sqlmax       = 'greatest("C(alpine_miner_emClust' || i || ')"';
		set sqlsum       = '("C(alpine_miner_emClust' || i || ')"';
		set updatesql    = ' set "C(alpine_miner_emClust' || i ||
                   ')"="C(alpine_miner_emClust' || i || ')"/alpine_em_sum ';
    else
		set sqlmax       = sqlmax || ',"C(alpine_miner_emClust' || i || ')"';
		set sqlsum       = sqlsum || '+"C(alpine_miner_emClust' || i || ')"';
		set updatesql    = updatesql || ',"C(alpine_miner_emClust' || i ||')"="C(alpine_miner_emClust' || i || ')"/alpine_em_sum ';
    end if;
	set i=i+1;
	end while;

	set sqlmax      = sqlmax || ')';
	set sqlsum      = sqlsum || ') as alpine_em_sum';
	set casewhen    = ' case ';
	
	set i=1; 
	while(i<=clusternumber) do
    set casewhen    = casewhen || ' when "C(alpine_miner_emClust' || i || ')"=' || sqlmax ||
                ' then  ' || i || ' ';
	set i=i+1;
	end while;

	set casewhen    = casewhen || ' end ';

	set sqlinsert=' (select  '|| predicttable ||'.*';
	set i=1;
	while(i<=clusternumber) do
		set j=1;
		set sqlinsert=sqlinsert||','||alpha[i]||'*exp(-0.5*(';
		while(j<=columnsize) do
			if j=1 then
			set sigmaValue=sigma[(i-1)*columnsize+j];
			set sqlinsert=sqlinsert||'alpine_em_getp('||columnname[j]||','||mu[(i-1)*columnsize+j]||','||sigma[(i-1)*columnsize+j]||')';
			else
			set sigmaValue=sigmaValue*sigma[(i-1)*columnsize+j];
			set sqlinsert=sqlinsert||'+alpine_em_getp('||columnname[j]||','||mu[(i-1)*columnsize+j]||','||sigma[(i-1)*columnsize+j]||')';
			end if;
			set j=j+1;
		end while;
		set sqlinsert=sqlinsert||'))/sqrt('||sigmaValue||') as "C(alpine_miner_emClust' || i ||')"';
		set i=i+1;
	end while;
	
	set sqlinsert=sqlinsert||' from ' || predicttable || ') ';
	
	set sqlexecute = 'create  table ' || temptablename ||' as ' || sqlinsert||' definition only';
	

	execute immediate sqlexecute;
	
	set sqlexecute='insert into '|| temptablename ||sqlinsert;
	execute immediate sqlexecute;

	set sqlinsert= ' (select ' || temptablename ||'.* , ' || sqlsum || ', ' || casewhen ||' alpine_em_cluster from '|| temptablename||' ) ';
	set sqlexecute = ' create  table ' || outputtable ||' as '||sqlinsert||' definition only';
	execute immediate sqlexecute;
	
	set sqlexecute = ' insert into ' || outputtable ||' '||sqlinsert;
	execute immediate sqlexecute;

        
  set sqlexecute = ' update ' || outputtable || updatesql;
  execute immediate sqlexecute;
  set sqlexecute = ' alter table ' || outputtable || ' drop column alpine_em_sum';
  execute immediate sqlexecute;
  return 1;
end@
