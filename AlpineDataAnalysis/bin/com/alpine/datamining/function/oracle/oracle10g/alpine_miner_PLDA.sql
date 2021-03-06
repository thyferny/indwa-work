create or replace function alpine_array_count(dataarray integerarray)
return integer is
length integer;
begin
      length:=dataarray.count();
return length;
end alpine_array_count;


/



create or replace TYPE plda_assign_topics AS object(
       assign IntegerArray,
       topic_count IntegerArray
);
/
create or replace TYPE plda_sum AS object(
       assign IntegerArray,
       topic_count IntegerArray,
       content IntegerArray,
       wordnumber integer,
       topicnumber integer,
       length     integer
);

/

create or replace type pldaSumImpl as object
(
  varraysum plda_sum,
  static function ODCIAggregateInitialize(fs IN OUT pldaSumImpl) 
    return number,
  member function ODCIAggregateIterate(self IN OUT pldaSumImpl, 
    value IN plda_sum ) return number,
  member function ODCIAggregateTerminate(self IN pldaSumImpl, 
    returnValue OUT plda_sum, flags IN number) return number,
  member function ODCIAggregateMerge(self IN OUT pldaSumImpl, 
    fs2 IN pldaSumImpl) return number
);
/ 
	  
create or replace type body pldaSumImpl is
static function ODCIAggregateInitialize(fs IN OUT pldaSumImpl)
return number is
arraysum plda_sum := plda_sum(integerarray(),integerarray(),integerarray(),0,0,0);
begin
  fs := pldaSumImpl(plda_sum(integerarray(),integerarray(),integerarray(),0,0,0));

  fs.varraysum := (arraysum);
  return ODCIConst.Success;
end;

member function ODCIAggregateIterate(self IN OUT pldaSumImpl,
    value IN plda_sum ) return number is
i integer := 0;
begin

  if self.varraysum.assign.count() = 0 then
    self.varraysum.assign.extend();
    self.varraysum.assign(1):=0;
    self.varraysum.assign.extend((value.wordnumber*value.topicnumber-1),1);
    self.varraysum.topic_count.extend();
    self.varraysum.topic_count(1):=0;
    self.varraysum.topic_count.extend((value.topicnumber-1),1);
  end if;
  for i in 1..value.content.count() loop
    self.varraysum.assign((value.content(i)-1)*value.topicnumber+value.assign(i)):=
    self.varraysum.assign((value.content(i)-1)*value.topicnumber+value.assign(i))+1;
    self.varraysum.topic_count(value.assign(i)):=self.varraysum.topic_count(value.assign(i))+1;
  end loop;
  return ODCIConst.Success;
end;

member function ODCIAggregateTerminate(self IN pldaSumImpl,
    returnValue OUT plda_sum, flags IN number)
return number is
begin
  returnValue := self.varraysum;
  return ODCIConst.Success;
end;

member function ODCIAggregateMerge(self IN OUT pldaSumImpl, fs2 IN pldaSumImpl)
return number is
i integer :=0;
mincount integer := 0;
begin
   if self.varraysum.assign.count() < fs2.varraysum.assign.count() then

    for i in 1..self.varraysum.assign.count() loop
      self.varraysum.assign(i) := self.varraysum.assign(i) + fs2.varraysum.assign(i);
    end loop;
    for i in 1..self.varraysum.topic_count.count() loop
      self.varraysum.topic_count(i) := self.varraysum.topic_count(i) + fs2.varraysum.topic_count(i);
    end loop;
     for i in (self.varraysum.assign.count() + 1) .. fs2.varraysum.assign.count() loop
         self.varraysum.assign.extend();
        self.varraysum.assign(i) :=   fs2.varraysum.assign(i);
    end loop;
      for i in (self.varraysum.topic_count.count() + 1) .. fs2.varraysum.topic_count.count() loop
         self.varraysum.topic_count.extend();
        self.varraysum.topic_count(i) :=   fs2.varraysum.topic_count(i);
    end loop;
   
    else 
     for i in 1..self.varraysum.assign.count() loop
      self.varraysum.assign(i) := self.varraysum.assign(i) + fs2.varraysum.assign(i);
    end loop;
    for i in 1..self.varraysum.topic_count.count() loop
      self.varraysum.topic_count(i) := self.varraysum.topic_count(i) + fs2.varraysum.topic_count(i);
    end loop;
end if;
  return ODCIConst.Success;
end;
 end ;

/

CREATE OR REPLACE TYPE AlpinePldaTableType
  AS TABLE OF integer;


/

  create or replace function generate_series(p_start in pls_integer,
                           p_end   in pls_integer,
                           p_step  in pls_integer := 1 )
       Return AlpinePldaTableType  Pipelined
  As
    v_i    integer := CASE WHEN p_start IS NULL THEN 1 ELSE p_start END;
    v_step integer := CASE WHEN p_step IS NULL OR p_step = 0 THEN 1 ELSE p_step END;
    v_terminating_value PLS_INTEGER :=  p_start + TRUNC(ABS(p_start-p_end) / abs(v_step) ) * v_step;
  Begin
     -- Check for impossible combinations
     If ( p_start > p_end AND SIGN(p_step) = 1 )
        Or
        ( p_start < p_end AND SIGN(p_step) = -1 ) Then
       Return;
     End If;
     -- Generate integers
     LOOP
       PIPE ROW ( v_i );
       EXIT WHEN ( v_i = v_terminating_value );
       v_i := v_i + v_step;
     End Loop;
     Return;
  End generate_series;

  /

 
 
 CREATE OR REPLACE FUNCTION alpine_plda_word_topic( arr integerarray, topicnumber integer,   wordnumber integer  )
  RETURN  integerarray is
  i integer; 
  resultarray integerarray:= integerarray();
 begin
      for i in 1..topicnumber loop
          resultarray.extend();
          resultarray(i):=arr((wordnumber-1)*topicnumber + i);
          end loop; 

       return resultarray;
end alpine_plda_word_topic;

/


create or replace
FUNCTION pldaSum (input plda_sum ) RETURN plda_sum 
 PARALLEL_ENABLE  AGGREGATE USING pldaSumImpl;
/
 
 

CREATE OR REPLACE FUNCTION alpine_miner_plda_first(columnsize integer,topicnumber integer)
  RETURN plda_assign_topics is
  tempnumber  integer;
  randomdata  integer;
  result plda_assign_topics := plda_assign_topics(IntegerArray(),IntegerArray());
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  commit;
   for tempnumber in 1..topicnumber loop
       result.topic_count.extend();
       result.topic_count(tempnumber):=0;
   end loop;


 for tempnumber in 1..columnsize loop
    result.assign.extend();
 
    randomdata:=trunc(dbms_random.value(1, (topicnumber+1)));
    result.assign(tempnumber):=randomdata;
    result.topic_count(randomdata):=result.topic_count(randomdata)+1;
  end loop;
  commit;
  RETURN(result);
END alpine_miner_plda_first;
/
CREATE OR REPLACE FUNCTION alpine_miner_plda_gene(doccontent IntegerArray,
																									alldoctopic IntegerArray,
																									wordtopic IntegerArray,
																									assign    IntegerArray,
																									topiccount IntegerArray,
																									alpha     binary_double,
																									beta      binary_double,
																									wordnumber integer,
																									topicnumber integer  )
  RETURN plda_assign_topics is
  tempnumber  integer;
 
  column_size integer;
  temptopic		integer;
  singledoctopic integer;
  temp_glwordtopic integer;
  k						integer;
  total_unpr binary_double;
  cl_prob   binary_double;
  j          integer;
	ret					integer;
	r				binary_double;
 
	topic_prs floatarray:=floatarray();
  result plda_assign_topics := plda_assign_topics(IntegerArray(),IntegerArray());
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN


   for tempnumber in 1..topicnumber loop
       result.topic_count.extend();
       result.topic_count(tempnumber):=0;
   end loop;
   column_size:=doccontent.count();
		for   k in 1.. column_size	loop
     total_unpr := 0;
     for j in 1.. topicnumber loop

		temp_glwordtopic :=  wordtopic((doccontent(k)-1) * topicnumber + j);
		singledoctopic := topiccount(j);
		if  j =   assign(k)
		then
		 temp_glwordtopic:=temp_glwordtopic-1;
		 singledoctopic:=singledoctopic-1;
		end if;
	  cl_prob := (singledoctopic + alpha) * (temp_glwordtopic + beta) /
			  (alldoctopic(j) + wordnumber * beta);
		total_unpr := total_unpr+cl_prob;
    topic_prs.extend();
		topic_prs(j) := total_unpr;
	end loop;
	for j in 1..topicnumber loop
		topic_prs(j) := topic_prs(j) / total_unpr;
	end loop;
	    r:= dbms_random.value(0,1);
   
	ret := 1;
  	while r > topic_prs(ret) and ret<topicnumber  loop
		      ret:=ret+1;
    end loop;
    temptopic:=ret;
    result.assign.extend();
		result.assign(k):=temptopic;
    result.topic_count(temptopic):=result.topic_count(temptopic)+1;
		end loop;
 
  RETURN(result);
END alpine_miner_plda_gene;


/

CREATE OR REPLACE FUNCTION alpine_miner_plda_train(content_table    varchar2,
                                                   docidcolumn      varchar2,
                                                   doccontentcolumn varchar2,
                                                   alpha            binary_double,
                                                   beta             binary_double,
                                                   topicnumber      integer,
                                                   dictable         varchar2,
                                                   diccontentcolumn varchar2,
                                                   iteration_number integer,
                                                   tempouttable     varchar2,
                                                   tempouttable1    varchar2,
                                                   outtable         varchar2,
                                                   doctopictable    varchar2,
                                                   topicouttable    varchar2)
  RETURN integer is
  tempiteration integer := 1;
  sqlan         clob;
  wordtopic     IntegerArray := IntegerArray();
  alldoctopic   IntegerArray := IntegerArray();
  temprecord    plda_sum := plda_sum(integerarray(),
                                     integerarray(),
                                     integerarray(),
                                     0,
                                     0,
                                     0);
  TYPE crt IS REF CURSOR;
  wordtopiclob    clob;
  alldoctopiclob  clob;
  executesql      varchar2(4000);
  myrecord        crt;
  diccontent      varchar2array := varchar2array();
  geneoutputtable varchar2(4000);
  geneinputtable  varchar2(4000);
  wordnumber      integer;
  PRAGMA AUTONOMOUS_TRANSACTION;
begin
  commit;
  sqlan := 'select  ' || diccontentcolumn || ' as content from ' ||
           dictable || ' ';
  open myrecord for to_char(sqlan);
  loop
    FETCH myrecord
      INTO diccontent;
    EXIT WHEN myrecord%NOTFOUND;
  end loop;
  wordnumber := diccontent.count();
  commit;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate 'create  table ' || tempouttable ||
                    ' parallel as ( select 1 as alpinepldagenera , ' ||
                    docidcolumn || ' , ' || doccontentcolumn ||
                    ' , alpine_miner_plda_first( alpine_array_count(' ||
                    doccontentcolumn || '),' || topicnumber ||
                    ') as alpinepldainfo from ' || content_table || ' ) ';
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate 'create  table ' || tempouttable1 ||
                    ' parallel as (select * from ' || tempouttable ||
                    ' where 1=0 )';
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  commit;
  sqlan := 'select pldaSum(plda_sum((alpinepldainfo).assign,(alpinepldainfo).topic_count,' ||
           doccontentcolumn || ',' || wordnumber || ',' || topicnumber ||
           ',alpine_array_count(' || doccontentcolumn ||
           '))) as wordtopic from ' || tempouttable || ' where ' ||
           docidcolumn || ' is not null and ' || doccontentcolumn ||
           ' is not null ';
  commit;
  open myrecord for to_char(sqlan);
  loop
    FETCH myrecord
      INTO temprecord;
    EXIT WHEN myrecord%NOTFOUND;
  end loop;
  alldoctopic.extend(wordnumber * topicnumber);
  wordtopic.extend(topicnumber);
  wordtopic       := temprecord.assign;
  alldoctopic     := temprecord.topic_count;
  geneoutputtable := tempouttable;
  geneinputtable  := tempouttable1;
  for tempiteration in 2 .. iteration_number loop
    if mod(tempiteration, 2) = 0 then
      geneoutputtable := tempouttable1;
      geneinputtable  := tempouttable;
    else
      geneoutputtable := tempouttable;
      geneinputtable  := tempouttable1;
    end if;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    commit;
    execute immediate ' TRUNCATE TABLE ' || geneoutputtable;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    commit;
    wordtopiclob   := alpine_iarray_to_clob(wordtopic, ',');
    alldoctopiclob := alpine_iarray_to_clob(alldoctopic, ',');
    sqlan          := ' ';
    dbms_lob.append(sqlan, 'insert into ');
    dbms_lob.append(sqlan, geneoutputtable);
    dbms_lob.append(sqlan, '    ( select ');
    dbms_lob.append(sqlan, to_char(tempiteration));
    dbms_lob.append(sqlan, ' as alpinepldagenera,');
    dbms_lob.append(sqlan, docidcolumn);
    dbms_lob.append(sqlan, ',');
    dbms_lob.append(sqlan, doccontentcolumn);
    dbms_lob.append(sqlan, ',alpine_miner_plda_gene(');
    dbms_lob.append(sqlan, doccontentcolumn);
    dbms_lob.append(sqlan, ',integerarray(');
    dbms_lob.append(sqlan, alldoctopiclob);
    dbms_lob.append(sqlan, '),integerarray(');
    dbms_lob.append(sqlan, wordtopiclob);
    dbms_lob.append(sqlan,
                    '),(alpinepldainfo).assign,(alpinepldainfo).topic_count,');
    dbms_lob.append(sqlan, to_char(alpha));
    dbms_lob.append(sqlan, ',');
    dbms_lob.append(sqlan, to_char(beta));
    dbms_lob.append(sqlan, ',');
    dbms_lob.append(sqlan, to_char(wordnumber));
    dbms_lob.append(sqlan, ',');
    dbms_lob.append(sqlan, to_char(topicnumber));
    dbms_lob.append(sqlan, ') as  alpinepldainfo  from ');
    dbms_lob.append(sqlan, geneinputtable);
    dbms_lob.append(sqlan, ') ');
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    execute immediate to_char(sqlan);
    commit;
    executesql := 'alter session disable parallel dml';
    execute immediate executesql;
    commit;
    executesql := 'alter session force parallel dml';
    execute immediate executesql;
    commit;
    sqlan := 'select pldaSum(plda_sum((alpinepldainfo).assign,(alpinepldainfo).topic_count,' ||
             doccontentcolumn || ',' || wordnumber || ',' || topicnumber ||
             ',alpine_array_count(' || doccontentcolumn ||
             '))) as wordtopic from ' || geneoutputtable || ' where ' ||
             docidcolumn || ' is not null and ' || doccontentcolumn ||
             ' is not null ';
    open myrecord for to_char(sqlan);
    loop
      FETCH myrecord
        INTO temprecord;
      EXIT WHEN myrecord%NOTFOUND;
    end loop;
    wordtopic   := temprecord.assign;
    alldoctopic := temprecord.topic_count;
    executesql  := 'alter session disable parallel dml';
    execute immediate executesql;
    commit;
  end loop;
  executesql := 'alter session force parallel dml';
  execute immediate executesql;
  execute immediate 'create table ' || outtable ||
                    '  parallel  as   select ' || docidcolumn || '  , ' ||
                    doccontentcolumn ||
                    ' ,   (alpinepldainfo).assign as assign from ' ||
                    geneoutputtable || ' where alpinepldagenera = ' ||
                    iteration_number || '  ';
  execute immediate 'create table ' || doctopictable ||
                    '  parallel  as  select ' || docidcolumn || '  , ' ||
                    doccontentcolumn ||
                    ' ,  (alpinepldainfo).topic_count as topic_count  from ' ||
                    geneoutputtable || ' where alpinepldagenera = ' ||
                    iteration_number || '  ';
  wordtopiclob := alpine_iarray_to_clob(wordtopic, ',');
  sqlan        := 'CREATE  TABLE ' || topicouttable ||
                  '  parallel  as  select alpine_miner_get_v2a_element(diccontent,i) word, alpine_plda_word_topic(integerarray(' ||
                  wordtopiclob || '),' || topicnumber ||
                  ', i) topic from  (select ' || diccontentcolumn ||
                  ' as diccontent from ' || dictable ||
                  ' where rownum=1   ) ,  (select column_value i  from Table(generate_series(1,' ||
                  wordnumber || ')))  ';
  execute immediate to_char(sqlan);
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  commit;
  RETURN 1;
END alpine_miner_plda_train;



/

CREATE OR REPLACE FUNCTION alpine_miner_plda_row(doc integerarray, gtopic_count integerarray, wordtopic integerarray, topicnumber integer, wordnumber integer, alpha binary_double, beta binary_double, iteraternumber integer)
  RETURN  plda_assign_topics is
 
    result plda_assign_topics;
BEGIN
    result := alpine_miner_plda_first(doc.count(), topicnumber);
    FOR i in 1..iteraternumber LOOP
        result := alpine_miner_plda_gene(doc,gtopic_count,wordtopic,result.assign, result.topic_count,alpha,beta,wordnumber,topicnumber);
        END LOOP;
    RETURN (result);
END alpine_miner_plda_row;


/




CREATE OR REPLACE FUNCTION alpine_miner_plda_predict(modeltable       varchar2,
                                                     content_table    varchar2,
                                                     docidcolumn      varchar2,
                                                     doccontentcolumn varchar2,
                                                     alpha            binary_double,
                                                     beta             binary_double,
                                                     topicnumber      binary_double,
                                                     iteration_number integer,
                                                     dictable         varchar2,
                                                     diccontentcolumn varchar2,
                                                     temptable        varchar2,
                                                     docouttable      varchar2,
                                                     doctopictable    varchar2)
  RETURN integer is
  i          binary_double;
  wordnumber binary_double;
  tempnumber binary_double := 0;
  sqlan      clob;
  TYPE crt IS REF CURSOR;
  wordtopiclob   clob;
  alldoctopiclob clob;
  executesql     varchar2(4000);
  myrecord       crt;
  wordtopic      integerarray := integerarray();
  alldoctopic    integerarray := integerarray();
  assigninfo     integerarray := integerarray();
  doccontent     integerarray := integerarray();
  diccontent     varchar2array := varchar2array();
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  sqlan := 'select  ' || diccontentcolumn || ' as content from ' ||
           dictable || ' where rownum=1 ';
  open myrecord for to_char(sqlan);
  loop
    FETCH myrecord
      INTO diccontent;
    EXIT WHEN myrecord%NOTFOUND;
  end loop;
  wordnumber := diccontent.count();
  wordtopic.extend();
  wordtopic(1) := 0;
  wordtopic.extend(wordnumber * topicnumber - 1, 1);
  alldoctopic.extend();
  alldoctopic(1) := 0;
  alldoctopic.extend(topicnumber - 1, 1);
  sqlan := 'select ' || docidcolumn || ' as alpineid,' || doccontentcolumn ||
           ' as alpinecontent, assign from ' || modeltable || ' ';
  open myrecord for to_char(sqlan);
  loop
    FETCH myrecord
      INTO i, doccontent, assigninfo;
    EXIT WHEN myrecord%NOTFOUND;
    for tempnumber in 1 .. assigninfo.count() loop
      alldoctopic(assigninfo(tempnumber)) := alldoctopic(assigninfo(tempnumber)) + 1;
      wordtopic(topicnumber * (doccontent(tempnumber) - 1) + assigninfo(tempnumber)) := wordtopic(topicnumber *
                                                                                                  (doccontent(tempnumber) - 1) +
                                                                                                  assigninfo(tempnumber)) + 1;
    end loop;
  end loop;
  wordtopiclob   := alpine_iarray_to_clob(wordtopic, ',');
  alldoctopiclob := alpine_iarray_to_clob(alldoctopic, ',');
  commit;
  executesql := 'alter session force parallel ddl';
  execute immediate executesql;
  execute immediate 'create   table ' || temptable ||
                    ' parallel as select ' || docidcolumn || ' ,' ||
                    doccontentcolumn || ',alpine_miner_plda_row(' ||
                    doccontentcolumn || ',integerarray(' || alldoctopiclob ||
                    ') ,integerarray(' || wordtopiclob || '),' ||
                    topicnumber || ',' || wordnumber || ',' || alpha || ',' || beta || ',' ||
                    iteration_number || ') as alpinepldainfo  from ' ||
                    content_table || ' where ' || docidcolumn ||
                    ' is not null and ' || doccontentcolumn ||
                    ' is not null   ';
  execute immediate 'CREATE  TABLE ' || docouttable ||
                    ' parallel as select  ' || docidcolumn || '  , ' ||
                    doccontentcolumn ||
                    ' ,  (alpinepldainfo).assign as alpinepldaassign  from ' ||
                    temptable || '   ';
  execute immediate ' create table ' || doctopictable ||
                    ' parallel  as select ' || docidcolumn || '  , ' ||
                    doccontentcolumn ||
                    ' ,  (alpinepldainfo).topic_count as alpinepldatopic  from  ' ||
                    temptable || '   ';
  executesql := 'alter session disable parallel dml';
  execute immediate executesql;
  commit;
  RETURN 1;
END alpine_miner_plda_predict;



/



