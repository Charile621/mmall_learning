package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;

    @RequestMapping(value = "/product_save",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product)
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
            //是管理员，进行添加商品的逻辑
            return productService.saveOrUpdateProduct(product);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }

    @RequestMapping(value = "/set_sale_status",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status)
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
            //是管理员，进行修改商品状态的逻辑
            return productService.setSaleStatus(productId,status);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }

    @RequestMapping(value = "/get_detail",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId)
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
            //是管理员，进行返回商品详情的逻辑
            return productService.manageProductDetail(productId);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }

    @RequestMapping(value = "/get_list",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize)
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
            //是管理员，进行返回商品详情的逻辑
            return productService.getProductList(pageNum,pageSize);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }

    @RequestMapping(value = "/product_search",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName,Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize)
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
            //是管理员，进行返回商品详情的逻辑
            return productService.searchProduct(productName,productId,pageNum,pageSize);
        }
        else
        {
            return ServerResponse.createByError("无权限操作");
        }
    }
}
