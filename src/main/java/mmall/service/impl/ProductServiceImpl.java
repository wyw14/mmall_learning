package mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.dao.CategoryMapper;
import mmall.dao.ProductMapper;
import mmall.pojo.Category;
import mmall.pojo.Product;
import mmall.service.IProductService;
import mmall.util.DateTimeUtil;
import mmall.util.PropertiesUtil;
import mmall.vo.ProductDetailVo;
import mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    public ServiceResponse saveOrUpdate(Product product){


        if (product!=null){
            if(StringUtils.isNotBlank(product.getSubImages())){
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId()!=null){
                int resultCount = productMapper.updateByPrimaryKeySelective(product);
                if (resultCount>0){
                    return ServiceResponse.createSuccessByMessage("更新商品成功");
                }else {
                    return ServiceResponse.createByErrorMessage("更新商品失败");
                }
            }else {
                int resultCount=productMapper.insertSelective(product);
                if (resultCount>0){
                    return ServiceResponse.createSuccessByMessage("添加商品成功");
                }else {
                    return ServiceResponse.createByErrorMessage("添加商品失败");
                }
            }
        }else {
            return ServiceResponse.createByErrorMessage("商品参数错误,添加失败");
        }
    }
    public ServiceResponse<String> setStatus(Integer productId,Integer productStatus){
        if (productId==null||productStatus==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        Product product=new Product();
        product.setId(productId);
        product.setStatus(productStatus);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount>0){
            return ServiceResponse.createSuccessByMessage("修改销售状态成功");
        }
        return ServiceResponse.createByErrorMessage("修改销售状态失败");
    }


    public ServiceResponse<ProductDetailVo> manageProduct(Integer productId){
        if (productId==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        Product product=productMapper.selectByPrimaryKey(productId);
        if (product==null){
            return ServiceResponse.createByErrorMessage("产品已下架或者删除");
        }
        //vo对象value object
        ProductDetailVo productDetailVo=assemleProductDetailVo(product);
        return ServiceResponse.createBySuccess(productDetailVo);


    }

    private ProductDetailVo assemleProductDetailVo(Product product){
        ProductDetailVo productDetailVo=new ProductDetailVo();
        product.setId(productDetailVo.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        Category category=categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category==null){
            productDetailVo.setParentCategory(0);//默认为根节点
        }else {
            productDetailVo.setParentCategory(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    public ServiceResponse<PageInfo> getProductList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> ProductList=new ArrayList<>();
        for (Product product:productList){
            ProductListVo productListVo=assemleProductListVo(product);
            ProductList.add(productListVo);
        }
        PageInfo pageResult=new PageInfo(productList);
        pageResult.setList(productList);
        return ServiceResponse.createBySuccess(pageResult);
    }

    private ProductListVo assemleProductListVo(Product product){
        ProductListVo productListVo=new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setName(product.getName());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        return productListVo;
    }

    public ServiceResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectSearchProduct(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList){
            ProductListVo productListVo = assemleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServiceResponse.createBySuccess(pageResult);
    }

}
