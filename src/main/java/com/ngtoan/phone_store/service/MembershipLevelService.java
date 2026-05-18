package com.ngtoan.phone_store.service;

import com.ngtoan.phone_store.dto.request.MembershipLevelRequest;
import com.ngtoan.phone_store.dto.response.MembershipLevelResponse;
import com.ngtoan.phone_store.dto.response.UserMembershipResponse;
import com.ngtoan.phone_store.entity.MembershipHistory;
import com.ngtoan.phone_store.entity.MembershipLevel;
import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.exception.BadRequestException;
import com.ngtoan.phone_store.exception.ResourceNotFoundException;
import com.ngtoan.phone_store.mapper.MembershipLevelMapper;
import com.ngtoan.phone_store.repository.MembershipHistoryRepository;
import com.ngtoan.phone_store.repository.MembershipLevelRepository;
import com.ngtoan.phone_store.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipLevelService {

    private static final Set<String> DEFAULT_LEVEL_NAMES = Set.of(
            "Đồng",
            "Bạc",
            "Vàng",
            "Kim Cương"
    );

    private final MembershipLevelRepository membershipLevelRepository;
    private final UserRepository userRepository;
    private final MembershipLevelMapper membershipLevelMapper;
    private final MembershipHistoryRepository membershipHistoryRepository;

    // USER / ADMIN - Lấy danh sách cấp độ chưa bị xóa mềm
    public List<MembershipLevelResponse> getAllLevels() {
        return membershipLevelRepository.findAllVisibleLevels()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ADMIN - Thêm cấp độ
    public MembershipLevelResponse createLevel(MembershipLevelRequest request) {

        if (membershipLevelRepository.existsVisibleByLevelNameIgnoreCase(
                request.getLevelName()
        )) {
            throw new BadRequestException("Level name already exists");
        }

        if (membershipLevelRepository.existsVisibleByMinSpent(
                request.getMinSpent()
        )) {
            throw new BadRequestException("Min spent already exists");
        }

        MembershipLevel level = MembershipLevel.builder()
                .levelName(request.getLevelName().trim())
                .discountPercent(request.getDiscountPercent())
                .minSpent(request.getMinSpent())
                .isDefault(false)
                .isDeleted(false)
                .build();

        membershipLevelRepository.save(level);

        // Không lưu MembershipHistory ở đây để tránh tạo lịch sử giả hàng loạt
        recalculateAllUsersMembership();

        return toResponse(level);
    }

    // ADMIN - Sửa cấp độ
    public MembershipLevelResponse updateLevel(Integer levelID, MembershipLevelRequest request) {

        MembershipLevel level = membershipLevelRepository.findVisibleById(levelID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership level not found with id: " + levelID
                ));

        if (membershipLevelRepository.existsVisibleByLevelNameIgnoreCaseAndLevelIDNot(
                request.getLevelName(),
                levelID
        )) {
            throw new BadRequestException("Level name already exists");
        }

        if (membershipLevelRepository.existsVisibleByMinSpentAndLevelIDNot(
                request.getMinSpent(),
                levelID
        )) {
            throw new BadRequestException("Min spent already exists");
        }

        level.setLevelName(request.getLevelName().trim());
        level.setDiscountPercent(request.getDiscountPercent());
        level.setMinSpent(request.getMinSpent());

        // Nếu là 4 hạng mặc định thì giữ isDefault = true
        if (DEFAULT_LEVEL_NAMES.contains(level.getLevelName())) {
            level.setIsDefault(true);
        }

        level.setIsDeleted(false);

        membershipLevelRepository.save(level);

        // Không lưu MembershipHistory ở đây để tránh spam lịch sử khi admin sửa mức chi tiêu
        recalculateAllUsersMembership();

        return toResponse(level);
    }

    // ADMIN - Xóa mềm cấp độ
    public void deleteLevel(Integer levelID) {

        MembershipLevel level = membershipLevelRepository.findVisibleById(levelID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership level not found with id: " + levelID
                ));

        if (Boolean.TRUE.equals(level.getIsDefault())
                || DEFAULT_LEVEL_NAMES.contains(level.getLevelName())) {
            throw new BadRequestException("Không thể xóa hạng thành viên mặc định.");
        }

        if (membershipLevelRepository.findAllVisibleLevels().size() <= 1) {
            throw new BadRequestException("Cannot delete the last membership level");
        }

        // User nào đang thuộc hạng bị xóa thì cho về null rồi tính lại hạng phù hợp
        List<User> usersUsingThisLevel = userRepository.findByLevelId(levelID);

        for (User user : usersUsingThisLevel) {
            user.setLevelId(null);
        }

        userRepository.saveAll(usersUsingThisLevel);

        // Đổi tên để admin có thể tạo lại hạng cùng tên sau này
        String deletedName = level.getLevelName() + "_deleted_" + level.getLevelID();

        level.setLevelName(deletedName);
        level.setIsDeleted(true);
        level.setIsDefault(false);

        membershipLevelRepository.save(level);

        // Không lưu MembershipHistory ở đây vì đây là tính lại do admin xóa hạng
        recalculateAllUsersMembership();
    }

    // USER - Xem cấp độ của chính mình
    public UserMembershipResponse getMyMembership(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // User chỉ xem hạng thì không nên tạo lịch sử lên hạng
        MembershipLevel level = updateUserMembershipLevelWithoutHistory(user.getUserId());

        BigDecimal totalSpent = getSafeTotalSpent(user);

        return toUserMembershipResponse(user, level, totalSpent);
    }

    // OrderService gọi hàm này sau khi admin cập nhật trạng thái order
    // Hàm này CÓ lưu lịch sử nếu user thật sự lên hạng
    public MembershipLevel updateUserMembershipLevel(Integer userID) {
        return updateUserMembershipLevelInternal(userID, true);
    }

    // Dùng cho trường hợp chỉ cần tính lại hạng nhưng KHÔNG lưu lịch sử
    public MembershipLevel updateUserMembershipLevelWithoutHistory(Integer userID) {
        return updateUserMembershipLevelInternal(userID, false);
    }

    private MembershipLevel updateUserMembershipLevelInternal(
            Integer userID,
            boolean saveHistory
    ) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userID
                ));

        BigDecimal totalSpent = getSafeTotalSpent(user);

        MembershipLevel bestLevel = findBestLevelByTotalSpent(totalSpent);

        Integer oldLevelId = user.getLevelId();
        Integer newLevelId = bestLevel.getLevelID();

        // Nếu hạng không đổi thì không cần save, không cần lưu history
        if (oldLevelId != null && oldLevelId.equals(newLevelId)) {
            return bestLevel;
        }

        boolean isUpgrade = isUpgradeLevel(oldLevelId, newLevelId);

        user.setLevelId(newLevelId);
        userRepository.save(user);

        if (saveHistory && isUpgrade) {
            MembershipHistory history = MembershipHistory.builder()
                    .userID(user.getUserId())
                    .fromLevelID(oldLevelId)
                    .toLevelID(newLevelId)
                    .build();

            membershipHistoryRepository.save(history);
        }

        return bestLevel;
    }

    // ADMIN - Tính lại level toàn bộ user dựa trên User.TotalSpent
    // Không lưu MembershipHistory ở đây để tránh dashboard bị đầy lịch sử giả
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

    // Chỉ xem là "lên hạng" nếu minSpent của hạng mới cao hơn hạng cũ
    private boolean isUpgradeLevel(Integer oldLevelId, Integer newLevelId) {

        if (oldLevelId == null || newLevelId == null) {
            return false;
        }

        if (oldLevelId.equals(newLevelId)) {
            return false;
        }

        MembershipLevel oldLevel = membershipLevelRepository.findVisibleById(oldLevelId)
                .orElse(null);

        MembershipLevel newLevel = membershipLevelRepository.findVisibleById(newLevelId)
                .orElse(null);

        if (oldLevel == null || newLevel == null) {
            return false;
        }

        return newLevel.getMinSpent().compareTo(oldLevel.getMinSpent()) > 0;
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

    private MembershipLevelResponse toResponse(MembershipLevel level) {
        MembershipLevelResponse response = membershipLevelMapper.toResponse(level);

        response.setIsDefault(Boolean.TRUE.equals(level.getIsDefault()));
        response.setIsDeleted(Boolean.TRUE.equals(level.getIsDeleted()));

        return response;
    }
}