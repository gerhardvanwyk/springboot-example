import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter
import ch.qos.logback.contrib.json.classic.JsonLayout

statusListener(OnConsoleStatusListener)

appender("FILE", RollingFileAppender, {
    file = "target/example.log"

    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        fileNamePattern = "target/example.%d{yyyy-MM-dd}.%i.log"
        maxHistory = 30
        maxFileSize = "50MB"
        totalSizeCap = "5GB"
    }

    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %d{MM-dd'T'HH:mm:ss.SSSX} [%thread] %X{MessageId}  %logger{100} - %msg%n"
    }
})

appender("JSON", RollingFileAppender, {

    file = "target/example.json"

    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        fileNamePattern = "target/example.%d{yyyy-MM-dd}.%i.json"
        maxHistory = 30
        maxFileSize = "50MB"
        totalSizeCap = "5GB"
    }

    encoder(LayoutWrappingEncoder){
        layout(JsonLayout){
            timestampFormat = "MM-dd'T'HH:mm:ss.SSSX"
            jsonFormatter(JacksonJsonFormatter){
                prettyPrint = true
            }
        }
    }
})

appender("CONSOLE", ConsoleAppender, {
    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %d{MM-dd'T'HH:mm:ss.SSSX} [%thread] %X{MessageId}  %logger{100} - %msg%n"
    }
})

Appender fileAppender = appenderList.find { it -> it.name == "FILE" };

AsyncAppender asyncAppender = new AsyncAppender();
asyncAppender.queueSize = 10000
asyncAppender.name = "ASYNC-FILE"
asyncAppender.context = context
asyncAppender.addAppender(fileAppender)

Appender jsonAppender = appenderList.find { it -> it.name == "JSON" };

AsyncAppender asyncJsonAppender = new AsyncAppender();
asyncJsonAppender.queueSize = 10000
asyncJsonAppender.name = "ASYNC-JSON"
asyncJsonAppender.context = context
asyncJsonAppender.addAppender(jsonAppender)

appenderList.add(asyncAppender)
appenderList.add(asyncJsonAppender)
asyncAppender.start()
asyncJsonAppender.start()

logger("org.wyk", Level.TRACE)

logger("org.keycloak", DEBUG)

logger("org.springframework.boot", DEBUG)

logger("org.springframework.security", DEBUG)

logger("org.springframework.web", DEBUG)

logger("org.springframework.util", INFO)

logger("org.springframework.jdbc.datasource", INFO)

root(INFO, ["ASYNC-FILE", "ASYNC-JSON", "CONSOLE"])

scan()
