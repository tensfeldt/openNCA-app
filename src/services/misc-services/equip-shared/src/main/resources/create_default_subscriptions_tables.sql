create table security_role_event_type (
   security_role_event_id integer generated always as identity,
   security_role_id integer  not null,
   event_type_id number(38) not null,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint security_role_event_fk1 foreign key (security_role_id) references security_role (security_role_id) on delete cascade,
   constraint security_role_event_fk2 foreign key (event_type_id) references event_type (event_type_id) on delete cascade
);