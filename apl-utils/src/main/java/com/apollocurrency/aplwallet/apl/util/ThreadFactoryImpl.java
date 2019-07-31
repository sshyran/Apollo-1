/*
 * Copyright © 2018 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.inject.Vetoed;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Vetoed
public class ThreadFactoryImpl implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public ThreadFactoryImpl(String threadPrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = threadPrefix +"-";
        log.trace("create ThreadPool/Factory/Executor : Group = '{}', Prefix = '{}'", group, namePrefix);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group,
                r,
                (namePrefix != null ? namePrefix : Thread.currentThread().getName()) + threadNumber.getAndIncrement(),
                0);
        log.trace("ThreadFactory created thread: group = {}, name = {}", t.getThreadGroup().getName(),
                t.getName());
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
