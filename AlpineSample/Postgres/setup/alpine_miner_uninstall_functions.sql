DROP FUNCTION IF EXISTS alpine_miner_generate_random_table(t text, rowCount bigint);
DROP FUNCTION IF EXISTS alpine_miner_getdistribution(tablename text, schemaname text);
DROP FUNCTION IF EXISTS alpine_miner_kmeans_c_array(table_name text, table_name_withoutschema text, column_name text[], column_number integer, id text, k integer, max_run integer, max_iter integer, distance integer);
DROP FUNCTION IF EXISTS alpine_miner_kmeans_c(table_name text, table_name_withoutschema text, column_name text[], column_number integer, id text, k integer, max_run integer, max_iter integer, distance integer);
DROP FUNCTION IF EXISTS alpine_miner_Kmeans_distance(distancetype integer, column_name text[], column_number integer, k integer);
DROP FUNCTION IF EXISTS alpine_miner_kmeans_sp(table_name text, table_name_withoutschema text, column_name text[], column_number integer, id text, k integer, max_run integer, max_iter integer, distance integer);
DROP FUNCTION IF EXISTS alpine_miner_lr_ca_he(beta float[],columns float[],add_intercept boolean, weight float) ;
DROP FUNCTION IF EXISTS alpine_miner_lr_ca_fitness(beta float[],columns float[],add_intercept boolean, weight float, label_value int);
DROP FUNCTION IF EXISTS alpine_miner_lr_ca_pi(beta float[],columns float[],add_intercept boolean);
DROP FUNCTION IF EXISTS alpine_miner_lr_ca_beta(beta float[],columns float[],add_intercept boolean, weight float, y int, times int);
DROP FUNCTION IF EXISTS alpine_miner_lr_ca_he_de(beta float[],columns float[],add_intercept boolean, weight float, y int);
DROP FUNCTION IF EXISTS alpine_miner_nn_ca_o(weight float[], column_names float[], input_range float[], input_base float[], hidden_node_number integer[], hidden_layer_number integer,  output_range float, output_base float, output_node_number integer, normalize boolean , floatal_label boolean);
DROP FUNCTION IF EXISTS alpine_miner_kmeans_distance_loop(sample double precision[], data double precision[], k integer, distancemode integer);
DROP FUNCTION IF EXISTS alpine_miner_kmeans_distance_result(sample double precision[], data double precision[], k integer, distancemode integer);
DROP FUNCTION IF EXISTS alpine_miner_nb_ca_deviance  (nominal_column_names text[], nominal_columns_mapping_count int[], nominal_columns_mapping text[],nominal_columns_probability float[],dependent_column text, dependent_column_mapping text[], dependent_column_probability float[], numerical_columns float[], numerical_columns_probability float[]);
DROP FUNCTION IF EXISTS alpine_miner_nb_ca_confidence(nominal_column_names text[], nominal_columns_mapping_count int[], nominal_columns_mapping text[],nominal_columns_probability float[],dependent_column_mapping text[], dependent_column_probability float[], numerical_columns float[], numerical_columns_probability float[]);
DROP FUNCTION IF EXISTS alpine_miner_nb_ca_prediction(confidence_column float[], dependent_column_mapping text[]);

