package io.vertx.ext.sql.assist.entity;

import io.vertx.ext.sql.assist.Table;
import io.vertx.ext.sql.assist.TableColumn;
import io.vertx.ext.sql.assist.TableId;

@Table("classes")
public class Classes {
	/** 班级的id */
	@TableId("id")
	private Integer id;
	/** 班级的名称 */
	@TableColumn("classanme")
	private String classanme;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getClassanme() {
		return classanme;
	}
	public void setClassanme(String classanme) {
		this.classanme = classanme;
	}
	@Override
	public String toString() {
		return "Classes [id=" + id + ", classanme=" + classanme + "]";
	}

}
