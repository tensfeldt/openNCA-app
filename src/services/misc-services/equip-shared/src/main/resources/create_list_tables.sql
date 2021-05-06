CREATE TABLE list_value (
   id INTEGER GENERATED ALWAYS AS IDENTITY,
   display_text VARCHAR(128) NOT NULL,
   text VARCHAR(128) NOT NULL,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   CONSTRAINT id_pk PRIMARY KEY (id)
);

CREATE TABLE list_name (
	id INTEGER GENERATED ALWAYS AS IDENTITY,
	name VARCHAR(128) NOT NULL UNIQUE,
    created_on TIMESTAMP NOT NULL,
    created_by VARCHAR2(100) NOT NULL,
    modified_on TIMESTAMP NOT NULL,
    modified_by VARCHAR2(100) NOT NULL,
	CONSTRAINT list_name_id_pk PRIMARY KEY (id)
);

CREATE TABLE list (
	list_name_id INTEGER NOT NULL,
	list_value_id INTEGER NOT NULL,
    created_on TIMESTAMP NOT NULL,
    created_by VARCHAR2(100) NOT NULL,
    modified_on TIMESTAMP NOT NULL,
    modified_by VARCHAR2(100) NOT NULL,
	CONSTRAINT name_value_pk PRIMARY KEY (list_name_id, list_value_id),
	CONSTRAINT list_value_id_fk FOREIGN KEY (list_value_id) REFERENCES list_value(id),
    CONSTRAINT list_name_id_fk FOREIGN KEY (list_name_id) REFERENCES list_name(id)
);

CREATE OR REPLACE TRIGGER list_value_trg1
BEFORE INSERT OR UPDATE ON list_value
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

CREATE OR REPLACE TRIGGER list_name_trg1
BEFORE INSERT OR UPDATE ON list_name
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

CREATE OR REPLACE TRIGGER list_trg1
BEFORE INSERT OR UPDATE ON list
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