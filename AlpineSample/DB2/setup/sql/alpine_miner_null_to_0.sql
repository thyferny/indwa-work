create or replace function alpine_miner_null_to_0(value double)
  returns double 
begin
  if (value is null) then
    return 0;
  else
    return value;
  end if;
end@

