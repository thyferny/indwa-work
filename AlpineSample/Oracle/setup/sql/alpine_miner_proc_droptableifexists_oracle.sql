 create or replace procedure proc_droptableifexists(tablename varchar2) is v_count number(10); executesql varchar2(4000); begin select count(*) into v_count from user_tables where table_name = tablename;  if v_count > 0 then executesql:='drop table ' ||'"'||tablename||'"'; execute immediate executesql; end if;   end proc_droptableifexists;
/
create or replace PROCEDURE PROC_DROPSCHTABLEIFEXISTS (tablename varchar2) is BEGIN EXECUTE IMMEDIATE 'DROP TABLE '||tablename; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;
/
