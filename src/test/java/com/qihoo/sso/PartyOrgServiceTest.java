package com.bird.sso;

import com.alibaba.fastjson.JSON;
import com.bird.UserCenterApplication;
import com.bird.sso.api.enums.AppEnum;
import com.bird.sso.api.enums.UserTypeEnum;
import com.bird.sso.mapper.MenuMapper;
import com.bird.sso.service.RoleManageService;
import com.bird.sso.service.RoleQueryService;
import com.bird.sso.service.UserAuthorityService;
import com.bird.sso.service.UserManageService;
import com.bird.sso.service.rpc.UserAuthoritiesRPC;
import com.bird.sso.utils.rsa.v2.RSA;
import com.bird.sso.web.controller.manage.user.UserAssignBatchForm;
import com.bird.sso.web.controller.manage.user.UserAssignForm;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/18 15:16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {UserCenterApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)// 指定启动类
@Slf4j
public class PartyOrgServiceTest {

    @Autowired
    private RoleQueryService roleQueryService;

    @Autowired
    private UserManageService userManageService;


    @Autowired
    private UserAuthorityService userAuthorityService;


    @Autowired
    private RoleManageService roleManageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MenuMapper menuMapper;


    //@Autowired
    private UserAuthoritiesRPC authoritiesRPC;


//    @Autowired
//    private RSAHandler rsaHandler;



//h


//    @Test
//    public void child(){
//        List<SysRole> roles = roleQueryService.listRecursionChildRole("",101);
//        SysRole role = roleQueryService.findRole(1010);
//        ArrayList<SysRole> objects = Lists.newArrayList();
//        objects.add(role);
//
//        objects.removeAll(roles);
//
//        System.out.println(JSON.toJSONString(objects));
//    }


    @Test public void test12(){

        userManageService.userRegisterAndDefaultAuthorities(
                "APP_EP", "123456789", "NHqFPLLea+ehoIvgwTfp1GTYfVCN8pVr0NSGcotpy5a+a3xNnGAbbcZ5DPBM9AJPpewIP9mYGKy0Vr8zt9gKm7TBT7c8PzpE9tuHcne2cqsuzpG/i4I/7OUnmC4IOK7a9xwh4UJbKX7mBmrKH6BsdwkitEnK9U/SVIV9joRpkVU=", "USER", ""
        );
    }



    @Test public void test22(){
        List<String> roleCodes = Lists.newArrayList(
                "ROLE_ENT_MSB",
                "ROLE_GOV_IPS",
                "ROLE_GOV_HMG"
                ,"ROLE_GOV_KIL"
        );
//        for(String code : roleCodes){
//            userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                    , 105707428904961L, code);
//        }

    }





    @Test public void test29(){
        List<String> roleCodes = Lists.newArrayList(
                "ROLE_ENT_ADMIN"
                ,"ROLE_ENT_ORD"
                ,"ROLE_ENT_SER"
                ,"ROLE_ENT_SKT "
                ,"ROLE_ENT_CTB"
                ,"ROLE_ENT_CCS"
                ,"ROLE_ENT_EYB"
                ,"ROLE_ENT_DRC"
                ,"ROLE_ENT_MSB"
                ,"ROLE_GOV_IPS"
                ,"ROLE_GOV_SKT"
                ,"ROLE_GOV_BMR"
                ,"ROLE_GOV_SLD"
                ,"ROLE_GOV_BCF"
        );
//        for(String code : roleCodes){
//            userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                    , 8765908888889999L, roleCodes);
//        }

    }



    @Test public void test09(){
        List<String> roleCodes = Lists.newArrayList(
                "ROLE_ENT_DRC_WEB"
        );
//        for(String code : roleCodes){
//            userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                    , "15011119999", code);
//        }

        //105708534104065l
    }



//    @Test public void test13(){
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 3333333L, "ROLE_ENT_EYB_WEB");
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 4444444L, "ROLE_ENT_CCS_WEB");
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 896576458757896L, "ROLE_GOV_IPS_WEB");
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 55555555L, "ROLE_GOV_POF_WEB");
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 666666666L, "ROLE_GOV_CTB_WEB");
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 7777777L, "ROLE_GOV_SKT_WEB");
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 88888888L, "ROLE_GOV_COC_WEB");
//
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 99999999L, "ROLE_GOV_MSB_WEB");
//
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 12212233445333L, "ROLE_GOV_DRC_WEB");
//
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 65643533434542L, "ROLE_GOV_DCS_WEB");
//
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 676353254367773L, "ROLE_GOV_EYB_WEB");
//
//
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 3456664888888L, "ROLE_GOV_BCF_WEB");
//
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , 8675434775L, "ROLE_GOV_SLD_WEB");
//
//
//    }










    @Test public void test1() throws Exception {


        System.out.println("+++++++++++++++++++++++++++++"+ userManageService.checkOriginalPassword("h9jKcLbTK56faJCUQlaRbkPXwf5qkgitmXQZW0rWSir4QMrHLMLNVtjpV0AA6OjtCRlLf55E2tprc6OfLVzwN0XTgkFJ+NdXhoFN2qp0zxw7I5C+usaoJFtCffusYI2pke7aOYR7LuVf1FtW5qlBwODPNYKhIXxGseIkjS1Gac8="));

    }


    @Test public void test40(){
        System.out.println(passwordEncoder.encode("111111"));
    }


    /**
     * 用户授予角色
     */
//    @Test public void  t(){
//        //
//        userAuthorityService.assignAuthorities(AppEnum.APP_EP.name()
//                , ClientEnum.APP.name(),8765908888888888L, "ROLE_ENT_DRC_WEB");
//    }



//    @Reference(version = "1.0")
//    private IUserService rpc;
//    @Test public void te(){
//        rpc.syncMobile(com.bird.sso.enums.AppEnum.APP_PB.name(),103346474057729L,"13843823721");
//    }


    @Test
    public void tt(){
        System.out.println(passwordEncoder.encode("13903333548"));
    }


//    @Test public void g(){
//        rpc.activeSSOUser(AppEnum.APP_EP.name(),"13701299208");
//    }



    @Test public void t1() throws Exception {
//        String m = " a123456789";
//        //String p = encrypt("111111", getPublicKey(m));
//
//        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyvAlA62yI74vWqa64PNLdqGtUZWdww7VYYAiEtKTiePUEsXgKESOtjC4UsTRvZVkJkYg1FMq1yZ+urz8jhyy6t9OQf0EvmWSCyfMI6saA1enYxKwHonxmtOu9vAH8kSFMA0proUKQg/hfqLfnmSbqpejHS3Ewjk8vuSmRoeu75QIDAQAB";
//
//        String decrypt = RSAUtils.encrypt(m, RSAUtils.getPublicKey(pub));
//        //RSAUtils.
//        userManageService.userRegisterAndDefaultAuthorities(AppEnum.APP_GA.name()
//                , "17777777777", decrypt, "", "");


        String encode = passwordEncoder.encode("111111");
        System.out.println(encode);

    }



    @Test
    public void t() throws Exception {
        String m = "111111";
        //String p = encrypt("111111", getPublicKey(m));

        String encode = passwordEncoder.encode(m);
        System.out.println(encode);

        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyvAlA62yI74vWqa64PNLdqGtUZWdww7VYYAiEtKTiePUEsXgKESOtjC4UsTRvZVkJkYg1FMq1yZ+urz8jhyy6t9OQf0EvmWSCyfMI6saA1enYxKwHonxmtOu9vAH8kSFMA0proUKQg/hfqLfnmSbqpejHS3Ewjk8vuSmRoeu75QIDAQAB";

        //String decrypt = RSAUtils.encrypt(m, RSAUtils.getPublicKey(pub));
//        System.out.println(decrypt);
//        userManageService.userRegisterAndALLAuthorities(
//                AppEnum.APP_PB.name(), "18787887877"
//                , decrypt, null, null
//        );

    }



    @Test
    public void  t2() throws Exception {

        userManageService.userRegisterAndDefaultAuthorities(AppEnum.APP_EP.name()
                , "17701342222", RSA.encrypt("111111",RSA.getPublicKey(RSA.PUBLIC_KEY))
                , UserTypeEnum.ADMIN.name(), null);
    }


    @Test
    public void  t3() throws Exception {


        //String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyvAlA62yI74vWqa64PNLdqGtUZWdww7VYYAiEtKTiePUEsXgKESOtjC4UsTRvZVkJkYg1FMq1yZ+urz8jhyy6t9OQf0EvmWSCyfMI6saA1enYxKwHonxmtOu9vAH8kSFMA0proUKQg/hfqLfnmSbqpejHS3Ewjk8vuSmRoeu75QIDAQAB";
        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1gMcYd07Nz3cNQDgej/V6ZVbuoLplSir1RkmUccaQI7P7h18Vbz5P96txvmQf4t6J+hzoWv/om3zN1g5TZuVwLLOFxn+g7Opi33Rwqmj34SFKiefveQi+ry4BtjJFxRWo6V3csSfUY1K5EwoCTou3yoM21TgGX9hWBlt3xmUZRwIDAQAB";
            pub = RSA.PUBLIC_KEY;

        String encrypt = RSA.encrypt("111111",RSA.getPublicKey(pub));

        System.out.println(encrypt);
//        userManageService.userRegisterAndDefaultAuthorities(AppEnum.APP_PB.name()
//                , "17701342555", encrypt
//                , UserTypeEnum.ADMIN.name(), null);
    }

    @Test
    public void test01(){
        UserAssignBatchForm userAssignBatchForm = new UserAssignBatchForm();

        List<Long> userIds = new ArrayList<>();
        userIds.add(161139223166977L);
        userIds.add(161139743260673L);

        List<UserAssignForm> roles = new ArrayList<>();
        UserAssignForm  ua1 = new UserAssignForm();
        ua1.setAppType("APP_UF");
        ua1.setCode("ROLE_HOSPITAL_GOV_TEST3");
        ua1.setName("测试管理员333");
        ua1.setSid(135944457945089L);
        roles.add(ua1);
        UserAssignForm  ua2 = new UserAssignForm();
        ua2.setAppType("APP_UF");
        ua2.setCode("ROLE_HOSPITAL_GOV_TEST4");
        ua2.setName("测试管理员444");
        ua2.setSid(135944464236545L);
        roles.add(ua2);
        userAssignBatchForm.setUserIds(userIds);
        userAssignBatchForm.setRoles(roles);

//        Map<String,Object> b = new HashMap<>();
//        b.put("appType","APP_UF");
//        b.put("code","ROLE_HOSPITAL_GOV_TEST2");
//        b.put("name","测试管理员");
//        b.put("pid",0);
//        b.put("sid",135944449556481L);
        String s = JSON.toJSONString(userAssignBatchForm);
        System.out.println(s);
    }
}
