##Custom Logback [![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fgjbae1212%2Fhit-counter)](https://hits.seeyoufarm.com)
***
Logback을 이용하여 원하는 시간별 로그 파일을 만드는 예제입니다.

## 개발 목적
***


##설정 가이드
***

###1. CustomTimeBasedFileNamingAndTriggeringPolicy 클래스
```java
@NoAutoStart
public class CustomTimeBasedFileNamingAndTriggeringPolicy<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E> {

	private final int LOG_CREATION_CYCLE = 5;

	@Override
	public boolean isTriggeringEvent(File activeFile, E event) {
		long time = this.getCurrentTime();
		if (time >= this.nextCheck) {
			Date dateOfElapsedPeriod = this.dateInCurrentPeriod;
			this.addInfo("Elapsed period: " + dateOfElapsedPeriod);

			this.setDateInCurrentPeriod(this.nextCheck);

			this.elapsedPeriodsFileName = this.getCurrentPeriodsFileNameWithoutCompressionSuffix();

			this.setDateInCurrentPeriod(time);
			this.computeNextCheck();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void computeNextCheck() {
		this.nextCheck = this.rc.getEndOfNextNthPeriod(this.dateInCurrentPeriod, LOG_CREATION_CYCLE).getTime();
	}
}
```
1. 클래스를 생성하고 `DefaultTimeBasedFileNamingAndTriggeringPolicy` 클래스를 상속받습니다.
2. `DefaultTimeBasedFileNamingAndTriggeringPolicy` 클래스의 `isTriggeringEvent` 메서드와 `TimeBasedFileNamingAndTriggeringPolicyBase` 클래스의 `computeNextCheck` 메서드를 오버라이딩하고, 위와 같이 코드를 수정합니다.
3. `LOG_CREATION_CYCLE` 값을 통해 원하는 로그 파일 롤링 시간 사이클을 지정합니다 (분 단위)

###2. logback-spring.xml 설정
```xml
<appender name="CUSTOM_APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_PATH}/${LOG_FILE_NAME}.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOG_PATH}/${LOG_FILE_NAME}.%d{yyyy-MM-dd-HH-mm}.log</fileNamePattern>
        <timeBasedFileNamingAndTriggeringPolicy class="com.huning.logback.CustomTimeBasedFileNamingAndTriggeringPolicy"/>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>${LOG_PATTERN}</pattern>
    </encoder>
</appender>
```
1. appender 태그를 선언하고 원하는 이름을 작성합니다.
2. appender 내부에 rollingPolicy 태그를 선언하고 `TimeBasedRollingPolicy`를 사용합니다.
3. rollingPolicy 태그 내부에 timeBasedFileNamingAndTriggeringPolicy 태그를 선언하고, `DefaultTimeBasedFileNamingAndTriggeringPolicy` 클래스를 상속받아 커스텀한 클래스를 사용합니다.

```xml
<logger name="com.huning.logback.LogbackScheduler" level="DEBUG" additivity="false">
    <appender-ref ref="CUSTOM_APPENDER"/>
    <appender-ref ref="CONSOLE"/>
</logger>
```
1. 설정한 appender를 적용시킬 범위를 지정합니다.
2. 해당 예제에서는 `LogbackScheduler` 클래스에서 발생하는 로그를 기록하고 파일로 저장하도록 하였습니다.

###3. LogbackScheduler 클래스
```java
@Slf4j
@Component
@EnableScheduling
public class LogbackScheduler {

	private static int index = 1;

	@Scheduled(cron = "1 * * * * *")
	public void checkRollingLogFile() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Appender<ILoggingEvent> appender = loggerContext.getLogger("com.huning.logback.LogbackScheduler").getAppender("CUSTOM_APPENDER");

		try {
			appender.doAppend(new LoggingEvent());
		} catch (Exception e) {
			new RuntimeException("로그 이벤트 발생 실패");
		}
	}

	@Scheduled(fixedRate = 10000) // 10초마다 실행
	public void writeLog() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String nowDateTime = now.format(formatter);
		log.info("{}번째 로그입니다. 현재 시간은 {}입니다.", index, nowDateTime);
		index++;
	}
}
```
- 여기서 두 개의 Scheduler를 사용합니다.
1. checkRollingLogFile
   - 사용자의 동작이 없어 로그가 기록되지 않아도 빈 로그 이벤트를 1분 단위로 발생시킵니다. 이는 로그가 없더라도 빈 로그 파일을 남기기 위함입니다.
2. writeLog
   - 실제 로그가 기록되는지 테스트하기 위해 10초 간격으로 로그를 기록합니다.