
Selenified tests can be executed without the use of the TestNG.xml file, but rather through Maven which will enable you to set the group, package or test class in the POM file.
These values can also be set or overridden at execution time by passing in command line inputs. Test methods can also be specified at the command line. 

Setup POM file
To run tests without the XML file, be sure to set up your POM file as follows:

First add the properties attributes which will hold the variable names for your test groups, test classes or packages. 

<properties>
    …
    <failsafe.groups></failsafe.groups>
    <failsafe.class></failsafe.class>
    <failsafe.packages></failsafe.packages>
 ...
</properties>

We will be using the Maven Failsafe plugin to execute our tests, so add the following configurations to your build attributes:

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>2.8.1</version>
            <configuration>
                <groups>${failsafe.groups}</groups>
                <test>${failsafe.class}</test>
                <test>${failsafe.packages}</test>
            </configuration>
            <executions>
                <execution>
                    <id>integration-test</id>
                    <goals>
                        <goal>integration-test</goal>
                    </goals>
                </execution>
                <execution>
                    <id>verify</id>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

Running your tests

To run your tests, include your group, class or package name either via the command line or in the POM file, using the variables set above. 

For example, to run a suite to tests with the group name “regression” from the command line, enter:

mvn verify -Dfailsafe.groups = regression 

To run all tests from a package called “uitests” run the command:

mvn verify -Dfailsafe.package = uitests.*

To run tests from a class called SampleTests.java, run the command:

mvn verify -Dfailsafe.class = SampleTests

Alternatively, these variable names can be persisted by adding them to the POM file properties attribute. 

For example to set the value of groups to “regression" in the POM file, add that group name to the “failsafe.groups” property

<failsafe.groups>regression</failsafe.groups>

This way the tests can be kicked off using the command: 

mvn verify

The same goes for packages and classes, though keep in mind that the command line inputs will override any previously set POM values. 

To run a test method called sampleTest via Maven without the XML file, use the command

test -Dtest=SampleIT#sampleTest

