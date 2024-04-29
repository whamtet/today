-- :name delete-question :execute
delete from question where question_id = :question_id

-- :name delete-fragment :execute
delete from fragment where fragment_id = :fragment_id
