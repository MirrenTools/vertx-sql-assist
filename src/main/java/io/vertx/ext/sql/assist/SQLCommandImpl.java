package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
/**
 * 数据库命令执行器的默认实现
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 */
public class SQLCommandImpl implements SQLCommand {
	/** 语句 */
	private SQLStatement statement;
	/** 执行器 */
	private SQLExecute<?> execute;
	/**
	 * 初始化
	 * 
	 * @param statement
	 * @param execute
	 */
	public SQLCommandImpl(SQLStatement statement, SQLExecute<?> execute) {
		super();
		this.statement = statement;
		this.execute = execute;
	}

	@Override
	public void getCount(SqlAssist assist, Handler<AsyncResult<Long>> handler) {
		SqlAndParams qp = statement.getCountSQL(assist);
		execute.execute(qp, res -> {
			if (res.succeeded()) {
				RowSet<Row> set = res.result();
				if (set == null || !set.iterator().hasNext()) {
					handler.handle(Future.succeededFuture(0L));
				} else {
					Long result = set.iterator().next().getLong(0);
					handler.handle(Future.succeededFuture(result));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	@Override
	public void selectAll(SqlAssist assist, Handler<AsyncResult<List<JsonObject>>> handler) {
		SqlAndParams qp = statement.selectAllSQL(assist);
		execute.queryAsList(qp, handler);
	}

	@Override
	public <S> void selectById(S primaryValue, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler) {
		SqlAndParams qp = statement.selectByIdSQL(primaryValue, resultColumns, joinOrReference);
		execute.queryAsObj(qp, handler);
	}

	@Override
	public <T> void selectSingleByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler) {
		SqlAndParams qp = statement.selectByObjSQL(obj, resultColumns, joinOrReference, true);
		execute.queryAsObj(qp, handler);
	}

	@Override
	public <T> void selectByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<List<JsonObject>>> handler) {
		SqlAndParams qp = statement.selectByObjSQL(obj, resultColumns, joinOrReference, false);
		execute.queryAsList(qp, handler);
	}

	@Override
	public <T> void insertAll(T obj, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.insertAllSQL(obj);
		execute.update(qp, handler);
	}

	@Override
	public <T> void insertNonEmpty(T obj, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.insertNonEmptySQL(obj);
		execute.update(qp, handler);
	}

	@Override
	public <T> void insertNonEmptyGeneratedKeys(T obj, Handler<AsyncResult<Object>> handler) {
		SqlAndParams qp = statement.insertNonEmptySQL(obj);
		execute.updateResult(qp, PropertyKind.create("generated-keys", Row.class), res -> {
			if (res.succeeded()) {
				Row row = res.result();
				if (row != null) {
					Object value = row.getValue(0);
					handler.handle(Future.succeededFuture(value));
				} else {
					handler.handle(Future.succeededFuture());
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	@Override
	public <T> void insertBatch(List<T> list, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.insertBatchSQL(list);
		if (qp.succeeded()) {
			execute.batch(qp, res -> {
				if (res.succeeded()) {
					handler.handle(Future.succeededFuture(res.result()));
				} else {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else {
			handler.handle(Future.succeededFuture(0));
		}
	}

	@Override
	public void insertBatch(List<String> columns, List<Tuple> params, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.insertBatchSQL(columns, params);
		if (qp.succeeded()) {
			execute.batch(qp, res -> {
				if (res.succeeded()) {
					handler.handle(Future.succeededFuture(res.result()));
				} else {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else {
			handler.handle(Future.succeededFuture(0));
		}
	}

	@Override
	public <T> void replace(T obj, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.replaceSQL(obj);
		execute.update(qp, handler);
	}

	@Override
	public <T> void updateAllById(T obj, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.updateAllByIdSQL(obj);
		execute.update(qp, handler);
	}

	@Override
	public <T> void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.updateAllByAssistSQL(obj, assist);
		execute.update(qp, handler);
	}

	@Override
	public <T> void updateNonEmptyById(T obj, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.updateNonEmptyByIdSQL(obj);
		execute.update(qp, handler);
	}

	@Override
	public <T> void updateNonEmptyByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.updateNonEmptyByAssistSQL(obj, assist);
		execute.update(qp, handler);
	}

	@Override
	public <S> void updateSetNullById(S primaryValue, List<String> columns, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.updateSetNullByIdSQL(primaryValue, columns);
		execute.update(qp, handler);
	}

	@Override
	public <T> void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.updateSetNullByAssistSQL(assist, columns);
		execute.update(qp, handler);
	}

	@Override
	public <S> void deleteById(S primaryValue, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.deleteByIdSQL(primaryValue);
		execute.update(qp, handler);
	}

	@Override
	public void deleteByAssist(SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		SqlAndParams qp = statement.deleteByAssistSQL(assist);
		execute.update(qp, handler);
	}

}
