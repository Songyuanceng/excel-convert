package com.yto;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.util.ObjectUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jbarcode.encode.InvalidAtributeException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * <pre>
 *  名称：FileUtil
 *  描述：
 * </pre>
 *
 * @author Songyuancheng
 * @date 2019/7/15 10:24
 * @since v1.0.0
 */
public class FileUtil {
    private static final Map<String, URL> TEMPLATE_CACHE = Maps.newConcurrentMap();

    public final static Map<String, GoodsHScode> DATAMAP = new HashMap<String, GoodsHScode>();

    public final static Map<String, String> currCodeMap = new HashMap<String, String>();

    //预读HS商品HS编码
    static {
        try {
            System.out.println("加载数据");
            String tiffPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").replaceAll("jar:", "").trim();
            String decodePath = URLDecoder.decode(tiffPath, "UTF-8");
            String imgPath = decodePath.substring(0, decodePath.lastIndexOf("/", decodePath.lastIndexOf("/") - 3)) + "/templates/Book.xls";
            //String imgPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").trim() + "/Book.xls";
            //InputStream resourceAsStream = ClassUtils.class.getClassLoader().getResourceAsStream("./Book.xls");
            FileInputStream fileInputStream = new FileInputStream(new File(imgPath));
            List<Object> read = EasyExcelFactory.read(new BufferedInputStream(fileInputStream), new Sheet(1, 1, GoodsHScode.class));
            for (Object obj : read) {
                GoodsHScode entity = (GoodsHScode) obj;
                String zhName = entity.getZhName();
                DATAMAP.put(zhName, entity);
            }

            //币制
            currCodeMap.put("110", "HKD");
            currCodeMap.put("113", "IRR");
            currCodeMap.put("116", "JPY");
            currCodeMap.put("118", "KWD");
            currCodeMap.put("121", "MOP");
            currCodeMap.put("122", "MYR");
            currCodeMap.put("127", "PRK");
            currCodeMap.put("129", "PHP");
            currCodeMap.put("132", "SGD");
            currCodeMap.put("136", "THB");
            currCodeMap.put("142", "CNY");
            currCodeMap.put("143", "TWD");
            currCodeMap.put("300", "EUR");
            currCodeMap.put("302", "DKK");
            currCodeMap.put("303", "GBP");
            currCodeMap.put("326", "NOK");
            currCodeMap.put("330", "SEK");
            currCodeMap.put("331", "CHF");
            currCodeMap.put("501", "CAD");
            currCodeMap.put("502", "USD");
            currCodeMap.put("601", "AUD");
            currCodeMap.put("609", "NZD");
            currCodeMap.put("133", "KRW");
            currCodeMap.put("304", "DEM");
            currCodeMap.put("305", "FRF");
            currCodeMap.put("307", "ITL");
            currCodeMap.put("312", "ESP");
            currCodeMap.put("315", "ATS");
            currCodeMap.put("318", "FIM");
            System.out.println("加载完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FileUtil() {
    }

    public static void createFile(File file, JProgressBar ax) throws FileNotFoundException, ParseException {
        //解析二行数据
        PDClearancePrintEntity pdClearancePrintEntity = ExcelUtils.readExcel(PDClearancePrintEntity.class, file, 2);

        //解析大表头信息
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        List<Object> read = EasyExcelFactory.read(inputStream, new Sheet(1, 5, PDClearancePrintEntity.class));

        //注入总运单号
        setBillNo(pdClearancePrintEntity, read);

        //合并数据
        Map<String, List<PDClearancePrintEntity>> sameWaybill = mergeTheSameWaybill(read);

        //遍历
        int a = 1;
        ax.setMaximum(read.size());
        ax.setString("正在生成文件，可前往D:\\customs\\photo目录下查看");
        for (Object object : read) {
            ax.setValue(a++);
            //Map<String, Object> map = BeanUtil.beanToMap(object);
            PDClearancePrintEntity entity = (PDClearancePrintEntity) object;

            //生成发票
            productCommercialInvoice(entity, sameWaybill, "C:/customs/photo");

            //生成委托协议书
            productAgreementBook(entity, sameWaybill, "C:/customs/photo");

            //生成运单信息单
            productWaybillInfo(entity, sameWaybill, "C:/customs/photo");
            if (a == read.size() + 1) {
                ax.setString("全部转换完成！");
            }
        }
        System.out.println("+++图片转换完成+++");
    }

    private static Map<String, List<PDClearancePrintEntity>> mergeTheSameWaybill(List<Object> read) {
        HashMap<String, List<PDClearancePrintEntity>> hashMap = Maps.newHashMap();
        ArrayList<PDClearancePrintEntity> list = Lists.newArrayList();
        Iterator<Object> iterator = read.iterator();
        String logisticsNo = "";
        int count = 1;
        int loop = 0;
        while (iterator.hasNext()) {
            loop++;
            PDClearancePrintEntity entity = (PDClearancePrintEntity) iterator.next();
            String mailNo = entity.getYtoMailNo();
            if (!logisticsNo.equalsIgnoreCase(mailNo)) {
                //多条
                if (read.size() > 1) {
                    if (count > 1) {
                        hashMap.put(logisticsNo, (ArrayList<PDClearancePrintEntity>) list.clone());
                        count = 1;
                        list.clear();
                        logisticsNo = mailNo;
                    }
                    if (count == 1 && loop > 1) {
                        hashMap.put(logisticsNo, (ArrayList<PDClearancePrintEntity>) list.clone());
                        list.clear();
                        logisticsNo = mailNo;
                    }
                    if (count == 1 && StringUtils.isBlank(logisticsNo)) {
                        logisticsNo = mailNo;
                    }
                    list.add(entity);
                } else {
                    //只有一条数据
                    list.add(entity);
                    hashMap.put(mailNo, list);
                }
                continue;
            }
            list.add(entity);
            iterator.remove(); //移除
            count++;
        }
        hashMap.put(logisticsNo, list);
        return hashMap;
    }

    private static void productCommercialInvoice(PDClearancePrintEntity entity, Map<String, List<PDClearancePrintEntity>> sameWaybill, String path) {
        try {
            //生成日期
            String date = entity.getInBoundYear() + "-" + entity.getInBoundMonth() + "-" + entity.getInBoundDay();

            // pdf模板
            URL resource = TEMPLATE_CACHE.get("commercialInvoice");
            if (ObjectUtils.isEmpty(resource)) {
                resource = ClassUtils.class.getClassLoader().getResource("templates/fapiao.tiff");
                TEMPLATE_CACHE.put("commercialInvoice", resource);
            }

            String tiffPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").replaceAll("jar:", "").trim();
            String decodePath = URLDecoder.decode(tiffPath, "UTF-8");
            String imgPath = decodePath.substring(0, decodePath.lastIndexOf("/", decodePath.lastIndexOf("/") - 3)) + "/templates/fapiao.tiff";
            //String imgPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").trim() + "/fapiao.tiff";
            System.out.println("path1=" + imgPath);
            BufferedImage bufferedImage = readBufferedImage(imgPath);
            //添加文字水印
            tagWaterMark(entity.getYtoMailNo(), bufferedImage, 22, 700, 2000);
            tagWaterMark(date, bufferedImage, 22, 1350, 1890);
            tagWaterMark(entity.getSender(), bufferedImage, 24, 1370, 1760);
            if (entity.getSenderAddress().length() > 22) {
                formatString(entity.getSenderAddress(), 22, bufferedImage, 24, 1370, 1720, 25);
            } else {
                tagWaterMark(entity.getSenderAddress(), bufferedImage, 24, 1370, 1700);
            }
            tagWaterMark(entity.getSenderTelephone(), bufferedImage, 24, 1370, 1650);
            tagWaterMark(entity.getReceiver(), bufferedImage, 24, 700, 1760);
            if (entity.getReceiverAddress().length() > 22) {
                formatString(entity.getReceiverAddress(), 22, bufferedImage, 24, 700, 1720, 25);
            } else {
                tagWaterMark(entity.getReceiverAddress(), bufferedImage, 24, 700, 1700);
            }
            tagWaterMark(entity.getReceiverPhone(), bufferedImage, 24, 700, 1650);
            //币制
            String currEnCode = getCurrenCode(entity.getCurrCode());
            tagWaterMark(currEnCode, bufferedImage, 24, 318, 1362);
            tagWaterMark(currEnCode, bufferedImage, 24, 210, 1362);
            tagWaterMark(currEnCode, bufferedImage, 24, 318, 748);
            //明细
            if (sameWaybill.get(entity.getYtoMailNo()).size() == 1) {
                formatString(entity.getGoodsName(), 5, bufferedImage, 24, 850, 1310, 27);
                tagWaterMark(entity.getPackNum(), bufferedImage, 24, 610, 1310);
                tagWaterMark(entity.getWeight(), bufferedImage, 24, 410, 1310);
                tagWaterMark(String.valueOf(entity.getWorth()), bufferedImage, 24, 210, 1310);
                tagWaterMark(entity.getWeight(), bufferedImage, 24, 415, 635);
                tagWaterMark(String.valueOf(entity.getWorth()), bufferedImage, 24, 205, 635);

                tagWaterMark("1", bufferedImage, 24, 1070, 635);
                tagWaterMark("CHINA", bufferedImage, 24, 1350, 1310);
                tagWaterMark("N/M", bufferedImage, 24, 1230, 1310);
                tagWaterMark("1", bufferedImage, 24, 1080, 1310);
                tagWaterMark("PAPER BAG", bufferedImage, 24, 1000, 1310);
                tagWaterMark("PCS", bufferedImage, 24, 520, 1310);
            } else {
                List<PDClearancePrintEntity> pdClearancePrintEntities = sameWaybill.get(entity.getYtoMailNo());
                BigDecimal weight = new BigDecimal("0.0");
                BigDecimal worth = new BigDecimal("0.0");
                for (int i = 0; i < pdClearancePrintEntities.size(); i++) {
                    PDClearancePrintEntity entity1 = pdClearancePrintEntities.get(i);
                    weight = weight.add(new BigDecimal(entity1.getWeight()));
                    worth = worth.add(new BigDecimal(entity1.getWorth()));
                    if (i < 13) {
                        formatString(entity1.getGoodsName(), 5, bufferedImage, 24, 850, 1310 - (i * 40), 27);
                        tagWaterMark(entity1.getPackNum(), bufferedImage, 24, 610, 1310 - (i * 40));
                        tagWaterMark(entity1.getWeight(), bufferedImage, 24, 410, 1310 - (i * 40));
                        tagWaterMark(String.valueOf(entity1.getWorth()), bufferedImage, 24, 210, 1310 - (i * 40));

                        tagWaterMark("CHINA", bufferedImage, 24, 1350, 1310 - (i * 40));
                        tagWaterMark("N/M", bufferedImage, 24, 1230, 1310 - (i * 40));
                        tagWaterMark(String.valueOf(i + 1), bufferedImage, 24, 1080, 1310 - (i * 40));
                        tagWaterMark("PAPER BAG", bufferedImage, 24, 1000, 1310 - (i * 40));
                        tagWaterMark("PCS", bufferedImage, 24, 520, 1310 - (i * 40));
                    }
                    if (i == 13) {
                        tagWaterMark("......", bufferedImage, 24, 1350, 1310 - (i * 40));
                    }
                }
                tagWaterMark(String.valueOf(pdClearancePrintEntities.size()), bufferedImage, 24, 1070, 635);
                tagWaterMark(String.valueOf(weight), bufferedImage, 24, 415, 635);
                tagWaterMark(String.valueOf(worth), bufferedImage, 24, 205, 635);
            }

            //生成pdf路径
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            StringBuilder fileName = new StringBuilder();
            fileName.append("YTE").append("_")
                    .append(entity.getBillNo()).append("_")
                    .append(entity.getYtoMailNo()).append("_")
                    .append("E").append("_")
                    .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append("_")
                    .append("INV.TIF");

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file, fileName.toString())));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufferedImage);
            out.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    private static String getCurrenCode(String currCode) {
        if (currCode.matches("^[A-Za-z]*$")) {
            return currCode;
        } else {
            return currCodeMap.get(currCode) == null ? "TWD" : currCodeMap.get(currCode);
        }
    }

    private static void productAgreementBook(PDClearancePrintEntity entity, Map<String, List<PDClearancePrintEntity>> sameWaybill, String path) {
        try {
            //生成日期
            String date = entity.getInBoundYear() + "年" + entity.getInBoundMonth() + "月" + entity.getInBoundDay() + "日";

            // pdf模板
            URL agreementUrl = TEMPLATE_CACHE.get("agreement");
            if (ObjectUtils.isEmpty(agreementUrl)) {
                agreementUrl = ClassUtils.class.getClassLoader().getResource("templates/weituoshu.tiff");
                TEMPLATE_CACHE.put("agreement", agreementUrl);
            }

            String tiffPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").replaceAll("jar:", "").trim();
            String decodePath = URLDecoder.decode(tiffPath, "UTF-8");
            String imgPath = decodePath.substring(0, decodePath.lastIndexOf("/", decodePath.lastIndexOf("/") - 3)) + "/templates/weituoshu.tiff";
            //String imgPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").trim() + "/weituoshu.tiff";
            System.out.println("路径=" + imgPath);
            BufferedImage bufferedImage = readBufferedImage(imgPath);

            //文字水印
            tagWaterMark(entity.getMandateNo(), bufferedImage, 25, 700, 2005);
            tagWaterMark(entity.getInBoundYear(), bufferedImage, 24, 1030, 1745);
            tagWaterMark(entity.getInBoundYear(), bufferedImage, 24, 720, 1575);
            tagWaterMark(entity.getInBoundMonth(), bufferedImage, 24, 550, 1575);
            tagWaterMark(entity.getInBoundDay(), bufferedImage, 24, 370, 1575);

            tagWaterMark(entity.getGoodsName(), bufferedImage, 24, 1210, 1310);
            tagWaterMark(date, bufferedImage, 24, 1210, 1180);
            tagWaterMark(computeTotalWorth(entity, sameWaybill), bufferedImage, 24, 1210, 1220);
            tagWaterMark(date, bufferedImage, 24, 550, 1265);
            tagWaterMark(entity.getYtoMailNo(), bufferedImage, 24, 1210, 1130);

            StringBuilder builder = new StringBuilder();
            String str = builder.append(entity.getInBoundYear()).append("  ")
                    .append(entity.getInBoundMonth()).append("   ")
                    .append(entity.getInBoundDay()).toString();
            tagWaterMark(str, bufferedImage, 24, 397, 345);
            tagWaterMark(str, bufferedImage, 24, 1020, 345);

            //hs编码
            GoodsHScode goodsHScode = DATAMAP.get(entity.getGoodsName());
            if (ObjectUtils.isEmpty(goodsHScode)) {
                tagWaterMark("", bufferedImage, 24, 1210, 1265);
            } else {
                String hsCode = goodsHScode.getHsCode();
                tagWaterMark(hsCode, bufferedImage, 24, 1210, 1265);
            }

            //生成pdf路径
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            StringBuilder fileName = new StringBuilder();
            fileName.append("YTE").append("_")
                    .append(entity.getBillNo()).append("_")
                    .append(entity.getYtoMailNo()).append("_")
                    .append("E").append("_")
                    .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append("_")
                    .append("AUT.TIF");

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file, fileName.toString())));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufferedImage);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String computeTotalWorth(PDClearancePrintEntity entity, Map<String, List<PDClearancePrintEntity>> sameWaybill) {
        //ji算总价
        String ytoMailNo = entity.getYtoMailNo();
        BigDecimal totalWorth = new BigDecimal("0.0");
        if (sameWaybill.get(ytoMailNo).size() == 1) {
            String worth = entity.getWorth();
            totalWorth = totalWorth.add(new BigDecimal(worth));
        } else {
            List<PDClearancePrintEntity> entityList = sameWaybill.get(ytoMailNo);
            for (int i = 0; i < entityList.size(); i++) {
                PDClearancePrintEntity printEntity = entityList.get(i);
                String worth = printEntity.getWorth();
                totalWorth = totalWorth.add(new BigDecimal(worth));
            }
        }
        //转化币制
        String currEnCode = getCurrenCode(entity.getCurrCode());
        return totalWorth + currEnCode;
    }

    private static void productWaybillInfo(PDClearancePrintEntity entity, Map<String, List<PDClearancePrintEntity>> sameWaybill, String path) {
        try {
            // pdf模板
            URL waybillUrl = TEMPLATE_CACHE.get("waybill");
            if (ObjectUtils.isEmpty(waybillUrl)) {
                waybillUrl = ClassUtils.class.getClassLoader().getResource("templates/yundan.tiff");
                TEMPLATE_CACHE.put("waybill", waybillUrl);
            }

            //读取tif图片
            String tiffPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").replaceAll("jar:", "").trim();
            String decodePath = URLDecoder.decode(tiffPath, "UTF-8");
            String imgPath = decodePath.substring(0, decodePath.lastIndexOf("/", decodePath.lastIndexOf("/") - 3)) + "/templates/yundan.tiff";
            //String imgPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").trim() + "/yundan.tiff";
            System.out.println("path=" + imgPath);
            BufferedImage bufferedImage = readBufferedImage(imgPath);

            //添加文自水印
            String ytoMailNo = entity.getYtoMailNo();
            //BufferedImage image = JbarcodeUtil.getJBarcode().createBarcode(ytoMailNo);
            //Graphics graphics = bufferedImage.getGraphics();
            //graphics.drawImage(image, 385, 57, image.getWidth() * 6 / 13, image.getHeight(), null);

            tagWaterMark(ytoMailNo, bufferedImage, 14, 230, 486);
            //收件人
            tagWaterMark(entity.getReceiver(), bufferedImage, 14, 310, 460);
            tagWaterMark(entity.getReceiverPhone(), bufferedImage, 14, 310, 440);
            formatString(entity.getReceiverAddress(), 19, bufferedImage, 14, 310, 420, 20);

            //寄件人
            tagWaterMark(entity.getSender() + " " + entity.getSenderTelephone(), bufferedImage, 14, 310, 372);
            formatString(entity.getSenderAddress(), 19, bufferedImage, 14, 310, 352, 20);
            //tagWaterMark(entity.getSenderTelephone(), bufferedImage, 14, 310, 332);
            //条码
            tagWaterMark(ytoMailNo, bufferedImage, 14, 160, 215);
            //收件人
            tagWaterMark(entity.getReceiver() + "  " + entity.getReceiverPhone(), bufferedImage, 14, 310, 190);
            formatString(entity.getReceiverAddress(), 19, bufferedImage, 14, 310, 170, 20);
            //订单详情
            if (sameWaybill.get(entity.getYtoMailNo()).size() == 1) {
                String goodsName = formatString(entity.getGoodsName(), 6);
                String packNum = entity.getPackNum();
                String worth = entity.getWorth();
                String weight = entity.getWeight();
                tagWaterMark(goodsName, bufferedImage, 14, 310, 125);
                tagWaterMark(packNum, bufferedImage, 14, 180, 125);
                tagWaterMark(weight, bufferedImage, 14, 145, 125);
                tagWaterMark(worth, bufferedImage, 14, 95, 125);
            } else {
                List<PDClearancePrintEntity> entityList = sameWaybill.get(entity.getYtoMailNo());
                for (int i = 0; i < entityList.size(); i++) {
                    if (i > 4) {
                        tagWaterMark("......", bufferedImage, 14, 310, 56);
                        break;
                    }
                    PDClearancePrintEntity printEntity = entityList.get(i);
                    String goodsName = formatString(printEntity.getGoodsName(), 6);
                    String packNum = printEntity.getPackNum();
                    String worth = printEntity.getWorth();
                    String weight = printEntity.getWeight();
                    tagWaterMark(goodsName, bufferedImage, 14, 310, 125 - (i * 15));
                    tagWaterMark(packNum, bufferedImage, 14, 180, 125 - (i * 15));
                    tagWaterMark(weight, bufferedImage, 14, 145, 125 - (i * 15));
                    tagWaterMark(worth, bufferedImage, 14, 95, 125 - (i * 15));
                }
            }
            tagWaterMark(entity.getYtoMailNo(), bufferedImage, 14, 300, 32);
            tagWaterMark(String.valueOf(sameWaybill.get(entity.getYtoMailNo()).size()), bufferedImage, 14, 40, 32);
            //时间
            tagWaterMark(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"), bufferedImage, 13, 145, 10);

            //生成TIF路径
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            StringBuilder fileName = new StringBuilder();
            fileName.append("YTE").append("_")
                    .append(entity.getBillNo()).append("_")
                    .append(entity.getYtoMailNo()).append("_")
                    .append("E").append("_")
                    .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append("_")
                    .append("AWB.TIF");

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file, fileName.toString())));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufferedImage);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String formatString(String str, int checkLen) {
        String result = str;
        if (str.length() > checkLen) {
            result = str.substring(0, checkLen).concat("...");
        }
        return result;
    }

    private static void productWaybillInfoBack(PDClearancePrintEntity entity, String path) {
        try {
            // pdf模板
            URL waybillUrl = TEMPLATE_CACHE.get("waybill");
            if (ObjectUtils.isEmpty(waybillUrl)) {
                waybillUrl = ClassUtils.class.getClassLoader().getResource("templates/yundan.tiff");
                TEMPLATE_CACHE.put("waybill", waybillUrl);
            }

            //读取tif图片
            String tiffPath = ClassLoader.getSystemResource("templates").toString().replaceAll("file:/", "").replaceAll("jar:", "").trim();
            String decodePath = URLDecoder.decode(tiffPath, "UTF-8");
            String imgPath = decodePath.substring(0, decodePath.lastIndexOf("/", decodePath.lastIndexOf("/") - 3)) + "/templates/yundan.tiff";
            //String imgPath=ClassLoader.getSystemResource("templates").toString().replaceAll("file:/","").trim()+"/yundan.tiff";
            System.out.println("path=" + imgPath);
            BufferedImage bufferedImage = readBufferedImage(imgPath);

            //添加文自水印
            String ytoMailNo = entity.getYtoMailNo();
            BufferedImage image = JbarcodeUtil.getJBarcode().createBarcode(ytoMailNo);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.drawImage(image, 385, 57, image.getWidth() * 6 / 13, image.getHeight(), null);

            tagWaterMark(ytoMailNo, bufferedImage, 24, 200, 900);

            tagWaterMark(entity.getReceiver(), bufferedImage, 14, 330, 362);
            tagWaterMark(entity.getSender(), bufferedImage, 14, 663, 362);
            tagWaterMark(entity.getReceiveCompanyName(), bufferedImage, 14, 328, 338);
            tagWaterMark(entity.getSendCompanyName(), bufferedImage, 14, 660, 338);

            //tagWaterMark(entity.getReceiverAddress(), bufferedImage, 14, 380, 295);
            formatString(entity.getReceiverAddress(), 18, bufferedImage, 14, 380, 295, 16);
            //tagWaterMark(entity.getSenderAddress(), bufferedImage, 14, 708, 295);
            formatString(entity.getSenderAddress(), 18, bufferedImage, 14, 708, 295, 16);

            tagWaterMark(entity.getReceiverPhone(), bufferedImage, 14, 300, 260);
            tagWaterMark(entity.getSenderTelephone(), bufferedImage, 14, 630, 260);

            tagWaterMark(entity.getGoodsNum(), bufferedImage, 14, 530, 210);
            tagWaterMark(entity.getWeight(), bufferedImage, 14, 440, 210);
            //tagWaterMark(entity.getGoodsName(), bufferedImage, 14, 660, 207);
            formatString(entity.getGoodsName(), 7, bufferedImage, 14, 676, 207, 15);
            tagWaterMark(entity.getYtoMailNo(), bufferedImage, 16, 530, 65);

            //生成TIF路径
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            StringBuilder fileName = new StringBuilder();
            fileName.append("YTE").append("_")
                    .append(entity.getBillNo()).append("_")
                    .append(entity.getYtoMailNo()).append("_")
                    .append("E").append("_")
                    .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append("_")
                    .append("AWB.TIF");

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file, fileName.toString())));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(bufferedImage);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAtributeException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取指定TIF格式文件
     *
     * @param srcImg
     * @return
     */
    private static BufferedImage readBufferedImage(final String srcImg) {
        //   String imgpath=ClassLoader.getSystemResource("templates").toString().replaceAll("file:/","").trim();
        try {
            //读取tif图片
            File file = new File(srcImg);
            ImageReader reader = ImageIO.getImageReadersByFormatName("tiff").next();
            FileImageInputStream inputStream = new FileImageInputStream(file);
            reader.setInput(inputStream);
            return reader.read(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加文字水印
     *
     * @param text      添加文字
     * @param targetImg 将添加图片
     * @param fontSize  字体大小
     * @param x         字体x坐标
     * @param y         字体y坐标
     */
    private static void tagWaterMark(String text, BufferedImage targetImg, int fontSize, int x, int y) {
        try {
            int width = targetImg.getWidth(null);
            int height = targetImg.getHeight(null);
            Graphics g = targetImg.getGraphics();
            g.setColor(Color.black);
            g.setFont(new java.awt.Font("宋体", Font.BOLD, fontSize));
            g.drawString(text, width - fontSize - x, height - fontSize / 2 - y);
            g.dispose();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void setBillNo(final PDClearancePrintEntity clearancePrint, final List<Object> lists) throws ParseException {
        String billNo = clearancePrint.getBillNo();
        String arrival = clearancePrint.getArrival();
        //转换字符串时间
        String entryDate = clearancePrint.getEntryDate();
        String pattern = "EEE MMM dd HH:mm:ss zzz yyyy";
        SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.US);
        System.out.println(entryDate);
        Date date = null;
        try {
            date = df.parse(entryDate);
        } catch (ParseException e) {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(entryDate);
        }
        System.out.println(date);

        Iterator<Object> iterator = lists.iterator();
        int count = 1;
        while (iterator.hasNext()) {
            PDClearancePrintEntity next = (PDClearancePrintEntity) iterator.next();
            next.setBillNo(billNo);
            next.setArrival(arrival);

            Calendar c = Calendar.getInstance();
            c.setTime(date);
            String year = String.valueOf(c.get(Calendar.YEAR));
            String month = String.valueOf(c.get(Calendar.MONTH) + 1);
            String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
            String monthDay = DateFormatUtils.format(new Date(), "MMdd");
            String num = ZhiyunUtil.frontCompWithZore(count, 3); //3就是一个常量

            next.setInBoundYear(year);
            next.setInBoundMonth(month);
            next.setInBoundDay(day);
            next.setMandateNo("0010" + monthDay + num);

            count++;
        }
    }

    /**
     * 按照指定长度编排字符串成多行
     *
     * @param str
     * @param length
     * @return
     */
    private static void formatString(String str, int length, BufferedImage bufferedImage, int font, int x, int y, int def) {
        int length1 = str.length();
        int loop = (length1 / length) + 1;
        int count = 1;
        for (int a = 1; a <= loop; a++) {
            try {
                String substring = str.substring((a - 1) * length, a * length);
                tagWaterMark(substring, bufferedImage, font, x, y - (count - 1) * def);
                count++;
            } catch (Exception e) {
                String substring = str.substring((a - 1) * length, length1);
                tagWaterMark(substring, bufferedImage, font, x, y - (count - 1) * def);
                count++;
            }
        }
    }
}
