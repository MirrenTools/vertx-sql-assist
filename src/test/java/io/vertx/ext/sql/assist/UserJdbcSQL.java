package io.vertx.ext.sql.assist;

import io.vertx.jdbcclient.JDBCPool;

public class UserJdbcSQL extends CommonSQL<User, JDBCPool> {
	public UserJdbcSQL(SQLExecute<JDBCPool> execute) {
		super(execute);
	}

}
