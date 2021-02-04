package io.vertx.ext.sql.assist.sql;

import java.util.List;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.sql.assist.SqlAndParams;
import io.vertx.ext.sql.assist.SqlAssist;
import io.vertx.ext.sql.assist.SqlPropertyValue;
import io.vertx.ext.sql.assist.SqlWhereCondition;
import io.vertx.sqlclient.Tuple;

/**
 * Oracle通用SQL操作
 * 
 * @author <a href="http://mirrentools.org">Mirren</a>
 *
 * @param <T>
 */
public class OracleStatementSQL extends AbstractStatementSQL {
	public OracleStatementSQL(Class<?> entity) {
		super(entity);
	}

	/** 日志工具 */
	private final Logger LOG = LoggerFactory.getLogger(OracleStatementSQL.class);

	@Override
	public SqlAndParams selectAllSQL(SqlAssist assist) {
		if (assist != null && assist.getRowSize() != null) {
			String distinct = assist.getDistinct() == null ? "" : assist.getDistinct();// 去重语句
			String column = assist.getResultColumn() == null ? resultColumns() : assist.getResultColumn();// 表的列名
			StringBuilder sql = new StringBuilder();
			// SQL语句添加分页
			sql.append("select * from ( select temp_table.*, ROWNUM AS tt_row_index from (");
			// SQL语句主查询
			sql.append(String.format("select %s %s from %s", distinct, column, tableName()));
			Tuple params = Tuple.tuple();// 参数
			if (assist.getJoinOrReference() != null) {
				sql.append(assist.getJoinOrReference());
			}
			if (assist.getCondition() != null && assist.getCondition().size() > 0) {
				List<SqlWhereCondition<?>> where = assist.getCondition();
				sql.append(" where " + where.get(0).getRequire());
				if (where.get(0).getValue() != null) {
					params.addValue(where.get(0).getValue());
				}
				if (where.get(0).getValues() != null) {
					for (Object value : where.get(0).getValues()) {
						params.addValue(value);
					}
				}
				for (int i = 1; i < where.size(); i++) {
					sql.append(where.get(i).getRequire());
					if (where.get(i).getValue() != null) {
						params.addValue(where.get(i).getValue());
					}
					if (where.get(i).getValues() != null) {
						for (Object value : where.get(i).getValues()) {
							params.addValue(value);
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
					for (Object value : assist.getHavingValue()) {
						params.addValue(value);
					}
				}
			}
			if (assist.getOrder() != null) {
				sql.append(assist.getOrder());
			}
			// SQL分页语句添加别名与结尾
			sql.append(") temp_table  where ROWNUM <= ? ) tt_result_table ");
			sql.append(" where tt_result_table.tt_row_index >= ? ");
			int startRow = assist.getStartRow() == null ? 0 : assist.getStartRow();
			params.addValue(startRow + assist.getRowSize());
			params.addValue(startRow);
			SqlAndParams result = new SqlAndParams(sql.toString(), (params.size() <= 0 ? null : params));
			if (LOG.isDebugEnabled()) {
				LOG.debug("SelectAllSQL : " + result.toString());
			}
			return result;
		} else {
			return super.selectAllSQL(assist);
		}
	}

	@Override
	public <T> SqlAndParams selectByObjSQL(T obj, String resultColumns, String tableAlias,String joinOrReference, boolean single) {
		StringBuilder sql = new StringBuilder(
				String.format("select %s from %s %s ", (resultColumns == null ? resultColumns() : resultColumns), 
																							 (tableName() + (tableAlias == null ? "" : (" AS " + tableAlias))), 
																							 (joinOrReference == null ? "" : joinOrReference)));
		Tuple params = Tuple.tuple();
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
					sql.append(String.format("where %s = ? ",  (tableAlias == null ? "" : (tableAlias+"."))+pv.getName()));
					isFrist = false;
				} else {
					sql.append(String.format("and %s = ? ",  (tableAlias == null ? "" : (tableAlias+"."))+pv.getName()));
				}
				params.addValue(pv.getValue());
			}
		}
		if (single) {
			if (!isFrist) {
				sql.append(" and ");
			}
			sql.append(" rownum <=1 ");
		}
		SqlAndParams result = new SqlAndParams(sql.toString(), (params.size() <= 0 ? null : params));
		if (LOG.isDebugEnabled()) {
			LOG.debug("selectByObjSQL : " + result.toString());
		}
		return result;
	}

}
