-- Function: alpine_miner_kmeans_sp_1_5(text, text, text[], integer, text, text, text, integer, integer, integer, integer)

-- DROP FUNCTION alpine_miner_kmeans_sp_1_5(text, text, text[], integer, text, text, text, integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION alpine_miner_kmeans_sp_1_5(table_name text, table_name_withoutschema text, column_name text[], column_number integer, id text, tempid text, clustername text, k integer, max_run integer, max_iter integer, distance integer)
  RETURNS integer AS
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
    columnname text;
    avg_array text;
    power_array text;
    d_array text;
    d_array1 text;
    x_array text;
    comp_array text;
    i integer := 0;
    j integer := 0;
    sql text;
    temptablename text;
    column_all text;
    caculate_array text;
    comp_sql text;
    result_sql text;
    sampleid integer;
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


sql:=sql||')';

execute sql;

column_array := column_name[1];

i := 2;
while i < (column_number + 1) loop
	column_array := column_array||',"'||column_name[i]||'"';
	i := i + 1;
end loop;

------generate random table start------

sql:='select tablek1.seq sample_id,0::smallint as stable,';
column_all:='';
i := 1;
 while i<(k + 1) loop
 j := 1;
	while j<(column_number+1) loop
		column_all:=column_all||'"k'||i||''||column_name[j]||'"::numeric(25,10),';
		j := j + 1;
		end loop;
	i := i + 1;	
 end loop;
sql:=sql||column_all||'0::integer as iter from ';
--random table's line count is variable max_run,default value is 10--
--The point in same sample is in same row
i := 1;
 while i<(k + 1) loop
sql:=sql||'(select ';
	 j := 1;
	while j<(column_number+1) loop
	  sql:=sql||'"'||column_name[j]||'" "k'||i||column_name[j]||'",';
	  j := j + 1;
	end loop;
	if i=1 then sql:=sql||' row_number() over (order by random())-1 as seq from '||temptablename||'copy limit '||max_run||') as tablek'||i||' inner join ';
	else if i=k then sql:=sql||' row_number() over (order by random())-1 as seq from '||temptablename||'copy limit '||max_run||') as tablek'||i||' on tablek'||(i-1)||'.seq=tablek'||i||'.seq'; 
	else sql:=sql||' row_number() over (order by random())-1 as seq from '||temptablename||'copy limit '||max_run||') as tablek'||i||' on tablek'||(i-1)||'.seq=tablek'||i||'.seq inner join ';end if;
	end if;
	i := i + 1;
 end loop;

sql:='create temp table '||temptablename||'_random_new as ('||sql||') ';
raise notice 'sql:%',sql;
execute sql;

---------generate random table end-------------

---------Adjust stable start--------------
while run<=max_iter  loop
tmp_res_1:='tmp_res_1'||run::varchar;
tmp_res_2:='tmp_res_2'||run::varchar;
tmp_res_3:='tmp_res_3'||run::varchar;
tmp_res_4:='tmp_res_4'||run::varchar;
i := 2;
avg_array :=  'avg("'||column_name[1]||'")::numeric(25,10) "'||column_name[1]||'"';
while i < (column_number + 1) loop
        avg_array := avg_array||',avg("'||column_name[i]||'")::numeric(25,10) "'||column_name[i]||'"';
	i := i + 1;
end loop;


i := 0;
j := 0;
d_array := 'case ';
d_array1 := 'case ';
while i < k - 1 loop
	j := i+1;
	d_array := d_array||' when d'||i||'<=d'||j;
	d_array1 := d_array1||' when d'||i||'<=d'||j;
	j := j + 1;
	while j < k loop
		d_array := d_array||' and d'||i||'<=d'||j;
		d_array1 := d_array1||' and d'||i||'<=d'||j;
		j:= j+1;
	end loop;
	d_array := d_array||' then '||i;
	d_array1 := d_array1||' then d'||i;
	i := i + 1;
end loop;
d_array := d_array||' else '||(k-1)||' end';
d_array1 := d_array1||' else d'||(k-1)||' end';
--d_array1 example:d0<=d1 and d0<=d2 then d0 when d1<=d2 then d1 else d2 end
------------------
caculate_array:='';

columnname:='array[';
i:=1;
while i<column_number loop
columnname:=columnname||''''||column_name[i]||''',';
i:=i+1;
end loop;
columnname:=columnname||''''||column_name[i]||''']';

raise notice 'column_name:%',columnname;

sql:='select alpine_miner_Kmeans_distance('||distance||','||columnname||','||column_number||','||k||')';
raise notice 'sql:%',sql;
execute sql into caculate_array;

--------tmp_res_2 caculate each point in random table's distance to each point in date table and get each point in date table should belong to which cluster----------
sql:='drop table if exists '||temptablename||tmp_res_2||';create temp table '||temptablename||tmp_res_2||' as (select 
	sample_id,'||alpine_id||',
	     '||d_array||' as cluster_id
from
(
select 
sample_id,'||alpine_id||',	     
'||caculate_array||'
 from '||temptablename||'copy x inner join '||temptablename||'_random_new y
   on y.stable=0) as foo) ';
raise notice 'sql:%',sql;
execute sql;

-----tmp_res_1 caculate unstable cluster---
sql:='drop table if exists '||temptablename||tmp_res_1||'; create temp table '||temptablename||tmp_res_1||'   as
(
select 
	sample_id,
 	cluster_id,
	'||avg_array||'
from '||temptablename||tmp_res_2||'
x,'||temptablename||'copy y
where x.'||alpine_id||'=y.'||alpine_id||'
 group by 1,2
)
 ;
 ';

execute sql;
 
--raise info '------------1--------------'; 

------------
comp_sql:='(case when ';
i:=1;
while i<(k + 1) loop
j:=1;
  while j<column_number+1 loop
  if i=k and j=column_number then comp_sql:=comp_sql||'x."k'||i||column_name[j]||'"=y."k'||i||column_name[j]||'"';
	else comp_sql:=comp_sql||'x."k'||i||column_name[j]||'"=y."k'||i||column_name[j]||'" and ';
	end if;
	j:=j+1;
  end loop;
  i:=i+1;
end loop;
comp_sql:=comp_sql||' then 1 else 0 end )as stable';

-----------

----------------
sql:='select tablek1.sample_id,0::smallint as stable,';
column_all:='';
i := 1;
 while i<(k + 1) loop
 j := 1;
	while j<(column_number+1) loop
		column_all:=column_all||'"k'||i||column_name[j]||'",';
		j := j + 1;
		end loop;
	i := i + 1;	
 end loop;
sql:=sql||column_all||'0::integer as iter from ';


i := 1;
 while i<(k + 1) loop
sql:=sql||'(select ';
	 j := 1;
	while j<(column_number+1) loop
	  sql:=sql||'"'||column_name[j]||'" "k'||i||column_name[j]||'",';
	  j := j + 1;
	end loop;
	if i=1 then sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' inner join ';
	else if i=k then sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' on tablek'||(i-1)||'.sample_id=tablek'||i||'.sample_id'; 
	else sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' on tablek'||(i-1)||'.sample_id=tablek'||i||'.sample_id inner join ';end if;
	end if;
	i := i + 1;
 end loop;

--------tmp_res_4 transform the point in same sample to same line.----
 sql:='drop table if exists '||temptablename||tmp_res_4||';create temp table '||temptablename||tmp_res_4||' as ('||sql||') ';
execute sql;

x_array:='';
i := 1;
 while i<(k + 1) loop
 j:=1;
	while j<(column_number+1) loop
	x_array:=x_array||'x."k'||i||column_name[j]||'",';
	  j := j + 1;
	end loop;
	i := i + 1;
 end loop;
 raise notice 'x_array:%',x_array;
 --------tmp_res_3 judge which sample is stable----
sql:='drop table if exists '||temptablename||tmp_res_3||';create temp table '||temptablename||tmp_res_3||' as
(
	select 
		x.sample_id,
	 	'||comp_sql||','||x_array
	 	||run||' as iter
	from  '||temptablename||tmp_res_4||' x, '||temptablename||'_random_new  y
	where x.sample_id=y.sample_id

)

;
';
----------------

execute sql;


execute 'delete from '||temptablename||'_random_new a using '||temptablename||tmp_res_3||' b where a.sample_id=b.sample_id';



execute 'insert into  '||temptablename||'_random_new select * from '||temptablename||tmp_res_3;



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
				select 
					sample_id,'||alpine_id||',
					'||d_array1||' as len
				from
				(	     
				select 
					    	sample_id,
					    	'||alpine_id||',
						'||caculate_array||'
					    from
					      '||temptablename||'copy x inner join '||temptablename||'_random_new y
					      on y.stable=1
				)t
			)a
		 	group by 1
		)b
)z
where seq=1';

raise notice '-------------------sql:%',sql;
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
);'
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
		select sample_id,alpine_miner_kmeans_distance_result('||sample_array||'::float[],'||data_array||'::float[],'||k||','||distance||') as len
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

execute 'drop table if exists '||temptablename||'result2; create temp table '||temptablename||'result2 as select *,0::integer '||temptablename||'copy_flag from '||temptablename||'copy;';


result_sql:='select '||alpine_id||' as temp_id,'||d_array||' as '||clustername||' from
(
select x.'||alpine_id||','||caculate_array||' 
from '||temptablename||'result2 x inner join '||temptablename||result1||' y on x.'||temptablename||'copy_flag=0
) as foo
';
raise notice 'result_sql:%',result_sql;



execute '
	drop table if exists '||temptablename||'table_name_temp;create temp table '||temptablename||'table_name_temp as
		(
		'||result_sql||'
		);';


resultarray[1]:=run;


RETURN resultarray;
 
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
