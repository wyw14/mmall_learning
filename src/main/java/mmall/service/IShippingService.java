package mmall.service;

import com.github.pagehelper.PageInfo;
import mmall.commons.ServiceResponse;
import mmall.pojo.Shipping;


public interface IShippingService {
    public ServiceResponse add(Integer userId, Shipping shipping);

    ServiceResponse del(Integer userId, Integer shippingId);

    public ServiceResponse update(Integer userId,Shipping shipping);

    public ServiceResponse<Shipping> select(Integer userId, Integer shippingId);

    public ServiceResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
