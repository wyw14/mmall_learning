package mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.dao.ShippingMapper;
import mmall.pojo.Shipping;
import mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;
    public ServiceResponse add(Integer userId, Shipping shipping){
        if (userId==null||shipping==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount>0){
            Map reslut= Maps.newHashMap();
            reslut.put("shippingId",shipping.getId());
            return ServiceResponse.createBySuccess("新增地址成功",reslut);
        }else {
            return ServiceResponse.createByErrorMessage("新增地址失败请稍后再试");
        }
    }
    public ServiceResponse del(Integer userId, Integer shippingId){
        if (userId==null||shippingId==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        int rowCount = shippingMapper.deleteByUserIdShipingId(userId,shippingId);
        if (rowCount>0){
            return ServiceResponse.createSuccessByMessage("删除地址成功");
        }else {
            return ServiceResponse.createByErrorMessage("删除地址失败");
        }
    }

    public ServiceResponse update(Integer userId,Shipping shipping){
        shipping.setUserId(userId);
        if (userId==null||shipping==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        int rowCount = shippingMapper.updateByShipping(shipping);
        if (rowCount>0){
            return ServiceResponse.createSuccessByMessage("更新地址成功");
        }else {
            return ServiceResponse.createByErrorMessage("更新地址失败");
        }
    }
    public ServiceResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping == null){
            return ServiceResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServiceResponse.createBySuccess("更新地址成功",shipping);
    }


    public ServiceResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServiceResponse.createBySuccess(pageInfo);
    }


}
