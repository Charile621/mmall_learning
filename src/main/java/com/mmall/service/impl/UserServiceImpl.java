package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录校验需要传入username和password
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0)
        {
            return ServerResponse.createByError("用户名不存在");
        }

        //密码登录MD5加密后和数据库中的比对
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null)
        {
            return ServerResponse.createByError("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySucess("登录成功",user);
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user)
    {
        //校验username和Email是否未被使用
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess())
        {
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess())
        {
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //密码要MD5加密后入库
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if(resultCount == 0)
        {
            return ServerResponse.createByError("注册失败");
        }
        return ServerResponse.createBySucess("注册成功");
    }

    /**
     * 校验Email或username是否未被使用
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNotBlank(type))
        {
            //开始校验
            if(Const.USERNAME.equals(type))
            {
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0)
                {
                    return ServerResponse.createByError("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type))
            {
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0)
                {
                    return ServerResponse.createByError("邮箱已存在");
                }
            }
        }
        else
        {
            return ServerResponse.createByError("参数错误");
        }
        return ServerResponse.createBySucess("校验成功");
    }

    /**
     * 查询找回密码的问题
     * @param username
     * @return
     */
    @Override
    public ServerResponse selectQuestion(String username) {
        ServerResponse<String> response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess())
        {
            return ServerResponse.createByError("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(!StringUtils.isNotBlank(question))
        {
            return ServerResponse.createByError("找回密码的问题是空的");
        }
        return ServerResponse.createBySucess(question);
    }

    /**
     * 校验找回密码问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount<=0)
        {
            return ServerResponse.createByError("问题的答案不正确");
        }
        //需要返回一个token作为重置密码的标志
        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKey("token_"+username,forgetToken);
        return ServerResponse.createBySucess(forgetToken);
    }

    /**
     * 找回密码问题答案正确后重置密码
     * @param username
     * @param passwordNew
     * @param token
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String token) {
        //校验token是否正确或有效
        if(StringUtils.isBlank(token))
        {
            return ServerResponse.createByError("参数错误，token需要传递");
        }
        ServerResponse<String> response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess())
        {
            return ServerResponse.createByError("用户不存在");
        }
        String localToken = TokenCache.getKey("token_"+username);
        if(StringUtils.isBlank(localToken))
        {
            return ServerResponse.createByError("token无效或者过期");
        }
        if(StringUtils.equals(token,localToken))
        {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount<=0)
            {
                return ServerResponse.createByError("修改密码失败");
            }
        }
        else
        {
            return ServerResponse.createByError("token错误，请重新获取");
        }
        return ServerResponse.createBySucess("修改密码成功");
    }

    /**
     * 登录状态下更新密码，为了防止横向越权需要指定需要修改密码的用户
     * 从controller中通过session中获取传递过来
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {

        int resultCount = userMapper.checkPassword(user.getId(),MD5Util.MD5EncodeUtf8(passwordOld));
        if(resultCount<=0)
        {
            return ServerResponse.createByError("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount<=0)
        {
            return ServerResponse.createByError("密码更新失败");
        }
        return ServerResponse.createBySucess("密码更新成功");
    }

    /**
     * 登录状态下时更新用户信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateInformation(User user) {
        //username不能被更新,所以传入的user对象中username为空
        //email也要校验，校验新的email是否存在，存在的话，不能是当前用户的，也就是当前用户更新后的邮箱不能是使用过的
        int resultCount = userMapper.checkEmailByUserId(user.getId(),user.getEmail());
        if(resultCount>0)
        {
            return ServerResponse.createByError("Email已经存在请更换后再尝试");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        //username等信息不更新，使用Selective方法保证
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount<=0)
        {
            return ServerResponse.createByError("更新个人信息失败");
        }
        return ServerResponse.createBySucess("更新个人信息成功",updateUser);
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null)
        {
            return ServerResponse.createByError("找不到用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySucess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user!=null&&user.getRole().intValue()==Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createBySucess();
        }
        return ServerResponse.createByError();
    }
}
