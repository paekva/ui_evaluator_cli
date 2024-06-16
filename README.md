# UI Tests Evaluator CLI

This document describes the purpose and the direction for use of the existing application. 

The application was developed as a part of the __Master Thesis work for the Department of Computer Science at Chemnitz Technical University__. 

The developed application is a proof-of-concept for the solution for calculation of UI complexity. It utilises analysis of UI tests to assess the complexity of the interfaces.

## Technologies
In this project the following technologies were used:
- Spring Boot CLI 
- Kotlin
- Maven

## Build process
The application can be packaged into .jar for the further use with the help of Maven:

`mvn package`

The resulting jar can be executed as usual:

`java -jar ui_evaluator_cli.jar`

## Use
To use the tool to analyse existing project, one should:
1. Prepare configuration file and put it in the root of the application's repository
2. Run the __evaluate__ command from the terminal using jar with the application's repository path
3. Observe the results

The tool provides following command:
- _evaluate_ - get compleixty metrics for the provided application

  `java -jar ui_evaluator_cli.jar evaluate ./`
  

- _help_ - get tool description
  
  `java -jar ui_evaluator_cli.jar help ./`

### Configuration file
In order for evaluation to work, the project root should have the __ui_evaluator_config.yaml__ available. 
The config contains path for UI tests, UI tests logs, used technologies, etc. See [example of the config](./ui_evaluator_config.example.yaml).

### Naming conventions
It is expected from the application input files, that each log file contains only one test and that the log files structure is the same as test files structure.
The names of the log files should be the names of the tests. 

### Project path
By default, the path of the configuration file is considered the root of the application, however this can be overwritten with the config contents.

The default path to the configuration file is './', however, it can be overwritten with argument after `evaluate` command.