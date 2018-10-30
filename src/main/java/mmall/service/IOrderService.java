package mmall.service;

import com.github.pagehelper.PageInfo;
import mmall.commons.ServiceResponse;
import mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {
    public ServiceResponse pay(Integer userId, Long orderNo, String path);
    public ServiceResponse aliCallback(Map<String,String> params);
    public ServiceResponse queryOrderPayStatus(Integer userId,Long orderNo);
    public ServiceResponse create(Integer userId, Integer shippingId);

    public ServiceResponse cancel(Integer userId, Long orderId);

    public ServiceResponse getOrderCartProduct(Integer userId);

    public ServiceResponse<OrderVo> getOrderDetail(Integer userId, Long orderId);

    public ServiceResponse<PageInfo> orderList(Integer userId, Integer pageNum, Integer pageSize);

    public ServiceResponse<PageInfo> manageList(Integer pageNum,Integer pageSize);

    public ServiceResponse<OrderVo> manageGetOrderDetail(Long orderId);

    public ServiceResponse<PageInfo> manageSearch(Long orderId,int pageNum,int pageSize);

    public ServiceResponse<String> manageSendGoods(Long orderId);
}
