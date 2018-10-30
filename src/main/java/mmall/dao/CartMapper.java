package mmall.dao;

import mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectAllCheckStatus(Integer userId);

    int deleteByUserIdProductIds(@Param("userId") Integer userId,@Param("productIds") List<String> productIds);

    int updateCheckOrUncheckAll(@Param("userId") Integer userId,@Param("checkStatus")Integer checkStatus,@Param(value = "product_id") Integer productId);

    int countProductNum(@Param("userId") Integer userId);

    List<Cart> selectByUserId(Integer userId);
}
















