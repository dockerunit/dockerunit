<img src="https://user-images.githubusercontent.com/630567/106909724-796c5f00-66f8-11eb-8f26-7a69f9ab0532.png" width=12% height=12% align="left" style="margin-right: 20px"/>


[![central]](https://search.maven.org/search?q=g:com.github.dockerunit%20AND%20a:parent)
&nbsp;
[![nexus]](https://oss.sonatype.org/index.html#nexus-search;gav~com.github.dockerunit~parent~~~)
&nbsp;
[![licence]](https://choosealicense.com/licenses/apache-2.0/)

[![circle-ci]](https://circleci.com/gh/dockerunit/dockerunit/tree/master)
&nbsp;
[![codacy-coverage]](https://app.codacy.com/project/dockerunit/dockerunit/dashboard)
&nbsp;
[![coverity]](https://scan.coverity.com/projects/dockerunit-dockerunit)

[![codacy]](https://app.codacy.com/project/dockerunit/dockerunit/dashboard)
&nbsp;
[![better-code]](https://bettercodehub.com/)
&nbsp;
[![lgtm-grade]](https://lgtm.com/projects/g/dockerunit/dockerunit/context:java)
&nbsp;
[![lgtm-alerts]](https://lgtm.com/projects/g/dockerunit/dockerunit/alerts)

<br/>
Dockerunit is an extensible framework for testing of dockerised services/applications in JUnit (4 or 5) based test
suites.

<br/>
<br/>

## Quick Start

---

<!-- TODO: Explain how to quickly get up and running using this in a maven or
gradle project -->

## Documentation

---

[![javadoc-core]](https://www.javadoc.io/doc/com.github.dockerunit/core)
&nbsp;
[![javadoc-consul]](https://www.javadoc.io/doc/com.github.dockerunit/consul)
&nbsp;
[![javadoc-junit4]](https://www.javadoc.io/doc/com.github.dockerunit/junit4)
&nbsp;
[![javadoc-junit5]](https://www.javadoc.io/doc/com.github.dockerunit/junit5)

## Support / Discussion

---

[![discord]](https://discordapp.com/channels/587583543081959435/587583543081959437)

## Development

---

### Prerequisites

- Java 8
- Maven 3.6+
- Docker (for running integration tests)

### Build

```
mvn clean verify
```

### Tests

```
mvn test
```

## Contributing

---

<!-- TODO: Link to CONTRIBUTING.md and related info -->

## Versioning

---

This project adheres to [semantic versioning](https://semver.org/spec/v2.0.0.html).

Modules are always released together, even if a particular module had no changes
since the last release it will still have it's version bumped whenever any one
module needs to be released.



## License

---

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE)
file for details


<!-- Links -->

[central]: https://img.shields.io/maven-central/v/com.github.dockerunit/dockerunit-core.svg?style=flat
[nexus]: https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.dockerunit/dockerunit-parent.svg?style=flat
[licence]: https://img.shields.io/github/license/dockerunit/dockerunit-core.svg?style=flat

[circle-ci]: https://img.shields.io/circleci/build/gh/dockerunit/dockerunit-core/master.svg?style=flat
[codacy-coverage]: https://img.shields.io/codacy/coverage/c152a56101134d439b1e8a005725df1e.svg?style=flat
[coverity]: https://img.shields.io/coverity/scan/18573.svg?style=flat

[codacy]: https://img.shields.io/codacy/grade/c152a56101134d439b1e8a005725df1e.svg?style=flat&label=codacy
[better-code]: https://bettercodehub.com/edge/badge/dockerunit/dockerunit?branch=master
[lgtm-grade]: https://img.shields.io/lgtm/grade/java/github/dockerunit/dockerunit.svg?style=flat&label=lgtm
[lgtm-alerts]: https://img.shields.io/lgtm/alerts/github/dockerunit/dockerunit.svg?style=flat&label=lgtm

[javadoc-core]: https://javadoc.io/badge/com.github.dockerunit/core.svg
[javadoc-consul]: https://javadoc.io/badge/com.github.dockerunit/consul.svg
[javadoc-junit4]: https://javadoc.io/badge/com.github.dockerunit/junit4.svg
[javadoc-junit5]: https://javadoc.io/badge/com.github.dockerunit/junit5.svg

[discord]: https://img.shields.io/discord/587583543081959435.svg?style=flat
