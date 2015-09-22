drop function if exists lr_ca_he(beta float[],beta_count integer,columns float[],column_count integer, add_intercept boolean, weight float);
create or replace function lr_ca_he(beta float[],beta_count integer,columns float[],column_count integer, add_intercept boolean, weight float)
returns float[]  as 
$body$

declare

i integer;
j integer;
ind integer;
gx float := 0.0;
pi float;
x float;
y float;
matrix float[];

begin

	i := 1;
	while (i <= beta_count and i <= column_count) loop
		gx := gx + beta[i] * columns[i];
		i := i + 1;
	end loop;
	if (add_intercept) then
		gx := gx + beta[beta_count];
	end if;

	pi = 1.0/(1.0 + exp(-1.0*gx));

	ind := 1;
	i := 1;
	while (i <= beta_count) loop
		if (i = beta_count) then
			if (add_intercept)
			then
				x := 1.0;
			else
				x := 0.0;
			end if;
		else
			x := columns[i];
		end if;
		j := i;
		--j := 1;
		while(j <= beta_count) loop
			if (j = beta_count)
			then
				if (add_intercept)
				then
					y := 1.0;
				else
					y := 0.0;
				end if;
			else
				y := columns[j];
			end if;	
			matrix[ind] = -x*y*weight*pi*(1.0 - pi);
			ind := ind + 1;
			j := j + 1;
		end loop;
		i := i + 1;
	end loop;
	return matrix;
end;
$body$
language 'plpgsql' immutable;
