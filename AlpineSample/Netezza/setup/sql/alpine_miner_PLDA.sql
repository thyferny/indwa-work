CREATE OR REPLACE PROCEDURE  alpine_miner_plda_train (text,text,text,	 float , float , float , text,text,integer ,text,text,text,text,text)
LANGUAGE nzplsql
returns integer as
BEGIN_PROC
DECLARE 
content_table ALIAS FOR $1;
docidcolumn ALIAS FOR  $2;		
doccontentcolumn ALIAS FOR $3; 
alpha ALIAS FOR $4; 
beta ALIAS FOR $5; 
topicnumber ALIAS FOR $6; 
dictable ALIAS FOR $7;
diccontentcolumn  ALIAS FOR $8;
iteration_number ALIAS FOR $9; 
tempouttable1 ALIAS FOR $10; 
tempouttable2 ALIAS FOR $11; 
outtable ALIAS FOR $12;
doctopoctable ALIAS FOR $13;
topicouttable ALIAS FOR $14;

myrecord record;
myrecord2 record;
i bigint;
j float;
k float;
temptopic float;
tempnumber float;
rowindex float;
totalrowindex float;
mutibynumber float;
randomnumber float;
tempiteration bigint;
sql text;
tempstring text;
sumpeoso float;
sumnumber float;
wordnumber float;
execStr text;
tempfloat float;
temptext  float;
singledoc VARRAY(1000000) of float;
docwordtopic VARRAY(1000000) of float;
peoso   VARRAY(1000000) of float;
wordtopic VARRAY(1000000) of float;
alldoctopic  VARRAY(1000000) of float;
tempwordtopic VARRAY(1000000) of float;
tempalldoctopic  VARRAY(1000000) of float;
lastgeneinfo VARRAY(1000000) of float;
lastwordtopic VARRAY(1000000) of float;
doccontent VARRAY(1000000) of float;
diccontent VARRAY(1000000) of text;

BEGIN
		sql:= 'select  count(*) as c from '||dictable||' ';
		for myrecord in execute sql loop
				wordnumber:=myrecord.c;
		end loop;
	  sql:='CREATE  TABLE '||tempouttable1||' ( alpinepldagenera float , '||docidcolumn||' float , '|| doccontentcolumn||' float,  alpine_plda_topic float)';
		    
		execute immediate sql;
	 	sql:='CREATE  TABLE '||tempouttable2||' ( alpinepldagenera float,alpinepldadocid float ,alpinepldatopicid float,alpinepldacount float)';
		execute  immediate sql;
		tempnumber:=1;
		while tempnumber<=topicnumber loop
				peoso(tempnumber):=1.0/topicnumber;
				alldoctopic(tempnumber):=0;
				tempnumber:=tempnumber+1;
		end loop;
  	tempnumber:=1;
		while tempnumber <=wordnumber*topicnumber loop	
				wordtopic(tempnumber):=0;
				tempnumber:=tempnumber+1;
		end loop;
	
	
	
		sql := 'select distinct('||docidcolumn||') as alpineid from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid limit 1';
		for myrecord in execute sql loop
				totalrowindex:=myrecord.alpineid;
		end	loop;
		rowindex:=totalrowindex;
		tempnumber:=1;
		sql := 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid';
		for myrecord in execute sql loop
				if myrecord.alpineid!=rowindex
				then
				k:=1;
				while k <= topicnumber loop
			 			singledoc(k):=0;
			 			k:=k+1;
				end loop;
				j:= 1;
				while j<tempnumber loop
						sumnumber:=0;
						execStr:='select random() as R';
				
							for myrecord2 in execute execStr loop
									randomnumber:=myrecord2.R;
							end loop;
    				temptopic:=1;
    			
						while temptopic<=topicnumber loop
								--sumnumber:=sumnumber+peoso(temptopic);
	    					if randomnumber<=sumnumber
  	  					then exit  ;
    						end if;
    						temptopic:=temptopic+1;
    				end loop;
    			
						singledoc(temptopic):=singledoc(temptopic)+1;
						docwordtopic(j):=temptopic;
						
						tempfloat:=	doccontent(j);
						wordtopic(topicnumber*(tempfloat-1)+temptopic):=wordtopic(topicnumber*(tempfloat-1)+temptopic)+1;
	  				
	  				alldoctopic(temptopic):=alldoctopic(temptopic)+1;
	  				tempfloat:=doccontent(j);
						sql:='insert into '||tempouttable1||' values ( 1,'||rowindex||','||tempfloat||','||temptopic||')';
						execute  immediate sql;
						j := j+1;
				end loop;
				j:=1;
				while j<=topicnumber loop
						tempfloat:=singledoc(j);
						sql:='insert into '||tempouttable2||' values ( 1,'||rowindex||','||j||','||tempfloat||')';
						execute immediate  sql;
						j:=j+1;
				end loop;
				
				--docwordtopic:=null;
				--singledoc:=null;
				tempnumber:=1;
				rowindex:=myrecord.alpineid;
				--doccontent:=null;
				end if;
				i:=myrecord.alpineid;
				doccontent(tempnumber):=myrecord.alpinecontent;
				tempnumber:=tempnumber+1;	
		END LOOP;

				k:=1;
		while k<=topicnumber loop
				singledoc(k):=0;
				k:=k+1;
		end loop;
		j:=1;	
		while j<= (tempnumber-1) loop
				sumnumber:=0;
				execStr := 'select random() as R' ;
    		for myrecord2 in execute execStr loop
									randomnumber:=myrecord2.R;
							end loop;
    		temptopic:=1;
				while temptopic<=topicnumber loop
								sumnumber:=sumnumber+peoso(temptopic);
	    					if randomnumber<=sumnumber
  	  					then exit  ;
    						end if;
    						temptopic:=temptopic+1;
    				end loop;
				singledoc(temptopic):=singledoc(temptopic)+1;
				docwordtopic(j):=temptopic;	
				tempfloat:=doccontent(j);
				wordtopic(topicnumber*(tempfloat-1)+temptopic):=wordtopic(topicnumber*(tempfloat-1)+temptopic)+1;
				alldoctopic(temptopic):=alldoctopic(temptopic)+1;
				tempfloat:=doccontent(j);
				sql:='insert into '||tempouttable1||' values ( 1,'||rowindex||','||tempfloat||','||temptopic||')';
				execute immediate sql;
						j := j+1;
				end loop;
				j:=1;
				while j<=topicnumber loop
				tempfloat:=singledoc(j);
				sql:='insert into '||tempouttable2||' values ( 1,'||rowindex||','||j||','||tempfloat||')';
				execute  immediate  sql;
				  j:=j+1;
			end loop;

		--docwordtopic:=null;
		--singledoc:=null;

			tempiteration:=2;
			while tempiteration<=iteration_number loop
						tempnumber:=1;
						while tempnumber<=wordnumber*topicnumber loop
								tempwordtopic(tempnumber):=0;
								tempnumber:=tempnumber+1;
						end loop;
					
		 				tempnumber:=1;
						while tempnumber  <=topicnumber loop
								tempalldoctopic(tempnumber):=0;
								tempnumber:=tempnumber+1;
						end loop;
						rowindex:=totalrowindex;
						tempnumber:=1;
						sql := 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent , alpine_plda_topic  as lastgeinfo from '|| tempouttable1 ||' where alpinepldagenera='||(tempiteration-1)||' and '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid';
						k:=1;
						
            for myrecord in execute sql loop
                if myrecord.alpineid!=rowindex
              	then

                sql:= 'select alpinepldadocid as alpineid,alpinepldatopicid as topicid,alpinepldacount as contents from '|| tempouttable2 ||' where alpinepldagenera='||(tempiteration-1)||' and alpinepldadocid= '||rowindex||'  order by alpineid';
  					
                for myrecord2 in execute sql loop
                      lastgeneinfo(myrecord2.topicid):=myrecord2.contents;
                  end loop;
                  j:= 1;
                  tempnumber:=1;
                  while tempnumber <=topicnumber loop
                      singledoc(tempnumber):=0;
                      tempnumber:=tempnumber+1;
                  end loop;
                  tempnumber:=1;
              
        	    while tempnumber <k loop
                      sumpeoso:=0;
                      j:=1;
                      while j <=topicnumber loop
                          tempfloat:=doccontent(tempnumber);
                         --peoso(j):=0.5;
                          peoso(j):=(lastgeneinfo(j)+alpha)*(wordtopic(topicnumber*(tempfloat-1)+j)+beta)/(alldoctopic(j)+wordnumber*beta);
                          sumpeoso:=sumpeoso+peoso(j);
                          j:=j+1;
                      end loop;
                      j:=1;
                      while  j <=topicnumber loop       
                          peoso(j):=peoso(j)/sumpeoso;
                          j:=j+1;
                      end loop;
                      sumnumber:=0;
                      execStr := 'select random() as R' ;
                      for myrecord2 in execute execStr loop
                          randomnumber:=myrecord2.R;
                      end loop;
                      temptopic:=1;
                      while temptopic<=topicnumber loop
                          sumnumber:=sumnumber+peoso(temptopic);
                          if randomnumber<=sumnumber
                          then exit  ;
                          end if;
                          temptopic:=temptopic+1;
                      end loop; 
                    
                    	singledoc(temptopic):=singledoc(temptopic)+1;
                     
                      lastwordtopic(tempnumber):=temptopic;
                     
                      tempfloat:=doccontent(tempnumber);
                     
                      
                      
                      tempwordtopic(topicnumber*(tempfloat-1)+temptopic):=tempwordtopic(topicnumber*(tempfloat-1)+temptopic)+1;
                      
                      tempalldoctopic(temptopic):=tempalldoctopic(temptopic)+1;
                      sql:='insert into '||tempouttable1||' values ( '||tempiteration||','||rowindex||','||tempfloat||','||temptopic||')';
                    	raise notice ' %',sql;
                     	execute immediate sql;
                    
                      tempnumber:=tempnumber+1;
                     
                  end loop;
                  j:=1;
                  while j<=topicnumber loop
                      tempfloat:=singledoc(j);
                      sql:='insert into '||tempouttable2||' values ( '||tempiteration||','||rowindex||','||j||','||tempfloat||')';
                  execute  immediate sql;
                  j:=j+1;
                  end loop;
                  --docwordtopic:=null;
                  --singledoc:=null;
                  tempnumber:=1;
  								k:=1;
            			rowindex:=myrecord.alpineid;
                  --doccontent:=null;
              end if;
  		   			i:=myrecord.alpineid;
              doccontent(k):=myrecord.alpinecontent;
              lastgeneinfo(k):=myrecord.lastgeinfo;    
              raise notice ' i: %  content: %',      myrecord.alpineid,   myrecord.alpinecontent;
              k:=k+1;
  						end loop;
  
     -- k:=1;
      j:= 1;
      tempnumber:=1;
      while tempnumber <=topicnumber loop
          singledoc(tempnumber):=0;
          tempnumber:=tempnumber+1;
      end loop;
      tempnumber:=1;
      sql := 'select alpinepldadocid as docid, alpinepldatopicid  as alpineid,alpinepldacount as count from '|| tempouttable2 ||' where alpinepldagenera='||(tempiteration-1)||' and alpinepldadocid= '||rowindex||'  order by alpineid';
      for myrecord in execute sql loop

          lastgeneinfo(myrecord.alpineid):=myrecord.count;
      end loop;
      while tempnumber <k loop
          sumpeoso:=0;
          j:=1;
         while j <=topicnumber loop
              tempfloat:=doccontent(tempnumber);
              peoso(j):=(lastgeneinfo(j)+alpha)*(wordtopic(topicnumber*(tempfloat-1)+j)+beta)/(alldoctopic(j)+wordnumber*beta);
              sumpeoso:=sumpeoso+peoso(j);
              j:=j+1;
          end loop;
          j:=1;
          while  j <=topicnumber loop
              peoso(j):=peoso(j)/sumpeoso;
              j:=j+1;
          end loop;
          sumnumber:=0;
  				
      execStr := 'select random() as R' ;
              for myrecord2 in execute execStr loop
                  randomnumber:=myrecord2.R;
              end loop;
          temptopic:=1;
          while temptopic<=topicnumber loop
              sumnumber:=sumnumber+peoso(temptopic);
              if randomnumber<=sumnumber
              then exit  ;
              end if;
              temptopic:=temptopic+1;
          end loop;
          singledoc(temptopic):=singledoc(temptopic)+1;
          lastwordtopic(tempnumber):=temptopic;
          tempfloat:=doccontent(tempnumber);
          tempwordtopic(topicnumber*(tempfloat-1)+temptopic):=tempwordtopic(topicnumber*(tempfloat-1)+temptopic)+1;
          tempalldoctopic(temptopic):=tempalldoctopic(temptopic)+1;
          tempfloat:=doccontent(tempnumber);
          sql:='insert into '||tempouttable1||' values ( '||tempiteration||','||rowindex||','||tempfloat||','||temptopic||')';
          execute immediate	sql;
         
          tempnumber:=tempnumber+1;
          
      end loop;
      j:=1;
      while j<=topicnumber loop
          tempfloat:=singledoc(j);
          sql:='insert into '||tempouttable2||' values ( '||tempiteration||','||rowindex||','||j||','||tempfloat||')';
          execute  immediate sql;
          j:=j+1;
      end loop;   
        
    --	docwordtopic:=null;
    --	singledoc:=null;
      tempnumber:=1;
    --  rowindex:=myrecord.docid;
    --	doccontent:=null;
        tempnumber:=1; 
              	while tempnumber <=wordnumber*topicnumber loop	
									wordtopic(tempnumber):=tempwordtopic(tempnumber);
									tempnumber:=tempnumber+1;
								end loop;
              	tempnumber:=1; 
              while tempnumber <=topicnumber loop	
           
              alldoctopic(tempnumber):=tempalldoctopic(tempnumber);
              	tempnumber:=tempnumber+1;
								end loop;
      tempiteration:=tempiteration+1;
      end loop;
             
      sql:='create table '||outtable||' as (select '||docidcolumn||'  , '|| doccontentcolumn||' ,  alpine_plda_topic   from '|| tempouttable1||' where alpinepldagenera = '||iteration_number||')  ';
      execute  immediate  sql;
			 sql:='create table '||doctopoctable||' as (select alpinepldadocid  , alpinepldatopicid ,  alpinepldacount  from '|| tempouttable2||' where alpinepldagenera = '||iteration_number||')  ';
      execute  immediate  sql;
        
      
      
      sql:='create table '||topicouttable||' as (';
      execStr:=' select '|| doccontentcolumn||' ' ;
      j:=1;
      while j<=topicnumber loop
          execStr:=execStr||', sum(case when alpine_plda_topic = '||j||' then 1 else 0 end )  topic'||j;
          j:=j+1;
      end loop;
      execStr:=execStr||'  from '||outtable||' group by  '|| doccontentcolumn||'  ';
      sql:=sql||execStr||' )  ';
      execute immediate sql;
          
		
	
RETURN 1;

END;
END_PROC;


CREATE OR REPLACE PROCEDURE  alpine_miner_plda_predict(text,text,text,text,float,float,float,integer,text,text,text,text,text)
LANGUAGE nzplsql
returns integer as
BEGIN_PROC
DECLARE 
modeltable ALIAS FOR $1;
content_table ALIAS FOR $2;
docidcolumn ALIAS FOR $3;
doccontentcolumn ALIAS FOR $4;
alpha ALIAS FOR $5;
beta ALIAS FOR $6;
topicnumber ALIAS FOR $7;

iteration_number ALIAS FOR $8;
dictable ALIAS FOR $9;
diccontentcolumn ALIAS FOR $10;
dicidcolumn ALIAS FOR $11;
docouttable ALIAS FOR $12;
docouttable1 ALIAS FOR $13;

i float;
j float;
k float;
wordnumber float;
myrecord record;
myrecord2 record;
temptopic float;
tempnumber float;
mutibynumber float;
tempiteration float;
sql text;
tempstring text;
tempfloat float;
temptext text;
singledoc  VARRAY(1000000) of float; 
docwordtopic VARRAY(1000000) of float;
peoso   VARRAY(1000000) of float; 
wordtopic VARRAY(1000000) of float; 
alldoctopic  VARRAY(1000000) of float; 
lastgeneinfo VARRAY(1000000) of float;
doccontent VARRAY(1000000) of float;
diccontent VARRAY(1000000) of text;
docwordcontent VARRAY(1000000) of text;
sumpeoso float;
rowindex float;
randomnumber float;
TOTALROWINDEX float;
sumnumber float;
execStr text;

begin

	tempnumber:=1;
	
	sql:= 'select  count(*) as c  from '||dictable||' ';
	for myrecord in execute sql loop
			wordnumber:=myrecord.c;
	end   loop;
	
	while tempnumber<=topicnumber loop
			alldoctopic(tempnumber):=0;
			tempnumber:=tempnumber+1;
	end   loop;
	tempnumber:=1;
	while tempnumber <=wordnumber*topicnumber loop	
			wordtopic(tempnumber):=0;
			tempnumber:=tempnumber+1;
	end   loop;

	sql:= 'select '||dicidcolumn||' as id , '||diccontentcolumn||' as content from '||dictable||' ';
	for myrecord in execute sql loop
			diccontent(myrecord.id)=myrecord.content;
	end   loop;
	sql:= 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent, alpine_plda_topic as topic from '|| modeltable ||' ';
	for myrecord in execute sql loop
			alldoctopic(myrecord.topic):=alldoctopic(myrecord.topic)+1;
			wordtopic(topicnumber*(myrecord.alpinecontent-1)+myrecord.topic):=wordtopic(topicnumber*(myrecord.alpinecontent-1)+myrecord.topic)+1;
	end   loop;
	sql:= 'CREATE  TABLE '||docouttable||' ( '||docidcolumn||' bigint , '|| doccontentcolumn||' varchar(100),  alpine_plda_topic  int)';
	execute immediate sql;
	k:=1;
	sql := 'select distinct('||docidcolumn||') as alpineid from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid limit 1';
	for myrecord in execute sql loop
				totalrowindex:=myrecord.alpineid;
	end   loop;

	rowindex:=totalrowindex;
	
	sql := 'select '||docidcolumn||' as alpineid,'|| doccontentcolumn||' as alpinecontent from '|| content_table||' where '||docidcolumn||' is not null and '|| doccontentcolumn||' is not null order by alpineid';
	
	for myrecord in execute sql loop

			if myrecord.alpineid!=rowindex
			then
					tempnumber:=1;
					while tempnumber <=topicnumber loop
							lastgeneinfo(tempnumber):=0;
							tempnumber:=tempnumber+1;
					end   loop;
					tempnumber:=1;
					while tempnumber <=k loop
							docwordtopic(tempnumber):=0;
							tempnumber:=tempnumber+1;
					end   loop;
					tempiteration:=1;
			
					while tempiteration <= iteration_number loop
							tempnumber:=1;
							while tempnumber <=topicnumber loop
									singledoc(tempnumber):=0;
									tempnumber:=tempnumber+1;
							end   loop;
							tempnumber:=1;
							while tempnumber <k loop
									sumpeoso:=0;
									j:=1;
									while j <=topicnumber loop
											tempfloat:=doccontent(tempnumber);
											peoso(j):=(lastgeneinfo(j)+alpha)*(wordtopic(topicnumber*(tempfloat-1)+j)+beta)/(alldoctopic(j)+wordnumber*beta);
											sumpeoso:=sumpeoso+peoso(j);
											j:=j+1;
									end   loop;
									j:=1;
							
									while j <=topicnumber loop
											peoso(j):=peoso(j)/sumpeoso;
											j:=j+1;
									end   loop;
									execStr := 'select random() as R' ;
		    					for myrecord2 in execute execStr loop
											randomnumber:=myrecord2.R;
									end   loop;
		    					temptopic:=1;
		    			
		    					sumnumber:=0;
									while temptopic<=topicnumber loop
								
											sumnumber:=sumnumber+peoso(temptopic);
			    						if randomnumber<=sumnumber
		  	  						then exit  ;
		    							end if;
		    							temptopic:=temptopic+1;
		    					end   loop;
		    			
									singledoc(temptopic):=singledoc(temptopic)+1;
									docwordtopic(tempnumber):=temptopic;
									tempnumber:=tempnumber+1;
							end   loop;
							tempiteration:=tempiteration+1;
					end   loop;
					j:=1;
		
					while j<k loop
							lastgeneinfo(j):=docwordtopic(j);
							tempfloat:=doccontent(j);
							docwordcontent(j):=diccontent(tempfloat);
							temptext:=docwordcontent(j);
							tempfloat:=docwordtopic(j);
							sql:='insert into '||docouttable||' values ( '||i||','''||temptext||''','||tempfloat||')';
							execute immediate sql;
							j:=j+1;
					end   loop;
					rowindex:=myrecord.alpineid;
					k:=1;
			end if;
	
			i:=myrecord.alpineid;
			doccontent(k):=myrecord.alpinecontent;
			k:=k+1;
	end   loop;
	
	
	
	tempnumber:=1;
	while tempnumber <=topicnumber loop
			lastgeneinfo(tempnumber):=0;
			tempnumber:=tempnumber+1;
	end   loop;
	tempnumber:=1;
	while tempnumber <=k loop
			docwordtopic(tempnumber):=0;
			tempnumber:=tempnumber+1;
	end   loop;
	tempiteration:=1;
	while tempiteration <= iteration_number loop
			tempnumber:=1;
			while tempnumber <=topicnumber loop
					singledoc(tempnumber):=0;
					tempnumber:=tempnumber+1;
			end   loop;
			tempnumber:=1;
			while tempnumber <k loop
					sumpeoso:=0;
					j:=1;
					while j <=topicnumber loop
							tempfloat:=doccontent(tempnumber);
							peoso(j):=(lastgeneinfo(j)+alpha)*(wordtopic(topicnumber*(tempfloat-1)+j)+beta)/(alldoctopic(j)+wordnumber*beta);
							sumpeoso:=sumpeoso+peoso(j);
							j:=j+1;
					end   loop;
					j:=1;
					while j <=topicnumber loop
							peoso(j):=peoso(j)/sumpeoso;
							j:=j+1;
					end   loop;
					execStr := 'select random() as R' ;
		    	for myrecord2 in execute execStr loop
								randomnumber:=myrecord2.R;
					end   loop;
		    	temptopic:=1;
		    	sumnumber:=0;
					while temptopic<=topicnumber loop
							sumnumber:=sumnumber+peoso(temptopic);
			    		if randomnumber<=sumnumber
		  	  		then exit  ;
		    			end if;
		    			temptopic:=temptopic+1;
		    	end   loop;
					singledoc(temptopic):=singledoc(temptopic)+1;
					docwordtopic(tempnumber):=temptopic;
					tempnumber:=tempnumber+1;
			end   loop;
			tempiteration:=tempiteration+1;
	end   loop;		

	j:=1;
	while j<=(k-1) loop
			lastgeneinfo(j):=docwordtopic(j);			
			tempfloat:=doccontent(j);
			docwordcontent(j):=diccontent(tempfloat);
			temptext:=docwordcontent(j);
			tempfloat:=docwordtopic(j);
			sql:='insert into '||docouttable||' values ( '||i||','''||temptext||''','||tempfloat||')';
			execute immediate sql;
			j:=j+1;
	end   loop;
	sql:='create table '||docouttable1||' as (';
	execStr:=' select '|| doccontentcolumn||' ' ;
	j:=1;
	while j<=topicnumber loop
			execStr:=execStr||', sum(case when alpine_plda_topic = '||j||' then 1 else 0 end )  topic'||j;
			j:=j+1;
	end   loop;
	execStr:=execStr||'  from '||docouttable||' group by  '|| doccontentcolumn||'  ';
	sql:=sql||execStr||' )  ';
	execute immediate  sql;

RETURN 1;

END;
END_PROC;