# Testing Documentation

This guide describes how to run automated tests for both the backend and frontend.

## Backend Unit & Integration Tests (JUnit 5 + Mockito)
Tests verify security, timetable entries configuration, clash checkers, and rate limiters.
* Command to run test suites:
  ```bash
  mvn test
  ```

## Frontend Unit & Component Tests (Vitest + React Testing Library)
Tests verify user interfaces, marking options, filters, and rendering states.
* Command to run frontend test suites:
  ```bash
  npm run test
  ```
