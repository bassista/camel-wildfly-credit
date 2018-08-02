create table if not exists example.credit_score (
  ssn varchar(11) not null,
  score int not null,
  version datetime,
  primary key (ssn, version)
);