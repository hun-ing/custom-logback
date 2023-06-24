package com.huning.logback;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

@Slf4j
@NoAutoStart
public class CustomTimeBasedFileNamingAndTriggeringPolicy<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E> {

	private final int LOG_CREATION_CYCLE_MINUTE = 5;
	private final int ONE_MINUTE_MILLISECONDS = 60000;
	private final int LOG_CREATION_CYCLE_MILLISECONDS = LOG_CREATION_CYCLE_MINUTE * ONE_MINUTE_MILLISECONDS;

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
		long milliseconds =this.dateInCurrentPeriod.getTime();
		long remainder = milliseconds % LOG_CREATION_CYCLE_MILLISECONDS;
		long difference = LOG_CREATION_CYCLE_MILLISECONDS - remainder;
		this.nextCheck = milliseconds + difference;
	}
}

