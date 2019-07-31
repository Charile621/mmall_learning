package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session)
    {
        //service->mybatis-dao
        ServerResponse<User> response = userService.login(username,password);
        if(response.isSuccess())
        {
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 用户登出，从session中清空user
     * @param session
     * @return
     */
    @RequestMapping(value = "logout",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySucess();
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    @RequestMapping(value = "register",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user)
    {
        return userService.register(user);
    }

    /**
     * 校验是否合法，type（需要校验的类型）：username，email，str:需要校验的内容
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type)
    {
        return userService.checkValid(str,type);
    }

    /**
     * 从session中获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return ServerResponse.createBySucess(user);
    }

    /**
     * 忘记密码时需要返回找回密码的问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username)
    {
        return userService.selectQuestion(username);
    }

    /**
     * 忘记密码时校验找回密码问题的答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer)
    {
        return userService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码时找回密码问题答案正确后重置密码
     * @param username
     * @param passwordNew
     * @param token
     * @return
     */
    @RequestMapping(value = "forget_reset_password",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String token)
    {
        return userService.forgetResetPassword(username,passwordNew,token);
    }

    /**
     * 登录状态下重置密码
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError("用户未登录");
        }
        return userService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 登录状态下更新用户的信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_info",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session,User user)
    {
        User curUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(curUser == null)
        {
            return ServerResponse.createByError("用户未登录");
        }
        //userId在校验时需要，所以从session中的user对象中获取赋值给请求中传递过来的新的user对象
        user.setId(curUser.getId());
        ServerResponse<User> response = userService.updateInformation(user);
        if(response.isSuccess())
        {
            //返回的user对象中没有username，而session中的user对象应该存有该信息
            response.getData().setUsername(curUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 从数据库中获取用户详细信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_info",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError("用户未登录，请登录后再进行操作");
        }
        return userService.getInformation(user.getId());
    }
}
