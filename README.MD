# Base test project

"base-project" is a multi-module system consisting of the following components:

- **api**: A module containing API tests, including business logic and request handling.

- **postman-collections**: A module containing Postman request collections used for API testing.

- **ui**: A module with UI tests for interacting with the system.

## Allure
### Generate report (for CI/CD)
```shell
./gradlew :{module-name}:allureReport
```
### See report (local)
```shell
./gradlew :{module-name}:allureServe
```

## Test execution
### Run all tests
```shell
./gradlew clean :{module-name}:test
```

### Run tests in specific class
```shell
./gradlew clean :{module-name}:test --tests {TestClass}
```

### Run specific test methods in specific class
```shell
./gradlew clean :{module-name}:test --tests {TestClass}.{TestMethod1} --tests {TestClass}.{TestMethod2}
```

### Run tests with specific tags (e.g. Smoke)
```shell
./gradlew clean :{module-name}:test -PincludeTags="{TagName}"
```

### Run tests excluded specific tags (e.g. Smoke)
```shell
./gradlew clean :{module-name}:test -PexcludeTags="{TagName}"
```

## Build and Dependency Management
### Clean and build project without tests
```shell
./gradlew clean build -x test --refresh-dependencies
```
