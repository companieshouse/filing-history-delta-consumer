package uk.gov.companieshouse.filinghistory.consumer.kafka;

import java.util.concurrent.CountDownLatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LatchAspect {

    private final CountDownLatch latch;

    public LatchAspect(@Value("${steps:1}") int steps) {
        this.latch = new CountDownLatch(steps);
    }

    @After("execution(* Consumer.consume(..))")
    void afterConsume(JoinPoint joinPoint) {
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
