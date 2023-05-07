# Ktor Demo Application

Current data model:

![](https://kroki.io/erd/svg/eNptkbEOwiAQQPf7CuJY4uDqZlxcNerSOJztRUkoKFyrifHfhYBKjROFe--F0HrnyR2gUi0Y7Agu6P3NunaF_gzYsBoIqEOloXGETO2CAeqN1VRIECOimk6liJMALK1hMpwYpjuDxJ7P1pWZDEVxJmIiiHvLuSyH8OlANomCAXVPpR7Rwv1uczfU1j15VtbkYouMwIrjFd8jMRsrC-Nv7xeR1wxldSRVIqGQlt_O0nbd5wXkBV08zpk8GyniofFIei4mkZk8P5D8C6VgwKDe4qn8F-M7huELix-ZwA==)

It's built with Erd in [Kroki](https://kroki.io/) with the following source:

```
[User]
*id
name
passwordHash
active
email
createdAt

[Role]
*id
name

User *--+ Role

[Content]
*id
text
+author
createdAt

Content *--1 User

[Vote]
*id
+voter
+content
value
createdAt

Vote *--1 User
Vote *--1 Content

[Question]
*id
+data
title

Question 1--1 Content

[Answer]
*id
+question
+data

Question 1--* Answer
Answer 1--1 Content

[Comment]
*id
+parent
+data

Comment 1--1 Content {label: "data"}
Comment +--1 Content {label: "parent"}

[Tag]
*id
name

Question 1--* Tag
```
