package mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import mmall.commons.Const;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.dao.*;
import mmall.pojo.*;
import mmall.service.IOrderService;
import mmall.util.BigDecimalUtils;
import mmall.util.DateTimeUtil;
import mmall.util.FTPUtil;
import mmall.util.PropertiesUtil;
import mmall.vo.OrderItemVo;
import mmall.vo.OrderProductVo;
import mmall.vo.OrderVo;
import mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
@Slf4j
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;
   // private static final Logger log= LoggerFactory.getLogger(OrderServiceImpl.class);

    public ServiceResponse create(Integer userId, Integer shippingId){
        List<Cart> cartList=cartMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(cartList)){
            return ServiceResponse.createByErrorMessage("购物车为空");
        }
        Long orderNo=this.generateOrderId();
        ServiceResponse<List<OrderItem>> serviceResponse = this.getCartOrderItemlist(userId, cartList);
        if (!serviceResponse.isSuccess()){
            return serviceResponse;
        }
        List<OrderItem> orderItemList=serviceResponse.getData();
        BigDecimal payment=this.totalPrice(orderItemList);
        Order order=this.assembleOrder(userId,shippingId,payment);
        if (order==null){
            return ServiceResponse.createSuccessByMessage("生成订单失败 ");
        }
        if (CollectionUtils.isEmpty(orderItemList)){
            return ServiceResponse.createByErrorMessage("购物车为空");
        }
        for(OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        orderItemMapper.batchInsert(orderItemList);
        //减少库存
        this.reduce(orderItemList);
        //清空一下购物车
        this.cleanCart(cartList);
        //返回前端数据
        OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
        return ServiceResponse.createBySuccess(orderVo);
    }

    public ServiceResponse cancel(Integer userId, Long orderId){
        Order order=orderMapper.selectByuserIdorderId(userId,orderId);
        if (order==null){
            return ServiceResponse.createByErrorMessage("该用户没有该订单");
        }
        if (order.getStatus()==Const.OrderStatusEnum.PAID.getCode()){
            return  ServiceResponse.createByErrorMessage("订单已经付款,无法取消");
        }
        Order updateOrder=new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        orderMapper.updateByPrimaryKeySelective(updateOrder);
        return ServiceResponse.createSuccessByMessage("订单取消成功");
    }
    public ServiceResponse getOrderCartProduct(Integer userId){

        OrderProductVo orderProductVo=new OrderProductVo();
        List<Cart> cartList=cartMapper.selectByUserId(userId);
        ServiceResponse<List<OrderItem>> serviceResponse = this.getCartOrderItemlist(userId, cartList);
        List<OrderItem> orderItemList = serviceResponse.getData();
        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        BigDecimal total=new BigDecimal("0");
        for (OrderItem orderItem:orderItemList){
            total=BigDecimalUtils.add(orderItem.getTotalPrice().doubleValue(),total.doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setProductTotalPrice(total);
        return ServiceResponse.createBySuccess(orderProductVo);
    }

    public ServiceResponse<OrderVo> getOrderDetail(Integer userId,Long orderId){
        Order order=orderMapper.selectByuserIdorderId(userId,orderId);
        if (order!=null){
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId,orderId);
            OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
            orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            return ServiceResponse.createBySuccess(orderVo);
        }
        return ServiceResponse.createByErrorMessage("没有找到该订单");
    }

    public ServiceResponse<PageInfo> orderList(Integer userId,Integer pageNum,Integer pageSize){
        List<Order> orderList = orderMapper.selectByuserId(userId);
        PageHelper.startPage(pageNum,pageSize);
        List<OrderVo> orderVoList = this.assembleOrderVolist(orderList, userId);
        PageInfo pageResult=new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageResult);
    }







    //backend
    public ServiceResponse<PageInfo> manageList(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orders = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList=this.assembleOrderVolist(orders,null);
        PageInfo pageReslut=new PageInfo(orderVoList);
        pageReslut.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageReslut);
    }
    public ServiceResponse<OrderVo> manageGetOrderDetail(Long orderId){
        Order order=orderMapper.selectByOrderNo(orderId);
        if (order!=null){
            List<OrderItem> orderItemList = orderItemMapper.selectAll(orderId);
            OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
            return  ServiceResponse.createBySuccess(orderVo);
        }
        return ServiceResponse.createByErrorMessage("没有找到该订单");
    }
    public ServiceResponse<PageInfo> manageSearch(Long orderId,int pageNum,int pageSize){
        Order order=orderMapper.selectByOrderNo(orderId);
        PageHelper.startPage(pageNum,pageSize);
        if (order!=null){
            List<OrderItem> orderItemList = orderItemMapper.selectAll(orderId);
            OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
            PageInfo resultPage=new PageInfo(Lists.newArrayList(order));
            resultPage.setList(Lists.newArrayList(orderVo));
            return  ServiceResponse.createBySuccess(resultPage);
        }
        return ServiceResponse.createByErrorMessage("没有找到该订单");
    }
    public ServiceResponse<String> manageSendGoods(Long orderId){
        Order order=orderMapper.selectByOrderNo(orderId);
        if (order!=null){
            if (Const.OrderStatusEnum.PAID.getCode()==order.getStatus()){
                order.setStatus(Const.OrderStatusEnum.SHIPPINGEN.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServiceResponse.createSuccessByMessage("发货成功");
            }
        }
        return ServiceResponse.createByErrorMessage("没有找到该订单");
    }













    private List<OrderVo> assembleOrderVolist(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList=Lists.newArrayList();
        for (Order order:orderList){
            List<OrderItem> orderItemList=Lists.newArrayList();
            if (userId==null){
                orderItemList=orderItemMapper.selectAll(order.getOrderNo());
            }else {
                orderItemList=orderItemMapper.selectByUserIdOrderNo(userId,order.getOrderNo());
            }
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo=new OrderVo();
        orderVo.setPayment(order.getPayment());
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPostage(order.getPostage());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.paymentEnun.codeOf(order.getPaymentType()).getValue());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping=shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVos=Lists.newArrayList();
        for (OrderItem orderItem:orderItemList){
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVos.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVos);
        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo=new OrderItemVo();
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        return orderItemVo;
    }
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo=new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverName(shipping.getReceiverName());
        return shippingVo;
    }
    private void cleanCart(List<Cart> cartList){
        for (Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduce(List<OrderItem> orderItemList){
        for (OrderItem orderItem:orderItemList){
            Product product=productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order=new Order();
        order.setShippingId(shippingId);
        order.setPostage(0);
        order.setUserId(userId);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPayment(payment);
        order.setPaymentType(Const.paymentEnun.online_payment.getCode());
        order.setOrderNo(this.generateOrderId());

        int rowCount = orderMapper.insert(order);
        if (rowCount>0){
            return order;
        }
        return null;
    }
    private Long generateOrderId(){
        Long orderId= System.currentTimeMillis()+new Random().nextInt(1000);
        return orderId;
    }


    private BigDecimal totalPrice(List<OrderItem> orderItemList){
        BigDecimal totalPrice=new BigDecimal("0");
        for (OrderItem  orderItem: orderItemList){
            totalPrice=BigDecimalUtils.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return totalPrice;
    }


    private ServiceResponse<List<OrderItem>> getCartOrderItemlist(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList=Lists.newArrayList();
        for (Cart cartItem :cartList){
            Product product=productMapper.selectByPrimaryKey(cartItem.getProductId());
            OrderItem orderItem=new OrderItem();
            if (Const.sellStatus.UNSELL.equals(product.getStatus())){
                return ServiceResponse.createByErrorMessage("商品"+product.getName()+"已经下架购买失败下次请早点下手哦");
            }
            if (cartItem.getQuantity()>product.getStock()){
                return ServiceResponse.createByErrorMessage("您购买的"+product.getName()+"商品数量已经超过库存量了呢,最大可购买量为"+product.getStock());
            }
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setProductId(product.getId());
            orderItem.setUserId(userId);
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtils.mul(product.getPrice().doubleValue(),cartItem.getQuantity().doubleValue()));
            orderItem.setQuantity(cartItem.getQuantity());
            orderItemList.add(orderItem);
        }
        return ServiceResponse.createBySuccess(orderItemList);
    }


    public ServiceResponse pay(Integer userId, Long orderNo,String path){

        if (userId==null||orderNo==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        Map<String,String> map= Maps.newHashMap();
        Order order=orderMapper.selectByuserIdorderId(userId,orderNo);
        if (order==null){
            return ServiceResponse.createByErrorMessage("用户没有该订单");
        }
        map.put("orderNo",order.getOrderNo().toString());

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = new StringBuilder().append("happymmall商城订单号:").append(order.getOrderNo()).append(".").toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "用户自己的支付宝付款码"; // 条码示例，286648048691290423
        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("订单").append(order.getOrderNo()).append("共需支付").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId,orderNo);

        for (OrderItem orderItem: orderItemList){
            GoodsDetail goods1=GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),BigDecimalUtils.mul(new Double(100).doubleValue(),orderItem.getCurrentUnitPrice().doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods1);
        }
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        // 创建好一个商品后添加至商品明细列表

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件

        String appAuthToken = "应用授权令牌";//根据真实值填写

        // 创建条码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 需要修改为运行机器上的路径
                File folder =new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
                String qrFileName=String.format("qr-%s.png",response.getOutTradeNo());

                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                File targetFile =new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常",e);
                }
                log.info("qrPath:" + qrPath);
                String qrUrl=PropertiesUtil.getProperty("ftp.server.http.prefix")+qrFileName;
                map.put("qrUrl",qrUrl);
                return ServiceResponse.createBySuccess(map);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServiceResponse.createByErrorMessage("支付宝预下单失败!!!");
            case UNKNOWN:
            log.error("系统异常，预下单状态未知!!!");
                return ServiceResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
            log.error("不支持的交易状态，交易返回异常!!!");
                return ServiceResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }
    public ServiceResponse aliCallback(Map<String,String> params){
        Long orderNo =Long.parseLong(params.get("out_trade_no"));
        String tradeNo=params.get("trade_no");
        String tradeStatus=params.get("trade_status");
        Order order=orderMapper.selectByOrderNo(orderNo);
        if (order==null){
            return ServiceResponse.createByErrorMessage("非本商城的订单,回调忽略");
        }
        if (order.getStatus()>= Const.OrderStatusEnum.PAID.getCode()){
            return ServiceResponse.createSuccessByMessage("支付宝重复调用");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo=new PayInfo();
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setUserId(order.getUserId());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServiceResponse.createBySuccess();
    }

    public ServiceResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order=orderMapper.selectByuserIdorderId(userId,orderNo);
        if (order==null){
            return ServiceResponse.createByErrorMessage("用户没有该订单");
        }
        if (order.getStatus()>= Const.OrderStatusEnum.PAID.getCode()){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }


}