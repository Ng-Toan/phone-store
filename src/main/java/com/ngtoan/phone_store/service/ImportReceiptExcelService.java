package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ImportDetailRequest;
import com.ngtoan.phone_store.dto.request.ImportReceiptRequest;
import com.ngtoan.phone_store.dto.response.ImportDetailResponse;
import com.ngtoan.phone_store.dto.response.ImportReceiptResponse;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.ProductRepository;
import com.ngtoan.phone_store.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportReceiptExcelService {

    private final ImportReceiptService importReceiptService;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    private static final String[] IMPORT_HEADERS = {
            "supplierID",
            "note",
            "productID",
            "quantity",
            "importPrice"
    };

    private static final String[] EXPORT_HEADERS = {
            "importID",
            "supplierID",
            "supplierName",
            "createdDate",
            "status",
            "note",
            "productID",
            "productName",
            "quantity",
            "importPrice",
            "subTotal",
            "totalAmount"
    };

    public byte[] exportImports() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Imports");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            List<ImportReceiptResponse> receipts = importReceiptService.getAll();

            int rowIndex = 1;

            for (ImportReceiptResponse receipt : receipts) {
                List<ImportDetailResponse> details = receipt.getDetails();

                if (details == null || details.isEmpty()) {
                    Row row = sheet.createRow(rowIndex++);
                    writeExportRow(row, receipt, null, moneyStyle, dateStyle);
                    continue;
                }

                for (ImportDetailResponse detail : details) {
                    Row row = sheet.createRow(rowIndex++);
                    writeExportRow(row, receipt, detail, moneyStyle, dateStyle);
                }
            }

            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Xuất Excel phiếu nhập thất bại: " + e.getMessage());
        }
    }

    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Import Template");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(IMPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sample1 = sheet.createRow(1);
            setCell(sample1, 0, 1);
            setCell(sample1, 1, "Nhập hàng từ file Excel");
            setCell(sample1, 2, 1);
            setCell(sample1, 3, 5);
            setCell(sample1, 4, 12000000);

            Row sample2 = sheet.createRow(2);
            setCell(sample2, 0, 1);
            setCell(sample2, 1, "Nhập hàng từ file Excel");
            setCell(sample2, 2, 2);
            setCell(sample2, 3, 3);
            setCell(sample2, 4, 15000000);

            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Tạo file mẫu nhập hàng thất bại: " + e.getMessage());
        }
    }

    public String importImports(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn file Excel.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BadRequestException("File không hợp lệ. Vui lòng chọn file .xlsx hoặc .xls.");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new BadRequestException("File Excel không có dữ liệu nhập hàng.");
            }

            ImportReceiptRequest request = buildRequestFromSheet(sheet);

            ImportReceiptResponse response = importReceiptService.create(request);

            int itemCount = request.getDetails() == null ? 0 : request.getDetails().size();

            return "Nhập Excel thành công phiếu nhập #" + response.getImportID()
                    + " với " + itemCount + " sản phẩm.";

        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Đọc file Excel nhập hàng thất bại: " + e.getMessage());
        }
    }

    private ImportReceiptRequest buildRequestFromSheet(Sheet sheet) {
        Integer supplierID = null;
        String note = null;
        List<ImportDetailRequest> details = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null || isEmptyRow(row)) {
                continue;
            }

            Integer rowSupplierID = getInteger(row, 0);
            String rowNote = getString(row, 1);
            Integer productID = getInteger(row, 2);
            Integer quantity = getInteger(row, 3);
            BigDecimal importPrice = getBigDecimal(row, 4);

            if (rowSupplierID == null) {
                throw new BadRequestException("Dòng " + (i + 1) + ": supplierID không được để trống.");
            }

            if (supplierID == null) {
                supplierID = rowSupplierID;
                note = rowNote;
            } else if (!supplierID.equals(rowSupplierID)) {
                throw new BadRequestException(
                        "Dòng " + (i + 1) + ": Một file Excel chỉ được tạo cho một supplierID."
                );
            }

            if (!supplierRepository.existsById(rowSupplierID)) {
                throw new ResourceNotFoundException(
                        "Dòng " + (i + 1) + ": supplierID không tồn tại: " + rowSupplierID
                );
            }

            if (productID == null) {
                throw new BadRequestException("Dòng " + (i + 1) + ": productID không được để trống.");
            }

            if (!productRepository.existsById(productID)) {
                throw new ResourceNotFoundException(
                        "Dòng " + (i + 1) + ": productID không tồn tại: " + productID
                );
            }

            if (quantity == null || quantity <= 0) {
                throw new BadRequestException("Dòng " + (i + 1) + ": quantity phải lớn hơn 0.");
            }

            if (importPrice == null || importPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Dòng " + (i + 1) + ": importPrice phải lớn hơn 0.");
            }

            ImportDetailRequest detail = new ImportDetailRequest();
            detail.setProductID(productID);
            detail.setQuantity(quantity);
            detail.setImportPrice(importPrice);

            details.add(detail);
        }

        if (supplierID == null) {
            throw new BadRequestException("File Excel không có dòng nhập hàng hợp lệ.");
        }

        if (details.isEmpty()) {
            throw new BadRequestException("Import details must not be empty.");
        }

        ImportReceiptRequest request = new ImportReceiptRequest();
        request.setSupplierID(supplierID);
        request.setNote(note);
        request.setDetails(details);

        return request;
    }

    private void writeExportRow(
            Row row,
            ImportReceiptResponse receipt,
            ImportDetailResponse detail,
            CellStyle moneyStyle,
            CellStyle dateStyle
    ) {
        setCell(row, 0, receipt.getImportID());
        setCell(row, 1, receipt.getSupplierID());
        setCell(row, 2, receipt.getSupplierName());
        setDateCell(row, 3, receipt.getCreatedDate(), dateStyle);
        setCell(row, 4, receipt.getStatus());
        setCell(row, 5, receipt.getNote());

        if (detail != null) {
            setCell(row, 6, detail.getProductID());
            setCell(row, 7, detail.getProductName());
            setCell(row, 8, detail.getQuantity());
            setMoneyCell(row, 9, detail.getImportPrice(), moneyStyle);
            setMoneyCell(row, 10, detail.getSubTotal(), moneyStyle);
        } else {
            setCell(row, 6, "");
            setCell(row, 7, "");
            setCell(row, 8, "");
            setMoneyCell(row, 9, BigDecimal.ZERO, moneyStyle);
            setMoneyCell(row, 10, BigDecimal.ZERO, moneyStyle);
        }

        setMoneyCell(row, 11, receipt.getTotalAmount(), moneyStyle);
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

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Integer getInteger(Row row, int index) {
        String value = getString(row, index);

        if (isBlank(value)) {
            return null;
        }

        try {
            if (value.contains(".")) {
                value = value.substring(0, value.indexOf("."));
            }

            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new BadRequestException("Cột " + IMPORT_HEADERS[index] + " phải là số nguyên.");
        }
    }

    private BigDecimal getBigDecimal(Row row, int index) {
        String value = getString(row, index);

        if (isBlank(value)) {
            return null;
        }

        try {
            value = value.replace(",", "").replace("đ", "").trim();
            return new BigDecimal(value);
        } catch (Exception e) {
            throw new BadRequestException("Cột " + IMPORT_HEADERS[index] + " phải là số.");
        }
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < IMPORT_HEADERS.length; i++) {
            if (!getString(row, i).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}