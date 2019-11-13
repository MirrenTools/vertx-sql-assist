package io.vertx.ext.sql.assist;

import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.assist.CommonSQL;
import io.vertx.ext.sql.assist.SQLExecute;

public class JDBCSQL extends CommonSQL<User, JDBCClient> {
	public JDBCSQL(SQLExecute<JDBCClient> execute) {
		super(execute);
	}

}
