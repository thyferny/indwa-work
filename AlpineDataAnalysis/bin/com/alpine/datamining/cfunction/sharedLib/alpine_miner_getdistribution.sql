-- Function: alpine_miner_getdistribution(text, text)

-- DROP FUNCTION alpine_miner_getdistribution(text, text);

CREATE OR REPLACE FUNCTION alpine_miner_getdistribution(tablename text, schemaname text)
  RETURNS smallint[] AS
$BODY$

DECLARE
	 result smallint[];
BEGIN
execute 'select attrnums from gp_distribution_policy where localoid in (select relid from pg_stat_user_tables where relname='''||tablename||''' and schemaname like '''||schemaname||''')' into result;


RETURN result;
 
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;
