CREATE OR REPLACE FUNCTION alpine_miner_covar_sam_accum(state DOUBLE PRECISION[], x DOUBLE PRECISION[])
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_miner_covar_sam_combine(state1 DOUBLE PRECISION[], state2 DOUBLE PRECISION[])
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_miner_covar_sam_final(state1 DOUBLE PRECISION[])
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

DROP AGGREGATE IF EXISTS alpine_miner_covar_sam(DOUBLE PRECISION[]);
CREATE AGGREGATE alpine_miner_covar_sam(DOUBLE PRECISION[]) (
    SFUNC=alpine_miner_covar_sam_accum,
    STYPE=float8[],
    prefunc=alpine_miner_covar_sam_combine,
    INITCOND='{0}',
    FINALFUNC=alpine_miner_covar_sam_final
);



