--liquibase formatted sql
--changeset asm0dey:initial-tables splitStatements:true endDelimiter:;
CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    "name"        VARCHAR(50)           NOT NULL,
    password_hash VARCHAR(100)          NOT NULL,
    active        BOOLEAN DEFAULT FALSE NOT NULL,
    email         TEXT                  NOT NULL,
    created_at    TIMESTAMP             NOT NULL
);
ALTER TABLE users
    ADD CONSTRAINT users_name_unique UNIQUE ("name");
ALTER TABLE users
    ADD CONSTRAINT users_email_unique UNIQUE (email);
CREATE TABLE IF NOT EXISTS "role"
(
    id     SERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL
);
ALTER TABLE "role"
    ADD CONSTRAINT role_name_unique UNIQUE ("name");
CREATE TABLE IF NOT EXISTS user_role
(
    "user" BIGINT,
    "role" INT,
    CONSTRAINT pk_user_role PRIMARY KEY ("user", "role"),
    CONSTRAINT fk_user_role_user__id FOREIGN KEY ("user") REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_user_role_role__id FOREIGN KEY ("role") REFERENCES "role" (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX user_role_user ON user_role ("user");
CREATE INDEX user_role_role ON user_role ("role");
CREATE TABLE IF NOT EXISTS "content"
(
    id         BIGSERIAL PRIMARY KEY,
    "text"     TEXT      NOT NULL,
    author_id  BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_content_author_id__id FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX content_author_id ON "content" (author_id);
CREATE INDEX content_created_at ON "content" (created_at);
CREATE TABLE IF NOT EXISTS vote
(
    id         BIGSERIAL PRIMARY KEY,
    voter      BIGINT    NOT NULL,
    "content"  BIGINT    NOT NULL,
    "value"    SMALLINT  NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_vote_voter__id FOREIGN KEY (voter) REFERENCES users (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_vote_content__id FOREIGN KEY ("content") REFERENCES "content" (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX vote_voter ON vote (voter);
CREATE INDEX vote_content ON vote ("content");
CREATE TABLE IF NOT EXISTS question
(
    id        BIGSERIAL PRIMARY KEY,
    "content" BIGINT NOT NULL,
    title     TEXT   NOT NULL,
    CONSTRAINT fk_question_content__id FOREIGN KEY ("content") REFERENCES "content" (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX question_content ON question ("content");
ALTER TABLE question
    ADD CONSTRAINT question_title_unique UNIQUE (title);
CREATE TABLE IF NOT EXISTS answer
(
    id       BIGSERIAL PRIMARY KEY,
    question BIGINT NOT NULL,
    "data"   BIGINT NOT NULL,
    CONSTRAINT fk_answer_question__id FOREIGN KEY (question) REFERENCES question (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_answer_data__id FOREIGN KEY ("data") REFERENCES "content" (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX answer_question ON answer (question);
CREATE TABLE IF NOT EXISTS "comment"
(
    id     BIGSERIAL PRIMARY KEY,
    "data" BIGINT NOT NULL,
    parent BIGINT NOT NULL,
    CONSTRAINT fk_comment_data__id FOREIGN KEY ("data") REFERENCES "content" (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_comment_parent__id FOREIGN KEY (parent) REFERENCES "content" (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX comment_data ON "comment" ("data");
CREATE INDEX comment_parent ON "comment" (parent);
CREATE TABLE IF NOT EXISTS tag
(
    id     BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL
);
ALTER TABLE tag
    ADD CONSTRAINT tag_name_unique UNIQUE ("name");
CREATE TABLE IF NOT EXISTS question_tag
(
    question_id BIGINT,
    tag_id      BIGINT,
    CONSTRAINT pk_question_tag PRIMARY KEY (question_id, tag_id),
    CONSTRAINT fk_question_tag_question_id__id FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_question_tag_tag_id__id FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE RESTRICT
);
CREATE INDEX question_tag_question_id ON question_tag (question_id);
CREATE INDEX question_tag_tag_id ON question_tag (tag_id);