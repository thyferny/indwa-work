create OR REPLACE function alpine_miner_null_to_0(x bigint)
returns bigint AS
$BODY$
BEGIN
if x is null
then return 0;
else return x;
end if;END;
$BODY$
LANGUAGE 'plpgsql' immutable;

create OR REPLACE function alpine_miner_null_to_0(x float)
returns float AS
$BODY$
BEGIN
if x is null
then return 0;
else return x;
end if;END;
$BODY$
LANGUAGE 'plpgsql' immutable;
