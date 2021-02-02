package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.db2client.DB2Pool;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

/**
 * SQL执行器
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 */
public interface SQLExecute<T> {
	/**
	 * 创建JDBC客户端实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<JDBCPool> createJDBC(JDBCPool pool) {
		return new SQLExecuteImplJDBC(pool);
	}

	/**
	 * 创建MySQL客户端实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<MySQLPool> createMySQL(MySQLPool pool) {
		return new SQLExecuteImplMySQL(pool);
	}

	/**
	 * 创建MySQL客户端实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<PgPool> createPg(PgPool pool) {
		return new SQLExecuteImplPg(pool);
	}
	/**
	 * 创建DB2客户端实例
	 * 
	 * @param client
	 * @return
	 */
	static SQLExecute<DB2Pool> createPg(DB2Pool pool) {
		return new SQLExecuteImplDB2(pool);
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
	void execute(SqlAndParams qp, Handler<AsyncResult<RowSet<Row>>> handler);

	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果 查询不到结果的时候返回null
	 */
	void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果 查询不到结果的时候返回不为null的空List
	 */
	void queryAsList(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler);

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
	 * 执行更新并操作结果,比如返回GENERATED_KEYS等:<br>
	 * 获取GENERATED_KEYS方法:property传入JDBCPool.GENERATED_KEYS得到结果后判断row!=null就执行row.get对应类型数据
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 */
	void updateResult(SqlAndParams qp, PropertyKind<Row> property, Handler<AsyncResult<Row>> handler);

	/**
	 * 批量操作
	 * 
	 * @param qp
	 *          SQL语句与批量参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	void batch(SqlAndParams qp, Handler<AsyncResult<Integer>> handler);
}
