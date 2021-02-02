## Code

### Features

- Add tmpfs capability
- Add OS and docker bridge detection at start up (Core)
- Add '@EnvFile' annotation to import a property file and avoid env variables being hardcoded 

### Maintenance

- Add UTs to all modules (JUnit 5 + Mockito + AssertJ)

- Sort out logging

    - Use slf4j everywhere (what about log4j2?)

    - Exclude all other logging libraries (use slf4j adapters/bridges if needed)

    - Use logback as the implementation for UTs (test scope only)

- Acceptance Test Suite (tests using dockerunit features)

    - In a multi-module project suite would be a module

    - We would need two test modules, one for Junit4 and another for Junit5

    - These should run on any change in consul, core or the corresponding junit module

- Add checkstyle/pmd/spotbugs rule configs

    - Run explicitly or always run on modified files in PRs?
    
    - Circle CI seems to support all of them

## Project Management / CI

- Automate releases

    - `release/M.n` branches

    - `M.n.p` tags

    - `master` always `M.m.0-SNAPSHOT`

    - Release from `master`:
        1. new branch
        2. set release version `M.m.0`
        3. deploy
        4. tag (`vM.m.0`)
        5. set next version `M.m.1-SNAPSHOT`
        6. set `master` version `M.(m+1).0-SNAPSHOT`

    - Release from `M.n`:
        1. set release version `M.m.p`
        2. deploy
        3. tag (`vM.m.p`)
        4. set next version `M.m.(p+1)-SNAPSHOT`

- Check out license plugins
    - <https://mycila.mathieu.photography/license-maven-plugin/>
    - <https://www.mojohaus.org/license-maven-plugin/>

### Integrations

- Move build/test/release to Github Actions?

- Integrate UT coverage with CI
    - Codecov ?
    - CircleCI ?
    - Coveralls ?

- Setup coverity scans

- [Mergify](https://mergify.io/) ?

- [SonarCloud](https://sonarcloud.io/) ?

- [Snyk](https://snyk.io/) ?

- Is [Cirrus CI](https://cirrus-ci.org) better than Circle CI?
