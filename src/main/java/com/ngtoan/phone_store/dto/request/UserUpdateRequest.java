package com.ngtoan.phone_store.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    String fullName;

    // Giữ field email để admin vẫn có thể dùng DTO này nếu cần.
    // Riêng API user tự cập nhật profile sẽ bỏ qua email trong UserService.
    @Email(message = "Email không hợp lệ")
    String email;

    @Pattern(
            regexp = "^(03|05|07|08|09)\\d{8}$",
            message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09"
    )
    String phone;

    String gender;

    // Tên field cũ đang khớp database/entity: BirthDate
    @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
    LocalDate birthDate;

    // Tên field mới frontend đang gửi: birthday
    @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
    LocalDate birthday;

    // Tên field cũ đang khớp database/entity: Address
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    String address;

    // Tên field mới frontend đang gửi: defaultAddress
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    String defaultAddress;

    // Dành cho admin
    Integer roleId;
    Boolean status;
}
