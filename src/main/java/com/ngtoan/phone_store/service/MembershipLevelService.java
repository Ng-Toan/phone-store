package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.MembershipLevelRequest;
import com.ngtoan.phone_store.dto.response.MembershipLevelResponse;
import com.ngtoan.phone_store.mapper.MembershipLevelMapper;
import com.ngtoan.phone_store.dto.response.UserMembershipResponse;
import com.ngtoan.phone_store.entity.MembershipLevel;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.repository.MembershipLevelRepository;
import com.ngtoan.phone_store.repository.UserRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipLevelService {

    private final MembershipLevelRepository membershipLevelRepository;
    private final UserRepository userRepository;
    private final MembershipLevelMapper membershipLevelMapper;

    // USER / ADMIN - Lấy danh sách cấp độ
    public List<MembershipLevelResponse> getAllLevels() {
    return membershipLevelRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(MembershipLevel::getMinSpent))
            .map(membershipLevelMapper::toResponse)
            .toList();
}

    // ADMIN - Thêm cấp độ
    public MembershipLevelResponse createLevel(MembershipLevelRequest request) {

        if (membershipLevelRepository.existsByLevelNameIgnoreCase(request.getLevelName())) {
            throw new BadRequestException("Level name already exists");
        }

        if (membershipLevelRepository.existsByMinSpent(request.getMinSpent())) {
            throw new BadRequestException("Min spent already exists");
        }

        MembershipLevel level = MembershipLevel.builder()
                .levelName(request.getLevelName())
                .discountPercent(request.getDiscountPercent())
                .minSpent(request.getMinSpent())
                .build();

        membershipLevelRepository.save(level);

        // Sau khi thêm level mới, có thể user nào đó đủ điều kiện lên level mới
        recalculateAllUsersMembership();

        return membershipLevelMapper.toResponse(level);
    }

    // ADMIN - Sửa cấp độ
    public MembershipLevelResponse updateLevel(Integer levelID, MembershipLevelRequest request) {

        MembershipLevel level = membershipLevelRepository.findById(levelID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership level not found with id: " + levelID
                ));

        if (membershipLevelRepository.existsByLevelNameIgnoreCaseAndLevelIDNot(
                request.getLevelName(),
                levelID
        )) {
            throw new BadRequestException("Level name already exists");
        }

        if (membershipLevelRepository.existsByMinSpentAndLevelIDNot(
                request.getMinSpent(),
                levelID
        )) {
            throw new BadRequestException("Min spent already exists");
        }

        level.setLevelName(request.getLevelName());
        level.setDiscountPercent(request.getDiscountPercent());
        level.setMinSpent(request.getMinSpent());

        membershipLevelRepository.save(level);

        // Nếu admin sửa MinSpent, level user có thể thay đổi
        recalculateAllUsersMembership();

        return membershipLevelMapper.toResponse(level);
    }

    // ADMIN - Xóa cấp độ
    public void deleteLevel(Integer levelID) {

        MembershipLevel level = membershipLevelRepository.findById(levelID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership level not found with id: " + levelID
                ));

        if (membershipLevelRepository.count() <= 1) {
            throw new BadRequestException("Cannot delete the last membership level");
        }

        // Nếu user đang dùng level này, set null trước để tránh lỗi khóa ngoại
        List<User> usersUsingThisLevel = userRepository.findByLevelId(levelID);

        for (User user : usersUsingThisLevel) {
            user.setLevelId(null);
        }

        userRepository.saveAll(usersUsingThisLevel);

        membershipLevelRepository.delete(level);

        // Sau khi xóa level, tính lại level phù hợp cho toàn bộ user
        recalculateAllUsersMembership();
    }

    // USER - Xem cấp độ của chính mình
    public UserMembershipResponse getMyMembership(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        MembershipLevel level = updateUserMembershipLevel(user.getUserId());

        BigDecimal totalSpent = getSafeTotalSpent(user);

        return toUserMembershipResponse(user, level, totalSpent);
    }

    // OrderService gọi hàm này sau khi admin cập nhật trạng thái order
    public MembershipLevel updateUserMembershipLevel(Integer userID) {

        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userID
                ));

        BigDecimal totalSpent = getSafeTotalSpent(user);

        MembershipLevel bestLevel = findBestLevelByTotalSpent(totalSpent);

        user.setLevelId(bestLevel.getLevelID());

        userRepository.save(user);

        return bestLevel;
    }

    // ADMIN - Tính lại level toàn bộ user dựa trên User.TotalSpent
    public void recalculateAllUsersMembership() {

        List<User> users = userRepository.findAll();

        for (User user : users) {
            BigDecimal totalSpent = getSafeTotalSpent(user);

            MembershipLevel bestLevel = findBestLevelByTotalSpent(totalSpent);

            user.setLevelId(bestLevel.getLevelID());

            userRepository.save(user);
        }
    }

    // Lấy TotalSpent an toàn, nếu null thì set về 0
    private BigDecimal getSafeTotalSpent(User user) {

        if (user.getTotalSpent() == null) {
            user.setTotalSpent(BigDecimal.ZERO);
            userRepository.save(user);
            return BigDecimal.ZERO;
        }

        return user.getTotalSpent();
    }

    // Tìm level cao nhất phù hợp với totalSpent
    private MembershipLevel findBestLevelByTotalSpent(BigDecimal totalSpent) {

        return membershipLevelRepository
                .findTopByMinSpentLessThanEqualOrderByMinSpentDesc(totalSpent)
                .orElseGet(() -> membershipLevelRepository.findTopByOrderByMinSpentAsc()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No membership level found"
                        )));
    }

    private UserMembershipResponse toUserMembershipResponse(
            User user,
            MembershipLevel level,
            BigDecimal totalSpent
    ) {
        UserMembershipResponse response = new UserMembershipResponse();

        response.setUserID(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());

        response.setTotalSpent(totalSpent);

        response.setLevelID(level.getLevelID());
        response.setLevelName(level.getLevelName());
        response.setDiscountPercent(level.getDiscountPercent());
        response.setMinSpent(level.getMinSpent());

        return response;
    }
}