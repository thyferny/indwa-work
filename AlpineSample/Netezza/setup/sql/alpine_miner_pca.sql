 create or replace procedure alpine_miner_initpca(text,
                                                	text,
                                                  text,
                                                	text,
                                                	text,
                                                  text,
                                                  text )
   language nzplsql
   returns text
   as
   BEGIN_PROC
DECLARE
   columnnumber integer;
   rownumber    integer;
   i            integer;
   j            integer;
   tempnumber   double;
   numberindex  integer;
   totalindex   integer;
   tempindex    integer;
   tempsql      text;
   tempsql1			text;
   outtablename  text;
     temptable    text;
   sql1         text;
   sql2         text;
   sql3         text;
   lastresult   VARRAY(1000000) of double;
   result       VARRAY(1000000) of double;
   notnull      text;
   infor        VARRAY(1600) of varchar; 
   myrecord record;
begin

  sql1:='select columninfo as c ,dataindex as i from  '||$3 ||' order by i';
   i:=1;
   	for myrecord in execute sql1 loop
		infor(i):=myrecord.c;
		i:=i+1;
		end loop;
     columnnumber := i-1;
     tempsql :=infor(1);
      notnull      := ' where ' || tempsql || ' is not null ';
   IF $6 = 'yes' THEN
    tempnumber:= position('.' in  $2 ) ;
    	if tempnumber = 0
   then
   execute  immediate 'select droptable_if_existsdoubleq('''|| $2 ||''')';
   else 
   sql1:=	  SUBSTRING($2 from 2 for tempnumber - 3);
	 sql2 := SUBSTRING($2 from tempnumber+2 for length($2)-tempnumber-2);
   execute  immediate 'select droptable_if_exists('''||sql2||''')'; 
   end if;
   
   END IF;

      sql1 := 'create table ' || $2 || '(  '|| tempsql||'    double';
   sql2 := ' ';
 
   for i in 2.. columnnumber loop
   tempsql :=infor(i);
   sql1:=sql1||', ' || tempsql || ' double';
     notnull:=notnull||' and ' || tempsql || ' is not null';
   end loop;
 
   sql1:=sql1||',"alpine_pcadataindex" integer,"alpine_pcaevalue" double,"alpine_pcacumvl" double,"alpine_pcatotalcumvl" double) ';
  
    execute immediate sql1;
   tempsql:= 'select count(*) as c  from ' || $1 || notnull   ;
    	for myrecord in execute tempsql loop
		rownumber:=myrecord.c;
	
		end loop;
   
   
   totalindex := i*(i+1)/2;

       tempindex:=0;
      		  for i in 1 .. columnnumber loop
     for j in i .. columnnumber loop
         tempsql :=infor(i);
       tempsql1:=infor(j);
       tempindex := tempindex + 1;
        sql3:= 'select sum(' || tempsql || '::double*' || tempsql1 || '::double)::double as s from ' ||
                     $1  ;
                   
      	for myrecord in execute sql3 loop
    	 result(tempindex):=myrecord.s;
     end loop;
          end loop;
           tempsql:=  'select avg(' || tempsql || '::double) as a from ' ||$1  ;
        	for myrecord in execute tempsql loop
    	 result(totalindex+i):=myrecord.a;
     end loop;
     end loop;
     
      execute immediate 'select droptable_if_exists(''alpineinitPCA'||$7||''')'; 
     numberindex := 0;
   IF $5 = 'cov-sam' THEN
     for i in 1.. columnnumber loop
       for j in i .. columnnumber loop
         numberindex := numberindex + 1;

         tempnumber:=result(numberindex);
         lastresult(numberindex) := (result(numberindex) -
                                    rownumber * result(totalindex + i) *
                                    result(totalindex + j)) /
                                    (rownumber - 1);
          tempnumber:=lastresult(numberindex);
       end loop;
     end loop;
 elsIF $5 = 'cov-pop' THEN
       for i in 1.. columnnumber loop
         for j in i .. columnnumber loop
           numberindex := numberindex + 1;
           lastresult.extend;
            tempnumber:=result(numberindex);
          tempnumber:=result(totalindex + i);
           lastresult(numberindex) := (result(numberindex) -
                                      rownumber * result(totalindex + i) *
                                      result(totalindex + j)) / (rownumber);
  tempnumber:=lastresult(numberindex);
   
         end loop;
         end loop;
     elsIF $5 = 'corr' THEN
         for i in 1.. columnnumber loop
           for j in i .. columnnumber loop
             numberindex := numberindex + 1;
             lastresult.extend;
              tempnumber:=result(numberindex);
        

             lastresult(numberindex) := (result(numberindex) -  rownumber * result(totalindex + i) *
                                        result(totalindex + j)) /
                                        (sqrt(result((2 * columnnumber + 2 - i) *
                                                     (i - 1) / 2 + 1) -
                                              rownumber *
                                              result(totalindex + i) *
                                              result(totalindex + i)) *
                                        sqrt(result((2 * columnnumber + 2 - j) *
                                                     (j - 1) / 2 + 1) -
                                              rownumber *
                                              result(totalindex + j) *
                                              result(totalindex + j)));
  tempnumber:=lastresult(numberindex);
           end loop;
         end loop; 
		end IF;
		
		
		    tempnumber:= position('.' in  $4 ) ;
    	if tempnumber = 0
   then
   execute  immediate 'select droptable_if_existsdoubleq('''|| $4 ||''')';
   else 
   sql1:=	  SUBSTRING($4 from 2 for tempnumber - 3);
	 sql2 := SUBSTRING($4 from tempnumber+2 for length($4)-tempnumber-2);
	
   execute  immediate 'select droptable_if_exists('''||sql2||''')'; 
   end if;
		
   sql1 := 'create table ' || $4 || '( mvalue    double,numindex integer)';
       execute immediate sql1;
       for i in 1.. numberindex loop
       	tempnumber:=lastresult(i);

       execute immediate 'insert into ' || $4 || ' values ('||tempnumber||' , '||i ||')';
       
       end loop;    
      return $4;
 END;
END_PROC;

 CREATE OR REPLACE procedure  alpine_miner_pcaresult(text,
                                                   text,
                                                   text,
                                                   text,
                                                   text,
                                                   integer,
                                                   text,integer)
language nzplsql
   returns integer
   as
   BEGIN_PROC
DECLARE
pcanumber  ALIAS FOR $6;
splitnumber   ALIAS FOR $8;
   i            integer; 
     sql1 				text;
      j            integer;
   tempnumber   double;
   numberindex  integer;
   totalindex   integer;
      remainnumber integer;
   tempindex    integer;
    tempvalue  double;
    totalsql       text;
   sumsql			text;
   remainsql         text;
      columnnumber integer;
   rownumber    integer;
	modnumber     integer;
   sql2         text;
   sql3         text;
   lastresult   VARRAY(1000000) of double;
   result       VARRAY(1000000) of double;
	remaincolumn  VARRAY(1600) of varchar;
   notnull      text;
   infor        VARRAY(1600) of varchar; 
   myrecord record;
   tablenumber integer;
   joinsql text;
 		totaltablenumber integer;      
 		dataindex    integer;
 		groupnumber integer;
begin
   sql1:='select remaincolumninfo as r from  '||$4 ;
   i:=1;
   for myrecord in execute sql1 loop
		remaincolumn(i):=myrecord.r;
		i:=i+1;
		end loop;
     remainnumber := i-1;
       if remainnumber =0
   then  remainsql    := ' ';
   else
       remainsql    := ' ';
     for i in 1 .. remainnumber loop
      sql1:=remaincolumn(i);
      remainsql:= remainsql||',' || sql1;
     end loop;
	end if;
	  sql1:='select columninfo as c from  '||$2 ;
   i:=1;
   	for myrecord in execute sql1 loop
		infor(i):=myrecord.c;
		i:=i+1;
		end loop;
     columnnumber := i-1;
        IF $7 = 'yes' THEN
    tempnumber:= position('.' in  $3 ) ;
    	if tempnumber = 0
   then
   execute  immediate 'select droptable_if_existsdoubleq('''|| $3 ||''')';
   else 
    sql1:=	  SUBSTRING($3 from 2 for tempnumber - 3);
	sql2 := SUBSTRING($3 from tempnumber+2 for length($3)-tempnumber-2);
   execute  immediate 'select droptable_if_exists('''||sql2||''')'; 
   end if;
   
   END IF;
		if columnnumber < splitnumber
		then						
								totalsql := 'create table    ' || $3 || '  as select';
								i        := 1;
								sumsql   := ' ';
								    while i <= $6 loop
								    sql2:=infor(1);
								 				 sql1:= '(select ' || sql2 || ' as i from ' || $5 ||
								                    ' where  ' || $5 || '."alpine_pcadataindex"=' ||
								                    (i - 1) || ')';
								     				for myrecord in execute sql1 loop
														tempvalue:=myrecord.i;
														end loop;
														sumsql=sumsql||' ' || $1 || '.' || sql2 || '* (' ||
								                  tempvalue || ')::double';
								j := 2;
								 				 while j <= columnnumber loop
													 sql2:=infor(j);
								    				sql1:= '(select ' || sql2 || ' as i from ' || $5 ||
								                      ' where  ' || $5 ||
								                      '."alpine_pcadataindex"=' || (i - 1) || ')';
								      													for myrecord in execute sql1 loop
																							tempvalue:=myrecord.i;
																							end loop;
													sql2:=infor(j);
								    			sumsql:=sumsql||'+ ' || $1 || '.' || sql2 || '*(' ||
								                    tempvalue || ')::double';
								                   
								    			j := j + 1;
								  				end loop;
								 IF i = $6 then
								   sumsql:=sumsql||'   "attribute' || i || '"';
								  ELSE
								   sumsql:=sumsql||'   "attribute' || i || '" ,';
								  END IF;
								  i := i + 1;
								  end loop;
								totalsql:=totalsql|| sumsql;
								if remainnumber!=0
									then
									totalsql:=totalsql|| remainsql;
								end if;
								totalsql:=totalsql||' from ' || $1;
								execute immediate totalsql;
	elsif   pcanumber *( 1+columnnumber /splitnumber) <1600
	then
									groupnumber:=1;
									 totalsql := 'create table    ' || $3 || '  as  select ';
  								
 									  i        := 1;
										   sumsql   := ' select ';
  						     while i <= $6 loop
 							      sql2:=infor(1);
 							       modnumber:=0;
   				 				 sql1:= '(select ' || sql2 || ' as i from ' || $5 ||
                       ' where  ' || $5 || '."alpine_pcadataindex"=' ||
                       (i - 1) || ')';
   			     				for myrecord in execute sql1 loop
									tempvalue:=myrecord.i;
									end loop;
									sumsql=sumsql||' ' || sql2 || '*' ||tempvalue || '::double';
							   modnumber:=modnumber+1;
 					    j := 2;
    				 while j <= columnnumber loop
    				      
   					 sql2:=infor(j);
       				sql1:= '(select ' || sql2 || ' as i from ' || $5 ||
                         ' where  ' || $5 ||
                         '."alpine_pcadataindex"=' || (i - 1) || ')';
         
         													for myrecord in execute sql1 loop
																	tempvalue:=myrecord.i;
																	end loop;
							sql2:=infor(j);
							  if mod(modnumber,splitnumber)=0  then
							    	sumsql:=sumsql||' as alpine_pca'|| groupnumber||' ,  ' || sql2 || '*' ||
                       tempvalue || '::double';
                       totalsql:=totalsql||'  alpine_pca'||groupnumber||' +';  
                 groupnumber:=groupnumber+1;
                else
       				sumsql:=sumsql||'+' || sql2 || '*' ||
                       tempvalue || '::double';
                 end if;
              modnumber:=modnumber+1;
 	      			j := j + 1;
 		    				end loop;
 		    				
						    IF i = $6 then
					      sumsql:=sumsql||' as alpine_pca' ||groupnumber || '';
 						     totalsql:=totalsql||'  alpine_pca'|| groupnumber||' ';  
							  totalsql:=totalsql||' as   "attribute' || i || '" ';
 							    ELSE
					      sumsql:=sumsql||' as alpine_pca' || groupnumber|| ',';
					      totalsql:=totalsql||'  alpine_pca'||groupnumber||' ';  
								  totalsql:=totalsql||' as   "attribute' || i || '", ';
 							    END IF;
 							     groupnumber:=groupnumber+1;
							     i := i + 1;
								     end loop;
							  	if remainnumber!=0
									then
								totalsql:=totalsql|| remainsql;
								end if;
							    totalsql:=totalsql||'  from (  ';
							 totalsql:=totalsql|| sumsql;
								if remainnumber!=0
									then
								totalsql:=totalsql|| remainsql;
								end if;
							 totalsql:=totalsql||' from ' || $1||' limit all ) foo ';
						
							 execute immediate totalsql;
			else				 
							 
							    IF $7 = 'yes' 
   THEN tempnumber := position('.' in $3); 
        if tempnumber = 0 then 
        execute immediate 'select droptable_if_existsdoubleq(''' || $3 || ''')'; 
        outtablename := $3; 
        else sql1 := SUBSTRING($3 from 2 for tempnumber - 3); 
        sql2 := SUBSTRING($3 from tempnumber + 2 for length($3) - tempnumber - 2); 

        outtablename := sql2; 
        execute immediate 'select droptable_if_exists(''' || sql2 || ''')';
        end if;
  end IF; 
  tempnumber := position('.' in $2); 
        if tempnumber = 0 then 
        temptable := $2; 
        else sql1 := SUBSTRING($2 from 2 for tempnumber - 3); 
        sql2 := SUBSTRING($2 from tempnumber + 2 for length($2) - tempnumber - 2); 
        temptable :=  sql2; 
        end if;
  sql1 := 'select remaincolumninfo as r from  ' || $4;
   i := 1;
    for myrecord in execute sql1 loop 
        remaincolumn(i) := myrecord.r; 
        i := i + 1;
    end loop; 
  remainnumber := i - 1; 
  if remainnumber = 0 
  then remainsql := ' '; 
  else remainsql := ' '; 
       for i in 1 .. remainnumber loop 
           sql1 := remaincolumn(i); 
           remainsql := remainsql || ', ' || temptable || '0.' || sql1;
       end loop;
  end if; 
  sql1 := 'select columninfo as c from  ' || $2; 
  i := 1; 
  for myrecord in execute sql1 loop 
  infor(i) := myrecord.c; 
  i := i + 1;
  end loop; 
  columnnumber := i - 1;
  sql1 := 'create temp table    ' || temptable || '0  as  select *,row_number()over(order by 1 ) as alpine_pca_id from ' || $1; 

   execute immediate sql1;
  totaltablenumber :=1+ pcanumber * (1 + columnnumber/splitnumber)/1600;
  dataindex := 1; 
  groupnumber:=1;
  for tablenumber in 1 .. totaltablenumber loop 
      totalsql := 'create temp table    ' || temptable || tablenumber ||'  as  select alpine_pca_id,'; 
      modnumber := 0; 
      i := 1; 
      sumsql := ' select  alpine_pca_id,';
      while(i + (tablenumber - 1) * ((1600-1)/(1 + columnnumber/splitnumber)) <= $6 and i <= ((1600-1)/(1 + columnnumber/splitnumber))) loop
      sql2 := infor(1); 
      sql1 := '(select ' || sql2 || ' as i from ' || $5 || ' where  ' || $5 || '."alpine_pcadataindex"=' || (dataindex - 1) || ')'; 
           for myrecord in execute sql1 loop 
           tempvalue := myrecord.i;
           end loop;
      sumsql = sumsql || ' ' || sql2 || '*' || tempvalue || '::double';

      modnumber := modnumber + 1;
      j := 2; 
      while j <= columnnumber loop
            sql2 := infor(j); 
            sql1 := '(select ' || sql2 || ' as i from ' || $5 || ' where  ' || $5 || '."alpine_pcadataindex"=' || (dataindex - 1) || ')';
                 for myrecord in execute sql1 loop 
                 tempvalue := myrecord.i;
                 end loop; 
            sql2 := infor(j); 
            if mod(modnumber, splitnumber) = 0 then 
            sumsql := sumsql || ' as alpine_pca' || groupnumber|| ' ,  ' || sql2 || '*' || tempvalue || '::double';
             totalsql := totalsql || '  alpine_pca' || groupnumber || ' +';
						groupnumber:=groupnumber+1;
else sumsql := sumsql || '+' || sql2 || '*' || tempvalue || '::double';

end if; 
            modnumber := modnumber + 1;

            j := j + 1;
            end loop;
						tempnumber:=i + (tablenumber - 1) * (1600/(1 + columnnumber/splitnumber));
						tempnumber:=1600/(1 + columnnumber/splitnumber);
						IF (i + (tablenumber - 1) * (1600-1)/(1 + columnnumber/splitnumber) >= $6 or i = (1600-1)/(1 + columnnumber/splitnumber))
            then 
             sumsql := sumsql || ' as alpine_pca' || groupnumber || '';
             totalsql := totalsql || '  alpine_pca' || groupnumber || ' ';
             totalsql := totalsql || ' as   "attribute' || dataindex || '" ';
              ELSE 
              sumsql := sumsql || ' as alpine_pca' || groupnumber || ','; 
             totalsql := totalsql || '  alpine_pca' || groupnumber|| ' ';
             totalsql := totalsql || ' as   "attribute' || dataindex || '", ';
             END IF; 
             groupnumber:=groupnumber+1;
             dataindex:=dataindex+1;
             i := i + 1;
             end loop;
             totalsql := totalsql || '  from (  '; 
             totalsql := totalsql || sumsql;
             totalsql := totalsql || ' from ' || temptable || '0 limit all ) foo '; 
             execute immediate totalsql;
             end loop;
             totalsql := ' create table  ' || outtablename || ' as select '; 
             if remainnumber != 0 then 
             totalsql := totalsql || remainsql; 
             i := 1; 
             dataindex := 1; 
             joinsql := ' '; 
             while i <= totaltablenumber loop 
             j := 1;
              while( j + (i - 1) *((1600-1)/(1 + columnnumber/splitnumber)) <= $6 and j <= ((1600-1)/(1 + columnnumber/splitnumber))) loop 
                  totalsql := totalsql || '  ' || temptable || i || '."attribute' || dataindex || '" ,';
              dataindex:=dataindex+1;
             end loop; 
             joinsql := joinsql || '  join ' || temptable || i || ' on ' || temptable || '0.alpine_pca_id =  ' || temptable || i || '.alpine_pca_id ';
             end loop; 
             totalsql := SUBSTRING(totalsql from 1 for length(totalsql) - 2); 
             totalsql := totalsql || ' from ' || temptable || '0 ' || joinsql; 
             execute immediate totalsql;
             else 
             i := 1; 
             dataindex := 1; 
             joinsql := ' ' || temptable || '1 '; 
             while i <= totaltablenumber loop 
             j := 1;
             while( j + (i - 1) *((1600-1)/(1 + columnnumber/splitnumber)) <= $6 and j <= ((1600-1)/(1 + columnnumber/splitnumber))) loop 
             totalsql := totalsql || '  ' || temptable || i || '."attribute' || dataindex || '" ,'; 
             j := j + 1;
             dataindex:=dataindex+1;
             end loop; 
             i := i + 1;
             end loop;
             i := 2; 
             while i <= totaltablenumber loop
             joinsql := joinsql || '  join ' || temptable || i || ' on ' || temptable || '1.alpine_pca_id =  ' || temptable || i || '.alpine_pca_id '; 
             i := i + 1;
             end loop;
             totalsql := SUBSTRING(totalsql from 1 for length(totalsql) - 1); 
             totalsql := totalsql || ' from ' ||  joinsql;             
             execute immediate totalsql;
             end if;
	  end if;
return 1;
 END;
END_PROC;