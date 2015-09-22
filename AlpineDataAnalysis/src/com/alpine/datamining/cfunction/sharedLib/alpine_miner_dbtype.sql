create or replace function alpine_miner_get_dbtype() 
returns text as
$$
begin
        return 'Greenplum';
end;
$$
language 'plpgsql';


create or replace function getAMVersion() 
returns text as
$$
begin
        return 'Alpine Miner Release 2.7';
end;
$$
language 'plpgsql';


