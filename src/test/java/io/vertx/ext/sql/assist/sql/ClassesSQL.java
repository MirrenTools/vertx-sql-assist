package io.vertx.ext.sql.assist.sql;

import io.vertx.ext.sql.assist.CommonSQL;
import io.vertx.ext.sql.assist.SQLExecute;
import io.vertx.ext.sql.assist.entity.Classes;
import io.vertx.jdbcclient.JDBCPool;

/**
 * 使用JDBCPool的班级SQL操作,这也是使用带注解实体类的SQL示例
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class ClassesSQL extends CommonSQL<Classes, JDBCPool> {
	public ClassesSQL(SQLExecute<JDBCPool> execute) {
		super(execute);
	}
}
