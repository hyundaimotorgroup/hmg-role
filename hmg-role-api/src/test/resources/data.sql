CREATE SEQUENCE IF NOT EXISTS jv_commit_pk_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS jv_snapshot_pk_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS jv_global_id_pk_seq START WITH 1 INCREMENT BY 1;

INSERT INTO projects (project_id, project_key, project_name, is_deleted) VALUES (50, 'hmg-notice-1', 'HMG Notice 1', 0);
INSERT INTO projects (project_id, project_key, project_name, is_deleted) VALUES (51, 'hmg-notice-2', 'HMG Notice 2', 0);

INSERT INTO members (member_name, api_key, is_deleted, project_id, member_key) VALUES ('member', '22ee17bd-699e-4b6b-a30b-e854e6c576c8', 0, 50, 'member1');
INSERT INTO members (member_name, api_key, is_deleted, project_id, member_key) VALUES ('member1', '22ee17bd-699e-4b6b-a30b-e854e6c57552', 0, 50, 'member2');
INSERT INTO members (member_name, api_key, is_deleted, project_id, member_key) VALUES ('member2', '22ee17bd-699e-4b6b-a30b-e854e6c57722', 0, 51, 'member3');
INSERT INTO members (member_name, api_key, is_deleted, project_id, member_key) VALUES ('member2', '22ee17bd-699e-4b6b-a30b-e854e6c57227', 0, 51, 'member4');