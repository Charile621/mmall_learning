package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICategoryService categoryService;

    @RequestMapping(value = "add_category",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId",defaultValue = "0")Integer parentId)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //校验是否是管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess())
        {
            //是管理员，进行添加分类的逻辑
            return categoryService.addCategory(categoryName,parentId);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }

    @RequestMapping(value = "update_category_name",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session,Integer categoryId,String categoryName)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //校验是否是管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess())
        {
            //是管理员，更新categoryName
            return categoryService.updateCategoryName(categoryId,categoryName);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }
    @RequestMapping(value = "get_category",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //校验是否是管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess())
        {
            //是管理员，查询子节点的category信息，并且不递归，保持平级
            return categoryService.getChildrenParallelCategory(categoryId);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }
    @RequestMapping(value = "get_deep_category",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        //校验是否是管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess())
        {
            //是管理员，查询子节点的id和递归子节点的id
            return categoryService.selectCategoryAndChildrenById(categoryId);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }
}
