package io.vertx.ext.sql.assist;

import io.vertx.mysqlclient.MySQLPool;

/**
 * SQL操作MySQL版实现
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class SQLExecuteImplMySQL extends SQLExecuteBase implements SQLExecute<MySQLPool> {
	private MySQLPool pool;
	public SQLExecuteImplMySQL(MySQLPool pool) {
		super(pool);
		this.pool = pool;
	}
	@Override
	public MySQLPool getClient() {
		return this.pool;
	}
}
