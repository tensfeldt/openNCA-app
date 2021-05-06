CREATE TABLE lock_control (
   lock_id INTEGER GENERATED ALWAYS AS IDENTITY,
   lock_name VARCHAR(128) NOT NULL UNIQUE,
   lock_flag  NUMERIC(1) NOT NULL CHECK (lock_flag IN (0,1)),
   CONSTRAINT lock_id_pk PRIMARY KEY (lock_id)
);

CREATE TABLE event_type (
   event_type_id INTEGER GENERATED ALWAYS AS IDENTITY,
   event_type_name VARCHAR(128) NOT NULL UNIQUE,
   event_category VARCHAR(128) NOT NULL,
   event_description VARCHAR(128) NOT NULL,
   global_flag NUMERIC(1) NOT NULL CHECK (global_flag IN (0,1)),
   active NUMERIC(1) default 1 NOT NULL CHECK (active IN (0,1)),
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   CONSTRAINT event_type_id_pk PRIMARY KEY (event_type_id)
);

CREATE TABLE event (
   event_id INTEGER GENERATED ALWAYS AS IDENTITY,
   component_name VARCHAR(128) NOT NULL,
   event_type_id INTEGER NOT NULL,
   entity_id VARCHAR(255),
   entity_type VARCHAR(128),
   study_id VARCHAR (128),
   program_number VARCHAR (128),
   description VARCHAR(4000),
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   attachment_content BLOB,
   attachment_mime_type VARCHAR2(100),
   attachment_filename VARCHAR2(100),
   CONSTRAINT event_id_pk PRIMARY KEY (event_id),
   CONSTRAINT event_type_id_fk FOREIGN KEY (event_type_id) REFERENCES event_type(event_type_id)
);

CREATE TABLE notification_type (
   notification_type_id INTEGER GENERATED ALWAYS AS IDENTITY,
   notification_type_name VARCHAR(128) NOT NULL UNIQUE,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   CONSTRAINT notification_type_id_pk PRIMARY KEY (notification_type_id)
);

CREATE TABLE subscription (
   subscription_id INTEGER GENERATED ALWAYS AS IDENTITY,
   event_type_id INTEGER NOT NULL,
   notification_type_id INTEGER NOT NULL,
   study_id VARCHAR(128),
   program_number VARCHAR(128),
   artifact_id VARCHAR(128),
   user_id VARCHAR(128) NOT NULL,
   email VARCHAR(1000),
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   CONSTRAINT event_type_id2_fk FOREIGN KEY (event_type_id) REFERENCES event_type(event_type_id),
   CONSTRAINT notification_type_id2_fk FOREIGN KEY (notification_type_id) REFERENCES notification_type(notification_type_id),
   CONSTRAINT subscription_id_pk PRIMARY KEY (subscription_id),
   CONSTRAINT subscription_uk1 UNIQUE (event_type_id, notification_type_id, study_id, program_number, artifact_id, user_id, email)
);

CREATE TABLE notification (
   notification_id INTEGER GENERATED ALWAYS AS IDENTITY,
   event_id INTEGER NOT NULL,
   notification_type_id INTEGER NOT NULL,
   processed NUMERIC(1) NOT NULL CHECK (processed IN (0,1)),
   processed_on TIMESTAMP,
   subscription_id INTEGER NOT NULL,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   CONSTRAINT event_id_fk FOREIGN KEY (event_id) REFERENCES event(event_id),
   CONSTRAINT notification_type_id_fk FOREIGN KEY (notification_type_id) REFERENCES notification_type(notification_type_id),
   CONSTRAINT subscription_id_fk FOREIGN KEY (subscription_id) REFERENCES subscription(subscription_id),
   CONSTRAINT notification_id_pk PRIMARY KEY (notification_id)
);
CREATE INDEX idx_notif_subscription_id ON notification(subscription_id);
CREATE INDEX idx_subs_event_type_id ON subscription(event_type_id);
CREATE INDEX idx_subs_program_number ON subscription(program_number);
CREATE INDEX idx_subs_artifact_id ON subscription(artifact_id);
CREATE INDEX idx_subs_user_id ON subscription(user_id);

CREATE OR REPLACE TRIGGER event_type_trg1
BEFORE INSERT OR UPDATE ON event_type
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

CREATE OR REPLACE TRIGGER event_trg1
BEFORE INSERT OR UPDATE ON event
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

CREATE OR REPLACE TRIGGER notification_type_trg1
BEFORE INSERT OR UPDATE ON notification_type
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

CREATE OR REPLACE TRIGGER subscription_trg1
BEFORE INSERT OR UPDATE ON subscription
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

CREATE OR REPLACE TRIGGER notification_trg1
BEFORE INSERT OR UPDATE ON notification
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