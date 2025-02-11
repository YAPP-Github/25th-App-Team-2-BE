package com.tnt.common;

import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Profile({"local", "dev"})
@Component
public class ControllerExecutionTimeAspect {

	private final ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();

	@Around("@within(org.springframework.web.bind.annotation.RestController)")
	public Object aroundControllerExecutionTimeLogging(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getSignature().getDeclaringTypeName();
		String methodName = joinPoint.getSignature().getName();
		String fullMethodName = className + "." + methodName;

		StopWatch sw = new StopWatch();

		sw.start();
		Object result = joinPoint.proceed();
		sw.stop();

		methodStats.computeIfAbsent(fullMethodName, k -> new MethodStats())
			.recordExecutionTime(sw.getTotalTimeMillis());

		MethodStats stats = methodStats.get(fullMethodName);

		log.info("Execution time of {} is {} ms", fullMethodName, sw.getTotalTimeMillis());
		log.info("{} stats: {}", fullMethodName, stats);

		return result;
	}

	private static class MethodStats {

		private long count;
		private long totalTime;
		private long minTime = Long.MAX_VALUE;
		private long maxTime;

		public synchronized void recordExecutionTime(long time) {
			count++;
			totalTime += time;
			minTime = Math.min(minTime, time);
			maxTime = Math.max(maxTime, time);
		}

		@Override
		public String toString() {
			return String.format("count: %d, average: %dms, min: %dms, max: %dms", count, totalTime / count, minTime,
				maxTime);
		}
	}
}
