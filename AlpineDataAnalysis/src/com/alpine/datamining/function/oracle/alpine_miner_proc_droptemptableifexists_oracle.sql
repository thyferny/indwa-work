 create or replace procedure proc_droptemptableifexists(tablename varchar2) is
   v_count number(10);
 begin
   select count(*)
     into v_count
     from user_objects
    where object_name = upper(tablename);
   if v_count > 0 then
     execute immediate 'drop table ' || tablename;
   end if;
 end proc_droptemptableifexists;
/
