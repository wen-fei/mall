package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by geely
 */

@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;



    @RequestMapping(value = "/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detailRESTful(@PathVariable Integer productId){
        return iProductService.getProductDetail(productId);
    }

    @RequestMapping(value = "/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> list(@PathVariable(value = "keyword") String keyword,
                                         @PathVariable(value = "categoryId")Integer categoryId,
                                         @PathVariable(value = "pageNum") Integer pageNum,
                                         @PathVariable(value = "pageSize") Integer pageSize,
                                         @PathVariable(value = "orderBy") String orderBy){
        if (pageNum == null) {
            pageNum = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }

        return iProductService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }





}
