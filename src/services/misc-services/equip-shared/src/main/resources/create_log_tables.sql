CREATE TABLE audit_entry (
   audit_entry_id INTEGER GENERATED ALWAYS AS IDENTITY,
   system_key varchar2(200) NOT NULL,
   entity_id VARCHAR2(1000) NOT NULL,
   entity_type VARCHAR2(400) NOT NULL,
   entity_version VARCHAR2(400),
   action VARCHAR2(1000) NOT NULL,
   action_status VARCHAR2(100),
   create_date DATE NOT NULL,
   user_id VARCHAR2(100) NOT NULL,
   first_name VARCHAR2(250) NOT NULL,
   last_name VARCHAR2(250) NOT NULL,
   email_address VARCHAR2(500) NOT NULL,
   script_id VARCHAR2(1000),
   context_entity_id VARCHAR2(1000),
   hostname VARCHAR2(250),
   operating_system VARCHAR2(250),
   execution_engine_name VARCHAR2(250),
   execution_engine_version NUMBER(20),
   runtime_environment VARCHAR2(400),
   runtime_path VARCHAR2(400),
   content clob,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   CONSTRAINT pk_audit_entry_id PRIMARY KEY (audit_entry_id)
);

CREATE OR REPLACE TRIGGER audit_entry_trg1
BEFORE INSERT OR UPDATE ON audit_entry
FOR EACH ROW
BEGIN
	IF INSERTING THEN
		:new.created_on := current_timestamp;
	END IF;
	IF UPDATING THEN
		:new.modified_on := current_timestamp;
	END IF;
END;
/