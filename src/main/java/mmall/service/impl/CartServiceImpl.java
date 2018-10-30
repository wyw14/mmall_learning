package mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import mmall.commons.Const;
import mmall.commons.ResponseCode;
import mmall.commons.ServiceResponse;
import mmall.dao.CartMapper;
import mmall.dao.ProductMapper;
import mmall.pojo.Cart;
import mmall.pojo.Product;
import mmall.service.ICartService;
import mmall.util.BigDecimalUtils;
import mmall.util.PropertiesUtil;
import mmall.vo.CartProductVo;
import mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    public ServiceResponse<CartVo> add(Integer userId,Integer productid,Integer count){
        if (productid==null||count==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        Cart cart=cartMapper.selectCartByUserIdProductId(userId,productid);
        if (cart==null){
            //该产品不再购物车中,需要新增一个这个产品的记录
            Cart cartItem=new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.cart.CHECKED);
            cartItem.setProductId(productid);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else {
            //这个产品已经在购物车里面了
            //所以此时需要将产品数量相加;
            count=cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }

    public ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count){
        if (productId==null||count==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        Cart cart=cartMapper.selectCartByUserIdProductId(userId,productId);
        if (cart==null){
            return ServiceResponse.createByErrorMessage("未查找到任何购物车记录");
        }else{
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
            CartVo cartVo = this.getCartVoLimit(userId);
            return ServiceResponse.createBySuccess(cartVo);
        }
    }
    public ServiceResponse<CartVo> deleteProduct(Integer userId,String productIds){
        List<String>productList= Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL.getCode(),ResponseCode.ILLEGAL.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }

    @Override
    public ServiceResponse<CartVo> list(Integer userId) {
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }


    @Override
    public ServiceResponse<CartVo> selectOrUnselect(Integer userId,Integer checkStatus,Integer productId) {
        cartMapper.updateCheckOrUncheckAll(userId,checkStatus,productId);
        return this.list(userId);
    }

    @Override
    public ServiceResponse<Integer> countProductNum(Integer userId) {
        if (userId==null){
            return ServiceResponse.createBySuccess(0);
        }
        return ServiceResponse.createBySuccess(cartMapper.countProductNum(userId));
    }


    private CartVo getCartVoLimit(Integer userId){
       CartVo cartVo=new CartVo();
        List<Cart>cartList=cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList= Lists.newArrayList();

        BigDecimal cartTotalPrice =new BigDecimal("0");
        if (CollectionUtils.isNotEmpty((cartList))){
            for (Cart cartItem:cartList){
                CartProductVo cartProductVo=new CartProductVo();
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());
                cartProductVo.setId(cartItem.getId());
                Product product=productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product!=null){
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    int buyLimitcount=0;
                    if (product.getStock()>=cartItem.getQuantity()){
                        cartProductVo.setLimitQuantity(Const.cart.LIMIT_NUM_SUCCESS);
                        buyLimitcount=cartItem.getQuantity();
                    }else {
                        buyLimitcount=product.getStock();
                        cartProductVo.setLimitQuantity(Const.cart.LIMIT_NUM_FAIL);
                        Cart cart=new Cart();
                        cart.setQuantity(buyLimitcount);
                        cart.setId(cartItem.getId());
                        cartMapper.updateByPrimaryKeySelective(cart);
                    }
                    cartProductVo.setQuantity(buyLimitcount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtils.mul(cartItem.getQuantity().doubleValue(),product.getPrice().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if (cartItem.getChecked()==Const.cart.CHECKED){
                    cartTotalPrice=BigDecimalUtils.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllCheckStatus(userId));
        return cartVo;

    }
    private Boolean getAllCheckStatus(Integer userId){
        if (userId==null){
            return false;
        }
          return cartMapper.selectAllCheckStatus(userId)==0;
    }
}
