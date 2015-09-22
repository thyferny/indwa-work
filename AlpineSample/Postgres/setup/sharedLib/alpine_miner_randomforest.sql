CREATE OR REPLACE FUNCTION alpine_miner_randomforest_inittra(schemaname text, tablename text, stamp text, dependcolumn text)
  RETURNS integer AS
$BODY$
DECLARE 
	rownumber integer;
	classnumber integer;
	sql text;
	peoso float;
	totalpeoso float;
	tempstring text;
	classstring text[];
	i integer:= 1;
BEGIN
	execute 'Drop table IF EXISTS '||schemaname||'."pnew'||stamp||'"';
	execute 'Drop table IF EXISTS'||schemaname||'."dn' || stamp || '"';
	execute 'Create  table '||schemaname||'."dn' || stamp || '" 
                      as select *
                    from '||schemaname||'.'|| tablename ||' where '||dependcolumn||' is not null  ';
  
	execute  'select count(*)   from '||schemaname||'."dn' || stamp || '"'
	into rownumber;
	peoso := 1.0 / rownumber;

	execute 'Create  table '||schemaname||'."pnew' || stamp || '" 
                      as select *,
                    row_number()over(order by 1) alpine_randomforest_id,
                    '||peoso||' alpine_randomforest_peoso, 
                    row_number()over()*'||peoso||' alpine_randomforest_totalpeoso 
                    from '||schemaname||'."dn' || stamp || '"   ';


			
RETURN rownumber;
end;

 $BODY$
  LANGUAGE plpgsql VOLATILE;
  
  
  
  
  
CREATE OR REPLACE FUNCTION  alpine_miner_randomforest_sample(schemaname text, tablename text, stamp text, partsize integer)
  RETURNS text AS
$BODY$
DECLARE 
	rownumber integer;
	randomnumber float;
	myrecord record;
	partnumber integer;
	--partsize   integer;
	tempstring text;
	splitpeoso double precision[];
	maxpeoso   double precision;
	i          integer;
BEGIN
	execute 'select count(*) from '||schemaname||'.'||tablename into rownumber;
	execute ' select max(alpine_randomforest_totalpeoso)  from '||schemaname||'.'||tablename into maxpeoso;
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
 	execute 'Drop table IF EXISTS '||schemaname||'."s'||stamp||'"';
	execute 'Drop table IF EXISTS "r'||stamp||'"';
	execute 'create temp table "r'||stamp||'" as select '||maxpeoso||'*random() as alpine_miner_randomforest_r from '||schemaname||'.'||tablename||' order by alpine_miner_randomforest_r ';
	if partnumber=1 
	then
		execute 'create table '||schemaname||'."s'||stamp||'" as select * from '||schemaname||'.'||tablename||' join  "r'||stamp||'" on 
		'||schemaname||'.'||tablename||'.alpine_randomforest_totalpeoso >= "r'||stamp||'".alpine_miner_randomforest_r  and '||schemaname||'.'||tablename||'.alpine_randomforest_peoso > 
		('||schemaname||'.'||tablename||'.alpine_randomforest_totalpeoso-"r'||stamp||'".alpine_miner_randomforest_r ) ';
	else 
		--partsize:=trunc(rownumber*percent);
		tempstring:=' select alpine_randomforest_totalpeoso as peoso from '||schemaname||'.'||tablename||' where mod(alpine_randomforest_id,'||partsize||')=0 order by peoso';

		i:=1;
		splitpeoso[i]:=0;
		for myrecord in execute tempstring loop
			i:=i+1;
			splitpeoso[i]:=myrecord.peoso;
		 
		end loop;

		
		if splitpeoso[i]!=maxpeoso
		then
			i:=i+1;
			splitpeoso[i]:=maxpeoso;
	 
		end if;
		i:=1;
		tempstring:='create table '||schemaname||'."s'||stamp||'" as select * from  ( select * from '||schemaname||'.'||tablename||' 
			where alpine_randomforest_totalpeoso>'||splitpeoso[i]||' and  alpine_randomforest_totalpeoso<='||splitpeoso[i+1]||') as foo'||i||' join (select * from "r'||stamp||'" where alpine_miner_randomforest_r 
			>'||splitpeoso[i]||' and  alpine_miner_randomforest_r<='||splitpeoso[i+1]||') as foor'||i||' on foo'||i||'.alpine_randomforest_totalpeoso >=foor'||i||'.alpine_miner_randomforest_r and foo'||i||'.alpine_randomforest_peoso > 
		(foo'||i||'.alpine_randomforest_totalpeoso-foor'||i||'.alpine_miner_randomforest_r) ';
		tempstring:=tempstring||'  ';
		execute tempstring;
		 
 		for i in 2..partnumber loop
			tempstring:= '  insert into  '||schemaname||'."s'||stamp||'"   select * from ( select * from '||schemaname||'.'||tablename||' 
  			where alpine_randomforest_totalpeoso>'||splitpeoso[i]||' and  alpine_randomforest_totalpeoso<='||splitpeoso[i+1]||') as foo'||i||' join (select * from "r'||stamp||'" where alpine_miner_randomforest_r 
  			>'||splitpeoso[i]||' and  alpine_miner_randomforest_r<='||splitpeoso[i+1]||') as foor'||i||' on foo'||i||'.alpine_randomforest_totalpeoso >=foor'||i||'.alpine_miner_randomforest_r and foo'||i||'.alpine_randomforest_peoso > 
 			(foo'||i||'.alpine_randomforest_totalpeoso-foor'||i||'.alpine_miner_randomforest_r) ';
		 
			execute tempstring;
		end loop;
 	end if;
	tempstring = 's'||stamp;
	RETURN tempstring; 
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE;
  
  
  
  
  
  
  
  
CREATE OR REPLACE FUNCTION alpine_miner_randomforest_initpre(tablename text, stamp text, dependcolumn text, infor text[],istemp boolean)
  RETURNS void AS
$BODY$
DECLARE 
	rownumber integer;
	classnumber integer;
	sql text;
	peoso float;
	totalpeoso float;
	tempstring text;
	classstring text[];
	i integer:= 0;
BEGIN
	execute 'Drop table IF EXISTS "id'||stamp||'"';
	execute 'Create temp table "id'||stamp||'" as select *,row_number()over() alpine_randomforest_id from '||tablename||' ';
	execute 'Drop table IF EXISTS  '||tablename;
	if istemp=true
	then execute 'Create temp table '||tablename||' as select * from "id'||stamp||'" ';
	else
	execute 'Create  table '||tablename||' as select * from "id'||stamp||'" ';
	end if ;
	execute 'Drop  table IF EXISTS "to'||stamp||'"';
	


 tempstring:='update '||tablename||' set "C('||infor[1]||')"=0';

  
  for i in 2 .. alpine_miner_get_array_count(infor) loop
    tempstring := tempstring||', "C(' || infor[i] ||')"=0';
  
  end loop;

	execute tempstring;
end;

 $BODY$
  LANGUAGE plpgsql VOLATILE;


  
  CREATE OR REPLACE FUNCTION alpine_miner_randomforest_prere(tablename text, dependcolumn text, infor text[], isnumeric int)
  RETURNS void AS
$BODY$
DECLARE 
	rownumber integer;
	classnumber integer;
	sql text;
	sql1 text;
	sql2 text;
	peoso float;
	totalpeoso float;
	tempstring text;
	classstring text[];
	i integer:= 0;
	err float;
BEGIN
	classnumber:= alpine_miner_get_array_count(infor);
	
	sql:= 'update '||tablename||' set  "P('||dependcolumn||')" = CASE';
	sql2 := '(';

	i:=classnumber;
	while i>1 loop
	sql2 :=sql2||'  "C('||infor[i]||')" ,';
	
	i:=i-1;
	end loop;
	sql2:=sql2||' "C('||infor[1]||')")';
	for i in 1..alpine_miner_get_array_count(infor) loop
		sql := sql||' WHEN "C('||infor[i]||')"=greatest'||sql2||' THEN ';
		if isnumeric = 1 then
			sql := sql || infor[i];
		else
			sql := sql||''''||infor[i]||'''';
		end if;
	end loop;
	sql := sql||' END ';
	execute sql;


end;

 $BODY$
  LANGUAGE plpgsql VOLATILE;


-- Function: alpine_miner_adaboost_prestep(text, text, text, double precision, text[])

-- DROP FUNCTION alpine_miner_adaboost_prestep(text, text, text, double precision, text[]);

CREATE OR REPLACE FUNCTION alpine_miner_randomforest_prestep(tablename text, temptable text, dependcolumn text,   infor text[])
  RETURNS double precision AS
$BODY$
DECLARE 
	rownumber integer;
	classnumber integer;
	sql text;
	sql1 text;
	sql2 text;
	peoso float;
	totalpeoso float;
	tempstring text;
	classstring text[];
	i integer:= 0;
	err float;
	
BEGIN	
	sql:='update ' || tablename || '  set "C(' ||
                      infor[1] || ')"= ' || tablename || '."C(' || infor[1] ||
                      ')"+ '||temptable||'."C('||infor[1]||')" ';
			for i in 2..alpine_miner_get_array_count(infor) loop
			sql:=sql||' ,"C('||infor[i]||')"= '||tablename||'."C('||infor[i]||')"+ 
				'||temptable||'."C('||infor[i]||')" '  ;
					
	end loop;

		sql:=sql||'from '||temptable||'  where '||tablename||'.alpine_randomforest_id = '||temptable||'.alpine_randomforest_id';
	execute sql;
RETURN 1;
end;

 $BODY$
  LANGUAGE plpgsql VOLATILE;



  
  
