package io.vertx.ext.sql.assist;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
/**
 * 数据库命令执行器的默认实现
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
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
		execute.queryAsListArray(qp, set -> {
			if (set.succeeded()) {
				List<JsonArray> rows = set.result();
				if (rows != null && rows.size() >= 0) {
					Object value = rows.get(0).getValue(0);
					if (value instanceof Number) {
						handler.handle(Future.succeededFuture(((Number) value).longValue()));
					} else {
						handler.handle(Future.succeededFuture(0L));
					}
				} else {
					handler.handle(Future.succeededFuture(0L));
				}
			} else {
				handler.handle(Future.failedFuture(set.cause()));
			}
		});
	}
	@Override
	public void selectAll(SqlAssist assist, Handler<AsyncResult<List<JsonObject>>> handler) {
		SqlAndParams qp = statement.selectAllSQL(assist);
		execute.queryAsListObj(qp, handler);
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
		execute.queryAsListObj(qp, handler);
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
	public <T> void insertBatch(List<T> list, Handler<AsyncResult<Long>> handler) {
		SqlAndParams qp = statement.insertBatchSQL(list);
		if (qp.succeeded()) {
			execute.batch(qp, res -> {
				if (res.succeeded()) {
					List<Integer> result = res.result() == null ? new ArrayList<>() : res.result();
					long count = result.stream().count();
					handler.handle(Future.succeededFuture(count));
				} else {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else {
			handler.handle(Future.succeededFuture(0L));
		}
	}
	@Override
	public void insertBatch(List<String> columns, List<JsonArray> params, Handler<AsyncResult<Long>> handler) {
		SqlAndParams qp = statement.insertBatchSQL(columns, params);
		if (qp.succeeded()) {
			execute.batch(qp, res -> {
				if (res.succeeded()) {
					List<Integer> result = res.result() == null ? new ArrayList<>() : res.result();
					long count = result.stream().count();
					handler.handle(Future.succeededFuture(count));
				} else {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else {
			handler.handle(Future.succeededFuture(0L));
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
