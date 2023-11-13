Use pos;

Create Table Patients(
	cnp char(13) primary key,
	id_user int, #So called foreign key,
	last_name varchar(50),
	first_name varchar(50),
	email varchar(70) unique, 
	phone char(10),
	birth_day date,
	is_active boolean
);

Create Table Physicians(
	id_physician int primary key AUTO_INCREMENT,
	id_user int, #So called foreign key,
	last_name varchar(50),
	first_name varchar(50),
	email varchar(70) unique,
	phone char(10),
	specialization char(10)
);

Create Table Appointments(
	id_patient varchar(13),
	id_physician int,
	data Date,
	status ENUM('onorată', 'neprezentat', 'anulată'),
	Constraint fk_id_patient foreign key (id_patient) references Patients(cnp),
	Constraint fk_id_physician foreign key (id_physician) references Physicians(id_physician)
);


delimiter //
CREATE TRIGGER bdate_check
	BEFORE INSERT ON Patients
	FOR EACH ROW
	BEGIN
	IF (NEW.birth_day > CURDATE() - interval 18 year) THEN
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid date!';
	END IF;
END;


CREATE TRIGGER pdate_check
	BEFORE INSERT ON Appointments
	FOR EACH ROW
	BEGIN
	IF NEW.data < CURDATE() THEN
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid date!';
	END IF;
END;

//

