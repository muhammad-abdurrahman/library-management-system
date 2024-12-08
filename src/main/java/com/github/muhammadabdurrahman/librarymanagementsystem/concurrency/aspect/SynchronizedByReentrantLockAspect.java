package com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.aspect;

import com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.annotation.LockKey;
import com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.annotation.SynchronizedByReentrantLock;
import com.github.muhammadabdurrahman.librarymanagementsystem.concurrency.model.Lockable;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class SynchronizedByReentrantLockAspect {

  // Map to hold locks for each bean and lock key
  private final ConcurrentHashMap<Object, ConcurrentHashMap<Object, ReentrantLock>> locksByBeanAndLockKey = new ConcurrentHashMap<>();

  @PreDestroy
  public void cleanup() {
    locksByBeanAndLockKey.clear();
  }

  @Around("@annotation(synchronizedByReentrantLock)")
  public Object lockByKey(ProceedingJoinPoint joinPoint, SynchronizedByReentrantLock synchronizedByReentrantLock) throws Throwable {
    Object bean = joinPoint.getTarget();
    Object lockKey = extractLockKey(joinPoint);
    ConcurrentHashMap<Object, ReentrantLock> locksForBean = locksByBeanAndLockKey.computeIfAbsent(bean, k -> new ConcurrentHashMap<>());
    ReentrantLock lockForKey = locksForBean.computeIfAbsent(lockKey, k -> new ReentrantLock(true));
    lockForKey.lock();
    try {
      return joinPoint.proceed();
    } finally {
      lockForKey.unlock();
      locksForBean.remove(lockKey, lockForKey);
    }
  }

  // Extracts the lock key from the method arguments
  private Object extractLockKey(ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Object[] args = joinPoint.getArgs();

    return Arrays.stream(methodSignature.getMethod().getParameters())
        .filter(parameter -> parameter.isAnnotationPresent(LockKey.class))
        .map(parameter -> args[Arrays.asList(methodSignature.getMethod().getParameters()).indexOf(parameter)])
        .findFirst()
        .map(arg -> arg instanceof Lockable lockable ? lockable.getLockKey() : arg)
        .orElseThrow(() -> new IllegalArgumentException("No valid lock key found in method arguments"));
  }
}