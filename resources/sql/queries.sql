-- Place your queries here. Docs available https://www.hugsql.org/
-- :name upsert-user :returning-execute
insert into user (email, first_name, last_name, pic)
values (:email, :given_name, :family_name, :picture)
on conflict(email)
do update
set first_name = :given_name,
last_name = :family_name,
pic = :picture
returning user_id;

-- :name upsert-fragment :execute
insert into fragment (fragment_id, file_id, fragment, page)
values (:fragment_id, :file_id, :fragment, :page)
on conflict(fragment_id)
do update
set fragment = :fragment,
page = :page;

-- :name get-user-by-id :query :one
select * from user where user_id = :user_id;

-- :name create-project :returning-execute
insert into project (name) values (:project-name)
returning project_id;

-- :name update-project :execute
update project set name = :name where project_id = :project_id;

-- :name get-projects :query
select * from project;

-- :name get-project :query :one
select * from project;

-- :name get-project-by-name :query :one
select project_id from project where name = :new-project-name;

-- :name get-project-by-id :query :one
select name from project where project_id = :project_id;

-- :name get-question :query :one
select * from question where question_id = :question_id;

-- :name get-questions :query
select * from question where project_id = :project_id;

-- :name get-questions-all :query
select * from question;

-- :name insert-question :execute
insert into question (project_id, question) values (:project_id, :question);

-- :name update-question :execute
update question set question = :question where question_id = :question_id;

-- :name insert-file :execute
insert into file (question_id, filename) values (:question_id, :filename);

-- :name get-files :query
select * from file where question_id = :question_id;

-- :name get-file :query :one
select * from file where file_id = :file_id;

-- :name get-fragments :query
select * from fragment where file_id = :file_id;

-- :name insert-soft-link :execute
insert into soft_link (file_id, q) values (:file_id, :q);

-- :name get-soft-links :query
select * from soft_link where file_id = :file_id;
