package io.vertx.ext.sql.assist;
@Table("table_user")
public class User {
	/** 用户的id */
	@TableId("id")
	private Long id;
	/** 自增长的数组 */
	@TableColumn(value = "auto_add", alias = "autoAdd")
	private Long autoAdd;
	/** 用户的名字 */
	@TableColumn("nickname")
	private String nickname;
	/** 用户的的密码 */
	@TableColumn(value = "pwd", alias = "possword")
	private String pwd;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAutoAdd() {
		return autoAdd;
	}

	public void setAutoAdd(Long autoAdd) {
		this.autoAdd = autoAdd;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", autoAdd=" + autoAdd + ", nickname=" + nickname + ", pwd=" + pwd + "]";
	}

}
