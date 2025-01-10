Template for creating a new Java project with Maven

Contains run configs, auto spotless formatting on build, and shade to include dependencies in the jar.

IF YOU JUST USED THIS TEMPLATE TO INITIATE A NEW PROJECT:

- [ ] Make sure to update the `artifactId` in the `pom.xml` and `module name` in `runConfigurations`(or search and
  replace all occurences of `java-maven-template` with `your-project-name`) file to match your project's name
- [ ] Right click on the project folder -> Module settings -> change module name to your project's name
- [ ] Update the runConfigs according to your project's needs (you might need to just click apply in `Run the program`
  config for it to be recognized)
- [ ] Update the README.md with your project's information