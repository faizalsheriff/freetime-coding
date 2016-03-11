package com.javacodegeeks.snippets.enterprise.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;

@Aspect
public class DoAroundAspect {

	@Around("execution(* com.javacodegeeks.snippets.enterprise.SimpleService.sayHello(..))")
	public void doAround(ProceedingJoinPoint joinPoint) throws Throwable {

		System.out.println("***AspectJ*** DoAround() is running!! intercepted : " +  joinPoint.getSignature().getName()
		+ " \narguments : "	+ Arrays.toString(joinPoint.getArgs()));

		System.out.println("***AspectJ*** DoAround() before is running!");
		joinPoint.proceed(); // continue on the intercepted method
		System.out.println("***AspectJ*** DoAround() after is running!");

	}

}
