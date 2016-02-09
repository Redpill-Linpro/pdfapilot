Promus PdfaPilot Server
==========================
This server application acts as a bridge between pdfapilot clients and the cli and server. It sets up REST endpoints which can be called to trigger PDF / PDF/A transformations of documents.

##Development environment
Several components are needed to successfully build and run the application locally.

###Callas PDFA cli
TBD

###MongoDB

#### MacOSX
$ brew install mongodb

### Ruby & Compass

#### MacOSX
```
sudo gem update --system
sudo gem install -n /usr/local/bin compass
```

### Node & Grunt
TBD

##Running
Helper scripts are checked in for running an embedded tomcat with the application. Simply call ```start.sh```or ```start-prod.sh``` respectively to issue development or production builds and run them.
##Building
Helper scripts are checked in for building the application. Simply call ```build.sh```or ```build-prod.sh``` respectively to issue development or production builds and run them.

##Configuration
TBD