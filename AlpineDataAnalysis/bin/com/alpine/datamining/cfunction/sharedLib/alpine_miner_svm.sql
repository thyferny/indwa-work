drop type IF EXISTS alpine_miner_svm_model cascade;
CREATE TYPE alpine_miner_svm_model AS (
       inds int, -- number of individuals processed
       cum_err float8, -- cumulative error
       epsilon float8, -- the size of the epsilon tube around the hyperplane, adaptively adjusted by algorithm
       rho float8, -- classification margin
       b float8, -- classifier offset
       nsvs int, -- number of support vectors
       ind_dim int, -- the dimension of the individuals
       weights float8[], -- the weight of the support vectors
       individuals float8[]--, -- the array of support vectors, represented as a 1-D array
--       kernel_oid oid -- OID of kernel function
);


CREATE OR REPLACE FUNCTION alpine_miner_svm_predict_sub(int,int,float8[],float8[],float8[],int, int, float8) RETURNS float8
AS 'alpine_miner', 'alpine_miner_svm_predict_sub' LANGUAGE C IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION alpine_miner_svs_predict(svs alpine_miner_svm_model, ind float8[], kernel_type int, degree int, gamma float8)
RETURNS float8 AS $$
SELECT alpine_miner_svm_predict_sub($1.nsvs, $1.ind_dim, $1.weights, $1.individuals, $2, $3, $4, $5);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION
alpine_miner_svm_reg_update(svs alpine_miner_svm_model, ind FLOAT8[], label FLOAT8, eta FLOAT8, nu FLOAT8, slambda FLOAT8, kernel_type int, degree int, gamma float8)
RETURNS alpine_miner_svm_model AS 'alpine_miner', 'alpine_miner_svm_reg_update' LANGUAGE C STRICT;

CREATE AGGREGATE alpine_miner_svm_reg_agg(float8[], float8, float8, float8, float8, int, int, float8) (
       sfunc = alpine_miner_svm_reg_update,
       stype = alpine_miner_svm_model,
       initcond = '(0,0,0,0,1,0,0,{},{})'
);

CREATE OR REPLACE FUNCTION
alpine_miner_svm_cls_update(svs alpine_miner_svm_model, ind FLOAT8[], label FLOAT8, eta FLOAT8, nu FLOAT8, kernel_type int, degree int, gamma float8)
RETURNS alpine_miner_svm_model AS 'alpine_miner', 'alpine_miner_svm_cls_update' LANGUAGE C STRICT;


CREATE AGGREGATE alpine_miner_svm_cls_agg(float8[], float8, float8, float8, int, int, float8) (
       sfunc = alpine_miner_svm_cls_update,
       stype = alpine_miner_svm_model,
       initcond = '(0,0,0,0,1,0,0,{},{})'
);

CREATE OR REPLACE FUNCTION
alpine_miner_svm_nd_update(svs alpine_miner_svm_model, ind FLOAT8[],  eta FLOAT8, nu FLOAT8, kernel_type int, degree int, gamma float8)
RETURNS alpine_miner_svm_model AS 'alpine_miner', 'alpine_miner_svm_nd_update' LANGUAGE C STRICT;

CREATE AGGREGATE alpine_miner_svm_nd_agg(float8[], float8, float8, int, int, float8) (
       sfunc = alpine_miner_svm_nd_update,
       stype = alpine_miner_svm_model,
       initcond = '(0,0,0,0,0,0,0,{},{})'
);

CREATE OR REPLACE FUNCTION alpine_miner_online_sv_reg(table_name text, ind text, label text,wherestr text,  kernel_type int, degree int, gamma float, eta FLOAT8, slambda FLOAT8, nu FLOAT8) 
RETURNS alpine_miner_svm_model AS $$
DECLARE
	svs alpine_miner_svm_model ;
	sql text;
BEGIN
	sql := 'select (model).inds, (model).cum_err, (model).epsilon, (model).rho, (model).b, (model).nsvs, (model).ind_dim, (model).weights,(model).individuals from (select  alpine_miner_svm_reg_agg('||ind||'::float8[], '||label||'::float8,' || eta || ',' || nu || ',' || slambda || ','|| kernel_type|| ','|| degree|| ','|| gamma|| ') as model from ' || table_name||' where  '||wherestr||' ) a';
	execute sql into svs.inds, svs.cum_err, svs.epsilon, svs.rho, svs.b, svs.nsvs, svs.ind_dim, svs.weights, svs.individuals;
	return svs;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION alpine_miner_online_sv_cl(table_name text, ind text, label text, wherestr text, kernel_type int, degree int, gamma float, eta FLOAT8, nu FLOAT8) 
RETURNS alpine_miner_svm_model AS $$
DECLARE
	svs alpine_miner_svm_model ;
	sql text;
BEGIN
--alpine_miner_svm_cls_update(svs alpine_miner_svm_model, ind FLOAT8[], label FLOAT8, eta FLOAT8, nu FLOAT8, kernel_type int, degree int, gamma float8)
	sql := 'select (model).inds, (model).cum_err, (model).epsilon, (model).rho, (model).b, (model).nsvs, (model).ind_dim, (model).weights,(model).individuals from (select  alpine_miner_svm_cls_agg('||ind||'::float8[], '||label||'::float8,' || eta || ',' ||nu ||','|| kernel_type|| ',' ||degree||','|| gamma|| ') as model from ' || table_name||' where '||wherestr||' ) a';
	execute sql into svs.inds, svs.cum_err, svs.epsilon, svs.rho, svs.b, svs.nsvs, svs.ind_dim, svs.weights, svs.individuals;
	return svs;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION alpine_miner_online_sv_nd(table_name text, ind text, wherestr text, kernel_type int, degree int, gamma float, eta FLOAT8, nu FLOAT8) 
RETURNS alpine_miner_svm_model AS $$
DECLARE
	svs alpine_miner_svm_model ;
	sql text;
BEGIN
--alpine_miner_svm_nd_update(svs alpine_miner_svm_model, ind FLOAT8[],  eta FLOAT8, nu FLOAT8, kernel_type int, degree int, gamma float8)
	sql := 'select (model).inds, (model).cum_err, (model).epsilon, (model).rho, (model).b, (model).nsvs, (model).ind_dim, (model).weights,(model).individuals from (select  alpine_miner_svm_nd_agg('||ind||'::float8[],' || eta || ',' || nu ||  ', '|| kernel_type|| ', '|| degree|| ', '|| gamma|| ') as model from ' || table_name||' where  '||wherestr||'  ) a';
	execute sql into svs.inds, svs.cum_err, svs.epsilon, svs.rho, svs.b, svs.nsvs, svs.ind_dim, svs.weights, svs.individuals;
	return svs;
END
$$ LANGUAGE plpgsql;


