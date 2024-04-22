-- Place your queries here. Docs available https://www.hugsql.org/
-- :name upsert-user :returning-execute
insert into user (email, first_name, last_name, pic)
values (:email, :given_name, :family_name, :picture)
on conflict(email)
do update
set first_name = :given_name,
last_name = :family_name,
pic = :picture
returning user_id

-- :name get-user-by-id :query :one
select * from user where user_id = :user_id

-- :name create-project :execute
insert into project (name) values (:project-name)
