create database webhistory;
use webhistory;

create table history
(
    url varchar(100),
    accessdate date,
    accesstime time,
    title varchar(100),
    domain varchar(30),
    constraint PK_HISTORY primary key (url, accessdate, accesstime)
);


insert into history value ('adasdas', '2018/Mar/13', '17:27:09', 'adasd', 'asdads');
#delete from history where accessdate = '2018/04/13' and url = 'adasdas' and accesstime='17:27:09';

select * from history;

/*
#tet
use jdbctest;
create table testtable
(
	id int auto_increment primary key,
    username varchar(30)
);

insert into testtable(username) value('thông');
insert into testtable(username) value('sơn');
insert into testtable(username) value('thảo');
insert into testtable value(0, 'tiến');

select * from testtable;
*/