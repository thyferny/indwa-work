-- Function: alpine_miner_initpca(text, text, text[], text, text)

-- DROP FUNCTION alpine_miner_initpca(text, text, text[], text, text);

CREATE OR REPLACE FUNCTION alpine_miner_initpca(tablename text, valueouttable text, infor text[], createway text, dropifexists text, append_only_string TEXT, ending_string TEXT)
  RETURNS double precision[] AS
$BODY$
DECLARE 
	columnnumber float;
	i integer;
	j integer;
	rownumber float;
	alpine_pcadataindex integer;
	temp float;
	sql text;
	sql1 text;
	sqlnotnull text;
	total integer;
	result  double precision[];
	vari double precision[];
BEGIN

	columnnumber:= alpine_miner_get_array_count(infor);
	execute 'select count(*) from '||tablename into rownumber;
	total:=columnnumber*(columnnumber+1)/2;

	IF dropIfExists='yes'
	THEN execute 'Drop table IF EXISTS '||valueouttable;
	END IF;
	sqlnotnull:=' where  '||infor[1]||' is not null';
	sql:= ' '||infor[1]||' ';
	sql1:=sql||' double precision';
	for i in 2..columnnumber loop
		sql:=sql||', '||infor[i]||'';
		sqlnotnull:=sqlnotnull||' and '||infor[i]||' is not null ';
		sql1:=sql1||', '||infor[i]||' double precision';
	end loop;
	
	
	execute 'create table '||valueouttable||' ('||sql1||',"alpine_pcadataindex" integer,"alpine_pcaevalue" float,"alpine_pcacumvl" float,"alpine_pcatotalcumvl" float)  '||append_only_string|| ' ' ||ending_string;


	
		
		
	IF createway='cov-pop' 
	THEN 
	execute  'select  alpine_miner_covar(array['||sql||'])  from  '||tablename||' '||sqlnotnull  into result;
	
	ELSE IF createway='cov-sam'
	THEN 
		execute  'select  alpine_miner_covar_sam(array['||sql||'])  from  '||tablename||' '||sqlnotnull into result;
	ELSE IF createway='corr'
	THEN 
		execute  'select  alpine_miner_corr(array['||sql||'])  from  '||tablename||' '||sqlnotnull into result;
	END IF;
	END IF;
	END IF;	
	
RETURN result;

END;

 $BODY$
  LANGUAGE plpgsql VOLATILE;

-- Function: alpine_miner_pcaresult(text, text[], text, text[], text, integer, text)

-- DROP FUNCTION alpine_miner_pcaresult(text, text[], text, text[], text, integer, text);

CREATE OR REPLACE FUNCTION alpine_miner_pcaresult(tablename text, infor text[], outtablename text, remaincolumns text[], outvaluetable text, pcanumber integer, dropifexists text, append_only_string TEXT, ending_string TEXT)
  RETURNS double precision[] AS
$BODY$
DECLARE 
	columnnumber integer;
	i integer;
	j integer;
	temp float;
	wrongnumber float;
	err float;
	remconames text;
	maxerror float;
	tempqvalue float;
	c float :=0;
	sumsql text;
	sumarrayname  text;
	notnulltext text;
	valuesarray float[];
	arraytext text;
	valuestext text;
	temparray float[];
	totalsql text;
	valuesql text;
	tempstring text;
	result  double precision[];
	tempnumber float;
	temprecord float[];
	remainnumber int;
BEGIN

	columnnumber:= alpine_miner_get_array_count(infor);
	if remaincolumns  is not null
	then 	remainnumber:= alpine_miner_get_array_count(remaincolumns);
		remconames:=' , '||array_to_string(remaincolumns,',');
	else  remconames:=' ';
	end if;
	IF dropIfExists='yes'
	THEN execute 'Drop table IF EXISTS '||outtablename||' ';
	END IF;
	
	i        := 1;
	sumsql:=' ';
	sumarrayname:=array_to_string(infor,',');
	while i <= pcanumber loop
			execute 'select array[' || sumarrayname|| '] from ' || outvaluetable ||
             ' where  "alpine_pcadataindex"=' ||
             (i - 1)   into temparray;
                      IF i>1
              THEN valuesarray:=array_cat(valuesarray,temparray);
		arraytext:=arraytext||' , arr['||i||'] alpine_pcaattr'||i;
             ELSE 
             valuesarray:=temparray;
            			arraytext:='arr[1] alpine_pcaattr1 ';
              END IF;
              i:=i+1;
	end loop;

	notnulltext:=array_to_string(infor,' is not null and ');
	
	valuestext:=array_to_string(valuesarray,',');
	execute 'create table ' || outtablename || ' ' ||append_only_string||' as select '|| arraytext ||'  '||remconames||' from (select alpine_miner_pcaresult(array['||sumarrayname||'],array['||valuestext||']) arr '||remconames||' from 	' || tablename ||' where '||notnulltext||' is not null ) AS foo '||ending_string;
  
 
	
RETURN result;

END;

 $BODY$
  LANGUAGE plpgsql VOLATILE;

