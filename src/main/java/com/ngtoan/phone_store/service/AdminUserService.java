package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.UserUpdateRequest;
import com.ngtoan.phone_store.dto.response.UserAdminResponse;
import com.ngtoan.phone_store.dto.response.UserStatisticsResponse;
import com.ngtoan.phone_store.entity.MembershipLevel;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.mapper.UserMapper;
import com.ngtoan.phone_store.repository.MembershipLevelRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final MembershipLevelRepository membershipLevelRepository;
    private final UserMapper userMapper;

    // ADMIN - lấy tất cả user chưa bị xóa mềm
    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAllVisibleUsers()
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    // ADMIN - lấy chi tiết 1 user
    public UserAdminResponse getUserById(Integer id) {
        User user = getUserEntityById(id);

        return toAdminResponse(user);
    }

    // ADMIN - tìm user theo keyword, chỉ lấy user chưa bị xóa mềm
    public List<UserAdminResponse> searchUsers(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }

        return userRepository.searchVisibleUsers(keyword.trim())
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    // ADMIN - cập nhật user
    public UserAdminResponse updateUser(Integer id, UserUpdateRequest dto) {

        User user = getUserEntityById(id);

        // Mapper có NullValuePropertyMappingStrategy.IGNORE
        // Field nào null thì không ghi đè dữ liệu cũ
        userMapper.updateUser(user, dto);

        if (dto.getRoleId() != null) {
            user.setRoleId(dto.getRoleId());
        }

        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        userRepository.save(user);

        return toAdminResponse(user);
    }

    // ADMIN - xóa mềm user
    // Không xóa khỏi database để tránh lỗi khóa ngoại với Order, Feedback, MembershipHistory...
    public void deleteUser(Integer id) {
        User user = getUserEntityById(id);

        // Không cho xóa chính tài khoản admin gốc nếu bạn muốn bảo vệ tài khoản đầu tiên
        // Nếu không cần rule này thì có thể xóa đoạn if này.
        if (user.getRoleId() == 1 && user.getUserId() == 1) {
            throw new BadRequestException("Không thể xóa tài khoản admin mặc định.");
        }

        user.setDeleted(true);
        user.setStatus(false);

        userRepository.save(user);
    }

    // ADMIN - thống kê user chưa bị xóa mềm
    public UserStatisticsResponse getStatistics() {

        List<User> users = userRepository.findAllVisibleUsers();

        UserStatisticsResponse response = new UserStatisticsResponse();

        response.setTotalUsers(users.size());

        response.setTotalAdmins(
                users.stream()
                        .filter(user -> user.getRoleId() == 1)
                        .count()
        );

        // VIP tính từ Bạc trở lên: LevelID >= 2
        // LevelID:
        // 1 Đồng, 2 Bạc, 3 Vàng, 4 Kim Cương
        response.setTotalVipUsers(
                users.stream()
                        .filter(user -> user.getLevelId() != null && user.getLevelId() >= 2)
                        .count()
        );

        response.setTotalLockedUsers(
                users.stream()
                        .filter(user -> Boolean.FALSE.equals(user.getStatus()))
                        .count()
        );

        return response;
    }

    private User getUserEntityById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + id
            );
        }

        return user;
    }

    private UserAdminResponse toAdminResponse(User user) {

        UserAdminResponse response = new UserAdminResponse();

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());

        response.setRoleId(user.getRoleId());
        response.setRoleName(getRoleName(user.getRoleId()));

        response.setLevelId(user.getLevelId());

        MembershipLevel level = null;

        if (user.getLevelId() != null) {
            level = membershipLevelRepository
                    .findById(user.getLevelId())
                    .orElse(null);
        }

        if (level != null) {
            response.setLevelName(level.getLevelName());
            response.setDiscountPercent(level.getDiscountPercent());
            response.setMinSpent(level.getMinSpent());
        } else {
            response.setLevelName("Đồng");
            response.setDiscountPercent(BigDecimal.ZERO);
            response.setMinSpent(BigDecimal.ZERO);
        }

        response.setTotalSpent(
                user.getTotalSpent() == null
                        ? BigDecimal.ZERO
                        : user.getTotalSpent()
        );

        response.setStatus(user.getStatus());
        response.setStatusName(
                Boolean.TRUE.equals(user.getStatus())
                        ? "Hoạt động"
                        : "Tạm khóa"
        );

        response.setDeleted(Boolean.TRUE.equals(user.getDeleted()));
        response.setCreatedDate(user.getCreatedDate());

        return response;
    }

    private String getRoleName(Integer roleId) {
        if (roleId == null) {
            return "Không rõ";
        }

        return switch (roleId) {
            case 1 -> "ADMIN";
            case 2 -> "USER";
            default -> "Không rõ";
        };
    }
}