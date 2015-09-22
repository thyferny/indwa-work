CREATE OR REPLACE procedure alpine_miner_generate_random_table(text,bigint) RETURNS integer language nzplsql AS 
BEGIN_PROC
DECLARE 
	count bigint := 1;
BEGIN
	WHILE count <= $2 LOOP
		execute IMMEDIATE 'insert into '||$1||' select '||count||','||random()::float;
		count := count + 1;
	END LOOP;
return 1;
END;
END_PROC;