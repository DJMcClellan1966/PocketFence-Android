# Contributing to PocketFence

Thank you for your interest in contributing to PocketFence! This document provides guidelines for contributing to the project.

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help create a welcoming environment
- Report any inappropriate behavior

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- Clear title and description
- Steps to reproduce
- Expected vs actual behavior
- Android version and device model
- Relevant logs or screenshots

### Suggesting Features

For feature requests:
- Check if it's already suggested
- Explain the use case
- Describe expected behavior
- Consider implementation complexity

### Pull Requests

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Follow code style guidelines**
5. **Test your changes**
6. **Commit with clear messages**
7. **Push to your fork**
8. **Create a Pull Request**

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 8 or higher
- Android SDK API 26+
- Git

### Building the Project

```bash
# Clone the repository
git clone https://github.com/DJMcClellan1966/PocketFence-Android.git
cd PocketFence-Android

# Build the project
./gradlew build

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

## Code Style Guidelines

### Kotlin Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Keep functions small and focused
- Add comments for complex logic

### File Organization
```
app/
├── model/          # Data classes
├── repository/     # Data management
├── service/        # Background services
├── ui/             # UI components
│   ├── adapter/    # RecyclerView adapters
│   └── fragments/  # UI fragments
├── util/           # Helper classes
└── viewmodel/      # UI state management
```

### Naming Conventions

**Classes**
- PascalCase for class names
- Descriptive names (e.g., `DeviceAdapter`, `VpnFilterService`)

**Functions**
- camelCase for function names
- Verb-based names (e.g., `startVpn()`, `blockDevice()`)

**Variables**
- camelCase for variable names
- Descriptive names (e.g., `connectedDevices`, `timeRemaining`)

**Constants**
- UPPER_SNAKE_CASE for constants
- Grouped in companion objects

### Layout Files
- Use descriptive IDs
- Follow naming pattern: `fragment_name.xml`, `item_type.xml`
- Use ConstraintLayout or LinearLayout
- Avoid deep view hierarchies

### Resources
- Use string resources for all text
- Use dimension resources for spacing
- Use color resources from colors.xml
- Use drawable resources for icons

## Testing

### Unit Tests
- Write tests for business logic
- Test edge cases
- Use meaningful test names
- Aim for good coverage

### UI Tests
- Test critical user flows
- Test different screen sizes
- Test error states
- Use Espresso framework

## Documentation

### Code Documentation
- Add KDoc for public APIs
- Explain complex algorithms
- Document assumptions
- Include usage examples

### README Updates
- Keep README in sync with features
- Update setup instructions
- Document new dependencies
- Add troubleshooting tips

## Commit Guidelines

### Commit Message Format
```
<type>: <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

### Examples
```
feat: Add device name editing functionality

- Added rename button to device card
- Implemented rename dialog
- Updated device adapter to show new names

Closes #42
```

```
fix: Resolve VPN connection timeout

- Increased connection timeout to 30 seconds
- Added retry logic for failed connections
- Improved error handling

Fixes #38
```

## Security

### Reporting Security Issues
- **Do not** create public issues for security vulnerabilities
- Email security concerns to the maintainers
- Provide detailed description
- Include steps to reproduce

### Security Best Practices
- Never commit API keys or secrets
- Validate all user input
- Use secure data storage
- Follow Android security guidelines

## Review Process

### What We Look For
- Code quality and style
- Test coverage
- Documentation
- Performance impact
- Security considerations
- Compatibility with existing features

### Review Timeline
- Initial response within 48 hours
- Feedback within 1 week
- Merge decision within 2 weeks

## Getting Help

### Resources
- Check existing documentation
- Search closed issues
- Review code examples
- Ask in issue discussions

### Contact
- Create an issue for questions
- Use discussion board for general topics
- Tag maintainers when needed

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Recognition

Contributors will be:
- Listed in release notes
- Credited in commit messages
- Acknowledged in documentation

Thank you for making PocketFence better! 🎉
