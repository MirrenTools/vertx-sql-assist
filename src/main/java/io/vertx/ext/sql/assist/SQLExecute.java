package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.pgclient.PgPool;

/**
 * SQL执行器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface SQLExecute<T> {
	/**
	 * 通过JDBC客户端创建一个实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<JDBCClient> create(JDBCClient client) {
		return new SQLExecuteJDBCImpl(client);
	}
	/**
	 * 通过 MySQL客户端创建一个实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<MySQLPool> create(MySQLPool client) {
		return new SQLExecuteMySQLImpl(client);
	}
	/**
	 * 通过 PostgreSQL客户端创建一个实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<PgPool> create(PgPool client) {
		return new SQLExecutePgImpl(client);
	}

	/**
	 * 获取客户端
	 * 
	 * @return
	 */
	T getClient();
	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler);
	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsListObj(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler);
	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	void queryAsListArray(SqlAndParams qp, Handler<AsyncResult<List<JsonArray>>> handler);

	/**
	 * 执行更新等操作得到受影响的行数
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 */
	void update(SqlAndParams qp, Handler<AsyncResult<Integer>> handler);

	/**
	 * 批量操作
	 * 
	 * @param qp
	 *          SQL语句与批量参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	void batch(SqlAndParams qp, Handler<AsyncResult<List<Integer>>> handler);
}
