package com.yummynoodlebar.config;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

public class SecureAspec {
	
	@Pointcut(
			"execution(* com.springinaction.springidol.Performer.perform(..))")
	public void performance() {
	}
	
	@Before("performance()")
	 public void turnOffCellPhones() {
		  System.out.println("The audience is turning off their cellphones");
		}
	
	@AfterReturning("performance()")
	  public void applaud() {
	    System.out.println("CLAP CLAP CLAP CLAP CLAP");
	  }
	
	@AfterThrowing("performance()")
	  public void demandRefund() {
	    System.out.println("Boo! We want our money back!");
	  }

}
