CREATE or replace procedure alpine_miner_generate_random_table(t varchar(4000), rowCount bigint,seed numeric) 
BEGIN
DECLARE  count bigint default 1;
DECLARE  executesql varchar(4000);
	while (count <= rowCount) do
	SET executesql='insert into '||t||' select '||count||','||rand(seed+count)||' from SYSIBM.SYSDUMMY1';
	execute immediate executesql;
	commit;
	set count = count + 1;
	END while;
END@

