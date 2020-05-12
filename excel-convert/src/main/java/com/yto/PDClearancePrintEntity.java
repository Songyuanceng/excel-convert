package com.yto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 *  名称：PDClearancePrintEntity
 *  描述：清关打印实体
 * </pre>
 *
 * @author Songyuancheng
 * @date 2019/6/14 10:55
 * @since v1.0.0
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PDClearancePrintEntity extends BaseRowModel implements Serializable {
    /**
     * 总运单号
     */
    @ExcelColumn(value = "总运单号", col = 2)
    private String billNo;

    /**
     * 进港日期
     */
    @ExcelColumn(value = "进港日期", col = 11)
    private String entryDate;

    /**
     * 抵运地
     */
    @ExcelColumn(value = "抵运地", col = 12)
    private String arrival;

    /**
     * 圆通面单号
     */
    @ExcelProperty(index = 1)
    private String ytoMailNo;

    /**
     * 商品名称
     */
    @ExcelProperty(index = 2)
    private String goodsName;

    /**
     * HS编码
     */
    @ExcelProperty(index = 3)
    private String hsCode;

    /**
     * 包裹件数
     */
    @ExcelProperty(index = 4)
    private String packNum;

    /**
     * 件数
     */
    @ExcelProperty(index = 5)
    private String goodsNum;

    /**
     * 重量
     */
    @ExcelProperty(index = 6)
    private String weight;

    /**
     * 价值
     */
    @ExcelProperty(index = 7)
    private String worth;

    /**
     * 币种
     */
    @ExcelProperty(index = 8)
    private String currCode;

    /**
     * 收件人公司名称
     */
    @ExcelProperty(index = 9)
    private String receiveCompanyName;

    /**
     * 外国城市
     */
    @ExcelProperty(index = 10)
    private String foreignCity;

    /**
     * 发件人公司名称
     */
    @ExcelProperty(index = 11)
    private String sendCompanyName;

    /**
     * 报关类别
     */
    @ExcelProperty(index = 12)
    private String declareType;

    /**
     * 计量单位
     */
    @ExcelProperty(index = 13)
    private String messaureUnit;

    /**
     * 规格型号
     */
    @ExcelProperty(index = 14)
    private String model;

    /**
     * 经营单位
     */
    @ExcelProperty(index = 15)
    private String businessUnit;

    /**
     * 十位编码
     */
    @ExcelProperty(index = 16)
    private String tenCode;

    /**
     * 收件人地址
     */
    @ExcelProperty(index = 17)
    private String receiverAddress;

    /**
     * 收件人
     */
    @ExcelProperty(index = 18)
    private String receiver;

    /**
     * 收件人电话
     */
    @ExcelProperty(index = 19)
    private String receiverPhone;

    /**
     * 发件人地址
     */
    @ExcelProperty(index = 20)
    private String senderAddress;

    /**
     * 发件人
     */
    @ExcelProperty(index = 21)
    private String sender;

    /**
     * 发件人电话
     */
    @ExcelProperty(index = 22)
    private String senderTelephone;

    /**
     * 袋号
     */
    @ExcelProperty(index = 23)
    private String bagNo;

    /**
     * 分运单号
     */
    @ExcelProperty(index = 24)
    private String remark;

    /**
     * 卖家信息
     */
    @ExcelProperty(index = 25)
    private String sellerInfo;

    /**
     * 备注
     */
    @ExcelProperty(index = 26)
    private String note;

    /**
     * 进港年份
     */
    private String inBoundYear;

    /**
     * 进港月份
     */
    private String inBoundMonth;

    /**
     * 进港日子
     */
    private String inBoundDay;

    /**
     * 委托书编号
     */
    private String mandateNo;
}
