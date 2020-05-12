package com.yto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

import java.io.Serializable;

/**
 * <pre>
 *  名称：GoodsHScode
 *  描述：浦东清关生成EDI文件时基础维护数据
 * </pre>
 *
 * @author Songyuancheng
 * @date 2019/7/9 9:51
 * @since v1.0.0
 */
@Data
public class GoodsHScode extends BaseRowModel implements Serializable {
    /**
     * HS编码
     */
    @ExcelProperty(index = 0)
    private String hsCode;

    /**
     * 商品名称
     */
    @ExcelProperty(index = 1)
    private String zhName;

    /**
     * 申报要素
     */
    private String declareFactor;

    /**
     * 第一法定计量单位
     */
    private String firstUnit;

    /**
     * 第二法定计量单位
     */
    private String secondUnit;
}
