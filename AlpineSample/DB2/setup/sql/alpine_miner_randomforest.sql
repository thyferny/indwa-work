CREATE  or replace  PROCEDURE   alpine_miner_randomforest_inittra (schemaname VARCHAR(4000), 
                                                tablename VARCHAR(4000), 
                                                stamp VARCHAR(4000), 
                                                dependcolumn VARCHAR(4000) )
LANGUAGE SQL

BEGIN
    DECLARE sqlan CLOB(255K);
    DECLARE sqlan1 CLOB(255K);
    DECLARE rownumber DECIMAL(31,0);
    DECLARE classnumber DECIMAL(31,0);
    DECLARE peoso FLOAT;
    DECLARE i DECIMAL(31,0) DEFAULT 0.0;
    DECLARE execStr VARCHAR(4000);
    DECLARE stmt STATEMENT;
    DECLARE curs CURSOR FOR stmt;
    COMMIT;
    SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''"pnew' || stamp || '"'')';
    EXECUTE IMMEDIATE execStr;
    commit;
    SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''"dn' || stamp || '"'')';
    EXECUTE IMMEDIATE execStr;
    commit;

   

    SET execStr = 'Create  table ' ||schemaname|| '."dn' || stamp || '" 
		  as (select ' || schemaname || '.' || tablename || '.* from ' ||schemaname || '.' ||tablename || ' where ' || dependcolumn || ' is not null)  definition only';
    EXECUTE IMMEDIATE execStr;
    commit;
    
   SET execStr = 'insert into  ' ||schemaname|| '."dn' || stamp || '" 
		  ( select ' || schemaname || '.' || tablename || '.* from ' ||schemaname || '.' ||tablename || ' where ' || dependcolumn || ' is not null )';
    EXECUTE IMMEDIATE execStr;
    commit;

    SET execStr = 'select count(*)   from ' ||schemaname || '."dn' || stamp || '"';
    PREPARE stmt FROM execStr;
    OPEN curs;
    FETCH FROM curs INTO rownumber;
    CLOSE curs;
    

    SET peoso =  1.0 / rownumber;
    
   

    SET execStr = 'Create  table ' ||schemaname || '."pnew' || stamp || '"   as ( select ' ||schemaname || '."dn' || stamp || '".*,
	 row_number()over(order by 1) "alpine_randomforest_id", ' || peoso || ' "alpine_randomforest_peoso", ROWNUMBER()OVER ()*' || peoso 
	 || ' "alpine_randomforest_totalpeoso" from ' || schemaname || '."dn' ||stamp || '") definition only';
    EXECUTE IMMEDIATE execStr;
    commit;
    SET execStr = 'insert into ' ||schemaname || '."pnew' || stamp || '"    ( select ' ||schemaname || '."dn' || stamp || '".*,
	 row_number()over(order by 1) "alpine_randomforest_id", ' || peoso || ' "alpine_randomforest_peoso", ROWNUMBER()OVER ()*' || peoso 
	 || ' "alpine_randomforest_totalpeoso" from ' || schemaname || '."dn' ||stamp || '") ';
    EXECUTE IMMEDIATE execStr;
    commit;

 
 
    COMMIT;
END@


CREATE or replace PROCEDURE alpine_miner_randomforest_sample (schemaname VARCHAR(4000), 
                                               tablename VARCHAR(4000), 
                                               stamp VARCHAR(4000),
						partsize integer )
LANGUAGE SQL
BEGIN
declare SQLCODE int ;
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
    DECLARE i INTEGER;
    DECLARE rownumber integer;
    DECLARE partnumber integer;
    DECLARE temppeoso  FLOAT;
    declare randomnumber float;
    DECLARE maxpeoso FLOAT;
    DECLARE tempstring VARCHAR(4000);
    DECLARE execStr VARCHAR(4000);
    
    DECLARE splitpeoso floatarray;
    DECLARE SQLSA VARCHAR(4000);
    DECLARE at_end SMALLINT DEFAULT 0;
    DECLARE not_found CONDITION for SQLSTATE '02000';
    DECLARE curs3  CURSOR  WITH RETURN FOR SQLSA ;
    DECLARE CONTINUE HANDLER for not_found 
    SET at_end = 1;
         
 SET execStr='select count(*) from '||schemaname||'.'||tablename ;
     
	PREPARE SQLSA FROM execStr ;
	OPEN  curs3 ;
	FETCH FROM curs3  into rownumber;
	close curs3;
     SET execStr=' select max("alpine_randomforest_totalpeoso")  from '||schemaname||'.'||tablename ;
    PREPARE SQLSA FROM execStr ;
	OPEN  curs3 ;
     FETCH FROM curs3 into maxpeoso;
     CLOSE curs3;

	if partsize>= rownumber
	then 
	set	partnumber =1;
	else 
		if mod(rownumber ,partsize)=0
		then
			set partnumber = rownumber/partsize;
		else 
			set partnumber =trunc(rownumber/partsize)+1;
		end if;
	end if;

    SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''"s' ||stamp || '"'')';
    EXECUTE IMMEDIATE execStr;
    COMMIT;
    SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''"r' || stamp || '"'')';
    EXECUTE IMMEDIATE execStr;
    COMMIT;
 
    SET execStr = 'create table ' || schemaname || '."r' || stamp || '"   as ( select rand() 
"alpine_miner_randomforest_r" from ' || schemaname|| '.' || tablename ||' order by "alpine_miner_randomforest_r") definition only ';
    EXECUTE IMMEDIATE execStr;
    COMMIT;
       SET execStr = 'insert into ' || schemaname || '."r' || stamp || '"    select rand() 
"alpine_miner_randomforest_r" from ' || schemaname|| '.' || tablename ||' order by "alpine_miner_randomforest_r" ';
    EXECUTE IMMEDIATE execStr;
    COMMIT;


    if partnumber=1
    then
    SET execStr = 'create table ' || schemaname || '."s' || stamp || '"   as (select * from ' || schemaname
    || '.' ||tablename || ' join ' ||schemaname|| '."r' ||stamp || '" on ' || schemaname || '.' || tablename
    || '."alpine_randomforest_totalpeoso"  >= ' || schemaname || '."r' ||stamp|| '"."alpine_miner_randomforest_r" and ' || schemaname || '
.' || tablename|| '."alpine_randomforest_peoso" > (' ||schemaname || '.' || tablename || '.
"alpine_randomforest_totalpeoso"-' || schemaname || '."r' || stamp || '"."alpine_miner_randomforest_r" ) ) definition only  ';
    EXECUTE IMMEDIATE execStr;
    COMMIT;

  SET execStr = 'insert into ' || schemaname || '."s' || stamp || '"     (select * from ' || schemaname
    || '.' ||tablename || ' join ' ||schemaname|| '."r' ||stamp || '" on ' || schemaname || '.' || tablename
    || '."alpine_randomforest_totalpeoso"  >= ' || schemaname || '."r' ||stamp|| '"."alpine_miner_randomforest_r" and ' || schemaname || '
.' || tablename|| '."alpine_randomforest_peoso" > (' ||schemaname || '.' || tablename || '.
"alpine_randomforest_totalpeoso"-' || schemaname || '."r' || stamp || '"."alpine_miner_randomforest_r" ) ) ';
    EXECUTE IMMEDIATE execStr;
    COMMIT;
 else 
	set tempstring=' select "alpine_randomforest_totalpeoso" as peoso from '||schemaname||'.'||tablename||' where mod("alpine_randomforest_id",'||partsize||')=0 order by peoso';
 
	set i=1;
 	set splitpeoso[i]=0;
 
	PREPARE SQLSA FROM tempstring ;
	OPEN  curs3 ;
	
	fetch_loop2:
	LOOP
		fetch curs3 into temppeoso;
	   	 IF at_end <> 0 THEN 
		LEAVE fetch_loop2;
    		END IF;
		set i=i+1;
 		set splitpeoso[i]=temppeoso;
 
	end loop;

		if splitpeoso[i]!=maxpeoso
		then
			set i =i+1;
			set splitpeoso[i] =maxpeoso;
	 
		end if;
		set i =1;
    SET tempstring = 's' || stamp;
 
    COMMIT;
    
 	set tempstring ='create table '||schemaname||'."s'||stamp||'"   as (select * from  ( select * from '||schemaname||'.'||tablename||' 
 			where "alpine_randomforest_totalpeoso">'||splitpeoso[i]||' and  "alpine_randomforest_totalpeoso"<='||splitpeoso[i+1]||')   foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "alpine_miner_randomforest_r"
 			>'||splitpeoso[i]||' and   "alpine_miner_randomforest_r"<='||splitpeoso[i+1]||')   foor'||i||' on foo'||i||'."alpine_randomforest_totalpeoso" >=foor'||i||'."alpine_miner_randomforest_r" and foo'||i||'."alpine_randomforest_peoso" > 
 		(foo'||i||'."alpine_randomforest_totalpeoso"-foor'||i||'."alpine_miner_randomforest_r")) definition only ';
  		execute  immediate tempstring;
    	set tempstring ='insert into '||schemaname||'."s'||stamp||'" ( select * from  ( select * from '||schemaname||'.'||tablename||' 
			where "alpine_randomforest_totalpeoso">'||splitpeoso[i]||' and  "alpine_randomforest_totalpeoso"<='||splitpeoso[i+1]||')   foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "alpine_miner_randomforest_r"
			>'||splitpeoso[i]||' and  "alpine_miner_randomforest_r"<='||splitpeoso[i+1]||')   foor'||i||' on foo'||i||'."alpine_randomforest_totalpeoso" >=foor'||i||'."alpine_miner_randomforest_r" and foo'||i||'."alpine_randomforest_peoso" > 
		(foo'||i||'."alpine_randomforest_totalpeoso"-foor'||i||'."alpine_miner_randomforest_r")  ) ';
	 
  		execute  immediate tempstring;
	while i <partnumber do
	set i=i+1;
	set	tempstring = '  insert into  '||schemaname||'."s'||stamp||'"   select * from ( select * from '||schemaname||'.'||tablename||' 
  		where "alpine_randomforest_totalpeoso">'||splitpeoso[i]||' and  "alpine_randomforest_totalpeoso"<='||splitpeoso[i+1]||') as foo'||i||' join (select * from '||schemaname||'."r'||stamp||'" where "alpine_miner_randomforest_r" 
  		>'||splitpeoso[i]||' and  "alpine_miner_randomforest_r"<='||splitpeoso[i+1]||') as foor'||i||' on foo'||i||'."alpine_randomforest_totalpeoso" >=foor'||i||'."alpine_miner_randomforest_r" and foo'||i||'."alpine_randomforest_peoso" > 
 		(foo'||i||'."alpine_randomforest_totalpeoso"-foor'||i||'."alpine_miner_randomforest_r") ';
		execute   immediate tempstring;
		end while;
	commit;
end if;   
    RETURN 0;

END@



CREATE  or replace  procedure alpine_miner_randomforest_initpre (schemaname VARCHAR(4000), 
                                                tablename VARCHAR(4000), 
                                                stamp VARCHAR(4000), 
                                              --  dependcolumn VARCHAR(4000), 
                                                infor VarcharArray )
LANGUAGE SQL

BEGIN
    DECLARE tempstring CLOB(255K);
	DECLARE SCHEMATABLE CLOB(255K);
    DECLARE i DECIMAL(31,0) DEFAULT 0.0;
    DECLARE idtable VARCHAR(4000);
    DECLARE i1 INTEGER;
    DECLARE execStr VARCHAR(4000);
    COMMIT;
    IF schemaname IS NULL THEN 
         SET idtable = '"id' || stamp|| '"';
	SET execStr = ' call PROC_DROPTABLEIFEXISTS('''||idtable|| ''')';
	SET SCHEMATABLE=tablename;
    ELSE
    SET idtable = schemaname || '."id' ||stamp || '"';
	SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''"id' || stamp|| '"'')';
	SET SCHEMATABLE=schemaname||'.'||tablename;
    END IF;
    EXECUTE IMMEDIATE execStr;
	commit;
    SET execStr = 'Create  table ' || idtable || ' as (select ' || SCHEMATABLE || '.*,row_number()over() "alpine_randomforest_id" from '||SCHEMATABLE || ') definition only';
	EXECUTE IMMEDIATE execStr;
	commit;
    SET execStr = 'insert into ' || idtable || '  (select ' || SCHEMATABLE || '.*,row_number()over() "alpine_randomforest_id" from '||SCHEMATABLE|| ') ';
	EXECUTE IMMEDIATE execStr;
	commit;
	IF schemaname IS NULL THEN 
 
   
	SET execStr = ' call PROC_DROPTABLEIFEXISTS('''||tablename|| ''')';
	else
    SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''' || tablename || ''')';
 END IF;
    EXECUTE IMMEDIATE execStr;
    commit;

    SET execStr = 'Create  table ' || SCHEMATABLE || ' as (select * from ' || idtable||') definition only';
    EXECUTE IMMEDIATE execStr;
    commit;
    SET execStr = 'insert into ' || SCHEMATABLE || '   (select * from ' || idtable||')  ';
    EXECUTE IMMEDIATE execStr;
    commit;
	 IF schemaname IS NULL THEN 
 
   
	SET execStr = ' call PROC_DROPTABLEIFEXISTS('''||idtable|| ''')';
	else
    SET execStr = 'call PROC_DROPSCHTABLEIFEXISTS(''' || schemaname || ''',''' || idtable || ''')';
    END IF;
    EXECUTE IMMEDIATE execStr;
    commit;

    SET tempstring = (  'update ' || SCHEMATABLE || ' set "C(' || infor[1]) || ')"=0';
    SET i = 0;
    SET i1 = 2;
    WHILE i1 <= CARDINALITY(infor) DO
        
  SET tempstring=tempstring||', "C(' || infor[i1] || ')"=0';
        SET i1 = i1 + 1;
    
    END WHILE ;

    EXECUTE IMMEDIATE tempstring;
    COMMIT;
      SET execStr='CALL SYSPROC.ADMIN_CMD('' REORG TABLE ' || SCHEMATABLE ||' '')';
	EXECUTE IMMEDIATE execStr;
	commit;
    RETURN 0;

END@






CREATE  or replace  PROCEDURE   alpine_miner_randomforest_prere (tablename VARCHAR(4000), 
                                              dependcolumn VARCHAR(4000), 
                                              isnumeric DECIMAL(31,0), 
                                               infor VarcharArray )
LANGUAGE SQL

BEGIN
   DECLARE rownumber DECIMAL(31,0);
   DECLARE classnumber DECIMAL(31,0);
   DECLARE sqlan CLOB(255K);
   DECLARE sql2 CLOB(255K);
   DECLARE peoso FLOAT;
   DECLARE totalpeoso FLOAT;
   DECLARE tempstring VARCHAR(4000);
   DECLARE classstring VarcharArray;
   DECLARE i DECIMAL(31,0) DEFAULT 0.0;
   DECLARE err FLOAT;
   DECLARE i1 INTEGER;
   COMMIT;

    SET classnumber = CARDINALITY(infor);
    SET sqlan = 'update ' ||tablename || ' set  "P(' ||dependcolumn || ')" = CASE';
    SET sql2 = '(';
    SET i = classnumber;
    WHILE i > 1 DO
    SET sql2=sql2||'  "C(' || infor[i] || ')" ,';
        SET i = i - 1;
    END WHILE ;
   SET sql2=sql2||'"C(' || infor[1] || ')" )';

    SET i1 = 1;
    WHILE i1 <= classnumber DO
    SET sqlan=sqlan||' WHEN "C(' || infor[i1] || ')"=greatest';
    SET sqlan=sqlan|| sql2;
    SET sqlan=sqlan||' THEN ';
        IF isnumeric = 1 THEN 
    SET sqlan=sqlan|| infor[i1];
        ELSE
      SET sqlan=sqlan|| '''' || infor[i1] || '''';
      END IF;
      SET i1 = i1 + 1;
    END WHILE ;
    
   SET sqlan=sqlan|| ' END ';
    EXECUTE IMMEDIATE sqlan;
    COMMIT;
    SET err = err*1.0  / rownumber;
     RETURN 0;

END@


CREATE  or replace   PROCEDURE alpine_miner_randomforest_prestep (tablename VARCHAR(4000), 
                                                temptablename VARCHAR(4000), 
                                                infor VarcharArray )
LANGUAGE SQL

BEGIN
    DECLARE rownumber DECIMAL(31,0);
    DECLARE classnumber DECIMAL(31,0);
    DECLARE sqlan CLOB(255K);
    DECLARE peoso FLOAT;
    DECLARE totalpeoso FLOAT;
    DECLARE tempstring VARCHAR(4000);
    DECLARE classstring VarcharArray;
    DECLARE i DECIMAL(31,0) DEFAULT 0.0;
 	DECLARE execStr  CLOB(255K);
    DECLARE err FLOAT;
    DECLARE i1 INTEGER;
    COMMIT;
    SET execStr='CALL SYSPROC.ADMIN_CMD('' REORG TABLE ' || tablename ||' '')';
	EXECUTE IMMEDIATE execStr;
	COMMIT;
    SET sqlan = (   'update ' || tablename || '  set "C(' || infor[1] || ')"=  "C(' ||            infor[1] || ')"+ 
(select "C(' || infor[1]) || ')"  from ' || temptablename|| '  where ' || tablename || '."alpine_randomforest_id" = ' ||
temptablename || '."alpine_randomforest_id")  ';
     SET i1 = 2;
    WHILE i1 <= CARDINALITY(infor) DO
        
     SET sqlan=(sqlan||
                     ', "C(' || infor[i1] || ')"=  "C(' || infor[i1] ||
                     ')"+ (select "C(' || infor[i1] || ')"  from ' ||
                     temptablename || '  where ' || tablename ||
                     '."alpine_randomforest_id" = ' || temptablename ||
                     '."alpine_randomforest_id")  ');
      
        
        SET i1 = i1 + 1;
    
    END WHILE ;

    EXECUTE IMMEDIATE sqlan;
    COMMIT;

  
    RETURN 0;

END@
 
