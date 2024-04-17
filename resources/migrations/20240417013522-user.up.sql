--;;
create table user
(
    user_id    integer primary key asc,
    email      text unique,
    first_name text,
    last_name  text,
    pic        text
);
