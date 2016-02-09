
#export JAVA_OPTS="$JAVA_OPTS -noverify -javaagent:/usr/local/jrebel/jrebel.jar"

 export MAVEN_OPTS="$MAVEN_OPTS -noverify"
 export MAVEN_OPTS="$MAVEN_OPTS -javaagent:/opt/alfresco/springloaded.jar"
# export MAVEN_OPTS="$MAVEN_OPTS -Dspringloaded=plugins=io.github.jhipster.loaded.instrument.JHipsterLoadtimeInstrumentationPlugin,io.github.jhipster.loaded.JHipsterPluginManagerReloadPlugin"
# export MAVEN_OPTS="$MAVEN_OPTS -DhotReload.enabled=true"
# export MAVEN_OPTS="$MAVEN_OPTS -DhotReload.package.project=org.redpill.pdfapilot.promus"
# export MAVEN_OPTS="$MAVEN_OPTS -DhotReload.package.domain=org.redpill.pdfapilot.promus.domain"
# export MAVEN_OPTS="$MAVEN_OPTS -DhotReload.package.restdto=org.redpill.pdfapilot.promus.web.rest"
# export MAVEN_OPTS="$MAVEN_OPTS -DhotReload.watchdir[0]=/Users/niklas/project/pdfapilot/pdfapilot-promus/target/classes"
# export MAVEN_OPTS="$MAVEN_OPTS -Dserver.port=8083"

mvn spring-boot:run $1
