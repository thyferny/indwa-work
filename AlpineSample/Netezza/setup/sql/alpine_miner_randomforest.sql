CREATE OR REPLACE procedure alpine_miner_randomforest_inittra(  text,   text,   text,    text )
 language nzplsql
   returns integer
   as
   BEGIN_PROC
DECLARE 
	schemaname ALIAS FOR $1;
	tablename ALIAS FOR $2;
	stamp ALIAS FOR $3;
	dependcolumn ALIAS FOR $4;
 
    myrecord record;
	rownumber integer;
	classnumber integer;
	sql text;
	peoso float;
	totalpeoso float;
	tempstring text;
	dependinfor VARRAY(1000000) of text;
	classstring  VARRAY(1000000) of text;
	i integer:= 1;
BEGIN
	execute immediate 'select droptable_if_exists(''pnew'||stamp||''')'; 
  execute immediate 'select droptable_if_exists(''dn' || stamp || ''')'; 
 
	execute immediate 'Create  table '||schemaname||'."dn' || stamp || '" 
                     as select *
                    from '||schemaname||'.'|| tablename ||' where '||dependcolumn||' is not null';
  sql:='select count(*) as  c  from '||schemaname||'."dn' || stamp || '"';
   	for myrecord in execute sql loop
		rownumber:=myrecord.c;
			end loop;
 
	peoso := 1.0::double / rownumber;
	execute immediate 'Create  table '||schemaname||'."pnew' || stamp || '" 
                      as select *,
                    row_number()over(order by 1) as alpine_randomforest_id,
                    '||peoso||'::double as  alpine_randomforest_peoso, 
                     row_number()over(order by 1) *'||peoso||'::double as alpine_randomforest_totalpeoso 
                    from '||schemaname||'."dn' || stamp || '"  ';

 	
RETURN rownumber;
 END;
END_PROC;




 CREATE OR REPLACE procedure alpine_miner_randomforest_sample( text,  text,  text,integer)
  language nzplsql
  RETURNS text AS
BEGIN_PROC
DECLARE 
	schemaname ALIAS FOR $1;
	tablename ALIAS FOR $2;
	stamp ALIAS FOR $3;
	partsize ALIAS FOR $4;
	rownumber integer;
  tempstring text;
  myrecord record;
  i INTEGER;
  j INTEGER;
  partnumber integer;
  temppeoso  FLOAT;
  temppeoso1  FLOAT;
  maxpeoso FLOAT;
  execStr text;
  sql text;
  splitpeoso VARRAY(1000000) of float;
   
BEGIN
          
  sql:= 'select count(*) as c from '||schemaname||'.'||tablename ;
  for myrecord in execute sql loop
				rownumber:=myrecord.c;
		end loop;
   
	 sql:= ' select max(alpine_randomforest_totalpeoso) as m from '||schemaname||'.'||tablename  ;
   for myrecord in execute sql loop
				  maxpeoso:=myrecord.m;
		end loop;

	if partsize>= rownumber
	then 
	 	partnumber :=1;
	else 
		if mod(rownumber ,partsize)=0
		then
			 partnumber := rownumber/partsize;
		else 
			 partnumber :=rownumber/partsize+1;
		end if;
	end if;
	
		execute immediate 'select droptable_if_exists(''s'||stamp||''')'; 
		execute immediate 'select droptable_if_exists(''r'||stamp||''')'; 

		execute immediate  'create temp table ' || schemaname || '."r' || stamp || '"   as ( select random() 
"alpine_miner_randomforest_r" from ' || schemaname|| '.' || tablename ||' order by "alpine_miner_randomforest_r")   ';
   
    

    if partnumber=1
    then
    execute immediate  'create table ' || schemaname || '."s' || stamp || '"   as (select * from ' || schemaname
    || '.' ||tablename || ' join ' ||schemaname|| '."r' ||stamp || '" on ' || schemaname || '.' || tablename
    || '.alpine_randomforest_totalpeoso  >= ' || schemaname || '."r' ||stamp|| '"."alpine_miner_randomforest_r" and ' || schemaname || '
.' || tablename|| '.alpine_randomforest_peoso > (' ||schemaname || '.' || tablename || '.
alpine_randomforest_totalpeoso-' || schemaname || '."r' || stamp || '"."alpine_miner_randomforest_r" ) )   ';
 
   
 
 else 
	  tempstring:=' select alpine_randomforest_totalpeoso as peoso from '||schemaname||'.'||tablename||' where mod(alpine_randomforest_id,'||partsize||')=0 order by peoso';
 
	  i:=1;
 	  splitpeoso(i):=0;
 
	for myrecord in execute tempstring loop
	 
  		i:=i+1;
 		  splitpeoso(i):=myrecord.peoso;
 
	end loop;

		if splitpeoso(i)!=maxpeoso
		then
			  i :=i+1;
			  splitpeoso(i) :=maxpeoso;
	 
		end if;
		  i =1;
     
   
   
    temppeoso:=splitpeoso(i); 
   
    j:=i+1;
    temppeoso1:=splitpeoso(j);
    
    
 	 tempstring :='create table '||schemaname||'."s'||stamp||'"   as (select * from  ( select * from '||schemaname||'.'||tablename||' 
 			where alpine_randomforest_totalpeoso>'||temppeoso||' and  alpine_randomforest_totalpeoso<='||temppeoso1||')   foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "alpine_miner_randomforest_r"
 			>'||temppeoso||' and   "alpine_miner_randomforest_r"<='||temppeoso1||')   foor'||i||' on foo'||i||'.alpine_randomforest_totalpeoso >=foor'||i||'."alpine_miner_randomforest_r" and foo'||i||'.alpine_randomforest_peoso > 
 		(foo'||i||'.alpine_randomforest_totalpeoso-foor'||i||'."alpine_miner_randomforest_r"))  ';
 	 
  		execute  immediate tempstring;
 
	while i <partnumber loop
	  i:=i+1;
	   
	   temppeoso:=splitpeoso(i);
	   j:=i+1;
 
    temppeoso1:=splitpeoso(j);
	 	tempstring := '  insert into  '||schemaname||'."s'||stamp||'" (  select * from ( select * from '||schemaname||'.'||tablename||' 
  		where alpine_randomforest_totalpeoso>'||temppeoso||' and  alpine_randomforest_totalpeoso<='||temppeoso1||') as foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "alpine_miner_randomforest_r" 
  		>'||temppeoso||' and  "alpine_miner_randomforest_r"<='||temppeoso1||') as foor'||i||' on foo'||i||'.alpine_randomforest_totalpeoso >=foor'||i||'."alpine_miner_randomforest_r" and foo'||i||'.alpine_randomforest_peoso > 
 		(foo'||i||'.alpine_randomforest_totalpeoso-foor'||i||'."alpine_miner_randomforest_r")) ';
 
		execute   immediate tempstring;
	 
		end loop;
		  
	 
end if;   
   tempstring := 's' || stamp;
RETURN tempstring;
 END;
END_PROC;
 

CREATE OR REPLACE procedure alpine_miner_randomforest_initpre(  text,   text,     text,  boolean)
  language nzplsql
  RETURNS integer AS
 
  BEGIN_PROC
   DECLARE 
	tablename ALIAS FOR $1;
	stamp ALIAS FOR $2;

	infortable ALIAS FOR $3;
	istemp ALIAS FOR $4;
	
	infor   VARRAY( 1000000 ) of text;
	infornumber integer;
	rownumber integer;
	classnumber integer;
	sql text;
	peoso float;
	totalpeoso float;
	tempstring text;
	classstring  VARRAY( 1000000 ) of text;
	i integer:= 0;
		 myrecord record;
BEGIN
	sql:='select info as s from  "'||infortable||'"' ;
   i:=1;
     	for myrecord in execute sql loop
			 infor(i):=myrecord.s;
		i:=i+1;
		end loop;
	infornumber:=i-1;
	execute immediate 'select droptable_if_exists(''id'||stamp||''')'; 
	execute  immediate 'Create   table "id'||stamp||'" as select *,row_number()over(order by 1) as alpine_randomforest_id from '||tablename||' ';
	execute immediate 'drop table '||tablename; 
	if istemp=true
	then execute immediate 'Create temp table '||tablename||' as select * from "id'||stamp||'" ';
	else
	execute immediate 'Create  table '||tablename||' as select * from "id'||stamp||'" ';
	end if ;
	execute immediate 'select droptable_if_exists(''to'||stamp||''')'; 
	sql:=infor(1);
 	tempstring:='update '||tablename||' set "C('||sql||')"=0';

  
  for i in 2 .. infornumber loop
  sql:=infor(i);
    tempstring := tempstring||', "C(' || sql ||')"=0';
  
  end loop;
  execute immediate tempstring;
END;
END_PROC;



CREATE OR REPLACE procedure alpine_miner_randomforest_prere(  text,   text,   text,   int)
  language nzplsql
  RETURNS integer AS
  BEGIN_PROC
   DECLARE 
 	tablename ALIAS FOR $1;
	dependcolumn ALIAS FOR $2;
	infortable ALIAS FOR $3;
	isnumeric ALIAS FOR $4;
	infor   VARRAY( 1000000 ) of text;
	classnumber integer;
	sql text;
	sql2 text;
	casesql text;
	tempstring text;
	tempstring1 text;
	i integer:= 0;
	j integer :=0;
	myrecord record;
BEGIN
	sql:='select info as s from   '||infortable ;
   i:=1;
     	for myrecord in execute sql loop
			 infor(i):=myrecord.s;
		i:=i+1;
		end loop;
	classnumber:=i-1;
	sql:= 'update '||tablename||' set  "P('||dependcolumn||')" =  ';
	sql2 := '(';

	i:=classnumber;
	while i>1 loop
	tempstring:=infor(i);
	sql2 :=sql2||'  "C('||tempstring||')" ,';
	i:=i-1;
	end loop;
	tempstring:=infor(1);
	sql2:=sql2||' "C('||tempstring||')")';
	casesql:= ' case ';
	
		for i in 1.. classnumber loop
		casesql:=casesql||'  when ';
		tempstring:=infor(i);
		for j in 1.. classnumber loop
		tempstring1:=infor(j);
		if j=1 
		then
					casesql:=casesql||' "C('||tempstring||')" >=  "C('||tempstring1||')" ';	
		else  casesql:=casesql||' and "C('||tempstring||')" >=  "C('||tempstring1||')" ';
		end if;
	end loop;
		casesql:=casesql|| ' then ';
				if isnumeric = 1 then
			casesql := casesql || tempstring;
		else
			casesql := casesql||''''||tempstring||'''';
		end if;
	end loop;
	casesql:= casesql||' END ';
	

	sql := sql||casesql;

	execute immediate sql;


END;
END_PROC;





CREATE OR REPLACE procedure alpine_miner_randomforest_prestep(  text,   text,       text )
 language nzplsql
  RETURNS double 
  AS
   BEGIN_PROC
   DECLARE 
   	tablename ALIAS FOR $1;
	temptable ALIAS FOR $2;
 
 
	infortable  ALIAS FOR $3;
	infor  VARRAY( 1000000 ) of text;
	infornumber integer;
	sql text;
	tempstring text;
	i integer;
	myrecord record;
BEGIN	
			sql:='select info as s from  "'||infortable||'"' ;
  i:=1;
     	for myrecord in execute sql loop
			infor(i):=myrecord.s;
		i:=i+1;
		end loop;
	infornumber:=i-1;
	
	tempstring:=infor(1);
	sql:='update ' || tablename || '  set "C(' ||
                      tempstring || ')"= ' || tablename || '."C(' || tempstring ||
                      ')"+ '||temptable||'."C('||tempstring||')"::double ' ;
			for i in 2.. infornumber loop
				tempstring:=infor(i);
			sql:=sql||' ,"C('||tempstring||')"= '||tablename||'."C('||tempstring||')"+ 
				'||temptable||'."C('||tempstring||')"::double '  ;
					
	end loop;

		sql:=sql||'from '||temptable||'  where '||tablename||'.alpine_randomforest_id = '||temptable||'.alpine_randomforest_id';
	execute immediate sql;
RETURN 1;
END;
END_PROC;



