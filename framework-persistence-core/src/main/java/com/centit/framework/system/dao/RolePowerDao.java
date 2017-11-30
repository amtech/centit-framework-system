package com.centit.framework.system.dao;

import com.centit.framework.system.po.RolePower;

import java.util.List;
import java.util.Map;

/**
 * 角色权限Dao
 * @author god
 * updated by zou_wy@centit.com
 */
public interface RolePowerDao {

  /**
   * 查询全部
   * @return List<RolePower>
   */
    List<RolePower> listObjectsAll();

  /**
   * 新增
   * @param rolePower 角色权限
   */
    void saveNewRolePower(RolePower rolePower);

  /**
   * 更新角色权限
   * @param rolePower 角色权限对象
   */
  void updateRolePower(RolePower rolePower);

  /**
   * 删除
   * @param rolePowers 角色权限对象
   */
    void deleteObject(RolePower rolePowers);

  /**
   * 根据条件查询
   * @param filterMap 查询条件
   * @return List<RolePower>
   */
    List<RolePower> listObjects(Map<String, Object> filterMap);

  /**
   * 根据角色代码删除角色权限
   * @param roleCode 角色代码
   */
    void deleteRolePowersByRoleCode(String roleCode);

  /**
   * 根据操作代码删除角色权限
   * @param optCode 操作代码
   */
    void deleteRolePowersByOptCode(String optCode);

  /**
   * 根据角色代码查询
   * @param roleCode 角色代码
   * @return List<RolePower>
   */
    List<RolePower> listRolePowersByRoleCode(String roleCode);

}
