package io.vertx.ext.sql.assist;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

/**
 * SQL操作的默认实现
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 */
public abstract class SQLExecuteBase {
	/** JDBC客户端 */
	private Pool pool;

	public SQLExecuteBase(Pool pool) {
		super();
		this.pool = pool;
	}

	public void execute(SqlAndParams qp, Handler<AsyncResult<RowSet<Row>>> handler) {
		if (qp.succeeded()) {
			if (qp.getParams() == null) {
				pool.query(qp.getSql()).execute(handler);
			} else {
				pool.preparedQuery(qp.getSql()).execute(qp.getParams(), handler);
			}
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}

	public void queryAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler) {
		execute(qp, res -> {
			if (res.succeeded()) {
				RowSet<Row> rowSet = res.result();
				if (rowSet == null || !rowSet.iterator().hasNext()) {
					handler.handle(Future.succeededFuture());
				} else {
					Row row = rowSet.iterator().next();
					handler.handle(Future.succeededFuture(row.toJson()));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	public void queryAsList(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler) {
		execute(qp, res -> {
			if (res.succeeded()) {
				RowSet<Row> rowSet = res.result();
				if (rowSet == null || rowSet.size() <= 0) {
					handler.handle(Future.succeededFuture(new ArrayList<>()));
				} else {
					List<JsonObject> list = new ArrayList<>();
					for (Row row : rowSet) {
						list.add(row.toJson());
					}
					handler.handle(Future.succeededFuture(list));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	public void update(SqlAndParams qp, Handler<AsyncResult<Integer>> handler) {
		execute(qp, res -> {
			if (res.succeeded()) {
				if (res.result() == null) {
					handler.handle(Future.succeededFuture(0));
				} else {
					handler.handle(Future.succeededFuture(res.result().rowCount()));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	public void updateResult(SqlAndParams qp, PropertyKind<Row> property, Handler<AsyncResult<Row>> handler) {
		execute(qp, res -> {
			if (res.succeeded()) {
				if (property == null || res.result() == null) {
					handler.handle(Future.succeededFuture());
				} else {
					handler.handle(Future.succeededFuture(res.result().property(property)));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	public void batch(SqlAndParams qp, Handler<AsyncResult<Integer>> handler) {
		if (qp.succeeded()) {
			pool.preparedQuery(qp.getSql()).executeBatch(qp.getBatchParams(), res -> {
				if (res.succeeded()) {
					if (res.result() == null) {
						handler.handle(Future.succeededFuture(0));
					} else {
						handler.handle(Future.succeededFuture(res.result().rowCount()));
					}
				} else {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
}
