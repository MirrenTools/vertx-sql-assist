package io.vertx.ext.sql.assist;

import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 数据库分页返回结果
 * 
 * @author <a href="https://mirrentools.org/">Mirren</a>
 *
 */
public class SqlLimitResult<T> {
	/** 数据总行数 */
	private long totals;
	/** 数据总页数 */
	private int pages;
	/** 当前是第几页 */
	private int page;
	/** 每页显示多少行数据 */
	private int size;
	/** 数据 */
	private List<T> data;
	/**
	 * 初始化
	 * 
	 * @param totals
	 *          总行数
	 * @param page
	 *          当前页
	 * @param size
	 *          每页显示多少行数据
	 */
	public SqlLimitResult(long totals, int page, int size) {
		super();
		this.totals = totals;
		this.page = page;
		this.size = size;
	}

	/**
	 * 将当前对象装换为JsonObject:<br>
	 * totals(long):数据总行数<br>
	 * pages(int):数据总页数<br>
	 * page(int):当前是第几页<br>
	 * size(int):每页显示多少行数据<br>
	 * data(List<T>):数据
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		result.put("totals", getTotals());
		result.put("pages", getPages());
		result.put("page", getPage());
		result.put("size", getSize());
		if (getData() == null) {
			result.put("data", new JsonArray());
		} else {
			result.put("data", getData());
		}
		return result;
	}

	/**
	 * 获取数据总行数
	 * 
	 * @return
	 */
	public long getTotals() {
		return totals;
	}
	/**
	 * 设置数据总行数
	 * 
	 * @param totals
	 * @return
	 */
	public SqlLimitResult<T> setTotals(long totals) {
		this.totals = totals;
		return this;
	}
	/**
	 * 获取数据总页数
	 * 
	 * @return
	 */
	public int getPages() {
		if (totals == 0) {
			return 0;
		}
		if (totals % size == 0) {
			pages = (int) (totals / size);
		} else {
			pages = (int) (totals / size) + 1;
		}
		return pages;
	}

	/**
	 * 获取当前是第几页
	 * 
	 * @return
	 */
	public int getPage() {
		return page;
	}
	/**
	 * 获取每页显示多少行数据
	 * 
	 * @return
	 */
	public int getSize() {
		return size;
	}
	/**
	 * 获取数据
	 * 
	 * @return
	 */
	public List<T> getData() {
		return data;
	}
	/**
	 * 设置数据
	 * 
	 * @param data
	 * @return
	 */
	public SqlLimitResult<T> setData(List<T> data) {
		this.data = data;
		return this;
	}
	@Override
	public String toString() {
		return "SqlLimitResult [totals=" + totals + ", pages=" + pages + ", page=" + page + ", size=" + size + ", data=" + data + "]";
	}

}
