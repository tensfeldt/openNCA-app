-- drop table report_job_output;
-- drop table report_job;

create table report_job (
   report_job_id        integer generated always as identity,
   start_date           date,
   end_date             date,
   status               varchar2(400) not null,
   error                clob,
   compute_stdin        clob,
   compute_stdout       clob,
   compute_stderr       clob,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint report_job_id_pk primary key (report_job_id)
);

create table report_job_output (
   report_job_output_id        integer generated always as identity,
   output_id                   varchar2(36),
   output_type                 varchar2(400),
   report_job_id               integer,
   created_on TIMESTAMP NOT NULL,
   created_by VARCHAR2(100) NOT NULL,
   modified_on TIMESTAMP NOT NULL,
   modified_by VARCHAR2(100) NOT NULL,
   constraint report_job_output_id_pk primary key (report_job_id),
   constraint report_job_output_fk1 foreign key (report_job_id) references report_job (report_job_id) on delete cascade
);

CREATE OR REPLACE TRIGGER report_job_trg1
BEFORE INSERT OR UPDATE ON report_job
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

CREATE OR REPLACE TRIGGER report_job_output_trg1
BEFORE INSERT OR UPDATE ON report_job_output
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