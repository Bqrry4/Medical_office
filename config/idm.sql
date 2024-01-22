Use IDM;

Create Table Users(
	userID int primary key AUTO_INCREMENT,
	username varchar(50) unique not null,
	password varchar(50) not null,
	role ENUM('admin', 'patient', 'physician')
);


Insert into Users (username, password, role) VALUES ('admin', 'imagod', 'admin');
