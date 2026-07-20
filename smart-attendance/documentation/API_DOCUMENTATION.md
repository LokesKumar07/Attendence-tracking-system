# REST API Endpoint Documentation

All requests expect headers containing JSON formats. Authenticated paths require access tokens:
`Authorization: Bearer <JWT>`

## Auth Module (`/api/v1/auth`)
* `POST /login`: Log in teacher. Returns accessToken, refreshToken.
* `POST /logout`: Revoke active tokens.
* `POST /refresh`: Handshake rotate refresh-token.
* `POST /forgot-password`: Generates reset token (simulated in logs).
* `POST /reset-password`: Set new password using token.

## Students Module (`/api/v1/students`)
* `GET /`: List students. Supports filters: `isActive`, `departmentId`, `classSectionId`, `search`.
* `GET /{id}`: Details of student.
* `POST /`: Add new student record.
* `PUT /{id}`: Update student record.
* `DELETE /{id}`: Delete or soft-deactivate student.
* `POST /import`: Import students from CSV or Excel spreadsheet.

## Attendance Module (`/api/v1/attendance`)
* `GET /sessions`: Fetch sessions list for specified date parameter.
* `POST /sessions/open`: Generate and open class session.
* `POST /sessions/{id}/submit`: Save draft or final submit attendance lists.
* `POST /corrections`: Apply status correction with reason.
* `GET /corrections/history`: Append-only status correction history listing.
* `GET /dashboard-status`: Check live period context.
