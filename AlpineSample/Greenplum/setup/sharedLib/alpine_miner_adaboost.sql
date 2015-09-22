-- Function: alpine_miner_adaboost_changep(text, text, text, text, text[])

-- DROP FUNCTION alpine_miner_adaboost_changep(text, text, text, text, text[]);

CREATE OR REPLACE FUNCTION alpine_miner_adaboost_changep(schemaname text, tablename text, stamp text, dependcolumnq text, dependentcolumnreplaceq text, dependentcolumnstr text,  dependinfor text[])
  RETURNS double precision AS
$BODY$
DECLARE 
	rownumber float;
	i integer;
	peoso float;	
	totalpeoso float;
	temppeoso float;
	wrongnumber float;
	err float;
	maxerror float;
	ind integer :=0;
	c float :=0;
	sql text;
	sqlan text;
	tempstring text;
	result  double precision[];
BEGIN
	execute 'alter table '||schemaname||'."tp'||stamp||'"  add column "notsame" int';
	execute 'update  '||schemaname||'."tp'||stamp||'" set "notsame" = CASE WHEN '||dependentcolumnstr||' = "P('||dependentcolumnreplaceq||')"::text  THEN 0 ELSE 1 END';
	execute 'select count(*) from '||schemaname||'."tp'||stamp||'" where "notsame" =1' into wrongnumber;
	execute 'select count(*) from '||schemaname||'.'||tablename||'' into rownumber;
	err := wrongnumber/rownumber;
	execute 'select count from ( select * from (select '||dependcolumnq||', count(*) from '||schemaname||'.'||tablename||' 
	group by '||dependcolumnq||') foo order by "count" desc limit 1) AS foo' into maxerror; 
	maxerror := maxerror/rownumber;
	IF err>=maxerror
	THEN	c=0.001;
		execute 'update '||schemaname||'."pnew'||stamp||'"  set "alpine_adaboost_peoso"='||1/rownumber||',"alpine_adaboost_totalpeoso"="alpine_adaboost_id"*'||1/rownumber;
	ELSIF err=0
	THEN c=3;
		execute 'update '||schemaname||'."pnew'||stamp||'"  set "alpine_adaboost_peoso"='||1/rownumber||',"alpine_adaboost_totalpeoso"="alpine_adaboost_id"*'||1/rownumber;
	ELSE 	
		c :=ln ((1-err)/err);
		c := c/2;
		totalpeoso :=0;
		temppeoso :=0;
		i:=1;
		execute 'alter table '||schemaname||'."pnew'||stamp||'"  add column "notsame" int';
		execute 'update  '||schemaname||'."pnew'||stamp||'" set "notsame" =  '||schemaname||'."tp'||stamp||'"."notsame" from '||schemaname||'."tp'||stamp||'" where '||schemaname||'."pnew'||stamp||'"."alpine_adaboost_id" = '||schemaname||'."tp'||stamp||'"."alpine_adaboost_id"'; 
		temppeoso := temppeoso*exp (c*ind); 
		execute 'update  '||schemaname||'."pnew'||stamp||'" set  "alpine_adaboost_peoso" = "alpine_adaboost_peoso"*exp('||c||'*notsame) ';
--		execute 'select sum("alpine_adaboost_peoso") from '||schemaname||'."pnew'||stamp||'"' into totalpeoso;
--		execute 'update  '||schemaname||'."pnew'||stamp||'" set  "alpine_adaboost_peoso" = "alpine_adaboost_peoso"/'||totalpeoso;
		execute 'Drop table IF EXISTS "sp'||stamp||'"';
		execute 'Create temp table "sp'||stamp||'" as select "alpine_adaboost_id", "alpine_adaboost_peoso", sum("alpine_adaboost_peoso")over(order by "alpine_adaboost_id" ) alpine_sum_peoso  from 
 '||schemaname||'."pnew'||stamp||'"  DISTRIBUTED BY (alpine_adaboost_id)';
		execute 'update  '||schemaname||'."pnew'||stamp||'" set  "alpine_adaboost_totalpeoso" = "alpine_sum_peoso" from "sp'||stamp||'" where '||schemaname||'."pnew'||stamp||'"."alpine_adaboost_id" = "sp'||stamp||'"."alpine_adaboost_id"';
		execute 'alter table '||schemaname||'."pnew'||stamp||'"  drop column "notsame"';
	END IF ;




	  i := 2;
  
 
  sqlan:='update '||schemaname||'."p'|| stamp || '" set "C('||dependinfor[1]||')" ='||schemaname||'."p'||stamp||'"."C('||dependinfor[1]||')"+
                  '||schemaname||'."tp'||stamp||'"."C('||dependinfor[1]||')" *'||c ;
                    
  while i <= alpine_miner_get_array_count(dependinfor) loop
        sqlan:=sqlan||' , "C('||dependinfor[i]||')"='||schemaname||'."p'||stamp||'"."C('||dependinfor[i]||')"+
          '||schemaname||'."tp'||stamp||'"."C('||dependinfor[i]||')"  *'||c;
        
        i:=i+1;
   end loop;
   sqlan:=sqlan||' from  '||schemaname||'."tp'||stamp||'"  where '||schemaname||'."p'||stamp||'".alpine_adaboost_id = '||schemaname||'."tp'||stamp||'".alpine_adaboost_id';
   execute  sqlan;

	
RETURN c;

END;

 $BODY$
  LANGUAGE plpgsql VOLATILE;





-- Function: alpine_miner_adaboost_initpre(text, text, text, text[])

-- DROP FUNCTION alpine_miner_adaboost_initpre(text, text, text, text[]);

CREATE OR REPLACE FUNCTION alpine_miner_adaboost_initpre(tablename text, stamp text, dependcolumn text, infor text[],istemp boolean)
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
	execute 'Create temp table "id'||stamp||'" as select *,row_number()over() alpine_adaboost_id from '||tablename||' DISTRIBUTED BY (alpine_adaboost_id)';
	execute 'Drop table IF EXISTS  '||tablename;
	if istemp=true
	then execute 'Create temp table '||tablename||' as select * from "id'||stamp||'" DISTRIBUTED BY (alpine_adaboost_id)';
	else
	execute 'Create  table '||tablename||' as select * from "id'||stamp||'" DISTRIBUTED BY (alpine_adaboost_id)';
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





-- Function: alpine_miner_adaboost_inittra(text, text, text, text[])

-- DROP FUNCTION alpine_miner_adaboost_inittra(text, text, text, text[]);

CREATE OR REPLACE FUNCTION alpine_miner_adaboost_inittra(schemaname text, tablename text, stamp text, dependcolumn text,dependinfor text[])
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
                    from '||schemaname||'.'|| tablename ||' where '||dependcolumn||' is not null DISTRIBUTED RANDOMLY';
  
	execute  'select count(*)   from '||schemaname||'."dn' || stamp || '"'
	into rownumber;
	peoso := 1.0 / rownumber;

	execute 'Create  table '||schemaname||'."pnew' || stamp || '" 
                      as select *,
                    row_number()over(order by 1) alpine_adaboost_id,
                    '||peoso||' alpine_adaboost_peoso, 
                    row_number()over()*'||peoso||' alpine_adaboost_totalpeoso 
                    from '||schemaname||'."dn' || stamp || '" DISTRIBUTED BY (alpine_adaboost_id) ';


	
	classnumber:=alpine_miner_get_array_count(dependinfor);
	execute 'Drop  table IF EXISTS '||schemaname||'."tp'||stamp||'"';
	execute 'CREATE  TABLE '||schemaname||'."tp'||stamp||'" as select * from  '||schemaname||'."pnew'||stamp||'" DISTRIBUTED BY (alpine_adaboost_id)';
	
	execute 'Drop  table IF EXISTS '||schemaname||'."p'||stamp||'"';

		sql:='CREATE  TABLE '||schemaname||'."p'||stamp||'" as select *';

	while i<=classnumber	loop
			sql:=sql||',  0.0  "C('||dependinfor[i]||')"';
			i:=i+1;
			
	end loop;
	sql:=sql||' from '||schemaname||'."pnew'||stamp||'" DISTRIBUTED BY (alpine_adaboost_id)';
	
	execute sql;
	execute 'create table '||schemaname||'."s'||stamp||'" as select * from '||schemaname||'."pnew'||stamp||'" DISTRIBUTED BY (alpine_adaboost_id)';
		
RETURN rownumber;
end;

 $BODY$
  LANGUAGE plpgsql VOLATILE;






-- Function: alpine_miner_adaboost_prere(text, text, text[])

-- DROP FUNCTION alpine_miner_adaboost_prere(text, text, text[]);

CREATE OR REPLACE FUNCTION alpine_miner_adaboost_prere(tablename text, dependcolumn text, infor text[], isnumeric int)
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

CREATE OR REPLACE FUNCTION alpine_miner_adaboost_prestep(tablename text, temptable text, dependcolumn text, c double precision, infor text[])
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
                      ')"+ '||temptable||'."C('||infor[1]||')"*'||c;
			for i in 2..alpine_miner_get_array_count(infor) loop
			sql:=sql||' ,"C('||infor[i]||')"= '||tablename||'."C('||infor[i]||')"+ 
				'||temptable||'."C('||infor[i]||')"*'||c ;
					
	end loop;

		sql:=sql||'from '||temptable||'  where '||tablename||'.alpine_adaboost_id = '||temptable||'.alpine_adaboost_id';
	execute sql;
RETURN c;
end;

 $BODY$
  LANGUAGE plpgsql VOLATILE;




-- Function: alpine_miner_adaboost_sample(text, text, text)

-- DROP FUNCTION alpine_miner_adaboost_sample(text, text, text);

CREATE OR REPLACE FUNCTION  alpine_miner_adaboost_sample(schemaname text, tablename text, stamp text, partsize integer)
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
	execute ' select max(alpine_adaboost_totalpeoso)  from '||schemaname||'.'||tablename into maxpeoso;
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
	execute 'create temp table "r'||stamp||'" as select '||maxpeoso||'*random() as alpine_miner_adaboost_r from '||schemaname||'.'||tablename||' order by alpine_miner_adaboost_r DISTRIBUTED by (alpine_miner_adaboost_r)';
	if partnumber=1 
	then
		execute 'create table '||schemaname||'."s'||stamp||'" as select * from '||schemaname||'.'||tablename||' join  "r'||stamp||'" on 
		'||schemaname||'.'||tablename||'.alpine_adaboost_totalpeoso >= "r'||stamp||'".alpine_miner_adaboost_r  and '||schemaname||'.'||tablename||'.alpine_adaboost_peoso > 
		('||schemaname||'.'||tablename||'.alpine_adaboost_totalpeoso-"r'||stamp||'".alpine_miner_adaboost_r ) DISTRIBUTED BY (alpine_adaboost_id)';
	else 
		--partsize:=trunc(rownumber*percent);
		tempstring:=' select alpine_adaboost_totalpeoso as peoso from '||schemaname||'.'||tablename||' where mod(alpine_adaboost_id,'||partsize||')=0 order by peoso';

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
			where alpine_adaboost_totalpeoso>'||splitpeoso[i]||' and  alpine_adaboost_totalpeoso<='||splitpeoso[i+1]||') as foo'||i||' join (select * from "r'||stamp||'" where alpine_miner_adaboost_r 
			>'||splitpeoso[i]||' and  alpine_miner_adaboost_r<='||splitpeoso[i+1]||') as foor'||i||' on foo'||i||'.alpine_adaboost_totalpeoso >=foor'||i||'.alpine_miner_adaboost_r and foo'||i||'.alpine_adaboost_peoso > 
		(foo'||i||'.alpine_adaboost_totalpeoso-foor'||i||'.alpine_miner_adaboost_r) ';
		tempstring:=tempstring||' DISTRIBUTED by (alpine_adaboost_id)';
		execute tempstring;
		 
 		for i in 2..partnumber loop
			tempstring:= '  insert into  '||schemaname||'."s'||stamp||'"   select * from ( select * from '||schemaname||'.'||tablename||' 
  			where alpine_adaboost_totalpeoso>'||splitpeoso[i]||' and  alpine_adaboost_totalpeoso<='||splitpeoso[i+1]||') as foo'||i||' join (select * from "r'||stamp||'" where alpine_miner_adaboost_r 
  			>'||splitpeoso[i]||' and  alpine_miner_adaboost_r<='||splitpeoso[i+1]||') as foor'||i||' on foo'||i||'.alpine_adaboost_totalpeoso >=foor'||i||'.alpine_miner_adaboost_r and foo'||i||'.alpine_adaboost_peoso > 
 			(foo'||i||'.alpine_adaboost_totalpeoso-foor'||i||'.alpine_miner_adaboost_r) ';
		 
			execute tempstring;
		end loop;
 	end if;
	tempstring = 's'||stamp;
	RETURN tempstring; 
end;
 $BODY$
  LANGUAGE plpgsql VOLATILE;


