package com.gooduo.wifitest;

import java.io.Serializable;

public class Item implements Serializable{
	private String name;
	private int dbm;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDbm() {
		return dbm;
	}

	public void setDbm(int dbm) {
		this.dbm = dbm;
	}

}
