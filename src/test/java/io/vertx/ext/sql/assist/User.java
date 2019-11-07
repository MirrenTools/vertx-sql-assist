package io.vertx.ext.sql.assist;
@Table("user")
public class User {
	/** 用户的id */
	@TableId("id")
	private Long id;
	/** 用户的名字 */
	@TableColumn("name")
	private String name;
	/** 用户的的密码 */
	@TableColumn(value = "pwd", alias = "possword")
	private String pwd;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

}
