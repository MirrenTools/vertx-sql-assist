package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

/**
 * JDBCClient版的SQL实现
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class SQLExecuteJDBCImpl implements SQLExecute<JDBCClient> {
	/** JDBC客户端 */
	private JDBCClient jdbcClient;

	public SQLExecuteJDBCImpl(JDBCClient jdbcClient) {
		super();
		this.jdbcClient = jdbcClient;
	}
	@Override
	public JDBCClient getClient() {
		return jdbcClient;
	}
	@Override
	public void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler) {
		queryExecute(qp, query -> {
			if (query.succeeded()) {
				List<JsonObject> rows = query.result().getRows();
				if (rows != null && rows.size() > 0) {
					handler.handle(Future.succeededFuture(rows.get(0)));
				} else {
					handler.handle(Future.succeededFuture());
				}
			} else {
				handler.handle(Future.failedFuture(query.cause()));
			}
		});
	}
	@Override
	public void queryAsListObj(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler) {
		queryExecute(qp, query -> {
			if (query.succeeded()) {
				List<JsonObject> rows = query.result().getRows();
				handler.handle(Future.succeededFuture(rows));
			} else {
				handler.handle(Future.failedFuture(query.cause()));
			}
		});
	}
	@Override
	public void queryAsListArray(SqlAndParams qp, Handler<AsyncResult<List<JsonArray>>> handler) {
		queryExecute(qp, query -> {
			if (query.succeeded()) {
				List<JsonArray> rows = query.result().getResults();
				handler.handle(Future.succeededFuture(rows));
			} else {
				handler.handle(Future.failedFuture(query.cause()));
			}
		});
	}
	@Override
	public void update(SqlAndParams qp, Handler<AsyncResult<Integer>> handler) {
		updateExecute(qp, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		});
	}
	@Override
	public void batch(SqlAndParams qp, Handler<AsyncResult<List<Integer>>> handler) {
		jdbcClient.getConnection(conn -> {
			if (conn.succeeded()) {
				SQLConnection connection = conn.result();
				connection.batchWithParams(qp.getSql(), qp.getBatchParams(), res -> {
					if (res.succeeded()) {
						connection.close(close -> {
							if (close.succeeded()) {
								handler.handle(Future.succeededFuture(res.result()));
							} else {
								handler.handle(Future.failedFuture(close.cause()));
							}
						});
					} else {
						handler.handle(Future.failedFuture(res.cause()));
						connection.close();
					}
				});
			} else {
				handler.handle(Future.failedFuture(conn.cause()));
			}
		});
	}
	/**
	 * 执行查询
	 * 
	 * @param qp
	 * @param handler
	 */
	public void queryExecute(SqlAndParams qp, Handler<AsyncResult<ResultSet>> handler) {
		if (qp.getParams() == null) {
			jdbcClient.query(qp.getSql(), handler);
		} else {
			jdbcClient.queryWithParams(qp.getSql(), qp.getParams(), handler);
		}
	}
	/**
	 * 执行更新
	 * 
	 * @param qp
	 * @param handler
	 */
	public void updateExecute(SqlAndParams qp, Handler<AsyncResult<UpdateResult>> handler) {
		if (qp.getParams() == null) {
			jdbcClient.update(qp.getSql(), handler);
		} else {
			jdbcClient.updateWithParams(qp.getSql(), qp.getParams(), handler);
		}
	}

}
