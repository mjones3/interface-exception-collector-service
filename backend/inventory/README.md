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
  run the command: java -jar rsc-0.9.1.jar --debug --request --data "{\"unitNumber\":\"W214152205701\", \"productCode\":\"E0869VA0\", \"locationCode\":\"LOCATION_1\"}" --route validateInventory tcp://api.local.gd:7002
