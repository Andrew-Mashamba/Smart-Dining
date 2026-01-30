# Contributing to Sea Cliff Smart Dining System

Thank you for considering contributing to the Sea Cliff project! This document outlines the process and guidelines for contributing.

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on constructive feedback
- Respect differing viewpoints and experiences

## Getting Started

1. **Fork the repository**
2. **Clone your fork**: `git clone <your-fork-url>`
3. **Create a branch**: `git checkout -b feature/your-feature-name`
4. **Make your changes**
5. **Test your changes**
6. **Commit with clear messages**
7. **Push to your fork**
8. **Open a Pull Request**

## Development Setup

See [README.md](README.md#quick-start) for detailed setup instructions.

## Branch Naming Convention

- `feature/` - New features
- `bugfix/` - Bug fixes
- `hotfix/` - Urgent production fixes
- `refactor/` - Code refactoring
- `docs/` - Documentation updates
- `test/` - Test additions or updates

Examples:
- `feature/whatsapp-payment-integration`
- `bugfix/order-status-not-updating`
- `docs/api-endpoint-documentation`

## Commit Message Guidelines

Follow the conventional commits specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples:

```
feat(orders): add real-time order status updates

Implemented WebSocket connection for live order updates
to kitchen and bar displays. Updates trigger immediately
when order status changes.

Closes #123
```

```
fix(android): resolve offline sync conflict

Fixed race condition when syncing multiple orders
simultaneously. Now uses transaction-based sync.

Fixes #456
```

## Code Style

### PHP (Laravel)

We use Laravel Pint for code formatting:

```bash
cd laravel-app
./vendor/bin/pint
```

**Key conventions:**
- PSR-12 coding standard
- Use type hints for parameters and return types
- Document complex logic with comments
- Keep methods focused and small
- Use dependency injection

### Kotlin (Android)

We use ktlint for code formatting:

```bash
cd android-pos
./gradlew ktlintFormat
```

**Key conventions:**
- Follow Kotlin coding conventions
- Use meaningful variable names
- Prefer immutability (`val` over `var`)
- Use coroutines for async operations
- Follow MVVM architecture pattern

### SQL

- Use lowercase for SQL keywords: `select`, `from`, `where`
- Use meaningful table and column names
- Add indexes for frequently queried columns
- Include comments for complex queries

## Testing Requirements

### Laravel Tests

All new features must include tests:

```bash
cd laravel-app

# Run all tests
php artisan test

# Run specific test
php artisan test --filter=OrderTest
```

**Test types:**
- **Unit tests**: Test individual methods and classes
- **Feature tests**: Test complete features and workflows
- **Integration tests**: Test external service integrations

### Android Tests

```bash
cd android-pos

# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

## Pull Request Process

1. **Ensure tests pass**: Run all tests before submitting
2. **Update documentation**: Update relevant docs
3. **Add to CHANGELOG**: Note your changes
4. **Request review**: Tag appropriate reviewers
5. **Address feedback**: Respond to review comments
6. **Squash commits**: Keep PR history clean

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
How has this been tested?

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Tests pass
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
- [ ] Added tests for new features
```

## Database Migrations

When creating migrations:

1. **Always be reversible**: Implement `down()` method
2. **Test rollback**: Ensure migrations can be rolled back
3. **Use descriptive names**: `create_orders_table`, not `create_table1`
4. **Add indexes**: For foreign keys and frequently queried columns
5. **Consider data migration**: Use seeders for data changes

```bash
# Create migration
php artisan make:migration create_orders_table

# Run migrations
php artisan migrate

# Rollback
php artisan migrate:rollback
```

## API Changes

When modifying APIs:

1. **Maintain backward compatibility**: Don't break existing clients
2. **Version breaking changes**: Use API versioning (`/api/v2/`)
3. **Update documentation**: Keep Postman collection updated
4. **Test with Android app**: Ensure mobile compatibility

## Security

- **Never commit secrets**: Use environment variables
- **Validate all input**: Use Form Requests in Laravel
- **Sanitize output**: Prevent XSS attacks
- **Use parameterized queries**: Prevent SQL injection
- **Implement rate limiting**: Protect against abuse
- **Report vulnerabilities**: Email security@seacliff.com

## Documentation

- **Code comments**: Explain "why", not "what"
- **API docs**: Update for endpoint changes
- **README**: Update for setup changes
- **Architecture docs**: Update for structural changes

## Review Process

1. **Automated checks**: CI/CD pipeline runs tests
2. **Code review**: At least one approval required
3. **Testing**: Manual testing in staging environment
4. **Approval**: Project maintainer final approval
5. **Merge**: Squash and merge to main branch

## Release Process

1. **Version bump**: Update version numbers
2. **Changelog**: Document all changes
3. **Testing**: Full regression testing
4. **Tag release**: Create Git tag
5. **Deploy**: Deploy to staging, then production
6. **Monitor**: Watch logs and metrics

## Getting Help

- **Documentation**: Check [docs/](docs/) first
- **Issues**: Search existing issues
- **Discussions**: Use GitHub Discussions for questions
- **Team contact**: Reach out to maintainers

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project README

Thank you for contributing to Sea Cliff! 🎉
