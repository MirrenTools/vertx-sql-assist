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
 * DB2通用SQL操作
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 * @param <T>
 */
public abstract class DB2<T> extends AbstractSQL<T> {
	/** 日志工具 */
	private final Logger LOG = LoggerFactory.getLogger(DB2.class);

	@Override
	protected SqlAndParams selectAllSQL(SqlAssist assist) {
		if (assist != null && assist.getRowSize() != null) {
			String distinct = assist.getDistinct() == null ? "" : assist.getDistinct();// 去重语句
			String column = assist.getResultColumn() == null ? columns() : assist.getResultColumn();// 表的列名
			StringBuilder sql = new StringBuilder();
			// SQL语句添加分页
			sql.append("select * from ( select temp_table.*, rownumber () over () as tt_row_index from (");
			// SQL语句主查询
			sql.append(String.format("select %s %s from %s", distinct, column, tableName()));
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
			// SQL分页语句添加别名与结尾
			sql.append(") AS temp_table ) AS tt_result_table ");
			sql.append(" where tt_result_table.tt_row_index between ? and ? ");
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
			sql.append(" FETCH FIRST 1 ROWS ONLY ");
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), params);
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByObjSQL : " + result.toString());
		}
		return result;
	}

}
