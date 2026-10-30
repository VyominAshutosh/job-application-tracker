# Walkthrough

Read this once with the code open. It is written to be understood, not memorised.

---

## 1. What happens when one request comes in

```
Browser / Postman
      |  HTTP:  PATCH /api/applications/1/status   {"status":"ASSESSMENT"}
      v
JobApplicationController      turns HTTP into Java, and Java back into HTTP
      |  changeStatus(1, ASSESSMENT)
      v
JobApplicationService         decides whether the move is allowed
      |  findById(1), setStatus(...), save(...)
      v
JobApplicationRepository      an interface with no code in it; Spring writes the SQL
      |  SELECT / UPDATE
      v
PostgreSQL
```

The whole point of the split: each layer has one job. The controller knows nothing about
pipeline rules. The service knows nothing about HTTP. The repository knows nothing about either.

---

## 2. Layer by layer

### `entity/`: the table

`JobApplication` is a normal Java class with `@Entity` on it. Hibernate reads those
annotations and creates the matching table. `@Column(nullable = false)` becomes `NOT NULL`.
`interviewDate` has no `nullable = false`, so it stays empty until an interview is scheduled.

`ApplicationStatus` is an enum, and it carries the rule itself:

```java
case APPLIED -> Set.of(ASSESSMENT, REJECTED);
```

The rule lives in one place. The service asks the enum; it never keeps its own copy of it.

`@Enumerated(EnumType.STRING)` matters. Without it JPA stores `0, 1, 2`, and the day you
insert a new enum constant in the middle, every old row silently means something else.

### `repository/`: the queries

```java
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> { }
```

You never write a class for this. At startup Spring Data reads the interface and hands the
service a generated implementation. Three kinds of method are in there:

- `findById`, `save`, `deleteById`: inherited, already written.
- `findByStatusOrderByAppliedOnDesc`: a **derived query**. Spring parses the method *name*
  and builds the SQL from it.
- `search(...)`: a **JPQL query** you wrote by hand, because the derived name for it would
  be unreadable. JPQL looks like SQL, but it names the entity class and its fields, not the
  table and its columns.

### `service/`: the rules

The only class where a decision is made. Two things worth noticing.

**Constructor injection.** The service does not create its repository, it receives one:

```java
public JobApplicationService(JobApplicationRepository repository) { ... }
```

That is inversion of control. Spring builds the object graph; your class just declares what
it needs. The field can be `final`, and the class can be tested with a fake repository.

**Find, change, save.** `update()` and `changeStatus()` both load the row with `findById`,
change fields with setters, and call `save()`. Because the object already has an id,
`save()` runs an `UPDATE` instead of an `INSERT`.

### `dto/`: what crosses the wire

DTOs are records: compact classes with final fields.

The entity never leaves the service. Three reasons, and an interviewer will want at least two:

1. The API shape is not tied to the table shape, so renaming a column does not break the React page.
2. You never leak a field you did not mean to expose.
3. The request can leave out fields the client must not set. `ApplicationRequest` has no
   `status` field, so a new application always enters at `APPLIED`, and only the status
   endpoint can move it. A rule that cannot be broken beats a rule that is checked.

`ApplicationResponse` sends `allowedNext` with every row, so the React dropdown shows only
legal moves without knowing the rule.

### `exception/`: errors as JSON

`@RestControllerAdvice` is one class that catches exceptions from every controller and turns
them into a consistent body. Without it, a missing id surfaces as a 500 with a stack trace.

The mapping is deliberate:

- missing id -> **404 Not Found**
- bad field -> **400 Bad Request**, with a `fieldErrors` map naming each field
- illegal stage move -> **409 Conflict**, because the request is well formed but fights the
  current state of the resource

### `frontend/src/App.jsx`: the UI

One component. `useState` holds the data, `useEffect` with an empty dependency array loads it
once after the first render, and `fetch` talks to the API. After a write it simply re-loads,
which is the simplest correct thing.

`@CrossOrigin` on the controller is what lets the React page call the API from a different
origin (port 5173 locally, the Vercel address once deployed). Without it the browser blocks
the response even though the server answered.

---

## 3. Questions you will actually get

**Why three layers instead of putting it all in the controller?**
One reason each: the rules are testable without HTTP, the same service can serve a second
controller later, and a change to the database does not reach the web layer.

**How does the repository work with no implementation?**
Spring Data creates a proxy at startup, parses the method names, and generates the queries.

**Derived query vs `@Query`?**
Derived reads the method name; `@Query` is JPQL you write. Use the name while it stays
readable, switch to `@Query` when it stops being readable.

**Did you use `@Transactional`?**
No. Every write in this project is a single repository call, and Spring Data's built-in
methods like `save()` already run inside their own transaction. You add `@Transactional`
when one method makes two writes that must succeed or fail together.

**Why DTOs and not entities?**
The API is not tied to the table, no field leaks by accident, and the request DTO can leave
out fields the client must not set, like `status`.

**Why 409 and not 400 for an illegal move?**
400 means the request itself is malformed. This request is fine; it conflicts with the current
state of the resource.

**Why PATCH for the status change?**
One field changes. PUT means "replace the whole resource with this".

**What is `EnumType.STRING` for?**
The name is stored instead of the ordinal, so reordering the enum cannot corrupt old rows.

**Where does validation happen?**
`@Valid` on the controller parameter triggers Bean Validation on the DTO. The failure becomes
`MethodArgumentNotValidException`, and the advice turns it into a 400 with field names.

**How is it deployed?**
The Spring Boot API runs in a Docker container on Render, the database is PostgreSQL on Neon,
and the React build is on Vercel. Nothing is hardcoded: the database URL, user and password
come from environment variables, and so does the API address the React page calls.

**What would you change for production?**
`ddl-auto=update` out, Flyway in. Add authentication so applications belong to a user.
Paginate the list endpoint. Add tests.

**What is the hardest bug you hit?**
Only answer with a bug you actually hit. The most likely one here is CORS: the API answers,
but the browser blocks the response until `@CrossOrigin` allows the page's origin.

---

## 4. If you get time, in this order

1. **Tests.** One `@DataJpaTest` on the repository, and one plain unit test on the service
   with a mock repository, checking that an illegal move throws. "I wrote tests" is worth saying.
2. **Status history.** A second table with one row per move, linked with `@OneToMany` and
   `@ManyToOne`. Add it only after the Spring Data JPA relationships lecture.
3. **Pagination.** `Page<JobApplication> findAll(Pageable p)` and a `page`/`size` parameter.
4. **Spring Security with JWT**, so applications belong to a user. This is the big one, and
   also the one that eats a week.

Do not add all of them. One finished thing beats three half things.
