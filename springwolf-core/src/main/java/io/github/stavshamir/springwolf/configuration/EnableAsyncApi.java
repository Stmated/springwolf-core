package io.github.stavshamir.springwolf.configuration;

import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enable the documentation of asynchronous consumer endpoints (methods annotated by @KafkaListener in a class annotated with @{@link Component}).
 * <br>
 * This annotation should be applied to a Spring java config and should have an accompanying '@{@link Configuration}' annotation.
 * @author Stav Shamir
 */
@Retention(value=RUNTIME)
@Target(value=TYPE)
@ComponentScan(basePackages={"io/github/stavshamir/springwolf"})
public @interface EnableAsyncApi { }
