package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.response.OrderAdminResponse;
import com.ngtoan.phone_store.dto.response.OrderDetailResponse;
import com.ngtoan.phone_store.dto.response.PaymentResponse;
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
public class OrderExcelService {

    private final OrderService orderService;

    private static final String[] HEADERS = {
            "orderID",
            "orderCode",
            "createdDate",
            "customerName",
            "phone",
            "address",
            "orderStatus",
            "paymentMethod",
            "paymentStatus",
            "paymentAmount",
            "paymentDate",
            "productID",
            "productName",
            "quantity",
            "price",
            "lineTotal",
            "orderTotal",
            "note"
    };

    public byte[] exportOrders() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Orders");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            List<OrderAdminResponse> orders = orderService.getAllOrdersForAdmin();

            int rowIndex = 1;

            for (OrderAdminResponse order : orders) {
                List<OrderDetailResponse> items = order.getItems();

                if (items == null || items.isEmpty()) {
                    Row row = sheet.createRow(rowIndex++);
                    writeOrderRow(row, order, null, moneyStyle, dateStyle);
                    continue;
                }

                for (OrderDetailResponse item : items) {
                    Row row = sheet.createRow(rowIndex++);
                    writeOrderRow(row, order, item, moneyStyle, dateStyle);
                }
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Xuất Excel đơn hàng thất bại: " + e.getMessage());
        }
    }

    private void writeOrderRow(
            Row row,
            OrderAdminResponse order,
            OrderDetailResponse item,
            CellStyle moneyStyle,
            CellStyle dateStyle
    ) {
        PaymentResponse payment = order.getPayment();

        setCell(row, 0, order.getOrderID());
        setCell(row, 1, order.getOrderCode());
        setDateCell(row, 2, order.getCreatedDate(), dateStyle);

        setCell(row, 3, order.getCustomerName());
        setCell(row, 4, order.getPhone());
        setCell(row, 5, order.getAddress());

        setCell(row, 6, order.getStatus());
        setCell(row, 7, order.getPaymentMethod());

        setCell(row, 8, payment != null ? payment.getStatus() : "");
        setMoneyCell(row, 9, payment != null ? payment.getAmount() : BigDecimal.ZERO, moneyStyle);
        setDateCell(row, 10, payment != null ? payment.getPaymentDate() : null, dateStyle);

        if (item != null) {
            setCell(row, 11, item.getProductID());
            setCell(row, 12, item.getProductName());
            setCell(row, 13, item.getQuantity());
            setMoneyCell(row, 14, item.getPrice(), moneyStyle);
            setMoneyCell(row, 15, item.getSubtotal(), moneyStyle);
        } else {
            setCell(row, 11, "");
            setCell(row, 12, "");
            setCell(row, 13, "");
            setMoneyCell(row, 14, BigDecimal.ZERO, moneyStyle);
            setMoneyCell(row, 15, BigDecimal.ZERO, moneyStyle);
        }

        setMoneyCell(row, 16, order.getTotalAmount(), moneyStyle);
        setCell(row, 17, order.getNote());
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