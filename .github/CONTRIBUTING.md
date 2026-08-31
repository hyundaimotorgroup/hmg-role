# Contributing

Thank you for your interest in contributing.

We welcome contributions from anyone interested in helping improve the project.

Contributions may include:

- Bug reports
- Feature requests
- Documentation improvements
- Bug fixes
- New features
- Performance enhancements
- Test improvements
- Security and reliability improvements

All contributions are reviewed according to the project's technical, quality, security, and licensing requirements.

Please review this guide before creating an issue or submitting a pull request.

---

## Code of Conduct

This project is committed to providing a welcoming, inclusive, and respectful environment for all participants.

By participating in this project, you agree to follow the project's Code of Conduct.

For more information, see:

- [Code of Conduct](./CODE_OF_CONDUCT.md)

---

## Before You Contribute

Before opening an issue or submitting a pull request:

1. Search existing issues and pull requests.
2. Confirm that the issue or proposal has not already been reported.
3. Verify that your contribution aligns with the project's goals and scope.
4. Read this guide before contributing.
5. Review the Contributor License Agreement (CLA).

---

## Reporting Bugs

If you discover a bug, please create a Bug Report using the provided issue template.

A good bug report should include:

- Clear problem description
- Steps to reproduce
- Expected behavior
- Actual behavior
- Environment details
- Relevant logs or error messages
- Screenshots or recordings when applicable

Providing complete and reproducible information helps maintainers investigate and resolve issues more efficiently.

---

## Requesting Features

Feature requests are welcome.

Before submitting a feature request:

- Clearly define the problem you are trying to solve
- Describe the proposed solution
- Explain why the feature is valuable
- Consider alternative solutions or approaches
- Provide practical use cases whenever possible

Well-defined feature requests help maintainers evaluate and prioritize enhancements effectively.

---

## Security Vulnerabilities

Do **NOT** report security vulnerabilities through public GitHub Issues, Discussions, or Pull Requests.

Instead, follow the reporting process described in:

- [Security Policy](./SECURITY.md)
Security-related reports submitted through public channels may be closed and redirected to the appropriate reporting process.

---

## Development Setup

### Prerequisites

- Git
- Java 17 or higher
- Node.js >= 18.x
- Yarn >= 1.22.x
- Gradle 8.x (included via Gradle wrapper)
- Docker (optional, for containerized deployment)

### Clone the Repository

```bash
# TODO: Replace with the public GitHub repository URL upon release
git clone <repository-url>
cd hmg-role
```

### Install Dependencies

```bash
# Install frontend dependencies
cd hmg-role-bo && yarn install
```

### Build the Project

```bash
# Build all backend modules
./gradlew build

# Build the back-office UI
cd hmg-role-bo && yarn build
```

### Run Tests

```bash
# Run backend tests
./gradlew test

# Run frontend tests
cd hmg-role-bo && yarn test
```

---

## Development Workflow

### 1. Fork the Repository

Create a personal fork of the repository.

### 2. Create a Branch

Create a branch from the default branch.

Examples:

```text
feature/oauth-login
fix/api-timeout
docs/update-installation-guide
```

### 3. Make Changes

Please ensure that your changes are:

- Focused and easy to review
- Consistent with existing design patterns
- Appropriately documented
- Accompanied by tests when applicable

Avoid including unrelated changes in the same pull request.

### 4. Validate Your Changes

Before creating a pull request:

- Run all relevant tests
- Verify the project builds successfully
- Ensure no new warnings or errors are introduced

### 5. Update Documentation

If your changes affect user-facing functionality, APIs, configuration, or workflows, update the relevant documentation as appropriate.

### 6. Submit a Pull Request

When opening a pull request:

- Complete the Pull Request template
- Provide a clear description of the change
- Explain the motivation and expected outcome
- Link related issues

Examples:

```text
Fixes #123
Closes #123
Resolves #123
```

Ensure all required CI checks pass before requesting review.

---

## Commit Message Guidelines

We recommend following the Conventional Commits specification.

Format:

```text
<type>(optional-scope): short description
```

Examples:

```text
feat(auth): add OAuth login support

fix(api): handle null response values

docs(readme): update installation instructions
```

Common commit types:

- feat — New functionality
- fix — Bug fix
- docs — Documentation updates
- refactor — Internal code improvements
- perf — Performance improvements
- test — Test additions or changes
- chore — Maintenance work
- ci — CI/CD related updates

Consistent commit messages improve project traceability and release management.

---

## Contributor License Agreement (CLA)

Contributions to this project are subject to a Contributor License Agreement (CLA).

Before contributing, please review:

- [Contributor License Agreement (Korean)](https://github.com/hyundaimotorgroup/.github/blob/main/legal/CLA-KOR.md)
- [Contributor License Agreement (English)](https://github.com/hyundaimotorgroup/.github/blob/main/legal/CLA-ENG.md)


By submitting a contribution, you confirm that:

- You have the legal right to submit the contribution.
- The contribution is your original work, or you have sufficient rights to contribute it.
- You are authorized to submit the contribution on behalf of yourself or your organization, if applicable.
- You agree to the terms of the applicable Contributor License Agreement (CLA).

If CLA verification is enabled for this repository, contributors may be required to complete the CLA process through the repository's CLA verification service before a pull request can be merged.

Failure to satisfy CLA requirements may result in delays or rejection of the contribution.

---

## Pull Request Review Process

All contributions are reviewed by project maintainers.

During review, maintainers may request:

- Additional testing
- Documentation updates
- Design clarification
- Security review
- Code improvements

Please address review feedback in a timely and constructive manner.

Submission of a pull request does not guarantee acceptance.

Project maintainers reserve the right to reject contributions that do not align with project objectives, quality standards, security requirements, or licensing requirements.

---

## Branch Naming Convention

Use descriptive branch names following the pattern:

```text
<type>/<description>
```

Examples:

```text
feature/oauth-login
fix/null-pointer-exception
docs/api-reference-update
refactor/cache-service
test/improve-unit-tests
chore/update-dependencies
```

---

## Questions and Support

Before creating a new issue, please search existing issues to avoid duplicates.

For bug reports, feature requests, and general project discussions:
- Use [GitHub Issues](https://github.com/hyundaimotorgroup/hmg-role/issues)

For security concerns:
- Follow [Security Policy](./SECURITY.md)

---

Thank you for contributing.
Your participation helps improve the quality, reliability, and long-term success of the project.
