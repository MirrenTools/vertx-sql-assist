package io.vertx.ext.sql.assist.sql;

import io.vertx.ext.sql.assist.CommonSQL;
import io.vertx.ext.sql.assist.SQLExecute;
import io.vertx.ext.sql.assist.entity.Student;
import io.vertx.jdbcclient.JDBCPool;
/**
 * 使用JDBCPool的学生SQL操作,这也是使用带注解实体类的SQL示例
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class StudentSQL extends CommonSQL<Student, JDBCPool> {
	public StudentSQL(SQLExecute<JDBCPool> execute) {
		super(execute);
	}
}
