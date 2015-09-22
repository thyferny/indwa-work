create or replace procedure droptable_if_exists(varchar(any))
language nzplsql
returns boolean
as
begin_proc
declare
    v_count integer;
    v_sql varchar(999);
begin
    select count(*)
      into v_count
      from _v_table
     where tablename = $1;


    if v_count = 0 then
        return false;
    end if;


    v_sql := 'drop table "' || $1||'"';
    execute immediate v_sql;
    return true;
end;
end_proc;


create or replace procedure dropschtab_if_exists(varchar(any),varchar(any))
language nzplsql
returns boolean
as
begin_proc
declare
    v_count integer;
    v_sql varchar(999);
begin
    select count(*)
      into v_count
      from _v_table
     where  tablename  =  $2  and  owner = $1 ;


    if v_count = 0 then
        return false;
    end if;


    v_sql := 'drop table "'||$1||'"."'||$2||'"';
    execute immediate v_sql;
    return true;
end;
end_proc;

create or replace procedure droptable_if_existsdoubleq(varchar(any))
language nzplsql
returns boolean
as
begin_proc
declare
    v_count integer;
    v_sql varchar(999);
    v_tablename varchar(999);
begin

		v_tablename:= substring($1 from 2 for length($1)-2);

    select count(*)
      into v_count
      from _v_table
     where tablename = v_tablename;


    if v_count = 0 then
        return false;
    end if;


    v_sql := 'drop table ' || $1;
    execute immediate v_sql;
    return true;
end;
end_proc;


create or replace procedure dropview_if_existsdoubleq(varchar(any))
language nzplsql
returns boolean
as
begin_proc
declare
    v_count integer;
    v_sql varchar(999);
    v_viewname varchar(999);
begin

		v_viewname:= substring($1 from 2 for length($1)-2);

    select count(*)
      into v_count
      from _v_view
     where viewname = v_viewname;


    if v_count = 0 then
        return false;
    end if;


    v_sql := 'drop view ' || $1;
    execute immediate v_sql;
    return true;
end;
end_proc;