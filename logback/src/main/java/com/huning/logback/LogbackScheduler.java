package com.huning.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

