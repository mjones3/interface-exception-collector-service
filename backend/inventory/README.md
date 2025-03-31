# Reactive Microservice POC 

This project is a proof of concept for a reactive microservice using Spring Boot, PostgreSQL, and Kafka. It includes
setup instructions for Rancher Desktop, Tilt, Kubectl, and Helm.

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
- getAvailableInventoryWithShortDatedProducts
run the command: java -jar rsc-0.9.1.jar --debug --request --data "{\"locationCode\":\"LOCATION_1\",\"availableInventoryCriteriaDTOS\": [{\"productFamily\":\"PLASMA_TRANSFUSABLE\", \"bloodType\":\"O\"}]}" --route getAvailableInventoryWithShortDatedProducts tcp://api.local.gd:7002
- validateInventory
  run the command: java -jar rsc-0.9.1.jar --debug --request --data "{\"unitNumber\":\"W012345678903\", \"productCode\":\"E0869V02\", \"locationCode\":\"LOCATION_2\"}" --route validateInventory tcp://api.local.gd:7002

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

Example: W036825001001

## How to Assign Unit Numbers

### 1. Start with the Donation FIN and Year:
Example: W036825.

Note: In the near future, we will change the donation FIN to use a new one specifically created for test automation purposes.

### 2. Add the Feature File ID:

Each feature file has a unique 3-digit ID.

Example for a random feature file: W036825001.

### 3. Add the Scenario ID:

Within the feature file, each scenario gets a unique 3-digit ID.

Example:

- First scenario: W036825001001

- Second scenario: W036825001002

- Third scenario: W036825001003

Document the Base Unit Number in the Feature File:
Add a comment at the top of each feature file to indicate its base unit number format.
Example:

``` Feature Unit Number Reference: W036825001000 ```

## List of current Unit Number Reference per Feature file
| **UN reference** | **Feature file**          |
|------------------|---------------------------|
| W036825001000    | CheckInCompleted.feature  |
| W036825002000    | DiscardReceived.feature   |
| W036825003000    | GetAllAvailable.feature   |
| W036825004000    | InventoryOutbound.feature |
| W036825005000    | KafkaListeners.feature    |  
| W036825006000    | LabelApplied.feature      |
| W036825007000    | ProductCompleted.feature  |
| W036825008000    | ProductCreated.feature    |
| W036825009000    | ProductRecovered.feature  |
| W036825010000    | ProductUnsuitable.feature |
| W036825011000    | RemoveQuarantine.feature  |
| W036825012000    | ShipmentCompleted.feature |
| W036825013000    | UnitUnsuitable.feature    |
| W036825014000    | ValidateProduct.feature   |
| W036825015000    | ***<== available***       |
                                                   
