Use IDM;

Create Table Users(
	userID int primary key AUTO_INCREMENT,
	username varchar(50) unique not null,
	password varchar(50) not null,
	role ENUM('admin', 'patient', 'physician')
);


Insert into Users (username, password, role) 
VALUES 
	('admin', 'imagod', 'admin'),
	('patientDef', 'password', 'patient'),
	('physicianDef', 'password', 'physician'),
	('physicianDef2', 'password', 'physician'),
	('physicianDef3', 'password', 'physician'),
	('physicianDef4', 'password', 'physician'),
	('physicianDef5', 'password', 'physician'),
	('physicianDef6', 'password', 'physician');
	
