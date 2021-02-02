package io.vertx.ext.sql.assist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类属性关联列的注解
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableColumn {
	String value();
	String alias() default "";
}
