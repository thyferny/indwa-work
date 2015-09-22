CREATE OR REPLACE FUNCTION alpine_miner_nn_ca_o(weight float[], column_names float[], input_range float[], input_base float[], hidden_node_number integer[], hidden_layer_number integer,  output_range float, output_base float, output_node_number integer, normalize boolean , floatal_label boolean)
    RETURNS float []
    AS 'alpine_miner', 'alpine_miner_nn_ca_o'
    LANGUAGE C immutable ;
CREATE OR REPLACE FUNCTION alpine_miner_nn_ca_change(weight float[], column_names float[], input_range float[], input_base float[], hidden_node_number integer[], hidden_layer_number integer,  output_range float, output_base float, output_node_number integer, normalize boolean , floatal_label boolean, label float, set_size int)
    RETURNS float []
    AS 'alpine_miner', 'alpine_miner_nn_ca_change'
    LANGUAGE C immutable ;

    CREATE OR REPLACE FUNCTION alpine_miner_kmeans_distance_loop(sample double precision[], data double precision[], k integer, distancemode integer)
  RETURNS integer AS
'alpine_miner', 'alpine_miner_kmeans_distance_loop'
  LANGUAGE 'c' IMMUTABLE;
  
  CREATE OR REPLACE FUNCTION alpine_miner_kmeans_distance_result(sample double precision[], data double precision[], k integer, distancemode integer)
  RETURNS double precision AS
'alpine_miner', 'alpine_miner_kmeans_distance_result'
  LANGUAGE 'c' IMMUTABLE;

CREATE OR REPLACE FUNCTION alpine_miner_nb_ca_deviance  (nominal_column_names text[], nominal_columns_mapping_count int[], nominal_columns_mapping text[],nominal_columns_probability float[],dependent_column text, dependent_column_mapping text[], dependent_column_probability float[], numerical_columns float[], numerical_columns_probability float[])
    RETURNS float
    AS 'alpine_miner', 'alpine_miner_nb_ca_deviance'
    LANGUAGE C immutable ;

CREATE OR REPLACE FUNCTION alpine_miner_nb_ca_confidence(nominal_column_names text[], nominal_columns_mapping_count int[], nominal_columns_mapping text[],nominal_columns_probability float[],dependent_column_mapping text[], dependent_column_probability float[], numerical_columns float[], numerical_columns_probability float[])
    RETURNS float []
    AS 'alpine_miner', 'alpine_miner_nb_ca_confidence'
    LANGUAGE C immutable ;

CREATE OR REPLACE FUNCTION alpine_miner_nb_ca_prediction(confidence_column float[], dependent_column_mapping text[])
    RETURNS text
    AS 'alpine_miner', 'alpine_miner_nb_ca_prediction'
    LANGUAGE C immutable ;

CREATE OR REPLACE FUNCTION alpine_miner_dot_product( float[], float[] ) 
	RETURNS float
	AS 'alpine_miner', 'alpine_miner_dot_product'
	LANGUAGE C IMMUTABLE;

CREATE OR REPLACE FUNCTION alpine_miner_has_novel_product( float[], float[] ) 
	RETURNS int
	AS 'alpine_miner', 'alpine_miner_has_novel_product'
	LANGUAGE C IMMUTABLE;
