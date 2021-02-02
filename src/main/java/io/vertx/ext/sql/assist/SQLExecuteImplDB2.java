package io.vertx.ext.sql.assist;

import io.vertx.db2client.DB2Pool;

/**
 * SQL操作DB2实现
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class SQLExecuteImplDB2 extends SQLExecuteBase implements SQLExecute<DB2Pool> {
	private DB2Pool pool;
	public SQLExecuteImplDB2(DB2Pool pool) {
		super(pool);
		this.pool = pool;
	}
	@Override
	public DB2Pool getClient() {
		return this.pool;
	}
}
