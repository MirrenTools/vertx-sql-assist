package io.vertx.ext.sql.assist;

import io.vertx.mysqlclient.MySQLPool;

public class MySQLSQL extends CommonSQL<MySQLPool> {
	public MySQLSQL(SQLExecute<MySQLPool> execute) {
		super(User.class, execute);
	}

}
