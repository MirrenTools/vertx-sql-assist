package io.vertx.ext.sql.assist;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

/**
 * JDBCClient版的SQL实现
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class SQLExecuteMySQLImpl implements SQLExecute<MySQLPool> {
	/** MySQL客户端 */
	private MySQLPool client;

	public SQLExecuteMySQLImpl(MySQLPool client) {
		super();
		this.client = client;
	}
	@Override
	public MySQLPool getClient() {
		return client;
	}
	@Override
	public void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler) {
		queryExecute(qp, query -> {
			if (query.succeeded()) {
				RowSet<Row> result = query.result();
				List<String> names = result.columnsNames();
				List<JsonObject> rows = new ArrayList<>();
				for (Row row : result) {
					JsonObject data = new JsonObject();
					for (int i = 0; i < names.size(); i++) {
						data.put(names.get(i), row.getValue(i));
					}
					rows.add(data);
				}
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
				RowSet<Row> result = query.result();
				List<String> names = result.columnsNames();
				List<JsonObject> rows = new ArrayList<>();
				for (Row row : result) {
					JsonObject data = new JsonObject();
					for (int i = 0; i < names.size(); i++) {
						data.put(names.get(i), row.getValue(i));
					}
					rows.add(data);
				}
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
				RowSet<Row> result = query.result();
				List<JsonArray> rows = new ArrayList<>();
				for (Row row : result) {
					JsonArray data = new JsonArray();
					for (int i = 0; i < row.size(); i++) {
						data.add(row.getValue(i));
					}
					rows.add(data);
				}
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
				int updated = result.result().rowCount();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		});
	}
	@Override
	public void batch(SqlAndParams qp, Handler<AsyncResult<List<Integer>>> handler) {
		if (qp.succeeded()) {
			client.getConnection(conn -> {
				if (conn.succeeded()) {
					SqlConnection connection = conn.result();
					List<Tuple> batch = new ArrayList<>();
					List<JsonArray> params = qp.getBatchParams();
					for (JsonArray param : params) {
						@SuppressWarnings("unchecked")
						List<Object> list = param.getList();
						batch.add(Tuple.tuple(list));
					}
					connection.preparedBatch(qp.getSql(), batch, res -> {
						if (res.succeeded()) {
							connection.close();
						} else {
							handler.handle(Future.failedFuture(res.cause()));
							connection.close();
						}
					});
				} else {
					handler.handle(Future.failedFuture(conn.cause()));
				}
			});
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
	/**
	 * 执行查询
	 * 
	 * @param qp
	 * @param handler
	 */
	public void queryExecute(SqlAndParams qp, Handler<AsyncResult<RowSet<Row>>> handler) {
		if (qp.succeeded()) {
			if (qp.getParams() == null) {
				client.query(qp.getSql(), handler);
			} else {
				@SuppressWarnings("unchecked")
				List<Object> list = qp.getParams().getList();
				client.preparedQuery(qp.getSql(), Tuple.tuple(list), handler);
			}
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
	/**
	 * 执行更新
	 * 
	 * @param qp
	 * @param handler
	 */
	public void updateExecute(SqlAndParams qp, Handler<AsyncResult<RowSet<Row>>> handler) {
		if (qp.succeeded()) {
			if (qp.getParams() == null) {
				client.query(qp.getSql(), handler);
			} else {
				@SuppressWarnings("unchecked")
				List<Object> list = qp.getParams().getList();
				client.preparedQuery(qp.getSql(), Tuple.tuple(list), handler);
			}
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}

}
