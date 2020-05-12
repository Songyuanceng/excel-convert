package com.yto;

import lombok.*;

/**
 * @author 王玉鹏
 * @version 1.0
 * @className ExportTest
 * @description TODO
 * @date 2019/5/22 16:18
 */

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTest {
    @ExcelColumn(value = "cityCode", col = 1)
    private String cityCode;

    @ExcelColumn(value = "markId", col = 2)
    private String markId;

    @ExcelColumn(value = "toaluv", col = 3)
    private String toaluv;

    @ExcelColumn(value = "date", col = 4)
    private String date;

    @ExcelColumn(value = "clientVer", col = 5)
    private String clientVer;

}
