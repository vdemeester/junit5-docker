package com.github.junit5docker;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.github.junit5docker.WaitFor.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerExtension.class)
public @interface Docker {
    String image();

    Port[] ports();

    Environment[] environments() default {};

    WaitFor waitFor() default @WaitFor(NOTHING);
}