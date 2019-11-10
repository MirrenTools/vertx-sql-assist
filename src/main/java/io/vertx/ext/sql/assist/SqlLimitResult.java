package io.vertx.ext.sql.assist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 数据库分页返回结果
 * 
 * @author <a href="https://mirrentools.org/">Mirren</a>
 *
 */
public class SqlLimitResult<T> {
	/** 分页返回结果键名称的map */
	private static final Map<String, String> JSON_NAME_KEY_MAPS = new HashMap<>();
	static {
		JSON_NAME_KEY_MAPS.put("totals", "totals");
		JSON_NAME_KEY_MAPS.put("pages", "pages");
		JSON_NAME_KEY_MAPS.put("page", "page");
		JSON_NAME_KEY_MAPS.put("size", "size");
		JSON_NAME_KEY_MAPS.put("data", "data");
	}

	/**
	 * 设置返回结果数据键的名称
	 * 
	 * @param oldName
	 *          totals=数据总行数<br>
	 *          pages=数据总页数 <br>
	 *          page=当前是第几页<br>
	 *          size=每页显示多少行数据<br>
	 *          data=数据
	 * @param newName
	 *          新的名称
	 */
	public static void registerResultKey(String oldName, String newName) {
		if (JSON_NAME_KEY_MAPS.get(oldName) == null) {
			throw new IllegalArgumentException("Failed to return result name, invalid old name, only totals、pages、page、size、data are supported");
		}
		Objects.requireNonNull(newName, "Failed to set return result name, new name can not be null");
		JSON_NAME_KEY_MAPS.put(oldName, newName);
	}

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
		result.put(JSON_NAME_KEY_MAPS.getOrDefault("totals", "totals"), getTotals());
		result.put(JSON_NAME_KEY_MAPS.getOrDefault("pages", "pages"), getPages());
		result.put(JSON_NAME_KEY_MAPS.getOrDefault("page", "page"), getPage());
		result.put(JSON_NAME_KEY_MAPS.getOrDefault("size", "size"), getSize());
		String dataKey = JSON_NAME_KEY_MAPS.getOrDefault("data", "data");
		if (getData() == null) {
			result.put(dataKey, new JsonArray());
		} else {
			result.put(dataKey, getData());
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
