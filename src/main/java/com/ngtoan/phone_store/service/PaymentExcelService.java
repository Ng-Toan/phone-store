package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.AdminPaymentResponse;
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
public class PaymentExcelService {

    private final PaymentService paymentService;

    private static final String[] HEADERS = {
            "paymentID",
            "paymentCode",
            "orderID",
            "orderCode",
            "customerName",
            "method",
            "paymentStatus",
            "orderStatus",
            "amount",
            "paymentDate",
            "transactionCode",
            "note"
    };

    public byte[] exportPayments() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payments");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            List<AdminPaymentResponse> payments = paymentService.getAllPayments();

            int rowIndex = 1;

            for (AdminPaymentResponse payment : payments) {
                Row row = sheet.createRow(rowIndex++);

                setCell(row, 0, payment.getPaymentID());
                setCell(row, 1, payment.getPaymentCode());

                setCell(row, 2, payment.getOrderID());
                setCell(row, 3, payment.getOrderCode());

                setCell(row, 4, payment.getCustomerName());
                setCell(row, 5, payment.getMethod());

                setCell(row, 6, payment.getStatus());
                setCell(row, 7, payment.getOrderStatus());

                setMoneyCell(row, 8, payment.getAmount(), moneyStyle);
                setDateCell(row, 9, payment.getPaymentDate(), dateStyle);

                setCell(row, 10, payment.getTransactionCode());
                setCell(row, 11, payment.getNote());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Xuất Excel thanh toán thất bại: " + e.getMessage());
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