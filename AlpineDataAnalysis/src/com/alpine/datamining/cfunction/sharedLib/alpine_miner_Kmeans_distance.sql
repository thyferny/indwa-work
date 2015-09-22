-- Function: Alpine_Miner_Kmeans_distance(integer, text[], integer, integer)

-- DROP FUNCTION Alpine_Miner_Kmeans_distance(integer, text[], integer, integer);

CREATE OR REPLACE FUNCTION alpine_miner_Kmeans_distance(distancetype integer, column_name text[], column_number integer, k integer)
  RETURNS text AS
$BODY$

DECLARE
	 caculate_array text:='';
	 temp1 text:='';
	 temp2 text:='';
	 temp3 text:='';
	 temp4 text:='';
	 i integer;
	 j integer;
	 m integer;

BEGIN
	if distancetype=1 --EuclideanDistance
	then 
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'(';
		while j<column_number loop
		caculate_array:=caculate_array||'(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")*(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")+';
		j:=j+1;
		end loop; 
		if i=k then caculate_array:=caculate_array||'(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")*(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")) as d'||(i-1);
		else caculate_array:=caculate_array||'(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")*(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")) as d'||(i-1)||',';
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
			temp1:=temp1||'(y."k'||i||column_name[j]||'"*ln(y."k'||i||column_name[j]||'"::float/x."'||column_name[j]||'"))+';
			temp2:=temp2||'(y."k'||i||column_name[j]||'"::float-x."'||column_name[j]||'")+';
			j:=j+1;
			end loop; 
		temp1:=temp1||'(y."k'||i||column_name[j]||'"*ln(y."k'||i||column_name[j]||'"::float/x."'||column_name[j]||'")))';
		temp2:=temp2||'(y."k'||i||column_name[j]||'"::float-x."'||column_name[j]||'"))';
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
		caculate_array:=caculate_array||'(y."k'||i||column_name[j]||'"*log(2.0,(y."k'||i||column_name[j]||'"::float/x."'||column_name[j]||'")::numeric))+';
		j:=j+1;
		end loop; 
		if i=k then caculate_array:=caculate_array||'(y."k'||i||column_name[j]||'"*log(2.0,(y."k'||i||column_name[j]||'"::float/x."'||column_name[j]||'")::numeric))) as d'||(i-1);

		else caculate_array:=caculate_array||'(y."k'||i||column_name[j]||'"*log(2.0,(y."k'||i||column_name[j]||'"::float/x."'||column_name[j]||'")::numeric))) as d'||(i-1)||',';
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
		caculate_array:=caculate_array||'(abs((x."'||column_name[j]||'"::float)-(y."k'||i||column_name[j]||'"))/abs((x."'||column_name[j]||'"::float)+(y."k'||i||column_name[j]||'")))+';
		j:=j+1;
		end loop; 
		if i=k then caculate_array:=caculate_array||'(abs((x."'||column_name[j]||'"::float)-(y."k'||i||column_name[j]||'"))/abs((x."'||column_name[j]||'"::float)+(y."k'||i||column_name[j]||'")))) as d'||(i-1);
		else caculate_array:=caculate_array||'(abs((x."'||column_name[j]||'"::float)-(y."k'||i||column_name[j]||'"))/abs((x."'||column_name[j]||'"::float)+(y."k'||i||column_name[j]||'")))) as d'||(i-1)||',';
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
		caculate_array:=caculate_array||'abs((x."'||column_name[j]||'"::float)-(y."k'||i||column_name[j]||'"))+';
		j:=j+1;
		end loop; 
		if i=k then caculate_array:=caculate_array||'abs((x."'||column_name[j]||'"::float)-(y."k'||i||column_name[j]||'"))) as d'||(i-1);
		else caculate_array:=caculate_array||'abs((x."'||column_name[j]||'"::float)-(y."k'||i||column_name[j]||'"))) as d'||(i-1)||',';
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
		temp1:=temp1||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'")+';
		temp2:=temp2||'(x."'||column_name[j]||'"::float*x."'||column_name[j]||'")+';
		temp3:=temp3||'(y."k'||i||column_name[j]||'"::float*y."k'||i||column_name[j]||'")+';
		j:=j+1;
		end loop; 
		temp1:=temp1||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'"))';
		temp2:=temp2||'(x."'||column_name[j]||'"::float*x."'||column_name[j]||'"))';
		temp3:=temp3||'(y."k'||i||column_name[j]||'"::float*y."k'||i||column_name[j]||'"))';
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
		temp1:=temp1||'(x."'||column_name[j]||'"::float)+';
		temp2:=temp2||'(y."k'||i||column_name[j]||'"::float)+';
		temp3:=temp3||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'")+';
		j:=j+1;
		end loop; 
		temp1:=temp1||'(x."'||column_name[j]||'"))';
		temp2:=temp2||'(y."k'||i||column_name[j]||'"))';
		temp3:=temp3||'(x."'||column_name[j]||'"*y."k'||i||column_name[j]||'"))';
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
		caculate_array:=caculate_array||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'")+';
		j:=j+1;
		end loop; 
		if i=k then caculate_array:=caculate_array||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'")) as d'||(i-1);
		else caculate_array:=caculate_array||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'")) as d'||(i-1)||',';
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
		temp1:=temp1||'(x."'||column_name[j]||'"::float)+';
		temp2:=temp2||'(y."k'||i||column_name[j]||'"::float)+';
		temp3:=temp3||'(x."'||column_name[j]||'"::float*y."k'||i||column_name[j]||'")+';
		j:=j+1;
		end loop; 
		temp1:=temp1||'(x."'||column_name[j]||'"))';
		temp2:=temp2||'(y."k'||i||column_name[j]||'"))';
		temp3:=temp3||'(x."'||column_name[j]||'"*y."k'||i||column_name[j]||'"))';
		if i=k then 
		temp4:='(-'||temp3||'/('||temp1||'+'||temp2||'-'||temp3||')) as d'||(i-1);
		else
		temp4:='(-'||temp3||'/('||temp1||'+'||temp2||'-'||temp3||')) as d'||(i-1)||',';
		end if;
		caculate_array:=caculate_array||temp4;
		i:=i+1;
		end loop;
/*	elseif distancetype=10	--ChebychevDistance 
	--select case when a1>a2 and a1>a3 and a1>a4 then a1 when a2>a3 and a2>a4 then a2 when a3>a4 then a3 else a4 end as d0
	then 
		i:=1;
		while i<(k + 1) loop
		j:=1;
		caculate_array:=caculate_array||'case ';
			while j<column_number loop
			m:=1;
			caculate_array:=caculate_array||' when ';
				while m<column_number-j loop
					caculate_array:=caculate_array||'abs(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")>abs(x."'||column_name[j+m]||'"::float-y."k'||i||column_name[j+m]||'") and ';
					m:=m+1;
				end loop;
				caculate_array:=caculate_array||'abs(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'")>abs(x."'||column_name[j+m]||'"::float-y."k'||i||column_name[j+m]||'") then abs(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'") ';
			j:=j+1;
			end loop; 
		if i=k then caculate_array:=caculate_array||' else abs(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'") end as d'||(i-1);
		else caculate_array:=caculate_array||' else abs(x."'||column_name[j]||'"::float-y."k'||i||column_name[j]||'") end as d'||(i-1)||',';
		end if;
		i:=i+1;
		end loop;*/
	else
	end if;

raise notice 'caculate_array:%',caculate_array;
RETURN caculate_array;
 
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;
