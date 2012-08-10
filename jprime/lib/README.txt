For Maven 2 (and hopefully later), this blog post on how to include local JARs properly:

http://blog.dub.podval.org/2010/01/maven-in-project-repository.html

Note that the lib/java repository has already been added.

Example:

If we want to add 'mydep-1.2.3.jar', with groupid 'abra.ca.dabra' you would typically create
the file hierarchy:

   lib/java/abra/ca/dabra/1.2.3
   
then add the JAR there, then add the dependency

 	<dependency>
		<groupId>abra.ca.dabra</groupId>
		<artifactId>mydep</artifactId>
		<version>1.2.3</version>
		<type>jar</type>
		<scope>compile</scope>
	</dependency>

to the pom.xml.