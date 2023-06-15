package com.huning.logback;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

@Slf4j
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

			log.info("다음 로그 파일 이름 = {}", this.elapsedPeriodsFileName);

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

