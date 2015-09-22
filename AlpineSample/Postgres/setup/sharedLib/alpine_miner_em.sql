CREATE OR REPLACE FUNCTION alpine_miner_array_avg(arraydata double precision[], arraysize bigint)
  RETURNS double precision[] AS
$BODY$
DECLARE 
	i integer;
	newarraydata double precision[];
BEGIN
	i:=1;
	while i <= alpine_miner_get_array_countf(arraydata) loop
	newarraydata[i]:=arraydata[i] /arraysize;
	i:=i+1;
	end loop;
RETURN newarraydata;

END;

$BODY$
LANGUAGE plpgsql VOLATILE;
  
  
CREATE OR REPLACE FUNCTION alpine_miner_em_getp(columnarray double precision[], mu double precision[],sigma double precision[],
alpha double precision)
RETURNS double precision
AS 'alpine_miner','alpine_miner_em_getp'
LANGUAGE C
IMMUTABLE STRICT;


CREATE OR REPLACE FUNCTION alpine_miner_em_getmaxsub(firstarray double precision[], firstarray double precision[])
RETURNS double precision
AS 'alpine_miner','alpine_miner_em_getmaxsub'
LANGUAGE C
IMMUTABLE STRICT;




-- Function: alpine_miner_em_train(text, text[], integer, integer, integer, double precision, text)

-- DROP FUNCTION alpine_miner_em_train(text, text[], integer, integer, integer, double precision, text);

CREATE OR REPLACE FUNCTION alpine_miner_em_train(tablename text, columnname text[], clusternumber integer, clustersize integer, maxiteration integer, epsilon double precision, temptable text,sigmas double precision[])
  RETURNS double precision[] AS
$BODY$
DECLARE 
	sql text;
	alpha double precision[];
	mu  double precision[];
	sigma double precision[];
	prealpha double precision[];
	premu  double precision[];
	presigma double precision[];
	maxsubalpha double precision;
	maxsubmu double precision;
	maxsubsigma double precision;
	maxsubvalue double precision;
	test double precision[];
	columnsize integer;
	tempmu text;
	tempsigma text;
	tempalpha text;
	sum text;
	tempiteration integer;
	stop integer;
	i integer;
	j integer;
	k integer;
	myrecord record;
BEGIN
	
	columnsize:=alpine_miner_get_array_count(columnname);
	sql:=' select alpine_miner_array_avg(sum(arraydata),sum(1)) as a from ( select array['||array_to_string(columnname,',')||'] as arraydata,mod( row_number() over (order by random()),'||clusternumber||' ) as clusterid from '||tablename||'  
	where '||array_to_string(columnname,' is not null and ')||' is not null limit   '||clusternumber||'*'||clustersize ||') as foo group by clusterid ';
	 
	i:=1;
	k:=1;
	j:=0;
	for myrecord in execute sql loop
		if j=0
		then 
		mu:= myrecord.a;
		sum:='(alpine_miner_em_p'||k;
		j:=1;
		else
		mu:=array_cat(mu,myrecord.a);
		sum:=sum||'+alpine_miner_em_p'||k;
		end if;
		alpha[k]:=1.0/clusternumber;
		k:=k+1;
	end loop;
	sum:=sum||') as alpine_miner_em_sum ';
	for i in 1.. columnsize*clusternumber loop
		sigma[i]:=sigmas[i];
	end loop;	
	sql:=' create temp table '||temptable||' as select * , '||sum||' from ( select  *';
	for i in 1..  clusternumber loop
		 
		sql:=sql||' , alpine_miner_em_getp(array['||array_to_string(columnname,',')||'] , array['||array_to_string(mu[((i-1)*columnsize+1):(i*columnsize)],',')||'] , 
		array['||array_to_string(sigma[((i-1)*columnsize+1):(i*columnsize)],',')||'] ,'||alpha[i]||' ) as alpine_miner_em_p'||i ;
	end loop;		
	sql:=sql||'   from '||tablename||') as foo';
	 
	execute sql;

	sql:=' update  '||temptable||' set alpine_miner_em_p1=(case when alpine_miner_em_p1<1e-22 and alpine_miner_em_sum<1e-20 then '||1.0/clusternumber||' else alpine_miner_em_p1 end)';
	for i in 2..  clusternumber loop
		sql:=sql||',alpine_miner_em_p'||i||'=(case when alpine_miner_em_p'||i||'<1e-22 and alpine_miner_em_sum<1e-20 then '||1.0/clusternumber||' else alpine_miner_em_p'||i||' end)';
	end loop;
	execute sql;

	sql:=' update  '||temptable||' set alpine_miner_em_sum=alpine_miner_em_p1';
	for i in 2..  clusternumber loop
		sql:=sql||'+alpine_miner_em_p'||i;
	end loop;
	execute sql;
	
	tempiteration:=2;
	stop:=0;
	while stop=0 and tempiteration<=maxiteration loop

	prealpha:=alpha;
	premu:=mu;
	presigma:=sigma;
	for i in 1..  clusternumber loop
		if i=1
		then 
		tempalpha:=' array[avg(alpine_miner_em_p1/alpine_miner_em_sum)';

		for j in 1 ..columnsize loop
			if j=1 then
			tempmu:=' array[sum('||columnname[j]||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
			else
			tempmu:=tempmu||',sum('||columnname[j]||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
			end if;
		end loop;
		else
		tempalpha:=tempalpha||',avg(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
		for j in 1 ..columnsize loop
			 
			tempmu:=tempmu||',sum('||columnname[j]||'*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
			 
		end loop;
		
		end if;
	end loop;
	tempalpha:=tempalpha||'] ';
	tempmu:=tempmu||']';
  	sql:=' select  '||tempalpha||' as alpha ,'||tempmu||' as mu from '||temptable;
  	for myrecord in execute sql loop
  	alpha:=myrecord.alpha;
  	mu:=myrecord.mu;
  	end loop;
  	k:=1;
  	for i in 1..  clusternumber loop
  		for j in 1 ..columnsize loop
  			if i=1
  			then 
  				if j=1 then
  				 
  				tempsigma:=' array[sum(('||columnname[j]||'- '||mu[k]||')*('||columnname[j]||'- '||mu[k]||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
  				else
  				 
  				tempsigma:=tempsigma||',sum(('||columnname[j]||'- '||mu[k]||')*('||columnname[j]||'- '||mu[k]||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';
  				end if;
  			
  		
  			else
  			 
  			tempsigma:=tempsigma||',sum(('||columnname[j]||'- '||mu[k]||')*('||columnname[j]||'- '||mu[k]||')*alpine_miner_em_p'||i||'/alpine_miner_em_sum)/sum(alpine_miner_em_p'||i||'/alpine_miner_em_sum)';  		
  			end if;
  			k:=k+1;
  		end loop;
  	end loop;
	tempsigma:=tempsigma||'] ';
  	sql:=' select  '||tempsigma||' as sigma  from '||temptable;
 	for myrecord in execute sql loop
  	sigma:=myrecord.sigma;
  	
  	end loop;

	maxsubalpha:=alpine_miner_em_getmaxsub(prealpha,alpha);
  	maxsubmu:=alpine_miner_em_getmaxsub(premu,mu);
	maxsubsigma:=alpine_miner_em_getmaxsub(presigma,sigma);

	if maxsubalpha>maxsubmu and maxsubalpha>maxsubsigma
	then maxsubvalue:=maxsubalpha;
	else 
	if maxsubmu>maxsubalpha and maxsubmu>maxsubsigma
	then maxsubvalue:=maxsubmu;
	else 
	if maxsubsigma>maxsubalpha and maxsubsigma>maxsubmu
	then maxsubvalue:=maxsubsigma;
	end if;
	end if;
	end if;
	if epsilon>maxsubvalue
	then
	stop:=1;
	end if;
	tempiteration:=tempiteration+1;
	end loop;


	
RETURN array_cat(array_cat(alpha,mu),sigma);

END;

 $BODY$
  LANGUAGE plpgsql VOLATILE;
 
CREATE OR REPLACE FUNCTION  alpine_miner_em_predict(outputtable text,predicttable text,columnname text[],modelinfo double precision[],clusternumber integer)
 RETURNS integer AS
$BODY$
DECLARE
	sql 	text;
	alpha 	double precision[];
	mu 	double precision[];
	sigma	double precision[];
	columnsize integer;
	sum 	text;
	max 	text;
	casewhen text;
	updatesql text;
	resultsql text;
	i 	integer;
	j	integer;
	k	integer;

BEGIN
	
	columnsize:=alpine_miner_get_array_count(columnname);


	for i in 1..clusternumber loop
		alpha[i]:=modelinfo[i];
		for j in 1..columnsize loop
			mu[(i-1)*columnsize+j]:=modelinfo[clusternumber+(i-1)*columnsize+j];
			sigma[(i-1)*columnsize+j]:=modelinfo[clusternumber+clusternumber*columnsize+(i-1)*columnsize+j];
		end loop;
	end loop;
	
	
	for i in 1..clusternumber loop
		if i = 1
		then
			max:='greatest("C(alpine_miner_emClust'||i||')"';
			sum:='("C(alpine_miner_emClust'||i||')"';
			updatesql:=' set "C(alpine_miner_emClust'||i||')"="C(alpine_miner_emClust'||i||')"/alpine_em_sum ';
		else 
			max:=max||',"C(alpine_miner_emClust'||i||')"';
			sum:=sum||'+"C(alpine_miner_emClust'||i||')"';
			updatesql:=updatesql||',"C(alpine_miner_emClust'||i||')"="C(alpine_miner_emClust'||i||')"/alpine_em_sum ';
		end if;
	end loop;

	
	
	max:=max||')';
	sum:=sum||') as alpine_em_sum';
	casewhen:=' case ';
	for i in 1..clusternumber loop
		casewhen:=casewhen||' when "C(alpine_miner_emClust'||i||')"='||max||' then  '||i||' ';

	end loop;
	casewhen := casewhen ||' end ';
	sql:=' create  table '||outputtable||' as select * , '||sum||', '||casewhen||' alpine_em_cluster from ( select  *';
	for i in 1..  clusternumber loop
		 
		sql:=sql||' , alpine_miner_em_getp(array['||array_to_string(columnname,',')||'] , array['||array_to_string(mu[((i-1)*columnsize+1):(i*columnsize)],',')||'] , 
		array['||array_to_string(sigma[((i-1)*columnsize+1):(i*columnsize)],',')||'] ,'||alpha[i]||' ) as "C(alpine_miner_emClust'||i||')"' ;
	end loop;		
	sql:=sql||'   from '||predicttable||') as foo';
	 
	 execute sql;
	 sql:=' update '||outputtable|| updatesql;
	 execute sql;
	 sql:=' alter table '||outputtable||' drop column alpine_em_sum';
	 execute sql;
 return 1;
end;
$BODY$
LANGUAGE plpgsql VOLATILE;