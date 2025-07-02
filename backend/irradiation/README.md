# Irradiation Service


## Project Setup

This project requires the following tools:

- [Rancher Desktop](https://rancherdesktop.io/)
- [Tilt](https://tilt.dev/)
- [Kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Helm](https://helm.sh/)

## Installation

Follow the links above to download and install each tool.

### Rancher Desktop

Rancher Desktop is an open-source project that provides Kubernetes and container management. You can download it from
the [official website](https://rancherdesktop.io/). Make sure Rancher Desktop is running after installation.

### Tilt

Tilt automates all the steps from a code change to a new process: watching files, building container images, and
bringing your environment up-to-date. You can download it from the [official website](https://tilt.dev/). To start the
development cluster in the root directory, run `tilt up`.

### Kubectl

Kubectl is a command line tool for controlling Kubernetes clusters. You can download it from
the [official website](https://kubernetes.io/docs/tasks/tools/).

### Helm

Helm is a package manager for Kubernetes that allows developers and operators to more easily package, configure, and
deploy applications and services onto Kubernetes clusters. You can download it from
the [official website](https://helm.sh/).

### Rsocket
Download the rsocket client (rsc) from https://github.com/making/rsc/releases


## Running Tests

Here's how you can run tests:

- To trigger both unit tests and verification tests, use the following command:

    ```bash
    mvn verify
    ```
- To trigger the verification tests based on cucumber tags, use the following command:

    ```bash
    mvn verify  -Dcucumber.filter.tags="@tag1 or @tag2 and not @tag3"
    ```
    
    By default, the tests will exclude scenarios tagged with `@disabled` and `@skipOnPipeline`. This is configured in the `CucumberSuite.java` file.
    
    The command line tags specified with `-Dcucumber.filter.tags` will take precedence over the default tags in `CucumberSuite.java`.

- To only trigger unit tests, use the following command:

    ```bash 
    mvn test
    ```

### Running Tests on Selenium Grid

If you want to run your tests on a Selenium Grid, you can specify the Selenium Grid URL by passing it as a system property when starting your application.

You can do this by using the `-D` option to define system properties when running your Maven commands. Here's how you can do it:

```bash
mvn test -Dselenium.grid.url=https://selenium-grid.local
```

# Unit Number Format For Testing

In the test suite, each unit number is structured as follows:

Structure:
<Donation FIN (5 characters)> + <Year (2 digits)> + <Feature File Identifier (3 digits)> + <Scenario Identifier (3 digits)>

Example: W777725001001

## How to Assign Unit Numbers

### 1. Start with the Donation FIN and Year:
Example: W777725.

Note: In the near future, we will change the donation FIN to use a new one specifically created for test automation purposes.

### 2. Add the Feature File ID:

Each feature file has a unique 3-digit ID.

Example for a random feature file: W777725001.

### 3. Add the Scenario ID:

Within the feature file, each scenario gets a unique 3-digit ID.

Example:

- First scenario: W777725001001

- Second scenario: W777725001002

- Third scenario: W777725001003

Document the Base Unit Number in the Feature File:
Add a comment at the top of each feature file to indicate its base unit number format.
Example:

``` Feature Unit Number Reference: W777725001000 ```

## List of current Unit Number Reference per Feature file
| **UN reference** | **Feature file** |
|------------------|------------------|
| W777725001000    | example.feature  |
