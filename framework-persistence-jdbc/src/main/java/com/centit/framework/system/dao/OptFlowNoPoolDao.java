package com.centit.framework.system.dao;

import com.centit.framework.system.po.OptFlowNoPool;
import com.centit.framework.system.po.OptFlowNoPoolId;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OptFlowNoPoolDao {
	 OptFlowNoPool getObjectById(OptFlowNoPoolId cid);
	
	 void deleteObject(OptFlowNoPool optFlowNoPool);
	
	 void deleteObjectById(OptFlowNoPoolId cid);
	
	 void saveObject(OptFlowNoPool optFlowNoPool);
	
	
     int  pageCount(Map<String, Object> filterDescMap);
     List<OptFlowNoPool>  pageQuery(Map<String, Object> pageQureyMap);
	
	
    /**
     *  "select min(CurNo) as MinNo from F_OptFlowNoPool" +
                " where OwnerCode = " + QueryUtils.buildStringForQuery(ownerCode) +
                " and CodeCode = " + QueryUtils.buildStringForQuery(ownerCode) +
                " and CodeDate = to_date(" + QueryUtils.buildStringForQuery(
                DatetimeOpt.convertDatetimeToString(codeBaseDate))
                + ",'YYYY-MM-DD HH:MI:SS')");
     * @return long
     */
     long fetchFirstLsh(String ownerCode, String codeCode, Date codeBaseDate);
}