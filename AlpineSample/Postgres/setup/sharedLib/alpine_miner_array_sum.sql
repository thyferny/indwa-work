
create or replace function alpine_miner_float_array_sum_accum(state float[], ind float[])
returns float[] 
AS 'alpine_miner', 'alpine_miner_float_array_sum_accum'
language C immutable;
DROP AGGREGATE IF EXISTS sum(float[]);
CREATE AGGREGATE sum (float[])(
    sfunc = alpine_miner_float_array_sum_accum,
    stype = float[],
    initcond = '{}'
);

