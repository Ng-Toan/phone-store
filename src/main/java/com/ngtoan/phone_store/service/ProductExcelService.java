package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.ProductDetailRequest;
import com.ngtoan.phone_store.dto.request.ProductRequest;
import com.ngtoan.phone_store.entity.Product;
import com.ngtoan.phone_store.entity.ProductDetail;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.BrandRepository;
import com.ngtoan.phone_store.repository.CategoryRepository;
import com.ngtoan.phone_store.repository.ProductDetailRepository;
import com.ngtoan.phone_store.repository.ProductRepository;
import com.ngtoan.phone_store.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductExcelService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;

    private static final String[] HEADERS = {
            "name",
            "image",
            "price",
            "promotionPrice",
            "vat",
            "warranty",
            "isHot",
            "status",
            "categoryID",
            "brandID",
            "supplierID",
            "description",
            "ram",
            "storage",
            "cpu",
            "screen",
            "battery",
            "camera",
            "os",
            "chargingSpeed",
            "connectivity"
    };

    public byte[] exportProducts() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);

            String[] exportHeaders = {
                    "productID",
                    "name",
                    "image",
                    "price",
                    "promotionPrice",
                    "vat",
                    "quantity",
                    "warranty",
                    "isHot",
                    "status",
                    "categoryID",
                    "brandID",
                    "supplierID",
                    "description",
                    "ram",
                    "storage",
                    "cpu",
                    "screen",
                    "battery",
                    "camera",
                    "os",
                    "chargingSpeed",
                    "connectivity"
            };

            for (int i = 0; i < exportHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(exportHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Product> products = productRepository.findByStatusNot(Product.STATUS_AN_SAN_PHAM);

            int rowIndex = 1;

            for (Product product : products) {
                Row row = sheet.createRow(rowIndex++);

                ProductDetail detail = productDetailRepository
                        .findByProductID(product.getProductID())
                        .orElse(null);

                setCell(row, 0, product.getProductID());
                setCell(row, 1, product.getName());
                setCell(row, 2, product.getImage());
                setCell(row, 3, product.getPrice());
                setCell(row, 4, product.getPromotionPrice());
                setCell(row, 5, product.getVat());
                setCell(row, 6, product.getQuantity());
                setCell(row, 7, product.getWarranty());
                setCell(row, 8, Boolean.TRUE.equals(product.getIsHot()) ? "TRUE" : "FALSE");
                setCell(row, 9, product.getStatus());
                setCell(row, 10, product.getCategoryID());
                setCell(row, 11, product.getBrandID());
                setCell(row, 12, product.getSupplierID());
                setCell(row, 13, product.getDescription());

                if (detail != null) {
                    setCell(row, 14, detail.getRam());
                    setCell(row, 15, detail.getStorage());
                    setCell(row, 16, detail.getCpu());
                    setCell(row, 17, detail.getScreen());
                    setCell(row, 18, detail.getBattery());
                    setCell(row, 19, detail.getCamera());
                    setCell(row, 20, detail.getOs());
                    setCell(row, 21, detail.getChargingSpeed());
                    setCell(row, 22, detail.getConnectivity());
                }
            }

            for (int i = 0; i < exportHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Xuất Excel sản phẩm thất bại: " + e.getMessage());
        }
    }

    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Product Import Template");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sample = sheet.createRow(1);
            setCell(sample, 0, "iPhone 17 Pro Max 256GB");
            setCell(sample, 1, "/img/phones/iphone/iphone-17-pro-max.webp");
            setCell(sample, 2, 34990000);
            setCell(sample, 3, 32990000);
            setCell(sample, 4, 10);
            setCell(sample, 5, 12);
            setCell(sample, 6, "TRUE");
            setCell(sample, 7, 1);
            setCell(sample, 8, 1);
            setCell(sample, 9, 2);
            setCell(sample, 10, 1);
            setCell(sample, 11, "Sản phẩm nhập từ file Excel");
            setCell(sample, 12, "8GB");
            setCell(sample, 13, "256GB");
            setCell(sample, 14, "Apple A19 Pro");
            setCell(sample, 15, "6.9 inch OLED");
            setCell(sample, 16, "4500mAh");
            setCell(sample, 17, "48MP + 12MP");
            setCell(sample, 18, "iOS 19");
            setCell(sample, 19, "35W");
            setCell(sample, 20, "5G, Wi-Fi 7, Bluetooth 5.4");

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BadRequestException("Tạo file mẫu Excel thất bại: " + e.getMessage());
        }
    }

public String importProducts(MultipartFile file) {
    if (file == null || file.isEmpty()) {
        throw new BadRequestException("Vui lòng chọn file Excel.");
    }

    String filename = file.getOriginalFilename();

    if (filename == null ||
            (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
        throw new BadRequestException("File không hợp lệ. Vui lòng chọn file .xlsx hoặc .xls.");
    }

    int successCount = 0;
    List<String> errors = new ArrayList<>();

    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);

        if (sheet == null || sheet.getLastRowNum() < 1) {
            throw new BadRequestException("File Excel không có dữ liệu.");
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null || isEmptyRow(row)) {
                continue;
            }

            try {
                ProductRequest request = buildRequestFromRow(row, i + 1);
                productService.create(request);
                successCount++;
            } catch (BadRequestException | ResourceNotFoundException e) {
                errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
            } catch (Exception e) {
                errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
            }
        }

    } catch (BadRequestException e) {
        throw e;
    } catch (Exception e) {
        throw new BadRequestException("Đọc file Excel thất bại: " + e.getMessage());
    }

    if (!errors.isEmpty()) {
        return "Nhập thành công " + successCount + " sản phẩm. Có "
                + errors.size() + " dòng lỗi: " + String.join(" | ", errors);
    }

    return "Nhập Excel thành công " + successCount + " sản phẩm.";
}

    private ProductRequest buildRequestFromRow(Row row, int rowNumber) {
        ProductRequest request = new ProductRequest();

        String name = getString(row, 0);
        String image = getString(row, 1);
        BigDecimal price = getBigDecimal(row, 2);
        BigDecimal promotionPrice = getBigDecimal(row, 3);
        BigDecimal vat = getBigDecimal(row, 4);
        Integer warranty = getInteger(row, 5);
        Boolean isHot = getBoolean(row, 6);
        Integer status = getInteger(row, 7);
        Integer categoryID = getInteger(row, 8);
        Integer brandID = getInteger(row, 9);
        Integer supplierID = getInteger(row, 10);
        String description = getString(row, 11);

        String ram = getString(row, 12);
        String storage = getString(row, 13);
        String cpu = getString(row, 14);
        String screen = getString(row, 15);
        String battery = getString(row, 16);
        String camera = getString(row, 17);
        String os = getString(row, 18);
        String chargingSpeed = getString(row, 19);
        String connectivity = getString(row, 20);

        if (isBlank(name)) {
            throw new BadRequestException("Tên sản phẩm không được để trống.");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá bán phải lớn hơn 0.");
        }

        if (status == null) {
            status = Product.STATUS_DANG_BAN;
        }

        if (status != Product.STATUS_DANG_BAN && status != Product.STATUS_NGUNG_BAN) {
            throw new BadRequestException("Trạng thái chỉ được nhập 1 hoặc 0.");
        }

        if (categoryID == null || !categoryRepository.existsById(categoryID)) {
            throw new ResourceNotFoundException("categoryID không tồn tại: " + categoryID);
        }

        if (brandID != null && !brandRepository.existsById(brandID)) {
            throw new ResourceNotFoundException("brandID không tồn tại: " + brandID);
        }

        if (supplierID == null || !supplierRepository.existsById(supplierID)) {
            throw new ResourceNotFoundException("supplierID không tồn tại: " + supplierID);
        }

        validateRequiredDetail(ram, "RAM");
        validateRequiredDetail(storage, "Bộ nhớ");
        validateRequiredDetail(cpu, "CPU");
        validateRequiredDetail(screen, "Màn hình");
        validateRequiredDetail(battery, "Pin");
        validateRequiredDetail(camera, "Camera");
        validateRequiredDetail(os, "Hệ điều hành");
        validateRequiredDetail(chargingSpeed, "Sạc nhanh");
        validateRequiredDetail(connectivity, "Kết nối");

        ProductDetailRequest detail = new ProductDetailRequest();
        detail.setRam(ram);
        detail.setStorage(storage);
        detail.setCpu(cpu);
        detail.setScreen(screen);
        detail.setBattery(battery);
        detail.setCamera(camera);
        detail.setOs(os);
        detail.setChargingSpeed(chargingSpeed);
        detail.setConnectivity(connectivity);

        request.setName(name);
        request.setImage(image);
        request.setPrice(price);
        request.setPromotionPrice(promotionPrice);
        request.setVat(vat);
        request.setQuantity(0);
        request.setWarranty(warranty);
        request.setIsHot(isHot);
        request.setStatus(status);
        request.setCategoryID(categoryID);
        request.setBrandID(brandID);
        request.setSupplierID(supplierID);
        request.setDescription(description);
        request.setDetail(detail);

        return request;
    }

    private void validateRequiredDetail(String value, String fieldName) {
        if (isBlank(value)) {
            throw new BadRequestException(fieldName + " không được để trống.");
        }
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getString(row, i).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
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

        if (value instanceof BigDecimal bigDecimal) {
            cell.setCellValue(bigDecimal.doubleValue());
            return;
        }

        cell.setCellValue(String.valueOf(value));
    }

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
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
            throw new BadRequestException("Cột " + HEADERS[index] + " phải là số.");
        }
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
            throw new BadRequestException("Cột " + HEADERS[index] + " phải là số nguyên.");
        }
    }

    private Boolean getBoolean(Row row, int index) {
        String value = getString(row, index);

        if (isBlank(value)) {
            return false;
        }

        return value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("có")
                || value.equals("1");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}