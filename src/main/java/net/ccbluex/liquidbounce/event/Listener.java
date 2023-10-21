package net.ccbluex.liquidbounce.event;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Listener {
    Priority value() default Priority.MEDIUM;
}
