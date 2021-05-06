-- drop table operational_metadata_job;

create table operational_metadata_job (
   operational_metadata_job_id        integer generated always as identity,
   start_date                          date,
   end_date                        date,
   status                             varchar2(400) not null,
   error                              clob,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint operational_metadata_job_id_pk primary key (operational_metadata_job_id)
);

CREATE OR REPLACE TRIGGER operational_metadata_job_trg1
BEFORE INSERT OR UPDATE ON operational_metadata_job
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