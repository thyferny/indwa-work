 create or replace procedure proc_dropviewifexists(tablename varchar2) is
   v_count    number(10);
   executesql varchar2(4000);
 begin
   select count(*)
     into v_count
     from user_views
    where view_name = tablename;
   if v_count > 0 then
     executesql := 'drop view ' || '"' || tablename || '"';
     execute immediate executesql;
   end if;
 end proc_dropviewifexists;
/
