CREATE OR REPLACE procedure alpine_miner_kmeans(text,  text,text,integer,text,text, text,integer,  integer, integer, integer)
  RETURNS text 
  language nzplsql
  AS
  BEGIN_PROC

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
    power_array text;
    d_array text;
    d_array1 text;
    x_array text;
    comp_array text;
    i integer := 0;
    j integer := 0;
    sql text;
    sql1 text:='';
    temptablename text;
    column_all text;
    comp_sql text;
    result_sql text;
    sampleid integer;
    sample_id text;
    sample_array text:='';
    roww record;
    temptablerecord  record;
    sample_array1 VARRAY(2) OF varchar(64000);
    sample_array2 text:='';
    alpine_id text;
    resultarray VARRAY(2) OF float;
    tempsum float;
    nullflag smallint:=0;
    column_name VARRAY(1600) OF varchar(1000);
    column_count integer := 0;
    caculate_array text;
    sumdistance float;
    result text;
    tempstr text;
    
  table_name ALIAS FOR $1;
  table_name_withoutschema ALIAS FOR $2;
  columntablename ALIAS FOR $3;
  column_number ALIAS FOR $4;
  id ALIAS FOR $5;
  tempid ALIAS FOR $6;
  clustername ALIAS FOR $7;
  k ALIAS FOR $8;
  max_run ALIAS FOR $9;
  max_iter ALIAS FOR $10;
  distance ALIAS FOR $11;
  
BEGIN

sql := 'select value from '||columntablename||' order by id ';
column_count := 0;
for temptablerecord in execute sql loop
       column_count := column_count + 1;
       column_name(column_count) := temptablerecord.value;
end loop;


temptablename:=table_name_withoutschema;

if id='null'
then 
sql:= 'create temp table '||temptablename||'copy as(select *,row_number() over (order by random()) '||tempid||' from '||table_name||' where ';
alpine_id:=tempid;
else
sql:= 'create temp table '||temptablename||'copy as(select * from '||table_name||' where ';
alpine_id:=id;
end if;


i := 1;
while i < (column_number) loop
tempstr:=column_name(i);
	sql:=sql||' "'||tempstr||'" is not null and ';
	i := i + 1;	
end loop;
tempstr:=column_name(i);
sql:=sql||' "'||tempstr||'" is not null';


sql:=sql||') distribute on ('||alpine_id||')';

execute IMMEDIATE sql;

column_array := column_name(1);

i := 2;
while i < (column_number + 1) loop
tempstr:=column_name(i);
	column_array := column_array||',"'||tempstr||'"';
	i := i + 1;
end loop;

------generate random table start------

sql:='select tablek1.seq sample_id,0::smallint as stable,';
column_all:='';
i := 1;
 while i<(k + 1) loop
 j := 1;
	while j<(column_number+1) loop
	tempstr:=column_name(j);
		column_all:=column_all||'"k'||i||''||tempstr||'"::numeric(25,10) as "k'||i||''||tempstr||'",';
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
	tempstr:=column_name(j);
	  sql:=sql||'"'||tempstr||'" as "k'||i||tempstr||'",';
	  j := j + 1;
	end loop;
	if i=1 then sql:=sql||' row_number() over (order by random())-1 as seq from (select * from '||temptablename||'copy limit '||max_run||') foo) as tablek'||i||' inner join ';
	else if i=k then sql:=sql||' row_number() over (order by random())-1 as seq from (select * from '||temptablename||'copy limit '||max_run||') foo) as tablek'||i||' on tablek'||(i-1)||'.seq=tablek'||i||'.seq'; 
	else sql:=sql||' row_number() over (order by random())-1 as seq from (select * from '||temptablename||'copy limit '||max_run||') foo) as tablek'||i||' on tablek'||(i-1)||'.seq=tablek'||i||'.seq inner join ';end if;
	end if;
	i := i + 1;
 end loop;

sql:='create temp table '||temptablename||'_random_new as ('||sql||') distribute on (sample_id)';
execute IMMEDIATE sql;

---------generate random table end-------------

---------Adjust stable start--------------
while run<=max_iter  loop
tmp_res_1:='tmp_res_1'||run::varchar(3);
tmp_res_2:='tmp_res_2'||run::varchar(3);
tmp_res_3:='tmp_res_3'||run::varchar(3);
tmp_res_4:='tmp_res_4'||run::varchar(3);
i := 2;
tempstr:=column_name(1);
avg_array :=  'avg("'||tempstr||'")::numeric(25,10) as "'||tempstr||'"';
while i < (column_number + 1) loop
tempstr:=column_name(i);
        avg_array := avg_array||',avg("'||tempstr||'")::numeric(25,10) as  "'||tempstr||'"';
	i := i + 1;
end loop;

------------------
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

--------------
sql:='select alpine_miner_kmeans_distance('||distance||','''||columntablename||''','||column_number||','||k||') as value';
for temptablerecord in execute sql loop
       caculate_array := temptablerecord.value;
end loop;

--------tmp_res_2 caculate each point in random table's distance to each point in date table and get each point in date table should belong to which cluster----------
sql:='call droptable_if_exists(upper('''||temptablename||tmp_res_2||'''))';
execute IMMEDIATE sql;

sql:='create temp table '||temptablename||tmp_res_2||' as (select 
	sample_id,'||alpine_id||',
	     '||d_array||' as cluster_id
from
(
select 
sample_id,'||alpine_id||',	     
'||caculate_array||'
 from '||temptablename||'copy x inner join '||temptablename||'_random_new y
   on y.stable=0) as foo) distribute on (sample_id,'||alpine_id||',cluster_id)';

execute IMMEDIATE sql;

-----tmp_res_1 caculate unstable cluster---
sql:='call droptable_if_exists(upper('''||temptablename||tmp_res_1||'''))';
execute  IMMEDIATE sql;
sql:='create temp table '||temptablename||tmp_res_1||'   as
(
select 
	sample_id,
 	cluster_id,
	'||avg_array||'
from '||temptablename||tmp_res_2||'
x,'||temptablename||'copy y
where x.'||alpine_id||'=y.'||alpine_id||'
 group by 1,2
) distribute on (sample_id,cluster_id)';

execute  IMMEDIATE sql;
 
--raise info '------------1--------------'; 

------------
comp_sql:='(case when ';
i:=1;
while i<(k + 1) loop
j:=1;
  while j<column_number+1 loop
  tempstr:=column_name(j);
  if i=k and j=column_number then comp_sql:=comp_sql||'x."k'||i||tempstr||'"=y."k'||i||tempstr||'"';
	else comp_sql:=comp_sql||'x."k'||i||tempstr||'"=y."k'||i||tempstr||'" and ';
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
	tempstr:=column_name(j);
		column_all:=column_all||'"k'||i||tempstr||'",';
		j := j + 1;
		end loop;
	i := i + 1;	
 end loop;
sql:=sql||column_all||'0::integer as iter from ';


i := 1;
 while i<(k + 1) loop
sql := sql||'(select ';
	 j := 1;
	while j<(column_number+1) loop
	tempstr:=column_name(j);
	  sql:=sql||'"'||tempstr||'" "k'||i||tempstr||'",';
	  j := j + 1;
	end loop;
	if i=1 then sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' inner join ';
	else if i=k then sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' on tablek'||(i-1)||'.sample_id=tablek'||i||'.sample_id'; 
	else sql:=sql||' sample_id from '||temptablename||tmp_res_1||' where cluster_id='||(i-1)||') as tablek'||i||' on tablek'||(i-1)||'.sample_id=tablek'||i||'.sample_id inner join ';end if;
	end if;
	i := i + 1;
 end loop;

--------tmp_res_4 transform the point in same sample to same line.----
sql1:='call droptable_if_exists(upper('''||temptablename||tmp_res_4||'''))';
execute IMMEDIATE sql1;
 sql:='create temp table '||temptablename||tmp_res_4||' as ('||sql||') distribute on (sample_id)';
execute IMMEDIATE sql;

x_array:='';
i := 1;
 while i<(k + 1) loop
 j:=1;
	while j<(column_number+1) loop
	tempstr:=column_name(j);
	x_array:=x_array||'x."k'||i||tempstr||'",';
	  j := j + 1;
	end loop;
	i := i + 1;
 end loop;
 --raise notice 'x_array:%',x_array;
 --------tmp_res_3 judge which sample is stable----
 sql:='call droptable_if_exists(upper('''||temptablename||tmp_res_3||'''))';
execute IMMEDIATE sql;
sql:='create temp table '||temptablename||tmp_res_3||' as
(
	select 
		x.sample_id,
	 	'||comp_sql||','||x_array
	 	||run||' as iter
	from  '||temptablename||tmp_res_4||' x, '||temptablename||'_random_new  y
	where x.sample_id=y.sample_id

)
distribute on (sample_id)
;
';

execute IMMEDIATE sql;


sql:='insert into '||temptablename||tmp_res_3||' (select a.* from '||temptablename||'_random_new a left join '||temptablename||tmp_res_3||' as b on a.sample_id=b.sample_id';
sql:=sql||' where b.sample_id is null)';
execute IMMEDIATE sql;
sql:='call droptable_if_exists(upper('''||temptablename||'_random_new''))';
execute IMMEDIATE sql;
sql:='alter table '||temptablename||tmp_res_3||' rename to '||temptablename||'_random_new';
execute IMMEDIATE sql;


sql:='select count(*) ct from  '||temptablename||'_random_new where stable=0;';
for temptablerecord in execute sql loop
       none_stable := temptablerecord.ct;    
end loop;

--raise notice '-------------------none_stable:%',none_stable;

if none_stable=0
then
	exit;
end if;

run := run+1;
raise notice '-------------------run:%',run;

end loop;
---------Adjust stable end--------------

sql:='select sample_id as id,len
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

for temptablerecord in execute sql loop
       sampleid := temptablerecord.id;    
       sumdistance := temptablerecord.len; 
end loop;

if sampleid is null then 
sampleid:=0;
nullflag:=1;
 end if;

--------deal result---------------- in (select sample_id from '||temptablename||'tmp_res_4) 
result1:='result1';
sql:='call droptable_if_exists(upper('''||temptablename||result1||'''))';
execute IMMEDIATE sql;

execute IMMEDIATE 'create temp table '||temptablename||result1||' as 
(
	select * from  '||temptablename||'_random_new  where sample_id ='||sampleid||'
)distribute on (sample_id);'
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
					      on y.stable=0
				)t
			)a
		 	group by 1
		)b
)z
where seq=1';

for temptablerecord in execute sql loop 
       sumdistance := temptablerecord.len; 
end loop;

end if;


sql:='call droptable_if_exists(upper('''||temptablename||'result2''))';
execute IMMEDIATE sql;
execute IMMEDIATE ' create temp table '||temptablename||'result2 as select *,0::integer '||temptablename||'copy_flag from '||temptablename||'copy ;';

sql:='call droptable_if_exists(upper('''||temptablename||'table_name_temp''))';
execute IMMEDIATE sql;
sql:='create temp table '||temptablename||'table_name_temp as
		(
		select ' || alpine_id || ' as temp_id,' || d_array ||
                ' as ' || clustername || ' from ( select x.' || alpine_id || ',' ||
                caculate_array || ' from ' || temptablename ||
                'result2 x inner join ' || temptablename || result1 ||
                ' y on x.' || temptablename || 'copy_flag=0 ) goo
		)';
                      
execute IMMEDIATE sql;

result:=run::varchar(10)||'_'||sumdistance;


RETURN result;
END;
END_PROC;

