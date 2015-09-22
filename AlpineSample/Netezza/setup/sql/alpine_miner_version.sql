
create  or replace procedure getAMVersion()
language nzplsql
returns varchar(2000)
as
BEGIN_PROC
BEGIN
return 'Alpine Miner Release 2.8';
END;
END_PROC;

