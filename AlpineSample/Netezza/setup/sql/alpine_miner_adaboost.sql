CREATE OR REPLACE procedure alpine_miner_adaboost_inittra(  text,   text,   text,   text,  text )
 language nzplsql
   returns integer
   as
   BEGIN_PROC
DECLARE 
	schemaname ALIAS FOR $1;
	tablename ALIAS FOR $2;
	stamp ALIAS FOR $3;
	dependcolumn ALIAS FOR $4;
	infortable ALIAS FOR $5;
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
  execute immediate 'select droptable_if_exists(''s' || stamp || ''')'; 
	execute immediate 'Create  table '||schemaname||'."dn' || stamp || '" 
                     as select *
                    from '||schemaname||'.'|| tablename ||' where '||dependcolumn||' is not null';
  sql:='select count(*) as  c  from '||schemaname||'."dn' || stamp || '"';
   	for myrecord in execute sql loop
		rownumber:=myrecord.c;
			end loop;
	sql:='select valueinfo as c from  '||infortable ;
   i:=1;
     	for myrecord in execute sql loop
			dependinfor(i):=myrecord.c;
		i:=i+1;
		end loop;
  classnumber:=i-1;
	peoso := 1.0::double / rownumber;
	execute immediate 'Create  table '||schemaname||'."pnew' || stamp || '" 
                      as select *,
                    row_number()over(order by 1) as alpine_adaboost_id,
                    '||peoso||'::double as  alpine_adaboost_peoso, 
                     row_number()over(order by 1) *'||peoso||'::double as alpine_adaboost_totalpeoso 
                    from '||schemaname||'."dn' || stamp || '"  ';



	 execute immediate 'select droptable_if_exists(''tp'||stamp||''')'; 

	 execute immediate 'CREATE  TABLE '||schemaname||'."tp'||stamp||'" as select * from  '||schemaname||'."pnew'||stamp||'" ';

			execute immediate 'select droptable_if_exists(''p'||stamp||''')'; 

		sql:='CREATE  TABLE '||schemaname||'."p'||stamp||'" as select *';
	i:=1;

	
	while i<=classnumber	loop
	tempstring:=dependinfor(i);
			sql:=sql||',  0.0::double  as "C('||tempstring||')"';
			
			
			i:=i+1;
	end loop;
	
	
	sql:=sql||' from '||schemaname||'."pnew'||stamp||'" ';

  
  	
	execute immediate sql;
	execute immediate 'create table '||schemaname||'."s'||stamp||'" as select * from '||schemaname||'."pnew'||stamp||'"  ';
	
RETURN rownumber;
 END;
END_PROC;


CREATE OR REPLACE procedure alpine_miner_adaboost_changep( text , 
														text , 
														text , 
														text , 
														text , 
														text , 
														text )
 
 language nzplsql
  RETURNS double 
  AS
   BEGIN_PROC
   DECLARE 
	schemaname ALIAS FOR $1;
	tablename ALIAS FOR $2;
	stamp ALIAS FOR $3;
	dependcolumnq ALIAS FOR $4;
	dependentcolumnreplaceq ALIAS FOR $5;
	dependentcolumnstr ALIAS FOR $6;
	infortable  ALIAS FOR $7;
	dependinfor  VARRAY( 1000000 ) of text;
	infornumber integer;
	rownumber float;
	i integer;
	peoso float;	
	totalpeoso float;
	temppeoso float;
	wrongnumber float;
	err float;
	maxerror float;
	ind integer ;
	c float  ;
	sql text;
	sqlan text;
	tempstring text;
	 myrecord record;
BEGIN
	execute immediate 'update  '||schemaname||'."tp'||stamp||'" set "notsame" = CASE WHEN '||dependentcolumnstr||' = "P('||dependentcolumnreplaceq||')"::varchar(128)  THEN 0 ELSE 1 END';
	sql:= 'select count(*) as c from '||schemaname||'."tp'||stamp||'" where "notsame" =1';
  	for myrecord in execute sql loop
	wrongnumber:=myrecord.c;
	end loop;
	sql:='select samplecolumn as s from  '||infortable ;
	i:=1;
   	for myrecord in execute sql loop
			dependinfor(i):=myrecord.s;
		i:=i+1;
		end loop;
	infornumber:=i-1;
	sql:= 'select count(*)  as c from '||schemaname||'.'||tablename ;
	for myrecord in execute sql loop
		rownumber:=myrecord.c;
			end loop; 
	err := wrongnumber/rownumber;
	sql:=  'select count as c from ( select * from (select '||dependcolumnq||', count(*) as count from '||schemaname||'.'||tablename||' group by '||dependcolumnq||') foo order by count desc limit 1) AS foo' ;
	for myrecord in execute sql loop
		maxerror:=myrecord.c;
			end loop; 
	
	maxerror := maxerror/rownumber;



	IF err>=maxerror
	THEN	c=0.001;
		execute immediate 'update '||schemaname||'."pnew'||stamp||'"  set alpine_adaboost_peoso='||1::double/rownumber||',alpine_adaboost_totalpeoso= alpine_adaboost_id::double *'||1/rownumber;
	ELSIF err=0
	THEN c=3;
		execute immediate 'update '||schemaname||'."pnew'||stamp||'"  set alpine_adaboost_peoso='||1::double/rownumber||',alpine_adaboost_totalpeoso= alpine_adaboost_id::double *'||1/rownumber;
	ELSE 	
		c :=ln ((1-err)/err);
		c := c/2;
		totalpeoso :=0;
		temppeoso :=0;
		i:=1;

		execute immediate 'update  '||schemaname||'."pnew'||stamp||'" set "notsame" = '||schemaname||'."tp'||stamp||'"."notsame" from '||schemaname||'."tp'||stamp||'" where '||schemaname||'."pnew'||stamp||'".alpine_adaboost_id = '||schemaname||'."tp'||stamp||'".alpine_adaboost_id'; 
		temppeoso := temppeoso*exp (c*ind); 
		execute immediate 'update  '||schemaname||'."pnew'||stamp||'" set  alpine_adaboost_peoso = alpine_adaboost_peoso*exp('||c||'::double*"notsame") ';
--		sql:='select sum(alpine_adaboost_peoso::double) as s from '||schemaname||'."pnew'||stamp||'"';
--		for myrecord in execute sql loop
--		totalpeoso:=myrecord.s;
--			end loop; 
		

--		execute immediate 'update  '||schemaname||'."pnew'||stamp||'" set  alpine_adaboost_peoso = alpine_adaboost_peoso::double/'||totalpeoso;
		execute immediate 'select droptable_if_exists(''sp'||stamp||''')'; 
		execute immediate 'Create temp table "sp'||stamp||'" as select alpine_adaboost_id, alpine_adaboost_peoso, sum(alpine_adaboost_peoso::double)over(order by alpine_adaboost_id ) alpine_sum_peoso  from 
 '||schemaname||'."pnew'||stamp||'" ';
		
		execute immediate 'update  '||schemaname||'."pnew'||stamp||'" set  alpine_adaboost_totalpeoso = alpine_sum_peoso from "sp'||stamp||'" where '||schemaname||'."pnew'||stamp||'".alpine_adaboost_id = "sp'||stamp||'".alpine_adaboost_id';
		END IF ;

	  i := 2;
  
 	sql:=dependinfor(1);
  sqlan:='update '||schemaname||'."p'|| stamp || '" set "C('||sql||')" ='||schemaname||'."p'||stamp||'"."C('||sql||')"::double+
                  '||schemaname||'."tp'||stamp||'"."C('||sql||')"::double *'||c||'::double' ;
                    
  while i <= infornumber loop
  			sql:=dependinfor(i);
        sqlan:=sqlan||' , "C('||sql||')"='||schemaname||'."p'||stamp||'"."C('||sql||')"::double+
          '||schemaname||'."tp'||stamp||'"."C('||sql||')"::double  *'||c||'::double';
        i:=i+1;
   end loop;
   sqlan:=sqlan||' from  '||schemaname||'."tp'||stamp||'"  where '||schemaname||'."p'||stamp||'".alpine_adaboost_id = '||schemaname||'."tp'||stamp||'".alpine_adaboost_id';
   execute immediate sqlan;

RETURN c;

END;
END_PROC;


 CREATE OR REPLACE procedure alpine_miner_adaboost_sample( text,  text,  text,integer)
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
   
	 sql:= ' select max(alpine_adaboost_totalpeoso) as m from '||schemaname||'.'||tablename  ;
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
"alpine_miner_adaboost_r" from ' || schemaname|| '.' || tablename ||' order by "alpine_miner_adaboost_r")   ';
   
    

    if partnumber=1
    then
    execute immediate  'create table ' || schemaname || '."s' || stamp || '"   as (select * from ' || schemaname
    || '.' ||tablename || ' join ' ||schemaname|| '."r' ||stamp || '" on ' || schemaname || '.' || tablename
    || '.alpine_adaboost_totalpeoso  >= ' || schemaname || '."r' ||stamp|| '"."alpine_miner_adaboost_r" and ' || schemaname || '
.' || tablename|| '.alpine_adaboost_peoso > (' ||schemaname || '.' || tablename || '.
alpine_adaboost_totalpeoso-' || schemaname || '."r' || stamp || '"."alpine_miner_adaboost_r" ) )   ';
 
   
 
 else 
	  tempstring:=' select alpine_adaboost_totalpeoso as peoso from '||schemaname||'.'||tablename||' where mod(alpine_adaboost_id,'||partsize||')=0 order by peoso';
 
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
 			where alpine_adaboost_totalpeoso>'||temppeoso||' and  alpine_adaboost_totalpeoso<='||temppeoso1||')   foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "random"
 			>'||temppeoso||' and   "alpine_miner_adaboost_r"<='||temppeoso1||')   foor'||i||' on foo'||i||'.alpine_adaboost_totalpeoso >=foor'||i||'."alpine_miner_adaboost_r" and foo'||i||'.alpine_adaboost_peoso > 
 		(foo'||i||'.alpine_adaboost_totalpeoso-foor'||i||'."alpine_miner_adaboost_r"))  ';
 	 
  		execute  immediate tempstring;
 
	while i <partnumber loop
	  i:=i+1;
	   
	   temppeoso:=splitpeoso(i);
	   j:=i+1;
 
    temppeoso1:=splitpeoso(j);
	 	tempstring := '  insert into  '||schemaname||'."s'||stamp||'" (  select * from ( select * from '||schemaname||'.'||tablename||' 
  		where alpine_adaboost_totalpeoso>'||temppeoso||' and  alpine_adaboost_totalpeoso<='||temppeoso1||') as foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "alpine_miner_adaboost_r" 
  		>'||temppeoso||' and  "alpine_miner_adaboost_r"<='||temppeoso1||') as foor'||i||' on foo'||i||'.alpine_adaboost_totalpeoso >=foor'||i||'."alpine_miner_adaboost_r" and foo'||i||'.alpine_adaboost_peoso > 
 		(foo'||i||'.alpine_adaboost_totalpeoso-foor'||i||'."alpine_miner_adaboost_r")) ';
 
		execute   immediate tempstring;
	 
		end loop;
		  
	 
end if;   
   tempstring := 's' || stamp;
RETURN tempstring;
 END;
END_PROC;
 
 



CREATE OR REPLACE procedure alpine_miner_adaboost_prere(  text,   text,   text,   int)
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

CREATE OR REPLACE procedure alpine_miner_adaboost_prestep(  text,   text,   text,   double  ,   text )
 language nzplsql
  RETURNS double 
  AS
   BEGIN_PROC
   DECLARE 
   	tablename ALIAS FOR $1;
	temptable ALIAS FOR $2;
	dependcolumn ALIAS FOR $3;
	c ALIAS FOR $4;
	infortable  ALIAS FOR $5;
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
                      ')"+ '||temptable||'."C('||tempstring||')"::double*'||c;
			for i in 2.. infornumber loop
				tempstring:=infor(i);
			sql:=sql||' ,"C('||tempstring||')"= '||tablename||'."C('||tempstring||')"+ 
				'||temptable||'."C('||tempstring||')"::double*'||c ;
					
	end loop;

		sql:=sql||'from '||temptable||'  where '||tablename||'.alpine_adaboost_id = '||temptable||'.alpine_adaboost_id';
	execute immediate sql;
RETURN c;
END;
END_PROC;


CREATE OR REPLACE procedure alpine_miner_adaboost_initpre(  text,   text,   text,   text,  boolean)
  language nzplsql
  RETURNS integer AS
 
  BEGIN_PROC
   DECLARE 
	tablename ALIAS FOR $1;
	stamp ALIAS FOR $2;
	dependcolumn ALIAS FOR $3;
	infortable ALIAS FOR $4;
	istemp ALIAS FOR $5;
	
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
 
	execute  immediate 'Create   table "id'||stamp||'" as select *,row_number()over(order by 1) as alpine_adaboost_id from '||tablename||' ';

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
