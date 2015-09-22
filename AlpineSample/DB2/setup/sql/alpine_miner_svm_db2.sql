CREATE OR REPLACE FUNCTION alpine_miner_dot_kernel (x FLOATARRAY, y FLOATARRAY )
RETURNS double

BEGIN
    DECLARE len int;
    DECLARE ret double DEFAULT 0;
    DECLARE i INTEGER;
    DECLARE xlen INTEGER;
    DECLARE ylen INTEGER;
	set xlen = CARDINALITY(x);
	set ylen = CARDINALITY(y);
    IF xlen < ylen THEN 
        SET len = xlen;
    ELSE
        SET len = ylen;
    END IF;
    SET i = 1;
    WHILE i <= len DO
        SET ret = ret + x[i] * y[i];
        SET i = i + 1;
    END WHILE ;
    RETURN ret;
END@

CREATE OR REPLACE FUNCTION alpine_miner_polynomial_kernel (x FLOATARRAY, 
                                                y FLOATARRAY, 
                                                degree integer )
RETURNS double
begin
RETURN POWER(alpine_miner_dot_kernel(x, y), degree);
end@


CREATE OR REPLACE FUNCTION alpine_miner_gaussian_kernel (x FLOATARRAY, 
                                               y FLOATARRAY, 
                                               gamma double )
RETURNS double
BEGIN
    DECLARE RETURN_VAL double DEFAULT 0;
    DECLARE len int;
    DECLARE diff floatarray;
    DECLARE i INTEGER;
    DECLARE temp double default 0;
    IF cardinality(x) < cardinality(y) THEN 
        SET len = cardinality(x);
    ELSE
        SET len = cardinality(y);
    END IF;
    SET i = 1;
    WHILE i <= len DO
        SET diff[i] = x[i] - y[i];
        SET i = i + 1;
    END WHILE ;
    set temp = -1.0 * double(gamma) * alpine_miner_dot_kernel(diff, diff);
    if(temp > 30) then
    	SET RETURN_VAL = EXP(double(30.0));
    elseif(temp < -30) then
    	SET RETURN_VAL = EXP(double(-30.0));
    else
    	set RETURN_VAL = EXP(temp) ;
    end if;
    RETURN RETURN_VAL;
END@

CREATE OR REPLACE function alpine_miner_kernel (x FLOATARRAY, 
                                      y FLOATARRAY, 
                                      kernel_type int, 
                                      degree int, 
                                      gamma double)
returns double
BEGIN
    DECLARE RETURN_VAL double default 0;
    DECLARE len int;
    IF kernel_type = 1 THEN 
        SET RETURN_VAL = alpine_miner_dot_kernel(x, y);
        RETURN RETURN_VAL;
    ELSE
        IF kernel_type = 2 THEN 
            SET RETURN_VAL = alpine_miner_polynomial_kernel(x, y, degree);
            RETURN RETURN_VAL;
        ELSE
            IF kernel_type = 3 THEN 
                set RETURN_VAL =  alpine_miner_gaussian_kernel(x,y,gamma);
                RETURN RETURN_VAL;
            ELSE
                SET RETURN_VAL = alpine_miner_dot_kernel(x, y);
                RETURN RETURN_VAL;
            END IF;
        END IF;
    END IF;
END@
CREATE OR REPLACE FUNCTION alpine_miner_svs_predict (
    nsvs        int,
    ind_dim     int,
    weights floatarray,
    individuals floatarray,
    ind FLOATARRAY, 
    kernel_type int, 
    degree int, 
    gamma double)
RETURNS double
BEGIN
    DECLARE ret double DEFAULT 0;
    DECLARE individual floatarray;
    DECLARE i int;
    DECLARE j int;
    SET i = 1;
    WHILE i <= nsvs DO
        SET j = 1;
        WHILE j <= ind_dim DO
            SET individual[j] = individuals[ind_dim * (i - 1) + j];
            SET j = j + 1;
        END WHILE ;
        SET ret = ret + weights[i] * alpine_miner_kernel(individual,ind,kernel_type,degree,gamma);
        SET i = i + 1;
    END WHILE ;
    RETURN ret;
end@

CREATE OR REPLACE PROCEDURE alpine_miner_online_sv_reg (table_name VARCHAR(32672), 
                                             ind VARCHARarray, 
                                             label VARCHAR(32672), 
                                             wherestr VARCHAR(32672), 
                                             kernel_type int, 
                                             degree int, 
                                             gamma double, 
                                             eta double, 
                                             slambda double, 
                                             nu double, 
											 OUT    inds        int,
											 OUT   cum_err     double,
											 OUT   epsilon     double,
											 OUT   rho         double,
											 OUT   b           double,
											 OUT   nsvs        int,
											 OUT   ind_dim     int,
											 OUT   weights     floatarray,
											 OUT   individuals floatarray                                           
)


BEGIN
    DECLARE p double;
    
    DECLARE diff double;
    DECLARE error double;
    DECLARE weight double;
    
    DECLARE sqlstr VARCHAR(32672);
    
    DECLARE fa floatArray;
    DECLARE labelvalue double;
    DECLARE cap double;
    DECLARE i int DEFAULT 0.0;
    DECLARE i1 INTEGER;
    DECLARE i2 INTEGER;
    DECLARE RETURN_VAL1 double;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	SET weights = ARRAY[];
	SET individuals = ARRAY[];
	
	set i = 1;
	set column_count = CARDINALITY(ind);
	set ind[column_count + 1] = label;
	set sql = alpine_miner_get_select_column_sql(table_name, ind, wherestr);
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;
	set inds = 0; 
	set cum_err = 0; 
	set epsilon = 0; 
	set rho = 0; 
	set b = 1; 
	set nsvs = 0; 
	set ind_dim = 0; 

fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count + 1);
		set labelvalue = column_array[column_count + 1];
		set fa  = TRIM_ARRAY(column_array, 1);
        IF i = 1 THEN 
            SET ind_dim = cardinality(fa);
        END IF;
        set RETURN_VAL1 = alpine_miner_svs_predict(
		    nsvs,
		    ind_dim,
		    weights,
		    individuals,
			fa,
			kernel_type,
			degree,gamma);
        SET p = RETURN_VAL1;
        SET diff = labelvalue - p;
        SET error = ABS(diff);
        SET inds = inds + 1;
        SET cum_err = cum_err + error;
        SET cap = 0.1 + CAST (1 AS double) / (1 - eta * slambda);
        IF (error > epsilon) THEN 
            SET i1 = 1;
            WHILE i1 <= nsvs DO
                IF (ABS(weights[i1]) < (cap + 0.1) * 2.2250738585072014e-308) THEN 
                    SET weights[i1] = 0;
                ELSE
                    SET weights[i1] = weights[i1] * (1 - eta * slambda);
                END IF;
                SET i1 = i1 + 1;
            END WHILE ;
            SET weight = eta;
            IF (diff < 0) THEN 
                SET weight = -1 * weight;
            END IF;
            SET nsvs = nsvs + 1;
            SET weights[nsvs] = weight;
            SET i2 = 1;
            WHILE i2 <= cardinality(fa) DO
                SET individuals[cardinality(individuals)+1] = fa[i2];
                SET i2 = i2 + 1;
            END WHILE ;
            SET epsilon = epsilon + (1 - nu) * eta;
        ELSE
            SET epsilon = epsilon - eta * nu;
        END IF;
	END LOOP;
	CLOSE my_cursor ;
END@
CREATE OR REPLACE PROCEDURE alpine_miner_online_sv_cl (table_name VARCHAR(32672), 
                                            ind VARCHARARRAY, 
                                            label VARCHAR(32672), 
                                            wherestr VARCHAR(32672), 
                                            kernel_type int, 
                                            degree int, 
                                            gamma double, 
                                            eta double, 
                                            nu double, 
											 OUT    inds        int,
											 OUT   cum_err     double,
											 OUT   epsilon     double,
											 OUT   rho         double,
											 OUT   b           double,
											 OUT   nsvs        int,
											 OUT   ind_dim     int,
											 OUT   weights     floatarray,
											 OUT   individuals floatarray                                           
)
BEGIN
    
    DECLARE p double;
    
    DECLARE sqlstr VARCHAR(32672);
    DECLARE fa floatArray;
    DECLARE labelvalue double;
    DECLARE i int DEFAULT 0;
    DECLARE i1 int DEFAULT 0;
    DECLARE i2 INTEGER;
    DECLARE RETURN_VAL1 double;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	SET weights = ARRAY[];
	SET individuals = ARRAY[];

	set i = 1;
	set column_count = CARDINALITY(ind);
	set ind[column_count + 1] = label;
	set sql = alpine_miner_get_select_column_sql(table_name, ind, wherestr); -- not null;TODO
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;
	set inds = 0; 
	set cum_err = 0; 
	set epsilon = 0; 
	set rho = 0; 
	set b = 1; 
	set nsvs = 0; 
	set ind_dim = 0; 

fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count + 1);
		set labelvalue = column_array[column_count + 1];
		set fa  = TRIM_ARRAY(column_array, 1);
		
       	IF i = 1 THEN 
            SET ind_dim = cardinality(fa);
        END IF;
        
        set RETURN_VAL1 = alpine_miner_svs_predict(
		   nsvs,
		   ind_dim,
		   weights,
		   individuals,
		   fa,
		   kernel_type,
		   degree,gamma);    

        SET p = labelvalue * (RETURN_VAL1 + b);
        SET inds = inds + 1;
        IF p < 0 THEN 
            SET cum_err = cum_err + 1;
        END IF;
        IF (p <= rho) THEN 
            SET i1 = 1;
            WHILE i1 <= nsvs DO
                IF (ABS(weights[i1]) < 1.15 * 2.2250738585072014e-308) THEN 
                    SET weights[i1] = 0;
                ELSE
                    SET weights[i1] = weights[i1] * (1 - 0.1 * eta);
                END IF;
                SET i1 = i1 + 1;
            END WHILE ;
            SET nsvs = nsvs + 1;
            SET weights[nsvs] = labelvalue * eta;
            SET i2 = 1;
            WHILE i2 <= cardinality(fa) DO
                SET individuals[cardinality(individuals) + 1] = fa[i2];
                SET i2 = i2 + 1;
            END WHILE ;
            SET b = b + eta * labelvalue;
            SET rho = rho - eta * (1 - nu);
        ELSE
            SET rho = rho + eta * nu;
        END IF;
        	END LOOP;
	CLOSE my_cursor ;
end@

CREATE OR REPLACE PROCEDURE alpine_miner_online_sv_nd (table_name VARCHAR(32672), 
                                            ind VARCHARARRAY, 
                                            wherestr VARCHAR(32672), 
                                            kernel_type int, 
                                            degree int, 
                                            gamma double, 
                                            eta double, 
                                            nu double,
											 OUT    inds        int,
											 OUT   cum_err     double,
											 OUT   epsilon     double,
											 OUT   rho         double,
											 OUT   b           double,
											 OUT   nsvs        int,
											 OUT   ind_dim     int,
											 OUT   weights     floatarray,
											 OUT   individuals floatarray                                           
                                             )
BEGIN
    DECLARE p double;
    DECLARE sqlstr VARCHAR(32672);
    DECLARE fa floatArray;
    DECLARE i int DEFAULT 0;
    DECLARE i1 INTEGER DEFAULT 0;
    DECLARE RETURN_VAL1 double;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	SET weights = ARRAY[];
	SET individuals = ARRAY[];
	set i = 1;
	set column_count = CARDINALITY(ind);
	set sql = alpine_miner_get_select_column_sql(table_name, ind, wherestr); -- not null;TODO
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;
	set inds = 0; 
	set cum_err = 0; 
	set epsilon = 0; 
	set rho = 0; 
	set b = 0; 
	set nsvs = 0; 
	set ind_dim = 0;     

fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set fa = alpine_miner_row_to_array(rowVariable1012, column_count);
	    IF i = 1 THEN 
            SET ind_dim = cardinality(fa);
        END IF;
        SET p = alpine_miner_svs_predict(
		    nsvs,
		    ind_dim,
		    weights,
		    individuals,
		    fa,
		    kernel_type,
		    degree,gamma);    
        SET inds = inds + 1;
        IF (p < rho) THEN 
            SET i1 = 1;
            WHILE i1 <= nsvs DO
                IF (weights[i1] < 1.15 * 2.2250738585072014e-308) THEN 
                    SET weights[i1] = 0;
                ELSE
                    SET weights[i1] = weights[i1] * (1 - 0.1 * eta);
                END IF;
                SET i1 = i + 1;
            END WHILE ;
            SET nsvs = nsvs + 1;
            SET weights[nsvs] = eta;
            SET i1 = 1;
            WHILE i1 <= cardinality(fa) DO
                SET individuals[cardinality(individuals) + 1] = fa[i1];
                SET i1 = i1 + 1;
            END WHILE ;
            SET rho = rho - eta * (1 - nu);
        ELSE
            SET rho = rho + eta * nu;
        END IF;
	END LOOP;
	CLOSE my_cursor ;
END@



CREATE OR REPLACE PROCEDURE alpine_miner_svs_predict_proc (
table_name VARCHAR(32672), 
temp_table_name varchar(32672),                                             
wherestr VARCHAR(32672), 
kernel_type int, 
degree int, 
gamma double, 
nsvs        int,
ind_dim     int,
weights floatarray,
individuals floatarray,
ind VARCHARARRAY
)

BEGIN
    DECLARE p double;
    DECLARE id bigint;
    DECLARE sqlstr VARCHAR(32672);
    DECLARE fa floatArray;
    DECLARE i int DEFAULT 0;
    DECLARE i1 INTEGER DEFAULT 0;
    DECLARE RETURN_VAL1 double;
	declare   rowVariable1012 row1012;
	DECLARE SQL VARCHAR(32672) DEFAULT '';
	DECLARE column_count integer default 0;
	declare column_array floatarray;
	DECLARE SQLSA VARCHAR(32672);
	DECLARE SQLSTATE CHAR(5) DEFAULT '00000';
	DECLARE at_end SMALLINT DEFAULT 0;
	DECLARE not_found CONDITION for SQLSTATE '02000';
	DECLARE my_cursor  CURSOR  WITH RETURN FOR SQLSA ;
	DECLARE CONTINUE HANDLER for not_found 
	SET at_end = 1;
	

	set i = 1;
	set column_count = CARDINALITY(ind);
	set ind[column_count + 1] = 'alpine_miner_id';
	set sql = alpine_miner_get_select_column_sql(table_name, ind, wherestr); -- not null;TODO
	PREPARE SQLSA FROM sql ;
	OPEN  my_cursor ;
	set i = 0;

fetch_loop:
	LOOP
		set i = i + 1;
		fetch my_cursor into rowVariable1012;
	    IF at_end <> 0 THEN LEAVE fetch_loop;
    	END IF;
		set column_array = alpine_miner_row_to_array(rowVariable1012, column_count+1);
		set id = column_array[column_count + 1];
		set fa = TRIM_ARRAY(column_array, 1);
        SET p = alpine_miner_svs_predict(
		    nsvs,
		    ind_dim,
		    weights,
		    individuals,
		    fa,
		    kernel_type,
		    degree,gamma);  
		set sqlstr = ' insert into '||temp_table_name||' values ( '||id||','||p||')';
		execute immediate  sqlstr ;
	END LOOP;
	CLOSE my_cursor ;
END@




