CREATE OR REPLACE FUNCTION alpine_miner_float8_mregr_accum(state DOUBLE PRECISION[], y DOUBLE PRECISION, x DOUBLE PRECISION[])
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_miner_float8_mregr_combine(state1 DOUBLE PRECISION[], state2 DOUBLE PRECISION[])
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

DROP AGGREGATE IF EXISTS alpine_miner_mregr_coef(DOUBLE PRECISION,DOUBLE PRECISION[]);
CREATE AGGREGATE alpine_miner_mregr_coef(DOUBLE PRECISION,DOUBLE PRECISION[]) (
    SFUNC=alpine_miner_float8_mregr_accum,
    STYPE=float8[],
    prefunc=alpine_miner_float8_mregr_combine,
    INITCOND='{0}'
);

