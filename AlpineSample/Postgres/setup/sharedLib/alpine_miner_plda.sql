drop type IF EXISTS plda_assign_topics cascade;
CREATE   TYPE plda_assign_topics AS (
       assign integer[],
       topic_count integer[]
);


CREATE OR REPLACE FUNCTION alpine_plda_gene(columnarray integer[], glassign integer[],wordtopic integer[],
lastassign integer[],lasttopic integer[],alpha double precision,beta double precision , wordnumber integer,topicnumber integer)
RETURNS plda_assign_topics
AS 'alpine_miner','alpine_plda_gene'
LANGUAGE C
IMMUTABLE STRICT;


CREATE OR REPLACE FUNCTION alpine_plda_first(x integer, y integer)
RETURNS plda_assign_topics
AS 'alpine_miner','alpine_plda_first'
LANGUAGE C
IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_plda_word_topic(IN arr integer[], IN topicnumber integer, IN wordnumber integer, OUT ret integer[])
  RETURNS integer[] AS
$BODY$
       SELECT $1[(($3-1)*$2 + 1):(($3-1)*$2 + $2)];
$BODY$
  LANGUAGE sql VOLATILE;

CREATE OR REPLACE FUNCTION alpine_plda_count_accum(state integer[],data integer[], x integer[],y integer,z integer)
RETURNS integer[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;



DROP AGGREGATE IF EXISTS alpine_plda_count(  integer[],   integer[],  integer,  integer);
CREATE AGGREGATE alpine_plda_count(  integer[],   integer[],  integer,  integer) (
    SFUNC=alpine_plda_count_accum,
    STYPE=integer[],
     INITCOND='{0}'
);


CREATE OR REPLACE FUNCTION alpine_miner_plda_train(content_table text, docidcolumn text, doccontentcolumn text, alpha double precision, beta double precision, topicnumber integer, dictable text, diccontentcolumn text, iteration_number integer, tempouttable text, tempouttable1 text, outtable text, doctopictable text, topicouttable text)
  RETURNS double precision AS
$BODY$
DECLARE 
	geneinputtable text;
	geneoutputtable text;
	tempiteration integer;
	sql text;
	wordtopic integer[];--each word counts in every topic
	alldoctopic  integer[];--counts for each topic
	myrecord record;
	diccontent text[];
	wordnumber integer;
BEGIN
	
	sql:= 'select  '||diccontentcolumn||' as content from '||dictable||' ';
	for myrecord in execute sql loop
		diccontent:=myrecord.content;
	end loop;
	wordnumber:=array_upper(diccontent,1);
	execute 'CREATE temp TABLE '||tempouttable||' ( alpinepldagenera integer , '||docidcolumn||' integer , '|| doccontentcolumn||' integer[],  alpinepldainfo plda_assign_topics)  ';
	execute 'CREATE temp TABLE '||tempouttable1||' ( alpinepldagenera integer , '||docidcolumn||' integer , '|| doccontentcolumn||' integer[], alpinepldainfo plda_assign_topics )  ';
	sql:= 'insert into '||tempouttable||' ( select 1  , '||docidcolumn||' , '|| doccontentcolumn||' ,
	alpine_plda_first( array_upper('|| doccontentcolumn||',1),'||topicnumber||') as alpinepldainfo from '||content_table||' ) ';
	execute sql;
	sql := 'select sum((alpinepldainfo).topic_count) as alldoctopic,alpine_plda_count('|| doccontentcolumn||',(alpinepldainfo).assign,'||topicnumber||','||wordnumber||') as wordtopic from '|| tempouttable||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null ';
		for myrecord in execute sql loop
			alldoctopic:=myrecord.alldoctopic;
			wordtopic:=myrecord.wordtopic;
		end loop;
		geneoutputtable:=tempouttable;
		geneinputtable:=tempouttable1;
	for tempiteration in 2.. iteration_number loop
		if mod(tempiteration,2) = 0
		then 
			geneoutputtable:=tempouttable1;
			geneinputtable:=tempouttable;
		else 
			geneoutputtable:=tempouttable;
			geneinputtable:=tempouttable1;
		end if;
		execute ' TRUNCATE TABLE '||geneoutputtable;
		execute 'insert into '||geneoutputtable||' ( select '||tempiteration||'  , '||docidcolumn||' , 
		'|| doccontentcolumn||' ,	alpine_plda_gene( '|| doccontentcolumn||',array['||array_to_string(alldoctopic,',')||'],array['||array_to_string(wordtopic,',')||'],(alpinepldainfo).assign,(alpinepldainfo).topic_count,'||alpha||','||beta||',
		'||wordnumber||','||topicnumber||') as alpinepldainfo from '||geneinputtable||') ';
		
		sql := 'select sum((alpinepldainfo).topic_count) as alldoctopic,alpine_plda_count('|| doccontentcolumn||',(alpinepldainfo).assign,'||topicnumber||','||wordnumber||') as wordtopic from '|| geneoutputtable||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null ';
		for myrecord in execute sql loop
			alldoctopic:=myrecord.alldoctopic;
			wordtopic:=myrecord.wordtopic;
		end loop;
	end loop;
		
	execute 'create table '||outtable||' as select '||docidcolumn||'  , '|| doccontentcolumn||' ,   (alpinepldainfo).assign  from '|| geneoutputtable||' where alpinepldagenera = '||iteration_number||'  ';
	execute 'create table '||doctopictable||' as select '||docidcolumn||'  , '|| doccontentcolumn||' ,  (alpinepldainfo).topic_count   from '|| geneoutputtable||' where alpinepldagenera = '||iteration_number||'  ';

	sql:='CREATE  TABLE '||topicouttable||'  as  select diccontent[ss.i], alpine_plda_word_topic(array['||array_to_string(wordtopic,',')||'],'||topicnumber||',ss.i) 
		from  (select '||diccontentcolumn||' as diccontent from '||dictable||' limit 1   ) as foo,  (select generate_series(1,'||wordnumber||') i) as ss   ';
	execute sql;
RETURN 1;

END;

 $BODY$
  LANGUAGE plpgsql VOLATILE;
  
CREATE OR REPLACE FUNCTION alpine_plda_predict(doc integer[], gtopic_count integer[], wordtopic integer[], topicnumber integer, wordnumber integer,
             alpha float, beta float,iteraternumber integer)
RETURNS plda_assign_topics AS $$
DECLARE
    result plda_assign_topics;
BEGIN
    result := alpine_plda_first(array_upper(doc,1), topicnumber);
    FOR i in 1..iteraternumber LOOP
        result := alpine_plda_gene(doc,gtopic_count,wordtopic,(result).assign,(result).topic_count,alpha,beta,wordnumber,topicnumber);
        END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION  alpine_miner_plda_predict( 
	modeltable text,
	content_table text , 
	docidcolumn text, 
	doccontentcolumn text, 
	alpha float , 
	beta float , 
	topicnumber integer , 
	iteration_number integer , 
	dictable text,
	diccontentcolumn text,
	temptable text,
	docouttable text,
	doctopictable text)
  RETURNS double precision AS
$BODY$
DECLARE 
	i integer;
	j integer;
	k integer;
	wordnumber integer;
	tempnumber integer:=0;
	tempiteration integer;
	sql text;
	wordtopic integer[];--each word counts in every topic
	alldoctopic  integer[];--counts for each topic
	myrecord record;
	doccontent integer[];
	diccontent text[];
	docwordcontent text[];

	
BEGIN

	
	for tempnumber in 1..topicnumber loop

		alldoctopic[tempnumber]:=0;
	end loop;
	
	
	execute 'select  array_upper('||diccontentcolumn||',1) as wordnumber from '||dictable||' limit 1 ' into wordnumber;
	sql := 'select alpine_plda_count('|| doccontentcolumn||',assign,'||topicnumber||','||wordnumber||') as wordtopic from '|| modeltable||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null ';
	for myrecord in execute sql loop
		wordtopic:=myrecord.wordtopic;
	end loop;
	i:=array_upper(wordtopic,1);
	for tempnumber in 1..i loop
		alldoctopic[mod((tempnumber-1),topicnumber::integer)+1]:=alldoctopic[mod(tempnumber-1,topicnumber::integer)+1]+wordtopic[tempnumber];
	end loop;
	

	
	
	execute 'create temp table '||temptable ||' as select '||docidcolumn||' ,'|| doccontentcolumn||',alpine_plda_predict('||doccontentcolumn||',array['||array_to_string(alldoctopic,',')||']
		,array['||array_to_string(wordtopic,',')||'],'|| topicnumber||','||wordnumber||','||alpha||','||beta||','||iteration_number||') as alpinepldainfo  from '||content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null   ';
	

	execute 'CREATE  TABLE '||docouttable||' as select  '||docidcolumn||'  , '|| doccontentcolumn||' ,  (alpinepldainfo).assign as alpinepldaassign  from '||temptable||'   ';
	execute   ' create table '||doctopictable|| '  as select '||docidcolumn||'  , '|| doccontentcolumn||' ,  (alpinepldainfo).topic_count as alpinepldatopic  from  '||temptable||'   ';





RETURN 1;

END;

 $BODY$
  LANGUAGE plpgsql VOLATILE;
  
