package io.vertx.ext.sql.assist;

import io.vertx.jdbcclient.JDBCPool;

/**
 * SQL操作JDBC实现
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class SQLExecuteImplJDBC extends SQLExecuteBase implements SQLExecute<JDBCPool> {
	private JDBCPool pool;
	public SQLExecuteImplJDBC(JDBCPool pool) {
		super(pool);
		this.pool = pool;
	}
	@Override
	public JDBCPool getClient() {
		return pool;
	}

}
