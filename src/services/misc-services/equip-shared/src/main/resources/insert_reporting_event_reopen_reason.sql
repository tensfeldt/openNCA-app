DECLARE
    list_name varchar(128) := 'reporting_event_reopen_reason';
    name_id integer;
    value_id integer;
    type valueArray is varray(6) of varchar(128);
    list_values valueArray := valueArray('Change to Clinical Database', 'Publish/Unpublish Concentration Data Change to Concentration Data', 
        'Update Identification Status', 'Publish/Unpublish Parameter Data', 'Change to Parameter Data', 'Other - Refer to Database Change Form');
BEGIN
    insert into list_name(name) values(list_name) returning id into name_id;
    for i in 1..list_values.count loop
        insert into list_value(display_text, text) values (list_values(i), list_values(i)) returning id into value_id;
        insert into list (list_name_id, list_value_id) values (name_id, value_id);
    end loop;
    commit;
END;
/
