#!/bin/bash

read -p "Please input oracle home:" oraclehome;
read -p "Please input hostname or ip address:" hostname;
read -p "Please input port number:" port;
read -p "Please input database name:" databasename;
read -p "Please input username:" username;
read -p "Please input password:" password;
$oraclehome/bin/sqlplus $username/$password@//$hostname:$port/$databasename < alpine_miner_setup_oracle_10g.sql;

