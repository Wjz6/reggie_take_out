package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")  //登录请求路径含有employee，处理请求地址映射的注解
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    //登陆成功，存储employee的id存到session info，想获取当前用户可以request.get
    //@RequestBody 用来接收前端传给后端的json字符串中的数据，POST方式
    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper); //查询数据库唯一索引

        //3.如果没有查询到则返回登陆失败结果
        if(emp==null)
            return R.error("登陆失败"); //R对象转json

        //4.密码比对，如果不一致则返回登陆失败结果
        if(!emp.getPassword().equals(password))
            return R.error("登陆失败");

        //5.查看员工状态（status），如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0)
            return R.error("账号已禁用");

        //6.登陆成功，将员工id存入Session并返回登陆成功结果
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")  //将HTTP post请求映射到特定处理程序的方法注解
    public R<String> logout(HttpServletRequest request){
        //清楚Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping  //用于处理客户端发过来的POST请求（/employee），并将响应结果直接返回客户端
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息:{}",employee.toString());
        //设置初始密码为123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //不用自己设置id，直接由mybatisplus的功能用雪花算法自动生成新的id

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //获得当前登录用户的id
        //Long empId = (Long) request.getSession().getAttribute("employee"); //Object转Long

        //employee.setCreateUser(empId);   //当前登录用户
        //employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")  //处理请求方法的GET类型
    public R<Page> page(int page,int pageSize, String name){  //页面要records和total，在page里（list.html）
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件  当name不等于空时才会添加
        queryWrapper.like(StringUtils.hasText(name),Employee::getName,name);   //对接sql上的
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息(启用、禁用)
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为:{}",id);
        //Long empId = (Long)request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);
        employeeService.updateById(employee);  //mybatis plus自带的修改函数
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
