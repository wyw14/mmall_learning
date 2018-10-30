package mmall.service;

import mmall.commons.ServiceResponse;
import mmall.vo.CartVo;

public interface ICartService {
    ServiceResponse<CartVo> add(Integer userId, Integer productid, Integer count);

    ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    public ServiceResponse<CartVo> deleteProduct(Integer userId,String productIds);

    ServiceResponse<CartVo> list(Integer id);

    public ServiceResponse<CartVo> selectOrUnselect(Integer userId,Integer checkStatus,Integer productId);

    ServiceResponse<Integer> countProductNum(Integer id);
}
