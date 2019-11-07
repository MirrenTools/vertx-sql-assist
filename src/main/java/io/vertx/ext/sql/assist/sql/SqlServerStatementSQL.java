package io.vertx.ext.sql.assist.sql;

import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.ext.sql.assist.SqlWhereCondition;

/**
 * SQL Server通用SQL操作
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 * @param <T>
 */
public class SqlServerStatementSQL extends AbstractStatementSQL {
	public SqlServerStatementSQL(Class<?> entity) {
		super(entity);
	}

	/** 日志工具 */
	private final Logger LOG = LoggerFactory.getLogger(SqlServerStatementSQL.class);
	/**
	 * 分页的排序,子类可以重写该方法
	 * 
	 * @return
	 */

	/**
	 * 分页时指定排序的SQL语句,默认排序主键,子类可以重写该方法排序其他的列<br>
	 * tips:当assist.getOrder()不为空时则该方法无效
	 * 
	 * @param cols
	 * @return
	 */
	protected String getRowNumberOverSQL(String cols) {
		return " order by " + cols + " ";
	}

	@Override
	public SqlAndParams selectAllSQL(SqlAssist assist) {
		if (assist != null && assist.getRowSize() != null) {
			String distinct = assist.getDistinct() == null ? "" : assist.getDistinct();// 去重语句
			String column = assist.getResultColumn() == null ? getSqlResultColumns() : assist.getResultColumn();// 表的列名
			StringBuilder sql = new StringBuilder();
			// SQL语句添加分页
			sql.append("select * from ( ");
			sql.append(String.format("select %s %s,row_number() over(", distinct, column));
			if (assist.getOrder() != null) {
				sql.append(assist.getOrder());
			} else {
				sql.append(getRowNumberOverSQL(getSqlPrimaryId()));
			}
			sql.append(String.format(") AS tt_row_index from %s ", getSqlTableName()));
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
			// SQL分页语句添加别名与结尾
			sql.append(" ) AS tt_result_table ");
			sql.append(" where tt_row_index > ? and tt_row_index <= ? ");
			if (params == null) {
				params = new JsonArray();
			}
			int startRow = assist.getStartRow() == null ? 0 : assist.getStartRow();
			params.add(startRow);
			params.add(startRow + assist.getRowSize());
			SqlAndParams result = new SqlAndParams(sql.toString(), params);
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		} else {
			return super.selectAllSQL(assist);
		}
	}

	@Override
	public <T> SqlAndParams selectByObjSQL(T obj, String resultColumns, String joinOrReference, boolean single) {
		StringBuilder sql = new StringBuilder(
				String.format("select %s %s from %s %s ", (single ? "top 1" : ""), (resultColumns == null ? getSqlResultColumns() : resultColumns),
						getSqlTableName(), (joinOrReference == null ? "" : joinOrReference)));
		JsonArray params = null;
		boolean isFrist = true;
		List<SqlPropertyValue<?>> propertyValue;
		try {
			propertyValue = getPropertyValue(obj);
		} catch (Exception e) {
			return new SqlAndParams(false, " Get SqlPropertyValue failed: " + e.getMessage());
		}
		for (int i = propertyValue.size() - 1; i >= 0; i--) {
			SqlPropertyValue<?> pv = propertyValue.get(i);
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
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByObjSQL : " + result.toString());
		}
		return result;
	}

}
