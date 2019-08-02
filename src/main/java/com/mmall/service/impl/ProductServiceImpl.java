package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVO;
import com.mmall.vo.ProductListVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        if(product == null)
        {
            return ServerResponse.createByError("新增或更新产品参数不正确");
        }
        if(StringUtils.isNotBlank(product.getSubImages()))
        {
            String[] subImageArray = product.getSubImages().split(",");
            if(subImageArray.length>0)
            {
                product.setMainImage(subImageArray[0]);
            }
        }
        if(product.getId() != null)
        {
            int resultCount = productMapper.updateByPrimaryKeySelective(product);
            if(resultCount <= 0)
            {
                return ServerResponse.createByError("更新产品失败");
            }
            return ServerResponse.createBySucess("更新产品成功");
        }
        else
        {
            int resultCount = productMapper.insert(product);
            if(resultCount <= 0)
            {
                return ServerResponse.createByError("新增产品失败");
            }
            return ServerResponse.createBySucess("新增产品成功");
        }
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if(productId == null || status == null)
        {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int resultRow = productMapper.updateByPrimaryKeySelective(product);
        if(resultRow <= 0)
        {
            return ServerResponse.createByError("修改产品销售状态失败");
        }
        return ServerResponse.createBySucess("修改产品销售状态成功");
    }

    @Override
    public ServerResponse<ProductDetailVO> manageProductDetail(Integer productId) {
        if(productId == null)
        {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null)
        {
            return ServerResponse.createByError("产品已下架或删除");
        }
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySucess(productDetailVO);
    }

    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVO> productListVOList = productList.stream().map(listItem->{
            ProductListVO productListVO = assembleProductListVO(listItem);
            return productListVO;
        }).collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(productListVOList);
        return ServerResponse.createBySucess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName))
        {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();

        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVO> productListVOList = productList.stream().map(listItem->{
            ProductListVO productListVO = assembleProductListVO(listItem);
            return productListVO;
        }).collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(productListVOList);
        return ServerResponse.createBySucess(pageInfo);
    }

    private ProductDetailVO assembleProductDetailVO(Product product)
    {
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(product,productDetailVO);

        productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null)
        {
            productDetailVO.setParentCategoryId(0);
        }
        else
        {
            productDetailVO.setParentCategoryId(category.getParentId());
        }

        productDetailVO.setCreateTime(DateTimeUtil.dateToString(product.getCreateTime()));
        productDetailVO.setUpdateTime(DateTimeUtil.dateToString(product.getUpdateTime()));
        return productDetailVO;
    }

    private ProductListVO assembleProductListVO(Product product)
    {
        ProductListVO productListVO = new ProductListVO();
        BeanUtils.copyProperties(product,productListVO);
        productListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        return productListVO;
    }
}
