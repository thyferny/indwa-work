CREATE OR REPLACE FUNCTION demo.calculator(types text, base double precision,rate double precision)
  RETURNS double precision AS
$BODY$
declare
  result text;
begin
        if types = 'sum' then
            result = $2+$3;
        else
            result = $2*$3 ;
        end if;
  return result;
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;