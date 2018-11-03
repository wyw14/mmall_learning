package mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import mmall.commons.Const;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.pojo.User;
import mmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
@Slf4j
public class OrderController {
    //private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;
    @RequestMapping("create.do")
    @ResponseBody
    public ServiceResponse create(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.create(user.getId(),shippingId);
    }

    @RequestMapping("cancel.do")
    @ResponseBody
    public ServiceResponse cancel(HttpSession session,Long orderId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderId);
    }

    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServiceResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping("get_order_detail.do")
    @ResponseBody
    public ServiceResponse getOrderDetail(HttpSession session,Long orderId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse list(HttpSession session, @RequestParam(value ="pageNum",defaultValue = "1") Integer pageNum,@RequestParam(value ="pageSize",defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.orderList(user.getId(),pageNum,pageSize);
    }












    @RequestMapping("pay.do")
    @ResponseBody
    public ServiceResponse pay(HttpSession session, Long orderNum, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        String path=request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(),orderNum,path);
    }
    @ResponseBody
    @RequestMapping("alipay_callback.do")
    public Object callBack(HttpServletRequest request){
        Map<String,String> map= Maps.newHashMap();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Iterator iter=parameterMap.keySet().iterator();iter.hasNext();){
            String name= (String) iter.next();
            String[] values= parameterMap.get(name);
            String valueStr="";
            for (int i=0;i<values.length;i++){
                valueStr=(i==values.length-1)?valueStr+values[i]: valueStr+values[i]+",";
            }
            map.put(name,valueStr);
        }
        log.info("支付宝回调,sign:{},trade_status:{},参数:{}",map.get("sign"),map.get("trade_status"),map.toString());
        map.remove("sign_type");
        try {
            Boolean alipayRSAChecked = AlipaySignature.rsaCheckV2(map, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if (!alipayRSAChecked){
                log.info("有内奸终止交易");
                return ServiceResponse.createByErrorMessage("有内奸终止交易");
            }
        } catch (AlipayApiException e) {
            log.error("回调请求出错",e);
        }
        ServiceResponse serviceResponse=iOrderService.aliCallback(map);
        if (serviceResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_Failed;
    }

    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServiceResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNum, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServiceResponse serviceResponse= iOrderService.queryOrderPayStatus(user.getId(),orderNum);
        if (serviceResponse.isSuccess()){
            return ServiceResponse.createBySuccess(true);
        }
        return ServiceResponse.createBySuccess(false);
    }








}
