DECLARE
    list_name varchar(128) := 'supplemental_data_type';
    name_id integer;
    value_id integer;
    type valueArray is varray(10) of varchar(128);
    list_values valueArray := valueArray('Demographic','Dosing','Inclusion/Exclusion','Molecular Weight Data','Pharmacogenomics','Protein Binding','Reconciliation/Monitoring/Population','Special Population Data','Treatment Data');
BEGIN
    insert into list_name(name) values(list_name) returning id into name_id;
    for i in 1..list_values.count loop
        insert into list_value(display_text, text) values (list_values(i), list_values(i)) returning id into value_id;
        insert into list(LIST_NAME_ID, LIST_VALUE_ID) values (name_id, value_id);
    end loop;
    commit;
END;
/