#!/bin/sh
psql -d miner_demo -f alpine_miner_kmeans_sp.sql
psql -d miner_demo -f alpine_miner_generate_random_table.sql
psql -d miner_demo -f alpine_miner_kmeans_distance.sql
psql -d miner_demo -f alpine_miner_getdistribution.sql
psql -d miner_demo -f alpine_miner_register_c_functions.sql
psql -d miner_demo -f alpine_miner_kmeans_c.sql
psql -d miner_demo -f alpine_miner_kmeans_c_array.sql
