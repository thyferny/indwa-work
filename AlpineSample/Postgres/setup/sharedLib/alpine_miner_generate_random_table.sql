CREATE OR REPLACE FUNCTION alpine_miner_generate_random_table(t text, rowCount bigint) RETURNS integer AS $$
DECLARE 
	--rand bigint;
	--rand double precision;
	count bigint := 1;
BEGIN
	WHILE count <= rowCount LOOP
		--select trunc(random()*rowCount) into rand;
		--select random() into rand;
		execute 'insert into '||t||' select '||count||','||random();
		count := count + 1;
	END LOOP;
return 1;
END;
$$ LANGUAGE plpgsql VOLATILE;
