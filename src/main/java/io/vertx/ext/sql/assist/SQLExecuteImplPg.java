package io.vertx.ext.sql.assist;

import io.vertx.pgclient.PgPool;

/**
 * SQL操作PostgreSQL版实现
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 */
public class SQLExecuteImplPg extends SQLExecuteBase implements SQLExecute<PgPool> {
	private PgPool pool;
	public SQLExecuteImplPg(PgPool pool) {
		super(pool);
		this.pool = pool;
	}
	@Override
	public PgPool getClient() {
		return this.pool;
	}
}
