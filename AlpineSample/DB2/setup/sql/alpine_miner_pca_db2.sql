CREATE or replace PROCEDURE   alpine_miner_initpca(
						tablename    VARCHAR(4000),
						valueschemaname VARCHAR(4000),
                                                valueoutname VARCHAR(4000),
                                                infor        varcharArray,
                                                createway    VARCHAR(4000),
                                                dropif       VARCHAR(4000),
 						OUT result Floatarray )
 LANGUAGE SQL

BEGIN
   DECLARE  columnnumber integer;
   DECLARE  i            integer;
   DECLARE  j            integer;
   DECLARE  numberindex  integer;
   DECLARE  temp         float;
   DECLARE  sql1         CLOB(255K);
   DECLARE  valuesql     CLOB(255K);
 DECLARE rownumber       integer;
  DECLARE execStr VARCHAR(4000);
  DECLARE stmt3 STATEMENT;
    DECLARE curs3 CURSOR FOR stmt3;
  commit;
  SET columnnumber = CARDINALITY(infor);

  IF dropif = 'yes' THEN
  SET execStr='call PROC_DROPSCHTABLEIFEXISTS(''' ||valueschemaname||''','''|| valueoutname ||
                      ''')';

    execute immediate execStr;
    COMMIT;
  END IF;
  SET sql1  = 'create table '||valueschemaname||'.'||valueoutname || '( ' || infor[1] ||
          '  double precision';
 SET i=2;
  while i <= columnnumber do
    SET sql1=sql1|| ', ' || infor[i]|| ' double precision';
	SET i=i+1;
  end while;
  SET sql1=sql1||',"alpine_pcadataindex" integer,"alpine_pcaevalue" float,"alpine_pcacumvl" float,"alpine_pcatotalcumvl" float) ';
  execute immediate sql1;
  COMMIT;



  IF createway = 'cov-sam' THEN
   SET numberindex  = 1;
   SET i = 1;
    while i<= columnnumber do
	SET j=i;
      while j<= columnnumber do

	SET execStr='select covar(' || infor[i] || ' ,' ||
                          infor[j] || ') from ' || tablename  ;

       PREPARE stmt3 FROM execStr;
        OPEN curs3;
        FETCH FROM curs3 INTO temp;
        CLOSE curs3;


  SET execStr='select count(*) from ' || tablename  ;
        PREPARE stmt3 FROM execStr;
        OPEN curs3;
        FETCH FROM curs3 INTO rownumber;
        CLOSE curs3;



         SET      result[numberindex] = temp*rownumber/(rownumber-1);
       SET numberindex = numberindex + 1;
	SET j=j+1;
      end while;
	SET i=i+1;
    end while;
  ELSE
    IF createway = 'cov-pop' THEN
     SET numberindex = 1;
      SET i = 1;
	
    while i<= columnnumber do
	SET j=i;
      while j<= columnnumber do
	SET execStr='select covar(' || infor[i] || ' ,' ||
                          infor[j] || ') from ' || tablename  ;
        PREPARE stmt3 FROM execStr;
        OPEN curs3;
        FETCH FROM curs3 INTO temp;
        CLOSE curs3;
      
       
        SET  result[numberindex] = temp;
         SET numberindex = numberindex + 1;
       SET j=j+1;
      end while;
	SET i=i+1;
      end while;
    ELSE
      IF createway = 'corr' THEN
       SET numberindex = 1;
        SET i = 1;
    while i<= columnnumber do
	SET j=i;
      while j<= columnnumber do
	SET execStr='select corr(' || infor[i] || ' ,' ||
                          infor[j] || ') from ' || tablename  ;
	
       PREPARE stmt3 FROM execStr;
        OPEN curs3;
        FETCH FROM curs3 INTO temp;
        CLOSE curs3;     
         SET     result[numberindex] = temp;
          SET  numberindex = numberindex + 1;
         SET j=j+1;
	
      end while;
	SET i=i+1;
        end while;
      END IF;
    END IF;
  END IF;
  commit;
return numberindex;
END@

CREATE or replace procedure ALPINE_MINER_PCARESULT(tablename    VARCHAR(4000),
                                                 
						  outschemaname VARCHAR(4000),
                                                  outtablename VARCHAR(4000),
                                                 
                                                  valuename    VARCHAR(4000),
                                                  pcanumber    integer,
                                                  dropif       VARCHAR(4000)
, remainname   varcharArray, infor        varcharArray)
  LANGUAGE SQL
BEGIN
 
    DECLARE   columnnumber integer;
    DECLARE   i            integer;
    DECLARE   j            integer;
    DECLARE   remainnumber integer;
    DECLARE   execStr 	CLOB(1024K);
    DECLARE   totalsql  CLOB(1024K);
    DECLARE   sumsql    CLOB(1024K);
    DECLARE   remainsql CLOB(1024K);
    DECLARE selectsql CLOB(1024K);
    DECLARE insertsql CLOB(1024K);
    DECLARE   tempvalue float;
    DECLARE stmt3 STATEMENT;
    DECLARE curs3 CURSOR FOR stmt3;
  


  if remainname is not null then

   SET remainnumber  = CARDINALITY(remainname);

  SET  remainsql     = ' ';
	SET i=1;

	while i<=  remainnumber do

	
    call dbms_lob.append_clob(remainsql, ',' || remainname[i]);
set i=i+1;
    end while;
  else
   SET remainsql = ' ';
  end if;
  SET columnnumber = CARDINALITY(infor);

  IF dropif = 'yes' THEN

	SET execStr='call PROC_DROPSCHTABLEIFEXISTS(''' || outschemaname ||
                      ''','''||outtablename||''')';
	execute immediate  execStr;
	COMMIT;
  
  END IF;
 SET totalsql = 'create table  ' || outschemaname ||
                      '.' || outtablename || '   as  ';
SET selectsql = '( select ';
 SET i        = 1;
 SET sumsql   = ' ';

  while i <= pcanumber do
	SET execStr='(select ' || infor[1] || ' from ' || valuename ||
                      ' where  ' || valuename || '."alpine_pcadataindex"=' ||
                      (i - 1) || ')';
    PREPARE stmt3 FROM execStr;
        OPEN curs3;
        FETCH FROM curs3 INTO tempvalue;
        CLOSE curs3;
        

   call	dbms_lob.append_clob(selectsql,
                    ' ' || tablename || '.' || infor[1] || '* (' ||
                    tempvalue || ')');

   SET j = 2;
    while j <= columnnumber do
      SET execStr= '(select ' || infor[j] || ' from ' || valuename ||
                        ' where  ' || valuename ||
                        '."alpine_pcadataindex"=' || (i - 1) || ')';
    PREPARE stmt3 FROM execStr;
        OPEN curs3;
        FETCH FROM curs3 INTO tempvalue;
        CLOSE curs3;
      call	dbms_lob.append_clob(selectsql,
                      '+ ' || tablename || '.' || infor[j] || '*(' ||
                      tempvalue || ')');
    SET  j = j + 1;
    end while;
    IF i = pcanumber then
      call	dbms_lob.append_clob(selectsql, '   "attribute' || i || '"');
    ELSE
     call	dbms_lob.append_clob(selectsql, '   "attribute' || i || '" ,');
    END IF;
  SET  i  = i + 1;
  end while;

  call	dbms_lob.append_clob(selectsql, remainsql);
  call	dbms_lob.append_clob(selectsql, ' from ' || tablename||')');
  call	dbms_lob.append_clob(totalsql, selectsql);

 call	dbms_lob.append_clob(totalsql, ' definition only');


  execute immediate totalsql;
  COMMIT;
	SET insertsql = 'insert into  ' || outschemaname ||
                      '.' || outtablename ;
call dbms_lob.append_clob(insertsql,selectsql);

 execute immediate insertsql;
  commit;
  
END@

