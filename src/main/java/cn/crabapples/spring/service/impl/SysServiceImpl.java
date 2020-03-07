package cn.crabapples.spring.service.impl;

import cn.crabapples.spring.common.config.ApplicationConfigure;
import cn.crabapples.spring.dao.SysMenuRepository;
import cn.crabapples.spring.dto.ResponseDTO;
import cn.crabapples.spring.entity.SysMenu;
import cn.crabapples.spring.entity.SysUser;
import cn.crabapples.spring.form.UserForm;
import cn.crabapples.spring.service.SysService;
import cn.crabapples.spring.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * TODO 系统相关服务实现类
 *
 * @author Mr.He
 * 2020/1/28 23:23
 * e-mail crabapples.cn@gmail.com
 * qq 294046317
 * pc-name 29404
 */
@Service
//@CacheConfig(cacheNames = "user:")
public class SysServiceImpl implements SysService {

    private static final Logger logger = LoggerFactory.getLogger(SysServiceImpl.class);
    private String aesKey;
    private String redisPrefix;
    private Long tokenCacheTime;
    private String salt;

    private final UserService userService;

    private final SysMenuRepository sysMenuRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    public SysServiceImpl(ApplicationConfigure applicationConfigure,
                          UserService userService,
                          SysMenuRepository sysMenuRepository,
                          RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.sysMenuRepository = sysMenuRepository;
        this.redisTemplate = redisTemplate;
        this.aesKey = applicationConfigure.AES_KEY;
        this.redisPrefix = applicationConfigure.REDIS_PREFIX;
        this.tokenCacheTime = applicationConfigure.TOKEN_CACHE_TIME;
        this.salt = applicationConfigure.SALT;
    }

    /**
     * Cacheable
     * * key: redis中key的值
     * * value: redis中key的前缀
     * * 例:
     * * key::value:tom
     * * userLogin::${#p0.username}
     * <p>
     * 用户登录验证
     *
     * @param form 用户信息
     * @return token
     */
//    @Cacheable(value = "login:token", key = "#p0.username")
    @Override
    public ResponseDTO login(UserForm form) {
        try {
            String username = form.getUsername();
            String password = new Md5Hash(form.getPassword(), salt).toString();
            logger.info("开始登录->用户名:[{}],密码:[{}]", username, password);
            SysUser user = shiroCheckLogin(username, password);
            if (user == null) {
                return ResponseDTO.returnError("用户名或密码错");
            }
        } catch (Exception e) {
            logger.warn("登录失败:[{}]", e.getMessage(), e);
            return ResponseDTO.returnError(e.getMessage());
        }
        return ResponseDTO.returnSuccess("登录成功");
    }

    /**
     * shiro认证
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证成功后的用户对象
     */
    private SysUser shiroCheckLogin(String username, String password) {
        try {
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            subject.login(token);
            return (SysUser) subject.getPrincipal();
        } catch (IncorrectCredentialsException e) {
            logger.warn("shiro认证失败,密码错误:[{}]", e.getMessage());
        } catch (UnknownAccountException e) {
            logger.warn("shiro认证失败,用户不存在:[{}]", e.getMessage());
        } catch (Exception e) {
            logger.error("shiro认证失败", e);
        }
        return null;
    }

    /**
     * 获取当前用户拥有的菜单
     *
     * @return 当前用户拥有的菜单
     */
    //    @Cacheable(value = "crabapples:sysMenus", key = "#auth")
    @Override
    public Set<? extends Object> getSysMenus() {
        SysUser user = (SysUser) SecurityUtils.getSubject().getPrincipal();
        logger.info("开始获取用户:[{}]已授权的菜单", user.getId());
        Set<SysMenu> userMenus = new HashSet<>();
        user.getSysRoles().forEach(e -> userMenus.addAll(e.getSysMenus()));
        logger.info("用户:[{}]已授权的菜单为:[{}]即将开始格式化菜单", user.getId(), userMenus);


        List<SysMenu> allMenus = sysMenuRepository.findAll();
        saveObj(userMenus,"8");
        saveObj(allMenus,"9");
//        insertChildrenMenus(sysMenus);
        return userMenus;
    }
    void saveObj(Object obj, String f) {
        File file = new File("d:/" + f);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object readObj(String f) {
        File file = new File("d:/" + f);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        Set<SysMenu> userMenus = (Set<SysMenu>) readObj("8");
        List<SysMenu> allMenus = (List<SysMenu>) readObj("9");
        userMenus.forEach(e->{
            allMenus.forEach(r->{
                if(e.getId().equals(r.getId())){
                    System.err.println(e);

                }
            });
        });
//        System.err.println(userMenus);
//        System.err.println(allMenus);
    }


}
