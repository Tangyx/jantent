package springboot.modal.vo;

import java.io.Serializable;
import java.util.Date;


/*
 * By tangyx
 * 2021.4.14
   *     普通数据
 */


public class BohVo implements Serializable {

	private Integer id;
	private String name;
	private int age;
	private String tel;
	private String mail;
	private String address;
	private Date createtime;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}
	
	private  String ToString() {
		return "Boh["
				+ "id="+id+","
						+ "name="+name+","
								+ "age="+age+","
										+ "tel="+tel+","
												+ "mail="+mail+","
														+ "address="+address+","
																+ "createtime="+createtime+""
																		+ "]";
	}
}
