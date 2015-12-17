
OpenKSCP Library 1.0.3 Public Release 9-21-2001
Open Source Java Libraries

Copyright (c) 2001, Kinetoscope, Inc.
All rights reserved.

This release of the OpenKSCP java libraries has been updated by Comet Way, Inc.
to resolve minor bugs found during its use. This release is included with the
Comet Way JAK as an opensource component supporting Comet Way agent technology. 

Please send feedback and bug reports to support@cometway.com


CHANGES

Version 1.0.3 includes minor improvements to HTTPResponse, HTTPClient,
HTTPLoader, HTTPRequest and HTMLTextParser.


BEFORE YOU INSTALL

Kinetoscope Open Source Libraries require a working knowledge of the Java
platform upon which it will be deployed. Instructions in this document are generic in nature, but refer specifically to most common UNIX based
implementations of the JDK. In most cases, you will need to know the following
fundamental java development skills and information:

1. Where java is installed.
   -- Often located in a directory named similar to "/usr/local/java"

2. How to run Java applications using the 'java' command line tool.
   -- Adding /usr/local/java/bin to the command PATH makes this easier.
   
3. How to set the Java classpath.
   -- Here is an example classpath: .:~/classes:/usr/local/java/lib/classes.zip

4. How to compile .java files using the 'javac' command line tool.
   -- Use the 'javac -d ~/classes' to route .class files to a separate classes
      directory.

If you are unexperienced with Java development in general, we highly recommend
reading the first three chapters of "Java In A Nutshell", published by O'Reilly.
This book contains most of the necessary information critical to learning how
to write, compile, and execute applications written in the Java language.


POINTS OF INTEREST

kinetoscope.io - a regular expression matching filename filter is here
kinetoscope.message - some generic SMTP message handling classes are here
kinetoscope.net - classes for handling HTTP, SMTP, POP3, FTP, and news and
                  finger connections
kinetoscope.net.html - classes for parsing and creating HTML tag trees
kinetoscope.om - an abstract model for object persistence
kinetoscope.props - an abstract model for encoding and decoding Props
                    representations
kinetoscope.text - an abstract model for text buffer manipulation
kinetoscope.util - Props, jGrep, Schedules, and other goodies.


VERSION HISTORY

Version 1.0.2 includes minor fixes to the ESMTPSender class so that it throws a
ESMTPException whenever it receives an IOException.

kinetoscope.util.Props has been changed to allow lazy instantiation of the
objects it (infrequently) uses for IPropsListeners and Props change monitoring.
Two Vectors and a Object are instantiated whenever a new Listener is added,
otherwise these objects are not instantiated and listener related code is
skipped. This is simply a conservative measure -- functionality should remain
the same.

kinetoscope.net.HTTPLoader has been fixed to address a potential
OutOfMemoryError when reading documents over 800K in size. Now checks to make
sure the file is HTML/XML before trying to parse it. Downloading REALLY large
files still gives out of memory errors unless you set the max heap size to be
larger. Also HTTPLoader was changed to use BufferedReader.read(char[]) instead
of BufferedReader.readLine() to avoid nasty endofline conversion problems where
char(13) was getting changed to char(10) under UNIX..

Version 1.0.1 includes minor fixes to the HTTPClient class.


CONTACT

Comet Way will be providing this and future updates to Kinetoscope software.

Comet Way, Inc.
4551 Forbes Avenue
Pittsburgh, PA 15213

Phone: 412-682-5282 
Fax:   412-682-4112
JFax:  412-291-1061

http://www.cometway.com

support@cometway.com
