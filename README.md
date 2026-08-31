# hmg-Role
<p>
  <img src="https://img.shields.io/badge/status-active-brightgreen.svg" alt="active" />
   <img
   src="https://img.shields.io/badge/version-v1.0.0-green.svg"
   alt="Version: v1.0.0"/>
  <a href="https://opensource.org/licenses/Apache-2.0">
    <img
      src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"
      alt="License: Apache 2./badge/version-1.0.0-blue.svg"/>
  </a>
</p>

## Overview

hmg-Role is an authorization management system that provides centralized access control for enterprise applications.

It is designed to provide Role-Based (RBAC), Attribute-Based (ABAC), and Policy-Based (PBAC) access control capabilities for developers and system administrators managing permissions across services.


## Key Features

- Role-Based Access Control (RBAC) for managing user roles and permissions
- Attribute-Based Access Control (ABAC) for context-aware, fine-grained authorization
- Policy-Based Access Control (PBAC) for rule-driven access management
- Back-office management UI for centralized role administration
- SDK for seamless integration with client services

## Getting Started

### Prerequisites

- Java 21 or higher
- Node.js >= 18.x
- Yarn >= 1.22.x
- Gradle 8.x (included via Gradle wrapper)

### Installation

```bash
git clone https://github.com/hyundaimotorgroup/hmg-role.git

cd hmg-role
```

### Build

```bash
# Build all backend modules
./gradlew build
```

### Test

```bash
# Run backend tests
./gradlew test
```

## Usage

```bash
# Start the API server
./gradlew :hmg-role-api:bootRun
```

## Contributing

Contributions are welcome.

Before contributing, please review:

- [Contributing Guide](.github/CONTRIBUTING.md)
- [Contributor License Agreement (Korean)](https://github.com/hyundaimotorgroup/.github/blob/main/legal/CLA-KOR.md)
- [Contributor License Agreement (English)](https://github.com/hyundaimotorgroup/.github/blob/main/legal/CLA-ENG.md)

If CLA verification is enabled, contributors may be required to complete the Contributor License Agreement (CLA) process before their pull request can be merged.

## Project Policies

- [Code of Conduct](.github/CODE_OF_CONDUCT.md)
- [Security Policy](.github/SECURITY.md)

## Support

Before creating a new issue, please search existing issues to avoid duplicates.

For bug reports, feature requests, and general project discussions:
- [GitHub Issues](https://github.com/hyundaimotorgroup/hmg-role/issues)
- [Issue Templates](.github/ISSUE_TEMPLATE/)

For security concerns:

- Follow the instructions in [Security Policy](.github/SECURITY.md).

## Releases

Release notes and version history are available in the following locations:
- [Changelog](./CHANGELOG.md)
- [Release Notes](https://github.com/hyundaimotorgroup/hmg-role/releases)

This project follows [Semantic Versioning](https://semver.org/).


## License

This project is licensed under the <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.

See the [LICENSE](LICENSE) file for details.

Copyright (c) 2026 Hyundai Motor Group.

## Third-Party Notices

This project may include third-party open source software.
For additional copyright notices and license information, see [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES).
