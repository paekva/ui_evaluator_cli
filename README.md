# UI Tests Evaluator CLI

This document describes the purpose and usage of the existing application. 

The application was developed as a part of the __Master Thesis work for the Department of Computer Science at Chemnitz Technical University__. 

The developed application is a proof-of-concept for the solution of evaluating UI complexity. It utilises analysis of UI tests to assess the complexity of the interfaces.

## Technologies
In this project the following technologies were used:
- Kotlin
- JDK 17
- Maven
- Spring framework including Spring Boot CLI

## Build process
The application can be packaged into executable `.jar` for the further use with the following command:

`mvn package`

This command will produce two jar files, one with bundled dependencies and one without them. 
The file with bundled dependencies is an executable file and can be executed as usual via

`java -jar ui_evaluator_cli.jar`

provided that ui_evaluator_cli.jar is a name of the file with bundled dependencies. Actual name might be different depending on the version number specified in pom.xml.

## Usage
To use the tool to analyse existing project, one should:
1. Prepare configuration file and put it in the root of the application's repository
2. Run the __evaluate__ command from the terminal using jar with the application's repository path
3. Observe the results

The tool provides following command:
- _evaluate_ - get complexity metrics for the provided application

  `java -jar ui_evaluator_cli.jar evaluate --path ./`
  

- _info_ - get tool description
  
  `java -jar ui_evaluator_cli.jar info`

If the jar file is executed without command line arguments, it is possible to execute commands via Spring CLI shell.

### Configuration file
In order for evaluation to work, the project root should have the __ui_evaluator_config.yaml__ available. 
The config contains path for UI tests, UI tests logs, used technologies, etc. See [example of the config](./ui_evaluator_config.example.yaml).

### Naming conventions
It is expected from the application input files, that each log file contains only one test and that the log files structure is the same as test files structure.
The names of the log files should be the names of the tests. 

### Project path
By default, the path of the configuration file is considered the root of the application, however this can be overwritten with the config contents.

Thus, the default path to the configuration file is `./`, however, it can be overwritten with the `path` argument of the `evaluate` command.

### Output
By default, the app provides the output for the calculated metrics in console and also saves it as a CSV file. The [example of CSV output file](./results_groups.example.csv) is presented in the repository. 
The content printed in the console and saved in the CSV file is identical, the only difference is the representation.

### Debug
The application can be used with `--debug` flag to allow for additional logging to console, reflecting the process of evaluation.