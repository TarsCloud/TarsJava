package com.qq.tars.spring.bean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class ApplicationFailListener implements SpringApplicationRunListener {
    private final SpringApplication application;
    private final String[] args;

    @Override
    public void starting() {

    }

    public ApplicationFailListener(SpringApplication sa, String[] args) {
        this.application = sa;
        this.args = args;
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        exception.printStackTrace();
        System.out.println("[TARS]  start application fail " + exception.getMessage());
        System.exit(-1);
    }

}
