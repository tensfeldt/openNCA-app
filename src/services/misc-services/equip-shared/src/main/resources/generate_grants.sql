-- execute as equip_owner
-- WARNING: Only use as a convenience to generate and verify, actual deployment SQL should be static.
set serveroutput on
set lines 200 pages 5000
DECLARE
   sql_text varchar2(4000);
BEGIN
   if (USER != 'EQUIP_OWNER') then
      raise_application_error(-20000, 'This script must be executed by equip_owner'); 
   end if;
   for idx in (select table_name from user_tables) loop
      sql_text := 'grant select, insert, delete, update on equip_owner.'||idx.table_name||' to equip_user';
      dbms_output.put_line(sql_text||';');
      -- execute immediate sql_text;
   end loop;
   commit;
END;
/
