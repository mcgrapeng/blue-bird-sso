package com.bird.sso;

import com.bird.UserCenterApplication;
import com.google.common.collect.Maps;
import com.bird.common.mq.birdKafkaExecutor;
import com.bird.common.mq.config.ProfileProperties;
import com.bird.sso.conts.Constants;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.domain.SysRole;
import com.bird.sso.enums.MenuTypeEnum;
import com.bird.sso.mapper.OrganizationMapper;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.service.*;
import com.bird.sso.web.ValidList;
import com.bird.sso.web.controller.manage.menu.UserResourceForm;
import com.bird.sso.web.controller.manage.role.UserRoleForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {UserCenterApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)// 指定启动类
@Slf4j
public class TestDemo {

    @Autowired
    private RoleManageService roleManageService;

    @Autowired
    private RoleQueryService roleQueryService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private MenuManageService menuManageService;

    @Autowired
    private OrganizeManageService organizationManageService;

    @Autowired
    private OrganizeQueryService organizeQueryService;

    @Autowired
    private birdKafkaExecutor kafkaExecutor;

    @Autowired
    private ProfileProperties profileProperties;

    @Autowired
    private OrganizationMapper organizationMapper;

    /**
     * 新增角色
     */
    @Test
    public void test01(){
        ValidList<UserRoleForm> roles = new ValidList<>();
        UserRoleForm roleForm1 = new UserRoleForm();
        roleForm1.setSid(1L);
        roleForm1.setName("测试管理员666");
        roleForm1.setCode("ROLE_HOSPITAL_GOV_TEST6");
        roleForm1.setAppType("APP_UF");
        UserRoleForm roleForm2 = new UserRoleForm();
        roleForm2.setSid(1L);
        roleForm2.setName("测试管理员777");
        roleForm2.setCode("ROLE_HOSPITAL_GOV_TEST7");
        roleForm2.setAppType("APP_UF");
        UserRoleForm roleForm3 = new UserRoleForm();
        roleForm3.setSid(1L);
        roleForm3.setName("测试管理员666");
        roleForm3.setCode("ROLE_HOSPITAL_GOV_TEST6");
        roleForm3.setAppType("APP_UF");
        roles.add(roleForm1);
        roles.add(roleForm2);
        roles.add(roleForm3);
        roleManageService.addInfo(SysRole.transform(roles.getList()));
    }

    /**
     * 更新角色
     */
    @Test
    public void test02(){
        ValidList<UserRoleForm> roles = new ValidList<>();
        UserRoleForm roleForm1 = new UserRoleForm();
        roleForm1.setSid(135944457945089L);
        roleForm1.setName("测试管理员333");
        roleForm1.setCode("ROLE_HOSPITAL_GOV_TEST3");
        roleForm1.setAppType("APP_UF");
        roles.add(roleForm1);
//        UserRoleForm roleForm2 = new UserRoleForm();
//        roleForm2.setSid(135944464236545L);
//        roleForm2.setName("测试管理员443");
//        roleForm2.setCode("ROLE_HOSPITAL_GOV_TEST4");
//        roleForm2.setAppType("APP_UF");
//        roles.add(roleForm2);
        roleManageService.updateRole(SysRole.transform(roles.getList()));
    }

    /**
     * 获取角色
     */
    @Test
    public void test03(){
        SysRole sysRole = roleQueryService.findRole(160242226888705L, "APP_UF");
        System.out.println(sysRole.getSid());
    }

    /**
     * 角色列表
     */
    @Test
    public void test04(){
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", "APP_UF");
        params.put("type", "NORMAL");
        params.put("name", StringUtils.join("%", "管理员", "%"));
        params.put("isAvailable", "Y");
        PageBean<SysRole> page = roleManageService.page(new PageParam(1, 15)
                , params);
        List<SysRole> recordList = page.getRecordList();
        for (SysRole sysRole : recordList) {
            System.out.println(sysRole.getSid());
        }
    }

    /**
     * 设置默认
     */
    @Test
    public void test05(){
        roleManageService.setDefault(161501562535937L, "APP_UF");
    }

    /**
     * 启用/停用角色
     */
    @Test
    public void test06(){
        roleManageService.changeStatus(161501562535938L, "APP_UF", "N");
    }



    //--------------------------menu-----------------------

    /**
     * 新增菜单
     */
    @Test
    public void test07(){
        ValidList<UserResourceForm> menus = new ValidList<>();
        //单个添加
        //一级菜单
//        UserResourceForm resourceForm = new UserResourceForm();
//        resourceForm.setSid(1L);
//        resourceForm.setCode("APP_UF_M0001");
//        resourceForm.setAppType("APP_UF");
//        resourceForm.setName("基本信息");
//        resourceForm.setClientType("PC");
//        resourceForm.setLevel(1);
//        resourceForm.setType("MENU");
//        resourceForm.setRouter("/Statistics");
//        menus.add(resourceForm);
        //二级菜单
//        UserResourceForm resourceForm1 = new UserResourceForm();
//        resourceForm1.setSid(1L);
//        resourceForm1.setCode("APP_UF_M0002");
//        resourceForm1.setAppType("APP_UF");
//        resourceForm1.setName("用户信息");
//        resourceForm1.setClientType("PC");
//        resourceForm1.setLevel(1);
//        resourceForm1.setType("MENU");
//        resourceForm1.setRouter("/Statistics");
//        resourceForm1.setPid(161683754647553L);
//        menus.add(resourceForm1);
        //三级菜单
//        UserResourceForm resourceForm2 = new UserResourceForm();
//        resourceForm2.setSid(1L);
//        resourceForm2.setCode("APP_UF_M0003");
//        resourceForm2.setAppType("APP_UF");
//        resourceForm2.setName("用户状态");
//        resourceForm2.setClientType("PC");
//        resourceForm2.setLevel();
//        resourceForm2.setType("MENU");
//        resourceForm2.setRouter("/Statistics");
//        resourceForm2.setPid(161686430613505L);
//        menus.add(resourceForm2);

        //多个添加
        UserResourceForm resForm = new UserResourceForm();
        resForm.setSid(1L);
        resForm.setCode("M90202111");
        resForm.setAppType("APP_EP");
        resForm.setName("街乡数据发送");
        resForm.setClientType("PC");
        resForm.setLevel(1);
        resForm.setPid(80001029L);
        resForm.setIcon("from");
        resForm.setRouter("/vaccine/vaccinationYet");
        resForm.setPath("/vaccine/vaccinationYet");
        resForm.setIsAvailable("Y");
        resForm.setType("MENU");
        menus.add(resForm);
//        UserResourceForm resForm1 = new UserResourceForm();
//        resForm1.setSid(1L);
//        resForm1.setCode("APP_UF_M0008");
//        resForm1.setAppType("APP_UF");
//        resForm1.setName("分组设置");
//        resForm1.setClientType("PC");
//        resForm1.setLevel(2);
//        resForm1.setType("MENU");
//        resForm1.setRouter("/Statistics");
//        menus.add(resForm1);
        menuManageService.addInfo(SysMenu.transform(menus.getList()));
    }



    /**
     * 更新菜单
     */
    @Test
    public void test08(){
        ValidList<UserResourceForm> menus = new ValidList<>();
        UserResourceForm resForm = new UserResourceForm();
        resForm.setSid(162777727762433L);
        resForm.setCode("APP_UF_M0007");
        resForm.setAppType("APP_UF");
        resForm.setName("主题设置");
        resForm.setClientType("PC");
        resForm.setLevel(2);
        resForm.setType("MENU");
        resForm.setRouter("/qqqqq");
        menus.add(resForm);
        UserResourceForm resForm1 = new UserResourceForm();
        resForm1.setSid(162777811648513L);
        resForm1.setCode("APP_UF_M0008");
        resForm1.setAppType("APP_UF");
        resForm1.setName("分组设置");
        resForm1.setClientType("PC");
        resForm1.setLevel(2);
        resForm1.setType("MENU");
        resForm1.setRouter("/qqqqq");
        menus.add(resForm1);
        menuManageService.updateMenu(SysMenu.transform(menus.getList()));

    }

    /**
     * 获取资源
     */
    @Test
    public void test09(){
        SysMenu app_uf = menuManageService.findInfoBySid("APP_UF", 160429328498689L);
        System.out.println(app_uf.getName());
    }

    /**
     * 菜单列表
     */
    @Test
    public void test10(){
        Map<String, Object> params = Maps.newHashMap();
        params.put("clientType", "PC");
        params.put("appType", "APP_UF");
        params.put("isAvailable", "Y");
        params.put("name", StringUtils.join("%", "用户", "%"));
        PageBean<SysMenu> page = menuManageService.page(new PageParam(1, 15), params);
        List<SysMenu> recordList = page.getRecordList();
        for (SysMenu sysMenu : recordList) {
            System.out.println(sysMenu.getName());
        }
    }

    /**
     * 生成代码
     */
    @Test
    public void test11(){
        String s = commonService.produceCode(Constants.RedisPrefix.SYS_MENU_CODE, "M", "APP_UF");
        System.out.println(s);
    }

    /**
     * 获取某个业务下某个级别的菜单列表
     */
    @Test
    public void test12(){
        Map<String, Object> params = Maps.newHashMap();
        params.put("level", 1);
        params.put("appType", "APP_UF");
        params.put("type", MenuTypeEnum.MENU.name());
        List<SysMenu> list = menuManageService.list(params);
        for (SysMenu sysMenu : list) {
            System.out.println(sysMenu.getName());
        }
    }

    /**
     * 获取某个业务下的菜单列表
     */
    @Test
    public void test13(){
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", "APP_UF");
        params.put("type", MenuTypeEnum.MENU.name());
        List<SysMenu> list = menuManageService.list(params);
        for (SysMenu sysMenu : list) {
            System.out.println(sysMenu.getName());
        }
    }

    /**
     * 交换顺序
     */
    @Test
    public void test14(){
        menuManageService.swapSort("APP_UF", 161686430613505L, 1
                , 161688007737345L, 2);
    }

    /**
     * 启用/停用
     */
    @Test
    public void test15(){
        menuManageService.updateResourceStatus("APP_UF", 161688137760769L
                , "N");
    }



    //--------------------------organization-----------------------

    /**
     * 新增组织
     */
    @Test
    public void test16(){
//        ValidList<ManageOrganization> orgs = new ValidList<>();
//        ManageOrganization m1 = new ManageOrganization();
//        m1.setSid(1L);
//        m1.setPid(0L);
//        m1.setName("第六野战军");
//        m1.setParentName("司令部");
//        m1.setIsAvailable("Y");
//        orgs.add(m1);
//        ManageOrganization m2 = new ManageOrganization();
//        m2.setSid(1L);
//        m2.setPid(0L);
//        m2.setName("第七野战军");
//        m2.setParentName("司令部");
//        m2.setIsAvailable("Y");
//        orgs.add(m2);
//        List<SysOrganization> organizations = SysOrganization.transform(orgs.getList());
//        organizationManageService.addInfo(organizations);
    }

    /**
     * 更新组织
     */
    @Test
    public void test17(){
//        ValidList<ManageOrganization> orgs = new ValidList<>();
//        ManageOrganization m1 = new ManageOrganization();
//        m1.setSid(162726836109313L);
//        m1.setCode("org1");
//        m1.setPid(0L);
//        m1.setName("第六野战军");
//        m1.setParentName("司令部");
//        m1.setIsAvailable("Y");
//        orgs.add(m1);
//        ManageOrganization m2 = new ManageOrganization();
//        m2.setSid(162728335638529L);
//        m2.setCode("org2");
//        m2.setPid(0L);
//        m2.setName("第七野战军集团军");
//        m2.setParentName("司令部");
//        m2.setIsAvailable("Y");
//        orgs.add(m2);
//        List<SysOrganization> organizations = SysOrganization.transform(orgs.getList());
//        organizationManageService.updInfo(organizations);
    }

    /**
     * 激活组织
     */
    @Test
    public void test18(){
//        ValidList<ManageOrganization> orgs = new ValidList<>();
//        ManageOrganization m1 = new ManageOrganization();
//        m1.setSid(162726836109313L);
//        m1.setCode("org1");
//        m1.setPid(0L);
//        m1.setName("第六野战军");
//        m1.setParentName("司令部");
//        m1.setIsAvailable("Y");
//        orgs.add(m1);
//        ManageOrganization m2 = new ManageOrganization();
//        m2.setSid(162728335638529L);
//        m2.setCode("org2");
//        m2.setPid(0L);
//        m2.setName("第七野战军集团军");
//        m2.setParentName("司令部");
//        m2.setIsAvailable("Y");
//        orgs.add(m2);
//        organizationManageService.updInfo(SysOrganization.transform(orgs.getList()));
    }

    /**
     * 获取组织
     */
    @Test
    public void test19(){
        SysOrganization app_uf = organizationManageService.findInfoBySid("APP_UF", 162726836109313L);
        System.out.println(app_uf.getName());
    }

    /**
     * 组织列表
     */
    @Test
    public void test20(){
        List<SysOrganization> app_uf = organizeQueryService.listByLevel("APP_UF", 1);
        for (SysOrganization sysOrganization : app_uf) {
            System.out.println(sysOrganization.getName());
        }
    }

    /**
     * 角色列表
     */
    @Test
    public void test21(){
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", "APP_UF");
        //params.put("name", StringUtils.join("%", "野战军", "%"));
        params.put("isAvailable", "Y");
        PageBean<SysOrganization> page = organizationManageService.page(new PageParam(1, 15)
                , params);
        List<SysOrganization> recordList = page.getRecordList();
        for (SysOrganization sysOrganization : recordList) {
            System.out.println(sysOrganization.getName());
        }
    }

    /**
     * 获取上级组织
     */
    @Test
    public void test22(){
        SysOrganization app_uf = organizeQueryService.getBasicOrganization("APP_UF", 5L);

    }

    @Test
    public void test(){
        List<SysOrganization> app_uf = organizationMapper.selectByAppType_Level("APP_UF", 1);
        System.out.println(app_uf.size());

    }


}
