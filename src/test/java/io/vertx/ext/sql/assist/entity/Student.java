package io.vertx.ext.sql.assist.entity;

import io.vertx.ext.sql.assist.Table;
import io.vertx.ext.sql.assist.TableColumn;
import io.vertx.ext.sql.assist.TableId;

@Table("student")
public class Student {
	/** 用户的id */
	@TableId("id")
	private Integer id;
	/** 自增长的数组 */
	@TableColumn("cid")
	private Integer cid;
	/** 用户的名字 */
	@TableColumn("nickname")
	private String nickname;
	/** 用户的的密码 */
	@TableColumn(value = "pwd", alias = "possword")
	private String pwd;

	public Integer getId() {
		return id;
	}

	public Student setId(Integer id) {
		this.id = id;
		return this;
	}

	public Integer getCid() {
		return cid;
	}

	public Student setCid(Integer cid) {
		this.cid = cid;
		return this;
	}

	public String getNickname() {
		return nickname;
	}

	public Student setNickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public String getPwd() {
		return pwd;
	}

	public Student setPwd(String pwd) {
		this.pwd = pwd;
		return this;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", cid=" + cid + ", nickname=" + nickname + ", pwd=" + pwd + "]";
	}

}
