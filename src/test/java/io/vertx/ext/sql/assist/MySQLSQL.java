package io.vertx.ext.sql.assist;

import io.vertx.mysqlclient.MySQLPool;

public class MySQLSQL extends CommonSQL<User, MySQLPool> {
	public MySQLSQL(SQLExecute<MySQLPool> execute) {
		super(execute);
	}

}
