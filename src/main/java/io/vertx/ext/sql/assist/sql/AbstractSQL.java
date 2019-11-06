package io.vertx.ext.sql.assist.sql;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.SqlLimitResult;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.ext.sql.assist.SqlWhereCondition;

/**
 * 抽象数据库操作语句,默认以MySQL标准来编写,如果其他数据库可以基础并重写不兼容的方法<br>
 * 通常不支持limit分页的数据库需要重写{@link #selectAllSQL(SqlAssist)}与{@link #selectByObjSQL(Object, String, String, boolean)}这两个方法
 * 
 * @author <a href="https://mirrentools.org/">Mirren</a>
 * @param <T>
 */
public abstract class AbstractSQL<T> {

	/** 日志工具 */
	private final Logger LOG = LoggerFactory.getLogger(AbstractSQL.class);

	// ====================================
	// =============抽象方法区=============
	// ====================================
	/**
	 * 表的名字
	 * 
	 * @return
	 */
	protected abstract String tableName();

	/**
	 * 表的主键
	 * 
	 * @return
	 */
	protected abstract String primaryId();

	/**
	 * 表的所有列名
	 * 
	 * @return
	 */
	protected abstract String columns();

	/**
	 * 表的所有列名与列名对应的值
	 * 
	 * @return
	 */
	protected abstract List<SqlPropertyValue<?>> propertyValue(T obj);
	/**
	 * 获取JDBC客户端
	 * 
	 * @return
	 */
	protected abstract JDBCClient jdbcClient();

	// ====================================
	// =============通用方法区=============
	// ====================================
	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	public void queryExecuteAsObj(SqlAndParams qp, Handler<AsyncResult<JsonObject>> handler) {
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
	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * @param handler
	 *          返回结果
	 */
	public void queryExecuteAsList(SqlAndParams qp, Handler<AsyncResult<List<JsonObject>>> handler) {
		queryExecute(qp, query -> {
			if (query.succeeded()) {
				List<JsonObject> rows = query.result().getRows();
				handler.handle(Future.succeededFuture(rows));
			} else {
				handler.handle(Future.failedFuture(query.cause()));
			}
		});
	}

	/**
	 * 执行查询
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	public void queryExecute(SqlAndParams qp, Handler<AsyncResult<ResultSet>> handler) {
		if (qp.getParams() == null) {
			jdbcClient().query(qp.getSql(), handler);
		} else {
			jdbcClient().queryWithParams(qp.getSql(), qp.getParams(), handler);
		}
	}
	/**
	 * 执行更新等操作得到受影响的行数
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 */
	public void updateExecuteResult(SqlAndParams qp, Handler<AsyncResult<Integer>> handler) {
		updateExecute(qp, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		});
	}

	/**
	 * 执行更新等操作
	 * 
	 * @param qp
	 *          SQL语句与参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	public void updateExecute(SqlAndParams qp, Handler<AsyncResult<UpdateResult>> handler) {
		if (qp.getParams() == null) {
			jdbcClient().update(qp.getSql(), handler);
		} else {
			jdbcClient().updateWithParams(qp.getSql(), qp.getParams(), handler);
		}
	}
	/**
	 * 批量操作
	 * 
	 * @param qp
	 *          SQL语句与批量参数
	 * 
	 * @param handler
	 *          返回结果
	 */
	public void batchExecute(SqlAndParams qp, Handler<AsyncResult<List<Integer>>> handler) {
		jdbcClient().getConnection(conn -> {
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

	// ====================================
	// ==============主方法区==============
	// ====================================

	/**
	 * 获得数据总行数SQL语句<br>
	 * 
	 * @param assist
	 * @return 返回:sql or sql与params
	 */
	protected SqlAndParams getCountSQL(SqlAssist assist) {
		StringBuilder sql = new StringBuilder(String.format("select count(*) from %s ", tableName()));
		JsonArray params = null;
		if (assist != null) {
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				List<SqlWhereCondition<?>> where = assist.getCondition();
				params = new JsonArray();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.add(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.add(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.add(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.add(value);
						}
					}
				}
			}
		}
		if (assist.getGroupBy() != null) {
			sql.append(" group by " + assist.getGroupBy() + " ");
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("getCountSQL : " + result.toString());
		}
		return result;
	}

	/**
	 * 获得数据总行数
	 * 
	 * @param handler
	 *          返回数据总行数
	 */
	public void getCount(Handler<AsyncResult<Long>> handler) {
		getCount(null, handler);
	}
	/**
	 * 获取数据总行数
	 * 
	 * @param assist
	 *          查询工具,如果没有可以为null
	 * @param handler
	 *          返回数据总行数
	 */
	public void getCount(SqlAssist assist, Handler<AsyncResult<Long>> handler) {
		getCount(assist, set -> {
			if (set.succeeded()) {
				List<JsonArray> rows = set.result().getResults();
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
		}, null);
	}
	/**
	 * 获取数据总行数
	 * 
	 * @param handler
	 *          结果为{@link AsyncResult<ResultSet>}
	 * @param nullable
	 *          用户重载方法没有实际作用传入null便可
	 */
	public void getCount(Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		getCount(null, handler, nullable);
	}
	/**
	 * 获取数据总行数
	 * 
	 * @param assist
	 *          查询工具,如果没有可以为null
	 * @param handler
	 *          结果为{@link AsyncResult<ResultSet>}
	 * @param nullable
	 *          用户重载方法没有实际作用传入null便可
	 */
	public void getCount(SqlAssist assist, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		SqlAndParams qp = getCountSQL(assist);
		queryExecute(qp, handler);
	}

	/**
	 * 获得查询全部数据SQL语句与参数<br>
	 * 
	 * @param assist
	 *          查询工具
	 * @return 返回:sql or sql与params
	 */
	protected SqlAndParams selectAllSQL(SqlAssist assist) {
		// 如果Assist为空返回默认默认查询语句,反则根据Assist生成语句sql语句
		if (assist == null) {
			SqlAndParams result = new SqlAndParams(String.format("select %s from %s ", columns(), tableName()));
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		} else {
			String distinct = assist.getDistinct() == null ? "" : assist.getDistinct();// 去重语句
			String column = assist.getResultColumn() == null ? columns() : assist.getResultColumn();// 表的列名
			// 初始化SQL语句
			StringBuilder sql = new StringBuilder(String.format("select %s %s from %s", distinct, column, tableName()));
			JsonArray params = null;// 参数
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				List<SqlWhereCondition<?>> where = assist.getCondition();
				params = new JsonArray();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.add(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.add(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.add(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.add(value);
						}
					}
				}
			}
			if (assist.getGroupBy() != null) {
				sql.append(" group by " + assist.getGroupBy() + " ");
			}
			if (assist.getHaving() != null) {
				sql.append(" having " + assist.getHaving() + " ");
				if (assist.getHavingValue() != null) {
					if (params == null) {
						params = new JsonArray();
					}
					params.addAll(assist.getHavingValue());
				}
			}
			if (assist.getOrder() != null) {
				sql.append(assist.getOrder());
			}
			if (assist.getRowSize() != null || assist.getStartRow() != null) {
				if (params == null) {
					params = new JsonArray();
				}
				if (assist.getStartRow() != null) {
					sql.append(" LIMIT ?");
					params.add(assist.getRowSize());
				}
				if (assist.getStartRow() != null) {
					sql.append(" OFFSET ?");
					params.add(assist.getStartRow());
				}
			}
			SqlAndParams result = new SqlAndParams(sql.toString(), params);
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		}
	}
	/**
	 * 查询所有数据
	 * 
	 * @param handler
	 *          结果集
	 */
	public void selectAll(Handler<AsyncResult<List<JsonObject>>> handler) {
		selectAll(null, handler);
	}
	/**
	 * 通过查询工具查询所有数据
	 * 
	 * @param assist
	 *          查询工具帮助类
	 * @param handler
	 *          结果集
	 */
	public void selectAll(SqlAssist assist, Handler<AsyncResult<List<JsonObject>>> handler) {
		selectAll(assist, set -> {
			if (set.succeeded()) {
				List<JsonObject> rows = set.result().getRows();
				handler.handle(Future.succeededFuture(rows));
			} else {
				handler.handle(Future.failedFuture(set.cause()));
			}
		}, null);
	}
	/**
	 * 查询所有数据
	 * 
	 * @param handler
	 *          结果集{@link AsyncResult<ResultSet>}
	 * @param nullable
	 *          没有任何作用传null就可以了,用于重载方法而已
	 */
	public void selectAll(Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		selectAll(null, handler, nullable);
	}
	/**
	 * 通过查询工具查询所有数据
	 * 
	 * @param assist
	 *          查询工具帮助类
	 * @param handler
	 *          结果集{@link AsyncResult<ResultSet>}
	 * @param nullable
	 *          没有任何作用传null就可以了,用于重载方法而已
	 */
	public void selectAll(SqlAssist assist, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		SqlAndParams qp = selectAllSQL(assist);
		queryExecute(qp, handler);
	}
	/**
	 * 分页查询,默认page=1,rowSize=15(取第一页,每页取15行数据)
	 * 
	 * @param assist
	 *          查询工具(注意:startRow在该方法中无效,最后会有page转换为startRow)
	 * @param handler
	 *          返回结果为(JsonObject)格式为: {@link SqlLimitResult#toJson()}
	 */
	public void limitAll(final SqlAssist assist, Handler<AsyncResult<JsonObject>> handler) {
		if (assist == null) {
			handler.handle(Future.failedFuture("The SqlAssist cannot be null , you can pass in new SqlAssist()"));
			return;
		}
		if (assist.getPage() == null || assist.getPage() < 1) {
			assist.setPage(1);
		}
		if (assist.getRowSize() == null || assist.getRowSize() < 1) {
			assist.setRowSize(15);
		}
		if (assist.getPage() == 1) {
			assist.setStartRow(0);
		} else {
			assist.setStartRow((assist.getPage() - 1) * assist.getRowSize());
		}
		getCount(assist, cres -> {
			if (cres.succeeded()) {
				Long count = cres.result();
				SqlLimitResult<JsonObject> result = new SqlLimitResult<>(count, assist.getPage(), assist.getRowSize());
				if (count == 0 || assist.getPage() > result.getPages()) {
					handler.handle(Future.succeededFuture(result.toJson()));
				} else {
					selectAll(assist, dres -> {
						if (dres.succeeded()) {
							result.setData(dres.result());
							handler.handle(Future.succeededFuture(result.toJson()));
						} else {
							handler.handle(Future.failedFuture(dres.cause()));
						}
					});
				}
			} else {
				handler.handle(Future.failedFuture(cres.cause()));
			}
		});
	}

	/**
	 * 通过主键查询一个对象<br>
	 * 返回:sql or sql与params
	 * 
	 * @param <S>
	 * @param primaryValue
	 *          主键的值
	 * @param resultColumns
	 *          指定返回列 格式 [table.]列名 [as 类的属性名字],...
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @return
	 */
	protected <S> SqlAndParams selectByIdSQL(S primaryValue, String resultColumns, String joinOrReference) {
		String sql = String.format("select %s from %s %s where %s = ? ", (resultColumns == null ? columns() : resultColumns), tableName(),
				(joinOrReference == null ? "" : joinOrReference), primaryId());
		JsonArray params = new JsonArray();
		params.add(primaryValue);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByIdSQL : " + result.toString());
		}
		return result;
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	public <S> void selectById(S primaryValue, Handler<AsyncResult<JsonObject>> handler) {
		selectById(primaryValue, null, null, handler);
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param handler
	 *          返回结果:ResultSet
	 * @param nullable
	 *          没有实际用处只是重载用,可以传null
	 */
	public <S> void selectById(S primaryValue, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		selectById(primaryValue, null, null, handler, nullable);
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	public <S> void selectById(S primaryValue, String resultColumns, Handler<AsyncResult<JsonObject>> handler) {
		selectById(primaryValue, resultColumns, null, handler);
	}

	/**
	 * 通过ID查询出数据
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param handler
	 *          返回结果:ResultSet
	 * @param nullable
	 *          没有实际用处只是重载用,可以传null
	 */
	public <S> void selectById(S primaryValue, String resultColumns, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		selectById(primaryValue, resultColumns, null, handler, nullable);
	}

	/**
	 * 通过ID查询出数据,并自定义返回列
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	public <S> void selectById(S primaryValue, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler) {
		selectById(primaryValue, resultColumns, joinOrReference, set -> {
			if (set.succeeded()) {
				List<JsonObject> rows = set.result().getRows();
				if (rows != null && rows.size() > 0) {
					handler.handle(Future.succeededFuture(rows.get(0)));
				} else {
					handler.handle(Future.succeededFuture());
				}
			} else {
				handler.handle(Future.failedFuture(set.cause()));
			}
		}, null);
	}
	/**
	 * 通过ID查询出数据,并自定义返回列
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果:ResultSet
	 * @param nullable
	 *          没有实际用处只是重载用,可以传null
	 */
	public <S> void selectById(S primaryValue, String resultColumns, String joinOrReference, Handler<AsyncResult<ResultSet>> handler,
			Boolean nullable) {
		SqlAndParams qp = selectByIdSQL(primaryValue, resultColumns, joinOrReference);
		queryExecute(qp, handler);
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param single
	 *          是否支取一条数据true支取一条,false取全部
	 * @return 返回sql 或 sql与params
	 * 
	 */
	protected SqlAndParams selectByObjSQL(T obj, String resultColumns, String joinOrReference, boolean single) {
		StringBuilder sql = new StringBuilder(String.format("select %s from %s %s ", (resultColumns == null ? columns() : resultColumns),
				tableName(), (joinOrReference == null ? "" : joinOrReference)));
		JsonArray params = null;
		boolean isFrist = true;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (pv.getValue() != null) {
				if (isFrist) {
					params = new JsonArray();
					sql.append(String.format("where %s = ? ", pv.getName()));
					params.add(pv.getValue());
					isFrist = false;
				} else {
					sql.append(String.format("and %s = ? ", pv.getName()));
					params.add(pv.getValue());
				}
			}
		}
		if (single) {
			sql.append(" LIMIT 1");
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByObjSQL : " + result.toString());
		}
		return result;
	}

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据;
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	public void selectSingleByObj(T obj, Handler<AsyncResult<JsonObject>> handler) {
		selectSingleByObj(obj, null, null, handler);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * 
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	public void selectSingleByObj(T obj, String resultColumns, Handler<AsyncResult<JsonObject>> handler) {
		selectSingleByObj(obj, resultColumns, null, handler);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	public void selectSingleByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<JsonObject>> handler) {
		SqlAndParams qp = selectByObjSQL(obj, resultColumns, joinOrReference, true);
		queryExecute(qp, set -> {
			if (set.succeeded()) {
				List<JsonObject> rows = set.result().getRows();
				JsonObject result = null;
				if (rows != null && rows.size() > 0) {
					result = rows.get(0);
				}
				handler.handle(Future.succeededFuture(result));
			} else {
				handler.handle(Future.failedFuture(set.cause()));
			}
		});
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回结果集
	 */
	public void selectByObj(T obj, Handler<AsyncResult<List<JsonObject>>> handler) {
		selectByObj(obj, null, null, handler);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * 
	 * @param handler
	 *          返回结果集
	 */
	public void selectByObj(T obj, String resultColumns, Handler<AsyncResult<List<JsonObject>>> handler) {
		selectByObj(obj, resultColumns, null, handler);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果集
	 */
	public void selectByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<List<JsonObject>>> handler) {
		selectByObj(obj, resultColumns, joinOrReference, set -> {
			if (set.succeeded()) {
				List<JsonObject> rows = set.result().getRows();
				handler.handle(Future.succeededFuture(rows));
			} else {
				handler.handle(Future.failedFuture(set.cause()));
			}
		}, null);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回结果集
	 */
	public void selectByObj(T obj, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		selectByObj(obj, null, null, handler, nullable);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param handler
	 *          返回结果集
	 */
	public void selectByObj(T obj, String resultColumns, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		selectByObj(obj, resultColumns, null, handler, nullable);
	}
	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 as t inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果集
	 */
	public void selectByObj(T obj, String resultColumns, String joinOrReference, Handler<AsyncResult<ResultSet>> handler, Boolean nullable) {
		SqlAndParams qp = selectByObjSQL(obj, resultColumns, joinOrReference, false);
		queryExecute(qp, handler);
	}

	/**
	 * 插入一个对象包括属性值为null的值<br>
	 * 
	 * @param obj
	 * @return 返回:sql 或者 sql与params
	 */
	protected SqlAndParams insertAllSQL(T obj) {
		JsonArray params = null;
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(pv.getName());
				tempValues = new StringBuilder("?");
				params = new JsonArray();
			} else {
				tempColumn.append("," + pv.getName());
				tempValues.append(",?");
			}
			if (pv.getValue() != null) {
				params.add(pv.getValue());
			} else {
				params.addNull();
			}
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertAllSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 插入一个对象包括属性值为null的值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	public void insertAll(T obj, Handler<AsyncResult<Integer>> handler) {
		insertAll(obj, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 插入一个对象包括属性值为null的值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void insertAll(T obj, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = insertAllSQL(obj);
		updateExecute(qp, handler);
	}

	/**
	 * 插入一个对象,只插入对象中值不为null的属性<br>
	 * 
	 * @param obj
	 *          对象
	 * @return 返回:sql 或 sql与params
	 */
	protected SqlAndParams insertNonEmptySQL(T obj) {
		JsonArray params = null;
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					tempColumn = new StringBuilder(pv.getName());
					tempValues = new StringBuilder("?");
					params = new JsonArray();
				} else {
					tempColumn.append("," + pv.getName());
					tempValues.append(",?");
				}
				params.add(pv.getValue());
			}
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertNonEmptySQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 插入一个对象,只插入对象中值不为null的属性
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	public void insertNonEmpty(T obj, Handler<AsyncResult<Integer>> handler) {
		insertNonEmpty(obj, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 插入一个对象,只插入对象中值不为null的属性
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void insertNonEmpty(T obj, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = insertNonEmptySQL(obj);
		updateExecute(qp, handler);
	}
	/**
	 * 批量添加全部所有字段
	 * 
	 * @param list
	 *          对象
	 * @param handler
	 *          成功返回受影响的行数,如果对象为null或空则返回0
	 */
	public void insertBatch(List<T> list, Handler<AsyncResult<Long>> handler) {
		if (list == null || list.isEmpty()) {
			handler.handle(Future.succeededFuture(0L));
			return;
		}
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		JsonArray param0 = new JsonArray();
		for (SqlPropertyValue<?> pv : propertyValue(list.get(0))) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(pv.getName());
				tempValues = new StringBuilder("?");
			} else {
				tempColumn.append("," + pv.getName());
				tempValues.append(",?");
			}
			param0.add(pv.getValue());
		}
		List<JsonArray> params = new ArrayList<>();
		params.add(param0);
		for (int i = 1; i < list.size(); i++) {
			JsonArray paramx = new JsonArray();
			for (SqlPropertyValue<?> pv : propertyValue(list.get(i))) {
				paramx.add(pv.getValue());
			}
			params.add(paramx);
		}

		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams qp = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertBatch : " + qp.toString());
		}
		batchExecute(qp, res -> {
			if (res.succeeded()) {
				List<Integer> result = res.result() == null ? new ArrayList<>() : res.result();
				long count = result.stream().count();
				handler.handle(Future.succeededFuture(count));
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}
	/**
	 * 批量添加自定字段
	 * 
	 * @param column
	 *          字段的名称示例:["id","name",...]
	 * @param params
	 *          字段对应的参数示例:[["id","name"],["id","name"]...]
	 * @param handler
	 *          成功返回受影响的行数,如果字段或字段参数为null或空则返回0
	 */
	public void insertBatch(List<String> columns, List<JsonArray> params, Handler<AsyncResult<Long>> handler) {
		if ((columns == null || columns.isEmpty()) || (params == null || params.isEmpty())) {
			handler.handle(Future.succeededFuture(0L));
			return;
		}
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		for (String column : columns) {
			if (tempColumn == null) {
				tempColumn = new StringBuilder(column);
				tempValues = new StringBuilder("?");
			} else {
				tempColumn.append("," + column);
				tempValues.append(",?");
			}
		}
		String sql = String.format("insert into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams qp = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("insertBatch : " + qp.toString());
		}
		batchExecute(qp, res -> {
			if (res.succeeded()) {
				List<Integer> result = res.result() == null ? new ArrayList<>() : res.result();
				long count = result.stream().count();
				handler.handle(Future.succeededFuture(count));
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	/**
	 * 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
	 * 
	 * @param obj
	 *          对象
	 * @return 返回:sql 或 sql与params
	 */
	protected SqlAndParams replaceSQL(T obj) {
		JsonArray params = null;
		StringBuilder tempColumn = null;
		StringBuilder tempValues = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					tempColumn = new StringBuilder(pv.getName());
					tempValues = new StringBuilder("?");
					params = new JsonArray();
				} else {
					tempColumn.append("," + pv.getName());
					tempValues.append(",?");
				}
				params.add(pv.getValue());
			}
		}
		String sql = String.format("replace into %s (%s) values (%s) ", tableName(), tempColumn, tempValues);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("replaceSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          结果集受影响的行数
	 */
	public void replace(T obj, Handler<AsyncResult<Integer>> handler) {
		replace(obj, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}

	/**
	 * 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          结果集
	 * @param nullable
	 *          没有什么作用,只是用于重载
	 */
	public void replace(T obj, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = replaceSQL(obj);
		updateExecute(qp, handler);
	}
	/**
	 * 更新一个对象中所有的属性包括null值,条件为对象中的主键值
	 * 
	 * @param obj
	 * @return 返回:sql or sql与params, 如果对象中的id为null将会返回SQL:"there is no primary key
	 *         in your SQL statement"
	 */
	protected SqlAndParams updateAllByIdSQL(T obj) {
		if (primaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		JsonArray params = null;
		StringBuilder tempColumn = null;
		Object tempIdValue = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (pv.getName().equals(primaryId())) {
				tempIdValue = pv.getValue();
				continue;
			}
			if (tempColumn == null) {
				params = new JsonArray();
				tempColumn = new StringBuilder(pv.getName() + " = ? ");
			} else {
				tempColumn.append(", " + pv.getName() + " = ? ");
			}
			if (pv.getValue() != null) {
				params.add(pv.getValue());
			} else {
				params.addNull();
			}
		}
		if (tempIdValue == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		params.add(tempIdValue);
		String sql = String.format("update %s set %s where %s = ? ", tableName(), tempColumn, primaryId());
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAllByIdSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 更新一个对象中所有的属性包括null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	public void updateAllById(T obj, Handler<AsyncResult<Integer>> handler) {
		updateAllById(obj, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 更新一个对象中所有的属性包括null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void updateAllById(T obj, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = updateAllByIdSQL(obj);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}

	/**
	 * 更新一个对象中所有的属性包括null值,条件为SqlAssist条件集<br>
	 * 
	 * @param obj
	 * @param assist
	 * @return 返回:sql or
	 *         sql与params如果SqlAssist对象或者对象的Condition为null将会返回SQL:"SqlAssist or
	 *         SqlAssist.condition is null"
	 */
	protected SqlAndParams updateAllByAssistSQL(T obj, SqlAssist assist) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		JsonArray params = null;
		StringBuilder tempColumn = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (tempColumn == null) {
				params = new JsonArray();
				tempColumn = new StringBuilder(pv.getName() + " = ? ");
			} else {
				tempColumn.append(", " + pv.getName() + " = ? ");
			}
			if (pv.getValue() != null) {
				params.add(pv.getValue());
			} else {
				params.addNull();
			}
		}
		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", tableName(), tempColumn, whereStr == null ? "" : whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateAllByAssistSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 更新一个对象中所有的属性包括null值,条件为SqlAssist条件集<br>
	 * 
	 * @param obj
	 *          对象
	 * @param SqlAssist
	 *          sql帮助工具
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	public void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		updateAllByAssist(obj, assist, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 更新一个对象中所有的属性包括null值,条件为SqlAssist条件集<br>
	 * 
	 * @param obj
	 *          对象
	 * @param assist
	 *          sql帮助工具
	 * 
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = updateAllByAssistSQL(obj, assist);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
	/**
	 * 更新一个对象中属性不为null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * @return 返回:sql or sql与params , 如果id为null或者没有要更新的数据将返回SQL:"there is no
	 *         primary key in your SQL statement"
	 */
	protected SqlAndParams updateNonEmptyByIdSQL(T obj) {
		if (primaryId() == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("there is no primary key in your SQL statement");
			}
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		JsonArray params = null;
		StringBuilder tempColumn = null;
		Object tempIdValue = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (pv.getName().equals(primaryId())) {
				tempIdValue = pv.getValue();
				continue;
			}
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					params = new JsonArray();
					tempColumn = new StringBuilder(pv.getName() + " = ? ");
				} else {
					tempColumn.append(", " + pv.getName() + " = ? ");
				}
				params.add(pv.getValue());
			}
		}
		if (tempColumn == null || tempIdValue == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("there is no set update value or no primary key in your SQL statement");
			}
			return new SqlAndParams(false, "there is no set update value or no primary key in your SQL statement");
		}
		params.add(tempIdValue);
		String sql = String.format("update %s set %s where %s = ? ", tableName(), tempColumn, primaryId());
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateNonEmptyByIdSQL : " + result.toString());
		}
		return result;
	}

	/**
	 * 更新一个对象中属性不为null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	public void updateNonEmptyById(T obj, Handler<AsyncResult<Integer>> handler) {
		updateNonEmptyById(obj, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 更新一个对象中属性不为null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void updateNonEmptyById(T obj, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = updateNonEmptyByIdSQL(obj);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}

	/**
	 * 将对象中属性值不为null的进行更新,条件为SqlAssist条件集
	 * 
	 * @param obj
	 *          对象
	 * @param assist
	 *          查询工具
	 * @return 返回:sql or sql与params , 如果assist为null将会返回sql:"SqlAssist or
	 *         SqlAssist.condition is null"
	 */
	protected SqlAndParams updateNonEmptyByAssistSQL(T obj, SqlAssist assist) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		JsonArray params = null;
		StringBuilder tempColumn = null;
		for (SqlPropertyValue<?> pv : propertyValue(obj)) {
			if (pv.getValue() != null) {
				if (tempColumn == null) {
					params = new JsonArray();
					tempColumn = new StringBuilder(pv.getName() + " = ? ");
				} else {
					tempColumn.append(", " + pv.getName() + " = ? ");
				}
				params.add(pv.getValue());
			}
		}
		if (tempColumn == null) {
			return new SqlAndParams(false, "The object has no value");
		}

		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", tableName(), tempColumn, whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateNonEmptyByAssistSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 更新一个对象中属性不为null值,条件为SqlAssist条件集
	 * 
	 * @param obj
	 *          对象
	 * @param assist
	 *          查询工具
	 * @param handler
	 *          返回操作结果
	 */
	public void updateNonEmptyByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		updateNonEmptyByAssist(obj, assist, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 更新一个对象中属性不为null值,条件为SqlAssist条件集
	 * 
	 * @param obj
	 *          对象
	 * @param assist
	 *          sql帮助工具
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void updateNonEmptyByAssist(T obj, SqlAssist assist, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = updateNonEmptyByAssistSQL(obj, assist);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
	/**
	 * 通过id将指定列设置为null
	 * 
	 * @param <S>
	 * @param primaryValue
	 *          id
	 * @param columns
	 *          要设置为空的列
	 * @return
	 */
	protected <S> SqlAndParams updateSetNullByIdSQL(S primaryValue, List<String> columns) {
		if (primaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}

		if (columns == null || columns.size() == 0) {
			return new SqlAndParams(false, "Columns cannot be null or empty");
		}
		StringBuilder setStr = new StringBuilder();
		setStr.append(" " + columns.get(0) + " = null ");
		for (int i = 1; i < columns.size(); i++) {
			setStr.append(", " + columns.get(i) + " = null ");
		}
		String sql = String.format("update %s set %s where %s = ? ", tableName(), setStr.toString(), primaryId());
		JsonArray params = new JsonArray();
		params.add(primaryValue);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateSetNullById : " + result.toString());
		}
		return result;
	}
	/**
	 * 通过主键值设置指定的列为空
	 * 
	 * @param <S>
	 * @param primaryValue
	 *          主键
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	public <S> void updateSetNullById(S primaryValue, List<String> columns, Handler<AsyncResult<Integer>> handler) {
		updateSetNullById(primaryValue, columns, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 通过主键值设置指定的列为空
	 * 
	 * @param <S>
	 * @param primaryValue
	 *          主键
	 * @param columns
	 *          要设置为null的列
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public <S> void updateSetNullById(S primaryValue, List<String> columns, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = updateSetNullByIdSQL(primaryValue, columns);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
	/**
	 * 
	 * 将指定的列设置为空,条件为SqlAssist条件集
	 * 
	 * @param assist
	 *          查询工具
	 * @param columns
	 *          要设置为空的列
	 * @return 返回:sql or sql与params , 如果assist为null将会返回sql:"SqlAssist or
	 *         SqlAssist.condition is null"
	 * @return
	 */
	protected SqlAndParams updateSetNullByAssistSQL(SqlAssist assist, List<String> columns) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		if (columns == null || columns.size() == 0) {
			return new SqlAndParams(false, "Columns cannot be null or empty");
		}
		StringBuilder setStr = new StringBuilder();
		setStr.append(" " + columns.get(0) + " = null ");
		for (int i = 1; i < columns.size(); i++) {
			setStr.append(", " + columns.get(i) + " = null ");
		}
		JsonArray params = new JsonArray();
		List<SqlWhereCondition<?>> where = assist.getCondition();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		}
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("update %s set %s %s", tableName(), setStr.toString(), whereStr);
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("updateSetNullByAssist : " + result.toString());
		}
		return result;
	}
	/**
	 * 通过Assist作为条件设置指定的列为空
	 * 
	 * @param assist
	 *          sql帮助工具
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<Integer>> handler) {
		updateSetNullByAssist(assist, columns, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 通过Assist作为条件设置指定的列为空
	 * 
	 * @param assist
	 *          sql帮助工具
	 * @param columns
	 *          要设置为null的列
	 * 
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = updateSetNullByAssistSQL(assist, columns);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}

	/**
	 * 通过主键值删除对应的数据行
	 * 
	 * @param primaryValue
	 *          id值
	 * @return 返回:sql or sql与params , 如果id为null或者没有要更新的数据将返回SQL:"there is no
	 *         primary key in your SQL statement"
	 */
	protected <S> SqlAndParams deleteByIdSQL(S primaryValue) {
		if (primaryId() == null) {
			return new SqlAndParams(false, "there is no primary key in your SQL statement");
		}
		String sql = String.format("delete from %s where %s = ? ", tableName(), primaryId());
		JsonArray params = new JsonArray();
		params.add(primaryValue);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("deleteByIdSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 通过主键值删除对应的数据行
	 * 
	 * @param obj
	 *          主键值
	 * @param handler
	 *          返回操作结果
	 */
	public <S> void deleteById(S primaryValue, Handler<AsyncResult<Integer>> handler) {
		deleteById(primaryValue, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 通过主键值删除对应的数据行
	 * 
	 * @param obj
	 *          主键值
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public <S> void deleteById(S primaryValue, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = deleteByIdSQL(primaryValue);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}

	/**
	 * 通过SqlAssist条件集删除对应的数据行
	 * 
	 * @param assist
	 *          查询工具
	 * @return 返回:sql or sql与params , 如果assist为null将会返回sql: "SqlAssist or
	 *         SqlAssist.condition is null"
	 */
	protected SqlAndParams deleteByAssistSQL(SqlAssist assist) {
		if (assist == null || assist.getCondition() == null || assist.getCondition().size() < 1) {
			return new SqlAndParams(false, "SqlAssist or SqlAssist.condition is null");
		}
		List<SqlWhereCondition<?>> where = assist.getCondition();
		JsonArray params = new JsonArray();
		StringBuilder whereStr = new StringBuilder(" where " + where.get(0).getRequire());
		if (where.get(0).getValue() != null) {
			params.add(where.get(0).getValue());
		}
		if (where.get(0).getValues() != null) {
			for (Object value : where.get(0).getValues()) {
				params.add(value);
			}
		} ;
		for (int i = 1; i < where.size(); i++) {
			whereStr.append(where.get(i).getRequire());
			if (where.get(i).getValue() != null) {
				params.add(where.get(i).getValue());
			}
			if (where.get(i).getValues() != null) {
				for (Object value : where.get(i).getValues()) {
					params.add(value);
				}
			}
		}
		String sql = String.format("delete from %s %s", tableName(), whereStr);
		SqlAndParams result = new SqlAndParams(sql, params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("deleteByAssistSQL : " + result.toString());
		}
		return result;
	}
	/**
	 * 通过SqlAssist条件集删除对应的数据行
	 * 
	 * @param assist
	 *          条件集
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	public void deleteByAssist(SqlAssist assist, Handler<AsyncResult<Integer>> handler) {
		deleteByAssist(assist, result -> {
			if (result.succeeded()) {
				int updated = result.result().getUpdated();
				handler.handle(Future.succeededFuture(updated));
			} else {
				handler.handle(Future.failedFuture(result.cause()));
			}
		}, null);
	}
	/**
	 * 通过SqlAssist条件集删除对应的数据行
	 * 
	 * @param assist
	 *          条件集
	 * 
	 * @param handler
	 *          返回操作结果
	 * @param nullable
	 *          没有实际作用,用于做方法重载而已,可以传入null
	 */
	public void deleteByAssist(SqlAssist assist, Handler<AsyncResult<UpdateResult>> handler, Boolean nullable) {
		SqlAndParams qp = deleteByAssistSQL(assist);
		if (qp.succeeded()) {
			updateExecute(qp, handler);
		} else {
			handler.handle(Future.failedFuture(qp.getSql()));
		}
	}
}
