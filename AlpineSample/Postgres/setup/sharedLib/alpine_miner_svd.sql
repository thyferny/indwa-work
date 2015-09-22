CREATE OR REPLACE FUNCTION alpine_miner_svd(
input_matrix text,
col_name text,
row_name text,
value text,
num_features int,
ORIGINAL_STEP float , 
SPEEDUP_CONST float,
FAST_SPEEDUP_CONST float,
SLOWDOWN_CONST float,
NUM_ITERATIONS int,
MIN_NUM_ITERATIONS int,
MIN_IMPROVEMENT float,
IMPROVEMENT_REACHED int,
INIT_VALUE float,
EARLY_TEMINATE int,
matrix_u text,
matrix_v text,
drop_u int,
drop_v int
)
RETURNS int AS
$BODY$
DECLARE 
    ORIGINAL_STEP_ADJUST float := 0; 
    error float :=0;
    old_error float :=0;
    keep_ind int := 1;
    SD_ind int := 1;

    feature_x float := 0;
    feature_y float := 0;
    i int := 0;
    j int := 0;
    cells int := 0;
    sql text := '';
    step float := 0;
    imp_reached int := 0;
BEGIN

    -- Find sizes of the input and number of elements in the input
    execute 'SELECT count(distinct ' || col_name || ') AS c FROM ' || input_matrix || ' where ' || col_name || ' is not null' into feature_x; 
    execute 'SELECT count(distinct ' || row_name || ') AS c FROM ' || input_matrix || ' where ' || row_name || ' is not null'into feature_y; 
    execute 'SELECT count(*) AS c FROM ' || input_matrix into cells; 
    
    ORIGINAL_STEP_ADJUST := ORIGINAL_STEP/(feature_x+feature_y)/(cells);

    sql := '';
    if(drop_u = 1) then
        sql := 'DROP TABLE IF EXISTS '||matrix_u||';';
    end if;
    sql := sql || 'CREATE TABLE '||matrix_u||' as select 1 as alpine_feature , ' || col_name || ', ' || value || ' from ' || input_matrix || ' where 0 = 1 ;';
    if(drop_v = 1) then
        sql := sql||'DROP TABLE IF EXISTS '|| matrix_v||';';
    end if;
    sql := sql || 'CREATE TABLE '||matrix_v||' as select ' || row_name || ' , 1 as alpine_feature, ' || value || ' from ' || input_matrix || ' where 0 =  1 ;
    DROP TABLE IF EXISTS e1;
    CREATE TEMP TABLE e1   as select ' || row_name || ' as row_num , ' || col_name || ' as col_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1 ;
    DROP TABLE IF EXISTS e2;
    CREATE TEMP TABLE e2   as select ' || row_name || ' as row_num , ' || col_name || ' as col_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1 ;
    DROP TABLE IF EXISTS S1;
    CREATE TEMP TABLE S1   as select ' || col_name || ' as col_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1
     ;
    DROP TABLE IF EXISTS S2;
    CREATE TEMP TABLE S2   as select ' || col_name || ' as col_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1
     ;
    DROP TABLE IF EXISTS D1;
    CREATE TEMP TABLE D1   as select ' || row_name || ' as row_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1
     ;
    DROP TABLE IF EXISTS D2;
    CREATE TEMP TABLE D2  as select ' || row_name || ' as row_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1
     ;
    DROP TABLE IF EXISTS e;
    CREATE TEMP TABLE e   as select ' || row_name || ' as row_num , ' || col_name || ' as col_num , ' || value || ' as val from ' || input_matrix || ' where 0 = 1 ;';

    --raise notice '0';
    execute sql;
    --raise notice '1';
    execute 'INSERT INTO e1 SELECT ' || row_name || ', ' || col_name || ', ' || value || ' FROM ' || input_matrix ;
    j := 1;
    while (j <=  num_features) loop
        
    	--raise notice 'j:%', j;
        sql := 'TRUNCATE TABLE S1;
        TRUNCATE TABLE S2;
        TRUNCATE TABLE D1;
        TRUNCATE TABLE D2;';
        execute sql;
        
        execute 'INSERT INTO S1 SELECT distinct ' || col_name || ', '||(INIT_VALUE)||' FROM ' || input_matrix || ' where ' || col_name || ' is not null';
        execute 'INSERT INTO D1 SELECT distinct ' || row_name || ', '||(INIT_VALUE)||' FROM ' || input_matrix || ' where ' || row_name || ' is not null';
        SD_ind := 1;
        i := 0;
        step := ORIGINAL_STEP_ADJUST;
        imp_reached := 0;
        
        while ( true ) loop

            i := i + 1;
    	    --raise notice 'i:%', i;

            sql := '   TRUNCATE TABLE e'; 
            execute sql;
        
            execute 'INSERT INTO e SELECT a.row_num, a.col_num, a.val-b.val*c.val FROM e'||(keep_ind)||' AS a, S'||(SD_ind)||' AS b, D'||(SD_ind)||' AS c WHERE a.row_num=c.row_num AND a.col_num=b.col_num';
            old_error := error;
            execute 'SELECT sqrt(sum(val*val)) AS c FROM e' into error;
            if(((abs(error - old_error) < MIN_IMPROVEMENT) and (i >= MIN_NUM_ITERATIONS) and ((error < MIN_IMPROVEMENT) or (not (IMPROVEMENT_REACHED = 1)) or (imp_reached = 1))) or (NUM_ITERATIONS < i)) then
                exit;
            end if;
               
            if((abs(error - old_error) >= MIN_IMPROVEMENT) and (old_error > 0)) then
                   imp_reached := 1;
            end if;
               
            if((error > old_error) and (old_error != 0)) then
                error := 0;
                step := step*SLOWDOWN_CONST;
                SD_ind := SD_ind%2+1;
            else
                if(sqrt((error - old_error)*(error - old_error)) < .1*MIN_IMPROVEMENT) then
                    step := step*FAST_SPEEDUP_CONST;
                else
                    step := step*SPEEDUP_CONST;
                end if;
                   
                execute 'TRUNCATE TABLE S'||(SD_ind%2+1);
                execute 'TRUNCATE TABLE D'||(SD_ind%2+1);
            
                execute 'INSERT INTO S'||(SD_ind%2+1)||' SELECT a.col_num, avg(b.val)+sum(a.val*c.val)*'||(step)||' FROM e as a, S'||(SD_ind)||' as b, D'||(SD_ind)||' as c WHERE a.col_num = b.col_num AND a.row_num=c.row_num GROUP BY a.col_num';
                execute 'INSERT INTO D'||(SD_ind%2+1)||' SELECT a.row_num, avg(c.val)+sum(a.val*b.val)*'||(step)||' FROM e as a, S'||(SD_ind)||' as b, D'||(SD_ind)||' as c WHERE a.col_num = b.col_num AND a.row_num=c.row_num GROUP BY a.row_num';    
                SD_ind := SD_ind%2+1;
            end if;
        end loop;

        execute 'TRUNCATE TABLE e'||(keep_ind%2+1);
        execute 'INSERT INTO e'||(keep_ind%2+1)||' SELECT a.row_num, a.col_num, (a.val-b.val*c.val) FROM e'||(keep_ind)||' as a, S'||(SD_ind)||' as b, D'||(SD_ind)||' as c WHERE a.col_num = b.col_num AND a.row_num=c.row_num';
        
        keep_ind := keep_ind%2+1;
        execute 'INSERT INTO '||matrix_u||' SELECT '||(j)||', col_num, val FROM S'||(SD_ind); 
        execute 'INSERT INTO '||matrix_v||' SELECT row_num, '||(j)||', val FROM D'||(SD_ind); 
        if((error < MIN_IMPROVEMENT) and (EARLY_TEMINATE = 1)) then
            exit;
	end if;
        
        error := 0;
        j := j + 1;
    end loop;
    return 1;        
END;
$BODY$
  LANGUAGE 'plpgsql' ;
