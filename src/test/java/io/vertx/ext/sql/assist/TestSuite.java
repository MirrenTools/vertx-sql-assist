package io.vertx.ext.sql.assist;

import java.util.Objects;

import org.mirrentools.sd.ScrewDriver;
import org.mirrentools.sd.ScrewDriverDbUtil;
import org.mirrentools.sd.SdType;
import org.mirrentools.sd.models.SdBean;
import org.mirrentools.sd.models.SdColumn;
import org.mirrentools.sd.models.db.update.SdBasicTableContent;
import org.mirrentools.sd.options.ScrewDriverOptions;
import org.mirrentools.sd.options.SdDatabaseOptions;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
/**
 * 测试方法合集
 * 
 * @author <a href="https://mirrentools.org">Mirren</a>
 *
 * @param <E>
 * @param <C>
 */
public class TestSuite<E, C> {
	/** 默认的id */
	private final static long DEFAULT_ID = 1L;
	/** 默认的名称 */
	private final static String DEFAULT_NAME = "name";
	/** 默认的密码 */
	private final static String DEFAULT_PWD = "pwd";

	private CommonSQL<E, C> sql;
	/** 数据库连接信息 */
	private JsonObject config;
	public TestSuite(CommonSQL<E, C> sql, JsonObject config) {
		this.sql = sql;
		this.config = config;
	}
	/**
	 * 执行测试
	 */
	public void runTest(Handler<AsyncResult<Integer>> handler) {
		try {
			this.resetDB();
		} catch (Exception e) {
			e.printStackTrace();
			handler.handle(Future.failedFuture(e));
		}
		step(1, handler);
	}
	/**
	 * 执行测试
	 * 
	 * @param step
	 * @param handler
	 */
	private void step(int step, Handler<AsyncResult<Integer>> handler) {
		switch (step) {
			case 1 :
				insert(res -> {
					if (res.succeeded()) {
						step(step + 1, handler);
					} else {
						handler.handle(Future.failedFuture(res.cause()));
					}
				});
				break;
			case 2 :
				getCount(res -> {
					if (res.succeeded()) {
						step(step + 1, handler);
					} else {
						handler.handle(Future.failedFuture(res.cause()));
					}
				});
				break;
			case 3 :
				selectById(res -> {
					if (res.succeeded()) {
						step(step + 1, handler);
					} else {
						handler.handle(Future.failedFuture(res.cause()));
					}
				});
				break;
			case 4 :
				deleteById(res -> {
					if (res.succeeded()) {
						step(step + 1, handler);
					} else {
						handler.handle(Future.failedFuture(res.cause()));
					}
				});
				break;
			default :
				handler.handle(Future.succeededFuture(step));
		}
	}

	/**
	 * 重设数据库表
	 * 
	 * @throws Exception
	 */
	private void resetDB() throws Exception {
		SdDatabaseOptions dbOptions = new SdDatabaseOptions(config.getString("driver_class"), config.getString("url"));
		if (config.getString("user") != null) {
			dbOptions.setUser(config.getString("user"));
		}
		if (config.getString("user") != null) {
			dbOptions.setUser(config.getString("user"));
		}
		String tableName = "table_user";

		ScrewDriverDbUtil dbUtil = ScrewDriverDbUtil.instance(dbOptions);
		SdBasicTableContent tableContent = new SdBasicTableContent() {
		};
		tableContent.setTableName(tableName);
		dbUtil.deleteTable(tableContent);

		ScrewDriver screwDriver = ScrewDriver.instance(new ScrewDriverOptions(dbOptions));
		SdBean bean = new SdBean().setName(tableName);
		bean.addColumn(new SdColumn().setName("id").setType(SdType.LONG).setPrimary(true));
		bean.addColumn(new SdColumn().setName("auto_add").setType(SdType.LONG).setAutoIncrement(true));
		bean.addColumn(new SdColumn().setName("nickname").setType(SdType.STRING).setLength(255));
		bean.addColumn(new SdColumn().setName("pwd").setType(SdType.STRING).setLength(255));
		screwDriver.createTable(bean);
	}

	private void insert(Handler<AsyncResult<Boolean>> handler) {
		User user = new User();
		user.setId(DEFAULT_ID);
		user.setNickname(DEFAULT_NAME);
		sql.insertAll(user).onSuccess(res -> {
			if (Objects.equals(1, res)) {
				handler.handle(Future.succeededFuture());
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("insert 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}
	/**
	 * 测试获取数据库行数
	 * 
	 * @param handler
	 */
	private void getCount(Handler<AsyncResult<Boolean>> handler) {
		sql.getCount().onSuccess(res -> {
			if (res >= 1) {
				handler.handle(Future.succeededFuture());
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("getCount 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	private void selectById(Handler<AsyncResult<Boolean>> handler) {
		sql.selectById(DEFAULT_ID).onSuccess(res -> {
			if (Objects.equals(DEFAULT_NAME, res.getString("nickname"))) {
				handler.handle(Future.succeededFuture());
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("selectById 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

	private void deleteById(Handler<AsyncResult<Boolean>> handler) {
		sql.deleteById(DEFAULT_ID).onSuccess(res -> {
			if (Objects.equals(1, res)) {
				handler.handle(Future.succeededFuture());
			} else {
				System.out.println(res);
				handler.handle(Future.failedFuture("deleteById 结果不匹配"));
			}
		}).onFailure(err -> handler.handle(Future.failedFuture(err)));
	}

}
