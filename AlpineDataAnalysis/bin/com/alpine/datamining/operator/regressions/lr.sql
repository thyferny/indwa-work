drop function if exists logistic_regression(schema_name text, table_name text, column_names text[], column_count integer, label text, max_generations integer, generations_without_improval integer, init_population_size integer, tournament_fraction float, keep_best boolean, random_seed integer, good_value text, crossover_prob float);
------parameters-----
--schema_name: schema name
--table_name: table name
--column_names: array of column names
--column_count: count of column names
--label: label name
--max_generations: max generations when reach to exit loop
--generations_without_improval: max generations without improval when reach to exit loop
--init_population_size: initial population size
--tournament_fraction: tournament fraction wich value is between 0 and 1
--keep_best: whether to remain best_ever_individual in tournament
--random_seed: seed for random
--good_value: good value for analysis
--crossover_prob crossover probability
create or replace function logistic_regression(schema_name text, table_name text, column_names text[], column_count integer, label text, max_generations integer, generations_without_improval integer, init_population_size integer, tournament_fraction float, keep_best boolean, random_seed integer, good_value text, crossover_prob float)
returns float[] as
$body$
declare

-- global var
-- population of individuals
population float[];
individual_size integer;
population_size integer;
population_index integer;
-- here individual is beta
individual float[];
individual_index integer;
fitness float[];
new_fitness float[];
sigma float [];
min float[];
max float[];
last_improvement integer;
generations integer;
float_result float;
sum text;
exp text;
sql text;
eta text;
--pi text;
x text;
y text;
hessian float[];
float_array_result float[];
i integer;
j integer;
k integer;

-- for best ever individual
best_ever_individual float[];
best_ever_fitness float;

-- for evaluate
evaluate_fitness_count integer;
evaluate_fitness_index integer[];


--for tournament
tournament_size integer;
tournament_fraction float;
new_generation float[];
new_generation_size integer;
winner float[];
winner_index integer;
current_index integer;

--for cross over
mating_pool float [];
mating_pool_size integer;
individual_p1 float[];
individual_p1_index integer;
individual_p2 float[];
individual_p2_index integer;
tournament_prob float;
dummy float;

-- for gaussian random
have_next_next_gaussian boolean := false;
gaussian_random float;
next_next_gaussian float;
s float;
v1 float;
v2 float;
multiplier float;

-- variance adaption
improvement_result_list boolean[];
wait_interval integer := 2;
interval_size integer  ;
success_count integer := 0;
improvement_result_list_count integer := 0;
factor float := 0.85;

update_column_name text;

begin

perform setseed(random_seed);

individual_size := column_count + 1;

interval_size := individual_size;

-- init min[] max[];
i := 1;
while (i <= individual_size) loop
	min[i] := -1.0;
	max[i] := 1.0;
	i := i + 1;
end loop;

--init pupulation[] by random
i := 1;
while(i <= init_population_size) loop
	j := 1;
	while (j <= individual_size) loop
		-- random number between -1.0 and 1.0
		population[(i - 1) * individual_size + j] := 2.0 * random() - 1.0;
		j := j + 1;
	end loop;
	i := i + 1;
end loop;
population_size := init_population_size;

--init sigma[]
i := 1;
while (i <= individual_size) loop
	sigma[i] := 0.02; -- compute as (max[i] - min[i]) / 100.0;
	i := i + 1;
end loop;

generations := 1;
last_improvement := 1;

while (true) loop

	if generations >= max_generations
	then
		exit;
	end if;

	if (generations - last_improvement) > generations_without_improval
	then
		exit;
	end if;

	--evaluate
	i := 1;
	evaluate_fitness_count := 0;
	evaluate_fitness_index := null;
	float_array_result := null;
	sum := '';
	-- generate sum sql;
	while (i <= population_size) loop
		if fitness[i] is null
		then
			evaluate_fitness_count := evaluate_fitness_count + 1;
			evaluate_fitness_index[evaluate_fitness_count] := i;

			individual := population[(i-1)*individual_size + 1 : i*individual_size];

			exp := '1.0/(1.0+exp(-(';
			j := 1;
			-- beta_1 to beta_column_count
			while(j <= column_count) loop
				exp := exp||column_names[j]||'*'||individual[j]||'+';
				j := j + 1;
			end loop;

			-- beta_0
			exp := exp||individual[individual_size]||')))';
			if evaluate_fitness_count > 1
			then
				sum := sum||',';
			end if ;
			sum := sum||'sum('||' (case when '||label||'!='''||good_value||''' then ln(1.0 - '||exp||' ) else ln('||exp||') end)) ';
		end if;
		i := i + 1;
	end loop;
	if evaluate_fitness_count > 0
	then
		sql := 'select array['||sum||'] from '||schema_name||'.'||table_name;
		-- sql example: sql:select array[sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*6.67776336838675+a2*9+a3*5.71973480511639+-0.661232769228869))) ) else ln(1.0/(1.0+exp(-(a1*6.67776336838675+a2*9+a3*5.71973480511639+-0.661232769228869)))) end)) ,sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*5.44343289819335+a2*10+a3*0.120163394136853+-1))) ) else ln(1.0/(1.0+exp(-(a1*5.44343289819335+a2*10+a3*0.120163394136853+-1)))) end)) ,sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*7+a2*10+a3*6+-1))) ) else ln(1.0/(1.0+exp(-(a1*7+a2*10+a3*6+-1)))) end)) ,sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*4.29920015873097+a2*8.34983955853562+a3*-1+0.556563796661363))) ) else ln(1.0/(1.0+exp(-(a1*4.29920015873097+a2*8.34983955853562+a3*-1+0.556563796661363)))) end)) ,sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*0.208645904136783+a2*8.1416298412664+a3*6+0.948377607302837))) ) else ln(1.0/(1.0+exp(-(a1*0.208645904136783+a2*8.1416298412664+a3*6+0.948377607302837)))) end)) ,sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*6.16449940294267+a2*8.52088659608096+a3*3.30813366022611+0.137068294505116))) ) else ln(1.0/(1.0+exp(-(a1*6.16449940294267+a2*8.52088659608096+a3*3.30813366022611+0.137068294505116)))) end)) ,sum( (case when a61!='Mine' then ln(1.0 - 1.0/(1.0+exp(-(a1*7+a2*9.89237044318071+a3*-1+0.532357084581349))) ) else ln(1.0/(1.0+exp(-(a1*7+a2*9.89237044318071+a3*-1+0.532357084581349)))) end)) ] from public.sonarshort
		execute sql into float_array_result;
		if float_array_result is not null
		then
			i := 1;
			while ( i <= evaluate_fitness_count ) loop
				-- get index of fitness
				j := evaluate_fitness_index[i];
				-- set fitness of fitness[j]
				fitness[j] := float_array_result[i];
				-- compare fitness[j] and best_ever_fitness to get the best fitness
				if (fitness[j] is not null and (best_ever_fitness is null or fitness[j] > best_ever_fitness))
				then
					best_ever_fitness := fitness[j];
					best_ever_individual := population[(j-1)*individual_size + 1 : j*individual_size];
					last_improvement := generations;
				end if;
				i := i + 1;
			end loop;
		end if;
	end if;

	-- reset min max array by best_ever_individual
	i := 1;
	while ( i <= individual_size) loop
		if min[i] = best_ever_individual[i]
		then
			min[i] := min[i] - 1;
		end if;
		if max[i] = best_ever_individual[i]
		then
			max[i] := max[i] + 1;
		end if;
		i := i + 1;
	end loop;

	-- tournament
	if population_size > 0
	then
		new_generation := null;
		new_fitness := null;
		new_generation_size := 0;
		-- set tournament_size
		tournament_size := greatest(round(population_size * tournament_fraction), 1);
		if (keep_best)
		then
			-- add best_ever_individual to new_generation if keep_best
			new_generation := array_cat(new_generation,best_ever_individual);
			new_generation_size := new_generation_size + 1;
			new_fitness[1] := best_ever_fitness;
		end if;

		-- get init_population_size winners from population by random tournament
		while (new_generation_size < init_population_size) loop
			winner := null;
			winner_index := 1;
			i := 1;
			while (i <= tournament_size) loop
				-- random index
				current_index := trunc(random() * population_size) + 1;
				individual := population[(current_index - 1) * individual_size+1:current_index*individual_size];
				-- get winner
				if (winner is null or fitness[current_index] > fitness[winner_index])
				then
					winner := individual;
					winner_index := current_index;
				end if;
				i := i + 1;
			end loop;
			-- add winner to new generation
			new_generation := array_cat(new_generation, winner);
			new_generation_size := new_generation_size + 1;
			new_fitness := array_append(new_fitness, fitness[winner_index]);
		end loop;
	end if;
	population := new_generation;
	fitness := new_fitness;
	population_size := new_generation_size;

	-- crossover
	if population_size >= 2
	then
		new_generation := null;
		new_generation_size := 0;
		mating_pool := population;
		mating_pool_size := population_size;
		while (mating_pool_size > 1) loop
			-- get and remove individual_p1 and individual_p2 from mating pool
			i := trunc(random()*mating_pool_size) + 1;
			individual_p1 := mating_pool[(i-1)*individual_size + 1:i*individual_size];
			mating_pool := array_cat(mating_pool[0:(i-1)*individual_size], mating_pool[i*individual_size+1:mating_pool_size*individual_size]);
			mating_pool_size := mating_pool_size - 1;
			i := trunc(random()*mating_pool_size) + 1;
			individual_p2 := mating_pool[(i-1)*individual_size + 1:i*individual_size];
			mating_pool := array_cat(mating_pool[0:(i-1)*individual_size], mating_pool[i*individual_size+1:mating_pool_size*individual_size]);
			mating_pool_size := mating_pool_size - 1;
			-- random cross over
			if (random() < crossover_prob)
			then
				j := 1;
				while (j <= individual_size) loop
					if ( random() > 0.5)
					then
						-- cross over here
						dummy := individual_p1[j];
						individual_p1[j] := individual_p2[j];
						individual_p2[j] := dummy;
					end if;
					j := j + 1;
				end loop;
			end if;
			new_generation := array_cat(new_generation,individual_p1);
			new_generation := array_cat(new_generation,individual_p2);
			new_generation_size := new_generation_size + 2;
		end loop;
		population := array_cat(population, new_generation);
		population_size := population_size + new_generation_size;
	end if;

	-- gassian mutation
	new_generation := null;
	new_generation_size := 0;
	i := 1;
	while (i <= population_size) loop
		individual := population[(i -1) * individual_size + 1: i * individual_size];
		j := 1;
		while( j <= individual_size) loop

			-- get gaussian distribution random number;
			if (have_next_next_gaussian)
			then
				have_next_next_gaussian := false;
				gaussian_random := next_next_gaussian;
			else
				s := 0;
				while ( s >= 1 or s = 0) loop
					v1 := 2 * random() - 1;
					v2 := 2 * random() - 1;
					s := v1 * v1 + v2 * v2;
				end loop;
				multiplier := sqrt( -2 * ln(s)/s);
				next_next_gaussian := v2 * multiplier;
				have_next_next_gaussian := true;
				gaussian_random := v1 * multiplier;
			end if;

			-- increase individual[j] by gaussian_random * sigma[j]
			individual[j] := individual[j] + gaussian_random * sigma[j];

			-- reset individual[j] if individual[j] < min[j] or individual[j] > max[j]
	                if (individual[j] < min[j])
			then
        	        	individual[j] := min[j];
			end if;
                	if (individual[j] > max[j])
			then
                		individual[j] := max[j];
			end if;
			j := j + 1;
		end loop;
		new_generation := array_cat(new_generation, individual);
		new_generation_size := new_generation_size + 1;
		i := i + 1;
	end loop;
	-- populatin add all new_individual
	population := array_cat(population, new_generation);
	population_size := population_size + new_generation_size;

	-- variance adaption
	if (generations = last_improvement) 
	then
		improvement_result_list := array_append(improvement_result_list, true);
	else
		improvement_result_list := array_append(improvement_result_list, false);
	end if;

	improvement_result_list_count := improvement_result_list_count + 1;
	-- 	
	if (generations >= wait_interval * interval_size) 
	then
		-- remove head of improvement_result_list to keep improvement_result_list_size is wait_interval * interval_size
		improvement_result_list := improvement_result_list[2:improvement_result_list_count];
		improvement_result_list_count := improvement_result_list_count - 1;
		if ((generations % interval_size) = 0) 
		then
			success_count := 0;
			i := 1;
			-- get success_count through improvement_result_list
			while (i <= improvement_result_list_count) loop
				if (improvement_result_list[i])
				then
					success_count := success_count + 1;
				end if;
				i := i + 1;
			end loop;
			j := 1;

			-- reset sigma by the 1/5-Rule for dynamic parameter adaption of the variance of Gaussian muatation
			if ((success_count::float / (wait_interval * interval_size)::float) < 0.2)
			then 
				while(j <= individual_size) loop
					sigma[j] := sigma[j] * factor;
					j := j + 1;
				end loop;
			else
				while(j <= individual_size) loop
					sigma[j] := sigma[j] / factor;
					j := j + 1;
				end loop;
			end if;
		end if;
	end if;
	generations:= generations + 1;
end loop;
--return
return best_ever_individual;

end;
$body$
language 'plpgsql';