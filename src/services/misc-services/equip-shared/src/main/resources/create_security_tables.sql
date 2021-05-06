prompt Uncomment delete lines to refresh
-- drop table security_group_access;
-- drop table security_role_priv;
-- drop table security_group;
-- drop table security_role;
-- drop table security_priv;

create table security_group (
   security_group_id    integer generated always as identity,
   group_name           varchar2(1000) not null,
   external_group_name  varchar2(1000) not null,
   active_yn            varchar2(1) default 'Y' not null,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint security_group_id_pk primary key (security_group_id),
   constraint security_group_uk1 unique (group_name),
   constraint security_group_uk2 unique (external_group_name),
   constraint security_group_uk3 unique (group_name, external_group_name)
);

create table security_group_access (
   security_group_access_id  integer generated always as identity,
   security_group_id                number not null,
   entity_id               varchar2(255),
   entity_type             varchar2(128),
   data_source_id          number,
   restricted_access_yn    varchar2(1) default 'N',
   blinded_access_yn       varchar2(1) default 'N',
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint pk_group_access primary key (security_group_access_id),
   constraint security_group_access_uk1 unique (security_group_id, entity_id, entity_type),
   constraint security_group_access_fk1 foreign key (security_group_id) references security_group (security_group_id) on delete cascade
);

create table security_role (
   security_role_id    integer generated always as identity,
   system_key          varchar2(100) not null,            
   role_name           varchar2(1000) not null,
   external_group_name varchar2(1000) not null,
   active_yn           varchar2(1) default 'Y' not null,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint security_role_id_pk primary key (security_role_id),
   constraint security_role_uk1 unique (system_key, role_name),
   constraint security_role_uk2 unique (system_key, external_group_name),
   constraint security_role_uk3 unique (system_key, role_name, external_group_name)
);

create table security_priv (
   security_priv_key     varchar2(100),
   system_key            varchar2(100),
   description           varchar2(4000),
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint security_priv_key_pk primary key (system_key, security_priv_key)
);

create table security_role_priv (
   security_role_priv_id integer generated always as identity,
   system_key            varchar2(100) not null,
   security_role_id      integer not null,
   security_priv_key     varchar2(100) not null,
   active_yn             varchar2(1) default 'Y' not null,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint security_role_priv_id_pk primary key (security_role_priv_id),
   constraint security_role_priv_uk1 unique (system_key, security_role_id, security_priv_key),
   constraint security_role_priv_fk1 foreign key (system_key, security_priv_key) references security_priv (system_key, security_priv_key) on delete cascade,
   constraint security_role_priv_fk2 foreign key (security_role_id) references security_role (security_role_id) on delete cascade
);

 -- create or replace synonym equip_user.security_group for equip_owner.security_group;
 -- create or replace synonym equip_user.security_role for equip_owner.security_role;

CREATE OR REPLACE TRIGGER security_group_trg1
BEFORE INSERT OR UPDATE ON security_group
FOR EACH ROW
BEGIN
	IF INSERTING THEN
		:new.created_on := current_timestamp;
		:new.modified_on := current_timestamp;

      IF :old.created_by is null then
         :new.created_by := user;
         :new.modified_by := user;
      END IF;
	END IF;
	IF UPDATING THEN
		:new.modified_on := current_timestamp;

      IF :old.modified_by is null then
         :new.modified_by := user;
      END IF;
	END IF;
END;
/

CREATE OR REPLACE TRIGGER security_group_access_trg1
BEFORE INSERT OR UPDATE ON security_group_access
FOR EACH ROW
BEGIN
	IF INSERTING THEN
		:new.created_on := current_timestamp;
		:new.modified_on := current_timestamp;

      IF :old.created_by is null then
         :new.created_by := user;
         :new.modified_by := user;
      END IF;
	END IF;
	IF UPDATING THEN
		:new.modified_on := current_timestamp;

      IF :old.modified_by is null then
         :new.modified_by := user;
      END IF;
	END IF;
END;
/

CREATE OR REPLACE TRIGGER security_role_trg1
BEFORE INSERT OR UPDATE ON security_role
FOR EACH ROW
BEGIN
	IF INSERTING THEN
		:new.created_on := current_timestamp;
		:new.modified_on := current_timestamp;

      IF :old.created_by is null then
         :new.created_by := user;
         :new.modified_by := user;
      END IF;
	END IF;
	IF UPDATING THEN
		:new.modified_on := current_timestamp;

      IF :old.modified_by is null then
         :new.modified_by := user;
      END IF;
	END IF;
END;
/

CREATE OR REPLACE TRIGGER security_priv_trg1
BEFORE INSERT OR UPDATE ON security_priv
FOR EACH ROW
BEGIN
	IF INSERTING THEN
		:new.created_on := current_timestamp;
		:new.modified_on := current_timestamp;

      IF :old.created_by is null then
         :new.created_by := user;
         :new.modified_by := user;
      END IF;
	END IF;
	IF UPDATING THEN
		:new.modified_on := current_timestamp;

      IF :old.modified_by is null then
         :new.modified_by := user;
      END IF;
	END IF;
END;
/

CREATE OR REPLACE TRIGGER security_role_priv_trg1
BEFORE INSERT OR UPDATE ON security_role_priv
FOR EACH ROW
BEGIN
	IF INSERTING THEN
		:new.created_on := current_timestamp;
		:new.modified_on := current_timestamp;

      IF :old.created_by is null then
         :new.created_by := user;
         :new.modified_by := user;
      END IF;
	END IF;
	IF UPDATING THEN
		:new.modified_on := current_timestamp;

      IF :old.modified_by is null then
         :new.modified_by := user;
      END IF;
	END IF;
END;
/