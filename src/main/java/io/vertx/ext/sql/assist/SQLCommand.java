package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;

/**
 * SQL可执行命令
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 */
public interface SQLCommand {
	/**
	 * 获取数据总行数
	 * 
	 * @param assist
	 *          查询工具,如果没有可以为null
	 * @param handler
	 *          返回数据总行数
	 */
	void getCount(SqlAssist assist, Handler<AsyncResult<Long>> handler);

	/**
	 * 通过查询工具查询所有数据
	 * 
	 * @param assist
	 *          查询工具帮助类
	 * @param handler
	 *          结果集
	 */
	void selectAll(SqlAssist assist, Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 分页查询,默认page=1,rowSize=15(取第一页,每页取15行数据)
	 * 
	 * @param assist
	 *          查询工具(注意:startRow在该方法中无效,最后会有page转换为startRow)
	 * @param handler
	 *          返回结果为(JsonObject)格式为: {@link SqlLimitResult#toJson()}
	 */
	default void limitAll(final SqlAssist assist, Handler<AsyncResult<JsonObject>> handler) {
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
	};

	/**
	 * 通过ID查询出数据,并自定义返回列
	 * 
	 * @param primaryValue
	 *          主键值
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果:如果查询得到返回JsonObject如果查询不到返回null
	 */
	<S> void selectById(S primaryValue, String resultColumns, String tableAlias, String joinOrReference,
			Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据,只取查询出来的第一条数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          结果:如果存在返回JsonObject,不存在返回null
	 */
	<T> void selectSingleByObj(T obj, String resultColumns, String tableAlias, String joinOrReference,
			Handler<AsyncResult<JsonObject>> handler);

	/**
	 * 将对象属性不为null的属性作为条件查询出数据
	 * 
	 * @param obj
	 *          对象
	 * @param resultColumns
	 *          自定义返回列
	 * @param tableAlias
	 *          当前表的别名
	 * @param joinOrReference
	 *          多表查询或表连接的语句,示例 inner join table2 as t2 on t.id=t2.id
	 * @param handler
	 *          返回结果集
	 */
	<T> void selectByObj(T obj, String resultColumns, String tableAlias, String joinOrReference,
			Handler<AsyncResult<List<JsonObject>>> handler);

	/**
	 * 插入一个对象包括属性值为null的值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	<T> void insertAll(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 插入一个对象,只插入对象中值不为null的属性
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	<T> void insertNonEmpty(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 插入一个对象,只插入对象中值不为null的属性
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 */
	<T> void insertNonEmptyGeneratedKeys(T obj, Handler<AsyncResult<Object>> handler);

	/**
	 * 批量添加全部所有字段
	 * 
	 * @param list
	 *          对象
	 * @param handler
	 *          成功返回受影响的行数,如果对象为null或空则返回0
	 */
	<T> void insertBatch(List<T> list, Handler<AsyncResult<Integer>> handler);
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
	void insertBatch(List<String> columns, List<Tuple> params, Handler<AsyncResult<Integer>> handler);

	/**
	 * 插入一个对象,如果该对象不存在就新建如果该对象已经存在就更新
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          结果集受影响的行数
	 */
	<T> void replace(T obj, Handler<AsyncResult<Integer>> handler);

	/**
	 * 更新一个对象中所有的属性包括null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateAllById(T obj, Handler<AsyncResult<Integer>> handler);

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
	<T> void updateAllByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler);
	/**
	 * 更新一个对象中属性不为null值,条件为对象中的主键值
	 * 
	 * @param obj
	 *          对象
	 * @param handler
	 *          返回操作结果
	 */
	<T> void updateNonEmptyById(T obj, Handler<AsyncResult<Integer>> handler);

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
	<T> void updateNonEmptyByAssist(T obj, SqlAssist assist, Handler<AsyncResult<Integer>> handler);

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
	<S> void updateSetNullById(S primaryValue, List<String> columns, Handler<AsyncResult<Integer>> handler);

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
	<T> void updateSetNullByAssist(SqlAssist assist, List<String> columns, Handler<AsyncResult<Integer>> handler);

	/**
	 * 通过主键值删除对应的数据行
	 * 
	 * @param obj
	 *          主键值
	 * @param handler
	 *          返回操作结果
	 */
	<S> void deleteById(S primaryValue, Handler<AsyncResult<Integer>> handler);
	/**
	 * 通过SqlAssist条件集删除对应的数据行
	 * 
	 * @param assist
	 *          条件集
	 * 
	 * @param handler
	 *          返回操作结果
	 */
	void deleteByAssist(SqlAssist assist, Handler<AsyncResult<Integer>> handler);

}
