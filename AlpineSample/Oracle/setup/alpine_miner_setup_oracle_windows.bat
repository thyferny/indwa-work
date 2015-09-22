@echo off
set /p oraclehome=Please input oracle home:
set /p hostname=Please input hostname or ip address:
set /p port=Please input port number:
set /p database=Please input database:
set /p username=Please input username:
set /p password=Please input password:
%oraclehome%\bin\sqlplus %username%/%password%@//%hostname%:%port%/%database% < alpine_miner_setup_oracle.sql

