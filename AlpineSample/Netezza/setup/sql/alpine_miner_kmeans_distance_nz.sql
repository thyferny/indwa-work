CREATE OR REPLACE procedure alpine_miner_kmeans_distance(integer,text,integer,integer)
  RETURNS text 
  language nzplsql
  AS
  BEGIN_PROC
DECLARE
	 caculate_array text:='';
	 sql text:='';
	 temp1 text:='';
	 temp2 text:='';
	 temp3 text:='';
	 temp4 text:='';
	 i integer;
	 j integer;
	 m integer;
	 column_name VARRAY(1600) OF varchar(128);
   column_count integer := 0;
   temptablerecord  record;
   value text:='';
   
  distancetype ALIAS FOR $1;
  temptablename ALIAS FOR $2;
  column_number ALIAS FOR $3;
  k ALIAS FOR $4;

BEGIN 
	sql := 'select value from '||temptablename||' order by id ';
column_count := 0;
for temptablerecord in execute sql loop
       column_count := column_count + 1;
       column_name(column_count) := temptablerecord.value;
end loop;

	
	if distancetype=1 --EuclideanDistance
	then 
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'(';
		while j<column_number loop
		value:=column_name(j);
		caculate_array:=caculate_array||'(x."'||value||'"::float-y."k'||i||value||'")*(x."'||value||'"::float-y."k'||i||value||'")+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		if i=k then caculate_array:=caculate_array||'(x."'||value||'"::float-y."k'||i||value||'")*(x."'||value||'"::float-y."k'||i||value||'")) as d'||(i-1);
		else caculate_array:=caculate_array||'(x."'||value||'"::float-y."k'||i||value||'")*(x."'||value||'"::float-y."k'||i||value||'")) as d'||(i-1)||',';
		end if;
		i:=i+1;
		end loop;
	elsif distancetype=2	--BregmanDivergence.GeneralizedIDivergence
	then
		i:=1;
		while i<(k + 1) loop
		j:=1;
		temp1:='(';
		temp2:='(';
			while j<column_number loop
			value:=column_name(j);
			temp1:=temp1||'(y."k'||i||value||'"*ln(y."k'||i||value||'"::float/x."'||value||'"))+';
			temp2:=temp2||'(y."k'||i||value||'"::float-x."'||value||'")+';
			j:=j+1;
			end loop; 
		value:=column_name(j);
		temp1:=temp1||'(y."k'||i||value||'"*ln(y."k'||i||value||'"::float/x."'||value||'")))';
		temp2:=temp2||'(y."k'||i||value||'"::float-x."'||value||'"))';
		temp3:='('||temp1||'-'||temp2||')';
		if i=k then temp3:=temp3||'as d'||(i-1);
		else temp3:=temp3||'as d'||(i-1)||',';		
		end if;
		caculate_array:=caculate_array||temp3;
		i:=i+1;
		end loop;
	elsif distancetype=3      --BregmanDivergence.KLDivergence
	then
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'(';
		while j<column_number loop
		value:=column_name(j);
		caculate_array:=caculate_array||'(y."k'||i||value||'"*(log((y."k'||i||value||'"::float/x."'||value||'")::numeric)/log(2.0)))+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		if i=k then caculate_array:=caculate_array||'(y."k'||i||value||'"*(log((y."k'||i||value||'"::float/x."'||value||'")::numeric)/log(2.0)))) as d'||(i-1);
		else caculate_array:=caculate_array||'(y."k'||i||value||'"*(log((y."k'||i||value||'"::float/x."'||value||'")::numeric)/log(2.0)))) as d'||(i-1)||',';
		end if;
		i:=i+1;
		end loop;
	elsif distancetype=4      --CamberraNumericalDistance
	then
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'(';
		while j<column_number loop
		value:=column_name(j);
		caculate_array:=caculate_array||'(abs((x."'||value||'"::float)-(y."k'||i||value||'"))/abs((x."'||value||'"::float)+(y."k'||i||value||'")))+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		if i=k then caculate_array:=caculate_array||'(abs((x."'||value||'"::float)-(y."k'||i||value||'"))/abs((x."'||value||'"::float)+(y."k'||i||value||'")))) as d'||(i-1);
		else caculate_array:=caculate_array||'(abs((x."'||value||'"::float)-(y."k'||i||value||'"))/abs((x."'||value||'"::float)+(y."k'||i||value||'")))) as d'||(i-1)||',';
		end if;
		i:=i+1;
		end loop;
	elsif distancetype=5      --ManhattanDistance
	then	
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'(';
		while j<column_number loop
		value:=column_name(j);
		caculate_array:=caculate_array||'abs((x."'||value||'"::float)-(y."k'||i||value||'"))+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		if i=k then caculate_array:=caculate_array||'abs((x."'||value||'"::float)-(y."k'||i||value||'"))) as d'||(i-1);
		else caculate_array:=caculate_array||'abs((x."'||value||'"::float)-(y."k'||i||value||'"))) as d'||(i-1)||',';
		end if;
		i:=i+1;
		end loop;
	
	elsif distancetype=6      --CosineSimilarityDistance
	then	
		i:=1;
		while i<(k + 1) loop
		j:=1;
		temp1:='(';
		temp2:='(';
		temp3:='(';
		while j<column_number loop
		value:=column_name(j);
		temp1:=temp1||'(x."'||value||'"::float*y."k'||i||value||'")+';
		temp2:=temp2||'(x."'||value||'"::float*x."'||value||'")+';
		temp3:=temp3||'(y."k'||i||value||'"::float*y."k'||i||value||'")+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		temp1:=temp1||'(x."'||value||'"::float*y."k'||i||value||'"))';
		temp2:=temp2||'(x."'||value||'"::float*x."'||value||'"))';
		temp3:=temp3||'(y."k'||i||value||'"::float*y."k'||i||value||'"))';
		if i=k then 
		temp4:='acos(case when ('||temp1||'/(sqrt('||temp2||')*sqrt('||temp3||')))>1 then 1 when ('||temp1||'/(sqrt('||temp2||')*sqrt('||temp3||')))<-1 then -1 else ('||temp1||'/(sqrt('||temp2||')*sqrt('||temp3||'))) end ) as d'||(i-1);--
		else
		temp4:='acos(case when ('||temp1||'/(sqrt('||temp2||')*sqrt('||temp3||')))>1 then 1 when ('||temp1||'/(sqrt('||temp2||')*sqrt('||temp3||')))<-1 then -1 else ('||temp1||'/(sqrt('||temp2||')*sqrt('||temp3||'))) end ) as d'||(i-1)||',';--acos
		end if;
		caculate_array:=caculate_array||temp4;
		i:=i+1;
		end loop;
		
	elsif distancetype=7	--DiceNumericalSimilarityDistance
	then
		i:=1;
		while i<(k + 1) loop
		j:=1;
		temp1:='(';
		temp2:='(';
		temp3:='(';
		while j<column_number loop
		value:=column_name(j);
		temp1:=temp1||'(x."'||value||'"::float)+';
		temp2:=temp2||'(y."k'||i||value||'"::float)+';
		temp3:=temp3||'(x."'||value||'"::float*y."k'||i||value||'")+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		temp1:=temp1||'(x."'||value||'"))';
		temp2:=temp2||'(y."k'||i||value||'"))';
		temp3:=temp3||'(x."'||value||'"*y."k'||i||value||'"))';
		if i=k then 
		temp4:='(-2*'||temp3||'/('||temp1||'+'||temp2||')) as d'||(i-1);
		else
		temp4:='(-2*'||temp3||'/('||temp1||'+'||temp2||')) as d'||(i-1)||',';
		end if;
		caculate_array:=caculate_array||temp4;
		i:=i+1;
		end loop;
	elsif distancetype=8	--InnerProductSimilarityDistance
	then
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'-(';
		while j<column_number loop
		value:=column_name(j);
		caculate_array:=caculate_array||'(x."'||value||'"::float*y."k'||i||value||'")+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		if i=k then caculate_array:=caculate_array||'(x."'||value||'"::float*y."k'||i||value||'")) as d'||(i-1);
		else caculate_array:=caculate_array||'(x."'||value||'"::float*y."k'||i||value||'")) as d'||(i-1)||',';
		end if;
		caculate_array:=caculate_array||temp4;
		i:=i+1;
		end loop;
	elsif distancetype=9	--JaccardNumericalSimilarityDistance
	then
		i:=1;
		while i<(k + 1) loop
		j:=1;
		temp1:='(';
		temp2:='(';
		temp3:='(';
		while j<column_number loop
		value:=column_name(j);
		temp1:=temp1||'(x."'||value||'"::float)+';
		temp2:=temp2||'(y."k'||i||value||'"::float)+';
		temp3:=temp3||'(x."'||value||'"::float*y."k'||i||value||'")+';
		j:=j+1;
		end loop; 
		value:=column_name(j);
		temp1:=temp1||'(x."'||value||'"))';
		temp2:=temp2||'(y."k'||i||value||'"))';
		temp3:=temp3||'(x."'||value||'"*y."k'||i||value||'"))';
		if i=k then 
		temp4:='(-'||temp3||'/('||temp1||'+'||temp2||'-'||temp3||')) as d'||(i-1);
		else
		temp4:='(-'||temp3||'/('||temp1||'+'||temp2||'-'||temp3||')) as d'||(i-1)||',';
		end if;
		caculate_array:=caculate_array||temp4;
		i:=i+1;
		end loop;
	else
	end if;

RETURN caculate_array;
END;
END_PROC;
