CREATE OR REPLACE FUNCTION Alpine_PLDA_transfor(doccontent floatArray,diccontent varchararray)
returns varchararray

BEGIN
declare resultarray varchararray;
DECLARE indexnumber float;
	SET indexnumber=1;
    While indexnumber<=CARDINALITY(doccontent) do
    SET  resultarray[indexnumber]=diccontent[doccontent[indexnumber]];
    SET indexnumber=indexnumber+1;
	end while;
return resultarray;
END@




CREATE OR REPLACE FUNCTION  Alpine_PLDA_random(peoso  floatArray)
returns float
begin
DECLARE randomnumber float;
declare sumnumber float;
declare indexnumber float;
DECLARE execStr VARCHAR(4000);
DECLARE stmt1 STATEMENT;
DECLARE curs1 CURSOR FOR stmt1;

set sumnumber=0;

SET execStr = 'select rand() from sysibm.sysdummy1' ;
    PREPARE stmt1 FROM execStr;
    OPEN curs1;
    FETCH FROM curs1 INTO randomnumber;
    CLOSE curs1;
set indexnumber=1;

while indexnumber<=CARDINALITY(peoso) do
		set  sumnumber=sumnumber+peoso[indexnumber];
    if randomnumber<=sumnumber
    then return indexnumber;
    end if;
    set indexnumber=indexnumber+1;
    end while;

return indexnumber;
END@



CREATE OR REPLACE PROCEDURE  alpine_miner_plda_train ( content_table varchar(4000) , 
	docidcolumn varchar(4000), 
	doccontentcolumn varchar(4000), 
	alpha float , 
	beta float , 
	topicnumber float , 
	dictable varchar(4000),
	diccontentcolumn varchar(4000),
	iteration_number integer , 
	tempouttable1 varchar(4000), 
	tempouttable2 varchar(4000), 
	outtable varchar(4000),
	doctopictable varchar(4000),
	topicouttable varchar(4000))
LANGUAGE SQL
begin
declare SQLCODE int;
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
--DECLARE stmt STATEMENT;
--DECLARE curs CURSOR FOR stmt;
declare	i bigint;
declare	j float;
declare	k float;
declare	temptopic float;
declare	tempnumber float;
declare rowindex float;
declare totalrowindex float;
declare	mutibynumber float;
declare	tempiteration bigint;
declare	sql clob;
declare	tempstring clob;
declare	singledoc  floatarray;
declare	docwordtopic floatarray;
declare	peoso   floatarray;
declare	wordtopic floatarray;
declare	alldoctopic  floatarray;
declare	tempwordtopic floatarray;
declare	tempalldoctopic  floatarray;
declare	lastgeneinfo floatarray;
declare	lastwordtopic floatarray;
declare	doccontent floatarray;
declare	diccontent varchararray;
declare	sumpeoso float;
declare	wordnumber float;
DECLARE execStr VARCHAR(4000);

declare datarow pldadatarow;
declare topicrow pldatopicrow;
declare gdatarow gpldadatarow;
declare gtopicrow gpldatopicrow;
declare dicdatarow pldadicrow;
DECLARE SQLSA VARCHAR(4000);
Declare SQLSA1 VARCHAR(4000);
DECLARE at_end SMALLINT DEFAULT 0;
declare at_end1 smallint default 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE curs3  CURSOR  WITH RETURN FOR SQLSA ;
declare curs4 cursor with return for sqlsa1;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;


set	sql= 'select  count(*)  from '||dictable||' ';
PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
FETCH FROM curs3 INTO wordnumber;
CLOSE curs3;
	set sql='CREATE  TABLE '||tempouttable1||' ( alpinepldagenera float , '||docidcolumn||' float , '|| doccontentcolumn||' float,  alpine_plda_topic float)';
	execute immediate sql;
	set sql='CREATE  TABLE '||tempouttable2||' ( alpinepldagenera float,alpinepldadocid float ,alpinepldatopicid float,alpinepldacount float)';
	
	execute immediate sql;
	
	
	set tempnumber=1;
	while tempnumber<=topicnumber do
		set peoso[tempnumber]=1.0/topicnumber;
		set alldoctopic[tempnumber]=0;
		set tempnumber=tempnumber+1;
	end while;
	set tempnumber=1;
	while tempnumber <=wordnumber*topicnumber do	
		set wordtopic[tempnumber]=0;
		set tempnumber=tempnumber+1;
	end while;
	set sql = 'select distinct('||docidcolumn||') as alpineid from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid Fetch First 1 rows only';
	PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
		fetch curs3 into totalrowindex;
        CLOSE curs3;
	set rowindex=totalrowindex;
	set tempnumber=1;
	set sql = 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid';
	PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
	
	fetch_loop2:
	LOOP
		fetch curs3 into datarow;
	   	 IF at_end <> 0 THEN 
				set k=1;
				while k<=topicnumber do
				set singledoc[k]=0;
				set k=k+1;
				end while;
		
				set j = 1;
				while j<= (tempnumber-1) do
				set temptopic= Alpine_PLDA_random(peoso);
				set singledoc[temptopic]=singledoc[temptopic]+1;
				set docwordtopic[j]=temptopic;	
				set wordtopic[topicnumber*(doccontent[j]-1)+temptopic]=wordtopic[topicnumber*(doccontent[j]-1)+temptopic]+1;
				set alldoctopic[temptopic]=alldoctopic[temptopic]+1;
				set sql='insert into '||tempouttable1||' values ( 1,'||rowindex||','||doccontent[j]||','||temptopic||')';
				execute immediate  sql;
				set j = j+1;
				end while;
				set j=1;
				while j<=topicnumber do
				set sql='insert into '||tempouttable2||' values ( 1,'||rowindex||','||j||','||singledoc[j]||')';
				execute immediate  sql;
				set j=j+1;
				end while;
				set docwordtopic=null;
				set singledoc=null;
		LEAVE fetch_loop2;
    		END IF;
		if datarow.docid!=rowindex
		then
		
		set k=1;
		while k<=topicnumber do
			set singledoc[k]=0;
			set k=k+1;
		end while;
		
		set j = 1;
		while j<= (tempnumber-1) do
			set temptopic= Alpine_PLDA_random(peoso);
			set singledoc[temptopic]=singledoc[temptopic]+1;
			set docwordtopic[j]=temptopic;	
			set wordtopic[topicnumber*(doccontent[j]-1)+temptopic]=wordtopic[topicnumber*(doccontent[j]-1)+temptopic]+1;
			set alldoctopic[temptopic]=alldoctopic[temptopic]+1;
			set sql='insert into '||tempouttable1||' values ( 1,'||rowindex||','||doccontent[j]||','||temptopic||')';

			execute immediate  sql;
		set j = j+1;
		end while;
		set j=1;
		while j<=topicnumber do
		set sql='insert into '||tempouttable2||' values ( 1,'||rowindex||','||j||','||singledoc[j]||')';
		execute immediate  sql;
		set j=j+1;
		end while;
		set docwordtopic=null;
		set singledoc=null;
		set tempnumber=1;
		set rowindex=datarow.docid;
		set doccontent=null;
		end if;
		set i=datarow.docid;
		set doccontent[tempnumber]=datarow.contents;
		set tempnumber=tempnumber+1;
	END LOOP;
	CLOSE curs3 ;
	set tempiteration=2;
	while tempiteration<=iteration_number do
		set tempnumber=1;
		while tempnumber<=wordnumber*topicnumber do
			set tempwordtopic[tempnumber]=0;
			set tempnumber=tempnumber+1;
		end while;
		set tempnumber=1;
		while tempnumber  <=topicnumber do
			set tempalldoctopic[tempnumber]=0;
			set tempnumber=tempnumber+1;
		end while;
		set rowindex=totalrowindex;
		set tempnumber=1;
		set sql = 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent , alpine_plda_topic from '|| tempouttable1 ||' where alpinepldagenera='||(tempiteration-1)||' and '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid';
		PREPARE SQLSA FROM sql ;
		OPEN  curs3 ;
		SET at_end = 0;
		set k=0;
		fetch_loop3:
		LOOP
		fetch curs3 into gdatarow;
   	IF at_end <> 0 THEN 
			set k=0;
			set j = 1;
			set tempnumber=1;
			while tempnumber <=topicnumber do
				set 	singledoc[tempnumber]=0;
				set tempnumber=tempnumber+1;
			end while;
			set tempnumber=1;
		set sql = 'select alpinepldadocid, alpinepldatopicid  as alpineid,alpinepldacount from '|| tempouttable2 ||' where alpinepldagenera='||(tempiteration-1)||' and alpinepldadocid= '||rowindex||'  order by alpineid';
		PREPARE SQLSA1 FROM sql ;
		OPEN  curs4 ;
		
		set k=1;
		while k<=topicnumber do
		set k=k+1;
		fetch curs4 into topicrow;
		set lastgeneinfo[topicrow.topicid]=topicrow.contents;
		end while;
		close curs4;
		fetch curs3 into gdatarow;
			while tempnumber <=CARDINALITY(doccontent) do
				set sumpeoso=0;
				set j=1;
				while j <=topicnumber do
					set peoso[j]=(lastgeneinfo[j]+alpha)*(wordtopic[topicnumber*(doccontent[tempnumber]-1)+j]+beta)/(alldoctopic[j]+wordnumber*beta);
					set sumpeoso=sumpeoso+peoso[j];
					set j=j+1;
				end while;
				set j=1;
				while  j <=topicnumber do
					set peoso[j]=peoso[j]/sumpeoso;
					set j=j+1;
				end while;
				set temptopic= Alpine_PLDA_random(peoso);
				set singledoc[temptopic]=singledoc[temptopic]+1;
				set lastwordtopic[tempnumber]=temptopic;
				set tempwordtopic[topicnumber*(doccontent[tempnumber]-1)+temptopic]=tempwordtopic[topicnumber*(doccontent[tempnumber]-1)+temptopic]+1;
				set tempalldoctopic[temptopic]=tempalldoctopic[temptopic]+1;
				set sql='insert into '||tempouttable1||' values ( '||tempiteration||','||rowindex||','||doccontent[tempnumber]||','||temptopic||')';
				execute immediate  sql;
				set tempnumber=tempnumber+1;
			end while;
			set j=1;
			while j<=topicnumber do
				set sql='insert into '||tempouttable2||' values ( '||tempiteration||','||rowindex||','||j||','||singledoc[j]||')';
				execute immediate  sql;
				set j=j+1;
			end while;
			set docwordtopic=null;
			set singledoc=null;
			set tempnumber=1;
			set rowindex=gdatarow.docid;
			set doccontent=null;
		LEAVE fetch_loop3;
    		END IF;
		if gdatarow.docid!=rowindex
		then

		set sql = 'select alpinepldadocid as alpineid,alpinepldatopicid ,alpinepldacount from '|| tempouttable2 ||' where alpinepldagenera='||(tempiteration-1)||' and alpinepldadocid= '||rowindex||'  order by alpineid';
		PREPARE SQLSA1 FROM sql ;
		OPEN  curs4 ;
		set k=1;
		while k<=topicnumber do
		set k=k+1;
		fetch curs4 into topicrow;
		set lastgeneinfo[topicrow.topicid]=topicrow.contents;
		end while;
		close curs4;
		set k=0;
		set j = 1;
		set tempnumber=1;
		while tempnumber <=topicnumber do
			set 	singledoc[tempnumber]=0;
			set tempnumber=tempnumber+1;
		end while;
		set tempnumber=1;
		while tempnumber <=CARDINALITY(doccontent) do
				set sumpeoso=0;
				set j=1;
				while j <=topicnumber do
				set peoso[j]=(lastgeneinfo[j]+alpha)*(wordtopic[topicnumber*(doccontent[tempnumber]-1)+j]+beta)/(alldoctopic[j]+wordnumber*beta);
					set sumpeoso=sumpeoso+peoso[j];
					set j=j+1;
				end while;
				set j=1;
				while  j <=topicnumber do
					set peoso[j]=peoso[j]/sumpeoso;
					set j=j+1;
				end while;
				set temptopic= Alpine_PLDA_random(peoso);
				set singledoc[temptopic]=singledoc[temptopic]+1;
				set lastwordtopic[tempnumber]=temptopic;
				set tempwordtopic[topicnumber*(doccontent[tempnumber]-1)+temptopic]=tempwordtopic[topicnumber*(doccontent[tempnumber]-1)+temptopic]+1;
				set tempalldoctopic[temptopic]=tempalldoctopic[temptopic]+1;
				set sql='insert into '||tempouttable1||' values ( '||tempiteration||','||rowindex||','||doccontent[tempnumber]||','||temptopic||')';
				execute immediate  sql;
				set tempnumber=tempnumber+1;
			end while;
			set j=1;
			while j<=topicnumber do
				set sql='insert into '||tempouttable2||' values ( '||tempiteration||','||rowindex||','||j||','||singledoc[j]||')';
				execute immediate  sql;
				set j=j+1;
			end while;
			set docwordtopic=null;
			set singledoc=null;
			set tempnumber=1;
			set rowindex=gdatarow.docid;
			set doccontent=null;
			
		end if;
		set i=gdatarow.docid;
		set doccontent[tempnumber]=gdatarow.contents;
		set lastgeneinfo[tempnumber]=gdatarow.lastgeinfo;
		set tempnumber=tempnumber+1;
		set k=k+1;
	END LOOP;
	CLOSE curs3 ;
	set wordtopic=tempwordtopic;
	set alldoctopic=tempalldoctopic;
	set tempiteration=tempiteration+1;
	end while;
	set sql='create table '||outtable||' as (select '||docidcolumn||'  , '|| doccontentcolumn||' ,  alpine_plda_topic   from '|| tempouttable1||' where alpinepldagenera = '||iteration_number||')  definition only';
	execute immediate  sql;
	set sql=' insert into '||outtable||'   (select '||docidcolumn||'  , '|| doccontentcolumn||' ,  alpine_plda_topic  from '|| tempouttable1||' where alpinepldagenera = '||iteration_number||')  ';
	execute immediate  sql;
	
	set sql='create table '||doctopictable||' as (select alpinepldadocid  , alpinepldatopicid ,  alpinepldacount   from '|| tempouttable2||' where alpinepldagenera = '||iteration_number||')  definition only';
	execute immediate  sql;
	set sql=' insert into '||doctopictable||'   (select alpinepldadocid  , alpinepldatopicid ,  alpinepldacount   from '|| tempouttable2||' where alpinepldagenera = '||iteration_number||')  ';
	execute immediate  sql;

	
	set sql='create table '||topicouttable||' as (';
	set execStr=' select '|| doccontentcolumn||' ' ;
	set j=1;
	while j<=topicnumber do
		set execStr=execStr||', sum(case when alpine_plda_topic = '||to_char(j)||' then 1 else 0 end )  topic'||to_char(j);
		set j=j+1;
	end while;
	set execStr=execStr||'  from '||outtable||' group by  '|| doccontentcolumn||'  ';
	set sql=sql||execStr||' )  definition only';
	execute immediate  sql;
	set sql=' insert into '||topicouttable||' (';
	set sql=sql||execStr||') ';
	execute immediate  sql;
RETURN 1;

END@





CREATE OR REPLACE PROCEDURE  alpine_miner_plda_predict( 
	modeltable varchar(4000),
	content_table varchar(4000) , 
	docidcolumn varchar(4000), 
	doccontentcolumn varchar(4000), 
	alpha float , 
	beta float , 
	topicnumber float , 
	iteration_number integer , 
	dictable varchar(4000),
	diccontentcolumn varchar(4000),
	dicidcolumn varchar(4000),
	docouttable varchar(4000),
	docouttable1 varchar(4000))
 LANGUAGE SQL
begin
declare SQLCODE int;
DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
DECLARE	i float;
DECLARE	j float;
DECLARE	k float;
DECLARE wordnumber float;
DECLARE	temptopic float;
DECLARE	tempnumber float;
DECLARE	mutibynumber float;
DECLARE	tempiteration float;
DECLARE	sql clob;
DECLARE	tempstring varchar(4000);
DECLARE	singledoc  floatarray;--word counts in every topic in a doc
DECLARE	docwordtopic floatarray;
DECLARE	peoso   floatarray;--peoso
DECLARE	wordtopic floatarray;--each word counts in every topic
DECLARE	alldoctopic  floatarray;--counts for each topic
DECLARE	tempwordtopic floatarray;
DECLARE	tempalldoctopic  floatarray;
DECLARE	lastgeneinfo floatarray;
DECLARE	traininfo floatarray;
DECLARE	lastwordtopic floatarray;
DECLARE	doccontent floatarray;
DECLARE	diccontent varchararray;
DECLARE	docwordcontent varchararray;
DECLARE	sumpeoso float;
declare rowindex float;
declare TOTALROWINDEX float;
declare datarow pldadatarow;
declare topicrow pldatopicrow;
declare gdatarow gpldadatarow;
declare gtopicro gpldatopicrow;
declare dicdatarow pldadicrow;
DECLARE SQLSA VARCHAR(4000);
Declare execStr varchar(4000);
DECLARE at_end SMALLINT DEFAULT 0;
DECLARE not_found CONDITION for SQLSTATE '02000';
DECLARE curs3  CURSOR  WITH RETURN FOR SQLSA ;
DECLARE CONTINUE HANDLER for not_found 
SET at_end = 1;

	set	sql= 'select  count(*)  from '||dictable||' ';
	PREPARE SQLSA FROM sql ;
		OPEN  curs3 ;
	FETCH FROM curs3 INTO wordnumber;
	CLOSE curs3;
	set tempnumber=1;
	while tempnumber<=topicnumber do
		set alldoctopic[tempnumber]=0;
		set tempnumber=tempnumber+1;
	end while;
	set tempnumber=1;
	while tempnumber <=wordnumber*topicnumber do	
		set wordtopic[tempnumber]=0;
		set tempnumber=tempnumber+1;
	end while;
	set sql= 'select '||dicidcolumn||', '||diccontentcolumn||' as content from '||dictable||' ';
	PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
	fetch_loop2:
	LOOP	
		fetch curs3 into dicdatarow;
		set diccontent[dicdatarow.docid]=dicdatarow.contents;
	IF at_end <> 0 THEN LEAVE fetch_loop2;
    		END IF;
	end loop;
	CLOSE curs3;

	set sql= 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent, alpine_plda_topic from '|| modeltable ||' ';
	set at_end=0;
	PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
	fetch_loop3:
	LOOP
	fetch curs3 into gdatarow;
	IF at_end <> 0 THEN LEAVE fetch_loop3;
    		END IF;
		set alldoctopic[gdatarow.lastgeinfo]=alldoctopic[gdatarow.lastgeinfo]+1;
		 set wordtopic[topicnumber*(gdatarow.contents-1)+gdatarow.lastgeinfo]=wordtopic[topicnumber*(gdatarow.contents-1)+gdatarow.lastgeinfo]+1;
	end loop;
	CLOSE curs3;
	set sql= 'CREATE  TABLE '||docouttable||' ( '||docidcolumn||' bigint , '|| doccontentcolumn||' varchar(100),  alpine_plda_topic  int)';
	execute immediate sql;



	set k=1;
	set sql = 'select distinct('||docidcolumn||') as alpineid from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid Fetch First 1 rows only';
	PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
	fetch curs3 into totalrowindex;
	CLOSE curs3;
	set rowindex=totalrowindex;
	set sql = 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid';
	set at_end=0;
	PREPARE SQLSA FROM sql ;
	OPEN  curs3 ;
	fetch_loop4:
	LOOP
	fetch curs3 into datarow;
	IF at_end <> 0 THEN

	set tempnumber=1;
	while tempnumber <=topicnumber do
		set lastgeneinfo[tempnumber]=0;
		set tempnumber=tempnumber+1;
	end while;
	set tempnumber=1;
	while tempnumber <=k do
		set docwordtopic[tempnumber]=0;
		set tempnumber=tempnumber+1;
	end while;

	set tempiteration=1;
	while tempiteration <= iteration_number do
		set tempnumber=1;
		while tempnumber <=topicnumber do
			set singledoc[tempnumber]=0;
			set tempnumber=tempnumber+1;
		end while;
		set tempnumber=1;
		while tempnumber <=(k-1) do
			set sumpeoso=0;
			set j=1;
			while j <=topicnumber do
				set peoso[j]=(lastgeneinfo[j]+alpha)*(wordtopic[topicnumber*(doccontent[tempnumber]-1)+j]+beta)/(alldoctopic[j]+wordnumber*beta);
				set sumpeoso=sumpeoso+peoso[j];
				set j=j+1;
			end while;
			set j=1;
			while j <=topicnumber do
				set peoso[j]=peoso[j]/sumpeoso;
				set j=j+1;
			end while;
			set temptopic=Alpine_PLDA_random(peoso);
			set singledoc[temptopic]=singledoc[temptopic]+1;
			set docwordtopic[tempnumber]=temptopic;
			set tempnumber=tempnumber+1;
		end while;
		set tempiteration=tempiteration+1;
	end while;		
	set docwordcontent= Alpine_PLDA_transfor(doccontent,diccontent);
	set j=1;

	while j<=(k-1) do
		set lastgeneinfo[j]=docwordtopic[j];			
		set sql='insert into '||docouttable||' values ( '||to_char(i)||','''||docwordcontent[j]||''','||to_char(docwordtopic[j])||')';
		execute immediate sql;
		set j=j+1;
	end while;
	LEAVE fetch_loop4;
    	END IF;

	if datarow.docid!=rowindex
	then

		set tempnumber=1;
		while tempnumber <=topicnumber do
			set lastgeneinfo[tempnumber]=0;
			set tempnumber=tempnumber+1;
		end while;

		set tempnumber=1;
		while tempnumber <=k do
			set docwordtopic[tempnumber]=0;
			set tempnumber=tempnumber+1;
		end while;

		set tempiteration=1;
		while tempiteration <= iteration_number do
			set tempnumber=1;
			while tempnumber <=topicnumber do
				set singledoc[tempnumber]=0;
				set tempnumber=tempnumber+1;
			end while;
			set tempnumber=1;
			while tempnumber <=(k-1) do
				set sumpeoso=0;
				set j=1;
				while j <=topicnumber do
					set peoso[j]=(lastgeneinfo[j]+alpha)*(wordtopic[topicnumber*(doccontent[tempnumber]-1)+j]+beta)/(alldoctopic[j]+wordnumber*beta);
					set sumpeoso=sumpeoso+peoso[j];
					set j=j+1;
				end while;
				set j=1;
				while j <=topicnumber do
					set peoso[j]=peoso[j]/sumpeoso;
					set j=j+1;
				end while;
				set temptopic=Alpine_PLDA_random(peoso);
				set singledoc[temptopic]=singledoc[temptopic]+1;
				set docwordtopic[tempnumber]=temptopic;
				set tempnumber=tempnumber+1;
			end while;

			set tempiteration=tempiteration+1;
		end while;

		set docwordcontent= Alpine_PLDA_transfor(doccontent,diccontent);
		set j=1;

		while j<=(k-1) do

			set lastgeneinfo[j]=docwordtopic[j];
			set sql='insert into '||docouttable||' values ( '||to_char(i)||','''||docwordcontent[j]||''','||to_char(docwordtopic[j])||')';
			execute immediate sql;
			set j=j+1;
		end while;

		set docwordtopic=null;
		set rowindex=datarow.docid;
		set k=1;
	end if;
	call dbms_output.put_line(' doc continue');
	set i=datarow.docid;
	set doccontent[k]=datarow.contents;
	set k=k+1;
	end loop;

	set sql='create table '||docouttable1||' as (';
	set execStr=' select '|| doccontentcolumn||' ' ;
	set j=1;
	while j<=topicnumber do
		set execStr=execStr||', sum(case when alpine_plda_topic = '||to_char(j)||' then 1 else 0 end )  topic'||to_char(j);
		set j=j+1;
	end while;
	set execStr=execStr||'  from '||docouttable||' group by  '|| doccontentcolumn||'  ';
	set sql=sql||execStr||' )  definition only';
	execute immediate  sql;
	set sql=' insert into '||docouttable1||' (';
	set sql=sql||execStr||') ';
	execute immediate  sql;
RETURN 1;

END@


