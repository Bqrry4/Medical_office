Use pos;

Create Table Patients(
	cnp char(13) primary key unique,
	id_user int unique, #So called foreign key,
	last_name varchar(50),
	first_name varchar(50),
	email varchar(70) unique, 
	phone char(10),
	birth_date date,
	is_active boolean default(true),
	Constraint CHK_Phone check (phone like '07%'),
	Constraint CHK_Email check (email like '%@%.%')
);

Create Table Physicians(
	id_physician int primary key AUTO_INCREMENT,
	id_user int unique, #So called foreign key,
	last_name varchar(50),
	first_name varchar(50),
	email varchar(70) unique,
	phone char(10),
	specialization char(10),
	Constraint CHK_Phone check (phone like '07%'),
	Constraint CHK_Email check (email like '%@%.%')
);

Create Table Appointments(
	id_patient varchar(13),
	id_physician int,
	ap_date datetime,
	status ENUM('onorată', 'neprezentat', 'anulată'),
	Constraint fk_id_patient foreign key (id_patient) references Patients(cnp),
	Constraint fk_id_physician foreign key (id_physician) references Physicians(id_physician),
	PRIMARY KEY (id_patient, id_physician, ap_date)
);


delimiter //

Create trigger bdate_insert_check
	before insert on Patients
	for each row
	begin
	if (new.birth_date > CURDATE() - interval 18 year) then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Patient must be at least 18 years old!';
	end if;
end;

Create trigger bdate_update_check
	before update on Patients
	for each row
	begin
	if (new.birth_date > CURDATE() - interval 18 year) then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Patient must be at least 18 years old!';
	end if;
end;


Create trigger pdate_insert_check
	before insert on Appointments
	for each row
	begin 
	if (new.ap_date - interval 15 minute <= NOW())  then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Appointment cannot be created in the past and only 15 minutes from now!';
	end if;
end;

Create trigger pdate_update_check
	before update on Appointments
	for each row
	begin 
	if (new.ap_date - interval 15 minute <= NOW())  then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Appointment cannot be created in the past and only 15 minutes from now!';
	end if;
end;


Create trigger pdate_overlap_inserted_check
	before insert on Appointments
	for each row
	begin 
	
	if (
	   Select COUNT(ap_date) from Appointments a
	   Where a.id_physician = new.id_physician
	   and a.ap_date <= new.ap_date + interval 15 minute 
	   and a.ap_date >= new.ap_date - interval 15 minute
	) > 0  then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Appointment time is overlapping with other appointments!';
	end if;
	
	if(
	   Select COUNT(ap_date) from Appointments a
	   Where new.id_patient = a.id_patient
	   and DATE(new.ap_date) = DATE(a.ap_date)
	   and new.id_physician = a.id_physician
	) > 0 then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Multiple appointments from one patient to the same physician within a day are not allowed';
	end if;
	
end;

Create trigger pdate_overlap_update_check
	before update on Appointments
	for each row
	begin 
	if (
	   Select COUNT(ap_date) from Appointments a
	   Where a.id_physician = new.id_physician
	   and a.ap_date <= new.ap_date + interval 15 minute 
	   and a.ap_date >= new.ap_date - interval 15 minute
	) > 0  then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Appointment time is overlapping with other appointments!';
	end if;
	
	if(
	   Select COUNT(ap_date) from Appointments a
	   Where new.id_patient = a.id_patient
	   and DATE(new.ap_date) = DATE(a.ap_date)
	   and new.id_physician = a.id_physician
	) > 0 then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Multiple appointments from one patient to the same physician within a day are not allowed';
	end if;
	
end;


Create trigger appointment_remove_check
	before delete on Appointments
	for each row
	begin
	if (old.ap_date < NOW()) then
	SIGNAL SQLSTATE '45000' set MESSAGE_TEXT = 'Cannot cancel an appointment from the past';
	end if;
end;
//

delimiter ;

