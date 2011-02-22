package com.agapple.asyncload.domain;

import java.io.Serializable;

/**
 * һ��asyncLoad�Ĳ��Զ���model�����صĶ���
 * 
 * @author jianghang 2011-1-21 ����10:45:41
 */
public class AsyncLoadTestModel implements Serializable {

    private static final long serialVersionUID = -5410019316926096126L;
    public int                id;
    public String             name;
    public String             detail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "AsyncLoadTestModel [detail=" + detail + ", id=" + id + ", name=" + name + "]";
    }
}
