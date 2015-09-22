CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_beta_accum(state DOUBLE PRECISION[],beta float[],columns float[],add_intercept boolean, weight float, y int, times int )
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_he_accum(state DOUBLE PRECISION[],beta float[],columns float[],add_intercept boolean, weight float)
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;
CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_he_de_accum(state DOUBLE PRECISION[],beta float[],columns float[],add_intercept boolean, weight float, y int)
RETURNS DOUBLE PRECISION[]
AS 'alpine_miner'
LANGUAGE C
IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_fitness(beta float[],columns float[],add_intercept boolean, weight float, label_value int) RETURNS float
    AS 'alpine_miner', 'alpine_miner_lr_ca_fitness'
    LANGUAGE C immutable ;
CREATE OR REPLACE FUNCTION alpine_miner_lr_ca_pi(beta float[],columns float[],add_intercept boolean) RETURNS float
    AS 'alpine_miner', 'alpine_miner_lr_ca_pi'
    LANGUAGE C immutable ;

drop function if exists alpine_miner_lr_ca_beta(float[],float[],boolean, float, int, int);
drop function if exists alpine_miner_lr_ca_he(float[],float[],boolean, float) ;
drop function if exists alpine_miner_lr_ca_he_de(float[],float[],boolean, float, int);
DROP AGGREGATE IF EXISTS alpine_miner_lr_ca_beta(float[],float[],boolean, float, int, int);
CREATE AGGREGATE alpine_miner_lr_ca_beta(float[],float[],boolean, float, int, int) (
    SFUNC=alpine_miner_lr_ca_beta_accum,
    STYPE=float8[],
    INITCOND='{0}'
);

DROP AGGREGATE IF EXISTS alpine_miner_lr_ca_he(float[],float[],boolean, float);
CREATE AGGREGATE alpine_miner_lr_ca_he(float[],float[],boolean, float) (
    SFUNC=alpine_miner_lr_ca_he_accum,
    STYPE=float8[],
    INITCOND='{0}'
);


DROP AGGREGATE IF EXISTS alpine_miner_lr_ca_he_de(float[],float[],boolean, float, int);
CREATE AGGREGATE alpine_miner_lr_ca_he_de(float[],float[],boolean, float, int) (
    SFUNC=alpine_miner_lr_ca_he_de_accum,
    STYPE=float8[],
    INITCOND='{0}'
);

