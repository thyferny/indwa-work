@echo off
set port=5432
set database=template1
set username=postgres
set /p postgreshome=Please input postgres installation path:
set ans=
set /p ans=Provide the port number or press ENTER to accept the default: %port% 
if not "%ans%" == "" set port=%ans%
set ans=
set /p ans=Provide the Postgres Database name or press ENTER to accept the default: %database% 
if not "%ans%" == "" set database=%ans%
set ans=
set /p ans=Provide the Postgres username or press ENTER to accept the default: %username% 
if not "%ans%" == "" set username=%ans%
set cmd="%postgreshome%"\bin\psql
copy sharedLib\alpine_miner.dll "%postgreshome%"\lib\
%cmd% -p %port%  -U %username% -c "CREATE PROCEDURAL LANGUAGE plpgsql;" -d %database% 
%cmd% -p %port%  -U %username% -f alpine_miner_setup.sql -d %database% 
set ans=
set /p ans=Would you like to install Alpine Miner demo database? (y/n) 
if "%ans%"=="y" %cmd% -p %port% -U %username% -f create_demo_db.sql -d %database% 
