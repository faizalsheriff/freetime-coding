package com.javacodegeeks.snippets.enterprise.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class DoBeforeAspect {

	 @Before("execution(* com.javacodegeeks.snippets.enterprise.*.*(..))")
	public void doBefore(JoinPoint joinPoint) {

		System.out
				.println("***AspectJ*** DoBefore() is running!! intercepted by Faizy : "
						+ joinPoint.getSignature().getName());
	}

}
