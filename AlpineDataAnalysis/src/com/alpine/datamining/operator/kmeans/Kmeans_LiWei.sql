CREATE OR REPLACE FUNCTION kmeans_1_new( table_name text, column_name text[], column_number int, id text, k int, max_run int, max_iter int )
  RETURNS integer AS
$BODY$
DECLARE
    run integer:=1;
    none_stable integer;
    tmp_res_1 varchar(50);
    tmp_res_2 varchar(50);
    tmp_res_3 varchar(50);
    result1 varchar(50);
    result2 varchar(50);
    column_array text;
    column_array1 text ; 
    avg_array text;
    power_array text;
    d_array text;
    d_array1 text;
    x_array text;
    comp_array text;
    min_array text;
    i integer := 0;
    j integer := 0;
    row RECORD;
    
BEGIN
execute 'drop table if exists tablecopy;create temp table tablecopy as(select * from '||table_name||' )distributed by('||id||')';

column_array := column_name[1];
column_array1 := column_name[1]||'::numeric';
i := 2;
while i < (column_number + 1) loop
	column_array := column_array||','||column_name[i];
	column_array1 := column_array||','||column_name[i]||'::numeric';
	i := i + 1;
end loop;

execute 'drop table if exists tmp_random;create temp table tmp_random  as
(
	select seq/'||k||' as sample_id ,seq%'||k||' as cluster_id,0::smallint as stable ,'||column_array||',0::integer as iter
	from 
	(select '||column_array||' ,row_number() over (order by random())-1 as seq from tablecopy limit '||max_run||'*'||k||') a
)distributed by(sample_id,cluster_id);
';
while run<max_iter  loop

tmp_res_1:='tmp_res_1'||run::varchar;
tmp_res_2:='tmp_res_2'||run::varchar;
tmp_res_3:='tmp_res_3'||run::varchar;
i := 2;
avg_array :=  'avg('||column_name[1]||')::numeric(25,10) '||column_name[1];
power_array :=  'power(x.'||column_name[1]||'-y.'||column_name[1]||',2)';
while i < (column_number + 1) loop
        avg_array := avg_array||',avg('||column_name[i]||')::numeric(25,10) '||column_name[i];
	power_array :=  power_array||'+power(x.'||column_name[i]||'-y.'||column_name[i]||',2)';
	i := i + 1;
end loop;

min_array := 'min(case when cluster_id=0 then '||power_array||' else null end) as d0';
i := 2;
while i <(column_number + 1) loop
	min_array := min_array||',min(case when cluster_id='||(i-1)||' then '||power_array||' else null end) as d'||(i-1);
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



execute 'drop table if exists '||tmp_res_1||';create temp table   '||tmp_res_1||'   as
(
select 
	sample_id,
 	cluster_id,
	'||avg_array||'
from
(
select 
	sample_id,id,
	     '||d_array||' as cluster_id
from
(	     
select 
	    	sample_id,
	    	id,
		'||min_array||'
	    from
	      tablecopy x inner join tmp_random y
	      on y.stable=0
	      group by 1,2
)a
)x,tablecopy y
where x.id=y.id
 group by 1,2
)distributed by(sample_id,cluster_id)
 ;
 '
 ;
raise info '------------1--------------'; 
comp_array := 'x.'||column_name[1]||'=y.'||column_name[1];
x_array := 'x.'||column_name[1];
i:=2;
while i < column_number + 1 loop
	comp_array := comp_array||' and x.'||column_name[i]||'=y.'||column_name[i];
	x_array := x_array||',x.'||column_name[i];
	i:=i+1;
end loop;
execute 'drop table if exists '||tmp_res_3||';create temp table '||tmp_res_3||' as
(
	select
	sample_id,
	cluster_id,
	first_value(stable) over(partition by sample_id order by stable) as stable,
	'||column_array||'
	, iter
	from
	(
	select 
		x.sample_id,
	 	x.cluster_id,
	 	case when '||comp_array||' then 1
	 			 else 0
	 			 end as stable,
		'||x_array||','
	 	||run||' as iter
	from  '||tmp_res_1||' x, tmp_random  y
	where x.sample_id=y.sample_id and x.cluster_id=y.cluster_id
	)a
)
distributed by(sample_id,cluster_id)
;
';
raise info '------------2--------------'; 

execute 'delete from tmp_random a using '||tmp_res_3||' b where a.sample_id=b.sample_id and a.cluster_id=b.cluster_id;';

raise info '------------3--------------'; 

execute 'insert into  tmp_random  select * from '||tmp_res_3;
raise info '------------4--------------'; 


select count(*) into none_stable  from  tmp_random where stable=0;
raise info '------------5--------------'; 

raise info '----------%',none_stable;
if none_stable=0
then
	exit;
end if;

run := run+1;

end loop;

raise info  '----------------------select--------------';


execute 'drop table if exists tmp_res_4;create temp table  tmp_res_4  as 
(
select sample_id
from
(
	select sample_id,row_number() over(order by len) as seq 
	from
	(
		select sample_id,sum(len) as len
		from
		(
				select 
					sample_id,id,
					'||d_array1||' as len
				from
				(	     
				select 
					    	sample_id,
					    	id,
						'||min_array||'
					    from
					      tablecopy x inner join tmp_random y
					      on y.stable=1
					      group by 1,2
				)t
			)a
		 	group by 1
		)b
)z
where seq=1
)distributed by(sample_id)
;'
;


result1:='result1';
execute 'drop table if exists '|| result1;

execute 'create table '|| result1 ||' as 
(
	select * from  tmp_random  where sample_id in (select sample_id from   tmp_res_4) 
)distributed randomly;'
;

RETURN 0;
 
END;
$BODY$
  LANGUAGE 'plpgsql' ;

