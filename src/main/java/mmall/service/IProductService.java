package mmall.service;

import com.github.pagehelper.PageInfo;
import mmall.commons.ServiceResponse;
import mmall.pojo.Product;
import mmall.vo.ProductDetailVo;

public interface IProductService {
    public ServiceResponse saveOrUpdate(Product product);

    public ServiceResponse<String> setStatus(Integer productId,Integer productStatus);

    public ServiceResponse<ProductDetailVo> manageProduct(Integer productId);

    public ServiceResponse<PageInfo> getProductList(int pageNum, int pageSize);

    public ServiceResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);


}
