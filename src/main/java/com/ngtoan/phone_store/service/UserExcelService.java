package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.UserAdminResponse;
import com.ngtoan.phone_store.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserExcelService {

    private final AdminUserService adminUserService;

    private static final String[] HEADERS = {
            "userId",
            "username",
            "fullName",
            "email",
            "phone",
            "roleId",
            "roleName",
            "levelId",
            "levelName",
            "discountPercent",
            "minSpent",
            "totalSpent",
            "status",
            "statusName",
            "createdDate"
    };

    public byte[] exportUsers() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            List<UserAdminResponse> users = adminUserService.getAllUsers();

            int rowIndex = 1;

            for (UserAdminResponse user : users) {
                Row row = sheet.createRow(rowIndex++);

                setCell(row, 0, user.getUserId());
                setCell(row, 1, user.getUsername());
                setCell(row, 2, user.getFullName());
                setCell(row, 3, user.getEmail());
                setCell(row, 4, user.getPhone());

                setCell(row, 5, user.getRoleId());
                setCell(row, 6, user.getRoleName());

                setCell(row, 7, user.getLevelId());
                setCell(row, 8, user.getLevelName());

                setMoneyCell(row, 9, user.getDiscountPercent(), moneyStyle);
                setMoneyCell(row, 10, user.getMinSpent(), moneyStyle);
                setMoneyCell(row, 11, user.getTotalSpent(), moneyStyle);

                setCell(row, 12, Boolean.TRUE.equals(user.getStatus()) ? "ACTIVE" : "LOCKED");
                setCell(row, 13, user.getStatusName());

                setDateCell(row, 14, user.getCreatedDate(), dateStyle);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Xuất Excel người dùng thất bại: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();

        style.setDataFormat(format.getFormat("#,##0"));

        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();

        style.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm"));

        return style;
    }

    private void setCell(Row row, int index, Object value) {
        Cell cell = row.createCell(index);

        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }

        cell.setCellValue(String.valueOf(value));
    }

    private void setMoneyCell(Row row, int index, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(index);

        if (value == null) {
            cell.setCellValue(0);
        } else {
            cell.setCellValue(value.doubleValue());
        }

        cell.setCellStyle(style);
    }

    private void setDateCell(Row row, int index, LocalDateTime value, CellStyle style) {
        Cell cell = row.createCell(index);

        if (value == null) {
            cell.setCellValue("");
            return;
        }

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}