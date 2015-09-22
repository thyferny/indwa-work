-- Function: alpine_miner_kmeans_c_array_1_5(text, text, text[], integer, text, text, text, integer, integer, integer, integer)

-- DROP FUNCTION alpine_miner_kmeans_c_array_1_5(text, text, text[], integer, text, text, text, integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION alpine_miner_kmeans_c_array_1_5(table_name text, table_name_withoutschema text, column_name text[], column_number integer, id text, tempid text, clustername text, k integer, max_run integer, max_iter integer, distance integer)
  RETURNS double precision[] AS
$BODY$
DECLARE
    run integer:=1;
    none_stable integer;
    tmp_res_1 varchar(50);
    tmp_res_2 varchar(50);
    tmp_res_3 varchar(50);
    tmp_res_4 varchar(50);
    result1 varchar(50);
    column_array text;
    avg_array text;
    x_array text;
    i integer := 0;
    j integer := 0;
    sql text;
    sql1 text:='';
    temptablename text;
    column_all text;
    comp_sql text;
    result_sql text;
    sampleid integer;
    sample_array text:='';
    data_array text;
    roww record;
    init_array text;
    init_array1 text;
    sample_array3 text[];
    sample_array1 text:='';
    sample_array2 text:='';
    column_new text:='';
     xx_array text;
     comp_sql_new text;
     alpine_id text;
     resultarray float[2];
    tempsum float;
    nullflag smallint:=0;
  
BEGIN

temptablename:=table_name_withoutschema;

if id='null'
then 
sql:= 'create temp table '||temptablename||'copy as(select *,row_number() over () '||tempid||' from '||table_name||' where ';
alpine_id:=tempid;
else
sql:= 'create temp table '||temptablename||'copy as(select * from '||table_name||' where ';
alpine_id:=id;
end if;

i := 1;
while i < (column_number) loop
	sql:=sql||' "'||column_name[i]||'" is not null and ';
	i := i + 1;	
end loop;
sql:=sql||' "'||column_name[i]||'" is not null';


sql:=sql||')distributed by('||alpine_id||')';

raise notice '----create copy sql:%',sql;
execute sql;

column_array := column_name[1];

i := 2;
while i < (column_number + 1) loop
	column_array := column_array||',"'||column_name[i]||'"';
	i := i + 1;
end loop;


-------------------------------
sql:='create temp table '||temptablename||'init as select tablek1.seq sample_id,0::smallint as stable,';
i := 1;
while i<(k + 1) loop
	sql:=sql||'k'||i||',';
	i := i + 1;
	end loop;
	sql:=sql||'0::integer as iter from';
i := 1;
 while i<(k + 1) loop
sql:=sql||'(select array[';
	 j := 1;
	while j<(column_number) loop
	  sql:=sql||'"'||column_name[j]||'",';
	  j := j + 1;
	end loop;
	sql:=sql||'"'||column_name[j]||'"] k'||i||',';
	if i=1 then sql:=sql||' row_number() over (order by random())-1 as seq from '||temptablename||'copy limit '||max_run||') as tablek'||i||' inner join ';
	else if i=k then sql:=sql||' row_number() over (order by random())-1 as seq from '||temptablename||'copy limit '||max_run||') as tablek'||i||' on tablek'||(i-1)||'.seq=tablek'||i||'.seq'; 
	else sql:=sql||' row_number() over (order by random())-1 as seq from '||temptablename||'copy limit '||max_run||') as tablek'||i||' on tablek'||(i-1)||'.seq=tablek'||i||'.seq inner join ';end if;
	end if;
	i := i + 1;
 end loop;
sql:=sql||'  distributed by (sample_id) ';
raise notice '----sql sql:%',sql;
execute sql;

sql:='create temp table '||temptablename||'_random_new as select sample_id,stable,';
i := 1;
while i<column_number+1 loop
	sql:=sql||'array[';
	j:=1;
	while j<k loop
	sql:=sql||'k'||j||'['||i||'],';
	j := j + 1;
	end loop;
	sql:=sql||'k'||j||'['||i||']]::float[] "'||column_name[i]||'",';
	i := i + 1;
	end loop;
	sql:=sql||' iter from '||temptablename||'init distributed by (sample_id)';
raise notice '----create random sql:%',sql;
execute sql;
-------------------------------
------generate random table start------

i := 1;
sample_array1:=sample_array1||'array[';
while i<(k + 1) loop
	 j := 1;
	while j<(column_number+1) loop
	if i=k and j=column_number then
	 sample_array1:=sample_array1||'y."'||column_name[j]||'"['||i||']]::float[]';
	 else
	 sample_array1:=sample_array1||'y."'||column_name[j]||'"['||i||'],';
	  end if;
	  j := j + 1;
	end loop;
	i := i + 1;
 end loop;
 
raise notice '----sample_array1 sql:%',sample_array1;
---------generate random table end-------------

---------Adjust stable start--------------
while run<=max_iter  loop
tmp_res_1:='tmp_res_1' ;
tmp_res_2:='tmp_res_2';
tmp_res_3:='tmp_res_3';
tmp_res_4:='tmp_res_4';
i := 2;
avg_array :=  'avg("'||column_name[1]||'")::numeric(25,10) "'||column_name[1]||'"';
while i < (column_number + 1) loop
        avg_array := avg_array||',avg("'||column_name[i]||'")::numeric(25,10) "'||column_name[i]||'"';
	i := i + 1;
end loop;


------------------

data_array:='array[';
i:=1;
while i<column_number loop
data_array:=data_array||'x."'||column_name[i]||'",';
i:=i+1;
end loop;
data_array:=data_array||'x."'||column_name[i]||'"]';

---------------
if run=1
then 
sql:='create temp table '||temptablename||tmp_res_2||' (sample_id integer,'||alpine_id||' character varying,cluster_id integer) distributed by ('||alpine_id||')';

execute sql;
end if;
i:=1;
sql:='select sample_id::smallint,'||sample_array1||'::float[] from '||temptablename||'_random_new y where stable=0 order by sample_id';
-- raise notice 'sql:%',sql;
     for roww in execute sql loop
	 sample_array3=roww.array;
	 sampleid=roww.sample_id;
	 sample_array2:='';
	j:=1;
	while j<column_number*k loop
	sample_array2:=sample_array2||sample_array3[j]||',';
	j:=j+1;
	end loop;
	sample_array2:=sample_array2||sample_array3[j];
	sample_array2:='array['||sample_array2||']';
	sql1:='insert into '||temptablename||tmp_res_2||' select '||sampleid||'::smallint,'||alpine_id||',alpine_miner_kmeans_distance_loop('||sample_array2;
	sql1:=sql1||'::float[],'||data_array||'::float[],'||k||','||distance||')as cluster_id from '||temptablename||'copy x';
	  --raise notice 'sqll:%',sql1;
	 execute sql1;
	 i:=i+1;
     end loop;
---------------


--------tmp_res_2 caculate each point in random table's distance to each point in date table and get each point in date table should belong to which cluster----------
/*sql:='drop table if exists '||temptablename||tmp_res_2||';create temp table '||temptablename||tmp_res_2||' as (select 
	sample_id,'||id||',alpine_miner_kmeans_distance_loop('||sample_array1||'::float[],'||data_array||'::float[],'||k||','||distance||') as cluster_id
from '||temptablename||'copy x inner join '||temptablename||'_random_new y on y.stable=0) distributed by (sample_id,'||id||',cluster_id)';

execute sql;*/

-----tmp_res_1 caculate unstable cluster---
if run=1
then 
sql:=' create temp table '||temptablename||tmp_res_1||'   as';
sql:=sql||'(
select 
	sample_id,
 	cluster_id,
	'||avg_array||'
from '||temptablename||tmp_res_2||'
x,'||temptablename||'copy y
where x.'||alpine_id||'=y.'||alpine_id||'
 group by 1,2
)distributed by(sample_id,cluster_id)
 ;
 ';

else 
execute ' truncate table '||temptablename||tmp_res_1;
sql:='insert into  '||temptablename||tmp_res_1  ;
sql:=sql||'(
select 
	sample_id,
 	cluster_id,
	'||avg_array||'
from '||temptablename||tmp_res_2||'
x,'||temptablename||'copy y
where x.'||alpine_id||'=y.'||alpine_id||'
 group by 1,2
) ';
end if;

execute sql;
 
--raise info '------------1--------------'; 

if run=1
then 
sql:=' create temp table '||temptablename||'temp   as';
else 
execute ' truncate table '||temptablename||'temp ';
sql:='insert into  '||temptablename||'temp '  ;
end if;
sql:=sql||' select tablek1.sample_id,0::smallint as stable,';
i := 1;
 while i<(k + 1) loop
	sql:=sql||'k'||i||',';
	i := i + 1;	
 end loop;
sql:=sql||'0::integer as iter from ';

i := 1;
 while i<(k + 1) loop
sql:=sql||'(select array[';
	 j := 1;
	while j<(column_number) loop
	  sql:=sql||'"'||column_name[j]||'",';
	  j := j + 1;
	end loop;
	sql:=sql||'"'||column_name[j]||'"] k'||i||',';
	if i=1 then sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' inner join ';
	else if i=k then sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' on tablek'||(i-1)||'.sample_id=tablek'||i||'.sample_id'; 
	else sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' on tablek'||(i-1)||'.sample_id=tablek'||i||'.sample_id inner join ';end if;
	end if;
	i := i + 1;
 end loop;
if run=1
then 
sql:=sql||'  distributed by (sample_id) ';
 
end if;
 
 

execute sql;
if run=1
then 
sql:=' create temp table '||temptablename||tmp_res_4||'   as';
else 
execute ' truncate table '||temptablename||tmp_res_4;
sql:='insert into  '||temptablename||tmp_res_4  ;
end if;
sql:=sql||' select sample_id,stable,';
i := 1;
while i<column_number+1 loop
	sql:=sql||'array[';
	j:=1;
	while j<k loop
	sql:=sql||'k'||j||'['||i||'],';
	j := j + 1;
	end loop;
	sql:=sql||'k'||j||'['||i||']]::float[] "'||column_name[i]||'",';
	i := i + 1;
	end loop;
if run=1
then 
sql:=sql||' iter from '||temptablename||'temp distributed by (sample_id)';
else 
sql:=sql||' iter from '||temptablename||'temp  ';
end if;
	 
raise notice '----create random sql:%',sql;
execute sql;

comp_sql_new:='(case when ';
i:=1;
while i<(k + 1) loop
j:=1;
  while j<column_number+1 loop
  if i=k and j=column_number then comp_sql_new:=comp_sql_new||'x."'||column_name[j]||'"['||i||']=y."'||column_name[j]||'"['||i||']';
	else comp_sql_new:=comp_sql_new||'x."'||column_name[j]||'"['||i||']=y."'||column_name[j]||'"['||i||'] and ';
	end if;
	j:=j+1;
  end loop;
  i:=i+1;
end loop;
comp_sql_new:=comp_sql_new||' then 1 else 0 end )as stable';
raise notice '----comp_sql_new :%',comp_sql_new;
------------

xx_array:='';
i := 1;
 while i<(column_number + 1) loop
 j:=1;
	xx_array:=xx_array||'array[';
	while j<k loop
	xx_array:=xx_array||'x."'||column_name[i]||'"['||j||'],';
	  j := j + 1;
	end loop;
	xx_array:=xx_array||'x."'||column_name[i]||'"['||j||']]::float[] "'||column_name[i]||'",';
	i := i + 1;
 end loop;
 raise notice 'xx_array:%',xx_array;
 
 --------tmp_res_3 judge which sample is stable----

if run=1
then 
sql:=' create temp table '||temptablename||tmp_res_3||'   as';
else 
execute ' truncate table '||temptablename||tmp_res_3;
sql:='insert into  '||temptablename||tmp_res_3  ;
end if;
sql:=sql||' 
(
	select 
		x.sample_id,
	 	'||comp_sql_new||','||xx_array
	 	||run||' as iter
	from  '||temptablename||tmp_res_4||' x, '||temptablename||'_random_new  y
	where x.sample_id=y.sample_id

)';
if run=1
then 
sql:=sql||'
distributed by(sample_id)
;
'; 
end if;
 
----------------

execute sql;



sql:='insert into '||temptablename||tmp_res_3||' (select a.* from '||temptablename||'_random_new a left join '||temptablename||tmp_res_3||' as b on a.sample_id=b.sample_id';
sql:=sql||' where b.sample_id is null);';

--if run=1
--then 
--sql:=' create temp table '||temptablename||'temp1   as select * from '||temptablename||tmp_res_3||' distributed by (sample_id);';
--else 
execute ' truncate table '||temptablename||'_random_new';
sql:='insert into  '||temptablename||'_random_new  select * from '||temptablename||tmp_res_3||'  ' ;
--end if;
 
execute sql;
 

--sql:= 'drop table if exists '||temptablename||'_random_new;';
--sql:=sql||'alter table '||temptablename||'temp1 rename to '||temptablename||'_random_new;';
--execute sql;


execute 'select count(*)  from  '||temptablename||'_random_new where stable=0;' into none_stable;--into '||none_stable||'


raise notice '-------------------none_stable:%',none_stable;

if none_stable=0
then
	exit;
end if;

run := run+1;

end loop;

---------Adjust stable end--------------

sql:='select array[sample_id,len]
from
(
	select sample_id,len,row_number() over(order by len) as seq 
	from
	(
		select sample_id,avg(len) as len
		from
		(
		select sample_id,alpine_miner_kmeans_distance_result('||sample_array1||'::float[],'||data_array||'::float[],'||k||','||distance||') as len
			from '||temptablename||'copy x inner join '||temptablename||'_random_new y on y.stable=1
			)a
		 	group by 1
		)b
)z
where seq=1';

raise notice '----get sample sql:%',sql;

execute sql into resultarray;
sampleid:=resultarray[1];

if sampleid is null then 
sampleid:=0;
nullflag:=1;
 end if;

--------deal result---------------- in (select sample_id from '||temptablename||'tmp_res_4) 
result1:='result1';
execute 'drop table if exists '||temptablename||result1;

execute 'create temp table '||temptablename||result1||' as 
(
	select * from  '||temptablename||'_random_new  where sample_id ='||sampleid||'
)distributed by(sample_id);'
;


if nullflag=1 then
sql:='select len
from
(
	select sample_id,len,row_number() over(order by len) as seq 
	from
	(
		select sample_id,avg(len) as len
		from
		(
		select sample_id,alpine_miner_kmeans_distance_result('||sample_array1||'::float[],'||data_array||'::float[],'||k||','||distance||') as len
			from '||temptablename||'copy x inner join '||temptablename||result1||' y on y.stable=0
			)a
		 	group by 1
		)b
)z
where seq=1';
execute sql into tempsum;
raise notice '-------------------tempsum:%',tempsum;
resultarray[2]:=tempsum;
end if;

execute 'drop table if exists '||temptablename||'result2; create temp table '||temptablename||'result2 as select *,0::integer '||temptablename||'copy_flag from '||temptablename||'copy  distributed randomly;';





sql:='
	drop table if exists '||temptablename||'table_name_temp;create temp table '||temptablename||'table_name_temp as
		(
		select '||alpine_id||' as temp_id,alpine_miner_kmeans_distance_loop('||sample_array1||'::float[],'||data_array||'::float[],'||k||','||distance||') as '||clustername||' 
		from '||temptablename||'result2 x inner join '||temptablename||result1||' y on x.'||temptablename||'copy_flag=0

		)  distributed randomly ;';

execute sql;

resultarray[1]:=run;

RETURN resultarray;
 
END;
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION alpine_miner_kmeans_c_array_1_5(text, text, text[], integer, text, text, text, integer, integer, integer, integer) OWNER TO gpadmin;
