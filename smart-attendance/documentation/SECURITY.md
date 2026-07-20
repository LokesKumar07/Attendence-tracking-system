# Security Audit Policy

SmartAttend implements top security standards based on OWASP recommendations.

## Mitigation Details
* **SQL Injection**: Prevented using Spring Data JPA parameterized bindings. Plain text SQL concatenations are strictly prohibited.
* **Brute-Force Lockout**: Teachers are temporarily locked out for 15 minutes after 5 consecutive failed login attempts.
* **JWT Expiration & Rotation**: Access tokens expire in 15 minutes. Refresh tokens rotate on every handshake.
* **XSS Mitigation**: Input validation using Zod on frontend and standard Spring Bean validation on backend controllers.
* **CSV Formula Injection**: All exported string cells are sanitized against leading `=`, `+`, `-`, or `@` symbols by appending single quotes `'`.
* **Idempotency Keys**: Submit headers require a unique client token to prevent accidental double-marking over network breaks.
