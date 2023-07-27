insert into users (id, name, password_hash, active, email, created_at, display_name, location, about_me, link)
values (1,
        'Kongmeng Said',
        '1115',
        true,
        'Kongmeng.Said@chemical.kzc',
        '2021-01-08 04:05:06',
        'Kongmeng Said',
        'Copperopolis',
        'Saving miscellaneous atmosphere http kodak thank.',
        'https://nearlyfjcmjynn.sn');

insert into users (id, name, password_hash, active, email, created_at, display_name, location, about_me, link)
values (2,
        'Demorrio Wyble',
        '2090284847',
        true,
        'Demorrio.Wyble@sociology.dbf',
        '2022-01-08 04:05:06',
        'Demorrio Wyble',
        'Melvin Village',
        'Reproduce msie roommates cardiac instrumentation effort claim.',
        'https://reportshcx.ch');

insert into users (id, name, password_hash, active, email, created_at, display_name, location, about_me, link)
values (3,
        'Synthia Olmos',
        '1086335466',
        true,
        'Synthia_Olmos@ryan.iu',
        '2023-01-08 04:05:06',
        'Synthia Olmos',
        'Launceston',
        'Symptoms ghana linear health replaced imports webmaster, pike served payday.',
        'https://reportshcx.ch');

insert into content (id, text, author_id, created_at)
values (100,
        'Please tell me which programming language is best',
        1,
        '2023-07-09 03:05:06');

insert into content (id, text, author_id, created_at)
values (10000,
        'Java is best',
        2,
        '2023-09-09 04:05:06');

insert into content (id, text, author_id, created_at)
values (10001,
        'Kotlin is best',
        3,
        '2023-09-09 04:05:07');

insert into content (id, text, author_id, created_at)
values (10002,
        'Oracle is best',
        2,
        '2023-09-09 04:05:06');

insert into content (id, text, author_id, created_at)
values (10003,
        'SQL Server is best',
        3,
        '2023-09-09 04:05:07');

insert into content (id, text, author_id, created_at)
values (10004,
        'AWS is best',
        2,
        '2023-09-09 04:05:06');

insert into content (id, text, author_id, created_at)
values (10005,
        'Azure is best',
        3,
        '2023-09-09 04:05:07');

insert into content (id, text, author_id, created_at)
values (101,
        'Please tell me which database is best',
        1,
        '2023-08-09 03:05:06');

insert into content (id, text, author_id, created_at)
values (102,
        'Please tell me which cloud provider is best',
        1,
        '2023-09-09 03:05:06');

insert into question (id, content, title)
values (10,
        100,
        'Which programming language is best?');

insert into question (id, content, title)
values (11,
        101,
        'Which database is best?');

insert into question (id, content, title)
values (12,
        102,
        'Which cloud provider is best?');

insert into answer (id, question, data, accepted)
values (1000,
        10,
        10000,
        false);

insert into answer (id, question, data, accepted)
values (1001,
        10,
        10001,
        false);

insert into answer (id, question, data, accepted)
values (1002,
        11,
        10000,
        false);

insert into answer (id, question, data, accepted)
values (1003,
        11,
        10001,
        false);

insert into answer (id, question, data, accepted)
values (1004,
        12,
        10000,
        false);

insert into answer (id, question, data, accepted)
values (1005,
        12,
        10001,
        false);

insert into vote (id, voter, content, "value", created_at)
values (1,
        3,
        10001,
        1,
        '2023-08-09 06:05:06');

insert into vote (id, voter, content, "value", created_at)
values (2,
        2,
        10001,
        1,
        '2023-08-09 06:05:06');