package com.agapple.asyncload.domain;

import java.util.List;

/**
 * һ��asyncLoad�Ĳ��Զ������
 * 
 * @author jianghang 2011-1-21 ����10:45:19
 */
public interface AsyncLoadTestService {

    public int countRemoteModel(String name, long sleep);

    public void updateRemoteModel(String name, long slepp);

    public AsyncLoadTestModel getRemoteModel(String name, long sleep);

    public String getRemoteName(String name, long sleep);

    public Object getRemoteObject(String name, long sleep);

    public List<AsyncLoadTestModel> listRemoteModel(String name, long sleep);
}
